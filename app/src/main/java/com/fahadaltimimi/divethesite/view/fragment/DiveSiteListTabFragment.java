package com.fahadaltimimi.divethesite.view.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;

public class DiveSiteListTabFragment extends DiveListTabFragment {

	public static DiveSiteListTabFragment newInstance(long diverID, boolean setToDiveLog, int tabIndex, boolean isChildTabView) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG, setToDiveLog);
		args.putInt(ARG_TAB_INDEX, tabIndex);
        args.putBoolean(ARG_IS_CHILD_VIEW, isChildTabView);
		DiveSiteListTabFragment rf = new DiveSiteListTabFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        FragmentTabHost tabHost = (FragmentTabHost) super.onCreateView(inflater, parent, savedInstanceState);

        View tabView;

        tabView = createTabView(getActivity(), getResources().getString(R.string.online));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.online)).setIndicator(tabView), DiveSiteListOnlineFragment.class, getArguments());
        mOnlineSubtitle1Label = (TextView) tabView.findViewById(R.id.tabSubTitleText1);
        mOnlineSubtitle2Label = (TextView) tabView.findViewById(R.id.tabSubTitleText2);

        tabView = createTabView(getActivity(), getResources().getString(R.string.saved));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.saved)).setIndicator(tabView), DiveSiteListLocalFragment.class, getArguments());
        mLocalSubtitle1Label = (TextView) tabView.findViewById(R.id.tabSubTitleText1);
        mLocalSubtitle2Label = (TextView) tabView.findViewById(R.id.tabSubTitleText2);

        // Set Local SubTitle right away with item count
        Long diverID = getArguments().getLong(DiverTabFragment.ARG_DIVER_ID);
        Boolean setToDiveLog = getArguments().getBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG);
        DiveSiteManager diveSiteManager = DiveSiteManager.get(getActivity());
        int diveSiteCount = diveSiteManager.queryVisibleDiveSitesCount(
                DiveSiteListLocalFragment.generateFilterSelection(getActivity(), setToDiveLog),
                DiveSiteListLocalFragment.generateFilterSelectionArgs(getActivity(), setToDiveLog),
                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE, diverID);
        updateLocalSubTitles(String.valueOf(diveSiteCount), "");

        // If child tab view (i.e. displaying for diver), need to adjust padding
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
		inflater.inflate(R.menu.fragment_divesite_list_tabs, menu);
	}
}
