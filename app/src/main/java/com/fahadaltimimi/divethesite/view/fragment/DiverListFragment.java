package com.fahadaltimimi.divethesite.view.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.controller.ListViewHelper;
import com.fahadaltimimi.divethesite.data.DiveCursorLoaders.DiverListCursorLoader;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.view.activity.DiverActivity;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.DiverCertification;

public class DiverListFragment extends ListFragment {

	private double LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT = 0.75;

	protected DiveSiteManager mDiveSiteManager;
	private Bundle mSavedInstanceState;

	private DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;
	
	private ListView mListView = null;
	private int mAdditionalItemsToLoad = 0;
	
	private View mLastDisplayedListItemView = null;

	private MenuItem mRefreshMenuItem = null;

	private LinearLayout mListFilter = null;
	private LinearLayout mFilterNotificationContainer;
	private TextView mFilterNotification;
	private EditText mFilterName, mFilterState, mFilterCity;
	private Spinner mFilterCountry;
	private Button mFilterClear, mFilterClose;

	private SharedPreferences mPrefs;
	private Date mLastDiverRefreshTime;

	private boolean mSetToDiveLog = false;
	private Diver mSelectedDiver = null;

	private DiverListCursorLoader mDiverListLoader = null;

	private Boolean mRefreshingOnlineDivers = false;

	private HashMap<Long, Integer> mDiverListItemLoaderIDs = new HashMap<Long, Integer>();
	private ArrayList<LoadOnlineImageTask> mDiverTasks = new ArrayList<LoadOnlineImageTask>();
	private LruCache<Long, Bitmap> mDiverProfileImageCache;

	private int ONLINE_FILTER_COUNT = 4;
	private int ONLINE_FILTER_NAME_INDEX = 0;
	private int ONLINE_FILTER_COUNTRY_INDEX = 1;
	private int ONLINE_FILTER_STATE_INDEX = 2;
	private int ONLINE_FILTER_CITY_INDEX = 3;

	private String[] mLastOnlineFilter = new String[ONLINE_FILTER_COUNT];

	public static DiverListFragment newInstance(boolean setToDiveLog) {
		Bundle args = new Bundle();
		args.putBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG, setToDiveLog);
		DiverListFragment rf = new DiverListFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
        setRetainInstance(true);

		mSavedInstanceState = savedInstanceState;
		mDiveSiteManager = DiveSiteManager.get(getActivity());

		DiverAdapter adapter = new DiverAdapter(new ArrayList<Diver>());
		setListAdapter(adapter);

		mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);
		mLastDiverRefreshTime = new Date(mPrefs.getLong(
				DiveSiteManager.PREF_LAST_DIVERS_REFRESH_DATE, 0));

		Bundle args = getArguments();
		if (args != null) {
			mSetToDiveLog = args.getBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG,
					false);
		}

		// Get max available memory, exceeding it will cause Out of Memory error
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Using 1/8th max memory for this cache
		final int cacheSize = maxMemory / 8;

		mDiverProfileImageCache = new LruCache<Long, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(Long key, Bitmap bitmap) {
				// Cache size measured in kilobytes rather than number of items
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_diver_list, parent, false);

		getActivity().setTitle(R.string.diverListTitle);

		mListView = v.findViewById(android.R.id.list);
		mListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// If we reached a certain point in list and not refreshing, refresh online sites
					// If items were loaded before but list view is empty, view is currently loading, 
					//  so don't refresh
				if (!(mAdditionalItemsToLoad > 0 && mListView.getCount() == 0) &&
					!mRefreshingOnlineDivers && 
						firstVisibleItem + visibleItemCount >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * totalItemCount) {
					refreshOnlineDivers();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mFilterNotificationContainer = v
				.findViewById(R.id.diver_list_filter_notification_container);
		mFilterNotification = v
				.findViewById(R.id.diver_list_filter_notification);

		// Initialize filter panel
		mListFilter = v.findViewById(R.id.diver_list_filter);

		mFilterName = mListFilter
				.findViewById(R.id.diver_list_filter_name);
		mFilterCountry = mListFilter
				.findViewById(R.id.diver_list_filter_country);
		mFilterState = mListFilter
				.findViewById(R.id.diver_list_filter_state);
		mFilterCity = mListFilter
				.findViewById(R.id.diver_list_filter_city);
		mFilterClear = mListFilter
				.findViewById(R.id.diver_list_clear_filter);
		mFilterClose = mListFilter
				.findViewById(R.id.diver_list_close_filter);

		mFilterName.setText(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_NAME, ""));
		mFilterName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String mFilterName = c.toString().trim();
				mPrefs.edit()
						.putString(DiveSiteManager.PREF_FILTER_DIVER_NAME,
								mFilterName).apply();

				filterDiverList();
			}

			@Override
			public void afterTextChanged(Editable s) {
				//
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				//
			}
		});

		// Initialize values and modify first blank entry to read 'All'
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item,
				android.R.id.text1);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerAdapter.addAll(getResources().getStringArray(
				R.array.countries_array));

		mFilterCountry.setAdapter(spinnerAdapter);
		spinnerAdapter.remove("");
		spinnerAdapter.insert(getResources()
				.getString(R.string.filter_list_all), 0);
		spinnerAdapter.notifyDataSetChanged();

		// Set selected country
		String currentFilterCountry = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_COUNTRY, getResources()
						.getString(R.string.filter_list_all));
		for (int i = 0; i < spinnerAdapter.getCount(); i++) {
			if (spinnerAdapter.getItem(i).toString()
					.equals(currentFilterCountry)) {
				mFilterCountry.setSelection(i);
				break;
			}
		}

		// Set list to reload after changing text
		mFilterCountry.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View v,
					int position, long id) {
				// Save selection and reset list view
				String filterCountry = (String) parentView
						.getItemAtPosition(position);

				if (filterCountry.isEmpty()) {
					filterCountry = getResources().getString(
							R.string.filter_list_all);
				}
				mPrefs.edit()
						.putString(DiveSiteManager.PREF_FILTER_DIVER_COUNTRY,
								filterCountry).apply();

				filterDiverList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Nothing to do in this case
			}

		});

		mFilterState.setText(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_STATE, ""));
		mFilterState.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterState = c.toString().trim();
				mPrefs.edit()
						.putString(DiveSiteManager.PREF_FILTER_DIVER_STATE,
								filterState).apply();

				filterDiverList();
			}

			@Override
			public void afterTextChanged(Editable s) {
				//
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				//
			}
		});

		mFilterCity.setText(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_CITY, ""));
		mFilterCity.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String mFilterCity = c.toString().trim();
				mPrefs.edit()
						.putString(DiveSiteManager.PREF_FILTER_DIVER_CITY,
								mFilterCity).apply();

				filterDiverList();
			}

			@Override
			public void afterTextChanged(Editable s) {
				//
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				//
			}
		});

		mFilterClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Clear all filters
				mFilterName.setText("");
				mFilterCountry.setSelection(0);
				mFilterState.setText("");
				mFilterCity.setText("");
			}
		});

		mFilterClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListFilter.setVisibility(View.GONE);
			}
		});

		updateFilterNotification();

		refreshOnlineDivers();

		return v;
	}

	@Override
	public void onStop() {
		super.onStop();

		// Cancel running tasks
		if (mDiveSiteOnlineDatabase != null && !mDiveSiteOnlineDatabase.isCancelled()) {
			mDiveSiteOnlineDatabase.stopBackground();
			mDiveSiteOnlineDatabase.cancel(true);
		}

		for (int i = 0; i < mDiverTasks.size(); i++) {
			mDiverTasks.get(i).cancel(true);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mLastDisplayedListItemView != v) {
            Diver diver = ((DiverAdapter) getListAdapter()).getItem(position);

            // Open Diver Page if not selecting for Log
            if (!mSetToDiveLog) {
                openDiver(diver);
            } else {
                if (mLastDisplayedListItemView != null) {
                    mLastDisplayedListItemView.setBackgroundColor(getResources().getColor(R.color.itemPublished));
                }
                setSelectedDiver(diver);

                mLastDisplayedListItemView = v;
                v.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
            }

		} else {
			// Set last view to null so if we reclick it will be selected
			v.setBackgroundColor(getResources().getColor(
					R.color.diveSiteSelected));
			mLastDisplayedListItemView = null;
			setSelectedDiver(null);
		}
	}

	private void setSelectedDiver(Diver diver) {
		if (mSelectedDiver != diver) {
			mSelectedDiver = diver;
			getActivity().invalidateOptionsMenu();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_diver_list, menu);

		mRefreshMenuItem = menu.findItem(R.id.menu_item_refresh_divers);

		if (mRefreshingOnlineDivers) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem selectDiveSiteMenuItem = menu.findItem(R.id.menu_item_select_diver);
		selectDiveSiteMenuItem.setVisible(mSetToDiveLog && mSelectedDiver != null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_select_diver:
			if (mSelectedDiver != null) {
				// Save local and online id's and finish activity
				Intent intent = new Intent(getActivity(), DiveLogActivity.class);
				intent.putExtra(DiveSiteManager.EXTRA_DIVER_ONLINE_ID,
						mSelectedDiver.getOnlineId());
				intent.putExtra(DiveSiteManager.EXTRA_DIVER_USERNAME,
						mSelectedDiver.getUsername());
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
			}
			return true;

		case R.id.menu_item_filter_diver_list:
			if (mListFilter.getVisibility() == View.GONE) {
				mListFilter.setVisibility(View.VISIBLE);
			} else {
				mListFilter.setVisibility(View.GONE);
			}

			return true;

		case R.id.menu_item_refresh_divers:
            clearDivers();
			refreshOnlineDivers();
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	private void updateFilterNotification() {
		// Determine current filter
		String nameFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_NAME, "").trim();
		String countryFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_COUNTRY, "").trim();
		String stateFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_STATE, "").trim();
		String cityFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_CITY, "").trim();

		ArrayList<String> filterNotification = new ArrayList<String>();
		if (!nameFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !nameFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_name)
					+ " " + nameFilter);
		}
		if (!countryFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !countryFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_country)
					+ " " + countryFilter);
		}
		if (!stateFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !stateFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_state)
					+ " " + stateFilter);
		}
		if (!cityFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !cityFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_city)
					+ " " + cityFilter);
		}

		if (filterNotification.size() == 0) {
			mFilterNotificationContainer.setVisibility(View.GONE);
		} else {
			mFilterNotificationContainer.setVisibility(View.VISIBLE);
		}

		mFilterNotification.setText(filterNotification.toString());
	}

	private void refreshOnlineDivers() {
		// Look for more divers and set menu item icon to spin
		String nameFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_NAME, "").trim();
		String countryFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_COUNTRY, "").trim();
		if (countryFilter.equals(getResources().getString(
				R.string.filter_list_all))) {
			countryFilter = "";
		}
		String stateFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_STATE, "").trim();
		String cityFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_CITY, "").trim();

		mLastOnlineFilter[ONLINE_FILTER_NAME_INDEX] = nameFilter;
		mLastOnlineFilter[ONLINE_FILTER_COUNTRY_INDEX] = countryFilter;
		mLastOnlineFilter[ONLINE_FILTER_STATE_INDEX] = stateFilter;
		mLastOnlineFilter[ONLINE_FILTER_CITY_INDEX] = cityFilter;

		if (mRefreshingOnlineDivers) {
			cancelOnlineRefresh();
		}
		
		mRefreshingOnlineDivers = true;
		
		if (mRefreshMenuItem != null){
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}

		mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mDiveSiteOnlineDatabase
				.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {

						if (getActivity() != null && message != null
								&& !message.isEmpty()) {
							Toast.makeText(getActivity(), message,
									Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineDivers = false;

						if (mRefreshMenuItem != null) {
							mRefreshMenuItem.setActionView(null);
						}

						mAdditionalItemsToLoad = ListViewHelper.additionalItemCountToLoad(mListView);
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSiteOnlineDatabase.getActive()
								&& getActivity() != null) {
							Diver diver = getDiverOnlineId((Diver) result);
							if (diver == null) {
								diver = (Diver) result;
								((DiverAdapter) getListAdapter()).add(diver);
							} else {
								int index = getDiverIndex(diver);
								((DiverAdapter) getListAdapter()).remove(diver);
								((DiverAdapter) getListAdapter()).insert(
										(Diver) result, index);
							}

							refreshDiverList();
						} else {
							mDiveSiteOnlineDatabase.stopBackground();
							mDiveSiteOnlineDatabase.cancel(true);
						}
					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						//
					}
				});
		
		if (mAdditionalItemsToLoad == 0) {
			mDiveSiteOnlineDatabase.getDiverList(new Date(0), nameFilter,
					countryFilter, stateFilter, cityFilter, "", "");
		} else {
			mDiveSiteOnlineDatabase.getDiverList(new Date(0), nameFilter,
					countryFilter, stateFilter, cityFilter, 
					String.valueOf(mListView.getCount()),
					String.valueOf(mAdditionalItemsToLoad));
		}
	}

	private int getDiverIndex(Diver diver) {
		int index = -1;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (((DiverAdapter) getListAdapter()).getItem(i).getOnlineId() == diver
					.getOnlineId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private Diver getDiverOnlineId(Diver diver) {
		Diver diverDuplicate = null;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (((DiverAdapter) getListAdapter()).getItem(i).getOnlineId() == diver
					.getOnlineId()) {
				diverDuplicate = ((DiverAdapter) getListAdapter()).getItem(i);
				break;
			}
		}
		return diverDuplicate;
	}

	private void refreshDiverList() {
		updateFilterNotification();
		((DiverAdapter) getListAdapter()).notifyDataSetChanged();

		if (mAdditionalItemsToLoad == 0) {
			// Check if limit reached and still loading online
			int lastItemPosition = mListView.getLastVisiblePosition();
			if (lastItemPosition >= 0) {

				if (!ListViewHelper.shouldRefreshAdditionalListViewItems(mListView)) {
					cancelOnlineRefresh();
				}
			}
		}
	}

	protected void filterDiverList() {
		if (onlineFilterChanged()) {
			// Filter changed
			clearDivers();
			refreshOnlineDivers();
		}
	}
	
	private void clearDivers() {
		// Clears list and resets adapter
		cancelOnlineRefresh();
		mAdditionalItemsToLoad = 0;
		
		((DiverAdapter) getListAdapter()).clear();
		DiverAdapter adapter = new DiverAdapter(new ArrayList<Diver>());
		setListAdapter(adapter);
		((DiverAdapter) getListAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineRefresh() {
		if (mDiveSiteOnlineDatabase != null && mDiveSiteOnlineDatabase.getActive()) {
			mDiveSiteOnlineDatabase.stopBackground();
			mDiveSiteOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineDivers = false;
		
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	protected boolean onlineFilterChanged() {
		String nameFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_NAME, "").trim();
		String countryFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_COUNTRY, "").trim();
		if (countryFilter.equals(getResources().getString(
				R.string.filter_list_all))) {
			countryFilter = "";
		}

		String stateFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_STATE, "").trim();
		String cityFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVER_CITY, "").trim();

		return !mLastOnlineFilter[ONLINE_FILTER_NAME_INDEX].equals(nameFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_COUNTRY_INDEX]
						.equals(countryFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_STATE_INDEX]
						.equals(stateFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_CITY_INDEX]
						.equals(cityFilter);
	}

    private void openDiver(Diver diver) {
        Intent i = new Intent(DiverListFragment.this.getActivity(), DiverActivity.class);

        long diver_id = diver.getOnlineId();
        String diver_username = diver.getUsername();
        i.putExtra(DiverActivity.EXTRA_DIVER_ID, diver_id);
        i.putExtra(DiverActivity.EXTRA_DIVER_USERNAME, diver_username);
        startActivity(i);
    }

	private class DiverAdapter extends ArrayAdapter<Diver> {

		public DiverAdapter(ArrayList<Diver> divers) {
			super(getActivity(), 0, divers);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			Diver diver = getItem(position);

			// If we weren't given a view, inflate one using the layout we
			// created for each list item
			if (view == null) {
				view = getActivity().getLayoutInflater().inflate(
						R.layout.diver_list_item, parent, false);
			}

			if (mDiverListItemLoaderIDs.get(diver.getOnlineId()) == null) {
				mDiverListItemLoaderIDs.put(diver.getOnlineId(),
						mDiverListItemLoaderIDs.size());
			}

			// Set up the view with the divers info
			view.setBackgroundColor(getResources().getColor(
					R.color.itemPublished));

			// Set profile image here
			ImageButton diverProfileImage = view
					.findViewById(R.id.diver_item_picture);
			diverProfileImage.setImageResource(R.drawable.logo_symbol);
			diverProfileImage.setTag(diver);

			if (mDiverProfileImageCache.get(diver.getOnlineId()) == null) {
				if (!diver.getPictureURL().trim().isEmpty()) {
					LoadOnlineImageTask task = new LoadOnlineImageTask(
							diverProfileImage) {

						@Override
						protected void onPostExecute(Bitmap result) {
							super.onPostExecute(result);
							Diver diver = (Diver) getTag();
							mDiverProfileImageCache.put(diver.getOnlineId(),
									result);
						}
					};
					task.setTag(diver);
					task.execute(diver.getPictureURL());
				}
			} else {
				diverProfileImage.setImageBitmap(mDiverProfileImageCache
						.get(diver.getOnlineId()));
			}

			// Profile Image button should open diver's profile page
			diverProfileImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Diver diver = (Diver) v.getTag();
					openDiver(diver);
				}
			});

			// Diver Username
			TextView diverUsername = view
					.findViewById(R.id.diver_item_username);
			diverUsername.setText(diver.getUsername());

			// Diver Fullname
			TextView diverName = view
					.findViewById(R.id.diver_item_fullname);
			diverName.setText(diver.getFirstName() + " " + diver.getLastName());

			// Diver Location
			TextView diverLocation = view
					.findViewById(R.id.diver_item_location);
			diverLocation.setText(diver.getFullLocation());

			// Diver's Log Count
			TextView diverLogCount = view
					.findViewById(R.id.diver_item_log_count);
			diverLogCount.setText(String.valueOf(diver.getLogCount()));

			// Diver's Dive Site Submitted Count
			TextView diverDiveSiteSubmittedCount = view
					.findViewById(R.id.diver_item_sites_count);
			diverDiveSiteSubmittedCount.setText(String.valueOf(diver
					.getDiveSiteSubmittedCount()));

			// Diver's Certifications
			for (int i = 0; i < diver.getCertifications().size(); i++) {
				DiverCertification cert = diver.getCertifications().get(i);
				// Diver's Primary Certification (last one added)
				TextView diverPrimaryCertification = view
						.findViewById(R.id.diver_item_primary_certification);

				if (cert.getPrimary()) {
					diverPrimaryCertification.setText(cert.getCertifTitle());
				}
			}

			return view;
		}
	}
}
