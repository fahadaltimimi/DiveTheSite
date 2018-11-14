package com.fahadaltimimi.divethesite.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class DiveSite implements Parcelable {

	public static CharSequence Difficulty_Names[] = { "Novice",
			"Novice to Intermediate", "Intermediate",
			"Intermediate to Experienced", "Experienced", "Technical",
			"Unknown" };

	public enum DiveSiteDifficulty {
		NOVICE(0, Difficulty_Names[0]),
        NOVICE_INTERMEDIATE(1, Difficulty_Names[1]),
        INTERMEDIATE(2, Difficulty_Names[2]),
        INTERMEDIATE_EXPERIENCED(3, Difficulty_Names[3]),
        EXPERIENCED(4, Difficulty_Names[4]),
        TECHNICAL(5, Difficulty_Names[5]),
        UNKNOWN(6, Difficulty_Names[6]);

		private final int mIndex;
		private final String mName;

		DiveSiteDifficulty(int index, CharSequence name) {
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

    private static final SimpleDateFormat JSONDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	private static final String JSON_TAG_DIVE_SITE_ID = "SITE_ID";
	private static final String JSON_TAG_NAME = "NAME";
	private static final String JSON_TAG_TOTAL_RATE = "TOTAL_RATE";
	private static final String JSON_TAG_NUM_RATES = "NUM_RATES";
	private static final String JSON_TAG_CITY = "CITY";
	private static final String JSON_TAG_PROVINCE = "PROVINCE";
	private static final String JSON_TAG_COUNTRY = "COUNTRY";
	private static final String JSON_TAG_DIFFICULTY = "DIFFICULTY";
	private static final String JSON_TAG_ISSALT = "ISSALT";
	private static final String JSON_TAG_ISFRESH = "ISFRESH";
	private static final String JSON_TAG_ISSHORE = "ISSHORE";
	private static final String JSON_TAG_ISBOAT = "ISBOAT";
	private static final String JSON_TAG_ISWRECK = "ISWRECK";
	private static final String JSON_TAG_HISTORY = "HISTORY";
	private static final String JSON_TAG_DESCRIPTION = "DESCRIPTION";
	private static final String JSON_TAG_DIRECTIONS = "DIRECTIONS";
	private static final String JSON_TAG_SOURCE = "SOURCE";
	private static final String JSON_TAG_NOTES = "NOTES";
	private static final String JSON_TAG_LATITUDE = "LATITUDE";
	private static final String JSON_TAG_LONGITUDE = "LONGITUDE";
	private static final String JSON_TAG_USER_ID = "USER_ID";
	private static final String JSON_TAG_DATE_ADDED = "DATE_ADDED";
	private static final String JSON_TAG_LAST_MODIFIED_ONLINE = "LAST_MODIFIED_ONLINE";
	private static final String JSON_TAG_APPROVED = "APPROVED";
	private static final String JSON_TAG_USERNAME = "USERNAME";

	public static final int DIVE_SITE_FIELD_COUNT = 28;
	public static final int DIVE_SITE_LOCAL_ID_INDEX = 0;
	public static final int DIVE_SITE_ONLINE_ID_INDEX = 1;
	public static final int DIVE_SITE_NAME_INDEX = 2;
	public static final int DIVE_SITE_TOTAL_RATE_INDEX = 3;
	public static final int DIVE_SITE_NUM_RATES_INDEX = 4;
	public static final int DIVE_SITE_CITY_INDEX = 5;
	public static final int DIVE_SITE_PROVINCE_INDEX = 6;
	public static final int DIVE_SITE_COUNTRY_INDEX = 7;
	public static final int DIVE_SITE_DIFFICULTY_INDEX = 8;
	public static final int DIVE_SITE_ISSALT_INDEX = 9;
	public static final int DIVE_SITE_ISFRESH_INDEX = 10;
	public static final int DIVE_SITE_ISSHORE_INDEX = 11;
	public static final int DIVE_SITE_ISBOAT_INDEX = 12;
	public static final int DIVE_SITE_ISWRECK_INDEX = 13;
	public static final int DIVE_SITE_HISTORY_INDEX = 14;
	public static final int DIVE_SITE_DESCRIPTION_INDEX = 15;
	public static final int DIVE_SITE_DIRECTIONS_INDEX = 16;
	public static final int DIVE_SITE_SOURCE_INDEX = 17;
	public static final int DIVE_SITE_NOTES_INDEX = 18;
	public static final int DIVE_SITE_LATITUDE_INDEX = 19;
	public static final int DIVE_SITE_LONGITUDE_INDEX = 20;
	public static final int DIVE_SITE_USER_ID_INDEX = 21;
	public static final int DIVE_SITE_DATE_ADDED_INDEX = 22;
	public static final int DIVE_SITE_LAST_MODIFIED_INDEX = 23;
	public static final int DIVE_SITE_APPROVED_INDEX = 24;
	public static final int DIVE_SITE_USERNAME_INDEX = 25;
	public static final int DIVE_SITE_PICTURES_START_POS_INDEX = 26;
	public static final int DIVE_SITE_PICTURES_COUNT_INDEX = 27;
	
	public static final String DIVE_SITE_ID_PARAM = "SITE_ID";
	public static final String DIVE_SITE_TITLE_PARAM = "TITLE";
	public static final String DIVE_SITE_COUNTRY_PARAM = "COUNTRY";
	public static final String DIVE_SITE_PROVINCE_PARAM = "PROVINCE";
	public static final String DIVE_SITE_CITY_PARAM = "CITY";
	public static final String DIVE_SITE_MIN_LATITUDE_PARAM = "MIN_LATITUDE";
	public static final String DIVE_SITE_MAX_LATITUDE_PARAM = "MAX_LATITUDE";
	public static final String DIVE_SITE_MIN_LONGITUDE_PARAM = "MIN_LONGITUDE";
	public static final String DIVE_SITE_MAX_LONGITUDE_PARAM = "MAX_LONGITUDE";
	public static final String DIVE_SITE_DISTANCE_PARAM = "DISTANCE";
	public static final String DIVE_SITE_START_INDEX_LOAD_PARAM = "START_INDEX_LOAD";
	public static final String DIVE_SITE_COUNT_LOAD_PARAM = "COUNT_LOAD";

    public static final String TAG_DIVE_SITE_SITE_COUNT = "SITE_COUNT";
	
	
	// DiveSite's properties
	private long mLocalId;
	private long mOnlineId;
	private long mUserId;
	private String mUsername;

	private String mName;
	private String mCity;
	private String mProvince;
	private String mCountry;
	private DiveSiteDifficulty mDifficulty;
	private boolean mIsSalty;
	private boolean mIsShoreDive;
	private boolean mIsBoatDive;
	private boolean mHasWreck;

	private String mHistory;
	private String mDescription;
	private String mDirections;
	private String mSource;
	private String mNotes;

	private double mLatitude;
	private double mLongitude;
	private double mAltitude;

	private float mTotalRating;
	private int mRatingCount;

	private Date mDateAdded;
	private Date mLastModifiedOnline;
	private boolean mIsPublished;
	private boolean mIsArchived;
	private boolean mRequiresRefresh;

    private int mDiveSiteCountWhenRetreived;

	private ArrayList<DiveSitePicture> mPictures;
	private ArrayList<DiveLog> mDiveLogs;

	public DiveSite(long diverID, String diverUsername) {
		mLocalId = -1;
		mOnlineId = -1;
		mUserId = diverID;
		mUsername = diverUsername;
		mName = "New Dive Site";
		mCity = "";
		mProvince = "";
		mCountry = "";
		mDescription = "";
		mDifficulty = DiveSiteDifficulty.NOVICE;
		mIsSalty = false;
		mIsShoreDive = false;
		mIsBoatDive = false;
		mHasWreck = false;
		mHistory = "";
		mDirections = "";
		mSource = "";
		mNotes = "";
		mLatitude = 0;
		mLongitude = 0;
		mTotalRating = 0;
		mRatingCount = 0;
		mDateAdded = new Date();
		mLastModifiedOnline = new Date(0);
		mPictures = new ArrayList<DiveSitePicture>();
		mIsPublished = false;
		mIsArchived = false;
		mRequiresRefresh = false;
		mDiveLogs = new ArrayList<DiveLog>();
        mDiveSiteCountWhenRetreived = 0;
	}

	public DiveSite(DiveSite diveSite, long diverID, String diverUsername) {
		// Copy dive site given
		mLocalId = -1;
		mOnlineId = -1;
		mUserId = diverID;
		mUsername = diverUsername;
		mName = diveSite.getName();
		mCity = diveSite.getCity();
		mProvince = diveSite.getProvince();
		mCountry = diveSite.getCountry();
		mDescription = diveSite.getDescription();
		mDifficulty = diveSite.getDifficulty();
		mIsSalty = diveSite.isSalty();
		mIsShoreDive = diveSite.isShoreDive();
		mIsBoatDive = diveSite.isBoatDive();
		mHasWreck = diveSite.isWreck();
		mHistory = diveSite.getHistory();
		mDirections = diveSite.getDirections();
		mSource = diveSite.getSource();
		mNotes = diveSite.getNotes();
		mLatitude = diveSite.getLatitude();
		mLongitude = diveSite.getLongitude();
		mTotalRating = 0;
		mRatingCount = 0;
		mDateAdded = new Date();
		mLastModifiedOnline = new Date(0);
		mPictures = new ArrayList<DiveSitePicture>();
		mIsPublished = false;
		mIsArchived = false;
		mRequiresRefresh = false;
		mDiveLogs = new ArrayList<DiveLog>();
	}

	public DiveSite(String name, long diverID, String diverUsername) {
		this(diverID, diverUsername);
		mName = name;
	}

	public DiveSite(JSONObject json) {
		try {
			mLocalId = -1;
			mPictures = new ArrayList<DiveSitePicture>();
			mDiveLogs = new ArrayList<DiveLog>();
			mIsPublished = false;
			mRequiresRefresh = false;

			mOnlineId = json.getInt(JSON_TAG_DIVE_SITE_ID);
			mUserId = json.getInt(JSON_TAG_USER_ID);
			mUsername = json.getString(JSON_TAG_USERNAME);
			mName = json.getString(JSON_TAG_NAME);
			mCity = json.getString(JSON_TAG_CITY);
			mProvince = json.getString(JSON_TAG_PROVINCE);
			mCountry = json.getString(JSON_TAG_COUNTRY);

			mDifficulty = DiveSiteDifficulty.NOVICE;
			String difficulty = json.getString(JSON_TAG_DIFFICULTY);
			for (DiveSiteDifficulty diff : DiveSiteDifficulty.values()) {
				if (diff.getName().equals(difficulty)) {
					mDifficulty = diff;
					break;
				}
			}

			mIsSalty = json.getInt(JSON_TAG_ISSALT) == 1;
			mIsShoreDive = json.getInt(JSON_TAG_ISSHORE) == 1;
			mIsBoatDive = json.getInt(JSON_TAG_ISBOAT) == 1;
			mHasWreck = json.getInt(JSON_TAG_ISWRECK) == 1;
			mHistory = json.getString(JSON_TAG_HISTORY);
			mDescription = json.getString(JSON_TAG_DESCRIPTION);
			mDirections = json.getString(JSON_TAG_DIRECTIONS);
			mSource = json.getString(JSON_TAG_SOURCE);
			mNotes = json.getString(JSON_TAG_NOTES);
			mLatitude = Float.parseFloat(json.getString(JSON_TAG_LATITUDE));
			mLongitude = Float.parseFloat(json.getString(JSON_TAG_LONGITUDE));
			mTotalRating = Float
					.parseFloat(json.getString(JSON_TAG_TOTAL_RATE));
			mRatingCount = json.getInt(JSON_TAG_NUM_RATES);
			mDateAdded = new Date(json.getLong(JSON_TAG_DATE_ADDED));

			mDateAdded = new Date(0);
			mLastModifiedOnline = new Date(0);
			
			try {
				mLastModifiedOnline = JSONDateFormat.parse(json.getString(JSON_TAG_LAST_MODIFIED_ONLINE));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            if (json.has(TAG_DIVE_SITE_SITE_COUNT)) {
                mDiveSiteCountWhenRetreived = json.getInt(TAG_DIVE_SITE_SITE_COUNT);
            } else {
                mDiveSiteCountWhenRetreived = 0;
            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DiveSite(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mName = source.readString();
		mTotalRating = source.readFloat();
		mRatingCount = source.readInt();
		mCity = source.readString();
		mProvince = source.readString();
		mCountry = source.readString();

		String difficulty = source.readString();
		for (DiveSiteDifficulty diff : DiveSiteDifficulty.values()) {
			if (diff.getName().equals(difficulty)) {
				mDifficulty = diff;
				break;
			}
		}

		mIsSalty = source.readByte() != 0;
		mIsShoreDive = source.readByte() != 0;
		mIsBoatDive = source.readByte() != 0;
		mHasWreck = source.readByte() != 0;
		mHistory = source.readString();
		mDescription = source.readString();
		mDirections = source.readString();
		mSource = source.readString();
		mNotes = source.readString();
		mLatitude = source.readDouble();
		mLongitude = source.readDouble();
		mUserId = source.readLong();
		mDateAdded = new Date(source.readLong());
		mLastModifiedOnline = new Date(source.readLong());
		mUsername = source.readString();
		mPictures = new ArrayList<DiveSitePicture>();
		source.readTypedList(mPictures, DiveSitePicture.CREATOR);
		mDiveLogs = new ArrayList<DiveLog>();
		source.readTypedList(mDiveLogs, DiveLog.CREATOR);
		mIsPublished = source.readByte() != 0;
		mIsArchived = source.readByte() != 0;
		mRequiresRefresh = source.readByte() != 0;
        mDiveSiteCountWhenRetreived = source.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeString(mName);
		dest.writeFloat(mTotalRating);
		dest.writeInt(mRatingCount);
		dest.writeString(mCity);
		dest.writeString(mProvince);
		dest.writeString(mCountry);
		dest.writeString(mDifficulty.getName());
		dest.writeByte((byte) (mIsSalty ? 1 : 0));
		dest.writeByte((byte) (mIsShoreDive ? 1 : 0));
		dest.writeByte((byte) (mIsBoatDive ? 1 : 0));
		dest.writeByte((byte) (mHasWreck ? 1 : 0));
		dest.writeString(mHistory);
		dest.writeString(mDescription);
		dest.writeString(mDirections);
		dest.writeString(mSource);
		dest.writeString(mNotes);
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
		dest.writeLong(mUserId);
		dest.writeLong(mDateAdded.getTime());
		dest.writeLong(mLastModifiedOnline.getTime());
		dest.writeString(mUsername);
		dest.writeTypedList(mPictures);
		dest.writeTypedList(mDiveLogs);
		dest.writeByte((byte) (mIsPublished ? 1 : 0));
		dest.writeByte((byte) (mIsArchived ? 1 : 0));
		dest.writeByte((byte) (mRequiresRefresh ? 1 : 0));
        dest.writeInt(mDiveSiteCountWhenRetreived);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DiveSite> CREATOR = new Parcelable.Creator<DiveSite>() {
		@Override
		public DiveSite createFromParcel(Parcel in) {
			return new DiveSite(in);
		}

		@Override
		public DiveSite[] newArray(int size) {
			return new DiveSite[size];
		}
	};

	public void copyPictures(DiveSite diveSite) {
		for (int i = 0; i < diveSite.getPicturesCount(); i++) {
			DiveSitePicture picture = new DiveSitePicture();
			picture.setBitmapFilePath(diveSite.getPicture(i)
					.getBitmapFilePath());
			picture.setDiveSiteOnlineID(mLocalId);
		}
	}
	
	public String getShareSummary() {
		String summary = mName + "\n";
		summary = summary + getFullLocation() + "\n\n";
		
		if (!mDescription.trim().isEmpty()) {
			summary = "Description:\n" + summary + mDescription + "\n\n";
		}
		if (!mDirections.trim().isEmpty()) {
			summary = "Directions:\n" + summary + mDirections + "\n\n";
		}
		if (!mHistory.trim().isEmpty()) {
			summary = "History:\n" + summary + mHistory + "\n\n";
		}
		if (!mNotes.trim().isEmpty()) {
			summary = "Notes:\n" + summary + mNotes + "\n\n";
		}
		
		summary = summary + "Source: " + mSource + "\n";
		summary = summary + "Submitter: " + mUsername + "\n";
		
		summary = summary + "\n\nhttps://www.google.com/maps/preview?q=loc:" 
				+ String.valueOf(getLatitude()) + "," 
				+ String.valueOf(getLongitude());
		
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

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getCity() {
		return mCity;
	}

	public void setCity(String city) {
		mCity = city;
	}

	public String getProvince() {
		return mProvince;
	}

	public void setProvince(String province) {
		mProvince = province;
	}

	public String getCountry() {
		return mCountry;
	}

	public void setCountry(String country) {
		mCountry = country;
	}

	public String getFullLocation() {
		String locationText = getCity();
		if (locationText.trim().isEmpty() && !getProvince().trim().isEmpty()) {
			locationText = getProvince();
		} else if (!getProvince().trim().isEmpty()) {
			locationText = locationText + ", " + getProvince();
		}

		if (locationText.trim().isEmpty() && !getCountry().trim().isEmpty()) {
			locationText = getCountry();
		} else if (!getCountry().trim().isEmpty()) {
			locationText = locationText + ", " + getCountry();
		}

		return locationText.trim();
	}

	public DiveSiteDifficulty getDifficulty() {
		return mDifficulty;
	}

	public void setDifficulty(DiveSiteDifficulty difficulty) {
		mDifficulty = difficulty;
	}

	public void setDifficulty(int index) {
		DiveSiteDifficulty difficulty_found = null;
		for (DiveSiteDifficulty diff : DiveSiteDifficulty.values()) {
			if (diff.getIndex() == index) {
				difficulty_found = diff;
				break;
			}
		}

		setDifficulty(difficulty_found);
	}

	public boolean isSalty() {
		return mIsSalty;
	}

	public void setSalty(boolean isSalty) {
		mIsSalty = isSalty;
	}

	public boolean isShoreDive() {
		return mIsShoreDive;
	}

	public void setShoreDive(boolean isShoreDive) {
		mIsShoreDive = isShoreDive;
	}

	public boolean isBoatDive() {
		return mIsBoatDive;
	}

	public void setBoatDive(boolean isBoatDive) {
		mIsBoatDive = isBoatDive;
	}

	public boolean isWreck() {
		return mHasWreck;
	}

	public void setWreck(boolean hasWreck) {
		mHasWreck = hasWreck;
	}

	public String getHistory() {
		return mHistory;
	}

	public void setHistory(String history) {
		mHistory = history;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getDirections() {
		return mDirections;
	}

	public void setDirections(String directions) {
		mDirections = directions;
	}

	public String getSource() {
		return mSource;
	}

	public void setSource(String source) {
		mSource = source;
	}

	public String getNotes() {
		return mNotes;
	}

	public void setNotes(String notes) {
		mNotes = notes;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public double getAltitude() {
		return mAltitude;
	}

	public void setAltitude(double altitude) {
		mAltitude = altitude;
	}

	public float getTotalRating() {
		return mTotalRating;
	}

	public void setTotalRating(float totalRating) {
		mTotalRating = totalRating;
	}

	public int getRatingCount() {
		return mRatingCount;
	}

	public void setRatingCount(int ratingCount) {
		mRatingCount = ratingCount;
	}

	public Date getDateAdded() {
		return mDateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		mDateAdded = dateAdded;
	}

	public Date getLastModifiedOnline() {
		return mLastModifiedOnline;
	}

	public void setLastModifiedOnline(Date dateModified) {
		mLastModifiedOnline = dateModified;
	}

	public int getPicturesCount() {
		return mPictures.size();
	}

	public DiveSitePicture getPicture(int index) {
		return mPictures.get(index);
	}

	public DiveSitePicture getPictureLocalID(long id) {
		DiveSitePicture diveSitePicture = null;

		for (int i = 0; i < mPictures.size(); i++) {
			if (mPictures.get(i).getLocalId() == id) {
				diveSitePicture = mPictures.get(i);
				break;
			}
		}

		return diveSitePicture;
	}

	public int getPictureIndexLocalID(long id) {
		int index = -1;

		for (int i = 0; i < mPictures.size(); i++) {
			if (mPictures.get(i).getLocalId() == id) {
				index = i;
				break;
			}
		}

		return index;
	}

	public int getPictureIndexOnlineID(long id) {
		int index = -1;

		for (int i = 0; i < mPictures.size(); i++) {
			if (mPictures.get(i).getOnlineId() == id) {
				index = i;
				break;
			}
		}

		return index;
	}

	public void addPicture(DiveSitePicture picture) {
		picture.setDiveSiteLocalID(mLocalId);
		picture.setDiveSiteOnlineID(mOnlineId);
		mPictures.add(picture);
	}

	public void removePicture(DiveSitePicture picture) {
		mPictures.remove(picture);
	}

	public void removePicture(int index) {
		mPictures.remove(index);
	}

	public boolean isPublished() {
		return mIsPublished;
	}

	public void setPublished(boolean isPublished) {
		mIsPublished = isPublished;
	}

	public boolean isArchived() {
		return mIsArchived;
	}

	public void setArchived(boolean isArchived) {
		mIsArchived = isArchived;
	}
	
	public boolean requiresRefresh() {
		return mRequiresRefresh;
	}

	public void setRequiresRefresh(boolean requiresRefresh) {
		mRequiresRefresh = requiresRefresh;
	}

    public int getDiveSiteCountWhenRetreived() {
        return mDiveSiteCountWhenRetreived;
    }

    public void setDiveSiteCountWhenRetreived(int siteCountWhenRetreived) {
        mDiveSiteCountWhenRetreived = siteCountWhenRetreived;
    }

	public ArrayList<DiveLog> getDiveLogs() {
		return mDiveLogs;
	}

	public String[] getFieldsAsStrings() {
		// Result will have all dive site fields
		String[] diveSiteFields = new String[DIVE_SITE_FIELD_COUNT
				+ (mPictures.size() * DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT)];

		// Set dive site fields
		diveSiteFields[DIVE_SITE_LOCAL_ID_INDEX] = String.valueOf(mLocalId);
		diveSiteFields[DIVE_SITE_ONLINE_ID_INDEX] = String.valueOf(mOnlineId);
		diveSiteFields[DIVE_SITE_NAME_INDEX] = mName;
		diveSiteFields[DIVE_SITE_TOTAL_RATE_INDEX] = String
				.valueOf(mTotalRating);
		diveSiteFields[DIVE_SITE_NUM_RATES_INDEX] = String
				.valueOf(mRatingCount);
		diveSiteFields[DIVE_SITE_CITY_INDEX] = mCity;
		diveSiteFields[DIVE_SITE_PROVINCE_INDEX] = mProvince;
		diveSiteFields[DIVE_SITE_COUNTRY_INDEX] = mCountry;
		diveSiteFields[DIVE_SITE_DIFFICULTY_INDEX] = mDifficulty.getName();
		diveSiteFields[DIVE_SITE_ISSALT_INDEX] = mIsSalty ? "1" : "0";
		diveSiteFields[DIVE_SITE_ISFRESH_INDEX] = !mIsSalty ? "1" : "0";
		diveSiteFields[DIVE_SITE_ISSHORE_INDEX] = mIsShoreDive ? "1" : "0";
		diveSiteFields[DIVE_SITE_ISBOAT_INDEX] = mIsBoatDive ? "1" : "0";
		diveSiteFields[DIVE_SITE_ISWRECK_INDEX] = mHasWreck ? "1" : "0";
		diveSiteFields[DIVE_SITE_HISTORY_INDEX] = mHistory;
		diveSiteFields[DIVE_SITE_DESCRIPTION_INDEX] = mDescription;
		diveSiteFields[DIVE_SITE_DIRECTIONS_INDEX] = mDirections;
		diveSiteFields[DIVE_SITE_SOURCE_INDEX] = mSource;
		diveSiteFields[DIVE_SITE_NOTES_INDEX] = mNotes;
		diveSiteFields[DIVE_SITE_LATITUDE_INDEX] = String.valueOf(mLatitude);
		diveSiteFields[DIVE_SITE_LONGITUDE_INDEX] = String.valueOf(mLongitude);
		diveSiteFields[DIVE_SITE_USER_ID_INDEX] = String.valueOf(mUserId);
		diveSiteFields[DIVE_SITE_DATE_ADDED_INDEX] = String.valueOf(mDateAdded
				.getTime());
		diveSiteFields[DIVE_SITE_LAST_MODIFIED_INDEX] = String
				.valueOf(mLastModifiedOnline.getTime());
		diveSiteFields[DIVE_SITE_APPROVED_INDEX] = "1";
		diveSiteFields[DIVE_SITE_USERNAME_INDEX] = mUsername;
		diveSiteFields[DIVE_SITE_PICTURES_START_POS_INDEX] = String
				.valueOf(DIVE_SITE_FIELD_COUNT);
		diveSiteFields[DIVE_SITE_PICTURES_COUNT_INDEX] = String
				.valueOf(mPictures.size());

		// Set dive site picture fields
		for (int i = 0; i < mPictures.size(); i++) {
			String[] pictureFields = mPictures.get(i).getFieldsAsStrings();
			System.arraycopy(
					pictureFields,
					0,
					diveSiteFields,
					DIVE_SITE_FIELD_COUNT
							+ (i * DiveSitePicture.DIVE_SITE_PICTURE_FIELD_COUNT),
					pictureFields.length);
		}

		return diveSiteFields;
	}
}
