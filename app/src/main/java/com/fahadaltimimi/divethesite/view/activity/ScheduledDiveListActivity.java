package com.fahadaltimimi.divethesite.view.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.view.fragment.ScheduledDiveListTabFragment;

public class ScheduledDiveListActivity extends DiveActivity {

	@Override
	protected Fragment createFragment() {
		long diverID = getIntent().getLongExtra(DiveSiteManager.EXTRA_DIVER_ID, -1);
		DiveSite diveSite = getIntent().getParcelableExtra(DiveSiteManager.EXTRA_DIVE_SITE);
		int tabIndex = getIntent().getIntExtra(DiveSiteManager.EXTRA_SCHEDULEDDIVELIST_TAB_INDEX, 0);
		
		return ScheduledDiveListTabFragment.newInstance(diverID, diveSite, tabIndex, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setTitle(R.string.scheduledDiveListTitle);
	}
}
