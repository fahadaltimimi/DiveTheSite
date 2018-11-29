package com.fahadaltimimi.divethesite.view.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.controller.ListViewHelper;
import com.fahadaltimimi.controller.LocationController;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.view.activity.DiverActivity;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.ScheduledDiveDiveSite;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.fahadaltimimi.divethesite.model.ScheduledDiveUser;

public class ScheduledDiveListOnlineFragment extends ScheduledDiveListFragment {
	
	private int ONLINE_FILTER_COUNT = 6;
	private int ONLINE_FILTER_TITLE_INDEX = 0;
	private int ONLINE_FILTER_COUNTRY_INDEX = 1;
	private int ONLINE_FILTER_STATE_INDEX = 2;
	private int ONLINE_FILTER_CITY_INDEX = 3;
	private int ONLINE_FILTER_TIMESTAMP_START_INDEX = 4;
	private int ONLINE_FILTER_TIMESTAMP_END_INDEX = 5;

	private double LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT = 0.60;

	private int mAdditionalItemsToLoad = 0;
	
	private String[] mLastOnlineFilter = new String[ONLINE_FILTER_COUNT];
	
	private Boolean mRefreshingOnlineScheduledDive = false;

    private Boolean mForceLocationDataRefresh = false;

	public static ScheduledDiveListOnlineFragment newInstance(long diverID, DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);

		ScheduledDiveListOnlineFragment rf = new ScheduledDiveListOnlineFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ScheduledDiveAdapter adapter = new ScheduledDiveAdapter(new ArrayList<ScheduledDive>());
		setListAdapter(adapter);

		mProgressDialog = new ProgressDialog(getActivity());
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
							 Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		// Register list view with context menu
        ListView listView = Objects.requireNonNull(v).findViewById(android.R.id.list);

        listView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    setSnapshot(View.INVISIBLE);
                } else {
                    setSnapshot(View.VISIBLE);
                }

                if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    // If we reached a certain point in list and not refreshing, refresh online sites
                    // If items were loaded before but list view is empty, view is currently loading,
                    //  so don't refresh
                    if (!(mAdditionalItemsToLoad > 0 && getListView().getCount() == 0) &&
                            !mRefreshingOnlineScheduledDive &&
                            view.getLastVisiblePosition() >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * view.getCount()) {
                        refreshOnlineScheduledDives();
                    }
                }
			}
			
		});

        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(final ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.menu_item_save_scheduleddive:
					// Save selected items
					mProgressDialog.setMessage(getString(R.string.save_scheduleddives_progress));
					mProgressDialog.setCancelable(false);
					mProgressDialog.setIndeterminate(false);
					mProgressDialog.setProgress(0);
					mProgressDialog.setMax(getListView().getCheckedItemCount());
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					mProgressDialog.show();

					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							ScheduledDiveAdapter adapter = (ScheduledDiveAdapter) getListView().getAdapter();
							ScheduledDive scheduledDive = adapter.getItem(i);

							// Save sites for each scheduled dive
							for (int j = 0; j < Objects.requireNonNull(scheduledDive).getScheduledDiveDiveSites().size(); j++) {
								DiveSite diveSite = scheduledDive.getScheduledDiveDiveSites().get(j).getDiveSite();
								diveSite.setLocalId(mDiveSiteManager.getDiveSiteLocalId(diveSite.getOnlineId()));
								if (diveSite != null) {
									mDiveSiteManager.saveDiveSite(diveSite);
									scheduledDive.getScheduledDiveDiveSites().get(j).setDiveSiteLocalId(diveSite.getLocalId());
								}
							}
							
							scheduledDive.setLocalId(mDiveSiteManager.getScheduledDiveLocalId(scheduledDive.getOnlineId()));
							mDiveSiteManager.saveScheduledDive(scheduledDive);

							mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
							if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
								mProgressDialog.dismiss();
							}
						}
					}

					Toast.makeText(getActivity(),R.string.scheduleddives_saved_message, Toast.LENGTH_LONG).show();

					mode.finish();

					return true;

				case R.id.menu_item_share_scheduleddive:
					// Get summary for each scheduled dive selected
					String combinedSummary = 
						getResources().getString(R.string.share_title_scheduleddive) + "\n\n";
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							ScheduledDive scheduledDive = (ScheduledDive) getListAdapter().getItem(i);
							combinedSummary = combinedSummary + scheduledDive.getShareSummary() + "\n\n";
						}
					}
					
					// Share info to what user selects
					Intent share = new Intent(android.content.Intent.ACTION_SEND);
			        share.setType("text/plain");
			        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			        share.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title_scheduleddive));
			        share.putExtra(Intent.EXTRA_TEXT, combinedSummary.trim());
			 
			        startActivity(Intent.createChooser(share, "Share Scheduled Dive!"));
					
			        mode.finish();
			        
					return true;
					
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.fragment_scheduleddive_list_online_contextual, menu);

				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Required but not used in this implementation
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
				// Required but not used in this implementation
			}

			@Override
			public void onItemCheckedStateChanged(ActionMode mode,
					int position, long id, boolean checked) {
				// Required but not used in this implementation
			}

		});

		// Hide published, unpublished filter for online fragment
		CheckBox filterPublished = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_published);
		filterPublished.setVisibility(View.GONE);

		CheckBox filterUnpublished = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_unpublished);
		filterUnpublished.setVisibility(View.GONE);

		return v;
	}

    @Override
    public void onResume() {
        super.onResume();

        if (ListViewHelper.shouldRefreshAdditionalListViewItems(getListView())) {
            refreshOnlineScheduledDives();
        }
    }

    @Override
	public void onStop() {
		super.onStop();
		cancelOnlineRefresh();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_scheduleddive_list_online, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			
		case R.id.menu_item_filter_scheduleddive_list:
			if (mListFilter.getVisibility() == View.GONE) {
				mListFilter.setVisibility(View.VISIBLE);
			} else {
				mListFilter.setVisibility(View.GONE);
			}

			return true;
			
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void updateFilterNotification() {
		// Determine filter to set from selection
		String titleFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "")).trim();
		String countryFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, "")).trim();
		String stateFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "")).trim();
		String cityFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "")).trim();

		String previousDaysFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "")).trim();
		String nextDaysFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "")).trim();

		ArrayList<String> filterNotification = new ArrayList<String>();

		if (!titleFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !titleFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_title)
					+ " " + titleFilter);
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
		if (!previousDaysFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !previousDaysFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_daysPrevious)
					+ " " + previousDaysFilter);
		}
		if (!nextDaysFilter.equals(getResources().getString(
				R.string.filter_list_all))
				&& !nextDaysFilter.isEmpty()) {
			filterNotification.add(getResources().getString(
					R.string.filter_daysNext)
					+ " " + nextDaysFilter);
		}

		if (filterNotification.size() == 0) {
			mFilterNotificationContainer.setVisibility(View.GONE);
		} else {
			mFilterNotificationContainer.setVisibility(View.VISIBLE);
		}

		mFilterNotification.setText(filterNotification.toString());
	}

	@Override
    protected void refreshListView() {
        if (canRequestLocationUpdates()) {
            mForceLocationDataRefresh = true;
            if (!startLocationUpdates()) {
                clearScheduledDives();
                refreshOnlineScheduledDives();
            }
        } else {
            clearScheduledDives();
            refreshOnlineScheduledDives();
        }
    }

	private void refreshOnlineScheduledDives() {
		if (mRefreshingOnlineScheduledDive) {
			cancelOnlineRefresh();
		}

        startRefresh();
		mRefreshingOnlineScheduledDive = true;

		String titleFilter;
		titleFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "")).trim();

		String countryFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, "")).trim();
		if (countryFilter.equals(getResources().getString(R.string.filter_list_all))) {
			countryFilter = "";
		}

		String stateFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "")).trim();
		String cityFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "")).trim();
		
		String previousDaysFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "")).trim();
		String nextDaysFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "")).trim();
		
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

		mLastOnlineFilter[ONLINE_FILTER_TITLE_INDEX] = titleFilter;
		mLastOnlineFilter[ONLINE_FILTER_COUNTRY_INDEX] = countryFilter;
		mLastOnlineFilter[ONLINE_FILTER_STATE_INDEX] = stateFilter;
		mLastOnlineFilter[ONLINE_FILTER_CITY_INDEX] = cityFilter;
		mLastOnlineFilter[ONLINE_FILTER_TIMESTAMP_START_INDEX] = previousDaysFilter;
		mLastOnlineFilter[ONLINE_FILTER_TIMESTAMP_END_INDEX] = nextDaysFilter;
		
		mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {

						if (getActivity() != null && message != null
								&& !message.isEmpty()) {
							Toast.makeText(getActivity(), message,
									Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineScheduledDive = false;

                        stopRefresh();
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSiteOnlineDatabase.getActive()) {
							ScheduledDive scheduledDive = 
									getScheduledDive(((ScheduledDive) result).getOnlineId());
							if (scheduledDive == null) {
								((ScheduledDiveAdapter) getListAdapter()).add((ScheduledDive) result);
							} else {
								int index = getScheduledDiveIndex(scheduledDive);
								((ScheduledDiveAdapter) getListAdapter()).remove(scheduledDive);
								((ScheduledDiveAdapter) getListAdapter()).insert((ScheduledDive) result, index);
							}

							refreshScheduledDiveList();

						}
					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						//
					}
				});

		long diveSiteID = -1;
		if (mDiveSite != null) {
			diveSiteID = mDiveSite.getOnlineId();
		}
		
		if (getLocation() != null) {
			if (mAdditionalItemsToLoad == 0) {
				mDiveSiteOnlineDatabase.getScheduledDiveList(new Date(0),
						mRestrictToDiverID, mRestrictToDiverID, diveSiteID,
						String.valueOf(getLocation().getLatitude()), 
						String.valueOf(getLocation().getLongitude()), 
						titleFilter, countryFilter, stateFilter, cityFilter, "", "",
						timeStampStartFilter, timeStampEndFilter, "", "", "");
			} else {
				mDiveSiteOnlineDatabase.getScheduledDiveList(new Date(0),
						mRestrictToDiverID, mRestrictToDiverID, diveSiteID,
						String.valueOf(getLocation().getLatitude()), 
						String.valueOf(getLocation().getLongitude()),
						titleFilter, countryFilter, stateFilter, cityFilter, "", "",
						timeStampStartFilter, timeStampEndFilter, "",
						String.valueOf(getListView().getCount()),
						String.valueOf(mAdditionalItemsToLoad));
			}
		} else {
			if (mAdditionalItemsToLoad == 0) {
				mDiveSiteOnlineDatabase.getScheduledDiveList(new Date(0),
						mRestrictToDiverID, mRestrictToDiverID, diveSiteID,
						"", "", titleFilter, countryFilter, stateFilter, cityFilter, "", "",
						timeStampStartFilter, timeStampEndFilter, "", "", "");
			} else {
				mDiveSiteOnlineDatabase.getScheduledDiveList(new Date(0),
						mRestrictToDiverID, mRestrictToDiverID, diveSiteID,
						"", "", titleFilter, countryFilter, stateFilter, cityFilter, "", "",
						timeStampStartFilter, timeStampEndFilter, "",
						String.valueOf(getListView().getCount()),
						String.valueOf(mAdditionalItemsToLoad));
			}
		}
	}

	private int getScheduledDiveIndex(ScheduledDive scheduledDive) {
		int index = -1;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (Objects.requireNonNull(((ScheduledDiveAdapter) getListAdapter()).getItem(i)).getOnlineId() ==
					scheduledDive.getOnlineId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private ScheduledDive getScheduledDive(long scheduledDiveOnlineId) {
		ScheduledDive scheduledDiveDuplicate = null;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (Objects.requireNonNull(((ScheduledDiveAdapter) getListAdapter()).getItem(i)).getOnlineId() ==
					scheduledDiveOnlineId) {
				scheduledDiveDuplicate = ((ScheduledDiveAdapter) getListAdapter()).getItem(i);
				break;
			}
		}
		return scheduledDiveDuplicate;
	}

	@Override
	protected void refreshScheduledDiveList() {
		updateFilterNotification();
		((ScheduledDiveAdapter) getListAdapter()).notifyDataSetChanged();

        if (!ListViewHelper.shouldRefreshAdditionalListViewItems(getListView())) {
            cancelOnlineRefresh();
        }
	}

	@Override
	protected void filterScheduledDiveList() {
		if (onlineFilterChanged()) {
			// Filter changed
			clearScheduledDives();
			refreshOnlineScheduledDives();
		}
	}
	
	private void clearScheduledDives() {
		// Clears list and resets adapter

        // Update parent fragment if tab
        if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
            ((DiveListTabFragment) getParentFragment()).updateOnlineSubTitles("0", "");
        }

		cancelOnlineRefresh();
		mAdditionalItemsToLoad = 0;
		
		((ScheduledDiveAdapter) getListAdapter()).clear();
		ScheduledDiveAdapter adapter = new ScheduledDiveAdapter(new ArrayList<ScheduledDive>());
		setListAdapter(adapter);
		((ScheduledDiveAdapter) getListAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineRefresh() {
		if (mDiveSiteOnlineDatabase != null && mDiveSiteOnlineDatabase.getActive()) {
			mDiveSiteOnlineDatabase.stopBackground();
			mDiveSiteOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineScheduledDive = false;
        stopRefresh();
	}
	
	protected boolean onlineFilterChanged() {
		String titleFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "")).trim();
		String countryFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, "")).trim();
		if (countryFilter.equals(getResources().getString(
				R.string.filter_list_all))) {
			countryFilter = "";
		}

		String stateFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "")).trim();
		String cityFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "")).trim();
		
		String previousDaysFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "")).trim();
		String nextDaysFilter = Objects.requireNonNull(mPrefs.getString(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "")).trim();

		return !mLastOnlineFilter[ONLINE_FILTER_TITLE_INDEX]
						.equals(titleFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_COUNTRY_INDEX]
						.equals(countryFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_STATE_INDEX]
						.equals(stateFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_CITY_INDEX]
						.equals(cityFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_TIMESTAMP_START_INDEX]
						.equals(previousDaysFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_TIMESTAMP_END_INDEX]
						.equals(nextDaysFilter);
	}

	@Override
	protected ScheduledDive getScheduledDiveItemClick(int position, long id) {
		return ((ScheduledDiveAdapter) getListAdapter()).getItem(position);
	}

    private Location getLocation() {
        return LocationController.getLocationControler().getLocation(getActivity(), mDiveSiteManager.getLastLocation());
    }

	private class ScheduledDiveAdapter extends ArrayAdapter<ScheduledDive> {

		public ScheduledDiveAdapter(ArrayList<ScheduledDive> scheduledDives) {
			super(Objects.requireNonNull(getActivity()), 0, scheduledDives);
		}

		@NonNull
        @Override
		public View getView(int position, View view, @NonNull ViewGroup parent) {
			// If we weren't given a view, inflate one using the layout we created for each list item
			if (view == null) {
				view = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.scheduleddive_list_item, parent, false);
			}

			final ScheduledDive scheduledDive = getItem(position);

            // Update parent fragment if tab
            if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
                ((DiveListTabFragment) getParentFragment()).updateOnlineSubTitles(String.valueOf(Objects.requireNonNull(scheduledDive).getScheduledDiveCountWhenRetreived()), "");
            }

			// Save id to view for later updating with buddies and stops
			mScheduledDiveListItemViews.put(Objects.requireNonNull(scheduledDive).getOnlineId(), view);
			if (mScheduledDiveListItemLoaderIDs.get(scheduledDive.getOnlineId()) == null) {
				mScheduledDiveListItemLoaderIDs.put(scheduledDive.getOnlineId(),
						mScheduledDiveListItemLoaderIDs.size());
			}

			// Remove sites and users before loading them
			LinearLayout scheduledDiveDiveSiteListView =
                    view.findViewById(R.id.scheduleddive_item_site_list);
			scheduledDiveDiveSiteListView.setTag(view);
			scheduledDiveDiveSiteListView.removeAllViews();
			
			LinearLayout scheduledDiveUserListView =
                    view.findViewById(R.id.scheduleddive_item_user_list);
			scheduledDiveUserListView.removeAllViews();

			// Process scheduled dive's sites 
			for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
				final ScheduledDiveDiveSite scheduledDiveDiveSite =
						scheduledDive.getScheduledDiveDiveSites().get(i);
				
				DiveSite diveSite = scheduledDiveDiveSite.getDiveSite();
				if (diveSite != null) {
					LayoutInflater layoutInflater = (LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View scheduledDiveDiveSiteView = layoutInflater.inflate(
							R.layout.scheduleddive_site_item, null);
					scheduledDiveDiveSiteView.setTag(view);
					
					// Trigger selection on click
					View.OnClickListener selectionClickListener = new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// If any items are checked, context menu is open
							//  So toggle item checked instead of selection
							ListView listView = 
									ScheduledDiveListOnlineFragment.this.getListView();
							View scheduledDiveView = (View)v.getTag();
							if (listView.getCheckedItemCount() > 0) {
								int viewPosition = listView.getPositionForView(scheduledDiveView);
								listView.setItemChecked(viewPosition, !listView.isItemChecked(viewPosition));
							} else {
                                // If dive site available, keep scheduled dive selected and open dive site
                                if (scheduledDiveDiveSite.getDiveSite() != null) {
                                    mLastDisplayedListItemView = null;
                                    triggerViewSelection(scheduledDiveView, (ScheduledDive)scheduledDiveView.getTag());

                                    // Open Dive Site's page
                                    openDiveSite(scheduledDiveDiveSite.getDiveSite());
                                } else {
                                    triggerViewSelection(scheduledDiveView, (ScheduledDive)scheduledDiveView.getTag());
                                }
							}
						}
					};
					
					scheduledDiveDiveSiteView.setOnClickListener(selectionClickListener);
					scheduledDiveDiveSiteListView.setOnClickListener(selectionClickListener);
					
					// Trigger contextual menu on long click
					View.OnLongClickListener contextClickListener = new View.OnLongClickListener() {
						
						@Override
						public boolean onLongClick(View v) {
							ListView listView = ScheduledDiveListOnlineFragment.this.getListView();
							View scheduledDiveView = (View)v.getTag();									
							int viewPosition = listView.getPositionForView(scheduledDiveView);
							listView.setItemChecked(viewPosition, !listView.isItemChecked(viewPosition));
                            listView.requestFocusFromTouch();

							return true;
						}
					};
					
					scheduledDiveDiveSiteView.setOnLongClickListener(contextClickListener);
					scheduledDiveDiveSiteListView.setOnLongClickListener(contextClickListener);
	
					// Add the view to the list
					scheduledDiveDiveSiteListView.addView(scheduledDiveDiveSiteView);
	
					// Set scheduled dive site view fields
					TextView scheduledDiveDiveSiteName =
                            scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_name);
					scheduledDiveDiveSiteName.setText(diveSite.getName());
					
					TextView scheduledDiveDiveSiteLocation =
                            scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_location);
					scheduledDiveDiveSiteLocation.setText(diveSite.getFullLocation());
					
					TextView scheduledDiveDiveSiteVoteCount =
                            scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_vote_count);
					//scheduledDiveDiveSiteVoteCount.setVisibility(View.VISIBLE);
					scheduledDiveDiveSiteVoteCount.setText(String.format(getResources().getString(R.string.scheduleddive_list_vote_count), 
							scheduledDiveDiveSite.getVoteCount()));
				}
			}

			// Process scheduled dive's users
			for (int i = 0; i < scheduledDive.getScheduledDiveUsers().size(); i++) {
				final ScheduledDiveUser scheduledDiveUser = scheduledDive.getScheduledDiveUsers().get(i);

				if (scheduledDiveUser.getAttendState() == ScheduledDiveUser.AttendState.ATTENDING) {
                    // Trigger contextual menu on long click
                    View.OnLongClickListener contextClickListener = new View.OnLongClickListener() {

                        @Override
                        public boolean onLongClick(View v) {
                            ListView listView = ScheduledDiveListOnlineFragment.this.getListView();
                            View scheduledDiveView = (View)v.getTag();
                            int viewPosition = listView.getPositionForView(scheduledDiveView);
                            listView.setItemChecked(viewPosition, !listView.isItemChecked(viewPosition));
                            listView.requestFocusFromTouch();

                            return true;
                        }
                    };

                    // Trigger selection on click
                    View.OnClickListener selectionClickListener = new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // If any items are checked, context menu is open
                            //  So toggle item checked instead of selection
                            ListView listView = ScheduledDiveListOnlineFragment.this.getListView();
                            View scheduledDiveView = (View)v.getTag();
                            if (listView.getCheckedItemCount() > 0) {
                                int viewPosition = listView.getPositionForView(scheduledDiveView);
                                listView.setItemChecked(viewPosition, !listView.isItemChecked(viewPosition));
                            } else {
                                triggerViewSelection(scheduledDiveView, (ScheduledDive)scheduledDiveView.getTag());
                            }
                        }
                    };

                    scheduledDiveUserListView.setOnLongClickListener(contextClickListener);
                    scheduledDiveUserListView.setOnClickListener(selectionClickListener);
                    scheduledDiveUserListView.setTag(view);

					LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View scheduledDiveUserView = layoutInflater.inflate(R.layout.scheduleddive_user_item, null);
                    scheduledDiveUserView.setOnLongClickListener(contextClickListener);
                    scheduledDiveUserView.setOnClickListener(selectionClickListener);
                    scheduledDiveUserView.setTag(view);
	
					// Add the view to the list
					scheduledDiveUserListView.addView(scheduledDiveUserView);
	
					if (scheduledDiveUser.getUserId() != -1) {
						ImageButton userProfile =
                                scheduledDiveUserView.findViewById(R.id.scheduleddive_user_picture);
	
						mScheduledDiveListItemDiverImageView.put(scheduledDiveUser.getOnlineId(), userProfile);
	
						// Set listener to access profile
						userProfile.setOnClickListener(new View.OnClickListener() {
	
									@Override
									public void onClick(View v) {
										Intent intent = 
												new Intent(ScheduledDiveListOnlineFragment.this.getActivity(),
												DiverActivity.class);
										intent.putExtra(
												DiverActivity.EXTRA_DIVER_ID,
												scheduledDiveUser.getUserId());
										startActivity(intent);
									}
								});
	
						// Set image
						if (mDiverProfileImageCache.get(scheduledDiveUser.getUserId()) == null) {
							DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = 
									new DiveSiteOnlineDatabaseLink(getActivity());
							diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
										@Override
										public void onOnlineDiveDataRetrievedComplete(
												ArrayList<Object> resultList,
												String message, Boolean isError) {
											if (resultList.size() > 0) {
												Diver diver = (Diver) resultList.get(0);
	
												// Get bitmap profile image for diver
												ImageView diverButton = 
														mScheduledDiveListItemDiverImageView.get(scheduledDiveUser.getOnlineId());
												LoadOnlineImageTask task = new LoadOnlineImageTask(diverButton) {
	
													@Override
													protected void onPostExecute(Bitmap result) {
														super.onPostExecute(result);
													}
	
												};
												task.execute(diver
														.getPictureURL());
											}
										}
	
										@Override
										public void onOnlineDiveDataProgress(
												Object result) {
											// TODO Auto-generated method stub
	
										}
	
										@Override
										public void onOnlineDiveDataPostBackground(
												ArrayList<Object> resultList,
												String message) {
											// TODO Auto-generated method stub
	
										}
									});
							diveSiteOnlineDatabaseUser.getUser(String.valueOf(scheduledDiveUser.getUserId()),"", "");
						} else {
							userProfile.setImageBitmap(mDiverProfileImageCache.get(scheduledDiveUser.getUserId()));
						}
					}
				}
			}

			// Set up the view with the scheduled dive info
			if (scheduledDive.isPublished()) {
				view.setBackgroundColor(getResources().getColor(
						R.color.itemPublished));
			} else {
				view.setBackgroundColor(getResources().getColor(
						R.color.itemUnpublished));
			}
			
			// Title
			TextView title = view.findViewById(R.id.scheduleddive_item_title);
			if (scheduledDive.getTitle().trim().isEmpty()) {
				title.setText(getResources().getString(R.string.scheduleddive_default_title) + 
						" " + scheduledDive.getTimestampStringLong());
			} else {
				title.setText(scheduledDive.getTitle());
			}

			// Timestamp
			TextView timeStamp = view.findViewById(R.id.scheduleddive_item_timestamp);
			timeStamp.setText(dateTimeFormat.format(scheduledDive.getTimestamp()));

			// Comment
			TextView commentView = view.findViewById(R.id.scheduleddive_comment);
			commentView.setText(scheduledDive.getComment());
			
			// Diver Count
			TextView diverCountView = view.findViewById(R.id.scheduleddive_item_divercount);
			diverCountView.setText(String.format(
					getResources().getString(R.string.scheduleddive_list_diver_count), 
					scheduledDive.getAttendingUsersCount()));
			
			// Determine which button to show
			Button scheduledDiveAttend = view.findViewById(R.id.scheduleddive_item_attend);
			scheduledDiveAttend.setTag(scheduledDive);
			scheduledDiveAttend.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Get or create scheduled dive user
					ScheduledDive scheduledDive = (ScheduledDive) v.getTag();
					int index = scheduledDive.getScheduledDiveUserForUser(mDiveSiteManager.getLoggedInDiverId());
					ScheduledDiveUser scheduledDiveUser;
					if (index == -1) {
						scheduledDiveUser = new ScheduledDiveUser();
						scheduledDiveUser.setScheduledDiveLocalId(scheduledDive.getLocalId());
						scheduledDiveUser.setScheduledDiveOnlineId(scheduledDive.getOnlineId());
						scheduledDiveUser.setUserId(mDiveSiteManager.getLoggedInDiverId());
					} else {
						scheduledDiveUser = scheduledDive.getScheduledDiveUsers().get(index);
					}
					
					scheduledDiveUser.setAttendState(ScheduledDiveUser.AttendState.ATTENDING);
					
					DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
					diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

								@Override
								public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {
									if (resultList.size() > 0) {
										// Update Scheduled Dive
										ScheduledDiveUser scheduledDiveUser = (ScheduledDiveUser) resultList.get(0);
										ScheduledDive scheduledDive = getScheduledDive(scheduledDiveUser.getScheduledDiveOnlineId());
										
										int index =  scheduledDive.getScheduledDiveUserIndex(scheduledDiveUser);
										if (index != -1) {
											scheduledDive.getScheduledDiveUsers().remove(index);
										}
										
										scheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
										((ScheduledDiveAdapter) getListAdapter()).notifyDataSetChanged();
										
										// Update buttons in view
										View scheduledDiveView = 
												mScheduledDiveListItemViews.get(scheduledDive.getOnlineId());
										Button scheduledDiveAttend =
                                                Objects.requireNonNull(scheduledDiveView).findViewById(R.id.scheduleddive_item_attend);
										Button scheduledDiveBail =
                                                scheduledDiveView.findViewById(R.id.scheduleddive_item_bail);
										
										scheduledDiveAttend.setVisibility(View.GONE);
										scheduledDiveBail.setVisibility(View.VISIBLE);
									} else if (!message.isEmpty()) {
										Toast.makeText(
												getActivity(),
												message,
												Toast.LENGTH_SHORT)
												.show();
									}
								}

								@Override
								public void onOnlineDiveDataProgress(Object result) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onOnlineDiveDataPostBackground(
										ArrayList<Object> resultList, String message) {
									// TODO Auto-generated method stub

								}
							});
					diveSiteOnlineDatabaseUser.setScheduledDiveUser(scheduledDiveUser);
				}
			});
			
			Button scheduledDiveBail = view.findViewById(R.id.scheduleddive_item_bail);
			scheduledDiveBail.setTag(scheduledDive);
			scheduledDiveBail.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Get or create scheduled dive user
					ScheduledDive scheduledDive = (ScheduledDive) v.getTag();
					int index = scheduledDive.getScheduledDiveUserForUser(mDiveSiteManager.getLoggedInDiverId());
					ScheduledDiveUser scheduledDiveUser;
					if (index == -1) {
						scheduledDiveUser = new ScheduledDiveUser();
						scheduledDiveUser.setScheduledDiveLocalId(scheduledDive.getLocalId());
						scheduledDiveUser.setScheduledDiveOnlineId(scheduledDive.getOnlineId());
						scheduledDiveUser.setUserId(mDiveSiteManager.getLoggedInDiverId());
					} else {
						scheduledDiveUser = scheduledDive.getScheduledDiveUsers().get(index);
					}
					
					scheduledDiveUser.setAttendState(ScheduledDiveUser.AttendState.NOT_ATTENDING);
					
					DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
					diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

								@Override
								public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {
									if (resultList.size() > 0) {
										// Update Scheduled Dive
										ScheduledDiveUser scheduledDiveUser = (ScheduledDiveUser) resultList.get(0);
										ScheduledDive scheduledDive = getScheduledDive(scheduledDiveUser.getScheduledDiveOnlineId());
										
										int index =  scheduledDive.getScheduledDiveUserIndex(scheduledDiveUser);
										if (index != -1) {
											scheduledDive.getScheduledDiveUsers().remove(index);
										}
										
										scheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
										((ScheduledDiveAdapter) getListAdapter()).notifyDataSetChanged();
										
										// Update buttons in view
										View scheduledDiveView = 
												mScheduledDiveListItemViews.get(scheduledDive.getOnlineId());
										Button scheduledDiveAttend =
                                                Objects.requireNonNull(scheduledDiveView).findViewById(R.id.scheduleddive_item_attend);
										Button scheduledDiveBail =
                                                scheduledDiveView.findViewById(R.id.scheduleddive_item_bail);
										
										scheduledDiveAttend.setVisibility(View.VISIBLE);
										scheduledDiveBail.setVisibility(View.GONE);
									} else if (!message.isEmpty()) {
										Toast.makeText(
												getActivity(),
												message,
												Toast.LENGTH_SHORT)
												.show();
									}
								}

								@Override
								public void onOnlineDiveDataProgress(Object result) {
									// TODO Auto-generated method stub

								}

								@Override
								public void onOnlineDiveDataPostBackground(
										ArrayList<Object> resultList, String message) {
									// TODO Auto-generated method stub

								}
							});
					diveSiteOnlineDatabaseUser.setScheduledDiveUser(scheduledDiveUser);
				}
			});
			
			Button scheduledDiveLog = view.findViewById(R.id.scheduleddive_item_log);
			scheduledDiveLog.setTag(scheduledDive);		
			scheduledDiveLog.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Let users select which scheduled dive dive sites they want to log
					final ScheduledDive scheduledDive = (ScheduledDive)v.getTag();
					CharSequence[] diveSiteNames = 
							new CharSequence[scheduledDive.getScheduledDiveDiveSites().size()];
					boolean[] diveSiteSelected =
							new boolean[scheduledDive.getScheduledDiveDiveSites().size()];
					
					final ArrayList<ScheduledDiveDiveSite> selectedScheduledDiveSites =
							new ArrayList<ScheduledDiveDiveSite>();
					for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
						if (scheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite() != null) {
							diveSiteNames[i] = 
									scheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite().getName();
							diveSiteSelected[i] = true;
							selectedScheduledDiveSites.add(scheduledDive.getScheduledDiveDiveSites().get(i));
						} else {
							diveSiteNames[i] = getResources().getString(R.string.notAvailable);
							diveSiteSelected[i] = false;
						}
						
					}
					
					new AlertDialog.Builder(getActivity())
					.setTitle(R.string.scheduleddive_log_select)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setMultiChoiceItems(diveSiteNames, diveSiteSelected,
									new DialogInterface.OnMultiChoiceClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which, boolean isChecked) {
											ScheduledDiveDiveSite scheduledDiveDiveSite =
													scheduledDive.getScheduledDiveDiveSites().get(which);
											if (isChecked) {
												selectedScheduledDiveSites.add(scheduledDiveDiveSite);
											} else {
												selectedScheduledDiveSites.remove(scheduledDiveDiveSite);
											}
										}
									})
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int whichButton) {
									// Create and initialize progress dialog
									mProgressDialog.setMessage(getString(R.string.log_scheduleddives_progress));
									mProgressDialog.setCancelable(false);
									mProgressDialog.setIndeterminate(false);
									mProgressDialog.setProgress(0);
									mProgressDialog.setMax(getListView().getCheckedItemCount());
									mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									mProgressDialog.show();

									for (int i = 0; i < selectedScheduledDiveSites.size(); i++) {
										// Log each selection
										ScheduledDiveDiveSite scheduledDiveDiveSite =
												selectedScheduledDiveSites.get(i);
										
										DiveLog diveLog = new DiveLog();
										diveLog.setUserId(mDiveSiteManager.getLoggedInDiverId());
										diveLog.setUsername(mDiveSiteManager.getLoggedInDiverUsername());
										diveLog.setTimestamp(scheduledDive.getTimestamp());
										diveLog.setDiveSite(scheduledDiveDiveSite.getDiveSite());
										diveLog.setDiveSiteLocalId(scheduledDiveDiveSite.getDiveSiteLocalId());
										diveLog.setDiveSiteOnlineId(scheduledDiveDiveSite.getDiveSiteOnlineId());
										
										// Add attendees as buddies
										for (int j = 0; j < scheduledDive.getScheduledDiveUsers().size(); j++) {
											ScheduledDiveUser scheduledDiveUser =
													scheduledDive.getScheduledDiveUsers().get(j);
											
											if (scheduledDiveUser.getUserId() != mDiveSiteManager.getLoggedInDiverId()){
												DiveLogBuddy diveLogBuddy = new DiveLogBuddy();
												diveLogBuddy.setDiverOnlineId(scheduledDiveUser.getUserId());
												diveLog.getBuddies().add(diveLogBuddy);
											}
										}

										mDiveSiteManager.saveDiveLog(diveLog);

                                        // Open Dive Log fragment to edit dive log
                                        Intent intent = new Intent(getActivity(), DiveLogActivity.class);
                                        intent.putExtra(DiveLogActivity.EXTRA_DIVE_LOG, diveLog);
                                        startActivity(intent);
									}

									mProgressDialog.dismiss();
									Toast.makeText(
											getActivity(),
											R.string.scheduleddives_logs_message,
											Toast.LENGTH_SHORT)
											.show();
								}
							})
					.setNegativeButton(android.R.string.no, null)
					.show();
				}
			});
			
			if (scheduledDive.getTimestamp().getTime() < (new Date()).getTime()) {
				// Scheduled Dive in the past, show log button				
				scheduledDiveAttend.setVisibility(View.GONE);
				scheduledDiveBail.setVisibility(View.GONE);
				scheduledDiveLog.setVisibility(View.VISIBLE);
			} else if (scheduledDive.isUserAttending(mDiveSiteManager.getLoggedInDiverId())) {
				// User attending, show bail button
				scheduledDiveAttend.setVisibility(View.GONE);
				scheduledDiveBail.setVisibility(View.VISIBLE);
				scheduledDiveLog.setVisibility(View.GONE);
			} else {
				// User not attending and scheduled dive in future show attend button
				scheduledDiveAttend.setVisibility(View.VISIBLE);
				scheduledDiveBail.setVisibility(View.GONE);
				scheduledDiveLog.setVisibility(View.GONE);
			}
			
			// Submitter
			ImageButton submitterView = view.findViewById(R.id.scheduleddive_item_picture);
			mScheduledDiveListItemDiverImageView.put(scheduledDive.getOnlineId(), submitterView);
			
			if (mDiverProfileImageCache.get(scheduledDive.getSubmitterId()) == null) {
				// Need to get diver from the ID, then the diver's picture
				DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
				diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

							@Override
							public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {
								if (resultList.size() > 0) {
									// First save diver so another list item can use it
									Diver diver = (Diver) resultList.get(0);
									mScheduledDiveListItemDiver.put(scheduledDive.getOnlineId(), diver);
									
									// Now get bitmap profile image for diver
									LoadOnlineImageTask task = 
											new LoadOnlineImageTask(mScheduledDiveListItemDiverImageView.get(scheduledDive.getOnlineId())) {

										@Override
										protected void onPostExecute(
												Bitmap result) {
											Diver diver = (Diver) getTag();
											mDiverProfileImageCache.put(diver.getOnlineId(), result);
											ImageButton diverProfileImage = 
												(ImageButton) mScheduledDiveListItemDiverImageView.get(scheduledDive.getOnlineId());
											Objects.requireNonNull(diverProfileImage).setImageBitmap(result);
										}

									};
									task.setTag(diver);
									task.execute(diver.getPictureURL());
								}
							}

							@Override
							public void onOnlineDiveDataProgress(Object result) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onOnlineDiveDataPostBackground(
									ArrayList<Object> resultList, String message) {
								// TODO Auto-generated method stub

							}
						});
				diveSiteOnlineDatabaseUser.getUser(String.valueOf(scheduledDive.getSubmitterId()), "", "");
			} else {
				submitterView.setImageBitmap(mDiverProfileImageCache.get(scheduledDive.getSubmitterId()));
			}

			submitterView.setTag(scheduledDive.getSubmitterId());
			submitterView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Open profile page
					Intent intent = 
							new Intent(ScheduledDiveListOnlineFragment.this.getActivity(), DiverActivity.class);
					long diverUserId = (Long) v.getTag();
					intent.putExtra(DiverActivity.EXTRA_DIVER_ID, diverUserId);
					startActivity(intent);
				}
			});

			ImageButton scheduledDivePublished =
                    view.findViewById(R.id.scheduleddive_indicate_isPublished);
			ImageButton scheduledDiveUnpublished =
                    view.findViewById(R.id.scheduleddive_indicate_isUnpublished);
			if (scheduledDive.isPublished()) {
				scheduledDivePublished.setVisibility(View.VISIBLE);
				scheduledDiveUnpublished.setVisibility(View.GONE);
			} else {
				scheduledDivePublished.setVisibility(View.GONE);
				scheduledDiveUnpublished.setVisibility(View.VISIBLE);
			}

			ImageButton scheduledDiveSaved =
                    view.findViewById(R.id.scheduleddive_indicate_isSaved);
			if (scheduledDive.getLocalId() != -1) {
				scheduledDiveSaved.setVisibility(View.VISIBLE);
			} else {
				scheduledDiveSaved.setVisibility(View.INVISIBLE);
			}

            // Initialize visibility secondary view
            View secondaryView = view.findViewById(R.id.scheduleddive_item_secondary_view);
            final ViewGroup mapContainer = view.findViewById(R.id.scheduleddive_list_item_mapView_container);
            if (scheduledDive == mSelectedScheduledDive) {
                view.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
                secondaryView.setVisibility(View.VISIBLE);
                setScheduledDiveMap(scheduledDive, mapContainer);
            } else if (mScheduledDiveItemMapView.getParent() == mapContainer) {
                secondaryView.setVisibility(View.GONE);
                mapContainer.setVisibility(View.GONE);
            }

            mAdditionalItemsToLoad = ListViewHelper.additionalItemCountToLoad(getListView());

			return view;
		}
	}
	
	/**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
    	super.onLocationChanged(location);

        if (mDiveSiteManager.getLastLocation() == null ||
            mForceLocationDataRefresh ||
            (Math.abs(location.getLatitude() - mDiveSiteManager.getLastLocation().getLatitude()) > DiveSiteManager.LOCATION_COMPARE_EPSILON) ||
            (Math.abs(location.getLongitude() - mDiveSiteManager.getLastLocation().getLongitude()) > DiveSiteManager.LOCATION_COMPARE_EPSILON)) {

            mForceLocationDataRefresh = false;

            clearScheduledDives();
            refreshOnlineScheduledDives();
        }
    }
}
