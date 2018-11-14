package com.fahadaltimimi.divethesite.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.fahadaltimimi.divethesite.R;

public abstract class SingleFragmentActivity extends AppCompatActivity {
	protected abstract Fragment createFragment();

	// This method will be overriden by sub classes t,o return the layout those
	// activities will inflate
	protected int getLayoutResId() {
		return R.layout.activity_fragment;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId());
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (fragment == null) {
			// Create fragment (depends on class implementing this)
			// Depending on class implementing this, appropriate view will be
			// created for the fragment being created
			fragment = createFragment();

			if (fragment != null) {
				fm.beginTransaction().add(R.id.fragmentContainer, fragment)
						.commit();
			}
		}
	}
}
