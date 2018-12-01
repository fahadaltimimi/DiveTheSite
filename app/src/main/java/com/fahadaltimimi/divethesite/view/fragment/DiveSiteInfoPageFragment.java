package com.fahadaltimimi.divethesite.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fahadaltimimi.controller.LocationController;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.view.activity.DiveActivity;
import com.fahadaltimimi.divethesite.view.activity.DiverActivity;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.view.FAMapView;
import com.fahadaltimimi.view.ObservableScrollView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiveSiteInfoPageFragment extends DiveSitePageFragment {

    private static final String ARG_DIVESITE = "DIVESITE";

	private static final int MAPVIEW_HEIGHT_BUFFER = 500;

    private boolean mDisableSave = false;

	private View mDiveSiteInfoProgressBar, mDiveSiteView;

	private RelativeLayout mDiveSiteLocationViewContainer,
			mDiveSiteLocationEditContainer;
	private RelativeLayout mDiveSiteCoordinatesEditContainer;

	private FAMapView mMapView;
	private GoogleMap mGoogleMap = null;
    private ImageView mMapViewSnapShot;

	private RatingBar mDiveSiteRating;
	private TextView mDiveSiteDifficultyView;
	private TextView mDiveSiteCityView, mDiveSiteProvinceView,
			mDiveSiteCountryView;
	private TextView mDiveSiteCityProvinceComma;
	private TextView mDiveSiteDescriptionView;
	private Button mDiveSiteDifficultyEdit;
	private EditText mDiveSiteNameEdit;
	private EditText mDiveSiteCityEdit, mDiveSiteProvinceEdit;
	private Spinner mDiveSiteCountryEdit;
	private EditText mDiveSiteDescriptionEdit;
	private EditText mDiveSiteLatitudeEdit, mDiveSiteLongitudeEdit;
	private Button mSetCurrentLatitudeLongitude;

	private ImageButton mDiveSiteIndicatorSalt;
	private ImageButton mDiveSiteIndicatorFresh;
	private ImageButton mDiveSiteIndicatorShore;
	private ImageButton mDiveSiteIndicatorBoat;
	private ImageButton mDiveSiteIndicatorWreck;

	private TextView mDiveSiteDirectionsTitle;
	private TextView mDiveSiteHistoryTitle;
	private TextView mDiveSiteNotesTitle;

	private TextView mDiveSiteDirectionsView;
	private EditText mDiveSiteDirectionsEdit;
	private TextView mDiveSiteHistoryView;
	private TextView mDiveSiteNotesView;
	private TextView mDiveSiteSourceView;
	private TextView mDiveSiteSubmitterView;
    private ImageView mDiveSiteSubmitterImage;
	private EditText mDiveSiteHistoryEdit;
	private EditText mDiveSiteNotesEdit;
	private EditText mDiveSiteSourceEdit;

	public static DiveSiteInfoPageFragment newInstance(DiveSite diveSite,
			DiveLog diveLog) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_DIVESITE, diveSite);
		args.putParcelable(DiveLogFragment.ARG_DIVELOG, diveLog);
		DiveSiteInfoPageFragment rf = new DiveSiteInfoPageFragment();
		rf.setArguments(args);
		return rf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, parent, savedInstanceState);

        ((ObservableScrollView) Objects.requireNonNull(view)).setOnScrollStoppedListener(new ObservableScrollView.OnScrollStoppedListener() {

            public void onScrollStopped() {
                setSnapshot(View.INVISIBLE);
            }

            public void onScrollChanged(ObservableScrollView view, int l, int t, int oldl, int oldt) {
                if (mMapViewSnapShot.getVisibility() != View.VISIBLE) {
                    setSnapshot(View.VISIBLE);
                    view.startScrollerTask();
                }
            }
        });

		mDiveSiteInfoProgressBar = view
				.findViewById(R.id.divesite_info_progress_bar);
		mDiveSiteView = view.findViewById(R.id.divesite_view_page1);

		mDiveSiteLocationViewContainer = view
				.findViewById(R.id.divesite_location_viewing_container);
		mDiveSiteLocationEditContainer = view
				.findViewById(R.id.divesite_location_editing_container);
		mDiveSiteCoordinatesEditContainer = view
				.findViewById(R.id.divesite_coordinates_edit_container);

		mDiveSiteRating = view.findViewById(R.id.divesite_rating);
		mDiveSiteRating
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
						mDiveSite.setRatingCount(1);
						mDiveSite.setTotalRating(rating);
						saveDiveSite();
					}

				});

		mDiveSiteDifficultyView = view
				.findViewById(R.id.divesite_difficulty_viewing);

		mDiveSiteCityView = view
				.findViewById(R.id.divesite_city_viewing);
		mDiveSiteProvinceView = view
				.findViewById(R.id.divesite_province_viewing);
		mDiveSiteCityProvinceComma = view
				.findViewById(R.id.divesite_city_province_comma);
		mDiveSiteCountryView = view
				.findViewById(R.id.divesite_country_viewing);

		mDiveSiteDescriptionView = view
				.findViewById(R.id.divesite_description_viewing);
		mDiveSiteDescriptionView.setMovementMethod(LinkMovementMethod
				.getInstance());

		mDiveSiteNameEdit = view
				.findViewById(R.id.divesite_name_editing);
		mDiveSiteNameEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
				mDiveSite.setName((c.toString()).trim());
				Objects.requireNonNull(getActivity()).setTitle(mDiveSite.getName());
			}

			@Override
			public void afterTextChanged(Editable s) {
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		mDiveSiteDifficultyEdit = view
				.findViewById(R.id.divesite_difficulty_editing);
		mDiveSiteDifficultyEdit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle(R.string.select_difficulty).setItems(
						DiveSite.Difficulty_Names,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// The 'which' argument contains the index
								// position
								// of the selected item
								mDiveSite.setDifficulty(which);
								mDiveSiteDifficultyEdit.setText(mDiveSite
										.getDifficulty().getName());
								saveDiveSite();
							}
						});
				builder.show();
			}
		});

		mDiveSiteCityEdit = view
				.findViewById(R.id.divesite_city_editing);
		mDiveSiteCityEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setCity(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
		mDiveSiteProvinceEdit = view
				.findViewById(R.id.divesite_province_editing);
		mDiveSiteProvinceEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setProvince(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}
		});
		mDiveSiteCountryEdit = view
				.findViewById(R.id.divesite_country_editing);
		mDiveSiteCountryEdit
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View v, int position, long id) {
						if (!mDiveSite.getCountry().equals(
								getResources().getTextArray(
										R.array.countries_array)[position]
										.toString())) {
							mDiveSite.setCountry(getResources().getTextArray(
									R.array.countries_array)[position]
									.toString());
							saveDiveSite();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}

				});

		mDiveSiteIndicatorSalt = view
				.findViewById(R.id.divesite_indicate_isSalt);
		mDiveSiteIndicatorSalt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode) {
					mDiveSite.setSalty(!mDiveSite.isSalty());
					saveDiveSite();

					if (mDiveSite.isSalty()) {
						mDiveSiteIndicatorSalt
								.setImageResource(R.drawable.divesite_salt_enabled_icon);
						mDiveSiteIndicatorFresh
								.setImageResource(R.drawable.divesite_fresh_disabled_icon);
					} else {
						mDiveSiteIndicatorSalt
								.setImageResource(R.drawable.divesite_salt_disabled_icon);
						mDiveSiteIndicatorFresh
								.setImageResource(R.drawable.divesite_fresh_enabled_icon);
					}
				}
			}
		});

		mDiveSiteIndicatorFresh = view
				.findViewById(R.id.divesite_indicate_isFresh);
		mDiveSiteIndicatorFresh.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode) {
					mDiveSite.setSalty(!mDiveSite.isSalty());
					saveDiveSite();

					if (mDiveSite.isSalty()) {
						mDiveSiteIndicatorSalt
								.setImageResource(R.drawable.divesite_salt_enabled_icon);
						mDiveSiteIndicatorFresh
								.setImageResource(R.drawable.divesite_fresh_disabled_icon);
					} else {
						mDiveSiteIndicatorSalt
								.setImageResource(R.drawable.divesite_salt_disabled_icon);
						mDiveSiteIndicatorFresh
								.setImageResource(R.drawable.divesite_fresh_enabled_icon);
					}
				}
			}
		});

		mDiveSiteIndicatorShore = view
				.findViewById(R.id.divesite_indicate_isShore);
		mDiveSiteIndicatorShore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode) {
					mDiveSite.setShoreDive(!mDiveSite.isShoreDive());
					saveDiveSite();

					if (mDiveSite.isShoreDive()) {
						mDiveSiteIndicatorShore
								.setImageResource(R.drawable.divesite_shore_enabled_icon);
					} else {
						mDiveSiteIndicatorShore
								.setImageResource(R.drawable.divesite_shore_disabled_icon);
					}
				}
			}
		});

		mDiveSiteIndicatorBoat = view
				.findViewById(R.id.divesite_indicate_isBoat);
		mDiveSiteIndicatorBoat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode) {
					mDiveSite.setBoatDive(!mDiveSite.isBoatDive());
					saveDiveSite();

					if (mDiveSite.isBoatDive()) {
						mDiveSiteIndicatorBoat
								.setImageResource(R.drawable.divesite_boat_enabled_icon);
					} else {
						mDiveSiteIndicatorBoat
								.setImageResource(R.drawable.divesite_boat_disabled_icon);
					}
				}
			}
		});

		mDiveSiteIndicatorWreck = view
				.findViewById(R.id.divesite_indicate_isWreck);
		mDiveSiteIndicatorWreck.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditMode) {
					mDiveSite.setWreck(!mDiveSite.isWreck());
					saveDiveSite();

					if (mDiveSite.isWreck()) {
						mDiveSiteIndicatorWreck
								.setImageResource(R.drawable.divesite_wreck_enabled_icon);
					} else {
						mDiveSiteIndicatorWreck
								.setImageResource(R.drawable.divesite_wreck_disabled_icon);
					}
				}
			}
		});

		mDiveSiteDescriptionEdit = view
				.findViewById(R.id.divesite_description_editing);
		mDiveSiteDescriptionEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setDescription(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		mDiveSiteDirectionsTitle = view
				.findViewById(R.id.divesite_directions_title);
		mDiveSiteHistoryTitle = view
				.findViewById(R.id.divesite_history_title);
		mDiveSiteNotesTitle = view
				.findViewById(R.id.divesite_notes_title);

		mDiveSiteDirectionsView = view
				.findViewById(R.id.divesite_directions_viewing);
		mDiveSiteDirectionsView.setMovementMethod(LinkMovementMethod
				.getInstance());
		mDiveSiteHistoryView = view
				.findViewById(R.id.divesite_history_viewing);
		mDiveSiteHistoryView
				.setMovementMethod(LinkMovementMethod.getInstance());
		mDiveSiteNotesView = view
				.findViewById(R.id.divesite_notes_viewing);
		mDiveSiteNotesView.setMovementMethod(LinkMovementMethod.getInstance());
		mDiveSiteSourceView = view
				.findViewById(R.id.divesite_source_viewing);
		mDiveSiteSourceView.setMovementMethod(LinkMovementMethod.getInstance());
		mDiveSiteSubmitterView = view
				.findViewById(R.id.divesite_submitter_viewing);
        mDiveSiteSubmitterImage = view.findViewById(R.id.divesite_submitter_image);

		mDiveSiteDirectionsEdit = view
				.findViewById(R.id.divesite_directions_editing);
		mDiveSiteDirectionsEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setDirections(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		mDiveSiteHistoryEdit = view
				.findViewById(R.id.divesite_history_editing);
		mDiveSiteHistoryEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setHistory(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		mDiveSiteNotesEdit = view
				.findViewById(R.id.divesite_notes_editing);
		mDiveSiteNotesEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setNotes(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		mDiveSiteSourceEdit = view
				.findViewById(R.id.divesite_source_editing);
		mDiveSiteSourceEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mDiveSite.setSource(s.toString().trim());
				saveDiveSite();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		mDiveSiteLatitudeEdit = view
				.findViewById(R.id.divesite_latitude_editing);
		mDiveSiteLatitudeEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				Pattern p = Pattern
						.compile(getString(R.string.divesite_latitude_regex));
				Matcher m = p.matcher(s.toString());
				if (m.matches()) {
					mDiveSite.setLatitude(Float.valueOf(s.toString()));
					updateMaps();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});
		mDiveSiteLongitudeEdit = view
				.findViewById(R.id.divesite_longitude_editing);
		mDiveSiteLongitudeEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence c, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				Pattern p = Pattern
						.compile(getString(R.string.divesite_longitude_regex));
				Matcher m = p.matcher(s.toString());
				if (m.matches()) {
					mDiveSite.setLongitude(Float.valueOf(s.toString()));
					updateMaps();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});
		mSetCurrentLatitudeLongitude = view
				.findViewById(R.id.divesite_getLatitudeLongitude);
		mSetCurrentLatitudeLongitude
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						updateMaps();
					}
				});

		mMapView = view.findViewById(R.id.divesite_view_mapView);
		mMapView.onCreate(savedInstanceState);
        initializeMap();

        mMapViewSnapShot = view.findViewById(R.id.divesite_view_mapView_snapShot);

		setDiveSiteAvailable(mDiveSite);

		view.post(new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });

		return view;
	}

    @Override
    protected void onLocationPermissionGranted() {
        initializeMap();
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

                    if (mDiveSite != null) {
                        LatLng latLng = new LatLng(mDiveSite.getLatitude(), mDiveSite.getLongitude());

                        // If this marker exists, no need to add it again
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng).title(mDiveSite.getName());

                        if (mDiveSite.getOnlineId() != -1) {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.divesite_active_marker));
                        } else {
                            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.divesite_active_local_marker));
                        }

                        googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                    }
                }
            });

            MapsInitializer.initialize(Objects.requireNonNull(getActivity()));
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

	public int getStatusBarHeight() {
		Rect r = new Rect();
		Window w = Objects.requireNonNull(getActivity()).getWindow();
		w.getDecorView().getWindowVisibleDisplayFrame(r);
		return r.top;
	}

	public int getTitleBarHeight() {
		int viewTop = Objects.requireNonNull(getActivity()).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return (viewTop - getStatusBarHeight() + Objects.requireNonNull(((DiveActivity) getActivity()).getSupportActionBar()).getHeight());
	}

	@Override
	protected void updateUI() {
		// Disable saving while loading UI
		mDisableSave = true;

		// Set visibility based on edit mode
		super.updateUI();

		if (mEditMode) {
			mDiveSiteLocationViewContainer.setVisibility(View.GONE);
			mDiveSiteDescriptionView.setVisibility(View.GONE);
			mDiveSiteDifficultyView.setVisibility(View.GONE);
			mDiveSiteDirectionsView.setVisibility(View.GONE);
			mDiveSiteHistoryView.setVisibility(View.GONE);
			mDiveSiteNotesView.setVisibility(View.GONE);
			mDiveSiteSourceView.setVisibility(View.GONE);
			mDiveSiteNameEdit.setVisibility(View.VISIBLE);
			mDiveSiteRating.setIsIndicator(false);
			mDiveSiteLocationEditContainer.setVisibility(View.VISIBLE);
			mDiveSiteDescriptionEdit.setVisibility(View.VISIBLE);
			mDiveSiteDifficultyEdit.setVisibility(View.VISIBLE);
			mDiveSiteCoordinatesEditContainer.setVisibility(View.VISIBLE);
			mDiveSiteDirectionsEdit.setVisibility(View.VISIBLE);
			mDiveSiteHistoryEdit.setVisibility(View.VISIBLE);
			mDiveSiteNotesEdit.setVisibility(View.VISIBLE);
			mDiveSiteSourceEdit.setVisibility(View.VISIBLE);
		} else {
			mDiveSiteLocationViewContainer.setVisibility(View.VISIBLE);
			mDiveSiteDescriptionView.setVisibility(View.VISIBLE);
			mDiveSiteDifficultyView.setVisibility(View.VISIBLE);
			mDiveSiteDirectionsView.setVisibility(View.VISIBLE);
			mDiveSiteHistoryView.setVisibility(View.VISIBLE);
			mDiveSiteNotesView.setVisibility(View.VISIBLE);
			mDiveSiteSourceView.setVisibility(View.VISIBLE);
			mDiveSiteNameEdit.setVisibility(View.GONE);
			mDiveSiteRating.setIsIndicator(true);
			mDiveSiteLocationEditContainer.setVisibility(View.GONE);
			mDiveSiteDescriptionEdit.setVisibility(View.GONE);
			mDiveSiteDifficultyEdit.setVisibility(View.GONE);
			mDiveSiteCoordinatesEditContainer.setVisibility(View.GONE);
			mDiveSiteDirectionsEdit.setVisibility(View.GONE);
			mDiveSiteHistoryEdit.setVisibility(View.GONE);
			mDiveSiteNotesEdit.setVisibility(View.GONE);
			mDiveSiteSourceEdit.setVisibility(View.GONE);
		}

		updateMapViewHeight();

		// Set UI elements with current diveSite
		if (mDiveSite != null) {

			mDiveSiteNameEdit.setText(mDiveSite.getName());
			Objects.requireNonNull(getActivity()).setTitle(mDiveSite.getName());

			if (mDiveSite.getRatingCount() == 0) {
				mDiveSiteRating.setRating(0);
			} else {
				mDiveSiteRating.setRating(mDiveSite.getTotalRating() / mDiveSite.getRatingCount());
			}

			mDiveSiteDifficultyView
					.setText(mDiveSite.getDifficulty().getName());
			mDiveSiteCityView.setText(mDiveSite.getCity());
			mDiveSiteProvinceView.setText(mDiveSite.getProvince());

			if (mDiveSite.getCity().isEmpty()
					|| mDiveSite.getProvince().isEmpty()) {
				mDiveSiteCityProvinceComma.setVisibility(View.INVISIBLE);
			} else {
				mDiveSiteCityProvinceComma.setVisibility(View.VISIBLE);
			}

			mDiveSiteCountryView.setText(mDiveSite.getCountry());

			if (mDiveSite.isSalty()) {
				mDiveSiteIndicatorSalt
						.setImageResource(R.drawable.divesite_salt_enabled_icon);
			} else {
				mDiveSiteIndicatorSalt
						.setImageResource(R.drawable.divesite_salt_disabled_icon);
			}

			if (!mDiveSite.isSalty()) {
				mDiveSiteIndicatorFresh
						.setImageResource(R.drawable.divesite_fresh_enabled_icon);
			} else {
				mDiveSiteIndicatorFresh
						.setImageResource(R.drawable.divesite_fresh_disabled_icon);
			}

			if (mDiveSite.isShoreDive()) {
				mDiveSiteIndicatorShore
						.setImageResource(R.drawable.divesite_shore_enabled_icon);
			} else {
				mDiveSiteIndicatorShore
						.setImageResource(R.drawable.divesite_shore_disabled_icon);
			}

			if (mDiveSite.isBoatDive()) {
				mDiveSiteIndicatorBoat
						.setImageResource(R.drawable.divesite_boat_enabled_icon);
			} else {
				mDiveSiteIndicatorBoat
						.setImageResource(R.drawable.divesite_boat_disabled_icon);
			}

			if (mDiveSite.isWreck()) {
				mDiveSiteIndicatorWreck
						.setImageResource(R.drawable.divesite_wreck_enabled_icon);
			} else {
				mDiveSiteIndicatorWreck
						.setImageResource(R.drawable.divesite_wreck_disabled_icon);
			}

			mDiveSiteDescriptionView.setText(Html.fromHtml(mDiveSite
					.getDescription()));
			mDiveSiteCityEdit.setText(mDiveSite.getCity());
			mDiveSiteProvinceEdit.setText(mDiveSite.getProvince());
			mDiveSiteCountryEdit
					.setSelection(findCountrySelectionPosition(mDiveSite
							.getCountry()));
			mDiveSiteDescriptionEdit.setText(mDiveSite.getDescription());
			mDiveSiteDifficultyEdit
					.setText(mDiveSite.getDifficulty().getName());

			mDiveSiteLatitudeEdit.setText(String.valueOf(mDiveSite
					.getLatitude()));
			mDiveSiteLongitudeEdit.setText(String.valueOf(mDiveSite
					.getLongitude()));

			if (!mEditMode && mDiveSite.getDirections().trim().isEmpty()) {
				mDiveSiteDirectionsTitle.setVisibility(View.GONE);
				mDiveSiteDirectionsView.setVisibility(View.GONE);
			} else {
				mDiveSiteDirectionsTitle.setVisibility(View.VISIBLE);
			}

			if (!mEditMode && mDiveSite.getHistory().trim().isEmpty()) {
				mDiveSiteHistoryTitle.setVisibility(View.GONE);
				mDiveSiteHistoryView.setVisibility(View.GONE);
			} else {
				mDiveSiteHistoryTitle.setVisibility(View.VISIBLE);
			}

			if (!mEditMode && mDiveSite.getNotes().trim().isEmpty()) {
				mDiveSiteNotesTitle.setVisibility(View.GONE);
				mDiveSiteNotesView.setVisibility(View.GONE);
			} else {
				mDiveSiteNotesTitle.setVisibility(View.VISIBLE);
			}

			mDiveSiteDirectionsView.setText(Html.fromHtml(mDiveSite
					.getDirections()));
			mDiveSiteDirectionsEdit.setText(mDiveSite.getDirections());
			mDiveSiteHistoryView.setText(Html.fromHtml(mDiveSite.getHistory()));
			mDiveSiteHistoryEdit.setText(mDiveSite.getHistory());
			mDiveSiteNotesView.setText(Html.fromHtml(mDiveSite.getNotes()));
			mDiveSiteNotesEdit.setText(mDiveSite.getNotes());
			mDiveSiteSourceView.setText(Html.fromHtml(mDiveSite.getSource()));
			mDiveSiteSourceEdit.setText(mDiveSite.getSource());
			mDiveSiteSubmitterView.setText(mDiveSite.getUsername());

            mDiveSiteSubmitterImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), DiverActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    i.putExtra(DiverActivity.EXTRA_DIVER_ID, mDiveSite.getUserId());
                    i.putExtra(DiverActivity.EXTRA_DIVER_USERNAME, mDiveSite.getUsername());
                    startActivity(i);
                }
            });

            DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseLink = new DiveSiteOnlineDatabaseLink(getActivity());
            diveSiteOnlineDatabaseLink.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                @Override
                public void onOnlineDiveDataRetrievedComplete(
                        ArrayList<Object> resultList,
                        String message, Boolean isError) {
                    if (resultList.size() > 0) {
                        // Now get bitmap profile image for diver if available
                        Diver diver = (Diver) resultList.get(0);
                        if (!diver.getPictureURL().trim().isEmpty()) {
                            LoadOnlineImageTask task = new LoadOnlineImageTask(mDiveSiteSubmitterImage);
                            task.execute(diver.getPictureURL());
                        }
                    }
                }

                @Override
                public void onOnlineDiveDataProgress(
                        Object result) {
                }

                @Override
                public void onOnlineDiveDataPostBackground(
                        ArrayList<Object> resultList,
                        String message) {
                }
            });
            diveSiteOnlineDatabaseLink.getUser(String.valueOf(mDiveSite.getUserId()), "", "");

		}

		mDisableSave = false;
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

	@Override
	protected void DoDiveSiteAvailable() {
		super.DoDiveSiteAvailable();

		if (mDiveSiteInfoProgressBar != null) {
			mDiveSiteInfoProgressBar.setVisibility(View.GONE);
		}

		if (mDiveSiteView != null) {
			mDiveSiteView.setVisibility(View.VISIBLE);
		}
	}

	private void saveDiveSite() {
		if (!mDisableSave) {
			mDiveSite.setPublished(false);
			mDiveSiteManager.saveDiveSite(mDiveSite);
		}
	}

	private int findCountrySelectionPosition(String country) {
		String[] countries = getResources().getStringArray(
				R.array.countries_array);
		for (int i = 0; i < countries.length; i++) {
			if (countries[i].equals(country)) {
				return i;
			}
		}

		return 0;
	}

	private void updateMaps() {
		LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
		LatLng latLng = new LatLng(mDiveSite.getLatitude(),
				mDiveSite.getLongitude());
		latLngBuilder.include(latLng);

		MarkerOptions markerOptions = new MarkerOptions().position(latLng);

		if (mDiveSite.getOnlineId() != -1) {
			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.divesite_active_marker));
		} else if (mDiveSite.isArchived()) {
			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.divesite_inactive_marker));
		} else {
			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.divesite_active_local_marker));
		}

		if (mGoogleMap != null) {
			mGoogleMap.clear();
			mGoogleMap.addMarker(markerOptions);
			mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
					12.0f));
		}
	}

	private void updateMapViewHeight() {
		// Set height of MapView
		int screenHeight = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay()
				.getHeight();

		int coordinatesContainerHeight = 0;
		if (mEditMode) {
			mDiveSiteCoordinatesEditContainer.measure(MeasureSpec.UNSPECIFIED,
					MeasureSpec.UNSPECIFIED);
			coordinatesContainerHeight = mDiveSiteCoordinatesEditContainer
					.getMeasuredHeight();
		}

		mMapView.getLayoutParams().height = screenHeight - getTitleBarHeight()
				- getStatusBarHeight() - coordinatesContainerHeight
				- MAPVIEW_HEIGHT_BUFFER;
	}

    protected void setSnapshot(int visibility) {
        switch (visibility) {
            case View.VISIBLE:
                if (mMapView.getVisibility() == View.VISIBLE) {
                	mMapView.getMapAsync(new OnMapReadyCallback() {
						@Override
						public void onMapReady(GoogleMap googleMap) {
							googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
								@Override
								public void onSnapshotReady(Bitmap bitmap) {
									mMapViewSnapShot.setImageBitmap(bitmap);
								}
							});
						}
					});

                    mMapViewSnapShot.setVisibility(View.VISIBLE);
                    mMapView.setVisibility(View.INVISIBLE);
                }
                break;
            case View.INVISIBLE:
                if (mMapView.getVisibility() == View.INVISIBLE) {
                    mMapView.setVisibility(View.VISIBLE);

                    mMapViewSnapShot.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }
}
