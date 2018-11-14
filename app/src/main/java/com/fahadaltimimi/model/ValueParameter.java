package com.fahadaltimimi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.fahadaltimimi.data.UnitConverter;

import java.text.DecimalFormat;

public class ValueParameter implements Parcelable {

	private double mValue;
	private String mUnits;

	public ValueParameter(double value, String units) {
		mValue = value;
		mUnits = units;
	}

	public ValueParameter(Parcel source) {
		mValue = source.readDouble();
		mUnits = source.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(mValue);
		dest.writeString(mUnits);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<ValueParameter> CREATOR = new Parcelable.Creator<ValueParameter>() {
		@Override
		public ValueParameter createFromParcel(Parcel in) {
			return new ValueParameter(in);
		}

		@Override
		public ValueParameter[] newArray(int size) {
			return new ValueParameter[size];
		}
	};

	public double getValue() {
		return mValue;
	}

	public void setValue(double value) {
		mValue = value;
	}

	public String getUnits() {
		return mUnits;
	}

	public void setUnits(String units) {
        // Change value with unit change before resetting units
        DecimalFormat df = new DecimalFormat("#.00");
        mValue = Math.round(UnitConverter.convertValueToUnits(mValue, mUnits, units) * 100.0) / 100.0;

		mUnits = units;
    }

	@Override
	public String toString() {
		return String.valueOf(mValue) + ' ' + mUnits;
	}

}
