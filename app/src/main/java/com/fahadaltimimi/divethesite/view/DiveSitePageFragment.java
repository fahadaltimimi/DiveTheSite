package com.fahadaltimimi.divethesite.view;

import java.util.ArrayList;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager.ErrorDialogFragment;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class DiveSitePageFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

	private static final String TAG = "DiveSitePageFragment";

	public static final int LOAD_DIVESITES = 0;

	protected DiveSiteManager mDiveSiteManager;

	private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
	private boolean mLocationEnabled;

	protected DiveSite mDiveSite;
	protected DiveLog mDiveLog;
	protected Boolean mEditMode;

	private ProgressDialog mProgressDialog;

	private SharedPreferences mPrefs;

	private DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;

	// Listeners
	private DiveSitePageListener mDiveSitePageListener;

	public interface DiveSitePageListener {
		void OnDiveSiteChangedListener(DiveSite newDiveSite);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		mDiveSiteManager = DiveSiteManager.get(getActivity());

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mLocationEnabled = false;
            Toast.makeText(getActivity(), "Enable location services for accurate data", Toast.LENGTH_SHORT).show();
        } else {
        	mLocationEnabled = true;
        }
		
		mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);

		mProgressDialog = new ProgressDialog(getActivity());

		// Check for a Dive Site ID as an argument, and find the Dive Site and
		// Pictures
		Bundle args = getArguments();
		if (args != null) {
			mDiveSite = args.getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
			mDiveLog = args.getParcelable(DiveLogFragment.ARG_DIVELOG);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_divesite, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Make sure edit mode menu item correctly shown
		// If a dive log is set, set edit mode to true and hide button
		MenuItem editModeMenuItem = menu.findItem(R.id.menu_item_edit_mode);
		if (mEditMode) {
			editModeMenuItem.setTitle(R.string.divesite_edit_mode);
		} else {
			editModeMenuItem.setTitle(R.string.divesite_view_mode);
		}

		// Show publish button if site requires publishing
		MenuItem publishMenuItem = menu
				.findItem(R.id.menu_item_publish_divesite);

		// If dive site is online one, show save menu item, hide edit mode and
		// archive buttons
		MenuItem saveMenuItem = menu.findItem(R.id.menu_item_divesite_save);
		MenuItem archiveMenuItem = menu
				.findItem(R.id.menu_item_divesite_archive);
		MenuItem unArchiveMenuItem = menu
				.findItem(R.id.menu_item_divesite_unarchive);

		if (mDiveSite != null) {
			saveMenuItem.setVisible(mDiveSite.getLocalId() == -1);

			if (mDiveLog != null) {
				editModeMenuItem.setVisible(false);
				archiveMenuItem.setVisible(false);
				unArchiveMenuItem.setVisible(false);

			} else if (mDiveSite.getUserId() == mDiveSiteManager
					.getLoggedInDiverId()) {
				editModeMenuItem.setVisible(true);

				if (mDiveSite.isArchived()) {
					archiveMenuItem.setVisible(false);
					unArchiveMenuItem.setVisible(true);
				} else {
					archiveMenuItem.setVisible(true);
					unArchiveMenuItem.setVisible(false);
				}

				publishMenuItem.setVisible(!mDiveSite.isPublished());

			} else {
				editModeMenuItem.setVisible(false);
				saveMenuItem.setVisible(mDiveSite.getLocalId() == -1);
				archiveMenuItem.setVisible(false);
				unArchiveMenuItem.setVisible(false);
				publishMenuItem.setVisible(false);
			}
		} else {
			editModeMenuItem.setVisible(false);
			archiveMenuItem.setVisible(false);
			unArchiveMenuItem.setVisible(false);
			publishMenuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_publish_divesite:
			// Confirm if user wants to publish dive sites, then publish them
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.publish)
					.setMessage(R.string.publish_divesite_message)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(android.R.string.yes,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Create and initialize progress dialog
									mProgressDialog
											.setMessage(getString(R.string.publish_divesite_progress));
									mProgressDialog.setCancelable(false);
									mProgressDialog.setIndeterminate(false);
									mProgressDialog.setProgress(0);
									mProgressDialog.setMax(1);
									mProgressDialog
											.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
									mProgressDialog.show();

									mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(
											getActivity());
									mDiveSiteOnlineDatabase
											.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

												@Override
												public void onOnlineDiveDataRetrievedComplete(
														ArrayList<Object> resultList,
														String message,
														Boolean isError) {

													if (getActivity() != null
															&& message != null
															&& !message
																	.isEmpty()) {
														Toast.makeText(
																getActivity(),
																message,
																Toast.LENGTH_LONG)
																.show();
													}

													// Save dive site
													if (resultList.size() > 0) {
														DiveSite diveSite = (DiveSite) resultList.get(0);
														mDiveSiteManager.saveDiveSite(diveSite);
														setDiveSiteAvailable(diveSite);
													}

													if (mProgressDialog
															.getProgress() == mProgressDialog
															.getMax()) {
														mProgressDialog
																.dismiss();

														// Exit edit mode if
														// we're in it
														if (mEditMode) {
															saveEditMode(false);
															updateUI();
															getActivity()
																	.invalidateOptionsMenu();
														}
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
													// TODO Auto-generated
													// method stub

												}
											});

									mDiveSiteOnlineDatabase
											.publishDiveSite(mDiveSite);

								}
							}).setNegativeButton(android.R.string.no, null)
					.show();

			return true;

		case R.id.menu_item_edit_mode:
			saveEditMode(!mEditMode);
			if (mEditMode) {
				// In edit mode, set text of menu item and go to view mode
				item.setTitle(R.string.divesite_edit_mode);
				updateUI();
			} else {
				// In view mode, set text of menu item and go to edit mode
				item.setTitle(R.string.divesite_view_mode);
				updateUI();
			}
			return true;

		case R.id.menu_item_divesite_save:
			// Save dive site as local one
			mDiveSiteManager.saveDiveSite(mDiveSite);
			getActivity().invalidateOptionsMenu();

			return true;

		case R.id.menu_item_divesite_archive:
			mDiveSite.setArchived(true);
			mDiveSiteManager.saveDiveSite(mDiveSite);
			getActivity().invalidateOptionsMenu();

			return true;

		case R.id.menu_item_divesite_unarchive:
			mDiveSite.setArchived(false);
			mDiveSiteManager.saveDiveSite(mDiveSite);
			getActivity().invalidateOptionsMenu();

			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	public void onStart() {
        super.onStart();

        // Connect the client.
        mGoogleApiClient.connect();
    }

	@Override
	public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
				
		super.onStop();
	}
	
	protected void updateUI() {
		// Read edit mode preference
		if (mPrefs != null) {
			mEditMode = mPrefs.getBoolean(
					DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE, false);
		}
		getActivity().invalidateOptionsMenu();
	}

	public void setOnDiveSiteChangedListener(DiveSitePageListener listen) {
		mDiveSitePageListener = listen;
	}

	protected void OnInitializeDiveSiteLoader() {
		// Do nothing here, may be inherited;
	}

	protected void DoDiveSiteAvailable() {
		// Do nothing here, may be inherited
	}

	protected void saveEditMode(Boolean editMode) {
		mEditMode = editMode;
		mPrefs.edit()
				.putBoolean(DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
						mEditMode).commit();
	}

	protected void setDiveSiteAvailable(DiveSite diveSite) {
		setDiveSite(diveSite);

		// Dive site available, set UI
		getActivity().invalidateOptionsMenu();
		updateUI();

		// Update visibility of menus
		DiveSitePageFragment.this.getActivity().invalidateOptionsMenu();

		if (mDiveSitePageListener != null) {
			mDiveSitePageListener.OnDiveSiteChangedListener(mDiveSite);
		}

		DoDiveSiteAvailable();
	}

	protected void setDiveSite(DiveSite diveSite) {
		mDiveSite = diveSite;
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	/**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            getActivity(),
            DiveSiteManager.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getActivity().getSupportFragmentManager(), TAG);
        }
    }
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (mLocationEnabled) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(1000); // Update location every second

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }
    
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                		getActivity(),
                        DiveSiteManager.CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }
    
    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    	mDiveSiteManager.saveLastLocation(location);
    }
}
