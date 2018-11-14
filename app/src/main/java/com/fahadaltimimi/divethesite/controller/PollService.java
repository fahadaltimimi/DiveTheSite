package com.fahadaltimimi.divethesite.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveSiteCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveCursor;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.fahadaltimimi.divethesite.view.DiveApplication;
import com.fahadaltimimi.divethesite.view.DiveLogListActivity;
import com.fahadaltimimi.divethesite.view.DiveLogListTabFragment;
import com.fahadaltimimi.divethesite.view.DiveSiteListActivity;
import com.fahadaltimimi.divethesite.view.DiveSiteListTabFragment;
import com.fahadaltimimi.divethesite.view.ScheduledDiveListActivity;
import com.fahadaltimimi.divethesite.view.ScheduledDiveListTabFragment;

public class PollService extends IntentService {
	
	private static final String TAG = "PollService"; 
	
	public static final int POLL_INTERVAL_DEFAULT_SECONDS = 60;
	public static final int DISTANCE_LIMIT_DEFAULT = 100; // kilometers
	public static final int SCHEDULED_DIVES_DAYS_NOTICE = 10;
	
	public static final String PREF_IS_ALARM_ON = "isAlarmOn";
	
	private static final int NOTIFICATION_REQUIRE_REFRESH_DIVE_SITE_LIST = 0;
	private static final int NOTIFICATION_REQUIRE_REFRESH_DIVE_LOG_LIST = 1;
	private static final int NOTIFICATION_REQUIRE_REFRESH_SCHEDULED_DIVE_LIST = 2;
	private static final int NOTIFICATION_NEARBY_DIVE_SITE_LIST = 3;
	private static final int NOTIFICATION_NEARBY_SCHEDULED_DIVE_LIST = 4;
	private static final int NOTIFICATION_AUTO_REFRESH_DIVE_SITE_LIST = 5;
	private static final int NOTIFICATION_AUTO_REFRESH_DIVE_LOG_LIST = 6;
	private static final int NOTIFICATION_AUTO_REFRESH_SCHEDULED_DIVE_LIST = 7;
	
	private ArrayList<DiveSite> mDiveSitesRequiringRefresh;
	private ArrayList<DiveLog> mDiveLogsRequiringRefresh;
	private ArrayList<ScheduledDive> mScheduledDivesRequiringRefresh;
	
	private ArrayList<DiveSite> mDiveSitesAutoRefresh;	
	private ArrayList<DiveLog> mDiveLogsAutoRefresh;		
	private ArrayList<ScheduledDive> mScheduledDivesAutoRefresh;
	
	private ArrayList<DiveSite> mDiveSitesNewCloseby;
	private ArrayList<ScheduledDive> mScheduledDivesNewCloseby;
	
	protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;
	
	public PollService() {
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		ConnectivityManager cm = 
				(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = 
			cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
		if (!isNetworkAvailable) return;
		
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		
		// Stop polling if logged out
		if (diveSiteManager.getLoggedInDiverId() == -1 ||
				diveSiteManager.getLoggedInDiverUsername().trim().isEmpty()) {
			// Not logged in, stop polling and exit
			PollService.setServiceAlarm(this, false, 0);
			return;
		}
		
		// Skip polling if visible
		if (DiveApplication.isActivityVisible()) {
			return;
		}

        // Check Sync Mode
        int syncModeValue =
                DiveSiteManager.getIntegerPreferenceFromString(this,
                        this.getResources().getString(R.string.PREF_SETTING_SYNC_CONDITION), 0);
        boolean disablePolling = false;
        switch (syncModeValue) {
            case 0:
                // Wi-Fi only, disable polling if no wifi connected
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                disablePolling = !mWifi.isConnected();

                break;
            case 1:
                // Always sync, do nothing
                break;
            case 2:
                // Never sync, disable polling
                disablePolling = true;
                break;
        }

        if (disablePolling) {
            return;
        }

        try {
            mDiveSitesRequiringRefresh = new ArrayList<DiveSite>();
            mDiveLogsRequiringRefresh = new ArrayList<DiveLog>();
            mScheduledDivesRequiringRefresh = new ArrayList<ScheduledDive>();

            mDiveSitesAutoRefresh = new ArrayList<DiveSite>();
            mDiveLogsAutoRefresh = new ArrayList<DiveLog>();
            mScheduledDivesAutoRefresh = new ArrayList<ScheduledDive>();

            mDiveSitesNewCloseby = new ArrayList<DiveSite>();
            mScheduledDivesNewCloseby = new ArrayList<ScheduledDive>();

            // Check if any saved Dive Sites require refresh
            boolean checkDiveSiteRefresh =
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getResources().getString(R.string.PREF_SETTING_ENABLE_AUTO_DIVE_SITE_REFRESH_CHECK_NOTIFICATION), true);
            if (checkDiveSiteRefresh) {
                DiveSiteCursor diveSiteCursor = diveSiteManager.queryDiveSites();
                diveSiteCursor.moveToFirst();
                while (!diveSiteCursor.isAfterLast()) {
                    DiveSite diveSite = diveSiteCursor.getDiveSite();
                    if (diveSite.getOnlineId() != -1) {
                        if (diveSite.requiresRefresh() &&
                                !PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_DIVE_SITE), false)) {
                            // Commenting following to prevent constant notification of item requiring refresh
                            //mDiveSitesRequiringRefresh.add(diveSite);
                        } else if (!diveSite.requiresRefresh() ||
                                    PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                        .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_DIVE_SITE), false)) {
                            // Check if dive site needs to be refreshed
                            Date checkModifiedDate = new Date(diveSite.getLastModifiedOnline().getTime());
                            mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(this);
                            mDiveSiteOnlineDatabase
                                .setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                                    @Override
                                    public void onOnlineDiveDataPostBackground(
                                            ArrayList<Object> resultList, String message) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void onOnlineDiveDataRetrievedComplete(
                                            ArrayList<Object> resultList, String message, Boolean isError) {
                                        if (resultList.size() > 0) {
                                            // Dive Site retreived, so a refresh is required
                                            DiveSite updatedDiveSite = (DiveSite)resultList.get(0);
                                            DiveSite existingDiveSite =
                                                    DiveSiteManager.get(PollService.this).getDiveSite(updatedDiveSite.getLocalId());
                                            if (updatedDiveSite.getLastModifiedOnline().after(existingDiveSite.getLastModifiedOnline())) {
                                                boolean autoRefresh =
                                                        PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                                            .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_DIVE_SITE), false);

                                                if (autoRefresh) {
                                                    DiveSiteManager.get(PollService.this).saveDiveSite(updatedDiveSite);

                                                    mDiveSitesAutoRefresh.add(updatedDiveSite);
                                                    showDiveSiteAutoRefreshNotification();
                                                } else {
                                                    existingDiveSite.setRequiresRefresh(true);
                                                    DiveSiteManager.get(PollService.this).setDiveSiteRequiresRefresh(existingDiveSite);

                                                    mDiveSitesRequiringRefresh.add(existingDiveSite);
                                                    showDiveSiteRequireRefreshNotification();
                                                }
                                            } else {
                                                existingDiveSite.setRequiresRefresh(false);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onOnlineDiveDataProgress(Object result) {
                                        // TODO Auto-generated method stub

                                    }

                                });
                            mDiveSiteOnlineDatabase.getDiveSite(checkModifiedDate, diveSite.getOnlineId(), diveSite);
                        }
                    }

                    diveSiteCursor.moveToNext();
                }
            }

            // Check if any saved Dive Logs require refresh
            boolean checkDiveLogRefresh =
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getResources().getString(R.string.PREF_SETTING_ENABLE_AUTO_DIVE_LOG_REFRESH_CHECK_NOTIFICATION), true);
            if (checkDiveLogRefresh) {
                DiveLogCursor diveLogCursor = diveSiteManager.queryDiveLogs(-1, null, false);
                diveLogCursor.moveToFirst();
                while (!diveLogCursor.isAfterLast()) {
                    DiveLog diveLog = diveLogCursor.getDiveLog();
                    if (diveLog.getOnlineId() != -1) {
                        if (diveLog.requiresRefresh() &&
                                !PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_DIVE_LOG), false)) {
                            // Commenting following to prevent constant notification of item requiring refresh
                            //mDiveLogsRequiringRefresh.add(diveLog);
                        } else if (!diveLog.requiresRefresh() ||
                                PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                        .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_DIVE_LOG), false)) {
                            // Check if dive log needs to be refreshed
                            Date checkModifiedDate = new Date(diveLog.getLastModifiedOnline().getTime());
                            mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(this);
                            mDiveSiteOnlineDatabase
                                .setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                                    @Override
                                    public void onOnlineDiveDataPostBackground(
                                            ArrayList<Object> resultList, String message) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void onOnlineDiveDataRetrievedComplete(
                                            ArrayList<Object> resultList, String message, Boolean isError) {
                                        if (resultList.size() > 0) {
                                            // Dive Log retreived, so a refresh is required
                                            DiveLog updatedDiveLog = (DiveLog)resultList.get(0);
                                            DiveLog existingDiveLog =
                                                    DiveSiteManager.get(PollService.this).getDiveLog(updatedDiveLog.getLocalId());
                                            if (updatedDiveLog.getLastModifiedOnline().after(existingDiveLog.getLastModifiedOnline())) {
                                                boolean autoRefresh =
                                                        PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                                            .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_DIVE_LOG), false);

                                                if (autoRefresh) {
                                                    DiveSiteManager.get(PollService.this).saveDiveLog(updatedDiveLog);

                                                    mDiveLogsAutoRefresh.add(updatedDiveLog);
                                                    showDiveLogAutoRefreshNotification();
                                                } else {
                                                    existingDiveLog.setRequiresRefresh(true);
                                                    DiveSiteManager.get(PollService.this).setDiveLogRequiresRefresh(existingDiveLog);

                                                    mDiveLogsRequiringRefresh.add(existingDiveLog);
                                                    showDiveLogRequireRefreshNotification();
                                                }
                                            } else {
                                                existingDiveLog.setRequiresRefresh(false);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onOnlineDiveDataProgress(Object result) {
                                        // TODO Auto-generated method stub

                                    }

                                });
                            mDiveSiteOnlineDatabase.getDiveLog(checkModifiedDate, diveLog.getOnlineId(), diveLog);
                        }
                    }

                    diveLogCursor.moveToNext();
                }
            }

            // Check if any saved Scheduled Dives require refresh
            boolean checkScheduledDiveRefresh =
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getResources().getString(R.string.PREF_SETTING_ENABLE_AUTO_SCHEDULED_DIVE_REFRESH_CHECK_NOTIFICATION), true);
            if (checkScheduledDiveRefresh) {
                ScheduledDiveCursor scheduledDiveCursor = diveSiteManager.queryScheduledDiveForSubmitter(-1, true, true, "", "", "", "", "", "");
                scheduledDiveCursor.moveToFirst();
                while (!scheduledDiveCursor.isAfterLast()) {
                    ScheduledDive scheduledDive = scheduledDiveCursor.getScheduledDive();
                    if (scheduledDive.getOnlineId() != -1) {
                        if (scheduledDive.requiresRefresh() &&
                                !PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                    .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_SCHEDULED_DIVE), false)) {
                            // Commenting following to prevent constant notification of item requiring refresh
                            //mScheduledDivesRequiringRefresh.add(scheduledDive);
                        } else if (!scheduledDive.requiresRefresh() ||
                                    PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                        .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_SCHEDULED_DIVE), false)) {
                            // Check if scheduled dive needs to be refreshed
                            Date checkModifiedDate = new Date(scheduledDive.getLastModifiedOnline().getTime());
                            mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(this);
                            mDiveSiteOnlineDatabase
                                .setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                                    @Override
                                    public void onOnlineDiveDataPostBackground(
                                            ArrayList<Object> resultList, String message) {
                                        // TODO Auto-generated method stub

                                    }

                                    @Override
                                    public void onOnlineDiveDataRetrievedComplete(
                                            ArrayList<Object> resultList, String message, Boolean isError) {
                                        if (resultList.size() > 0) {
                                            // Scheduled Dive retreived, so a refresh is required
                                            ScheduledDive updatedScheduledDive = (ScheduledDive)resultList.get(0);
                                            ScheduledDive existingScheduledDive =
                                                    DiveSiteManager.get(PollService.this).getScheduledDive(updatedScheduledDive.getLocalId());
                                            if (updatedScheduledDive.getLastModifiedOnline().after(existingScheduledDive.getLastModifiedOnline())) {
                                                boolean autoRefresh =
                                                        PreferenceManager.getDefaultSharedPreferences(PollService.this)
                                                            .getBoolean(getResources().getString(R.string.PREF_SETTING_AUTO_REFRESH_SCHEDULED_DIVE), false);

                                                if (autoRefresh) {
                                                    DiveSiteManager.get(PollService.this).saveScheduledDive(updatedScheduledDive);

                                                    mScheduledDivesAutoRefresh.add(updatedScheduledDive);
                                                    showScheduledDiveAutoRefreshNotification();
                                                } else {
                                                    existingScheduledDive.setRequiresRefresh(true);
                                                    DiveSiteManager.get(PollService.this).setScheduledDiveRequiresRefresh(existingScheduledDive);

                                                    mScheduledDivesRequiringRefresh.add(existingScheduledDive);
                                                    showScheduledDiveRequireRefreshNotification();
                                                }
                                            } else {
                                                existingScheduledDive.setRequiresRefresh(false);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onOnlineDiveDataProgress(Object result) {
                                        // TODO Auto-generated method stub

                                    }

                                });
                            mDiveSiteOnlineDatabase.getScheduledDive(checkModifiedDate, scheduledDive.getOnlineId(), scheduledDive);
                        }
                    }

                    scheduledDiveCursor.moveToNext();
                }
            }

            // Display notification for each item type
            showDiveSiteRequireRefreshNotification();
            showDiveLogRequireRefreshNotification();
            showScheduledDiveRequireRefreshNotification();

            // Get location parameters
            Location lastLocation = diveSiteManager.getLastLocation();
            Location lastCheckLocation = getLastCheckLocation();

            // Have last checked location, can now save new one
            saveCheckLocation(lastLocation);

            // Check if there are any new dive sites near by
            boolean checkNearbyDiveSites =
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getResources().getString(R.string.PREF_SETTING_ENABLE_NEAR_DIVE_SITE_NOTIFICATION), true);

            int diveSiteDistanceLimit =
                    DiveSiteManager.getIntegerPreferenceFromString(this,
                            getResources().getString(R.string.PREF_SETTING_NEAR_DIVE_SITE_DISTANCE),
                            DISTANCE_LIMIT_DEFAULT);

            if (checkNearbyDiveSites && lastLocation != null && diveSiteDistanceLimit > 0) {
                Date lastNearDiveSiteCheck =
                        new Date(PreferenceManager.getDefaultSharedPreferences(this).
                                getLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_DIVE_SITE_CHECK_TIMESTAMP, 0));

                // If last checked location is greater than 50% of the set distance from current location, reset check date
                //  to trigger research of all items
                if (lastCheckLocation != null &&
                        Math.abs(lastLocation.distanceTo(lastCheckLocation)) > 0.5 * diveSiteDistanceLimit * 1000) {
                    lastNearDiveSiteCheck = new Date(0);
                }

                mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(this);
                mDiveSiteOnlineDatabase
                    .setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                        @Override
                        public void onOnlineDiveDataPostBackground(
                                ArrayList<Object> resultList, String message) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onOnlineDiveDataRetrievedComplete(
                                ArrayList<Object> resultList, String message, Boolean isError) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onOnlineDiveDataProgress(Object result) {
                            // Notify for new item if not already saved, i.e. item has no local ID
                            if (((DiveSite)result).getLocalId() == -1) {
                                mDiveSitesNewCloseby.add((DiveSite) result);
                                showDiveSiteNewCloseby();
                            }
                        }

                    });
                mDiveSiteOnlineDatabase.getDiveSiteList(lastNearDiveSiteCheck, -1,
                        String.valueOf(lastLocation.getLatitude()), String.valueOf(lastLocation.getLongitude()),
                        "", "", "", "", "", "", "", "", String.valueOf(diveSiteDistanceLimit), "", "");

                // Store date searched and location searched
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit().putLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_DIVE_SITE_CHECK_TIMESTAMP,
                            (new Date()).getTime()).apply();

            }

            // Check if there are any new dives near by
            boolean checkNearbyScheduledDives =
                    PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getResources().getString(R.string.PREF_SETTING_ENABLE_NEAR_SCHEDULED_DIVE_NOTIFICATION), true);

            int scheduledDiveDistanceLimit =
                    DiveSiteManager.getIntegerPreferenceFromString(this,
                            getResources().getString(R.string.PREF_SETTING_NEAR_SCHEDULED_DIVE_DISTANCE),
                            DISTANCE_LIMIT_DEFAULT);

            if (checkNearbyScheduledDives && lastLocation != null && scheduledDiveDistanceLimit > 0) {
                Date lastNearScheduledDiveCheck =
                        new Date(PreferenceManager.getDefaultSharedPreferences(this).
                                getLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP, 0));

                // If last checked location is greater than 50% of the set distance from current location, reset check date
                //  to trigger research of all items
                if (lastCheckLocation != null &&
                        Math.abs(lastLocation.distanceTo(lastCheckLocation)) > 0.5 * scheduledDiveDistanceLimit * 1000) {
                    lastNearScheduledDiveCheck = new Date(0);
                }

                mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(this);
                mDiveSiteOnlineDatabase
                    .setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                        @Override
                        public void onOnlineDiveDataPostBackground(
                                ArrayList<Object> resultList, String message) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onOnlineDiveDataRetrievedComplete(
                                ArrayList<Object> resultList, String message, Boolean isError) {
                            // TODO Auto-generated method stub
                        }

                        @Override
                        public void onOnlineDiveDataProgress(Object result) {
                            // Notify for new item if not already saved, i.e. item has no local ID
                            if (((ScheduledDive)result).getLocalId() == -1) {
                                mScheduledDivesNewCloseby.add((ScheduledDive) result);
                                showScheduledDivesNewCloseby();
                            }
                        }

                    });

                int scheduledDiveDaysNotice =
                        DiveSiteManager.getIntegerPreferenceFromString(this,
                                getResources().getString(R.string.PREF_SETTING_NEAR_SCHEDULED_DIVE_DAYS_NOTICE),
                                SCHEDULED_DIVES_DAYS_NOTICE);

                Date ignoreTimeStampStart = lastNearScheduledDiveCheck;
                Calendar calIgnore = Calendar.getInstance();
                calIgnore.setTime(ignoreTimeStampStart);
                calIgnore.add(Calendar.DATE, scheduledDiveDaysNotice);
                Date ignoreTimeStampEnd = calIgnore.getTime();

                lastNearScheduledDiveCheck = new Date();
                Date timeStampStart = lastNearScheduledDiveCheck;
                Calendar cal = Calendar.getInstance();
                cal.setTime(timeStampStart);
                cal.add(Calendar.DATE, scheduledDiveDaysNotice);
                Date timeStampEnd = cal.getTime();

                mDiveSiteOnlineDatabase.getScheduledDiveList(new Date(0), -1, -1, -1,
                        String.valueOf(lastLocation.getLatitude()), String.valueOf(lastLocation.getLongitude()),
                        "", "", "", "",
                        String.valueOf(ignoreTimeStampStart.getTime()), String.valueOf(ignoreTimeStampEnd.getTime()),
                        String.valueOf(timeStampStart.getTime()), String.valueOf(timeStampEnd.getTime()),
                        String.valueOf(scheduledDiveDistanceLimit), "", "");

                // Store date searched and location searched
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit().putLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP,
                            (lastNearScheduledDiveCheck).getTime()).apply();

            }
        } catch (Exception e) {
            e.printStackTrace();

            // Error occurred, disable polling
            PollService.setServiceAlarm(this, false, 0);
        }
	}
	
	private void showDiveSiteRequireRefreshNotification() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mDiveSitesRequiringRefresh.size() > 0) {
			Intent i = new Intent(this, DiveSiteListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_DIVESITELIST_TAB_INDEX, DiveSiteListTabFragment.LOCAL_TAB_INDEX);
			
			PendingIntent piDiveSiteList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mDiveSitesRequiringRefresh.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_REQUIRE_REFRESH_DIVE_SITE_LIST, 
						r.getString(R.string.notify_divesites_require_refresh),
						r.getString(R.string.notify_divesites_require_refresh),
						r.getString(R.string.notify_divesite_require_refresh_text),
						piDiveSiteList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_REQUIRE_REFRESH_DIVE_SITE_LIST, 
						r.getString(R.string.notify_divesites_require_refresh),
						r.getString(R.string.notify_divesites_require_refresh),
						String.format(r.getString(R.string.notify_divesites_require_refresh_text), 
								mDiveSitesRequiringRefresh.size()),
						piDiveSiteList);
			}			
		}
	}
	
	private void showDiveLogRequireRefreshNotification() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mDiveLogsRequiringRefresh.size() > 0) {
			Intent i = new Intent(this, DiveLogListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_DIVELOGLIST_TAB_INDEX, DiveLogListTabFragment.LOCAL_TAB_INDEX);
			
			PendingIntent piDiveLogList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mDiveLogsRequiringRefresh.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_REQUIRE_REFRESH_DIVE_LOG_LIST, 
						r.getString(R.string.notify_divelogs_require_refresh),
						r.getString(R.string.notify_divelogs_require_refresh),
						r.getString(R.string.notify_divelog_require_refresh_text),
						piDiveLogList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_REQUIRE_REFRESH_DIVE_LOG_LIST, 
						r.getString(R.string.notify_divelogs_require_refresh),
						r.getString(R.string.notify_divelogs_require_refresh),
						String.format(r.getString(R.string.notify_divelogs_require_refresh_text), 
								mDiveLogsRequiringRefresh.size()),
						piDiveLogList);
			}
		}
	}

	private void showScheduledDiveRequireRefreshNotification() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mScheduledDivesRequiringRefresh.size() > 0) {
			Intent i = new Intent(this, ScheduledDiveListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_SCHEDULEDDIVELIST_TAB_INDEX, ScheduledDiveListTabFragment.LOCAL_TAB_INDEX);
			
			PendingIntent piScheduledDiveList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mScheduledDivesRequiringRefresh.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_REQUIRE_REFRESH_SCHEDULED_DIVE_LIST, 
						r.getString(R.string.notify_scheduleddives_require_refresh),
						r.getString(R.string.notify_scheduleddives_require_refresh),
						r.getString(R.string.notify_scheduleddive_require_refresh_text),
						piScheduledDiveList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_REQUIRE_REFRESH_SCHEDULED_DIVE_LIST, 
						r.getString(R.string.notify_scheduleddives_require_refresh),
						r.getString(R.string.notify_scheduleddives_require_refresh),
						String.format(r.getString(R.string.notify_scheduleddives_require_refresh_text), 
								mScheduledDivesRequiringRefresh.size()),
						piScheduledDiveList);
			}
		}
	}
	
	private void showDiveSiteAutoRefreshNotification() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mDiveSitesAutoRefresh.size() > 0) {
			Intent i = new Intent(this, DiveSiteListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_DIVESITELIST_TAB_INDEX, DiveSiteListTabFragment.LOCAL_TAB_INDEX);
			
			PendingIntent piDiveSiteList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mDiveSitesAutoRefresh.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_AUTO_REFRESH_DIVE_SITE_LIST, 
						r.getString(R.string.notify_divesites_require_refresh),
						r.getString(R.string.notify_divesites_require_refresh),
						r.getString(R.string.notify_divesite_auto_refresh_text),
						piDiveSiteList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_AUTO_REFRESH_DIVE_SITE_LIST, 
						r.getString(R.string.notify_divesites_require_refresh),
						r.getString(R.string.notify_divesites_require_refresh),
						String.format(r.getString(R.string.notify_divesites_auto_refresh_text), 
								mDiveSitesAutoRefresh.size()),
						piDiveSiteList);
			}			
		}
	}
	
	private void showDiveLogAutoRefreshNotification() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mDiveLogsAutoRefresh.size() > 0) {
			Intent i = new Intent(this, DiveLogListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_DIVELOGLIST_TAB_INDEX, DiveLogListTabFragment.LOCAL_TAB_INDEX);
			
			PendingIntent piDiveLogList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mDiveLogsAutoRefresh.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_AUTO_REFRESH_DIVE_LOG_LIST, 
						r.getString(R.string.notify_divelogs_require_refresh),
						r.getString(R.string.notify_divelogs_require_refresh),
						r.getString(R.string.notify_divelog_auto_refresh_text),
						piDiveLogList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_AUTO_REFRESH_DIVE_LOG_LIST, 
						r.getString(R.string.notify_divelogs_require_refresh),
						r.getString(R.string.notify_divelogs_require_refresh),
						String.format(r.getString(R.string.notify_divelogs_auto_refresh_text), 
								mDiveLogsAutoRefresh.size()),
						piDiveLogList);
			}
		}
	}

	private void showScheduledDiveAutoRefreshNotification() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mScheduledDivesAutoRefresh.size() > 0) {
			Intent i = new Intent(this, ScheduledDiveListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_SCHEDULEDDIVELIST_TAB_INDEX, ScheduledDiveListTabFragment.LOCAL_TAB_INDEX);
			
			PendingIntent piScheduledDiveList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mScheduledDivesAutoRefresh.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_AUTO_REFRESH_SCHEDULED_DIVE_LIST, 
						r.getString(R.string.notify_scheduleddives_require_refresh),
						r.getString(R.string.notify_scheduleddives_require_refresh),
						r.getString(R.string.notify_scheduleddive_auto_refresh_text),
						piScheduledDiveList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_AUTO_REFRESH_SCHEDULED_DIVE_LIST, 
						r.getString(R.string.notify_scheduleddives_require_refresh),
						r.getString(R.string.notify_scheduleddives_require_refresh),
						String.format(r.getString(R.string.notify_scheduleddives_auto_refresh_text), 
								mScheduledDivesAutoRefresh.size()),
						piScheduledDiveList);
			}
		}
	}
	
	private void showDiveSiteNewCloseby() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mDiveSitesNewCloseby.size() > 0) {
			Intent i = new Intent(this, DiveSiteListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_DIVESITELIST_TAB_INDEX, DiveSiteListTabFragment.ONLINE_TAB_INDEX);
			
			PendingIntent piDiveSiteList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mDiveSitesNewCloseby.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_NEARBY_DIVE_SITE_LIST, 
						r.getString(R.string.notify_divesite_close_nearby),
						r.getString(R.string.notify_divesite_close_nearby),
						r.getString(R.string.notify_divesite_close_nearby_text),
						piDiveSiteList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_NEARBY_DIVE_SITE_LIST, 
						r.getString(R.string.notify_divesites_close_nearby),
						r.getString(R.string.notify_divesites_close_nearby),
						String.format(r.getString(R.string.notify_divesites_close_nearby_text), 
								mDiveSitesNewCloseby.size()),
						piDiveSiteList);
			}			
		}
	}
	
	private void showScheduledDivesNewCloseby() {
		DiveSiteManager diveSiteManager = DiveSiteManager.get(this);
		Resources r = getResources();
		
		if (mScheduledDivesNewCloseby.size() > 0) {
			Intent i = new Intent(this, ScheduledDiveListActivity.class);
			i.putExtra(DiveSiteManager.EXTRA_SCHEDULEDDIVELIST_TAB_INDEX, ScheduledDiveListTabFragment.ONLINE_TAB_INDEX);
			
			PendingIntent piScheduledDiveList = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			if (mScheduledDivesNewCloseby.size() == 1) {
				diveSiteManager.showNotification(NOTIFICATION_NEARBY_SCHEDULED_DIVE_LIST, 
						r.getString(R.string.notify_scheduleddive_close_nearby),
						r.getString(R.string.notify_scheduleddive_close_nearby),
						r.getString(R.string.notify_scheduleddive_close_nearby_text),
						piScheduledDiveList);
			} else {
				diveSiteManager.showNotification(NOTIFICATION_NEARBY_SCHEDULED_DIVE_LIST, 
						r.getString(R.string.notify_scheduleddives_close_nearby),
						r.getString(R.string.notify_scheduleddives_close_nearby),
						String.format(r.getString(R.string.notify_scheduleddives_close_nearby_text), 
								mScheduledDivesNewCloseby.size()),
						piScheduledDiveList);
			}			
		}
	}
	
	public static void setServiceAlarm(Context context, boolean isOn, int intervalCheckUpdatesSeconds) {
		Intent i = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		if (intervalCheckUpdatesSeconds <= 0) {
			isOn = false;
		}
		
		if (isOn) {
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalCheckUpdatesSeconds * 1000, pi);	
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
		
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit()
			.putBoolean(PREF_IS_ALARM_ON, isOn)
			.apply();
	}
	
	private Location getLastCheckLocation() {
		Location location = new Location(TAG);
		
		String latitude = 
				PreferenceManager.getDefaultSharedPreferences(this)
					.getString(DiveSiteManager.PREF_SETTING_LAST_NEAR_ITEM_CHECK_LATITUDE, "");
		String longitude = 
				PreferenceManager.getDefaultSharedPreferences(this)
					.getString(DiveSiteManager.PREF_SETTING_LAST_NEAR_ITEM_CHECK_LONGITUDE, "");
		
		if (!latitude.isEmpty()) {
			location.setLatitude(Float.valueOf(latitude));
		}
		
		if (!longitude.isEmpty()) {
			location.setLongitude(Float.valueOf(longitude));
		}
		
		if (latitude.isEmpty() || longitude.isEmpty()) {
			location = null;
		}
		
		return location;
	}
	
	private void saveCheckLocation(Location location) {
		if (location != null) {
			PreferenceManager.getDefaultSharedPreferences(this)
				.edit().putString(DiveSiteManager.PREF_SETTING_LAST_NEAR_ITEM_CHECK_LATITUDE, 
						String.valueOf(location.getLatitude())).apply();
			PreferenceManager.getDefaultSharedPreferences(this)
				.edit().putString(DiveSiteManager.PREF_SETTING_LAST_NEAR_ITEM_CHECK_LONGITUDE, 
						String.valueOf(location.getLongitude())).apply();
		} else {
			PreferenceManager.getDefaultSharedPreferences(this)
			.edit().putString(DiveSiteManager.PREF_SETTING_LAST_NEAR_ITEM_CHECK_LATITUDE, 
					"").apply();
		PreferenceManager.getDefaultSharedPreferences(this)
			.edit().putString(DiveSiteManager.PREF_SETTING_LAST_NEAR_ITEM_CHECK_LONGITUDE, 
					"").apply();
		}
	}
	
	public static boolean isServiceAlarmOn(Context context) {
		Intent i = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		return pi != null;
	}
}
