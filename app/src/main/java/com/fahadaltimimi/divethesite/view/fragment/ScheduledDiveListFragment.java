package com.fahadaltimimi.divethesite.view.fragment;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.LruCache;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.fahadaltimimi.controller.LocationController;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.fahadaltimimi.divethesite.view.activity.DiveSiteActivity;
import com.fahadaltimimi.divethesite.view.activity.ScheduledDiveActivity;
import com.fahadaltimimi.view.FAMapView;
import com.fahadaltimimi.view.fragment.LocationListFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.fahadaltimimi.controller.LocationFragmentHelper.move;

public class ScheduledDiveListFragment extends LocationListFragment {

    protected static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEEE MMMM dd yyyy, HH:mm");

	public static final String TAG = "ScheduledDiveListFragment";
	protected static final int REQUEST_NEW_SCHEDULEDDIVE = 0;

	protected DiveSiteManager mDiveSiteManager;
	
	protected Bundle mSavedInstanceState;

	protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;
	
	protected LinearLayout mListFilter = null;

	protected View mLastDisplayedListItemView = null;
	
	protected LinearLayout mFilterNotificationContainer;
	protected TextView mFilterNotification;
	protected EditText mFilterTitle, mFilterState, mFilterCity = null;
	protected Spinner mFilterCountry = null;
	protected EditText mFilterPreviousDays, mFilterNextDays = null;
	protected CheckBox mFilterPublished, mFilterUnpublished = null;
	protected Button mFilterClear, mFilterClose;

    protected FAMapView mScheduledDiveItemMapView;
    protected ImageView mScheduledDiveItemMapViewSnapShot;

	protected ProgressDialog mProgressDialog;

	protected SharedPreferences mPrefs;

	protected long mRestrictToDiverID = -1;
	protected DiveSite mDiveSite = null;
	protected ScheduledDive mSelectedScheduledDive = null;

    protected HashMap<Long, View> mScheduledDiveListItemViews = new HashMap<>();
	protected HashMap<Long, View> mScheduledDiveDiveSiteListItemViews = new HashMap<>();
	protected HashMap<Long, View> mScheduledDiveUserListItemViews = new HashMap<>();
	protected HashMap<Long, Integer> mScheduledDiveListItemLoaderIDs = new HashMap<>();
	protected HashMap<Long, Diver> mScheduledDiveListItemDiver = new HashMap<>();
	protected HashMap<Long, ImageView> mScheduledDiveListItemDiverImageView = new HashMap<>();
	protected LruCache<Long, Bitmap> mDiverProfileImageCache;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mSavedInstanceState = savedInstanceState;
		mDiveSiteManager = DiveSiteManager.get(getActivity());

		mPrefs = 
			Objects.requireNonNull(getActivity()).getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

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
			protected int sizeOf(@NonNull Long key, @NonNull Bitmap bitmap) {
				// Cache size measured in kilobytes rather than number of items
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, parent, savedInstanceState);

		mFilterNotificationContainer =
				Objects.requireNonNull(view).findViewById(R.id.list_filter_notification_container);
		mFilterNotification =
                view.findViewById(R.id.list_filter_notification);

		// Initialize filter panel
		mListFilter = view.findViewById(R.id.scheduleddive_list_filter);

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
				if (!Objects.requireNonNull(mPrefs.getString(
                        DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, "")).equals(filterTitle)) {
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

				if (!Objects.requireNonNull(mPrefs.getString(
                        DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY, ""))
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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        mScheduledDiveItemMapView = view.findViewById(R.id.scheduleddive_list_item_mapView);
        mScheduledDiveItemMapView.onCreate(savedInstanceState);
        mScheduledDiveItemMapView.onResume();
        mScheduledDiveItemMapView.getLayoutParams().height = (displayMetrics.heightPixels - getTitleBarHeight() - getStatusBarHeight()) / 2;
        MapsInitializer.initialize(getActivity());
        mScheduledDiveItemMapView.setVisibility(View.GONE);

        mScheduledDiveItemMapViewSnapShot = view.findViewById(R.id.scheduleddive_list_item_mapView_snapShot);
        mScheduledDiveItemMapViewSnapShot.setVisibility(View.GONE);

		updateFilterNotification();

		FloatingActionButton fab = view.findViewById(R.id.fab_action_add);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addNewScheduledDive();
			}
		});

		if (mRestrictToDiverID != -1 && mRestrictToDiverID != mDiveSiteManager.getLoggedInDiverId()) {
			fab.hide();
		}

		return view;
	}

	protected void addNewScheduledDive() {
        // If user not registered, don't allow
        if (mDiveSiteManager.getLoggedInDiverId() == -1) {
            Toast.makeText(getActivity(), R.string.not_registered_create_log, Toast.LENGTH_LONG).show();
        }

        openScheduledDive(null);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_scheduleddive_list;
    }

	@Override
	protected int getSwipeRefreshLayout() {
		return R.id.list_swipe_refresh;
	}

    @Override
    protected void onLocationPermissionGranted() {
        //
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

            initializeMap(scheduledDive);
        }
    }

    private void initializeMap(final ScheduledDive scheduledDive) {
        if (checkLocationPermission()) {
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

                                Display display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
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

	private void setSelectedScheduledDive(ScheduledDive scheduleddive) {
		if (mSelectedScheduledDive != scheduleddive) {
			mSelectedScheduledDive = scheduleddive;
			Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
		}
	}
	
	protected void setFilterViews() {
		if (mFilterTitle != null) {
			mFilterTitle.setText(mPrefs.getString(
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_TITLE, ""));
		}

		if (mFilterCountry != null) {
			// Initialize values and modify first blank entry to read 'All'
			ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
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
					DiveSiteManager.PREF_FILTER_SCHEDULEDDIVE_COUNTRY,
					getResources().getString(R.string.filter_list_all));
			for (int i = 0; i < spinnerAdapter.getCount(); i++) {
				if (spinnerAdapter.getItem(i).equals(currentFilterCountry)) {
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
        super.refreshListView();
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
		Window w = Objects.requireNonNull(getActivity()).getWindow();
		w.getDecorView().getWindowVisibleDisplayFrame(r);
		return r.top;
	}

	public int getTitleBarHeight() {
		int viewTop = Objects.requireNonNull(getActivity()).getWindow()
				.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		return (viewTop - getStatusBarHeight());
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
