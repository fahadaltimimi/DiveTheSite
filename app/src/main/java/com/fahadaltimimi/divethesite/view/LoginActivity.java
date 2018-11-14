package com.fahadaltimimi.divethesite.view;

import java.util.ArrayList;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.model.GoogleUserAuthentication;
import com.fahadaltimimi.model.GoogleUserAuthentication.UserAuthenticationListener;
import com.fahadaltimimi.divethesite.model.Diver;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LoginActivity extends AppCompatActivity {

	private static final int REGISTER_USER = 0;

	private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	private static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
	private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

	public static final String EXTRA_ACCOUNTNAME = "extra_accountname";

	private SharedPreferences mPrefs;

	private EditText mUsername, mPassword;
	private Button mLogin, mSkipLogin, mRegister;
	private ImageButton mGoogleLogin;
	private ProgressDialog mProgressDialog;

	private String mEmail = "";
	private String mFirstName = "";
	private String mPictureURL = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mProgressDialog = new ProgressDialog(this);
		mPrefs = getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);

		// Check if a user id is already saved, move on if so
		long diver_id = mPrefs
				.getLong(DiveSiteManager.PREF_CURRENT_USER_ID, -1);
		String diver_username = mPrefs.getString(
				DiveSiteManager.PREF_CURRENT_USERNAME, "");
		if (diver_id != -1 && diver_username != "") {
			loginComplete(diver_id, diver_username);
		} else {
			// Remove title bar before setting layout
            getSupportActionBar().hide();
			
			//Remove notification bar
		    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
		    		WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
		    setContentView(R.layout.activity_login);

			mUsername = findViewById(R.id.login_username_edit);
			mPassword = findViewById(R.id.login_password_edit);

			mLogin = findViewById(R.id.login_button);
			mLogin.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					userLogin(mUsername.getText().toString(), mPassword
							.getText().toString());
				}
			});

			mRegister = findViewById(R.id.register_button);
			mRegister.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent(LoginActivity.this,
							RegisterActivity.class);
					startActivityForResult(i, REGISTER_USER);
				}
			});
			
			mSkipLogin = findViewById(R.id.skip_login_button);
			mSkipLogin.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					loginComplete(-1, "");
				}
			});

			mGoogleLogin = findViewById(R.id.login_google_button);
			mGoogleLogin.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					pickGoogleAccount();
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void userLogin(String username, String password) {
		// Create and initialize progress dialog
		mProgressDialog.setMessage(getString(R.string.login_progress));
		mProgressDialog.setCancelable(false);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.show();

		DiveSiteOnlineDatabaseLink diveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(
				this);
		diveSiteOnlineDatabase
				.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {
						if (resultList.size() == 0) {
							// Invalid login, no user retrieved
							mProgressDialog.dismiss();
							if (message.isEmpty()) {
								Toast.makeText(getApplicationContext(),
										R.string.login_invalid,
										Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(getApplicationContext(),
										message, Toast.LENGTH_LONG).show();
							}
						} else {
							// Valid login, save user id and proceed to next
							// activity
							loginComplete(
									((Diver) resultList.get(0)).getOnlineId(),
									((Diver) resultList.get(0)).getUsername());
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						// TODO Auto-generated method stub

					}
				});

		diveSiteOnlineDatabase.checkUserLogin(username, password);
	}

	/**
	 * Starts an activity in Google Play Services so the user can pick an
	 * account
	 */
	private void pickGoogleAccount() {
		String[] accountTypes = new String[] { "com.google" };
		Intent intent = AccountPicker.newChooseAccountIntent(null, null,
				accountTypes, false, null, null, null, null);
		startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
	}

	/** Checks whether the device currently has a network connection */
	private boolean isDeviceOnline() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
			if (resultCode == RESULT_OK) {
				mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				authenticateSelectedAccount();
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "You must pick an account",
						Toast.LENGTH_SHORT).show();
			}
		} else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR || requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
				&& resultCode == RESULT_OK) {
			handleAuthorizeResult(resultCode, data);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void handleAuthorizeResult(int resultCode, Intent data) {
		if (data == null) {
			Toast.makeText(this, "Unknown error, try to login again.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (resultCode == RESULT_OK) {
			authenticateSelectedAccount();
			return;
		}
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Registration cancelled.", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Toast.makeText(this, "Unknown error, try to login again.",
				Toast.LENGTH_SHORT).show();
	}

	private void authenticateSelectedAccount() {
		if (isDeviceOnline()) {
			mProgressDialog.setMessage(getString(R.string.login_progress));
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.show();

			// Authenticate User
			GoogleUserAuthentication googleUserAuthentication = new GoogleUserAuthentication(
					this, mEmail);
			googleUserAuthentication
					.setUserAuthenticationListener(new UserAuthenticationListener() {

						@Override
						public void onUserAuthenticationComplete(String email,
								String firstName, String picture,
								String errorMessage) {
							// User authentication complete
							// If no errors received, check if user exists
							// If user exists, login. Otherwise, ask for
							// username & password, register user, then login
							if (!errorMessage.isEmpty()) {
								mProgressDialog.dismiss();
								Toast.makeText(LoginActivity.this,
										errorMessage, Toast.LENGTH_SHORT)
										.show();
							} else {
								mFirstName = firstName;
								mPictureURL = picture;
								getOrRegisterAuthorizedUser();
							}
						}
					});
			googleUserAuthentication.execute();

		} else {
			Toast.makeText(this, "No network connection available",
					Toast.LENGTH_SHORT).show();
		}
	}

	// Try to get user, if doesn't exist then register
	private void getOrRegisterAuthorizedUser() {
		DiveSiteOnlineDatabaseLink diveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(
				this);
		diveSiteOnlineDatabase
				.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {
						if (resultList.size() == 0) {
							// No user returned, ask for password and insert new
							// user
							LayoutInflater layoutInflater = (LayoutInflater) LoginActivity.this
									.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							final View usernamePasswordConfirmationView = layoutInflater
									.inflate(R.layout.username_password_confirmation, null);

							new AlertDialog.Builder(LoginActivity.this)
									.setTitle(R.string.login_create_account)
									.setView(usernamePasswordConfirmationView)
									.setNegativeButton(
											R.string.cancel,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													// User cancelled, don't
													// proceed
													mProgressDialog.dismiss();
												}
											})
									.setPositiveButton(
											R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													// Check that password
													// entered matches
													// confirmation
													EditText usernameEntered = (EditText) usernamePasswordConfirmationView
															.findViewById(R.id.usernameEntered);
													EditText passwordEntered = (EditText) usernamePasswordConfirmationView
															.findViewById(R.id.passwordEntered);
													EditText confirmPasswordEntered = (EditText) usernamePasswordConfirmationView
															.findViewById(R.id.confirmPasswordEntered);

													if (passwordEntered
															.getText()
															.toString()
															.equals(confirmPasswordEntered
																	.getText()
																	.toString())) {
														// Create user with entered username and password
														insertAuthorizedUser(
																usernameEntered.getText().toString(),
																passwordEntered.getText().toString());
													} else {
														mProgressDialog
																.dismiss();
														Toast.makeText(
																getApplicationContext(),
																getResources()
																		.getString(
																				R.string.register_password_mismatch),
																Toast.LENGTH_LONG)
																.show();
													}
												}
											}).show();
						} else {
							// User returned, save id and proceed
							loginComplete(
									((Diver) resultList.get(0)).getOnlineId(),
									((Diver) resultList.get(0)).getUsername());
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						// TODO Auto-generated method stub

					}
				});

		diveSiteOnlineDatabase.getUser("", "", mEmail);
	}

	private void insertAuthorizedUser(String username, String password) {
		DiveSiteOnlineDatabaseLink diveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(
				LoginActivity.this);
		diveSiteOnlineDatabase
				.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {
						if (resultList.size() > 0) {
							loginComplete(
									((Diver) resultList.get(0)).getOnlineId(),
									((Diver) resultList.get(0)).getUsername());
						} else {
							// Error while adding user, display error
							mProgressDialog.dismiss();
							Toast.makeText(getApplicationContext(), message,
									Toast.LENGTH_LONG).show();
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						// TODO Auto-generated method stub

					}
				});

		// Insert user as approved right away since they used their google
		// account to register
		diveSiteOnlineDatabase.createUser(username, password, mEmail, mFirstName, "",
				"", "", "", mPictureURL, "1");
	}

	private void loginComplete(long userId, String username) {
		mProgressDialog.dismiss();

		mPrefs.edit().putLong(DiveSiteManager.PREF_CURRENT_USER_ID, userId)
				.commit();
		mPrefs.edit()
				.putString(DiveSiteManager.PREF_CURRENT_USERNAME, username)
				.commit();

		Intent i = new Intent(LoginActivity.this, HomeActivity.class);
		
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(i);
	}

	/**
	 * This method is a hook for background threads and async tasks that need to
	 * provide the user a response UI when an exception occurs.
	 */
	public void handleException(final Exception e) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (e instanceof GooglePlayServicesAvailabilityException) {
					// The Google Play services APK is old, disabled, or not
					// present.
					// Show a dialog created by Google Play services that allows
					// the user to update the APK
					int statusCode = ((GooglePlayServicesAvailabilityException) e)
							.getConnectionStatusCode();
					Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
							statusCode, LoginActivity.this,
							REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
					dialog.show();
				} else if (e instanceof UserRecoverableAuthException) {
					// Unable to authenticate, such as when the user has not yet
					// granted
					// the app access to the account, but the user can fix this.
					// Forward the user to an activity in Google Play services.
					Intent intent = ((UserRecoverableAuthException) e)
							.getIntent();
					startActivityForResult(intent,
							REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
				}
			}
		});
	}

}
