package com.fahadaltimimi.divethesite.view.fragment;

import java.util.ArrayList;
import java.util.Date;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.controller.ListViewHelper;
import com.fahadaltimimi.divethesite.model.DiveLogStop;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.view.activity.DiveSiteActivity;
import com.fahadaltimimi.divethesite.view.activity.DiverActivity;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;

public class DiveLogListOnlineFragment extends DiveLogListFragment {

	private double LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT = 0.60;

	private ListView mListView = null;
	private int mAdditionalItemsToLoad = 0;
	
	private Boolean mRefreshingOnlineDiveLogs = false;

	public static DiveLogListOnlineFragment newInstance(long diverID,
			DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);

		DiveLogListOnlineFragment rf = new DiveLogListOnlineFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DiveLogAdapter adapter = new DiveLogAdapter(new ArrayList<DiveLog>());
		setListAdapter(adapter);

		mProgressDialog = new ProgressDialog(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		// Register list view with context menu
		mListView = v.findViewById(android.R.id.list);
		
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
                            !mRefreshingOnlineDiveLogs &&
                            view.getLastVisiblePosition() >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * view.getCount()) {
                        refreshOnlineDiveLogs();

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
				case R.id.menu_item_save_divelog:
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
							DiveLogAdapter adapter = (DiveLogAdapter) mListView.getAdapter();
							DiveLog diveLog = adapter.getItem(i);

							if (diveLog.getDiveSite() != null) {
								// First save Dive Site, then set Dive Site ID to Dive Log
								diveLog.getDiveSite().setLocalId(mDiveSiteManager.getDiveSiteLocalId(diveLog.getDiveSiteOnlineId()));
								mDiveSiteManager.saveDiveSite(diveLog.getDiveSite());

								diveLog.setDiveSiteLocalId(diveLog.getDiveSite().getLocalId());
							}

							diveLog.setLocalId(mDiveSiteManager.getDiveLogLocalId(diveLog.getOnlineId()));
							mDiveSiteManager.saveDiveLog(diveLog);

							mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);
							if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
								mProgressDialog.dismiss();
							}
						}
					}

					Toast.makeText(getActivity(),R.string.dive_logs_saved_message, Toast.LENGTH_LONG).show();

					mode.finish();

					return true;
					
				case R.id.menu_item_share_divelog:
					// Get summary for each dive log selected
					String combinedSummary = 
						getResources().getString(R.string.share_title_divelog) + "\n\n";
					for (int i = 0; i < getListAdapter().getCount(); i++) {
						if (getListView().isItemChecked(i)) {
							DiveLog diveLog = (DiveLog) getListAdapter().getItem(i);
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

				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.fragment_divelog_list_online_contextual, menu);

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
    public void onResume() {
        super.onResume();

        if (ListViewHelper.shouldRefreshAdditionalListViewItems(mListView)) {
            refreshOnlineDiveLogs();
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
		inflater.inflate(R.menu.fragment_divelog_list_online, menu);

		mRefreshMenuItem = menu.findItem(R.id.menu_item_refresh_divelogs);

		if (mRefreshingOnlineDiveLogs && mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_refresh_divelogs:
			clearDiveLogs();
			refreshOnlineDiveLogs();
			return true;

		case R.id.menu_item_add_divelog:
			// If user not registered, don't allow
			if (mDiveSiteManager.getLoggedInDiverId() == -1) {
				Toast.makeText(getActivity(), R.string.not_registered_create_log, Toast.LENGTH_LONG).show();
				return true;
			}
						
			openDiveLog(null);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void updateFilterNotification() {
		// Determine current filter
		ArrayList<String> filterNotification = new ArrayList<String>();

		if (filterNotification.size() == 0) {
			mFilterNotificationContainer.setVisibility(View.GONE);
		} else {
			mFilterNotificationContainer.setVisibility(View.VISIBLE);
		}

		mFilterNotification.setText(filterNotification.toString());
	}

	private void refreshOnlineDiveLogs() {
		if (mRefreshingOnlineDiveLogs) {
			cancelOnlineRefresh();
		}
		
		mRefreshingOnlineDiveLogs = true;
		
		if (mRefreshMenuItem != null){
			mRefreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
		}

		mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mDiveSiteOnlineDatabase
				.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {

						if (getActivity() != null && message != null
								&& !message.isEmpty()) {
							Toast.makeText(getActivity(), message,
									Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineDiveLogs = false;

						if (mRefreshMenuItem != null) {
							mRefreshMenuItem.setActionView(null);
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSiteOnlineDatabase.getActive()) {
							DiveLog diveLog = getDiveLogOnlineId((DiveLog) result);
							if (diveLog == null) {
								((DiveLogAdapter) getListAdapter())
										.add((DiveLog) result);
							} else {
								int index = getDiveLogIndex(diveLog);
								((DiveLogAdapter) getListAdapter())
										.remove(diveLog);
								((DiveLogAdapter) getListAdapter()).insert(
										(DiveLog) result, index);
							}

							refreshDiveLogList();

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
		
		if (mAdditionalItemsToLoad == 0) {
			mDiveSiteOnlineDatabase.getDiveLogList(new Date(0),
					mRestrictToDiverID, diveSiteID, "", "", "", "", "", "");
		} else {
			mDiveSiteOnlineDatabase.getDiveLogList(new Date(0),
					mRestrictToDiverID, diveSiteID, "", "", "", "",
					String.valueOf(mListView.getCount()),
					String.valueOf(mAdditionalItemsToLoad));
		}
	}

	private int getDiveLogIndex(DiveLog diveLog) {
		int index = -1;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (((DiveLogAdapter) getListAdapter()).getItem(i).getOnlineId() == diveLog
					.getOnlineId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private DiveLog getDiveLogOnlineId(DiveLog diveLog) {
		DiveLog diveLogDuplicate = null;
		for (int i = 0; i < getListAdapter().getCount(); i++) {
			if (((DiveLogAdapter) getListAdapter()).getItem(i).getOnlineId() == diveLog
					.getOnlineId()) {
				diveLogDuplicate = ((DiveLogAdapter) getListAdapter())
						.getItem(i);
				break;
			}
		}
		return diveLogDuplicate;
	}

	@Override
	protected void refreshDiveLogList() {
		updateFilterNotification();
		((DiveLogAdapter) getListAdapter()).notifyDataSetChanged();

        if (!ListViewHelper.shouldRefreshAdditionalListViewItems(mListView)) {
            cancelOnlineRefresh();
        }
	}

	private void clearDiveLogs() {
		// Clears list and resets adapter

        // Update parent fragment if tab
        if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
            ((DiveListTabFragment) getParentFragment()).updateOnlineSubTitles("0", "");
        }

		cancelOnlineRefresh();
		mAdditionalItemsToLoad = 0;
		
		((DiveLogAdapter) getListAdapter()).clear();
		DiveLogAdapter adapter = new DiveLogAdapter(new ArrayList<DiveLog>());
		setListAdapter(adapter);
		((DiveLogAdapter) getListAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineRefresh() {
		if (mDiveSiteOnlineDatabase != null && mDiveSiteOnlineDatabase.getActive()) {
			mDiveSiteOnlineDatabase.stopBackground();
			mDiveSiteOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineDiveLogs = false;
		
		if (mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	@Override
	protected DiveLog getDiveLogItemClick(int position, long id) {
		return ((DiveLogAdapter) getListAdapter()).getItem(position);
	}

	private class DiveLogAdapter extends ArrayAdapter<DiveLog> {

		public DiveLogAdapter(ArrayList<DiveLog> diveLogs) {
			super(getActivity(), 0, diveLogs);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// If we weren't given a view, inflate one using the layout we
			// created for each list item
			if (view == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.divelog_list_item, parent, false);
			}

			final DiveLog diveLog = getItem(position);
			final DiveSite diveSite = diveLog.getDiveSite();

            // Update parent fragment if tab
            if (getParentFragment() != null && getParentFragment() instanceof DiveListTabFragment) {
                int hours = diveLog.getDiveLogTotalMinutesWhenRetreived() / 60;
                int minutes = diveLog.getDiveLogTotalMinutesWhenRetreived() % 60;

                ((DiveListTabFragment) getParentFragment()).updateOnlineSubTitles(
                        String.valueOf(diveLog.getDiveLogCountWhenRetreived()),
                        String.format(getActivity().getResources().getString(R.string.time_format), hours, minutes));
            }

			// Save id to view for later updating with buddies and stops
			mDiveLogListItemViews.put(diveLog.getOnlineId(), view);
			if (mDiveLogListItemLoaderIDs.get(diveLog.getOnlineId()) == null) {
				mDiveLogListItemLoaderIDs.put(diveLog.getOnlineId(),
						mDiveLogListItemLoaderIDs.size());
			}

			// Remove buddies and sites before loading them
			LinearLayout buddyListView = view
					.findViewById(R.id.divelog_item_buddy_list);
			buddyListView.removeAllViews();
			LinearLayout stopListView = view
					.findViewById(R.id.divelog_item_stop_list);
			stopListView.removeAllViews();

			for (int i = 0; i < diveLog.getBuddies().size(); i++) {
				final DiveLogBuddy diveLogBuddy = diveLog.getBuddies().get(i);

				LayoutInflater layoutInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View buddyView = layoutInflater.inflate(
						R.layout.divelog_buddy_view_item, null);
				buddyView.setTag(diveLogBuddy.getOnlineId());

				// Add the view to the list
				buddyListView.addView(buddyView);

				if (diveLogBuddy.getDiverOnlineId() != -1) {
					ImageButton buddyProfile = buddyView
							.findViewById(R.id.divelog_buddy_picture);

					mDiveLogListItemBuddyImageView.put(
							diveLogBuddy.getOnlineId(), buddyProfile);

					// Set listener to access profile
					buddyProfile.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent intent = new Intent(
									DiveLogListOnlineFragment.this
											.getActivity(), DiverActivity.class);
							intent.putExtra(DiverActivity.EXTRA_DIVER_ID,
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
											// First save diver so another list
											// item can use it
											Diver diver = (Diver) resultList.get(0);

											// Now get bitmap profile image for
											// diver
											ImageView diverButton = mDiveLogListItemBuddyImageView.get(diveLogBuddy.getOnlineId());
											LoadOnlineImageTask task = new LoadOnlineImageTask(diverButton) {

												@Override
												protected void onPostExecute(
														Bitmap result) {
													super.onPostExecute(result);
												}

											};
											task.execute(diver.getPictureURL());
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
						diveSiteOnlineDatabaseUser
								.getUser(String.valueOf(diveLogBuddy
										.getDiverOnlineId()), "", "");
					} else {
						buddyProfile.setImageBitmap(mDiverProfileImageCache
								.get(diveLogBuddy.getDiverOnlineId()));
					}
				}

				// Set buddy view fields
				TextView buddyName = buddyView
						.findViewById(R.id.divelog_buddy_name);
				buddyName.setText(diveLogBuddy.getDiverUsername());
			}

			for (int i = 0; i < diveLog.getStops().size(); i++) {
				DiveLogStop diveLogStop = diveLog.getStops().get(i);
				LayoutInflater layoutInflater = (LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View stopView = layoutInflater.inflate(
						R.layout.divelog_stop_view_item, null);
				stopView.setTag(diveLogStop.getOnlineId());

				// Add the view to the list
				stopListView.addView(stopView);

				// Set stop view fields
				TextView stopTime = stopView
						.findViewById(R.id.divelog_stop_time);
				stopTime.setText(diveLogStop.getTime() + " "
						+ getResources().getString(R.string.unit_minutes));

				TextView stopDepth = stopView
						.findViewById(R.id.divelog_stop_depth);
				stopDepth.setText(diveLogStop.getDepth().toString());
			}

			// Set up the view with the dive logs info
			if (diveLog.isPublished()) {
				view.setBackgroundColor(getResources().getColor(
						R.color.itemPublished));
			} else {
				view.setBackgroundColor(getResources().getColor(
						R.color.itemUnpublished));
			}

			// Dive Site Title
            Button diveSiteTitle = view.findViewById(R.id.divelog_item_divesite);
            String diveSiteTitleString = "";
			if (diveSite != null) {
                diveSiteTitleString = diveSite.getName();
				diveSiteTitle.setTag(diveSite);
				diveSiteTitle.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Open the dive site with edit mode set to false
						mPrefs.edit()
								.putBoolean(
										DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
										false).apply();

						DiveSite diveSite = (DiveSite) v.getTag();
						Intent intent = new Intent(getActivity(),
								DiveSiteActivity.class);
						intent.putExtra(DiveSiteManager.EXTRA_DIVE_SITE,
								diveSite);
						startActivity(intent);
					}
				});
			}
            int diveLogCountDisplay = diveLog.getDiveLogCountWhenRetreived() - position;
            diveSiteTitle.setText(diveLogCountDisplay + ". " + diveSiteTitleString);

			// Dive Log Diver
			View diverView = view.findViewById(R.id.divelog_item_diver);
			ImageButton diverButton = diverView
					.findViewById(R.id.divelog_buddy_picture);
			TextView diverUsername = diverView
					.findViewById(R.id.divelog_buddy_name);
			mDiveLogListItemDiverImageView.put(diveLog.getOnlineId(),
					diverButton);
			mDiveLogListItemDiverTextView.put(diveLog.getOnlineId(),
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
									mDiveLogListItemDiver.put(
											diveLog.getOnlineId(), diver);

									TextView diverUsername = mDiveLogListItemDiverTextView
											.get(diveLog.getOnlineId());
									diverUsername.setText(diver.getUsername());

									// Now get bitmap profile image for diver
									LoadOnlineImageTask task = new LoadOnlineImageTask(
											mDiveLogListItemDiverImageView
													.get(diveLog.getOnlineId())) {

										@Override
										protected void onPostExecute(Bitmap result) {
											Diver diver = (Diver) getTag();
											if (diver != null && result != null) {
												mDiverProfileImageCache.put(diver.getOnlineId(), result);
												ImageButton diverProfileImage = 
													(ImageButton) mDiveLogListItemDiverImageView.get(diveLog.getOnlineId());
												diverProfileImage.setImageBitmap(result);
											}
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
				
				if (mDiveLogListItemDiver.get(diveLog.getOnlineId()) != null) {
					diverUsername.setText(mDiveLogListItemDiver.get(
							diveLog.getOnlineId()).getUsername());
				}
			}

			diverButton.setTag(diveLog.getUserId());
			diverButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// Open profile page
					Intent intent = new Intent(DiveLogListOnlineFragment.this
							.getActivity(), DiverActivity.class);
					long diverUserId = (Long) v.getTag();
					intent.putExtra(DiverActivity.EXTRA_DIVER_ID, diverUserId);
					startActivity(intent);
				}
			});

			// Timestamp
			TextView diveLogDate = view
					.findViewById(R.id.divelog_item_timestamp);
			diveLogDate.setText(logItemDateFormat.format(diveLog.getTimestamp()) + " "
					+ logItemTimeFormat.format(diveLog.getTimestamp()));

			// Rating Bar
			RatingBar diveLogRating = view
					.findViewById(R.id.divelog_item_rating);
			diveLogRating.setRating((float) diveLog.getRating());

			// Dive Time
			TextView diveLogDiveTime = view
					.findViewById(R.id.divelog_item_dive_time);
			diveLogDiveTime.setText(String.valueOf(diveLog.getDiveTime()) + ' '
					+ getResources().getString(R.string.unit_minutes));

			// Gax Mix
			TextView diveLogGasMix = view
					.findViewById(R.id.divelog_item_gas_mix);
			diveLogGasMix.setText(diveLog.getAirType());

			// Pressure Change
			TextView diveLogPressureChange = view
					.findViewById(R.id.divelog_item_pressure_change);
			if (diveLog.getStartPressure() != ' ' && diveLog.getEndPressure() != ' ') {
				diveLogPressureChange.setText(String.format(getResources()
						.getString(R.string.values_change), diveLog
						.getStartPressure(), diveLog.getEndPressure()));
			} else {
				diveLogPressureChange.setText(String.valueOf(' '));
			}

            // Air Change
            TextView diveLogAirChange = view.findViewById(R.id.divelog_item_air_usage);
            diveLogAirChange.setText(String.format(getResources()
                    .getString(R.string.values_change),
                    diveLog.getStartAir().toString(), diveLog.getEndAir().toString()));

			// Max Depth
			TextView diveLogMaxDepth = view
					.findViewById(R.id.divelog_item_max_depth);
			diveLogMaxDepth.setText(diveLog.getMaxDepth().toString());

			// Average Depth
			TextView diveLogAverageDepth = view
					.findViewById(R.id.divelog_item_average_depth);
			diveLogAverageDepth.setText(diveLog.getAverageDepth().toString());

			// Air Temperature
			TextView diveLogAirTemperature = view
					.findViewById(R.id.divelog_item_air_temperature);
			diveLogAirTemperature.setText(diveLog.getSurfaceTemperature()
					.toString());

			// Water Temperature
			TextView diveLogWaterTemperature = view
					.findViewById(R.id.divelog_item_water_temperature);
			diveLogWaterTemperature.setText(diveLog.getWaterTemperature()
					.toString());

			// Comment
			TextView diveLogComment = view
					.findViewById(R.id.divelog_item_comment);
			diveLogComment.setText(diveLog.getComments());

			// Visibility
			TextView diveLogVisibility = view
					.findViewById(R.id.divelog_item_visibility);
			diveLogVisibility.setText(diveLog.getVisibility().toString());

			// Weights
			TextView diveLogWeights = view
					.findViewById(R.id.divelog_item_weights);
			diveLogWeights.setText(diveLog.getWeightsRequired().toString());

			// Surface Time
			TextView diveLogSurfaceTime = view
					.findViewById(R.id.divelog_item_surface_time);
			diveLogSurfaceTime.setText(String.valueOf(diveLog.getSurfaceTime())
					+ ' ' + getResources().getString(R.string.unit_minutes));

			// Indicators
			ImageButton diveLogDayIndicator = view
					.findViewById(R.id.divelog_item_day_indicator);
			if (diveLog.isNight()) {
				diveLogDayIndicator.setVisibility(View.GONE);
			} else {
				diveLogDayIndicator.setVisibility(View.VISIBLE);
			}

			ImageButton diveLogNightIndicator = view
					.findViewById(R.id.divelog_item_night_indicator);
			if (!diveLog.isNight()) {
				diveLogNightIndicator.setVisibility(View.GONE);
			} else {
				diveLogNightIndicator.setVisibility(View.VISIBLE);
			}

			ImageButton diveLogPhotoIndicator = view
					.findViewById(R.id.divelog_item_photo_indicator);
			if (diveLog.isPhotoVideo()) {
				diveLogPhotoIndicator.setVisibility(View.VISIBLE);
			} else {
				diveLogPhotoIndicator.setVisibility(View.GONE);
			}

			ImageButton diveLogDeepIndicator = view
					.findViewById(R.id.divelog_item_deep_indicator);
			if (diveLog.isDeep()) {
				diveLogDeepIndicator.setVisibility(View.VISIBLE);
			} else {
				diveLogDeepIndicator.setVisibility(View.GONE);
			}

			ImageButton diveLogIceIndicator = view
					.findViewById(R.id.divelog_item_ice_indicator);
			if (diveLog.isIce()) {
				diveLogIceIndicator.setVisibility(View.VISIBLE);
			} else {
				diveLogIceIndicator.setVisibility(View.GONE);
			}

			ImageButton diveLogCourseIndicator = view
					.findViewById(R.id.divelog_item_course_indicator);
			if (diveLog.isCourse()) {
				diveLogCourseIndicator.setVisibility(View.VISIBLE);
			} else {
				diveLogCourseIndicator.setVisibility(View.GONE);
			}

			ImageButton diveLogInstructorIndicator = view
					.findViewById(R.id.divelog_item_instructor_indicator);
			if (diveLog.isInstructing()) {
				diveLogInstructorIndicator.setVisibility(View.VISIBLE);
			} else {
				diveLogInstructorIndicator.setVisibility(View.GONE);
			}
			
			ImageButton diveLogPublished = view
					.findViewById(R.id.divelog_indicate_isPublished);
			ImageButton diveLogUnpublished = view
					.findViewById(R.id.divelog_indicate_isUnpublished);
			if (diveLog.isPublished()) {
				diveLogPublished.setVisibility(View.VISIBLE);
				diveLogUnpublished.setVisibility(View.GONE);
			} else {
				diveLogPublished.setVisibility(View.GONE);
				diveLogUnpublished.setVisibility(View.VISIBLE);
			}

			ImageButton diveLogSaved = view
					.findViewById(R.id.divelog_indicate_isSaved);
			if (diveLog.getLocalId() != -1) {
				diveLogSaved.setVisibility(View.VISIBLE);
			} else {
				diveLogSaved.setVisibility(View.INVISIBLE);
			}

            // Initialize visibility secondary view
            View secondaryView = view.findViewById(R.id.divelog_item_secondary_view);
            final ViewGroup mapContainer = view.findViewById(R.id.divelog_list_item_mapView_container);
            if (diveLog == mSelectedDiveLog) {
                view.setBackgroundColor(getResources().getColor(R.color.diveSiteSelected));
                secondaryView.setVisibility(View.VISIBLE);
                setDiveLogMap(diveLog, mapContainer);
            } else if (mDiveLogItemMapView.getParent() == mapContainer) {
                secondaryView.setVisibility(View.GONE);
                mapContainer.setVisibility(View.GONE);
            }

            mAdditionalItemsToLoad = ListViewHelper.additionalItemCountToLoad(mListView);

			return view;
		}
	}
}
