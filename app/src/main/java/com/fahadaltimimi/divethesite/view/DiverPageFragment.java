package com.fahadaltimimi.divethesite.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;

public abstract class DiverPageFragment extends Fragment {

	protected DiveSiteManager mDiveSiteManager;

	private SharedPreferences mPrefs;

	protected abstract void updateUI();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		mDiveSiteManager = DiveSiteManager.get(getActivity());

		mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}
