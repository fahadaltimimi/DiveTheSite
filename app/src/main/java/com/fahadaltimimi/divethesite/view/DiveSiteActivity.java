package com.fahadaltimimi.divethesite.view;

import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.DiveSite;

public class DiveSiteActivity extends DiveActivity {

	@Override
	protected Fragment createFragment() {
		DiveSite diveSite = getIntent().getParcelableExtra(
				DiveSiteManager.EXTRA_DIVE_SITE);
		DiveLog diveLog = getIntent().getParcelableExtra(
				DiveLogActivity.EXTRA_DIVE_LOG);

		return DiveSiteTabFragment.newInstance(diveLog, diveSite);
	}
}