package com.fahadaltimimi.divethesite.data;

import android.content.Context;

import com.fahadaltimimi.data.DataLoader;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveLog;

public class DiveLogLoader extends DataLoader<DiveLog> {

	private long mDiveLogId;

	public DiveLogLoader(Context context, long diveLogId) {
		super(context);
		mDiveLogId = diveLogId;
	}

	@Override
	public DiveLog loadInBackground() {
		return DiveSiteManager.get(getContext()).getDiveLog(mDiveLogId);
	}
}
