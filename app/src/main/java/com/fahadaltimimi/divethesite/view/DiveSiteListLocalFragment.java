package com.fahadaltimimi.divethesite.view;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.data.DiveCursorLoaders;
import com.fahadaltimimi.divethesite.data.DiveCursorLoaders.DiveSiteListCursorLoader;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveSiteCursor;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.DiveSitePicture;
import com.google.android.gms.location.LocationServices;

public class DiveSiteListLocalFragment extends DiveSiteListFragment implements
		LoaderCallbacks<Cursor> {

	private boolean mArchives = false;
	private boolean mPublishMode = false;

	private DiveSiteListCursorLoader mDiveSiteListLoader = null;

    private HashMap<Long, DiveSite> mDiveSiteUpdates = new HashMap<Long, DiveSite>();

    private int mDiveSiteCount = 0;

	public static DiveSiteListLocalFragment newInstance(long diverID,
			boolean setToDiveLog) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG, setToDiveLog);
		DiveSiteListLocalFragment rf = new DiveSiteListLocalFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize the loader to load the list of Dive Sites
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}

        updateDiveSiteCount();

		mDiveSiteListLoader = (DiveSiteListCursorLoader) getLoaderManager()
				.initLoader(DiveCursorLoaders.LOAD_DIVESITE, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		// Register list view with context menu
		ListView listView = (ListView) v.findViewById(android.R.id.list);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    setSnapshot(View.INVISIBLE);
                } else {
                    setSnapshot(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //
            }
        });
		listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public boolean onActionItemClicked(final ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.menu_item_publish_divesite:
					// Confirm if user wants to publish dive sites, then publish
					// them
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.publish)
							.setMessage(R.string.publish_divesites_message)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											// Create and initialize progress
											// dialog
											mProgressDialog.setMessage(getString(R.string.publish_divesite_progress));
											mProgressDialog.setCancelable(false);
											mProgressDialog.setIndeterminate(false);
											mProgressDialog.setProgress(0);
											mProgressDialog.setMax(getListView().getCheckedItemCount());
											mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
											mProgressDialog.show();

											// Publish selected dive sites
                                            int itemsPublished = 0;
											for (int i = 0; i < getListAdapter().getCount(); i++) {
												if (getListView().isItemChecked(i)) {
													DiveSiteCursorAdapter adapter = (DiveSiteCursorAdapter) getListView().getAdapter();
													long diveSiteId = adapter.getItemId(i);
													DiveSite diveSite = mDiveSiteManager.getDiveSite(diveSiteId);

													if (diveSite.isPublished() || 
															diveSite.getUserId() != mDiveSiteManager.getLoggedInDiverId()) {
														
														if (diveSite.getUserId() != mDiveSiteManager.getLoggedInDiverId()) {
															Toast.makeText(
																	getActivity(),
																	R.string.publish_not_users_site,
																	Toast.LENGTH_SHORT)
																	.show();
														}
														
														// No need to publish
														mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
														if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
															mProgressDialog.dismiss();
                                                            if (itemsPublished > 0) {
                                                                Toast.makeText(
                                                                        getActivity(),
                                                                        R.string.dive_sites_published_message,
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
														}
													} else {
                                                        itemsPublished = itemsPublished + 1;

														mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
														mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
																	@Override
																	public void onOnlineDiveDataRetrievedComplete(
																			ArrayList<Object> resultList, String message, Boolean isError) {
	
																		if (getActivity() != null && message != null & !message.isEmpty()) {
																			Toast.makeText(
																					getActivity(), message, Toast.LENGTH_LONG).show();
																		}
	
																		// Save dive site
																		if (resultList.size() > 0) {
																			DiveSite diveSite = (DiveSite) resultList.get(0);
																			mDiveSiteManager.saveDiveSite(diveSite);
																			refreshDiveSiteList();
																		}
	
																		if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
																			mProgressDialog.dismiss();
                                                                            Toast.makeText(
                                                                                    getActivity(),
                                                                                    R.string.dive_sites_published_message,
                                                                                    Toast.LENGTH_SHORT)
                                                                                    .show();
																		} 	
																	}
	
																	@Override
																	public void onOnlineDiveDataProgress(
																			Object result) {
																		mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
																	}
	
																	@Override
																	public void onOnlineDiveDataPostBackground(
																			ArrayList<Object> resultList,
																			String message) {
																		// TODO
																		// Auto-generated
																		// method
																		// stub
	
																	}
																});
	
														mDiveSiteOnlineDatabase.publishDiveSite(diveSite);
													}
												}
											}

											mode.finish();
										}
									})
							.setNegativeButton(android.R.string.no, null)
							.show();

					return true;
					
				case R.id.menu_item_share_divesite:
					// Get summary for each dive site selected
					String combinedSummary = 
						getResources().getString(R.string.share_title_divesite) + "\n\n";
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							long diveSiteId = getListAdapter().getItemId(i);
							DiveSite diveSite = mDiveSiteManager.getDiveSite(diveSiteId);
							
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
					
				case R.id.menu_item_delete_local_divesite:
					// Confirm if user wants to delete dive sites, then delete them
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.delete)
							.setMessage(R.string.delete_divesites_message)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											// Delete selected dive sites
                                            boolean unableDeleteAll = false;
											for (int i = 0; i < getListAdapter().getCount(); i++) {
												if (getListView().isItemChecked(i)) {
													DiveSiteCursorAdapter adapter = (DiveSiteCursorAdapter) getListView().getAdapter();
                                                    DiveSiteCursor cursor = (DiveSiteCursor) adapter.getItem(i);
													DiveSite diveSite = cursor.getDiveSite();

                                                    // If dive site linked to a dive log or scheduled dive, don't delete!
                                                    int totalSiteLinks = mDiveSiteManager.queryDiveLogsCount(-1, diveSite, false) +
                                                            mDiveSiteManager.queryScheduledDiveForSiteCount(-1, diveSite.getLocalId(), true, true, "", "");

                                                    if (totalSiteLinks == 0) {
                                                        mDiveSiteManager.deleteDiveSite(diveSite.getLocalId());
                                                    } else {
                                                        unableDeleteAll = true;
                                                    }
												}
											}

                                            if (unableDeleteAll) {
                                                Toast.makeText(getActivity(),
                                                        "Cannot delete Dive Sites with existing links to downloaded Dive Logs or Scheduled Dives!",
                                                        Toast.LENGTH_LONG).show();
                                            }

											mode.finish();
											refreshDiveSiteList();
										}
									})
							.setNegativeButton(android.R.string.no, null)
							.show();

					return true;

				case R.id.menu_item_archive_local_divesite:
					// Archive selected dive sites then refresh dive site list
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							DiveSiteCursorAdapter adapter = (DiveSiteCursorAdapter) getListView()
									.getAdapter();
							long diveSiteId = adapter.getItemId(i);
							mDiveSiteManager.setDiveSiteArchive(diveSiteId,
									true);
						}
					}

					mode.finish();
					refreshDiveSiteList();

					return true;

				case R.id.menu_item_unarchive_local_divesite:
					// Un-archive selected dive sites then refresh dive site
					// list
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							DiveSiteCursorAdapter adapter = (DiveSiteCursorAdapter) getListView()
									.getAdapter();
							long diveSiteId = adapter.getItemId(i);
							mDiveSiteManager.setDiveSiteArchive(diveSiteId,
									false);
						}
					}

					mode.finish();
					refreshDiveSiteList();

					return true;

				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(
						R.menu.fragment_divesite_list_local_contextual, menu);

				MenuItem archive = menu
						.findItem(R.id.menu_item_archive_local_divesite);
				MenuItem unarchive = menu
						.findItem(R.id.menu_item_unarchive_local_divesite);
				MenuItem delete = menu
						.findItem(R.id.menu_item_delete_local_divesite);
				MenuItem publish = menu
						.findItem(R.id.menu_item_publish_divesite);

				if (mPublishMode) {
					archive.setVisible(false);
					unarchive.setVisible(false);
					delete.setVisible(false);
					publish.setVisible(true);
				} else if (mArchives) {
					archive.setVisible(false);
					unarchive.setVisible(true);
					delete.setVisible(true);
					publish.setVisible(true);
				} else {
					archive.setVisible(true);
					unarchive.setVisible(false);
					delete.setVisible(true);
					publish.setVisible(true);
				}

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
		
		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (REQUEST_NEW_DIVESITE == requestCode) {

			// Restart the loader to get any new dive sites available
			getLoaderManager().restartLoader(DiveCursorLoaders.LOAD_DIVESITE, null, this);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_divesite_list_local, menu);

		mRefreshMenuItem = menu.findItem(R.id.menu_item_refresh_divesites);
		
		MenuItem showDiveSitesArchivesMenuItem = menu
				.findItem(R.id.menu_item_showDiveSitesArchives);

		if (mArchives) {
			showDiveSitesArchivesMenuItem
					.setTitle(R.string.divesite_show_visible);
		} else {
			showDiveSitesArchivesMenuItem
					.setTitle(R.string.divesite_show_archived);
		}

		MenuItem publishModeMenuItem = menu
				.findItem(R.id.menu_item_publishMode);

		if (mPublishMode) {
			publishModeMenuItem.setTitle(R.string.divesite_show_visible);
		} else {
			publishModeMenuItem.setTitle(R.string.divesite_publish_sites);
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
		MenuItem archiveDiveSiteMenuItem = menu.findItem(R.id.menu_item_showDiveSitesArchives);
		MenuItem publishModeMenuItem = menu.findItem(R.id.menu_item_publishMode);

		// Only show add, archive, publish buttons if were looking at our own
		// sites
		if (mRestrictToDiverID != -1 && 
			mRestrictToDiverID != mDiveSiteManager.getLoggedInDiverId()) {
			if (addDiveSiteMenuItem != null) {
				addDiveSiteMenuItem.setVisible(false);
			}
			archiveDiveSiteMenuItem.setVisible(false);
			publishModeMenuItem.setVisible(false);
		} else {
			if (addDiveSiteMenuItem != null) {
				addDiveSiteMenuItem.setVisible(true);
			}
			publishModeMenuItem.setVisible(true);

			// If we're in publish mode, don't allow archive to be selected
			if (mPublishMode) {
				archiveDiveSiteMenuItem.setVisible(false);
			} else {
				archiveDiveSiteMenuItem.setVisible(true);
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
				getActivity().setResult(Activity.RESULT_OK, intent);
				getActivity().finish();
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
			
		case R.id.menu_item_refresh_divesites:
			if (mGoogleApiClient.isConnected() && mLocationEnabled) {
                LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
	    	} else {
	    		refreshDiveSiteList();
	    	}
			return true;

		case R.id.menu_item_filter_divesite_list:
			if (mListFilter.getVisibility() == View.GONE) {
				mListFilter.setVisibility(View.VISIBLE);
			} else {
				mListFilter.setVisibility(View.GONE);
			}

			return true;

		case R.id.menu_item_publishMode:
			mArchives = false;
			mPublishMode = !mPublishMode;

			getActivity().invalidateOptionsMenu();
			refreshDiveSiteList();
			return true;

		case R.id.menu_item_showDiveSitesArchives:
			mArchives = !mArchives;

			getActivity().invalidateOptionsMenu();
			refreshDiveSiteList();
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
			titleFilter = mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, "")
					.trim();
		} else {
			titleFilter = mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, "").trim();
		}
		String countryFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, "").trim();
		String stateFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "").trim();
		String cityFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "").trim();

		Boolean publishedFilter = mPrefs.getBoolean(
				DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_PUBLISHED, true);
		Boolean unPublishedFilter = mPrefs.getBoolean(
				DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED, true);

		ArrayList<String> filterNotification = new ArrayList<String>();

		if (mArchives) {
			filterNotification.add(getResources().getString(
					R.string.divesite_show_archived));
		}

		if (mPublishMode) {
			filterNotification.add(getResources().getString(
					R.string.divesite_publish_sites));
		}

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

	@Override
	protected void refreshDiveSiteList() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}
		
		updateFilterNotification();
        updateDiveSiteCount();

		// Initialize the loader to load the list of Dive Sites
		mDiveSiteListLoader = (DiveSiteListCursorLoader) getLoaderManager().restartLoader(DiveCursorLoaders.LOAD_DIVESITE, null, this);
	}

    private void updateDiveSiteCount() {
        // Refresh dive site count
        if (mArchives) {
            mDiveSiteCount = DiveSiteManager.get(getActivity()).queryArchivedDiveSitesCount(
                generateFilterSelection(getActivity(), mSetToDiveLog),
                generateFilterSelectionArgs(getActivity(), mSetToDiveLog),
                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
                mRestrictToDiverID);
        } else if (mPublishMode) {
            mDiveSiteCount = DiveSiteManager.get(getActivity()).queryPublishedDiveSitesCount(true,
                generateFilterSelection(getActivity(), mSetToDiveLog),
                generateFilterSelectionArgs(getActivity(), mSetToDiveLog),
                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
                mRestrictToDiverID);
        } else{
            mDiveSiteCount = DiveSiteManager.get(getActivity()).queryVisibleDiveSitesCount(
                generateFilterSelection(getActivity(), mSetToDiveLog),
                generateFilterSelectionArgs(getActivity(), mSetToDiveLog),
                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
                mRestrictToDiverID);
        }

        // Update parent fragment if tab
        if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
            ((DiveListTabFragment) getParentFragment()).updateLocalSubTitles(String.valueOf(mDiveSiteCount), "");
        }
    }

	@Override
	protected DiveSite getDiveSiteItemClick(int position, long id) {
		return mDiveSiteManager.getDiveSite(id);
	}

	private class DiveSiteCursorAdapter extends CursorAdapter {

		private DiveSiteCursor mDiveSiteCursor;

		public DiveSiteCursorAdapter(Context context, DiveSiteCursor cursor) {
			super(context, cursor, 0);
			mDiveSiteCursor = cursor;
		}

		@Override
		public void changeCursor(Cursor cursor) {
			mDiveSiteCursor = (DiveSiteCursor) cursor;
			super.changeCursor(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.divesite_list_item, parent,
					false);

			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			DiveSite diveSite = mDiveSiteCursor.getDiveSite();

			view.setTag(diveSite);
			
			// Set up the view with the dive sites info
			updateView(view, diveSite);
			
			if (diveSite.getOnlineId() != -1) {
				// Check if dive site needs to be refreshed
				Date checkModifiedDate = new Date(diveSite.getLastModifiedOnline().getTime());
				mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
				mDiveSiteOnlineDatabase
					.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
						@Override
						public void onOnlineDiveDataPostBackground(
								ArrayList<Object> resultList, String message) {
							// TODO Auto-generated method stub
							
						}
	
						@Override
						public void onOnlineDiveDataRetrievedComplete(
								ArrayList<Object> resultList, String message, Boolean isError) {
							if (resultList.size() > 0) {
								// Dive Site retreived, so a refresh is required
                                DiveSite updatedDiveSite = (DiveSite)resultList.get(0);
                                DiveSite existingDiveSite = mDiveSiteManager.getDiveSite(updatedDiveSite.getLocalId());

                                if (existingDiveSite != null) {
                                    if (updatedDiveSite.getLastModifiedOnline().after(existingDiveSite.getLastModifiedOnline())) {
                                        mDiveSiteUpdates.put(existingDiveSite.getLocalId(), updatedDiveSite);
                                        if (!existingDiveSite.requiresRefresh()) {
                                            existingDiveSite.setRequiresRefresh(true);
                                            mDiveSiteManager.setDiveSiteRequiresRefresh(existingDiveSite);
                                            refreshDiveSiteList();
                                        }
                                    } else if (existingDiveSite.requiresRefresh()) {
                                        existingDiveSite.setRequiresRefresh(false);
                                        mDiveSiteManager.saveDiveSite(existingDiveSite);
                                        refreshDiveSiteList();
                                    }
                                }
							}
						}
	
						@Override
						public void onOnlineDiveDataProgress(Object result) {
							// TODO Auto-generated method stub
							
						}
						
					});
				mDiveSiteOnlineDatabase.getDiveSite(checkModifiedDate, diveSite.getOnlineId(), diveSite);
			}
		}
	}
	
	private void updateView(View view, DiveSite diveSite) {
		if (diveSite.isPublished()) {
			view.setBackgroundColor(getResources().getColor(
					R.color.itemPublished));
		} else {
			view.setBackgroundColor(getResources().getColor(
					R.color.itemUnpublished));
		}

		// Dive Site Title
		TextView diveSiteTitle = (TextView) view.findViewById(R.id.divesite_list_item_titleTextView);
		diveSiteTitle.setText(diveSite.getName());

		// Dive Site Location
		TextView diveSiteLocation = (TextView) view
				.findViewById(R.id.divesite_list_item_locationTextView);
		diveSiteLocation.setText(diveSite.getFullLocation());

		// Set text colour
		if (mArchives) {
			diveSiteTitle.setTextColor(getResources().getColor(
					R.color.diveSiteListArchivesFontColor));
			diveSiteLocation.setTextColor(getResources().getColor(
					R.color.diveSiteListArchivesFontColor));
		} else {
			diveSiteTitle.setTextColor(getResources().getColor(
					R.color.diveSiteListVisibleFontColor));
			diveSiteLocation.setTextColor(getResources().getColor(
					R.color.diveSiteListVisibleFontColor));
		}

		// Dive Site Indicator Icons
		mDiveSiteIndicatorSalt = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isSalt);
		if (diveSite.isSalty()) {
			mDiveSiteIndicatorSalt.setVisibility(View.VISIBLE);
		} else {
			mDiveSiteIndicatorSalt.setVisibility(View.GONE);
		}

		mDiveSiteIndicatorFresh = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isFresh);
		if (!diveSite.isSalty()) {
			mDiveSiteIndicatorFresh.setVisibility(View.VISIBLE);
		} else {
			mDiveSiteIndicatorFresh.setVisibility(View.GONE);
		}

		mDiveSiteIndicatorShore = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isShore);
		if (diveSite.isShoreDive()) {
			mDiveSiteIndicatorShore.setVisibility(View.VISIBLE);
		} else {
			mDiveSiteIndicatorShore.setVisibility(View.GONE);
		}

		mDiveSiteIndicatorBoat = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isBoat);
		if (diveSite.isBoatDive()) {
			mDiveSiteIndicatorBoat.setVisibility(View.VISIBLE);
		} else {
			mDiveSiteIndicatorBoat.setVisibility(View.GONE);
		}

		mDiveSiteIndicatorWreck = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isWreck);
		if (diveSite.isWreck()) {
			mDiveSiteIndicatorWreck.setVisibility(View.VISIBLE);
		} else {
			mDiveSiteIndicatorWreck.setVisibility(View.GONE);
		}

		ImageButton diveSitePublished = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isPublished);
		ImageButton diveSiteUnpublished = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isUnpublished);
		if (diveSite.isPublished()) {
			diveSitePublished.setVisibility(View.VISIBLE);
			diveSiteUnpublished.setVisibility(View.GONE);
		} else {
			diveSitePublished.setVisibility(View.GONE);
			diveSiteUnpublished.setVisibility(View.VISIBLE);
		}

		ImageButton diveSiteSaved = (ImageButton) view
				.findViewById(R.id.divesite_indicate_isSaved);
		if (diveSite.getLocalId() != -1) {
			diveSiteSaved.setVisibility(View.VISIBLE);
		} else {
			diveSiteSaved.setVisibility(View.INVISIBLE);
		}

		// Dive Site Distance
		if (getLocation() != null) {
			TextView diveSiteDistance = (TextView) view
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
			if (mArchives) {
				diveSiteDistance.setTextColor(getResources().getColor(
						R.color.diveSiteListArchivesFontColor));
			} else {
				diveSiteDistance.setTextColor(getResources().getColor(
						R.color.diveSiteListVisibleFontColor));
			}
		}

		// Dive Site Button
		ImageButton diveSiteShowDetails = (ImageButton) view
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
		
		ImageButton requiresRefresh =
				(ImageButton)view.findViewById(R.id.divesite_indicate_refresh_required);
        if (diveSite.requiresRefresh()) {
            requiresRefresh.setVisibility(View.VISIBLE);
            requiresRefresh.setEnabled(mDiveSiteUpdates.get(diveSite.getLocalId()) != null);
        } else {
            requiresRefresh.setVisibility(View.GONE);
        }

        requiresRefresh.setTag(diveSite);
        requiresRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // Ask user if they want to update
                DiveSite existingDiveSite = (DiveSite) v.getTag();
                final DiveSite updatedDiveSite = mDiveSiteUpdates.get(existingDiveSite.getLocalId());

                String updateMessage =
                        getResources().getString(R.string.divesite_requiresRefresh_message);
                if (!existingDiveSite.isPublished()) {
                    updateMessage = updateMessage +
                            getResources().getString(R.string.divesite_requiresRefresh_overwrite);
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.divesite_requiresRefresh)
                        .setMessage(updateMessage)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        mProgressDialog.setMessage(getString(R.string.save_divelogs_progress));
                                        mProgressDialog.setCancelable(false);
                                        mProgressDialog.setIndeterminate(false);
                                        mProgressDialog.setProgress(0);
                                        mProgressDialog.setMax(1);
                                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                        mProgressDialog.show();

                                        updatedDiveSite.setRequiresRefresh(false);
                                        mDiveSiteManager.saveDiveSite(updatedDiveSite);

                                        // Save logs for site
                                        for (int j = 0; j < updatedDiveSite.getDiveLogs().size(); j++) {
                                            mDiveSiteManager.saveDiveLog(updatedDiveSite.getDiveLogs().get(j));
                                        }

                                        if (updatedDiveSite.getPicturesCount() > 0) {
                                            // Download pictures for each dive site, save each one
                                            for (int j = 0; j < updatedDiveSite.getPicturesCount(); j++) {
                                                if (updatedDiveSite.getPicture(j).getBitmapURL() != null
                                                        && !updatedDiveSite.getPicture(j)
                                                        .getBitmapURL().isEmpty()) {

                                                    LoadOnlineImageTask task;
                                                    if (j == updatedDiveSite.getPicturesCount() - 1) {
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
                                                                mDiveSiteManager.saveDiveSitePicture(picture);

                                                                mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
                                                                mProgressDialog.dismiss();
                                                                Toast.makeText(
                                                                        getActivity(),
                                                                        R.string.dive_sites_saved_message,
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
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
                                                                mDiveSiteManager.saveDiveSitePicture(picture);
                                                            }
                                                        };
                                                    }

                                                    task.setTag(updatedDiveSite.getPicture(j));
                                                    task.execute(updatedDiveSite.getPicture(j)
                                                            .getBitmapURL());
                                                } else if (j == updatedDiveSite.getPicturesCount()) {
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
                                            mProgressDialog.dismiss();
                                            Toast.makeText(getActivity(),
                                                    R.string.dive_sites_saved_message,
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        refreshDiveSiteList();
                                    }
                                })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        // Initialize visibility map view
        final ViewGroup mapContainer = (ViewGroup) view.findViewById(R.id.divesite_list_item_mapView_container);
        if (mSelectedDiveSite != null && diveSite.getLocalId() == mSelectedDiveSite.getLocalId()) {
            view.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
            setDiveSiteMap(diveSite, mapContainer);
        } else if (mDiveSiteItemMapView.getParent() == mapContainer) {
            mDiveSiteItemMapView.setVisibility(View.GONE);
        }
	}

    public static String generateFilterSelection(Context context, Boolean setToDiveLog) {
        // Generate filter selection and filter selection arguments
        SharedPreferences prefs = context.getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

        String filterSelection = "";

        String titleFilter = "";
        if (setToDiveLog) {
            titleFilter = prefs.getString(
                    DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, "");
        } else {
            titleFilter = prefs.getString(
                    DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, "");
        }
        String countryFilter = prefs.getString(
                DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, context.getResources()
                        .getString(R.string.filter_list_all));
        String stateFilter = prefs.getString(
                DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "");
        String cityFilter = prefs.getString(
                DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "");

        Boolean publishedFilter = prefs.getBoolean(
                DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_PUBLISHED, true);
        Boolean unPublishedFilter = prefs.getBoolean(
                DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED, true);

        if (!titleFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !titleFilter.isEmpty()) {
            filterSelection = filterSelection + " AND "
                    + DiveSiteDatabaseHelper.COLUMN_DIVESITE_NAME + " LIKE ? ";
        }

        if (!countryFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !countryFilter.isEmpty()) {
            filterSelection = filterSelection + " AND "
                    + DiveSiteDatabaseHelper.COLUMN_DIVESITE_COUNTRY
                    + " LIKE ? ";
        }

        if (!stateFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !stateFilter.isEmpty()) {
            filterSelection = filterSelection + " AND "
                    + DiveSiteDatabaseHelper.COLUMN_DIVESITE_PROVINCE
                    + " LIKE ? ";
        }

        if (!cityFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !cityFilter.isEmpty()) {
            filterSelection = filterSelection + " AND "
                    + DiveSiteDatabaseHelper.COLUMN_DIVESITE_CITY + " LIKE ? ";
        }

        if (!unPublishedFilter) {
            filterSelection = filterSelection + " AND "
                    + DiveSiteDatabaseHelper.COLUMN_DIVESITE_IS_PUBLISHED
                    + " = 1";
        }

        if (!publishedFilter) {
            filterSelection = filterSelection + " AND "
                    + DiveSiteDatabaseHelper.COLUMN_DIVESITE_IS_PUBLISHED
                    + " = 0";
        }

        return filterSelection;
    }

    public static ArrayList<String> generateFilterSelectionArgs(Context context, Boolean setToDiveLog) {
        // Generate filter selection and filter selection arguments
        SharedPreferences prefs = context.getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

        ArrayList<String> filterSelectionArgs = new ArrayList<String>();

        String titleFilter = "";
        if (setToDiveLog) {
            titleFilter = prefs.getString(
                    DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, "");
        } else {
            titleFilter = prefs.getString(
                    DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, "");
        }
        String countryFilter = prefs.getString(
                DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, context.getResources()
                        .getString(R.string.filter_list_all));
        String stateFilter = prefs.getString(
                DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "");
        String cityFilter = prefs.getString(
                DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "");

        if (!titleFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !titleFilter.isEmpty()) {
            filterSelectionArgs.add("%" + titleFilter + "%");
        }

        if (!countryFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !countryFilter.isEmpty()) {
            filterSelectionArgs.add("%" + countryFilter + "%");
        }

        if (!stateFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !stateFilter.isEmpty()) {
            filterSelectionArgs.add("%" + stateFilter + "%");
        }

        if (!cityFilter.equals(context.getResources().getString(
                R.string.filter_list_all))
                && !cityFilter.isEmpty()) {
            filterSelectionArgs.add("%" + cityFilter + "%");
        }

        return filterSelectionArgs;
    }

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		// Generate filter
		if (!mPublishMode) {
			return new DiveSiteListCursorLoader(getActivity(), mArchives,
					mRestrictToDiverID,
                    generateFilterSelection(getActivity(), mSetToDiveLog),
                    generateFilterSelectionArgs(getActivity(), mSetToDiveLog));
		} else {
			return new DiveSiteListCursorLoader(getActivity(), false, true,
                    generateFilterSelection(getActivity(), mSetToDiveLog),
                    generateFilterSelectionArgs(getActivity(), mSetToDiveLog));
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		// Create an adapter to point at this cursor
		if (getListAdapter() == null) {
			DiveSiteCursorAdapter adapter = new DiveSiteCursorAdapter(
					getActivity(), (DiveSiteCursor) cursor);
			setListAdapter(adapter);
		} else {
			((DiveSiteCursorAdapter) getListAdapter()).changeCursor(cursor);
		}
		
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Stop using the cursor (via the adapter)
		((DiveSiteCursorAdapter) getListAdapter()).swapCursor(null);
	}
	
	@Override
    public void onLocationChanged(Location location) {
    	super.onLocationChanged(location);
    	
    	// Refresh dive site list to set new distance to each site
    	refreshDiveSiteList();
    }
}
