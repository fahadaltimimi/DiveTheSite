package com.fahadaltimimi.divethesite.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogBuddyCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogStopCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveSiteCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiverCertificationCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiverCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.PictureCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveDiveSiteCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveUserCursor;
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

public class DiveSiteManager {

	public static final String TAG = "DiveSiteManager";

	public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	/*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    public static final String MIN_LATITUDE = "-90";
    public static final String MAX_LATITUDE = "90";
    public static final String MIN_LONGITUDE = "-180";
    public static final String MAX_LONGITUDE = "180";

    public static final double LOCATION_COMPARE_EPSILON = 0.001;
	
	public static final String ACTION_LOCATION = "com.fahadaltimimi.divethesite.ACTION_LOCATION";
	
	public static final String EXTRA_DIVE_SITE = "com.fahadaltimimi.divethesite.dive_site";

	public static final String EXTRA_DIVER_ID = "com.fahadaltimimi.divethesite.diver_id";
	
	public static final String EXTRA_DIVER_ONLINE_ID = "com.fahadaltimimi.divethesite.diver_online_id";
	public static final String EXTRA_DIVER_USERNAME = "com.fahadaltimimi.divethesite.diver_username";
	
	public static final String EXTRA_DIVESITELIST_TAB_INDEX = "com.fahadaltimimi.divethesite.divesitelist_tab_index";
	public static final String EXTRA_DIVELOGLIST_TAB_INDEX = "com.fahadaltimimi.divethesite.diveloglist_tab_index";
	public static final String EXTRA_SCHEDULEDDIVELIST_TAB_INDEX = "com.fahadaltimimi.divethesite.scheduleddivelist_tab_index";

	public static final String PREFS_FILE = "DiveTheSite";
	
	private static final String PREF_LAST_LOCATION_LATITUDE = "DiveTheSite.lastLocationLatitude";
	private static final String PREF_LAST_LOCATION_LONGITUDE = "DiveTheSite.lastLocationLongitude";

    private static final String PREF_LAST_LOCATION_LATITUDE_LONG = "DiveTheSite.lastLocationLatitudeLong";
    private static final String PREF_LAST_LOCATION_LONGITUDE_LONG = "DiveTheSite.lastLocationLongitudeLong";

	public static final String PREF_LAST_DIVESITES_REFRESH_DATE = "DiveTheSite.DiveSite.lastRefreshDate";
	public static final String PREF_LAST_DIVESITES_DIVER_REFRESH_DATE = "DiveTheSite.DiveSite.lastRefreshDate.Diver.";
	public static final String PREF_LAST_DIVER_REFRESH_DATE = "DiveTheSite.Diver.lastRefreshDate";
	public static final String PREF_CURRENT_DIVESITE_VIEW_MODE = "DiveTheSite.currentViewMode";
	public static final String PREF_CURRENT_USER_ID = "DiveTheSite.currentUserId";
	public static final String PREF_CURRENT_USERNAME = "DiveTheSite.currentUsername";
    public static final String PREF_CURRENT_PROFILEIMAGE = "DiveTheSite.currentProfileImage";
	public static final String PREF_LAST_DIVELOGS_REFRESH_DATE = "DiveTheSite.lastLogsRefreshDate";
	public static final String PREF_LAST_DIVELOGS_DIVER_REFRESH_DATE = "DiveTheSite.lastLogsRefreshDate.Diver.";
	public static final String PREF_LAST_DIVELOGS_DIVESITE_REFRESH_DATE = "DiveTheSite.lastLogsRefreshDate.DiveSite.";
	public static final String PREF_LAST_DIVERS_REFRESH_DATE = "DiveTheSite.lastDiversRefreshDate";

	public static final String PREF_FILTER_DIVESITE_TITLE = "DiveTheSite.DiveSite.filterTitle";
	public static final String PREF_FILTER_DIVESITE_COUNTRY = "DiveTheSite.DiveSite.filterCountry";
	public static final String PREF_FILTER_DIVESITE_STATE = "DiveTheSite.DiveSite.filterState";
	public static final String PREF_FILTER_DIVESITE_CITY = "DiveTheSite.DiveSite.filterCity";
	public static final String PREF_FILTER_DIVESITE_SHOW_PUBLISHED = "DiveTheSite.DiveSite.showPublished";
	public static final String PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED = "DiveTheSite.DiveSite.showUnpublished";
	
	public static final String PREF_FILTER_SCHEDULEDDIVE_TITLE = "DiveTheSite.ScheduledDive.filterTitle";
	public static final String PREF_FILTER_SCHEDULEDDIVE_COUNTRY = "DiveTheSite.ScheduledDive.filterCountry";
	public static final String PREF_FILTER_SCHEDULEDDIVE_STATE = "DiveTheSite.ScheduledDive.filterState";
	public static final String PREF_FILTER_SCHEDULEDDIVE_CITY = "DiveTheSite.ScheduledDive.filterCity";
	public static final String PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS = "DiveTheSite.ScheduledDive.previousDays";
	public static final String PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS = "DiveTheSite.ScheduledDive.nextDays";
	public static final String PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED = "DiveTheSite.ScheduledDive.showPublished";
	public static final String PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED = "DiveTheSite.ScheduledDive.showUnpublished";

	public static final String PREF_FILTER_DIVELOG_DIVESITE_TITLE = "DiveTheSite.DiveSite.filterTitle";
	public static final String PREF_FILTER_DIVER_NAME = "DiveTheSite.Diver.filterName";
	public static final String PREF_FILTER_DIVER_COUNTRY = "DiveTheSite.Diver.filterCountry";
	public static final String PREF_FILTER_DIVER_STATE = "DiveTheSite.Diver.filterState";
	public static final String PREF_FILTER_DIVER_CITY = "DiveTheSite.Diver.filterCity";
		
	public static final String PREF_SETTING_LAST_NEAR_ITEM_CHECK_LATITUDE = "DiveTheSite.Setting.lastNearItemCheckLatitude";
	public static final String PREF_SETTING_LAST_NEAR_ITEM_CHECK_LONGITUDE = "DiveTheSite.Setting.lastNearItemCheckLongitude";
	public static final String PREF_SETTING_LAST_NEAR_DIVE_SITE_CHECK_TIMESTAMP = "DiveTheSite.Setting.lastNearDiveSiteCheckTimestamp";
	public static final String PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP = "DiveTheSite.Setting.lastNearScheduledDiveCheckTimestamp";

    public static final String PREF_DIVELOG_STARTAIRUNITS = "DiveTheSite.DiveLog.StartAirUnits";
    public static final String PREF_DIVELOG_ENDAIRUNITS = "DiveTheSite.DiveLog.EndAirUnits";
    public static final String PREF_DIVELOG_MAXDEPTHUNITS = "DiveTheSite.DiveLog.MaxDepthUnits";
    public static final String PREF_DIVELOG_AVGDEPTHUNITS = "DiveTheSite.DiveLog.AvgDepthUnits";
    public static final String PREF_DIVELOG_AIRTEMPUNITS = "DiveTheSite.DiveLog.AitTempUnits";
    public static final String PREF_DIVELOG_WATERTEMPUNITS = "DiveTheSite.DiveLog.WaterTempUnits";
    public static final String PREF_DIVELOG_VISIBILITYUNITS = "DiveTheSite.DiveLog.VisibilityUnits";
    public static final String PREF_DIVELOG_WEIGHTUNITS = "DiveTheSite.DiveLog.WeightUnits";

    private static final String DIVER_PROFILE_IMAGE = "DiverProfileImage";

	public static DiveSiteManager sDiveSiteManager;
	private Context mAppContext;
	private SharedPreferences mPrefs;
	private DiveSiteDatabaseHelper mHelper;

	// The private constructor forces users to use RunManager.get(Context)
	private DiveSiteManager(Context appContext) {
		mAppContext = appContext;

		mHelper = new DiveSiteDatabaseHelper(mAppContext);

		mPrefs = appContext.getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);
	}

	public static DiveSiteManager get(Context c) {
		if (sDiveSiteManager == null) {
			// Use the application context to avoid leaking activities
			sDiveSiteManager = new DiveSiteManager(c.getApplicationContext());
		}
		return sDiveSiteManager;
	}

	public DiveSite insertDiveSite() {
		DiveSite diveSite = new DiveSite(getLoggedInDiverId(), getLoggedInDiverUsername());
		mHelper.insertDiveSite(diveSite);
		return diveSite;
	}

	public void insertDiveSite(DiveSite diveSite) {
		mHelper.insertDiveSite(diveSite);
	}

	public void saveDiveSite(DiveSite diveSite) {
		if (diveSite.getLocalId() == -1) {
			mHelper.insertDiveSite(diveSite);
		} else {
			mHelper.saveDiveSite(diveSite);
		}
	}
	
	public void setDiveSiteRequiresRefresh(DiveSite diveSite) {
		mHelper.setDiveSiteRequiresRefresh(diveSite);
	}

	public void setDiveSiteArchive(long diveSiteID, boolean archive) {
		mHelper.setDiveSiteArchive(diveSiteID, archive);
	}

	public void insertDiveSitePicture(DiveSitePicture diveSitePicture) {
		mHelper.insertDiveSitePicture(diveSitePicture);
	}

	public void saveDiveSitePicture(DiveSitePicture diveSitePicture) {
		if (diveSitePicture.getLocalId() == -1) {
			mHelper.insertDiveSitePicture(diveSitePicture);
		} else {
			mHelper.saveDiveSitePicture(diveSitePicture);
		}
	}

	public long getDiveSitePictureLocalId(long online_id) {
		return mHelper.queryDiveSitePictureLocalId(online_id);
	}

	public DiveSiteCursor queryDiveSites() {
		return mHelper.queryDiveSites();
	}

	public DiveSiteCursor queryVisibleDiveSites(String filterSelection,
			ArrayList<String> filterSelectionArgs, String minLatitude,
			String maxLatitude, String minLongitude, String maxLongitude,
			long diverSubmitterID) {

		return mHelper.queryVisibleDiveSites(filterSelection,
				filterSelectionArgs, minLatitude, maxLatitude, minLongitude,
				maxLongitude, diverSubmitterID);
	}

    public int queryVisibleDiveSitesCount(String filterSelection,
            ArrayList<String> filterSelectionArgs, String minLatitude,
            String maxLatitude, String minLongitude, String maxLongitude,
            long diverSubmitterID) {

        return mHelper.queryVisibleDiveSitesCount(filterSelection,
                filterSelectionArgs, minLatitude, maxLatitude, minLongitude,
                maxLongitude, diverSubmitterID);
    }

	public DiveSiteCursor queryArchivedDiveSites(String filterSelection,
			ArrayList<String> filterSelectionArgs, String minLatitude,
			String maxLatitude, String minLongitude, String maxLongitude,
			long diverSubmitterID) {

		return mHelper.queryArchivedDiveSites(filterSelection,
				filterSelectionArgs, minLatitude, maxLatitude, minLongitude,
				maxLongitude, diverSubmitterID);
	}

    public int queryArchivedDiveSitesCount(String filterSelection,
            ArrayList<String> filterSelectionArgs, String minLatitude,
            String maxLatitude, String minLongitude, String maxLongitude,
            long diverSubmitterID) {

        return mHelper.queryArchivedDiveSitesCount(filterSelection,
                filterSelectionArgs, minLatitude, maxLatitude, minLongitude,
                maxLongitude, diverSubmitterID);
    }

	public DiveSiteCursor queryPublishedDiveSites(boolean isPublished,
			String filterSelection, ArrayList<String> filterSelectionArgs,
			String minLatitude, String maxLatitude, String minLongitude,
			String maxLongitude, long diverSubmitterID) {

		return mHelper.queryPublishedDiveSites(isPublished, filterSelection,
				filterSelectionArgs, minLatitude, maxLatitude, minLongitude,
				maxLongitude, diverSubmitterID);
	}

    public int queryPublishedDiveSitesCount(boolean isPublished,
            String filterSelection, ArrayList<String> filterSelectionArgs,
            String minLatitude, String maxLatitude, String minLongitude,
            String maxLongitude, long diverSubmitterID) {

        return mHelper.queryPublishedDiveSitesCount(isPublished, filterSelection,
                filterSelectionArgs, minLatitude, maxLatitude, minLongitude,
                maxLongitude, diverSubmitterID);
    }

	public void deleteDiveSite(long diveSiteID) {
		mHelper.deleteDiveSite(diveSiteID);
	}

	public PictureCursor queryDiveSitePictures(long diveSiteID) {
		return mHelper.queryDiveSitePictures(diveSiteID);
	}

	public DiveSite getDiveSite(long id) {
		DiveSite diveSite = null;
		DiveSiteCursor cursor = mHelper.queryDiveSite(id);
		cursor.moveToFirst();

		if (!cursor.isAfterLast())
			diveSite = cursor.getDiveSite();
		cursor.close();
		return diveSite;
	}

	public ArrayList<DiveSite> getDiveSitesLikeName(String diveSiteName) {
		DiveSiteCursor cursor = mHelper.queryDiveSites(diveSiteName);

		ArrayList<DiveSite> diveSites = new ArrayList<DiveSite>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			diveSites.add(cursor.getDiveSite());
			cursor.moveToNext();
		}

		return diveSites;
	}

	public long getDiveSiteLocalId(long online_id) {
		return mHelper.queryDiveSiteLocalId(online_id);
	}

	public void insertDiverCertification(DiverCertification cert) {
		mHelper.insertDiverCertification(cert);
	}

	public void saveDiverCertification(DiverCertification cert) {
		if (cert.getLocalId() == -1) {
			mHelper.insertDiverCertification(cert);
		} else {
			mHelper.saveDiverCertification(cert);
		}
	}

	public DiverCertificationCursor queryDiverCertifications(long diverID) {
		return mHelper.queryDiverCertifications(diverID);
	}

	public long getDiverCertificationLocalId(long online_id) {
		return mHelper.queryDiverCertificationLocalId(online_id);
	}

	public Diver insertDiver() {
		Diver diver = new Diver();
		mHelper.insertDiver(diver);
		return diver;
	}

	public void insertDiver(Diver diver) {
		mHelper.insertDiver(diver);
	}

	public void saveDiver(Diver diver) {
		if (diver.getLocalId() == -1) {
			mHelper.insertDiver(diver);
		} else {
			mHelper.saveDiver(diver);
		}
	}

	public DiverCursor queryDivers() {
		return mHelper.queryDivers();
	}

	public DiverCursor queryFilteredDivers(String filterSelection,
			ArrayList<String> filterSelectionArgs) {

		return mHelper
				.queryFilteredDivers(filterSelection, filterSelectionArgs);
	}

	public Diver getDiver(long id) {
		Diver diver = null;
		DiverCursor cursor = mHelper.queryDiver(id);
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			diver = cursor.getDiver();
		cursor.close();
		return diver;
	}

	public long getDiverLocalId(long online_id) {
		return mHelper.queryDiverLocalId(online_id);
	}

	public DiveLog insertDiveLog() {
		DiveLog diveLog = new DiveLog();
		diveLog.setUserId(getLoggedInDiverId());
		diveLog.setUsername(getLoggedInDiverUsername());

		mHelper.insertDiveLog(diveLog);
		return diveLog;
	}

	public void insertDiveLog(DiveLog diveLog) {
		mHelper.insertDiveLog(diveLog);
	}

	public void saveDiveLog(DiveLog diveLog) {
		if (diveLog.getLocalId() == -1) {
			mHelper.insertDiveLog(diveLog);
		} else {
			mHelper.saveDiveLog(diveLog);
		}
	}
	
	public void setDiveLogRequiresRefresh(DiveLog diveLog) {
		mHelper.setDiveLogRequiresRefresh(diveLog);
	}
	
	public ScheduledDive insertScheduledDive() {
		ScheduledDive scheduledDive = new ScheduledDive();
		scheduledDive.setSubmitterId(getLoggedInDiverId());

		mHelper.insertScheduledDive(scheduledDive);
		return scheduledDive;
	}

	public void insertScheduledDive(ScheduledDive scheduledDive) {
		mHelper.insertScheduledDive(scheduledDive);
	}

	public void saveScheduledDive(ScheduledDive scheduledDive) {
		if (scheduledDive.getLocalId() == -1) {
			mHelper.insertScheduledDive(scheduledDive);
		} else {
			mHelper.saveScheduledDive(scheduledDive);
		}
	}
	
	public void setScheduledDiveRequiresRefresh(ScheduledDive scheduledDive) {
		mHelper.setScheduledDiveRequiresRefresh(scheduledDive);
	}

	public DiveLogCursor queryDiveLogs(long diverID, DiveSite diveSite, boolean unPublishedOnly) {
		return mHelper.queryDiveLogs(diverID, diveSite, unPublishedOnly);
	}

    public int queryDiveLogsCount(long diverID, DiveSite diveSite, boolean unPublishedOnly) {
        return mHelper.queryDiveLogsCount(diverID, diveSite, unPublishedOnly);
    }

    public int queryDiveLogsTotalMinutes(long diverID, DiveSite diveSite, boolean unPublishedOnly) {
        return mHelper.queryDiveLogsTotalMinutes(diverID, diveSite, unPublishedOnly);
    }

	public void deleteDiveSitePicture(long diveSitePictureID) {
		mHelper.deleteDiveSitePicture(diveSitePictureID);
	}

	public void deleteDiveLog(long diveLogID) {
		mHelper.deleteDiveLog(diveLogID);
	}
	
	public void deleteScheduledDive(long scheduledDiveID) {
		mHelper.deleteScheduledDive(scheduledDiveID);
	}
	
	public void deleteScheduledDiveDiveSites(long scheduledDiveID) {
		mHelper.deleteScheduledDiveDiveSites(scheduledDiveID);
	}
	
	public void deleteScheduledDiveUsers(long scheduledDiveID) {
		mHelper.deleteScheduledDiveUsers(scheduledDiveID);
	}

	public DiveLog getDiveLog(long id) {
		DiveLog diveLog = null;
		DiveLogCursor cursor = mHelper.queryDiveLog(id);

		cursor.moveToFirst();
		if (!cursor.isAfterLast())
			diveLog = cursor.getDiveLog();
		cursor.close();
		return diveLog;
	}

	public DiveLogBuddyCursor queryDiveLogBuddies(long diveLogId) {
		return mHelper.queryDiveLogBuddies(diveLogId);
	}

	public ArrayList<DiveLogBuddy> getDiveLogBuddies(long diveLogId) {
		ArrayList<DiveLogBuddy> diveLogBuddies = new ArrayList<DiveLogBuddy>();

		DiveLogBuddyCursor cursor = queryDiveLogBuddies(diveLogId);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			diveLogBuddies.add(cursor.getDiveLogBuddy());
			cursor.moveToNext();
		}

		return diveLogBuddies;
	}

	public DiveLogBuddy getDiveLogBuddy(long id) {
		DiveLogBuddy diveLogBuddy = null;
		DiveLogBuddyCursor cursor = mHelper.queryDiveLogBuddy(id);
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			diveLogBuddy = cursor.getDiveLogBuddy();
		cursor.close();
		return diveLogBuddy;
	}

	public long getDiveLogBuddyLocalId(long online_id) {
		return mHelper.queryDiveLogBuddyLocalId(online_id);
	}

	public void deleteDiveLogBuddy(long diveLogBuddyID) {
		mHelper.deleteDiveLogBuddy(diveLogBuddyID);
	}

	public DiveLogStopCursor queryDiveLogStops(long diveLogId) {
		return mHelper.queryDiveLogStops(diveLogId);
	}

	public ArrayList<DiveLogStop> getDiveLogStops(long diveLogId) {
		ArrayList<DiveLogStop> diveLogStops = new ArrayList<DiveLogStop>();

		DiveLogStopCursor cursor = queryDiveLogStops(diveLogId);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			diveLogStops.add(cursor.getDiveLogStop());
			cursor.moveToNext();
		}

		return diveLogStops;
	}

	public DiveLogStop getDiveLogStop(long id) {
		DiveLogStop diveLogStop = null;
		DiveLogStopCursor cursor = mHelper.queryDiveLogStop(id);
		cursor.moveToFirst();
		// If you got a row, get a run
		if (!cursor.isAfterLast())
			diveLogStop = cursor.getDiveLogStop();
		cursor.close();
		return diveLogStop;
	}
	
	public ScheduledDiveCursor queryScheduledDiveForSubmitter(long submitterID, 
			boolean isPublished, boolean isUnPublished, String title, String country,
			String state, String city, String timeStampStart, String timeStampEnd) {
		return mHelper.queryScheduledDiveForSubmitter(submitterID, isPublished, isUnPublished,
				title, country, state, city, timeStampStart, timeStampEnd);
	}

    public int queryScheduledDiveForSubmitterCount(long submitterID,
            boolean isPublished, boolean isUnPublished, String title, String country,
            String state, String city, String timeStampStart, String timeStampEnd) {
        return mHelper.queryScheduledDiveForSubmitterCount(submitterID, isPublished, isUnPublished,
                title, country, state, city, timeStampStart, timeStampEnd);
    }
	
	public ScheduledDiveCursor queryScheduledDiveForSite(long submitterID, long diveSiteID, 
			boolean isPublished, boolean isUnPublished, String timeStampStart, String timeStampEnd) {
		return mHelper.queryScheduledDiveForSite(submitterID, diveSiteID, isPublished, isUnPublished,
				timeStampStart, timeStampEnd);
	}

    public int queryScheduledDiveForSiteCount(long submitterID, long diveSiteID,
            boolean isPublished, boolean isUnPublished, String timeStampStart, String timeStampEnd) {
        return mHelper.queryScheduledDiveForSiteCount(submitterID, diveSiteID, isPublished, isUnPublished,
                timeStampStart, timeStampEnd);
    }
	
	public ScheduledDiveCursor queryScheduledDiveForUser(long submitterID, long diverID, 
			boolean isPublished, boolean isUnPublished, String title, String country,
			String state, String city, String timeStampStart, String timeStampEnd) {
		return mHelper.queryScheduledDiveForUser(submitterID, diverID, isPublished, isUnPublished,
				title, country, state, city, timeStampStart, timeStampEnd);
	}

    public int queryScheduledDiveForUserCount(long submitterID, long diverID,
            boolean isPublished, boolean isUnPublished, String title, String country,
            String state, String city, String timeStampStart, String timeStampEnd) {
        return mHelper.queryScheduledDiveForUserCount(submitterID, diverID, isPublished, isUnPublished,
                title, country, state, city, timeStampStart, timeStampEnd);
    }
	
	public ScheduledDive getScheduledDive(long id) {
		ScheduledDive scheduledDive = null;
		ScheduledDiveCursor cursor = mHelper.queryScheduledDive(id);

		cursor.moveToFirst();
		if (!cursor.isAfterLast())
			scheduledDive = cursor.getScheduledDive();
		cursor.close();
		return scheduledDive;
	}
	
	public long getScheduledDiveLocalId(long online_id) {
		return mHelper.queryScheduledDiveLocalId(online_id);
	}

	public ScheduledDiveDiveSiteCursor queryScheduledDiveDiveSites(long scheduledDiveLocalId) {
		return mHelper.queryScheduledDiveDiveSites(scheduledDiveLocalId);
	}

	public ArrayList<ScheduledDiveDiveSite> getScheduledDiveDiveSites(long scheduledDiveLocalId) {
		ArrayList<ScheduledDiveDiveSite> scheduledDiveDiveSites = new ArrayList<ScheduledDiveDiveSite>();

		ScheduledDiveDiveSiteCursor cursor = queryScheduledDiveDiveSites(scheduledDiveLocalId);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			scheduledDiveDiveSites.add(cursor.getScheduledDiveDiveSite());
			cursor.moveToNext();
		}

		return scheduledDiveDiveSites;
	}

	public ScheduledDiveDiveSite getScheduledDiveDiveSite(long id) {
		ScheduledDiveDiveSite scheduledDiveDiveSite = null;
		ScheduledDiveDiveSiteCursor cursor = mHelper.queryScheduledDiveDiveSite(id);
		cursor.moveToFirst();
		
		if (!cursor.isAfterLast())
			scheduledDiveDiveSite = cursor.getScheduledDiveDiveSite();
		cursor.close();
		return scheduledDiveDiveSite;
	}

	public long getScheduledDiveDiveSiteLocalId(long online_id) {
		return mHelper.queryScheduledDiveDiveSiteLocalId(online_id);
	}
	
	public ScheduledDiveUserCursor queryScheduledDiveUsers(long scheduledDiveLocalId) {
		return mHelper.queryScheduledDiveUsers(scheduledDiveLocalId);
	}

	public ArrayList<ScheduledDiveUser> getScheduledDiveUsers(long scheduledDiveLocalId) {
		ArrayList<ScheduledDiveUser> scheduledDiveUsers = new ArrayList<ScheduledDiveUser>();

		ScheduledDiveUserCursor cursor = queryScheduledDiveUsers(scheduledDiveLocalId);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			scheduledDiveUsers.add(cursor.getScheduledDiveUser());
			cursor.moveToNext();
		}

		return scheduledDiveUsers;
	}

	public ScheduledDiveUser getScheduledDiveUser(long id) {
		ScheduledDiveUser scheduledDiveUser = null;
		ScheduledDiveUserCursor cursor = mHelper.queryScheduledDiveUser(id);
		cursor.moveToFirst();
		
		if (!cursor.isAfterLast())
			scheduledDiveUser = cursor.getScheduledDiveUser();
		cursor.close();
		return scheduledDiveUser;
	}

	public long getScheduledDiveUserLocalId(long online_id) {
		return mHelper.queryScheduledDiveUserLocalId(online_id);
	}
	
	public long getDiveLogStopLocalId(long online_id) {
		return mHelper.queryDiveLogStopLocalId(online_id);
	}

	public void deleteDiveLogStop(long diveLogStopID) {
		mHelper.deleteDiveLogStop(diveLogStopID);
	}

	public long getDiveLogLocalId(long online_id) {
		return mHelper.queryDiveLogLocalId(online_id);
	}

	public long getLoggedInDiverId() {
		return mPrefs.getLong(PREF_CURRENT_USER_ID, -1);
	}

	public String getLoggedInDiverUsername() {
		return mPrefs.getString(PREF_CURRENT_USERNAME, "");
	}

    public String getLoggedInDiverProfileImagePath() {
        return mPrefs.getString(PREF_CURRENT_PROFILEIMAGE + getLoggedInDiverId(), "");
    }

    public Bitmap getLoggedInDiverProfileImage() {
        Bitmap profileImage = null;
        String filePath = getLoggedInDiverProfileImagePath().trim();
        if (!filePath.isEmpty()) {
            profileImage = decodeFileImage(filePath);
        }
        return profileImage;
    }

    public void saveLoggedInDiverProfileImage(Bitmap profileImage) {
        if (profileImage == null) {
            mPrefs.edit().putString(PREF_CURRENT_PROFILEIMAGE + getLoggedInDiverId(), "").apply();
        } else {
            String filePath = saveImageInternalStorage(profileImage, DIVER_PROFILE_IMAGE + getLoggedInDiverId());
            mPrefs.edit().putString(PREF_CURRENT_PROFILEIMAGE + getLoggedInDiverId(), filePath).apply();
        }
    }
	
	public Location getLastLocation() {
		Location location = new Location(TAG);

        if (mPrefs.contains(PREF_LAST_LOCATION_LATITUDE_LONG) && mPrefs.contains(PREF_LAST_LOCATION_LONGITUDE_LONG)) {
            double latitude = Double.longBitsToDouble(mPrefs.getLong(PREF_LAST_LOCATION_LATITUDE_LONG, 0));
            double longitude = Double.longBitsToDouble(mPrefs.getLong(PREF_LAST_LOCATION_LONGITUDE_LONG, 0));

            location.setLatitude(latitude);
            location.setLongitude(longitude);
        } else {
            String latitude = mPrefs.getString(PREF_LAST_LOCATION_LATITUDE, "");
            String longitude = mPrefs.getString(PREF_LAST_LOCATION_LONGITUDE, "");

            if (!latitude.isEmpty()) {
                location.setLatitude(Double.valueOf(latitude));
            }

            if (!longitude.isEmpty()) {
                location.setLongitude(Double.valueOf(longitude));
            }

            if (latitude.isEmpty() || longitude.isEmpty()) {
                location = null;
            }
        }

        return location;
	}
	
	public void saveLastLocation(Location location) {
        mPrefs.edit().putLong(PREF_LAST_LOCATION_LATITUDE_LONG, Double.doubleToLongBits(location.getLatitude())).apply();
        mPrefs.edit().putLong(PREF_LAST_LOCATION_LONGITUDE_LONG, Double.doubleToLongBits(location.getLongitude())).apply();
	}

	public void showNotification(int notificationId, String ticker, String contentTitle,
			String contentText, PendingIntent contentIntent) {
		
		NotificationManager notificationManager = 
				(NotificationManager) mAppContext.getSystemService(Activity.NOTIFICATION_SERVICE);
		
		Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		Notification notification = new NotificationCompat.Builder(mAppContext)
		.setTicker(ticker)
		.setSmallIcon(R.drawable.logo_symbol)
		.setContentTitle(contentTitle)
		.setContentText(contentText)
		.setContentIntent(contentIntent)
		.setAutoCancel(true)
		.setLights(R.color.notificationColor, 1000, 1000)
		.setSound(alarmSound)
		.setDefaults(Notification.DEFAULT_VIBRATE)
		.setOnlyAlertOnce(true)
		.build();
		
		notificationManager.notify(notificationId, notification);
	}
	
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param uri
	 *            The Uri to query.
	 * @author paulburke
	 */
	@SuppressLint("NewApi")
	public String getPath(final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(mAppContext, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(mAppContext, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(mAppContext, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(mAppContext, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	public Bitmap decodeFileImage(String filePath) {
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, o);

		// The new size we want to scale to
		final int REQUIRED_SIZE = 1024;

		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;

		return BitmapFactory.decodeFile(filePath, o2);
	}

	public String saveImageInternalStorage(Bitmap bitmapImage, String filename) {
		ContextWrapper cw = new ContextWrapper(mAppContext.getApplicationContext());
		File directory = cw.getDir("directoryName", Context.MODE_PRIVATE);
		File newPath = new File(directory, filename + fileCountInDirectory(directory.getAbsolutePath()) + ".jpg");

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(newPath);

			// Use the compress method on the BitMap object to write image to
			// the OutputStream
			bitmapImage.compress(Bitmap.CompressFormat.JPEG, 40, fos);

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newPath.getAbsolutePath();
	}

	private int fileCountInDirectory(String directory) {
		File f = new File(directory);
		File[] files = f.listFiles();

		return files.length;
	}
	
	public static int getIntegerPreferenceFromString(Context context, String key, int defaultValue) {
		Integer result = defaultValue;
		
		String valueString =
				PreferenceManager.getDefaultSharedPreferences(context)
				.getString(key, "");
		
		if (valueString != null && !valueString.isEmpty()) {
			result = Integer.valueOf(valueString);
		}
		
		return result;
	}
	
	 // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
