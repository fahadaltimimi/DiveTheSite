package com.fahadaltimimi.divethesite.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class DiverCertification implements Parcelable {

	private long mLocalId;
	private long mOnlineId;
	private long mCertifUserId;
	private String mCertifTitle;
	private String mCertifDate;
	private String mCertifNumber;
	private String mCertifLocation;
	private Boolean mPrimary;

	public static final int CERTIFICATION_FIELD_COUNT = 7;
	public static final int CERTIF_ID_INDEX = 0;
	public static final int CERTIF_USER_ID_INDEX = 1;
	public static final int CERTIF_TITLE_INDEX = 2;
	public static final int CERTIF_DATE_INDEX = 3;
	public static final int CERTIF_NUMBER_INDEX = 4;
	public static final int CERTIF_LOCATION_INDEX = 5;
	public static final int CERTIF_PRIMARY_INDEX = 6;

	private static final String JSON_TAG_DIVER_CERTIF_ID = "CERTIF_ID";
	private static final String JSON_TAG_DIVER_USER_ID = "USER_ID";
	private static final String JSON_TAG_DIVER_CERTIF_NAME = "CERTIF_NAME";
	private static final String JSON_TAG_DIVER_CERTIF_DATE = "CERTIF_DATE";
	private static final String JSON_TAG_DIVER_CERTIF_NO = "CERTIF_NO";
	private static final String JSON_TAG_DIVER_CERTIF_LOCATION = "LOCATION";
	private static final String JSON_TAG_DIVER_CERTIF_IS_PRIMARY = "IS_PRIMARY";

	public DiverCertification() {
		mLocalId = -1;
		mOnlineId = -1;
		mCertifUserId = -1;
		mCertifTitle = "";
		mCertifDate = "";
		mCertifNumber = "";
		mCertifLocation = "";
		mPrimary = false;
	}

	public DiverCertification(JSONObject json) {
		try {
			mLocalId = -1;
			mOnlineId = json.getLong(JSON_TAG_DIVER_CERTIF_ID);
			mCertifUserId = json.getLong(JSON_TAG_DIVER_USER_ID);
			mCertifTitle = json.getString(JSON_TAG_DIVER_CERTIF_NAME);
			mCertifDate = json.getString(JSON_TAG_DIVER_CERTIF_DATE);
			mCertifNumber = json.getString(JSON_TAG_DIVER_CERTIF_NO);
			mCertifLocation = json.getString(JSON_TAG_DIVER_CERTIF_LOCATION);
			mPrimary = json.getInt(JSON_TAG_DIVER_CERTIF_IS_PRIMARY) == 1;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Create's copy of given certification
	public DiverCertification(DiverCertification diverCertification) {
		mLocalId = diverCertification.getLocalId();
		mOnlineId = diverCertification.getOnlineId();
		mCertifUserId = diverCertification.getCertifUserId();
		mCertifTitle = diverCertification.mCertifTitle;
		mCertifDate = diverCertification.getCertifDate();
		mCertifNumber = diverCertification.getCertifNumber();
		mCertifLocation = diverCertification.getCertifLocation();
		mPrimary = diverCertification.getPrimary();
	}

	public DiverCertification(Parcel source) {
		mLocalId = source.readLong();
		mOnlineId = source.readLong();
		mCertifUserId = source.readLong();
		mCertifTitle = source.readString();
		mCertifDate = source.readString();
		mCertifNumber = source.readString();
		mCertifLocation = source.readString();
		mPrimary = source.readByte() != 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalId);
		dest.writeLong(mOnlineId);
		dest.writeLong(mCertifUserId);
		dest.writeString(mCertifTitle);
		dest.writeString(mCertifDate);
		dest.writeString(mCertifNumber);
		dest.writeString(mCertifLocation);
		dest.writeByte((byte) (mPrimary ? 1 : 0));
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DiverCertification> CREATOR = new Parcelable.Creator<DiverCertification>() {
		@Override
		public DiverCertification createFromParcel(Parcel in) {
			return new DiverCertification(in);
		}

		@Override
		public DiverCertification[] newArray(int size) {
			return new DiverCertification[size];
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

	public long getCertifUserId() {
		return mCertifUserId;
	}

	public void setCertifUserId(long certifUserId) {
		mCertifUserId = certifUserId;
	}

	public String getCertifTitle() {
		return mCertifTitle;
	}

	public void setCertifTitle(String certifTitle) {
		mCertifTitle = certifTitle;
	}

	public String getCertifDate() {
		return mCertifDate;
	}

	public void setCertifDate(String certifDate) {
		mCertifDate = certifDate;
	}

	public String getCertifNumber() {
		return mCertifNumber;
	}

	public void setCertifNumber(String certifNumber) {
		mCertifNumber = certifNumber;
	}

	public String getCertifLocation() {
		return mCertifLocation;
	}

	public void setCertifLocation(String certifLocation) {
		mCertifLocation = certifLocation;
	}

	public Boolean getPrimary() {
		return mPrimary;
	}

	public void setPrimary(Boolean primary) {
		mPrimary = primary;
	}

	public String[] getFieldsAsStrings() {
		String[] certificationFields = new String[CERTIFICATION_FIELD_COUNT];
		certificationFields[CERTIF_ID_INDEX] = String.valueOf(mOnlineId);
		certificationFields[CERTIF_USER_ID_INDEX] = String.valueOf(mCertifUserId);
		certificationFields[CERTIF_TITLE_INDEX] = mCertifTitle;
		certificationFields[CERTIF_DATE_INDEX] = mCertifDate;
		certificationFields[CERTIF_NUMBER_INDEX] = mCertifNumber;
		certificationFields[CERTIF_LOCATION_INDEX] = mCertifLocation;
		certificationFields[CERTIF_PRIMARY_INDEX] = String.valueOf(mPrimary ? 1: 0);

		return certificationFields;
	}
}