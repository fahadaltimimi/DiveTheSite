package com.fahadaltimimi.divethesite.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class ScheduledDiveUser implements Parcelable {
	
	public static CharSequence Attend_State_Names[] = {
		    "Attending",
			"Maybe Attending",
			"Not Attending",
			"Invited"};

	public enum AttendState {
			ATTENDING(0, Attend_State_Names[0]), 
			MAYBE_ATTENDING(1, Attend_State_Names[1]),
			NOT_ATTENDING(2, Attend_State_Names[2]),
			INVITED(3, Attend_State_Names[3]);
	
		private final int mIndex;
		private final String mName;
	
		AttendState(int index, CharSequence name) {
			mIndex = index;
			mName = name.toString();
		}
	
		public int getIndex() {
			return mIndex;
		}
	
		public String getName() {
			return mName;
		}
	}
	
	public static final String TAG_SCHEDULED_DIVE_USER_ID = "SCHEDULED_DIVE_USER_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_LOCAL_ID = "SCHEDULED_DIVE_USER_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ID = "SCHEDULED_DIVE_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID = "SCHEDULED_DIVE_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ID = "VOTED_SCHEDULED_DIVE_SITE_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID = "VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_USER_ID = "USER_ID";
	public static final String TAG_SCHEDULED_DIVE_USER_ATTEND_STATE = "ATTEND_STATE";
	
	private long mLocalId;
	private long mOnlineId;
	private long mScheduledDiveLocalId;
	private long mScheduledDiveOnlineId;
	private long mVotedScheduledDiveDiveSiteLocalId;
	private long mVotedScheduledDiveDiveSiteOnlineId;
	private long mUserId;
	private AttendState mAttendState;
	
	public static final int SCHEDULED_DIVE_USER_FIELD_COUNT = 8;
	public static final int SCHEDULED_DIVE_USER_LOCAL_ID_INDEX = 0;
	public static final int SCHEDULED_DIVE_USER_ONLINE_ID_INDEX = 1;
	public static final int SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID_INDEX = 2;
	public static final int SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ONLINE_ID_INDEX = 3;
	public static final int SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID_INDEX = 4;
	public static final int SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ONLINE_ID_INDEX = 5;
	public static final int SCHEDULED_DIVE_USER_USER_ID_INDEX = 6;
	public static final int SCHEDULED_DIVE_USER_ATTEND_STATE_INDEX = 7;
	
	public ScheduledDiveUser() {
		mLocalId = -1;
		mOnlineId = -1;
		mScheduledDiveLocalId = -1;
		mScheduledDiveOnlineId = -1;
		mVotedScheduledDiveDiveSiteLocalId = -1;
		mVotedScheduledDiveDiveSiteOnlineId = -1;
		mUserId = -1;
		mAttendState = AttendState.NOT_ATTENDING;
	}
	
	public ScheduledDiveUser(JSONObject json) {
		try {
			mLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_USER_LOCAL_ID)) {
				mLocalId = json.getInt(TAG_SCHEDULED_DIVE_USER_LOCAL_ID);
			}
			mOnlineId = json.getInt(TAG_SCHEDULED_DIVE_USER_ID);
			
			mScheduledDiveLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID)) {
				mScheduledDiveLocalId = json.getInt(TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID);
			}
			mScheduledDiveOnlineId = json.getInt(TAG_SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ID);
			
			mVotedScheduledDiveDiveSiteLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID)) {
				mVotedScheduledDiveDiveSiteLocalId = json.getInt(TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID);
			}
			mVotedScheduledDiveDiveSiteOnlineId = json.getInt(TAG_SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ID);
			
			mUserId = json.getInt(TAG_SCHEDULED_DIVE_USER_USER_ID);
			
			mAttendState = AttendState.NOT_ATTENDING;
			String attendState = json.getString(TAG_SCHEDULED_DIVE_USER_ATTEND_STATE);
			for (AttendState attend : AttendState.values()) {
				if (attend.getName().equals(attendState)) {
					mAttendState = attend;
					break;
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ScheduledDiveUser(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mScheduledDiveLocalId = source.readLong();
		mScheduledDiveOnlineId = source.readLong();
		mVotedScheduledDiveDiveSiteLocalId = source.readLong();
		mVotedScheduledDiveDiveSiteOnlineId = source.readLong();
		mUserId = source.readLong();
		
		String attendState = source.readString();
		for (AttendState attend : AttendState.values()) {
			if (attend.getName().equals(attendState)) {
				mAttendState = attend;
				break;
			}
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeLong(mScheduledDiveLocalId);
		dest.writeLong(mScheduledDiveOnlineId);
		dest.writeLong(mVotedScheduledDiveDiveSiteLocalId);
		dest.writeLong(mVotedScheduledDiveDiveSiteOnlineId);
		dest.writeLong(mUserId);
		dest.writeString(mAttendState.getName());
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<ScheduledDiveUser> CREATOR = new Parcelable.Creator<ScheduledDiveUser>() {
		@Override
		public ScheduledDiveUser createFromParcel(Parcel in) {
			return new ScheduledDiveUser(in);
		}

		@Override
		public ScheduledDiveUser[] newArray(int size) {
			return new ScheduledDiveUser[size];
		}
	};

	public String[] getFieldsAsStrings() {
		String[] scheduledDiveDiveSiteFields = new String[SCHEDULED_DIVE_USER_FIELD_COUNT];
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_LOCAL_ID_INDEX] = String.valueOf(mLocalId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_ONLINE_ID_INDEX] = String.valueOf(mOnlineId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_SCHEDULED_DIVE_LOCAL_ID_INDEX] = String.valueOf(mScheduledDiveLocalId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_SCHEDULED_DIVE_ONLINE_ID_INDEX] = String.valueOf(mScheduledDiveOnlineId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID_INDEX] = String.valueOf(mVotedScheduledDiveDiveSiteLocalId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_VOTED_SCHEDULED_DIVE_SITE_ONLINE_ID_INDEX] = String.valueOf(mVotedScheduledDiveDiveSiteOnlineId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_USER_ID_INDEX] = String.valueOf(mUserId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_USER_ATTEND_STATE_INDEX] = mAttendState.getName();
		
		return scheduledDiveDiveSiteFields;
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

	public long getScheduledDiveLocalId() {
		return mScheduledDiveLocalId;
	}

	public void setScheduledDiveLocalId(long scheduledDiveLocalId) {
		mScheduledDiveLocalId = scheduledDiveLocalId;
	}

	public long getScheduledDiveOnlineId() {
		return mScheduledDiveOnlineId;
	}

	public void setScheduledDiveOnlineId(long scheduledDiveOnlineId) {
		mScheduledDiveOnlineId = scheduledDiveOnlineId;
	}

	public long getVotedScheduledDiveDiveSiteLocalId() {
		return mVotedScheduledDiveDiveSiteLocalId;
	}

	public void setVotedScheduledDiveDiveSiteLocalId(
			long votedScheduledDiveDiveSiteLocalId) {
		mVotedScheduledDiveDiveSiteLocalId = votedScheduledDiveDiveSiteLocalId;
	}

	public long getVotedScheduledDiveDiveSiteOnlineId() {
		return mVotedScheduledDiveDiveSiteOnlineId;
	}

	public void setVotedScheduledDiveDiveSiteOnlineId(
			long votedScheduledDiveDiveSiteOnlineId) {
		mVotedScheduledDiveDiveSiteOnlineId = votedScheduledDiveDiveSiteOnlineId;
	}

	public long getUserId() {
		return mUserId;
	}

	public void setUserId(long userId) {
		mUserId = userId;
	}

	public AttendState getAttendState() {
		return mAttendState;
	}

	public void setAttendState(AttendState attendState) {
		mAttendState = attendState;
	}
}
