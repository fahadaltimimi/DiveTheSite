package com.fahadaltimimi.divethesite.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.DiverCertification;
import com.fahadaltimimi.view.FAMapView;
import com.fahadaltimimi.view.ObservableScrollView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DiverProfileFragment extends DiverPageFragment {

	private static final String TAG = "DiverPage1Fragment";

	public static final int REQUEST_PICK_PROFILE_IMAGE = 0;
    private static final int REQUEST_NEW_DIVESITE = 0;

    private static final int MAPVIEW_HEIGHT_BUFFER = 200;
    private static final double EARTHRADIUS = 6366198;

	private long mDiverID = -1;
	private Diver mDiver = null;
	private Boolean mEditMode;

	private ImageView mDiverProfileImage;
	private View mDiverInfoProgressContainer, mDiverProfileImageProgressContainer;
	private View mDiverInfoViewContainer;
	private View mDiverInfoEditContainer;
	private View mDiverInfoCertificationContainer;
	private TextView mDiverName;
	private TextView mDiverEmail;
	private TextView mDiverCityStateCountry;
	private TextView mDiverBio;
    private TextView mDiverCertsLabel;
	private LinearLayout mDiverCerts;
	private ImageButton mDiverProfileImageEdit;
	private Button mDiverProfileNoImageEdit;
	private EditText mDiverUsernameEdit;
	private EditText mDiverFirstNameEdit;
	private EditText mDiverLastNameEdit;
	private EditText mDiverEmailEdit;
	private EditText mDiverCityEdit;
	private EditText mDiverProvinceEdit;
	private EditText mDiverCountryEdit;
	private EditText mDiverBioEdit;
    private TextView mDiverInfoUnavailable;

	private String mProfileNewImageFilePath = null;

	private View mLastEmptyCertificationView;
	private ProgressDialog mProgressDialog;

    private View mMapLoadingProgress;

	private Bitmap mProfileImage = null;
	private Bitmap mProfileImageEdit = null;

    private View mMapContainer;
    private TextView mLogMapLabel;
    private FAMapView mMapView;
    private GoogleMap mGoogleMap;
    private ImageView mMapViewSnapShot;

    protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;
    private Boolean mRefreshingOnlineDiveLogs = false;

    private HashMap<DiveSite, Marker> mDiveSiteMarkers;
    LatLngBounds.Builder mLatLngBuilder;

	private class CertificationFieldTextWatcher implements TextWatcher {

		private View mParentView;
		private EditText mEditText;

		private CertificationFieldTextWatcher(View parentView, EditText editText) {
			mParentView = parentView;
			mEditText = editText;
		}

		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1,
				int i2) {
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1,
				int i2) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
			// After changing any field for the certification, add new empty
			// certification view if empty one being modified
			if (mParentView.equals(mLastEmptyCertificationView)) {
				mLastEmptyCertificationView = null;

				addEmptyCertificationView();
			}
		}

		protected DiverCertification getCertificationTagged() {
			return (DiverCertification) mParentView.getTag();
		}
	}

	public static DiverProfileFragment newInstance(long diverID) {
		Bundle args = new Bundle();
		args.putLong(DiverTabFragment.ARG_DIVER_ID, diverID);
		DiverProfileFragment rf = new DiverProfileFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setRetainInstance(true);

		// Get diver from given id
		Bundle args = getArguments();
		if (args != null) {
			mDiverID = args.getLong(DiverTabFragment.ARG_DIVER_ID, -1);
		}

		mEditMode = false;
		mProgressDialog = new ProgressDialog(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_diver_profile, parent, false);

        ((ObservableScrollView) view).setOnScrollStoppedListener(new ObservableScrollView.OnScrollStoppedListener() {

            public void onScrollStopped() {
                setSnapshot(View.INVISIBLE);
            }

            public void onScrollChanged(ObservableScrollView view, int l, int t, int oldl, int oldt) {
                if (mMapContainer.getVisibility() == View.VISIBLE && mMapViewSnapShot.getVisibility() != View.VISIBLE) {
                    setSnapshot(View.VISIBLE);
                    view.startScrollerTask();
                }
            }
        });

		mDiverInfoProgressContainer = view
				.findViewById(R.id.diver_info_progress_bar);
		mDiverProfileImageProgressContainer = view
				.findViewById(R.id.diver_picture_progress_bar);
		mDiverInfoCertificationContainer = view
				.findViewById(R.id.diver_certifications_container);

		// View Fields
		mDiverInfoViewContainer = view.findViewById(R.id.diver_info_view_container);

		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		int height = metrics.heightPixels;
		mDiverProfileImage = view.findViewById(R.id.diver_profile_image);
		mDiverProfileImage.getLayoutParams().height = height / 3;

		mDiverName = view.findViewById(R.id.diver_name);
		mDiverEmail = view.findViewById(R.id.diver_email);
		mDiverEmail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("plain/text");
				intent.putExtra(Intent.EXTRA_EMAIL,
						new String[] { ((TextView) v).getText().toString() });
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getResources().getString(R.string.app_name));
				startActivity(Intent.createChooser(intent, ""));
			}
		});

		mDiverCityStateCountry = view
				.findViewById(R.id.diver_city_state_country);
		mDiverBio = view.findViewById(R.id.diver_bio);

		// Edit Fields
		mDiverInfoEditContainer = view
				.findViewById(R.id.diver_info_edit_container);

		mDiverProfileImageEdit = view
				.findViewById(R.id.diver_profile_image_edit);
		mDiverProfileImageEdit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Ask user to select an image on their phone to set as their
				// profile
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						REQUEST_PICK_PROFILE_IMAGE);
			}
		});

		mDiverProfileNoImageEdit = view
				.findViewById(R.id.diver_profile_no_image_edit);
		mDiverProfileNoImageEdit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Ask user to select an image on their phone to set as their
				// profile
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						REQUEST_PICK_PROFILE_IMAGE);
			}
		});

		mDiverUsernameEdit = view
				.findViewById(R.id.diver_username_edit);
		mDiverFirstNameEdit = view
				.findViewById(R.id.diver_first_name_edit);
		mDiverLastNameEdit = view
				.findViewById(R.id.diver_last_name_edit);
		mDiverEmailEdit = view.findViewById(R.id.diver_email_edit);
		mDiverCityEdit = view.findViewById(R.id.diver_city_edit);
		mDiverProvinceEdit = view
				.findViewById(R.id.diver_province_edit);
		mDiverCountryEdit = view
				.findViewById(R.id.diver_country_edit);
		mDiverBioEdit = view.findViewById(R.id.diver_bio_edit);

		// Certifications List
        mDiverCertsLabel = view.findViewById(R.id.diver_certs_list_label);
		mDiverCerts = view.findViewById(R.id.diver_certs_list);

		mDiverInfoViewContainer.setVisibility(View.GONE);
		mDiverInfoEditContainer.setVisibility(View.GONE);
		mDiverInfoCertificationContainer.setVisibility(View.GONE);
		mDiverInfoProgressContainer.setVisibility(View.VISIBLE);

        mDiveSiteMarkers = new HashMap<DiveSite, Marker>();
        mLatLngBuilder = new LatLngBounds.Builder();

        mMapLoadingProgress = view.findViewById(R.id.diver_diveLog_map_progress_bar);

        mMapContainer = view.findViewById(R.id.mapHost);

        mLogMapLabel = view.findViewById(R.id.diver_profile_log_map_label);

        mDiverInfoUnavailable = view.findViewById(R.id.diver_no_info_available);

        // Set height of map view to screen's size minus title bar after 2
        // second delay
        mMapView = view.findViewById(R.id.diver_diveLog_mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.post(new Runnable() {
            @Override
            public void run() {
                updateMapViewHeight();
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

				mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick(Marker marker) {
						// Display Dive Site info
						Object[] diveSites = mDiveSiteMarkers.keySet().toArray();
						for (int i = 0; i < diveSites.length; i++) {
							if (mDiveSiteMarkers.get(diveSites[i]).equals(marker)) {
								// Dive Site found
								Intent intent = new Intent(getActivity(), DiveSiteActivity.class);
								intent.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, (DiveSite)diveSites[i]);
								startActivityForResult(intent, REQUEST_NEW_DIVESITE);
								break;
							}
						}
					}
				});
			}
		});

        MapsInitializer.initialize(this.getActivity());

        mMapViewSnapShot = view.findViewById(R.id.diver_diveLog_mapView_snapShot);

		// Get diver info and picture separately, since picture may take longer
        if (mDiverID != -1) {
            mDiverInfoUnavailable.setVisibility(View.GONE);

            mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
            mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                @Override
                public void onOnlineDiveDataRetrievedComplete(
                        ArrayList<Object> resultList, String message,
                        Boolean isError) {
                    if (resultList.size() > 0) {
                        mDiver = (Diver) resultList.get(0);
                        updateUI();
                    } else {
                        mDiverInfoProgressContainer.setVisibility(View.GONE);
                        mDiverInfoUnavailable.setVisibility(View.VISIBLE);
                    }

                    refreshOnlineDiveLogs();
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
            mDiveSiteOnlineDatabase.getUser(String.valueOf(mDiverID), "", "");
        } else {
            mDiverInfoProgressContainer.setVisibility(View.GONE);
            mMapContainer.setVisibility(View.GONE);
            mDiverInfoUnavailable.setVisibility(View.VISIBLE);
        }

		return view;
	}

    private void updateMapViewHeight() {
        // Set height of MapView
        int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

        int coordinatesContainerHeight = 0;

        mMapView.getLayoutParams().height = screenHeight - getTitleBarHeight()
                - getStatusBarHeight() - coordinatesContainerHeight
                - MAPVIEW_HEIGHT_BUFFER;
    }

    public int getStatusBarHeight() {
        Rect r = new Rect();
        Window w = getActivity().getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        return r.top;
    }

    public int getTitleBarHeight() {
        int viewTop = getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return (viewTop - getStatusBarHeight() + ((DiveActivity) getActivity()).getSupportActionBar().getHeight());
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_PICK_PROFILE_IMAGE && data != null
				&& data.getData() != null) {
			// User has selected an image and came back, upload as their profile
			// image
			Uri selectedImage = data.getData();

			// Process file path
			try {
				String filePath = null;

				// OI FILE Manager
				String filemanagerstring = selectedImage.getPath();

				// MEDIA GALLERY
				String selectedImagePath = mDiveSiteManager
						.getPath(selectedImage);

				if (selectedImagePath != null) {
					filePath = selectedImagePath;
				} else if (filemanagerstring != null) {
					filePath = filemanagerstring;
				} else {
					Toast.makeText(getActivity().getApplicationContext(),
							"Unknown path", Toast.LENGTH_LONG).show();
					Log.e("Bitmap", "Unknown path");
				}
 
				mProfileImageEdit = null; 
				if (filePath != null) {
					Bitmap image = mDiveSiteManager.decodeFileImage(filePath);

					if (image != null) {
						mProfileImageEdit = image;
						mDiverProfileImageEdit.setImageBitmap(image);

						mDiverProfileNoImageEdit.setVisibility(View.GONE);
						mDiverProfileImageEdit.setVisibility(View.VISIBLE);

						// Need to re save image and get new file to upload
						mProfileNewImageFilePath = mDiveSiteManager
								.saveImageInternalStorage(image,
										mDiver.getUsername());
					} else {
						Toast.makeText(
								DiverProfileFragment.this.getActivity()
										.getApplicationContext(),
								R.string.diver_profile_save_image_error,
								Toast.LENGTH_LONG).show();
						mProfileNewImageFilePath = null;
					}
				}

			} catch (Exception e) {
				Toast.makeText(getActivity().getApplicationContext(),
						"Internal error", Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onDestroy() {
        mMapView.onDestroy();

		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
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
	protected void updateUI() {
		// Set visibility based on edit mode
        if (getActivity() != null) {
            if (mDiverID != -1 && mDiver != null) {
                mDiverInfoProgressContainer.setVisibility(View.GONE);

                if (mEditMode) {
                    mDiverInfoViewContainer.setVisibility(View.GONE);
                    mMapContainer.setVisibility(View.GONE);
                    mDiverInfoEditContainer.setVisibility(View.VISIBLE);
                } else {
                    mDiverInfoViewContainer.setVisibility(View.VISIBLE);
                    mMapContainer.setVisibility(View.VISIBLE);
                    mDiverInfoEditContainer.setVisibility(View.GONE);
                }

                // Set UI elements with current diveSite
                mDiverCerts.removeAllViews();

                if (getActivity() != null) {
                    getActivity().setTitle(mDiver.getUsername());
                }
                mDiverUsernameEdit.setText(mDiver.getUsername());

                mDiverName.setText(mDiver.getFirstName() + " " + mDiver.getLastName());
                mDiverFirstNameEdit.setText(mDiver.getFirstName());
                mDiverLastNameEdit.setText(mDiver.getLastName());

                mDiverEmail.setText(mDiver.getEmail());
                mDiverEmailEdit.setText(mDiver.getEmail());

                String location = "";
                if (mDiver.getProvince().trim().isEmpty()) {
                    location = mDiver.getCity();
                } else if (mDiver.getCity().trim().isEmpty()) {
                    location = mDiver.getProvince();
                } else {
                    location = mDiver.getCity() + ", " + mDiver.getProvince();
                }

                location = location + " " + mDiver.getCountry();
                mDiverCityStateCountry.setText(location.trim());
                mDiverCityEdit.setText(mDiver.getCity());
                mDiverProvinceEdit.setText(mDiver.getProvince());
                mDiverCountryEdit.setText(mDiver.getCountry());

                mDiverBio.setText(mDiver.getBio());
                mDiverBioEdit.setText(mDiver.getBio());

                if (!mDiver.getPictureURL().trim().isEmpty()) {

                    mDiverProfileImageProgressContainer.setVisibility(View.VISIBLE);
                    if (mDiver.getOnlineId() == mDiveSiteManager.getLoggedInDiverId()) {
                        Bitmap profileImage = mDiveSiteManager.getLoggedInDiverProfileImage();
                        if (profileImage != null) {
                            mDiverProfileImageProgressContainer.setVisibility(View.GONE);
                            mDiverProfileNoImageEdit.setVisibility(View.GONE);
                            mDiverProfileImageEdit.setVisibility(View.VISIBLE);

                            mDiverProfileImage.setVisibility(View.VISIBLE);
                            mDiverProfileImage.setImageBitmap(profileImage);
                            mDiverProfileImageEdit.setImageBitmap(profileImage);
                        }
                    }

                    if (mProfileImage == null) {
                        LoadOnlineImageTask task = new LoadOnlineImageTask(mDiverProfileImage) {
                            @Override
                            protected void onPostExecute(Bitmap result) {
                                super.onPostExecute(result);

                                if (mDiver.getOnlineId() == mDiveSiteManager.getLoggedInDiverId()) {
                                    mDiveSiteManager.saveLoggedInDiverProfileImage(result);
                                }

                                mProfileImage = result;

                                mDiverProfileImageEdit.setVisibility(View.VISIBLE);
                                mDiverProfileImageEdit.setImageBitmap(mProfileImage);

                                mDiverProfileImageProgressContainer.setVisibility(View.GONE);
                                mDiverProfileNoImageEdit.setVisibility(View.GONE);
                            }
                        };
                        task.execute(mDiver.getPictureURL());
                    } else {
                        mDiverProfileImageProgressContainer.setVisibility(View.GONE);
                        mDiverProfileNoImageEdit.setVisibility(View.GONE);
                        mDiverProfileImageEdit.setVisibility(View.VISIBLE);

                        mDiverProfileImage.setVisibility(View.VISIBLE);
                        mDiverProfileImage.setImageBitmap(mProfileImage);
                        mDiverProfileImageEdit.setImageBitmap(mProfileImage);
                    }
                } else {
                    mDiverProfileImage.setVisibility(View.GONE);
                    mDiverProfileImageProgressContainer.setVisibility(View.GONE);
                    mDiverProfileNoImageEdit.setVisibility(View.VISIBLE);
                    mDiverProfileImageEdit.setVisibility(View.GONE);
                }

                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (mDiver.getCertifications().size() == 0) {
                    mDiverInfoCertificationContainer.setVisibility(View.GONE);
                    mDiverCertsLabel.setVisibility(View.GONE);
                } else {
                    mDiverInfoCertificationContainer.setVisibility(View.VISIBLE);
                    mDiverCertsLabel.setVisibility(View.VISIBLE);
                    // Create and add view for each certification
                    for (int i = 0; i < mDiver.getCertifications().size(); i++) {
                        DiverCertification c = mDiver.getCertifications().get(i);

                        if (mEditMode) {
                            View view = layoutInflater.inflate(
                                    R.layout.diver_cert_edit_list_item, null);

                            // Add copy of certification to save
                            DiverCertification certificationSave = new DiverCertification(c);
                            view.setTag(certificationSave);

                            EditText diverCertTitle = view.findViewById(R.id.diver_cert_title);
                            diverCertTitle.setText(c.getCertifTitle());
                            diverCertTitle.addTextChangedListener(new CertificationFieldTextWatcher(
                                    view, diverCertTitle) {

                                @Override
                                public void afterTextChanged(Editable s) {
                                    super.afterTextChanged(s);
                                    DiverCertification certification = super
                                            .getCertificationTagged();
                                    if (certification != null) {
                                        certification.setCertifTitle(s
                                                .toString());
                                    }
                                }

                            });

                            EditText diverCertDate = view
                                    .findViewById(R.id.diver_cert_date);
                            diverCertDate.setText(c.getCertifDate());
                            diverCertDate.addTextChangedListener(new CertificationFieldTextWatcher(
                                    view, diverCertDate) {

                                @Override
                                public void afterTextChanged(Editable s) {
                                    super.afterTextChanged(s);
                                    DiverCertification certification = super
                                            .getCertificationTagged();
                                    if (certification != null) {
                                        certification.setCertifDate(s
                                                .toString());
                                    }
                                }

                            });

                            EditText diverCertLocation = view.findViewById(R.id.diver_cert_location);
                            diverCertLocation.setText(c.getCertifLocation());
                            diverCertLocation.addTextChangedListener(new CertificationFieldTextWatcher(
                                    view, diverCertLocation) {

                                @Override
                                public void afterTextChanged(Editable s) {
                                    super.afterTextChanged(s);
                                    DiverCertification certification = super
                                            .getCertificationTagged();
                                    if (certification != null) {
                                        certification.setCertifLocation(s
                                                .toString());
                                    }
                                }

                            });

                            EditText diverCertNumber = view.findViewById(R.id.diver_cert_number);
                            diverCertNumber.setText(c.getCertifNumber());
                            diverCertNumber.addTextChangedListener(new CertificationFieldTextWatcher(
                                    view, diverCertNumber) {

                                @Override
                                public void afterTextChanged(Editable s) {
                                    super.afterTextChanged(s);
                                    DiverCertification certification = super.getCertificationTagged();
                                    if (certification != null) {
                                        certification.setCertifNumber(s.toString());
                                    }
                                }

                            });

                            CheckBox diverCertPrimary = view.findViewById(R.id.diver_cert_primary);
                            diverCertPrimary.setTag(view);
                            diverCertPrimary.setChecked(c.getPrimary());
                            diverCertPrimary.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    DiverCertification c = (DiverCertification) ((View) buttonView.getTag()).getTag();
                                    c.setPrimary(isChecked);

                                    if (isChecked) {
                                        // Need to unselect other cert's primary button without triggering their listener
                                        for (int j = 0; j < mDiverCerts.getChildCount(); j++) {
                                            View certView = mDiverCerts.getChildAt(j);
                                            DiverCertification c2 = (DiverCertification) certView.getTag();

                                            if (c2.getLocalId() != c.getLocalId() ||
                                                    c2.getOnlineId() != c.getOnlineId()) {
                                                CheckBox diverCertPrimary =
														certView.findViewById(R.id.diver_cert_primary);
                                                diverCertPrimary.setChecked(false);
                                            }
                                        }
                                    }
                                }

                            });

                            mDiverCerts.addView(view);

                        } else {
                            View view = layoutInflater.inflate(
                                    R.layout.diver_cert_list_item, null);

                            TextView diverCertTitle = view
                                    .findViewById(R.id.diver_cert_title);
                            diverCertTitle.setText(c.getCertifTitle());
                            if (diverCertTitle.getText().toString().trim().isEmpty()) {
                                diverCertTitle.setVisibility(View.GONE);
                            } else {
                                diverCertTitle.setVisibility(View.VISIBLE);
                            }

                            TextView diverCertDate = view
                                    .findViewById(R.id.diver_cert_date);
                            diverCertDate.setText(c.getCertifDate());
                            if (diverCertDate.getText().toString().trim().isEmpty()) {
                                diverCertDate.setVisibility(View.GONE);
                            } else {
                                diverCertDate.setVisibility(View.VISIBLE);
                            }

                            TextView diverCertLocation = view
                                    .findViewById(R.id.diver_cert_location);
                            diverCertLocation.setText(c.getCertifLocation());
                            if (diverCertLocation.getText().toString().trim().isEmpty()) {
                                diverCertLocation.setVisibility(View.GONE);
                            } else {
                                diverCertLocation.setVisibility(View.VISIBLE);
                            }

                            TextView diverCertNumber = view.findViewById(R.id.diver_cert_number);
                            diverCertNumber.setText(c.getCertifNumber());
                            if (diverCertNumber.getText().toString().trim().isEmpty()) {
                                diverCertNumber.setVisibility(View.GONE);
                            } else {
                                diverCertNumber.setVisibility(View.VISIBLE);
                            }

                            if (c.getPrimary()) {
                                view.setBackgroundResource(R.color.primaryCertColor);
                                diverCertTitle.setTextColor(getResources().getColor(R.color.White));
                                diverCertDate.setTextColor(getResources().getColor(R.color.White));
                                diverCertLocation.setTextColor(getResources().getColor(R.color.White));
                                diverCertNumber.setTextColor(getResources().getColor(R.color.White));
                            } else {
                                view.setBackgroundResource(R.color.White);
                                diverCertTitle.setTextColor(getResources().getColor(R.color.Black));
                                diverCertDate.setTextColor(getResources().getColor(R.color.Black));
                                diverCertLocation.setTextColor(getResources().getColor(R.color.Black));
                                diverCertNumber.setTextColor(getResources().getColor(R.color.Black));
                            }

                            mDiverCerts.addView(view);
                        }
                    }
                }

                if (mEditMode) {
                    addEmptyCertificationView();
                }

                getActivity().invalidateOptionsMenu();
            } else {
                mDiverInfoProgressContainer.setVisibility(View.GONE);
                mMapContainer.setVisibility(View.GONE);
                mDiverInfoUnavailable.setVisibility(View.VISIBLE);
            }
        }
	}

	private void addEmptyCertificationView() {

		// Add empty certification for new ones
		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.diver_cert_edit_list_item, null);

		DiverCertification cert = new DiverCertification();
		cert.setCertifUserId(mDiver.getOnlineId());
		view.setTag(cert);

		EditText diverCertTitle = view.findViewById(R.id.diver_cert_title);
		diverCertTitle.addTextChangedListener(new CertificationFieldTextWatcher(view,
						diverCertTitle) {

					@Override
					public void afterTextChanged(Editable s) {
						super.afterTextChanged(s);
						DiverCertification certification = super
								.getCertificationTagged();
						if (certification != null) {
							certification.setCertifTitle(s.toString());
						}
					}

				});

		EditText diverCertDate = view.findViewById(R.id.diver_cert_date);
		diverCertDate.addTextChangedListener(new CertificationFieldTextWatcher(view, diverCertDate) {

			@Override
			public void afterTextChanged(Editable s) {
				super.afterTextChanged(s);
				DiverCertification certification = super
						.getCertificationTagged();
				if (certification != null) {
					certification.setCertifDate(s.toString());
				}
			}

		});

		EditText diverCertLocation = view.findViewById(R.id.diver_cert_location);
		diverCertLocation.addTextChangedListener(new CertificationFieldTextWatcher(view,
						diverCertLocation) {

					@Override
					public void afterTextChanged(Editable s) {
						super.afterTextChanged(s);
						DiverCertification certification = super
								.getCertificationTagged();
						if (certification != null) {
							certification.setCertifLocation(s.toString());
						}
					}

				});

		EditText diverCertNumber = view.findViewById(R.id.diver_cert_number);
		diverCertNumber.addTextChangedListener(new CertificationFieldTextWatcher(view,
						diverCertNumber) {

					@Override
					public void afterTextChanged(Editable s) {
						super.afterTextChanged(s);
						DiverCertification certification = super
								.getCertificationTagged();
						if (certification != null) {
							certification.setCertifNumber(s.toString());
						}
					}

				});
		
		CheckBox diverCertPrimary = view.findViewById(R.id.diver_cert_primary);
		diverCertPrimary.setTag(view);
		diverCertPrimary.setChecked(cert.getPrimary());
		diverCertPrimary.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DiverCertification c = (DiverCertification)((View)buttonView.getTag()).getTag();
				c.setPrimary(isChecked);
				
				if (isChecked) {
					// Need to unselect other cert's primary button without triggering their listener
					for (int j = 0; j < mDiverCerts.getChildCount(); j++) {
						View certView = mDiverCerts.getChildAt(j);
						DiverCertification c2 = (DiverCertification)certView.getTag();
						
						if (c2.getLocalId() != c.getLocalId() || 
							c2.getOnlineId() != c.getOnlineId()) {
							CheckBox diverCertPrimary =
									certView.findViewById(R.id.diver_cert_primary);
							diverCertPrimary.setChecked(false);
						}
					}
				}
			}
			
		});
		
		mDiverCerts.addView(view);

		mLastEmptyCertificationView = view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_diver, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Make sure edit mode menu item correctly shown
		MenuItem editModeMenuItem = menu.findItem(R.id.menu_item_edit_mode);
		MenuItem saveMenuItem = menu.findItem(R.id.menu_item_save);
		MenuItem cancelMenuItem = menu.findItem(R.id.menu_item_cancel);

		if (mEditMode) {
			editModeMenuItem.setVisible(false);
			saveMenuItem.setVisible(true);
			cancelMenuItem.setVisible(true);
		} else {
			// Only show edit mode if diver being displayed matches logged in
			// diver
			if (mDiver != null && mDiveSiteManager.getLoggedInDiverId() == mDiver.getOnlineId()) {
				editModeMenuItem.setVisible(true);
			} else {
				editModeMenuItem.setVisible(false);
			}

			saveMenuItem.setVisible(false);
			cancelMenuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_item_edit_mode:
			mEditMode = true;
			mProfileNewImageFilePath = null;

			updateUI();
			getActivity().invalidateOptionsMenu();

			return true;

		case R.id.menu_item_save:
			// Ask user for password before changing view
			final EditText input_password = new EditText(getActivity());
			input_password.setTransformationMethod(PasswordTransformationMethod
					.getInstance());

			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.diver_profile_save_profile)
					.setMessage(R.string.diver_profile_enter_password)
					.setView(input_password)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {

									// Show progress dialog
									mProgressDialog
											.setMessage(getString(R.string.diver_profile_saving_profile_progress));
									mProgressDialog.setCancelable(false);
									mProgressDialog.setIndeterminate(true);
									mProgressDialog.show();

									Editable password = input_password
											.getText();

									DiveSiteOnlineDatabaseLink diveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(
											DiverProfileFragment.this
													.getActivity());
									diveSiteOnlineDatabase
											.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

												@Override
												public void onOnlineDiveDataRetrievedComplete(
														ArrayList<Object> resultList,
														String message,
														Boolean isError) {
													if (resultList.size() == 0) {
														// Invalid save, no user
														// retrieved
														if (message.isEmpty()) {
															Toast.makeText(
																	DiverProfileFragment.this
																			.getActivity()
																			.getApplicationContext(),
																	R.string.diver_profile_save_profile_error,
																	Toast.LENGTH_LONG)
																	.show();
														} else {
															Toast.makeText(
																	DiverProfileFragment.this
																			.getActivity()
																			.getApplicationContext(),
																	message,
																	Toast.LENGTH_LONG)
																	.show();
														}
													} else {
														// Save successful, set
														// updated diver data
														// and refresh
														mDiver = (Diver) resultList.get(0);
														
														if (mProfileImageEdit != null) {
															mProfileImage = mProfileImageEdit;
															mProfileImageEdit = null;
														}

														mProfileNewImageFilePath = null;
														mEditMode = false;
														updateUI();
														getActivity().invalidateOptionsMenu();
													}

													mProgressDialog.dismiss();
												}

												@Override
												public void onOnlineDiveDataProgress(
														Object result) {
													// TODO Auto-generated
													// method stub

												}

												@Override
												public void onOnlineDiveDataPostBackground(
														ArrayList<Object> resultList,
														String message) {
													// TODO Auto-generated
													// method stub

												}
											});

									// Get certifications to save
									ArrayList<DiverCertification> certifications = new ArrayList<DiverCertification>();
									for (int i = 0; i < mDiverCerts.getChildCount(); i++) {
										DiverCertification certification = 
											(DiverCertification) mDiverCerts.getChildAt(i).getTag();
										certifications.add(certification);
									}

									// Create diver to save
									Diver diverSave = new Diver(mDiver.getOnlineId(), mDiverFirstNameEdit.getText().toString(),
											mDiverLastNameEdit.getText().toString(),
											mDiverEmailEdit.getText().toString(), 
											mDiverCityEdit.getText().toString(),
											mDiverProvinceEdit.getText().toString(),
											mDiverCountryEdit.getText().toString(),
											mDiverUsernameEdit.getText().toString(), 
											mDiverBioEdit.getText().toString(),
											mDiver.getPictureURL(), 
											mDiver.getPictureDrawable(),
											mDiver.isMod(), 
											certifications);

									diveSiteOnlineDatabase.saveUser(diverSave,
											password.toString(),
											mProfileNewImageFilePath);
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();

			return true;

		case R.id.menu_item_cancel:
			mProfileNewImageFilePath = null;
			mEditMode = false;
			updateUI();
			getActivity().invalidateOptionsMenu();

			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
	}

    private void refreshOnlineDiveLogs() {
        if (mRefreshingOnlineDiveLogs) {
            cancelOnlineRefresh();
        }

        mRefreshingOnlineDiveLogs = true;

        mDiveSiteMarkers.clear();

        mDiveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
        mDiveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

            @Override
            public void onOnlineDiveDataRetrievedComplete(ArrayList<Object> resultList, String message, Boolean isError) {

                if (getActivity() != null) {
                    if (message != null && !message.isEmpty()) {
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }

                    mRefreshingOnlineDiveLogs = false;
                    mMapLoadingProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onOnlineDiveDataProgress(Object result) {
                if (mDiveSiteOnlineDatabase.getActive() && getActivity() != null) {
                    DiveSite diveSite = ((DiveLog) result).getDiveSite();
                    if (diveSite != null) {

                        mMapLoadingProgress.setVisibility(View.GONE);
                        mLogMapLabel.setVisibility(View.VISIBLE);
                        mMapView.setVisibility(View.VISIBLE);
                        mMapViewSnapShot.setVisibility(View.INVISIBLE);

                        Marker marker = getMarkerForDiveSiteOnlineID(diveSite.getOnlineId());
                        if (marker == null) {
                            // Create new marker for dive site
                            LatLng latLng = new LatLng(diveSite.getLatitude(),
                                    diveSite.getLongitude());
                            mLatLngBuilder.include(latLng);

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title(diveSite.getName())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.divesite_active_marker));
                            marker = mGoogleMap.addMarker(markerOptions);
                            mDiveSiteMarkers.put(diveSite, marker);

                            // Adjust map to show all log point
                            /**
                             * Add 2 points 1000m northEast and southWest of the
                             * center. They increase the bounds only, if they are
                             * not already larger than this.
                             */
                            LatLngBounds tmpBounds = mLatLngBuilder.build();
                            LatLng center = tmpBounds.getCenter();
                            LatLng norhtEast = move(center, 5000, 5000);
                            LatLng southWest = move(center, -5000, -5000);
                            mLatLngBuilder.include(southWest);
                            mLatLngBuilder.include(norhtEast);

                            Display display = getActivity().getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);

                            LatLngBounds latLngBounds = mLatLngBuilder.build();
                            CameraUpdate movement = CameraUpdateFactory
                                    .newLatLngBounds(latLngBounds, size.x, size.y, 100);

                            mGoogleMap.moveCamera(movement);
                        }
                    }
                }
            }

            @Override
            public void onOnlineDiveDataPostBackground(
                    ArrayList<Object> resultList, String message) {
                //
            }
        });

        mDiveSiteOnlineDatabase.getDiveLogList(new Date(0),
            mDiverID, -1, "", "", "", "", "", "");
    }

    private Marker getMarkerForDiveSiteOnlineID(long onlineID) {
        Marker marker = null;
        Object[] diveSites = mDiveSiteMarkers.keySet().toArray();
        for (int i = 0; i < diveSites.length; i++) {
            if (((DiveSite) diveSites[i]).getOnlineId() == onlineID) {
                marker = mDiveSiteMarkers.get(diveSites[i]);
                break;
            }
        }

        return marker;
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

    private void cancelOnlineRefresh() {
        if (mDiveSiteOnlineDatabase != null && mDiveSiteOnlineDatabase.getActive()) {
            mDiveSiteOnlineDatabase.stopBackground();
            mDiveSiteOnlineDatabase.cancel(true);
        }

        mRefreshingOnlineDiveLogs = false;
    }

    protected void setSnapshot(int visibility) {
        if (mDiveSiteMarkers.size() > 0) {
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
}
