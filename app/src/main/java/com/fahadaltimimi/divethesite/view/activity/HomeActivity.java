package com.fahadaltimimi.divethesite.view.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.view.fragment.HomeFragment;

public class HomeActivity extends DiveActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        hideActionBar();

	}
	
	@Override
	protected Fragment createFragment() {
		return new HomeFragment();
	}
}
