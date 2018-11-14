package com.fahadaltimimi.divethesite.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class DiveLogBuddy implements Parcelable {

	private static final String JSON_TAG_DIVE_LOG_BUDDY_ID = "LOG_BUDDY_ID";
	private static final String JSON_TAG_DIVE_LOG_BUDDY_LOCAL_ID = "LOG_BUDDY_LOCAL_ID";
	private static final String JSON_TAG_DIVE_LOG_BUDDY_LOG_ID = "LOG_BUDDY_LOG_ID";
	private static final String JSON_TAG_DIVE_LOG_BUDDY_DIVER_ID = "LOG_BUDDY_DIVER_ID";
	private static final String JSON_TAG_DIVE_LOG_BUDDY_DIVER_USERNAME = "LOG_BUDDY_DIVER_USERNAME";

	// Dive Log Buddy's properties
	private long mLocalId;
	private long mOnlineId;
	private long mDiveLogLocalId;
	private long mDiveLogOnlineId;
	private long mDiverOnlineId;
	private String mDiverUsername;

	public static final int DIVE_LOG_BUDDY_FIELD_COUNT = 5;
	public static final int DIVE_LOG_BUDDY_LOCAL_ID_INDEX = 0;
	public static final int DIVE_LOG_BUDDY_ONLINE_ID_INDEX = 1;
	public static final int DIVE_LOG_BUDDY_LOG_ID_INDEX = 2;
	public static final int DIVE_LOG_BUDDY_DIVER_ID_INDEX = 3;
	public static final int DIVE_LOG_BUDDY_DIVER_USERNAME_INDEX = 4;

	public static final String DIVE_LOG_BUDDY_ID_PARAM = "LOG_BUDDY_ID_";
	public static final String DIVE_LOG_BUDDY_LOCAL_ID_PARAM = "LOG_BUDDY_LOCAL_ID_";
	public static final String DIVE_LOG_BUDDY_LOG_ID_PARAM = "LOG_ID_";
	public static final String DIVE_LOG_BUDDY_USER_ID_PARAM = "USER_ID_";
	public static final String DIVE_LOG_BUDDY_USERNAME_PARAM = "USERNAME_";

	public DiveLogBuddy() {
		mLocalId = -1;
		mOnlineId = -1;
		mDiveLogLocalId = -1;
		mDiveLogOnlineId = -1;
		mDiverOnlineId = -1;
		mDiverUsername = "";
	}

	public DiveLogBuddy(DiveLog diveLog) {
		mLocalId = -1;
		mOnlineId = -1;
		mDiveLogLocalId = diveLog.getLocalId();
		mDiveLogOnlineId = diveLog.getOnlineId();
		mDiverOnlineId = -1;
		mDiverUsername = "";
	}

	public DiveLogBuddy(JSONObject json) {
		try {
			mLocalId = -1;
			mDiveLogLocalId = -1;
			if (json.has(JSON_TAG_DIVE_LOG_BUDDY_LOCAL_ID)) {
				mLocalId = json.getInt(JSON_TAG_DIVE_LOG_BUDDY_LOCAL_ID);
			}
			mOnlineId = json.getInt(JSON_TAG_DIVE_LOG_BUDDY_ID);
			mDiveLogOnlineId = json.getInt(JSON_TAG_DIVE_LOG_BUDDY_LOG_ID);
			mDiverOnlineId = json.getInt(JSON_TAG_DIVE_LOG_BUDDY_DIVER_ID);
			mDiverUsername = json
					.getString(JSON_TAG_DIVE_LOG_BUDDY_DIVER_USERNAME);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DiveLogBuddy(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mDiveLogLocalId = source.readLong();
		mDiveLogOnlineId = source.readLong();
		mDiverOnlineId = source.readLong();
		mDiverUsername = source.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeLong(mDiveLogLocalId);
		dest.writeLong(mDiveLogOnlineId);
		dest.writeLong(mDiverOnlineId);
		dest.writeString(mDiverUsername);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DiveLogBuddy> CREATOR = new Parcelable.Creator<DiveLogBuddy>() {
		@Override
		public DiveLogBuddy createFromParcel(Parcel in) {
			return new DiveLogBuddy(in);
		}

		@Override
		public DiveLogBuddy[] newArray(int size) {
			return new DiveLogBuddy[size];
		}
	};

	public String[] getFieldsAsStrings() {
		String[] diveLogBuddyFields = new String[DIVE_LOG_BUDDY_FIELD_COUNT];
		diveLogBuddyFields[DIVE_LOG_BUDDY_LOCAL_ID_INDEX] = String
				.valueOf(mLocalId);
		diveLogBuddyFields[DIVE_LOG_BUDDY_ONLINE_ID_INDEX] = String
				.valueOf(mOnlineId);
		diveLogBuddyFields[DIVE_LOG_BUDDY_LOG_ID_INDEX] = String
				.valueOf(mDiveLogOnlineId);
		diveLogBuddyFields[DIVE_LOG_BUDDY_DIVER_ID_INDEX] = String
				.valueOf(mDiverOnlineId);
		diveLogBuddyFields[DIVE_LOG_BUDDY_DIVER_USERNAME_INDEX] = mDiverUsername;

		return diveLogBuddyFields;
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

	public long getDiverOnlineId() {
		return mDiverOnlineId;
	}

	public void setDiverOnlineId(long diverId) {
		mDiverOnlineId = diverId;
	}

	public String getDiverUsername() {
		return mDiverUsername;
	}

	public void setDiverUsername(String diverUsername) {
		mDiverUsername = diverUsername;
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
