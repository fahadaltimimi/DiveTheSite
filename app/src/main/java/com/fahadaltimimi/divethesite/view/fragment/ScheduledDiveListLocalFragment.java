package com.fahadaltimimi.divethesite.view.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.data.DiveCursorLoaders;
import com.fahadaltimimi.divethesite.data.DiveCursorLoaders.ScheduledDiveListCursorLoader;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveDiveSiteCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.ScheduledDiveUserCursor;
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

public class ScheduledDiveListLocalFragment extends ScheduledDiveListFragment implements
		LoaderCallbacks<Cursor> {
	
	private static final String ARG_LOADER_DIVER_ID = "LOADER_DIVER_ID";
	private static final String ARG_LOADER_DIVESITE = "LOADER_DIVESITE";
	private static final String ARG_LOADER_SCHEDULEDDIVE_ID = "LOADER_DIVELOG_ID";

	private boolean mPublishMode = false;

	private ScheduledDiveListCursorLoader mScheduledDiveListLoader = null;

    private HashMap<Long, ScheduledDive> mScheduledDiveUpdates = new HashMap<Long, ScheduledDive>();

    private int mScheduledDiveCount = 0;

	public static ScheduledDiveListLocalFragment newInstance(long diverID,
			DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);

		ScheduledDiveListLocalFragment rf = new ScheduledDiveListLocalFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVER_ID, mRestrictToDiverID);
		loaderArgs.putParcelable(ARG_LOADER_DIVESITE, mDiveSite);
        updateScheduledDiveCount();
		
		mScheduledDiveListLoader = 
				(ScheduledDiveListCursorLoader) getLoaderManager().initLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE, loaderArgs, this);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		// Register list view with context menu
		ListView listView = v.findViewById(android.R.id.list);

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

			private MenuItem mEditMenu;
			
			@Override
			public boolean onActionItemClicked(final ActionMode mode,
					MenuItem item) {
				switch (item.getItemId()) {
				case R.id.menu_item_publish_scheduleddive:
					// Confirm if user wants to publish scheduled dive, then publish
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.publish)
							.setMessage(R.string.publish_scheduleddives_message)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int whichButton) {
											// Create and initialize progress dialog
											mProgressDialog.setMessage(getString(R.string.publish_scheduleddives_progress));
											mProgressDialog.setCancelable(false);
											mProgressDialog.setIndeterminate(false);
											mProgressDialog.setProgress(0);
											mProgressDialog.setMax(getListView().getCheckedItemCount());
											mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
											mProgressDialog.show();

											// Publish selected scheduled dives
											for (int i = 0; i < getListAdapter().getCount(); i++) {
												if (getListView().isItemChecked(i)) {
													ScheduledDiveCursorAdapter adapter = 
														(ScheduledDiveCursorAdapter) getListView().getAdapter();
													long scheduledDiveId = adapter.getItemId(i);
													ScheduledDive scheduledDive = mDiveSiteManager.getScheduledDive(scheduledDiveId);
													
													if (scheduledDive.isPublished() || 
															scheduledDive.getSubmitterId() != mDiveSiteManager.getLoggedInDiverId()) {
														
														if (scheduledDive.getSubmitterId() != mDiveSiteManager.getLoggedInDiverId()) {
															Toast.makeText(
																	getActivity(),
																	R.string.publish_not_users_scheduled,
																	Toast.LENGTH_SHORT)
																	.show();
														}
														
														// No need to publish
														mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
														if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
															mProgressDialog.dismiss();
															Toast.makeText(
																	getActivity(),
																	R.string.scheduleddives_published_message,
																	Toast.LENGTH_SHORT)
																	.show();
														}
													} else {
														scheduledDive.setScheduledDiveDiveSites(mDiveSiteManager.getScheduledDiveDiveSites(scheduledDiveId));
														scheduledDive.setScheduledDiveUsers(mDiveSiteManager.getScheduledDiveUsers(scheduledDiveId));

                                                        publishScheduledDiveDiveSites(scheduledDive);
													}
												}
											}

											mode.finish();
										}
									})
							.setNegativeButton(android.R.string.no, null)
							.show();

					return true;
					
				case R.id.menu_item_edit_scheduleddive:
					for (int i = getListAdapter().getCount() - 1; i >= 0; i--) {
						if (getListView().isItemChecked(i)) {							
							ScheduledDiveCursorAdapter adapter = 
								(ScheduledDiveCursorAdapter) getListView().getAdapter();
							long scheduledDiveId = adapter.getItemId(i);
							ScheduledDive scheduledDive = mDiveSiteManager.getScheduledDive(scheduledDiveId);
							if (scheduledDive.getSubmitterId() == mDiveSiteManager.getLoggedInDiverId()) {
								scheduledDive.setScheduledDiveDiveSites(mDiveSiteManager.getScheduledDiveDiveSites(scheduledDiveId));
								for (int j = 0; j < scheduledDive.getScheduledDiveDiveSites().size(); j++) {
									ScheduledDiveDiveSite scheduledDiveDiveSite = scheduledDive.getScheduledDiveDiveSites().get(j);
									scheduledDiveDiveSite.setDiveSite(mDiveSiteManager.getDiveSite(scheduledDiveDiveSite.getDiveSiteLocalId()));
								}
								scheduledDive.setScheduledDiveUsers(mDiveSiteManager.getScheduledDiveUsers(scheduledDiveId));
								openScheduledDive(scheduledDive);
							} else {
								Toast.makeText(getActivity(), 
										R.string.edit_own_scheduleddive_fail, Toast.LENGTH_LONG);
							}
						}
					}
					return true;

				case R.id.menu_item_share_scheduleddive:
					// Get summary for each scheduled dive selected
					String combinedSummary = 
						getResources().getString(R.string.share_title_scheduleddive) + "\n\n";
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							long scheduledDiveId = getListAdapter().getItemId(i);
							ScheduledDive scheduledDive = mDiveSiteManager.getScheduledDive(scheduledDiveId);
							scheduledDive.setScheduledDiveDiveSites(mDiveSiteManager.getScheduledDiveDiveSites(scheduledDiveId));
							for (int j = 0; j < scheduledDive.getScheduledDiveDiveSites().size(); j++) {
								ScheduledDiveDiveSite scheduledDiveDiveSite = scheduledDive.getScheduledDiveDiveSites().get(j);
								scheduledDiveDiveSite.setDiveSite(mDiveSiteManager.getDiveSite(scheduledDiveDiveSite.getDiveSiteLocalId()));
							}
							
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
					
				case R.id.menu_item_delete_local_scheduleddive:
					// Confirm if user wants to delete
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.delete)
							.setMessage(R.string.delete_scheduleddives_message)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int whichButton) {
											// Delete selected scheduled dives
											for (int i = 0; i < getListAdapter().getCount(); i++) {
												if (getListView().isItemChecked(i)) {
													ScheduledDiveCursorAdapter adapter = 
														(ScheduledDiveCursorAdapter) getListView().getAdapter();
													long scheduledDiveId = adapter.getItemId(i);
													mDiveSiteManager.deleteScheduledDive(scheduledDiveId);
												}
											}

											mode.finish();
											refreshScheduledDiveList();
										}
									})
							.setNegativeButton(android.R.string.no, null)
							.show();

					return true;

				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.fragment_scheduleddive_list_local_contextual, menu);

				MenuItem delete = menu.findItem(R.id.menu_item_delete_local_scheduleddive);
				MenuItem publish = menu.findItem(R.id.menu_item_publish_scheduleddive);

				mEditMenu = menu.findItem(R.id.menu_item_edit_scheduleddive);
				
				if (mPublishMode) {
					delete.setVisible(false);
					publish.setVisible(true);
				} else {
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

    private void publishScheduledDiveDiveSites(final ScheduledDive scheduledDive) {

        // First generate list with dive sites to publish
        final ArrayList<DiveSite> diveSitesToPublish = new ArrayList<DiveSite>();

        for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
            DiveSite diveSite = mDiveSiteManager.getDiveSite(scheduledDive.getScheduledDiveDiveSites().get(i).getDiveSiteLocalId());
            if (diveSite != null && (!diveSite.isPublished() && diveSite.getUserId() == mDiveSiteManager.getLoggedInDiverId())) {
                diveSitesToPublish.add(diveSite);
            }
        }

        if (diveSitesToPublish.size() == 0) {
            // No sites to publish, publish scheduled dive
            publishScheduledDive(scheduledDive);
        } else {
            for (int i = 0; i < diveSitesToPublish.size(); i++) {
                final boolean lastDiveSiteToPublish = i == diveSitesToPublish.size() - 1;

                DiveSite diveSite = diveSitesToPublish.get(i);

                mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
                mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                    @Override
                    public void onOnlineDiveDataRetrievedComplete(
                            ArrayList<Object> resultList, String message, Boolean isError) {

                        if (getActivity() != null && message != null & !message.isEmpty()) {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                        }

                        // Save dive site
                        if (resultList.size() > 0) {
                            DiveSite diveSite = (DiveSite) resultList.get(0);
                            mDiveSiteManager.saveDiveSite(diveSite);

                            // Update scheduledDive before publishing by finding and replace dive site with same local ID
                            for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
                                if (scheduledDive.getScheduledDiveDiveSites().get(i).getDiveSiteLocalId() == diveSite.getLocalId()) {

                                    scheduledDive.getScheduledDiveDiveSites().get(i).setDiveSite(diveSite);
                                    scheduledDive.getScheduledDiveDiveSites().get(i).setDiveSiteOnlineId(diveSite.getOnlineId());

                                    break;
                                }
                            }
                        }

                        if (lastDiveSiteToPublish) {
                            publishScheduledDive(scheduledDive);
                        }
                    }

                    @Override
                    public void onOnlineDiveDataProgress(Object result) {
                        mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
                    }

                    @Override
                    public void onOnlineDiveDataPostBackground(ArrayList<Object> resultList, String message) {
                        //
                    }
                });

                mDiveSiteOnlineDatabase.publishDiveSite(diveSite);
            }
        }
    }

    private void publishScheduledDive(ScheduledDive scheduledDive) {
        mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
        mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

            @Override
            public void onOnlineDiveDataRetrievedComplete(
                    ArrayList<Object> resultList, String message, Boolean isError) {

                if (getActivity() != null && message != null && !message.isEmpty()) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }

                // Save scheduled dive
                if (resultList.size() > 0) {
                    ScheduledDive scheduledDive = (ScheduledDive) resultList.get(0);
                    mDiveSiteManager.saveScheduledDive(scheduledDive);
                    refreshScheduledDiveList();
                }

                if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(
                            getActivity(),
                            R.string.scheduleddives_published_message,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onOnlineDiveDataProgress(
                    Object result) {
                mProgressDialog
                        .setProgress(mProgressDialog
                                .getProgress() + 1);
            }

            @Override
            public void onOnlineDiveDataPostBackground(
                    ArrayList<Object> resultList,
                    String message) {
            }
        });

        mDiveSiteOnlineDatabase.publishScheduledDive(scheduledDive);
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_scheduleddive_list_local, menu);

		MenuItem publishModeMenuItem = 
			menu.findItem(R.id.menu_item_publishMode);

		if (mPublishMode) {
			publishModeMenuItem.setTitle(R.string.scheduleddive_show_visible);
		} else {
			publishModeMenuItem.setTitle(R.string.scheduleddive_publish_logs);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem publishModeMenuItem = menu.findItem(R.id.menu_item_publishMode);

		// Only show publish and edit button if were looking at our own items
		if (mRestrictToDiverID != -1
			&& mRestrictToDiverID != mDiveSiteManager.getLoggedInDiverId()) {
			publishModeMenuItem.setVisible(false);
		} else {
			publishModeMenuItem.setVisible(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_publishMode:
			mPublishMode = !mPublishMode;

			getActivity().invalidateOptionsMenu();
			refreshScheduledDiveList();
			return true;
			
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
	public void onResume() {
		super.onResume();
		
		// Restart the loader to get any new items available
		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVER_ID, mRestrictToDiverID);
		loaderArgs.putParcelable(ARG_LOADER_DIVESITE, mDiveSite);

        updateScheduledDiveCount();

		if (mScheduledDiveListLoader != null) {
			getLoaderManager().restartLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE, loaderArgs, this);
		} else {
			mScheduledDiveListLoader = 
					(ScheduledDiveListCursorLoader) getLoaderManager().initLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE, loaderArgs, this);
		}
	}

	@Override
	protected void updateFilterNotification() {
		// Determine filter to set from selection
		String titleFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "").trim();
		String countryFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, "").trim();
		String stateFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "").trim();
		String cityFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "").trim();
		
		String previousDaysFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "").trim();
		String nextDaysFilter = mPrefs.getString(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "").trim();

		Boolean publishedFilter = mPrefs.getBoolean(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED, true);
		Boolean unPublishedFilter = mPrefs.getBoolean(
				DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED, true);

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
		
		if (mPublishMode) {
			filterNotification.add(getResources().getString(R.string.scheduleddive_publish));
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
        super.refreshListView();
        refreshScheduledDiveList();
    }

	@Override
	protected void refreshScheduledDiveList() {
		super.refreshScheduledDiveList();

		updateFilterNotification();
        updateScheduledDiveCount();

		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVER_ID, mRestrictToDiverID);
		loaderArgs.putParcelable(ARG_LOADER_DIVESITE, mDiveSite);

		if (mScheduledDiveListLoader != null) {
			getLoaderManager().restartLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE, loaderArgs, this);
		} else {
			mScheduledDiveListLoader = 
					(ScheduledDiveListCursorLoader) getLoaderManager().initLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE, loaderArgs, this);
		}
	}

    private void updateScheduledDiveCount() {
        String titleFilter = mPrefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "").trim();
        String countryFilter = mPrefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, getResources()
                .getString(R.string.filter_list_all)).trim();
        if (countryFilter.trim().equals(getResources().getString(R.string.filter_list_all))) {
            countryFilter = "";
        }

        String stateFilter = mPrefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "").trim();
        String cityFilter = mPrefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "").trim();

        String previousDaysFilter = mPrefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "").trim();
        String nextDaysFilter = mPrefs.getString(DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "").trim();

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

        Boolean publishedFilter = mPrefs.getBoolean(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED, true);
        Boolean unPublishedFilter = mPrefs.getBoolean(
                DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED, true);

        if (mRestrictToDiverID != -1) {
            mScheduledDiveCount = mDiveSiteManager.queryScheduledDiveForUserCount(mRestrictToDiverID, mRestrictToDiverID,
                    publishedFilter, unPublishedFilter,titleFilter, countryFilter,
                    stateFilter, cityFilter, timeStampStartFilter, timeStampEndFilter);
        } else if (mDiveSite != null) {
            mScheduledDiveCount = mDiveSiteManager.queryScheduledDiveForSiteCount(mRestrictToDiverID, mDiveSite.getLocalId(),
                    publishedFilter, unPublishedFilter, timeStampStartFilter, timeStampEndFilter);
        } else {
            mScheduledDiveCount = mDiveSiteManager.queryScheduledDiveForSubmitterCount(mRestrictToDiverID,
                    publishedFilter, unPublishedFilter,titleFilter, countryFilter,
                    stateFilter, cityFilter, timeStampStartFilter, timeStampEndFilter);
        }

        // Update parent fragment if tab
        if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
            ((DiveListTabFragment) getParentFragment()).updateLocalSubTitles(String.valueOf(mScheduledDiveCount), "");
        }
    }

	@Override
	protected ScheduledDive getScheduledDiveItemClick(int position, long id) {
		ScheduledDive scheduledDive = mDiveSiteManager.getScheduledDive(id);
		
		scheduledDive.setScheduledDiveDiveSites(mDiveSiteManager.getScheduledDiveDiveSites(id));
		for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++){
			ScheduledDiveDiveSite scheduledDiveDiveSite = 
					scheduledDive.getScheduledDiveDiveSites().get(i);
			if (scheduledDiveDiveSite.getDiveSiteLocalId() != -1) {
				scheduledDiveDiveSite.setDiveSite(mDiveSiteManager.getDiveSite(scheduledDiveDiveSite.getDiveSiteLocalId()));
			}
		}
		
		scheduledDive.setScheduledDiveUsers(mDiveSiteManager.getScheduledDiveUsers(id));
		return scheduledDive;
	}

	private class ScheduledDiveCursorAdapter extends CursorAdapter {

		private ScheduledDiveCursor mScheduledDiveCursor;

		public ScheduledDiveCursorAdapter(Context context, ScheduledDiveCursor cursor) {
			super(context, cursor, 0);
			mScheduledDiveCursor = cursor;
		}

		@Override
		public void changeCursor(Cursor cursor) {
			mScheduledDiveCursor = (ScheduledDiveCursor) cursor;
			super.changeCursor(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.scheduleddive_list_item, parent, false);

			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ScheduledDive scheduledDive = mScheduledDiveCursor.getScheduledDive();

            view.setTag(scheduledDive);
			
			updateView(view, scheduledDive);
			
			if (scheduledDive.getOnlineId() != -1) {
				// Check if item needs to be refreshed
				Date checkModifiedDate = new Date(scheduledDive.getLastModifiedOnline().getTime());
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
								// Scheduled Dive retreived, so a refresh is required
                                ScheduledDive updatedScheduledDive = (ScheduledDive)resultList.get(0);
                                ScheduledDive existingScheduledDive = mDiveSiteManager.getScheduledDive(updatedScheduledDive.getLocalId());

                                if (existingScheduledDive != null) {
                                    if (updatedScheduledDive.getLastModifiedOnline().after(existingScheduledDive.getLastModifiedOnline())) {
                                        boolean refreshList = false;

                                        if (!existingScheduledDive.requiresRefresh()) {
                                            existingScheduledDive.setRequiresRefresh(true);
                                            mDiveSiteManager.setScheduledDiveRequiresRefresh(existingScheduledDive);
                                            refreshList = true;
                                        }
                                        ScheduledDive existingUpdatedScheduledDive =
                                            mScheduledDiveUpdates.get(existingScheduledDive.getLocalId());
                                        if (existingUpdatedScheduledDive == null ||
                                                updatedScheduledDive.getLastModifiedOnline().after(existingUpdatedScheduledDive.getLastModifiedOnline())) {
                                            mScheduledDiveUpdates.put(existingScheduledDive.getLocalId(), updatedScheduledDive);
                                            refreshList = true;
                                        }
                                        if (refreshList) {
                                            refreshScheduledDiveList();
                                        }
                                    } else if (existingScheduledDive.requiresRefresh()) {
                                        existingScheduledDive.setRequiresRefresh(false);
                                        mDiveSiteManager.saveScheduledDive(existingScheduledDive);
                                        refreshScheduledDiveList();
                                    }
                                }
							}
						}
	
						@Override
						public void onOnlineDiveDataProgress(Object result) {
							// TODO Auto-generated method stub
							
						}
						
					});
				mDiveSiteOnlineDatabase.getScheduledDive(checkModifiedDate, scheduledDive.getOnlineId(), scheduledDive);
			}
		} 
	}
	
	private void updateView(View view, final ScheduledDive scheduledDive) {
		// Save id to view for later updating with buddies and stops
		mScheduledDiveListItemViews.put(scheduledDive.getLocalId(), view);
		if (mScheduledDiveListItemLoaderIDs.get(scheduledDive.getLocalId()) == null) {
			mScheduledDiveListItemLoaderIDs.put(scheduledDive.getLocalId(),
					mScheduledDiveListItemLoaderIDs.size());
		}

		// Initialize loaders and add arguments
		LoaderManager lm = getLoaderManager();
		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_SCHEDULEDDIVE_ID, scheduledDive.getLocalId());

		lm.restartLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE_SITES_INDEX
				+ mScheduledDiveListItemLoaderIDs.get(scheduledDive.getLocalId()),
				loaderArgs, ScheduledDiveListLocalFragment.this);
		lm.restartLoader(DiveCursorLoaders.LOAD_SCHEDULEDDIVE_USERS_INDEX
				+ mScheduledDiveListItemLoaderIDs.get(scheduledDive.getLocalId()),
				loaderArgs, ScheduledDiveListLocalFragment.this);
			
		// Set up the view with the scheduled dives info
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
		diverCountView.setText(String.format(getResources().getString(R.string.scheduleddive_list_diver_count), 0));
		diverCountView.setTag(0);
		
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
				
				if (scheduledDive.getOnlineId() != -1) {
					DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
					diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
		
								@Override
								public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {
									if (resultList.size() > 0) {
										// Update Scheduled Dive
										ScheduledDiveUser scheduledDiveUser = (ScheduledDiveUser) resultList.get(0);
										View scheduledDiveView = 
												mScheduledDiveListItemViews.get(scheduledDiveUser.getScheduledDiveLocalId());
										ScheduledDive scheduledDive = 
												(ScheduledDive) scheduledDiveView.getTag();
										
										int index =  scheduledDive.getScheduledDiveUserIndex(scheduledDiveUser);
										if (index != -1) {
											scheduledDive.getScheduledDiveUsers().remove(index);
										}
										
										scheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
										mDiveSiteManager.saveScheduledDive(scheduledDive);			
										
										// Update scheduled dive from online source if published
										if (scheduledDive.isPublished()) {
											DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseScheduledDive = new DiveSiteOnlineDatabaseLink(getActivity());
											diveSiteOnlineDatabaseScheduledDive.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
												@Override
												public void onOnlineDiveDataRetrievedComplete(
														ArrayList<Object> resultList,
														String message,
														Boolean isError) {
													if (resultList.size() > 0) {
														// Updated scheduled dive, save it
														ScheduledDive updatedScheduledDive = (ScheduledDive)resultList.get(0);	
														
														// Save sites for each scheduled dive
														for (int i = 0; i > updatedScheduledDive.getScheduledDiveDiveSites().size(); i++) {
															DiveSite diveSite = updatedScheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite();
															if (diveSite != null) {
																mDiveSiteManager.saveDiveSite(diveSite);
																updatedScheduledDive.getScheduledDiveDiveSites().get(i).setDiveSiteLocalId(diveSite.getLocalId());
															}
														}
														
														mDiveSiteManager.saveScheduledDive(updatedScheduledDive);
													} else if (!message.isEmpty()) {
														Toast.makeText(
																getActivity(),
																message,
																Toast.LENGTH_SHORT)
																.show();
													}
													
													refreshScheduledDiveList();
												}
												
												@Override
												public void onOnlineDiveDataPostBackground(
														ArrayList<Object> resultList,
														String message) {
													// TODO Auto-generated method stub
													
												}
	
												@Override
												public void onOnlineDiveDataProgress(
														Object result) {
													// TODO Auto-generated method stub
													
												}
												
											});
											diveSiteOnlineDatabaseScheduledDive.getScheduledDive(new Date(0), scheduledDive.getOnlineId(), null);
										} else {
											// Still need to refresh list if not published
											refreshScheduledDiveList();
										}
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
				} else {
					// Local scheduled dive only, just add and save it to the scheduled dive
					index =  scheduledDive.getScheduledDiveUserIndex(scheduledDiveUser);
					if (index != -1) {
						scheduledDive.getScheduledDiveUsers().remove(index);
					}
					
					scheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
					mDiveSiteManager.saveScheduledDive(scheduledDive);
					
					refreshScheduledDiveList();
				}
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
				
				if (scheduledDive.getOnlineId() != -1) {
					DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
					diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
								@Override
								public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {
									if (resultList.size() > 0) {
										// Update Scheduled Dive
										ScheduledDiveUser scheduledDiveUser = (ScheduledDiveUser) resultList.get(0);
										View scheduledDiveView = 
												mScheduledDiveListItemViews.get(scheduledDiveUser.getScheduledDiveLocalId());
										ScheduledDive scheduledDive = 
												(ScheduledDive) scheduledDiveView.getTag();
										
										int index =  scheduledDive.getScheduledDiveUserIndex(scheduledDiveUser);
										if (index != -1) {
											scheduledDive.getScheduledDiveUsers().remove(index);
										}
										
										scheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
										mDiveSiteManager.saveScheduledDive(scheduledDive);			
										
										if (scheduledDive.isPublished()) {
											DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseScheduledDive = new DiveSiteOnlineDatabaseLink(getActivity());
											diveSiteOnlineDatabaseScheduledDive.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
												@Override
												public void onOnlineDiveDataRetrievedComplete(
														ArrayList<Object> resultList,
														String message,
														Boolean isError) {
													if (resultList.size() > 0) {
														// Updated scheduled dive, save it
														ScheduledDive updatedScheduledDive = (ScheduledDive)resultList.get(0);	
														
														// Save sites for each scheduled dive
														for (int i = 0; i > updatedScheduledDive.getScheduledDiveDiveSites().size(); i++) {
															DiveSite diveSite = updatedScheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite();
															if (diveSite != null) {
																mDiveSiteManager.saveDiveSite(diveSite);
																updatedScheduledDive.getScheduledDiveDiveSites().get(i).setDiveSiteLocalId(diveSite.getLocalId());
															}
														}
														
														mDiveSiteManager.saveScheduledDive(updatedScheduledDive);
													} else if (!message.isEmpty()) {
														Toast.makeText(
																getActivity(),
																message,
																Toast.LENGTH_SHORT)
																.show();
													}
													
													refreshScheduledDiveList();
												}
												
												@Override
												public void onOnlineDiveDataPostBackground(
														ArrayList<Object> resultList,
														String message) {
													// TODO Auto-generated method stub
													
												}
	
												@Override
												public void onOnlineDiveDataProgress(
														Object result) {
													// TODO Auto-generated method stub
													
												}
												
											});
											diveSiteOnlineDatabaseScheduledDive.getScheduledDive(new Date(0), scheduledDive.getOnlineId(), null);
										} else {
											// Still need to refresh list if not published
											refreshScheduledDiveList();
										}
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
				} else {
					// Local scheduled dive only, just add and save it to the scheduled dive
					index =  scheduledDive.getScheduledDiveUserIndex(scheduledDiveUser);
					if (index != -1) {
						scheduledDive.getScheduledDiveUsers().remove(index);
					}
					
					scheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
					mDiveSiteManager.saveScheduledDive(scheduledDive);
					
					refreshScheduledDiveList();
				}
			}
		});
		
		// Button to create a log from the scheduled dives dive sites
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
									ScheduledDiveDiveSite scheduledDiveDiveSite = selectedScheduledDiveSites.get(i);
									
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
		
		// If no buttons are visible, determine which to show
		if (scheduledDiveAttend.getVisibility() == View.GONE && 
				scheduledDiveBail.getVisibility() == View.GONE &&
				scheduledDiveLog.getVisibility() == View.GONE) {
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
		}
		
		// Submitter
		ImageButton submitterView = view.findViewById(R.id.scheduleddive_item_picture);
		mScheduledDiveListItemDiverImageView.put(scheduledDive.getLocalId(), submitterView);
		
		if (mDiverProfileImageCache.get(scheduledDive.getSubmitterId()) == null) {
			// Need to get diver from the ID, then the diver's picture
			DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
			diveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

						@Override
						public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {
							if (resultList.size() > 0) {
								// First save diver so another list item can use it
								Diver diver = (Diver) resultList.get(0);
								mScheduledDiveListItemDiver.put(scheduledDive.getLocalId(), diver);
								
								// Now get bitmap profile image for diver
								LoadOnlineImageTask task =
										new LoadOnlineImageTask(mScheduledDiveListItemDiverImageView.get(scheduledDive.getLocalId())) {

									@Override
									protected void onPostExecute(
											Bitmap result) {
										Diver diver = (Diver) getTag();
										mDiverProfileImageCache.put(diver.getOnlineId(), result);
										ImageButton diverProfileImage = 
											(ImageButton) mScheduledDiveListItemDiverImageView.get(scheduledDive.getLocalId());
										diverProfileImage.setImageBitmap(result);
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
						new Intent(ScheduledDiveListLocalFragment.this.getActivity(), DiverActivity.class);
				long diverUserId = (Long) v.getTag();
				intent.putExtra(DiverActivity.EXTRA_DIVER_ID, diverUserId);
				startActivity(intent);
			}
		});

		// Indicators
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
		
		ImageButton requiresRefresh =
				view.findViewById(R.id.scheduleddive_indicate_refresh_required);
        if (scheduledDive.requiresRefresh()) {
            requiresRefresh.setVisibility(View.VISIBLE);
            requiresRefresh.setEnabled(mScheduledDiveUpdates.get(scheduledDive.getLocalId()) != null);
        } else {
            requiresRefresh.setVisibility(View.GONE);
        }

        requiresRefresh.setTag(scheduledDive);
        requiresRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // Ask user if they want to update
                ScheduledDive existingScheduledDive = (ScheduledDive) v.getTag();
                final ScheduledDive updatedScheduledDive = mScheduledDiveUpdates.get(existingScheduledDive.getLocalId());

                String updateMessage =
                        getResources().getString(R.string.scheduleddive_requiresRefresh_message);
                if (!existingScheduledDive.isPublished()) {
                    updateMessage = updateMessage +
                            getResources().getString(R.string.scheduleddive_requiresRefresh_overwrite);
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.scheduleddive_requiresRefresh)
                        .setMessage(updateMessage)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        mProgressDialog.setMessage(getString(R.string.save_scheduleddives_progress));
                                        mProgressDialog.setCancelable(false);
                                        mProgressDialog.setIndeterminate(false);
                                        mProgressDialog.setProgress(0);
                                        mProgressDialog.setMax(1);
                                        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                        mProgressDialog.show();

                                        updatedScheduledDive.setRequiresRefresh(false);

                                        // Save sites for each scheduled dive
                                        for (int i = 0; i > scheduledDive.getScheduledDiveDiveSites().size(); i++) {
                                            DiveSite diveSite = scheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite();
                                            if (diveSite != null) {
                                                mDiveSiteManager.saveDiveSite(diveSite);
                                                scheduledDive.getScheduledDiveDiveSites().get(i).setDiveSiteLocalId(diveSite.getLocalId());
                                            }
                                        }

                                        mDiveSiteManager.saveScheduledDive(scheduledDive);

                                        refreshScheduledDiveList();

                                        mProgressDialog.dismiss();
                                    }
                                })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        // Initialize visibility secondary view
        View secondaryView = view.findViewById(R.id.scheduleddive_item_secondary_view);
        final ViewGroup mapContainer = view.findViewById(R.id.scheduleddive_list_item_mapView_container);
        if (mSelectedScheduledDive != null && scheduledDive.getLocalId() == mSelectedScheduledDive.getLocalId()) {
            view.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
            secondaryView.setVisibility(View.VISIBLE);
            setScheduledDiveMap(scheduledDive, mapContainer);
        } else if (mScheduledDiveItemMapView.getParent() == mapContainer) {
            secondaryView.setVisibility(View.GONE);
            mScheduledDiveItemMapView.setVisibility(View.GONE);
        }
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (args != null) {
			if (id == DiveCursorLoaders.LOAD_SCHEDULEDDIVE) {
				String titleFilter = mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "").trim();
				String countryFilter = mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, getResources()
								.getString(R.string.filter_list_all)).trim();
				if (countryFilter.trim().equals(getResources().getString(R.string.filter_list_all))) {
					countryFilter = "";
				}
				
				String stateFilter = mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "").trim();
				String cityFilter = mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "").trim();
				
				String previousDaysFilter = mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "").trim();
				String nextDaysFilter = mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "").trim();
				
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

				Boolean publishedFilter = mPrefs.getBoolean(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED, true);
				Boolean unPublishedFilter = mPrefs.getBoolean(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED, true);

				if (mDiveSite != null) {
					return new ScheduledDiveListCursorLoader(getActivity(), publishedFilter, mPublishMode || unPublishedFilter,
                            mRestrictToDiverID, mDiveSite, timeStampStartFilter, timeStampEndFilter);
				} else {
					return new ScheduledDiveListCursorLoader(getActivity(), publishedFilter, mPublishMode || unPublishedFilter,
                            mRestrictToDiverID, mRestrictToDiverID, titleFilter, countryFilter, stateFilter, cityFilter,
							timeStampStartFilter, timeStampEndFilter);
				}
				
				
			} else {
				long scheduledDiveID = args.getLong(ARG_LOADER_SCHEDULEDDIVE_ID);

				if (id - mScheduledDiveListItemLoaderIDs.get(scheduledDiveID) == DiveCursorLoaders.LOAD_SCHEDULEDDIVE_SITES_INDEX) {
					return new DiveCursorLoaders.ScheduledDiveDiveSiteListCursorLoader(
							getActivity(), scheduledDiveID);
				} else if (id - mScheduledDiveListItemLoaderIDs.get(scheduledDiveID) == DiveCursorLoaders.LOAD_SCHEDULEDDIVE_USERS_INDEX) {
					return new DiveCursorLoaders.ScheduledDiveUserListCursorLoader(
							getActivity(), scheduledDiveID);
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor instanceof ScheduledDiveCursor) {
			// Create an adapter to point at this cursor
			if (getListAdapter() == null) {
				ScheduledDiveCursorAdapter adapter = 
					new ScheduledDiveCursorAdapter(getActivity(), (ScheduledDiveCursor) cursor);
				setListAdapter(adapter);
			} else {
				((ScheduledDiveCursorAdapter) getListAdapter()).changeCursor(cursor);
			}

            stopRefresh();

		} else if (cursor instanceof ScheduledDiveDiveSiteCursor) {
			cursor.moveToFirst();
			
			if (!cursor.isAfterLast()) {
				// Clear scheduled sites for scheduled dive of first item
				//  Valid since we retrieve users per scheduled dive
				long scheduledDiveID = 
						((ScheduledDiveDiveSiteCursor) cursor).getScheduledDiveDiveSite().getScheduledDiveLocalId();
				View view = mScheduledDiveListItemViews.get(scheduledDiveID);
				LinearLayout sitesListView = view.findViewById(R.id.scheduleddive_item_site_list);
				sitesListView.removeAllViews();
			}
			
			while (!cursor.isAfterLast()) {
				// First find view for the scheduled dive for the dive site
				final ScheduledDiveDiveSite scheduledDiveDiveSite = 
						((ScheduledDiveDiveSiteCursor) cursor).getScheduledDiveDiveSite();
				DiveSite diveSite = mDiveSiteManager.getDiveSite(scheduledDiveDiveSite.getDiveSiteLocalId());
				scheduledDiveDiveSite.setDiveSite(diveSite);
				
				if (diveSite != null) {
					View scheduledDiveView = 
							mScheduledDiveListItemViews.get(scheduledDiveDiveSite.getScheduledDiveLocalId());
					if (scheduledDiveView != null) {
						// Found the view, now find the site list
						LinearLayout scheduledDiveDiveSiteListView =
								scheduledDiveView.findViewById(R.id.scheduleddive_item_site_list);
						scheduledDiveDiveSiteListView.setTag(
								R.id.scheduleddive_local_view_tag, 
								scheduledDiveView);

                        ScheduledDive scheduledDive = (ScheduledDive)scheduledDiveView.getTag();

						// Add scheduled dive dive site to tagged scheduled dive
						if (scheduledDive.getScheduledDiveDiveSiteIndex(scheduledDiveDiveSite) == -1) {
							scheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
						}						
						
						LayoutInflater layoutInflater = 
								(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View scheduledDiveDiveSiteView = 
								layoutInflater.inflate(R.layout.scheduleddive_site_item, null);
						
						scheduledDiveDiveSiteView.setTag(
								R.id.scheduleddive_local_id_tag, 
								scheduledDiveDiveSite.getLocalId());
						scheduledDiveDiveSiteView.setTag(
								R.id.scheduleddive_local_view_tag, 
								scheduledDiveView);
						
						mScheduledDiveDiveSiteListItemViews.put(scheduledDiveDiveSite.getLocalId(),
								scheduledDiveDiveSiteView);
						
						// Trigger selection on click
						View.OnClickListener selectionClickListener = new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// If any items are checked, context menu is open
								//  So toggle item checked instead of selection
								ListView listView = ScheduledDiveListLocalFragment.this.getListView();
								View scheduledDiveView = (View)v.getTag(R.id.scheduleddive_local_view_tag);
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
								ListView listView = ScheduledDiveListLocalFragment.this.getListView();
								View scheduledDiveView = (View)v.getTag(R.id.scheduleddive_local_view_tag);
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
						ImageButton scheduledDiveDiveSiteRemove = scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_remove);
						scheduledDiveDiveSiteRemove.setVisibility(View.GONE);
						
						TextView scheduledDiveDiveSiteName = scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_name);
						scheduledDiveDiveSiteName.setText(diveSite.getName());
						
						TextView scheduledDiveDiveSiteLocation = scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_location);
						scheduledDiveDiveSiteLocation.setText(diveSite.getFullLocation());
						
						TextView scheduledDiveDiveSiteVoteCount = scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_vote_count);
						scheduledDiveDiveSiteVoteCount.setText(String.format(getResources().getString(R.string.scheduleddive_list_vote_count), 
								scheduledDiveDiveSite.getVoteCount()));

                        // Initialize visibility map view
                        final ViewGroup mapContainer = scheduledDiveView.findViewById(R.id.scheduleddive_list_item_mapView_container);
                        if (mSelectedScheduledDive != null && scheduledDive.getLocalId() == mSelectedScheduledDive.getLocalId()) {
                            setScheduledDiveMap(scheduledDive, mapContainer);
                        } else if (mScheduledDiveItemMapView.getParent() == mapContainer) {
                            mScheduledDiveItemMapView.setVisibility(View.GONE);
                        }
					}
				}
				cursor.moveToNext();
			}

		} else if (cursor instanceof ScheduledDiveUserCursor) {
			cursor.moveToFirst();
			
			if (!cursor.isAfterLast()) {
				// Clear scheduled sites for scheduled dive of first item
				//  Valid since we retrieve sites per scheduled dive
				long scheduledDiveID = 
						((ScheduledDiveUserCursor) cursor).getScheduledDiveUser().getScheduledDiveLocalId();
				View view = mScheduledDiveListItemViews.get(scheduledDiveID);
				LinearLayout usersListView = view.findViewById(R.id.scheduleddive_item_user_list);
				usersListView.removeAllViews();
			}

            // Trigger contextual menu on long click
            View.OnLongClickListener contextClickListener = new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    ListView listView = ScheduledDiveListLocalFragment.this.getListView();
                    View scheduledDiveView = (View)v.getTag(R.id.scheduleddive_local_view_tag);
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
                    ListView listView = ScheduledDiveListLocalFragment.this.getListView();
                    View scheduledDiveView = (View)v.getTag(R.id.scheduleddive_local_view_tag);
                    if (listView.getCheckedItemCount() > 0) {
                        int viewPosition = listView.getPositionForView(scheduledDiveView);
                        listView.setItemChecked(viewPosition, !listView.isItemChecked(viewPosition));
                    } else {
                        triggerViewSelection(scheduledDiveView, (ScheduledDive)scheduledDiveView.getTag());
                    }
                }
            };
			
			while (!cursor.isAfterLast()) {
				// First find view for the scheduled dive for the user
				final ScheduledDiveUser scheduledDiveUser = 
						((ScheduledDiveUserCursor) cursor).getScheduledDiveUser();
				View scheduledDiveView = 
						mScheduledDiveListItemViews.get(scheduledDiveUser.getScheduledDiveLocalId());
				if (scheduledDiveView != null) {
					// Update diver count
					if (scheduledDiveUser.getAttendState() == ScheduledDiveUser.AttendState.ATTENDING) {
						TextView diverCountView =
								scheduledDiveView.findViewById(R.id.scheduleddive_item_divercount);
						int newDiverCount = (Integer) diverCountView.getTag() + 1;
						diverCountView.setText(String.format(
								getResources().getString(R.string.scheduleddive_list_diver_count), newDiverCount));
						diverCountView.setTag(newDiverCount);
					}

					// Found the view, now find the user list
					LinearLayout scheduledDiveUserListView = scheduledDiveView.findViewById(R.id.scheduleddive_item_user_list);
                    scheduledDiveUserListView.setOnLongClickListener(contextClickListener);
                    scheduledDiveUserListView.setOnClickListener(selectionClickListener);
                    scheduledDiveUserListView.setTag(R.id.scheduleddive_local_view_tag, scheduledDiveView);

					// Add scheduled dive user to tagged scheduled dive
					int scheduledDiveUserIndex = 
							((ScheduledDive)scheduledDiveView.getTag()).getScheduledDiveUserIndex(scheduledDiveUser);
					if (scheduledDiveUserIndex != -1) {
						((ScheduledDive)scheduledDiveView.getTag()).getScheduledDiveUsers().remove(scheduledDiveUserIndex);
					}
					((ScheduledDive)scheduledDiveView.getTag()).getScheduledDiveUsers().add(scheduledDiveUser);
					
					Button scheduledDiveAttend = scheduledDiveView.findViewById(R.id.scheduleddive_item_attend);
					Button scheduledDiveBail = scheduledDiveView.findViewById(R.id.scheduleddive_item_bail);
					Button scheduledDiveLog = scheduledDiveView.findViewById(R.id.scheduleddive_item_log);
					
					if (((ScheduledDive)scheduledDiveView.getTag()).getTimestamp().getTime() < (new Date()).getTime()) {
						// Scheduled Dive in the past, show log button				
						scheduledDiveAttend.setVisibility(View.GONE);
						scheduledDiveBail.setVisibility(View.GONE);
						scheduledDiveLog.setVisibility(View.VISIBLE);
					} else if (((ScheduledDive)scheduledDiveView.getTag()).isUserAttending(mDiveSiteManager.getLoggedInDiverId())) {
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
					
					if (scheduledDiveUser.getAttendState() == ScheduledDiveUser.AttendState.ATTENDING) {
						// User attending and no view exists, create and add
						LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View scheduledDiveUserView = layoutInflater.inflate(R.layout.scheduleddive_user_item, null);
                        scheduledDiveUserView.setTag(R.id.scheduleddive_local_id_tag, scheduledDiveUser.getLocalId());
                        scheduledDiveUserView.setTag(R.id.scheduleddive_local_view_tag, scheduledDiveView);
                        scheduledDiveUserView.setOnLongClickListener(contextClickListener);
                        scheduledDiveUserView.setOnClickListener(selectionClickListener);
						
						mScheduledDiveUserListItemViews.put(scheduledDiveUser.getLocalId(), scheduledDiveUserView);

						// Add the view to the list
						scheduledDiveUserListView.addView(scheduledDiveUserView);
						
						if (scheduledDiveUser.getUserId() != -1 && 
								scheduledDiveUser.getAttendState() == ScheduledDiveUser.AttendState.ATTENDING) {
							ImageButton userProfile = scheduledDiveUserView.findViewById(R.id.scheduleddive_user_picture);

							mScheduledDiveListItemDiverImageView.put(scheduledDiveUser.getLocalId(), userProfile);

							// Set listener to access profile
							userProfile.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View v) {
											Intent intent = 
													new Intent(ScheduledDiveListLocalFragment.this.getActivity(),
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
															mScheduledDiveListItemDiverImageView.get(scheduledDiveUser.getLocalId());
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
							
							ImageButton scheduledDiveUserRemove = scheduledDiveUserView.findViewById(R.id.scheduleddive_user_remove);
							scheduledDiveUserRemove.setVisibility(View.GONE);
						}
					}
				}	
				cursor.moveToNext();
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Stop using the cursor (via the adapter)
		((CursorAdapter) getListAdapter()).swapCursor(null);
	}
}
