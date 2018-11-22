package com.fahadaltimimi.divethesite.view.activity;

import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.view.fragment.DiverTabFragment;

public class DiverActivity extends DiveActivity {

	public static final String EXTRA_DIVER_ID = "com.fahadaltimimi.divethesite.diver_id";
	public static final String EXTRA_DIVER_USERNAME = "com.fahadaltimimi.divethesite.diver_username";

	@Override
	protected Fragment createFragment() {
		long diverID = getIntent().getLongExtra(EXTRA_DIVER_ID, -1);
		String diverUsername = getIntent().getStringExtra(EXTRA_DIVER_USERNAME);

		if (diverID != -1) {
			return DiverTabFragment.newInstance(diverID, diverUsername);
		} else {
			return new DiverTabFragment();
		}
	}
}