package com.fahadaltimimi.divethesite.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.data.DiveCursorLoaders;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveSiteCursor;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager.ErrorDialogFragment;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.NDBCStation;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCDriftingBuoyData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCMeteorologicalData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCSpectralWaveData;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.data.SQLiteCursorLoader;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DiveSiteFullMapFragment extends Fragment implements
		LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

	public static final String TAG = "DiveSiteFullMapFragment";
	
	private static final int REQUEST_NEW_DIVESITE = 0;

	private static final int MINIMUM_ZOOM_LEVEL_FOR_DATA = 6;

	protected DiveSiteManager mDiveSiteManager;
	
	private LocationRequest mLocationRequest;
    protected GoogleApiClient mGoogleApiClient;
	private boolean mLocationEnabled;
	
	private Bundle mSavedInstanceState;

	private MenuItem mRefreshMenuItem = null;

	private SharedPreferences mPrefs;
	private Date mLastDiveSiteRefreshTime;
	private boolean mArchives = false;

	private MapView mMapView;
	private GoogleMap mGoogleMap;

	private LinearLayout mDiveSiteDataViewContainer;
	private LinearLayout mDiveSiteLatestMeteorologicalData;
	private LinearLayout mDiveSiteLatestWaveData;

	private LinearLayout mDiveSiteDataToolbar;
	private Button mMeteorologicalDataButton;
	private Button mWaveDataButton;

	private TextView mNDBCMeteorologicalDataTitle;
	private TextView mNDBCMeteorologicalDataLabel00,
			mNDBCMeteorologicalDataValue00;
	private TextView mNDBCMeteorologicalDataLabel01,
			mNDBCMeteorologicalDataValue01;
	private TextView mNDBCMeteorologicalDataLabel02,
			mNDBCMeteorologicalDataValue02;
	private TextView mNDBCMeteorologicalDataLabel10,
			mNDBCMeteorologicalDataValue10;
	private TextView mNDBCMeteorologicalDataLabel11,
			mNDBCMeteorologicalDataValue11;
	private TextView mNDBCMeteorologicalDataLabel12,
			mNDBCMeteorologicalDataValue12;
	private TextView mNDBCMeteorologicalDataLabel20,
			mNDBCMeteorologicalDataValue20;
	private TextView mNDBCMeteorologicalDataLabel21,
			mNDBCMeteorologicalDataValue21;
	private TextView mNDBCMeteorologicalDataLabel22,
			mNDBCMeteorologicalDataValue22;

	private TextView mNDBCWaveDataTitle;
	private TextView mNDBCWaveDataLabel00, mNDBCWaveDataValue00;
	private TextView mNDBCWaveDataLabel01, mNDBCWaveDataValue01;
	private TextView mNDBCWaveDataLabel02, mNDBCWaveDataValue02;
	private TextView mNDBCWaveDataLabel10, mNDBCWaveDataValue10;
	private TextView mNDBCWaveDataLabel11, mNDBCWaveDataValue11;
	private TextView mNDBCWaveDataLabel12, mNDBCWaveDataValue12;
	private TextView mNDBCWaveDataLabel20, mNDBCWaveDataValue20;
	private TextView mNDBCWaveDataLabel21, mNDBCWaveDataValue21;
	private TextView mNDBCWaveDataLabel22, mNDBCWaveDataValue22;

	private HashMap<DiveSite, Marker> mVisibleDiveSiteMarkers;
	private Marker mAddDiveSiteMarker;
	private ArrayList<Marker> mSearchResultsMarkers;
	private HashMap<NDBCStation, Marker> mVisibleNDBCStationMarkers;

	private DiveSiteListCursorLoader mDiveSiteListLoader = null;

	private Boolean mRefreshingOnlineDiveSites = false;
	private Boolean mRefreshingOnlineNDBCData = false;

	private Boolean mNDCPBuoysDisplayed = true;
	private Boolean mOnlineSitesDisplayed = true;
	private Boolean mSavedSitesDisplayed = true;

	private Geocoder mGeocoder;

	private DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase = null;
	private DiveSiteOnlineDatabaseLink mNDBCDataOnlineDatabase = null;

	private static final double EARTHRADIUS = 6366198;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

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
		
		mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);
		mLastDiveSiteRefreshTime = new Date(mPrefs.getLong(
				DiveSiteManager.PREF_LAST_DIVESITES_REFRESH_DATE, 0));

		mVisibleDiveSiteMarkers = new HashMap<DiveSite, Marker>();
		mSearchResultsMarkers = new ArrayList<Marker>();
		mVisibleNDBCStationMarkers = new HashMap<NDBCStation, Marker>();

		// Initialize the loader to load the list of Dive Sites
		mDiveSiteListLoader = (DiveSiteListCursorLoader) getLoaderManager()
				.initLoader(DiveCursorLoaders.LOAD_DIVESITE, null, this);

		mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_divesite_fullmap, parent,
				false);

		getActivity().setTitle(R.string.mapTitle);

		mMapView = (MapView) v.findViewById(R.id.diveSite_fullMap_MapView);
		mMapView.onCreate(savedInstanceState);

		mDiveSiteDataViewContainer = (LinearLayout) v
				.findViewById(R.id.diveSite_fullMap_data_view_container);

		// Create and add views for each type of data
		mDiveSiteLatestMeteorologicalData = (LinearLayout) inflater.inflate(
				R.layout.ndbc_station_data_item, mDiveSiteDataViewContainer,
				false);
		mDiveSiteLatestMeteorologicalData.setVisibility(View.GONE);
		mDiveSiteDataViewContainer.addView(mDiveSiteLatestMeteorologicalData);

		mNDBCMeteorologicalDataTitle = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_station_data_item_title);
		mNDBCMeteorologicalDataLabel00 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_00);
		mNDBCMeteorologicalDataValue00 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_00);
		mNDBCMeteorologicalDataLabel01 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_01);
		mNDBCMeteorologicalDataValue01 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_01);
		mNDBCMeteorologicalDataLabel02 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_02);
		mNDBCMeteorologicalDataValue02 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_02);
		mNDBCMeteorologicalDataLabel10 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_10);
		mNDBCMeteorologicalDataValue10 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_10);
		mNDBCMeteorologicalDataLabel11 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_11);
		mNDBCMeteorologicalDataValue11 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_11);
		mNDBCMeteorologicalDataLabel12 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_12);
		mNDBCMeteorologicalDataValue12 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_12);
		mNDBCMeteorologicalDataLabel20 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_20);
		mNDBCMeteorologicalDataValue20 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_20);
		mNDBCMeteorologicalDataLabel21 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_21);
		mNDBCMeteorologicalDataValue21 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_21);
		mNDBCMeteorologicalDataLabel22 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_22);
		mNDBCMeteorologicalDataValue22 = (TextView) mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_22);

		mDiveSiteLatestWaveData = (LinearLayout) inflater.inflate(
				R.layout.ndbc_station_data_item, mDiveSiteDataViewContainer,
				false);
		mDiveSiteLatestWaveData.setVisibility(View.GONE);
		mDiveSiteDataViewContainer.addView(mDiveSiteLatestWaveData);

		mNDBCWaveDataTitle = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_station_data_item_title);
		mNDBCWaveDataLabel00 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_00);
		mNDBCWaveDataValue00 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_00);
		mNDBCWaveDataLabel01 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_01);
		mNDBCWaveDataValue01 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_01);
		mNDBCWaveDataLabel02 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_02);
		mNDBCWaveDataValue02 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_02);
		mNDBCWaveDataLabel10 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_10);
		mNDBCWaveDataValue10 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_10);
		mNDBCWaveDataLabel11 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_11);
		mNDBCWaveDataValue11 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_11);
		mNDBCWaveDataLabel12 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_12);
		mNDBCWaveDataValue12 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_12);
		mNDBCWaveDataLabel20 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_20);
		mNDBCWaveDataValue20 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_20);
		mNDBCWaveDataLabel21 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_21);
		mNDBCWaveDataValue21 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_21);
		mNDBCWaveDataLabel22 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_22);
		mNDBCWaveDataValue22 = (TextView) mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_22);

		mDiveSiteDataToolbar = (LinearLayout) v
				.findViewById(R.id.diveSite_fullMap_data_toolbar);

		mMeteorologicalDataButton = (Button) v
				.findViewById(R.id.diveSite_fullMap_meteorological);
		mMeteorologicalDataButton
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mDiveSiteLatestMeteorologicalData.getVisibility() == View.GONE) {
							mDiveSiteLatestMeteorologicalData
									.setVisibility(View.VISIBLE);
						} else {
							mDiveSiteLatestMeteorologicalData
									.setVisibility(View.GONE);
						}
					}
				});

		mWaveDataButton = (Button) v.findViewById(R.id.diveSite_fullMap_wave);
		mWaveDataButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDiveSiteLatestWaveData.getVisibility() == View.GONE) {
					mDiveSiteLatestWaveData.setVisibility(View.VISIBLE);
				} else {
					mDiveSiteLatestWaveData.setVisibility(View.GONE);
				}
			}
		});

		mMapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				mGoogleMap = googleMap;

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }

				mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

					@Override
					public void onMapLongClick(LatLng latLng) {
						// Remove existing add marker if it exists
						if (mAddDiveSiteMarker != null) {
							mAddDiveSiteMarker.remove();
						}

						// Add marker for user to click to add a new dive site
						MarkerOptions markerOptions = new MarkerOptions()
								.position(latLng)
								.title(getResources().getString(
										R.string.divesite_add_divesite_here))
								.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.divesite_add_marker));

						mAddDiveSiteMarker = mGoogleMap.addMarker(markerOptions);
						mAddDiveSiteMarker.showInfoWindow();
					}
				});

				mGoogleMap.setOnMapClickListener(new OnMapClickListener() {

					@Override
					public void onMapClick(LatLng arg0) {
						// Remove existing add marker if it exists
						if (mAddDiveSiteMarker != null) {
							mAddDiveSiteMarker.remove();
						}

						// Hide data
						hideCurrentlyDisplayedData();
					}
				});

				mGoogleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(Marker marker) {
						if (mVisibleNDBCStationMarkers.containsValue(marker)) {

							// Load buoy data then show data toolbar
							NDBCStation selectedStation = null;
							Object[] ndbcStations = mVisibleNDBCStationMarkers.keySet()
									.toArray();
							for (int i = 0; i < ndbcStations.length; i++) {
								if (mVisibleNDBCStationMarkers.get(ndbcStations[i])
										.equals(marker)) {
									selectedStation = (NDBCStation) ndbcStations[i];
									break;
								}
							}

							loadDataForStation(selectedStation);
						}

						return false;
					}

				});

				mGoogleMap
						.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
							@Override
							public void onInfoWindowClick(Marker marker) {
								// If user clicked add marker, add a new one, otherwise
								// search for existing dive site
								if (marker.equals(mAddDiveSiteMarker)) {
									// Open the dive site in edit mode
									mPrefs.edit()
											.putBoolean(
													DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
													true).commit();

									// Add a new dive site with the selected markers
									// location and switch to Dive Site view
									DiveSite diveSite = new DiveSite(mDiveSiteManager
											.getLoggedInDiverId(), mDiveSiteManager
											.getLoggedInDiverUsername());
									diveSite.setLatitude(marker.getPosition().latitude);
									diveSite.setLongitude(marker.getPosition().longitude);

									mDiveSiteManager.insertDiveSite(diveSite);
									openDiveSite(diveSite);
								} else {
									mPrefs.edit()
											.putBoolean(
													DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
													false).commit();

									// Display Dive Site info
									Object[] diveSites = mVisibleDiveSiteMarkers
											.keySet().toArray();
									for (int i = 0; i < diveSites.length; i++) {
										if (mVisibleDiveSiteMarkers.get(diveSites[i])
												.equals(marker)) {
											// Dive Site found
											openDiveSite((DiveSite) diveSites[i]);
											break;
										}
									}
								}
							}
						});

				mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

					@Override
					public void onCameraChange(CameraPosition cameraPosition) {
						// Only refresh dive sites and station data if we're zoomed in
						// enough, otherwise display message
						if (cameraPosition.zoom >= MINIMUM_ZOOM_LEVEL_FOR_DATA) {
							refreshDiveSiteList();
							refreshVisibleNDBCStations();
							if (!mArchives) {
								refreshOnlineDiveSites();
							}
						} else {
							Toast.makeText(getActivity().getApplicationContext(),
									R.string.divesite_map_zoom_view_data,
									Toast.LENGTH_SHORT).show();
						}
					}

				});

				if (getLocation() != null) {
					CameraPosition cameraPosition = new CameraPosition.Builder()
							.target(new LatLng(getLocation().getLatitude(),
									getLocation().getLongitude())).zoom(7).build();
					mGoogleMap.animateCamera(CameraUpdateFactory
							.newCameraPosition(cameraPosition));
				}
			}
		});

        MapsInitializer.initialize(this.getActivity());

		return v;
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

		// Cancel running task
		if (mDiveSiteOnlineDatabase != null
				&& !mDiveSiteOnlineDatabase.isCancelled()) {
			mDiveSiteOnlineDatabase.stopBackground();
			mDiveSiteOnlineDatabase.cancel(true);
		}

		if (mNDBCDataOnlineDatabase != null
				&& !mNDBCDataOnlineDatabase.isCancelled()) {
			mNDBCDataOnlineDatabase.stopBackground();
			mNDBCDataOnlineDatabase.cancel(true);
		}
	}

	public void loadDataForStation(NDBCStation station) {
		// Load new data if user update time is less than station's online
		// update time
		if (station != null) {
			if (station.getLastUserUpdate().before(
					station.getLastOnlineUpdate())) {
				mRefreshMenuItem
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				mRefreshMenuItem
						.setActionView(R.layout.actionbar_indeterminate_progress);

				DiveSiteOnlineDatabaseLink diveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(
						getActivity());
				diveSiteOnlineDatabase
						.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

							@Override
							public void onOnlineDiveDataRetrievedComplete(
									ArrayList<Object> resultList,
									String message, Boolean isError) {
								// Replace existing station with updated one
								if (resultList.size() > 0) {
									NDBCStation updatedNDBCStation = (NDBCStation) resultList
											.get(0);
									Object[] ndbcStations = mVisibleNDBCStationMarkers
											.keySet().toArray();
									for (int i = 0; i < ndbcStations.length; i++) {
										if (((NDBCStation) ndbcStations[i])
												.getStationId() == updatedNDBCStation
												.getStationId()) {
											NDBCStation existingNDBCStation = (NDBCStation) ndbcStations[i];
											Marker existingMarker = mVisibleNDBCStationMarkers
													.get(existingNDBCStation);

											mVisibleNDBCStationMarkers
													.remove(existingNDBCStation);
											mVisibleNDBCStationMarkers.put(
													updatedNDBCStation,
													existingMarker);

											break;
										}
									}

									showDataForStation(updatedNDBCStation);
								} else {
									Toast.makeText(
											getActivity()
													.getApplicationContext(),
											message, Toast.LENGTH_LONG).show();
								}

                                if (!mRefreshingOnlineDiveSites && !mRefreshingOnlineNDBCData && mRefreshMenuItem != null) {
                                    mRefreshMenuItem.setActionView(null);
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

				diveSiteOnlineDatabase.updateNDBCDataForStation(station, "1");
			} else {
				showDataForStation(station);
			}
		}
	}
    
	private void showDataForStation(NDBCStation station) {
		// Hide buttons first
		mMeteorologicalDataButton.setVisibility(View.GONE);
		mWaveDataButton.setVisibility(View.GONE);

		// Show toolbar
		mDiveSiteDataToolbar.setVisibility(View.VISIBLE);

		// Show valid buttons and latest data for station
		if (station.getMeteorologicalDataCount() > 0) {
			NDBCMeteorologicalData data = station.getLatestMeteorologicalData();
			String naString = getResources().getString(R.string.na_string);

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources()
							.getString(R.string.meteorologicalDataTitle));
			mNDBCMeteorologicalDataTitle.setText(title);

			mNDBCMeteorologicalDataLabel00.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel00.setText(getResources().getString(
					R.string.ndbc_station_data_air_temp));

			mNDBCMeteorologicalDataValue00.setVisibility(View.VISIBLE);
			if (data.getAirTemperature() == null) {
				mNDBCMeteorologicalDataValue00.setText(naString);
			} else {
				String value = String
						.format(getResources().getString(
								R.string.temperatureDegCValue),
								data.getAirTemperature());
				mNDBCMeteorologicalDataValue00.setText(value);
			}

			mNDBCMeteorologicalDataLabel01.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel01.setText(getResources().getString(
					R.string.ndbc_station_data_water_temp));

			mNDBCMeteorologicalDataValue01.setVisibility(View.VISIBLE);
			if (data.getWaterTemperature() == null) {
				mNDBCMeteorologicalDataValue01.setText(naString);
			} else {
				String value = String
						.format(getResources().getString(
								R.string.temperatureDegCValue),
								data.getWaterTemperature());
				mNDBCMeteorologicalDataValue01.setText(value);
			}

			mNDBCMeteorologicalDataLabel10.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel10.setText(getResources().getString(
					R.string.ndbc_station_data_wind_direction));

			mNDBCMeteorologicalDataValue10.setVisibility(View.VISIBLE);
			if (data.getWindDirection() == null) {
				mNDBCMeteorologicalDataValue10.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.directionDegTValue),
						data.getWindDirection());
				mNDBCMeteorologicalDataValue10.setText(value);
			}

			mNDBCMeteorologicalDataLabel11.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel11.setText(getResources().getString(
					R.string.ndbc_station_data_wind_speed));

			mNDBCMeteorologicalDataValue11.setVisibility(View.VISIBLE);
			if (data.getWindSpeed() == null) {
				mNDBCMeteorologicalDataValue11.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.speedMSValue),
						data.getWindSpeed());
				mNDBCMeteorologicalDataValue11.setText(value);
			}

			mNDBCMeteorologicalDataLabel12.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel12.setText(getResources().getString(
					R.string.ndbc_station_data_wind_gust));

			mNDBCMeteorologicalDataValue12.setVisibility(View.VISIBLE);
			if (data.getWindGust() == null) {
				mNDBCMeteorologicalDataValue12.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.speedMSValue),
						data.getWindGust());
				mNDBCMeteorologicalDataValue12.setText(value);
			}

			mNDBCMeteorologicalDataLabel02.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel02.setText(getResources().getString(
					R.string.ndbc_station_data_tide));

			mNDBCMeteorologicalDataValue02.setVisibility(View.VISIBLE);
			if (data.getTide() == null) {
				mNDBCMeteorologicalDataValue02.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceFTValue),
						data.getTide());
				mNDBCMeteorologicalDataValue02.setText(value);
			}

			mNDBCMeteorologicalDataLabel20.setVisibility(View.GONE);
			mNDBCMeteorologicalDataLabel21.setVisibility(View.GONE);
			mNDBCMeteorologicalDataLabel22.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue20.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue21.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue22.setVisibility(View.GONE);

			mMeteorologicalDataButton.setVisibility(View.VISIBLE);
		} else if (station.getDriftingBuoyDataCount() > 0) {
			NDBCDriftingBuoyData data = station.getLatestDriftingBuoyData();
			String naString = getResources().getString(R.string.na_string);

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources()
							.getString(R.string.meteorologicalDataTitle));
			mNDBCMeteorologicalDataTitle.setText(title);

			mNDBCMeteorologicalDataLabel00.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel00.setText(getResources().getString(
					R.string.ndbc_station_data_air_temp));

			mNDBCMeteorologicalDataValue00.setVisibility(View.VISIBLE);
			if (data.getAirTemperature() == null) {
				mNDBCMeteorologicalDataValue00.setText(naString);
			} else {
				String value = String
						.format(getResources().getString(
								R.string.temperatureDegCValue),
								data.getAirTemperature());
				mNDBCMeteorologicalDataValue00.setText(value);
			}

			mNDBCMeteorologicalDataLabel01.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel01.setText(getResources().getString(
					R.string.ndbc_station_data_water_temp));

			mNDBCMeteorologicalDataValue01.setVisibility(View.VISIBLE);
			if (data.getWaterTemperature() == null) {
				mNDBCMeteorologicalDataValue01.setText(naString);
			} else {
				String value = String
						.format(getResources().getString(
								R.string.temperatureDegCValue),
								data.getWaterTemperature());
				mNDBCMeteorologicalDataValue01.setText(value);
			}

			mNDBCMeteorologicalDataLabel10.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel10.setText(getResources().getString(
					R.string.ndbc_station_data_wind_direction));

			mNDBCMeteorologicalDataValue10.setVisibility(View.VISIBLE);
			if (data.getWindDirection() == null) {
				mNDBCMeteorologicalDataValue10.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.directionDegTValue),
						data.getWindDirection());
				mNDBCMeteorologicalDataValue10.setText(value);
			}

			mNDBCMeteorologicalDataLabel11.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel11.setText(getResources().getString(
					R.string.ndbc_station_data_wind_speed));

			mNDBCMeteorologicalDataValue11.setVisibility(View.VISIBLE);
			if (data.getWindSpeed() == null) {
				mNDBCMeteorologicalDataValue11.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.speedMSValue),
						data.getWindSpeed());
				mNDBCMeteorologicalDataValue11.setText(value);
			}

			mNDBCMeteorologicalDataLabel12.setVisibility(View.VISIBLE);
			mNDBCMeteorologicalDataLabel12.setText(getResources().getString(
					R.string.ndbc_station_data_wind_gust));

			mNDBCMeteorologicalDataValue12.setVisibility(View.VISIBLE);
			if (data.getWindGust() == null) {
				mNDBCMeteorologicalDataValue12.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.speedMSValue),
						data.getWindGust());
				mNDBCMeteorologicalDataValue12.setText(value);
			}

			mNDBCMeteorologicalDataLabel02.setVisibility(View.GONE);
			mNDBCMeteorologicalDataLabel20.setVisibility(View.GONE);
			mNDBCMeteorologicalDataLabel21.setVisibility(View.GONE);
			mNDBCMeteorologicalDataLabel22.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue02.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue20.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue21.setVisibility(View.GONE);
			mNDBCMeteorologicalDataValue22.setVisibility(View.GONE);

			mMeteorologicalDataButton.setVisibility(View.VISIBLE);
		}

		if (station.getSpectralWaveDataCount() > 0) {
			NDBCSpectralWaveData data = station.getLatestSpectralWaveData();
			String naString = getResources().getString(R.string.na_string);

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources().getString(R.string.waveDataTitle));
			mNDBCWaveDataTitle.setText(title);

			mNDBCWaveDataLabel00.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel00.setText(getResources().getString(
					R.string.ndbc_station_data_swell_height));

			mNDBCWaveDataValue00.setVisibility(View.VISIBLE);
			if (data.getSwellHeight() == null) {
				mNDBCWaveDataValue00.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceMValue),
						data.getSwellHeight());
				mNDBCWaveDataValue00.setText(value);
			}

			mNDBCWaveDataLabel01.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel01.setText(getResources().getString(
					R.string.ndbc_station_data_swell_period));

			mNDBCWaveDataValue01.setVisibility(View.VISIBLE);
			if (data.getSwellPeriod() == null) {
				mNDBCWaveDataValue01.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.timeSValue),
						data.getSwellPeriod());
				mNDBCWaveDataValue01.setText(value);
			}

			mNDBCWaveDataLabel02.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel02.setText(getResources().getString(
					R.string.ndbc_station_data_swell_direction));

			mNDBCWaveDataValue02.setVisibility(View.VISIBLE);
			if (data.getSwellDirection() == null) {
				mNDBCWaveDataValue02.setText(naString);
			} else {
				String value = data.getSwellDirection();
				mNDBCWaveDataValue02.setText(value);
			}

			mNDBCWaveDataLabel10.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel10.setText(getResources().getString(
					R.string.ndbc_station_data_wave_height));

			mNDBCWaveDataValue10.setVisibility(View.VISIBLE);
			if (data.getWaveHeight() == null) {
				mNDBCWaveDataValue10.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceMValue),
						data.getWaveHeight());
				mNDBCWaveDataValue10.setText(value);
			}

			mNDBCWaveDataLabel11.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel11.setText(getResources().getString(
					R.string.ndbc_station_data_average_wave_period));

			mNDBCWaveDataValue11.setVisibility(View.VISIBLE);
			if (data.getAverageWavePeriod() == null) {
				mNDBCWaveDataValue11.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.timeSValue),
						data.getAverageWavePeriod());
				mNDBCWaveDataValue11.setText(value);
			}

			mNDBCWaveDataLabel12.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel12.setText(getResources().getString(
					R.string.ndbc_station_data_wave_steepness));

			mNDBCWaveDataValue12.setVisibility(View.VISIBLE);
			if (data.getWaveSteepness() == null) {
				mNDBCWaveDataValue12.setText(naString);
			} else {
				String value = data.getWaveSteepness();
				mNDBCWaveDataValue12.setText(value);
			}

			mNDBCWaveDataLabel20.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel20.setText(getResources().getString(
					R.string.ndbc_station_data_wind_wave_height));

			mNDBCWaveDataValue20.setVisibility(View.VISIBLE);
			if (data.getWindWaveHeight() == null) {
				mNDBCWaveDataValue20.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceMValue),
						data.getWindWaveHeight());
				mNDBCWaveDataValue20.setText(value);
			}

			mNDBCWaveDataLabel21.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel21.setText(getResources().getString(
					R.string.ndbc_station_data_wind_wave_period));

			mNDBCWaveDataValue21.setVisibility(View.VISIBLE);
			if (data.getWindWavePeriod() == null) {
				mNDBCWaveDataValue21.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.timeSValue),
						data.getWindWavePeriod());
				mNDBCWaveDataValue21.setText(value);
			}

			mNDBCWaveDataLabel22.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel22.setText(getResources().getString(
					R.string.ndbc_station_data_wind_wave_direction));

			mNDBCWaveDataValue22.setVisibility(View.VISIBLE);
			if (data.getWindWaveDirection() == null) {
				mNDBCWaveDataValue22.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceMValue),
						data.getWindWaveDirection());
				mNDBCWaveDataValue22.setText(value);
			}

			mWaveDataButton.setVisibility(View.VISIBLE);
		} else if (station.getMeteorologicalDataCount() > 0) {
			// Spectral Wave data not available, display wave data from
			// meteotrological data
			NDBCMeteorologicalData data = station.getLatestMeteorologicalData();
			String naString = getResources().getString(R.string.na_string);

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources().getString(R.string.waveDataTitle));
			mNDBCWaveDataTitle.setText(title);

			mNDBCWaveDataLabel00.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel00.setText(getResources().getString(
					R.string.ndbc_station_data_wave_height));

			mNDBCWaveDataValue00.setVisibility(View.VISIBLE);
			if (data.getSignificantWaveHeight() == null) {
				mNDBCWaveDataValue00.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceMValue),
						data.getSignificantWaveHeight());
				mNDBCWaveDataValue00.setText(value);
			}

			mNDBCWaveDataLabel01.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel01.setText(getResources().getString(
					R.string.ndbc_station_data_dominant_wave_period));

			mNDBCWaveDataValue01.setVisibility(View.VISIBLE);
			if (data.getDominantWavePeriod() == null) {
				mNDBCWaveDataValue01.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.timeSValue),
						data.getDominantWavePeriod());
				mNDBCWaveDataValue01.setText(value);
			}

			mNDBCWaveDataLabel10.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel10.setText(getResources().getString(
					R.string.ndbc_station_data_dominant_wave_direction));

			mNDBCWaveDataValue10.setVisibility(View.VISIBLE);
			if (data.getDominantWaveDirection() == null) {
				mNDBCWaveDataValue10.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.distanceMValue),
						data.getDominantWaveDirection());
				mNDBCWaveDataValue10.setText(value);
			}

			mNDBCWaveDataLabel11.setVisibility(View.VISIBLE);
			mNDBCWaveDataLabel11.setText(getResources().getString(
					R.string.ndbc_station_data_average_wave_period));

			mNDBCWaveDataValue11.setVisibility(View.VISIBLE);
			if (data.getAverageWavePeriod() == null) {
				mNDBCWaveDataValue11.setText(naString);
			} else {
				String value = String.format(
						getResources().getString(R.string.timeSValue),
						data.getAverageWavePeriod());
				mNDBCWaveDataValue11.setText(value);
			}

			mNDBCWaveDataLabel02.setVisibility(View.GONE);
			mNDBCWaveDataValue02.setVisibility(View.GONE);

			mNDBCWaveDataLabel12.setVisibility(View.GONE);
			mNDBCWaveDataValue12.setVisibility(View.GONE);

			mNDBCWaveDataLabel20.setVisibility(View.GONE);
			mNDBCWaveDataValue20.setVisibility(View.GONE);

			mNDBCWaveDataLabel21.setVisibility(View.GONE);
			mNDBCWaveDataValue21.setVisibility(View.GONE);

			mNDBCWaveDataLabel22.setVisibility(View.GONE);
			mNDBCWaveDataValue22.setVisibility(View.GONE);

			mWaveDataButton.setVisibility(View.VISIBLE);
		}

        if (!mRefreshingOnlineDiveSites && !mRefreshingOnlineNDBCData && mRefreshMenuItem != null) {
            mRefreshMenuItem.setActionView(null);
        }
	}

	private void hideCurrentlyDisplayedData() {
		mDiveSiteLatestMeteorologicalData.setVisibility(View.GONE);
		mDiveSiteLatestWaveData.setVisibility(View.GONE);
		mDiveSiteDataToolbar.setVisibility(View.GONE);
	}

	private void toggleOnlineDiveSiteVisibility(boolean visible) {
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
		for (int i = 0; i < diveSites.length; i++) {
			if (((DiveSite) diveSites[i]).getLocalId() == -1) {
				Marker marker = mVisibleDiveSiteMarkers.get(diveSites[i]);
				marker.setVisible(visible);
			}
		}
	}

	private void toggleSavedDiveSiteVisibility(boolean visible) {
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
		for (int i = 0; i < diveSites.length; i++) {
			if (((DiveSite) diveSites[i]).getLocalId() != -1) {
				Marker marker = mVisibleDiveSiteMarkers.get(diveSites[i]);
				marker.setVisible(visible);
			}
		}
	}

	@Override
	public void onDestroy() {
		mMapView.onDestroy();

		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Remove existing add marker if it exists
		if (mAddDiveSiteMarker != null) {
			mAddDiveSiteMarker.remove();
		}

		if (REQUEST_NEW_DIVESITE == requestCode) {

			// Restart the loader to get any new dive sites available
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_divesites_map, menu);

		mRefreshMenuItem = menu.findItem(R.id.menu_item_refresh_divesites);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_item_search));

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(new OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Clear current search marker results
                    for (int i = 0; i < mSearchResultsMarkers.size(); i++) {
                        mSearchResultsMarkers.get(i).remove();
                    }
                    mSearchResultsMarkers.clear();

                    // Search user's query in map
                    List<Address> addresses;
                    try {
                        addresses = mGeocoder.getFromLocationName(query, 5);

                        if (addresses.size() == 0) {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    R.string.divesite_search_no_results,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                            for (int i = 0; i < addresses.size(); i++) {
                                LatLng latLng = new LatLng(addresses.get(i)
                                        .getLatitude(), addresses.get(i)
                                        .getLongitude());

                                latLngBuilder.include(latLng);

                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                                Marker marker = mGoogleMap.addMarker(markerOptions);
                                mSearchResultsMarkers.add(marker);
                            }

                            /**
                             * Add 2 points 1000m northEast and southWest of the
                             * center. They increase the bounds only, if they are
                             * not already larger than this.
                             */
                            LatLngBounds tmpBounds = latLngBuilder.build();
                            LatLng center = tmpBounds.getCenter();
                            LatLng norhtEast = move(center, 5000, 5000);
                            LatLng southWest = move(center, -5000, -5000);
                            latLngBuilder.include(southWest);
                            latLngBuilder.include(norhtEast);

                            Display display = getActivity().getWindowManager()
                                    .getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);

                            LatLngBounds latLngBounds = latLngBuilder.build();

                            CameraUpdate movement = CameraUpdateFactory
                                    .newLatLngBounds(latLngBounds, size.x, size.y, 100);

                            mGoogleMap.moveCamera(movement);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return true;
                }

            });
        }
	}

	/**
	 * Create a new LatLng which lies toNorth meters north and toEast meters
	 * east of startLL
	 */
	private static LatLng move(LatLng startLL, double toNorth, double toEast) {
		double lonDiff = meterToLongitude(toEast, startLL.latitude);
		double latDiff = meterToLatitude(toNorth);
		return new LatLng(startLL.latitude + latDiff, startLL.longitude
				+ lonDiff);
	}

	private static double meterToLongitude(double meterToEast, double latitude) {
		double latArc = Math.toRadians(latitude);
		double radius = Math.cos(latArc) * EARTHRADIUS;
		double rad = meterToEast / radius;
		return Math.toDegrees(rad);
	}

	private static double meterToLatitude(double meterToNorth) {
		double rad = meterToNorth / EARTHRADIUS;
		return Math.toDegrees(rad);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (mRefreshingOnlineDiveSites || mRefreshingOnlineNDBCData) {
			mRefreshMenuItem
					.setActionView(R.layout.actionbar_indeterminate_progress);
		}

		MenuItem showNDCPBuoysMenuItem = menu
				.findItem(R.id.menu_item_showNDBCBuoys);
		MenuItem hideNDCPBuoysMenuItem = menu
				.findItem(R.id.menu_item_hideNDBCBuoys);

		if (mNDCPBuoysDisplayed) {
			showNDCPBuoysMenuItem.setVisible(false);
			hideNDCPBuoysMenuItem.setVisible(true);
		} else {
			showNDCPBuoysMenuItem.setVisible(true);
			hideNDCPBuoysMenuItem.setVisible(false);
		}

		MenuItem showOnlineSitesMenuItem = menu
				.findItem(R.id.menu_item_showOnlineDiveSites);
		MenuItem hideOnlineSitesMenuItem = menu
				.findItem(R.id.menu_item_hideOnlineDiveSites);

		if (mOnlineSitesDisplayed) {
			showOnlineSitesMenuItem.setVisible(false);
			hideOnlineSitesMenuItem.setVisible(true);
		} else {
			showOnlineSitesMenuItem.setVisible(true);
			hideOnlineSitesMenuItem.setVisible(false);
		}

		MenuItem showSavedSitesMenuItem = menu
				.findItem(R.id.menu_item_showSavedDiveSites);
		MenuItem hideSavedSitesMenuItem = menu
				.findItem(R.id.menu_item_hideSavedDiveSites);

		if (mSavedSitesDisplayed) {
			showSavedSitesMenuItem.setVisible(false);
			hideSavedSitesMenuItem.setVisible(true);
		} else {
			showSavedSitesMenuItem.setVisible(true);
			hideSavedSitesMenuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_refresh_divesites:
			refreshOnlineDiveSites();
			return true;

		case R.id.menu_item_showNDBCBuoys:
			mNDCPBuoysDisplayed = true;
			getActivity().invalidateOptionsMenu();
			refreshVisibleNDBCStations();
			return true;

		case R.id.menu_item_hideNDBCBuoys:
			mNDCPBuoysDisplayed = false;
			getActivity().invalidateOptionsMenu();
			refreshVisibleNDBCStations();
			return true;

		case R.id.menu_item_showOnlineDiveSites:
			mOnlineSitesDisplayed = true;
			getActivity().invalidateOptionsMenu();
			toggleOnlineDiveSiteVisibility(true);
			return true;

		case R.id.menu_item_hideOnlineDiveSites:
			mOnlineSitesDisplayed = false;
			getActivity().invalidateOptionsMenu();
			toggleOnlineDiveSiteVisibility(false);
			return true;

		case R.id.menu_item_showSavedDiveSites:
			mSavedSitesDisplayed = true;
			getActivity().invalidateOptionsMenu();
			toggleSavedDiveSiteVisibility(true);
			return true;

		case R.id.menu_item_hideSavedDiveSites:
			mSavedSitesDisplayed = false;
			getActivity().invalidateOptionsMenu();
			toggleSavedDiveSiteVisibility(false);
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	private void refreshOnlineDiveSites() {
		// Look for more dive sites and set menu item icon to spin
		if (mRefreshingOnlineDiveSites) {
            if (mDiveSiteOnlineDatabase != null && mDiveSiteOnlineDatabase.getActive()) {
                mDiveSiteOnlineDatabase.stopBackground();
                mDiveSiteOnlineDatabase.cancel(true);
            }
		}

        if (mRefreshMenuItem != null) {
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

						mRefreshingOnlineDiveSites = false;

						if (!mRefreshingOnlineNDBCData && mRefreshMenuItem != null) {
							mRefreshMenuItem.setActionView(null);
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSiteOnlineDatabase.getActive()) {
							DiveSite diveSite = (DiveSite) result;
							LatLng latLng = new LatLng(diveSite.getLatitude(),
									diveSite.getLongitude());

							// If this marker exists, no need to add it again
							Marker marker = getMarkerForDiveSiteOnlineID(diveSite
									.getOnlineId());
							if (marker == null) {
								MarkerOptions markerOptions = new MarkerOptions()
										.position(latLng).title(
												diveSite.getName());

								markerOptions.icon(BitmapDescriptorFactory
										.fromResource(R.drawable.divesite_active_marker));

								marker = mGoogleMap.addMarker(markerOptions);
								marker.setVisible(mOnlineSitesDisplayed);
								mVisibleDiveSiteMarkers.put(diveSite, marker);
							} else {
								marker.setPosition(latLng);
								marker.setTitle(diveSite.getName());
							}
						}
					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						//
					}
				});

		String coordinateRange[] = getCoordinateRange();
		if (getLocation() != null) {
			Location lastLocation = getLocation();
			mDiveSiteOnlineDatabase.getDiveSiteList(new Date(0), -1, 
					String.valueOf(lastLocation.getLatitude()),
					String.valueOf(lastLocation.getLongitude()), "", "", "",
					"", coordinateRange[0], coordinateRange[1], coordinateRange[2],
					coordinateRange[3], "", "", "");
		}
	}

	private void refreshVisibleNDBCStations() {
		if (mNDCPBuoysDisplayed) {
			if (mRefreshMenuItem != null) {
				mRefreshMenuItem
						.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				mRefreshMenuItem
						.setActionView(R.layout.actionbar_indeterminate_progress);
			}

			if (mRefreshingOnlineNDBCData) {
				mNDBCDataOnlineDatabase.stopBackground();
			}

			mRefreshingOnlineNDBCData = true;

			if (mNDBCDataOnlineDatabase != null
					&& mNDBCDataOnlineDatabase.getActive()) {
				mNDBCDataOnlineDatabase.stopBackground();
				mNDBCDataOnlineDatabase.cancel(true);
			}

			// Make sure all current buoy markers are visible
			Object[] ndbcStations = mVisibleNDBCStationMarkers.keySet()
					.toArray();
			for (int i = 0; i < ndbcStations.length; i++) {
				mVisibleNDBCStationMarkers.get(ndbcStations[i])
						.setVisible(true);
			}

			mNDBCDataOnlineDatabase = new DiveSiteOnlineDatabaseLink(
					getActivity());
			mNDBCDataOnlineDatabase
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

							mRefreshingOnlineNDBCData = false;

							if (!mRefreshingOnlineDiveSites && mRefreshMenuItem != null) {
								mRefreshMenuItem.setActionView(null);
							}
						}

						@Override
						public void onOnlineDiveDataProgress(Object result) {
							if (mNDBCDataOnlineDatabase.getActive()) {
								NDBCStation ndbcStation = (NDBCStation) result;

								// Add station to the google map
								LatLng latLng = new LatLng(ndbcStation
										.getLatitude(), ndbcStation
										.getLongitude());
								MarkerOptions markerOptions = new MarkerOptions()
										.position(latLng)
										.title(String.format(
												getResources().getString(
														R.string.stationTitle),
												ndbcStation.getStationName()))
										.icon(BitmapDescriptorFactory
												.fromResource(R.drawable.ndcp_buoy_marker));

								// If station marker already exists, replace it
								// but keep data
								Object[] ndbcStations = mVisibleNDBCStationMarkers
										.keySet().toArray();
								for (int j = 0; j < ndbcStations.length; j++) {
									if (((NDBCStation) ndbcStations[j])
											.getStationId() == ndbcStation
											.getStationId()) {
										NDBCStation existingNDBCStation = (NDBCStation) ndbcStations[j];

										// Remove existing marker, drifting buoy
										// position may have changed
										mVisibleNDBCStationMarkers.get(
												existingNDBCStation).remove();

										// Set user data update time
										ndbcStation
												.setLastUserUpdate(existingNDBCStation
														.getLastUserUpdate());

										// Copy data
										ndbcStation
												.copyMeteorologicalData(existingNDBCStation);
										ndbcStation
												.copyDriftingBuoyData(existingNDBCStation);
										ndbcStation
												.copySpectralWaveData(existingNDBCStation);
										ndbcStation
												.copyOceanicData(existingNDBCStation);

										// Delete existing station
										mVisibleNDBCStationMarkers
												.remove(existingNDBCStation);
									}

								}

								// Add marker for new station
								Marker marker = mGoogleMap
										.addMarker(markerOptions);
								mVisibleNDBCStationMarkers.put(ndbcStation,
										marker);
							}
						}

						@Override
						public void onOnlineDiveDataPostBackground(
								ArrayList<Object> resultList, String message) {
							// TODO Auto-generated method stub

						}
					});

			// Show buoys in range with data from last day at least only
			String coordinateRange[] = getCoordinateRange();
			
			Date minLastUpdateTimestamp = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(minLastUpdateTimestamp);
			c.add(Calendar.DATE, -1);
			minLastUpdateTimestamp = c.getTime();
			
			mNDBCDataOnlineDatabase.getNDBCStations(coordinateRange[0],
					coordinateRange[1], coordinateRange[2], coordinateRange[3],
					"1", String.valueOf(minLastUpdateTimestamp.getTime()),
					"", "", "", "", "");
		} else {
			// Hide all buoy markers from google map
			Object[] ndbcStations = mVisibleNDBCStationMarkers.keySet()
					.toArray();
			for (int i = 0; i < ndbcStations.length; i++) {
				mVisibleNDBCStationMarkers.get(ndbcStations[i]).setVisible(
						false);
			}
		}
	}

	private void refreshDiveSiteList() {
		// Only refresh list if online sites retrieved
		if (!mRefreshingOnlineDiveSites) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	private void openDiveSite(DiveSite diveSite) {
		Intent i = new Intent(getActivity(), DiveSiteActivity.class);
		i.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, diveSite);
		startActivityForResult(i, REQUEST_NEW_DIVESITE);
	}

	private String[] getCoordinateRange() {
		String coordinateRange[] = new String[4];
		if (mGoogleMap == null) {
			coordinateRange[0] = "0";
			coordinateRange[1] = "0";
			coordinateRange[2] = "0";
			coordinateRange[3] = "0";
		} else {
			LatLngBounds curMapBounds = mGoogleMap.getProjection()
					.getVisibleRegion().latLngBounds;

			String minLatitude = String
					.valueOf(curMapBounds.southwest.latitude);
			String maxLatitude = String
					.valueOf(curMapBounds.northeast.latitude);

			String minLongitude = String
					.valueOf(curMapBounds.southwest.longitude);
			String maxLongitude = String
					.valueOf(curMapBounds.northeast.longitude);

			coordinateRange[0] = minLatitude;
			coordinateRange[1] = maxLatitude;
			coordinateRange[2] = minLongitude;
			coordinateRange[3] = maxLongitude;
		}

		return coordinateRange;
	}
	
	private Location getLocation() {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
			return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		} else {
			return mDiveSiteManager.getLastLocation();
		}
	}

	private static class DiveSiteListCursorLoader extends SQLiteCursorLoader {

		private boolean mArchives;

		private String mMinLatitude, mMaxLatitude;
		private String mMinLongitude, mMaxLongitude;

		public DiveSiteListCursorLoader(Context context, boolean archives,
				String minLatitude, String maxLatitude, String minLongitude,
				String maxLongitude) {
			super(context);

			mArchives = archives;

			mMinLatitude = minLatitude;
			mMaxLatitude = maxLatitude;
			mMinLongitude = minLongitude;
			mMaxLongitude = maxLongitude;
		}

		@Override
		protected Cursor loadCursor() {

			// Query the list of dive sites
			if (mArchives) {
				return DiveSiteManager.get(getContext())
						.queryArchivedDiveSites(null, null, mMinLatitude,
								mMaxLatitude, mMinLongitude, mMaxLongitude, -1);
			} else {
				return DiveSiteManager.get(getContext()).queryVisibleDiveSites(
						null, null, mMinLatitude, mMaxLatitude, mMinLongitude,
						mMaxLongitude, -1);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String coordinateRange[] = getCoordinateRange();
		return new DiveSiteListCursorLoader(getActivity(), mArchives,
				coordinateRange[0], coordinateRange[1], coordinateRange[2],
				coordinateRange[3]);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Add markers for dive sites in cursor to map
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			DiveSite diveSite = ((DiveSiteCursor) cursor).getDiveSite();

			LatLng latLng = new LatLng(diveSite.getLatitude(),
					diveSite.getLongitude());

			// If this marker exists, no need to add it again
			Marker marker = getMarkerForDiveSiteLocalID(diveSite.getLocalId());
			if (marker == null) {
				MarkerOptions markerOptions = new MarkerOptions().position(
						latLng).title(diveSite.getName());

				if (diveSite.isArchived()) {
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.divesite_inactive_marker));
				} else {
					markerOptions
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.divesite_active_local_marker));
				}

				marker = mGoogleMap.addMarker(markerOptions);
				marker.setVisible(mSavedSitesDisplayed);
				mVisibleDiveSiteMarkers.put(diveSite, marker);
			} else {
				marker.setPosition(latLng);
				marker.setTitle(diveSite.getName());
			}

			cursor.moveToNext();
		}

		if (!mRefreshingOnlineDiveSites && !mRefreshingOnlineNDBCData && mRefreshMenuItem != null) {
			mRefreshMenuItem.setActionView(null);
		}
	}

	private Marker getMarkerForDiveSiteLocalID(long localID) {
		Marker marker = null;
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
		for (int i = 0; i < diveSites.length; i++) {
			if (((DiveSite) diveSites[i]).getLocalId() == localID) {
				marker = mVisibleDiveSiteMarkers.get(diveSites[i]);
				break;
			}
		}

		return marker;
	}

	private Marker getMarkerForDiveSiteOnlineID(long onlineID) {
		Marker marker = null;
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
		for (int i = 0; i < diveSites.length; i++) {
			if (((DiveSite) diveSites[i]).getOnlineId() == onlineID) {
				marker = mVisibleDiveSiteMarkers.get(diveSites[i]);
				break;
			}
		}

		return marker;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
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
    	
    	CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), 
								   location.getLongitude())).zoom(7).build();
		mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
