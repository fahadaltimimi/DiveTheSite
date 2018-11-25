package com.fahadaltimimi.divethesite.view.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.view.fragment.DiveSiteListTabFragment;

public class DiveSiteListActivity extends DiveActivity {

	@Override
	protected Fragment createFragment() {
		long diverID = getIntent().getLongExtra(DiverActivity.EXTRA_DIVER_ID, -1);
		boolean setToDiveLog = getIntent().getBooleanExtra(
				DiveLogActivity.EXTRA_SET_TO_DIVELOG, false);
		int tabIndex = getIntent().getIntExtra(DiveSiteManager.EXTRA_DIVESITELIST_TAB_INDEX, 0);
		
		return DiveSiteListTabFragment.newInstance(diverID, setToDiveLog, tabIndex, false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.diveSiteListTitle);
	}
}
