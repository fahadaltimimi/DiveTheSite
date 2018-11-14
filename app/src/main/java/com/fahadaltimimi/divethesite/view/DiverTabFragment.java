package com.fahadaltimimi.divethesite.view;

import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fahadaltimimi.divethesite.R;

public class DiverTabFragment extends Fragment {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	DiverPagesPagerAdapter mDiverPagesPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	PagerTitleStrip mDiveSiteTitleStrip;

	DiverPageFragment mPage1Fragment;
	DiveLogListTabFragment mPage2Fragment;
	DiveSiteListTabFragment mPage3Fragment;
	ScheduledDiveListTabFragment mPage4Fragment;

	public static final String ARG_DIVER_ID = "DIVER_ID";
	public static final String ARG_DIVER_USERNAME = "DIVER_USERNAME";

	private long mDiverID = -1;
	private String mDiverUsername = "";

	public static DiverTabFragment newInstance(long diverID,
			String diverUsername) {
		Bundle args = new Bundle();
		args.putLong(ARG_DIVER_ID, diverID);
		args.putString(ARG_DIVER_USERNAME, diverUsername);
		DiverTabFragment rf = new DiverTabFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check for a Diver ID as an argument
		Bundle args = getArguments();
		if (args != null) {
			long diverID = args.getLong(ARG_DIVER_ID, -1);
			String diverUsername = args.getString(ARG_DIVER_USERNAME, "");

			mDiverID = diverID;
			mDiverUsername = diverUsername;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_diver_tabs, parent,
				false);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mDiverPagesPagerAdapter = new DiverPagesPagerAdapter(getActivity()
				.getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) view.findViewById(R.id.divesite_pages_pager);
		mViewPager.setAdapter(mDiverPagesPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				switch (position) {
				case 0:
					if (mPage1Fragment != null) {
						mPage1Fragment.updateUI();
					}
					break;
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		mDiveSiteTitleStrip = (PagerTitleStrip) view
				.findViewById(R.id.divesite_pages_pager_title_strip);

		return view;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class DiverPagesPagerAdapter extends FragmentPagerAdapter {

		public DiverPagesPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				mPage1Fragment = DiverProfileFragment.newInstance(mDiverID);
				return mPage1Fragment;
			case 1:
				mPage2Fragment = DiveLogListTabFragment.newInstance(mDiverID, null, 0, true);
				return mPage2Fragment;
			case 2:
				mPage3Fragment = DiveSiteListTabFragment.newInstance(mDiverID, false, 0, true);
				return mPage3Fragment;
			case 3:
				mPage4Fragment = ScheduledDiveListTabFragment.newInstance(mDiverID, null, 0, true);
				return mPage4Fragment;
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale locale = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.diver_user_info_title).toUpperCase(locale);
			case 1:
				return getResources().getString(R.string.diver_log_title);
			case 2:
				return getResources().getString(R.string.diver_submitted_sites_title);
			case 3:
				return getResources().getString(R.string.scheduledDiveListTitle).toUpperCase(locale);
			}
			return null;
		}
	}
}
