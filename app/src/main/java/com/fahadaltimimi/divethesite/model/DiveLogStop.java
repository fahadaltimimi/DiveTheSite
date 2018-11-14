package com.fahadaltimimi.divethesite.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.fahadaltimimi.model.ValueParameter;

public class DiveLogStop implements Parcelable {

	private static final String JSON_TAG_DIVE_LOG_STOP_ID = "LOG_STOP_ID";
	private static final String JSON_TAG_DIVE_LOG_STOP_LOCAL_ID = "LOG_STOP_LOCAL_ID";
	private static final String JSON_TAG_DIVE_LOG_STOP_LOG_ID = "LOG_STOP_LOG_ID";
	private static final String JSON_TAG_DIVE_LOG_STOP_TIME = "LOG_STOP_TIME";
	private static final String JSON_TAG_DIVE_LOG_DEPTH_VALUE = "LOG_STOP_DEPTH_VALUE";
	private static final String JSON_TAG_DIVE_LOG_DEPTH_UNITS = "LOG_STOP_DEPTH_UNITS";

	// Dive Log Stop's properties
	private long mLocalId;
	private long mOnlineId;
	private long mDiveLogLocalId;
	private long mDiveLogOnlineId;
	private int mTime;
	private ValueParameter mDepth;

	public static final int DIVE_LOG_STOP_FIELD_COUNT = 6;
	public static final int DIVE_LOG_STOP_LOCAL_ID_INDEX = 0;
	public static final int DIVE_LOG_STOP_ONLINE_ID_INDEX = 1;
	public static final int DIVE_LOG_STOP_LOG_ID_INDEX = 2;
	public static final int DIVE_LOG_STOP_TIME_INDEX = 3;
	public static final int DIVE_LOG_STOP_DEPTH_VALUE_INDEX = 4;
	public static final int DIVE_LOG_STOP_DEPTH_UNITS_INDEX = 5;

	public static final String DIVE_LOG_STOP_ID_PARAM = "LOG_STOP_ID_";
	public static final String DIVE_LOG_STOP_LOCAL_ID_PARAM = "LOG_STOP_LOCAL_ID_";
	public static final String DIVE_LOG_STOP_LOG_ID_PARAM = "LOG_ID_";
	public static final String DIVE_LOG_STOP_TIME_PARAM = "TIME_";
	public static final String DIVE_LOG_STOP_DEPTH_VALUE_PARAM = "DEPTH_VALUE_";
	public static final String DIVE_LOG_STOP_DEPTH_UNITS_PARAM = "DEPTH_UNITS_";

	public DiveLogStop() {
		mLocalId = -1;
		mOnlineId = -1;
		mDiveLogLocalId = -1;
		mDiveLogOnlineId = -1;
		mTime = 0;
		mDepth = new ValueParameter(0, "");
	}

	public DiveLogStop(DiveLog diveLog) {
		mLocalId = -1;
		mOnlineId = -1;
		mDiveLogLocalId = diveLog.getLocalId();
		mDiveLogOnlineId = diveLog.getOnlineId();
		mTime = 0;
		mDepth = new ValueParameter(0, "");
	}

	public DiveLogStop(JSONObject json) {
		try {
			mLocalId = -1;
			mDiveLogLocalId = -1;
			if (json.has(JSON_TAG_DIVE_LOG_STOP_LOCAL_ID)) {
				mLocalId = json.getInt(JSON_TAG_DIVE_LOG_STOP_LOCAL_ID);
			}
			mOnlineId = json.getInt(JSON_TAG_DIVE_LOG_STOP_ID);
			mDiveLogOnlineId = json.getInt(JSON_TAG_DIVE_LOG_STOP_LOG_ID);
			mTime = json.getInt(JSON_TAG_DIVE_LOG_STOP_TIME);
			mDepth = new ValueParameter(
					json.getInt(JSON_TAG_DIVE_LOG_DEPTH_VALUE),
					json.getString(JSON_TAG_DIVE_LOG_DEPTH_UNITS));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DiveLogStop(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mDiveLogLocalId = source.readLong();
		mDiveLogOnlineId = source.readLong();
		mTime = source.readInt();
		mDepth = source.readParcelable(ValueParameter.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeLong(mDiveLogLocalId);
		dest.writeLong(mDiveLogOnlineId);
		dest.writeInt(mTime);
		dest.writeParcelable(mDepth, flags);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DiveLogStop> CREATOR = new Parcelable.Creator<DiveLogStop>() {
		@Override
		public DiveLogStop createFromParcel(Parcel in) {
			return new DiveLogStop(in);
		}

		@Override
		public DiveLogStop[] newArray(int size) {
			return new DiveLogStop[size];
		}
	};

	public String[] getFieldsAsStrings() {
		String[] diveLogStopFields = new String[DIVE_LOG_STOP_FIELD_COUNT];
		diveLogStopFields[DIVE_LOG_STOP_LOCAL_ID_INDEX] = String
				.valueOf(mLocalId);
		diveLogStopFields[DIVE_LOG_STOP_ONLINE_ID_INDEX] = String
				.valueOf(mOnlineId);
		diveLogStopFields[DIVE_LOG_STOP_LOG_ID_INDEX] = String
				.valueOf(mDiveLogOnlineId);
		diveLogStopFields[DIVE_LOG_STOP_TIME_INDEX] = String.valueOf(mTime);
		diveLogStopFields[DIVE_LOG_STOP_DEPTH_VALUE_INDEX] = String
				.valueOf(mDepth.getValue());
		diveLogStopFields[DIVE_LOG_STOP_DEPTH_UNITS_INDEX] = mDepth.getUnits();

		return diveLogStopFields;
	}

	public long getLocalId() {
		return mLocalId;
	}

	public void setLocalId(long localId) {
		mLocalId = localId;
	}

	public long getOnlineId() {
		return mOnlineId;
	}

	public void setOnlineId(long onlineId) {
		mOnlineId = onlineId;
	}

	public int getTime() {
		return mTime;
	}

	public void setTime(int time) {
		mTime = time;
	}

	public ValueParameter getDepth() {
		return mDepth;
	}

	public void setDepth(ValueParameter depth) {
		mDepth = depth;
	}

	public long getDiveLogLocalId() {
		return mDiveLogLocalId;
	}

	public void setDiveLogLocalId(long diveLogLocalId) {
		mDiveLogLocalId = diveLogLocalId;
	}

	public long getDiveLogOnlineId() {
		return mDiveLogOnlineId;
	}

	public void setDiveLogOnlineId(long diveLogOnlineId) {
		mDiveLogOnlineId = diveLogOnlineId;
	}
}
