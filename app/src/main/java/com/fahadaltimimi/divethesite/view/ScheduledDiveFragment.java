package com.fahadaltimimi.divethesite.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.ScheduledDiveDiveSite;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.fahadaltimimi.divethesite.model.ScheduledDiveUser;

public class ScheduledDiveFragment extends Fragment {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy");

	private static final String TAG = "ScheduledDiveFragment";

	public static final String ARG_SCHEDULEDDIVE = "SCHEDULEDDIVE_LOCAL_ID";
	public static final String ARG_SET_TO_SCHEDULEDDIVE = "SET_TO_SCHEDULEDDIVE";

	public static final int SELECT_DIVE_SITE = 0;
	public static final int CREATE_DIVE_SITE = 1;

	protected DiveSiteManager mDiveSiteManager;

	private ScheduledDive mScheduledDive = null;
	private DiveSite mDiveSiteAdding = null;
	private ArrayList<ScheduledDiveDiveSite> mScheduledDiveDiveSitesDelete =
			new ArrayList<ScheduledDiveDiveSite>();

	private EditText mScheduledDiveTitle;
	private Button mScheduledDiveSetTimestamp;
	private EditText mScheduledDiveSetDiveSite;
	private ImageButton mScheduledDiveDiveSiteSearch;
	private ImageButton mScheduledDiveDiveSiteAdd;
	private EditText mScheduledDiveSetComments;
	private LinearLayout mScheduledDiveDiveSiteList;

	private DatePickerDialog mScheduledDiveTimestampDateDialog = null;
	private TimePickerDialog mScheduledDiveTimestampTimeDialog = null;

	public static ScheduledDiveFragment newInstance(ScheduledDive scheduledDive, DiveSite diveSite) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_SCHEDULEDDIVE, scheduledDive);
        args.putParcelable(DiveSiteTabFragment.ARG_DIVESITE, diveSite);
		ScheduledDiveFragment rf = new ScheduledDiveFragment();
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
			mScheduledDive = args.getParcelable(ARG_SCHEDULEDDIVE);
		}

		if (mScheduledDive == null) {
			mScheduledDive = new ScheduledDive();
			mScheduledDive.setSubmitterId(mDiveSiteManager.getLoggedInDiverId());

            DiveSite diveSite = args.getParcelable(DiveSiteTabFragment.ARG_DIVESITE);
            if (diveSite != null) {
                ScheduledDiveDiveSite scheduledDiveDiveSite = new ScheduledDiveDiveSite(diveSite);
                mScheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
            }
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_scheduleddive, parent, false);

		mScheduledDiveTitle = (EditText) view.findViewById(R.id.scheduleddive_set_title);
		mScheduledDiveTitle.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				mScheduledDive.setTitle(s.toString().trim());
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
		
		mScheduledDiveTimestampDateDialog = new DatePickerDialog(getActivity(),
				new PickScheduledDiveTimestampDate(), 0, 0, 0);
		mScheduledDiveTimestampDateDialog.getDatePicker().init(0, 0, 0, new DatePicker.OnDateChangedListener() {
			
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthOfYear, dayOfMonth);

				mScheduledDiveTimestampDateDialog.setTitle(dateFormat.format(calendar.getTime()));
			}
		}); 

		mScheduledDiveTimestampTimeDialog = new TimePickerDialog(getActivity(),
				new PickScheduledDiveTimestampTime(), 0, 0, true);

		mScheduledDiveSetTimestamp = (Button) view
				.findViewById(R.id.scheduleddive_set_timestamp);
		mScheduledDiveSetTimestamp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Ask user first for a date selection, then time selection
				Calendar currentCalendar = Calendar.getInstance();
				currentCalendar.setTime(mScheduledDive.getTimestamp());

				mScheduledDiveTimestampDateDialog.updateDate(
						currentCalendar.get(Calendar.YEAR),
						currentCalendar.get(Calendar.MONTH),
						currentCalendar.get(Calendar.DATE));

				mScheduledDiveTimestampDateDialog.show();
			}
		});

		mScheduledDiveSetComments = (EditText) view
				.findViewById(R.id.scheduleddive_set_comments);
		mScheduledDiveSetComments.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				mScheduledDive.setComment(s.toString());
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
		
		mScheduledDiveSetDiveSite = 
				(EditText) view.findViewById(R.id.scheduleddive_set_divesite);

		mScheduledDiveSetDiveSite.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// Reset dive site if changed
				if (mDiveSiteAdding != null && !mDiveSiteAdding.getName().equals(s.toString())) {
					mDiveSiteAdding = null;
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

		mScheduledDiveDiveSiteSearch = 
				(ImageButton) view.findViewById(R.id.scheduleddive_divesite_search);
		mScheduledDiveDiveSiteSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Open Dive Site List with filter set to text entered
				getActivity()
						.getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE)
						.edit()
						.putString(
								DiveSiteManager.PREF_FILTER_DIVELOG_DIVESITE_TITLE,
								mScheduledDiveSetDiveSite.getText().toString())
						.commit();

				Intent intent = new Intent(ScheduledDiveFragment.this.getActivity(),
						DiveSiteListActivity.class);
				intent.putExtra(DiveLogActivity.EXTRA_SET_TO_DIVELOG, true);
				startActivityForResult(intent, SELECT_DIVE_SITE);
			}
		});
		
		mScheduledDiveDiveSiteAdd = 
				(ImageButton) view.findViewById(R.id.scheduleddive_divesite_add);
		mScheduledDiveDiveSiteAdd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final ArrayList<DiveSite> diveSites;
				if (mDiveSiteAdding == null) {
					diveSites = 
							mDiveSiteManager.getDiveSitesLikeName(
									mScheduledDiveSetDiveSite.getText().toString().trim());

					// Add new dive site for creation
					DiveSite diveSite = new DiveSite(
							mDiveSiteManager.getLoggedInDiverId(),
							mDiveSiteManager.getLoggedInDiverUsername());
					diveSite.setName(mScheduledDiveSetDiveSite.getText().toString());
					diveSites.add(diveSite);
				} else {
					diveSites = null;
				}
				
				if (diveSites != null) {
					// Prompt user which dive site they want
					CharSequence[] diveSiteChoices = 
							new CharSequence[diveSites.size()];
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
					diveSiteChoices[diveSiteChoices.length - 1] = 
							getResources().getString(R.string.new_dive_site_name);

					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.select_saved_site)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setSingleChoiceItems(diveSiteChoices, -1,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int item) {
											// Allow user to create dive site if they selected to
											if (diveSites.get(item).getLocalId() == -1) {

												mDiveSiteManager.insertDiveSite(diveSites.get(item));
                                                mDiveSiteAdding = diveSites.get(item);

												// Open Dive Site in Edit Mode
												getActivity().getSharedPreferences(
																DiveSiteManager.PREFS_FILE,
																Context.MODE_PRIVATE)
														.edit()
														.putBoolean(
																DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE,
																true).commit();
												Intent intent = new Intent(
														ScheduledDiveFragment.this
																.getActivity(),
														DiveSiteActivity.class);
												intent.putExtra(
														DiveSiteManager.EXTRA_DIVE_SITE,
														diveSites.get(item));
												startActivityForResult(intent,
														CREATE_DIVE_SITE);
											} else {
												// Create scheduled dive for dive site
												ScheduledDiveDiveSite scheduledDiveDiveSite = 
														new ScheduledDiveDiveSite(diveSites.get(item));
												addScheduledDiveDiveSiteView(scheduledDiveDiveSite);
												mScheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
											}
											
											dialog.dismiss();
										}
									}).show();
				} else {
					// Create scheduled dive for dive site
					ScheduledDiveDiveSite scheduledDiveDiveSite = 
							new ScheduledDiveDiveSite(mDiveSiteAdding);
					addScheduledDiveDiveSiteView(scheduledDiveDiveSite);
					mScheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
				}
			}
		});

		mScheduledDiveDiveSiteList = 
				(LinearLayout) view.findViewById(R.id.scheduleddive_site_list);

		updateScheduledDiveUI();

		view.requestFocus();

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_DIVE_SITE && resultCode == Activity.RESULT_OK) {
			mDiveSiteAdding = 
					((DiveSite) data.getParcelableExtra(DiveSiteManager.EXTRA_DIVE_SITE));
			
			if (mDiveSiteAdding != null && mDiveSiteAdding.getName() != null) {
				mScheduledDiveSetDiveSite.setText(mDiveSiteAdding.getName());
			}
			
			ScheduledDiveDiveSite scheduledDiveDiveSite = 
					new ScheduledDiveDiveSite(mDiveSiteAdding);
			addScheduledDiveDiveSiteView(scheduledDiveDiveSite);
			mScheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
		} else if (requestCode == CREATE_DIVE_SITE) {
			// Need to get new dive site from database when creating
			// Dive Site's ID should have been saved when button was clicked
			// (site created)
			mDiveSiteAdding = 
					mDiveSiteManager.getDiveSite(mDiveSiteAdding.getLocalId());
			mScheduledDiveSetDiveSite.setText(mDiveSiteAdding.getName());
			
			ScheduledDiveDiveSite scheduledDiveDiveSite = 
					new ScheduledDiveDiveSite(mDiveSiteAdding);
			addScheduledDiveDiveSiteView(scheduledDiveDiveSite);
			mScheduledDive.getScheduledDiveDiveSites().add(scheduledDiveDiveSite);
		} 
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_scheduleddive, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_scheduleddive_save:
			// Save scheduled dive and return to list
			saveScheduledDive();

			Intent data = new Intent();
			data.putExtra(DiveLogActivity.EXTRA_DIVE_LOG, mScheduledDive);
			if (getActivity().getParent() == null) {
				getActivity().setResult(Activity.RESULT_OK, data);
			} else {
				getActivity().getParent().setResult(Activity.RESULT_OK, data);
			}
			
			getActivity().finish();

			return true;

		case R.id.menu_item_scheduleddive_cancel:
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

	public void saveScheduledDive() {
		// Save Dive Sites first if not local
		for (int i = 0; i < mScheduledDive.getScheduledDiveDiveSites().size(); i++) {
			DiveSite diveSite = mScheduledDive.getScheduledDiveDiveSites().get(i).getDiveSite();
			if (diveSite.getLocalId() == -1) {
				mDiveSiteManager.saveDiveSite(diveSite);
				mScheduledDive.getScheduledDiveDiveSites().get(i).setDiveSiteLocalId(diveSite.getLocalId());
			}
		}
		
		// If this is a new scheduled dive, add creator as attending user
		if (mScheduledDive.getLocalId() == -1) {
			ScheduledDiveUser scheduledDiveUser = new ScheduledDiveUser();
			scheduledDiveUser.setUserId(mDiveSiteManager.getLoggedInDiverId());
			scheduledDiveUser.setAttendState(ScheduledDiveUser.AttendState.ATTENDING);
			mScheduledDive.getScheduledDiveUsers().add(scheduledDiveUser);
		}
		
		mScheduledDive.setPublished(false);
		mDiveSiteManager.saveScheduledDive(mScheduledDive);
		
		// Delete marked scheduled dive dive site and users
		for (int i = 0; i < mScheduledDiveDiveSitesDelete.size(); i++) {
			mDiveSiteManager.deleteScheduledDiveDiveSites(mScheduledDiveDiveSitesDelete.get(i)
					.getLocalId());
		}
	}

	protected void updateScheduledDiveUI() {
		if (mScheduledDive != null) {

			if (mScheduledDive.getLocalId() == -1) {
				mScheduledDive.setTimestamp(new Date());
			}

			mScheduledDiveTitle.setText(mScheduledDive.getTitle());
			mScheduledDiveSetTimestamp.setText(mScheduledDive.getTimestamp().toString());
			mScheduledDiveSetComments.setText(mScheduledDive.getComment());
			
			mScheduledDiveDiveSiteList.removeAllViews();
			for (int i = 0; i < mScheduledDive.getScheduledDiveDiveSites().size(); i++) {
				addScheduledDiveDiveSiteView(mScheduledDive.getScheduledDiveDiveSites().get(i));
			}
		}
	}

	private View addScheduledDiveDiveSiteView(final ScheduledDiveDiveSite scheduledDiveDiveSite) {
		// Inflate and add item view
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.scheduleddive_site_item, null);
		view.setTag(scheduledDiveDiveSite);
		mScheduledDiveDiveSiteList.addView(view);

		ImageButton siteRemove = 
				(ImageButton) view.findViewById(R.id.scheduleddive_site_remove);
		siteRemove.setVisibility(View.VISIBLE);
		siteRemove.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mScheduledDiveDiveSiteList.requestFocus();
				mScheduledDiveDiveSiteList.removeView((View) v.getParent());

				ScheduledDiveDiveSite scheduledDiveDiveSite = 
						(ScheduledDiveDiveSite) ((View) v.getParent()).getTag();
				if (scheduledDiveDiveSite.getLocalId() != -1) {
					mScheduledDiveDiveSitesDelete.add(scheduledDiveDiveSite);
				}
				mScheduledDive.getScheduledDiveDiveSites().remove(scheduledDiveDiveSite);
			}
		});

		if (scheduledDiveDiveSite.getDiveSite() != null) {
			TextView siteName = (TextView) view.findViewById(R.id.scheduleddive_site_name);
			siteName.setText(scheduledDiveDiveSite.getDiveSite().getName());
			
			TextView siteLocation = (TextView) view.findViewById(R.id.scheduleddive_site_location);
			siteLocation.setText(scheduledDiveDiveSite.getDiveSite().getFullLocation());
		}
		
		TextView siteVoteCount = (TextView) view.findViewById(R.id.scheduleddive_site_vote_count);
		siteVoteCount.setVisibility(View.GONE);
		
		mDiveSiteAdding = null;
		mScheduledDiveSetDiveSite.setText("");

		return view;
	}

	private class PickScheduledDiveTimestampDate implements DatePickerDialog.OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mScheduledDiveTimestampDateDialog.hide();

			// Set year, month, day
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mScheduledDive.getTimestamp());
			calendar.set(year, monthOfYear, dayOfMonth);
			mScheduledDive.setTimestamp(calendar.getTime());

			// Now show time picker
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTime(mScheduledDive.getTimestamp());

			mScheduledDiveTimestampTimeDialog.updateTime(
					currentCalendar.get(Calendar.HOUR_OF_DAY),
					currentCalendar.get(Calendar.MINUTE));

			mScheduledDiveTimestampTimeDialog.setTitle(dateFormat.format(currentCalendar.getTime()));
			mScheduledDiveTimestampTimeDialog.show();
		}

	}

	private class PickScheduledDiveTimestampTime implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mScheduledDiveTimestampTimeDialog.hide();

			// Set hour, minute
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mScheduledDive.getTimestamp());

			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);

			mScheduledDive.setTimestamp(calendar.getTime());
			mScheduledDiveSetTimestamp.setText(mScheduledDive.getTimestamp().toString());
		}
	}
}
