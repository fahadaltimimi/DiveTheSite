package com.fahadaltimimi.divethesite.view;

import com.fahadaltimimi.controller.LocationController;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.view.FAMapView;
import com.fahadaltimimi.view.LocationListFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.Objects;

public class DiveSiteListFragment extends LocationListFragment {

	public static final String TAG = "DiveSiteListFragment";

	protected static final int REQUEST_NEW_DIVESITE = 0;

	protected DiveSiteManager mDiveSiteManager;

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

    protected FAMapView mDiveSiteItemMapView;
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

		mProgressDialog = new ProgressDialog(getActivity());

		mPrefs = Objects.requireNonNull(getActivity()).getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

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
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		mFilterNotificationContainer = Objects.requireNonNull(v)
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
				if (!Objects.requireNonNull(mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, ""))
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

				if (!Objects.requireNonNull(mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_COUNTRY, ""))
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

				if (!Objects.requireNonNull(mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_STATE, "")).equals(
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

				if (!Objects.requireNonNull(mPrefs.getString(
						DiveSiteManager.PREF_FILTER_DIVESITE_CITY, "")).equals(
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
        initializeMap();

        mDiveSiteItemMapViewSnapShot = v.findViewById(R.id.divesite_list_item_mapView_snapShot);
        mDiveSiteItemMapViewSnapShot.setVisibility(View.GONE);

		updateFilterNotification();

		return v;
	}

	@Override
	protected int getLayoutView() {
		return R.layout.fragment_divesite_list;
	}

	protected Location getLocation() {
		return LocationController.getLocationControler().getLocation(getActivity(), mDiveSiteManager.getLastLocation());
	}

	@Override
	protected void onLocationPermissionGranted() {
		initializeMap();
	}

    private void initializeMap() {
        if (checkLocationPermission()) {
            mDiveSiteItemMapView.onResume();
            mDiveSiteItemMapView.getLayoutParams().height = (Objects.requireNonNull(getActivity())
                    .getWindowManager().getDefaultDisplay().getHeight()
                    - getTitleBarHeight() - getStatusBarHeight()) / 2;
            MapsInitializer.initialize(getActivity());
            mDiveSiteItemMapView.setVisibility(View.GONE);
        }
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
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
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
					Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item,
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
				if (Objects.requireNonNull(spinnerAdapter.getItem(i)).toString()
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
		Window w = Objects.requireNonNull(getActivity()).getWindow();
		w.getDecorView().getWindowVisibleDisplayFrame(r);
		return r.top;
	}

	public int getTitleBarHeight() {
		int viewTop = Objects.requireNonNull(getActivity()).getWindow()
				.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		return (viewTop - getStatusBarHeight());
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
    
    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
        LocationController.getLocationControler().stopLocationUpdates(getActivity());
    	mDiveSiteManager.saveLastLocation(location);
    }
}
