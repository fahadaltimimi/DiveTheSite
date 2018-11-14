package com.fahadaltimimi.divethesite.controller;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.model.JSONParser;
import com.fahadaltimimi.model.JSONParser.JSONParserListener;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;
import com.fahadaltimimi.divethesite.model.DiveLogStop;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.DiveSitePicture;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.DiverCertification;
import com.fahadaltimimi.divethesite.model.NDBCStation;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.fahadaltimimi.divethesite.model.ScheduledDiveDiveSite;
import com.fahadaltimimi.divethesite.model.ScheduledDiveUser;
import com.fahadaltimimi.model.LoadFileImageTask;

public class DiveSiteOnlineDatabaseLink extends
		AsyncTask<String, Object, String> {

	private static final String URL_POST = "POST";
	private static final String URL_GET = "GET";

	private static final String JSON_TAG_SUCCESS = "SUCCESS";
	private static final String JSON_TAG_METEOROLOGICAL_DATA_SUCCESS = "METEOROLOGICAL_DATA_SUCCESS";
	private static final String JSON_TAG_SPECTRAL_WAVE_DATA_SUCCESS = "SPECTRAL_WAVE_DATA_SUCCESS";
	private static final String JSON_TAG_DRIFTING_BUOY_DATA_SUCCESS = "DRIFTING_BUOY_DATA_SUCCESS";
	private static final String JSON_TAG_OCEANIC_DATA_SUCCESS = "OCEANIC_DATA_SUCCESS";
	private static final String JSON_TAG_MESSAGE = "MESSAGE";
	private static final String JSON_TAG_DIVESITES = "DIVESITES";
	private static final String JSON_TAG_PICTURE_SITE = "PICTURE_DIVESITE_";
	private static final String JSON_TAG_DIVESITES_LOG = "DIVESITES_DIVELOG_";
	private static final String JSON_TAG_DIVELOGS = "DIVELOGS";
	private static final String JSON_TAG_DIVELOG_SITE = "DIVELOGS_DIVESITE_";
	private static final String JSON_TAG_BUDDY_LOG = "BUDDY_DIVELOG_";
	private static final String JSON_TAG_STOP_LOG = "STOP_DIVELOG_";
	private static final String JSON_TAG_DIVERS = "DIVERS";
	private static final String JSON_TAG_DIVER_CERTIFICATIONS_DIVER = "DIVER_CERTIFICATIONS_DIVER_";
	private static final String JSON_TAG_NDBC_STATIONS = "NDBC_STATIONS";
	private static final String JSON_TAG_METEOROLOGICAL_DATA = "METEOROLOGICAL_DATA";
	private static final String JSON_TAG_SPECTRAL_WAVE_DATA = "SPECTRAL_WAVE_DATA";
	private static final String JSON_TAG_DRIFTING_BUOY_DATA = "DRIFTING_BUOY_DATA";
	private static final String JSON_TAG_OCEANIC_DATA = "OCEANIC_DATA";
	private static final String JSON_TAG_SCHEDULEDDIVES = "SCHEDULEDDIVES";
	private static final String JSON_TAG_SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE = "SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE_";
	private static final String JSON_TAG_DIVESITES_SCHEDULEDDIVEDIVESITE = "DIVESITES_SCHEDULEDDIVEDIVESITE_";
	private static final String JSON_TAG_SCHEDULEDDIVEUSER_SCHEDULEDDIVE = "SCHEDULEDDIVEUSER_SCHEDULEDDIVE_";

	private static String url_get_divesite = "https://divethesite.com/app/get_divesite_JSON.php";
	private static String url_get_divesites = "https://divethesite.com/app/get_divesites_JSON.php";
	private static String url_publish_divesite = "https://divethesite.com/app/publish_divesite_JSON.php";
	private static String url_get_divelog = "https://divethesite.com/app/get_divelog_JSON.php";
	private static String url_get_divelogs = "https://divethesite.com/app/get_divelogs_JSON.php";
	private static String url_publish_divelog = "https://divethesite.com/app/publish_divelog_JSON.php";
	private static String url_check_login = "https://divethesite.com/app/check_login_JSON.php";
	private static String url_get_diver = "https://divethesite.com/app/get_user_JSON.php";
	private static String url_get_diver_picture = "https://divethesite.com/app/get_user_picture_JSON.php";
	private static String url_get_divers = "https://divethesite.com/app/get_users_JSON.php";
	private static String url_create_diver = "https://divethesite.com/app/create_user_JSON.php";
	private static String url_get_NDBC_stations = "https://divethesite.com/app/get_NDBC_Stations_JSON.php";
	private static String url_get_NDBC_data_for_station = "https://divethesite.com/app/get_NDBC_data_for_station_JSON.php";
	private static String url_save_user = "https://divethesite.com/app/save_user_JSON.php";
	private static String url_get_scheduleddive = "https://divethesite.com/app/get_scheduleddive_JSON.php";
	private static String url_get_scheduleddives = "https://divethesite.com/app/get_scheduleddives_JSON.php";
	private static String url_publish_scheduleddive = "https://divethesite.com/app/publish_scheduleddive_JSON.php";
	private static String url_set_scheduleddiveuser = "https://divethesite.com/app/set_scheduleddiveuser_JSON.php";

	private static final int DIVESITE_ONLINE_MODE_GET_SITE = 0;
	private static final int DIVESITE_ONLINE_MODE_GET_SITES = 1;
	private static final int DIVESITE_ONLINE_MODE_PUBLISH_SITE = 2;
	private static final int DIVESITE_ONLINE_MODE_GET_LOG = 3;
	private static final int DIVESITE_ONLINE_MODE_GET_LOGS = 4;
	private static final int DIVESITE_ONLINE_MODE_PUBLISH_LOG = 5;
	private static final int DIVESITE_ONLINE_MODE_USER_LOGIN = 6;
	private static final int DIVESITE_ONLINE_MODE_CREATE_USER = 7;
	private static final int DIVESITE_ONLINE_MODE_GET_USER = 8;
	private static final int DIVESITE_ONLINE_MODE_GET_USER_LIST = 9;
	private static final int DIVESITE_ONLINE_MODE_GET_USER_PICTURE = 10;
	private static final int DIVESITE_ONLINE_MODE_GET_NDBC_STATIONS = 11;
	private static final int DIVESITE_ONLINE_MODE_GET_NDBC_DATA = 12;
	private static final int DIVESITE_ONLINE_MODE_SAVE_USER = 13;
	private static final int DIVESITE_ONLINE_MODE_GET_SCHEDULEDDIVE = 14;
	private static final int DIVESITE_ONLINE_MODE_GET_SCHEDULEDDIVES = 15;
	private static final int DIVESITE_ONLINE_MODE_PUBLISH_SCHEDULEDDIVE = 16;
	private static final int DIVESITE_ONLINE_MODE_SET_SCHEDULEDDIVEUSER = 17;

	private static final String DATE_CREATED_URL_PARAM = "DATE_CREATED";
	private static final String LAST_MODIFIED_URL_PARAM = "LAST_MODIFIED";
	private static final String USER_ID_PARAM = "USER_ID";
	private static final String USERNAME_PARAM = "USERNAME";
	private static final String PASSWORD_PARAM = "PASSWORD";
	private static final String EMAIL_PARAM = "EMAIL";
	private static final String FIRSTNAME_PARAM = "FIRST_NAME";
	private static final String LASTNAME_PARAM = "LAST_NAME";
	private static final String NAME_PARAM = "NAME";
	private static final String CITY_PARAM = "CITY";
	private static final String COUNTRY_PARAM = "COUNTRY";
	private static final String PROVINCE_PARAM = "PROVINCE";
	private static final String START_INDEX_LOAD_PARAM = "START_INDEX_LOAD";
	private static final String COUNT_LOAD_PARAM = "COUNT_LOAD";
	private static final String PICTURE_URL_PARAM = "PICTURE_URL";
	private static final String BIO_PARAM = "BIO";
	private static final String IS_MOD_PARAM = "IS_MOD";
	private static final String NEW_PICTURE_PARAM = "NEW_PICTURE";
	private static final String APPROVED_PARAM = "APPROVED";
	private static final String STATION_ID_PARAM = "STATION_ID";
	private static final String MAX_DATA_RECORDS_PARAM = "MAX_DATA_RECORDS";
	private static final String STATION_UPDATED_PARAM = "STATION_UPDATED";
	private static final String CERTIF_COUNT_PARAM = "CERTIF_COUNT";
	private static final String CERTIF_ID_PARAM = "CERTIF_ID_";
	private static final String CERTIF_USER_ID_PARAM = "CERTIF_USER_ID_";
	private static final String CERTIF_TITLE_PARAM = "CERTIF_TITLE_";
	private static final String CERTIF_NUMBER_PARAM = "CERTIF_NUMBER_";
	private static final String CERTIF_LOATION_PARAM = "CERTIF_LOCATION_";
	private static final String CERTIF_DATE_PARAM = "CERTIF_DATE_";
	private static final String CERTIF_PRIMARY_PARAM = "CERTIF_PRIMARY_";
	private static final String DIVE_SITE_ID_PARAM = "DIVE_SITE_ID";
	private static final String DIVE_SITE_NAME_PARAM = "DIVE_SITE_NAME";
	private static final String TOTAL_RATING_PARAM = "TOTAL_RATING";
	private static final String NUM_RATES_PARAM = "NUM_RATES";
	private static final String DIFFICULTY_PARAM = "DIFFICULTY";
	private static final String IS_SALT_PARAM = "IS_SALT";
	private static final String IS_FRESH_PARAM = "IS_FRESH";
	private static final String IS_SHORE_PARAM = "IS_SHORE";
	private static final String IS_BOAT_PARAM = "IS_BOAT";
	private static final String IS_WRECK_PARAM = "IS_WRECK";
	private static final String HISTORY_PARAM = "HISTORY";
	private static final String DESCRIPTION_PARAM = "DESCRIPTION";
	private static final String DIRECTIONS_PARAM = "DIRECTIONS";
	private static final String SOURCE_PARAM = "SOURCE";
	private static final String NOTES_PARAM = "NOTES";
	private static final String LATITUDE_PARAM = "LATITUDE";
	private static final String LONGITUDE_PARAM = "LONGITUDE";
	private static final String DATE_ADDED_PARAM = "DATE_ADDED";
	private static final String PICTURE_COUNT_PARAM = "PICTURE_COUNT";
	private static final String CURRENT_LATITUDE_PARAM = "CURRENT_LATITUDE";
	private static final String CURRENT_LONGITUDE_PARAM = "CURRENT_LONGITUDE";
	private static final String DISTANCE_FROM_CURRENT_PARAM = "DISTANCE_FROM_CURRENT";

	private static final int DIVE_SITE_DATA_MODE_INDEX = 0;

	private static final int REFRESH_TIME_INDEX = 1;
	
	private static final int REFRESH_DIVER_ID_INDEX = 2;
	private static final int REFRESH_DIVESITE_ID_INDEX = 2;
	private static final int REFRESH_DIVELOG_ID_INDEX = 2;
	private static final int REFRESH_SCHEDULEDDIVE_ID_INDEX = 2;
	
	private static final int REFRESH_SITES_CURRENT_LATITUDE = 3;
	private static final int REFRESH_SITES_CURRENT_LONGITUDE = 4;
	private static final int REFRESH_SITES_FILTER_TITLE_INDEX = 5;
	private static final int REFRESH_SITES_FILTER_COUNTRY_INDEX = 6;
	private static final int REFRESH_SITES_FILTER_STATE_INDEX = 7;
	private static final int REFRESH_SITES_FILTER_CITY_INDEX = 8;
	private static final int REFRESH_SITES_FILTER_MIN_LATITUDE = 9;
	private static final int REFRESH_SITES_FILTER_MAX_LATITUDE = 10;
	private static final int REFRESH_SITES_FILTER_MIN_LONGITUDE = 11;
	private static final int REFRESH_SITES_FILTER_MAX_LONGITUDE = 12;
	private static final int REFRESH_SITES_DISTANCE_LIMIT = 13;
	private static final int REFRESH_SITES_START_INDEX_LOAD = 14;
	private static final int REFRESH_SITES_COUNT_LOAD = 15;

	private static final int REFRESH_LOGS_DIVESITE_ID_INDEX = 3;
    private static final int REFRESH_LOGS_FILTER_MIN_LATITUDE = 4;
    private static final int REFRESH_LOGS_FILTER_MAX_LATITUDE = 5;
    private static final int REFRESH_LOGS_FILTER_MIN_LONGITUDE = 6;
    private static final int REFRESH_LOGS_FILTER_MAX_LONGITUDE = 7;
	private static final int REFRESH_LOGS_START_INDEX_LOAD = 8;
	private static final int REFRESH_LOGS_COUNT_LOAD = 9;
	
	private static final int REFRESH_SCHEDULEDDIVES_SUBMITTER_ID_INDEX = 2;
	private static final int REFRESH_SCHEDULEDDIVES_USER_ATTENDING_ID_INDEX = 3;
	private static final int REFRESH_SCHEDULEDDIVES_DIVESITE_ID_INDEX = 4;
	private static final int REFRESH_SCHEDULEDDIVES_CURRENT_LATITUDE = 5;
	private static final int REFRESH_SCHEDULEDDIVES_CURRENT_LONGITUDE = 6;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_TITLE_INDEX = 7;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_COUNTRY_INDEX = 8;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_STATE_INDEX = 9;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_CITY_INDEX = 10;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_IGNORE_TIMESTAMPSTART_INDEX = 11;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_IGNORE_TIMESTAMPEND_INDEX = 12;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_TIMESTAMPSTART_INDEX = 13;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_TIMESTAMPEND_INDEX = 14;
	private static final int REFRESH_SCHEDULEDDIVES_FILTER_DISTANCE_LIMIT_INDEX = 15;
	private static final int REFRESH_SCHEDULEDDIVES_START_INDEX_LOAD = 16;
	private static final int REFRESH_SCHEDULEDDIVES_COUNT_LOAD = 17;

	private static final int LOGIN_USERNAME_INDEX = 1;
	private static final int LOGIN_PASSWORD_INDEX = 2;

	private static final int CREATE_USER_USERNAME_INDEX = 1;
	private static final int CREATE_USER_PASSWORD_INDEX = 2;
	private static final int CREATE_USER_EMAIL_INDEX = 3;
	private static final int CREATE_USER_FIRSTNAME_INDEX = 4;
	private static final int CREATE_USER_LASTNAME_INDEX = 5;
	private static final int CREATE_USER_COUNTRY_INDEX = 6;
	private static final int CREATE_USER_PROVINCE_INDEX = 7;
	private static final int CREATE_USER_CITY_INDEX = 8;
	private static final int CREATE_USER_PICTURE_URL_INDEX = 9;
	private static final int CREATE_USER_APPROVED_INDEX = 10;

	private static final int SAVE_USER_PASSWORD_INDEX = 1;
	private static final int SAVE_USER_PICTURE_INDEX = 2;

	private static final int GET_USER_USERID_INDEX = 1;
	private static final int GET_USER_USERNAME_INDEX = 2;
	private static final int GET_USER_EMAIL_INDEX = 3;

	private static final int REFRESH_USERS_FILTER_NAME_INDEX = 2;
	private static final int REFRESH_USERS_FILTER_COUNTRY_INDEX = 3;
	private static final int REFRESH_USERS_FILTER_STATE_INDEX = 4;
	private static final int REFRESH_USERS_FILTER_CITY_INDEX = 5;
	private static final int REFRESH_USERS_START_INDEX_LOAD = 6;
	private static final int REFRESH_USERS_COUNT_LOAD = 7;

	private static final int GET_STATIONS_MIN_LATITUDE_INDEX = 1;
	private static final int GET_STATIONS_MAX_LATITUDE_INDEX = 2;
	private static final int GET_STATIONS_MIN_LONGITUDE_INDEX = 3;
	private static final int GET_STATIONS_MAX_LONGITUDE_INDEX = 4;
	private static final int GET_STATIONS_UPDATED_INDEX = 5;
	private static final int GET_STATIONS_MIN_UPDATED_TIMESTAMP = 6;
	private static final int GET_STATIONS_CURRENT_LATITUDE = 7;
	private static final int GET_STATIONS_CURRENT_LONGITUDE = 8;
	private static final int GET_STATIONS_DISTANCE_FROM_CURRENT = 9;
	private static final int GET_STATIONS_START_INDEX_LOAD = 10;
	private static final int GET_STATIONS_COUNT_LOAD = 11;

	private static final int GET_NDBC_DATA_STATION_ID_INDEX = 1;
	private static final int GET_NDBC_DATA_MAX_DATA_RECORDS_INDEX = 2;

	private Context mContext;
	private JSONParser mJSONParser;

	private Object mUpdateObject = null;
	private ArrayList<Object> mResultList;

	private String mMessage = "";
	private Boolean mIsError = false;

	private Object mTag = null;

	private Boolean mActive = false;

	protected DiveSiteManager mDiveSiteManager;

	// Online Database Listeners
	private OnlineDiveDataListener mOnlineDiveDataListener;

	public interface OnlineDiveDataListener {
		void onOnlineDiveDataPostBackground(ArrayList<Object> resultList,
				String message);

		void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList,
				String message, Boolean isError);

		void onOnlineDiveDataProgress(Object result);
	}

	public DiveSiteOnlineDatabaseLink(Context context) {
		mContext = context;
		mDiveSiteManager = DiveSiteManager.get(context);
		mJSONParser = new JSONParser(mContext);

		mResultList = new ArrayList<Object>();
		mMessage = "";
	}

	public void setDiveSiteOnlineLoaderListener(OnlineDiveDataListener listen) {
		mOnlineDiveDataListener = listen;
	}

	public void setTag(Object tag) {
		mTag = tag;
	}

	public Object getTag() {
		return mTag;
	}

	/**
	 * Before starting background thread Show Progress Dialog
	 * */
	@Override
	protected void onPreExecute() {
		mActive = true;
		super.onPreExecute();
	}

	public AsyncTask<String, Object, String> getDiveSite(
			Date lastRefreshDate, long onlineID, DiveSite diveSite) {
        mUpdateObject = diveSite;
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_SITE),
				String.valueOf(lastRefreshDate.getTime()),
				String.valueOf(onlineID));
	}
	
	public AsyncTask<String, Object, String> getDiveSiteList(
			Date lastRefreshDate, long diverID, String currentLatitude,
			String currentLongitude, String title, String country,
			String state, String city, String minLatitude, String maxLatitude,
			String minLongitude, String maxLongitude, String distanceLimit,
			String startIndexLoad, String countLoad) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_SITES),
				String.valueOf(lastRefreshDate.getTime()),
				String.valueOf(diverID), currentLatitude, currentLongitude,
				title, country, state, city, minLatitude, maxLatitude, 
				minLongitude, maxLongitude, distanceLimit, startIndexLoad, countLoad);
	}

	public AsyncTask<String, Object, String> publishDiveSite(DiveSite diveSite) {
		String[] diveSiteFields = diveSite.getFieldsAsStrings();

		// Need to add MODE to start of list
		String[] fieldsToAppend = new String[1];
		String[] diveSiteFieldsAppended = new String[diveSiteFields.length
				+ fieldsToAppend.length];

		fieldsToAppend[DIVE_SITE_DATA_MODE_INDEX] = String
				.valueOf(DIVESITE_ONLINE_MODE_PUBLISH_SITE);

		System.arraycopy(fieldsToAppend, 0, diveSiteFieldsAppended, 0,
				fieldsToAppend.length);
		System.arraycopy(diveSiteFields, 0, diveSiteFieldsAppended,
				fieldsToAppend.length, diveSiteFields.length);

		return execute(diveSiteFieldsAppended);
	}

	public AsyncTask<String, Object, String> getDiveLog(Date lastRefreshDate, long onlineID, DiveLog diveLog) {
        mUpdateObject = diveLog;
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_LOG),
				String.valueOf(lastRefreshDate.getTime()), 
				String.valueOf(onlineID));
	}
	
	public AsyncTask<String, Object, String> getDiveLogList(
			Date lastRefreshDate, long diverID, long diveSiteID,
            String minLatitude, String maxLatitude,
            String minLongitude, String maxLongitude,
			String startIndexLoad, String countLoad) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_LOGS),
				String.valueOf(lastRefreshDate.getTime()),
				String.valueOf(diverID), String.valueOf(diveSiteID),
                minLatitude, maxLatitude, minLongitude, maxLongitude,
				startIndexLoad, countLoad);
	}

	public AsyncTask<String, Object, String> publishDiveLog(DiveLog diveLog) {
		String[] diveLogFields = diveLog.getFieldsAsStrings();

		// Need to add MODE
		String[] fieldsToAppend = new String[1];
		String[] diveLogFieldsAppended = new String[diveLogFields.length
				+ fieldsToAppend.length];

		fieldsToAppend[DIVE_SITE_DATA_MODE_INDEX] = String
				.valueOf(DIVESITE_ONLINE_MODE_PUBLISH_LOG);

		System.arraycopy(fieldsToAppend, 0, diveLogFieldsAppended, 0,
				fieldsToAppend.length);
		System.arraycopy(diveLogFields, 0, diveLogFieldsAppended,
				fieldsToAppend.length, diveLogFields.length);

		return execute(diveLogFieldsAppended);
	}

	public AsyncTask<String, Object, String> checkUserLogin(String username,
			String password) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_USER_LOGIN),
				username, password);
	}

	public AsyncTask<String, Object, String> createUser(String username,
			String password, String email, String firstName, String lastName,
			String country, String province, String city, String pictureURL,
			String approved) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_CREATE_USER),
				username, password, email, firstName, lastName, country,
				province, city, pictureURL, approved);
	}

	public AsyncTask<String, Object, String> saveUser(Diver diver,
			String password, String profileNewImageFilePath) {
		String[] diverFields = diver.getFieldsAsStrings();

		// Need to add MODE and PASSWORD to start of list
		String[] fieldsToAppend = new String[3];
		String[] diverFieldsAppended = new String[diverFields.length
				+ fieldsToAppend.length];

		fieldsToAppend[DIVE_SITE_DATA_MODE_INDEX] = String.valueOf(DIVESITE_ONLINE_MODE_SAVE_USER);
		fieldsToAppend[SAVE_USER_PASSWORD_INDEX] = password;
		fieldsToAppend[SAVE_USER_PICTURE_INDEX] = profileNewImageFilePath;

		System.arraycopy(fieldsToAppend, 0, diverFieldsAppended, 0,
				fieldsToAppend.length);
		System.arraycopy(diverFields, 0, diverFieldsAppended,
				fieldsToAppend.length, diverFields.length);

		return execute(diverFieldsAppended);
	}

	public AsyncTask<String, Object, String> getUser(String userID,
			String username, String email) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_USER), userID,
				username, email);
	}

	public AsyncTask<String, Object, String> getDiverList(Date lastRefreshDate,
			String name, String country, String state, String city,
			String startIndexLoad, String countLoad) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_USER_LIST),
				String.valueOf(lastRefreshDate.getTime()), name, country,
				state, city, startIndexLoad, countLoad);
	}

	public AsyncTask<String, Object, String> getUserPicture(String userID,
			String username, String email) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_USER_PICTURE),
				userID, username, email);
	}

	public AsyncTask<String, Object, String> getNDBCStations(
			String minLatitude, String maxLatitude, String minLongitude,
			String maxLongitude, String stationsUpdated,
			String minLastUpdatedTimestamp,
			String currentLatitude, String currentLongitude,
			String distanceFromCurrent,
			String startIndexLoad, String countLoad) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_NDBC_STATIONS),
				minLatitude, maxLatitude, minLongitude, maxLongitude,
				stationsUpdated, minLastUpdatedTimestamp, currentLatitude, 
				currentLongitude, distanceFromCurrent,
			    startIndexLoad, countLoad);
	}

	public AsyncTask<String, Object, String> updateNDBCDataForStation(
			NDBCStation ndbcStation, String maxDataRecords) {
		mUpdateObject = ndbcStation;
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_NDBC_DATA),
				String.valueOf(ndbcStation.getStationId()), maxDataRecords);
	}

	public AsyncTask<String, Object, String> getScheduledDive(Date lastRefreshDate, long onlineID, ScheduledDive scheduledDive) {
        mUpdateObject = scheduledDive;
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_SCHEDULEDDIVE),
				String.valueOf(lastRefreshDate.getTime()), 
				String.valueOf(onlineID));
	}
	
	public AsyncTask<String, Object, String> getScheduledDiveList(
			Date lastRefreshDate, long submitterID, long userAttendingID, long diveSiteID,
			String currentLatitude, String currentLongitude,
			String title, String country, String state, String city,
			String ignoreTimeStampStartFilter, String ignoreTimeStampEndFilter,
			String timeStampStartFilter, String timeStampEndFilter,
			String distanceLimit,
			String startIndexLoad, String countLoad) {
		return execute(String.valueOf(DIVESITE_ONLINE_MODE_GET_SCHEDULEDDIVES),
				String.valueOf(lastRefreshDate.getTime()),
				String.valueOf(submitterID), String.valueOf(userAttendingID), String.valueOf(diveSiteID),
				currentLatitude, currentLongitude, title, country, state, city, 
				ignoreTimeStampStartFilter, ignoreTimeStampEndFilter,
				timeStampStartFilter, timeStampEndFilter,
				distanceLimit, startIndexLoad, countLoad);
	}

	public AsyncTask<String, Object, String> publishScheduledDive(ScheduledDive scheduledDive) {
		String[] scheduledDiveFields = scheduledDive.getFieldsAsStrings();

		// Need to add MODE
		String[] fieldsToAppend = new String[1];
		String[] scheduledDiveFieldsAppended = 
				new String[scheduledDiveFields.length + fieldsToAppend.length];

		fieldsToAppend[DIVE_SITE_DATA_MODE_INDEX] = 
				String.valueOf(DIVESITE_ONLINE_MODE_PUBLISH_SCHEDULEDDIVE);

		System.arraycopy(fieldsToAppend, 0, scheduledDiveFieldsAppended, 0, fieldsToAppend.length);
		System.arraycopy(scheduledDiveFields, 0, scheduledDiveFieldsAppended, fieldsToAppend.length, scheduledDiveFields.length);

		return execute(scheduledDiveFieldsAppended);
	}
	
	public AsyncTask<String, Object, String> setScheduledDiveUser(ScheduledDiveUser scheduledDiveUser) {
		String[] scheduledDiveUserFields = scheduledDiveUser.getFieldsAsStrings();

		// Need to add MODE
		String[] fieldsToAppend = new String[1];
		String[] scheduledDiveUserFieldsAppended = 
				new String[scheduledDiveUserFields.length + fieldsToAppend.length];

		fieldsToAppend[DIVE_SITE_DATA_MODE_INDEX] = 
				String.valueOf(DIVESITE_ONLINE_MODE_SET_SCHEDULEDDIVEUSER);

		System.arraycopy(fieldsToAppend, 0, scheduledDiveUserFieldsAppended, 0, fieldsToAppend.length);
		System.arraycopy(scheduledDiveUserFields, 0, scheduledDiveUserFieldsAppended, fieldsToAppend.length, scheduledDiveUserFields.length);

		return execute(scheduledDiveUserFieldsAppended);
	}
	
	/**
	 * Getting dive sites from URL
	 * */
	@Override
	protected String doInBackground(String... args) {
		mMessage = "";

		int mode = Integer.valueOf(args[DIVE_SITE_DATA_MODE_INDEX]);

		switch (mode) {
		case DIVESITE_ONLINE_MODE_GET_SITE:
			long diveSiteRefreshTime = Long
					.valueOf(args[REFRESH_TIME_INDEX]);
			long diveSiteRefreshOnlineID = Long
					.valueOf(args[REFRESH_DIVESITE_ID_INDEX]);
			
			getDiveSiteBackground(diveSiteRefreshTime, diveSiteRefreshOnlineID);
			break;
		
		case DIVESITE_ONLINE_MODE_GET_SITES:
			long diveSiteListRefreshTime = Long
					.valueOf(args[REFRESH_TIME_INDEX]);
			long diveSiteListRefreshDiverID = Long
					.valueOf(args[REFRESH_DIVER_ID_INDEX]);
			
			String currentLatitude = args[REFRESH_SITES_CURRENT_LATITUDE];
			String currentLongitude = args[REFRESH_SITES_CURRENT_LONGITUDE];
			String diveSiteListTitleFilter = args[REFRESH_SITES_FILTER_TITLE_INDEX];
			String diveSiteListCountryFilter = args[REFRESH_SITES_FILTER_COUNTRY_INDEX];
			String diveSiteListStateFilter = args[REFRESH_SITES_FILTER_STATE_INDEX];
			String diveSiteListCityFilter = args[REFRESH_SITES_FILTER_CITY_INDEX];
			String diveSiteListMinLatitudeFilter = args[REFRESH_SITES_FILTER_MIN_LATITUDE];
			String diveSiteListMaxLatitudeFilter = args[REFRESH_SITES_FILTER_MAX_LATITUDE];
			String diveSiteListMinLongitudeFilter = args[REFRESH_SITES_FILTER_MIN_LONGITUDE];
			String diveSiteListMaxLongitudeFilter = args[REFRESH_SITES_FILTER_MAX_LONGITUDE];
			String diveSiteListDistanceLimit = args[REFRESH_SITES_DISTANCE_LIMIT];
			String diveSiteListStartIndexLoad = args[REFRESH_SITES_START_INDEX_LOAD];
			String diveSiteListCountLoad = args[REFRESH_SITES_COUNT_LOAD];

			getDiveSiteListBackground(diveSiteListRefreshTime,
					diveSiteListRefreshDiverID, 
					currentLatitude, currentLongitude,
					diveSiteListTitleFilter,
					diveSiteListCountryFilter, diveSiteListStateFilter,
					diveSiteListCityFilter, diveSiteListMinLatitudeFilter,
					diveSiteListMaxLatitudeFilter,
					diveSiteListMinLongitudeFilter,
					diveSiteListMaxLongitudeFilter,
					diveSiteListDistanceLimit,
					diveSiteListStartIndexLoad, diveSiteListCountLoad);
			break;

		case DIVESITE_ONLINE_MODE_PUBLISH_SITE:

			// Diver fields were shifted from hardcoded positions, set increment
			int diveSiteFieldPositionIncrement = DIVE_SITE_DATA_MODE_INDEX + 1;

			publishDiveSiteBackground(diveSiteFieldPositionIncrement, args);
			break;

		case DIVESITE_ONLINE_MODE_GET_LOG:
			long diveLogRefreshTime = Long.valueOf(args[REFRESH_TIME_INDEX]);
			long diveLogRefreshOnlineID = Long.valueOf(args[REFRESH_DIVELOG_ID_INDEX]);
			
			getDiveLogBackground(diveLogRefreshTime, diveLogRefreshOnlineID);
			break;
			
		case DIVESITE_ONLINE_MODE_GET_LOGS:
			long diveLogListRefreshTime = Long.valueOf(args[REFRESH_TIME_INDEX]);
			long diveLogListRefreshDiverID = Long.valueOf(args[REFRESH_DIVER_ID_INDEX]);
			long diveLogListRefreshDiveSiteID = Long.valueOf(args[REFRESH_LOGS_DIVESITE_ID_INDEX]);
            String diveLogListMinLatitudeFilter = args[REFRESH_LOGS_FILTER_MIN_LATITUDE];
            String diveLogListMaxLatitudeFilter = args[REFRESH_LOGS_FILTER_MAX_LATITUDE];
            String diveLogListMinLongitudeFilter = args[REFRESH_LOGS_FILTER_MIN_LONGITUDE];
            String diveLogListMaxLongitudeFilter = args[REFRESH_LOGS_FILTER_MAX_LONGITUDE];
			String diveLogListStartIndexLoad = args[REFRESH_LOGS_START_INDEX_LOAD];
			String diveLogListCountLoad = args[REFRESH_LOGS_COUNT_LOAD];

			getDiveLogListBackground(diveLogListRefreshTime,
					diveLogListRefreshDiverID, diveLogListRefreshDiveSiteID,
                    diveLogListMinLatitudeFilter, diveLogListMaxLatitudeFilter,
                    diveLogListMinLongitudeFilter, diveLogListMaxLongitudeFilter,
					diveLogListStartIndexLoad, diveLogListCountLoad);
			break;

		case DIVESITE_ONLINE_MODE_PUBLISH_LOG:

			// Diver fields were shifted from hardcoded positions
			int diveLogFieldPositionIncrement = DIVE_SITE_DATA_MODE_INDEX + 1;

			publishDiveLogBackground(diveLogFieldPositionIncrement, args);
			break;

		case DIVESITE_ONLINE_MODE_USER_LOGIN:
			String usernameLogin = args[LOGIN_USERNAME_INDEX];
			String passwordLogin = args[LOGIN_PASSWORD_INDEX];

			checkDiverLoginBackground(usernameLogin, passwordLogin);
			break;

		case DIVESITE_ONLINE_MODE_CREATE_USER:
			String usernameCreate = args[CREATE_USER_USERNAME_INDEX];
			String passwordCreate = args[CREATE_USER_PASSWORD_INDEX];
			String emailCreate = args[CREATE_USER_EMAIL_INDEX];
			String firstNameCreate = args[CREATE_USER_FIRSTNAME_INDEX];
			String lastNameCreate = args[CREATE_USER_LASTNAME_INDEX];
			String countryCreate = args[CREATE_USER_COUNTRY_INDEX];
			String provinceCreate = args[CREATE_USER_PROVINCE_INDEX];
			String cityCreate = args[CREATE_USER_CITY_INDEX];
			String pictureCreate = args[CREATE_USER_PICTURE_URL_INDEX];
			String approvedCreate = args[CREATE_USER_APPROVED_INDEX];

			createDiverBackground(usernameCreate, passwordCreate, emailCreate,
					firstNameCreate, lastNameCreate, countryCreate,
					provinceCreate, cityCreate, pictureCreate, approvedCreate);
			break;

		case DIVESITE_ONLINE_MODE_SAVE_USER:
			String passwordSave = args[SAVE_USER_PASSWORD_INDEX];
			String profileNewImageFilePath = args[SAVE_USER_PICTURE_INDEX];

			// Diver fields were shifted from hardcoded positions
			int DiverFieldPositionIncrement = SAVE_USER_PICTURE_INDEX + 1;
			String usernameSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_USERNAME_INDEX];
			String emailSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_EMAIL_INDEX];
			String firstNameSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_FIRSTNAME_INDEX];
			String lastNameSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_LASTNAME_INDEX];
			String countrySave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_COUNTRY_INDEX];
			String provinceSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_PROVINCE_INDEX];
			String citySave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_CITY_INDEX];
			String bioSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_BIO_INDEX];
			String isModSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_IS_MOD_INDEX];
			String userIDSave = args[DiverFieldPositionIncrement
					+ Diver.DIVER_ID_INDEX];

			// Generate list of certifications
			ArrayList<String[]> certifications = new ArrayList<String[]>();
			int DiverCertificationIndexStart = DiverFieldPositionIncrement
					+ Diver.DIVER_FIELD_COUNT;
			int currentCertificationIndex = -1;

			for (int i = DiverCertificationIndexStart; i < args.length; i++) {
				// Figure out if we're at the start of the fields for a new
				// certification
				int certificationFieldIndex = (i - DiverCertificationIndexStart)
						% DiverCertification.CERTIFICATION_FIELD_COUNT;
				if (certificationFieldIndex == 0) {
					certifications
							.add(new String[DiverCertification.CERTIFICATION_FIELD_COUNT]);
					currentCertificationIndex = currentCertificationIndex + 1;
				}

				certifications.get(currentCertificationIndex)[certificationFieldIndex] = args[i];
			}

			saveDiverBackground(usernameSave, passwordSave, emailSave,
					firstNameSave, lastNameSave, countrySave, provinceSave,
					citySave, bioSave, isModSave, userIDSave, certifications,
					profileNewImageFilePath);
			break;

		case DIVESITE_ONLINE_MODE_GET_USER:
			String userIDGet = args[GET_USER_USERID_INDEX];
			String usernameGet = args[GET_USER_USERNAME_INDEX];
			String emailGet = args[GET_USER_EMAIL_INDEX];

			getDiverBackground(userIDGet, usernameGet, emailGet);
			break;

		case DIVESITE_ONLINE_MODE_GET_USER_LIST:
			long diverListRefreshTime = Long.valueOf(args[REFRESH_TIME_INDEX]);

			String diverListNameFilter = args[REFRESH_USERS_FILTER_NAME_INDEX];
			String diverListCountryFilter = args[REFRESH_USERS_FILTER_COUNTRY_INDEX];
			String diverListStateFilter = args[REFRESH_USERS_FILTER_STATE_INDEX];
			String diverListCityFilter = args[REFRESH_USERS_FILTER_CITY_INDEX];
			String diverListStartIndexLoad = args[REFRESH_USERS_START_INDEX_LOAD];
			String diverListCountLoad = args[REFRESH_USERS_COUNT_LOAD];

			getDiverListBackground(diverListRefreshTime, diverListNameFilter,
					diverListCountryFilter, diverListStateFilter,
					diverListCityFilter, diverListStartIndexLoad,
					diverListCountLoad);
			break;

		case DIVESITE_ONLINE_MODE_GET_USER_PICTURE:
			String userIDPictureGet = args[GET_USER_USERID_INDEX];
			String usernamePictureGet = args[GET_USER_USERNAME_INDEX];
			String emailPictureGet = args[GET_USER_EMAIL_INDEX];

			getUserPictureBackground(userIDPictureGet, usernamePictureGet,
					emailPictureGet);
			break;

		case DIVESITE_ONLINE_MODE_GET_NDBC_STATIONS:
			String minLatitudeStationGet = args[GET_STATIONS_MIN_LATITUDE_INDEX];
			String maxLatitudeStationGet = args[GET_STATIONS_MAX_LATITUDE_INDEX];
			String minLongitudeStationGet = args[GET_STATIONS_MIN_LONGITUDE_INDEX];
			String maxLongitudeStationGet = args[GET_STATIONS_MAX_LONGITUDE_INDEX];
			String stationUpdatedStationGet = args[GET_STATIONS_UPDATED_INDEX];
			String minimimLastUpdateTimestampStationGet = args[GET_STATIONS_MIN_UPDATED_TIMESTAMP];
			String currentLatitudeStationGet = args[GET_STATIONS_CURRENT_LATITUDE];
			String currentLongitudeStationGet = args[GET_STATIONS_CURRENT_LONGITUDE];
			String distanceFromCurrent = args[GET_STATIONS_DISTANCE_FROM_CURRENT];
			String startIndexLoadStationGet = args[GET_STATIONS_START_INDEX_LOAD];
			String countLoadStationGet = args[GET_STATIONS_COUNT_LOAD];
			
			getNDBCStationsBackground(minLatitudeStationGet,
					maxLatitudeStationGet, minLongitudeStationGet,
					maxLongitudeStationGet, stationUpdatedStationGet, 
					minimimLastUpdateTimestampStationGet, currentLatitudeStationGet, 
					currentLongitudeStationGet, distanceFromCurrent,
					startIndexLoadStationGet, countLoadStationGet);
			break;

		case DIVESITE_ONLINE_MODE_GET_NDBC_DATA:
			String stationIDGet = args[GET_NDBC_DATA_STATION_ID_INDEX];
			String maxDataRecordsGet = args[GET_NDBC_DATA_MAX_DATA_RECORDS_INDEX];
			
			updateNDBCDataForStationBackground(stationIDGet, maxDataRecordsGet);
			
		case DIVESITE_ONLINE_MODE_GET_SCHEDULEDDIVE:
			long scheduledDiveRefreshTime = Long.valueOf(args[REFRESH_TIME_INDEX]);
			long scheduledDiveRefreshOnlineID = Long.valueOf(args[REFRESH_SCHEDULEDDIVE_ID_INDEX]);
			
			getScheduledDiveBackground(scheduledDiveRefreshTime, scheduledDiveRefreshOnlineID);
			break;
			
		case DIVESITE_ONLINE_MODE_GET_SCHEDULEDDIVES:
			long scheduledDiveListRefreshTime = Long.valueOf(args[REFRESH_TIME_INDEX]);
			long scheduledDiveListRefreshSubmitterID = Long.valueOf(args[REFRESH_SCHEDULEDDIVES_SUBMITTER_ID_INDEX]);
			long scheduledDiveListRefreshUserAttendingID = Long.valueOf(args[REFRESH_SCHEDULEDDIVES_USER_ATTENDING_ID_INDEX]);
			long scheduledDiveListRefreshDiveSiteID = Long.valueOf(args[REFRESH_SCHEDULEDDIVES_DIVESITE_ID_INDEX]);
			String scheduledDiveListCurrentLatitude = args[REFRESH_SCHEDULEDDIVES_CURRENT_LATITUDE];
			String scheduledDiveListCurrentLongitude = args[REFRESH_SCHEDULEDDIVES_CURRENT_LONGITUDE];
			String scheduledDiveListTitleFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_TITLE_INDEX];
			String scheduledDiveListCountryFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_COUNTRY_INDEX];
			String scheduledDiveListStateFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_STATE_INDEX];
			String scheduledDiveListCityFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_CITY_INDEX];
			String scheduledDiveListIgnoreTimeStampStartFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_IGNORE_TIMESTAMPSTART_INDEX];
			String scheduledDiveListIgnoreTimeStampEndFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_IGNORE_TIMESTAMPEND_INDEX];
			String scheduledDiveListTimeStampStartFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_TIMESTAMPSTART_INDEX];
			String scheduledDiveListTimeStampEndFilter = args[REFRESH_SCHEDULEDDIVES_FILTER_TIMESTAMPEND_INDEX];
			String scheduledDiveListDistanceLimit = args[REFRESH_SCHEDULEDDIVES_FILTER_DISTANCE_LIMIT_INDEX];
			String scheduledDiveListStartIndexLoad = args[REFRESH_SCHEDULEDDIVES_START_INDEX_LOAD];
			String scheduledDiveListCountLoad = args[REFRESH_SCHEDULEDDIVES_COUNT_LOAD];

			getScheduledDiveListBackground(scheduledDiveListRefreshTime,
					scheduledDiveListRefreshSubmitterID, scheduledDiveListRefreshUserAttendingID, 
					scheduledDiveListRefreshDiveSiteID, scheduledDiveListCurrentLatitude,
					scheduledDiveListCurrentLongitude, scheduledDiveListTitleFilter,
					scheduledDiveListCountryFilter, scheduledDiveListStateFilter,
					scheduledDiveListCityFilter, 
					scheduledDiveListIgnoreTimeStampStartFilter, scheduledDiveListIgnoreTimeStampEndFilter,
					scheduledDiveListTimeStampStartFilter, scheduledDiveListTimeStampEndFilter,
					scheduledDiveListDistanceLimit,
					scheduledDiveListStartIndexLoad, scheduledDiveListCountLoad);
			break;

		case DIVESITE_ONLINE_MODE_PUBLISH_SCHEDULEDDIVE:

			// Fields were shifted from hardcoded positions
			int scheduledDiveFieldPositionIncrement = DIVE_SITE_DATA_MODE_INDEX + 1;

			publishScheduledDiveBackground(scheduledDiveFieldPositionIncrement, args);
			break;
			
		case DIVESITE_ONLINE_MODE_SET_SCHEDULEDDIVEUSER:
			
			// Fields were shifted from hardcoded positions
			int scheduledDiveUserFieldPositionIncrement = DIVE_SITE_DATA_MODE_INDEX + 1;

			setScheduledDiveUserBackground(scheduledDiveUserFieldPositionIncrement, args);
			break;
		}

		if (mOnlineDiveDataListener != null) {
			mOnlineDiveDataListener.onOnlineDiveDataPostBackground(mResultList,
					mMessage);
		}

		return null;
	}

	private void getDiveSiteBackground(long lastRefreshDateTime, long diveSiteOnlineID) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(LAST_MODIFIED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(DIVE_SITE_ID_PARAM, String.valueOf(diveSiteOnlineID));
		
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_get_divesite, URL_GET, params);

			// Check your log cat for JSON response
			Log.d("Dive Site:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					ArrayList<DiveSite> diveSites = getDiveSitesJSON(json, JSON_TAG_DIVESITES);
					if (diveSites.size() > 0) {
						DiveSite diveSite = diveSites.get(0);
						
						// Add Dive Logs found
						ArrayList<DiveLog> diveLogs =  
							getDiveLogsJSON(json, JSON_TAG_DIVELOG_SITE + String.valueOf(diveSite.getOnlineId()));
						for (int j = 0; j < diveLogs.size(); j++) {
							diveLogs.get(j).setDiveSiteLocalId(diveSite.getLocalId());
							diveLogs.get(j).setDiveSite(diveSite);
							diveSite.getDiveLogs().add(diveLogs.get(j));
						}
	
						mResultList.add(diveSite);
						publishProgress(diveSite);
					}
					
					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.publish_divesite_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			publishProgress(null);
		}
		
		if (mResultList.size() == 0) {
            if (mUpdateObject != null) {
                mResultList.add(mUpdateObject);
            } else {
                // No update found, return local one
                long localID = mDiveSiteManager.getDiveSiteLocalId(diveSiteOnlineID);
                DiveSite diveSite = mDiveSiteManager.getDiveSite(localID);
                if (diveSite != null) {
                    mResultList.add(diveSite);
                }
            }
		}
	}

	private void getDiveSiteListBackground(long lastRefreshDateTime,
			long diveSiteListRefreshDiverID, String currentLatitude, 
			String currentLongitude, String diveSiteListTitleFilter,
			String diveSiteListCountryFilter, String diveSiteListStateFilter,
			String diveSiteListCityFilter, String minLatitudeFilter,
			String maxLatitudeFilter, String minLongitudeFilter,
			String maxLongitudeFilter, String distanceLimit,
			String diveSiteListStartIndexLoad, String diveSiteListCountLoad) {

		// Building Parameters
        ContentValues params = new ContentValues();
		params.put(DATE_CREATED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(USER_ID_PARAM, String.valueOf(diveSiteListRefreshDiverID));
		params.put(CURRENT_LATITUDE_PARAM, currentLatitude);
		params.put(CURRENT_LONGITUDE_PARAM, currentLongitude);
		params.put(DiveSite.DIVE_SITE_TITLE_PARAM, diveSiteListTitleFilter);
		params.put(DiveSite.DIVE_SITE_COUNTRY_PARAM, diveSiteListCountryFilter);
		params.put(DiveSite.DIVE_SITE_PROVINCE_PARAM, diveSiteListStateFilter);
		params.put(DiveSite.DIVE_SITE_CITY_PARAM, diveSiteListCityFilter);
		params.put(DiveSite.DIVE_SITE_MIN_LATITUDE_PARAM, minLatitudeFilter);
		params.put(DiveSite.DIVE_SITE_MAX_LATITUDE_PARAM, maxLatitudeFilter);
		params.put(DiveSite.DIVE_SITE_MIN_LONGITUDE_PARAM, minLongitudeFilter);
		params.put(DiveSite.DIVE_SITE_MAX_LONGITUDE_PARAM, maxLongitudeFilter);
		params.put(DiveSite.DIVE_SITE_DISTANCE_PARAM, distanceLimit);
		params.put(DiveSite.DIVE_SITE_START_INDEX_LOAD_PARAM, diveSiteListStartIndexLoad);
		params.put(DiveSite.DIVE_SITE_COUNT_LOAD_PARAM, diveSiteListCountLoad);

		// getting JSON string from URL
		mJSONParser.setJSONParserListener(new JSONParserListener() {

			@Override
			public void onJSONParserObjectAvailable(JSONObject json) {
				// Process JSON Object as a single dive site
				Log.d("Online Dive Site: ", json.toString());

				try {
					// Checking for SUCCESS TAG
					int success = json.getInt(JSON_TAG_SUCCESS);

					if (success == 1) {
						// Dive Sites found
						ArrayList<DiveSite> diveSites = getDiveSitesJSON(json, JSON_TAG_DIVESITES);

						for (int i = 0; i < diveSites.size(); i++) {
							DiveSite diveSite = diveSites.get(i);
							
							// Add Dive Logs found
							ArrayList<DiveLog> diveLogs = 
								getDiveLogsJSON(json, JSON_TAG_DIVELOG_SITE + String.valueOf(diveSite.getOnlineId()));
							for (int j = 0; j < diveLogs.size(); j++) {
								DiveLog diveLog = diveLogs.get(j);
								diveSite.getDiveLogs().add(diveLog);								
							}
							mResultList.add(diveSite);
							publishProgress(diveSite);
						}
					} else {
						mIsError = true;
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});

		try {
			mJSONParser.makeHttpRequestJSONLines(url_get_divesites, URL_GET, params);
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}
	}

	private void publishDiveSiteBackground(int diveSiteFieldPositionIncrement,
			String[] args) {

		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(DIVE_SITE_ID_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_ONLINE_ID_INDEX]);
		params.put(DIVE_SITE_NAME_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_NAME_INDEX]);
		params.put(TOTAL_RATING_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_TOTAL_RATE_INDEX]);
		params.put(NUM_RATES_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_NUM_RATES_INDEX]);
		params.put(CITY_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_CITY_INDEX]);
		params.put(DiveSite.DIVE_SITE_PROVINCE_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_PROVINCE_INDEX]);
		params.put(DiveSite.DIVE_SITE_COUNTRY_PARAM, args[diveSiteFieldPositionIncrement+ DiveSite.DIVE_SITE_COUNTRY_INDEX]);
		params.put(DIFFICULTY_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_DIFFICULTY_INDEX]);
		params.put(IS_SALT_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_ISSALT_INDEX]);
		params.put(IS_FRESH_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_ISFRESH_INDEX]);
		params.put(IS_SHORE_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_ISSHORE_INDEX]);
		params.put(IS_BOAT_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_ISBOAT_INDEX]);
		params.put(IS_WRECK_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_ISWRECK_INDEX]);
		params.put(HISTORY_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_HISTORY_INDEX]);
		params.put(DESCRIPTION_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_DESCRIPTION_INDEX]);
		params.put(DIRECTIONS_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_DIRECTIONS_INDEX]);
		params.put(SOURCE_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_SOURCE_INDEX]);
		params.put(NOTES_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_NOTES_INDEX]);
		params.put(LATITUDE_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_LATITUDE_INDEX]);
		params.put(LONGITUDE_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_LONGITUDE_INDEX]);
		params.put(USER_ID_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_USER_ID_INDEX]);
		params.put(DATE_ADDED_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_DATE_ADDED_INDEX]);
		params.put(LAST_MODIFIED_URL_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_LAST_MODIFIED_INDEX]);
		params.put(APPROVED_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_APPROVED_INDEX]);
		params.put(USERNAME_PARAM, args[diveSiteFieldPositionIncrement + DiveSite.DIVE_SITE_USERNAME_INDEX]);

		int diveSitePicturesIndexStart = diveSiteFieldPositionIncrement
				+ Integer.valueOf(args[diveSiteFieldPositionIncrement
						+ DiveSite.DIVE_SITE_PICTURES_START_POS_INDEX]);
		int diveSitePictureCount = Integer
				.valueOf(args[diveSiteFieldPositionIncrement
						+ DiveSite.DIVE_SITE_PICTURES_COUNT_INDEX]);

		// Dive Site Pictures Parameters
		params.put(PICTURE_COUNT_PARAM, String.valueOf(diveSitePictureCount));
		for (int i = 0; i < diveSitePictureCount; i++) {
			params.put(
					DiveSitePicture.DIVE_SITE_PICTURE_LOCAL_ID_PARAM + i,
					args[diveSitePicturesIndexStart + i
							* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
							+ DiveSitePicture.DIVE_SITE_PICTURE_LOCAL_ID_INDEX]);

			params.put(
					DiveSitePicture.DIVE_SITE_PICTURE_ONLINE_ID_PARAM + i,
					args[diveSitePicturesIndexStart + i
							* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
							+ DiveSitePicture.DIVE_SITE_PICTURE_ONLINE_ID_INDEX]);

			params.put(
					DiveSitePicture.DIVE_SITE_PICTURE_SITE_ID_PARAM + i,
					args[diveSitePicturesIndexStart
							+ i
							* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
							+ DiveSitePicture.DIVE_SITE_PICTURE_DIVE_SITE_ONLINE_ID_INDEX]);

			params.put(
					DiveSitePicture.DIVE_SITE_PICTURE_DESCRIPTION_PARAM + i,
					args[diveSitePicturesIndexStart
							+ i
							* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
							+ DiveSitePicture.DIVE_SITE_PICTURE_DESCRIPTION_INDEX]);

			params.put(
					DiveSitePicture.DIVE_SITE_PICTURE_FILE_PATH_PARAM + i,
					args[diveSitePicturesIndexStart + i
							* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
							+ DiveSitePicture.DIVE_SITE_PICTURE_FILE_PATH_INDEX]);

			params.put(
					DiveSitePicture.DIVE_SITE_PICTURE_URL_PARAM + i,
					args[diveSitePicturesIndexStart + i
							* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
							+ DiveSitePicture.DIVE_SITE_PICTURE_URL_INDEX]);

			// Now, if available, get bitmap from file and save
			String pictureFilePath = args[diveSitePicturesIndexStart + i
					* DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT
					+ DiveSitePicture.DIVE_SITE_PICTURE_FILE_PATH_INDEX];

			if (pictureFilePath != null && !pictureFilePath.trim().isEmpty()) {
				// Encode image
				String encodedImage = "";
				Bitmap bm = LoadFileImageTask.getImageFromFile(pictureFilePath);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
				byte[] b = baos.toByteArray();

				encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

				params.put(DiveSitePicture.DIVE_SITE_PICTURE_NEW_IMAGE_PARAM + i, encodedImage);
			} else {
				params.put(DiveSitePicture.DIVE_SITE_PICTURE_NEW_IMAGE_PARAM + i, "");
			}
		}

		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_publish_divesite, URL_POST,
					params);

			// Check your log cat for JSON response
			Log.d("Publish Dive Site:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Dive Sites found
					ArrayList<DiveSite> diveSites = getDiveSitesJSON(json, JSON_TAG_DIVESITES);

					// Should have one dive site if successful
					if (diveSites.size() > 0) {
						DiveSite diveSite = diveSites.get(0);
						diveSite.setLocalId(Integer.valueOf(args[diveSiteFieldPositionIncrement
							+ DiveSite.DIVE_SITE_LOCAL_ID_INDEX]));

						// Add Dive Logs found
						ArrayList<DiveLog> diveLogs = 
							getDiveLogsJSON(json, JSON_TAG_DIVELOG_SITE + String.valueOf(diveSite.getOnlineId()));
						for (int j = 0; j < diveLogs.size(); j++) {
							DiveLog diveLog = diveLogs.get(j);
							diveLog.setDiveSiteLocalId(diveSite.getLocalId());
							diveLog.setDiveSite(diveSite);
							diveSite.getDiveLogs().add(diveLog);
						}

						mResultList.add(diveSite);
						publishProgress(diveSite);
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.publish_divesite_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			publishProgress(null);
		}
	}
	
	private void getDiveLogBackground(long lastRefreshDateTime, long diveLogOnlineID) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(LAST_MODIFIED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(DiveLog.DIVE_LOG_ID_PARAM, String.valueOf(diveLogOnlineID));
		
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_get_divelog, URL_GET, params);

			// Check your log cat for JSON response
			Log.d("Dive Log:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Dive Logs found
					ArrayList<DiveLog> diveLogs = getDiveLogsJSON(json, JSON_TAG_DIVELOGS);
				
					// Should have one dive log if successful
					if (diveLogs.size() > 0) {
						DiveLog diveLog = diveLogs.get(0);
													
						// Set the Dive Sites found
						ArrayList<DiveSite> diveSites = 
							getDiveSitesJSON(json, JSON_TAG_DIVESITES_LOG + String.valueOf(diveLog.getOnlineId()));
						
						for (int i = 0; i < diveSites.size(); i++) {
							DiveSite diveSite = diveSites.get(i);
							diveLog.setDiveSite(diveSite);
						}

						mResultList.add(diveLog);
						publishProgress(diveLog);
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.online_communication_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (mResultList.size() == 0) {
			// No update found, return one given as update object if available, otherwise, return local one
            if (mUpdateObject != null) {
                mResultList.add(mUpdateObject);
            } else {
                long localID = mDiveSiteManager.getDiveLogLocalId(diveLogOnlineID);
                DiveLog diveLog = mDiveSiteManager.getDiveLog(localID);
                if (diveLog != null){
                    mResultList.add(mDiveSiteManager.getDiveLog(localID));
                }
            }
		}
	}

	private void getScheduledDiveListBackground(long lastRefreshDateTime,
			long submitterID, long userAttendingID, long diveSiteID,
			String scheduledDiveListCurrentLatitude, String scheduledDiveListCurrentLongitude,
			String scheduledDiveListTitleFilter, String scheduledDiveListCountryFilter, 
			String scheduledDiveListStateFilter, String scheduledDiveListCityFilter, 
			String scheduledDiveListIgnoreTimeStampStartFilter, String scheduledDiveListIgnoreTimeStampEndFilter,
			String scheduledDiveListTimeStampStartFilter, String scheduledDiveListTimeStampEndFilter,
			String scheduledDiveListDistanceLimit,
			String scheduledDiveListStartIndexLoad, String scheduledDiveListCountLoad) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(DATE_CREATED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_SUBMITTER_ID, String.valueOf(submitterID));
		params.put(ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_USER_ID, String.valueOf(userAttendingID));
		params.put(ScheduledDiveDiveSite.TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_ID, String.valueOf(diveSiteID));
		params.put(CURRENT_LATITUDE_PARAM, scheduledDiveListCurrentLatitude);
		params.put(CURRENT_LONGITUDE_PARAM, scheduledDiveListCurrentLongitude);
		params.put(DiveSite.DIVE_SITE_TITLE_PARAM, scheduledDiveListTitleFilter);
		params.put(DiveSite.DIVE_SITE_COUNTRY_PARAM, scheduledDiveListCountryFilter);
		params.put(DiveSite.DIVE_SITE_PROVINCE_PARAM, scheduledDiveListStateFilter);
		params.put(DiveSite.DIVE_SITE_CITY_PARAM, scheduledDiveListCityFilter);
		params.put(ScheduledDive.TAG_IGNORE_TIMESTAMP_START, scheduledDiveListIgnoreTimeStampStartFilter);
		params.put(ScheduledDive.TAG_IGNORE_TIMESTAMP_END, scheduledDiveListIgnoreTimeStampEndFilter);
		params.put(ScheduledDive.TAG_TIMESTAMP_START, scheduledDiveListTimeStampStartFilter);
		params.put(ScheduledDive.TAG_TIMESTAMP_END, scheduledDiveListTimeStampEndFilter);
		params.put(DiveSite.DIVE_SITE_DISTANCE_PARAM, scheduledDiveListDistanceLimit);
		params.put(START_INDEX_LOAD_PARAM, scheduledDiveListStartIndexLoad);
		params.put(COUNT_LOAD_PARAM, scheduledDiveListCountLoad);

		// getting JSON string from URL
		mJSONParser.setJSONParserListener(new JSONParserListener() {

			@Override
			public void onJSONParserObjectAvailable(JSONObject json) {

				Log.d("Online Sched. Dives: ", json.toString());

				try {
					// Checking for SUCCESS TAG
					int success = json.getInt(JSON_TAG_SUCCESS);

					if (success == 1) {
						// Scheduled Dives found
						ArrayList<ScheduledDive> scheduledDives = getScheduledDivesJSON(json, JSON_TAG_SCHEDULEDDIVES);
						for (int i = 0; i < scheduledDives.size(); i++) {
							ScheduledDive scheduledDive = scheduledDives.get(i);
							
							mResultList.add(scheduledDive);
							publishProgress(scheduledDive);
						}
					} else {
						mIsError = true;
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		try {
			mJSONParser.makeHttpRequestJSONLines(url_get_scheduleddives, URL_GET, params);
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}
	}

	private void publishScheduledDiveBackground(int scheduledDiveFieldPositionIncrement,
			String[] args) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_ONLINE_ID_INDEX]);
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_TITLE,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_TITLE_INDEX]);
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_SUBMITTER_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_SUBMITTER_ID_INDEX]);
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_TIMESTAMP,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_TIMESTAMP_INDEX]);
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_COMMENT,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_COMMENT_INDEX]);
		
		int scheduledDiveDiveSitesIndexStart = scheduledDiveFieldPositionIncrement
				+ Integer.valueOf(args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_DIVE_SITE_DATA_START_POS_INDEX]);
		int scheduledDiveDiveSitesCount = Integer
				.valueOf(args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_DIVE_SITE_DATA_COUNT_INDEX]);
		
		// Scheduled Dive Dive Site Parameters
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_DIVE_SITE_DATA_COUNT_PARAM, String.valueOf(scheduledDiveDiveSitesCount));
		for (int i = 0; i < scheduledDiveDiveSitesCount; i++) {
			params.put(
					ScheduledDiveDiveSite.TAG_SCHEDULED_DIVE_DIVE_SITE_ID + "_" + i,
					args[scheduledDiveDiveSitesIndexStart + i
							* ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT
							+ ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_ONLINE_ID_INDEX]);
			params.put(
					ScheduledDiveDiveSite.TAG_SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID + "_" + i,
					args[scheduledDiveDiveSitesIndexStart + i
							* ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT
							+ ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID_INDEX]);
			params.put(
					ScheduledDiveDiveSite.TAG_SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_ID + "_" + i,
					args[scheduledDiveDiveSitesIndexStart + i
							* ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT
							+ ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_ONLINE_ID_INDEX]);
			params.put(
					ScheduledDiveDiveSite.TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_ID + "_" + i,
					args[scheduledDiveDiveSitesIndexStart + i
							* ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT
							+ ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_SITE_ONLINE_ID_INDEX]);
		}
		
		int scheduledDiveUsersIndexStart = scheduledDiveFieldPositionIncrement
				+ Integer.valueOf(args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_USER_DATA_START_POS_INDEX]);
		int scheduledDiveUsersCount = Integer
				.valueOf(args[scheduledDiveFieldPositionIncrement
						+ ScheduledDive.SCHEDULED_DIVE_USER_DATA_COUNT_INDEX]);
		
		// Scheduled Dive Dive User Parameters
		params.put(
				ScheduledDive.TAG_SCHEDULED_DIVE_USER_DATA_COUNT_PARAM, String
						.valueOf(scheduledDiveUsersCount));
		for (int i = 0; i < scheduledDiveUsersCount; i++) {
			params.put(
					ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_ID + "_" + i,
					args[scheduledDiveUsersIndexStart + i
							* ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT
							+ ScheduledDiveUser.SCHEDULED_DIVE_USER_ONLINE_ID_INDEX]);
			params.put(
					ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_LOCAL_ID + "_" + i,
					args[scheduledDiveUsersIndexStart + i
							* ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT
							+ ScheduledDiveUser.SCHEDULED_DIVE_USER_LOCAL_ID_INDEX]);
			params.put(
					ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ID + "_" + i,
					args[scheduledDiveUsersIndexStart + i
							* ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT
							+ ScheduledDiveUser.SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ONLINE_ID_INDEX]);
			params.put(
					ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ID + "_" + i,
					args[scheduledDiveUsersIndexStart + i
							* ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT
							+ ScheduledDiveUser.SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ONLINE_ID_INDEX]);
			params.put(
					ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_USER_ID + "_" + i,
					args[scheduledDiveUsersIndexStart + i
							* ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT
							+ ScheduledDiveUser.SCHEDULED_DIVE_USER_USER_ID_INDEX]);
			params.put(
					ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_ATTEND_STATE + "_" + i,
					args[scheduledDiveUsersIndexStart + i
							* ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT
							+ ScheduledDiveUser.SCHEDULED_DIVE_USER_ATTEND_STATE_INDEX]);
		}

		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_publish_scheduleddive, URL_POST, params);

			// Check your log cat for JSON response
			Log.d("Publish Scheduled Dive:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					ArrayList<ScheduledDive> scheduledDives = getScheduledDivesJSON(json, JSON_TAG_SCHEDULEDDIVES);

					// Should have one scheduled dive if successful
					if (scheduledDives.size() > 0) {
						ScheduledDive scheduledDive = scheduledDives.get(0);
						scheduledDive.setLocalId(Integer.valueOf(args[scheduledDiveFieldPositionIncrement + 
						                                              ScheduledDive.SCHEDULED_DIVE_LOCAL_ID_INDEX]));

						mResultList.add(scheduledDive);
						publishProgress(scheduledDive);
					}
					
					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.publish_scheduleddive_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			publishProgress(null);
		}
	}
	
	private void setScheduledDiveUserBackground(int scheduledDiveFieldPositionIncrement,
			String[] args) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_ONLINE_ID_INDEX]);
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_LOCAL_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_LOCAL_ID_INDEX]);
		String scheduledDiveOnlineId = args[scheduledDiveFieldPositionIncrement
		            						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ONLINE_ID_INDEX];
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ID,
				scheduledDiveOnlineId);
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID_INDEX]);
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ONLINE_ID_INDEX]);
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID_INDEX]);
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_USER_ID,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_USER_ID_INDEX]);
		params.put(
				ScheduledDiveUser.TAG_SCHEDULED_DIVE_USER_ATTEND_STATE,
				args[scheduledDiveFieldPositionIncrement
						+ ScheduledDiveUser.SCHEDULED_DIVE_USER_ATTEND_STATE_INDEX]);
		
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_set_scheduleddiveuser, URL_POST, params);

			// Check your log cat for JSON response
			Log.d("Set Sched. Dive User:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					ArrayList<ScheduledDiveUser> scheduledDiveUsers = 
							getScheduledDiveUsersJSON(json, 
									JSON_TAG_SCHEDULEDDIVEUSER_SCHEDULEDDIVE + scheduledDiveOnlineId);

					// Should have one scheduled dive user if successful
					if (scheduledDiveUsers.size() > 0) {
						ScheduledDiveUser scheduledDive = scheduledDiveUsers.get(0);
						mResultList.add(scheduledDive);
						publishProgress(scheduledDive);
					}
					
					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.update_scheduleddive_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			publishProgress(null);
		}
	}

	private void checkDiverLoginBackground(String username, String password) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(USERNAME_PARAM, username);
		params.put(PASSWORD_PARAM, password);

		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_check_login, URL_POST,
					params);

			// Check your log cat for JSON response
			Log.d("Diver: ", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Diver found
					JSONArray resultJSON = json.getJSONArray(JSON_TAG_DIVERS);

					// Should only have one diver returned
					if (resultJSON.length() > 0) {
						JSONObject diverJSON = resultJSON.getJSONObject(0);
						Diver diver = new Diver(diverJSON);

						mResultList.add(diver);
					}

				} else {
					mIsError = true;
				}

				if (json.has(JSON_TAG_MESSAGE)) {
					mMessage = json.getString(JSON_TAG_MESSAGE);
				} else {
					mMessage = "";
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void createDiverBackground(String username, String password,
			String email, String firstName, String lastName, String country,
			String province, String city, String picture, String approved) {

		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(USERNAME_PARAM, username);
		params.put(PASSWORD_PARAM, password);
		params.put(EMAIL_PARAM, email);
		params.put(FIRSTNAME_PARAM, firstName);
		params.put(LASTNAME_PARAM, lastName);
		params.put(COUNTRY_PARAM, country);
		params.put(PROVINCE_PARAM, province);
		params.put(CITY_PARAM, city);
		params.put(PICTURE_URL_PARAM, picture);
		params.put(APPROVED_PARAM, approved);

		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_create_diver, URL_POST,
					params);

			// Check your log cat for JSON response
			Log.d("Diver: ", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Diver found
					JSONArray resultJSON = json.getJSONArray(JSON_TAG_DIVERS);

					// Should only have one diver returned
					if (resultJSON.length() > 0) {
						JSONObject diverJSON = resultJSON.getJSONObject(0);
						Diver diver = new Diver(diverJSON);

						mResultList.add(diver);
					}

				} else {
					mIsError = true;
				}

				if (json.has(JSON_TAG_MESSAGE)) {
					mMessage = json.getString(JSON_TAG_MESSAGE);
				} else {
					mMessage = "";
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveDiverBackground(String username, String password,
			String email, String firstName, String lastName, String country,
			String province, String city, String bio, String isMod,
			String userID, ArrayList<String[]> certifications,
			String profileNewImageFilePath) {

		// Encode profile image
		String encodedImage = "";
		if (profileNewImageFilePath != null) {
			Bitmap bm = LoadFileImageTask
					.getImageFromFile(profileNewImageFilePath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
			byte[] b = baos.toByteArray();

			encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
		}

		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(USERNAME_PARAM, username);
		params.put(PASSWORD_PARAM, password);
		params.put(EMAIL_PARAM, email);
		params.put(FIRSTNAME_PARAM, firstName);
		params.put(LASTNAME_PARAM, lastName);
		params.put(COUNTRY_PARAM, country);
		params.put(PROVINCE_PARAM, province);
		params.put(CITY_PARAM, city);
		params.put(BIO_PARAM, bio);
		params.put(IS_MOD_PARAM, bio);
		params.put(USER_ID_PARAM, userID);
		params.put(NEW_PICTURE_PARAM, encodedImage);

		int ValidCertCount = 0;
		for (int i = 0; i < certifications.size(); i++) {
			// No need to save if description fields are empty
			if (!certifications.get(i)[DiverCertification.CERTIF_TITLE_INDEX]
					.trim().isEmpty()
					|| !certifications.get(i)[DiverCertification.CERTIF_NUMBER_INDEX]
							.trim().isEmpty()
					|| !certifications.get(i)[DiverCertification.CERTIF_LOCATION_INDEX]
							.trim().isEmpty()
					|| !certifications.get(i)[DiverCertification.CERTIF_DATE_INDEX]
							.trim().isEmpty()) {

				ValidCertCount = ValidCertCount + 1;
				params.put(
						CERTIF_ID_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_ID_INDEX]);
				params.put(
						CERTIF_USER_ID_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_USER_ID_INDEX]);
				params.put(
						CERTIF_TITLE_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_TITLE_INDEX]);
				params.put(
						CERTIF_NUMBER_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_NUMBER_INDEX]);
				params.put(
						CERTIF_LOATION_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_LOCATION_INDEX]);
				params.put(
						CERTIF_DATE_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_DATE_INDEX]);
				params.put(
						CERTIF_PRIMARY_PARAM + i,
						certifications.get(i)[DiverCertification.CERTIF_PRIMARY_INDEX]);
			}
		}

		params.put(CERTIF_COUNT_PARAM, String.valueOf(ValidCertCount));

		// getting JSON string from URL
		JSONObject json = null;
		try {
			// getting JSON string from URL
			json = mJSONParser.makeHttpRequest(url_save_user, URL_POST, params);

			// Check your log cat for JSON response
			Log.d("Diver: ", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Diver found
					JSONArray resultJSON = json.getJSONArray(JSON_TAG_DIVERS);

					// Should only have one diver returned
					if (resultJSON.length() > 0) {
						JSONObject diverJSON = resultJSON.getJSONObject(0);
						Diver diver = new Diver(diverJSON);

						// Add any certifications exist
						resultJSON = json
								.getJSONArray(JSON_TAG_DIVER_CERTIFICATIONS_DIVER
										+ String.valueOf(diver.getOnlineId()));
						for (int i = 0; i < resultJSON.length(); i++) {
							diver.addJSONCertification(resultJSON
									.getJSONObject(i));
						}

						mResultList.add(diver);
					} else {
						mIsError = true;
					}

				} else {
					mIsError = true;
				}

				if (json.has(JSON_TAG_MESSAGE)) {
					mMessage = json.getString(JSON_TAG_MESSAGE);
				} else {
					mMessage = "";
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void getDiverBackground(String userID, String username, String email) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(USER_ID_PARAM, userID);
		params.put(USERNAME_PARAM, username);
		params.put(EMAIL_PARAM, email);

		// getting JSON string from URL
		JSONObject json = null;
		try {
			// getting JSON string from URL
			json = mJSONParser.makeHttpRequest(url_get_diver, URL_GET, params);

			// Check your log cat for JSON response
			Log.d("Diver: ", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Diver found
					JSONArray resultJSON = json.getJSONArray(JSON_TAG_DIVERS);

					// Should only have one diver returned
					if (resultJSON.length() > 0) {
						JSONObject diverJSON = resultJSON.getJSONObject(0);
						Diver diver = new Diver(diverJSON);

						// Add any certifications exist
						resultJSON = json
								.getJSONArray(JSON_TAG_DIVER_CERTIFICATIONS_DIVER
										+ String.valueOf(diver.getOnlineId()));
						for (int i = 0; i < resultJSON.length(); i++) {
							diver.addJSONCertification(resultJSON
									.getJSONObject(i));
						}

						mResultList.add(diver);
					}

				} else {
					mIsError = true;
				}

				if (json.has(JSON_TAG_MESSAGE)) {
					mMessage = json.getString(JSON_TAG_MESSAGE);
				} else {
					mMessage = "";
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void getDiverListBackground(long lastRefreshDateTime,
			String diverListNameFilter, String diverListCountryFilter,
			String diverListStateFilter, String diverListCityFilter,
			String diverListStartIndexLoad, String diverListCountLoad) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(DATE_CREATED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(NAME_PARAM, diverListNameFilter);
		params.put(COUNTRY_PARAM, diverListCountryFilter);
		params.put(PROVINCE_PARAM, diverListStateFilter);
		params.put(CITY_PARAM, diverListCityFilter);
		params.put(START_INDEX_LOAD_PARAM, diverListStartIndexLoad);
		params.put(COUNT_LOAD_PARAM, diverListCountLoad);

		// getting JSON string from URL
		mJSONParser.setJSONParserListener(new JSONParserListener() {

			@Override
			public void onJSONParserObjectAvailable(JSONObject json) {
				// Process JSON Object as a single dive site
				Log.d("Online Divers: ", json.toString());

				try {
					// Checking for SUCCESS TAG
					int success = json.getInt(JSON_TAG_SUCCESS);

					if (success == 1) {
						// Divers found
						if (json.has(JSON_TAG_DIVERS)) {
							JSONArray diverResultJSON = json
									.getJSONArray(JSON_TAG_DIVERS);

							// Loop through Divers retrieved
							for (int i = 0; i < diverResultJSON.length(); i++) {
								JSONObject diverJSON = diverResultJSON
										.getJSONObject(i);
								Diver diver = new Diver(diverJSON);
								diver.setLocalId(mDiveSiteManager
										.getDiverLocalId(diver.getOnlineId()));

								// Add any certifications exist
								JSONArray certResultJSON = json
										.getJSONArray(JSON_TAG_DIVER_CERTIFICATIONS_DIVER
												+ String.valueOf(diver
														.getOnlineId()));
								for (int j = 0; j < certResultJSON.length(); j++) {
									// Following will make sure that cert's user
									// ID matches before adding
									DiverCertification cert = new DiverCertification(
											certResultJSON.getJSONObject(j));
									cert.setLocalId(mDiveSiteManager
											.getDiverCertificationLocalId(cert
													.getOnlineId()));
									diver.addCertification(cert);
								}

								mResultList.add(diver);
								publishProgress(diver);
							}
						}
					} else {
						mIsError = true;
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		try {
			mJSONParser.makeHttpRequestJSONLines(url_get_divers, URL_GET,
					params);
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}
	}

	private void getUserPictureBackground(String userID, String username,
			String email) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(USER_ID_PARAM, userID);
		params.put(USERNAME_PARAM, username);
		params.put(EMAIL_PARAM, email);

		// getting JSON string from URL
		JSONObject json = null;
		try {
			// getting JSON string from URL
			json = mJSONParser.makeHttpRequest(url_get_diver_picture, URL_GET,
					params);

			// Check your log cat for JSON response
			Log.d("Diver Picture: ", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Diver Picture found
					JSONArray resultJSON = json.getJSONArray(JSON_TAG_DIVERS);

					// Should only have one picture returned
					if (resultJSON.length() > 0) {
						JSONObject resultJSONObject = resultJSON
								.getJSONObject(0);
						Bitmap userPicture = null;

						String bitmap = resultJSONObject
								.getString(Diver.JSON_TAG_DIVER_PICTURE);
						if (!bitmap.isEmpty()) {
							try {
								byte[] encodeByte = Base64.decode(bitmap,
										Base64.DEFAULT);
								userPicture = BitmapFactory.decodeByteArray(
										encodeByte, 0, encodeByte.length);
							} catch (Exception e) {
								e.getMessage();
							}
						}

						mResultList.add(userPicture);
					}

				} else {
					mIsError = true;
				}

				if (json.has(JSON_TAG_MESSAGE)) {
					mMessage = json.getString(JSON_TAG_MESSAGE);
				} else {
					mMessage = "";
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void getNDBCStationsBackground(String minLatitude,
			String maxLatitude, String minLongitude, String maxLongitude,
			String stationUpdated, String minLastUpdateTimestamp,
			String currentLatitude, String currentLongitude,
			String distanceFromCurrent, String startIndexLoad, String countLoad) {
		ContentValues params = new ContentValues();
		params.put(DiveSite.DIVE_SITE_MIN_LATITUDE_PARAM, minLatitude);
		params.put(DiveSite.DIVE_SITE_MAX_LATITUDE_PARAM, maxLatitude);
		params.put(DiveSite.DIVE_SITE_MIN_LONGITUDE_PARAM, minLongitude);
		params.put(DiveSite.DIVE_SITE_MAX_LONGITUDE_PARAM, maxLongitude);
		params.put(STATION_UPDATED_PARAM, stationUpdated);
		params.put(NDBCStation.TAG_MIN_LAST_UPDATE_TIMESTAMP, minLastUpdateTimestamp);
		params.put(CURRENT_LATITUDE_PARAM, currentLatitude);
		params.put(CURRENT_LONGITUDE_PARAM, currentLongitude);
		params.put(DISTANCE_FROM_CURRENT_PARAM, distanceFromCurrent);
		params.put(START_INDEX_LOAD_PARAM, startIndexLoad);
		params.put(COUNT_LOAD_PARAM, countLoad);

		// getting JSON string from URL
		mJSONParser.setJSONParserListener(new JSONParserListener() {

			@Override
			public void onJSONParserObjectAvailable(JSONObject json) {
				Log.d("NDBC Stations ", json.toString());

				try {
					// Checking for SUCCESS TAG
					int success = json.getInt(JSON_TAG_SUCCESS);

					if (success == 1) {
						JSONArray resultJSON = json
								.getJSONArray(JSON_TAG_NDBC_STATIONS);

						// Loop through Dive Sites retrieved
						for (int i = 0; i < resultJSON.length(); i++) {
							JSONObject ndbcStationJSON = resultJSON
									.getJSONObject(i);
							NDBCStation ndbcStation = new NDBCStation(
									ndbcStationJSON);

							mResultList.add(ndbcStation);
							publishProgress(ndbcStation);
						}
					} else {
						mIsError = true;
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});

		try {
			mJSONParser.makeHttpRequestJSONLines(url_get_NDBC_stations,
					URL_GET, params);
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}
	}

	private void updateNDBCDataForStationBackground(String stationID,
			String maxDataRecords) {
		ContentValues params = new ContentValues();
		params.put(STATION_ID_PARAM, stationID);
		params.put(MAX_DATA_RECORDS_PARAM, maxDataRecords);

		// getting JSON string from URL
		JSONObject json = null;
		try {
			// getting JSON string from URL
			json = mJSONParser.makeHttpRequest(url_get_NDBC_data_for_station,
					URL_GET, params);

			// Check your log cat for JSON response
			Log.d("NDBC Stations ", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		// Check your log cat for JSON response
		if (json != null) {
			Log.d("NDBC Data ", json.toString());

			try {
				// Checking for SUCCESS tags for each type of data
				int meteorologicalDataSuccess = json
						.getInt(JSON_TAG_METEOROLOGICAL_DATA_SUCCESS);
				int spectralWaveDataSuccess = json
						.getInt(JSON_TAG_SPECTRAL_WAVE_DATA_SUCCESS);
				int driftingBuoyDataSuccess = json
						.getInt(JSON_TAG_DRIFTING_BUOY_DATA_SUCCESS);
				int oceanicDataSuccess = json
						.getInt(JSON_TAG_OCEANIC_DATA_SUCCESS);

				if (meteorologicalDataSuccess == 0
						&& spectralWaveDataSuccess == 0
						&& driftingBuoyDataSuccess == 0
						&& oceanicDataSuccess == 0) {

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}
				} else {
					NDBCStation ndbcStation = (NDBCStation) mUpdateObject;

					// Clear current station's data
					ndbcStation.clearMeteorologicalData();
					ndbcStation.clearDriftingBuoyData();
					ndbcStation.clearOceanicData();
					ndbcStation.clearSpectralWaveData();

					// Set update time for station
					ndbcStation.setLastUserUpdate(new Date());

					// Get meteorological data retrieved
					JSONArray resultJSON = json
							.getJSONArray(JSON_TAG_METEOROLOGICAL_DATA);

					for (int i = 0; i < resultJSON.length(); i++) {
						JSONObject stationDataJSON = resultJSON
								.getJSONObject(i);
						ndbcStation.addMeteorologicalData(stationDataJSON);
					}

					// Get spectral wave data retrieved
					resultJSON = json.getJSONArray(JSON_TAG_SPECTRAL_WAVE_DATA);

					for (int i = 0; i < resultJSON.length(); i++) {
						JSONObject stationDataJSON = resultJSON
								.getJSONObject(i);
						ndbcStation.addSpectralWaveData(stationDataJSON);
					}

					// Get drifting buoy data retrieved
					resultJSON = json.getJSONArray(JSON_TAG_DRIFTING_BUOY_DATA);

					for (int i = 0; i < resultJSON.length(); i++) {
						JSONObject stationDataJSON = resultJSON
								.getJSONObject(i);
						ndbcStation.addDriftingBuoyData(stationDataJSON);
					}

					// Get oceanic data retrieved
					resultJSON = json.getJSONArray(JSON_TAG_OCEANIC_DATA);

					for (int i = 0; i < resultJSON.length(); i++) {
						JSONObject stationDataJSON = resultJSON
								.getJSONObject(i);
						ndbcStation.addOceanicData(stationDataJSON);
					}

					mResultList.add(ndbcStation);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getScheduledDiveBackground(long lastRefreshDateTime, long scheduledDiveOnlineID) {
		// Building Parameters
		ContentValues params = new ContentValues();

        params.put(LAST_MODIFIED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(ScheduledDive.TAG_SCHEDULED_DIVE_ID, String.valueOf(scheduledDiveOnlineID));
		
		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_get_scheduleddive, URL_GET, params);

			// Check your log cat for JSON response
			Log.d("Scheduled Dive:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Scheduled Dives found
					ArrayList<ScheduledDive> scheduledDives = 
							getScheduledDivesJSON(json, JSON_TAG_SCHEDULEDDIVES);
				
					// Should have one dive log if successful
					if (scheduledDives.size() > 0) {
						ScheduledDive scheduledDive = scheduledDives.get(0);

						mResultList.add(scheduledDive);
						publishProgress(scheduledDive);
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.online_communication_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (mResultList.size() == 0) {
            if (mUpdateObject != null) {
                mResultList.add(mUpdateObject);
            } else {
                // No update found, return local one
                long localID = mDiveSiteManager.getScheduledDiveLocalId(scheduledDiveOnlineID);
                ScheduledDive scheduledDive = mDiveSiteManager.getScheduledDive(localID);
                if (scheduledDive != null) {
                    mResultList.add(scheduledDive);
                }
            }
		}
	}

	private void getDiveLogListBackground(long lastRefreshDateTime,
			long diverID, long diveSiteID,
            String diveLogListMinLatitudeFilter, String diveLogListMaxLatitudeFilter,
            String diveLogListMinLongitudeFilter, String diveLogListMaxLongitudeFilter,
			String diveLogListStartIndexLoad, String diveLogListCountLoad) {
		// Building Parameters
		ContentValues params = new ContentValues();
		params.put(DATE_CREATED_URL_PARAM, String.valueOf(lastRefreshDateTime));
		params.put(DiveLog.DIVE_LOG_USER_ID_PARAM, String.valueOf(diverID));
		params.put(DiveLog.DIVE_LOG_DIVE_SITE_ID_PARAM, String.valueOf(diveSiteID));
        params.put(DiveLog.DIVE_LOG_MIN_LATITUDE_PARAM, diveLogListMinLatitudeFilter);
        params.put(DiveLog.DIVE_LOG_MAX_LATITUDE_PARAM, diveLogListMaxLatitudeFilter);
        params.put(DiveLog.DIVE_LOG_MIN_LONGITUDE_PARAM, diveLogListMinLongitudeFilter);
        params.put(DiveLog.DIVE_LOG_MAX_LONGITUDE_PARAM, diveLogListMaxLongitudeFilter);
		params.put(DiveLog.DIVE_LOG_START_INDEX_LOAD_PARAM, diveLogListStartIndexLoad);
		params.put(DiveLog.DIVE_LOG_COUNT_LOAD_PARAM, diveLogListCountLoad);

		// getting JSON string from URL
		mJSONParser.setJSONParserListener(new JSONParserListener() {

			@Override
			public void onJSONParserObjectAvailable(JSONObject json) {
				// Process JSON Object as a single dive site
				Log.d("Online Dive Logs: ", json.toString());

				try {
					// Checking for SUCCESS TAG
					int success = json.getInt(JSON_TAG_SUCCESS);

					if (success == 1) {
						// Dive Logs found

						ArrayList<DiveLog> diveLogs = getDiveLogsJSON(json, JSON_TAG_DIVELOGS);
						for (int i = 0; i < diveLogs.size(); i++) {
							DiveLog diveLog = diveLogs.get(i);
							
							// Set the Dive Site found
							ArrayList<DiveSite> diveSites = 
								getDiveSitesJSON(json, JSON_TAG_DIVESITES_LOG + String.valueOf(diveLog.getOnlineId()));
							
							for (int j = 0; j < diveSites.size(); j++) {
								DiveSite diveSite = diveSites.get(j);
								diveLog.setDiveSite(diveSite);
							}

							mResultList.add(diveLog);
							publishProgress(diveLog);
						}

					} else {
						mIsError = true;
					}

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		try {
			mJSONParser.makeHttpRequestJSONLines(url_get_divelogs, URL_GET,
					params);
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}
	}

	private void publishDiveLogBackground(int diveLogFieldPositionIncrement,
			String[] args) {
		// Building Parameters
		// Dive Log Parameters
		ContentValues params = new ContentValues();
		params.put(DiveLog.DIVE_LOG_ID_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_ONLINE_ID_INDEX]);
		params.put(DiveLog.DIVE_LOG_DIVE_SITE_ID_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_DIVE_SITE_ID_INDEX]);
		params.put(DiveLog.DIVE_LOG_USER_ID_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_USER_ID_INDEX]);
		params.put(DiveLog.DIVE_LOG_TIMESTAMP_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_TIMESTAMP_INDEX]);
		params.put(DiveLog.DIVE_LOG_AIR_TYPE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_AIR_TYPE_INDEX]);
		params.put(DiveLog.DIVE_LOG_START_PRESSURE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_START_PRESSURE_INDEX]);
        params.put(DiveLog.TAG_DIVE_LOG_START_AIR_VALUE,
                args[diveLogFieldPositionIncrement
                        + DiveLog.DIVE_LOG_START_AIR_VALUE_INDEX]);
        params.put(DiveLog.TAG_DIVE_LOG_START_AIR_UNITS,
                args[diveLogFieldPositionIncrement
                        + DiveLog.DIVE_LOG_START_AIR_UNITS_INDEX]);
        params.put(DiveLog.TAG_DIVE_LOG_END_AIR_VALUE,
                args[diveLogFieldPositionIncrement
                        + DiveLog.DIVE_LOG_END_AIR_VALUE_INDEX]);
        params.put(DiveLog.TAG_DIVE_LOG_END_AIR_UNITS,
                args[diveLogFieldPositionIncrement
                        + DiveLog.DIVE_LOG_END_AIR_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_END_PRESSURE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_END_PRESSURE_INDEX]);
		params.put(DiveLog.DIVE_LOG_DIVE_TIME_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_DIVE_TIME_INDEX]);
		params.put(DiveLog.DIVE_LOG_MAX_DEPTH_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_MAX_DEPTH_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_MAX_DEPTH_UNITS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_MAX_DEPTH_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_AVERAGE_DEPTH_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_AVERAGE_DEPTH_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_AVERAGE_DEPTH_UNITS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_AVERAGE_DEPTH_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_SURFACE_TEMPERATURE_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_SURFACE_TEMPERATURE_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_SURFACE_TEMPERATURE_UNITS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_SURFACE_TEMPERATURE_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_WATER_TEMPERATURE_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_WATER_TEMPERATURE_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_WATER_TEMPERATURE_UNITS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_WATER_TEMPERATURE_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_VISIBILITY_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_VISIBILITY_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_VISIBILITY_UNITS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_VISIBILITY_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_WEIGHTS_REQUIRED_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_WEIGHTS_REQUIRED_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_WEIGHTS_REQUIRED_UNITS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_WEIGHTS_REQUIRED_UNITS_INDEX]);
		params.put(DiveLog.DIVE_LOG_SURFACE_TIME_VALUE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_SURFACE_TIME_VALUE_INDEX]);
		params.put(DiveLog.DIVE_LOG_RATING_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_RATING_INDEX]);
		params.put(DiveLog.DIVE_LOG_COMMENTS_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_COMMENTS_INDEX]);
		params.put(DiveLog.DIVE_LOG_IS_COURSE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_IS_COURSE_INDEX]);
		params.put(DiveLog.DIVE_LOG_IS_PHOTO_VIDEO_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_IS_PHOTO_VIDEO_INDEX]);
		params.put(DiveLog.DIVE_LOG_IS_ICE_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_IS_ICE_INDEX]);
		params.put(DiveLog.DIVE_LOG_IS_DEEP_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_IS_DEEP_INDEX]);
		params.put(DiveLog.DIVE_LOG_IS_INSTRUCTING_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_IS_INSTRUCTING_INDEX]);
		params.put(DiveLog.DIVE_LOG_IS_NIGHT_PARAM,
				args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_IS_NIGHT_INDEX]);

		int diveLogBuddiesIndexStart = diveLogFieldPositionIncrement
				+ Integer.valueOf(args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_BUDDY_DATA_START_POS_INDEX]);
		int diveLogBuddyCount = Integer
				.valueOf(args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_BUDDY_DATA_COUNT_INDEX]);

		// Dive Log Buddies Parameters
		params.put(
				DiveLog.DIVE_LOG_BUDDY_DATA_COUNT_PARAM, String
						.valueOf(diveLogBuddyCount));
		for (int i = 0; i < diveLogBuddyCount; i++) {
			params.put(
					DiveLogBuddy.DIVE_LOG_BUDDY_ID_PARAM + i,
					args[diveLogBuddiesIndexStart + i
							* DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT
							+ DiveLogBuddy.DIVE_LOG_BUDDY_ONLINE_ID_INDEX]);
			params.put(
					DiveLogBuddy.DIVE_LOG_BUDDY_LOCAL_ID_PARAM + i,
					args[diveLogBuddiesIndexStart + i
							* DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT
							+ DiveLogBuddy.DIVE_LOG_BUDDY_LOCAL_ID_INDEX]);
			params.put(
					DiveLogBuddy.DIVE_LOG_BUDDY_LOG_ID_PARAM + i,
					args[diveLogBuddiesIndexStart + i
							* DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT
							+ DiveLogBuddy.DIVE_LOG_BUDDY_LOG_ID_INDEX]);
			params.put(
					DiveLogBuddy.DIVE_LOG_BUDDY_USER_ID_PARAM + i,
					args[diveLogBuddiesIndexStart + i
							* DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT
							+ DiveLogBuddy.DIVE_LOG_BUDDY_DIVER_ID_INDEX]);
			params.put(
					DiveLogBuddy.DIVE_LOG_BUDDY_USERNAME_PARAM + i,
					args[diveLogBuddiesIndexStart + i
							* DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT
							+ DiveLogBuddy.DIVE_LOG_BUDDY_DIVER_USERNAME_INDEX]);
		}
		
		int diveLogStopsIndexStart = diveLogFieldPositionIncrement
				+ Integer.valueOf(args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_STOP_DATA_START_POS_INDEX]);
		int diveLogStopCount = Integer
				.valueOf(args[diveLogFieldPositionIncrement
						+ DiveLog.DIVE_LOG_STOP_DATA_COUNT_INDEX]);

		// Dive Log Stops Parameters
		params.put(
				DiveLog.DIVE_LOG_STOP_DATA_COUNT_PARAM, String
						.valueOf(diveLogStopCount));
		for (int i = 0; i < diveLogStopCount; i++) {
			params.put(
					DiveLogStop.DIVE_LOG_STOP_ID_PARAM + i,
					args[diveLogStopsIndexStart + i
							* DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT
							+ DiveLogStop.DIVE_LOG_STOP_ONLINE_ID_INDEX]);
			params.put(
					DiveLogStop.DIVE_LOG_STOP_LOCAL_ID_PARAM + i,
					args[diveLogStopsIndexStart + i
							* DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT
							+ DiveLogStop.DIVE_LOG_STOP_LOCAL_ID_INDEX]);
			params.put(
					DiveLogStop.DIVE_LOG_STOP_LOG_ID_PARAM + i,
					args[diveLogStopsIndexStart + i
							* DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT
							+ DiveLogStop.DIVE_LOG_STOP_LOG_ID_INDEX]);
			params.put(
					DiveLogStop.DIVE_LOG_STOP_TIME_PARAM + i,
					args[diveLogStopsIndexStart + i
							* DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT
							+ DiveLogStop.DIVE_LOG_STOP_TIME_INDEX]);
			params.put(
					DiveLogStop.DIVE_LOG_STOP_DEPTH_VALUE_PARAM + i,
					args[diveLogStopsIndexStart + i
							* DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT
							+ DiveLogStop.DIVE_LOG_STOP_DEPTH_VALUE_INDEX]);
			params.put(
					DiveLogStop.DIVE_LOG_STOP_DEPTH_UNITS_PARAM + i,
					args[diveLogStopsIndexStart + i
							* DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT
							+ DiveLogStop.DIVE_LOG_STOP_DEPTH_UNITS_INDEX]);
		}

		// getting JSON string from URL
		JSONObject json = null;
		try {
			json = mJSONParser.makeHttpRequest(url_publish_divelog, URL_POST, params);

			// Check your log cat for JSON response
			Log.d("Publish Dive Log:", json.toString());
		} catch (Exception e) {
			mMessage = mContext.getResources().getString(
					R.string.online_communication_error);
			e.printStackTrace();
		}

		if (json != null) {
			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(JSON_TAG_SUCCESS);

				if (success == 1) {
					// Dive Logs found
					ArrayList<DiveLog> diveLogs = getDiveLogsJSON(json, JSON_TAG_DIVELOGS);

					// Should have one dive log if successful
					if (diveLogs.size() > 0) {
						DiveLog diveLog = diveLogs.get(0);
						diveLog.setLocalId(Integer.valueOf(args[diveLogFieldPositionIncrement + DiveLog.DIVE_LOG_LOCAL_ID_INDEX]));
													
						// Set the Dive Sites found
						ArrayList<DiveSite> diveSites = 
							getDiveSitesJSON(json, JSON_TAG_DIVESITES_LOG + String.valueOf(diveLog.getOnlineId()));
						
						for (int i = 0; i < diveSites.size(); i++) {
							DiveSite diveSite = diveSites.get(i);
							diveLog.setDiveSite(diveSite);									
						}

						mResultList.add(diveLog);
						publishProgress(diveLog);
					}
					
					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = "";
					}

				} else {
					mIsError = true;

					publishProgress((Object) null);

					if (json.has(JSON_TAG_MESSAGE)) {
						mMessage = json.getString(JSON_TAG_MESSAGE);
					} else {
						mMessage = mContext.getResources().getString(
								R.string.publish_divelog_error);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			publishProgress(null);
		}
	}
	
	private ArrayList<DiveSite> getDiveSitesJSON(JSONObject json, String JSONName) {
		ArrayList<DiveSite> diveSites = new ArrayList<DiveSite>();
		
		try {
			if (json.has(JSONName)) {
				JSONArray diveSiteResultJSON = json.getJSONArray(JSONName);
		
				for (int i = 0; i < diveSiteResultJSON.length(); i++) {
					JSONObject diveSiteJSON = diveSiteResultJSON.getJSONObject(i);
					DiveSite diveSite = new DiveSite(diveSiteJSON);
					diveSite.setLocalId(mDiveSiteManager.getDiveSiteLocalId(diveSite.getOnlineId()));
					diveSite.setPublished(true);
					
					diveSites.add(diveSite);
			
					// Add Dive Site Pictures found
					if (json.has(JSON_TAG_PICTURE_SITE + String.valueOf(diveSite.getOnlineId()))) {
						
						JSONArray diveSitePictureResultJSON = 
								json.getJSONArray(JSON_TAG_PICTURE_SITE + String.valueOf(diveSite.getOnlineId()));
						
						for (int j = 0; j < diveSitePictureResultJSON.length(); j++) {
							JSONObject diveSitePictureJSON = diveSitePictureResultJSON.getJSONObject(j);
							DiveSitePicture diveSitePicture = new DiveSitePicture(diveSitePictureJSON);
							
							if (diveSitePicture.getLocalId() == -1) {
								diveSitePicture.setLocalId(mDiveSiteManager.getDiveSitePictureLocalId(diveSitePicture.getOnlineId()));
							}
							
							diveSitePicture.setDiveSiteLocalID(diveSite.getLocalId());
							diveSite.addPicture(diveSitePicture);
						}		
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return diveSites;
	}
	
	private ArrayList<DiveLog> getDiveLogsJSON(JSONObject json, String JSONName) {
		ArrayList<DiveLog> diveLogs = new ArrayList<DiveLog>();
		
		try {
			if (json.has(JSONName)){
				JSONArray diveLogResultJSON = json.getJSONArray(JSONName);
				
				for (int i = 0; i < diveLogResultJSON.length(); i++) {
					JSONObject diveLogJSON = diveLogResultJSON.getJSONObject(i);
					DiveLog diveLog = new DiveLog(diveLogJSON);
					diveLog.setLocalId(mDiveSiteManager.getDiveLogLocalId(diveLog.getOnlineId()));
					diveLog.setDiveSiteLocalId(mDiveSiteManager.getDiveSiteLocalId(diveLog.getDiveSiteOnlineId()));
					diveLog.setPublished(true);
					
					diveLogs.add(diveLog);
					
					// Add any dive log buddies exist
					if (json.has(JSON_TAG_BUDDY_LOG + String.valueOf(diveLog.getOnlineId()))) {
						JSONArray diveLogBuddiesResultJSON = 
							json.getJSONArray(JSON_TAG_BUDDY_LOG + String.valueOf(diveLog.getOnlineId()));
						for (int j = 0; j < diveLogBuddiesResultJSON.length(); j++) {
							DiveLogBuddy buddy = new DiveLogBuddy(diveLogBuddiesResultJSON.getJSONObject(j));
							
							if (buddy.getLocalId() == -1) {
								buddy.setLocalId(mDiveSiteManager.getDiveLogBuddyLocalId(buddy.getOnlineId()));
							}
							
							buddy.setDiveLogLocalId(diveLog.getLocalId());
							diveLog.getBuddies().add(buddy);
						}
					}
		
					// Add any dive log stops exist
					if (json.has(JSON_TAG_STOP_LOG + String.valueOf(diveLog.getOnlineId()))) {
						JSONArray diveLogStopsResultJSON = 
							json.getJSONArray(JSON_TAG_STOP_LOG + String.valueOf(diveLog.getOnlineId()));
						for (int j = 0; j < diveLogStopsResultJSON.length(); j++) {
							DiveLogStop stop = new DiveLogStop(diveLogStopsResultJSON.getJSONObject(j));
							
							if (stop.getLocalId() == -1) {
								stop.setLocalId(mDiveSiteManager.getDiveLogBuddyLocalId(stop.getOnlineId()));
							}
							
							stop.setDiveLogLocalId(diveLog.getLocalId());
							diveLog.getStops().add(stop);
						}
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return diveLogs;
	}
	
	private ArrayList<ScheduledDive> getScheduledDivesJSON(JSONObject json, String JSONName) {
		ArrayList<ScheduledDive> scheduledDives = new ArrayList<ScheduledDive>();
		
		try {
			if (json.has(JSONName)){
				JSONArray scheduledDiveResultJSON = json.getJSONArray(JSONName);
				
				for (int i = 0; i < scheduledDiveResultJSON.length(); i++) {
					JSONObject scheduledDiveJSON = scheduledDiveResultJSON.getJSONObject(i);
					ScheduledDive scheduledDive = new ScheduledDive(scheduledDiveJSON);
					scheduledDive.setLocalId(mDiveSiteManager.getScheduledDiveLocalId(scheduledDive.getOnlineId()));
					scheduledDive.setPublished(true);
					
					scheduledDives.add(scheduledDive);
					
					// Add any scheduled dive dive sites exist
					if (json.has(JSON_TAG_SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE + 
							String.valueOf(scheduledDive.getOnlineId()))) {
						JSONArray scheduledDiveDiveSiteResultJSON = 
							json.getJSONArray(JSON_TAG_SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE + 
									String.valueOf(scheduledDive.getOnlineId()));
						for (int j = 0; j < scheduledDiveDiveSiteResultJSON.length(); j++) {
							ScheduledDiveDiveSite scheduledDiveDiveSite = 
									new ScheduledDiveDiveSite(scheduledDiveDiveSiteResultJSON.getJSONObject(j));
							
							if (scheduledDiveDiveSite.getLocalId() == -1) {
								scheduledDiveDiveSite.setLocalId(mDiveSiteManager.getScheduledDiveDiveSiteLocalId(scheduledDiveDiveSite.getOnlineId()));
							}
							
							scheduledDiveDiveSite.setScheduledDiveLocalId(scheduledDive.getLocalId());
							scheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
							
							// Get dive site for each scheduled dive dive site
							ArrayList<DiveSite> diveSites = 
									getDiveSitesJSON(json, JSON_TAG_DIVESITES_SCHEDULEDDIVEDIVESITE + String.valueOf(scheduledDiveDiveSite.getOnlineId()));
							if (diveSites.size() > 0) {
								// Should have 0 or 1 site for the scheduled dive dive site
								scheduledDiveDiveSite.setDiveSite(diveSites.get(0));
								scheduledDiveDiveSite.setDiveSiteLocalId(diveSites.get(0).getLocalId());
							}
						}
					}
		
					ArrayList<ScheduledDiveUser> scheduledDiveUsers = 
							getScheduledDiveUsersJSON(json, 
									JSON_TAG_SCHEDULEDDIVEUSER_SCHEDULEDDIVE + scheduledDive.getOnlineId());
					for (int j = 0; j < scheduledDiveUsers.size(); j++) {
						scheduledDiveUsers.get(j).setScheduledDiveLocalId(scheduledDive.getLocalId());
						scheduledDive.getScheduledDiveUsers().add(scheduledDiveUsers.get(j));
					}
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return scheduledDives;
	}
	
	private ArrayList<ScheduledDiveUser> getScheduledDiveUsersJSON(JSONObject json, String JSONName) {
		ArrayList<ScheduledDiveUser> scheduledDiveUsers = new ArrayList<ScheduledDiveUser>();
		
		try {
			if (json.has(JSONName)){
				JSONArray scheduledDiveUserResultJSON = json.getJSONArray(JSONName);
				
				for (int i = 0; i < scheduledDiveUserResultJSON.length(); i++) {
					JSONObject scheduledDiveUserJSON = scheduledDiveUserResultJSON.getJSONObject(i);
					ScheduledDiveUser scheduledDiveUser = new ScheduledDiveUser(scheduledDiveUserJSON);
					if (scheduledDiveUser.getLocalId() == -1) {
						scheduledDiveUser.setLocalId(mDiveSiteManager.getScheduledDiveUserLocalId(scheduledDiveUser.getOnlineId()));
					}
					
					scheduledDiveUsers.add(scheduledDiveUser);
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return scheduledDiveUsers;
	}

	@Override
	protected void onProgressUpdate(Object... progressObjects) {
		if (progressObjects != null) {
			for (int i = 0; i < progressObjects.length; i++) {
				if (mOnlineDiveDataListener != null) {
					mOnlineDiveDataListener.onOnlineDiveDataProgress(progressObjects[i]);
				}
			}
		} else {
			if (mOnlineDiveDataListener != null) {
				mOnlineDiveDataListener.onOnlineDiveDataProgress(null);
			}
		}
	}

	@Override
	protected void onPostExecute(String file_url) {
		if (mOnlineDiveDataListener != null) {
			mOnlineDiveDataListener.onOnlineDiveDataRetrievedComplete(
					mResultList, mMessage, mIsError);
		}
		mActive = false;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		mActive = false;
	}
	
	public Object getUpdateObject() {
		return mUpdateObject;
	}

	public void stopBackground() {
		mJSONParser.setCancelled(true);
	}

	public Boolean getActive() {
		return mActive;
	}
}
