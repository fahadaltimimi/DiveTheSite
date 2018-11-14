package com.fahadaltimimi.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationReceiver extends BroadcastReceiver {

	private static final String TAG = "LocationReceiver";
	public static final String KEY_LOCATION_RETREIVED_RECENT = "KEY_LOCATION_RETREIVED_RECENT";

	@Override
	public void onReceive(Context context, Intent intent) {
		// If you got a Location extra, use it
		Location loc = (Location) intent
				.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
		boolean isLocationRetreivedRecent = intent.getBooleanExtra(
				KEY_LOCATION_RETREIVED_RECENT, false);

		if (loc != null) {
			onLocationReceived(context, loc, isLocationRetreivedRecent);
			return;
		}

		// If you get here, something else has happened
		if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
			boolean enabled = intent.getBooleanExtra(
					LocationManager.KEY_PROVIDER_ENABLED, false);
			onProviderEnabledChanged(enabled);
		}
	}

	protected void onLocationReceived(Context context, Location loc,
			boolean isLocationRetreivedRecent) {
		Log.d(TAG, this + " Got location from " + loc.getProvider() + ": "
				+ loc.getLatitude() + ", " + loc.getLongitude());
	}

	protected void onProviderEnabledChanged(boolean enabled) {
		Log.d(TAG, "Provider " + (enabled ? "enabled" : "disabled"));
	}

}
