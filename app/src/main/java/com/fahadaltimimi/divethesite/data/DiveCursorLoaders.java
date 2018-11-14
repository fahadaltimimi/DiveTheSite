package com.fahadaltimimi.divethesite.data;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;

import com.fahadaltimimi.divethesite.controller.DiveSiteManager;
import com.fahadaltimimi.data.SQLiteCursorLoader;
import com.fahadaltimimi.divethesite.model.DiveSite;

public class DiveCursorLoaders {

	public static final int LOAD_DIVESITE = 0;
	public static final int LOAD_DIVELOG = 1;
	public static final int LOAD_DIVER = 2;
	public static final int LOAD_DIVESITE_PICTURES = 3;
	public static final int LOAD_SCHEDULEDDIVE = 4;
	
	public static final int LOAD_DIVELOG_BUDDIES_INDEX = 1000000;
	public static final int LOAD_DIVELOG_STOPS_INDEX = 2000000;
	public static final int LOAD_DIVER_CERTIFICATIONS_INDEX = 3000000;
	public static final int LOAD_SCHEDULEDDIVE_SITES_INDEX = 4000000;
	public static final int LOAD_SCHEDULEDDIVE_USERS_INDEX = 5000000;

	public static class DiveSiteListCursorLoader extends SQLiteCursorLoader {

		private boolean mArchives = false;
		private long mRestrictToDiverID = -1;
		private boolean mPublished, mUnpublished = false;

		private String mFilterSelection;
		private ArrayList<String> mFilterSelectionArgs;

		public DiveSiteListCursorLoader(Context context, boolean archives,
				long diverID, String filterSelection,
				ArrayList<String> filterSelectionArgs) {
			super(context);

			mArchives = archives;
			mRestrictToDiverID = diverID;

			mFilterSelection = filterSelection;
			mFilterSelectionArgs = filterSelectionArgs;
		}

		public DiveSiteListCursorLoader(Context context, boolean published,
				boolean unpublished, String filterSelection,
				ArrayList<String> filterSelectionArgs) {
			super(context);

			mPublished = published;
			mUnpublished = unpublished;

			mFilterSelection = filterSelection;
			mFilterSelectionArgs = filterSelectionArgs;
		}

		@Override
		protected Cursor loadCursor() {

			// Query the list of dive sites
			if (mPublished && !mUnpublished) {
				return DiveSiteManager.get(getContext())
						.queryPublishedDiveSites(true, mFilterSelection,
								mFilterSelectionArgs,
                                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
								mRestrictToDiverID);
			} else if (!mPublished && mUnpublished) {
				return DiveSiteManager.get(getContext())
						.queryPublishedDiveSites(false, mFilterSelection,
								mFilterSelectionArgs,
                                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
								mRestrictToDiverID);
			} else if (mArchives) {
				return DiveSiteManager.get(getContext())
						.queryArchivedDiveSites(mFilterSelection,
								mFilterSelectionArgs,
                                DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                                DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
								mRestrictToDiverID);
			} else {
				return DiveSiteManager.get(getContext()).queryVisibleDiveSites(
						mFilterSelection, mFilterSelectionArgs,
                        DiveSiteManager.MIN_LATITUDE, DiveSiteManager.MAX_LATITUDE,
                        DiveSiteManager.MIN_LONGITUDE, DiveSiteManager.MAX_LONGITUDE,
						mRestrictToDiverID);
			}
		}
	}

	public static class DiveLogListCursorLoader extends SQLiteCursorLoader {
		private long mRestrictToDiverID = -1;
		private DiveSite mRestrictToDiveSite = null;
		private boolean mUnPublishedOnly = false;

		public DiveLogListCursorLoader(Context context, long diverID, DiveSite diveSite, boolean unPublishedOnly) {
			super(context);

			mUnPublishedOnly = unPublishedOnly;

			mRestrictToDiverID = diverID;
            mRestrictToDiveSite = diveSite;
		}

		@Override
		protected Cursor loadCursor() {
            return DiveSiteManager.get(getContext()).queryDiveLogs(
                    mRestrictToDiverID, mRestrictToDiveSite, mUnPublishedOnly);
		}
	}

	public static class DiveLogBuddyListCursorLoader extends SQLiteCursorLoader {
		private long mDiveLogID = -1;

		public DiveLogBuddyListCursorLoader(Context context, long diveLogID) {
			super(context);

			mDiveLogID = diveLogID;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryDiveLogBuddies(
					mDiveLogID);
		}
	}

	public static class DiveLogStopListCursorLoader extends SQLiteCursorLoader {
		private long mDiveLogID = -1;

		public DiveLogStopListCursorLoader(Context context, long diveLogID) {
			super(context);

			mDiveLogID = diveLogID;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryDiveLogStops(mDiveLogID);
		}
	}

	public static class DiverListCursorLoader extends SQLiteCursorLoader {
		private String mFilterSelection;
		private ArrayList<String> mFilterSelectionArgs;

		public DiverListCursorLoader(Context context, String filterSelection,
				ArrayList<String> filterSelectionArgs) {
			super(context);

			mFilterSelection = filterSelection;
			mFilterSelectionArgs = filterSelectionArgs;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryFilteredDivers(
					mFilterSelection, mFilterSelectionArgs);
		}
	}

	public static class DiverCertificationListCursorLoader extends
			SQLiteCursorLoader {
		private long mDiverID = -1;

		public DiverCertificationListCursorLoader(Context context, long diverID) {
			super(context);

			mDiverID = diverID;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryDiverCertifications(
					mDiverID);
		}
	}

	public static class PicturesListCursorLoader extends SQLiteCursorLoader {

		long mDiveSiteId;

		public PicturesListCursorLoader(Context context, long diveSiteId) {
			super(context);
			mDiveSiteId = diveSiteId;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryDiveSitePictures(
					mDiveSiteId);
		}
	}
	
	public static class ScheduledDiveListCursorLoader extends SQLiteCursorLoader {
		private long mRestrictToSubmitterID = -1;
		private long mRestrictToUserID = -1;	
		private long mRestrictToDiveSiteID = -1;
		private boolean mPublished, mUnpublished = false;
		private String mTitleFilter = "";
		private String mCountryFilter = "";
		private String mStateFilter = "";
		private String mCityFilter = "";
		private String mTimeStampStartFilter = "";
		private String mTimeStampEndFilter = "";

		public ScheduledDiveListCursorLoader(Context context, boolean published,
				boolean unPublished, long submitterID, long userID,
				String titleFilter, String countryFilter, String stateFilter, String cityFilter,
				String timeStampStartFilter, String timeStampEndFilter) {
			super(context);

			mPublished = published;
			mUnpublished = unPublished;

			mRestrictToSubmitterID = submitterID;
			mRestrictToUserID = userID;
			
			mTitleFilter = titleFilter;
			mCountryFilter = countryFilter;
			mStateFilter = stateFilter;
			mCityFilter = cityFilter;
			mTimeStampStartFilter = timeStampStartFilter;
			mTimeStampEndFilter = timeStampEndFilter;
		}
		
		public ScheduledDiveListCursorLoader(Context context, boolean published,
				boolean unPublished, long submitterID, DiveSite diveSite,
				String timeStampStartFilter, String timeStampEndFilter) {
			super(context);

			mPublished = published;
			mUnpublished = unPublished;

			mRestrictToSubmitterID = submitterID;
			if (diveSite != null) {
				mRestrictToDiveSiteID = diveSite.getLocalId();
			}
			
			mTimeStampStartFilter = timeStampStartFilter;
			mTimeStampEndFilter = timeStampEndFilter;
		}

		@Override
		protected Cursor loadCursor() {
			
			if (mRestrictToUserID != -1) {
				return DiveSiteManager.get(getContext())
						.queryScheduledDiveForUser(mRestrictToSubmitterID, mRestrictToUserID,
								mPublished, mUnpublished, mTitleFilter, mCountryFilter,
								mStateFilter, mCityFilter, mTimeStampStartFilter, mTimeStampEndFilter);
			} else if (mRestrictToDiveSiteID != -1) {
				return DiveSiteManager.get(getContext())
						.queryScheduledDiveForSite(mRestrictToSubmitterID, mRestrictToDiveSiteID,
								mPublished, mUnpublished, mTimeStampStartFilter, mTimeStampEndFilter);
			} else {
				return DiveSiteManager.get(getContext())
						.queryScheduledDiveForSubmitter(mRestrictToSubmitterID, 
								mPublished, mUnpublished, mTitleFilter, mCountryFilter,
								mStateFilter, mCityFilter, mTimeStampStartFilter, mTimeStampEndFilter);
			}
		}
	}

	public static class ScheduledDiveDiveSiteListCursorLoader extends SQLiteCursorLoader {
		private long mScheduledDiveID = -1;

		public ScheduledDiveDiveSiteListCursorLoader(Context context, long scheduledDiveID) {
			super(context);

			mScheduledDiveID = scheduledDiveID;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryScheduledDiveDiveSites(mScheduledDiveID);
		}
	}
	
	public static class ScheduledDiveUserListCursorLoader extends SQLiteCursorLoader {
		private long mScheduledDiveID = -1;

		public ScheduledDiveUserListCursorLoader(Context context, long scheduledDiveID) {
			super(context);

			mScheduledDiveID = scheduledDiveID;
		}

		@Override
		protected Cursor loadCursor() {
			return DiveSiteManager.get(getContext()).queryScheduledDiveUsers(mScheduledDiveID);
		}
	}
}
