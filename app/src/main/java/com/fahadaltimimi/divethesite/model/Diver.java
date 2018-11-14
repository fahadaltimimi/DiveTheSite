package com.fahadaltimimi.divethesite.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Diver implements Parcelable {

    private static final SimpleDateFormat JDONDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	// Diver's properties
	private long mLocalId;
	private long mOnlineId;
	private String mFirstName;
	private String mLastName;
	private String mEmail;
	private String mCity;
	private String mProvince;
	private String mCountry;
	private String mUsername;
	private String mBio;
	private String mPictureURL;
	private boolean mMod;
	private Date mCreated;
	private Date mLastModified;
	private int mLogCount;
	private int mDiveSiteSubmittedCount;
	private Drawable mPictureDrawable;
    private int mDiverCountWhenRetreived;
	private ArrayList<DiverCertification> mCertifications;

	public static final int DIVER_FIELD_COUNT = 11;
	public static final int DIVER_USERNAME_INDEX = 0;
	public static final int DIVER_EMAIL_INDEX = 1;
	public static final int DIVER_FIRSTNAME_INDEX = 2;
	public static final int DIVER_LASTNAME_INDEX = 3;
	public static final int DIVER_COUNTRY_INDEX = 4;
	public static final int DIVER_PROVINCE_INDEX = 5;
	public static final int DIVER_CITY_INDEX = 6;
	public static final int DIVER_PICTURE_URL_INDEX = 7;
	public static final int DIVER_BIO_INDEX = 8;
	public static final int DIVER_IS_MOD_INDEX = 9;
	public static final int DIVER_ID_INDEX = 10;

	private static final String JSON_TAG_DIVER_USER_ID = "USER_ID";
	private static final String JSON_TAG_DIVER_FIRST_NAME = "FIRST_NAME";
	private static final String JSON_TAG_DIVER_LAST_NAME = "LAST_NAME";
	private static final String JSON_TAG_DIVER_EMAIL = "EMAIL";
	private static final String JSON_TAG_DIVER_CITY = "CITY";
	private static final String JSON_TAG_DIVER_PROVINCE = "PROVINCE";
	private static final String JSON_TAG_DIVER_COUNTRY = "COUNTRY";
	private static final String JSON_TAG_DIVER_USERNAME = "USERNAME";
	private static final String JSON_TAG_DIVER_BIO = "BIO";
	public static final String JSON_TAG_DIVER_PICTURE = "PICTURE";
	public static final String JSON_TAG_DIVER_PICTURE_URL = "PICTURE_URL";
	private static final String JSON_TAG_DIVER_MOD = "IS_MOD";
	private static final String JSON_TAG_DIVER_CREATED = "CREATED";
	private static final String JSON_TAG_DIVER_LAST_MODIFIED = "LAST_MODIFIED";
	private static final String JSON_TAG_DIVER_LOG_COUNT = "LOG_COUNT";
	private static final String JSON_TAG_DIVER_DIVE_SITE_SUBMITTED_COUNT = "DIVE_SITE_SUBMITTED_COUNT";

    public static final String TAG_DIVER_COUNT = "DIVER_COUNT";

	public Diver() {
		mLocalId = -1;
		mOnlineId = -1;
		mCertifications = new ArrayList<DiverCertification>();
		mCreated = new Date();
		mLastModified = new Date();
		mLogCount = 0;
		mDiveSiteSubmittedCount = 0;
	}

	public Diver(long userId, String firstName, String lastName, String email,
			String city, String province, String country, String username,
			String bio, String pictureURL, Drawable pictureDrawable,
			boolean isMod, ArrayList<DiverCertification> certifications) {
		mLocalId = -1;
		mOnlineId = userId;
		mFirstName = firstName;
		mLastName = lastName;
		mEmail = email;
		mCity = city;
		mProvince = province;
		mCountry = country;
		mUsername = username;
		mBio = bio;
		mPictureURL = pictureURL;
		mPictureDrawable = pictureDrawable;
		mMod = isMod;

		mCertifications = new ArrayList<DiverCertification>();
		mCertifications.addAll(certifications);

		mCreated = new Date();
		mLastModified = new Date();

		mLogCount = 0;
		mDiveSiteSubmittedCount = 0;
        mDiverCountWhenRetreived = 0;
	}

	public Diver(JSONObject json) {
		try {
			mLocalId = -1;
			mOnlineId = json.getInt(JSON_TAG_DIVER_USER_ID);
			mFirstName = json.getString(JSON_TAG_DIVER_FIRST_NAME);
			mLastName = json.getString(JSON_TAG_DIVER_LAST_NAME);
			mEmail = json.getString(JSON_TAG_DIVER_EMAIL);
			mCity = json.getString(JSON_TAG_DIVER_CITY);
			mProvince = json.getString(JSON_TAG_DIVER_PROVINCE);
			mCountry = json.getString(JSON_TAG_DIVER_COUNTRY);
			mUsername = json.getString(JSON_TAG_DIVER_USERNAME);
			mBio = json.getString(JSON_TAG_DIVER_BIO);
			mMod = json.getInt(JSON_TAG_DIVER_MOD) == 1;
			mCertifications = new ArrayList<DiverCertification>();
			mPictureURL = json.getString(JSON_TAG_DIVER_PICTURE_URL);

			mLastModified = new Date();
			mCreated = new Date();
			try {
				if (json.has(JSON_TAG_DIVER_LAST_MODIFIED)) {
					mLastModified = JDONDateFormat.parse(json
							.getString(JSON_TAG_DIVER_LAST_MODIFIED));
				}

				if (json.has(JSON_TAG_DIVER_CREATED)) {
					mCreated = JDONDateFormat.parse(json
							.getString(JSON_TAG_DIVER_CREATED));
				}
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mLogCount = json.getInt(JSON_TAG_DIVER_LOG_COUNT);
			mDiveSiteSubmittedCount = json.getInt(JSON_TAG_DIVER_DIVE_SITE_SUBMITTED_COUNT);

            if (json.has(TAG_DIVER_COUNT)) {
                mDiverCountWhenRetreived = json.getInt(TAG_DIVER_COUNT);
            } else {
                mDiverCountWhenRetreived = 0;
            }

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Diver(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mFirstName = source.readString();
		mLastName = source.readString();
		mEmail = source.readString();
		mCity = source.readString();
		mProvince = source.readString();
		mCountry = source.readString();
		mUsername = source.readString();
		mBio = source.readString();
		mPictureURL = source.readString();
		mMod = source.readByte() != 0;
		mCertifications = new ArrayList<DiverCertification>();
		source.readTypedList(mCertifications, DiverCertification.CREATOR);
		mCreated = new Date();
		mLastModified = new Date();
		mLogCount = source.readInt();
		mDiveSiteSubmittedCount = source.readInt();
        mDiverCountWhenRetreived = source.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeString(mFirstName);
		dest.writeString(mLastName);
		dest.writeString(mEmail);
		dest.writeString(mCity);
		dest.writeString(mProvince);
		dest.writeString(mCountry);
		dest.writeString(mUsername);
		dest.writeString(mBio);
		dest.writeString(mPictureURL);
		dest.writeByte((byte) (mMod ? 1 : 0));
		dest.writeTypedList(mCertifications);
		dest.writeLong(mCreated.getTime());
		dest.writeLong(mLastModified.getTime());
		dest.writeInt(mLogCount);
		dest.writeInt(mDiveSiteSubmittedCount);
        dest.writeInt(mDiverCountWhenRetreived);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<Diver> CREATOR = new Parcelable.Creator<Diver>() {
		@Override
		public Diver createFromParcel(Parcel in) {
			return new Diver(in);
		}

		@Override
		public Diver[] newArray(int size) {
			return new Diver[size];
		}
	};

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

	public void setUserId(long userId) {
		mOnlineId = userId;
	}

	public String getFirstName() {
		if (mFirstName == null || mFirstName.trim().isEmpty()) {
			return " ";
		} else {
			return mFirstName;
		}
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public String getLastName() {
		if (mLastName == null || mLastName.trim().isEmpty()) {
			return " ";
		} else {
			return mLastName;
		}
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	public String getEmail() {
		if (mEmail == null || mEmail.trim().isEmpty()) {
			return " ";
		} else {
			return mEmail;
		}
	}

	public void setEmail(String email) {
		mEmail = email;
	}

	public String getCity() {
		if (mCity == null || mCity.trim().isEmpty()) {
			return " ";
		} else {
			return mCity;
		}
	}

	public void setCity(String city) {
		mCity = city;
	}

	public String getProvince() {
		if (mProvince == null || mProvince.trim().isEmpty()) {
			return " ";
		} else {
			return mProvince;
		}
	}

	public void setProvince(String province) {
		mProvince = province;
	}

	public String getCountry() {
		if (mCountry == null || mCountry.trim().isEmpty()) {
			return " ";
		} else {
			return mCountry;
		}
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

	public String getUsername() {
		if (mUsername == null || mUsername.trim().isEmpty()) {
			return null;
		} else {
			return mUsername;
		}
	}

	public void setUsername(String username) {
		mUsername = username;
	}

	public String getBio() {
		if (mBio == null) {
			return "";
		} else {
			return mBio;
		}
	}

	public void setBio(String bio) {
		mBio = bio;
	}

	public String getPictureURL() {
		if (mPictureURL == null || mPictureURL.trim().isEmpty()) {
			return " ";
		} else {
			return mPictureURL;
		}
	}

	public void setPictureURL(String pictureURL) {
		mPictureURL = pictureURL;
	}

	public Drawable getPictureDrawable() {
		return mPictureDrawable;
	}

	public void setPictureDrawable(Drawable pictureDrawable) {
		mPictureDrawable = pictureDrawable;
	}

	public boolean isMod() {
		return mMod;
	}

	public void setMod(boolean mod) {
		mMod = mod;
	}

	public Date getCreated() {
		return mCreated;
	}

	public void setCreated(Date created) {
		mCreated = created;
	}

	public Date getLastModified() {
		return mLastModified;
	}

	public void setLastModified(Date dateModified) {
		mLastModified = dateModified;
	}

	public int getLogCount() {
		return mLogCount;
	}

	public void setLogCount(int logCount) {
		mLogCount = logCount;
	}

	public int getDiveSiteSubmittedCount() {
		return mDiveSiteSubmittedCount;
	}

	public void setDiveSiteSubmittedCount(int diveSiteSubmittedCount) {
		mDiveSiteSubmittedCount = diveSiteSubmittedCount;
	}

    public int getDiverCountWhenRetreived() {
        return mDiverCountWhenRetreived;
    }

    public void setDiverCountWhenRetreived(int diverCountWhenRetreived) {
        mDiverCountWhenRetreived = diverCountWhenRetreived;
    }

	public ArrayList<DiverCertification> getCertifications() {
		return mCertifications;
	}

	public void setCertifications(ArrayList<DiverCertification> certifications) {
		mCertifications = certifications;
	}

	public DiverCertification addNewCertification() {
		DiverCertification cert = new DiverCertification();
		cert.setCertifUserId(getOnlineId());

		return cert;
	}

	public void addCertification(DiverCertification cert) {
		if (cert.getCertifUserId() == mOnlineId) {
			// Replace cert with same id
			for (int i = 0; i < mCertifications.size(); i++) {
				if ((mCertifications.get(i).getLocalId() != -1 &&
					 mCertifications.get(i).getLocalId() == cert.getLocalId()) ||
					(mCertifications.get(i).getOnlineId() != -1 &&
					 mCertifications.get(i).getOnlineId() == cert.getOnlineId())) {
					mCertifications.remove(i);
					break;
				}
			}

			mCertifications.add(cert);
		}
	}

	public void addJSONCertification(JSONObject json) {
		DiverCertification diverCertification = new DiverCertification(json);
		if (diverCertification.getCertifUserId() == mOnlineId) {
			mCertifications.add(diverCertification);
		}
	}

	public String[] getFieldsAsStrings() {
		// Result will have all diver fields and all certification fields
		String[] diverFields = new String[DIVER_FIELD_COUNT
				+ (mCertifications.size() * DiverCertification.CERTIFICATION_FIELD_COUNT)];

		// Set diver fields
		diverFields[DIVER_USERNAME_INDEX] = mUsername;
		diverFields[DIVER_EMAIL_INDEX] = mEmail;
		diverFields[DIVER_FIRSTNAME_INDEX] = mFirstName;
		diverFields[DIVER_LASTNAME_INDEX] = mLastName;
		diverFields[DIVER_COUNTRY_INDEX] = mCountry;
		diverFields[DIVER_PROVINCE_INDEX] = mProvince;
		diverFields[DIVER_CITY_INDEX] = mCity;
		diverFields[DIVER_PICTURE_URL_INDEX] = mPictureURL;
		diverFields[DIVER_BIO_INDEX] = mBio;
		diverFields[DIVER_IS_MOD_INDEX] = String.valueOf(mMod);
		diverFields[DIVER_ID_INDEX] = String.valueOf(mOnlineId);

		// Set certification fields
		for (int i = 0; i < mCertifications.size(); i++) {
			String[] certFields = mCertifications.get(i).getFieldsAsStrings();
			System.arraycopy(certFields, 0, diverFields, DIVER_FIELD_COUNT
					+ (i * DiverCertification.CERTIFICATION_FIELD_COUNT),
					certFields.length);
		}

		return diverFields;
	}
}
