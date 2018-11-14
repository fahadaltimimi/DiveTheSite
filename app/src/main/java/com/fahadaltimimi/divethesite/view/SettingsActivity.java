package com.fahadaltimimi.divethesite.view;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.PollService;
import com.fahadaltimimi.divethesite.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

        // Add container
        addPreferencesFromResource(R.xml.pref_container);

		// Add 'general' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_general);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_general_settings);

		// Add 'dive sites' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_divesites);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_dive_sites);
		
		// Add 'dive logs' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_divelogs);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_dive_logs);
		
		// Add 'scheduled dives' preferences, and a corresponding header.
		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_scheduleddives);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_scheduled_dives);

        Preference syncMode =
                findPreference(getResources().getString(R.string.PREF_SETTING_SYNC_CONDITION));
        syncMode.setSummary(((ListPreference)syncMode).getEntry());
        syncMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference listPreference = (ListPreference) preference;

                // Set the summary to reflect the new value.
                int index = listPreference.findIndexOfValue(newValue.toString());
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);

                return true;
            }

        });

		Preference syncFrequency = 
				findPreference(getResources().getString(R.string.PREF_SETTING_INTERVAL_CHECK_UPDATES_SEC));
		syncFrequency.setSummary(((ListPreference)syncFrequency).getEntry());
		syncFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// Reset Poll Service if value changed
				ListPreference listPreference = (ListPreference) preference;
				String oldValue = listPreference.getValue();
				
				if (!oldValue.equals(newValue.toString())) {
					if (PollService.isServiceAlarmOn(SettingsActivity.this)) {
			        	PollService.setServiceAlarm(SettingsActivity.this, false, 0);
			        }
			        PollService.setServiceAlarm(SettingsActivity.this, true, Integer.valueOf(newValue.toString()));
				}

				// Set the summary to reflect the new value.
				int index = listPreference.findIndexOfValue(newValue.toString());
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);
				
				return true;
			}
			
		});
		
		Preference diveSiteDistance = 
				findPreference(getResources().getString(R.string.PREF_SETTING_NEAR_DIVE_SITE_DISTANCE));
		diveSiteDistance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// Append (km) to summary
				preference.setSummary(newValue.toString() + " km");
				return true;
			}
			
		});
		diveSiteDistance.getOnPreferenceChangeListener().onPreferenceChange(
				diveSiteDistance,
				PreferenceManager.getDefaultSharedPreferences(
						diveSiteDistance.getContext()).getString(diveSiteDistance.getKey(), ""));
		
		Preference scheduledDiveDistance = 
				findPreference(getResources().getString(R.string.PREF_SETTING_NEAR_SCHEDULED_DIVE_DISTANCE));		
		scheduledDiveDistance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// Append (km) to summary
				preference.setSummary(newValue.toString() + " km");
				return true;
			}
			
		});
		scheduledDiveDistance.getOnPreferenceChangeListener().onPreferenceChange(
				scheduledDiveDistance,
				PreferenceManager.getDefaultSharedPreferences(
						scheduledDiveDistance.getContext()).getString(scheduledDiveDistance.getKey(), ""));
		
		Preference scheduledDiveDaysNotice =
				findPreference(getResources().getString(R.string.PREF_SETTING_NEAR_SCHEDULED_DIVE_DAYS_NOTICE));
		scheduledDiveDaysNotice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				// Modify saved timestamp
				Date lastNearScheduledDiveCheck = 
						new Date(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).
								getLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP, 0));
				
				int previousDaysNotice = Integer.valueOf(((EditTextPreference)preference).getText());
				int newDaysNotice = Integer.valueOf(newValue.toString());
				
				int timeStampAdjustment = previousDaysNotice - newDaysNotice;
				Calendar cal = Calendar.getInstance();
				cal.setTime(lastNearScheduledDiveCheck);
				cal.add(Calendar.DATE, timeStampAdjustment);
				lastNearScheduledDiveCheck = cal.getTime();
				
				PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
					.edit().putLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP, 
							(lastNearScheduledDiveCheck).getTime()).commit();
				
				// Append (days) to summary
				preference.setSummary(newValue.toString() + " days");
				return true;
			}
			
		});
		scheduledDiveDaysNotice.getOnPreferenceChangeListener().onPreferenceChange(
				scheduledDiveDaysNotice,
				PreferenceManager.getDefaultSharedPreferences(
						scheduledDiveDaysNotice.getContext()).getString(scheduledDiveDaysNotice.getKey(), ""));
	}

	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & 
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/** {@inheritDoc} */
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = 
			new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);

				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(), ""));
	}

	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general_settings);

            Preference syncMode =
                    findPreference(getResources().getString(R.string.PREF_SETTING_SYNC_CONDITION));
            syncMode.setSummary(((ListPreference)syncMode).getEntry());
            syncMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Reset Poll Service if value changed
                    ListPreference listPreference = (ListPreference) preference;

                    // Set the summary to reflect the new value.
                    int index = listPreference.findIndexOfValue(newValue.toString());
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);

                    return true;
                }

            });

			Preference syncFrequency = 
					findPreference(getResources().getString(R.string.PREF_SETTING_INTERVAL_CHECK_UPDATES_SEC));
			syncFrequency.setSummary(((ListPreference)syncFrequency).getEntry());	
			syncFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Reset Poll Service if value changed
					ListPreference listPreference = (ListPreference) preference;
					String oldValue = listPreference.getValue();
					
					if (!oldValue.equals(newValue.toString())) {
						if (PollService.isServiceAlarmOn(getActivity())) {
				        	PollService.setServiceAlarm(getActivity(), false, 0);
				        }
				        PollService.setServiceAlarm(getActivity(), true, Integer.valueOf(newValue.toString()));
					}

					// Set the summary to reflect the new value.
					int index = listPreference.findIndexOfValue(newValue.toString());
					preference.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);
					
					return true;
				}
				
			});
		}
	}

	/**
	 * This fragment shows dive sites preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DiveSitePreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_dive_sites);

			Preference distance = 
					findPreference(getResources().getString(R.string.PREF_SETTING_NEAR_DIVE_SITE_DISTANCE));
					
			distance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Append (km) to summary
					preference.setSummary(newValue.toString() + " km");
					return true;
				}
				
			});
			distance.getOnPreferenceChangeListener().onPreferenceChange(
					distance,
					PreferenceManager.getDefaultSharedPreferences(
							distance.getContext()).getString(distance.getKey(), ""));
		}
	}
	
	/**
	 * This fragment shows dive logs preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class DiveLogPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_dive_logs);
		}
	}
	
	/**
	 * This fragment shows scheduled dives preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class ScheduledDivePreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_scheduled_dives);

			Preference distance = 
					findPreference(getResources().getString(R.string.PREF_SETTING_NEAR_SCHEDULED_DIVE_DISTANCE));		
			distance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Append (km) to summary
					preference.setSummary(newValue.toString() + " km");
					return true;
				}
				
			});
			distance.getOnPreferenceChangeListener().onPreferenceChange(
					distance,
					PreferenceManager.getDefaultSharedPreferences(
							distance.getContext()).getString(distance.getKey(), ""));
			
			Preference daysNotice =
					findPreference(getResources().getString(R.string.PREF_SETTING_NEAR_SCHEDULED_DIVE_DAYS_NOTICE));
			daysNotice.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// Modify saved timestamp
					Date lastNearScheduledDiveCheck = 
							new Date(PreferenceManager.getDefaultSharedPreferences(ScheduledDivePreferenceFragment.this.getActivity()).
									getLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP, 0));
					
					int previousDaysNotice = Integer.valueOf(((EditTextPreference)preference).getText());
					int newDaysNotice = Integer.valueOf(newValue.toString());
					
					int timeStampAdjustment = previousDaysNotice - newDaysNotice;
					Calendar cal = Calendar.getInstance();
					cal.setTime(lastNearScheduledDiveCheck);
					cal.add(Calendar.DATE, timeStampAdjustment);
					lastNearScheduledDiveCheck = cal.getTime();
					
					PreferenceManager.getDefaultSharedPreferences(ScheduledDivePreferenceFragment.this.getActivity())
						.edit().putLong(DiveSiteManager.PREF_SETTING_LAST_NEAR_SCHEDULED_DIVE_CHECK_TIMESTAMP, 
								(lastNearScheduledDiveCheck).getTime()).commit();
					
					// Append (days) to summary
					preference.setSummary(newValue.toString() + " days");
					return true;
				}
				
			});
			daysNotice.getOnPreferenceChangeListener().onPreferenceChange(
					daysNotice,
					PreferenceManager.getDefaultSharedPreferences(
							daysNotice.getContext()).getString(daysNotice.getKey(), ""));
		}
	}
}
