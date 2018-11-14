package com.fahadaltimimi.divethesite.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.model.DiveSite;

import java.util.Calendar;

public class ScheduledDiveListTabFragment extends DiveListTabFragment {

	public static ScheduledDiveListTabFragment newInstance(long diverID, DiveSite diveSite, int tabIndex, boolean isChildTabView) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);
		args.putInt(ARG_TAB_INDEX, tabIndex);
        args.putBoolean(ARG_IS_CHILD_VIEW, isChildTabView);
		ScheduledDiveListTabFragment rf = new ScheduledDiveListTabFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,Bundle savedInstanceState) {
        FragmentTabHost tabHost = (FragmentTabHost) super.onCreateView(inflater, parent, savedInstanceState);

        View tabView;

        tabView = createTabView(getActivity(), getResources().getString(R.string.online));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.online)).setIndicator(tabView), ScheduledDiveListOnlineFragment.class, getArguments());
        mOnlineSubtitle1Label = (TextView) tabView.findViewById(R.id.tabSubTitleText1);
        mOnlineSubtitle2Label = (TextView) tabView.findViewById(R.id.tabSubTitleText2);

        tabView = createTabView(getActivity(), getResources().getString(R.string.saved));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.saved)).setIndicator(tabView), ScheduledDiveListLocalFragment.class, getArguments());
        mLocalSubtitle1Label = (TextView) tabView.findViewById(R.id.tabSubTitleText1);
        mLocalSubtitle2Label = (TextView) tabView.findViewById(R.id.tabSubTitleText2);

        // Set Local SubTitle right away with item count
        Long diverID = getArguments().getLong(DiverTabFragment.ARG_DIVER_ID);
        DiveSite diveSite = getArguments().getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
        DiveSiteManager diveSiteManager = DiveSiteManager.get(getActivity());

        SharedPreferences prefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);
        String titleFilter = prefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "").trim();
        String countryFilter = prefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, getResources()
            .getString(R.string.filter_list_all)).trim();
        if (countryFilter.trim().equals(getResources().getString(R.string.filter_list_all))) {
            countryFilter = "";
        }

        String stateFilter = prefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "").trim();
        String cityFilter = prefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "").trim();

        String previousDaysFilter = prefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "").trim();
        String nextDaysFilter = prefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "").trim();

        String timeStampStartFilter = "";
        String timeStampEndFilter = "";

        if (!previousDaysFilter.isEmpty()) {
            Calendar calendarStartFilter = Calendar.getInstance();
            calendarStartFilter.add(Calendar.DAY_OF_YEAR, -(Integer.valueOf(previousDaysFilter)));
            timeStampStartFilter = String.valueOf(calendarStartFilter.getTime().getTime());
        }
        if (!nextDaysFilter.isEmpty()) {
            Calendar calendarEndFilter = Calendar.getInstance();
            calendarEndFilter.add(Calendar.DAY_OF_YEAR, Integer.valueOf(nextDaysFilter));
            timeStampEndFilter = String.valueOf(calendarEndFilter.getTime().getTime());
        }

        Boolean publishedFilter = prefs.getBoolean(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED, true);
        Boolean unPublishedFilter = prefs.getBoolean(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED, true);

        int scheduledDiveCount = 0;
        if (diverID != -1) {
            scheduledDiveCount = diveSiteManager.queryScheduledDiveForUserCount(diverID, diverID,
                publishedFilter, unPublishedFilter,titleFilter, countryFilter,
                stateFilter, cityFilter, timeStampStartFilter, timeStampEndFilter);
        } else if (diveSite != null) {
            scheduledDiveCount = diveSiteManager.queryScheduledDiveForSiteCount(diverID, diveSite.getLocalId(),
                publishedFilter, unPublishedFilter, timeStampStartFilter, timeStampEndFilter);
        } else {
            scheduledDiveCount = diveSiteManager.queryScheduledDiveForSubmitterCount(diverID,
                publishedFilter, unPublishedFilter,titleFilter, countryFilter,
                stateFilter, cityFilter, timeStampStartFilter, timeStampEndFilter);
        }

        updateLocalSubTitles(String.valueOf(scheduledDiveCount), "");

        // If child tab view (i.e. displaying for diver or dive site), need to adjust padding
        boolean isChildTabView = getArguments().getBoolean(ARG_IS_CHILD_VIEW, false);
        if (isChildTabView) {
            tabHost.getChildAt(0).setPadding(tabHost.getPaddingLeft(), 0, tabHost.getPaddingRight(), tabHost.getPaddingBottom());
        }

        tabHost.setCurrentTab(mTabIndexToShow);

        return tabHost;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_scheduleddive_list_tabs, menu);
	}
}
