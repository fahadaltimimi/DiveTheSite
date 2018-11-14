package com.fahadaltimimi.divethesite.view;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.divethesite.controller.DiveSiteOnlineDatabaseLink;
import com.fahadaltimimi.divethesite.model.Diver;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.model.NavDrawerItem;
import com.fahadaltimimi.model.NavDrawerListAdapter;
import com.fahadaltimimi.divethesite.R;

public class DiveActivity extends SingleFragmentActivity {

	private DrawerLayout mDrawerLayout;
	private View mDrawer;
    private ViewGroup mDrawerHeader;
    private ListView mDrawerMidList;
    private ViewGroup mDrawerFooter;
	private ActionBarDrawerToggle mDrawerToggle;

    private ImageView mDrawerHeaderImageView;
    private TextView mDrawerHeaderTextView;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;

	private SharedPreferences mPrefs;

	@Override
	protected int getLayoutResId() {
		return R.layout.dive_activity;
	}

	@Override
	protected Fragment createFragment() {
		// Should not be called, other activities should override
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE);

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.nav_drawer_layout);
		mDrawer = findViewById(R.id.nav_list_slidermenu);

        mDrawerHeader = (ViewGroup) findViewById(R.id.nav_list_slidermenu_header);
        mDrawerMidList = (ListView) findViewById(R.id.nav_list_slidermenu_mid_list);
        mDrawerFooter = (ViewGroup) findViewById(R.id.nav_list_slidermenu_footer);

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Header View - Profile, if not using guest account
        if (mPrefs.getLong(DiveSiteManager.PREF_CURRENT_USER_ID, -1) != -1) {
            View navHeaderView = mInflater.inflate(R.layout.drawer_list_header_item, mDrawerHeader);
            mDrawerHeaderImageView = (ImageView) navHeaderView.findViewById(R.id.icon);
            mDrawerHeaderImageView.setImageResource(R.drawable.logo_symbol);
            mDrawerHeaderTextView = (TextView) navHeaderView.findViewById(R.id.title);
            mDrawerHeaderTextView.setText(mPrefs.getString(DiveSiteManager.PREF_CURRENT_USERNAME,
                    getResources().getString(R.string.nav_profile)));
            navHeaderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DiveActivity.this, DiverActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    long diver_id = mPrefs.getLong(DiveSiteManager.PREF_CURRENT_USER_ID, -1);
                    String diver_username = mPrefs.getString(DiveSiteManager.PREF_CURRENT_USERNAME, "");
                    i.putExtra(DiverActivity.EXTRA_DIVER_ID, diver_id);
                    i.putExtra(DiverActivity.EXTRA_DIVER_USERNAME, diver_username);
                    startActivity(i);
                }
            });
            Bitmap profileImage = DiveSiteManager.get(this).getLoggedInDiverProfileImage();
            if (profileImage != null) {
                mDrawerHeaderImageView.setImageBitmap(profileImage);
            } else {
                DiveSiteOnlineDatabaseLink diveSiteOnlineDatabaseLink = new DiveSiteOnlineDatabaseLink(this);
                diveSiteOnlineDatabaseLink.setDiveSiteOnlineLoaderListener(new DiveSiteOnlineDatabaseLink.OnlineDiveDataListener() {

                    @Override
                    public void onOnlineDiveDataRetrievedComplete(
                            ArrayList<Object> resultList,
                            String message, Boolean isError) {
                        if (resultList.size() > 0) {
                            // Now get bitmap profile image for diver
                            Diver diver = (Diver) resultList.get(0);
                            LoadOnlineImageTask task = new LoadOnlineImageTask(mDrawerHeaderImageView) {

                                @Override
                                protected void onPostExecute(Bitmap result) {
                                    super.onPostExecute(result);
                                    DiveSiteManager.get(DiveActivity.this).saveLoggedInDiverProfileImage(result);
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
                diveSiteOnlineDatabaseLink.getUser(String.valueOf(mPrefs.getLong(DiveSiteManager.PREF_CURRENT_USER_ID, -1)), "", "");
            }
        }

        // Foot View - Settings, Log Out
        View navFooterSettingView = mInflater.inflate(R.layout.drawer_list_item, mDrawerFooter, false);
        ImageView settingIcon = (ImageView) navFooterSettingView.findViewById(R.id.icon);
        settingIcon.setImageResource(R.drawable.ic_menu_preferences);
        TextView settingText = (TextView) navFooterSettingView.findViewById(R.id.title);
        settingText.setText(R.string.nav_settings);
        navFooterSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(DiveActivity.this, SettingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        mDrawerFooter.addView(navFooterSettingView);

        View navFooterLogoutView = mInflater.inflate(R.layout.drawer_list_item, mDrawerFooter, false);
        ImageView logoutIcon = (ImageView) navFooterLogoutView.findViewById(R.id.icon);
        logoutIcon.setImageResource(R.drawable.ic_menu_unarchive);
        TextView logoutText = (TextView) navFooterLogoutView.findViewById(R.id.title);
        logoutText.setText(R.string.nav_logout);
        navFooterLogoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DiveActivity.this)
                        .setTitle(R.string.logout)
                        .setMessage(R.string.logout_confirm)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        // Delete preference to logout then go back
                                        // to login screen
                                        getSharedPreferences(DiveSiteManager.PREFS_FILE, Context.MODE_PRIVATE)
                                                .edit()
                                                .remove(DiveSiteManager.PREF_CURRENT_USER_ID)
                                                .commit();
                                        Intent i = new Intent(DiveActivity.this, LoginActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                }).setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
        mDrawerFooter.addView(navFooterLogoutView);

        // Mid View - List
		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], R.drawable.ic_home));
		// Diver List
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], R.drawable.ic_menu_friendslist));
        // Map View
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], R.drawable.ic_menu_mapmode));
		// Dive Site List
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], R.drawable.ic_menu_sort_by_size));
		// Schedule
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], R.drawable.ic_menu_sort_by_size));
		// Dive Log List
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], R.drawable.ic_menu_sort_by_size));
				
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerMidList.setAdapter(adapter);

		if (getActionBar() != null) {
			// enabling action bar app icon and behaving it as toggle button
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
		}

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name // nav drawer close - description for
									// accessibility
		) {
			@Override
			public void onDrawerClosed(View view) {
				if (getSupportActionBar() != null) {
					getSupportActionBar().setTitle(mTitle);
					// calling onPrepareOptionsMenu() to show action bar icons
					invalidateOptionsMenu();
				}
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mDrawerTitle);
					// calling onPrepareOptionsMenu() to hide action bar icons
					invalidateOptionsMenu();
				}
			}
		};
	
		mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerMidList.setOnItemClickListener(new SlideMenuClickListener());
	}

	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			displayView(position);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawer);

		for (int i = 0; i < menu.size(); i++) {
			menu.getItem(i).setVisible(!drawerOpen);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	private void displayView(int position) {
        closeDrawer();
		Intent i;
		switch (position) {
		case 0:
			i = new Intent(this, HomeActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
            break;
		case 1:
			i = new Intent(this, DiverListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			break;
		case 2:
			i = new Intent(this, DiveSiteFullMapActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			break;
        case 3:
            i = new Intent(this, DiveSiteListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            break;
		case 4:
			i = new Intent(this, ScheduledDiveListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			break;
		case 5:
			i = new Intent(this, DiveLogListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			break;
		default:
			break;
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(mTitle);
		}
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggle
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	protected void openDrawer() {
		mDrawerLayout.openDrawer(mDrawer);
	}

    protected void closeDrawer() {
        mDrawerLayout.closeDrawer(mDrawer);
    }
	
	@Override
	protected void onResume() {
	  super.onResume();
	  DiveApplication.activityResumed();
	}

	@Override
	protected void onPause() {
	  super.onPause();
	  DiveApplication.activityPaused();
	}

    @Override
    public void onBackPressed() {
        // Close drawer if open, otherwise do default functionality
        if (mDrawerLayout.isDrawerOpen(mDrawer)) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    protected void hideActionBar() {
        getSupportActionBar().hide();
        mDrawer.setPadding(mDrawerLayout.getPaddingLeft(), 0,
                           mDrawerLayout.getPaddingRight(), mDrawerLayout.getPaddingBottom());
    }
}
