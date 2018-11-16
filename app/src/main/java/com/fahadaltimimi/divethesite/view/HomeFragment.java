package com.fahadaltimimi.divethesite.view;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteManager.ErrorDialogFragment;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.model.NDBCStation;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCDriftingBuoyData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCMeteorologicalData;
import com.fahadaltimimi.divethesite.model.NDBCStation.NDBCSpectralWaveData;
import com.fahadaltimimi.divethesite.controller.PollService;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.ScheduledDiveDiveSite;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.divethesite.model.ScheduledDive;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class HomeFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final SimpleDateFormat scheduledDiveTimestampFormat = new SimpleDateFormat("dd/MM/yy, HH:mm");

	public static final String TAG = "HomeFragment";

	private static int LIST_ITEMS_BUFFER_MULTIPLIER = 3;
	private static double LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT = 0.60;
	private static int LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD = 20;
	
	private static final int MINIMUM_ZOOM_LEVEL_FOR_DATA = 6;

	public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
	
	private int mSitesAdditionalItemsToLoad = 0;
	private int mScheduledDivesAdditionalItemsToLoad = 0;
	private int mNDBCAdditionalItemsToLoad = 0;
	
	private Boolean mRefreshingOnlineDiveSites = false;
	private Boolean mRefreshingOnlineMapDiveSites = false;
	private Boolean mRefreshingOnlineNDBCs = false;
	private Boolean mRefreshingOnlineScheduledDives = false;
	
	private Boolean mForceLocationDataRefresh = false;
	
	protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabaseUser;
	protected DiveSiteOnlineDatabaseLink mDiveSitesOnlineDatabase;
	protected DiveSiteOnlineDatabaseLink mDiveSitesMapOnlineDatabase;
	protected DiveSiteOnlineDatabaseLink mScheduledDivesOnlineDatabase;
	protected DiveSiteOnlineDatabaseLink mNDBCOnlineDatabase;
	protected HashMap<Long, DiveSiteOnlineDatabaseLink> mNDBCDataOnlineDatabase =
			new HashMap<>();
    protected ArrayList<Long> mRefreshNDBCStationIDData = new ArrayList<>();
	
	private DiveSiteManager mDiveSiteManager;
	
	private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
	private boolean mLocationEnabled;

    private View mProfileProgress;
	private ImageButton mProfileImageButton;
	private Button mProfileNoImageButton;

    private View mDiveSiteListProgress;
	private ListView mDiveSiteListView;
    private Button mDiveSiteListNoDataLabel;
	
	private Button mMapTitle;
	private MapView mMapView;
	private GoogleMap mGoogleMap = null;

    private View mScheduledDiveListProgress;
	private ListView mScheduledDiveListView;
	private Button mScheduledDiveListNoDataLabel;

	private View mNDBCListProgress;
	private ListView mNDBCListView;
	private Button mNDBCListNoDataLabel;
	
	private HashMap<DiveSite, Marker> mDiveSiteMarkers = new HashMap<>();
	private HashMap<NDBCStation, Marker> mNDBCMarkers = new HashMap<>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
			    
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
        
        if (PollService.isServiceAlarmOn(getActivity())) {
        	PollService.setServiceAlarm(getActivity(), false, 0);
        }
        PollService.setServiceAlarm(getActivity(), true, 
    			DiveSiteManager.getIntegerPreferenceFromString(getActivity(), 
    				getActivity().getResources().getString(R.string.PREF_SETTING_INTERVAL_CHECK_UPDATES_SEC),
					PollService.POLL_INTERVAL_DEFAULT_SECONDS));

		checkLocationPermission();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, parent, false);
		
		// Get and set Views
        Button welcomeTitle = view.findViewById(R.id.home_welcome);
	    welcomeTitle.setText(String.format(getResources().getString(R.string.welcomeMessage),
	    		mDiveSiteManager.getLoggedInDiverUsername()));
	    welcomeTitle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    if (getActivity() != null) {
                    ((DiveActivity) getActivity()).openDrawer();
                }
			}
		});
	    
	    mProfileProgress = view.findViewById(R.id.home_item_profile_progress_bar);
	    
	    Drawable profileImage = null;
	    if (mProfileImageButton != null) {
	    	profileImage = mProfileImageButton.getDrawable();
	    }
	    mProfileImageButton = view.findViewById(R.id.home_item_profile);
	    if (profileImage != null) {
	    	mProfileImageButton.setImageDrawable(profileImage);
	    	mProfileProgress.setVisibility(View.GONE);
	    	mProfileImageButton.setVisibility(View.VISIBLE);
	    }
	    
	    mProfileNoImageButton = view.findViewById(R.id.home_item_profile_button);
	    
	    View.OnClickListener profileClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), DiverActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

				long diver_id = mDiveSiteManager.getLoggedInDiverId();
				String diver_username = mDiveSiteManager.getLoggedInDiverUsername();
				i.putExtra(DiverActivity.EXTRA_DIVER_ID, diver_id);
				i.putExtra(DiverActivity.EXTRA_DIVER_USERNAME, diver_username);
				startActivity(i);
			}
		};
		
		mProfileProgress.setOnClickListener(profileClickListener);
		mProfileImageButton.setOnClickListener(profileClickListener);
		mProfileNoImageButton.setOnClickListener(profileClickListener);
		
		if (mDiveSiteManager.getLoggedInDiverId() != -1) {

            Bitmap profileImageBitmap = mDiveSiteManager.getLoggedInDiverProfileImage();
            if (profileImageBitmap != null) {
                mProfileImageButton.setImageBitmap(profileImageBitmap);
                mProfileProgress.setVisibility(View.GONE);
                mProfileImageButton.setVisibility(View.VISIBLE);
                mProfileNoImageButton.setVisibility(View.GONE);
            }

			mDiveSiteOnlineDatabaseUser = 
					new DiveSiteOnlineDatabaseLink(getActivity());
			mDiveSiteOnlineDatabaseUser.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {
	
						@Override
						public void onOnlineDiveDataRetrievedComplete(
								ArrayList<Object> resultList,
								String message, Boolean isError) {
							if (resultList.size() > 0) {							
								// Now get bitmap profile image for diver
								Diver diver = (Diver) resultList.get(0);
								LoadOnlineImageTask task = new LoadOnlineImageTask(mProfileImageButton) {
	
									@Override
									protected void onPostExecute(Bitmap result) {
										super.onPostExecute(result);
                                        DiveSiteManager.get(getActivity()).saveLoggedInDiverProfileImage(result);
										if (result != null) {
											mProfileProgress.setVisibility(View.GONE);
											mProfileImageButton.setVisibility(View.VISIBLE);
											mProfileNoImageButton.setVisibility(View.GONE);
										} else {
											mProfileProgress.setVisibility(View.GONE);
											mProfileImageButton.setVisibility(View.GONE);
											mProfileNoImageButton.setVisibility(View.VISIBLE);
										}
									}
	
								};
								task.execute(diver.getPictureURL());
							}
						}
	
						@Override
						public void onOnlineDiveDataProgress(
								Object result) {
							// TODO Auto-generated method stub
	
						}
	
						@Override
						public void onOnlineDiveDataPostBackground(
								ArrayList<Object> resultList,
								String message) {
							// TODO Auto-generated method stub
	
						}
					});
			mDiveSiteOnlineDatabaseUser.getUser(String.valueOf(mDiveSiteManager.getLoggedInDiverId()), "", "");
		} else {
			mProfileProgress.setVisibility(View.GONE);
			mProfileImageButton.setVisibility(View.GONE);
			mProfileNoImageButton.setVisibility(View.VISIBLE);
		}

        Button diverList = view.findViewById(R.id.home_item_diver_list);

	    View.OnClickListener diverListClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), DiverListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		};
		
		diverList.setOnClickListener(diverListClickListener);

        Button refresh = view.findViewById(R.id.home_item_refresh);
		refresh.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mDiveSiteListView.setVisibility(View.GONE);
                mDiveSiteListNoDataLabel.setVisibility(View.GONE);
				mDiveSiteListProgress.setVisibility(View.VISIBLE);
				mScheduledDiveListView.setVisibility(View.GONE);
				mScheduledDiveListNoDataLabel.setVisibility(View.GONE);
				mScheduledDiveListProgress.setVisibility(View.VISIBLE);
				mNDBCListView.setVisibility(View.GONE);
				mNDBCListProgress.setVisibility(View.VISIBLE);
				mNDBCListNoDataLabel.setVisibility(View.GONE);

                cancelOnlineDiveSiteMapRefresh();
                clearDiveSites();
                clearScheduledDives();
                clearNDBCs();

				if (mGoogleApiClient.isConnected() && mLocationEnabled) {
					mForceLocationDataRefresh = true;
					if (ContextCompat.checkSelfPermission(getActivity(),
							Manifest.permission.ACCESS_FINE_LOCATION)
							== PackageManager.PERMISSION_GRANTED) {
						LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, HomeFragment.this);
					}
		    	} else {
					refreshOnlineDiveSites();
					refreshOnlineDiveSitesMap();
					refreshOnlineScheduledDives();
					refreshOnlineNDBCs();
		    	}
			}
		});

        Button diveSiteListTitle = view.findViewById(R.id.home_item_dive_site_title);
	    mDiveSiteListProgress = view.findViewById(R.id.home_item_site_list_progress_bar);
        mDiveSiteListNoDataLabel = view.findViewById(R.id.home_item_site_list_no_data);
	    
	    // Retain list view adapter if available
	    ListAdapter siteAdapter = null;
	    boolean showSiteList = false;
	    if (mDiveSiteListView != null) {
	    	siteAdapter = mDiveSiteListView.getAdapter();
	    	showSiteList = true;
	    } else {
	    	siteAdapter = new DiveSiteAdapter(new ArrayList<DiveSite>());
	    }
	    mDiveSiteListView = view.findViewById(R.id.home_item_site_list);
	    mDiveSiteListView.setAdapter(siteAdapter);
	    
	    if (showSiteList) {
	    	refreshDiveSiteList();
	    	mDiveSiteListView.setVisibility(View.VISIBLE);
	    	mDiveSiteListProgress.setVisibility(View.GONE);
	    }

        mDiveSiteListProgress.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), DiveSiteListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
	    diveSiteListTitle.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), DiveSiteListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
	    mDiveSiteListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				DiveSite diveSite = 
						(DiveSite) mDiveSiteListView.getAdapter().getItem(position);
				openDiveSite(diveSite);
			}
	    });

	    mDiveSiteListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    // If we reached a certain point in list and not refreshing, refresh online sites
                    // If items were loaded before but list view is empty, view is currently loading,
                    //  so don't refresh
                    if (!(mSitesAdditionalItemsToLoad > 0 && mDiveSiteListView.getCount() == 0) &&
                            !mRefreshingOnlineDiveSites && getLocation() != null &&
                            view.getLastVisiblePosition() >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * view.getCount()) {
                        refreshOnlineDiveSites();

                    }
                }
            }
		});

	    mMapTitle = view.findViewById(R.id.home_item_map_title);
        mMapView = view.findViewById(R.id.home_item_mapView);
        mMapView.onCreate(savedInstanceState);
        initializeMap();

        Button scheduledDiveTitle = view.findViewById(R.id.home_item_scheduled_dive_title);
	    mScheduledDiveListProgress = view.findViewById(R.id.home_item_scheduled_list_progress_bar);
	    mScheduledDiveListNoDataLabel = view.findViewById(R.id.home_item_scheduled_list_no_data);
	    
	    // Retain list view adapter if available
	    ListAdapter scheduledAdapter = null;
	    boolean showScheduledList = false;
	    if (mScheduledDiveListView != null) {
	    	scheduledAdapter = mScheduledDiveListView.getAdapter();
	    	showScheduledList = true;
	    }  else {
	    	scheduledAdapter = new ScheduledDiveAdapter(new ArrayList<ScheduledDive>());
	    }
	    mScheduledDiveListView = view.findViewById(R.id.home_item_scheduled_list);
	    mScheduledDiveListView.setAdapter(scheduledAdapter);	
	    
	    if (showScheduledList) {
	    	refreshScheduledDiveList();
	    	if (mScheduledDiveListView.getAdapter().getCount() > 0) {
				mScheduledDiveListView.setVisibility(View.VISIBLE);
				mScheduledDiveListProgress.setVisibility(View.GONE);
				mScheduledDiveListNoDataLabel.setVisibility(View.GONE);
			} else {
				mScheduledDiveListView.setVisibility(View.GONE);
				mScheduledDiveListProgress.setVisibility(View.GONE);
				mScheduledDiveListNoDataLabel.setVisibility(View.VISIBLE);
			}
	    }	    
	    
	    View.OnClickListener scheduledDiveClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), ScheduledDiveListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		};
		mScheduledDiveListProgress.setOnClickListener(scheduledDiveClickListener);
	    scheduledDiveTitle.setOnClickListener(scheduledDiveClickListener);
	    mScheduledDiveListNoDataLabel.setOnClickListener(scheduledDiveClickListener);
	    mScheduledDiveListView.setOnItemClickListener(new OnItemClickListener() {
	    	@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
	    		Intent i = new Intent(getActivity(), ScheduledDiveListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
	    });
	    mScheduledDiveListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    // If we reached a certain point in list and not refreshing, refresh online sites
                    // If items were loaded before but list view is empty, view is currently loading,
                    //  so don't refresh
                    if (!(mScheduledDivesAdditionalItemsToLoad > 0 && mScheduledDiveListView.getCount() == 0) &&
                            !mRefreshingOnlineScheduledDives && getLocation() != null &&
                            view.getLastVisiblePosition() >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * view.getCount()) {
                        refreshOnlineScheduledDives();
                    }
                }
			}
			
		});

	    mNDBCListProgress = view.findViewById(R.id.home_item_ndbc_list_progress_bar);
	    mNDBCListNoDataLabel = view.findViewById(R.id.home_item_ndbc_list_no_data);
	    
	    // Retain list view adapter if available
	    ListAdapter ndbcAdapter;
	    boolean showNDBCList = false;
	    if (mNDBCListView != null) {
	    	ndbcAdapter = mNDBCListView.getAdapter();
	    	showNDBCList = true;
	    }  else {
	    	ndbcAdapter = new NDBCAdapter(new ArrayList<NDBCStation>());
	    }
	    mNDBCListView = view.findViewById(R.id.home_item_ndbc_list);
	    mNDBCListView.setAdapter(ndbcAdapter);	
	    
	    if (showNDBCList) {
	    	refreshNDBCList();
	    	
	    	if (mNDBCListView.getAdapter().getCount() > 0) {
				mNDBCListView.setVisibility(View.VISIBLE);
				mNDBCListProgress.setVisibility(View.GONE);
				mNDBCListNoDataLabel.setVisibility(View.GONE);
			} else {
				mNDBCListView.setVisibility(View.GONE);
				mNDBCListProgress.setVisibility(View.GONE);
				mNDBCListNoDataLabel.setVisibility(View.VISIBLE);
			}
	    }
	    
	    mNDBCListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				NDBCStation ndbc = 
						(NDBCStation) mNDBCListView.getAdapter().getItem(position);
				
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(ndbc.getLatitude(), 
										   ndbc.getLongitude())).zoom(7).build();
				mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
	    	
	    });
	    mNDBCListView.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    // If we reached a certain point in list and not refreshing, refresh online sites
                    // If items were loaded before but list view is empty, view is currently loading,
                    //  so don't refresh
                    if (!(mNDBCAdditionalItemsToLoad > 0 && mNDBCListView.getCount() == 0) &&
                            !mRefreshingOnlineNDBCs && getLocation() != null &&
                            view.getLastVisiblePosition() >= LIST_ITEMS_TRIGGER_REFRESH_AT_COUNT * view.getCount()) {
                        refreshOnlineNDBCs();
                    }
                }
			}
			
		});
		
		// Load markers already available
	    Object[] diveSites = mDiveSiteMarkers.keySet().toArray();
		for (int i = 0; i < diveSites.length; i++) {
			DiveSite diveSite = (DiveSite) diveSites[i];
			LatLng latLng = new LatLng(diveSite.getLatitude(), diveSite.getLongitude());
			MarkerOptions markerOptions = new MarkerOptions().position(
					latLng).title(diveSite.getName());

			if (diveSite.isArchived()) {
				markerOptions.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.divesite_inactive_marker));
			} else {
				markerOptions
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.divesite_active_marker));
			}
			
			mGoogleMap.addMarker(markerOptions);
		}
		
		Object[] ndbcs = mNDBCMarkers.keySet().toArray();
		for (int i = 0; i < ndbcs.length; i++) {
			NDBCStation ndbc = (NDBCStation) ndbcs[i];
			LatLng latLng = new LatLng(ndbc.getLatitude(), ndbc.getLongitude());
			MarkerOptions markerOptions = new MarkerOptions().position(
					latLng).title(ndbc.getStationName());
			
			markerOptions.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ndcp_buoy_marker));
			
			mGoogleMap.addMarker(markerOptions);
		}

		return view;
	}
	
	private void openDiveSite(DiveSite diveSite) {
		getActivity()
			.getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(DiveSiteManager.PREF_CURRENT_DIVESITE_VIEW_MODE, false).apply();
		
		Intent i = new Intent(getActivity(), DiveSiteActivity.class);
		i.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, diveSite);
		startActivity(i);
	}
	
	protected void refreshOnlineDiveSites() {
		// Look for more dive sites and set menu item icon to spin
		if (mRefreshingOnlineDiveSites) {
			cancelOnlineDiveSiteRefresh();
		}
		
		mRefreshingOnlineDiveSites = true;

		mDiveSitesOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mDiveSitesOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {

						if (message != null && !message.isEmpty()) {
							Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineDiveSites = false;

                        if (mDiveSiteListView.getAdapter().getCount() > 0) {
                            mDiveSiteListView.setVisibility(View.VISIBLE);
                            mDiveSiteListProgress.setVisibility(View.GONE);
                            mDiveSiteListNoDataLabel.setVisibility(View.GONE);
                        } else {
                            mDiveSiteListView.setVisibility(View.GONE);
                            mDiveSiteListProgress.setVisibility(View.GONE);
                            mDiveSiteListNoDataLabel.setVisibility(View.VISIBLE);
                        }
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSitesOnlineDatabase.getActive()) {
							DiveSite diveSite = getDiveSiteOnlineId((DiveSite) result);
							if (diveSite == null) {
								((DiveSiteAdapter) mDiveSiteListView.getAdapter()).add((DiveSite) result);
							} else {
								((DiveSiteAdapter) mDiveSiteListView.getAdapter()).remove(diveSite);
								
								// Add new item in order of distance
								Location newLocation = new Location(((DiveSite) result).getName());
								newLocation.setLatitude(((DiveSite) result).getLatitude());
								newLocation.setLongitude(((DiveSite) result).getLongitude());
								
								boolean added = false;
								for (int i = 0; i < mDiveSiteListView.getAdapter().getCount(); i++) {
									diveSite = ((DiveSiteAdapter) mDiveSiteListView.getAdapter()).getItem(i);
									Location location = new Location(diveSite.getName());
									location.setLatitude(diveSite.getLatitude());
									location.setLongitude(diveSite.getLongitude());
									
									if (mDiveSiteManager.getLastLocation() != null &&
										mDiveSiteManager.getLastLocation().distanceTo(newLocation) <
										mDiveSiteManager.getLastLocation().distanceTo(location)) {
										
										((DiveSiteAdapter) mDiveSiteListView.getAdapter()).insert((DiveSite) result, i);
										added = true;
										break;
									}
								}

                                if (!added) {
                                    ((DiveSiteAdapter) mDiveSiteListView.getAdapter()).add((DiveSite) result);
                                }
							}

							refreshDiveSiteList();

						} else {
							cancelOnlineDiveSiteRefresh();
						}
						
						mDiveSiteListView.setVisibility(View.VISIBLE);
						mDiveSiteListProgress.setVisibility(View.GONE);
					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						//
					}
				});
		
		
		if (getLocation() != null) {
			if (mSitesAdditionalItemsToLoad == 0) {
				mDiveSitesOnlineDatabase.getDiveSiteList(new Date(0),
						-1, String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLongitude()),
						"", "", "", "", "", "", "", "", "", "", "");
			} else {
				mDiveSitesOnlineDatabase.getDiveSiteList(new Date(0),
						-1, String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLongitude()), 
						"", "", "", "", "", "", "", "", "",
						String.valueOf(mDiveSiteListView.getCount()),
						String.valueOf(mSitesAdditionalItemsToLoad));
			}
		} else {
			if (mSitesAdditionalItemsToLoad == 0) {
				mDiveSitesOnlineDatabase.getDiveSiteList(new Date(0),
						-1, "", "",
						"", "", "", "", "", "", "", "", "", "", "");
			} else {
				mDiveSitesOnlineDatabase.getDiveSiteList(new Date(0),
						-1, "", "", 
						"", "", "", "", "", "", "", "", "",
						String.valueOf(mDiveSiteListView.getCount()),
						String.valueOf(mSitesAdditionalItemsToLoad));
			}
		}
	}
	
	protected void refreshOnlineDiveSitesMap() {
		// Look for more dive sites and set menu item icon to spin
		if (mRefreshingOnlineMapDiveSites) {
			cancelOnlineDiveSiteMapRefresh();
		}
		
		mRefreshingOnlineMapDiveSites = true;

		mDiveSitesMapOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mDiveSitesMapOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {

						if (message != null && !message.isEmpty()) {
							Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineMapDiveSites = false;
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mDiveSitesMapOnlineDatabase.getActive()) {
							DiveSite diveSite = (DiveSite) result;
							LatLng latLng = new LatLng(diveSite.getLatitude(), diveSite.getLongitude());

							// If this marker exists, no need to add it again
							Marker marker = getMarkerForDiveSiteOnlineID(diveSite.getOnlineId());
							if (marker == null) {
								MarkerOptions markerOptions = new MarkerOptions().position(
										latLng).title(diveSite.getName());

								if (diveSite.isArchived()) {
									markerOptions.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.divesite_inactive_marker));
								} else {
									markerOptions
											.icon(BitmapDescriptorFactory
													.fromResource(R.drawable.divesite_active_marker));
								}

								marker = mGoogleMap.addMarker(markerOptions);
								mDiveSiteMarkers.put(diveSite, marker);
							} else {
								marker.setPosition(latLng);
								marker.setTitle(diveSite.getName());
							}
						} else {
							cancelOnlineDiveSiteRefresh();
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
			mDiveSitesMapOnlineDatabase.getDiveSiteList(new Date(0), -1, 
					String.valueOf(getLocation().getLatitude()),
					String.valueOf(getLocation().getLongitude()), "", "", "",
					"", coordinateRange[0], coordinateRange[1], coordinateRange[2],
					coordinateRange[3], "", "", "");
		} else {
			mDiveSitesMapOnlineDatabase.getDiveSiteList(new Date(0), -1, 
					"", "", "", "", "",
					"", coordinateRange[0], coordinateRange[1], coordinateRange[2],
					coordinateRange[3], "", "", "");
		}
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
	
	protected void refreshOnlineScheduledDives() {
		// Look for more scheduled dives and set menu item icon to spin
		if (mRefreshingOnlineScheduledDives) {
			cancelOnlineScheduledDiveRefresh();
		}
		
		mRefreshingOnlineScheduledDives = true;

		mScheduledDivesOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mScheduledDivesOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

					@Override
					public void onOnlineDiveDataRetrievedComplete(
							ArrayList<Object> resultList, String message,
							Boolean isError) {

						if (message != null && !message.isEmpty()) {
							Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
						}

						mRefreshingOnlineScheduledDives = false;
						
						if (mScheduledDiveListView.getAdapter().getCount() > 0) {
							mScheduledDiveListView.setVisibility(View.VISIBLE);
							mScheduledDiveListProgress.setVisibility(View.GONE);
							mScheduledDiveListNoDataLabel.setVisibility(View.GONE);
						} else {
							mScheduledDiveListView.setVisibility(View.GONE);
							mScheduledDiveListProgress.setVisibility(View.GONE);
							mScheduledDiveListNoDataLabel.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onOnlineDiveDataProgress(Object result) {
						if (mScheduledDivesOnlineDatabase.getActive()) {
							ScheduledDive scheduledDive = getScheduledDiveOnlineId((ScheduledDive) result);
							if (scheduledDive == null) {
								((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).add((ScheduledDive) result);
							} else {
								int index = getScheduledDiveIndex(scheduledDive);
								((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).remove(scheduledDive);
								
								// Add new item in order of distance
								Location newLocation = 
									((ScheduledDive) result).getClosestLocation(mDiveSiteManager.getLastLocation());
								if (newLocation == null) {
									((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).add((ScheduledDive) result);
								} else {
									boolean added = false;
									for (int i = 0; i < mDiveSiteListView.getAdapter().getCount(); i++) {
										scheduledDive = ((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).getItem(i);
										Location location = scheduledDive.getClosestLocation(mDiveSiteManager.getLastLocation());
												
										if (location != null && 
											mDiveSiteManager.getLastLocation() != null &&
											mDiveSiteManager.getLastLocation().distanceTo(newLocation) <
											mDiveSiteManager.getLastLocation().distanceTo(location)) {
											
											((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).insert((ScheduledDive) result, i);
											added = true;
											break;
										}
									}

                                    if (!added) {
                                        ((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).add((ScheduledDive) result);
                                    }
								}
							}

							refreshScheduledDiveList();

						} else {
							cancelOnlineScheduledDiveRefresh();
						}
						
						mScheduledDiveListView.setVisibility(View.VISIBLE);
						mScheduledDiveListProgress.setVisibility(View.GONE);
					}

					@Override
					public void onOnlineDiveDataPostBackground(
							ArrayList<Object> resultList, String message) {
						//
					}
				});
		
		

		if (getLocation() != null) {
			if (mScheduledDivesAdditionalItemsToLoad == 0) {
				mScheduledDivesOnlineDatabase.getScheduledDiveList(new Date(0),
						-1, -1, -1, String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLatitude()), "", "", "", "", "", "",
						String.valueOf(new Date().getTime()), "", "", "", "");
			} else {
				mScheduledDivesOnlineDatabase.getScheduledDiveList(new Date(0),
						-1, -1, -1, String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLatitude()), 
						"", "", "", "", "", "",
						String.valueOf(new Date().getTime()), "", "",
						String.valueOf(mScheduledDiveListView.getCount()),
						String.valueOf(mScheduledDivesAdditionalItemsToLoad));
			}
		} else {
			if (mScheduledDivesAdditionalItemsToLoad == 0) {
				mScheduledDivesOnlineDatabase.getScheduledDiveList(new Date(0),
						-1, -1, -1, "", "", "", "", "", "", "", "",
						String.valueOf(new Date().getTime()), "", "", "", "");
			} else {
				mScheduledDivesOnlineDatabase.getScheduledDiveList(new Date(0),
						-1, -1, -1, "", "", "", "", "", "", "", "",
						String.valueOf(new Date().getTime()), "", "",
						String.valueOf(mScheduledDiveListView.getCount()),
						String.valueOf(mScheduledDivesAdditionalItemsToLoad));
			}
		}
	}
	
	protected void refreshOnlineNDBCs() {
		// Look for more ndbc and set menu item icon to spin
		if (mRefreshingOnlineNDBCs) {
			cancelOnlineNDBCRefresh();
		}
		
		mRefreshingOnlineNDBCs = true;

		mNDBCOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity());
		mNDBCOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

			@Override
			public void onOnlineDiveDataRetrievedComplete(
					ArrayList<Object> resultList, String message,
					Boolean isError) {

				if (message != null && !message.isEmpty()) {
					Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
				}

				mRefreshingOnlineNDBCs = false;
				
				if (mNDBCListView.getAdapter().getCount() == 0) {
					mNDBCListView.setVisibility(View.GONE);
					mNDBCListProgress.setVisibility(View.GONE);
					mNDBCListNoDataLabel.setVisibility(View.VISIBLE);
				} else {
                    mNDBCListView.setVisibility(View.VISIBLE);
                    mNDBCListProgress.setVisibility(View.GONE);
                    mNDBCListNoDataLabel.setVisibility(View.GONE);
                }
			}

			@Override
			public void onOnlineDiveDataProgress(Object result) {
				if (mNDBCOnlineDatabase.getActive()) {
					NDBCStation ndbc = getNDBCOnlineId((NDBCStation) result);
					if (ndbc == null) {						
						((NDBCAdapter) mNDBCListView.getAdapter()).add((NDBCStation) result);
					} else {
						((NDBCAdapter) mNDBCListView.getAdapter()).remove(ndbc);
						
						// Add new item in order of distance
						Location newLocation = new Location(((NDBCStation) result).getStationName());
						newLocation.setLatitude(((NDBCStation) result).getLatitude());
						newLocation.setLongitude(((NDBCStation) result).getLongitude());
						
						boolean added = false;
						for (int i = 0; i < mNDBCListView.getAdapter().getCount(); i++) {
							ndbc = ((NDBCAdapter) mNDBCListView.getAdapter()).getItem(i);
							Location location = new Location(ndbc.getStationName());
							location.setLatitude(ndbc.getLatitude());
							location.setLongitude(ndbc.getLongitude());
							
							if (mDiveSiteManager.getLastLocation() != null &&
								mDiveSiteManager.getLastLocation().distanceTo(newLocation) <
								mDiveSiteManager.getLastLocation().distanceTo(location)) {
								
								((NDBCAdapter) mNDBCListView.getAdapter()).insert((NDBCStation) result, i);
								added = true;
								break;
							}
						}

                        if (!added) {
                            ((NDBCAdapter) mNDBCListView.getAdapter()).add((NDBCStation) result);
                        }
					}

					ndbc = (NDBCStation) result;
                    if (mRefreshNDBCStationIDData.indexOf(ndbc.getStationId()) == -1) {
                        mRefreshNDBCStationIDData.add(ndbc.getStationId());
                    }

					LatLng latLng = new LatLng(ndbc.getLatitude(), ndbc.getLongitude());

					// If this marker exists, no need to add it again
					Marker marker = getMarkerForNDBCStationID(ndbc.getStationId());
					if (marker == null) {
						MarkerOptions markerOptions = 
								new MarkerOptions().position(latLng).title(ndbc.getStationName());

						markerOptions.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.ndcp_buoy_marker));
			
						marker = mGoogleMap.addMarker(markerOptions);
						mNDBCMarkers.put(ndbc, marker);
					} else {
						marker.setPosition(latLng);
						marker.setTitle(ndbc.getStationName());
					}

					refreshNDBCList();

					mNDBCListView.setVisibility(View.VISIBLE);
					mNDBCListProgress.setVisibility(View.GONE);
					mNDBCListNoDataLabel.setVisibility(View.GONE);
				} else {
					cancelOnlineNDBCRefresh();
				}
				
				mNDBCListView.setVisibility(View.VISIBLE);
				mNDBCListProgress.setVisibility(View.GONE);
			}

			@Override
			public void onOnlineDiveDataPostBackground(
					ArrayList<Object> resultList, String message) {
				//
			}
		});
				
		Date minLastUpdateTimestamp = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(minLastUpdateTimestamp);
		c.add(Calendar.DATE, -1);
		minLastUpdateTimestamp = c.getTime();
		
		if (getLocation() != null) {
			if (mNDBCAdditionalItemsToLoad == 0) {
				mNDBCOnlineDatabase.getNDBCStations("", "", "", "",
						"1", String.valueOf(minLastUpdateTimestamp.getTime()),
						String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLongitude()), 
						"", "", "");
			} else {
				mNDBCOnlineDatabase.getNDBCStations("", "", "", "",
						"1", String.valueOf(minLastUpdateTimestamp.getTime()),
						String.valueOf(getLocation().getLatitude()),
						String.valueOf(getLocation().getLongitude()), "", 
						String.valueOf(mNDBCListView.getCount()),
						String.valueOf(mNDBCAdditionalItemsToLoad));
			}
		} else {
			if (mNDBCAdditionalItemsToLoad == 0) {
				mNDBCOnlineDatabase.getNDBCStations("", "", "", "",
						"1", String.valueOf(minLastUpdateTimestamp.getTime()),
						"", "", "", "", "");
			} else {
				mNDBCOnlineDatabase.getNDBCStations("", "", "", "",
						"1", String.valueOf(minLastUpdateTimestamp.getTime()),
						"", "", "", 
						String.valueOf(mNDBCListView.getCount()),
						String.valueOf(mNDBCAdditionalItemsToLoad));
			}
		}
	}
	
	private Marker getMarkerForNDBCStationID(long stationID) {
		Marker marker = null;
		Object[] ndbcs = mNDBCMarkers.keySet().toArray();
		for (int i = 0; i < ndbcs.length; i++) {
			if (((NDBCStation) ndbcs[i]).getStationId() == stationID) {
				marker = mNDBCMarkers.get(ndbcs[i]);
				break;
			}
		}

		return marker;
	}
	
	private int getDiveSiteIndex(DiveSite diveSite) {
		int index = -1;
		for (int i = 0; i < mDiveSiteListView.getAdapter().getCount(); i++) {
			if (((DiveSiteAdapter) mDiveSiteListView.getAdapter()).getItem(i).getOnlineId() == 
					diveSite.getOnlineId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private DiveSite getDiveSiteOnlineId(DiveSite diveSite) {
		DiveSite diveSiteDuplicate = null;
		for (int i = 0; i < mDiveSiteListView.getAdapter().getCount(); i++) {
			if (((DiveSiteAdapter) mDiveSiteListView.getAdapter()).getItem(i).getOnlineId() == diveSite
					.getOnlineId()) {
				diveSiteDuplicate = ((DiveSiteAdapter) mDiveSiteListView.getAdapter()).getItem(i);
				break;
			}
		}
		return diveSiteDuplicate;
	}

	private void refreshDiveSiteList() {
		((DiveSiteAdapter) mDiveSiteListView.getAdapter()).notifyDataSetChanged();

        if (!refreshAdditionalDiveSiteListRequired()) {
            cancelOnlineDiveSiteRefresh();
        }
	}

    private boolean refreshAdditionalDiveSiteListRequired() {
        int lastItemPosition = mDiveSiteListView.getLastVisiblePosition();
        return !(lastItemPosition >= 0 && mDiveSiteListView.getChildCount() > 0 && mDiveSiteListView.getParent() != null &&
                 mDiveSiteListView.getChildAt(mDiveSiteListView.getChildCount() - 1).getBottom() >= ((ViewGroup) mDiveSiteListView.getParent()).getHeight() &&
                 mDiveSiteListView.getCount() >= lastItemPosition * LIST_ITEMS_BUFFER_MULTIPLIER);
    }
	
	private int getScheduledDiveIndex(ScheduledDive scheduledDive) {
		int index = -1;
		for (int i = 0; i < mScheduledDiveListView.getAdapter().getCount(); i++) {
			if (((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).getItem(i).getOnlineId() == 
					scheduledDive.getOnlineId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private ScheduledDive getScheduledDiveOnlineId(ScheduledDive scheduledDive) {
		ScheduledDive scheduledDiveDuplicate = null;
		for (int i = 0; i < mScheduledDiveListView.getAdapter().getCount(); i++) {
			if (((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).getItem(i).getOnlineId() == 
					scheduledDive.getOnlineId()) {
				scheduledDiveDuplicate = ((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).getItem(i);
				break;
			}
		}
		return scheduledDiveDuplicate;
	}

	private void refreshScheduledDiveList() {
		((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).notifyDataSetChanged();

        if (!refreshAdditionalScheduledDiveListRequired()) {
            cancelOnlineScheduledDiveRefresh();
        }
	}

    private boolean refreshAdditionalScheduledDiveListRequired() {
        int lastItemPosition = mScheduledDiveListView.getLastVisiblePosition();
        return !(lastItemPosition >= 0 && mScheduledDiveListView.getChildCount() > 0 &&
                 mScheduledDiveListView.getChildAt(mScheduledDiveListView.getChildCount() - 1).getBottom() >= ((ViewGroup) mScheduledDiveListView.getParent()).getHeight() &&
                 mScheduledDiveListView.getCount() >= lastItemPosition * LIST_ITEMS_BUFFER_MULTIPLIER);
    }
	
	private int getNDBCIndex(NDBCStation ndbcStation) {
		int index = -1;
		for (int i = 0; i < mNDBCListView.getAdapter().getCount(); i++) {
			if (((NDBCAdapter) mNDBCListView.getAdapter()).getItem(i).getStationId() == 
					ndbcStation.getStationId()) {
				index = i;
				break;
			}
		}
		return index;
	}

	private NDBCStation getNDBCOnlineId(NDBCStation ndbc) {
		NDBCStation ndbcDuplicate = null;
		for (int i = 0; i < mNDBCListView.getAdapter().getCount(); i++) {
			if (((NDBCAdapter) mNDBCListView.getAdapter()).getItem(i).getStationId() == ndbc
					.getStationId()) {
				ndbcDuplicate = ((NDBCAdapter) mNDBCListView.getAdapter()).getItem(i);
				break;
			}
		}
		return ndbcDuplicate;
	}

	private void refreshNDBCList() {
		((NDBCAdapter) mNDBCListView.getAdapter()).notifyDataSetChanged();

        if (!refreshAdditionalNDBCListRequired()) {
            cancelOnlineNDBCRefresh();
        }
	}

    private boolean refreshAdditionalNDBCListRequired() {
        int lastItemPosition = mNDBCListView.getLastVisiblePosition();
        return !(lastItemPosition >= 0 && mNDBCListView.getChildCount() > 0 &&
                 mNDBCListView.getChildAt(mNDBCListView.getChildCount() - 1).getBottom() >= ((ViewGroup) mNDBCListView.getParent()).getHeight() &&
                 mNDBCListView.getCount() >= lastItemPosition * LIST_ITEMS_BUFFER_MULTIPLIER);
    }
	
	private void clearDiveSites() {
		// Clears list and resets adapter
		cancelOnlineDiveSiteRefresh();
		mSitesAdditionalItemsToLoad = 0;
		
		((DiveSiteAdapter) mDiveSiteListView.getAdapter()).clear();
		DiveSiteAdapter adapter = new DiveSiteAdapter(new ArrayList<DiveSite>());
		mDiveSiteListView.setAdapter(adapter);
		((DiveSiteAdapter) mDiveSiteListView.getAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineDiveSiteRefresh() {
		if (mDiveSitesOnlineDatabase != null && mDiveSitesOnlineDatabase.getActive()) {
			mDiveSitesOnlineDatabase.stopBackground();
			mDiveSitesOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineDiveSites = false;
	}
	
	private void cancelOnlineDiveSiteMapRefresh() {
		if (mDiveSitesMapOnlineDatabase != null && mDiveSitesMapOnlineDatabase.getActive()) {
			mDiveSitesMapOnlineDatabase.stopBackground();
			mDiveSitesMapOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineMapDiveSites = false;
	}
	
	private void clearScheduledDives() {
		// Clears list and resets adapter
		cancelOnlineScheduledDiveRefresh();
		mScheduledDivesAdditionalItemsToLoad = 0;
		
		((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).clear();
		ScheduledDiveAdapter adapter = new ScheduledDiveAdapter(new ArrayList<ScheduledDive>());
		mScheduledDiveListView.setAdapter(adapter);
		((ScheduledDiveAdapter) mScheduledDiveListView.getAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineScheduledDiveRefresh() {
		if (mScheduledDivesOnlineDatabase != null && mScheduledDivesOnlineDatabase.getActive()) {
			mScheduledDivesOnlineDatabase.stopBackground();
			mScheduledDivesOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineScheduledDives = false;
	}
	
	private void clearNDBCs() {
		// Clears list and resets adapter
		cancelOnlineNDBCRefresh();
		cancelOnlineNDBCDataRefresh();
		
		mNDBCAdditionalItemsToLoad = 0;
		
		((NDBCAdapter) mNDBCListView.getAdapter()).clear();
		NDBCAdapter adapter = new NDBCAdapter(new ArrayList<NDBCStation>());
		mNDBCListView.setAdapter(adapter);
		((NDBCAdapter) mNDBCListView.getAdapter()).notifyDataSetChanged();
	}
	
	private void cancelOnlineNDBCRefresh() {
		if (mNDBCOnlineDatabase != null && mNDBCOnlineDatabase.getActive()) {
			mNDBCOnlineDatabase.stopBackground();
			mNDBCOnlineDatabase.cancel(true);
		}
		
		mRefreshingOnlineNDBCs = false;
	}
	
	private void cancelOnlineNDBCDataRefresh() {
		Object[] ndbcStationIDs = mNDBCDataOnlineDatabase.keySet().toArray();
		for (int i = 0; i < ndbcStationIDs.length; i++) {
			if (mNDBCDataOnlineDatabase.get(ndbcStationIDs[i]) != null && 
					mNDBCDataOnlineDatabase.get(ndbcStationIDs[i]).getActive()) {
				mNDBCDataOnlineDatabase.get(ndbcStationIDs[i]).stopBackground();
				mNDBCDataOnlineDatabase.get(ndbcStationIDs[i]).cancel(true);
			}
		}
	}
	
	private void cancelProfileRefresh() {
		if (mDiveSiteOnlineDatabaseUser != null && mDiveSiteOnlineDatabaseUser.getActive()) {
			mDiveSiteOnlineDatabaseUser.stopBackground();
			mDiveSiteOnlineDatabaseUser.cancel(true);
		}
	}
	
	private Location getLocation() {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() &&
                LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient) != null) {
			return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		} else {
			return mDiveSiteManager.getLastLocation();
		}
	}

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }
	
	@Override
	public void onStart() {
		super.onStart();

        // Connect the client.
        mGoogleApiClient.connect();
	}
	
	@Override
	public void onResume() {
        super.onResume();
        if (mMapView != null && mGoogleMap != null) {
			mMapView.onResume();
		}

        if (refreshAdditionalDiveSiteListRequired()) {
            refreshOnlineDiveSites();
        }
        if (refreshAdditionalScheduledDiveListRequired()) {
            refreshOnlineScheduledDives();
        }
        if (refreshAdditionalNDBCListRequired()) {
            refreshOnlineNDBCs();
        }

        refreshOnlineDiveSitesMap();
	}
	
	@Override
	public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();

        cancelProfileRefresh();
        cancelOnlineDiveSiteRefresh();
        cancelOnlineDiveSiteMapRefresh();
        cancelOnlineNDBCRefresh();
        cancelOnlineScheduledDiveRefresh();
		
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

			if (ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				LocationServices.FusedLocationApi.requestLocationUpdates(
						mGoogleApiClient, mLocationRequest, this);
			}
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
        if (mGoogleMap != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            if (mDiveSiteManager.getLastLocation() == null ||
                    mForceLocationDataRefresh ||
                    (Math.abs(location.getLatitude() - mDiveSiteManager.getLastLocation().getLatitude()) > DiveSiteManager.LOCATION_COMPARE_EPSILON) ||
                    (Math.abs(location.getLongitude() - mDiveSiteManager.getLastLocation().getLongitude()) > DiveSiteManager.LOCATION_COMPARE_EPSILON)) {

                mDiveSiteManager.saveLastLocation(location);

                mForceLocationDataRefresh = false;

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(),
                                location.getLongitude())).zoom(7).build();
                mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                // Clear and refresh lists
                mDiveSiteListView.setVisibility(View.GONE);
                mDiveSiteListNoDataLabel.setVisibility(View.GONE);
                mDiveSiteListProgress.setVisibility(View.VISIBLE);
                mScheduledDiveListView.setVisibility(View.GONE);
                mScheduledDiveListNoDataLabel.setVisibility(View.GONE);
                mScheduledDiveListProgress.setVisibility(View.VISIBLE);
                mNDBCListView.setVisibility(View.GONE);
                mNDBCListProgress.setVisibility(View.VISIBLE);
                mNDBCListNoDataLabel.setVisibility(View.GONE);

                clearDiveSites();
                clearScheduledDives();
                clearNDBCs();

                refreshOnlineDiveSites();
                refreshOnlineDiveSitesMap();
                refreshOnlineScheduledDives();
                refreshOnlineNDBCs();
            }
        }
    }
    	
	private class DiveSiteAdapter extends ArrayAdapter<DiveSite> {

		public DiveSiteAdapter(ArrayList<DiveSite> diveSites) {
			super(getActivity(), 0, diveSites);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// If we weren't given a view, inflate one using the layout we
			// created for each list item
			if (view == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.home_divesite_list_item, parent, false);
			}

			DiveSite diveSite = getItem(position);
			
			TextView title = view.findViewById(R.id.home_divesite_list_item_title);
			TextView location = view.findViewById(R.id.home_divesite_list_item_location);

			title.setText(diveSite.getName());
			location.setText(diveSite.getFullLocation());

            if (mDiveSiteListView.getCount() < LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD) {
                mSitesAdditionalItemsToLoad = LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD;
            } else {
                mSitesAdditionalItemsToLoad = mDiveSiteListView.getCount();
            }
			
			return view;
		}
	}
	
	private class ScheduledDiveAdapter extends ArrayAdapter<ScheduledDive> {

		public ScheduledDiveAdapter(ArrayList<ScheduledDive> scheduledDives) {
			super(getActivity(), 0, scheduledDives);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// If we weren't given a view, inflate one using the layout we
			// created for each list item
			if (view == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.home_scheduleddive_list_item, parent, false);
			}

			ScheduledDive scheduledDive = getItem(position);
			
			TextView title = view.findViewById(R.id.home_scheduleddive_list_item_title);
			if (scheduledDive.getTitle().trim().isEmpty()) {
				title.setText(getResources().getString(R.string.scheduleddive_default_title) + 
						" " + scheduledDive.getTimestampStringShort());
			} else {
				title.setText(scheduledDive.getTitle());
			}			

			TextView timestamp = view.findViewById(R.id.home_scheduleddive_list_item_timestamp);
			timestamp.setText(scheduledDiveTimestampFormat.format(scheduledDive.getTimestamp()));

			TextView diverCount = view.findViewById(R.id.home_scheduleddive_list_item_diver_count);
			diverCount.setText(String.format(
					getResources().getString(R.string.scheduleddive_list_diver_short_count), 
					scheduledDive.getScheduledDiveUsers().size()));
			
			LinearLayout scheduledDiveDiveSiteListView =
					view.findViewById(R.id.home_scheduleddive_item_site_list);
			
			// Process scheduled dive's sites 
			scheduledDiveDiveSiteListView.removeAllViews();
			if (scheduledDive.getScheduledDiveDiveSites().size() > 0) {
				for (int i = 0; i < scheduledDive.getScheduledDiveDiveSites().size(); i++) {
					final ScheduledDiveDiveSite scheduledDiveDiveSite =
							scheduledDive.getScheduledDiveDiveSites().get(i);
					
					DiveSite diveSite = scheduledDiveDiveSite.getDiveSite();
					if (diveSite != null) {
						LayoutInflater layoutInflater = 
								(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						View scheduledDiveDiveSiteView = layoutInflater.inflate(
								R.layout.scheduleddive_site_item, null);
				
						// Open scheduled dive if scheduled dive dive site clicked
						scheduledDiveDiveSiteView.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								Intent i = new Intent(getActivity(), ScheduledDiveListActivity.class);
								i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(i);
							}
						});
						
						// Add the view to the list
						scheduledDiveDiveSiteListView.addView(scheduledDiveDiveSiteView);
		
						// Set scheduled dive site view fields
						TextView scheduledDiveDiveSiteName =
								scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_name);
						scheduledDiveDiveSiteName.setText(diveSite.getName());
						
						TextView scheduledDiveDiveSiteLocation =
								scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_location);
						scheduledDiveDiveSiteLocation.setText(diveSite.getFullLocation());
						
						TextView scheduledDiveDiveSiteVoteCount =
								scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_vote_count);
						//scheduledDiveDiveSiteVoteCount.setVisibility(View.VISIBLE);
						scheduledDiveDiveSiteVoteCount.setText(String.format(getResources().getString(R.string.scheduleddive_list_vote_count), 
								scheduledDiveDiveSite.getVoteCount()));
					}
				}
			} else {
				// Add item displaying no sites
				LayoutInflater layoutInflater = 
						(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View scheduledDiveDiveSiteView = layoutInflater.inflate(
						R.layout.scheduleddive_site_item, null);
		
				// Open scheduled dive if scheduled dive dive site clicked
				scheduledDiveDiveSiteView.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent i = new Intent(getActivity(), ScheduledDiveListActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				});
				
				// Add the view to the list
				scheduledDiveDiveSiteListView.addView(scheduledDiveDiveSiteView);

				// Set scheduled dive site view fields
				TextView scheduledDiveDiveSiteName =
						scheduledDiveDiveSiteView.findViewById(R.id.scheduleddive_site_name);
				scheduledDiveDiveSiteName.setText(getResources().getString(R.string.scheduleddive_no_sites));
			}

            if (mScheduledDiveListView.getCount() < LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD) {
                mScheduledDivesAdditionalItemsToLoad = LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD;
            } else {
                mScheduledDivesAdditionalItemsToLoad = mScheduledDiveListView.getCount();
            }

			return view;
		}
	}
	
	private class NDBCAdapter extends ArrayAdapter<NDBCStation> {

		public NDBCAdapter(ArrayList<NDBCStation> ndbcs) {
			super(getActivity(), 0, ndbcs);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			// If we weren't given a view, inflate one using the layout we
			// created for each list item
			if (view == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.home_ndbc_list_item, parent, false);
			}

            NDBCStation ndbc = getItem(position);
			
			TextView stationTitle = view.findViewById(R.id.home_ndbc_list_item_station);
			stationTitle.setText(ndbc.getStationName());
			
			TextView stationDistance = view.findViewById(R.id.home_ndbc_list_item_distance);
			
			Location ndbcLocation = new Location(ndbc.getStationName());
			ndbcLocation.setLatitude(ndbc.getLatitude());
			ndbcLocation.setLongitude(ndbc.getLongitude());

			// Get distance to station in km, format to 1 dp
			NumberFormat distanceFormat = NumberFormat.getNumberInstance();
			distanceFormat.setMinimumFractionDigits(0);
			distanceFormat.setMaximumFractionDigits(1);

			float distance = getLocation().distanceTo(ndbcLocation);
			distance = distance / 1000;
			stationDistance.setText(distanceFormat.format(distance) + " km");
	
			View ndbcDataProgress = view.findViewById(R.id.home_item_ndbc_data_progress_bar);

			// Hide progress bar if station doesn't require refresh and not currently being refreshed
			if (mRefreshNDBCStationIDData.indexOf(ndbc.getStationId()) == -1 &&
                !mNDBCDataOnlineDatabase.containsKey(ndbc.getStationId()) &&
                 ndbcDataProgress.getVisibility() != View.GONE) {
                // Hide progress bar if NDBC stationd doesn't require refresh
				ndbcDataProgress.setVisibility(View.GONE);
			} else if (mRefreshNDBCStationIDData.indexOf(ndbc.getStationId()) != -1) {
                // If NDBC station requires refresh then refresh it
                // Get station data
                mRefreshNDBCStationIDData.remove(ndbc.getStationId());
                DiveSiteOnlineDatabaseLink diveSiteOnlineDatabase = new DiveSiteOnlineDatabaseLink(getActivity()) {

                    @Override
                    protected void onPostExecute(String file_url) {
                        super.onPostExecute(file_url);

                        if (getUpdateObject() != null) {
                            NDBCStation ndbc = (NDBCStation) getUpdateObject();
                            mNDBCDataOnlineDatabase.remove(ndbc.getStationId());
                        }
                    }

                };
                diveSiteOnlineDatabase.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                    @Override
                    public void onOnlineDiveDataRetrievedComplete(
                            ArrayList<Object> resultList,
                            String message, Boolean isError) {
                        // Replace existing station with updated one
                        if (resultList.size() > 0) {
                            NDBCStation updatedNDBCStation = (NDBCStation) resultList.get(0);
                            NDBCStation ndbc = getNDBCOnlineId(updatedNDBCStation);
                            if (ndbc == null) {
                                ((NDBCAdapter) mNDBCListView.getAdapter()).add(updatedNDBCStation);
                            } else {
                                int index = getNDBCIndex(ndbc);
                                ((NDBCAdapter) mNDBCListView.getAdapter()).remove(ndbc);
                                ((NDBCAdapter) mNDBCListView.getAdapter()).insert(updatedNDBCStation, index);
                            }

                            //refreshNDBCList();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
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

                mNDBCDataOnlineDatabase.put(ndbc.getStationId(), diveSiteOnlineDatabase);
                diveSiteOnlineDatabase.updateNDBCDataForStation(ndbc, "1");
            }

			if (ndbc.getMeteorologicalDataCount() > 0) {
				NDBCMeteorologicalData data = ndbc.getLatestMeteorologicalData();
				
				TableRow airTempContainer =
						view.findViewById(R.id.home_ndbc_list_item_air_temp_container);
				if (data.getAirTemperature() == null) {
					airTempContainer.setVisibility(View.GONE);
				} else {
					airTempContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_air_temp_value);
					value.setText(String.format(getResources().getString(
								R.string.temperatureDegCValue),
								data.getAirTemperature()));
				}
				
				TableRow waterTempContainer =
						view.findViewById(R.id.home_ndbc_list_item_water_temp_container);
				if (data.getWaterTemperature() == null) {
					waterTempContainer.setVisibility(View.GONE);
				} else {
					waterTempContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_water_temp_value);
					value.setText(String.format(
								getResources().getString(R.string.temperatureDegCValue),
								data.getWaterTemperature()));
				}
				
				TableRow tideContainer =
						view.findViewById(R.id.home_ndbc_list_item_tide_container);
				if (data.getTide() == null) {
					tideContainer.setVisibility(View.GONE);
				} else {
					tideContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_tide_value);
					value.setText(String.format(
							getResources().getString(R.string.distanceFTValue),
							data.getTide()));
				}
				
				TableRow windDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_direction_container);
				if (data.getWindDirection() == null) {
					windDirectionContainer.setVisibility(View.GONE);
				} else {
					windDirectionContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_direction_value);
					value.setText(String.format(
							getResources().getString(R.string.directionDegTValue),
							data.getWindDirection()));
				}
				
				TableRow windSpeedContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_speed_container);
				if (data.getWindSpeed() == null) {
					windSpeedContainer.setVisibility(View.GONE);
				} else {
					windSpeedContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_speed_value);
					value.setText(String.format(
							getResources().getString(R.string.speedMSValue),
							data.getWindSpeed()));
				}
				
				TableRow windGustContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_gust_container);
				if (data.getWindSpeed() == null) {
					windGustContainer.setVisibility(View.GONE);
				} else {
					windGustContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_gust_value);
					value.setText(String.format(
							getResources().getString(R.string.speedMSValue),
							data.getWindGust()));
				}
				
			} else if (ndbc.getDriftingBuoyDataCount() > 0) {
				NDBCDriftingBuoyData data = ndbc.getLatestDriftingBuoyData();
				
				TableRow airTempContainer =
						view.findViewById(R.id.home_ndbc_list_item_air_temp_container);
				if (data.getAirTemperature() == null) {
					airTempContainer.setVisibility(View.GONE);
				} else {
					airTempContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_air_temp_value);
					value.setText(String.format(getResources().getString(
								R.string.temperatureDegCValue),
								data.getAirTemperature()));
				}
				
				TableRow waterTempContainer =
						view.findViewById(R.id.home_ndbc_list_item_water_temp_container);
				if (data.getWaterTemperature() == null) {
					waterTempContainer.setVisibility(View.GONE);
				} else {
					waterTempContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_water_temp_value);
					value.setText(String.format(
								getResources().getString(R.string.temperatureDegCValue),
								data.getWaterTemperature()));
				}
				
				TableRow windDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_direction_container);
				if (data.getWindDirection() == null) {
					windDirectionContainer.setVisibility(View.GONE);
				} else {
					windDirectionContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_direction_value);
					value.setText(String.format(
							getResources().getString(R.string.directionDegTValue),
							data.getWindDirection()));
				}
				
				TableRow windSpeedContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_speed_container);
				if (data.getWindSpeed() == null) {
					windSpeedContainer.setVisibility(View.GONE);
				} else {
					windSpeedContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_speed_value);
					value.setText(String.format(
							getResources().getString(R.string.speedMSValue),
							data.getWindSpeed()));
				}
				
				TableRow windGustContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_gust_container);
				if (data.getWindSpeed() == null) {
					windGustContainer.setVisibility(View.GONE);
				} else {
					windGustContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_gust_value);
					value.setText(String.format(
							getResources().getString(R.string.speedMSValue),
							data.getWindGust()));
				}
				
				TableRow tideContainer =
						view.findViewById(R.id.home_ndbc_list_item_tide_container);
				tideContainer.setVisibility(View.GONE);
			}
			
			if (ndbc.getSpectralWaveDataCount() > 0) {
				NDBCSpectralWaveData data = ndbc.getLatestSpectralWaveData();
				
				TableRow swellHeightContainer =
						view.findViewById(R.id.home_ndbc_list_item_swell_height_container);
				if (data.getSwellHeight() == null) {
					swellHeightContainer.setVisibility(View.GONE);
				} else {
					swellHeightContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_swell_height_value);
					value.setText(String.format(
							getResources().getString(R.string.distanceMValue),
							data.getWaveHeight()));
				}
				
				TableRow swellPeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_swell_period_container);
				if (data.getSwellPeriod() == null) {
					swellPeriodContainer.setVisibility(View.GONE);
				} else {
					swellPeriodContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_swell_period_value);
					value.setText(String.format(
							getResources().getString(R.string.timeSValue),
							data.getSwellPeriod()));
				}
				
				TableRow swellDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_swell_direction_container);
				if (data.getSwellDirection() == null) {
					swellDirectionContainer.setVisibility(View.GONE);
				} else {
					swellDirectionContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_swell_direction_value);
					value.setText(data.getSwellDirection());
				}
				
				TableRow waveHeightContainer =
						view.findViewById(R.id.home_ndbc_list_item_wave_height_container);
				if (data.getWaveHeight() == null) {
					waveHeightContainer.setVisibility(View.GONE);
				} else {
					waveHeightContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wave_height_value);
					value.setText(String.format(
							getResources().getString(R.string.distanceMValue),
							data.getWaveHeight()));
				}
				
				TableRow avgWavePeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_average_wave_period_container);
				if (data.getAverageWavePeriod() == null) {
					avgWavePeriodContainer.setVisibility(View.GONE);
				} else {
					avgWavePeriodContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_average_wave_period_value);
					value.setText(String.format(
							getResources().getString(R.string.timeSValue),
							data.getAverageWavePeriod()));
				}
				
				TableRow waveSteepnessContainer =
						view.findViewById(R.id.home_ndbc_list_item_wave_steepness_container);
				if (data.getWaveSteepness() == null) {
					waveSteepnessContainer.setVisibility(View.GONE);
				} else {
					waveSteepnessContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wave_steepness_value);
					value.setText(data.getWaveSteepness());
				}
				
				TableRow windWaveHeightContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_wave_height_container);
				if (data.getWindWaveHeight() == null) {
					windWaveHeightContainer.setVisibility(View.GONE);
				} else {
					windWaveHeightContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_wave_height_value);
					value.setText(String.format(
							getResources().getString(R.string.distanceMValue),
							data.getWindWaveHeight()));
				}
				
				TableRow windWavePeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_wave_period_container);
				if (data.getWindWavePeriod() == null) {
					windWavePeriodContainer.setVisibility(View.GONE);
				} else {
					windWavePeriodContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_wave_period_value);
					value.setText(String.format(
							getResources().getString(R.string.timeSValue),
							data.getWindWavePeriod()));
				}
				
				TableRow windWaveDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_wave_direction_container);
				if (data.getWindWaveDirection() == null) {
					windWaveDirectionContainer.setVisibility(View.GONE);
				} else {
					windWaveDirectionContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_wind_wave_direction_value);
					value.setText(data.getWindWaveDirection());
				}
				
				TableRow domWavePeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_dominant_wave_period_container);
				domWavePeriodContainer.setVisibility(View.GONE);
				
				TableRow domWaveDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_dominant_wave_direction_container);
				domWaveDirectionContainer.setVisibility(View.GONE);
				
			} else if (ndbc.getMeteorologicalDataCount() > 0) {
				NDBCMeteorologicalData data = ndbc.getLatestMeteorologicalData();
	
				TableRow waveHeightContainer =
						view.findViewById(R.id.home_ndbc_list_item_wave_height_container);
				if (data.getSignificantWaveHeight() == null) {
					waveHeightContainer.setVisibility(View.GONE);
				} else {
					waveHeightContainer.setVisibility(View.VISIBLE);
					
					TextView value = view.findViewById(R.id.home_ndbc_list_item_wave_height_value);
					value.setText(String.format(getResources().getString(R.string.distanceMValue), data.getSignificantWaveHeight()));
				}
				
				TableRow domWavePeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_dominant_wave_period_container);
				if (data.getDominantWavePeriod() == null) {
					domWavePeriodContainer.setVisibility(View.GONE);
				} else {
					domWavePeriodContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_dominant_wave_period_value);
					value.setText(String.format(
							getResources().getString(R.string.timeSValue),
							data.getDominantWavePeriod()));
				}
				
				TableRow waveDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_dominant_wave_direction_container);
				if (data.getDominantWaveDirection() == null) {
					waveDirectionContainer.setVisibility(View.GONE);
				} else {
					waveDirectionContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_dominant_wave_direction_value);
					value.setText(String.format(
							getResources().getString(R.string.distanceMValue),
							data.getDominantWaveDirection()));
				}
				
				TableRow avgWavePeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_average_wave_period_container);
				if (data.getAverageWavePeriod() == null) {
					avgWavePeriodContainer.setVisibility(View.GONE);
				} else {
					avgWavePeriodContainer.setVisibility(View.VISIBLE);
					
					TextView value =
							view.findViewById(R.id.home_ndbc_list_item_average_wave_period_value);
					value.setText(String.format(
							getResources().getString(R.string.timeSValue),
							data.getAverageWavePeriod()));
				}
				
				TableRow swellHeightContainer =
						view.findViewById(R.id.home_ndbc_list_item_swell_height_container);
				swellHeightContainer.setVisibility(View.GONE);
				
				TableRow swellPeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_swell_period_container);
				swellPeriodContainer.setVisibility(View.GONE);
				
				TableRow swellDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_swell_direction_container);
				swellDirectionContainer.setVisibility(View.GONE);
				
				TableRow waveSteepnessContainer =
						view.findViewById(R.id.home_ndbc_list_item_wave_steepness_container);
				waveSteepnessContainer.setVisibility(View.GONE);
				
				TableRow windWaveHeightContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_wave_height_container);
				windWaveHeightContainer.setVisibility(View.GONE);
				
				TableRow windWavePeriodContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_wave_period_container);
				windWavePeriodContainer.setVisibility(View.GONE);
				
				TableRow windWaveDirectionContainer =
						view.findViewById(R.id.home_ndbc_list_item_wind_wave_direction_container);
                windWaveDirectionContainer.setVisibility(View.GONE);
			}

            if (mNDBCListView.getCount() < LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD) {
                mNDBCAdditionalItemsToLoad = LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD;
            } else {
                mNDBCAdditionalItemsToLoad = mNDBCListView.getCount();
            }
			
			return view;
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	}

	public boolean checkLocationPermission() {
		if (ContextCompat.checkSelfPermission(getActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
					Manifest.permission.ACCESS_FINE_LOCATION)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.title_location_permission)
						.setMessage(R.string.text_location_permission)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								// Prompt the user once explanation has been shown
								ActivityCompat.requestPermissions(getActivity(),
										new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
										MY_PERMISSIONS_REQUEST_LOCATION);
							}
						})
						.create()
						.show();


			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(getActivity(),
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_LOCATION);
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					initializeMap();

				} else {

					// Permission denied
				}
			}
		}
	}

	private void initializeMap() {
        // Permission was granted
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMapView.onResume();
            mMapTitle.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), DiveSiteFullMapActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            });
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mGoogleMap = googleMap;
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mGoogleMap.setMyLocationEnabled(true);
                        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);

                        if (getLocation() != null) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(getLocation().getLatitude(),
                                            getLocation().getLongitude())).zoom(7).build();
                            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }

                        mGoogleMap.setOnMapClickListener(new OnMapClickListener() {

                            @Override
                            public void onMapClick(LatLng arg0) {
                                Intent i = new Intent(getActivity(), DiveSiteFullMapActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                            }

                        });

                        mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                            @Override
                            public void onCameraChange(CameraPosition cameraPosition) {
                                // Only refresh dive sites and station data if we're zoomed in
                                // enough, otherwise display message
                                if (cameraPosition.zoom >= MINIMUM_ZOOM_LEVEL_FOR_DATA) {
                                    refreshOnlineDiveSitesMap();
                                }
                            }

                        });
                        mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                // Display Dive Site info
                                Object[] diveSites = mDiveSiteMarkers.keySet().toArray();
                                for (int i = 0; i < diveSites.length; i++) {
                                    if (mDiveSiteMarkers.get(diveSites[i]).equals(marker)) {
                                        // Dive Site found
                                        openDiveSite((DiveSite) diveSites[i]);
                                        break;
                                    }
                                }
                            }
                        });
                    }
                }
            });

            MapsInitializer.initialize(getActivity());
        }
    }
}
