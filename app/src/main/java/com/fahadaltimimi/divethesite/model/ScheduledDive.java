package com.fahadaltimimi.divethesite.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class ScheduledDive implements Parcelable {

    private static final SimpleDateFormat JSONDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private static final SimpleDateFormat longDateFormat = new SimpleDateFormat("EEEE MMMM dd yyyy, HH:mm");
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MM/yy, HH:mm");

    public static final String TAG_SCHEDULED_DIVE_ID = "SCHEDULED_DIVE_ID";
	public static final String TAG_SCHEDULED_DIVE_LOCAL_ID = "SCHEDULED_DIVE_LOCAL_ID";
	public static final String TAG_SCHEDULED_DIVE_TITLE = "TITLE";
	public static final String TAG_SCHEDULED_DIVE_SUBMITTER_ID = "SUBMITTER_ID";
	public static final String TAG_SCHEDULED_DIVE_TIMESTAMP = "TIMESTAMP";
	public static final String TAG_SCHEDULED_DIVE_COMMENT = "COMMENT";
	public static final String TAG_SCHEDULED_DIVE_LAST_MODIFIED_ONLINE = "LAST_MODIFIED_ONLINE";
	public static final String TAG_SCHEDULED_DIVE_DIVE_SITE_DATA_COUNT_PARAM = "SCHEDULEDDIVE_SITE_COUNT";
	public static final String TAG_SCHEDULED_DIVE_USER_DATA_COUNT_PARAM = "SCHEDULEDDIVE_USER_COUNT";
	public static final String TAG_TIMESTAMP_START = "TIMESTAMP_START";
	public static final String TAG_TIMESTAMP_END = "TIMESTAMP_END";
	public static final String TAG_IGNORE_TIMESTAMP_START = "IGNORE_TIMESTAMP_START";
	public static final String TAG_IGNORE_TIMESTAMP_END = "IGNORE_TIMESTAMP_END";
    public static final String TAG_SCHEDULED_DIVE_DIVE_COUNT = "SCHEDULED_DIVE_COUNT";
	
	private long mLocalId;
	private long mOnlineId;
	private String mTitle;
	private long mSubmitterId;
	private Date mTimestamp;
	private String mComment;
	private boolean mIsPublished;
	private Date mLastModifiedOnline;
	private boolean mRequiresRefresh;

    private int mScheduledDiveCountWhenRetreived;

	private ArrayList<ScheduledDiveDiveSite> mScheduledDiveDiveSites;
	private ArrayList<ScheduledDiveUser> mScheduledDiveUsers;
	
	public static final int SCHEDULED_DIVE_FIELD_COUNT = 11;
	public static final int SCHEDULED_DIVE_LOCAL_ID_INDEX = 0;
	public static final int SCHEDULED_DIVE_ONLINE_ID_INDEX = 1;
	public static final int SCHEDULED_DIVE_TITLE_INDEX = 2;
	public static final int SCHEDULED_DIVE_SUBMITTER_ID_INDEX = 3;
	public static final int SCHEDULED_DIVE_TIMESTAMP_INDEX = 4;
	public static final int SCHEDULED_DIVE_COMMENT_INDEX = 5;
	public static final int SCHEDULED_DIVE_LAST_MODIFIED_ONLINE_INDEX = 6;
	public static final int SCHEDULED_DIVE_DIVE_SITE_DATA_START_POS_INDEX = 7;
	public static final int SCHEDULED_DIVE_DIVE_SITE_DATA_COUNT_INDEX = 8;
	public static final int SCHEDULED_DIVE_USER_DATA_START_POS_INDEX = 9;
	public static final int SCHEDULED_DIVE_USER_DATA_COUNT_INDEX = 10;
	
	public ScheduledDive() {
		mLocalId = -1;
		mOnlineId = -1;
		mTitle = "";
		mSubmitterId = -1;
		mTimestamp = new Date(0);
		mComment = "";
		mIsPublished = false;
		mRequiresRefresh = false;
		mLastModifiedOnline = new Date(0);
        mScheduledDiveCountWhenRetreived = 0;
		mScheduledDiveDiveSites = new ArrayList<ScheduledDiveDiveSite>();
		mScheduledDiveUsers = new ArrayList<ScheduledDiveUser>();
	}
	
	public ScheduledDive(JSONObject json) {
		try {
			mScheduledDiveDiveSites = new ArrayList<ScheduledDiveDiveSite>();
			mScheduledDiveUsers = new ArrayList<ScheduledDiveUser>();
			
			mLocalId = -1;
			if (json.has(TAG_SCHEDULED_DIVE_LOCAL_ID)) {
				mLocalId = json.getInt(TAG_SCHEDULED_DIVE_LOCAL_ID);
			}
			mOnlineId = json.getInt(TAG_SCHEDULED_DIVE_ID);
			mTitle = json.getString(TAG_SCHEDULED_DIVE_TITLE);
			mSubmitterId = json.getInt(TAG_SCHEDULED_DIVE_SUBMITTER_ID);
			mTimestamp = new Date(json.getLong(TAG_SCHEDULED_DIVE_TIMESTAMP));
			mComment = json.getString(TAG_SCHEDULED_DIVE_COMMENT);
			mIsPublished = false;
			mRequiresRefresh = false;

			mLastModifiedOnline = new Date(0);

			try {
				mLastModifiedOnline = 
					JSONDateFormat.parse(json.getString(TAG_SCHEDULED_DIVE_LAST_MODIFIED_ONLINE));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            if (json.has(TAG_SCHEDULED_DIVE_DIVE_COUNT)) {
                mScheduledDiveCountWhenRetreived = json.getInt(TAG_SCHEDULED_DIVE_DIVE_COUNT);
            } else {
                mScheduledDiveCountWhenRetreived = 0;
            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ScheduledDive(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mTitle = source.readString();
		mSubmitterId = source.readLong();
		mTimestamp = new Date(source.readLong());
		mComment = source.readString();
		mIsPublished = source.readByte() != 0;
		mRequiresRefresh = source.readByte() != 0;
		mLastModifiedOnline = new Date(source.readLong());
        mScheduledDiveCountWhenRetreived = source.readInt();
		mScheduledDiveDiveSites = new ArrayList<ScheduledDiveDiveSite>();
		source.readTypedList(mScheduledDiveDiveSites, ScheduledDiveDiveSite.CREATOR);
		mScheduledDiveUsers = new ArrayList<ScheduledDiveUser>();
		source.readTypedList(mScheduledDiveUsers, ScheduledDiveUser.CREATOR);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeString(mTitle);
		dest.writeLong(mSubmitterId);
		dest.writeLong(mTimestamp.getTime());
		dest.writeString(mComment);
		dest.writeByte((byte) (mIsPublished ? 1 : 0));
		dest.writeByte((byte) (mRequiresRefresh ? 1 : 0));
		dest.writeLong(mLastModifiedOnline.getTime());
        dest.writeInt(mScheduledDiveCountWhenRetreived);
		dest.writeTypedList(mScheduledDiveDiveSites);
		dest.writeTypedList(mScheduledDiveUsers);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<ScheduledDive> CREATOR = new Parcelable.Creator<ScheduledDive>() {
		@Override
		public ScheduledDive createFromParcel(Parcel in) {
			return new ScheduledDive(in);
		}

		@Override
		public ScheduledDive[] newArray(int size) {
			return new ScheduledDive[size];
		}
	};

	public String[] getFieldsAsStrings() {
		String[] scheduledDiveFields = new String[SCHEDULED_DIVE_FIELD_COUNT
		                                          + mScheduledDiveDiveSites.size() 
		                                          * ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT
		                                          + mScheduledDiveUsers.size() 
		                                          * ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT];
		
		scheduledDiveFields[SCHEDULED_DIVE_LOCAL_ID_INDEX] = String.valueOf(mLocalId);
		scheduledDiveFields[SCHEDULED_DIVE_ONLINE_ID_INDEX] = String.valueOf(mOnlineId);
		scheduledDiveFields[SCHEDULED_DIVE_TITLE_INDEX] = mTitle;
		scheduledDiveFields[SCHEDULED_DIVE_SUBMITTER_ID_INDEX] = String.valueOf(mSubmitterId);
		scheduledDiveFields[SCHEDULED_DIVE_TIMESTAMP_INDEX] = String.valueOf(mTimestamp.getTime());
		scheduledDiveFields[SCHEDULED_DIVE_COMMENT_INDEX] = mComment;		
		
		scheduledDiveFields[SCHEDULED_DIVE_DIVE_SITE_DATA_START_POS_INDEX] = 
				String.valueOf(getScheduledDiveDiveSiteFieldsStartPosition());
		scheduledDiveFields[SCHEDULED_DIVE_DIVE_SITE_DATA_COUNT_INDEX] = 
				String.valueOf(mScheduledDiveDiveSites.size());
		scheduledDiveFields[SCHEDULED_DIVE_USER_DATA_START_POS_INDEX] = 
				String.valueOf(getScheduledDiveUserStartPosition());
		scheduledDiveFields[SCHEDULED_DIVE_USER_DATA_COUNT_INDEX] = 
				String.valueOf(mScheduledDiveUsers.size());
		
		// Set scheduled dive site fields
		for (int i = 0; i < mScheduledDiveDiveSites.size(); i++) {
			String[] scheduledDiveDiveSiteFields = mScheduledDiveDiveSites.get(i).getFieldsAsStrings();
			System.arraycopy(scheduledDiveDiveSiteFields, 0, scheduledDiveFields,
					getScheduledDiveDiveSiteFieldsStartPosition()
							+ (i * ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT),
					scheduledDiveDiveSiteFields.length);
		}
		
		// Set scheduled dive users fields
		for (int i = 0; i < mScheduledDiveUsers.size(); i++) {
			String[] scheduledDiveUserFields = mScheduledDiveUsers.get(i).getFieldsAsStrings();
			System.arraycopy(scheduledDiveUserFields, 0, scheduledDiveFields,
					getScheduledDiveUserStartPosition()
							+ (i * ScheduledDiveUser.SCHEDULED_DIVE_USER_FIELD_COUNT),
					scheduledDiveUserFields.length);
		}
		
		return scheduledDiveFields;
	}
	
	public int getScheduledDiveDiveSiteFieldsStartPosition() {
		// When getFieldsAsStrings is called, this method called to determine
		// when scheduled dive site fields start in string array
		// Scheduled dive site fields start after Scheduled dive fields
		return SCHEDULED_DIVE_FIELD_COUNT;
	}

	public int getScheduledDiveUserStartPosition() {
		// When getFieldsAsStrings is called, this method called to determine
		// when scheduled dive users fields start in string array
		// Scheduled dive users fields start after Scheduled dive site fields
		return SCHEDULED_DIVE_FIELD_COUNT
				+ (mScheduledDiveDiveSites.size() 
				* ScheduledDiveDiveSite.SCHEDULED_DIVE_DIVE_SITE_FIELD_COUNT);
	}
	
	public String getShareSummary() {
		String summary = mTitle + "\n" + getTimestampStringShort() + "\n";
		summary = summary + "Sites: ";
		for (int i = 0; i < mScheduledDiveDiveSites.size(); i++) {
			if (i < mScheduledDiveDiveSites.size()  - 1) {
				summary = summary + mScheduledDiveDiveSites.get(i).getDiveSite().getName() + ", ";
			} else {
				summary = summary + mScheduledDiveDiveSites.get(i).getDiveSite().getName();
			}
		}
		summary = summary + "\n\n";
		summary = summary + mComment;
		
		return summary;
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

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}
	
	public long getSubmitterId() {
		return mSubmitterId;
	}

	public void setSubmitterId(long submitterId) {
		mSubmitterId = submitterId;
	}

	public Date getTimestamp() {
		return mTimestamp;
	}
	
	public String getTimestampStringLong() {
		return longDateFormat.format(mTimestamp);
	}
	
	public String getTimestampStringShort() {
		return shortDateFormat.format(mTimestamp);
	}

	public void setTimestamp(Date timestamp) {
		mTimestamp = timestamp;
	}

	public String getComment() {
		return mComment;
	}

	public void setComment(String comment) {
		mComment = comment;
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

    public int getScheduledDiveCountWhenRetreived() {
        return mScheduledDiveCountWhenRetreived;
    }

    public void setScheduledDiveCountWhenRetreived(int scheduledDiveCountWhenRetreived) {
        mScheduledDiveCountWhenRetreived = scheduledDiveCountWhenRetreived;
    }

	public ArrayList<ScheduledDiveDiveSite> getScheduledDiveDiveSites() {
		return mScheduledDiveDiveSites;
	}

	public void setScheduledDiveDiveSites(ArrayList<ScheduledDiveDiveSite> scheduledDiveDiveSites) {
		mScheduledDiveDiveSites = scheduledDiveDiveSites;
	}
	
	public int getScheduledDiveDiveSiteIndex(ScheduledDiveDiveSite scheduledDiveDiveSite) {
		int index = -1;
		
		for (int i = 0; i < mScheduledDiveDiveSites.size(); i++) {
			if (mScheduledDiveDiveSites.get(i).getLocalId() == scheduledDiveDiveSite.getLocalId() &&
					mScheduledDiveDiveSites.get(i).getOnlineId() == scheduledDiveDiveSite.getOnlineId()) {
				index = i;
				break;
			}
		}
		
		return index;
	}

	public ArrayList<ScheduledDiveUser> getScheduledDiveUsers() {
		return mScheduledDiveUsers;
	}

	public void setScheduledDiveUsers(
			ArrayList<ScheduledDiveUser> scheduledDiveUsers) {
		mScheduledDiveUsers = scheduledDiveUsers;
	}
	
	public int getScheduledDiveUserIndex(ScheduledDiveUser scheduledDiveUser) {
		int index = -1;
		
		for (int i = 0; i < mScheduledDiveUsers.size(); i++) {
			if (mScheduledDiveUsers.get(i).getLocalId() == scheduledDiveUser.getLocalId() &&
					mScheduledDiveUsers.get(i).getOnlineId() == scheduledDiveUser.getOnlineId()) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	public int getScheduledDiveUserForUser(long userId) {
		int index = -1;
		
		for (int i = 0; i < mScheduledDiveUsers.size(); i++) {
			if (mScheduledDiveUsers.get(i).getUserId() == userId) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	public boolean isUserAttending(long userId) {
		boolean attending = false;
		int index = getScheduledDiveUserForUser(userId);
		
		if (index != -1 && 
				mScheduledDiveUsers.get(index).getAttendState() == 
				ScheduledDiveUser.AttendState.ATTENDING) {
			attending = true;
		}
		
		return attending;
	}
	
	public int getAttendingUsersCount() {
		int attendingCount = 0;
		
		for (int i = 0; i < mScheduledDiveUsers.size(); i++) {
			if (mScheduledDiveUsers.get(i).getAttendState() ==
					ScheduledDiveUser.AttendState.ATTENDING) {
				attendingCount = attendingCount + 1;
			}
		}
		
		return attendingCount;
	}
	
	public Location getClosestLocation(Location currentLocation) {
		Location location = null;
		
		if (currentLocation != null) {
			for (int i = 0; i < mScheduledDiveDiveSites.size(); i++) {
				if (mScheduledDiveDiveSites.get(i).getDiveSite() != null) {
					Location siteLocation = new Location(mScheduledDiveDiveSites.get(i).getDiveSite().getName());
					siteLocation.setLatitude(mScheduledDiveDiveSites.get(i).getDiveSite().getLatitude());
					siteLocation.setLongitude(mScheduledDiveDiveSites.get(i).getDiveSite().getLongitude());
					
					if (location == null || (siteLocation.distanceTo(currentLocation) < location.distanceTo(currentLocation))) {
						location = siteLocation;
					}
				}							
			}
		}
		
		return location;
	}
}
