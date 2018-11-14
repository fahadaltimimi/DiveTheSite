package com.fahadaltimimi.divethesite.view;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;

public class RegisterActivity extends AppCompatActivity {

	private SharedPreferences mPrefs;

	private EditText mUsername, mEmail;
	private EditText mPassword, mConfirmPassword;
	private EditText mFirstName, mLastName;
	private Spinner mCountry;
	private EditText mCity, mProvince;
	private Button mRegisterSubmit;

	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);

		// Remove title bar before setting layout
        getSupportActionBar().hide();
		
		//Remove notification bar
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	    		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_register);

		mProgressDialog = new ProgressDialog(this);

		mUsername = findViewById(R.id.register_username_edit);
		mEmail = findViewById(R.id.register_email_edit);
		mPassword = findViewById(R.id.register_password_edit);
		mConfirmPassword = findViewById(R.id.register_confirm_password_edit);
		mFirstName = findViewById(R.id.register_firstname_edit);
		mLastName = findViewById(R.id.register_lastname_edit);
		mCountry = findViewById(R.id.register_country_edit);
		mCity = findViewById(R.id.register_city_edit);
		mProvince = findViewById(R.id.register_province_edit);

		mRegisterSubmit = findViewById(R.id.register_button);
		mRegisterSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				registerUser();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void registerUser() {
		String username = mUsername.getText().toString().trim();
		if (username.isEmpty()) {
			Toast.makeText(getApplicationContext(),
					R.string.register_blank_username, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		String email = mEmail.getText().toString().trim();
		if (email.isEmpty()) {
			Toast.makeText(getApplicationContext(),
					R.string.register_blank_email, Toast.LENGTH_SHORT).show();
			return;
		}

		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			Toast.makeText(getApplicationContext(),
					R.string.register_invalid_email, Toast.LENGTH_SHORT).show();
			return;
		}

		String password = mPassword.getText().toString();
		if (password.isEmpty()) {
			Toast.makeText(getApplicationContext(),
					R.string.register_blank_password, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (!password.equals(mConfirmPassword.getText().toString())) {
			Toast.makeText(getApplicationContext(),
					R.string.register_password_mismatch, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		String firstName = mFirstName.getText().toString().trim();
		String lastName = mLastName.getText().toString().trim();
		String country = mCountry.getSelectedItem().toString();
		String province = mProvince.getText().toString().trim();
		String city = mCity.getText().toString().trim();

		// Try to add user
		mProgressDialog.setMessage(getString(R.string.register_progress));
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
						if (resultList.size() > 0) {
							// User successfully added, display toast for user
							// to check their email and go back to login
							// activity
							Toast.makeText(RegisterActivity.this,
									R.string.register_check_email,
									Toast.LENGTH_LONG).show();

							Intent i = new Intent(RegisterActivity.this,
									LoginActivity.class);
							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(i);
						} else {
							// Error adding user, display error
							Toast.makeText(getApplicationContext(), message,
									Toast.LENGTH_LONG).show();
						}

						mProgressDialog.dismiss();
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

		// Set approved to blank, will be set to a key in php script requiring
		// user to confirm registration
		diveSiteOnlineDatabase.createUser(username, password, email, firstName,
				lastName, country, province, city, "", "");
	}
}
