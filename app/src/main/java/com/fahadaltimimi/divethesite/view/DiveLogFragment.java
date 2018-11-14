package com.fahadaltimimi.divethesite.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.fahadaltimimi.divethesite.model.DiveLogStop;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.DiveLogBuddy;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;

public class DiveLogFragment extends Fragment {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy");

	private static final String TAG = "DiveLogFragment";

	public static final String ARG_DIVELOG = "DIVELOG_LOCAL_ID";
	public static final String ARG_SET_TO_DIVELOG = "SET_TO_DIVELOG";

	public static final int SELECT_DIVE_SITE = 0;
	public static final int CREATE_DIVE_SITE = 1;
	public static final int SELECT_DIVER_DIVERLIST = 2;
	public static final int SELECT_DIVER_CONTACTS = 3;

	private static final int BUDDY_BUTTON_OPTIONS_COUNT = 3;
	private static final int BUDDY_BUTTON_OPTION_DIVETHESITE_SELECT = 0;
	private static final int BUDDY_BUTTON_OPTION_CONTACTS_SELECT = 1;
	private static final int BUDDY_BUTTON_OPTION_VIEW_PROFILE = 2;

	protected DiveSiteManager mDiveSiteManager;

	private DiveLog mDiveLog = null;
	private View mLastSelectedBuddyView = null;
	private ArrayList<DiveLogBuddy> mDiveLogBuddiesDelete = new ArrayList<DiveLogBuddy>();
	private ArrayList<DiveLogStop> mDiveLogStopsDelete = new ArrayList<DiveLogStop>();
	private HashMap<DiveLogBuddy, ImageView> mDiveLogBuddyImages = new HashMap<DiveLogBuddy, ImageView>();

	private EditText mDiveLogSetDiveSite;
	private ImageButton mDiveLogDiveSiteSearch;
	private Button mDiveLogSetTimestamp;
	private RatingBar mDiveLogSetRating;
	private EditText mDiveLogSetDiveTime;
	private Spinner mDiveLogSetStartPressure, mDiveLogSetEndPressure;
    private EditText mDiveLogSetStartAirValue;
    private Spinner mDiveLogSetStartAirUnits;
    private EditText mDiveLogSetEndAirValue;
    private Spinner mDiveLogSetEndAirUnits;
	private EditText mDiveLogSetMaxDepthValue;
	private Spinner mDiveLogSetMaxDepthUnits;
	private EditText mDiveLogSetAvgDepthValue;
	private Spinner mDiveLogSetAvgDepthUnits;
	private EditText mDiveLogSetGasMixValue;
	private EditText mDiveLogSetAirTempValue;
	private Spinner mDiveLogSetAirTempUnits;
	private EditText mDiveLogSetWaterTempValue;
	private Spinner mDiveLogSetWaterTempUnits;
	private EditText mDiveLogSetVisibilityValue;
	private Spinner mDiveLogSetVisibilityUnits;
	private EditText mDiveLogSetSurfaceTime;
	private EditText mDiveLogSetWeightsValue;
	private Spinner mDiveLogSetWeightsUnits;

	private EditText mDiveLogSetComments;
	private LinearLayout mDiveLogSetBuddies;
	private ImageButton mDiveLogAddBuddy;
	private LinearLayout mDiveLogSetStops;
	private ImageButton mDiveLogAddStop;

	private ImageButton mDiveLogDayIndicator;
	private ImageButton mDiveLogNightIndicator;
	private ImageButton mDiveLogPhotoEnabledIndicator;
	private ImageButton mDiveLogPhotoDisabledIndicator;
	private ImageButton mDiveLogDeepEnabledIndicator;
	private ImageButton mDiveLogDeepDisabledIndicator;
	private ImageButton mDiveLogIceEnabledIndicator;
	private ImageButton mDiveLogIceDisabledIndicator;
	private ImageButton mDiveLogCourseEnabledIndicator;
	private ImageButton mDiveLogCourseDisabledIndicator;
	private ImageButton mDiveLogInstructorEnabledIndicator;
	private ImageButton mDiveLogInstructorDisabledIndicator;

	private DatePickerDialog mDiveLogTimestampDateDialog = null;
	private TimePickerDialog mDiveLogTimestampTimeDialog = null;

    protected SharedPreferences mPrefs;

	public static DiveLogFragment newInstance(DiveLog diveLog, DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_DIVELOG, diveLog);
        args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);
		DiveLogFragment rf = new DiveLogFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);

		mDiveSiteManager = DiveSiteManager.get(getActivity());

		Bundle args = getArguments();
		if (args != null) {
			mDiveLog = args.getParcelable(ARG_DIVELOG);
		}

        mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

		if (mDiveLog == null) {
			mDiveLog = new DiveLog();
			mDiveLog.setUserId(mDiveSiteManager.getLoggedInDiverId());
			mDiveLog.setUsername(mDiveSiteManager.getLoggedInDiverUsername());

            DiveSite diveSite = args.getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
            mDiveLog.setDiveSite(diveSite);

			if (mDiveLog.getDiveSite() != null) {
				mDiveLog.setDiveSiteLocalId(mDiveLog.getDiveSite().getLocalId());
				mDiveLog.setDiveSiteOnlineId(mDiveLog.getDiveSite().getOnlineId());
			}

            // Set default units for new Dive Log
            mDiveLog.getStartAir().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_STARTAIRUNITS, ""));
            mDiveLog.getEndAir().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_ENDAIRUNITS, ""));
            mDiveLog.getMaxDepth().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_MAXDEPTHUNITS, ""));
            mDiveLog.getAverageDepth().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_AVGDEPTHUNITS, ""));
            mDiveLog.getSurfaceTemperature().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_AIRTEMPUNITS, ""));
            mDiveLog.getWaterTemperature().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_WATERTEMPUNITS, ""));
            mDiveLog.getVisibility().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_VISIBILITYUNITS, ""));
            mDiveLog.getWeightsRequired().setUnits(mPrefs.getString(DiveSiteManager.PREF_DIVELOG_WEIGHTUNITS, ""));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_divelog, parent, false);

		mDiveLogSetDiveSite = (EditText) view
				.findViewById(R.id.divelog_set_divesite);
		if (mDiveLog.getDiveSite() != null) {
			mDiveLogSetDiveSite.setText(mDiveLog.getDiveSite().getName());
		}

		mDiveLogSetDiveSite.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// Reset dive log's dive site if changed
				if (mDiveLog.getDiveSite() != null && 
						!mDiveLog.getDiveSite().getName().equals(s.toString())) {
					mDiveLog.setDiveSite(null);
					mDiveLog.setDiveSiteLocalId(-1);
					mDiveLog.setDiveSiteOnlineId(-1);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogDiveSiteSearch = (ImageButton) view
				.findViewById(R.id.divelog_divesite_search);
		mDiveLogDiveSiteSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Open Dive Site List with filter set to text entered
				mPrefs.edit().putString(DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE, mDiveLogSetDiveSite.getText().toString().trim()).commit();

				Intent intent = new Intent(DiveLogFragment.this.getActivity(),
						DiveSiteListActivity.class);
				intent.putExtra(DiveLogActivity.EXTRA_SET_TO_DIVELOG, true);
				startActivityForResult(intent, SELECT_DIVE_SITE);
			}
		});

		mDiveLogTimestampDateDialog = new DatePickerDialog(getActivity(),
				new PickDiveLogTimestampDate(), 0, 0, 0);
		mDiveLogTimestampDateDialog.getDatePicker().init(0, 0, 0, new DatePicker.OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthOfYear, dayOfMonth);
			}
		}); 
		
		mDiveLogTimestampTimeDialog = new TimePickerDialog(getActivity(),
				new PickDiveLogTimestampTime(), 0, 0, true);

		mDiveLogSetTimestamp = (Button) view
				.findViewById(R.id.divelog_set_timestamp);
		mDiveLogSetTimestamp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Ask user first for a date selection, then time selection
				Calendar currentCalendar = Calendar.getInstance();
				currentCalendar.setTime(mDiveLog.getTimestamp());

				mDiveLogTimestampDateDialog.updateDate(
						currentCalendar.get(Calendar.YEAR),
						currentCalendar.get(Calendar.MONTH),
						currentCalendar.get(Calendar.DATE));

				mDiveLogTimestampDateDialog.show();
			}
		});

		mDiveLogSetRating = (RatingBar) view
				.findViewById(R.id.divelog_set_rating);
		mDiveLogSetRating
				.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

					@Override
					public void onRatingChanged(RatingBar ratingBar,
							float rating, boolean fromUser) {
						mDiveLog.setRating(rating);
					}

				});

		mDiveLogSetDiveTime = (EditText) view
				.findViewById(R.id.divelog_set_dive_time);
		mDiveLogSetDiveTime.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.setDiveTime(0);
				} else {
					mDiveLog.setDiveTime(Integer.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetStartPressure = (Spinner) view
				.findViewById(R.id.divelog_set_start_pressure);
		mDiveLogSetStartPressure
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View v, int position, long id) {
						if (mDiveLog != null) {
							mDiveLog.setStartPressure(getResources()
									.getStringArray(R.array.alphabet)[position]
									.charAt(0));
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}

				});

		mDiveLogSetEndPressure = (Spinner) view
				.findViewById(R.id.divelog_set_end_pressure);
		mDiveLogSetEndPressure
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parentView,
							View v, int position, long id) {
						if (mDiveLog != null) {
							mDiveLog.setEndPressure(getResources()
									.getStringArray(R.array.alphabet)[position]
									.charAt(0));
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}

				});

        mDiveLogSetStartAirValue = (EditText) view.findViewById(R.id.divelog_set_start_air);
        mDiveLogSetStartAirValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    mDiveLog.getStartAir().setValue(0);
                } else {
                    mDiveLog.getStartAir().setValue(Double.valueOf(s.toString()));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

        });

        mDiveLogSetStartAirUnits = (Spinner) view.findViewById(R.id.divelog_set_start_air_units);

        mDiveLogSetEndAirValue = (EditText) view.findViewById(R.id.divelog_set_end_air);
        mDiveLogSetEndAirValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    mDiveLog.getEndAir().setValue(0);
                } else {
                    mDiveLog.getEndAir().setValue(Double.valueOf(s.toString()));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

        });

        mDiveLogSetEndAirUnits = (Spinner) view.findViewById(R.id.divelog_set_end_air_units);

		mDiveLogSetMaxDepthValue = (EditText) view.findViewById(R.id.divelog_set_max_depth);
		mDiveLogSetMaxDepthValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.getMaxDepth().setValue(0);
				} else {
					mDiveLog.getMaxDepth().setValue(
							Double.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetMaxDepthUnits = (Spinner) view
				.findViewById(R.id.divelog_set_max_depth_units);

		mDiveLogSetAvgDepthValue = (EditText) view
				.findViewById(R.id.divelog_set_avg_depth);
		mDiveLogSetAvgDepthValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.getAverageDepth().setValue(0);
				} else {
					mDiveLog.getAverageDepth().setValue(
							Double.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetGasMixValue = (EditText) view
				.findViewById(R.id.divelog_set_gas_mix);
		mDiveLogSetGasMixValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				mDiveLog.setAirType(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetAvgDepthUnits = (Spinner) view
				.findViewById(R.id.divelog_set_avg_depth_units);

		mDiveLogSetAirTempValue = (EditText) view
				.findViewById(R.id.divelog_set_air_temp);
		mDiveLogSetAirTempValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.getSurfaceTemperature().setValue(0);
				} else {
					mDiveLog.getSurfaceTemperature().setValue(
							Double.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetAirTempUnits = (Spinner) view
				.findViewById(R.id.divelog_set_air_temp_units);

		mDiveLogSetWaterTempValue = (EditText) view
				.findViewById(R.id.divelog_set_water_temp);
		mDiveLogSetWaterTempValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.getWaterTemperature().setValue(0);
				} else {
					mDiveLog.getWaterTemperature().setValue(
							Double.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetWaterTempUnits = (Spinner) view
				.findViewById(R.id.divelog_set_water_temp_units);

		mDiveLogSetVisibilityValue = (EditText) view
				.findViewById(R.id.divelog_set_visibility);
		mDiveLogSetVisibilityValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.getVisibility().setValue(0);
				} else {
					mDiveLog.getVisibility().setValue(
							Double.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetVisibilityUnits = (Spinner) view
				.findViewById(R.id.divelog_set_visibility_units);

		mDiveLogSetSurfaceTime = (EditText) view
				.findViewById(R.id.divelog_set_surface);
		mDiveLogSetSurfaceTime.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.setSurfaceTime(0);
				} else {
					mDiveLog.setSurfaceTime(Integer.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetWeightsValue = (EditText) view
				.findViewById(R.id.divelog_set_weights);
		mDiveLogSetWeightsValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mDiveLog.getWeightsRequired().setValue(0);
				} else {
					mDiveLog.getWeightsRequired().setValue(
							Double.valueOf(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetWeightsUnits = (Spinner) view
				.findViewById(R.id.divelog_set_weights_units);

		mDiveLogSetComments = (EditText) view
				.findViewById(R.id.divelog_set_comments);
		mDiveLogSetComments.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				mDiveLog.setComments(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDiveLogSetBuddies = (LinearLayout) view
				.findViewById(R.id.divelog_set_buddy_list);

		mDiveLogAddBuddy = (ImageButton) view
				.findViewById(R.id.divelog_buddy_add);
		mDiveLogAddBuddy.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DiveLogBuddy diveLogBuddy = new DiveLogBuddy(mDiveLog);
				addDiveBuddyView(diveLogBuddy, true);
				mDiveLog.getBuddies().add(diveLogBuddy);
			}
		});

		mDiveLogSetStops = (LinearLayout) view
				.findViewById(R.id.divelog_set_stop_list);

		mDiveLogAddStop = (ImageButton) view
				.findViewById(R.id.divelog_stop_add);
		mDiveLogAddStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DiveLogStop diveLogStop = new DiveLogStop(mDiveLog);
				addDiveStopView(diveLogStop, true);
				mDiveLog.getStops().add(diveLogStop);
			}
		});

		mDiveLogDayIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_day_indicator);
		mDiveLogDayIndicator.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mDiveLogDayIndicator.setVisibility(View.GONE);
				mDiveLogNightIndicator.setVisibility(View.VISIBLE);

				mDiveLog.setIsNight(true);
			}
		});

		mDiveLogNightIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_night_indicator);
		mDiveLogNightIndicator.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mDiveLogNightIndicator.setVisibility(View.GONE);
				mDiveLogDayIndicator.setVisibility(View.VISIBLE);

				mDiveLog.setIsNight(false);
			}
		});

		mDiveLogPhotoEnabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_photo_enabled_indicator);
		mDiveLogPhotoEnabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogPhotoEnabledIndicator.setVisibility(View.GONE);
						mDiveLogPhotoDisabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsPhotoVideo(false);
					}
				});

		mDiveLogPhotoDisabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_photo_disabled_indicator);
		mDiveLogPhotoDisabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogPhotoDisabledIndicator.setVisibility(View.GONE);
						mDiveLogPhotoEnabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsPhotoVideo(true);
					}
				});

		mDiveLogDeepEnabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_deep_enabled_indicator);
		mDiveLogDeepEnabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogDeepEnabledIndicator.setVisibility(View.GONE);
						mDiveLogDeepDisabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsDeep(false);
					}
				});

		mDiveLogDeepDisabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_deep_disabled_indicator);
		mDiveLogDeepDisabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogDeepDisabledIndicator.setVisibility(View.GONE);
						mDiveLogDeepEnabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsDeep(true);
					}
				});

		mDiveLogIceEnabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_ice_enabled_indicator);
		mDiveLogIceEnabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogIceEnabledIndicator.setVisibility(View.GONE);
						mDiveLogIceDisabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsIce(false);
					}
				});

		mDiveLogIceDisabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_ice_disabled_indicator);
		mDiveLogIceDisabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogIceDisabledIndicator.setVisibility(View.GONE);
						mDiveLogIceEnabledIndicator.setVisibility(View.VISIBLE);

						mDiveLog.setIsIce(true);
					}
				});

		mDiveLogCourseEnabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_course_enabled_indicator);
		mDiveLogCourseEnabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogCourseEnabledIndicator.setVisibility(View.GONE);
						mDiveLogCourseDisabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsCourse(false);
					}
				});

		mDiveLogCourseDisabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_course_disabled_indicator);
		mDiveLogCourseDisabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogCourseDisabledIndicator
								.setVisibility(View.GONE);
						mDiveLogCourseEnabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsCourse(true);
					}
				});

		mDiveLogInstructorEnabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_instructor_enabled_indicator);
		mDiveLogInstructorEnabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogInstructorEnabledIndicator
								.setVisibility(View.GONE);
						mDiveLogInstructorDisabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsInstructing(false);
					}
				});

		mDiveLogInstructorDisabledIndicator = (ImageButton) view
				.findViewById(R.id.divelog_item_set_instructor_disabled_indicator);
		mDiveLogInstructorDisabledIndicator
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mDiveLogInstructorDisabledIndicator
								.setVisibility(View.GONE);
						mDiveLogInstructorEnabledIndicator
								.setVisibility(View.VISIBLE);

						mDiveLog.setIsInstructing(true);
					}
				});

        // Update UI before setting edit events for units
		updateDiveLogUI();

        mDiveLogSetStartAirUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_pressure_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getStartAir().setUnits(newUnits);
                    mDiveLogSetStartAirValue.setText(String.valueOf(mDiveLog.getStartAir().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_STARTAIRUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mDiveLogSetEndAirUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_pressure_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getEndAir().setUnits(newUnits);
                    mDiveLogSetEndAirValue.setText(String.valueOf(mDiveLog.getEndAir().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_ENDAIRUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mDiveLogSetMaxDepthUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_depth_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getMaxDepth().setUnits(newUnits);
                    mDiveLogSetMaxDepthValue.setText(String.valueOf(mDiveLog.getMaxDepth().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_MAXDEPTHUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        mDiveLogSetAvgDepthUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_depth_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getAverageDepth().setUnits(newUnits);
                    mDiveLogSetAvgDepthValue.setText(String.valueOf(mDiveLog.getAverageDepth().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_AVGDEPTHUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mDiveLogSetAirTempUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_temp_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getSurfaceTemperature().setUnits(newUnits);
                    mDiveLogSetAirTempValue.setText(String.valueOf(mDiveLog.getSurfaceTemperature().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_AIRTEMPUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mDiveLogSetWaterTempUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_temp_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getWaterTemperature().setUnits(newUnits);
                    mDiveLogSetWaterTempValue.setText(String.valueOf(mDiveLog.getWaterTemperature().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_WATERTEMPUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mDiveLogSetVisibilityUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_depth_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getVisibility().setUnits(newUnits);
                    mDiveLogSetVisibilityValue.setText(String.valueOf(mDiveLog.getVisibility().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_VISIBILITYUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        mDiveLogSetWeightsUnits.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView,
                                       View v, int position, long id) {
                String newUnits = getResources().getStringArray(R.array.units_weight_array)[position];
                if (mDiveLog != null) {
                    mDiveLog.getWeightsRequired().setUnits(newUnits);
                    mDiveLogSetWeightsValue.setText(String.valueOf(mDiveLog.getWeightsRequired().getValue()));
                }

                mPrefs.edit().putString(DiveSiteManager.PREF_DIVELOG_WEIGHTUNITS, newUnits).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

		view.requestFocus();

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_DIVE_SITE && resultCode == Activity.RESULT_OK) {

			mDiveLog.setDiveSite((DiveSite) data.getParcelableExtra(DiveSiteManager.EXTRA_DIVE_SITE));
			long diveSiteLocalID = -1;
			long diveSiteOnlineID = -1;
			String diveSiteName = "";
			if (mDiveLog.getDiveSite() != null) {
				diveSiteLocalID = mDiveLog.getDiveSite().getLocalId();
				diveSiteOnlineID = mDiveLog.getDiveSite().getOnlineId();
				diveSiteName = mDiveLog.getDiveSite().getName();
			}

			if (diveSiteName != null) {
				mDiveLogSetDiveSite.setText(diveSiteName);
			}
			mDiveLog.setDiveSiteLocalId(diveSiteLocalID);
			mDiveLog.setDiveSiteOnlineId(diveSiteOnlineID);
		} else if (requestCode == CREATE_DIVE_SITE) {
			// Need to get new dive site from database when creating
			// Dive Site's ID should have been saved when button was clicked
			// (site created)
			mDiveLog.setDiveSite(mDiveSiteManager.getDiveSite(mDiveLog.getDiveSiteLocalId()));
            mDiveLog.setDiveSiteOnlineId(mDiveLog.getDiveSite().getOnlineId());

			mDiveLogSetDiveSite.setText(mDiveLog.getDiveSite().getName());
		} else if (requestCode == SELECT_DIVER_DIVERLIST
				&& resultCode == Activity.RESULT_OK
				&& mLastSelectedBuddyView != null) {
			long diverOnlineID = data.getLongExtra(
					DiveSiteManager.EXTRA_DIVER_ONLINE_ID, -1);
			String diverUsername = data
					.getStringExtra(DiveSiteManager.EXTRA_DIVER_USERNAME);

			DiveLogBuddy buddy = (DiveLogBuddy) mLastSelectedBuddyView.getTag();
			buddy.setDiverOnlineId(diverOnlineID);

			EditText diveLogBuddyUsername = (EditText) mLastSelectedBuddyView
					.findViewById(R.id.divelog_buddy_name_edit);
			diveLogBuddyUsername.setText(diverUsername);

			mLastSelectedBuddyView = null;
		} else if (requestCode == SELECT_DIVER_CONTACTS
				&& resultCode == Activity.RESULT_OK
				&& mLastSelectedBuddyView != null) {
			Uri contactData = data.getData();
			Cursor c = getActivity().getContentResolver().query(contactData,
					null, null, null, null);

			if (c.moveToFirst()) {
				String name = c
						.getString(c
								.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

				EditText diveLogBuddyUsername = (EditText) mLastSelectedBuddyView
						.findViewById(R.id.divelog_buddy_name_edit);
				diveLogBuddyUsername.setText(name);
			}

			mLastSelectedBuddyView = null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_divelog, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_divelog_save:
			// First check if entered dive site name exists and none assigned
			// If so, create it right away
			final ArrayList<DiveSite> diveSites;
			if (mDiveLog.getDiveSite() == null) {
				diveSites = mDiveSiteManager
						.getDiveSitesLikeName(mDiveLogSetDiveSite.getText()
								.toString().trim());

				// Add new dive site for creation
				DiveSite diveSite = new DiveSite(
						mDiveSiteManager.getLoggedInDiverId(),
						mDiveSiteManager.getLoggedInDiverUsername());
				diveSite.setName(mDiveLogSetDiveSite.getText().toString());
				diveSites.add(diveSite);
			} else {
				diveSites = null;
			}

			if (diveSites != null) {
				// Prompt user which dive site they want

				CharSequence[] diveSiteChoices = new CharSequence[diveSites
						.size()];
				for (int i = 0; i < diveSiteChoices.length - 1; i++) {
					if (diveSites.get(i).getFullLocation().isEmpty()) {
						diveSiteChoices[i] = diveSites.get(i).getName();
					} else {
						diveSiteChoices[i] = String.format(getResources()
								.getString(R.string.divesite_in), diveSites
								.get(i).getName(), diveSites.get(i)
								.getFullLocation());
					}
				}
				// Add one for new dive site
				diveSiteChoices[diveSiteChoices.length - 1] = getResources()
						.getString(R.string.new_dive_site_name);

				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.select_saved_site)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setSingleChoiceItems(diveSiteChoices, -1,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int item) {
										// Allow user to create dive site if
										// they selected to
										if (diveSites.get(item).getLocalId() == -1) {

											mDiveSiteManager.insertDiveSite(diveSites.get(item));
											mDiveLog.setDiveSiteLocalId(diveSites.get(item).getLocalId());

											// Open Dive Site in Edit Mode
											mPrefs.edit().putBoolean(DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE, true).commit();
											Intent intent = new Intent(
													DiveLogFragment.this
															.getActivity(),
													DiveSiteActivity.class);
											intent.putExtra(
													DiveSiteManager.EXTRA_DIVE_SITE,
													diveSites.get(item));
											intent.putExtra(
													DiveLogActivity.EXTRA_DIVE_LOG,
													mDiveLog);
											startActivityForResult(intent,
													CREATE_DIVE_SITE);
										} else {
                                            mDiveLog.setDiveSite(diveSites.get(item));
											mDiveLog.setDiveSiteLocalId(diveSites.get(item).getLocalId());
											mDiveLog.setDiveSiteOnlineId(diveSites.get(item).getOnlineId());
	
											// Save dive log and return to list
											saveDiveLog();
	
											Intent data = new Intent();
											data.putExtra(DiveLogActivity.EXTRA_DIVE_LOG, mDiveLog);
											if (getActivity().getParent() == null) {
												getActivity().setResult(Activity.RESULT_OK, data);
											} else {
												getActivity().getParent().setResult(Activity.RESULT_OK, data);
											}
											
											getActivity().finish();
										}
										
										dialog.dismiss();
									}
								}).show();
			} else {
				// Save dive log and return to list
				saveDiveLog();

				Intent data = new Intent();
				data.putExtra(DiveLogActivity.EXTRA_DIVE_LOG, mDiveLog);
				if (getActivity().getParent() == null) {
					getActivity().setResult(Activity.RESULT_OK, data);
				} else {
					getActivity().getParent().setResult(Activity.RESULT_OK, data);
				}
				getActivity().finish();
			}

			return true;

		case R.id.menu_item_divelog_cancel:
			// Cancel dive log and return to list
			getActivity().finish();

			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void saveDiveLog() {
		// Save Dive Site first if not local
		if (mDiveLog.getDiveSite().getLocalId() == -1) {
			mDiveSiteManager.saveDiveSite(mDiveLog.getDiveSite());
			mDiveLog.setDiveSiteLocalId(mDiveLog.getDiveSite().getLocalId());
		}
		
		mDiveLog.setPublished(false);
		mDiveSiteManager.saveDiveLog(mDiveLog);
		
		// Delete marked dive log buddies and stops
		for (int i = 0; i < mDiveLogBuddiesDelete.size(); i++) {
			mDiveSiteManager.deleteDiveLogBuddy(mDiveLogBuddiesDelete.get(i)
					.getLocalId());
		}

		for (int i = 0; i < mDiveLogStopsDelete.size(); i++) {
			mDiveSiteManager.deleteDiveLogStop(mDiveLogStopsDelete.get(i)
					.getLocalId());
		}
	}

	protected void updateDiveLogUI() {
		if (mDiveLog != null) {

			if (mDiveLog.getDiveSite() != null) {
				mDiveLogSetDiveSite.setText(mDiveLog.getDiveSite().getName());
			}

			if (mDiveLog.getLocalId() == -1) {
				mDiveLog.setTimestamp(new Date());
			}

			mDiveLogSetTimestamp.setText(mDiveLog.getTimestamp().toString());

			mDiveLogSetRating.setRating((float) mDiveLog.getRating());

			mDiveLogSetDiveTime.setText(String.valueOf(mDiveLog.getDiveTime()));

			mDiveLogSetStartPressure.setSelection(FindPositionInArray(String
					.valueOf(mDiveLog.getStartPressure()), getResources()
					.getStringArray(R.array.alphabet)));

			mDiveLogSetEndPressure.setSelection(FindPositionInArray(String
					.valueOf(mDiveLog.getEndPressure()), getResources()
					.getStringArray(R.array.alphabet)));

            mDiveLogSetStartAirValue.setText(String.valueOf(mDiveLog.getStartAir().getValue()));

            mDiveLogSetStartAirUnits.setSelection(FindPositionInArray(
                    String.valueOf(mDiveLog.getStartAir().getUnits()),
                    getResources().getStringArray(R.array.units_pressure_array)));

            mDiveLogSetEndAirValue.setText(String.valueOf(mDiveLog.getEndAir().getValue()));

            mDiveLogSetEndAirUnits.setSelection(FindPositionInArray(
                    String.valueOf(mDiveLog.getEndAir().getUnits()),
                    getResources().getStringArray(R.array.units_pressure_array)));

			mDiveLogSetMaxDepthValue.setText(String.valueOf(mDiveLog
					.getMaxDepth().getValue()));

			mDiveLogSetMaxDepthUnits.setSelection(FindPositionInArray(
					String.valueOf(mDiveLog.getMaxDepth().getUnits()),
					getResources().getStringArray(R.array.units_depth_array)));

			mDiveLogSetAvgDepthValue.setText(String.valueOf(mDiveLog
					.getAverageDepth().getValue()));

			mDiveLogSetAvgDepthUnits.setSelection(FindPositionInArray(
					String.valueOf(mDiveLog.getAverageDepth().getUnits()),
					getResources().getStringArray(R.array.units_depth_array)));

			mDiveLogSetAirTempValue.setText(String.valueOf(mDiveLog
					.getSurfaceTemperature().getValue()));

			mDiveLogSetAirTempUnits
					.setSelection(FindPositionInArray(
							String.valueOf(mDiveLog.getSurfaceTemperature()
									.getUnits()),
							getResources().getStringArray(
									R.array.units_temp_array)));

			mDiveLogSetWaterTempValue.setText(String.valueOf(mDiveLog
					.getWaterTemperature().getValue()));

			mDiveLogSetWaterTempUnits.setSelection(FindPositionInArray(
					String.valueOf(mDiveLog.getWaterTemperature().getUnits()),
					getResources().getStringArray(R.array.units_temp_array)));

			mDiveLogSetVisibilityValue.setText(String.valueOf(mDiveLog
					.getVisibility().getValue()));
			mDiveLogSetVisibilityUnits.setSelection(FindPositionInArray(
					String.valueOf(mDiveLog.getVisibility().getUnits()),
					getResources().getStringArray(R.array.units_depth_array)));

			mDiveLogSetWeightsValue.setText(String.valueOf(mDiveLog
					.getWeightsRequired().getValue()));
			mDiveLogSetWeightsValue.setSelection(FindPositionInArray(
					String.valueOf(mDiveLog.getWeightsRequired().getUnits()),
					getResources().getStringArray(R.array.units_weight_array)));

			mDiveLogSetSurfaceTime.setText(String.valueOf(mDiveLog
					.getSurfaceTime()));

            mDiveLogSetGasMixValue.setText(mDiveLog.getAirType());

			mDiveLogSetComments.setText(mDiveLog.getComments());

			if (mDiveLog.isNight()) {
				mDiveLogDayIndicator.setVisibility(View.GONE);
				mDiveLogNightIndicator.setVisibility(View.VISIBLE);
			} else {
				mDiveLogDayIndicator.setVisibility(View.VISIBLE);
				mDiveLogNightIndicator.setVisibility(View.GONE);
			}

			if (mDiveLog.isPhotoVideo()) {
				mDiveLogPhotoEnabledIndicator.setVisibility(View.VISIBLE);
				mDiveLogPhotoDisabledIndicator.setVisibility(View.GONE);
			} else {
				mDiveLogPhotoEnabledIndicator.setVisibility(View.GONE);
				mDiveLogPhotoDisabledIndicator.setVisibility(View.VISIBLE);
			}

			if (mDiveLog.isDeep()) {
				mDiveLogDeepEnabledIndicator.setVisibility(View.VISIBLE);
				mDiveLogDeepDisabledIndicator.setVisibility(View.GONE);
			} else {
				mDiveLogDeepEnabledIndicator.setVisibility(View.GONE);
				mDiveLogDeepDisabledIndicator.setVisibility(View.VISIBLE);
			}

			if (mDiveLog.isIce()) {
				mDiveLogIceEnabledIndicator.setVisibility(View.VISIBLE);
				mDiveLogIceDisabledIndicator.setVisibility(View.GONE);
			} else {
				mDiveLogIceEnabledIndicator.setVisibility(View.GONE);
				mDiveLogIceDisabledIndicator.setVisibility(View.VISIBLE);
			}

			if (mDiveLog.isCourse()) {
				mDiveLogCourseEnabledIndicator.setVisibility(View.VISIBLE);
				mDiveLogCourseDisabledIndicator.setVisibility(View.GONE);
			} else {
				mDiveLogCourseEnabledIndicator.setVisibility(View.GONE);
				mDiveLogCourseDisabledIndicator.setVisibility(View.VISIBLE);
			}

			if (mDiveLog.isInstructing()) {
				mDiveLogInstructorEnabledIndicator.setVisibility(View.VISIBLE);
				mDiveLogInstructorDisabledIndicator.setVisibility(View.GONE);
			} else {
				mDiveLogInstructorEnabledIndicator.setVisibility(View.GONE);
				mDiveLogInstructorDisabledIndicator.setVisibility(View.VISIBLE);
			}
			
			mDiveLogSetBuddies.removeAllViews();
			for (int i = 0; i < mDiveLog.getBuddies().size(); i++) {
				addDiveBuddyView(mDiveLog.getBuddies().get(i), false);
			}
			
			mDiveLogSetStops.removeAllViews();
			for (int i = 0; i < mDiveLog.getStops().size(); i++) {
				addDiveStopView(mDiveLog.getStops().get(i), false);
			}
		}
	}

	private View addDiveBuddyView(final DiveLogBuddy diveLogBuddy, Boolean requestFocus) {
		// Inflate and add buddy item view
        View view = null;
        if (getActivity() != null) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.divelog_buddy_edit_item, null);
            view.setTag(diveLogBuddy);
            mDiveLogSetBuddies.addView(view);

            ImageButton diverRemove = (ImageButton) view
                    .findViewById(R.id.divelog_buddy_remove);
            diverRemove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Remove view from buddy view list and dive log's buddy list
                    mDiveLogSetBuddies.requestFocus();
                    mDiveLogSetBuddies.removeView((View) v.getParent());

                    DiveLogBuddy diveLogBuddy = (DiveLogBuddy) ((View) v
                            .getParent()).getTag();
                    if (diveLogBuddy.getLocalId() != -1) {
                        mDiveLogBuddiesDelete.add(diveLogBuddy);
                    }
                    mDiveLog.getBuddies().remove(diveLogBuddy);
                }
            });

            ImageButton diverButtonOptions = (ImageButton) view
                    .findViewById(R.id.divelog_buddy_button);
            mDiveLogBuddyImages.put(diveLogBuddy, diverButtonOptions);
            diverButtonOptions.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    DiveLogBuddy buddy = (DiveLogBuddy) ((View) v.getParent())
                            .getTag();

                    // Ask user what they want to do with radio button choices
                    CharSequence[] buddyOptionChoices = null;
                    if (buddy.getDiverOnlineId() != -1) {
                        buddyOptionChoices = new CharSequence[BUDDY_BUTTON_OPTIONS_COUNT];
                        buddyOptionChoices[BUDDY_BUTTON_OPTION_VIEW_PROFILE] = getResources()
                                .getString(R.string.divelog_buddy_view_profile);
                    } else {
                        buddyOptionChoices = new CharSequence[BUDDY_BUTTON_OPTIONS_COUNT - 1];
                    }

                    buddyOptionChoices[BUDDY_BUTTON_OPTION_DIVETHESITE_SELECT] = getResources()
                            .getString(R.string.divelog_buddy_divethesite_select);
                    buddyOptionChoices[BUDDY_BUTTON_OPTION_CONTACTS_SELECT] = getResources()
                            .getString(R.string.divelog_buddy_contacts_select);

                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setSingleChoiceItems(buddyOptionChoices, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int item) {
                                            Intent intent;

                                            switch (item) {
                                                case BUDDY_BUTTON_OPTION_DIVETHESITE_SELECT:
                                                    // Show diver list fragment for
                                                    // diver selection,
                                                    // save diver's username and diver
                                                    // ID
                                                    mLastSelectedBuddyView = (View) v
                                                            .getParent();
                                                    intent = new Intent(
                                                            DiveLogFragment.this
                                                                    .getActivity(),
                                                            DiverListActivity.class);
                                                    intent.putExtra(
                                                            DiveLogActivity.EXTRA_SET_TO_DIVELOG,
                                                            true);
                                                    startActivityForResult(intent,
                                                            SELECT_DIVER_DIVERLIST);

                                                    break;
                                                case BUDDY_BUTTON_OPTION_CONTACTS_SELECT:
                                                    // Show contacts for contact
                                                    // selection,
                                                    // save contact's first name and
                                                    // last name
                                                    mLastSelectedBuddyView = (View) v
                                                            .getParent();
                                                    intent = new Intent(
                                                            Intent.ACTION_PICK,
                                                            ContactsContract.Contacts.CONTENT_URI);
                                                    startActivityForResult(intent,
                                                            SELECT_DIVER_CONTACTS);

                                                    break;
                                                case BUDDY_BUTTON_OPTION_VIEW_PROFILE:
                                                    // Open Diver Activity for the buddy
                                                    DiveLogBuddy buddy = (DiveLogBuddy) ((View) v
                                                            .getParent()).getTag();

                                                    intent = new Intent(
                                                            DiveLogFragment.this
                                                                    .getActivity(),
                                                            DiverActivity.class);
                                                    intent.putExtra(
                                                            DiverActivity.EXTRA_DIVER_ID,
                                                            buddy.getDiverOnlineId());
                                                    startActivity(intent);

                                                    break;
                                            }

                                            dialog.dismiss();
                                        }
                                    }).show();
                }
            });

            // Get Diver and Picture for Dive Log Buddy if an id and URL is
            // available
            if (diveLogBuddy.getDiverOnlineId() != -1) {
                DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseUser = new DiveSiteOnlineDatabaseLink(getActivity());
                diveSiteOnlineDatabaseUser
                        .setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                            @Override
                            public void onOnlineDiveDataRetrievedComplete(
                                    ArrayList<Object> resultList, String message,
                                    Boolean isError) {
                                if (resultList.size() > 0) {
                                    // First save diver so another list item can use
                                    // it
                                    Diver diver = (Diver) resultList.get(0);

                                    // Now get bitmap profile image for diver
                                    ImageView diverButton = mDiveLogBuddyImages.get(diveLogBuddy);
                                    LoadOnlineImageTask task = new LoadOnlineImageTask(diverButton) {

                                        @Override
                                        protected void onPostExecute(Bitmap result) {
                                            super.onPostExecute(result);

                                            mDiveLogBuddyImages.remove(diveLogBuddy);
                                        }

                                    };
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
                        String.valueOf(diveLogBuddy.getDiverOnlineId()), "", "");
            }

            final EditText diverUsernameEdit =
                    (EditText) view.findViewById(R.id.divelog_buddy_name_edit);
            diverUsernameEdit.setText(diveLogBuddy.getDiverUsername());
            diverUsernameEdit.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    DiveLogBuddy diveLogBuddy = (DiveLogBuddy) ((View) diverUsernameEdit
                            .getParent()).getTag();
                    if (diveLogBuddy != null) {
                        diveLogBuddy.setDiverUsername(s.toString());
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                }

            });

            if (requestFocus) {
                diverUsernameEdit.requestFocus();
            }
        }

		return view;
	}

	private View addDiveStopView(DiveLogStop diveLogStop, Boolean requestFocus) {
		// Inflate and add stop item view
        View view = null;
        if (getActivity() != null) {
            LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.divelog_stop_edit_item, null);
            mDiveLogSetStops.addView(view);
            view.setTag(diveLogStop);

            ImageButton stopRemove = (ImageButton) view
                    .findViewById(R.id.divelog_stop_remove);
            stopRemove.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Remove view from buddy view list and dive log's buddy list
                    mDiveLogSetStops.requestFocus();
                    mDiveLogSetStops.removeView((View) v.getParent().getParent());

                    DiveLogStop diveLogStop = (DiveLogStop) ((View) v.getParent()
                            .getParent()).getTag();
                    if (diveLogStop.getLocalId() != -1) {
                        mDiveLogStopsDelete.add(diveLogStop);
                    }
                    mDiveLog.getStops().remove(diveLogStop);
                }
            });

            final EditText stopTimeEdit = (EditText) view
                    .findViewById(R.id.divelog_stop_time);
            stopTimeEdit.setText(String.valueOf(diveLogStop.getTime()));
            stopTimeEdit.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    DiveLogStop logStop = (DiveLogStop) ((View) stopTimeEdit
                            .getParent().getParent()).getTag();
                    if (logStop != null) {
                        if (s.toString().trim().isEmpty()) {
                            logStop.setTime(0);
                        } else {
                            logStop.setTime(Integer.valueOf(s.toString().trim()));
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                }

            });

            final EditText stopTimeDepthValue = (EditText) view
                    .findViewById(R.id.divelog_stop_depth);
            stopTimeDepthValue.setText(String.valueOf(diveLogStop.getDepth().getValue()));
            stopTimeDepthValue.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    DiveLogStop logStop = (DiveLogStop) ((View) stopTimeEdit
                            .getParent().getParent()).getTag();
                    if (logStop != null) {
                        if (s.toString().trim().isEmpty()) {
                            logStop.getDepth().setValue(0);
                        } else {
                            logStop.getDepth().setValue(Double.valueOf(s.toString()));
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                }

            });

            final Spinner stopTimeDepthUnits = (Spinner) view
                    .findViewById(R.id.divelog_stop_depth_units);
            stopTimeDepthUnits.setSelection(FindPositionInArray(diveLogStop.getDepth().getUnits(),
                    getResources().getStringArray(R.array.units_depth_array)));
            stopTimeDepthUnits
                    .setOnItemSelectedListener(new OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parentView,
                                                   View v, int position, long id) {
                            DiveLogStop logStop = (DiveLogStop) ((View) stopTimeEdit
                                    .getParent().getParent()).getTag();
                            if (logStop != null) {
                                logStop.getDepth()
                                        .setUnits(
                                                getResources().getStringArray(
                                                        R.array.units_depth_array)[position]);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                        }

                    });

            if (requestFocus) {
                stopTimeEdit.requestFocus();
            }
        }

		return view;
	}

	private int FindPositionInArray(String value, String[] array) {
		int position = -1;

		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) {
				position = i;
				break;
			}
		}

		return position;
	}

	private class PickDiveLogTimestampDate implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mDiveLogTimestampDateDialog.hide();

			// Set year, month, day
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDiveLog.getTimestamp());
			calendar.set(year, monthOfYear, dayOfMonth);
			mDiveLog.setTimestamp(calendar.getTime());

			// Now show time picker
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(mDiveLog.getTimestamp());

			mDiveLogTimestampTimeDialog.updateTime(
					currentCalendar.get(Calendar.HOUR_OF_DAY),
					currentCalendar.get(Calendar.MINUTE));

			mDiveLogTimestampTimeDialog.setTitle(dateFormat.format(currentCalendar.getTime()));
			mDiveLogTimestampTimeDialog.show();
		}

	}

	private class PickDiveLogTimestampTime implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mDiveLogTimestampTimeDialog.hide();

			// Set hour, minute
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mDiveLog.getTimestamp());

			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);

			mDiveLog.setTimestamp(calendar.getTime());
			mDiveLogSetTimestamp.setText(mDiveLog.getTimestamp().toString());
		}
	}
}
