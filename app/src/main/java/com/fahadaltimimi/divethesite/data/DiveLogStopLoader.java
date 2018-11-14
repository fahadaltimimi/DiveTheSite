package com.fahadaltimimi.divethesite.data;

import android.content.Context;

import com.fahadaltimimi.data.DataLoader;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveLogStop;

public class DiveLogStopLoader extends DataLoader<DiveLogStop> {

	private long mDiveLogStopId;

	public DiveLogStopLoader(Context context, long diveLogStopId) {
		super(context);
		mDiveLogStopId = diveLogStopId;
	}

	@Override
	public DiveLogStop loadInBackground() {
		return DiveSiteManager.get(getContext()).getDiveLogStop(mDiveLogStopId);
	}
}
