package com.fahadaltimimi.divethesite.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class DiveSitePicture implements Parcelable {
	private long mLocalID;
	private long mOnlineID;
	private long mDiveSiteLocalID;
	private long mDiveSiteOnlineID;
	private String mBitmapFilePath;
	private String mBitmapURL;
	private String mPictureDescription;

	public static final int DIVE_SITE_PICTURE_FIELD_COUNT = 7;
	public static final int DIVE_SITE_PICTURE_LOCAL_ID_INDEX = 0;
	public static final int DIVE_SITE_PICTURE_ONLINE_ID_INDEX = 1;
	public static final int DIVE_SITE_PICTURE_DIVE_SITE_LOCAL_ID_INDEX = 2;
	public static final int DIVE_SITE_PICTURE_DIVE_SITE_ONLINE_ID_INDEX = 3;
	public static final int DIVE_SITE_PICTURE_FILE_PATH_INDEX = 4;
	public static final int DIVE_SITE_PICTURE_URL_INDEX = 5;
	public static final int DIVE_SITE_PICTURE_DESCRIPTION_INDEX = 6;

	public static final String DIVE_SITE_PICTURE_LOCAL_ID_PARAM = "PIC_LOCAL_ID";
	public static final String DIVE_SITE_PICTURE_ONLINE_ID_PARAM = "PIC_ID";
	public static final String DIVE_SITE_PICTURE_SITE_ID_PARAM = "SITE_ID";
	public static final String DIVE_SITE_PICTURE_DESCRIPTION_PARAM = "PIC_DESC";
	public static final String DIVE_SITE_PICTURE_URL_PARAM = "PIC_URL";
	public static final String DIVE_SITE_PICTURE_FILE_PATH_PARAM = "FILE_PATH";
	public static final String DIVE_SITE_PICTURE_NEW_IMAGE_PARAM = "NEW_IMAGE";

	public DiveSitePicture() {
		mLocalID = -1;
		mOnlineID = -1;
		mDiveSiteLocalID = -1;
		mDiveSiteOnlineID = -1;
		mBitmapFilePath = "";
		mBitmapURL = "";
		mPictureDescription = "";
	}

	public DiveSitePicture(Parcel source) {
		mLocalID = source.readLong();
		mOnlineID = source.readLong();
		mDiveSiteLocalID = source.readLong();
		mDiveSiteOnlineID = source.readLong();
		mBitmapFilePath = source.readString();
		mBitmapURL = source.readString();
		mPictureDescription = source.readString();
	}

	public DiveSitePicture(JSONObject json) {
		try {
			mLocalID = -1;
			mDiveSiteLocalID = -1;
			mBitmapFilePath = "";
			if (json.has(DIVE_SITE_PICTURE_LOCAL_ID_PARAM)) {
				mLocalID = json.getInt(DIVE_SITE_PICTURE_FILE_PATH_PARAM);
			}
			mOnlineID = json.getInt(DIVE_SITE_PICTURE_ONLINE_ID_PARAM);
			mDiveSiteOnlineID = json.getInt(DIVE_SITE_PICTURE_SITE_ID_PARAM);
			if (json.has(DIVE_SITE_PICTURE_FILE_PATH_PARAM)) {
				mBitmapFilePath = json
						.getString(DIVE_SITE_PICTURE_FILE_PATH_PARAM);
			}
			mBitmapURL = json.getString(DIVE_SITE_PICTURE_URL_PARAM);
			mPictureDescription = json
					.getString(DIVE_SITE_PICTURE_DESCRIPTION_PARAM);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mLocalID);
		dest.writeLong(mOnlineID);
		dest.writeLong(mDiveSiteLocalID);
		dest.writeLong(mDiveSiteOnlineID);
		dest.writeString(mBitmapFilePath);
		dest.writeString(mBitmapURL);
		dest.writeString(mPictureDescription);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<DiveSitePicture> CREATOR = new Parcelable.Creator<DiveSitePicture>() {
		@Override
		public DiveSitePicture createFromParcel(Parcel in) {
			return new DiveSitePicture(in);
		}

		@Override
		public DiveSitePicture[] newArray(int size) {
			return new DiveSitePicture[size];
		}
	};

	public long getLocalId() {
		return mLocalID;
	}

	public void setLocalId(long localID) {
		mLocalID = localID;
	}

	public long getOnlineId() {
		return mOnlineID;
	}

	public void setOnlineID(long onlineID) {
		mOnlineID = onlineID;
	}

	public long getDiveSiteLocalID() {
		return mDiveSiteLocalID;
	}

	public void setDiveSiteLocalID(long diveSiteLocalID) {
		mDiveSiteLocalID = diveSiteLocalID;
	}

	public long getDiveSiteOnlineID() {
		return mDiveSiteOnlineID;
	}

	public void setDiveSiteOnlineID(long diveSiteOnlineID) {
		mDiveSiteOnlineID = diveSiteOnlineID;
	}

	public String getBitmapFilePath() {
		return mBitmapFilePath;
	}

	public void setBitmapFilePath(String bitmapFilePath) {
		mBitmapFilePath = bitmapFilePath;
	}

	public String getBitmapURL() {
		return mBitmapURL;
	}

	public void setBitmapURL(String bitmapURL) {
		mBitmapURL = bitmapURL;
	}

	public String getPictureDescription() {
		return mPictureDescription;
	}

	public void setPictureDescription(String pictureDescription) {
		mPictureDescription = pictureDescription;
	}

	public String[] getFieldsAsStrings() {
		String[] diveSitePictureFields = new String[DIVE_SITE_PICTURE_FIELD_COUNT];

		diveSitePictureFields[DIVE_SITE_PICTURE_LOCAL_ID_INDEX] = String
				.valueOf(mLocalID);
		diveSitePictureFields[DIVE_SITE_PICTURE_ONLINE_ID_INDEX] = String
				.valueOf(mOnlineID);
		diveSitePictureFields[DIVE_SITE_PICTURE_DIVE_SITE_LOCAL_ID_INDEX] = String
				.valueOf(mDiveSiteLocalID);
		diveSitePictureFields[DIVE_SITE_PICTURE_DIVE_SITE_ONLINE_ID_INDEX] = String
				.valueOf(mDiveSiteOnlineID);
		diveSitePictureFields[DIVE_SITE_PICTURE_FILE_PATH_INDEX] = String
				.valueOf(mBitmapFilePath);
		diveSitePictureFields[DIVE_SITE_PICTURE_URL_INDEX] = String
				.valueOf(mBitmapURL);
		diveSitePictureFields[DIVE_SITE_PICTURE_DESCRIPTION_INDEX] = String
				.valueOf(mPictureDescription);

		return diveSitePictureFields;
	}
}