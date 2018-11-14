package com.fahadaltimimi.divethesite.view;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import android.animation.LayoutTransition;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager.ErrorDialogFragment;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ScheduledDiveListFragment extends ListFragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE MMMM dd yyyy, HH:mm");

	public static final String TAG = "ScheduledDiveListFragment";
	protected static final int REQUEST_NEW_SCHEDULEDDIVE = 0;
	private static final double EARTHRADIUS = 6366198;

	protected DiveSiteManager mDiveSiteManager;
	
	protected LocationRequest mLocationRequest;
	protected GoogleApiClient mGoogleApiClient;
    protected boolean mLocationEnabled;
	
	protected Bundle mSavedInstanceState;

	protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;

	protected MenuItem mRefreshMenuItem = null;
	
	protected LinearLayout mListFilter = null;

	protected View mLastDisplayedListItemView = null;
	
	protected LinearLayout mFilterNotificationContainer;
	protected TextView mFilterNotification;
	protected EditText mFilterTitle, mFilterState, mFilterCity = null;
	protected Spinner mFilterCountry = null;
	protected EditText mFilterPreviousDays, mFilterNextDays = null;
	protected CheckBox mFilterPublished, mFilterUnpublished = null;
	protected Button mFilterClear, mFilterClose;

    protected MapView mScheduledDiveItemMapView;
    protected ImageView mScheduledDiveItemMapViewSnapShot;

	protected ProgressDialog mProgressDialog;

	protected SharedPreferences mPrefs;

	protected long mRestrictToDiverID = -1;
	protected DiveSite mDiveSite = null;
	protected ScheduledDive mSelectedScheduledDive = null;

    protected HashMap<Long, View> mScheduledDiveListItemViews = new HashMap<Long, View>();
	protected HashMap<Long, View> mScheduledDiveDiveSiteListItemViews = new HashMap<Long, View>();
	protected HashMap<Long, View> mScheduledDiveUserListItemViews = new HashMap<Long, View>();
	protected HashMap<Long, Integer> mScheduledDiveListItemLoaderIDs = new HashMap<Long, Integer>();
	protected HashMap<Long, Diver> mScheduledDiveListItemDiver = new HashMap<Long, Diver>();
	protected HashMap<Long, ImageView> mScheduledDiveListItemDiverImageView = new HashMap<Long, ImageView>();
	protected LruCache<Long, Bitmap> mDiverProfileImageCache;
	
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

		mPrefs = 
			getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

		mProgressDialog = new ProgressDialog(getActivity());
		
		Bundle args = getArguments();

		if (args != null) {
			mRestrictToDiverID = args.getLong(DiverTabFragment.ARG_DIVER_ID, -1);
			mDiveSite = args.getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
		}

		// Get max available memory, exceeding it will cause Out of Memory error
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		// Using 1/8th max memory for this cache
		final int cacheSize = maxMemory / 8;

		mDiverProfileImageCache = new LruCache<Long, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(Long key, Bitmap bitmap) {
				// Cache size measured in kilobytes rather than number of items
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_scheduleddive_list, parent, false);

		mFilterNotificationContainer =
				v.findViewById(R.id.scheduleddive_list_filter_notification_container);
		mFilterNotification =
				v.findViewById(R.id.scheduleddive_list_filter_notification);

		// Initialize filter panel
		mListFilter = v.findViewById(R.id.scheduleddive_list_filter);

		mFilterTitle = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_title);
		mFilterCountry = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_country);
		mFilterState = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_state);
		mFilterCity = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_city);
		mFilterPreviousDays = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_daysPrevious);
		mFilterNextDays = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_daysNext);
		mFilterPublished = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_published);
		mFilterUnpublished = mListFilter
				.findViewById(R.id.scheduleddive_list_filter_unpublished);
		mFilterClear = mListFilter
				.findViewById(R.id.scheduleddive_list_clear_filter);
		mFilterClose = mListFilter
				.findViewById(R.id.scheduleddive_list_close_filter);

		setFilterViews();

		mFilterTitle.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterTitle = c.toString().trim();
				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "")
						.equals(filterTitle)) {
					mPrefs.edit().putString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE,
						filterTitle).apply();
					filterScheduledDiveList();
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
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, "")
						.equals(filterCountry)) {
					if (filterCountry.isEmpty()) {
						filterCountry = getResources().getString(
								R.string.filter_list_all);
					}
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY,
									filterCountry).apply();

					filterScheduledDiveList();
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
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, "").equals(
						filterState)) {
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE,
									filterState).apply();
					filterScheduledDiveList();
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
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, "").equals(
						filterCity)) {
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY,
									filterCity).apply();
					filterScheduledDiveList();
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

		mFilterPreviousDays.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterPreviousDays = c.toString().trim();

				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, "").equals(
						filterPreviousDays)) {
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS,
									filterPreviousDays).apply();
					filterScheduledDiveList();
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
		
		mFilterNextDays.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				// Save text and reset list view
				String filterNextDays = c.toString().trim();

				if (!mPrefs.getString(
						DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, "").equals(
						filterNextDays)) {
					mPrefs.edit()
							.putString(
									DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS,
									filterNextDays).apply();
					filterScheduledDiveList();
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
										DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED,
										true) == isChecked) {
							mPrefs.edit()
									.putBoolean(
											DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED,
											isChecked).apply();
							filterScheduledDiveList();
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
										DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED,
										true) == isChecked) {
							mPrefs.edit()
									.putBoolean(
											DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED,
											isChecked).apply();
							filterScheduledDiveList();
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
				mFilterPreviousDays.setText("");
				mFilterNextDays.setText("");
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

        mScheduledDiveItemMapView = v.findViewById(R.id.scheduleddive_list_item_mapView);
        mScheduledDiveItemMapView.onCreate(savedInstanceState);
        mScheduledDiveItemMapView.onResume();
        mScheduledDiveItemMapView.getLayoutParams().height = (getActivity()
                .getWindowManager().getDefaultDisplay().getHeight()
                - getTitleBarHeight() - getStatusBarHeight()) / 2;
        MapsInitializer.initialize(getActivity());
        mScheduledDiveItemMapView.setVisibility(View.GONE);

        mScheduledDiveItemMapViewSnapShot = v.findViewById(R.id.scheduleddive_list_item_mapView_snapShot);
        mScheduledDiveItemMapViewSnapShot.setVisibility(View.GONE);

		updateFilterNotification();

		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ScheduledDive scheduledDive = getScheduledDiveItemClick(position, id);
		triggerViewSelection(v, scheduledDive);
	}
	
	protected void triggerViewSelection(View view, ScheduledDive scheduledDive) {

        if (mLastDisplayedListItemView == view) {
            setSelectedScheduledDive(null);
            mLastDisplayedListItemView = null;
        } else {
            setSelectedScheduledDive(scheduledDive);
            mLastDisplayedListItemView = view;
        }

        refreshScheduledDiveList();
	}

    protected void setSnapshot(int visibility) {
        if (mSelectedScheduledDive != null) {
            switch (visibility) {
                case View.VISIBLE:
                    if (mScheduledDiveItemMapView.getVisibility() == View.VISIBLE) {
						mScheduledDiveItemMapView.getMapAsync(new OnMapReadyCallback() {
							@Override
							public void onMapReady(GoogleMap googleMap) {
								googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
									@Override
									public void onSnapshotReady(Bitmap bitmap) {
										mScheduledDiveItemMapViewSnapShot.setImageBitmap(bitmap);
										mScheduledDiveItemMapViewSnapShot.setVisibility(View.VISIBLE);
										mScheduledDiveItemMapView.setVisibility(View.INVISIBLE);
									}
								});
							}
						});
                    }
                    break;
                case View.INVISIBLE:
                    if (mScheduledDiveItemMapView.getVisibility() == View.INVISIBLE) {
                        mScheduledDiveItemMapView.setVisibility(View.VISIBLE);

                        mScheduledDiveItemMapViewSnapShot.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    }

    protected void setScheduledDiveMap(final ScheduledDive scheduledDive, ViewGroup mapContainer) {
        if (scheduledDive.getScheduledDiveDiveSites().size() == 0) {
            mapContainer.setVisibility(View.GONE);
        } else {
            mapContainer.setVisibility(View.VISIBLE);
            ViewGroup existingMapContainter = (ViewGroup) mScheduledDiveItemMapView.getParent();
            if (existingMapContainter != mapContainer) {
                if (existingMapContainter != null) {
                    final LayoutTransition transition = existingMapContainter.getLayoutTransition();
                    existingMapContainter.setLayoutTransition(null);
                    existingMapContainter.removeView(mScheduledDiveItemMapView);
                    existingMapContainter.setLayoutTransition(transition);
                }

                mapContainer.addView(mScheduledDiveItemMapView);
            }
            mScheduledDiveItemMapView.setVisibility(View.VISIBLE);

            ViewGroup existingMapSnapShotContainter = (ViewGroup) mScheduledDiveItemMapViewSnapShot.getParent();
            if (existingMapSnapShotContainter != mapContainer) {
                if (existingMapSnapShotContainter != null) {
                    final LayoutTransition transition = existingMapSnapShotContainter.getLayoutTransition();
                    existingMapSnapShotContainter.setLayoutTransition(null);
                    existingMapSnapShotContainter.removeView(mScheduledDiveItemMapViewSnapShot);
                    existingMapSnapShotContainter.setLayoutTransition(transition);
                }

                mapContainer.addView(mScheduledDiveItemMapViewSnapShot);
            }
            mScheduledDiveItemMapView.setVisibility(View.VISIBLE);
            mScheduledDiveItemMapViewSnapShot.setVisibility(View.INVISIBLE);

            mScheduledDiveItemMapView.onResume();
			mScheduledDiveItemMapView.getMapAsync(new OnMapReadyCallback() {
				@Override
				public void onMapReady(GoogleMap googleMap) {
					LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
					for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
						final DiveSite diveSite = scheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite();
						if (diveSite != null) {
							LatLng latLng = new LatLng(diveSite.getLatitude(), diveSite.getLongitude());
							latLngBuilder.include(latLng);

							// If this marker exists, no need to add it again
							MarkerOptions markerOptions = new MarkerOptions()
									.position(latLng).title(diveSite.getName());

							if (diveSite.getOnlineId() != -1) {
								markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.divesite_active_marker));
							} else {
								markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.divesite_active_local_marker));
							}

							googleMap.addMarker(markerOptions);

							/**
							 * Add 2 points 1000m northEast and southWest of the
							 * center. They increase the bounds only, if they are
							 * not already larger than this.
							 */

							if (scheduledDive.getScheduledDiveDiveSites().size() > 0) {
								LatLngBounds tmpBounds = latLngBuilder.build();
								LatLng center = tmpBounds.getCenter();
								LatLng norhtEast = move(center, 5000, 5000);
								LatLng southWest = move(center, -5000, -5000);
								latLngBuilder.include(southWest);
								latLngBuilder.include(norhtEast);

								Display display = getActivity().getWindowManager().getDefaultDisplay();
								Point size = new Point();
								display.getSize(size);

								LatLngBounds latLngBounds = latLngBuilder.build();
								CameraUpdate movement = CameraUpdateFactory.newLatLngBounds(latLngBounds, size.x, size.y, 100);

								googleMap.moveCamera(movement);
							}
						}
					}
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

	private void setSelectedScheduledDive(ScheduledDive scheduleddive) {
		if (mSelectedScheduledDive != scheduleddive) {
			mSelectedScheduledDive = scheduleddive;
			getActivity().invalidateOptionsMenu();
		}
	}
	
	protected void setFilterViews() {
		if (mFilterTitle != null) {
			mFilterTitle.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, ""));
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
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY,
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
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_STATE, ""));
		}

		if (mFilterCity != null) {
			mFilterCity.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_CITY, ""));
		}
		
		if (mFilterPreviousDays != null) {
			mFilterPreviousDays.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_PREVIOUSDAYS, ""));
		}

		if (mFilterNextDays != null) {
			mFilterNextDays.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_NEXTDAYS, ""));
		}

		if (mFilterPublished != null) {
			mFilterPublished.setChecked(mPrefs.getBoolean(
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_PUBLISHED, true));
		}

		if (mFilterUnpublished != null) {
			mFilterUnpublished
					.setChecked(mPrefs
							.getBoolean(
									DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_SHOW_UNPUBLISHED,
									true));
		}

	}

	protected void updateFilterNotification() {
		// Inherited
	}

	protected ScheduledDive getScheduledDiveItemClick(int position, long id) {
		// Inherited
		return null;
	}

	protected void refreshScheduledDiveList() {
		// Inherited
	}
	
	protected void filterScheduledDiveList() {
		refreshScheduledDiveList();
	}

	protected void openScheduledDive(ScheduledDive scheduledDive) {
		Intent i = new Intent(getActivity(), ScheduledDiveActivity.class);
		i.putExtra(ScheduledDiveActivity.EXTRA_SCHEDULED_DIVE, scheduledDive);
        i.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, mDiveSite);
		startActivityForResult(i, REQUEST_NEW_SCHEDULEDDIVE);
	}

    protected void openDiveSite(DiveSite diveSite) {
        // Open the dive site with edit mode set to false
        mPrefs.edit().putBoolean(DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE, false).apply();
        Intent intent = new Intent(getActivity(), DiveSiteActivity.class);
        intent.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, diveSite);
        startActivity(intent);
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
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
			return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		} else {
			return mDiveSiteManager.getLastLocation();
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
