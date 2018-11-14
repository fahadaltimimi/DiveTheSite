package com.fahadaltimimi.divethesite.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fahadaltimimi.divethesite.R;

/**
 * Created by Fahad on 2015-05-08.
 */
public class DiveListTabFragment extends Fragment {

    public static final String ARG_TAB_INDEX = "ARG_TAB_INDEX";
    protected static final String ARG_IS_CHILD_VIEW = "ARG_IS_CHILD_VIEW";

    public static final int ONLINE_TAB_INDEX = 0;
    public static final int LOCAL_TAB_INDEX = 1;

    protected int mTabIndexToShow = 0;

    private FragmentTabHost mTabHost;

    protected TextView mLocalSubtitle1Label, mLocalSubtitle2Label = null;
    protected TextView mOnlineSubtitle1Label, mOnlineSubtitle2Label = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null) {
            mTabIndexToShow = args.getInt(ARG_TAB_INDEX, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mTabHost = new FragmentTabHost(getActivity());
        inflater.inflate(R.layout.fragment_tabs, mTabHost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);

        return mTabHost;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    protected View createTabView(Context context, String tabText) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_custom, null, false);

        TextView title = (TextView) view.findViewById(R.id.tabTitleText);
        title.setText(tabText.toUpperCase());

        return view;
    }

    public void updateLocalSubTitles(String subTitle1Text, String subTitle2Text) {
        if (mLocalSubtitle1Label != null) {
            mLocalSubtitle1Label.setText(subTitle1Text.trim());
            if (subTitle1Text.trim().isEmpty()) {
                mLocalSubtitle1Label.setVisibility(View.GONE);
            } else {
                mLocalSubtitle1Label.setVisibility(View.VISIBLE);
            }
        }

        if (mLocalSubtitle2Label != null){
            mLocalSubtitle2Label.setText(subTitle2Text.trim());
            if (subTitle2Text.trim().isEmpty()) {
                mLocalSubtitle2Label.setVisibility(View.GONE);
            } else {
                mLocalSubtitle2Label.setVisibility(View.VISIBLE);
            }
        }
    }

    public void updateOnlineSubTitles(String subTitle1Text, String subTitle2Text) {
        if (mOnlineSubtitle1Label != null) {
            mOnlineSubtitle1Label.setText(subTitle1Text.trim());
            if (subTitle1Text.trim().isEmpty()) {
                mOnlineSubtitle1Label.setVisibility(View.GONE);
            } else {
                mOnlineSubtitle1Label.setVisibility(View.VISIBLE);
            }
        }

        if (mOnlineSubtitle2Label != null){
            mOnlineSubtitle2Label.setText(subTitle2Text.trim());
            if (subTitle2Text.trim().isEmpty()) {
                mOnlineSubtitle2Label.setVisibility(View.GONE);
            } else {
                mOnlineSubtitle2Label.setVisibility(View.VISIBLE);
            }
        }
    }
}
