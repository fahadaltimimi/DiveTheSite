package com.fahadaltimimi.divethesite.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.data.DiveCursorLoaders;
import com.fahadaltimimi.divethesite.data.DiveCursorLoaders.DiveLogListCursorLoader;
import com.fahadaltimimi.divethesite.model.DiveLogStop;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogBuddyCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogCursor;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveLogStopCursor;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

public class DiveLogListLocalFragment extends DiveLogListFragment implements
		LoaderCallbacks<Cursor> {

	private static final String ARG_LOADER_DIVER_ID = "LOADER_DIVER_ID";
	private static final String ARG_LOADER_DIVESITE = "LOADER_DIVESITE";
	private static final String ARG_LOADER_DIVELOG_ID = "LOADER_DIVELOG_ID";

	private boolean mPublishMode = false;

	private DiveLogListCursorLoader mDiveLogListLoader = null;

    private HashMap<Long, DiveLog> mDiveLogUpdates = new HashMap<Long, DiveLog>();

    private int mDiveLogCount = 0;

	public static DiveLogListLocalFragment newInstance(long diverID,
			DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);

		DiveLogListLocalFragment rf = new DiveLogListLocalFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVER_ID, mRestrictToDiverID);
		loaderArgs.putParcelable(ARG_LOADER_DIVESITE, mDiveSite);

		// Initialize the loader to load the list of Dive Sites
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}

        updateDiveLogCount();
		
		mDiveLogListLoader = (DiveLogListCursorLoader) getLoaderManager().initLoader(DiveCursorLoaders.LOAD_DIVELOG, loaderArgs, this);
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
				case R.id.menu_item_publish_divelog:
					// Confirm if user wants to publish dive logs, then publish
					// them
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.publish)
							.setMessage(R.string.publish_divelogs_message)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int whichButton) {
											// Create and initialize progress dialog
											mProgressDialog.setMessage(getString(R.string.publish_divelogs_progress));
											mProgressDialog.setCancelable(false);
											mProgressDialog.setIndeterminate(false);
											mProgressDialog.setProgress(0);
											mProgressDialog.setMax(getListView().getCheckedItemCount());
											mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
											mProgressDialog.show();

											// Publish selected dive logs
                                            int itemsPublished = 0;
											for (int i = 0; i < getListAdapter().getCount(); i++) {
												if (getListView().isItemChecked(i)) {
													DiveLogCursorAdapter adapter = (DiveLogCursorAdapter) getListView().getAdapter();
													long diveLogId = adapter.getItemId(i);
													DiveLog diveLog = mDiveSiteManager.getDiveLog(diveLogId);

													if (diveLog.isPublished() || diveLog.getUserId() != mDiveSiteManager.getLoggedInDiverId()) {
														
														if (diveLog.getUserId() != mDiveSiteManager.getLoggedInDiverId()) {
															Toast.makeText(
																	getActivity(),
																	R.string.publish_not_users_log,
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
                                                                        R.string.dive_logs_published_message,
                                                                        Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
														}
													} else {
                                                        itemsPublished = itemsPublished + 1;

														diveLog.setBuddies(mDiveSiteManager.getDiveLogBuddies(diveLogId));
														diveLog.setStops(mDiveSiteManager.getDiveLogStops(diveLogId));

                                                        // Retrieve log's dive site and make sure online id is set for log
                                                        DiveSite diveSite = mDiveSiteManager.getDiveSite(diveLog.getDiveSiteLocalId());
                                                        diveLog.setDiveSite(diveSite);

                                                        // Publish Dive Site if required
                                                        if (diveSite != null && (!diveSite.isPublished() && diveSite.getUserId() == mDiveSiteManager.getLoggedInDiverId())) {
                                                            publishDiveLogDiveSite(diveLog);
                                                        } else {
                                                            publishDiveLog(diveLog);
                                                        }
													}
												}
											}

											mode.finish();
										}
									})
							.setNegativeButton(android.R.string.no, null)
							.show();

					return true;

				case R.id.menu_item_edit_divelog:
					DiveLogCursorAdapter adapter = (DiveLogCursorAdapter) getListView().getAdapter();
					for (int i = getListAdapter().getCount() - 1; i >= 0; i--) {
						if (getListView().isItemChecked(i)) {														
							long diveLogId = adapter.getItemId(i);
							DiveLog diveLog = mDiveSiteManager.getDiveLog(diveLogId);
							if (diveLog.getUserId() == mDiveSiteManager.getLoggedInDiverId()) {
								diveLog.setDiveSite(mDiveSiteManager.getDiveSite(diveLog.getDiveSiteLocalId()));
								diveLog.setBuddies(mDiveSiteManager.getDiveLogBuddies(diveLogId));
								diveLog.setStops(mDiveSiteManager.getDiveLogStops(diveLogId));
								openDiveLog(diveLog);
							} else {
                                Toast.makeText(getActivity(), R.string.edit_own_divelog_fail, Toast.LENGTH_LONG).show();
							}
						}
					}
					
					mode.finish();
					
					return true;
					
				case R.id.menu_item_share_divelog:
					// Get summary for each dive log selected
					String combinedSummary = 
						getResources().getString(R.string.share_title_divelog) + "\n\n";
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							long diveLogId = getListAdapter().getItemId(i);
							DiveLog diveLog = mDiveSiteManager.getDiveLog(diveLogId);
							diveLog.setDiveSite(mDiveSiteManager.getDiveSite(diveLog.getDiveSiteLocalId()));
							
							combinedSummary = combinedSummary + diveLog.getShareSummary() + "\n\n";
						}
					}
					
					// Share info to what user selects
					Intent share = new Intent(android.content.Intent.ACTION_SEND);
			        share.setType("text/plain");
			        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			        share.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title_divelog));
			        share.putExtra(Intent.EXTRA_TEXT, combinedSummary.trim());
			 
			        startActivity(Intent.createChooser(share, "Share Dive Log!"));
					
			        mode.finish();
			        
					return true;
					
				case R.id.menu_item_delete_local_divelog:
					// Confirm if user wants to delete
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.delete)
							.setMessage(R.string.delete_divelogs_message)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.yes,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											// Delete selected dive sites
											for (int i = 0; i < getListAdapter().getCount(); i++) {
												if (getListView().isItemChecked(i)) {
													DiveLogCursorAdapter adapter = (DiveLogCursorAdapter) getListView().getAdapter();
													long diveLogId = adapter.getItemId(i);
													mDiveSiteManager.deleteDiveLog(diveLogId);
												}
											}

											mode.finish();
											refreshDiveLogList();
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
				inflater.inflate(R.menu.fragment_divelog_list_local_contextual,menu);

				MenuItem delete = menu.findItem(R.id.menu_item_delete_local_divelog);
				MenuItem publish = menu.findItem(R.id.menu_item_publish_divelog);
				
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

    private void publishDiveLogDiveSite(final DiveLog diveLog) {
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

                    // Update diveLog before publishing
                    diveLog.setDiveSiteOnlineId(diveSite.getOnlineId());
                }

                publishDiveLog(diveLog);
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

        mDiveSiteOnlineDatabase.publishDiveSite(diveLog.getDiveSite());
    }

    private void publishDiveLog(DiveLog diveLog) {
        mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
        mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

            @Override
            public void onOnlineDiveDataRetrievedComplete(
                    ArrayList<Object> resultList, String message, Boolean isError) {

                if (getActivity() != null && message != null && !message.isEmpty()) {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }

                // Save dive log
                if (resultList.size() > 0) {
                    DiveLog diveLog = (DiveLog) resultList.get(0);
                    mDiveSiteManager.saveDiveLog(diveLog);
                    refreshDiveLogList();
                }

                if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getActivity(), R.string.dive_logs_published_message, Toast.LENGTH_SHORT).show();
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
                // TODO
                // Auto-generated
                // method
                // stub

            }
        });

        mDiveSiteOnlineDatabase.publishDiveLog(diveLog);
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_divelog_list_local, menu);

		mRefreshMenuItem = menu.findItem(R.id.menu_item_refresh_divelogs);
		
		MenuItem publishModeMenuItem = menu
				.findItem(R.id.menu_item_publishMode);

		if (mPublishMode) {
			publishModeMenuItem.setTitle(R.string.divelog_show_visible);
		} else {
			publishModeMenuItem.setTitle(R.string.divelog_publish_logs);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		MenuItem publishModeMenuItem = menu
				.findItem(R.id.menu_item_publishMode);

		// Only show publish and edit button if were looking at our own logs
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

		case R.id.menu_item_add_divelog:
			// If user not registered, don't allow
			if (mDiveSiteManager.getLoggedInDiverId() == -1) {
				Toast.makeText(getActivity(), R.string.not_registered_create_log, Toast.LENGTH_LONG).show();
				return true;
			}
						
			openDiveLog(null);
			return true;
			
		case R.id.menu_item_refresh_divelogs:
			refreshDiveLogList();
			return true;

		case R.id.menu_item_publishMode:
			mPublishMode = !mPublishMode;

			getActivity().invalidateOptionsMenu();
			refreshDiveLogList();
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Restart the loader to get any new dive logs available
		Bundle args = getArguments();
		long restrictToDiverID = -1;
		DiveSite restrictToDiveSite = null;
		if (args != null) {
			restrictToDiverID = args.getLong(DiverTabFragment.ARG_DIVER_ID, -1);
			restrictToDiveSite = args.getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
		}

        updateDiveLogCount();

		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVER_ID, restrictToDiverID);
		loaderArgs.putParcelable(ARG_LOADER_DIVESITE, restrictToDiveSite);
		getLoaderManager().restartLoader(DiveCursorLoaders.LOAD_DIVELOG, loaderArgs, this);
	}

	@Override
	protected void updateFilterNotification() {
		// Determine current filter
		ArrayList<String> filterNotification = new ArrayList<String>();

		if (mPublishMode) {
			filterNotification.add(getResources().getString(
					R.string.divelog_publish_logs));
		}

		if (filterNotification.size() == 0) {
			mFilterNotificationContainer.setVisibility(View.GONE);
		} else {
			mFilterNotificationContainer.setVisibility(View.VISIBLE);
		}

		mFilterNotification.setText(filterNotification.toString());
	}

	@Override
	protected void refreshDiveLogList() {
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}

		updateFilterNotification();

		long restrictToDiverID = -1;
		DiveSite restrictToDiveSite = null;
		Bundle args = getArguments();
		if (args != null) {
			restrictToDiverID = args.getLong(DiverTabFragment.ARG_DIVER_ID, -1);
			restrictToDiveSite = args.getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
		}

        updateDiveLogCount();

		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVER_ID, restrictToDiverID);
		loaderArgs.putParcelable(ARG_LOADER_DIVESITE, restrictToDiveSite);
		getLoaderManager().restartLoader(DiveCursorLoaders.LOAD_DIVELOG, loaderArgs, this);
	}

    private void updateDiveLogCount() {
        // Refresh dive log count
        mDiveLogCount = mDiveSiteManager.queryDiveLogsCount(mRestrictToDiverID, mDiveSite, mPublishMode);
        int diveLogTotalMinutes = mDiveSiteManager.queryDiveLogsTotalMinutes(mRestrictToDiverID, mDiveSite, mPublishMode);

        int hours = diveLogTotalMinutes / 60;
        int minutes = diveLogTotalMinutes % 60;

        // Update parent fragment if tab
        if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
            ((DiveListTabFragment) getParentFragment()).updateLocalSubTitles(String.valueOf(mDiveLogCount),
                    String.format(getActivity().getResources().getString(R.string.time_format), hours, minutes));
        }
    }

	@Override
	protected DiveLog getDiveLogItemClick(int position, long id) {
		DiveLog diveLog = mDiveSiteManager.getDiveLog(id);
		diveLog.setBuddies(mDiveSiteManager.getDiveLogBuddies(id));
		diveLog.setStops(mDiveSiteManager.getDiveLogStops(id));
		if (diveLog.getDiveSiteLocalId() != -1) {
			diveLog.setDiveSite(mDiveSiteManager.getDiveSite(diveLog
					.getDiveSiteLocalId()));
		}
		return diveLog;
	}

	private class DiveLogCursorAdapter extends CursorAdapter {

		private DiveLogCursor mDiveLogCursor;

		public DiveLogCursorAdapter(Context context, DiveLogCursor cursor) {
			super(context, cursor, 0);
			mDiveLogCursor = cursor;
		}

		@Override
		public void changeCursor(Cursor cursor) {
			mDiveLogCursor = (DiveLogCursor) cursor;
			super.changeCursor(cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// Use a layout inflater to get a row view
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.divelog_list_item, parent, false);

			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			DiveLog diveLog = mDiveLogCursor.getDiveLog();
			DiveSite diveSite = mDiveSiteManager.getDiveSite(diveLog.getDiveSiteLocalId());
			diveLog.setDiveSite(diveSite);
			
			view.setTag(diveLog);
			
			updateView(view, diveLog, cursor);
			
			if (diveLog.getOnlineId() != -1) {
				// Check if dive log needs to be refreshed
				Date checkModifiedDate = new Date(diveLog.getLastModifiedOnline().getTime());
				mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
				mDiveSiteOnlineDatabase
					.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
						@Override
						public void onOnlineDiveDataPostBackground(
								ArrayList<Object> resultList, String message) {
							//
						}
	
						@Override
						public void onOnlineDiveDataRetrievedComplete(
								ArrayList<Object> resultList, String message, Boolean isError) {
							if (resultList.size() > 0) {
								// Dive Log retreived, so a refresh is required
								DiveLog updatedDiveLog = (DiveLog)resultList.get(0);
								DiveLog existingDiveLog = mDiveSiteManager.getDiveLog(updatedDiveLog.getLocalId());

								if (existingDiveLog != null) {
									if (updatedDiveLog.getLastModifiedOnline().after(existingDiveLog.getLastModifiedOnline())) {
                                        mDiveLogUpdates.put(existingDiveLog.getLocalId(), updatedDiveLog);
                                        if (!existingDiveLog.requiresRefresh()) {
                                            existingDiveLog.setRequiresRefresh(true);
                                            mDiveSiteManager.setDiveLogRequiresRefresh(existingDiveLog);
                                            refreshDiveLogList();
                                        }
									} else if (existingDiveLog.requiresRefresh()) {
                                        existingDiveLog.setRequiresRefresh(false);
                                        mDiveSiteManager.saveDiveLog(existingDiveLog);
                                        refreshDiveLogList();
                                    }
								}
							}
						}
	
						@Override
						public void onOnlineDiveDataProgress(Object result) {
							// TODO Auto-generated method stub
							
						}
						
					});
				mDiveSiteOnlineDatabase.getDiveLog(checkModifiedDate, diveLog.getOnlineId(), diveLog);
			}
		} 
	}
	
	private void updateView(View view, final DiveLog diveLog, Cursor cursor) {
		// Save id to view for later updating with buddies and stops
		mDiveLogListItemViews.put(diveLog.getLocalId(), view);
		if (mDiveLogListItemLoaderIDs.get(diveLog.getLocalId()) == null) {
			mDiveLogListItemLoaderIDs.put(diveLog.getLocalId(),
					mDiveLogListItemLoaderIDs.size());
		}
		
		DiveSite diveSite = diveLog.getDiveSite();

        int diveLogIndexDisplay = mDiveLogCount - cursor.getPosition();

		// Initialize buddy and site loaders, add dive site id to args with
		// loader
		LoaderManager lm = getLoaderManager();
		Bundle loaderArgs = new Bundle();
		loaderArgs.putLong(ARG_LOADER_DIVELOG_ID, diveLog.getLocalId());

		lm.restartLoader(DiveCursorLoaders.LOAD_DIVELOG_BUDDIES_INDEX
				+ mDiveLogListItemLoaderIDs.get(diveLog.getLocalId()),
				loaderArgs, DiveLogListLocalFragment.this);
		lm.restartLoader(DiveCursorLoaders.LOAD_DIVELOG_STOPS_INDEX
				+ mDiveLogListItemLoaderIDs.get(diveLog.getLocalId()),
				loaderArgs, DiveLogListLocalFragment.this);

		// Remove buddies and sites before restarting loaders
		LinearLayout buddyListView = (LinearLayout) view
				.findViewById(R.id.divelog_item_buddy_list);
		buddyListView.removeAllViews();
		LinearLayout stopListView = (LinearLayout) view
				.findViewById(R.id.divelog_item_stop_list);
		stopListView.removeAllViews();

		// Set up the view with the dive logs info
		if (diveLog.isPublished()) {
			view.setBackgroundColor(getResources().getColor(
					R.color.itemPublished));
		} else {
			view.setBackgroundColor(getResources().getColor(
					R.color.itemUnpublished));
		}

		// Dive Site Title
        Button diveSiteTitle = (Button) view.findViewById(R.id.divelog_item_divesite);
        String diveSiteTitleString = "";
		if (diveSite != null) {
			diveSiteTitleString = diveSite.getName();
			diveSiteTitle.setTag(diveSite);
			diveSiteTitle.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Open the dive site with edit mode set to false
					mPrefs.edit().putBoolean(DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE, false).commit();

					DiveSite diveSite = (DiveSite) v.getTag();
					Intent intent = new Intent(getActivity(), DiveSiteActivity.class);
					intent.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, diveSite);
					startActivity(intent);
				}
			});
		}
        diveSiteTitle.setText(diveLogIndexDisplay + ". " + diveSiteTitleString);

		// Dive Log Diver
		View diverView = view.findViewById(R.id.divelog_item_diver);
		ImageButton diverButton = (ImageButton) diverView
				.findViewById(R.id.divelog_buddy_picture);
		TextView diverUsername = (TextView) diverView
				.findViewById(R.id.divelog_buddy_name);
		mDiveLogListItemDiverImageView.put(diveLog.getLocalId(),
				diverButton);
		mDiveLogListItemDiverTextView.put(diveLog.getLocalId(),
				diverUsername);

		if (mDiverProfileImageCache.get(diveLog.getUserId()) == null) {
			// Need to get diver from the ID, then the diver's picture
			DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(
					getActivity());
			diveSiteOnlineDatabaseUser
					.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

						@Override
						public void onOnlineDiveDataRetrievedComplete(
								ArrayList<Object> resultList,
								String message, Boolean isError) {
							if (resultList.size() > 0) {
								// First save diver so another list item can use it
								Diver diver = (Diver) resultList.get(0);
								mDiveLogListItemDiver.put(diveLog.getUserId(), diver);

								TextView diverUsername = mDiveLogListItemDiverTextView
										.get(diveLog.getLocalId());
								diverUsername.setText(diver.getUsername());

								// Now get bitmap profile image for diver
								LoadOnlineImageTask task = new LoadOnlineImageTask(
										mDiveLogListItemDiverImageView
												.get(diveLog.getLocalId())) {

									@Override
									protected void onPostExecute(
											Bitmap result) {
										Diver diver = (Diver) getTag();
										mDiverProfileImageCache.put(
												diver.getOnlineId(), result);
										ImageButton diverProfileImage = (ImageButton) mDiveLogListItemDiverImageView
												.get(diveLog.getLocalId());
										diverProfileImage
												.setImageBitmap(result);
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
			diveSiteOnlineDatabaseUser.getUser(
					String.valueOf(diveLog.getUserId()), "", "");
		} else {
			diverButton.setImageBitmap(mDiverProfileImageCache.get(diveLog
					.getUserId()));
			if (mDiveLogListItemDiver.get(diveLog.getUserId()) != null) {
				diverUsername.setText(mDiveLogListItemDiver.get(
						diveLog.getUserId()).getUsername());
			}
		}

		diverButton.setTag(diveLog.getUserId());
		diverButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Open profile page
				Intent intent = new Intent(DiveLogListLocalFragment.this
						.getActivity(), DiverActivity.class);
				long diverUserId = (Long) v.getTag();
				intent.putExtra(DiverActivity.EXTRA_DIVER_ID, diverUserId);
				startActivity(intent);
			}
		});

		// Timestamp
		TextView diveLogDate = (TextView) view
				.findViewById(R.id.divelog_item_timestamp);
		diveLogDate.setText(logItemDateFormat.format(diveLog.getTimestamp()) + " "
                + logItemTimeFormat.format(diveLog.getTimestamp()));

		// Rating Bar
		RatingBar diveLogRating = (RatingBar) view
				.findViewById(R.id.divelog_item_rating);
		diveLogRating.setRating((float) diveLog.getRating());

		// Dive Time
		TextView diveLogDiveTime = (TextView) view
				.findViewById(R.id.divelog_item_dive_time);
		diveLogDiveTime.setText(String.valueOf(diveLog.getDiveTime()) + ' '
				+ getResources().getString(R.string.unit_minutes));

		// Gax Mix
		TextView diveLogGasMix = (TextView) view
				.findViewById(R.id.divelog_item_gas_mix);
		diveLogGasMix.setText(diveLog.getAirType());

		// Pressure Change
		TextView diveLogPressureChange = (TextView) view
				.findViewById(R.id.divelog_item_pressure_change);
		if (diveLog.getStartPressure() != ' ' && diveLog.getEndPressure() != ' ') {
			diveLogPressureChange.setText(String.format(getResources()
					.getString(R.string.values_change), diveLog
					.getStartPressure(), diveLog.getEndPressure()));
		} else {
			diveLogPressureChange.setText(String.valueOf(' '));
		}

        // Air Change
        TextView diveLogAirChange = (TextView) view.findViewById(R.id.divelog_item_air_usage);
        diveLogAirChange.setText(String.format(getResources()
                .getString(R.string.values_change),
                diveLog.getStartAir().toString(), diveLog.getEndAir().toString()));

		// Max Depth
		TextView diveLogMaxDepth = (TextView) view
				.findViewById(R.id.divelog_item_max_depth);
		diveLogMaxDepth.setText(diveLog.getMaxDepth().toString());

		// Average Depth
		TextView diveLogAverageDepth = (TextView) view
				.findViewById(R.id.divelog_item_average_depth);
		diveLogAverageDepth.setText(diveLog.getAverageDepth().toString());

		// Air Temperature
		TextView diveLogAirTemperature = (TextView) view
				.findViewById(R.id.divelog_item_air_temperature);
		diveLogAirTemperature.setText(diveLog.getSurfaceTemperature()
				.toString());

		// Water Temperature
		TextView diveLogWaterTemperature = (TextView) view
				.findViewById(R.id.divelog_item_water_temperature);
		diveLogWaterTemperature.setText(diveLog.getWaterTemperature()
				.toString());

		// Comment
		TextView diveLogComment = (TextView) view
				.findViewById(R.id.divelog_item_comment);
		diveLogComment.setText(diveLog.getComments());

		// Visibility
		TextView diveLogVisibility = (TextView) view
				.findViewById(R.id.divelog_item_visibility);
		diveLogVisibility.setText(diveLog.getVisibility().toString());

		// Weights
		TextView diveLogWeights = (TextView) view
				.findViewById(R.id.divelog_item_weights);
		diveLogWeights.setText(diveLog.getWeightsRequired().toString());

		// Surface Time
		TextView diveLogSurfaceTime = (TextView) view
				.findViewById(R.id.divelog_item_surface_time);
		diveLogSurfaceTime.setText(String.valueOf(diveLog.getSurfaceTime())
				+ ' ' + getResources().getString(R.string.unit_minutes));

		// Indicators
		ImageButton diveLogDayIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_day_indicator);
		if (diveLog.isNight()) {
			diveLogDayIndicator.setVisibility(View.GONE);
		} else {
			diveLogDayIndicator.setVisibility(View.VISIBLE);
		}

		ImageButton diveLogNightIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_night_indicator);
		if (!diveLog.isNight()) {
			diveLogNightIndicator.setVisibility(View.GONE);
		} else {
			diveLogNightIndicator.setVisibility(View.VISIBLE);
		}

		ImageButton diveLogPhotoIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_photo_indicator);
		if (diveLog.isPhotoVideo()) {
			diveLogPhotoIndicator.setVisibility(View.VISIBLE);
		} else {
			diveLogPhotoIndicator.setVisibility(View.GONE);
		}

		ImageButton diveLogDeepIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_deep_indicator);
		if (diveLog.isDeep()) {
			diveLogDeepIndicator.setVisibility(View.VISIBLE);
		} else {
			diveLogDeepIndicator.setVisibility(View.GONE);
		}

		ImageButton diveLogIceIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_ice_indicator);
		if (diveLog.isIce()) {
			diveLogIceIndicator.setVisibility(View.VISIBLE);
		} else {
			diveLogIceIndicator.setVisibility(View.GONE);
		}

		ImageButton diveLogCourseIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_course_indicator);
		if (diveLog.isCourse()) {
			diveLogCourseIndicator.setVisibility(View.VISIBLE);
		} else {
			diveLogCourseIndicator.setVisibility(View.GONE);
		}

		ImageButton diveLogInstructorIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_instructor_indicator);
		if (diveLog.isInstructing()) {
			diveLogInstructorIndicator.setVisibility(View.VISIBLE);
		} else {
			diveLogInstructorIndicator.setVisibility(View.GONE);
		}
		
		ImageButton diveLogPublished = (ImageButton) view
				.findViewById(R.id.divelog_indicate_isPublished);
		ImageButton diveLogUnpublished = (ImageButton) view
				.findViewById(R.id.divelog_indicate_isUnpublished);
		if (diveLog.isPublished()) {
			diveLogPublished.setVisibility(View.VISIBLE);
			diveLogUnpublished.setVisibility(View.GONE);
		} else {
			diveLogPublished.setVisibility(View.GONE);
			diveLogUnpublished.setVisibility(View.VISIBLE);
		}

		ImageButton diveLogSaved = (ImageButton) view
				.findViewById(R.id.divelog_indicate_isSaved);
		if (diveLog.getLocalId() != -1) {
			diveLogSaved.setVisibility(View.VISIBLE);
		} else {
			diveLogSaved.setVisibility(View.INVISIBLE);
		}
		
		ImageButton requiresRefresh = (ImageButton)view.findViewById(R.id.divelog_indicate_refresh_required);
		if (diveLog.requiresRefresh()) {
			requiresRefresh.setVisibility(View.VISIBLE);
            requiresRefresh.setEnabled(mDiveLogUpdates.get(diveLog.getLocalId()) != null);
		} else {
			requiresRefresh.setVisibility(View.GONE);
		}

        requiresRefresh.setTag(diveLog);
        requiresRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // Ask user if they want to update
                DiveLog existingDiveLog = (DiveLog) v.getTag();
                final DiveLog updatedDiveLog = mDiveLogUpdates.get(existingDiveLog.getLocalId());

                String updateMessage = getResources().getString(R.string.divelog_requiresRefresh_message);
                if (!existingDiveLog.isPublished()) {
                    updateMessage = updateMessage +
                            getResources().getString(R.string.divelog_requiresRefresh_overwrite);
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.divelog_requiresRefresh)
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

                                        updatedDiveLog.setRequiresRefresh(false);
                                        mDiveSiteManager.saveDiveLog(updatedDiveLog);
                                        refreshDiveLogList();

                                        mProgressDialog.dismiss();
                                    }
                                })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        // Initialize visibility secondary view
        View secondaryView = view.findViewById(R.id.divelog_item_secondary_view);
        final ViewGroup mapContainer = (ViewGroup) view.findViewById(R.id.divelog_list_item_mapView_container);
        if (mSelectedDiveLog != null && diveLog.getLocalId() == mSelectedDiveLog.getLocalId()) {
            view.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
            secondaryView.setVisibility(View.VISIBLE);
            setDiveLogMap(diveLog, mapContainer);
        } else if (mDiveLogItemMapView.getParent() == mapContainer) {
            secondaryView.setVisibility(View.GONE);
            mDiveLogItemMapView.setVisibility(View.GONE);
        }
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (args != null) {
			if (id == DiveCursorLoaders.LOAD_DIVELOG) {
				long diverID = args.getLong(ARG_LOADER_DIVER_ID);
				DiveSite diveSite = args.getParcelable(ARG_LOADER_DIVESITE);

                return new DiveLogListCursorLoader(getActivity(), diverID, diveSite, mPublishMode);

			} else {
				long diveLogID = args.getLong(ARG_LOADER_DIVELOG_ID);

				if (id - mDiveLogListItemLoaderIDs.get(diveLogID) == DiveCursorLoaders.LOAD_DIVELOG_BUDDIES_INDEX) {
					return new DiveCursorLoaders.DiveLogBuddyListCursorLoader(
							getActivity(), diveLogID);
				} else if (id - mDiveLogListItemLoaderIDs.get(diveLogID) == DiveCursorLoaders.LOAD_DIVELOG_STOPS_INDEX) {
					return new DiveCursorLoaders.DiveLogStopListCursorLoader(
							getActivity(), diveLogID);
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
		if (cursor instanceof DiveLogCursor) {
			// Create an adapter to point at this cursor
			if (getListAdapter() == null) {
				DiveLogCursorAdapter adapter = new DiveLogCursorAdapter(
						getActivity(), (DiveLogCursor) cursor);
				setListAdapter(adapter);
			} else {
				((DiveLogCursorAdapter) getListAdapter()).changeCursor(cursor);
			}
			
			if (mRefreshMenuItem != null) {
				mRefreshMenuItem.setActionView(null);
			}

		} else if (cursor instanceof DiveLogBuddyCursor) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				// First find view for the dive log for the dive buddy
				final DiveLogBuddy diveLogBuddy = ((DiveLogBuddyCursor) cursor)
						.getDiveLogBuddy();
				View diveLogView = mDiveLogListItemViews.get(diveLogBuddy
						.getDiveLogLocalId());
				if (diveLogView != null) {		
					// Found the view, now find the buddy list
					LinearLayout buddyListView = (LinearLayout) diveLogView
							.findViewById(R.id.divelog_item_buddy_list);

					// Find the buddy view if it already exists,
					// Otherwise inflate a new buddy view
					View buddyView = buddyListView.findViewWithTag(diveLogBuddy
							.getLocalId());
					if (buddyView == null) {
						LayoutInflater layoutInflater = (LayoutInflater) getActivity()
								.getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);
						buddyView = layoutInflater.inflate(
								R.layout.divelog_buddy_view_item, null);
						buddyView.setTag(diveLogBuddy.getLocalId());

						// Add the view to the list
						buddyListView.addView(buddyView);
					}

					if (diveLogBuddy.getDiverOnlineId() != -1) {
						ImageButton buddyProfile = (ImageButton) buddyView
								.findViewById(R.id.divelog_buddy_picture);

						mDiveLogListItemBuddyImageView.put(
								diveLogBuddy.getLocalId(), buddyProfile);

						// Set listener to access profile
						buddyProfile
								.setOnClickListener(new View.OnClickListener() {

									@Override
									public void onClick(View v) {
										Intent intent = new Intent(
												DiveLogListLocalFragment.this
														.getActivity(),
												DiverActivity.class);
										intent.putExtra(
												DiverActivity.EXTRA_DIVER_ID,
												diveLogBuddy.getDiverOnlineId());
										startActivity(intent);
									}
								});

						// Set image
						if (mDiverProfileImageCache.get(diveLogBuddy
								.getDiverOnlineId()) == null) {
							DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(
									getActivity());
							diveSiteOnlineDatabaseUser
									.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

										@Override
										public void onOnlineDiveDataRetrievedComplete(
												ArrayList<Object> resultList,
												String message, Boolean isError) {
											if (resultList.size() > 0) {
												// First save diver so another
												// list item can use it
												Diver diver = (Diver) resultList
														.get(0);

												// Now get bitmap profile image
												// for diver
												ImageView diverButton = mDiveLogListItemBuddyImageView
														.get(diveLogBuddy
																.getLocalId());
												LoadOnlineImageTask task = new LoadOnlineImageTask(
														diverButton) {

													@Override
													protected void onPostExecute(
															Bitmap result) {
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
							diveSiteOnlineDatabaseUser.getUser(String
									.valueOf(diveLogBuddy.getDiverOnlineId()),
									"", "");
						} else {
							buddyProfile.setImageBitmap(mDiverProfileImageCache
									.get(diveLogBuddy.getDiverOnlineId()));
						}
					}

					// Set buddy view fields
					TextView buddyName = (TextView) buddyView
							.findViewById(R.id.divelog_buddy_name);
					buddyName.setText(diveLogBuddy.getDiverUsername());
				}
				cursor.moveToNext();
			}
		} else if (cursor instanceof DiveLogStopCursor) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				// First find view for the dive log for the dive stop
				DiveLogStop diveLogStop = ((DiveLogStopCursor) cursor)
						.getDiveLogStop();
				View diveLogView = mDiveLogListItemViews.get(diveLogStop
						.getDiveLogLocalId());
				if (diveLogView != null) {
					// Found the view, now find the stop list
					LinearLayout stopListView = (LinearLayout) diveLogView
							.findViewById(R.id.divelog_item_stop_list);

					// Find the stop view if it already exists,
					// Otherwise inflate a new stop view
					View stopView = stopListView.findViewWithTag(diveLogStop
							.getLocalId());
					if (stopView == null) {
						LayoutInflater layoutInflater = (LayoutInflater) getActivity()
								.getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);
						stopView = layoutInflater.inflate(
								R.layout.divelog_stop_view_item, null);
						stopView.setTag(diveLogStop.getLocalId());

						// Add the view to the list
						stopListView.addView(stopView);
					}

					// Set stop view fields
					TextView stopTime = (TextView) stopView
							.findViewById(R.id.divelog_stop_time);
					stopTime.setText(diveLogStop.getTime() + " "
							+ getResources().getString(R.string.unit_minutes));

					TextView stopDepth = (TextView) stopView
							.findViewById(R.id.divelog_stop_depth);
					stopDepth.setText(diveLogStop.getDepth().toString());
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
