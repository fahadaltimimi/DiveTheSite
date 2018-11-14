package com.fahadaltimimi.divethesite.data;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;
import com.fahadaltimimi.divethesite.model.DiveLogStop;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.DiveSitePicture;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.DiverCertification;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.fahadaltimimi.divethesite.model.ScheduledDiveDiveSite;
import com.fahadaltimimi.divethesite.model.ScheduledDiveUser;
import com.fahadaltimimi.model.ValueParameter;

public class DiveSiteDatabaseHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "DiveTheSite.sqlite";
	private static final int VERSION = 3;

	private static final String TABLE_DIVESITES = "DiveSites";
	private static final String COLUMN_DIVESITE_LOCAL_ID = "_id";
	private static final String COLUMN_DIVESITE_ONLINE_ID = "dive_site_online_id";
    private static final String COLUMN_DIVESITE_LOG_COUNT = "site_count";
	private static final String COLUMN_DIVESITE_USER_ID = "user_id";
	private static final String COLUMN_DIVESITE_USERNAME = "username";
	public static final String COLUMN_DIVESITE_NAME = "name";
	public static final String COLUMN_DIVESITE_CITY = "city";
	public static final String COLUMN_DIVESITE_PROVINCE = "province";
	public static final String COLUMN_DIVESITE_COUNTRY = "country";
	private static final String COLUMN_DIVESITE_DIFFICULTY = "difficulty";
	private static final String COLUMN_DIVESITE_ISSALT = "is_salt";
	private static final String COLUMN_DIVESITE_ISSHORE = "is_shore";
	private static final String COLUMN_DIVESITE_ISBOAT = "is_boat";
	private static final String COLUMN_DIVESITE_ISWRECK = "is_wreck";
	private static final String COLUMN_DIVESITE_HISTORY = "history";
	private static final String COLUMN_DIVESITE_DESCRIPTION = "description";
	private static final String COLUMN_DIVESITE_DIRECTIONS = "directions";
	private static final String COLUMN_DIVESITE_SOURCE = "source";
	private static final String COLUMN_DIVESITE_NOTES = "notes";
	private static final String COLUMN_DIVESITE_LATITUDE = "latitude";
	private static final String COLUMN_DIVESITE_LONGITUDE = "longitude";
	private static final String COLUMN_DIVESITE_ALTITUDE = "altitude";
	private static final String COLUMN_DIVESITE_TOTALRATE = "total_rate";
	private static final String COLUMN_DIVESITE_RATE_COUNT = "rate_count";
	private static final String COLUMN_DIVESITE_DATE_ADDED = "date_added";
	public static final String COLUMN_DIVESITE_IS_PUBLISHED = "is_published";
	private static final String COLUMN_DIVESITE_IS_ARCHIVED = "is_archived";
	private static final String COLUMN_DIVESITE_LAST_MODIFIED_ONLINE = "last_modified_online";
	private static final String COLUMN_DIVESITE_REQUIRES_REFRESH = "requires_refresh";

	private static final String TABLE_PICTURES = "Pictures";
	private static final String COLUMN_PICTURE_LOCAL_ID = "_id";
	private static final String COLUMN_PICTURE_ONLINE_ID = "picture_online_id";
	private static final String COLUMN_PICTURE_SITE_LOCAL_ID = "dive_site_local_id";
	private static final String COLUMN_PICTURE_SITE_ONLINE_ID = "dive_site_online_id";
	private static final String COLUMN_PICTURE_IMAGE_PATH = "image_file_path";
	private static final String COLUMN_PICTURE_URL = "picture_url";
	private static final String COLUMN_PICTURE_DESCRIPTION = "picture_description";

	private static final String TABLE_DIVELOG = "DiveLog";
	private static final String COLUMN_DIVELOG_LOCAL_ID = "_id";
	private static final String COLUMN_DIVELOG_ONLINE_ID = "dive_log_online_id";
    private static final String COLUMN_DIVELOG_LOG_COUNT = "log_count";
    private static final String COLUMN_DIVELOG_TIME_SUM = "log_time_sum";
	private static final String COLUMN_DIVELOG_SITE_ONLINE_ID = "dive_site_online_id";
	private static final String COLUMN_DIVELOG_SITE_LOCAL_ID = "dive_site_local_id";
	private static final String COLUMN_DIVELOG_USER_ID = "user_id";
	private static final String COLUMN_DIVELOG_USERNAME = "username";
	private static final String COLUMN_DIVELOG_TIMESTAMP = "timestamp";
	private static final String COLUMN_DIVELOG_AIR_TYPE = "air_type";
	private static final String COLUMN_DIVELOG_START_PRESSURE = "start_pressure";
	private static final String COLUMN_DIVELOG_END_PRESSURE = "end_pressure";
    private static final String COLUMN_DIVELOG_START_AIR_VALUE = "start_air_value";
    private static final String COLUMN_DIVELOG_START_AIR_UNITS = "start_air_units";
    private static final String COLUMN_DIVELOG_END_AIR_VALUE = "end_air_value";
    private static final String COLUMN_DIVELOG_END_AIR_UNITS = "end_air_units";
	private static final String COLUMN_DIVELOG_DIVE_TIME = "dive_time";
	private static final String COLUMN_DIVELOG_MAX_DEPTH_VALUE = "max_depth_value";
	private static final String COLUMN_DIVELOG_MAX_DEPTH_UNITS = "max_depth_units";
	private static final String COLUMN_DIVELOG_AVERAGE_DEPTH_VALUE = "average_depth_value";
	private static final String COLUMN_DIVELOG_AVERAGE_DEPTH_UNITS = "average_depth_units";
	private static final String COLUMN_DIVELOG_SURFACE_TEMPERATURE_VALUE = "surface_temperature_value";
	private static final String COLUMN_DIVELOG_SURFACE_TEMPERATURE_UNITS = "surface_temperature_units";
	private static final String COLUMN_DIVELOG_WATER_TEMPERATURE_VALUE = "water_temperature_value";
	private static final String COLUMN_DIVELOG_WATER_TEMPERATURE_UNITS = "water_temperature_units";
	private static final String COLUMN_DIVELOG_VISIBILITY_VALUE = "visibility_value";
	private static final String COLUMN_DIVELOG_VISIBILITY_UNITS = "visibility_units";
	private static final String COLUMN_DIVELOG_WEIGHTS_REQUIRED_VALUE = "weights_required_value";
	private static final String COLUMN_DIVELOG_WEIGHTS_REQUIRED_UNITS = "weights_required_units";
	private static final String COLUMN_DIVELOG_SURFACE_TIME = "surface_time";
	private static final String COLUMN_DIVELOG_RATING = "rating";
	private static final String COLUMN_DIVELOG_COMMENTS = "comments";
	private static final String COLUMN_DIVELOG_IS_COURSE = "is_course";
	private static final String COLUMN_DIVELOG_IS_PHOTO_VIDEO = "is_photo_video";
	private static final String COLUMN_DIVELOG_IS_ICE = "is_ice";
	private static final String COLUMN_DIVELOG_IS_DEEP = "is_deep";
	private static final String COLUMN_DIVELOG_IS_INSTRUCTING = "is_instructing";
	private static final String COLUMN_DIVELOG_IS_NIGHT = "is_night";
	private static final String COLUMN_DIVELOG_IS_PUBLISHED = "is_published";
	private static final String COLUMN_DIVELOG_LAST_MODIFIED_ONLINE = "last_modified_online";
	private static final String COLUMN_DIVELOG_REQUIRES_REFRESH = "requires_refresh";

	private static final String TABLE_DIVELOG_BUDDIES = "DiveLogBuddies";
	private static final String COLUMN_DIVELOG_BUDDIES_LOCAL_ID = "_id";
	private static final String COLUMN_DIVELOG_BUDDIES_ONLINE_ID = "dive_log_buddies_online_id";
	private static final String COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID = "dive_log_local_id";
	private static final String COLUMN_DIVELOG_BUDDIES_LOG_ONLINE_ID = "dive_log_online_id";
	private static final String COLUMN_DIVELOG_BUDDIES_USER_ID = "user_id";
	private static final String COLUMN_DIVELOG_BUDDIES_USERNAME = "username";

	private static final String TABLE_DIVELOG_STOPS = "DiveLogStops";
	private static final String COLUMN_DIVELOG_STOPS_LOCAL_ID = "_id";
	private static final String COLUMN_DIVELOG_STOPS_ONLINE_ID = "dive_log_stops_online_id";
	private static final String COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID = "dive_log_local_id";
	private static final String COLUMN_DIVELOG_STOPS_LOG_ONLINE_ID = "dive_log_online_id";
	private static final String COLUMN_DIVELOG_STOPS_TIME = "time";
	private static final String COLUMN_DIVELOG_STOP_DEPTH_VALUE = "depth_value";
	private static final String COLUMN_DIVELOG_STOP_DEPTH_UNITS = "depth_units";

	private static final String TABLE_DIVERS = "Divers";
	private static final String COLUMN_DIVER_LOCAL_ID = "_id";
	private static final String COLUMN_DIVER_ONLINE_ID = "diver_online_id";
	public static final String COLUMN_DIVER_FIRST_NAME = "first_name";
	public static final String COLUMN_DIVER_LAST_NAME = "last_name";
	private static final String COLUMN_DIVER_EMAIL = "email";
	public static final String COLUMN_DIVER_CITY = "city";
	public static final String COLUMN_DIVER_PROVINCE = "province";
	public static final String COLUMN_DIVER_COUNTRY = "country";
	public static final String COLUMN_DIVER_USERNAME = "username";
	private static final String COLUMN_DIVER_BIO = "bio";
	private static final String COLUMN_DIVER_PICTURE_URL = "picture_url";
	private static final String COLUMN_DIVER_IS_MOD = "is_mod";
	private static final String COLUMN_DIVER_CREATED = "created";
	private static final String COLUMN_DIVER_LAST_MODIFIED = "last_modified";
	private static final String COLUMN_DIVER_LOG_COUNT = "log_count";
	private static final String COLUMN_DIVER_DIVE_SITES_SUBMITTED_COUNT = "dive_sites_submitted_count";

	private static final String TABLE_DIVER_CERTIFICATIONS = "DiverCertifications";
	private static final String COLUMN_DIVER_CERT_LOCAL_ID = "_id";
	private static final String COLUMN_DIVER_CERT_ONLINE_ID = "diver_cert_id";
	private static final String COLUMN_DIVER_CERT_USER_ID = "cert_user_id";
	private static final String COLUMN_DIVER_CERT_NAME = "cert_name";
	private static final String COLUMN_DIVER_CERT_DATE = "cert_date";
	private static final String COLUMN_DIVER_CERT_NO = "cert_no";
	private static final String COLUMN_DIVER_CERT_INSTR_NO = "instr_no";
	private static final String COLUMN_DIVER_CERT_INSTR_NAME = "instr_name";
	private static final String COLUMN_DIVER_CERT_LOCATION = "cert_location";
	private static final String COLUMN_DIVER_CERT_IS_PRIMARY = "cert_primary";
	
	private static final String TABLE_SCHEDULED_DIVES = "ScheduledDives";
	private static final String COLUMN_SCHEDULED_DIVES_LOCAL_ID = "_id";
    private static final String COLUMN_SCHEDULED_DIVES_COUNT = "scheduled_dives_count";
	private static final String COLUMN_SCHEDULED_DIVES_TITLE = "title";
	private static final String COLUMN_SCHEDULED_DIVES_ONLINE_ID = "scheduled_dive_id";
	private static final String COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID = "submitter_online_id";
	private static final String COLUMN_SCHEDULED_DIVES_TIMESTAMP = "timestamp";
	private static final String COLUMN_SCHEDULED_DIVES_COMMENT = "comment";
	public static final String COLUMN_SCHEDULED_DIVES_IS_PUBLISHED = "is_published";
	private static final String COLUMN_SCHEDULED_DIVES_LAST_MODIFIED_ONLINE = "last_modified_online";
	private static final String COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH = "requires_refresh";

	private static final String TABLE_SCHEDULED_DIVES_DIVE_SITES = "ScheduledDivesSites";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID = "_id";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_ONLINE_ID = "scheduled_dive_site_id";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID = "scheduled_dive_local_id";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_ONLINE_ID = "scheduled_dive_online_id";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID = "site_local_id";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_ONLINE_ID = "site_online_id";
	private static final String COLUMN_SCHEDULED_DIVES_DIVE_SITE_VOTE_COUNT = "vote_count";
	
	private static final String TABLE_SCHEDULED_DIVES_USERS = "ScheduledDivesUsers";
	private static final String COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID = "_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_ONLINE_ID = "scheduled_dive_user_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID = "scheduled_dive_local_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_ONLINE_ID = "scheduled_dive_online_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID = "voted_scheduled_site_local_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_ONLINE_ID = "voted_scheduled_site_online_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID = "user_online_id";
	private static final String COLUMN_SCHEDULED_DIVES_USER_ATTEND_STATE = "attend_state";
	
	
	public DiveSiteDatabaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the "dive site" table
		db.execSQL("create table DiveSites ("
				+ "_id integer primary key autoincrement, "
				+ "dive_site_online_id integer, " + "user_id integer, "
				+ "username varchar(50), " + "name varchar(50), "
				+ "city varchar(50), " + "province varchar(50), "
				+ "country varchar(50), " + "difficulty varchar(50), "
				+ "is_salt integer, " + "is_shore integer, "
				+ "is_boat integer, " + "is_wreck integer, " + "history text, "
				+ "description text, " + "directions text, " + "source text, "
				+ "notes text, " + "latitude real, " + "longitude real, "
				+ "altitude real, " + "total_rate real, "
				+ "rate_count integer, " + "date_added integer, "
				+ "is_published integer, " + "is_archived integer, "
				+ "last_modified_online integer, "
				+ COLUMN_DIVESITE_REQUIRES_REFRESH + " integer)");

		// Create the "pictures" table
		db.execSQL("create table Pictures (" + COLUMN_PICTURE_LOCAL_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_PICTURE_ONLINE_ID + " integer, "
				+ COLUMN_PICTURE_SITE_LOCAL_ID + " integer, "
				+ COLUMN_PICTURE_SITE_ONLINE_ID + " integer, "
				+ COLUMN_PICTURE_IMAGE_PATH + " text, " + COLUMN_PICTURE_URL
				+ " text, " + COLUMN_PICTURE_DESCRIPTION + " text)");

		// Create the "dive log" table
		db.execSQL("create table " + TABLE_DIVELOG + " ("
				+ COLUMN_DIVELOG_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_DIVELOG_ONLINE_ID + " integer, "
				+ COLUMN_DIVELOG_SITE_ONLINE_ID + " integer, "
				+ COLUMN_DIVELOG_SITE_LOCAL_ID + " integer, "
				+ COLUMN_DIVELOG_USER_ID + " integer, "
				+ COLUMN_DIVELOG_USERNAME + " varchar(50), "
				+ COLUMN_DIVELOG_TIMESTAMP + " integer, "
				+ COLUMN_DIVELOG_AIR_TYPE + " text, "
				+ COLUMN_DIVELOG_START_PRESSURE + " char(1), "
				+ COLUMN_DIVELOG_END_PRESSURE + " char(1), "
                + COLUMN_DIVELOG_START_AIR_VALUE + " real, "
                + COLUMN_DIVELOG_START_AIR_UNITS + " varchar(10), "
                + COLUMN_DIVELOG_END_AIR_VALUE + " real, "
                + COLUMN_DIVELOG_END_AIR_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_DIVE_TIME + " integer, "
				+ COLUMN_DIVELOG_MAX_DEPTH_VALUE + " real, "
				+ COLUMN_DIVELOG_MAX_DEPTH_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_AVERAGE_DEPTH_VALUE + " real, "
				+ COLUMN_DIVELOG_AVERAGE_DEPTH_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_SURFACE_TEMPERATURE_VALUE + " real, "
				+ COLUMN_DIVELOG_SURFACE_TEMPERATURE_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_WATER_TEMPERATURE_VALUE + " real, "
				+ COLUMN_DIVELOG_WATER_TEMPERATURE_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_VISIBILITY_VALUE + " real, "
				+ COLUMN_DIVELOG_VISIBILITY_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_WEIGHTS_REQUIRED_VALUE + " real, "
				+ COLUMN_DIVELOG_WEIGHTS_REQUIRED_UNITS + " varchar(10), "
				+ COLUMN_DIVELOG_SURFACE_TIME + " integer, "
				+ COLUMN_DIVELOG_RATING + " integer, "
				+ COLUMN_DIVELOG_COMMENTS + " text, "
				+ COLUMN_DIVELOG_IS_COURSE + " integer, "
				+ COLUMN_DIVELOG_IS_PHOTO_VIDEO + " integer, "
				+ COLUMN_DIVELOG_IS_ICE + " integer, " 
				+ COLUMN_DIVELOG_IS_DEEP + " integer, " 
				+ COLUMN_DIVELOG_IS_INSTRUCTING + " integer, "
				+ COLUMN_DIVELOG_IS_NIGHT + " integer, "
				+ COLUMN_DIVELOG_IS_PUBLISHED + " integer, "
				+ COLUMN_DIVELOG_LAST_MODIFIED_ONLINE + " integer, "
				+ COLUMN_DIVELOG_REQUIRES_REFRESH + " integer)");

		// Create the "dive log buddies" table
		db.execSQL("create table " + TABLE_DIVELOG_BUDDIES + " ("
				+ COLUMN_DIVELOG_BUDDIES_LOCAL_ID
				+ " integer primary key autoincrement, "
				+ COLUMN_DIVELOG_BUDDIES_ONLINE_ID + " integer, "
				+ COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID + " integer, "
				+ COLUMN_DIVELOG_BUDDIES_LOG_ONLINE_ID + " integer, "
				+ COLUMN_DIVELOG_BUDDIES_USER_ID + " integer, "
				+ COLUMN_DIVELOG_BUDDIES_USERNAME + " varchar(50))");

		// Create the "dive log stops" table
		db.execSQL("create table " + TABLE_DIVELOG_STOPS + " ("
				+ COLUMN_DIVELOG_STOPS_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_DIVELOG_STOPS_ONLINE_ID + " integer, "
				+ COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID + " integer, "
				+ COLUMN_DIVELOG_STOPS_LOG_ONLINE_ID + " integer, "
				+ COLUMN_DIVELOG_STOPS_TIME + " integer, "
				+ COLUMN_DIVELOG_STOP_DEPTH_VALUE + " real, "
				+ COLUMN_DIVELOG_STOP_DEPTH_UNITS + " varchar(10))");

		// Create the "diver" table
		db.execSQL("create table " + TABLE_DIVERS + " ("
				+ COLUMN_DIVER_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_DIVER_ONLINE_ID + " integer, "
				+ COLUMN_DIVER_FIRST_NAME + " text, " 
				+ COLUMN_DIVER_LAST_NAME + " text, " 
				+ COLUMN_DIVER_EMAIL + " text, "
				+ COLUMN_DIVER_CITY + " text, " 
				+ COLUMN_DIVER_PROVINCE + " text, " 
				+ COLUMN_DIVER_COUNTRY + " text, "
				+ COLUMN_DIVER_USERNAME + " text, " 
				+ COLUMN_DIVER_BIO + " text, " 
				+ COLUMN_DIVER_PICTURE_URL + " text, "
				+ COLUMN_DIVER_IS_MOD + " integer, " 
				+ COLUMN_DIVER_CREATED + " integer, " 
				+ COLUMN_DIVER_LAST_MODIFIED + " integer, "
				+ COLUMN_DIVER_LOG_COUNT + " integer, "
				+ COLUMN_DIVER_DIVE_SITES_SUBMITTED_COUNT + " integer)");

		// Create the "diver cert" table
		db.execSQL("create table " + TABLE_DIVER_CERTIFICATIONS + " ("
				+ COLUMN_DIVER_CERT_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_DIVER_CERT_ONLINE_ID + " integer, "
				+ COLUMN_DIVER_CERT_USER_ID + " integer, "
				+ COLUMN_DIVER_CERT_NAME + " text, " 
				+ COLUMN_DIVER_CERT_DATE + " text, " 
				+ COLUMN_DIVER_CERT_NO + " text, "
				+ COLUMN_DIVER_CERT_INSTR_NO + " text, "
				+ COLUMN_DIVER_CERT_INSTR_NAME + " text, "
				+ COLUMN_DIVER_CERT_LOCATION + " text, "
				+ COLUMN_DIVER_CERT_IS_PRIMARY + " integer)");
		
		// Create the "scheduled dives" table
		db.execSQL("create table " + TABLE_SCHEDULED_DIVES + " ("
				+ COLUMN_SCHEDULED_DIVES_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_SCHEDULED_DIVES_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_TITLE + " text, " 
				+ COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_TIMESTAMP + " integer, "
				+ COLUMN_SCHEDULED_DIVES_COMMENT + " text, "
				+ COLUMN_SCHEDULED_DIVES_IS_PUBLISHED + " integer, "
				+ COLUMN_SCHEDULED_DIVES_LAST_MODIFIED_ONLINE + " integer, "
				+ COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH + " integer)");

		// Create the "scheduled dives sites" table
		db.execSQL("create table " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " ("
				+ COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_SCHEDULED_DIVES_DIVE_SITE_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_ONLINE_ID + " integer)");
		
		// Create the "scheduled dives users" table
		db.execSQL("create table " + TABLE_SCHEDULED_DIVES_USERS + " ("
				+ COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID + " integer primary key autoincrement, "
				+ COLUMN_SCHEDULED_DIVES_USER_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID + " integer, "
				+ COLUMN_SCHEDULED_DIVES_USER_ATTEND_STATE + " text)");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch(oldVersion) {
		   case 1:
			   db.execSQL("alter table " + TABLE_SCHEDULED_DIVES + 
					   " ADD COLUMN " + COLUMN_SCHEDULED_DIVES_TITLE + " text DEFAULT ''");
			   
			   db.execSQL("alter table " + TABLE_DIVESITES + 
					   " ADD COLUMN " + COLUMN_DIVESITE_REQUIRES_REFRESH + " integer DEFAULT 0");
			   db.execSQL("alter table " + TABLE_DIVELOG + 
					   " ADD COLUMN " + COLUMN_DIVELOG_REQUIRES_REFRESH + " integer DEFAULT 0");
			   db.execSQL("alter table " + TABLE_SCHEDULED_DIVES + 
					   " ADD COLUMN " + COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH + " integer DEFAULT 0");
           case 2:
               db.execSQL("alter table " + TABLE_DIVELOG +
                       " ADD COLUMN " + COLUMN_DIVELOG_START_AIR_VALUE + " real DEFAULT 0");
               db.execSQL("alter table " + TABLE_DIVELOG +
                       " ADD COLUMN " + COLUMN_DIVELOG_START_AIR_UNITS + " text DEFAULT ''");
               db.execSQL("alter table " + TABLE_DIVELOG +
                       " ADD COLUMN " + COLUMN_DIVELOG_END_AIR_VALUE + " real DEFAULT 0");
               db.execSQL("alter table " + TABLE_DIVELOG +
                       " ADD COLUMN " + COLUMN_DIVELOG_END_AIR_UNITS + " text DEFAULT ''");

		   // Note for next version: Don't add break to allow 
		   // subsequent updates to occur from old version
		   }
	}

	public void insertDiveSite(DiveSite diveSite) {
		ContentValues cvDiveSiteInfo = new ContentValues();

		cvDiveSiteInfo.put(COLUMN_DIVESITE_ONLINE_ID, diveSite.getOnlineId());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_USER_ID, diveSite.getUserId());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_USERNAME, diveSite.getUsername());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_NAME, diveSite.getName());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_CITY, diveSite.getCity());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_PROVINCE, diveSite.getProvince());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_COUNTRY, diveSite.getCountry());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_DIFFICULTY, diveSite.getDifficulty()
				.getName());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISSALT, diveSite.isSalty());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISSHORE, diveSite.isShoreDive());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISBOAT, diveSite.isBoatDive());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISWRECK, diveSite.isWreck());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_HISTORY, diveSite.getHistory());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_DESCRIPTION,
				diveSite.getDescription());
		cvDiveSiteInfo
				.put(COLUMN_DIVESITE_DIRECTIONS, diveSite.getDirections());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_SOURCE, diveSite.getSource());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_NOTES, diveSite.getNotes());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_LATITUDE, diveSite.getLatitude());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_LONGITUDE, diveSite.getLongitude());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ALTITUDE, diveSite.getAltitude());
		cvDiveSiteInfo
				.put(COLUMN_DIVESITE_TOTALRATE, diveSite.getTotalRating());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_RATE_COUNT,
				diveSite.getRatingCount());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_DATE_ADDED, diveSite.getDateAdded()
				.getTime());
		cvDiveSiteInfo
				.put(COLUMN_DIVESITE_IS_PUBLISHED, diveSite.isPublished());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_IS_ARCHIVED, diveSite.isArchived());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_LAST_MODIFIED_ONLINE, diveSite.getLastModifiedOnline()
				.getTime());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_REQUIRES_REFRESH, diveSite.requiresRefresh());

		long diveSiteID = getWritableDatabase().insert(TABLE_DIVESITES, null,
				cvDiveSiteInfo);
		diveSite.setLocalId(diveSiteID);

		// Save Dive Site Pictures
		for (int i = 0; i < diveSite.getPicturesCount(); i++) {
			diveSite.getPicture(i).setDiveSiteLocalID(diveSite.getLocalId());
			diveSite.getPicture(i).setDiveSiteOnlineID(diveSite.getOnlineId());
			if (diveSite.getPicture(i).getLocalId() == -1) {
				insertDiveSitePicture(diveSite.getPicture(i));
			} else {
				saveDiveSitePicture(diveSite.getPicture(i));
			}
		}
	}

	public void saveDiveSite(DiveSite diveSite) {
		ContentValues cvDiveSiteInfo = new ContentValues();

		cvDiveSiteInfo.put(COLUMN_DIVESITE_ONLINE_ID, diveSite.getOnlineId());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_USER_ID, diveSite.getUserId());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_USERNAME, diveSite.getUsername());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_NAME, diveSite.getName());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_CITY, diveSite.getCity());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_PROVINCE, diveSite.getProvince());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_COUNTRY, diveSite.getCountry());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_DIFFICULTY, diveSite.getDifficulty()
				.getName());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISSALT, diveSite.isSalty());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISSHORE, diveSite.isShoreDive());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISBOAT, diveSite.isBoatDive());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ISWRECK, diveSite.isWreck());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_HISTORY, diveSite.getHistory());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_DESCRIPTION,
				diveSite.getDescription());
		cvDiveSiteInfo
				.put(COLUMN_DIVESITE_DIRECTIONS, diveSite.getDirections());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_SOURCE, diveSite.getSource());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_NOTES, diveSite.getNotes());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_LATITUDE, diveSite.getLatitude());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_LONGITUDE, diveSite.getLongitude());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_ALTITUDE, diveSite.getAltitude());
		cvDiveSiteInfo
				.put(COLUMN_DIVESITE_TOTALRATE, diveSite.getTotalRating());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_RATE_COUNT,
				diveSite.getRatingCount());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_DATE_ADDED, diveSite.getDateAdded()
				.getTime());
		cvDiveSiteInfo
				.put(COLUMN_DIVESITE_IS_PUBLISHED, diveSite.isPublished());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_IS_ARCHIVED, diveSite.isArchived());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_LAST_MODIFIED_ONLINE, diveSite.getLastModifiedOnline()
				.getTime());
		cvDiveSiteInfo.put(COLUMN_DIVESITE_REQUIRES_REFRESH, diveSite.requiresRefresh());
		
		String updateWhereClause = "_id=" + diveSite.getLocalId();
		getWritableDatabase().update(TABLE_DIVESITES, cvDiveSiteInfo,
				updateWhereClause, null);

		// Save Dive Site Pictures
		for (int i = 0; i < diveSite.getPicturesCount(); i++) {
			diveSite.getPicture(i).setDiveSiteLocalID(diveSite.getLocalId());
			diveSite.getPicture(i).setDiveSiteOnlineID(diveSite.getOnlineId());
			if (diveSite.getPicture(i).getLocalId() == -1) {
				insertDiveSitePicture(diveSite.getPicture(i));
			} else {
				saveDiveSitePicture(diveSite.getPicture(i));
			}
		}
	}
	
	public void setDiveSiteRequiresRefresh(DiveSite diveSite) {
		ContentValues cvDiveSiteInfo = new ContentValues();
		cvDiveSiteInfo.put(COLUMN_DIVESITE_REQUIRES_REFRESH, diveSite.requiresRefresh());
		
		String updateWhereClause = "_id=" + diveSite.getLocalId();
		getWritableDatabase().update(TABLE_DIVESITES, cvDiveSiteInfo, updateWhereClause, null);
	}

	public void insertDiveLog(DiveLog diveLog) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVELOG_ONLINE_ID, diveLog.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SITE_ONLINE_ID,
				diveLog.getDiveSiteOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SITE_LOCAL_ID,
				diveLog.getDiveSiteLocalId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_USER_ID, diveLog.getUserId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_USERNAME, diveLog.getUsername());
		cvDiveLogInfo.put(COLUMN_DIVELOG_TIMESTAMP, diveLog.getTimestamp()
				.getTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_AIR_TYPE, diveLog.getAirType());
		cvDiveLogInfo.put(COLUMN_DIVELOG_START_PRESSURE,
				String.valueOf(diveLog.getStartPressure()));
		cvDiveLogInfo.put(COLUMN_DIVELOG_END_PRESSURE,
				String.valueOf(diveLog.getEndPressure()));
        cvDiveLogInfo.put(COLUMN_DIVELOG_START_AIR_VALUE, diveLog.getStartAir().getValue());
        cvDiveLogInfo.put(COLUMN_DIVELOG_START_AIR_UNITS, diveLog.getStartAir().getUnits());
        cvDiveLogInfo.put(COLUMN_DIVELOG_END_AIR_VALUE, diveLog.getEndAir().getValue());
        cvDiveLogInfo.put(COLUMN_DIVELOG_END_AIR_UNITS, diveLog.getEndAir().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_DIVE_TIME, diveLog.getDiveTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_MAX_DEPTH_VALUE, diveLog.getMaxDepth()
				.getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_MAX_DEPTH_UNITS, diveLog.getMaxDepth()
				.getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_AVERAGE_DEPTH_VALUE, diveLog
				.getAverageDepth().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_AVERAGE_DEPTH_UNITS, diveLog
				.getAverageDepth().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SURFACE_TEMPERATURE_VALUE, diveLog
				.getSurfaceTemperature().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SURFACE_TEMPERATURE_UNITS, diveLog
				.getSurfaceTemperature().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WATER_TEMPERATURE_VALUE, diveLog
				.getWaterTemperature().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WATER_TEMPERATURE_UNITS, diveLog
				.getWaterTemperature().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_VISIBILITY_VALUE, diveLog
				.getVisibility().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_VISIBILITY_UNITS, diveLog
				.getVisibility().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WEIGHTS_REQUIRED_VALUE, diveLog
				.getWeightsRequired().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WEIGHTS_REQUIRED_UNITS, diveLog
				.getWeightsRequired().getUnits());
		cvDiveLogInfo
				.put(COLUMN_DIVELOG_SURFACE_TIME, diveLog.getSurfaceTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_RATING, diveLog.getRating());
		cvDiveLogInfo.put(COLUMN_DIVELOG_COMMENTS, diveLog.getComments());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_COURSE, diveLog.isCourse());
		cvDiveLogInfo
				.put(COLUMN_DIVELOG_IS_PHOTO_VIDEO, diveLog.isPhotoVideo());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_ICE, diveLog.isIce());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_DEEP, diveLog.isDeep());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_INSTRUCTING,
				diveLog.isInstructing());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_NIGHT, diveLog.isNight());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_PUBLISHED, diveLog.isPublished());
		cvDiveLogInfo.put(COLUMN_DIVELOG_LAST_MODIFIED_ONLINE, diveLog.getLastModifiedOnline()
				.getTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_REQUIRES_REFRESH, diveLog.requiresRefresh());

		long diveLogId = getWritableDatabase().insert(TABLE_DIVELOG, null,
				cvDiveLogInfo);
		diveLog.setLocalId(diveLogId);

		// Save Buddies and Stops
		for (int i = 0; i < diveLog.getBuddies().size(); i++) {
			diveLog.getBuddies().get(i).setDiveLogLocalId(diveLogId);
			diveLog.getBuddies().get(i).setDiveLogOnlineId(diveLog.getOnlineId());
			insertDiveLogBuddy(diveLog.getBuddies().get(i));
		}

		for (int i = 0; i < diveLog.getStops().size(); i++) {
			diveLog.getStops().get(i).setDiveLogLocalId(diveLogId);
			diveLog.getStops().get(i).setDiveLogOnlineId(diveLog.getOnlineId());
			insertDiveLogStop(diveLog.getStops().get(i));
		}
	}

	public void saveDiveLog(DiveLog diveLog) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVELOG_ONLINE_ID, diveLog.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SITE_ONLINE_ID,
				diveLog.getDiveSiteOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SITE_LOCAL_ID,
				diveLog.getDiveSiteLocalId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_USER_ID, diveLog.getUserId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_USERNAME, diveLog.getUsername());
		cvDiveLogInfo.put(COLUMN_DIVELOG_TIMESTAMP, diveLog.getTimestamp()
				.getTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_AIR_TYPE, diveLog.getAirType());
		cvDiveLogInfo.put(COLUMN_DIVELOG_START_PRESSURE,
				String.valueOf(diveLog.getStartPressure()));
		cvDiveLogInfo.put(COLUMN_DIVELOG_END_PRESSURE,
				String.valueOf(diveLog.getEndPressure()));
        cvDiveLogInfo.put(COLUMN_DIVELOG_START_AIR_VALUE, diveLog.getStartAir().getValue());
        cvDiveLogInfo.put(COLUMN_DIVELOG_START_AIR_UNITS, diveLog.getStartAir().getUnits());
        cvDiveLogInfo.put(COLUMN_DIVELOG_END_AIR_VALUE, diveLog.getEndAir().getValue());
        cvDiveLogInfo.put(COLUMN_DIVELOG_END_AIR_UNITS, diveLog.getEndAir().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_DIVE_TIME, diveLog.getDiveTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_MAX_DEPTH_VALUE, diveLog.getMaxDepth()
				.getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_MAX_DEPTH_UNITS, diveLog.getMaxDepth()
				.getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_AVERAGE_DEPTH_VALUE, diveLog
				.getAverageDepth().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_AVERAGE_DEPTH_UNITS, diveLog
				.getAverageDepth().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SURFACE_TEMPERATURE_VALUE, diveLog
				.getSurfaceTemperature().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_SURFACE_TEMPERATURE_UNITS, diveLog
				.getSurfaceTemperature().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WATER_TEMPERATURE_VALUE, diveLog
				.getWaterTemperature().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WATER_TEMPERATURE_UNITS, diveLog
				.getWaterTemperature().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_VISIBILITY_VALUE, diveLog
				.getVisibility().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_VISIBILITY_UNITS, diveLog
				.getVisibility().getUnits());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WEIGHTS_REQUIRED_VALUE, diveLog
				.getWeightsRequired().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_WEIGHTS_REQUIRED_UNITS, diveLog
				.getWeightsRequired().getUnits());
		cvDiveLogInfo
				.put(COLUMN_DIVELOG_SURFACE_TIME, diveLog.getSurfaceTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_RATING, diveLog.getRating());
		cvDiveLogInfo.put(COLUMN_DIVELOG_COMMENTS, diveLog.getComments());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_COURSE, diveLog.isCourse());
		cvDiveLogInfo
				.put(COLUMN_DIVELOG_IS_PHOTO_VIDEO, diveLog.isPhotoVideo());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_ICE, diveLog.isIce());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_DEEP, diveLog.isDeep());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_INSTRUCTING,
				diveLog.isInstructing());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_NIGHT, diveLog.isNight());
		cvDiveLogInfo.put(COLUMN_DIVELOG_IS_PUBLISHED, diveLog.isPublished());
		cvDiveLogInfo.put(COLUMN_DIVELOG_LAST_MODIFIED_ONLINE, diveLog.getLastModifiedOnline()
				.getTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_REQUIRES_REFRESH, diveLog.requiresRefresh());
		
		String updateWhereClause = "_id=" + diveLog.getLocalId();
		getWritableDatabase().update(TABLE_DIVELOG, cvDiveLogInfo,
				updateWhereClause, null);

		// First delete Buddies and Stops
		deleteDiveLogBuddyForLog(diveLog.getLocalId());
		deleteDiveLogStopForLog(diveLog.getLocalId());
		
		// Save Buddies and Stops
		for (int i = 0; i < diveLog.getBuddies().size(); i++) {
			diveLog.getBuddies().get(i).setDiveLogLocalId(diveLog.getLocalId());
			diveLog.getBuddies().get(i).setDiveLogOnlineId(diveLog.getOnlineId());
			insertDiveLogBuddy(diveLog.getBuddies().get(i));
		}

		for (int i = 0; i < diveLog.getStops().size(); i++) {
			diveLog.getStops().get(i).setDiveLogLocalId(diveLog.getLocalId());
			diveLog.getStops().get(i).setDiveLogOnlineId(diveLog.getOnlineId());
			insertDiveLogStop(diveLog.getStops().get(i));
		}
	}

	public void setDiveLogRequiresRefresh(DiveLog diveLog) {
		ContentValues cvDiveLogInfo = new ContentValues();
		cvDiveLogInfo.put(COLUMN_DIVELOG_REQUIRES_REFRESH, diveLog.requiresRefresh());
		
		String updateWhereClause = "_id=" + diveLog.getLocalId();
		getWritableDatabase().update(TABLE_DIVELOG, cvDiveLogInfo, updateWhereClause, null);
	}
	
	public void insertDiveLogBuddy(DiveLogBuddy diveLogBuddy) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_ONLINE_ID,
				diveLogBuddy.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_USER_ID,
				diveLogBuddy.getDiverOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID,
				diveLogBuddy.getDiveLogLocalId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_LOG_ONLINE_ID,
				diveLogBuddy.getDiveLogOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_USERNAME,
				diveLogBuddy.getDiverUsername());

		long diveLogBuddyId = getWritableDatabase().insert(
				TABLE_DIVELOG_BUDDIES, null, cvDiveLogInfo);
		diveLogBuddy.setLocalId(diveLogBuddyId);
	}

	public void saveDiveLogBuddy(DiveLogBuddy diveLogBuddy) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_ONLINE_ID,
				diveLogBuddy.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_USER_ID,
				diveLogBuddy.getDiverOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID,
				diveLogBuddy.getDiveLogLocalId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_LOG_ONLINE_ID,
				diveLogBuddy.getDiveLogOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_BUDDIES_USERNAME,
				diveLogBuddy.getDiverUsername());

		String updateWhereClause = "_id=" + diveLogBuddy.getLocalId();
		getWritableDatabase().update(TABLE_DIVELOG_BUDDIES, cvDiveLogInfo,
				updateWhereClause, null);
	}

	public void insertDiveLogStop(DiveLogStop diveLogStop) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_ONLINE_ID,
				diveLogStop.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID,
				diveLogStop.getDiveLogLocalId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_LOG_ONLINE_ID,
				diveLogStop.getDiveLogOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_TIME, diveLogStop.getTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOP_DEPTH_VALUE, diveLogStop
				.getDepth().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOP_DEPTH_UNITS, diveLogStop
				.getDepth().getUnits());

		long diveLogStopId = getWritableDatabase().insert(TABLE_DIVELOG_STOPS,
				null, cvDiveLogInfo);
		diveLogStop.setLocalId(diveLogStopId);
	}

	public void saveDiveLogStop(DiveLogStop diveLogStop) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_ONLINE_ID,
				diveLogStop.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID,
				diveLogStop.getDiveLogLocalId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_LOG_ONLINE_ID,
				diveLogStop.getDiveLogOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOPS_TIME, diveLogStop.getTime());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOP_DEPTH_VALUE, diveLogStop
				.getDepth().getValue());
		cvDiveLogInfo.put(COLUMN_DIVELOG_STOP_DEPTH_UNITS, diveLogStop
				.getDepth().getUnits());

		String updateWhereClause = "_id=" + diveLogStop.getLocalId();
		getWritableDatabase().update(TABLE_DIVELOG_STOPS, cvDiveLogInfo,
				updateWhereClause, null);
	}

	public void insertDiver(Diver diver) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVER_ONLINE_ID, diver.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVER_FIRST_NAME, diver.getFirstName());
		cvDiveLogInfo.put(COLUMN_DIVER_LAST_NAME, diver.getLastName());
		cvDiveLogInfo.put(COLUMN_DIVER_EMAIL, diver.getEmail());
		cvDiveLogInfo.put(COLUMN_DIVER_CITY, diver.getCity());
		cvDiveLogInfo.put(COLUMN_DIVER_PROVINCE, diver.getProvince());
		cvDiveLogInfo.put(COLUMN_DIVER_COUNTRY, diver.getCountry());
		cvDiveLogInfo.put(COLUMN_DIVER_USERNAME, diver.getUsername());
		cvDiveLogInfo.put(COLUMN_DIVER_BIO, diver.getBio());
		cvDiveLogInfo.put(COLUMN_DIVER_PICTURE_URL, diver.getPictureURL());
		cvDiveLogInfo.put(COLUMN_DIVER_IS_MOD, diver.isMod());
		cvDiveLogInfo.put(COLUMN_DIVER_CREATED, diver.getCreated().getTime());
		cvDiveLogInfo.put(COLUMN_DIVER_LAST_MODIFIED, diver.getLastModified()
				.getTime());
		cvDiveLogInfo.put(COLUMN_DIVER_LOG_COUNT, diver.getLogCount());
		cvDiveLogInfo.put(COLUMN_DIVER_DIVE_SITES_SUBMITTED_COUNT,
				diver.getDiveSiteSubmittedCount());

		long diverId = getWritableDatabase().insert(TABLE_DIVERS, null,
				cvDiveLogInfo);
		diver.setLocalId(diverId);

		// Save Certification
		for (int i = 0; i < diver.getCertifications().size(); i++) {
			if (diver.getCertifications().get(i).getLocalId() == -1) {
				insertDiverCertification(diver.getCertifications().get(i));
			} else {
				saveDiverCertification(diver.getCertifications().get(i));
			}
		}
	}

	public void saveDiver(Diver diver) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVER_ONLINE_ID, diver.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVER_FIRST_NAME, diver.getFirstName());
		cvDiveLogInfo.put(COLUMN_DIVER_LAST_NAME, diver.getLastName());
		cvDiveLogInfo.put(COLUMN_DIVER_EMAIL, diver.getEmail());
		cvDiveLogInfo.put(COLUMN_DIVER_CITY, diver.getCity());
		cvDiveLogInfo.put(COLUMN_DIVER_PROVINCE, diver.getProvince());
		cvDiveLogInfo.put(COLUMN_DIVER_COUNTRY, diver.getCountry());
		cvDiveLogInfo.put(COLUMN_DIVER_USERNAME, diver.getUsername());
		cvDiveLogInfo.put(COLUMN_DIVER_BIO, diver.getBio());
		cvDiveLogInfo.put(COLUMN_DIVER_PICTURE_URL, diver.getPictureURL());
		cvDiveLogInfo.put(COLUMN_DIVER_IS_MOD, diver.isMod());
		cvDiveLogInfo.put(COLUMN_DIVER_CREATED, diver.getCreated().getTime());
		cvDiveLogInfo.put(COLUMN_DIVER_LAST_MODIFIED, diver.getLastModified()
				.getTime());
		cvDiveLogInfo.put(COLUMN_DIVER_LOG_COUNT, diver.getLogCount());
		cvDiveLogInfo.put(COLUMN_DIVER_DIVE_SITES_SUBMITTED_COUNT,
				diver.getDiveSiteSubmittedCount());

		String updateWhereClause = "_id=" + diver.getLocalId();
		getWritableDatabase().update(TABLE_DIVERS, cvDiveLogInfo, updateWhereClause, null);

		// Save Certification
		for (int i = 0; i < diver.getCertifications().size(); i++) {
			if (diver.getCertifications().get(i).getLocalId() == -1) {
				insertDiverCertification(diver.getCertifications().get(i));
			} else {
				saveDiverCertification(diver.getCertifications().get(i));
			}
		}
	}

	public void insertDiverCertification(DiverCertification cert) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVER_CERT_ONLINE_ID, cert.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_USER_ID, cert.getCertifUserId());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_NAME, cert.getCertifTitle());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_DATE, cert.getCertifDate());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_NO, cert.getCertifNumber());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_LOCATION, cert.getCertifLocation());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_IS_PRIMARY, cert.getPrimary());

		long certId = getWritableDatabase().insert(TABLE_DIVER_CERTIFICATIONS,
				null, cvDiveLogInfo);
		cert.setLocalId(certId);
	}

	public void saveDiverCertification(DiverCertification cert) {
		ContentValues cvDiveLogInfo = new ContentValues();

		cvDiveLogInfo.put(COLUMN_DIVER_CERT_ONLINE_ID, cert.getOnlineId());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_USER_ID, cert.getCertifUserId());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_NAME, cert.getCertifTitle());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_DATE, cert.getCertifDate());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_NO, cert.getCertifNumber());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_LOCATION, cert.getCertifLocation());
		cvDiveLogInfo.put(COLUMN_DIVER_CERT_IS_PRIMARY, cert.getPrimary());

		String updateWhereClause = "_id=" + cert.getLocalId();
		getWritableDatabase().update(TABLE_DIVER_CERTIFICATIONS, cvDiveLogInfo,
				updateWhereClause, null);
	}

	public void insertDiveSitePicture(DiveSitePicture diveSitePicture) {
		ContentValues cvDiveSitePictures = new ContentValues();

		cvDiveSitePictures.put(COLUMN_PICTURE_ONLINE_ID,
				diveSitePicture.getOnlineId());
		cvDiveSitePictures.put(COLUMN_PICTURE_SITE_LOCAL_ID,
				diveSitePicture.getDiveSiteLocalID());
		cvDiveSitePictures.put(COLUMN_PICTURE_SITE_ONLINE_ID,
				diveSitePicture.getDiveSiteOnlineID());
		cvDiveSitePictures.put(COLUMN_PICTURE_IMAGE_PATH,
				diveSitePicture.getBitmapFilePath());
		cvDiveSitePictures.put(COLUMN_PICTURE_URL,
				diveSitePicture.getBitmapURL());
		cvDiveSitePictures.put(COLUMN_PICTURE_DESCRIPTION,
				diveSitePicture.getPictureDescription());

		long diveSitePictureID = getWritableDatabase().insert(TABLE_PICTURES,
				null, cvDiveSitePictures);
		diveSitePicture.setLocalId(diveSitePictureID);
	}

	public void saveDiveSitePicture(DiveSitePicture diveSitePicture) {
		ContentValues cvDiveSitePictures = new ContentValues();

		cvDiveSitePictures.put(COLUMN_PICTURE_ONLINE_ID,
				diveSitePicture.getOnlineId());
		cvDiveSitePictures.put(COLUMN_PICTURE_SITE_LOCAL_ID,
				diveSitePicture.getDiveSiteLocalID());
		cvDiveSitePictures.put(COLUMN_PICTURE_SITE_ONLINE_ID,
				diveSitePicture.getDiveSiteOnlineID());
		cvDiveSitePictures.put(COLUMN_PICTURE_IMAGE_PATH,
				diveSitePicture.getBitmapFilePath());
		cvDiveSitePictures.put(COLUMN_PICTURE_URL,
				diveSitePicture.getBitmapURL());
		cvDiveSitePictures.put(COLUMN_PICTURE_DESCRIPTION,
				diveSitePicture.getPictureDescription());

		String updateWhereClause = "_id=" + diveSitePicture.getLocalId();
		getWritableDatabase().update(TABLE_PICTURES, cvDiveSitePictures,
				updateWhereClause, null);
	}
	
	public void insertScheduledDive(ScheduledDive scheduledDive) {
		ContentValues cvScheduledDiveInfo = new ContentValues();

		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_ONLINE_ID, scheduledDive.getOnlineId());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_TITLE, scheduledDive.getTitle());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID, scheduledDive.getSubmitterId());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_TIMESTAMP, scheduledDive.getTimestamp().getTime());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_COMMENT, scheduledDive.getComment());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_IS_PUBLISHED, scheduledDive.isPublished());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_LAST_MODIFIED_ONLINE, scheduledDive.getLastModifiedOnline().getTime());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH, scheduledDive.requiresRefresh());
		
		long scheduledDiveID = getWritableDatabase().insert(TABLE_SCHEDULED_DIVES, null, cvScheduledDiveInfo);
		scheduledDive.setLocalId(scheduledDiveID);

		// Save Scheduled Dive Sites and Users
		for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
			scheduledDive.getScheduledDiveDiveSites().get(i).setScheduledDiveLocalId(scheduledDiveID);
			scheduledDive.getScheduledDiveDiveSites().get(i).setScheduledDiveOnlineId(scheduledDive.getOnlineId());
			insertScheduledDiveDiveSite(scheduledDive.getScheduledDiveDiveSites().get(i));
		}
		
		for (int i = 0; i < scheduledDive.getScheduledDiveUsers().size(); i++) {
			scheduledDive.getScheduledDiveUsers().get(i).setScheduledDiveLocalId(scheduledDiveID);
			scheduledDive.getScheduledDiveUsers().get(i).setScheduledDiveOnlineId(scheduledDive.getOnlineId());
			insertScheduledDiveUser(scheduledDive.getScheduledDiveUsers().get(i));
		}
	}

	public void saveScheduledDive(ScheduledDive scheduledDive) {
		ContentValues cvScheduledDiveInfo = new ContentValues();

		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_ONLINE_ID, scheduledDive.getOnlineId());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_TITLE, scheduledDive.getTitle());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID, scheduledDive.getSubmitterId());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_TIMESTAMP, scheduledDive.getTimestamp().getTime());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_COMMENT, scheduledDive.getComment());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_IS_PUBLISHED, scheduledDive.isPublished());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_LAST_MODIFIED_ONLINE, scheduledDive.getLastModifiedOnline().getTime());
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH, scheduledDive.requiresRefresh());
		
		String updateWhereClause = "_id=" + scheduledDive.getLocalId();
		getWritableDatabase().update(TABLE_SCHEDULED_DIVES, cvScheduledDiveInfo, updateWhereClause, null);

		// First delete Scheduled Dive Sites and Users
		deleteScheduledDiveDiveSitesForScheduledDive(scheduledDive.getLocalId());
		deleteScheduledDiveUsersForScheduledDive(scheduledDive.getLocalId());
		
		// Save Scheduled Dive Sites and Users
		for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
			scheduledDive.getScheduledDiveDiveSites().get(i).setScheduledDiveLocalId(scheduledDive.getLocalId());
			scheduledDive.getScheduledDiveDiveSites().get(i).setScheduledDiveOnlineId(scheduledDive.getOnlineId());			
			insertScheduledDiveDiveSite(scheduledDive.getScheduledDiveDiveSites().get(i));
		}
		
		for (int i = 0; i < scheduledDive.getScheduledDiveUsers().size(); i++) {
			scheduledDive.getScheduledDiveUsers().get(i).setScheduledDiveLocalId(scheduledDive.getLocalId());
			scheduledDive.getScheduledDiveUsers().get(i).setScheduledDiveOnlineId(scheduledDive.getOnlineId());
			insertScheduledDiveUser(scheduledDive.getScheduledDiveUsers().get(i));
		}
	}
	
	public void setScheduledDiveRequiresRefresh(ScheduledDive scheduledDive) {
		ContentValues cvScheduledDiveInfo = new ContentValues();
		cvScheduledDiveInfo.put(COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH, scheduledDive.requiresRefresh());
		
		String updateWhereClause = "_id=" + scheduledDive.getLocalId();
		getWritableDatabase().update(TABLE_SCHEDULED_DIVES, cvScheduledDiveInfo, updateWhereClause, null);
	}
	
	public void insertScheduledDiveDiveSite(ScheduledDiveDiveSite scheduledDiveDiveSite) {
		ContentValues cvScheduledDiveDiveSiteInfo = new ContentValues();

		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_ONLINE_ID, scheduledDiveDiveSite.getOnlineId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID, scheduledDiveDiveSite.getScheduledDiveLocalId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_ONLINE_ID, scheduledDiveDiveSite.getScheduledDiveOnlineId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID, scheduledDiveDiveSite.getDiveSiteLocalId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_ONLINE_ID, scheduledDiveDiveSite.getDiveSiteOnlineId());

		long scheduledDiveDiveSiteID = getWritableDatabase().insert(TABLE_SCHEDULED_DIVES_DIVE_SITES, null, cvScheduledDiveDiveSiteInfo);
		scheduledDiveDiveSite.setLocalId(scheduledDiveDiveSiteID);
	}

	public void saveScheduledDiveDiveSite(ScheduledDiveDiveSite scheduledDiveDiveSite) {
		ContentValues cvScheduledDiveDiveSiteInfo = new ContentValues();

		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_ONLINE_ID, scheduledDiveDiveSite.getOnlineId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID, scheduledDiveDiveSite.getScheduledDiveLocalId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_ONLINE_ID, scheduledDiveDiveSite.getScheduledDiveOnlineId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID, scheduledDiveDiveSite.getDiveSiteLocalId());
		cvScheduledDiveDiveSiteInfo.put(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_ONLINE_ID, scheduledDiveDiveSite.getDiveSiteOnlineId());

		String updateWhereClause = "_id=" + scheduledDiveDiveSite.getLocalId();
		getWritableDatabase().update(TABLE_SCHEDULED_DIVES_DIVE_SITES, cvScheduledDiveDiveSiteInfo, updateWhereClause, null);
	}
	
	public void insertScheduledDiveUser(ScheduledDiveUser scheduledDiveUser) {
		ContentValues cvScheduledDiveUserInfo = new ContentValues();

		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_ONLINE_ID, scheduledDiveUser.getOnlineId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID, scheduledDiveUser.getScheduledDiveLocalId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_ONLINE_ID, scheduledDiveUser.getScheduledDiveOnlineId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID, scheduledDiveUser.getVotedScheduledDiveDiveSiteLocalId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_ONLINE_ID, scheduledDiveUser.getVotedScheduledDiveDiveSiteOnlineId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID, scheduledDiveUser.getUserId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_ATTEND_STATE, scheduledDiveUser.getAttendState().getName());
		
		long scheduledDiveUserID = getWritableDatabase().insert(TABLE_SCHEDULED_DIVES_USERS, null, cvScheduledDiveUserInfo);
		scheduledDiveUser.setLocalId(scheduledDiveUserID);
	}

	public void saveScheduledDiveUser(ScheduledDiveUser scheduledDiveUser) {
		ContentValues cvScheduledDiveUserInfo = new ContentValues();

		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_ONLINE_ID, scheduledDiveUser.getOnlineId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID, scheduledDiveUser.getScheduledDiveLocalId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_ONLINE_ID, scheduledDiveUser.getScheduledDiveOnlineId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID, scheduledDiveUser.getVotedScheduledDiveDiveSiteLocalId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_ONLINE_ID, scheduledDiveUser.getVotedScheduledDiveDiveSiteOnlineId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID, scheduledDiveUser.getUserId());
		cvScheduledDiveUserInfo.put(COLUMN_SCHEDULED_DIVES_USER_ATTEND_STATE, scheduledDiveUser.getAttendState().getName());

		String updateWhereClause = "_id=" + scheduledDiveUser.getLocalId();
		getWritableDatabase().update(TABLE_SCHEDULED_DIVES_USERS, cvScheduledDiveUserInfo, updateWhereClause, null);
	}
	
	public void setDiveSiteArchive(long diveSiteID, boolean archive) {
		ContentValues cvDiveSiteInfo = new ContentValues();

		cvDiveSiteInfo.put(COLUMN_DIVESITE_IS_ARCHIVED, archive);

		String updateWhereClause = "_id=" + diveSiteID;
		getWritableDatabase().update(TABLE_DIVESITES, cvDiveSiteInfo,
				updateWhereClause, null);
	}

	public DiveSiteCursor queryDiveSites() {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVESITES, null,
				null, null, null, null, COLUMN_DIVESITE_NAME + " asc");

		return new DiveSiteCursor(wrapped);
	}

	public DiveSiteCursor queryVisibleDiveSites(String filterSelection,
			ArrayList<String> filterSelectionArgs, String minLatitude,
			String maxLatitude, String minLongitude, String maxLongitude,
			long diverSubmitterID) {

		// Generate required string array
		if (filterSelectionArgs == null) {
			filterSelectionArgs = new ArrayList<String>();
		}

		if (filterSelection == null) {
			filterSelection = "";
		}

		// SQL where selection for min, max latitude and longitude
		String latitudeSelection = COLUMN_DIVESITE_LATITUDE + " >= ? AND "
				+ COLUMN_DIVESITE_LATITUDE + " <= ? AND "
				+ COLUMN_DIVESITE_LONGITUDE + " >= ? AND "
				+ COLUMN_DIVESITE_LONGITUDE + " <= ? ";

		// SQL where selection for submitter diver ID
		String diverSubmitter = "";
		if (diverSubmitterID != -1) {
			diverSubmitter = " AND " + COLUMN_DIVESITE_USER_ID + " = ?";
		}

		// Add 0 argument to return un-archived dive sites
		filterSelectionArgs.add(0, "0");

		// Add min, max latitude and longitude
		filterSelectionArgs.add(1, minLatitude);
		filterSelectionArgs.add(2, maxLatitude);
		filterSelectionArgs.add(3, minLongitude);
		filterSelectionArgs.add(4, maxLongitude);

		// Add diver submitter ID
		if (diverSubmitterID != -1) {
			filterSelectionArgs.add(5, String.valueOf(diverSubmitterID));
		}

		String[] selectionArgs = new String[filterSelectionArgs.size()];
		filterSelectionArgs.toArray(selectionArgs);

		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVESITES,
				null, // All columns
				COLUMN_DIVESITE_IS_ARCHIVED + " = ? AND " + latitudeSelection
						+ diverSubmitter + filterSelection, selectionArgs,
				null, null, COLUMN_DIVESITE_NAME + " asc", null);

		return new DiveSiteCursor(wrapped);
	}

    public int queryVisibleDiveSitesCount(String filterSelection,
        ArrayList<String> filterSelectionArgs, String minLatitude,
        String maxLatitude, String minLongitude, String maxLongitude,
        long diverSubmitterID) {

        // Generate required string array
        if (filterSelectionArgs == null) {
            filterSelectionArgs = new ArrayList<String>();
        }

        if (filterSelection == null) {
            filterSelection = "";
        }

        // SQL where selection for min, max latitude and longitude
        String latitudeSelection = COLUMN_DIVESITE_LATITUDE + " >= ? AND "
                + COLUMN_DIVESITE_LATITUDE + " <= ? AND "
                + COLUMN_DIVESITE_LONGITUDE + " >= ? AND "
                + COLUMN_DIVESITE_LONGITUDE + " <= ? ";

        // SQL where selection for submitter diver ID
        String diverSubmitter = "";
        if (diverSubmitterID != -1) {
            diverSubmitter = " AND " + COLUMN_DIVESITE_USER_ID + " = ?";
        }

        // Add 0 argument to return un-archived dive sites
        filterSelectionArgs.add(0, "0");

        // Add min, max latitude and longitude
        filterSelectionArgs.add(1, minLatitude);
        filterSelectionArgs.add(2, maxLatitude);
        filterSelectionArgs.add(3, minLongitude);
        filterSelectionArgs.add(4, maxLongitude);

        // Add diver submitter ID
        if (diverSubmitterID != -1) {
            filterSelectionArgs.add(5, String.valueOf(diverSubmitterID));
        }

        String[] selectionArgs = new String[filterSelectionArgs.size()];
        filterSelectionArgs.toArray(selectionArgs);

        String sqlWhere = " WHERE " + COLUMN_DIVESITE_IS_ARCHIVED + " = ? AND " +
                latitudeSelection + diverSubmitter + filterSelection;

        String sql = "SELECT COUNT(site." + COLUMN_DIVESITE_LOCAL_ID + ") as " + COLUMN_DIVESITE_LOG_COUNT +
            " FROM " + TABLE_DIVESITES + " site " + sqlWhere;

        Cursor cursor = getReadableDatabase().rawQuery(sql, selectionArgs);
        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_DIVESITE_LOG_COUNT));
        }
        cursor.close();

        return count;
    }

	public DiveSiteCursor queryArchivedDiveSites(String filterSelection,
			ArrayList<String> filterSelectionArgs, String minLatitude,
			String maxLatitude, String minLongitude, String maxLongitude,
			long diverSubmitterID) {

		// Generate required string array
		if (filterSelectionArgs == null) {
			filterSelectionArgs = new ArrayList<String>();
		}

		if (filterSelection == null) {
			filterSelection = "";
		}

		// SQL where selection for min, max latitude and longitude
		String latitudeSelection = COLUMN_DIVESITE_LATITUDE + " >= ? AND "
				+ COLUMN_DIVESITE_LATITUDE + " <= ? AND "
				+ COLUMN_DIVESITE_LONGITUDE + " >= ? AND "
				+ COLUMN_DIVESITE_LONGITUDE + " <= ? ";

		// SQL where selection for submitter diver ID
		String diverSubmitter = "";
		if (diverSubmitterID != -1) {
			diverSubmitter = " AND " + COLUMN_DIVESITE_USER_ID + " = ?";
		}

		// Add 1 argument to return archived dive sites
		filterSelectionArgs.add(0, "1");

		// Add min, max latitude and longitude
		filterSelectionArgs.add(1, minLatitude);
		filterSelectionArgs.add(2, maxLatitude);
		filterSelectionArgs.add(3, minLongitude);
		filterSelectionArgs.add(4, maxLongitude);

		// Add diver submitter ID
		if (diverSubmitterID != -1) {
			filterSelectionArgs.add(5, String.valueOf(diverSubmitterID));
		}

		String[] selectionArgs = new String[filterSelectionArgs.size()];
		filterSelectionArgs.toArray(selectionArgs);

		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVESITES,
				null, // All columns
				COLUMN_DIVESITE_IS_ARCHIVED + " = ? AND " + latitudeSelection
						+ diverSubmitter + filterSelection, selectionArgs,
				null, null, COLUMN_DIVESITE_NAME + " asc", null);

		return new DiveSiteCursor(wrapped);
	}

    public int queryArchivedDiveSitesCount(String filterSelection,
        ArrayList<String> filterSelectionArgs, String minLatitude,
        String maxLatitude, String minLongitude, String maxLongitude,
        long diverSubmitterID) {

        // Generate required string array
        if (filterSelectionArgs == null) {
            filterSelectionArgs = new ArrayList<String>();
        }

        if (filterSelection == null) {
            filterSelection = "";
        }

        // SQL where selection for min, max latitude and longitude
        String latitudeSelection = COLUMN_DIVESITE_LATITUDE + " >= ? AND "
                + COLUMN_DIVESITE_LATITUDE + " <= ? AND "
                + COLUMN_DIVESITE_LONGITUDE + " >= ? AND "
                + COLUMN_DIVESITE_LONGITUDE + " <= ? ";

        // SQL where selection for submitter diver ID
        String diverSubmitter = "";
        if (diverSubmitterID != -1) {
            diverSubmitter = " AND " + COLUMN_DIVESITE_USER_ID + " = ?";
        }

        // Add 1 argument to return archived dive sites
        filterSelectionArgs.add(0, "1");

        // Add min, max latitude and longitude
        filterSelectionArgs.add(1, minLatitude);
        filterSelectionArgs.add(2, maxLatitude);
        filterSelectionArgs.add(3, minLongitude);
        filterSelectionArgs.add(4, maxLongitude);

        // Add diver submitter ID
        if (diverSubmitterID != -1) {
            filterSelectionArgs.add(5, String.valueOf(diverSubmitterID));
        }

        String[] selectionArgs = new String[filterSelectionArgs.size()];
        filterSelectionArgs.toArray(selectionArgs);

        String sqlWhere = " WHERE " + COLUMN_DIVESITE_IS_ARCHIVED + " = ? AND " +
                latitudeSelection + diverSubmitter + filterSelection;

        String sql = "SELECT COUNT(site." + COLUMN_DIVESITE_LOCAL_ID + ") as " + COLUMN_DIVESITE_LOG_COUNT +
                " FROM " + TABLE_DIVESITES + " site " + sqlWhere;

        Cursor cursor = getReadableDatabase().rawQuery(sql, selectionArgs);
        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_DIVESITE_LOG_COUNT));
        }
        cursor.close();

        return count;
    }

	public DiveSiteCursor queryPublishedDiveSites(Boolean isPublished,
			String filterSelection, ArrayList<String> filterSelectionArgs,
			String minLatitude, String maxLatitude, String minLongitude,
			String maxLongitude, long diverSubmitterID) {

		// Generate required string array
		if (filterSelectionArgs == null) {
			filterSelectionArgs = new ArrayList<String>();
		}

		if (filterSelection == null) {
			filterSelection = "";
		}

		// SQL where selection for min, max latitude and longitude
		String latitudeSelection = COLUMN_DIVESITE_LATITUDE + " >= ? AND "
				+ COLUMN_DIVESITE_LATITUDE + " <= ? AND "
				+ COLUMN_DIVESITE_LONGITUDE + " >= ? AND "
				+ COLUMN_DIVESITE_LONGITUDE + " <= ? ";

		// SQL where selection for submitter diver ID
		String diverSubmitter = "";
		if (diverSubmitterID != -1) {
			diverSubmitter = " AND " + COLUMN_DIVESITE_USER_ID + " = ?";
		}

		// Add 1 argument to return published dive sites
		filterSelectionArgs.add(0, isPublished ? "1" : "0");

		// Add min, max latitude and longitude
		filterSelectionArgs.add(1, minLatitude);
		filterSelectionArgs.add(2, maxLatitude);
		filterSelectionArgs.add(3, minLongitude);
		filterSelectionArgs.add(4, maxLongitude);

		// Add diver submitter ID
		if (diverSubmitterID != -1) {
			filterSelectionArgs.add(5, String.valueOf(diverSubmitterID));
		}

		String[] selectionArgs = new String[filterSelectionArgs.size()];
		filterSelectionArgs.toArray(selectionArgs);

		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVESITES,
				null, // All columns
				COLUMN_DIVESITE_IS_PUBLISHED + " = ? AND " + latitudeSelection
						+ diverSubmitter + filterSelection, selectionArgs,
				null, null, COLUMN_DIVESITE_NAME + " asc", null);

		return new DiveSiteCursor(wrapped);
	}

    public int queryPublishedDiveSitesCount(Boolean isPublished,
        String filterSelection, ArrayList<String> filterSelectionArgs,
        String minLatitude, String maxLatitude, String minLongitude,
        String maxLongitude, long diverSubmitterID) {

        // Generate required string array
        if (filterSelectionArgs == null) {
            filterSelectionArgs = new ArrayList<String>();
        }

        if (filterSelection == null) {
            filterSelection = "";
        }

        // SQL where selection for min, max latitude and longitude
        String latitudeSelection = COLUMN_DIVESITE_LATITUDE + " >= ? AND "
                + COLUMN_DIVESITE_LATITUDE + " <= ? AND "
                + COLUMN_DIVESITE_LONGITUDE + " >= ? AND "
                + COLUMN_DIVESITE_LONGITUDE + " <= ? ";

        // SQL where selection for submitter diver ID
        String diverSubmitter = "";
        if (diverSubmitterID != -1) {
            diverSubmitter = " AND " + COLUMN_DIVESITE_USER_ID + " = ?";
        }

        // Add 1 argument to return published dive sites
        filterSelectionArgs.add(0, isPublished ? "1" : "0");

        // Add min, max latitude and longitude
        filterSelectionArgs.add(1, minLatitude);
        filterSelectionArgs.add(2, maxLatitude);
        filterSelectionArgs.add(3, minLongitude);
        filterSelectionArgs.add(4, maxLongitude);

        // Add diver submitter ID
        if (diverSubmitterID != -1) {
            filterSelectionArgs.add(5, String.valueOf(diverSubmitterID));
        }

        String[] selectionArgs = new String[filterSelectionArgs.size()];
        filterSelectionArgs.toArray(selectionArgs);

        String sqlWhere = " WHERE " + COLUMN_DIVESITE_IS_ARCHIVED + " = ? AND " +
                latitudeSelection + diverSubmitter + filterSelection;

        String sql = "SELECT COUNT(site." + COLUMN_DIVESITE_LOCAL_ID + ") as " + COLUMN_DIVESITE_LOG_COUNT +
                " FROM " + TABLE_DIVESITES + " site " + sqlWhere;

        Cursor cursor = getReadableDatabase().rawQuery(sql, selectionArgs);
        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_DIVESITE_LOG_COUNT));
        }
        cursor.close();

        return count;
    }

	public DiveSiteCursor queryDiveSite(long id) {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVESITES, null, // All
																			// columns
				COLUMN_DIVESITE_LOCAL_ID + " = ?", // Look for a dive site ID
				new String[] { String.valueOf(id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		return new DiveSiteCursor(wrapped);
	}

	public DiveSiteCursor queryDiveSites(String diveSiteName) {
		Cursor cursor = getReadableDatabase().query(
				TABLE_DIVESITES,
				null, // All columns
				"lower(" + COLUMN_DIVESITE_NAME + ") LIKE ?",
				new String[] { "%" + diveSiteName.toLowerCase() + "%" }, null,
				null, COLUMN_DIVESITE_NAME + " asc", null);

		return new DiveSiteCursor(cursor);
	}

	public long queryDiveSiteLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_DIVESITES,
				new String[] { COLUMN_DIVESITE_LOCAL_ID }, // Local ID only
				COLUMN_DIVESITE_ONLINE_ID + " = ?", // Look for an online ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_DIVESITE_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public PictureCursor queryDiveSitePictures(long diveSiteID) {
		Cursor wrapped = getReadableDatabase().query(TABLE_PICTURES, null,
				COLUMN_PICTURE_SITE_LOCAL_ID + " = ?", // Limit to the given
														// dive site
				new String[] { String.valueOf(diveSiteID) }, null, // group by
				null, // having
				COLUMN_PICTURE_LOCAL_ID + " asc"); // order by picture id

		return new PictureCursor(wrapped);
	}

	public long queryDiveSitePictureLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_PICTURES,
				new String[] { COLUMN_PICTURE_LOCAL_ID }, // Local ID only
				COLUMN_PICTURE_ONLINE_ID + " = ?", // Look for an online ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_PICTURE_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public void deleteDiveSite(long diveSiteID) {
		getWritableDatabase().delete(TABLE_DIVESITES,
				COLUMN_DIVESITE_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveSiteID) });

		getWritableDatabase().delete(TABLE_PICTURES,
				COLUMN_PICTURE_SITE_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveSiteID) });
	}

	public DiverCursor queryDivers() {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVERS, null, null,
				null, null, null, COLUMN_DIVER_USERNAME + " asc");

		return new DiverCursor(wrapped);
	}

	public DiverCursor queryFilteredDivers(String filterSelection,
			ArrayList<String> filterSelectionArgs) {

		// Generate required string array
		if (filterSelectionArgs == null) {
			filterSelectionArgs = new ArrayList<String>();
		}

		if (filterSelection == null) {
			filterSelection = "";
		}

		String[] selectionArgs = new String[filterSelectionArgs.size()];
		filterSelectionArgs.toArray(selectionArgs);

		Cursor wrapped = getReadableDatabase().query(TABLE_DIVERS,
				null, // All columns
				filterSelection, selectionArgs, null, null,
				COLUMN_DIVER_USERNAME + " asc", null);

		return new DiverCursor(wrapped);
	}

	public DiverCursor queryDiver(long id) {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVERS, null, // All
																			// columns
				COLUMN_DIVER_LOCAL_ID + " = ?", // Look for a diver ID
				new String[] { String.valueOf(id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		return new DiverCursor(wrapped);
	}

	public long queryDiverLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_DIVERS,
				new String[] { COLUMN_DIVER_LOCAL_ID }, // Local ID only
				COLUMN_DIVER_ONLINE_ID + " = ?", // Look for an online ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();

		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_DIVER_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public DiverCertificationCursor queryDiverCertifications(long diverID) {
		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVER_CERTIFICATIONS, null,
				COLUMN_DIVER_CERT_USER_ID + " = ?",
				new String[] { String.valueOf(diverID) }, null, null,
				COLUMN_DIVER_CERT_LOCAL_ID + " asc", null);

		return new DiverCertificationCursor(wrapped);
	}

	public DiverCertificationCursor queryDiverCertification(long id) {
		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVER_CERTIFICATIONS, null, // All columns
				COLUMN_DIVER_CERT_LOCAL_ID + " = ?", // Look for a dive log ID
				new String[] { String.valueOf(id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		return new DiverCertificationCursor(wrapped);
	}

	public long queryDiverCertificationLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_DIVER_CERTIFICATIONS,
				new String[] { COLUMN_DIVER_CERT_LOCAL_ID }, // Local ID only
				COLUMN_DIVER_CERT_ONLINE_ID + " = ?", // Look for an online ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_DIVER_CERT_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public DiveLogCursor queryDiveLogs(long diverID, DiveSite diveSite, boolean unPublishedOnly) {

        long diveSiteID = -1;
        if (diveSite != null) {
            diveSiteID = diveSite.getLocalId();
        }

		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVELOG,
				null,
				"(" + ((diverID == -1) ? 1 : 0) + " OR " + COLUMN_DIVELOG_USER_ID + " = ?) AND " +
                "(" + ((diveSite == null) ? 1 : 0) + " OR " + COLUMN_DIVELOG_SITE_LOCAL_ID + " = ?) AND " +
                "(" + (unPublishedOnly ? 0 : 1) + " OR " + COLUMN_DIVELOG_IS_PUBLISHED + " = ?)",
				new String[] { String.valueOf(diverID),
						       String.valueOf(diveSiteID),
                               unPublishedOnly ? "0" : "1"},
                null, null,
				COLUMN_DIVELOG_TIMESTAMP + " desc", null);

		return new DiveLogCursor(wrapped);
	}

    public int queryDiveLogsCount(long diverID, DiveSite diveSite, boolean unPublishedOnly) {

        long diveSiteID = -1;
        if (diveSite != null) {
            diveSiteID = diveSite.getLocalId();
        }

        String sqlWhere = "WHERE (" + ((diverID == -1) ? 1 : 0) + " OR " + COLUMN_DIVELOG_USER_ID + " = ?) AND " +
                "(" + ((diveSite == null) ? 1 : 0) + " OR " + COLUMN_DIVELOG_SITE_LOCAL_ID + " = ?) AND " +
                "(" + (unPublishedOnly ? 0 : 1) + " OR " + COLUMN_DIVELOG_IS_PUBLISHED + " = ?)";

        String sql =
            "SELECT COUNT(log." + COLUMN_DIVELOG_LOCAL_ID + ") as " + COLUMN_DIVELOG_LOG_COUNT +
                    " FROM " + TABLE_DIVELOG + " log " +
                    sqlWhere +
                    " ORDER BY " + COLUMN_DIVELOG_TIMESTAMP + " desc";

        String[] sqlWhereArgs = new String[] { String.valueOf(diverID),
            String.valueOf(diveSiteID),
            unPublishedOnly ? "0" : "1"};

        Cursor cursor = getReadableDatabase().rawQuery(sql, sqlWhereArgs);
        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_DIVELOG_LOG_COUNT));
        }
        cursor.close();

        return count;
    }

    public int queryDiveLogsTotalMinutes(long diverID, DiveSite diveSite, boolean unPublishedOnly) {

        long diveSiteID = -1;
        if (diveSite != null) {
            diveSiteID = diveSite.getLocalId();
        }

        String sqlWhere = "WHERE (" + ((diverID == -1) ? 1 : 0) + " OR " + COLUMN_DIVELOG_USER_ID + " = ?) AND " +
                "(" + ((diveSite == null) ? 1 : 0) + " OR " + COLUMN_DIVELOG_SITE_LOCAL_ID + " = ?) AND " +
                "(" + (unPublishedOnly ? 0 : 1) + " OR " + COLUMN_DIVELOG_IS_PUBLISHED + " = ?)";

        String sql =
                "SELECT SUM(log." + COLUMN_DIVELOG_DIVE_TIME + ") as " + COLUMN_DIVELOG_TIME_SUM +
                        " FROM " + TABLE_DIVELOG + " log " +
                        sqlWhere +
                        " ORDER BY " + COLUMN_DIVELOG_TIMESTAMP + " desc";

        String[] sqlWhereArgs = new String[] { String.valueOf(diverID),
            String.valueOf(diveSiteID),
            unPublishedOnly ? "0" : "1"};

        Cursor cursor = getReadableDatabase().rawQuery(sql, sqlWhereArgs);
        cursor.moveToFirst();

        int sum = 0;
        if (!cursor.isAfterLast()) {
            sum = cursor.getInt(cursor.getColumnIndex(COLUMN_DIVELOG_TIME_SUM));
        }
        cursor.close();

        return sum;
    }

	public DiveLogCursor queryDiveLog(long id) {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVELOG, null, // All
																			// columns
				COLUMN_DIVELOG_LOCAL_ID + " = ?", // Look for a dive log ID
				new String[] { String.valueOf(id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		return new DiveLogCursor(wrapped);
	}

	public DiveLogBuddyCursor queryDiveLogBuddies(long diveLogID) {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVELOG_BUDDIES,
				null, COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogID) }, null, null,
				COLUMN_DIVELOG_BUDDIES_LOCAL_ID + " asc", null);

		return new DiveLogBuddyCursor(wrapped);
	}

	public DiveLogBuddyCursor queryDiveLogBuddy(long id) {
		Cursor wrapped = getReadableDatabase().query(
				TABLE_DIVELOG_BUDDIES,
				null, // All columns
				COLUMN_DIVELOG_BUDDIES_LOCAL_ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, null, "1");

		return new DiveLogBuddyCursor(wrapped);
	}

	public long queryDiveLogBuddyLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_DIVELOG_BUDDIES,
				new String[] { COLUMN_DIVELOG_BUDDIES_LOCAL_ID }, // Local ID
																	// only
				COLUMN_DIVELOG_BUDDIES_ONLINE_ID + " = ?", // Look for an online
															// ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();

		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_DIVELOG_BUDDIES_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public DiveLogStopCursor queryDiveLogStops(long diveLogID) {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVELOG_STOPS, null,
				COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogID) }, null, null,
				COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID + " asc", null);

		return new DiveLogStopCursor(wrapped);
	}

	public DiveLogStopCursor queryDiveLogStop(long id) {
		Cursor wrapped = getReadableDatabase().query(TABLE_DIVELOG_STOPS,
				null, // All columns
				COLUMN_DIVELOG_STOPS_LOCAL_ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, null, "1");

		return new DiveLogStopCursor(wrapped);
	}

	public long queryDiveLogStopLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_DIVELOG_STOPS,
				new String[] { COLUMN_DIVELOG_STOPS_LOCAL_ID }, // Local ID only
				COLUMN_DIVELOG_STOPS_ONLINE_ID + " = ?", // Look for an online
															// ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();

		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_DIVELOG_STOPS_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public long queryDiveLogLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_DIVELOG,
				new String[] { COLUMN_DIVELOG_LOCAL_ID }, // Local ID only
				COLUMN_DIVELOG_ONLINE_ID + " = ?", // Look for an online ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();
		
		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor
					.getColumnIndex(COLUMN_DIVELOG_LOCAL_ID));
		cursor.close();

		return local_id;
	}
	
	public ScheduledDiveCursor queryScheduledDiveForSubmitter(long submitterID, 
			boolean isPublished, boolean isUnPublished, String title, String country,
			String state, String city, String timeStampStart, String timeStampEnd) {
		
		String sqlTimestamp = "";
		if (!timeStampStart.trim().isEmpty()) {
			sqlTimestamp = " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " >= " + timeStampStart;
		}
		if (!timeStampEnd.trim().isEmpty()) {
			sqlTimestamp = sqlTimestamp +
				" AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " <= " + timeStampEnd;
		}
		
		String sqlDiveSitesWhere;
		if (title.isEmpty() && country.isEmpty() && state.isEmpty() && city.isEmpty()) {
			sqlDiveSitesWhere = "1";
		} else {
			sqlDiveSitesWhere = 
				" sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
					" IN (SELECT " + 
					COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
				 " FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " + "LEFT JOIN " + TABLE_DIVESITES + " d " 
					+ "ON sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID + " = " + "d." + COLUMN_DIVESITE_LOCAL_ID +
				 " WHERE d." + COLUMN_DIVESITE_NAME + " LIKE '%" + title + "%'" + " AND " +
				 	"d." + COLUMN_DIVESITE_COUNTRY + " LIKE '%" + country + "%'" + " AND " +
				 	"d." + COLUMN_DIVESITE_PROVINCE + " LIKE '%" + state + "%'" + " AND " +
				 	"d." + COLUMN_DIVESITE_CITY + " LIKE '%" + city + "%')";
		}

		String sqlWhere = sqlDiveSitesWhere;
		
		if (submitterID != -1) {
			sqlWhere = "(" + sqlWhere + " AND " + 
					COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID + 
					" = " + String.valueOf(submitterID) + ")";
		}
		
		if (isPublished && !isUnPublished) {
			sqlWhere = sqlWhere + " AND " + 
					COLUMN_SCHEDULED_DIVES_IS_PUBLISHED + 
					" = 1";
		} else if (isUnPublished && !isPublished) {
			sqlWhere = sqlWhere + " AND " + 
					COLUMN_SCHEDULED_DIVES_IS_PUBLISHED + 
					" = 0";
		} else if (!isPublished && !isUnPublished) {
			// Show nothing if neither checkbox was selected
			sqlWhere = sqlWhere + " AND 0";
		}
		
		sqlWhere = sqlWhere + sqlTimestamp;
		
		String sql = 
				"SELECT *" +
				" FROM " + TABLE_SCHEDULED_DIVES + " sd " +
				" WHERE " + sqlWhere +
				" ORDER BY " + COLUMN_SCHEDULED_DIVES_TIMESTAMP;
		
		Cursor wrapped = getReadableDatabase().rawQuery(sql, null);

		return new ScheduledDiveCursor(wrapped);
	}

    public int queryScheduledDiveForSubmitterCount(long submitterID,
        boolean isPublished, boolean isUnPublished, String title, String country,
        String state, String city, String timeStampStart, String timeStampEnd) {

        String sqlTimestamp = "";
        if (!timeStampStart.trim().isEmpty()) {
            sqlTimestamp = " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " >= " + timeStampStart;
        }
        if (!timeStampEnd.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " <= " + timeStampEnd;
        }

        String sqlDiveSitesWhere;
        if (title.isEmpty() && country.isEmpty() && state.isEmpty() && city.isEmpty()) {
            sqlDiveSitesWhere = "1";
        } else {
            sqlDiveSitesWhere =
                    " sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
                            " IN (SELECT " +
                            COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
                            " FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " + "LEFT JOIN " + TABLE_DIVESITES + " d "
                            + "ON sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID + " = " + "d." + COLUMN_DIVESITE_LOCAL_ID +
                            " WHERE d." + COLUMN_DIVESITE_NAME + " LIKE '%" + title + "%'" + " AND " +
                            "d." + COLUMN_DIVESITE_COUNTRY + " LIKE '%" + country + "%'" + " AND " +
                            "d." + COLUMN_DIVESITE_PROVINCE + " LIKE '%" + state + "%'" + " AND " +
                            "d." + COLUMN_DIVESITE_CITY + " LIKE '%" + city + "%')";
        }

        String sqlWhere = sqlDiveSitesWhere;

        if (submitterID != -1) {
            sqlWhere = "(" + sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID +
                    " = " + String.valueOf(submitterID) + ")";
        }

        if (isPublished && !isUnPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 1";
        } else if (isUnPublished && !isPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 0";
        } else if (!isPublished && !isUnPublished) {
            // Show nothing if neither checkbox was selected
            sqlWhere = sqlWhere + " AND 0";
        }

        sqlWhere = sqlWhere + sqlTimestamp;

        String sql =
                "SELECT COUNT(sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID + ") as " + COLUMN_SCHEDULED_DIVES_COUNT +
                        " FROM " + TABLE_SCHEDULED_DIVES + " sd " +
                        " WHERE " + sqlWhere;

        Cursor cursor = getReadableDatabase().rawQuery(sql, null);

        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULED_DIVES_COUNT));
        }
        cursor.close();

        return count;
    }

    public ScheduledDiveCursor queryScheduledDiveForSite(long submitterID, long diveSiteID,
        boolean isPublished, boolean isUnPublished, String timeStampStart, String timeStampEnd) {

        String sqlDiveSitesWhere =
                "sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
                        " IN (SELECT " +
                        COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
                        " FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " +
                        " WHERE sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID +
                        " = " + String.valueOf(diveSiteID) + ")";

        String sqlTimestamp = "";
        if (!timeStampStart.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " >= " + timeStampStart;
        }
        if (!timeStampEnd.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " <= " + timeStampEnd;
        }

        String sqlWhere = sqlDiveSitesWhere;

        if (submitterID != -1) {
            sqlWhere = "(" + sqlWhere + " OR " +
                    COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID +
                    " = " + String.valueOf(submitterID) + ")";
        }

        if (isPublished && !isUnPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 1";
        } else if (isUnPublished && !isPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 0";
        } else if (!isPublished && !isUnPublished) {
            // Show nothing if neither checkbox was selected
            sqlWhere = sqlWhere + " AND 0";
        }

        sqlWhere = sqlWhere + sqlTimestamp;

        String sql =
                "SELECT *" +
                        " FROM " + TABLE_SCHEDULED_DIVES + " sd " +
                        " WHERE " + sqlWhere +
                        " ORDER BY " + COLUMN_SCHEDULED_DIVES_TIMESTAMP;

        Cursor wrapped = getReadableDatabase().rawQuery(sql, null);

        return new ScheduledDiveCursor(wrapped);
    }

    public int queryScheduledDiveForSiteCount(long submitterID, long diveSiteID,
        boolean isPublished, boolean isUnPublished, String timeStampStart, String timeStampEnd) {

        String sqlDiveSitesWhere =
                "sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
                        " IN (SELECT " +
                        COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
                        " FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " +
                        " WHERE sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID +
                        " = " + String.valueOf(diveSiteID) + ")";

        String sqlTimestamp = "";
        if (!timeStampStart.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " >= " + timeStampStart;
        }
        if (!timeStampEnd.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " <= " + timeStampEnd;
        }

        String sqlWhere = sqlDiveSitesWhere;

        if (submitterID != -1) {
            sqlWhere = "(" + sqlWhere + " OR " +
                    COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID +
                    " = " + String.valueOf(submitterID) + ")";
        }

        if (isPublished && !isUnPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 1";
        } else if (isUnPublished && !isPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 0";
        } else if (!isPublished && !isUnPublished) {
            // Show nothing if neither checkbox was selected
            sqlWhere = sqlWhere + " AND 0";
        }

        sqlWhere = sqlWhere + sqlTimestamp;

        String sql =
                "SELECT COUNT(" + COLUMN_SCHEDULED_DIVES_LOCAL_ID + ") as " + COLUMN_SCHEDULED_DIVES_COUNT +
                        " FROM " + TABLE_SCHEDULED_DIVES + " sd " +
                        " WHERE " + sqlWhere +
                        " ORDER BY " + COLUMN_SCHEDULED_DIVES_TIMESTAMP;

        Cursor cursor = getReadableDatabase().rawQuery(sql, null);

        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULED_DIVES_COUNT));
        }
        cursor.close();

        return count;
    }
	
	public ScheduledDiveCursor queryScheduledDiveForUser(long submitterID, long diverID, 
			boolean isPublished, boolean isUnPublished, String title, String country,
			String state, String city, String timeStampStart, String timeStampEnd) { 
		
		String sqlUsersWhere =
				"sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
				" IN (SELECT " + 
						COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID +
					 " FROM " + TABLE_SCHEDULED_DIVES_USERS + " sd_u" +
					 " WHERE sd_u." + COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID +
					   " = " + String.valueOf(diverID) + ")";
		
		String sqlTimestamp = "";
		if (!timeStampStart.trim().isEmpty()) {
			sqlTimestamp = sqlTimestamp +
					" AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " >= " + timeStampStart;
		}
		if (!timeStampEnd.trim().isEmpty()) {
			sqlTimestamp = sqlTimestamp +
					" AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " <= " + timeStampEnd;
		}
		
		String sqlDiveSitesWhere =
				" AND sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
				" IN (SELECT " + 
						COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
					 " FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " + "LEFT JOIN " + TABLE_DIVESITES + " d " 
						+ "ON sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID + " = " + "d." + COLUMN_DIVESITE_LOCAL_ID +
					 " WHERE d." + COLUMN_DIVESITE_NAME + " LIKE '%" + title + "%'" + " AND " +
					 	"d." + COLUMN_DIVESITE_COUNTRY + " LIKE '%" + country + "%'" + " AND " +
					 	"d." + COLUMN_DIVESITE_PROVINCE + " LIKE '%" + state + "%'" + " AND " +
					 	"d." + COLUMN_DIVESITE_CITY + " LIKE '%" + city + "%')";

		String sqlWhere = sqlUsersWhere;
		
		if (submitterID != -1) {
			sqlWhere = "(" + sqlWhere + " OR " + 
					COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID + 
					" = " + String.valueOf(submitterID) + ")";
		}
		
		if (isPublished && !isUnPublished) {
			sqlWhere = sqlWhere + " AND " + 
					COLUMN_SCHEDULED_DIVES_IS_PUBLISHED + 
					" = 1";
		} else if (isUnPublished && !isPublished) {
			sqlWhere = sqlWhere + " AND " + 
					COLUMN_SCHEDULED_DIVES_IS_PUBLISHED + 
					" = 0";
		} else if (!isPublished && !isUnPublished) {
			// Show nothing if neither checkbox was selected
			sqlWhere = sqlWhere + " AND 0";
		}
		
		sqlWhere = sqlWhere + sqlTimestamp + sqlDiveSitesWhere;
		
		String sql = 
				"SELECT *" +
				" FROM " + TABLE_SCHEDULED_DIVES + " sd " +
				" WHERE " + sqlWhere +
				" ORDER BY " + COLUMN_SCHEDULED_DIVES_TIMESTAMP;
		
		Cursor wrapped = getReadableDatabase().rawQuery(sql, null);

		return new ScheduledDiveCursor(wrapped);
	}

    public int queryScheduledDiveForUserCount(long submitterID, long diverID,
        boolean isPublished, boolean isUnPublished, String title, String country,
        String state, String city, String timeStampStart, String timeStampEnd) {

        String sqlUsersWhere =
                "sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
                        " IN (SELECT " +
                        COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID +
                        " FROM " + TABLE_SCHEDULED_DIVES_USERS + " sd_u" +
                        " WHERE sd_u." + COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID +
                        " = " + String.valueOf(diverID) + ")";

        String sqlTimestamp = "";
        if (!timeStampStart.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " >= " + timeStampStart;
        }
        if (!timeStampEnd.trim().isEmpty()) {
            sqlTimestamp = sqlTimestamp +
                    " AND sd." + COLUMN_SCHEDULED_DIVES_TIMESTAMP + " <= " + timeStampEnd;
        }

        String sqlDiveSitesWhere =
                " AND sd." + COLUMN_SCHEDULED_DIVES_LOCAL_ID +
                        " IN (SELECT " +
                        COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
                        " FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " + "LEFT JOIN " + TABLE_DIVESITES + " d "
                        + "ON sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID + " = " + "d." + COLUMN_DIVESITE_LOCAL_ID +
                        " WHERE d." + COLUMN_DIVESITE_NAME + " LIKE '%" + title + "%'" + " AND " +
                        "d." + COLUMN_DIVESITE_COUNTRY + " LIKE '%" + country + "%'" + " AND " +
                        "d." + COLUMN_DIVESITE_PROVINCE + " LIKE '%" + state + "%'" + " AND " +
                        "d." + COLUMN_DIVESITE_CITY + " LIKE '%" + city + "%')";

        String sqlWhere = sqlUsersWhere;

        if (submitterID != -1) {
            sqlWhere = "(" + sqlWhere + " OR " +
                    COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID +
                    " = " + String.valueOf(submitterID) + ")";
        }

        if (isPublished && !isUnPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 1";
        } else if (isUnPublished && !isPublished) {
            sqlWhere = sqlWhere + " AND " +
                    COLUMN_SCHEDULED_DIVES_IS_PUBLISHED +
                    " = 0";
        } else if (!isPublished && !isUnPublished) {
            // Show nothing if neither checkbox was selected
            sqlWhere = sqlWhere + " AND 0";
        }

        sqlWhere = sqlWhere + sqlTimestamp + sqlDiveSitesWhere;

        String sql =
                "SELECT COUNT(" + COLUMN_SCHEDULED_DIVES_LOCAL_ID + ") as " + COLUMN_SCHEDULED_DIVES_COUNT +
                        " FROM " + TABLE_SCHEDULED_DIVES + " sd " +
                        " WHERE " + sqlWhere +
                        " ORDER BY " + COLUMN_SCHEDULED_DIVES_TIMESTAMP;

        Cursor cursor = getReadableDatabase().rawQuery(sql, null);

        cursor.moveToFirst();

        int count = 0;
        if (!cursor.isAfterLast()) {
            count = cursor.getInt(cursor.getColumnIndex(COLUMN_SCHEDULED_DIVES_COUNT));
        }
        cursor.close();

        return count;
    }

	public ScheduledDiveCursor queryScheduledDive(long id) {
		Cursor wrapped = getReadableDatabase().query(TABLE_SCHEDULED_DIVES, 
				null,
				COLUMN_SCHEDULED_DIVES_LOCAL_ID + " = ?", 
				new String[] { String.valueOf(id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		return new ScheduledDiveCursor(wrapped);
	}
	
	public long queryScheduledDiveLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_SCHEDULED_DIVES,
				new String[] {  }, // Local ID only
				COLUMN_SCHEDULED_DIVES_ONLINE_ID + " = ?", // Look for an online ID
				new String[] { String.valueOf(online_id) }, // with this value
				null, // group by
				null, // order by
				null, // having
				"1"); // limit 1 row

		long local_id = -1;
		cursor.moveToFirst();
		
		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor.getColumnIndex(COLUMN_SCHEDULED_DIVES_LOCAL_ID));
		cursor.close();

		return local_id;
	}
	
	public ScheduledDiveDiveSiteCursor queryScheduledDiveDiveSites(long scheduledDiveId) {
		String sql = 
			"SELECT *, (SELECT COUNT(" + COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID + ")" +
		               " FROM " + TABLE_SCHEDULED_DIVES_USERS + " sd_u " +
					   " WHERE sd_u." + COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID +
					     " = sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID + 
					   ") AS " + COLUMN_SCHEDULED_DIVES_DIVE_SITE_VOTE_COUNT +
			" FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " +
			" WHERE sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID +
			  " = " + String.valueOf(scheduledDiveId);
		
		Cursor wrapped = getReadableDatabase().rawQuery(sql, null);

		return new ScheduledDiveDiveSiteCursor(wrapped);
	}

	public ScheduledDiveDiveSiteCursor queryScheduledDiveDiveSite(long id) {
		String sql = 
			"SELECT *, (SELECT COUNT(" + COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID + ")" +
		               " FROM " + TABLE_SCHEDULED_DIVES_USERS + " sd_u " +
					   " WHERE sd_u." + COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID +
					     " = sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID + 
					   ") AS " + COLUMN_SCHEDULED_DIVES_DIVE_SITE_VOTE_COUNT +
			" FROM " + TABLE_SCHEDULED_DIVES_DIVE_SITES + " sd_d " +
			" WHERE sd_d." + COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID +
			  " = " + String.valueOf(id);
		
		Cursor wrapped = getReadableDatabase().rawQuery(sql, null);
		
		return new ScheduledDiveDiveSiteCursor(wrapped);
	}

	public long queryScheduledDiveDiveSiteLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_SCHEDULED_DIVES_DIVE_SITES,
				new String[] { COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID }, 
				COLUMN_SCHEDULED_DIVES_DIVE_SITE_ONLINE_ID + " = ?", 
				new String[] { String.valueOf(online_id) },
				null,
				null,
				null,
				"1");

		long local_id = -1;
		cursor.moveToFirst();

		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor.getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID));
		cursor.close();

		return local_id;
	}
	
	public ScheduledDiveUserCursor queryScheduledDiveUsers(long scheduledDiveId) {
		Cursor wrapped = getReadableDatabase().query(TABLE_SCHEDULED_DIVES_USERS,
				null, COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID + " = ?",
				new String[] { String.valueOf(scheduledDiveId) }, null, null,
				COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID + " asc", null);

		return new ScheduledDiveUserCursor(wrapped);
	}

	public ScheduledDiveUserCursor queryScheduledDiveUser(long id) {
		Cursor wrapped = getReadableDatabase().query(
				TABLE_SCHEDULED_DIVES_USERS,
				null, // All columns
				COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID + " = ?",
				new String[] { String.valueOf(id) }, null, null, null, "1");

		return new ScheduledDiveUserCursor(wrapped);
	}

	public long queryScheduledDiveUserLocalId(long online_id) {
		Cursor cursor = getReadableDatabase().query(TABLE_SCHEDULED_DIVES_USERS,
				new String[] { COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID }, 
				COLUMN_SCHEDULED_DIVES_USER_ONLINE_ID + " = ?", 
				new String[] { String.valueOf(online_id) },
				null,
				null,
				null,
				"1");

		long local_id = -1;
		cursor.moveToFirst();

		if (!cursor.isAfterLast())
			local_id = cursor.getLong(cursor.getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID));
		cursor.close();

		return local_id;
	}

	public void deleteDiveSitePicture(long diveSitePictureID) {
		getWritableDatabase().delete(TABLE_PICTURES,
				COLUMN_PICTURE_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveSitePictureID) });
	}

	public void deleteDiveLog(long diveLogID) {
		getWritableDatabase().delete(TABLE_DIVELOG,
				COLUMN_DIVELOG_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogID) });
		
		deleteDiveLogBuddyForLog(diveLogID);
		deleteDiveLogStopForLog(diveLogID);
	}

	public void deleteDiveLogBuddy(long diveLogBuddyID) {
		getWritableDatabase().delete(TABLE_DIVELOG_BUDDIES,
				COLUMN_DIVELOG_BUDDIES_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogBuddyID) });
	}

	public void deleteDiveLogStop(long diveLogStopID) {
		getWritableDatabase().delete(TABLE_DIVELOG_STOPS,
				COLUMN_DIVELOG_STOPS_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogStopID) });
	}
	
	public void deleteDiveLogBuddyForLog(long diveLogID) {
		getWritableDatabase().delete(TABLE_DIVELOG_BUDDIES,
				COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogID) });
	}

	public void deleteDiveLogStopForLog(long diveLogID) {
		getWritableDatabase().delete(TABLE_DIVELOG_STOPS,
				COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID + " = ?",
				new String[] { String.valueOf(diveLogID) });
	}
	
	public void deleteScheduledDive(long scheduledDiveID) {
		getWritableDatabase().delete(TABLE_SCHEDULED_DIVES,
				COLUMN_SCHEDULED_DIVES_LOCAL_ID + " = ?",
				new String[] { String.valueOf(scheduledDiveID) });
		
		deleteScheduledDiveDiveSitesForScheduledDive(scheduledDiveID);
		deleteScheduledDiveUsersForScheduledDive(scheduledDiveID);
	}
	
	public void deleteScheduledDiveDiveSites(long scheduledDiveDiveSiteID) {
		getWritableDatabase().delete(TABLE_SCHEDULED_DIVES_DIVE_SITES,
				COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID + " = ?",
				new String[] { String.valueOf(scheduledDiveDiveSiteID) });
	}
	
	public void deleteScheduledDiveUsers(long scheduledDiveUserID) {
		getWritableDatabase().delete(TABLE_SCHEDULED_DIVES_USERS,
				COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID + " = ?",
				new String[] { String.valueOf(scheduledDiveUserID) });
	}
	
	public void deleteScheduledDiveDiveSitesForScheduledDive(long scheduledDiveID) {
		getWritableDatabase().delete(TABLE_SCHEDULED_DIVES_DIVE_SITES,
				COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID + " = ?",
				new String[] { String.valueOf(scheduledDiveID) });
	}
	
	public void deleteScheduledDiveUsersForScheduledDive(long scheduledDiveID) {
		getWritableDatabase().delete(TABLE_SCHEDULED_DIVES_USERS,
				COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID + " = ?",
				new String[] { String.valueOf(scheduledDiveID) });
	}
	
	/**
	 * A convenience class to wrap a cursor that returns rows from the
	 * "DiveSite" table. The {@link getDiveSite()} method will give you a
	 * DiveSite instance representing the current row.
	 */
	public static class DiveSiteCursor extends CursorWrapper {

		public DiveSiteCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a DiveSite object configured for the current row, or null if
		 * the current row is invalid.
		 */

		public DiveSite getDiveSite() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			DiveSite diveSite = new DiveSite(-1, "");

			diveSite.setLocalId(getLong(getColumnIndex(COLUMN_DIVESITE_LOCAL_ID)));
			diveSite.setOnlineId(getLong(getColumnIndex(COLUMN_DIVESITE_ONLINE_ID)));
			diveSite.setUserId(getLong(getColumnIndex(COLUMN_DIVESITE_USER_ID)));
			diveSite.setUsername(getString(getColumnIndex(COLUMN_DIVESITE_USERNAME)));
			diveSite.setName(getString(getColumnIndex(COLUMN_DIVESITE_NAME)));
			diveSite.setCity(getString(getColumnIndex(COLUMN_DIVESITE_CITY)));
			diveSite.setProvince(getString(getColumnIndex(COLUMN_DIVESITE_PROVINCE)));
			diveSite.setCountry(getString(getColumnIndex(COLUMN_DIVESITE_COUNTRY)));

			String difficulty = getString(getColumnIndex(COLUMN_DIVESITE_DIFFICULTY));
			if (difficulty.equals(DiveSite.DiveSiteDifficulty.NOVICE.getName()))
				diveSite.setDifficulty(DiveSite.DiveSiteDifficulty.NOVICE);
			else if (difficulty
					.equals(DiveSite.DiveSiteDifficulty.NOVICE_INTERMEDIATE
							.getName()))
				diveSite.setDifficulty(DiveSite.DiveSiteDifficulty.NOVICE_INTERMEDIATE);
			else if (difficulty.equals(DiveSite.DiveSiteDifficulty.INTERMEDIATE
					.getName()))
				diveSite.setDifficulty(DiveSite.DiveSiteDifficulty.INTERMEDIATE);
			else if (difficulty
					.equals(DiveSite.DiveSiteDifficulty.INTERMEDIATE_EXPERIENCED
							.getName()))
				diveSite.setDifficulty(DiveSite.DiveSiteDifficulty.INTERMEDIATE_EXPERIENCED);
			else if (difficulty.equals(DiveSite.DiveSiteDifficulty.EXPERIENCED
					.getName()))
				diveSite.setDifficulty(DiveSite.DiveSiteDifficulty.EXPERIENCED);
			else if (difficulty.equals(DiveSite.DiveSiteDifficulty.UNKNOWN
					.getName()))
				diveSite.setDifficulty(DiveSite.DiveSiteDifficulty.UNKNOWN);

			diveSite.setSalty(getString(getColumnIndex(COLUMN_DIVESITE_ISSALT))
					.equals("1"));
			diveSite.setShoreDive(getString(
					getColumnIndex(COLUMN_DIVESITE_ISSHORE)).equals("1"));
			diveSite.setBoatDive(getString(
					getColumnIndex(COLUMN_DIVESITE_ISBOAT)).equals("1"));
			diveSite.setWreck(getString(getColumnIndex(COLUMN_DIVESITE_ISWRECK))
					.equals("1"));
			diveSite.setHistory(getString(getColumnIndex(COLUMN_DIVESITE_HISTORY)));
			diveSite.setDescription(getString(getColumnIndex(COLUMN_DIVESITE_DESCRIPTION)));
			diveSite.setDirections(getString(getColumnIndex(COLUMN_DIVESITE_DIRECTIONS)));
			diveSite.setSource(getString(getColumnIndex(COLUMN_DIVESITE_SOURCE)));
			diveSite.setNotes(getString(getColumnIndex(COLUMN_DIVESITE_NOTES)));
			diveSite.setLatitude(getDouble(getColumnIndex(COLUMN_DIVESITE_LATITUDE)));
			diveSite.setLongitude(getDouble(getColumnIndex(COLUMN_DIVESITE_LONGITUDE)));
			diveSite.setAltitude(getDouble(getColumnIndex(COLUMN_DIVESITE_ALTITUDE)));
			diveSite.setTotalRating(getInt(getColumnIndex(COLUMN_DIVESITE_TOTALRATE)));
			diveSite.setRatingCount(getInt(getColumnIndex(COLUMN_DIVESITE_RATE_COUNT)));
			diveSite.setDateAdded(new Date(
					getLong(getColumnIndex(COLUMN_DIVESITE_DATE_ADDED))));
			diveSite.setPublished(getString(
					getColumnIndex(COLUMN_DIVESITE_IS_PUBLISHED)).equals("1"));
			diveSite.setArchived(getString(
					getColumnIndex(COLUMN_DIVESITE_IS_ARCHIVED)).equals("1"));
			diveSite.setLastModifiedOnline(new Date(
					getLong(getColumnIndex(COLUMN_DIVESITE_LAST_MODIFIED_ONLINE))));
			diveSite.setRequiresRefresh(getString(
					getColumnIndex(COLUMN_DIVESITE_REQUIRES_REFRESH)).equals("1"));
			
			return diveSite;
		}
	}

	/**
	 * A convenience class to wrap a cursor that returns rows from the "Diver"
	 * table. The {@link getDiver()} method will give you a Diver instance
	 * representing the current row.
	 */

	public static class DiverCursor extends CursorWrapper {

		public DiverCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a Diver object configured for the current row, or null if the
		 * current row is invalid.
		 */

		public Diver getDiver() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			Diver diver = new Diver();

			diver.setLocalId(getLong(getColumnIndex(COLUMN_DIVER_LOCAL_ID)));
			diver.setOnlineId(getLong(getColumnIndex(COLUMN_DIVER_ONLINE_ID)));
			diver.setFirstName(getString(getColumnIndex(COLUMN_DIVER_FIRST_NAME)));
			diver.setLastName(getString(getColumnIndex(COLUMN_DIVER_LAST_NAME)));
			diver.setEmail(getString(getColumnIndex(COLUMN_DIVER_EMAIL)));
			diver.setCity(getString(getColumnIndex(COLUMN_DIVER_CITY)));
			diver.setProvince(getString(getColumnIndex(COLUMN_DIVER_PROVINCE)));
			diver.setUsername(getString(getColumnIndex(COLUMN_DIVER_USERNAME)));
			diver.setBio(getString(getColumnIndex(COLUMN_DIVER_BIO)));
			diver.setPictureURL(getString(getColumnIndex(COLUMN_DIVER_PICTURE_URL)));
			diver.setMod(getString(getColumnIndex(COLUMN_DIVER_IS_MOD)).equals(
					"1"));
			diver.setCreated(new Date(
					getLong(getColumnIndex(COLUMN_DIVER_CREATED))));
			diver.setLastModified(new Date(
					getLong(getColumnIndex(COLUMN_DIVER_LAST_MODIFIED))));
			diver.setLogCount(getInt(getColumnIndex(COLUMN_DIVER_LOG_COUNT)));
			diver.setDiveSiteSubmittedCount(getInt(getColumnIndex(COLUMN_DIVER_DIVE_SITES_SUBMITTED_COUNT)));

			return diver;
		}
	}

	/**
	 * A convenience class to wrap a cursor that returns rows from the
	 * "Diver Certification" table. The {@link getDiverCertification()} method
	 * will give you a Diver Certification instance representing the current
	 * row.
	 */

	public static class DiverCertificationCursor extends CursorWrapper {

		public DiverCertificationCursor(Cursor c) {
			super(c);
		}

		public DiverCertification getDiverCertification() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			DiverCertification cert = new DiverCertification();

			cert.setLocalId(getLong(getColumnIndex(COLUMN_DIVER_CERT_LOCAL_ID)));
			cert.setOnlineId(getLong(getColumnIndex(COLUMN_DIVER_CERT_ONLINE_ID)));
			cert.setCertifUserId(getLong(getColumnIndex(COLUMN_DIVER_CERT_USER_ID)));
			cert.setCertifTitle(getString(getColumnIndex(COLUMN_DIVER_CERT_NAME)));
			cert.setCertifDate(getString(getColumnIndex(COLUMN_DIVER_CERT_DATE)));
			cert.setCertifNumber(getString(getColumnIndex(COLUMN_DIVER_CERT_NO)));
			cert.setCertifLocation(getString(getColumnIndex(COLUMN_DIVER_CERT_LOCATION)));
			cert.setPrimary(getString(
					getColumnIndex(COLUMN_DIVER_CERT_IS_PRIMARY)).equals("1"));

			return cert;
		}
	}

	/**
	 * A convenience class to wrap a cursor that returns rows from the "DiveLog"
	 * table. The {@link getDiveLog()} method will give you a DiveLog instance
	 * representing the current row.
	 */
	public static class DiveLogCursor extends CursorWrapper {

		public DiveLogCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a getDiveLog object configured for the current row, or null
		 * if the current row is invalid.
		 */

		public DiveLog getDiveLog() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			DiveLog diveLog = new DiveLog();

			diveLog.setLocalId(getLong(getColumnIndex(COLUMN_DIVELOG_LOCAL_ID)));
			diveLog.setOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_ONLINE_ID)));
			diveLog.setDiveSiteLocalId(getLong(getColumnIndex(COLUMN_DIVELOG_SITE_LOCAL_ID)));
			diveLog.setDiveSiteOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_SITE_ONLINE_ID)));
			diveLog.setUserId(getLong(getColumnIndex(COLUMN_DIVELOG_USER_ID)));
			diveLog.setUsername(getString(getColumnIndex(COLUMN_DIVELOG_USERNAME)));
			diveLog.setTimestamp(new Date(getLong(getColumnIndex(COLUMN_DIVELOG_TIMESTAMP))));
			diveLog.setAirType(getString(getColumnIndex(COLUMN_DIVELOG_AIR_TYPE)));
			diveLog.setStartPressure(getString(getColumnIndex(COLUMN_DIVELOG_START_PRESSURE)).charAt(0));
			diveLog.setEndPressure(getString(getColumnIndex(COLUMN_DIVELOG_END_PRESSURE)).charAt(0));
            diveLog.setStartAir(new ValueParameter(
                    getDouble(getColumnIndex(COLUMN_DIVELOG_START_AIR_VALUE)),
                    getString(getColumnIndex(COLUMN_DIVELOG_START_AIR_UNITS))));
            diveLog.setEndAir(new ValueParameter(
                    getDouble(getColumnIndex(COLUMN_DIVELOG_END_AIR_VALUE)),
                    getString(getColumnIndex(COLUMN_DIVELOG_END_AIR_UNITS))));
			diveLog.setDiveTime(getInt(getColumnIndex(COLUMN_DIVELOG_DIVE_TIME)));
			diveLog.setMaxDepth(new ValueParameter(
					getDouble(getColumnIndex(COLUMN_DIVELOG_MAX_DEPTH_VALUE)),
					getString(getColumnIndex(COLUMN_DIVELOG_MAX_DEPTH_UNITS))));
			diveLog.setAverageDepth(new ValueParameter(
					getDouble(getColumnIndex(COLUMN_DIVELOG_AVERAGE_DEPTH_VALUE)),
					getString(getColumnIndex(COLUMN_DIVELOG_AVERAGE_DEPTH_UNITS))));
			diveLog.setSurfaceTemperature(new ValueParameter(
					getDouble(getColumnIndex(COLUMN_DIVELOG_SURFACE_TEMPERATURE_VALUE)),
					getString(getColumnIndex(COLUMN_DIVELOG_SURFACE_TEMPERATURE_UNITS))));
			diveLog.setWaterTemperature(new ValueParameter(
					getDouble(getColumnIndex(COLUMN_DIVELOG_WATER_TEMPERATURE_VALUE)),
					getString(getColumnIndex(COLUMN_DIVELOG_WATER_TEMPERATURE_UNITS))));
			diveLog.setVisibility(new ValueParameter(
					getDouble(getColumnIndex(COLUMN_DIVELOG_VISIBILITY_VALUE)),
					getString(getColumnIndex(COLUMN_DIVELOG_VISIBILITY_UNITS))));
			diveLog.setWeightsRequired(new ValueParameter(
					getDouble(getColumnIndex(COLUMN_DIVELOG_WEIGHTS_REQUIRED_VALUE)),
					getString(getColumnIndex(COLUMN_DIVELOG_WEIGHTS_REQUIRED_UNITS))));
			diveLog.setSurfaceTime(getInt(getColumnIndex(COLUMN_DIVELOG_SURFACE_TIME)));
			diveLog.setRating(getInt(getColumnIndex(COLUMN_DIVELOG_RATING)));
			diveLog.setComments(getString(getColumnIndex(COLUMN_DIVELOG_COMMENTS)));
			diveLog.setIsCourse(getString(
					getColumnIndex(COLUMN_DIVELOG_IS_COURSE)).equals("1"));
			diveLog.setIsPhotoVideo(getString(
					getColumnIndex(COLUMN_DIVELOG_IS_PHOTO_VIDEO)).equals("1"));
			diveLog.setIsIce(getString(getColumnIndex(COLUMN_DIVELOG_IS_ICE))
					.equals("1"));
			diveLog.setIsDeep(getString(getColumnIndex(COLUMN_DIVELOG_IS_DEEP))
					.equals("1"));
			diveLog.setIsInstructing(getString(
					getColumnIndex(COLUMN_DIVELOG_IS_INSTRUCTING)).equals("1"));
			diveLog.setIsNight(getString(
					getColumnIndex(COLUMN_DIVELOG_IS_NIGHT)).equals("1"));
			diveLog.setPublished(getString(
					getColumnIndex(COLUMN_DIVELOG_IS_PUBLISHED)).equals("1"));
			diveLog.setLastModifiedOnline(new Date(
					getLong(getColumnIndex(COLUMN_DIVELOG_LAST_MODIFIED_ONLINE))));
			diveLog.setRequiresRefresh(getString(
					getColumnIndex(COLUMN_DIVELOG_REQUIRES_REFRESH)).equals("1"));
			diveLog.setDiveLogCountWhenRetreived(getCount());

			return diveLog;
		}
	}

	/**
	 * A convenience class to wrap a cursor that returns rows from the
	 * "DiveLogBuddies" table. The {@link getDiveLogBuddy()} method will give
	 * you a DiveLogBuddy instance representing the current row.
	 */
	public static class DiveLogBuddyCursor extends CursorWrapper {

		public DiveLogBuddyCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a getDiveLogBuddy object configured for the current row, or
		 * null if the current row is invalid.
		 */

		public DiveLogBuddy getDiveLogBuddy() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			DiveLogBuddy diveLogBuddy = new DiveLogBuddy();

			diveLogBuddy
					.setLocalId(getLong(getColumnIndex(COLUMN_DIVELOG_BUDDIES_LOCAL_ID)));
			diveLogBuddy
					.setOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_BUDDIES_ONLINE_ID)));
			diveLogBuddy
					.setDiveLogLocalId(getLong(getColumnIndex(COLUMN_DIVELOG_BUDDIES_LOG_LOCAL_ID)));
			diveLogBuddy
					.setDiveLogOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_BUDDIES_LOG_ONLINE_ID)));
			diveLogBuddy
					.setDiverOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_BUDDIES_USER_ID)));
			diveLogBuddy
					.setDiverUsername(getString(getColumnIndex(COLUMN_DIVELOG_BUDDIES_USERNAME)));

			return diveLogBuddy;
		}
	}

	/**
	 * A convenience class to wrap a cursor that returns rows from the
	 * "DiveLogStops" table. The {@link getDiveLogStop()} method will give you a
	 * DiveLogStop instance representing the current row.
	 */
	public static class DiveLogStopCursor extends CursorWrapper {

		public DiveLogStopCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a getDiveLogStop object configured for the current row, or
		 * null if the current row is invalid.
		 */

		public DiveLogStop getDiveLogStop() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			DiveLogStop diveLogStop = new DiveLogStop();

			diveLogStop
					.setLocalId(getLong(getColumnIndex(COLUMN_DIVELOG_STOPS_LOCAL_ID)));
			diveLogStop
					.setOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_STOPS_ONLINE_ID)));
			diveLogStop
					.setDiveLogLocalId(getLong(getColumnIndex(COLUMN_DIVELOG_STOPS_LOG_LOCAL_ID)));
			diveLogStop
					.setDiveLogOnlineId(getLong(getColumnIndex(COLUMN_DIVELOG_STOPS_LOG_ONLINE_ID)));
			diveLogStop
					.setTime(getInt(getColumnIndex(COLUMN_DIVELOG_STOPS_TIME)));
			diveLogStop
					.setDepth(new ValueParameter(
							getDouble(getColumnIndex(COLUMN_DIVELOG_STOP_DEPTH_VALUE)),
							getString(getColumnIndex(COLUMN_DIVELOG_STOP_DEPTH_UNITS))));

			return diveLogStop;
		}
	}

	public static class PictureCursor extends CursorWrapper {

		public PictureCursor(Cursor c) {
			super(c);
		}

		public DiveSitePicture getDiveSitePicture() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			DiveSitePicture diveSitePicture = new DiveSitePicture();

			// Populate the remaining properties
			diveSitePicture
					.setLocalId(getInt(getColumnIndex(COLUMN_PICTURE_LOCAL_ID)));
			diveSitePicture
					.setOnlineID(getInt(getColumnIndex(COLUMN_PICTURE_ONLINE_ID)));
			diveSitePicture
					.setDiveSiteLocalID(getInt(getColumnIndex(COLUMN_PICTURE_SITE_LOCAL_ID)));
			diveSitePicture
					.setDiveSiteOnlineID(getInt(getColumnIndex(COLUMN_PICTURE_SITE_ONLINE_ID)));
			diveSitePicture
					.setBitmapFilePath(getString(getColumnIndex(COLUMN_PICTURE_IMAGE_PATH)));
			diveSitePicture
					.setBitmapURL(getString(getColumnIndex(COLUMN_PICTURE_URL)));
			diveSitePicture
					.setPictureDescription(getString(getColumnIndex(COLUMN_PICTURE_DESCRIPTION)));

			return diveSitePicture;
		}

	}
	
	/**
	 * A convenience class to wrap a cursor that returns rows from the "ScheduledDive"
	 * table. The {@link getScheduledDive()} method will give you a ScheduledDive instance
	 * representing the current row.
	 */
	public static class ScheduledDiveCursor extends CursorWrapper {

		public ScheduledDiveCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a getScheduledDive object configured for the current row, or null
		 * if the current row is invalid.
		 */

		public ScheduledDive getScheduledDive() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			ScheduledDive scheduledDive = new ScheduledDive();

			scheduledDive.setLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_LOCAL_ID)));
			scheduledDive.setOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_ONLINE_ID)));
			scheduledDive.setTitle(getString(getColumnIndex(COLUMN_SCHEDULED_DIVES_TITLE)));
			scheduledDive.setSubmitterId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_SUBMITTER_ONLINE_ID)));
			scheduledDive.setTimestamp(new Date((getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_TIMESTAMP)))));
			scheduledDive.setComment(getString(getColumnIndex(COLUMN_SCHEDULED_DIVES_COMMENT)));
			scheduledDive.setPublished(getString(getColumnIndex(COLUMN_SCHEDULED_DIVES_IS_PUBLISHED)).equals("1"));
			scheduledDive.setLastModifiedOnline(new Date((getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_LAST_MODIFIED_ONLINE)))));
			scheduledDive.setRequiresRefresh(getString(getColumnIndex(COLUMN_SCHEDULED_DIVES_REQUIRES_REFRESH)).equals("1"));
			
			return scheduledDive;
		}
	}
	
	/**
	 * A convenience class to wrap a cursor that returns rows from the "ScheduledDiveDiveSite"
	 * table. The {@link getScheduledDiveDiveSite()} method will give you a ScheduledDiveDiveSite instance
	 * representing the current row.
	 */
	public static class ScheduledDiveDiveSiteCursor extends CursorWrapper {

		public ScheduledDiveDiveSiteCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a getScheduledDiveDiveSite object configured for the current row, or null
		 * if the current row is invalid.
		 */

		public ScheduledDiveDiveSite getScheduledDiveDiveSite() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			ScheduledDiveDiveSite scheduledDiveDiveSite = new ScheduledDiveDiveSite();

			scheduledDiveDiveSite.setLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_LOCAL_ID)));
			scheduledDiveDiveSite.setOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_ONLINE_ID)));
			scheduledDiveDiveSite.setScheduledDiveLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_LOCAL_ID)));
			scheduledDiveDiveSite.setScheduledDiveOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SCHEDULED_ONLINE_ID)));
			scheduledDiveDiveSite.setDiveSiteLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_LOCAL_ID)));
			scheduledDiveDiveSite.setDiveSiteOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_SITE_ONLINE_ID)));
			scheduledDiveDiveSite.setVoteCount(getInt(getColumnIndex(COLUMN_SCHEDULED_DIVES_DIVE_SITE_VOTE_COUNT)));

			return scheduledDiveDiveSite;
		}
	}
	
	/**
	 * A convenience class to wrap a cursor that returns rows from the "ScheduledDiveUser"
	 * table. The {@link getScheduledDiveUser()} method will give you a ScheduledDiveUser instance
	 * representing the current row.
	 */
	public static class ScheduledDiveUserCursor extends CursorWrapper {

		public ScheduledDiveUserCursor(Cursor c) {
			super(c);
		}

		/**
		 * Returns a getScheduledDiveUser object configured for the current row, or null
		 * if the current row is invalid.
		 */

		public ScheduledDiveUser getScheduledDiveUser() {
			if (isBeforeFirst() || isAfterLast())
				return null;

			ScheduledDiveUser scheduledDiveUser = new ScheduledDiveUser();

			scheduledDiveUser.setLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_LOCAL_ID)));
			scheduledDiveUser.setOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_ONLINE_ID)));
			scheduledDiveUser.setScheduledDiveLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_LOCAL_ID)));
			scheduledDiveUser.setScheduledDiveOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_SCHEDULED_ONLINE_ID)));
			scheduledDiveUser.setVotedScheduledDiveDiveSiteLocalId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_LOCAL_ID)));
			scheduledDiveUser.setVotedScheduledDiveDiveSiteOnlineId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_VOTED_SCHEDULED_SITE_ONLINE_ID)));
			scheduledDiveUser.setUserId(getLong(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_USER_ONLINE_ID)));
			
			String attendState = getString(getColumnIndex(COLUMN_SCHEDULED_DIVES_USER_ATTEND_STATE));
			if (attendState.equals(ScheduledDiveUser.AttendState.ATTENDING.getName())) {
				scheduledDiveUser.setAttendState(ScheduledDiveUser.AttendState.ATTENDING);
			} else if (attendState.equals(ScheduledDiveUser.AttendState.MAYBE_ATTENDING.getName())) {
				scheduledDiveUser.setAttendState(ScheduledDiveUser.AttendState.MAYBE_ATTENDING);
			} else if (attendState.equals(ScheduledDiveUser.AttendState.NOT_ATTENDING.getName())) {
				scheduledDiveUser.setAttendState(ScheduledDiveUser.AttendState.NOT_ATTENDING);
			}
			
			return scheduledDiveUser;
		}
	}
}
