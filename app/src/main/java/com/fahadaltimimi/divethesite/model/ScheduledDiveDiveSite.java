package com.fahadaltimimi.divethesite.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class ScheduledDiveDiveSite implements Parcelable {
	
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_ID = "SCHEDULED_DIVE_SITE_ID";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID = "SCHEDULED_DIVE_SITE_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_ID = "SCHEDULED_DIVE_ID";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_LOCAL_ID = "SCHEDULED_DIVE_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_ID = "SITE_ID";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_LOCAL_ID = "SITE_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_VOTE_COUNT = "VOTE_COUNT";
	
	private long mLocalId;
	private long mOnlineId;
	private long mScheduledDiveLocalId;
	private long mScheduledDiveOnlineId;
	private long mDiveSiteLocalId;
	private long mDiveSiteOnlineId;
	private int mVoteCount;
	private DiveSite mDiveSite;
	
	public static final int SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT = 6;
	public static final int SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID_INDEX = 0;
	public static final int SCHEDULED_DIVE_DIVE_SITE_ONLINE_ID_INDEX = 1;
	public static final int SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_LOCAL_ID_INDEX = 2;
	public static final int SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_ONLINE_ID_INDEX = 3;
	public static final int SCHEDULED_DIVE_DIVE_SITE_SITE_LOCAL_ID_INDEX = 4;
	public static final int SCHEDULED_DIVE_DIVE_SITE_SITE_ONLINE_ID_INDEX = 5;
	
	public ScheduledDiveDiveSite() {
		mLocalId = -1;
		mOnlineId = -1;
		mScheduledDiveLocalId = -1;
		mScheduledDiveOnlineId = -1;
		mDiveSiteLocalId = -1;
		mDiveSiteOnlineId = -1;
		mVoteCount = 0;
		mDiveSite = null;
	}
	
	public ScheduledDiveDiveSite(DiveSite diveSite) {
		mLocalId = -1;
		mOnlineId = -1;
		mScheduledDiveLocalId = -1;
		mScheduledDiveOnlineId = -1;
		mDiveSiteLocalId = diveSite.getLocalId();
		mDiveSiteOnlineId = diveSite.getOnlineId();
		mVoteCount = 0;
		mDiveSite = diveSite;
	}
	
	public ScheduledDiveDiveSite(JSONObject json) {
		try {
			mDiveSite = null;
			
			mLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID)) {
				mLocalId = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID);
			}
			mOnlineId = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_ID);
			mScheduledDiveLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_LOCAL_ID)) {
				mScheduledDiveLocalId = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_LOCAL_ID);
			}
			mScheduledDiveOnlineId = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_ID);
			mDiveSiteLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_LOCAL_ID)) {
				mDiveSiteLocalId = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_LOCAL_ID);
			}
			mDiveSiteOnlineId = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_SITE_ID);
			
			mVoteCount = json.getInt(TAG_SCHEDULED_DIVE_DIVE_SITE_VOTE_COUNT);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ScheduledDiveDiveSite(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mScheduledDiveLocalId = source.readLong();
		mScheduledDiveOnlineId = source.readLong();
		mDiveSiteLocalId = source.readLong();
		mDiveSiteOnlineId = source.readLong();
		mVoteCount = source.readInt();
		
		boolean hasDiveSite = source.readByte() != 0;
		if (hasDiveSite) {
			mDiveSite = source.readParcelable(DiveSite.class.getClassLoader());
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeLong(mScheduledDiveLocalId);
		dest.writeLong(mScheduledDiveOnlineId);
		dest.writeLong(mDiveSiteLocalId);
		dest.writeLong(mDiveSiteOnlineId);
		dest.writeInt(mVoteCount);
		
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

	public static final Parcelable.Creator<ScheduledDiveDiveSite> CREATOR = new Parcelable.Creator<ScheduledDiveDiveSite>() {
		@Override
		public ScheduledDiveDiveSite createFromParcel(Parcel in) {
			return new ScheduledDiveDiveSite(in);
		}

		@Override
		public ScheduledDiveDiveSite[] newArray(int size) {
			return new ScheduledDiveDiveSite[size];
		}
	};

	public String[] getFieldsAsStrings() {
		String[] scheduledDiveDiveSiteFields = new String[SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT];
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_DIVE_SITE_LOCAL_ID_INDEX] = String.valueOf(mLocalId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_DIVE_SITE_ONLINE_ID_INDEX] = String.valueOf(mOnlineId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_LOCAL_ID_INDEX] = String.valueOf(mScheduledDiveLocalId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_DIVE_SITE_SCHEDULED_DIVE_ONLINE_ID_INDEX] = String.valueOf(mScheduledDiveOnlineId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_DIVE_SITE_SITE_LOCAL_ID_INDEX] = String.valueOf(mDiveSiteLocalId);
		scheduledDiveDiveSiteFields[SCHEDULED_DIVE_DIVE_SITE_SITE_ONLINE_ID_INDEX] = String.valueOf(mDiveSiteOnlineId);

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

	public int getVoteCount() {
		return mVoteCount;
	}

	public void setVoteCount(int voteCount) {
		mVoteCount = voteCount;
	}
	
	public DiveSite getDiveSite() {
		return mDiveSite;
	}

	public void setDiveSite(DiveSite diveSite) {
		mDiveSite = diveSite;
	}
}
