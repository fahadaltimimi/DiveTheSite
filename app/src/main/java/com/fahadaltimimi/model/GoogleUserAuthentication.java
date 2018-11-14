package com.fahadaltimimi.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.fahadaltimimi.divethesite.view.LoginActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;

public class GoogleUserAuthentication extends AsyncTask<Void, Void, Boolean> {
	private static final String TAG = "TokenInfoTask";
	private static final String NAME_KEY = "given_name";
	private static final String PICTURE_KEY = "picture";

	private static final String EXTRA_ACCOUNTNAME = "extra_accountname";
	private static String TYPE_KEY = "type_key";
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	private static final String BACKGROUND = "BACKGROUND";

	private LoginActivity mActivity;
	private String mEmail;

	private String mFirstName = "";
	private String mPictureURL = "";
	private String mErrorMessage = "";

	// User Authentication Listeners
	private UserAuthenticationListener mOnUserAuthenticationComplete;

	public interface UserAuthenticationListener {
		void onUserAuthenticationComplete(String email, String firstName,
				String pictureURL, String errorMessage);
	}

	public GoogleUserAuthentication(LoginActivity activity, String email) {
		mActivity = activity;
		mEmail = email;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	public void setUserAuthenticationListener(UserAuthenticationListener listen) {
		mOnUserAuthenticationComplete = listen;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			fetchNameFromProfileServer();
		} catch (IOException ex) {
			onError("Following Error occured, please try again. "
					+ ex.getMessage(), ex);
			return false;
		} catch (JSONException e) {
			onError("Bad response: " + e.getMessage(), e);
			return false;
		}
		return true;
	}

	protected void onError(String msg, Exception e) {
		if (e != null) {
			Log.e(TAG, "Exception: ", e);
		}
		mErrorMessage = msg;
	}

	/**
	 * Get a authentication token if one is not available. If the error was
	 * recoverable then a notification will automatically be pushed. The
	 * callback provided will be fired once the notification is addressed by the
	 * user successfully. If the error is not recoverable then it displays the
	 * error message on parent activity.
	 */
	/*
	 * protected String fetchToken() throws IOException { try { return
	 * GoogleAuthUtil.getToken(mActivity, mEmail, SCOPE, null); } catch
	 * (UserRecoverableNotifiedException userRecoverableException) { // Unable
	 * to authenticate, but the user can fix this. // Because we've used
	 * getTokenWithNotification(), a Notification is // created automatically so
	 * the user can recover from the error onError("Could not fetch token.",
	 * null); } catch (GoogleAuthException fatalException) {
	 * onError("Unrecoverable error " + fatalException.getMessage(),
	 * fatalException); } return null; }
	 */

	protected String fetchToken() throws IOException {
		try {
			return GoogleAuthUtil.getTokenWithNotification(mActivity, mEmail,
					SCOPE, null, makeCallback(mEmail));
		} catch (UserRecoverableNotifiedException userRecoverableException) {
			// Unable to authenticate, but the user can fix this.
			// Because we've used getTokenWithNotification(), a Notification is
			// created automatically so the user can recover from the error
			onError("Check your notification bar for a Google login message.", null);
		} catch (GoogleAuthException fatalException) {
			onError("Unrecoverable error " + fatalException.getMessage(),
					fatalException);
		}
		return null;
	}

	private Intent makeCallback(String accountName) {
		Intent intent = new Intent();
		intent.setAction("com.google.android.gms.auth.D.Callback");
		intent.putExtra(EXTRA_ACCOUNTNAME, accountName);
		intent.putExtra(TYPE_KEY, BACKGROUND);
		return intent;
	}

	/**
	 * Contacts the user info server to get the profile of the user and extracts
	 * the first name of the user from the profile. In order to authenticate
	 * with the user info server the method first fetches an access token from
	 * Google Play services.
	 * 
	 * @throws IOException
	 *             if communication with user info server failed.
	 * @throws JSONException
	 *             if the response from the server could not be parsed.
	 */
	private void fetchNameFromProfileServer() throws IOException, JSONException {
		String token = fetchToken();
		if (token == null) {
			// error has already been handled in fetchToken()
			return;
		}
		URL url = new URL(
				"https://www.googleapis.com/oauth2/v1/userinfo?access_token="
						+ token);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		int sc = con.getResponseCode();
		if (sc == 200) {
			InputStream is = con.getInputStream();

			JSONObject profileResponse = new JSONObject(readResponse(is));
			mFirstName = profileResponse.getString(NAME_KEY);
			mPictureURL = profileResponse.getString(PICTURE_KEY);

		} else {
			onError("Server returned the following error code: " + sc, null);
			return;
		}
	}

	public static Bitmap getBitmapFromURL(String link) {
		/*--- this method downloads an Image from the given URL, 
		 *  then decodes and returns a Bitmap object
		 ---*/
		try {
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);

			return myBitmap;

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("getBmpFromUrl error: ", e.getMessage().toString());
			return null;
		}
	}

	/**
	 * Reads the response from the input stream and returns it as a string.
	 */
	private static String readResponse(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] data = new byte[2048];
		int len = 0;
		while ((len = is.read(data, 0, data.length)) >= 0) {
			bos.write(data, 0, len);
		}
		return new String(bos.toByteArray(), "UTF-8");
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (mOnUserAuthenticationComplete != null) {
			mOnUserAuthenticationComplete.onUserAuthenticationComplete(mEmail,
					mFirstName, mPictureURL, mErrorMessage);
		}
	}
}
