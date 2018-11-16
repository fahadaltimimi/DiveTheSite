package com.fahadaltimimi.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class JSONParser {

	static InputStream is = null;

	private Context mContext;

	private Boolean mCancelled = false;

	private JSONParserListener mJSONParserListener;

	public interface JSONParserListener {
		void onJSONParserObjectAvailable(JSONObject json);
	}

	// constructor
	public JSONParser(Context context) {
		mContext = context;
	}

	public void setJSONParserListener(JSONParserListener listen) {
		mJSONParserListener = listen;
	}

    public JSONObject makeHttpRequest(String urlString, String method, ContentValues params) throws JSONException {

        // Making HTTP request
        try {
            System.setProperty("http.keepAlive", "false");

            // check for request method
            if (method == "POST") {
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                is = urlConnection.getInputStream();

            } else if (method == "GET") {
                String paramString = getQuery(params);
                urlString += "?" + paramString;

                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

                is = urlConnection.getInputStream();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = "";
        JSONObject jObj = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null && !mCancelled) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString().trim();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            throw e;
        }

        // return JSON String
        return jObj;

    }

    public void makeHttpRequestJSONLines(String urlString, String method,
                                         ContentValues params) throws Exception {

        // Making HTTP request
        try {

            // check for request method
            if (method == "POST") {
                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url
                        .openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                is = urlConnection.getInputStream();

            } else if (method == "GET") {
                String paramString = getQuery(params);
                urlString += "?" + paramString;

                URL url = new URL(urlString);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url
                        .openConnection();

                is = urlConnection.getInputStream();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = "";
        JSONObject jObj = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            String line;
            while ((line = reader.readLine()) != null && !mCancelled) {
                if (!line.trim().isEmpty() && mJSONParserListener != null) {

                    // try parse the string to a JSON object
                    try {
                        json = line.trim();
                        jObj = new JSONObject(json);

                        mJSONParserListener.onJSONParserObjectAvailable(jObj);

                    } catch (JSONException e) {
                        Log.e("JSON Parser",
                                "Error parsing data " + e.toString());
                    }
                }
            }
            is.close();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
            throw e;
        }
    }

    private String getQuery(ContentValues params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Object[] names = params.keySet().toArray();

        for (int i = 0; i < params.size(); i++) {
            if (first) {
                first = false;
            }
            else {
                result.append("&");
            }

            String name = (String)names[i];
            String value = params.getAsString(name);

            result.append(URLEncoder.encode(name, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value, "UTF-8"));
        }

        return result.toString();
    }

	public Boolean getCancelled() {
		return mCancelled;
	}

	public void setCancelled(Boolean cancelled) {
		mCancelled = cancelled;
	}

}