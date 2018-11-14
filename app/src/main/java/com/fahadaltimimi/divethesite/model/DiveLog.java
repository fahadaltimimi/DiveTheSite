package com.fahadaltimimi.divethesite.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.fahadaltimimi.model.ValueParameter;

public class DiveLog implements Parcelable {

    private static final SimpleDateFormat JSONDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MM/yy, HH:mm");

    private static final String JSON_TAG_DIVE_LOG_ID = "LOG_ID";
	private static final String JSON_TAG_DIVE_LOG_DIVE_SITE_ID = "SITE_ID";
	private static final String JSON_TAG_DIVE_LOG_USER_ID = "USER_ID";
	private static final String JSON_TAG_DIVE_LOG_USERNAME = "USERNAME";
	private static final String JSON_TAG_DIVE_LOG_TIMESTAMP = "TIMESTAMP";
	private static final String JSON_TAG_DIVE_LOG_AIR_TYPE = "AIR_TYPE";
	private static final String JSON_TAG_DIVE_LOG_START_PRESSURE = "START_PRESSURE";
	private static final String JSON_TAG_DIVE_LOG_END_PRESSURE = "END_PRESSURE";
	private static final String JSON_TAG_DIVE_LOG_DIVE_TIME = "DIVE_TIME";
	private static final String JSON_TAG_DIVE_LOG_MAX_DEPTH_VALUE = "MAX_DEPTH_VALUE";
	private static final String JSON_TAG_DIVE_LOG_MAX_DEPTH_UNITS = "MAX_DEPTH_UNITS";
	private static final String JSON_TAG_DIVE_LOG_AVERAGE_DEPTH_VALUE = "AVERAGE_DEPTH_VALUE";
	private static final String JSON_TAG_DIVE_LOG_AVERAGE_DEPTH_UNITS = "AVERAGE_DEPTH_UNITS";
	private static final String JSON_TAG_DIVE_LOG_SURFACE_TEMPERATURE_VALUE = "SURFACE_TEMPERATURE_VALUE";
	private static final String JSON_TAG_DIVE_LOG_SURFACE_TEMPERATURE_UNITS = "SURFACE_TEMPERATURE_UNITS";
	private static final String JSON_TAG_DIVE_LOG_WATER_TEMPERATURE_VALUE = "WATER_TEMPERATURE_VALUE";
	private static final String JSON_TAG_DIVE_LOG_WATER_TEMPERATURE_UNITS = "WATER_TEMPERATURE_UNITS";
	private static final String JSON_TAG_DIVE_LOG_VISIBILITY_VALUE = "VISIBILITY_VALUE";
	private static final String JSON_TAG_DIVE_LOG_VISIBILITY_UNITS = "VISIBILITY_UNITS";
	private static final String JSON_TAG_DIVE_LOG_WEIGHTS_REQUIRED_VALUE = "WEIGHTS_REQUIRED_VALUE";
	private static final String JSON_TAG_DIVE_LOG_WEIGHTS_REQUIRED_UNITS = "WEIGHTS_REQUIRED_UNITS";
	private static final String JSON_TAG_DIVE_LOG_SURFACE_TIME_VALUE = "SURFACE_TIME_VALUE";
	private static final String JSON_TAG_DIVE_LOG_RATING = "RATING";
	private static final String JSON_TAG_DIVE_LOG_COMMENTS = "COMMENTS";
	private static final String JSON_TAG_DIVE_LOG_IS_COURSE = "IS_COURSE";
	private static final String JSON_TAG_DIVE_LOG_IS_PHOTO_VIDEO = "IS_PHOTO_VIDEO";
	private static final String JSON_TAG_DIVE_LOG_IS_ICE = "IS_ICE";
	private static final String JSON_TAG_DIVE_LOG_IS_DEEP = "IS_DEEP";
	private static final String JSON_TAG_DIVE_LOG_IS_INSTRUCTING = "IS_INSTRUCTING";
	private static final String JSON_TAG_DIVE_LOG_IS_NIGHT = "IS_NIGHT";
	private static final String JSON_TAG_DIVE_LOG_LAST_MODIFIED_ONLINE = "LAST_MODIFIED_ONLINE";

	// DiveLog's properties
	private long mLocalId;
	private long mOnlineId;
	private long mDiveSiteLocalId;
	private long mDiveSiteOnlineId;
	private long mUserId;
	private String mUsername;

	private Date mTimestamp;
	private String mAirType;
	private char mStartPressure;
	private char mEndPressure;
    private ValueParameter mStartAir;
    private ValueParameter mEndAir;
	private int mDiveTime;
	private ValueParameter mMaxDepth;
	private ValueParameter mAverageDepth;
	private ValueParameter mSurfaceTemperature;
	private ValueParameter mWaterTemperature;
	private ValueParameter mVisibility;
	private ValueParameter mWeightsRequired;
	private int mSurfaceTime;
	private double mRating;
	private String mComments;
	private boolean mIsCourse;
	private boolean mIsPhotoVideo;
	private boolean mIsIce;
	private boolean mIsDeep;
	private boolean mIsInstructing;
	private boolean mIsNight;
	private boolean mIsPublished;
	private Date mLastModifiedOnline;
	private boolean mRequiresRefresh;

    private int mDiveLogCountWhenRetreived;
    private int mDiveLogTotalMinutesWhenRetreived;

	private DiveSite mDiveSite;

	private ArrayList<DiveLogBuddy> mBuddies;
	private ArrayList<DiveLogStop> mStops;

	public static final int DIVE_LOG_FIELD_COUNT = 40;
	public static final int DIVE_LOG_LOCAL_ID_INDEX = 0;
	public static final int DIVE_LOG_ONLINE_ID_INDEX = 1;
	public static final int DIVE_LOG_DIVE_SITE_ID_INDEX = 2;
	public static final int DIVE_LOG_USER_ID_INDEX = 3;
	public static final int DIVE_LOG_USERNAME_INDEX = 4;
	public static final int DIVE_LOG_TIMESTAMP_INDEX = 5;
	public static final int DIVE_LOG_AIR_TYPE_INDEX = 6;
	public static final int DIVE_LOG_START_PRESSURE_INDEX = 7;
	public static final int DIVE_LOG_END_PRESSURE_INDEX = 8;
    public static final int DIVE_LOG_START_AIR_VALUE_INDEX = 9;
    public static final int DIVE_LOG_START_AIR_UNITS_INDEX = 10;
    public static final int DIVE_LOG_END_AIR_VALUE_INDEX = 11;
    public static final int DIVE_LOG_END_AIR_UNITS_INDEX = 12;
	public static final int DIVE_LOG_DIVE_TIME_INDEX = 13;
	public static final int DIVE_LOG_MAX_DEPTH_VALUE_INDEX = 14;
	public static final int DIVE_LOG_MAX_DEPTH_UNITS_INDEX = 15;
	public static final int DIVE_LOG_AVERAGE_DEPTH_VALUE_INDEX = 16;
	public static final int DIVE_LOG_AVERAGE_DEPTH_UNITS_INDEX = 17;
	public static final int DIVE_LOG_SURFACE_TEMPERATURE_VALUE_INDEX = 18;
	public static final int DIVE_LOG_SURFACE_TEMPERATURE_UNITS_INDEX = 19;
	public static final int DIVE_LOG_WATER_TEMPERATURE_VALUE_INDEX = 20;
	public static final int DIVE_LOG_WATER_TEMPERATURE_UNITS_INDEX = 21;
	public static final int DIVE_LOG_VISIBILITY_VALUE_INDEX = 22;
	public static final int DIVE_LOG_VISIBILITY_UNITS_INDEX = 23;
	public static final int DIVE_LOG_WEIGHTS_REQUIRED_VALUE_INDEX = 24;
	public static final int DIVE_LOG_WEIGHTS_REQUIRED_UNITS_INDEX = 25;
	public static final int DIVE_LOG_SURFACE_TIME_VALUE_INDEX = 26;
	public static final int DIVE_LOG_RATING_INDEX = 27;
	public static final int DIVE_LOG_COMMENTS_INDEX = 28;
	public static final int DIVE_LOG_IS_COURSE_INDEX = 29;
	public static final int DIVE_LOG_IS_PHOTO_VIDEO_INDEX = 30;
	public static final int DIVE_LOG_IS_ICE_INDEX = 31;
	public static final int DIVE_LOG_IS_DEEP_INDEX = 32;
	public static final int DIVE_LOG_IS_INSTRUCTING_INDEX = 33;
	public static final int DIVE_LOG_IS_NIGHT_INDEX = 34;
	public static final int DIVE_LOG_LAST_MODIFIED_ONLINE_INDEX = 35;
	public static final int DIVE_LOG_BUDDY_DATA_START_POS_INDEX = 36;
	public static final int DIVE_LOG_BUDDY_DATA_COUNT_INDEX = 37;
	public static final int DIVE_LOG_STOP_DATA_START_POS_INDEX = 38;
	public static final int DIVE_LOG_STOP_DATA_COUNT_INDEX = 39;

	public static final String DIVE_LOG_ID_PARAM = "LOG_ID";
	public static final String DIVE_LOG_DIVE_SITE_ID_PARAM = "SITE_ID";
	public static final String DIVE_LOG_USER_ID_PARAM = "USER_ID";
	public static final String DIVE_LOG_TIMESTAMP_PARAM = "TIMESTAMP";
	public static final String DIVE_LOG_AIR_TYPE_PARAM = "AIR_TYPE";
	public static final String DIVE_LOG_START_PRESSURE_PARAM = "START_PRESSURE";
	public static final String DIVE_LOG_END_PRESSURE_PARAM = "END_PRESSURE";
	public static final String DIVE_LOG_DIVE_TIME_PARAM = "DIVE_TIME";
	public static final String DIVE_LOG_MAX_DEPTH_VALUE_PARAM = "MAX_DEPTH_VALUE";
	public static final String DIVE_LOG_MAX_DEPTH_UNITS_PARAM = "MAX_DEPTH_UNITS";
	public static final String DIVE_LOG_AVERAGE_DEPTH_VALUE_PARAM = "AVERAGE_DEPTH_VALUE";
	public static final String DIVE_LOG_AVERAGE_DEPTH_UNITS_PARAM = "AVERAGE_DEPTH_UNITS";
	public static final String DIVE_LOG_SURFACE_TEMPERATURE_VALUE_PARAM = "SURFACE_TEMPERATURE_VALUE";
	public static final String DIVE_LOG_SURFACE_TEMPERATURE_UNITS_PARAM = "SURFACE_TEMPERATURE_UNITS";
	public static final String DIVE_LOG_WATER_TEMPERATURE_VALUE_PARAM = "WATER_TEMPERATURE_VALUE";
	public static final String DIVE_LOG_WATER_TEMPERATURE_UNITS_PARAM = "WATER_TEMPERATURE_UNITS";
	public static final String DIVE_LOG_VISIBILITY_VALUE_PARAM = "VISIBILITY_VALUE";
	public static final String DIVE_LOG_VISIBILITY_UNITS_PARAM = "VISIBILITY_UNITS";
	public static final String DIVE_LOG_WEIGHTS_REQUIRED_VALUE_PARAM = "WEIGHTS_REQUIRED_VALUE";
	public static final String DIVE_LOG_WEIGHTS_REQUIRED_UNITS_PARAM = "WEIGHTS_REQUIRED_UNITS";
	public static final String DIVE_LOG_SURFACE_TIME_VALUE_PARAM = "SURFACE_TIME_VALUE";
	public static final String DIVE_LOG_RATING_PARAM = "RATING";
	public static final String DIVE_LOG_COMMENTS_PARAM = "COMMENTS";
	public static final String DIVE_LOG_IS_COURSE_PARAM = "IS_COURSE";
	public static final String DIVE_LOG_IS_PHOTO_VIDEO_PARAM = "IS_PHOTO_VIDEO";
	public static final String DIVE_LOG_IS_ICE_PARAM = "IS_ICE";
	public static final String DIVE_LOG_IS_DEEP_PARAM = "IS_DEEP";
	public static final String DIVE_LOG_IS_INSTRUCTING_PARAM = "IS_INSTUCTING";
	public static final String DIVE_LOG_IS_NIGHT_PARAM = "IS_NIGHT";
    public static final String DIVE_LOG_MIN_LATITUDE_PARAM = "MIN_LATITUDE";
    public static final String DIVE_LOG_MAX_LATITUDE_PARAM = "MAX_LATITUDE";
    public static final String DIVE_LOG_MIN_LONGITUDE_PARAM = "MIN_LONGITUDE";
    public static final String DIVE_LOG_MAX_LONGITUDE_PARAM = "MAX_LONGITUDE";
	public static final String DIVE_LOG_BUDDY_DATA_COUNT_PARAM = "BUDDY_COUNT";
	public static final String DIVE_LOG_STOP_DATA_COUNT_PARAM = "STOP_COUNT";
	public static final String DIVE_LOG_START_INDEX_LOAD_PARAM = "START_INDEX_LOAD";
	public static final String DIVE_LOG_COUNT_LOAD_PARAM = "COUNT_LOAD";

    public static final String TAG_DIVE_LOG_START_AIR_VALUE = "START_AIR_VALUE";
    public static final String TAG_DIVE_LOG_START_AIR_UNITS = "START_AIR_UNITS";
    public static final String TAG_DIVE_LOG_END_AIR_VALUE = "END_AIR_VALUE";
    public static final String TAG_DIVE_LOG_END_AIR_UNITS = "END_AIR_UNITS";
    public static final String TAG_DIVE_LOG_LOG_COUNT = "LOG_COUNT";
    public static final String TAG_DIVE_LOG_TOTAL_MINUTES = "TOTAL_MINUTES";

	public DiveLog() {
		mLocalId = -1;
		mOnlineId = -1;
		mDiveSiteLocalId = -1;
		mDiveSiteOnlineId = -1;
		mUserId = -1;
		mUsername = "";
		mTimestamp = new Date();
		mAirType = "";
		mStartPressure = ' ';
		mEndPressure = ' ';
        mStartAir = new ValueParameter(0, "");
        mEndAir = new ValueParameter(0, "");
		mDiveTime = 0;
		mMaxDepth = new ValueParameter(0, "");
		mAverageDepth = new ValueParameter(0, "");
		mSurfaceTemperature = new ValueParameter(0, "");
		mWaterTemperature = new ValueParameter(0, "");
		mVisibility = new ValueParameter(0, "");
		mWeightsRequired = new ValueParameter(0, "");
		mSurfaceTime = 0;
		mRating = -1;
		mComments = "";
		mIsCourse = false;
		mIsPhotoVideo = false;
		mIsIce = false;
		mIsDeep = false;
		mIsInstructing = false;
		mIsNight = false;
		mIsPublished = false;
		mRequiresRefresh = false;
		mLastModifiedOnline = new Date(0);
		mBuddies = new ArrayList<DiveLogBuddy>();
		mStops = new ArrayList<DiveLogStop>();
		mDiveSite = null;
        mDiveLogCountWhenRetreived = 0;
        mDiveLogTotalMinutesWhenRetreived = 0;
	}

	public DiveLog(JSONObject json) {
		try {
			mLocalId = -1;
			mDiveSiteLocalId = -1;
			mBuddies = new ArrayList<DiveLogBuddy>();
			mStops = new ArrayList<DiveLogStop>();
			mDiveSite = null;
			mRequiresRefresh = false;

			mOnlineId = json.getInt(JSON_TAG_DIVE_LOG_ID);
			mDiveSiteOnlineId = json.getInt(JSON_TAG_DIVE_LOG_DIVE_SITE_ID);
			mUserId = json.getInt(JSON_TAG_DIVE_LOG_USER_ID);
			mUsername = json.getString(JSON_TAG_DIVE_LOG_USERNAME);

			mTimestamp = new Date(json.getLong(JSON_TAG_DIVE_LOG_TIMESTAMP));

			mAirType = json.getString(JSON_TAG_DIVE_LOG_AIR_TYPE);
			
			if (json.getString(JSON_TAG_DIVE_LOG_START_PRESSURE).length() > 0) {
				mStartPressure = json.getString(JSON_TAG_DIVE_LOG_START_PRESSURE)
						.charAt(0);
			} else {
				mStartPressure = ' ';
			}
			
			if (json.getString(JSON_TAG_DIVE_LOG_END_PRESSURE).length() > 0) {
				mEndPressure = json.getString(JSON_TAG_DIVE_LOG_END_PRESSURE)
						.charAt(0);
			} else {
				mEndPressure = ' ';
			}

            mStartAir = new ValueParameter(
                    json.getInt(TAG_DIVE_LOG_START_AIR_VALUE),
                    json.getString(TAG_DIVE_LOG_START_AIR_UNITS));

            mEndAir = new ValueParameter(
                    json.getInt(TAG_DIVE_LOG_END_AIR_VALUE),
                    json.getString(TAG_DIVE_LOG_END_AIR_UNITS));
				
			mDiveTime = json.getInt(JSON_TAG_DIVE_LOG_DIVE_TIME);
			mMaxDepth = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_MAX_DEPTH_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_MAX_DEPTH_UNITS));
			mAverageDepth = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_AVERAGE_DEPTH_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_AVERAGE_DEPTH_UNITS));
			mSurfaceTemperature = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_SURFACE_TEMPERATURE_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_SURFACE_TEMPERATURE_UNITS));
			mWaterTemperature = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_WATER_TEMPERATURE_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_WATER_TEMPERATURE_UNITS));
			mVisibility = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_VISIBILITY_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_VISIBILITY_UNITS));
			mWeightsRequired = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_WEIGHTS_REQUIRED_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_WEIGHTS_REQUIRED_UNITS));
			mSurfaceTime = json.getInt(JSON_TAG_DIVE_LOG_SURFACE_TIME_VALUE);
			mRating = json.getDouble(JSON_TAG_DIVE_LOG_RATING);
			mComments = json.getString(JSON_TAG_DIVE_LOG_COMMENTS);
			mIsCourse = json.getInt(JSON_TAG_DIVE_LOG_IS_COURSE) == 1;
			mIsPhotoVideo = json.getInt(JSON_TAG_DIVE_LOG_IS_PHOTO_VIDEO) == 1;
			mIsIce = json.getInt(JSON_TAG_DIVE_LOG_IS_ICE) == 1;
			mIsDeep = json.getInt(JSON_TAG_DIVE_LOG_IS_DEEP) == 1;
			mIsInstructing = json.getInt(JSON_TAG_DIVE_LOG_IS_INSTRUCTING) == 1;
			mIsNight = json.getInt(JSON_TAG_DIVE_LOG_IS_NIGHT) == 1;
			mIsPublished = false;

			mLastModifiedOnline = new Date(0);

			try {
				mLastModifiedOnline = 
					JSONDateFormat.parse(json.getString(JSON_TAG_DIVE_LOG_LAST_MODIFIED_ONLINE));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            if (json.has(TAG_DIVE_LOG_LOG_COUNT)) {
                mDiveLogCountWhenRetreived = json.getInt(TAG_DIVE_LOG_LOG_COUNT);
            } else {
                mDiveLogCountWhenRetreived = 0;
            }

            if (json.has(TAG_DIVE_LOG_TOTAL_MINUTES)) {
                mDiveLogTotalMinutesWhenRetreived = json.getInt(TAG_DIVE_LOG_TOTAL_MINUTES);
            } else {
                mDiveLogTotalMinutesWhenRetreived = 0;
            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DiveLog(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mDiveSiteLocalId = source.readLong();
		mDiveSiteOnlineId = source.readLong();
		mUserId = source.readLong();
		mUsername = source.readString();
		mTimestamp = new Date(source.readLong());
		mAirType = source.readString();
		mStartPressure = (char) source.readInt();
		mEndPressure = (char) source.readInt();
        mStartAir = source.readParcelable(ValueParameter.class.getClassLoader());
        mEndAir = source.readParcelable(ValueParameter.class.getClassLoader());
		mDiveTime = source.readInt();
		mMaxDepth = source.readParcelable(ValueParameter.class.getClassLoader());
		mAverageDepth = source.readParcelable(ValueParameter.class
				.getClassLoader());
		mSurfaceTemperature = source.readParcelable(ValueParameter.class
				.getClassLoader());
		mWaterTemperature = source.readParcelable(ValueParameter.class
				.getClassLoader());
		mVisibility = source.readParcelable(ValueParameter.class
				.getClassLoader());
		mWeightsRequired = source.readParcelable(ValueParameter.class
				.getClassLoader());
		mSurfaceTime = source.readInt();
		mRating = source.readDouble();
		mComments = source.readString();
		mIsCourse = source.readByte() != 0;
		mIsPhotoVideo = source.readByte() != 0;
		mIsIce = source.readByte() != 0;
		mIsDeep = source.readByte() != 0;
		mIsInstructing = source.readByte() != 0;
		mIsNight = source.readByte() != 0;
		mIsPublished = source.readByte() != 0;
		mRequiresRefresh = source.readByte() != 0;
		mLastModifiedOnline = new Date(source.readLong());
        mDiveLogCountWhenRetreived = source.readInt();
        mDiveLogTotalMinutesWhenRetreived = source.readInt();
		mBuddies = new ArrayList<DiveLogBuddy>();
		source.readTypedList(mBuddies, DiveLogBuddy.CREATOR);
		mStops = new ArrayList<DiveLogStop>();
		source.readTypedList(mStops, DiveLogStop.CREATOR);

		boolean hasDiveSite = source.readByte() != 0;
		if (hasDiveSite) {
			mDiveSite = source.readParcelable(DiveSite.class.getClassLoader());
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeLong(mDiveSiteLocalId);
		dest.writeLong(mDiveSiteOnlineId);
		dest.writeLong(mUserId);
		dest.writeString(mUsername);
		dest.writeLong(mTimestamp.getTime());
		dest.writeString(mAirType);
		dest.writeInt(mStartPressure);
		dest.writeInt(mEndPressure);
        dest.writeParcelable(mStartAir, flags);
        dest.writeParcelable(mEndAir, flags);
		dest.writeInt(mDiveTime);
		dest.writeParcelable(mMaxDepth, flags);
		dest.writeParcelable(mAverageDepth, flags);
		dest.writeParcelable(mSurfaceTemperature, flags);
		dest.writeParcelable(mWaterTemperature, flags);
		dest.writeParcelable(mVisibility, flags);
		dest.writeParcelable(mWeightsRequired, flags);
		dest.writeInt(mSurfaceTime);
		dest.writeDouble(mRating);
		dest.writeString(mComments);
		dest.writeByte((byte) (mIsCourse ? 1 : 0));
		dest.writeByte((byte) (mIsPhotoVideo ? 1 : 0));
		dest.writeByte((byte) (mIsIce ? 1 : 0));
		dest.writeByte((byte) (mIsDeep ? 1 : 0));
		dest.writeByte((byte) (mIsInstructing ? 1 : 0));
		dest.writeByte((byte) (mIsNight ? 1 : 0));
		dest.writeByte((byte) (mIsPublished ? 1 : 0));
		dest.writeByte((byte) (mRequiresRefresh ? 1 : 0));
		dest.writeLong(mLastModifiedOnline.getTime());
        dest.writeInt(mDiveLogCountWhenRetreived);
        dest.writeInt(mDiveLogTotalMinutesWhenRetreived);
		dest.writeTypedList(mBuddies);
		dest.writeTypedList(mStops);

		if (mDiveSite == null) {
			dest.writeByte((byte) 0);
		} else {
			dest.writeByte((byte) 1);
			dest.writeParcelable(mDiveSite, flags);
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DiveLog> CREATOR = new Parcelable.Creator<DiveLog>() {
		@Override
		public DiveLog createFromParcel(Parcel in) {
			return new DiveLog(in);
		}

		@Override
		public DiveLog[] newArray(int size) {
			return new DiveLog[size];
		}
	};

	public String[] getFieldsAsStrings() {
		String[] diveLogFields = new String[DIVE_LOG_FIELD_COUNT
				+ mBuddies.size() * DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT
				+ mStops.size() * DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT];

		diveLogFields[DIVE_LOG_LOCAL_ID_INDEX] = String.valueOf(mLocalId);
		diveLogFields[DIVE_LOG_ONLINE_ID_INDEX] = String.valueOf(mOnlineId);
		diveLogFields[DIVE_LOG_DIVE_SITE_ID_INDEX] = String.valueOf(mDiveSiteOnlineId);
		diveLogFields[DIVE_LOG_USER_ID_INDEX] = String.valueOf(mUserId);
		diveLogFields[DIVE_LOG_USERNAME_INDEX] = mUsername;
		diveLogFields[DIVE_LOG_TIMESTAMP_INDEX] = String.valueOf(mTimestamp.getTime());
		diveLogFields[DIVE_LOG_AIR_TYPE_INDEX] = mAirType;
		diveLogFields[DIVE_LOG_START_PRESSURE_INDEX] = String.valueOf(mStartPressure);
		diveLogFields[DIVE_LOG_END_PRESSURE_INDEX] = String.valueOf(mEndPressure);
        diveLogFields[DIVE_LOG_START_AIR_VALUE_INDEX] = String.valueOf(mStartAir.getValue());
        diveLogFields[DIVE_LOG_START_AIR_UNITS_INDEX] = mStartAir.getUnits();
        diveLogFields[DIVE_LOG_END_AIR_VALUE_INDEX] = String.valueOf(mEndAir.getValue());
        diveLogFields[DIVE_LOG_END_AIR_UNITS_INDEX] = mEndAir.getUnits();
		diveLogFields[DIVE_LOG_DIVE_TIME_INDEX] = String.valueOf(mDiveTime);
		diveLogFields[DIVE_LOG_MAX_DEPTH_VALUE_INDEX] = String.valueOf(mMaxDepth.getValue());
		diveLogFields[DIVE_LOG_MAX_DEPTH_UNITS_INDEX] = mMaxDepth.getUnits();
		diveLogFields[DIVE_LOG_AVERAGE_DEPTH_VALUE_INDEX] = String
				.valueOf(mAverageDepth.getValue());
		diveLogFields[DIVE_LOG_AVERAGE_DEPTH_UNITS_INDEX] = mAverageDepth
				.getUnits();
		diveLogFields[DIVE_LOG_SURFACE_TEMPERATURE_VALUE_INDEX] = String
				.valueOf(mSurfaceTemperature.getValue());
		diveLogFields[DIVE_LOG_SURFACE_TEMPERATURE_UNITS_INDEX] = mSurfaceTemperature
				.getUnits();
		diveLogFields[DIVE_LOG_WATER_TEMPERATURE_VALUE_INDEX] = String
				.valueOf(mWaterTemperature);
		diveLogFields[DIVE_LOG_WATER_TEMPERATURE_UNITS_INDEX] = mWaterTemperature
				.getUnits();
		diveLogFields[DIVE_LOG_VISIBILITY_VALUE_INDEX] = String
				.valueOf(mVisibility.getValue());
		diveLogFields[DIVE_LOG_VISIBILITY_UNITS_INDEX] = mVisibility.getUnits();
		diveLogFields[DIVE_LOG_WEIGHTS_REQUIRED_VALUE_INDEX] = String
				.valueOf(mWeightsRequired.getValue());
		diveLogFields[DIVE_LOG_WEIGHTS_REQUIRED_UNITS_INDEX] = mWeightsRequired
				.getUnits();
		diveLogFields[DIVE_LOG_SURFACE_TIME_VALUE_INDEX] = String
				.valueOf(mSurfaceTime);
		diveLogFields[DIVE_LOG_RATING_INDEX] = String.valueOf(mRating);
		diveLogFields[DIVE_LOG_COMMENTS_INDEX] = mComments;
		diveLogFields[DIVE_LOG_IS_COURSE_INDEX] = mIsCourse ? "1" : "0";
		diveLogFields[DIVE_LOG_IS_PHOTO_VIDEO_INDEX] = mIsPhotoVideo ? "1"
				: "0";
		diveLogFields[DIVE_LOG_IS_ICE_INDEX] = mIsIce ? "1" : "0";
		diveLogFields[DIVE_LOG_IS_DEEP_INDEX] = mIsDeep ? "1" : "0";
		diveLogFields[DIVE_LOG_IS_INSTRUCTING_INDEX] = mIsInstructing ? "1"
				: "0";
		diveLogFields[DIVE_LOG_IS_NIGHT_INDEX] = mIsNight ? "1" : "0";
		diveLogFields[DIVE_LOG_BUDDY_DATA_START_POS_INDEX] = String
				.valueOf(getBuddyFieldsStartPosition());
		diveLogFields[DIVE_LOG_BUDDY_DATA_COUNT_INDEX] = String
				.valueOf(mBuddies.size());
		diveLogFields[DIVE_LOG_STOP_DATA_START_POS_INDEX] = String
				.valueOf(getStopFieldsStartPosition());
		diveLogFields[DIVE_LOG_STOP_DATA_COUNT_INDEX] = String.valueOf(mStops
				.size());

		// Set buddy fields
		for (int i = 0; i < mBuddies.size(); i++) {
			String[] buddyFields = mBuddies.get(i).getFieldsAsStrings();
			System.arraycopy(buddyFields, 0, diveLogFields,
					getBuddyFieldsStartPosition()
							+ (i * DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT),
					buddyFields.length);
		}

		// Set stop fields
		for (int i = 0; i < mStops.size(); i++) {
			String[] stopFields = mStops.get(i).getFieldsAsStrings();
			System.arraycopy(stopFields, 0, diveLogFields,
					getStopFieldsStartPosition()
							+ (i * DiveLogStop.DIVE_LOG_STOP_FIELD_COUNT),
					stopFields.length);
		}

		return diveLogFields;
	}

	public int getBuddyFieldsStartPosition() {
		// When getFieldsAsStrings is called, this method called to determine
		// when buddy fields start in string array
		// Buddy fields start after Dive Log fields
		return DIVE_LOG_FIELD_COUNT;
	}

	public int getStopFieldsStartPosition() {
		// When getFieldsAsStrings is called, this method called to determine
		// when stop fields start in string array
		// Stop fields start after Dive Log Buddy fields
		return DIVE_LOG_FIELD_COUNT
				+ (mBuddies.size() * DiveLogBuddy.DIVE_LOG_BUDDY_FIELD_COUNT);
	}
	
	public String getShareSummary() {
		String summary;
		if (mDiveSite != null) {
			summary = mDiveSite.getName() + " - " + getTimestampStringShort() + "\n";
		} else {
			summary = getTimestampStringShort() + "\n";
		}
		
		summary = summary + "Rating: " + String.valueOf(getRating()) + "/5\n";
		summary = summary + "Visibility: " + mVisibility.toString() + "\n";
		summary = summary + "Dive Time: " + String.valueOf(mDiveTime) + " min.\n";
		summary = summary + "Depths: " + mMaxDepth.toString() + " (M) " 
						  			   + mAverageDepth.toString() + " (A) \n";
		summary = summary + "Temp: " + mSurfaceTemperature.toString() + " (A) " 
						  			 + mWaterTemperature.toString() + " (W)\n\n";
		summary = summary + mComments;
		if (mDiveSite != null) {
			summary = summary + "\n\nhttps://www.google.com/maps/preview?q=loc:" 
					+ String.valueOf(mDiveSite.getLatitude()) + "," 
					+ String.valueOf(mDiveSite.getLongitude());
		}
		
		return summary.trim();
	}

	public long getLocalId() {
		return mLocalId;
	}

	public void setLocalId(long id) {
		mLocalId = id;
	}

	public long getOnlineId() {
		return mOnlineId;
	}

	public void setOnlineId(long id) {
		mOnlineId = id;
	}

	public long getDiveSiteLocalId() {
		return mDiveSiteLocalId;
	}

	public void setDiveSiteLocalId(long diveSiteLocalId) {
		mDiveSiteLocalId = diveSiteLocalId;
	}

	public long getDiveSiteOnlineId() {
		return mDiveSiteOnlineId;
	}

	public void setDiveSiteOnlineId(long diveSiteOnlineId) {
		mDiveSiteOnlineId = diveSiteOnlineId;
	}

	public long getUserId() {
		return mUserId;
	}

	public void setUserId(long userId) {
		mUserId = userId;
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String username) {
		mUsername = username;
	}

	public Date getTimestamp() {
		return mTimestamp;
	}

	public String getTimestampStringShort() {
		return shortDateFormat.format(mTimestamp);
	}
	
	public void setTimestamp(Date timestamp) {
		mTimestamp = timestamp;
	}

	public String getAirType() {
		return mAirType;
	}

	public void setAirType(String airType) {
		mAirType = airType;
	}

	public char getStartPressure() {
		return mStartPressure;
	}

	public void setStartPressure(char startPressure) {
		mStartPressure = startPressure;
	}

	public char getEndPressure() {
		return mEndPressure;
	}

	public void setEndPressure(char endPressure) {
		mEndPressure = endPressure;
	}

    public ValueParameter getStartAir() {
        return mStartAir;
    }

    public void setStartAir(ValueParameter startAir) {
        mStartAir = startAir;
    }

    public ValueParameter getEndAir() {
        return mEndAir;
    }

    public void setEndAir(ValueParameter endAir) {
        mEndAir = endAir;
    }

	public int getDiveTime() {
		return mDiveTime;
	}

	public void setDiveTime(int diveTime) {
		mDiveTime = diveTime;
	}

	public ValueParameter getMaxDepth() {
		return mMaxDepth;
	}

	public void setMaxDepth(ValueParameter maxDepth) {
		mMaxDepth = maxDepth;
	}

	public ValueParameter getAverageDepth() {
		return mAverageDepth;
	}

	public void setAverageDepth(ValueParameter averageDepth) {
		mAverageDepth = averageDepth;
	}

	public ValueParameter getSurfaceTemperature() {
		return mSurfaceTemperature;
	}

	public void setSurfaceTemperature(ValueParameter surfaceTemperature) {
		mSurfaceTemperature = surfaceTemperature;
	}

	public ValueParameter getWaterTemperature() {
		return mWaterTemperature;
	}

	public void setWaterTemperature(ValueParameter waterTemperature) {
		mWaterTemperature = waterTemperature;
	}

	public ValueParameter getVisibility() {
		return mVisibility;
	}

	public void setVisibility(ValueParameter visibility) {
		mVisibility = visibility;
	}

	public ValueParameter getWeightsRequired() {
		return mWeightsRequired;
	}

	public void setWeightsRequired(ValueParameter weightsRequired) {
		mWeightsRequired = weightsRequired;
	}

	public int getSurfaceTime() {
		return mSurfaceTime;
	}

	public void setSurfaceTime(int surfaceTime) {
		mSurfaceTime = surfaceTime;
	}

	public double getRating() {
		return mRating;
	}

	public void setRating(double rating) {
		mRating = rating;
	}

	public String getComments() {
		return mComments;
	}

	public void setComments(String comments) {
		mComments = comments;
	}

	public boolean isCourse() {
		return mIsCourse;
	}

	public void setIsCourse(boolean isCourse) {
		mIsCourse = isCourse;
	}

	public boolean isPhotoVideo() {
		return mIsPhotoVideo;
	}

	public void setIsPhotoVideo(boolean isPhotoVideo) {
		mIsPhotoVideo = isPhotoVideo;
	}

	public boolean isIce() {
		return mIsIce;
	}

	public void setIsIce(boolean isIce) {
		mIsIce = isIce;
	}

	public boolean isDeep() {
		return mIsDeep;
	}

	public void setIsDeep(boolean isDeep) {
		mIsDeep = isDeep;
	}

	public boolean isInstructing() {
		return mIsInstructing;
	}

	public void setIsInstructing(boolean isInstructing) {
		mIsInstructing = isInstructing;
	}

	public boolean isNight() {
		return mIsNight;
	}

	public void setIsNight(boolean isNight) {
		mIsNight = isNight;
	}

	public boolean isPublished() {
		return mIsPublished;
	}

	public void setPublished(boolean isPublished) {
		mIsPublished = isPublished;
	}
	
	public boolean requiresRefresh() {
		return mRequiresRefresh;
	}

	public void setRequiresRefresh(boolean requiresRefresh) {
		mRequiresRefresh = requiresRefresh;
	}
	
	public Date getLastModifiedOnline() {
		return mLastModifiedOnline;
	}

	public void setLastModifiedOnline(Date dateModified) {
		mLastModifiedOnline = dateModified;
	}

    public int getDiveLogCountWhenRetreived() {
        return mDiveLogCountWhenRetreived;
    }

    public void setDiveLogCountWhenRetreived(int logCountWhenRetreived) {
        mDiveLogCountWhenRetreived = logCountWhenRetreived;
    }

    public int getDiveLogTotalMinutesWhenRetreived() {
        return mDiveLogTotalMinutesWhenRetreived;
    }

    public void setDiveLogTotalMinutesWhenRetreived(int totalMinutesWhenRetreived) {
        mDiveLogTotalMinutesWhenRetreived = totalMinutesWhenRetreived;
    }

	public ArrayList<DiveLogBuddy> getBuddies() {
		return mBuddies;
	}

	public void setBuddies(ArrayList<DiveLogBuddy> buddies) {
		mBuddies = buddies;
	}

	public int getBuddyIndex(DiveLogBuddy buddy) {
		int index = -1;
		
		for (int i = 0; i < mBuddies.size(); i++) {
			if (mBuddies.get(i).getLocalId() == buddy.getLocalId() &&
					mBuddies.get(i).getOnlineId() == buddy.getOnlineId()) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	public ArrayList<DiveLogStop> getStops() {
		return mStops;
	}
	
	public void setStops(ArrayList<DiveLogStop> stops) {
		mStops = stops;
	}

	public int getStopIndex(DiveLogStop stop) {
		int index = -1;
		
		for (int i = 0; i < mStops.size(); i++) {
			if (mStops.get(i).getLocalId() == stop.getLocalId() &&
					mStops.get(i).getOnlineId() == stop.getOnlineId()) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	public DiveSite getDiveSite() {
		return mDiveSite;
	}

	public void setDiveSite(DiveSite diveSite) {
		mDiveSite = diveSite;
	}
}
