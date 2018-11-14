package com.fahadaltimimi.divethesite.view;

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
import com.fahadaltimimi.divethesite.model.DiveSite;

public class DiveLogListTabFragment extends DiveListTabFragment {

	public static DiveLogListTabFragment newInstance(long diverID, DiveSite diveSite, int tabIndex, boolean isChildTabView) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);
		args.putInt(ARG_TAB_INDEX, tabIndex);
        args.putBoolean(ARG_IS_CHILD_VIEW, isChildTabView);
		DiveLogListTabFragment rf = new DiveLogListTabFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        FragmentTabHost tabHost = (FragmentTabHost) super.onCreateView(inflater, parent, savedInstanceState);

        View tabView;

        tabView = createTabView(getActivity(), getResources().getString(R.string.online));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.online)).setIndicator(tabView), DiveLogListOnlineFragment.class, getArguments());
        mOnlineSubtitle1Label = tabView.findViewById(R.id.tabSubTitleText1);
        mOnlineSubtitle2Label = tabView.findViewById(R.id.tabSubTitleText2);

        tabView = createTabView(getActivity(), getResources().getString(R.string.saved));
        tabHost.addTab(tabHost.newTabSpec(getResources().getString(R.string.saved)).setIndicator(tabView), DiveLogListLocalFragment.class, getArguments());
        mLocalSubtitle1Label = tabView.findViewById(R.id.tabSubTitleText1);
        mLocalSubtitle2Label = tabView.findViewById(R.id.tabSubTitleText2);

        // Set Local SubTitle right away with item count
        Long diverID = getArguments().getLong(DiverTabFragment.ARG_DIVER_ID);
        DiveSite diveSite = getArguments().getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
        DiveSiteManager diveSiteManager = DiveSiteManager.get(getActivity());
        int diveLogCount = diveSiteManager.queryDiveLogsCount(diverID, diveSite, false);
        int diveLogTotalMinutes = diveSiteManager.queryDiveLogsTotalMinutes(diverID, diveSite, false);

        int hours = diveLogTotalMinutes / 60;
        int minutes = diveLogTotalMinutes % 60;

        updateLocalSubTitles(String.valueOf(diveLogCount),
            String.format(getActivity().getResources().getString(R.string.time_format), hours, minutes));

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
		inflater.inflate(R.menu.fragment_divelog_list_tabs, menu);
	}
}
