package com.fahadaltimimi.divethesite.view.fragment;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import android.animation.LayoutTransition;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveLogActivity;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.view.FAMapView;
import com.fahadaltimimi.view.fragment.BaseListFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DiveLogListFragment extends BaseListFragment {

    protected static final SimpleDateFormat logItemDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat logItemTimeFormat = new SimpleDateFormat("HH:mm");

	protected static final int REQUEST_NEW_DIVELOG = 0;

	protected DiveSiteManager mDiveSiteManager;
	protected Bundle mSavedInstanceState;

	protected DiveSiteOnlineDatabaseLink mDiveSiteOnlineDatabase;

	protected LinearLayout mFilterNotificationContainer;
	protected TextView mFilterNotification;

    protected FAMapView mDiveLogItemMapView;
    protected ImageView mDiveLogItemMapViewSnapShot;

	protected View mLastDisplayedListItemView = null;

	protected ProgressDialog mProgressDialog;

	protected SharedPreferences mPrefs;

	protected long mRestrictToDiverID = -1;
	protected DiveSite mDiveSite = null;
	protected DiveLog mSelectedDiveLog = null;

	protected HashMap<Long, View> mDiveLogListItemViews = new HashMap<Long, View>();
	protected HashMap<Long, Integer> mDiveLogListItemLoaderIDs = new HashMap<Long, Integer>();
	protected HashMap<Long, Diver> mDiveLogListItemDiver = new HashMap<Long, Diver>();
	protected HashMap<Long, ImageView> mDiveLogListItemDiverImageView = new HashMap<Long, ImageView>();
	protected HashMap<Long, ImageView> mDiveLogListItemBuddyImageView = new HashMap<Long, ImageView>();
	protected HashMap<Long, TextView> mDiveLogListItemDiverTextView = new HashMap<Long, TextView>();
	protected LruCache<Long, Bitmap> mDiverProfileImageCache;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mSavedInstanceState = savedInstanceState;
		mDiveSiteManager = DiveSiteManager.get(getActivity());

		mPrefs = getActivity().getSharedPreferences(DiveSiteManager.PREFS_FILE,
				Context.MODE_PRIVATE);

		mProgressDialog = new ProgressDialog(getActivity());

		Bundle args = getArguments();

		if (args != null) {
			mRestrictToDiverID = args
					.getLong(DiverTabFragment.ARG_DIVER_ID, -1);
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
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		mFilterNotificationContainer = v
				.findViewById(R.id.list_filter_notification_container);
		mFilterNotification = v
				.findViewById(R.id.list_filter_notification);

        mDiveLogItemMapView = v.findViewById(R.id.divelog_list_item_mapView);
        mDiveLogItemMapView.onCreate(savedInstanceState);
        mDiveLogItemMapView.onResume();
        mDiveLogItemMapView.getLayoutParams().height = (getActivity()
                .getWindowManager().getDefaultDisplay().getHeight()
                - getTitleBarHeight() - getStatusBarHeight()) / 2;
        MapsInitializer.initialize(getActivity());
        mDiveLogItemMapView.setVisibility(View.GONE);

        mDiveLogItemMapViewSnapShot = v.findViewById(R.id.divelog_list_item_mapView_snapShot);
        mDiveLogItemMapViewSnapShot.setVisibility(View.GONE);

		updateFilterNotification();

        FloatingActionButton fab = v.findViewById(R.id.fab_action_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewDiveLog();
            }
        });

        if (mRestrictToDiverID != -1 && mRestrictToDiverID != mDiveSiteManager.getLoggedInDiverId()) {
            fab.hide();
        }

		return v;
	}

	protected void addNewDiveLog() {
        // If user not registered, don't allow
        if (mDiveSiteManager.getLoggedInDiverId() == -1) {
            Toast.makeText(getActivity(), R.string.not_registered_create_log, Toast.LENGTH_LONG).show();
        }

        openDiveLog(null);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_divelog_list;
    }

    @Override
    protected int getSwipeRefreshLayout() {
        return R.id.list_swipe_refresh;
    }

    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		DiveLog diveLog = getDiveLogItemClick(position, id);

        if (mLastDisplayedListItemView == v) {
            setSelectedDiveLog(null);
            mLastDisplayedListItemView = null;
        } else {
            setSelectedDiveLog(diveLog);
            mLastDisplayedListItemView = v;
        }

        refreshDiveLogList();
	}

    protected void setSnapshot(int visibility) {
        if (mSelectedDiveLog != null) {
            switch (visibility) {
                case View.VISIBLE:
                    if (mDiveLogItemMapView.getVisibility() == View.VISIBLE) {
                        mDiveLogItemMapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                                    @Override
                                    public void onSnapshotReady(Bitmap bitmap) {
                                        mDiveLogItemMapViewSnapShot.setImageBitmap(bitmap);
                                        mDiveLogItemMapViewSnapShot.setVisibility(View.VISIBLE);
                                        mDiveLogItemMapView.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        });
                    }
                    break;
                case View.INVISIBLE:
                    if (mDiveLogItemMapView.getVisibility() == View.INVISIBLE) {
                        mDiveLogItemMapView.setVisibility(View.VISIBLE);

                        mDiveLogItemMapViewSnapShot.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    }

	private void setSelectedDiveLog(DiveLog diveLog) {
		if (mSelectedDiveLog != diveLog) {
			mSelectedDiveLog = diveLog;
			getActivity().invalidateOptionsMenu();
		}
	}

	protected void updateFilterNotification() {
		// Inherited
	}

	protected DiveLog getDiveLogItemClick(int position, long id) {
		// Inherited
		return null;
	}

	protected void refreshDiveLogList() {
        super.refreshListView();
	}

	protected void openDiveLog(DiveLog diveLog) {
		Intent i = new Intent(getActivity(), DiveLogActivity.class);
		i.putExtra(DiveLogActivity.EXTRA_DIVE_LOG, diveLog);
        i.putExtra(DiveSiteManager.EXTRA_DIVE_SITE, mDiveSite);
		startActivityForResult(i, REQUEST_NEW_DIVELOG);
	}

    protected void setDiveLogMap(DiveLog diveLog, ViewGroup mapContainer) {
        final DiveSite diveSite = diveLog.getDiveSite();
        if (diveSite == null) {
            mapContainer.setVisibility(View.GONE);
        } else {
            mapContainer.setVisibility(View.VISIBLE);
            ViewGroup existingMapContainter = (ViewGroup) mDiveLogItemMapView.getParent();
            if (existingMapContainter != mapContainer) {
                if (existingMapContainter != null) {
                    final LayoutTransition transition = existingMapContainter.getLayoutTransition();
                    existingMapContainter.setLayoutTransition(null);
                    existingMapContainter.removeView(mDiveLogItemMapView);
                    existingMapContainter.setLayoutTransition(transition);
                }

                mapContainer.addView(mDiveLogItemMapView);
            }
            mDiveLogItemMapView.setVisibility(View.VISIBLE);

            ViewGroup existingMapSnapShotContainter = (ViewGroup) mDiveLogItemMapViewSnapShot.getParent();
            if (existingMapSnapShotContainter != mapContainer) {
                if (existingMapSnapShotContainter != null) {
                    final LayoutTransition transition = existingMapSnapShotContainter.getLayoutTransition();
                    existingMapSnapShotContainter.setLayoutTransition(null);
                    existingMapSnapShotContainter.removeView(mDiveLogItemMapViewSnapShot);
                    existingMapSnapShotContainter.setLayoutTransition(transition);
                }

                mapContainer.addView(mDiveLogItemMapViewSnapShot);
            }
            mDiveLogItemMapView.setVisibility(View.VISIBLE);
            mDiveLogItemMapViewSnapShot.setVisibility(View.INVISIBLE);

            mDiveLogItemMapView.onResume();
            mDiveLogItemMapView.getMapAsync(new OnMapReadyCallback() {
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
}
