package com.fahadaltimimi.divethesite.view;

import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.model.DiveLogActivity;

public class DiverListActivity extends DiveActivity {

	@Override
	protected Fragment createFragment() {
		boolean setToDiveLog = getIntent().getBooleanExtra(
				DiveLogActivity.EXTRA_SET_TO_DIVELOG, false);
		return DiverListFragment.newInstance(setToDiveLog);
	}

}
