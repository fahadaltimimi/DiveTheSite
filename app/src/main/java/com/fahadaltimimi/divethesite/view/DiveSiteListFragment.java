package com.fahadaltimimi.divethesite.view;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager.ErrorDialogFragment;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class DiveSiteListFragment extends ListFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

	public static final String TAG = "DiveSiteListFragment";

	protected static final int REQUEST_NEW_DIVESITE = 0;

	protected DiveSiteManager mDiveSiteManager;

	protected LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
	protected boolean mLocationEnabled;

	protected Bundle mSavedInstanceState;

	protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;

	protected View mLastDisplayedListItemView = null;
	protected TextView mLastDisplayedTitleView = null;
	protected MenuItem mRefreshMenuItem = null;

	protected LinearLayout mListFilter = null;

	protected ImageButton mDiveSiteIndicatorSalt;
	protected ImageButton mDiveSiteIndicatorFresh;
	protected ImageButton mDiveSiteIndicatorShore;
	protected ImageButton mDiveSiteIndicatorBoat;
	protected ImageButton mDiveSiteIndicatorWreck;

	protected LinearLayout mFilterNotificationContainer;
	protected TextView mFilterNotification;
	protected EditText mFilterTitle, mFilterState, mFilterCity = null;
	protected Spinner mFilterCountry = null;
	protected CheckBox mFilterPublished, mFilterUnpublished = null;
	protected Button mFilterClear, mFilterClose;

    protected MapView mDiveSiteItemMapView;
    protected ImageView mDiveSiteItemMapViewSnapShot;

	protected ProgressDialog mProgressDialog;

	protected SharedPreferences mPrefs;

	protected long mRestrictToDiverID = -1;
	protected boolean mSetToDiveLog = false;

	protected DiveSite mSelectedDiveSite = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mSavedInstanceState = savedInstanceState;
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

		mProgressDialog = new ProgressDialog(getActivity());

		mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

		Bundle args = getArguments();
		if (args != null) {
			mRestrictToDiverID = args
					.getLong(DiverTabFragment.ARG_DIVER_ID, -1);
			mSetToDiveLog = args.getBoolean(DiveLogFragment.ARG_SET_TO_DIVELOG,
					false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_divesite_list, parent, false);

		mFilterNotificationContainer = v
				.findViewById(R.id.divesite_list_filter_notification_container);
		mFilterNotification = v
				.findViewById(R.id.divesite_list_filter_notification);

		// Initialize filter panel
		mListFilter = v.findViewById(R.id.divesite_list_filter);

		mFilterTitle = mListFilter
				.findViewById(R.id.divesite_list_filter_title);
		mFilterCountry = mListFilter
				.findViewById(R.id.divesite_list_filter_country);
		mFilterState = mListFilter
				.findViewById(R.id.divesite_list_filter_state);
		mFilterCity = mListFilter
				.findViewById(R.id.divesite_list_filter_city);
		mFilterPublished = mListFilter
				.findViewById(R.id.divesite_list_filter_published);
		mFilterUnpublished = mListFilter
				.findViewById(R.id.divesite_list_filter_unpublished);
		mFilterClear = mListFilter
				.findViewById(R.id.divesite_list_clear_filter);
		mFilterClose = mListFilter
				.findViewById(R.id.divesite_list_close_filter);

		setFilterViews();

		mFilterTitle.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterTitle = c.toString().trim();
				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, "")
						.equals(filterTitle)) {
					if (mSetToDiveLog) {
						mPrefs.edit()
								.putString(
										DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE,
										filterTitle).apply();
					} else {
						mPrefs.edit()
								.putString(
										DiveSiteManager.PREF_FILTER_DIVESITE_TITLE,
										filterTitle).apply();
					}

					filterDiveSiteList();
				}
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

		// Set list to reload after changing text
		mFilterCountry.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View v,
					int position, long id) {
				// Save selection and reset list view
				String filterCountry = (String) parentView
						.getItemAtPosition(position);

				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, "")
						.equals(filterCountry)) {
					if (filterCountry.isEmpty()) {
						filterCountry = getResources().getString(
								R.string.filter_list_all);
					}
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY,
									filterCountry).apply();

					filterDiveSiteList();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Nothing to do in this case
			}

		});

		mFilterState.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterState = c.toString().trim();

				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "").equals(
						filterState)) {
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_DIVESITE_STATE,
									filterState).apply();
					filterDiveSiteList();
				}
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

		mFilterCity.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterCity = c.toString().trim();

				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "").equals(
						filterCity)) {
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_DIVESITE_CITY,
									filterCity).apply();
					filterDiveSiteList();
				}
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

		mFilterPublished
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (!mPrefs
								.getBoolean(
										DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_PUBLISHED,
										true) == isChecked) {
							mPrefs.edit()
									.putBoolean(
											DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_PUBLISHED,
											isChecked).apply();
							filterDiveSiteList();
						}
					}

				});

		mFilterUnpublished
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (!mPrefs
								.getBoolean(
										DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED,
										true) == isChecked) {
							mPrefs.edit()
									.putBoolean(
											DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED,
											isChecked).apply();
							filterDiveSiteList();
						}
					}

				});

		mFilterClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Clear all filters
				mFilterTitle.setText("");
				mFilterCountry.setSelection(0);
				mFilterState.setText("");
				mFilterCity.setText("");
				mFilterPublished.setChecked(true);
				mFilterUnpublished.setChecked(true);
			}
		});

		mFilterClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListFilter.setVisibility(View.GONE);
			}
		});

        mDiveSiteItemMapView = v.findViewById(R.id.divesite_list_item_mapView);
        mDiveSiteItemMapView.onCreate(savedInstanceState);
        mDiveSiteItemMapView.onResume();
        mDiveSiteItemMapView.getLayoutParams().height = (getActivity()
                .getWindowManager().getDefaultDisplay().getHeight()
                - getTitleBarHeight() - getStatusBarHeight()) / 2;
        MapsInitializer.initialize(getActivity());
        mDiveSiteItemMapView.setVisibility(View.GONE);

        mDiveSiteItemMapViewSnapShot = v.findViewById(R.id.divesite_list_item_mapView_snapShot);
        mDiveSiteItemMapViewSnapShot.setVisibility(View.GONE);

		updateFilterNotification();

		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		DiveSite diveSite = getDiveSiteItemClick(position, id);

        if (mLastDisplayedListItemView == v) {
            setSelectedDiveSite(null);

            mLastDisplayedListItemView = null;
            mLastDisplayedTitleView = null;
        } else {
            setSelectedDiveSite(diveSite);

            mLastDisplayedListItemView = v;
            mLastDisplayedTitleView = v .findViewById(R.id.divesite_list_item_titleTextView);
        }

        refreshDiveSiteList();
	}

    protected void setSnapshot(int visibility) {
        if (mSelectedDiveSite != null) {
            switch (visibility) {
                case View.VISIBLE:
                    if (mDiveSiteItemMapView.getVisibility() == View.VISIBLE) {
						mDiveSiteItemMapView.getMapAsync(new OnMapReadyCallback() {
							@Override
							public void onMapReady(GoogleMap googleMap) {
								googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
									@Override
									public void onSnapshotReady(Bitmap bitmap) {
										mDiveSiteItemMapViewSnapShot.setImageBitmap(bitmap);
										mDiveSiteItemMapViewSnapShot.setVisibility(View.VISIBLE);
										mDiveSiteItemMapView.setVisibility(View.INVISIBLE);
									}
								});
							}
						});
                    }
                    break;
                case View.INVISIBLE:
                    if (mDiveSiteItemMapView.getVisibility() == View.INVISIBLE) {
                        mDiveSiteItemMapView.setVisibility(View.VISIBLE);

                        mDiveSiteItemMapViewSnapShot.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    }

	protected void setSelectedDiveSite(DiveSite diveSite) {
		if (mSelectedDiveSite != diveSite) {
			mSelectedDiveSite = diveSite;
			getActivity().invalidateOptionsMenu();
		}
	}

	protected void setFilterViews() {
		if (mFilterTitle != null) {
			if (mSetToDiveLog) {
				mFilterTitle
						.setText(mPrefs
								.getString(
										DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE,
										""));
			} else {
				mFilterTitle.setText(mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_TITLE, ""));
			}
		}

		if (mFilterCountry != null) {
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
			spinnerAdapter.insert(
					getResources().getString(R.string.filter_list_all), 0);
			spinnerAdapter.notifyDataSetChanged();

			// Set selected country
			String currentFilterCountry = mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY,
					getResources().getString(R.string.filter_list_all));
			for (int i = 0; i < spinnerAdapter.getCount(); i++) {
				if (spinnerAdapter.getItem(i).toString()
						.equals(currentFilterCountry)) {
					mFilterCountry.setSelection(i);
					break;
				}
			}
		}

		if (mFilterState != null) {
			mFilterState.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_STATE, ""));
		}

		if (mFilterCity != null) {
			mFilterCity.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_DIVESITE_CITY, ""));
		}

		if (mFilterPublished != null) {
			mFilterPublished.setChecked(mPrefs.getBoolean(
					DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_PUBLISHED, true));
		}

		if (mFilterUnpublished != null) {
			mFilterUnpublished
					.setChecked(mPrefs
							.getBoolean(
									DiveSiteManager.PREF_FILTER_DIVESITE_SHOW_UNPUBLISHED,
									true));
		}

	}

	protected boolean updateFilterNotification() {
		// Inherited
		return false;
	}

	protected void refreshDiveSiteList() {
		// Inherited
	}

	protected void filterDiveSiteList() {
		refreshDiveSiteList();
	}

	protected DiveSite getDiveSiteItemClick(int position, long id) {
		// Inherited
		return null;
	}

	protected void openDiveSite(DiveSite diveSite) {
		if (diveSite != null) {
            if (mLastDisplayedListItemView != null) {
                if (diveSite.isPublished()) {
                    mLastDisplayedListItemView.setBackgroundColor(getResources()
                            .getColor(R.color.itemPublished));
                } else {
                    mLastDisplayedListItemView.setBackgroundColor(getResources()
                            .getColor(R.color.itemUnpublished));
                }
            }

			mLastDisplayedListItemView = null;
			mLastDisplayedTitleView = null;
		}

		// Save local and online id's and finish activity
		Intent intent = new Intent(getActivity(), DiveSiteActivity.class);
		intent.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, diveSite);
		startActivityForResult(intent, REQUEST_NEW_DIVESITE);
	}

	public int getStatusBarHeight() {
		Rect r = new Rect();
		Window w = getActivity().getWindow();
		w.getDecorView().getWindowVisibleDisplayFrame(r);
		return r.top;
	}

	public int getTitleBarHeight() {
		int viewTop = getActivity().getWindow()
				.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		return (viewTop - getStatusBarHeight());
	}

	protected Location getLocation() {
		if ((ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) &&
				mGoogleApiClient != null && mGoogleApiClient.isConnected() &&
				LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
			return  LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		}
		else {
			return mDiveSiteManager.getLastLocation();
		}
	}

    protected void setDiveSiteMap(final DiveSite diveSite, ViewGroup mapContainer) {
        mapContainer.setVisibility(View.VISIBLE);
        ViewGroup existingMapContainter = (ViewGroup) mDiveSiteItemMapView.getParent();
        if (existingMapContainter != mapContainer) {
            if (existingMapContainter != null) {
                final LayoutTransition transition = existingMapContainter.getLayoutTransition();
                existingMapContainter.setLayoutTransition(null);
                existingMapContainter.removeView(mDiveSiteItemMapView);
                existingMapContainter.setLayoutTransition(transition);
            }

            mapContainer.addView(mDiveSiteItemMapView);
        }
        mDiveSiteItemMapView.setVisibility(View.VISIBLE);

        ViewGroup existingMapSnapShotContainter = (ViewGroup) mDiveSiteItemMapViewSnapShot.getParent();
        if (existingMapSnapShotContainter != mapContainer) {
            if (existingMapSnapShotContainter != null) {
                final LayoutTransition transition = existingMapSnapShotContainter.getLayoutTransition();
                existingMapSnapShotContainter.setLayoutTransition(null);
                existingMapSnapShotContainter.removeView(mDiveSiteItemMapViewSnapShot);
                existingMapSnapShotContainter.setLayoutTransition(transition);
            }

            mapContainer.addView(mDiveSiteItemMapViewSnapShot);
        }
        mDiveSiteItemMapView.setVisibility(View.VISIBLE);
        mDiveSiteItemMapViewSnapShot.setVisibility(View.INVISIBLE);

        mDiveSiteItemMapView.onResume();
		mDiveSiteItemMapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				LatLng latLng = new LatLng(diveSite.getLatitude(),
						diveSite.getLongitude());
				MarkerOptions markerOptions = new MarkerOptions().position(latLng);

				if (diveSite.getOnlineId() != -1) {
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.divesite_active_marker));
				} else if (diveSite.isArchived()) {
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.divesite_inactive_marker));
				} else {
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.divesite_active_local_marker));
				}

				googleMap.clear();
				googleMap.addMarker(markerOptions);

				googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(Marker marker) {
						// Directions to this marker using intent
						String destLatLng = marker.getPosition().latitude + ","
								+ marker.getPosition().longitude;
						Intent intent = new Intent(
								android.content.Intent.ACTION_VIEW, Uri
								.parse("http://maps.google.com/maps?daddr="
										+ destLatLng));
						startActivity(intent);

						return true;
					}
				});

				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
			}
		});
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
