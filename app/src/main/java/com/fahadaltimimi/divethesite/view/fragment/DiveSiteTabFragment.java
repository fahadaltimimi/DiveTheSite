package com.fahadaltimimi.divethesite.view.fragment;

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

import com.fahadaltimimi.divethesite.view.fragment.DiveSitePageFragment.DiveSitePageListener;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;

public class DiveSiteTabFragment extends Fragment {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	DiveSitePagesPagerAdapter mDiveSitePagesPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	PagerTitleStrip mDiveSiteTitleStrip;

	DiveSitePageFragment mPage1Fragment, mPage2Fragment;
	DiveLogListTabFragment mPage3Fragment;
	ScheduledDiveListTabFragment mPage4Fragment;

	public static final String ARG_DIVESITE = "DIVESITE";

	private DiveSite mDiveSite = null;
	private DiveLog mDiveLog = null;

	public static final int DIVE_SITE_CREATED = 1;

	public static DiveSiteTabFragment newInstance(DiveLog diveLog,
			DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_DIVESITE, diveSite);
		args.putParcelable(DiveLogFragment.ARG_DIVELOG, diveLog);
		DiveSiteTabFragment rf = new DiveSiteTabFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Check for a Dive Site ID as an argument, and find the Dive Site and
		// Pictures
		Bundle args = getArguments();
		if (args != null) {
			mDiveSite = args.getParcelable(ARG_DIVESITE);
			mDiveLog = args.getParcelable(DiveLogFragment.ARG_DIVELOG);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_divesite_tabs, parent,
				false);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mDiveSitePagesPagerAdapter = new DiveSitePagesPagerAdapter(
				getActivity().getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) view.findViewById(R.id.divesite_pages_pager);
		mViewPager.setAdapter(mDiveSitePagesPagerAdapter);
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

				case 1:
					if (mPage2Fragment != null) {
						mPage2Fragment.updateUI();
					}
					break;
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub

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
	public class DiveSitePagesPagerAdapter extends FragmentPagerAdapter {

		public DiveSitePagesPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				mPage1Fragment = DiveSiteInfoPageFragment.newInstance(mDiveSite, mDiveLog);
				mPage1Fragment
						.setOnDiveSiteChangedListener(new DiveSitePageListener() {

							@Override
							public void OnDiveSiteChangedListener(
									DiveSite newDiveSite) {
								if (newDiveSite != null
										&& !mDiveSite.equals(newDiveSite)) {
									mPage2Fragment.getArguments().putParcelable(
											ARG_DIVESITE, newDiveSite);
									mPage2Fragment.setDiveSite(newDiveSite);
								}
							}

						});

				return mPage1Fragment;

			case 1:
				mPage2Fragment = DiveSitePicturePageFragment.newInstance(
						mDiveSite, mDiveLog);
				mPage2Fragment
						.setOnDiveSiteChangedListener(new DiveSitePageListener() {

							@Override
							public void OnDiveSiteChangedListener(
									DiveSite newDiveSite) {
								if (newDiveSite != null
										&& !mDiveSite.equals(newDiveSite)) {
									mPage1Fragment.getArguments().putParcelable(
											ARG_DIVESITE, newDiveSite);
									mPage1Fragment.setDiveSite(newDiveSite);
								}
							}

						});

				return mPage2Fragment;

			case 2:
				mPage3Fragment = DiveLogListTabFragment.newInstance(-1, mDiveSite, 0, true);
				return mPage3Fragment;
				
			case 3:
				mPage4Fragment = ScheduledDiveListTabFragment.newInstance(-1, mDiveSite, 0, true);
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
				return getString(R.string.divesite_details_title).toUpperCase(
						locale);
			case 1:
				return getString(R.string.divesite_pictures_title).toUpperCase(
						locale);
			case 2:
				return getString(R.string.divesite_logs_title).toUpperCase(
						locale);
			case 3:
				return getString(R.string.scheduledDiveListTitle).toUpperCase(
						locale);
			}
			return null;
		}
	}
}
