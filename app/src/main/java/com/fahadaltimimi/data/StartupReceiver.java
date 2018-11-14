package com.fahadaltimimi.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.PollService;
import com.fahadaltimimi.divethesite.R;

public class StartupReceiver extends BroadcastReceiver {
	
	private static final String TAG = "StartupReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isOn = prefs.getBoolean(PollService.PREF_IS_ALARM_ON, false);
		PollService.setServiceAlarm(context, isOn, 
				DiveSiteManager.getIntegerPreferenceFromString(context,
                        context.getResources().getString(R.string.PREF_SETTING_INTERVAL_CHECK_UPDATES_SEC),
                        PollService.POLL_INTERVAL_DEFAULT_SECONDS));
	}
}
