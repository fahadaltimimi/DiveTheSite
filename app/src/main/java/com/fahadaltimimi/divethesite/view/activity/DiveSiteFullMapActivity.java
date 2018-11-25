package com.fahadaltimimi.divethesite.view.activity;

import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.view.fragment.DiveSiteFullMapFragment;

public class DiveSiteFullMapActivity extends DiveActivity {

	@Override
	protected Fragment createFragment() {
		return new DiveSiteFullMapFragment();
	}

}
