package com.fahadaltimimi.divethesite.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;

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
