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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.controller.LocationController;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.NDBCStation;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCDriftingBuoyData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCMeteorologicalData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCSpectralWaveData;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiveSiteInfoPageFragment extends DiveSitePageFragment {

    private static final String ARG_DIVESITE = "DIVESITE";

	private static final int MAPVIEW_HEIGHT_BUFFER = 500;

    private boolean mDisableSave = false;
	private Boolean mRefreshingOnlineNDBCData = false;
	
	private HashMap<NDBCStation, Marker> mVisibleNDBCStationMarkers;

	private View mDiveSiteInfoProgressBar, mDiveSiteView;

	private RelativeLayout mDiveSiteLocationViewContainer,
			mDiveSiteLocationEditContainer;
	private RelativeLayout mDiveSiteCoordinatesEditContainer;

	private FAMapView mMapView;
	private GoogleMap mGoogleMap = null;
    private ImageView mMapViewSnapShot;

	private DiveSiteOnlineDatabaseLink mNDBCDataOnlineDatabase = null;

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

		mVisibleNDBCStationMarkers = new HashMap<>();
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
		
		mDiveSiteDataViewContainer = view
				.findViewById(R.id.diveSite_map_data_view_container);
		
		// Create and add views for each type of data
		mDiveSiteLatestMeteorologicalData = (LinearLayout) inflater.inflate(
				R.layout.ndbc_station_data_item, mDiveSiteDataViewContainer,
				false);
		mDiveSiteLatestMeteorologicalData.setVisibility(View.GONE);
		mDiveSiteDataViewContainer.addView(mDiveSiteLatestMeteorologicalData);

		mNDBCMeteorologicalDataTitle = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_station_data_item_title);
		mNDBCMeteorologicalDataLabel00 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_00);
		mNDBCMeteorologicalDataValue00 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_00);
		mNDBCMeteorologicalDataLabel01 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_01);
		mNDBCMeteorologicalDataValue01 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_01);
		mNDBCMeteorologicalDataLabel02 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_02);
		mNDBCMeteorologicalDataValue02 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_02);
		mNDBCMeteorologicalDataLabel10 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_10);
		mNDBCMeteorologicalDataValue10 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_10);
		mNDBCMeteorologicalDataLabel11 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_11);
		mNDBCMeteorologicalDataValue11 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_11);
		mNDBCMeteorologicalDataLabel12 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_12);
		mNDBCMeteorologicalDataValue12 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_12);
		mNDBCMeteorologicalDataLabel20 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_20);
		mNDBCMeteorologicalDataValue20 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_20);
		mNDBCMeteorologicalDataLabel21 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_21);
		mNDBCMeteorologicalDataValue21 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_21);
		mNDBCMeteorologicalDataLabel22 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_label_22);
		mNDBCMeteorologicalDataValue22 = mDiveSiteLatestMeteorologicalData
				.findViewById(R.id.ndbc_data_item_value_22);

		mDiveSiteLatestWaveData = (LinearLayout) inflater.inflate(
				R.layout.ndbc_station_data_item, mDiveSiteDataViewContainer,
				false);
		mDiveSiteLatestWaveData.setVisibility(View.GONE);
		mDiveSiteDataViewContainer.addView(mDiveSiteLatestWaveData);

		mNDBCWaveDataTitle = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_station_data_item_title);
		mNDBCWaveDataLabel00 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_00);
		mNDBCWaveDataValue00 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_00);
		mNDBCWaveDataLabel01 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_01);
		mNDBCWaveDataValue01 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_01);
		mNDBCWaveDataLabel02 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_02);
		mNDBCWaveDataValue02 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_02);
		mNDBCWaveDataLabel10 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_10);
		mNDBCWaveDataValue10 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_10);
		mNDBCWaveDataLabel11 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_11);
		mNDBCWaveDataValue11 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_11);
		mNDBCWaveDataLabel12 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_12);
		mNDBCWaveDataValue12 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_12);
		mNDBCWaveDataLabel20 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_20);
		mNDBCWaveDataValue20 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_20);
		mNDBCWaveDataLabel21 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_21);
		mNDBCWaveDataValue21 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_21);
		mNDBCWaveDataLabel22 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_label_22);
		mNDBCWaveDataValue22 = mDiveSiteLatestWaveData
				.findViewById(R.id.ndbc_data_item_value_22);

		mDiveSiteDataToolbar = view
				.findViewById(R.id.diveSite_map_data_toolbar);

		mMeteorologicalDataButton = view
				.findViewById(R.id.diveSite_map_meteorological);
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

		mWaveDataButton = view.findViewById(R.id.diveSite_map_wave);
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
	
	private void refreshVisibleNDBCStations() {
		if (mRefreshingOnlineNDBCData) {
			mNDBCDataOnlineDatabase.stopBackground();
		}

		mRefreshingOnlineNDBCData = true;

		if (mNDBCDataOnlineDatabase != null
				&& mNDBCDataOnlineDatabase.getActive()) {
			mNDBCDataOnlineDatabase.stopBackground();
			mNDBCDataOnlineDatabase.cancel(true);
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
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mNDBCDataOnlineDatabase.getActive() && getActivity() != null) {
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
							for (int j = 0; j < Objects.requireNonNull(ndbcStations).length; j++) {
								if (((NDBCStation) ndbcStations[j])
										.getStationId() == ndbcStation
										.getStationId()) {
									NDBCStation existingNDBCStation = (NDBCStation) ndbcStations[j];

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
					}
				});

		// Show buoys in range with data from last day at least only
		String coordinateRange[] = getCoordinateRange(mGoogleMap);
		
		Date minLastUpdateTimestamp = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(minLastUpdateTimestamp);
		c.add(Calendar.DATE, -1);
		minLastUpdateTimestamp = c.getTime();
		
		mNDBCDataOnlineDatabase.getNDBCStations(coordinateRange[0],
				coordinateRange[1], coordinateRange[2], coordinateRange[3],
				"1", String.valueOf(minLastUpdateTimestamp.getTime()),
				"", "", "", "", "");
	}
	
	public void loadDataForStation(NDBCStation station) {
		// Load new data if user update time is less than station's online
		// update time
		if (station != null) {
			if (station.getLastUserUpdate().before(
					station.getLastOnlineUpdate())) {

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
									for (int i = 0; i < Objects.requireNonNull(ndbcStations).length; i++) {
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
											Objects.requireNonNull(getActivity())
													.getApplicationContext(),
											message, Toast.LENGTH_LONG).show();
								}

							}

							@Override
							public void onOnlineDiveDataProgress(Object result) {
							}

							@Override
							public void onOnlineDiveDataPostBackground(
									ArrayList<Object> resultList, String message) {
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
				mNDBCWaveDataValue22.setText(data.getWindWaveDirection());
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
