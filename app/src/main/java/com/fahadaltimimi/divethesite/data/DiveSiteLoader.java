package com.fahadaltimimi.divethesite.data;

import android.content.Context;

import com.fahadaltimimi.data.DataLoader;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveSite;

public class DiveSiteLoader extends DataLoader<DiveSite> {

	private long mDiveSiteId;

	public DiveSiteLoader(Context context, long diveSiteId) {
		super(context);
		mDiveSiteId = diveSiteId;
	}

	@Override
	public DiveSite loadInBackground() {
		return DiveSiteManager.get(getContext()).getDiveSite(mDiveSiteId);
	}
}
