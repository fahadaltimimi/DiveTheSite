package com.fahadaltimimi.divethesite.data;

import android.content.Context;

import com.fahadaltimimi.data.DataLoader;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;

public class DiveLogBuddyLoader extends DataLoader<DiveLogBuddy> {

	private long mDiveLogBuddyId;

	public DiveLogBuddyLoader(Context context, long diveLogBuddyId) {
		super(context);
		mDiveLogBuddyId = diveLogBuddyId;
	}

	@Override
	public DiveLogBuddy loadInBackground() {
		return DiveSiteManager.get(getContext()).getDiveLogBuddy(
				mDiveLogBuddyId);
	}
}
