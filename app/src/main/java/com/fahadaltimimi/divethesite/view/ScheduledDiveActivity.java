package com.fahadaltimimi.divethesite.view;

import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.ScheduledDive;

public class ScheduledDiveActivity extends DiveActivity {

	public static final String EXTRA_SCHEDULED_DIVE = "com.fahadaltimimi.divethesite.scheduled_dive";
	public static final String EXTRA_SET_TO_SCHEDULED_DIVE = "com.fahadaltimimi.divethesite.set_to_scheduled_dive";

	@Override
	protected Fragment createFragment() {
		ScheduledDive scheduledDive = getIntent().getParcelableExtra(EXTRA_SCHEDULED_DIVE);
        DiveSite diveSite = getIntent().getParcelableExtra(DiveSiteManager.EXTRA_DIVE_SITE);

		return ScheduledDiveFragment.newInstance(scheduledDive, diveSite);
	}
}