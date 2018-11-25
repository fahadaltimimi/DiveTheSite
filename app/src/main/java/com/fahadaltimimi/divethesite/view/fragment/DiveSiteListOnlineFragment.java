package com.fahadaltimimi.divethesite.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.controller.ListViewHelper;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.DiveSitePicture;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class DiveSiteListOnlineFragment extends DiveSiteListFragment {

	private int ONLINE_FILTER_COUNT = 4;
	private int ONLINE_FILTER_TITLE_INDEX = 0;
	private int ONLINE_FILTER_COUNTRY_INDEX = 1;
	private int ONLINE_FILTER_STATE_INDEX = 2;
	private int ONLINE_FILTER_CITY_INDEX = 3;

	private double LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT = 0.60;

	private ListView mListView = null;
	private int mAdditionalItemsToLoad = 0;

	private String[] mLastOnlineFilter = new String[ONLINE_FILTER_COUNT];

	private Boolean mRefreshingOnlineDiveSites = false;

    private Boolean mForceLocationDataRefresh = false;

	public static DiveSiteListOnlineFragment newInstance(long diverID,
			boolean setToDiveLog) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG, setToDiveLog);
		DiveSiteListOnlineFragment rf = new DiveSiteListOnlineFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DiveSiteAdapter adapter = new DiveSiteAdapter(new ArrayList<DiveSite>());
		setListAdapter(adapter);

		mProgressDialog = new ProgressDialog(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		// Register list view with context menu
		mListView = Objects.requireNonNull(v).findViewById(android.R.id.list);
		
		mListView.setOnScrollListener(new OnScrollListener(){

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
                    if (!(mAdditionalItemsToLoad > 0 && mListView.getCount() == 0) &&
                            !mRefreshingOnlineDiveSites &&
                            view.getLastVisiblePosition() >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * view.getCount()) {
                        refreshOnlineDiveSites();
                    }
                }
			}
			
		});

		mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(final ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.menu_item_save_divesite:
					// Save selected dive sites
					mProgressDialog.setMessage(getString(R.string.save_divelogs_progress));
					mProgressDialog.setCancelable(false);
					mProgressDialog.setIndeterminate(false);
					mProgressDialog.setProgress(0);
					mProgressDialog.setMax(mListView.getCheckedItemCount());
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					mProgressDialog.show();

					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (mListView.isItemChecked(i)) {
							DiveSiteAdapter adapter = (DiveSiteAdapter)mListView.getAdapter();
							DiveSite diveSite = adapter.getItem(i);
							diveSite.setLocalId(mDiveSiteManager.getDiveSiteLocalId(Objects.requireNonNull(diveSite).getOnlineId()));

							mDiveSiteManager.saveDiveSite(diveSite);
							
							// Save logs for site
							for (int j = 0; j < diveSite.getDiveLogs().size(); j++) {
								mDiveSiteManager.saveDiveLog(diveSite.getDiveLogs().get(j));
							}

							if (diveSite.getPicturesCount() > 0) {
								// Download pictures for each dive site, save each one
								for (int j = 0; j < diveSite.getPicturesCount(); j++) {
									if (diveSite.getPicture(j).getBitmapURL() != null
											&& !diveSite.getPicture(j)
													.getBitmapURL().isEmpty()) {

										LoadOnlineImageTask task;
										if (j == diveSite.getPicturesCount() - 1) {
											// Need to update progress bar since
											// this is the last picture for the
											// dive site
											task = new LoadOnlineImageTask(null) {
												@Override
												protected void onPostExecute(
														Bitmap result) {
													// Need to save bitmap to
													// file and set path
													String imagePathToSave = mDiveSiteManager
															.saveImageInternalStorage(
																	result,
																	"DiveSiteImage");

													DiveSitePicture picture = (DiveSitePicture) getTag();
													picture.setBitmapFilePath(imagePathToSave);
													mDiveSiteManager
															.saveDiveSitePicture(picture);

													mProgressDialog
															.setProgress(mProgressDialog
																	.getProgress() + 1);
													if (mProgressDialog
															.getProgress() == mProgressDialog
															.getMax()) {
														mProgressDialog
																.dismiss();
														Toast.makeText(
																getActivity(),
																R.string.dive_sites_saved_message,
																Toast.LENGTH_SHORT)
																.show();
													}
												}
											};
										} else {
											task = new LoadOnlineImageTask(null) {
												@Override
												protected void onPostExecute(
														Bitmap result) {
													// Need to save bitmap to
													// file and set path
													String imagePathToSave = mDiveSiteManager
															.saveImageInternalStorage(
																	result,
																	"DiveSiteImage");

													DiveSitePicture picture = (DiveSitePicture) getTag();
													picture.setBitmapFilePath(imagePathToSave);
													mDiveSiteManager
															.saveDiveSitePicture(picture);
												}
											};
										}

										task.setTag(diveSite.getPicture(j));
										task.execute(diveSite.getPicture(j)
												.getBitmapURL());
									} else if (j == diveSite.getPicturesCount()) {
										mProgressDialog
												.setProgress(mProgressDialog
														.getProgress() + 1);
										if (mProgressDialog.getProgress() == mProgressDialog
												.getMax()) {
											mProgressDialog.dismiss();
											Toast.makeText(
													getActivity(),
													R.string.dive_sites_saved_message,
													Toast.LENGTH_SHORT).show();
										}
									}
								}
							} else {
								mProgressDialog.setProgress(mProgressDialog
										.getProgress() + 1);
								if (mProgressDialog.getProgress() == mProgressDialog
										.getMax()) {
									mProgressDialog.dismiss();
									Toast.makeText(getActivity(),
											R.string.dive_sites_saved_message,
											Toast.LENGTH_SHORT).show();
								}
							}
						}
					}

					mode.finish();

					return true;
					
				case R.id.menu_item_share_divesite:
					// Get summary for each dive site selected
					String combinedSummary = 
						getResources().getString(R.string.share_title_divesite) + "\n\n";
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							DiveSite diveSite = (DiveSite) getListAdapter().getItem(i);
							combinedSummary = combinedSummary + diveSite.getShareSummary() + "\n\n";
						}
					}
					
					// Share info to what user selects
					Intent share = new Intent(android.content.Intent.ACTION_SEND);
			        share.setType("text/plain");
			        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			        share.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title_divesite));
			        share.putExtra(Intent.EXTRA_TEXT, combinedSummary.trim());
			 
			        startActivity(Intent.createChooser(share, "Share Dive Site!"));
					
			        mode.finish();
			        
					return true;

				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(
						R.menu.fragment_divesite_list_online_contextual, menu);

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
				.findViewById(R.id.divesite_list_filter_published);
		filterPublished.setVisibility(View.GONE);

		CheckBox filterUnpublished = mListFilter
				.findViewById(R.id.divesite_list_filter_unpublished);
		filterUnpublished.setVisibility(View.GONE);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_divesite_list_online, menu);

		mRefreshMenuItem = menu.findItem(R.id.menu_item_refresh_divesites);

		if (mRefreshingOnlineDiveSites && mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem selectDiveSiteMenuItem = menu.findItem(R.id.menu_item_select_divesite);
		if (selectDiveSiteMenuItem != null) {
			selectDiveSiteMenuItem.setVisible(mSetToDiveLog && mSelectedDiveSite != null);
		}

		MenuItem addDiveSiteMenuItem = menu.findItem(R.id.menu_item_add_divesite);

		if (addDiveSiteMenuItem != null) {
			// Only show add button if were looking at our own sites
			if (mRestrictToDiverID != -1 && mRestrictToDiverID != mDiveSiteManager.getLoggedInDiverId()) {
				addDiveSiteMenuItem.setVisible(false);
			} else {
				addDiveSiteMenuItem.setVisible(true);
			}	
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_select_divesite:
			if (mSelectedDiveSite != null) {
				// Save local and online id's and finish activity
				Intent intent = new Intent(getActivity(), DiveLogActivity.class);
				intent.putExtra(DiveSiteManager.EXTRA_DIVE_SITE,
						mSelectedDiveSite);
				Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
			}
			return true;

		case R.id.menu_item_refresh_divesites:
			if (!startLocationUpdates()) {
                clearDiveSites();
                refreshOnlineDiveSites();
	    	}
			return true;

		case R.id.menu_item_add_divesite:
			// If user not registered, don't allow
			if (mDiveSiteManager.getLoggedInDiverId() == -1) {
				Toast.makeText(getActivity(), R.string.not_registered_create_site, Toast.LENGTH_LONG).show();
				return true;
			}
						
			// Open the dive site in edit mode
			mPrefs.edit()
					.putBoolean(
							DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
							true).apply();

			// Add a new dive site and switch to Dive Site view
			DiveSite diveSite = mDiveSiteManager.insertDiveSite();
			openDiveSite(diveSite);
			return true;

		case R.id.menu_item_filter_divesite_list:
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
	protected boolean updateFilterNotification() {
		String currentFilter = mFilterNotification.getText().toString();

		// Determine filter to set from selection
		String titleFilter = "";
		if (mSetToDiveLog) {
			titleFilter = Objects.requireNonNull(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, ""))
					.trim();
		} else {
			titleFilter = Objects.requireNonNull(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, "")).trim();
		}
		String countryFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, "")).trim();
		String stateFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "")).trim();
		String cityFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "")).trim();

		Boolean publishedFilter = mPrefs.getBoolean(
				DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_PUBLISHED, true);
		Boolean unPublishedFilter = mPrefs.getBoolean(
				DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED, true);

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

		if (!publishedFilter) {
			filterNotification
					.add(getResources().getString(R.string.filter_hiding)
							+ " "
							+ getResources().getString(
									R.string.filter_published));
		}
		if (!unPublishedFilter) {
			filterNotification.add(getResources().getString(
					R.string.filter_hiding)
					+ " "
					+ getResources().getString(R.string.filter_unpublished));
		}

		if (filterNotification.size() == 0) {
			mFilterNotificationContainer.setVisibility(View.GONE);
		} else {
			mFilterNotificationContainer.setVisibility(View.VISIBLE);
		}

		mFilterNotification.setText(filterNotification.toString());

		return !filterNotification.toString().equals(currentFilter);
	}

	protected void refreshOnlineDiveSites() {
		// Look for more dive sites and set menu item icon to spin
		if (mRefreshingOnlineDiveSites) {
			cancelOnlineRefresh();
		}
		
		mRefreshingOnlineDiveSites = true;
		
		if (mRefreshMenuItem != null){
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}

		String titleFilter;
		if (mSetToDiveLog) {
			titleFilter = Objects.requireNonNull(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, ""))
					.trim();
		} else {
			titleFilter = Objects.requireNonNull(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, "")).trim();
		}

		String countryFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, "")).trim();
		if (countryFilter.equals(getResources().getString(
				R.string.filter_list_all))) {
			countryFilter = "";
		}

		String stateFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "")).trim();
		String cityFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "")).trim();

		mLastOnlineFilter[ONLINE_FILTER_TITLE_INDEX] = titleFilter;
		mLastOnlineFilter[ONLINE_FILTER_COUNTRY_INDEX] = countryFilter;
		mLastOnlineFilter[ONLINE_FILTER_STATE_INDEX] = stateFilter;
		mLastOnlineFilter[ONLINE_FILTER_CITY_INDEX] = cityFilter;

		mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mDiveSiteOnlineDatabase
				.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {

						if (getActivity() != null && message != null && !message.isEmpty()) {
							Toast.makeText(getActivity(), message,
									Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineDiveSites = false;
						
						if (mRefreshMenuItem != null) {
							mRefreshMenuItem.setActionView(null);
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSiteOnlineDatabase.getActive() && getActivity() != null) {
							DiveSite diveSite = getDiveSiteOnlineId((DiveSite) result);
							if (diveSite == null) {
								((DiveSiteAdapter) getListAdapter())
										.add((DiveSite) result);
							} else {
								int index = getDiveSiteIndex(diveSite);
								((DiveSiteAdapter) getListAdapter())
										.remove(diveSite);
								((DiveSiteAdapter) getListAdapter()).insert(
										(DiveSite) result, index);
							}

							refreshDiveSiteList();

						} else {
							cancelOnlineRefresh();
						}
					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						//
					}
				});
		
		

		if (getLocation() != null) {
			if (mAdditionalItemsToLoad == 0) {
				mDiveSiteOnlineDatabase.getDiveSiteList(new Date(0),
						mRestrictToDiverID, String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLongitude()),
						titleFilter, countryFilter, stateFilter,
						cityFilter, "", "", "", "", "", "", "");
			} else {
				mDiveSiteOnlineDatabase.getDiveSiteList(new Date(0),
						mRestrictToDiverID, String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLongitude()), 
						titleFilter, countryFilter, stateFilter,
						cityFilter, "", "", "", "", "",
						String.valueOf(mListView.getCount()),
						String.valueOf(mAdditionalItemsToLoad));
			}
		} else {
			if (mAdditionalItemsToLoad == 0) {
				mDiveSiteOnlineDatabase.getDiveSiteList(new Date(0),
						mRestrictToDiverID, "", "",
						titleFilter, countryFilter, stateFilter,
						cityFilter, "", "", "", "", "", "", "");
			} else {
				mDiveSiteOnlineDatabase.getDiveSiteList(new Date(0),
						mRestrictToDiverID, "", "", 
						titleFilter, countryFilter, stateFilter,
						cityFilter, "", "", "", "", "",
						String.valueOf(mListView.getCount()),
						String.valueOf(mAdditionalItemsToLoad));
			}
		}
	}

	private int getDiveSiteIndex(DiveSite diveSite) {
		int index = -1;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (Objects.requireNonNull(((DiveSiteAdapter) getListAdapter()).getItem(i)).getOnlineId() == diveSite
					.getOnlineId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private DiveSite getDiveSiteOnlineId(DiveSite diveSite) {
		DiveSite diveSiteDuplicate = null;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (Objects.requireNonNull(((DiveSiteAdapter) getListAdapter()).getItem(i)).getOnlineId() == diveSite
					.getOnlineId()) {
				diveSiteDuplicate = ((DiveSiteAdapter) getListAdapter())
						.getItem(i);
				break;
			}
		}
		return diveSiteDuplicate;
	}

	@Override
	protected void refreshDiveSiteList() {
		updateFilterNotification();
		((DiveSiteAdapter) getListAdapter()).notifyDataSetChanged();

        if (!ListViewHelper.shouldRefreshAdditionalListViewItems(mListView)) {
            cancelOnlineRefresh();
        }
	}

	@Override
	protected void filterDiveSiteList() {
		if (onlineFilterChanged()) {
			// Filter changed
			clearDiveSites();
			refreshOnlineDiveSites();
		}
	}

	private void clearDiveSites() {
		// Clears list and resets adapter

        // Update parent fragment if tab
        if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
            ((DiveListTabFragment) getParentFragment()).updateOnlineSubTitles("0", "");
        }

		cancelOnlineRefresh();
		mAdditionalItemsToLoad = 0;
		
		((DiveSiteAdapter) getListAdapter()).clear();
		DiveSiteAdapter adapter = new DiveSiteAdapter(new ArrayList<DiveSite>());
		setListAdapter(adapter);
		((DiveSiteAdapter) getListAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineRefresh() {
		if (mDiveSiteOnlineDatabase != null && mDiveSiteOnlineDatabase.getActive()) {
			mDiveSiteOnlineDatabase.stopBackground();
			mDiveSiteOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineDiveSites = false;
		
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	protected boolean onlineFilterChanged() {
		String titleFilter;
		if (mSetToDiveLog) {
			titleFilter = Objects.requireNonNull(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, ""))
					.trim();
		} else {
			titleFilter = Objects.requireNonNull(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, "")).trim();
		}
		String countryFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, "")).trim();
		if (countryFilter.equals(getResources().getString(
				R.string.filter_list_all))) {
			countryFilter = "";
		}

		String stateFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "")).trim();
		String cityFilter = Objects.requireNonNull(mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "")).trim();

		return !mLastOnlineFilter[ONLINE_FILTER_TITLE_INDEX]
				.equals(titleFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_COUNTRY_INDEX]
						.equals(countryFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_STATE_INDEX]
						.equals(stateFilter)
				|| !mLastOnlineFilter[ONLINE_FILTER_CITY_INDEX]
						.equals(cityFilter);
	}

	@Override
	protected DiveSite getDiveSiteItemClick(int position, long id) {
		return ((DiveSiteAdapter) getListAdapter()).getItem(position);
	}

    @Override
    public void onResume() {
        super.onResume();

        if (ListViewHelper.shouldRefreshAdditionalListViewItems(mListView)) {
            refreshOnlineDiveSites();
        }
    }

	@Override
	public void onStop() {
		super.onStop();
		cancelOnlineRefresh();
	}

	private class DiveSiteAdapter extends ArrayAdapter<DiveSite> {

		public DiveSiteAdapter(ArrayList<DiveSite> diveSites) {
			super(Objects.requireNonNull(getActivity()), 0, diveSites);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// If we weren't given a view, inflate one using the layout we
			// created for each list item
			if (view == null) {
				view = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.divesite_list_item, parent, false);
			}

			final DiveSite diveSite = getItem(position);

            // Update parent fragment if tab
            if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
                ((DiveListTabFragment) getParentFragment()).updateOnlineSubTitles(String.valueOf(Objects.requireNonNull(diveSite).getDiveSiteCountWhenRetreived()), "");
            }

			// Set up the view with the dive sites info
			if (Objects.requireNonNull(diveSite).isPublished()) {
				view.setBackgroundColor(getResources().getColor(R.color.itemPublished));
			} else {
				view.setBackgroundColor(getResources().getColor(R.color.itemUnpublished));
			}

			// Dive Site Title
			TextView diveSiteTitle = view
					.findViewById(R.id.divesite_list_item_titleTextView);
			diveSiteTitle.setText(diveSite.getName());

			// Dive Site Location
			TextView diveSiteLocation = view
					.findViewById(R.id.divesite_list_item_locationTextView);
			diveSiteLocation.setText(diveSite.getFullLocation());

			diveSiteTitle.setTextColor(getResources().getColor(
					R.color.diveSiteListVisibleFontColor));
			diveSiteLocation.setTextColor(getResources().getColor(
					R.color.diveSiteListVisibleFontColor));

			// Dive Site Indicator Icons
			mDiveSiteIndicatorSalt = view
					.findViewById(R.id.divesite_indicate_isSalt);
			if (diveSite.isSalty()) {
				mDiveSiteIndicatorSalt.setVisibility(View.VISIBLE);
			} else {
				mDiveSiteIndicatorSalt.setVisibility(View.GONE);
			}

			mDiveSiteIndicatorFresh = view
					.findViewById(R.id.divesite_indicate_isFresh);
			if (!diveSite.isSalty()) {
				mDiveSiteIndicatorFresh.setVisibility(View.VISIBLE);
			} else {
				mDiveSiteIndicatorFresh.setVisibility(View.GONE);
			}

			mDiveSiteIndicatorShore = view
					.findViewById(R.id.divesite_indicate_isShore);
			if (diveSite.isShoreDive()) {
				mDiveSiteIndicatorShore.setVisibility(View.VISIBLE);
			} else {
				mDiveSiteIndicatorShore.setVisibility(View.GONE);
			}

			mDiveSiteIndicatorBoat = view
					.findViewById(R.id.divesite_indicate_isBoat);
			if (diveSite.isBoatDive()) {
				mDiveSiteIndicatorBoat.setVisibility(View.VISIBLE);
			} else {
				mDiveSiteIndicatorBoat.setVisibility(View.GONE);
			}

			mDiveSiteIndicatorWreck = view
					.findViewById(R.id.divesite_indicate_isWreck);
			if (diveSite.isWreck()) {
				mDiveSiteIndicatorWreck.setVisibility(View.VISIBLE);
			} else {
				mDiveSiteIndicatorWreck.setVisibility(View.GONE);
			}

			ImageButton diveSitePublished = view
					.findViewById(R.id.divesite_indicate_isPublished);
			ImageButton diveSiteUnpublished = view
					.findViewById(R.id.divesite_indicate_isUnpublished);
			if (diveSite.isPublished()) {
				diveSitePublished.setVisibility(View.VISIBLE);
				diveSiteUnpublished.setVisibility(View.GONE);
			} else {
				diveSitePublished.setVisibility(View.GONE);
				diveSiteUnpublished.setVisibility(View.VISIBLE);
			}

			ImageButton diveSiteSaved = view
					.findViewById(R.id.divesite_indicate_isSaved);
			if (diveSite.getLocalId() != -1) {
				diveSiteSaved.setVisibility(View.VISIBLE);
			} else {
				diveSiteSaved.setVisibility(View.GONE);
			}

			// Dive Site Distance
			if (getLocation() != null) {
				TextView diveSiteDistance = view
						.findViewById(R.id.divesite_list_item_distanceTextView);

				Location diveSiteLocationEnd = new Location(diveSite.getName());
				diveSiteLocationEnd.setLatitude(diveSite.getLatitude());
				diveSiteLocationEnd.setLongitude(diveSite.getLongitude());

				// Get distance to dive site in km, format to 1 dp
				NumberFormat distanceFormat = NumberFormat.getNumberInstance();
				distanceFormat.setMinimumFractionDigits(0);
				distanceFormat.setMaximumFractionDigits(1);

				float distanceToDiveSite = getLocation()
						.distanceTo(diveSiteLocationEnd);
				distanceToDiveSite = distanceToDiveSite / 1000;
				diveSiteDistance.setText(distanceFormat
						.format(distanceToDiveSite) + " km");

				// Set text colour
				diveSiteDistance.setTextColor(getResources().getColor(
						R.color.diveSiteListVisibleFontColor));
			}

			// Dive Site Button
			ImageButton diveSiteShowDetails = view
					.findViewById(R.id.divesite_list_item_showDetails);

			if (diveSite.getOnlineId() == -1) {
				diveSiteShowDetails
						.setImageResource(R.drawable.show_details_divesite_button_local);
			} else {
				diveSiteShowDetails
						.setImageResource(R.drawable.show_details_divesite_button_online);
			}

			diveSiteShowDetails.setTag(diveSite);
			diveSiteShowDetails.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Open site in review mode
					getActivity()
							.getSharedPreferences(DiveSiteManager.PREFS_FILE,
									Context.MODE_PRIVATE)
							.edit()
							.putBoolean(
									DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
									false).apply();

					// Get dive site from tag
					openDiveSite((DiveSite) v.getTag());
				}
			});

            // Initialize visibility map view
            final ViewGroup mapContainer = view.findViewById(R.id.divesite_list_item_mapView_container);
            if (diveSite == mSelectedDiveSite) {
                view.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
                setDiveSiteMap(diveSite, mapContainer);
            } else if (mDiveSiteItemMapView.getParent() == mapContainer) {
                mapContainer.setVisibility(View.GONE);
            }

            mAdditionalItemsToLoad = ListViewHelper.additionalItemCountToLoad(mListView);

			return view;
		}
	}
	
	@Override
    public void onLocationChanged(Location location) {
    	super.onLocationChanged(location);

        if (mDiveSiteManager.getLastLocation() == null ||
            mForceLocationDataRefresh ||
            (Math.abs(location.getLatitude() - mDiveSiteManager.getLastLocation().getLatitude()) > DiveSiteManager.LOCATION_COMPARE_EPSILON) ||
            (Math.abs(location.getLongitude() - mDiveSiteManager.getLastLocation().getLongitude()) > DiveSiteManager.LOCATION_COMPARE_EPSILON)) {

            mForceLocationDataRefresh = false;

            clearDiveSites();
            refreshOnlineDiveSites();
        }
    }
}
