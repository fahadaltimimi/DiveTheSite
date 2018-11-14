package com.fahadaltimimi.divethesite.model;

import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.view.DiveActivity;
import com.fahadaltimimi.divethesite.view.DiveLogFragment;

public class DiveLogActivity extends DiveActivity {

	public static final String EXTRA_DIVE_LOG = "com.fahadaltimimi.divethesite.dive_log";
	public static final String EXTRA_SET_TO_DIVELOG = "com.fahadaltimimi.divethesite.set_to_divelog";

	@Override
	protected Fragment createFragment() {
		DiveLog diveLog = getIntent().getParcelableExtra(EXTRA_DIVE_LOG);
        DiveSite diveSite = getIntent().getParcelableExtra(DiveSiteManager.EXTRA_DIVE_SITE);

		return DiveLogFragment.newInstance(diveLog, diveSite);
	}
}