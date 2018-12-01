package com.fahadaltimimi.divethesite.view.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayout;
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

import com.fahadaltimimi.controller.LocationController;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.DiveSiteCursor;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.NDBCStation;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCDriftingBuoyData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCMeteorologicalData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCSpectralWaveData;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.data.SQLiteCursorLoader;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.view.activity.DiveSiteActivity;
import com.fahadaltimimi.view.FAMapView;
import com.fahadaltimimi.view.fragment.LocationFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.fahadaltimimi.controller.LocationFragmentHelper.move;

public class DiveSiteFullMapFragment extends LocationFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = "DiveSiteFullMapFragment";
	
	private static final int REQUEST_NEW_DIVESITE = 0;
	private static final int MINIMUM_ZOOM_LEVEL_FOR_DATA = 6;
	private static final float GRID_COLUMN_WEIGHT = 1f;

	protected DiveSiteManager mDiveSiteManager;

	private SharedPreferences mPrefs;
    private boolean mArchives = false;

    private SwipeRefreshLayout mRefreshLayout;
	private FAMapView mMapView;
	private GoogleMap mGoogleMap;

    private LinearLayout mDiveSiteLatestMeteorologicalData;
	private LinearLayout mDiveSiteLatestWaveData;

	private LinearLayout mDiveSiteDataToolbar;
	private Button mMeteorologicalDataButton;
	private Button mWaveDataButton;

	private TextView mNDBCMeteorologicalDataTitle;
	private GridLayout mNDBCMeteorologicalDataGrid;

	private TextView mNDBCWaveDataTitle;
	private GridLayout mNDBCWaveDataGrid;

	private HashMap<DiveSite, Marker> mVisibleDiveSiteMarkers;
	private Marker mAddDiveSiteMarker;
	private ArrayList<Marker> mSearchResultsMarkers;
	private HashMap<NDBCStation, Marker> mVisibleNDBCStationMarkers;

    private Boolean mRefreshingOnlineDiveSites = false;
	private Boolean mRefreshingOnlineNDBCData = false;

	private Boolean mNDCPBuoysDisplayed = true;
	private Boolean mOnlineSitesDisplayed = true;
	private Boolean mSavedSitesDisplayed = true;

	private Geocoder mGeocoder;

	private DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase = null;
	private DiveSiteOnlineDatabaseLink mNDBCDataOnlineDatabase = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		mDiveSiteManager = DiveSiteManager.get(getActivity());
		
		mPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);

        mVisibleDiveSiteMarkers = new HashMap<>();
		mSearchResultsMarkers = new ArrayList<>();
		mVisibleNDBCStationMarkers = new HashMap<>();
		mGeocoder = new Geocoder(getActivity(), Locale.getDefault());
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		Objects.requireNonNull(getActivity()).setTitle(R.string.mapTitle);

		mRefreshLayout = v.findViewById(R.id.map_refresh);
        mRefreshLayout.setEnabled(false);

		mMapView = Objects.requireNonNull(v).findViewById(R.id.diveSite_fullMap_MapView);
		mMapView.onCreate(savedInstanceState);

        LinearLayout diveSiteDataViewContainer = v
                .findViewById(R.id.diveSite_fullMap_data_view_container);

		// Create and add views for each type of data
		mDiveSiteLatestMeteorologicalData = (LinearLayout) inflater.inflate(
				R.layout.ndbc_station_data_item, diveSiteDataViewContainer,
				false);
		mDiveSiteLatestMeteorologicalData.setVisibility(View.GONE);
		diveSiteDataViewContainer.addView(mDiveSiteLatestMeteorologicalData);

		mNDBCMeteorologicalDataTitle = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_station_data_item_title);
        mNDBCMeteorologicalDataGrid = mDiveSiteLatestMeteorologicalData
                .findViewById(R.id.ndbc_station_data_table);

		mDiveSiteLatestWaveData = (LinearLayout) inflater.inflate(
				R.layout.ndbc_station_data_item, diveSiteDataViewContainer,
				false);
		mDiveSiteLatestWaveData.setVisibility(View.GONE);
		diveSiteDataViewContainer.addView(mDiveSiteLatestWaveData);

		mNDBCWaveDataTitle = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_station_data_item_title);
		mNDBCWaveDataGrid = mDiveSiteLatestWaveData
                .findViewById(R.id.ndbc_station_data_table);

		mDiveSiteDataToolbar = v
				.findViewById(R.id.diveSite_fullMap_data_toolbar);

		mMeteorologicalDataButton = v
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

		mWaveDataButton = v.findViewById(R.id.diveSite_fullMap_wave);
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

		initializeMap();

		return v;
	}

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_divesite_fullmap;
    }

    private Location getLocation() {
        return LocationController.getLocationControler().getLocation(getActivity(), mDiveSiteManager.getLastLocation());
    }

	@Override
	protected void onLocationPermissionGranted() {
		initializeMap();
	}

	@Override
	public void onStop() {
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
        mRefreshingOnlineNDBCData = true;
        mRefreshLayout.setRefreshing(true);

	    // Show loading incase user clicks between stations, don't show past station's data
        clearStationData();

		// Load new data if user update time is less than station's online
		// update time
		if (station != null) {
			if (station.getLastUserUpdate().before(
					station.getLastOnlineUpdate())) {

                mRefreshLayout.setRefreshing(true);
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
                                    for (Object ndbcStation : Objects.requireNonNull(ndbcStations)) {
                                        if (((NDBCStation) ndbcStation)
                                                .getStationId() == updatedNDBCStation
                                                .getStationId()) {
                                            NDBCStation existingNDBCStation = (NDBCStation) ndbcStation;
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
									Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),
											message, Toast.LENGTH_LONG).show();
								}

                                if (!mRefreshingOnlineDiveSites && !mRefreshingOnlineNDBCData) {
                                    mRefreshLayout.setRefreshing(false);
                                }

                                mRefreshingOnlineNDBCData = false;
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
        clearStationData();

		// Show toolbar
		mDiveSiteDataToolbar.setVisibility(View.VISIBLE);

		// Show valid buttons and latest data for station
		if (station.getMeteorologicalDataCount() > 0) {
			NDBCMeteorologicalData data = station.getLatestMeteorologicalData();

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources()
							.getString(R.string.meteorologicalDataTitle));
			mNDBCMeteorologicalDataTitle.setText(title);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_air_temp,
                    data.getAirTemperature(),
                    R.string.temperatureDegCValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_water_temp,
                    data.getWaterTemperature(),
                    R.string.temperatureDegCValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_wind_direction,
                    data.getWindDirection(),
                    R.string.directionDegTValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_wind_speed,
                    data.getWindSpeed(),
                    R.string.speedMSValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_wind_gust,
                    data.getWindGust(),
                    R.string.speedMSValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_tide,
                    data.getTide(),
                    R.string.distanceFTValue);

			mMeteorologicalDataButton.setVisibility(View.VISIBLE);
		} else if (station.getDriftingBuoyDataCount() > 0) {
			NDBCDriftingBuoyData data = station.getLatestDriftingBuoyData();

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources()
							.getString(R.string.meteorologicalDataTitle));
			mNDBCMeteorologicalDataTitle.setText(title);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_air_temp,
                    data.getAirTemperature(),
                    R.string.temperatureDegCValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_water_temp,
                    data.getWaterTemperature(),
                    R.string.temperatureDegCValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_wind_direction,
                    data.getWindDirection(),
                    R.string.directionDegTValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_wind_speed,
                    data.getWindSpeed(),
                    R.string.speedMSValue);

            addStationDataEntry(mNDBCMeteorologicalDataGrid,
                    R.string.ndbc_station_data_wind_gust,
                    data.getWindGust(),
                    R.string.speedMSValue);
		}

		if (station.getSpectralWaveDataCount() > 0) {
			NDBCSpectralWaveData data = station.getLatestSpectralWaveData();

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources().getString(R.string.waveDataTitle));
			mNDBCWaveDataTitle.setText(title);

            String swellValues = " ";
            if (data.getSwellHeight() != null) {
                swellValues = swellValues.concat(String.format(getResources().getString(R.string.distanceMValue),
                        data.getSwellHeight())).concat(" ");
            }
            if (data.getSwellPeriod() != null) {
                swellValues = swellValues.concat(String.format(getResources().getString(R.string.timeSValue),
                        data.getSwellPeriod())).concat(" ");
            }
            if (data.getSwellDirection() != null) {
                swellValues = swellValues.concat(data.getSwellDirection()).concat(" ");
            }
            if (!swellValues.trim().isEmpty()) {
                addStationDataEntry(mNDBCWaveDataGrid, R.string.ndbc_station_data_swell, swellValues.trim());
            }

            String waveValues = " ";
            if (data.getWaveHeight() != null) {
                waveValues = waveValues.concat(String.format(getResources().getString(R.string.distanceMValue),
                        data.getWaveHeight())).concat(" ");
            }
            if (data.getAverageWavePeriod() != null) {
                waveValues = waveValues.concat(String.format(getResources().getString(R.string.timeSValue),
                        data.getAverageWavePeriod())).concat(" ");
            }
            if (data.getWaveSteepness() != null) {
                waveValues = waveValues.concat(data.getWaveSteepness()).concat(" ");
            }
            if (!waveValues.trim().isEmpty()) {
                addStationDataEntry(mNDBCWaveDataGrid, R.string.ndbc_station_data_wave, waveValues.trim());
            }

            String windWaveValues = " ";
            if (data.getWindWaveHeight() != null) {
                windWaveValues = windWaveValues.concat(String.format(getResources().getString(R.string.distanceMValue),
                        data.getWindWaveHeight())).concat(" ");
            }
            if (data.getWindWavePeriod() != null) {
                windWaveValues = windWaveValues.concat(String.format(getResources().getString(R.string.timeSValue),
                        data.getWindWavePeriod())).concat(" ");
            }
            if (data.getWindWaveDirection() != null) {
                windWaveValues = windWaveValues.concat(data.getWindWaveDirection()).concat(" ");
            }
            if (!windWaveValues.trim().isEmpty()) {
                addStationDataEntry(mNDBCWaveDataGrid, R.string.ndbc_station_data_wind_wave, windWaveValues.trim());
            }

			mWaveDataButton.setVisibility(View.VISIBLE);
		} else if (station.getMeteorologicalDataCount() > 0) {
			// Spectral Wave data not available, display wave data from
			// meteotrological data
			NDBCMeteorologicalData data = station.getLatestMeteorologicalData();

			String title = String.format(
					getResources().getString(R.string.stationTitle),
					station.getStationName());
			title = title.concat(" - "
					+ getResources().getString(R.string.waveDataTitle));
			mNDBCWaveDataTitle.setText(title);

            addStationDataEntry(mNDBCWaveDataGrid,
                    R.string.ndbc_station_data_wave_height,
                    data.getSignificantWaveHeight(),
                    R.string.distanceMValue);

            addStationDataEntry(mNDBCWaveDataGrid,
                    R.string.ndbc_station_data_dominant_wave_period,
                    data.getDominantWavePeriod(),
                    R.string.timeSValue);

            addStationDataEntry(mNDBCWaveDataGrid,
                    R.string.ndbc_station_data_dominant_wave_direction,
                    data.getDominantWaveDirection(),
                    R.string.distanceMValue);

            addStationDataEntry(mNDBCWaveDataGrid,
                    R.string.ndbc_station_data_average_wave_period,
                    data.getAverageWavePeriod(),
                    R.string.timeSValue);

			mWaveDataButton.setVisibility(View.VISIBLE);
		}

		if (mNDBCMeteorologicalDataGrid.getChildCount() == 1) {
		    mNDBCMeteorologicalDataGrid.setColumnCount(1);
        }

        if (mNDBCWaveDataGrid.getChildCount() == 1) {
            mNDBCWaveDataGrid.setColumnCount(1);
        }

        if (!mRefreshingOnlineDiveSites && !mRefreshingOnlineNDBCData) {
            mRefreshLayout.setRefreshing(false);
        }
	}

	private void addStationDataEntry(ViewGroup parentView, int titleID, Double value, int valueFormatID) {
        if (value != null)
        {
            LinearLayout ndbcStationDataItemEntry = (LinearLayout) getLayoutInflater().
                    inflate(R.layout.ndbc_station_data_item_entry, parentView, false);

            ((GridLayout.LayoutParams) ndbcStationDataItemEntry.getLayoutParams()).columnSpec =
                    GridLayout.spec(GridLayout.UNDEFINED, GRID_COLUMN_WEIGHT);

            TextView itemTitle = ndbcStationDataItemEntry.findViewById(R.id.ndbc_station_data_item_entry_header);
            TextView itemValue = ndbcStationDataItemEntry.findViewById(R.id.ndbc_station_data_item_entry_value);

            itemTitle.setText(getResources().getString(titleID));
            itemValue.setText(String.format(getResources().getString(valueFormatID), value));

            parentView.addView(ndbcStationDataItemEntry);
        }
    }

    private void addStationDataEntry(ViewGroup parentView, int titleID, String value) {
        if (value != null)
        {
            LinearLayout ndbcStationDataItemEntry = (LinearLayout) getLayoutInflater().
                    inflate(R.layout.ndbc_station_data_item_entry, parentView, false);

            TextView itemTitle = ndbcStationDataItemEntry.findViewById(R.id.ndbc_station_data_item_entry_header);
            TextView itemValue = ndbcStationDataItemEntry.findViewById(R.id.ndbc_station_data_item_entry_value);

            itemTitle.setText(getResources().getString(titleID));
            itemValue.setText(value);

            parentView.addView(ndbcStationDataItemEntry);
        }
    }

    private void clearStationData() {
        // Hide buttons
        mMeteorologicalDataButton.setVisibility(View.GONE);
        mWaveDataButton.setVisibility(View.GONE);
        mDiveSiteDataToolbar.setVisibility(View.GONE);

        // Clear grids
        mNDBCMeteorologicalDataGrid.removeAllViewsInLayout();
        mNDBCWaveDataGrid.removeAllViewsInLayout();

        // Set Loading titles
        String title = getResources().getString(R.string.meteorologicalDataTitle).concat(" - "
                + getResources().getString(R.string.loading));
        mNDBCMeteorologicalDataTitle.setText(title);

        title = getResources().getString(R.string.waveDataTitle).concat(" - "
                + getResources().getString(R.string.loading));
        mNDBCWaveDataTitle.setText(title);
    }

	private void hideCurrentlyDisplayedData() {
		mDiveSiteLatestMeteorologicalData.setVisibility(View.GONE);
		mDiveSiteLatestWaveData.setVisibility(View.GONE);
		mDiveSiteDataToolbar.setVisibility(View.GONE);
	}

	private void toggleOnlineDiveSiteVisibility(boolean visible) {
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
        for (Object diveSite : Objects.requireNonNull(diveSites)) {
            if (((DiveSite) diveSite).getLocalId() == -1) {
                Marker marker = mVisibleDiveSiteMarkers.get(diveSite);
                Objects.requireNonNull(marker).setVisible(visible);
            }
        }
	}

	private void toggleSavedDiveSiteVisibility(boolean visible) {
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
        for (Object diveSite : Objects.requireNonNull(diveSites)) {
            if (((DiveSite) diveSite).getLocalId() != -1) {
                Marker marker = mVisibleDiveSiteMarkers.get(diveSite);
                Objects.requireNonNull(marker).setVisible(visible);
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

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) Objects.requireNonNull(getActivity()).getSystemService(Context.SEARCH_SERVICE);
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

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
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

		case R.id.menu_item_showNDBCBuoys:
			mNDCPBuoysDisplayed = true;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
			refreshVisibleNDBCStations();
			return true;

		case R.id.menu_item_hideNDBCBuoys:
			mNDCPBuoysDisplayed = false;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
			refreshVisibleNDBCStations();
			return true;

		case R.id.menu_item_showOnlineDiveSites:
			mOnlineSitesDisplayed = true;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
			toggleOnlineDiveSiteVisibility(true);
			return true;

		case R.id.menu_item_hideOnlineDiveSites:
			mOnlineSitesDisplayed = false;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
			toggleOnlineDiveSiteVisibility(false);
			return true;

		case R.id.menu_item_showSavedDiveSites:
			mSavedSitesDisplayed = true;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
			toggleSavedDiveSiteVisibility(true);
			return true;

		case R.id.menu_item_hideSavedDiveSites:
			mSavedSitesDisplayed = false;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
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

        mRefreshLayout.setRefreshing(true);

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
                        mRefreshLayout.setRefreshing(false);
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

		String coordinateRange[] = getCoordinateRange(mGoogleMap);
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

            mRefreshLayout.setRefreshing(true);

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
            for (Object ndbcStation : Objects.requireNonNull(ndbcStations)) {
                Objects.requireNonNull(mVisibleNDBCStationMarkers.get(ndbcStation))
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
                            mRefreshLayout.setRefreshing(false);
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
                                for (Object ndbcStation1 : Objects.requireNonNull(ndbcStations)) {
                                    if (((NDBCStation) ndbcStation1)
                                            .getStationId() == ndbcStation
                                            .getStationId()) {
                                        NDBCStation existingNDBCStation = (NDBCStation) ndbcStation1;

                                        // Remove existing marker, drifting buoy
                                        // position may have changed
                                        Objects.requireNonNull(mVisibleNDBCStationMarkers.get(
                                                existingNDBCStation)).remove();

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
            Date minLastUpdateTimestamp = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(minLastUpdateTimestamp);
			c.add(Calendar.DATE, -1);
			minLastUpdateTimestamp = c.getTime();

			mNDBCDataOnlineDatabase.getNDBCStations("",
					"", "", "",
					"1", String.valueOf(minLastUpdateTimestamp.getTime()),
					"", "", "", "", "");
		} else {
			// Hide all buoy markers from google map
			Object[] ndbcStations = mVisibleNDBCStationMarkers.keySet()
					.toArray();
            for (Object ndbcStation : Objects.requireNonNull(ndbcStations)) {
                Objects.requireNonNull(mVisibleNDBCStationMarkers.get(ndbcStation)).setVisible(
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

    private void initializeMap() {
        if (checkLocationPermission()) {
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mGoogleMap = googleMap;

                    if (checkLocationPermission()) {
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
                                for (Object ndbcStation : Objects.requireNonNull(ndbcStations)) {
                                    if (Objects.equals(mVisibleNDBCStationMarkers.get(ndbcStation), marker)) {
                                        selectedStation = (NDBCStation) ndbcStation;
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
                                                        true).apply();

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
                                                        false).apply();

                                        // Display Dive Site info
                                        Object[] diveSites = mVisibleDiveSiteMarkers
                                                .keySet().toArray();
                                        for (Object diveSite : Objects.requireNonNull(diveSites)) {
                                            if (Objects.requireNonNull(mVisibleDiveSiteMarkers.get(diveSite))
                                                    .equals(marker)) {
                                                // Dive Site found
                                                openDiveSite((DiveSite) diveSite);
                                                break;
                                            }
                                        }
                                    }
                                }
                            });

                    mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            // Only refresh dive sites and station data if we're zoomed in
                            // enough, otherwise display message
                            if (mGoogleMap.getCameraPosition().zoom >= MINIMUM_ZOOM_LEVEL_FOR_DATA) {
                                refreshDiveSiteList();
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

                    refreshVisibleNDBCStations();
                }
            });

            MapsInitializer.initialize(Objects.requireNonNull(getActivity()));
        }
    }

	private static class DiveSiteListCursorLoader extends SQLiteCursorLoader {

		private boolean mArchives;

		private String mMinLatitude, mMaxLatitude;
		private String mMinLongitude, mMaxLongitude;

		DiveSiteListCursorLoader(Context context, boolean archives,
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

	@NonNull
    @Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String coordinateRange[] = getCoordinateRange(mGoogleMap);
		return new DiveSiteListCursorLoader(getActivity(), mArchives,
				coordinateRange[0], coordinateRange[1], coordinateRange[2],
				coordinateRange[3]);
	}

	@Override
	public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
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

		if (!mRefreshingOnlineDiveSites && !mRefreshingOnlineNDBCData) {
            mRefreshLayout.setRefreshing(false);
		}
	}

	private Marker getMarkerForDiveSiteLocalID(long localID) {
		Marker marker = null;
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
        for (Object diveSite : Objects.requireNonNull(diveSites)) {
            if (((DiveSite) diveSite).getLocalId() == localID) {
                marker = mVisibleDiveSiteMarkers.get(diveSite);
                break;
            }
        }

		return marker;
	}

	private Marker getMarkerForDiveSiteOnlineID(long onlineID) {
		Marker marker = null;
		Object[] diveSites = mVisibleDiveSiteMarkers.keySet().toArray();
        for (Object diveSite : Objects.requireNonNull(diveSites)) {
            if (((DiveSite) diveSite).getOnlineId() == onlineID) {
                marker = mVisibleDiveSiteMarkers.get(diveSite);
                break;
            }
        }

		return marker;
	}

	@Override
	public void onLoaderReset(@NonNull Loader<Cursor> loader) {
	}

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        LocationController.getLocationControler().stopLocationUpdates(getActivity());
    	mDiveSiteManager.saveLastLocation(location);
    	
    	CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), 
								   location.getLongitude())).zoom(7).build();
		mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
