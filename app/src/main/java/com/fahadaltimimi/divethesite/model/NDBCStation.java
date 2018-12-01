package com.fahadaltimimi.divethesite.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

public class NDBCStation {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public class NDBCMeteorologicalData {
		private static final String JSON_TAG_METEOROLOGICAL_DATA_TIME = "DATA_TIME";
		private static final String JSON_TAG_METEOROLOGICAL_WIND_DIRECTION = "WIND_DIRECTION";
		private static final String JSON_TAG_METEOROLOGICAL_WIND_SPEED = "WIND_SPEED";
		private static final String JSON_TAG_METEOROLOGICAL_WIND_GUST = "WIND_GUST";
		private static final String JSON_TAG_METEOROLOGICAL_SIGNIFICANT_WAVE_HEIGHT = "SIGNIFICANT_WAVE_HEIGHT";
		private static final String JSON_TAG_METEOROLOGICAL_DOMINANT_WAVE_PERIOD = "DOMINANT_WAVE_PERIOD";
		private static final String JSON_TAG_METEOROLOGICAL_AVERAGE_WAVE_PERIOD = "AVERAGE_WAVE_PERIOD";
		private static final String JSON_TAG_METEOROLOGICAL_DOMINANT_WAVE_DIRECTION = "DOMINANT_WAVE_DIRECTION";
		private static final String JSON_TAG_METEOROLOGICAL_SEA_LEVEL_PRESSURE = "SEA_LEVEL_PRESSURE";
		private static final String JSON_TAG_METEOROLOGICAL_AIR_TEMPERATURE = "AIR_TEMPERATURE";
		private static final String JSON_TAG_METEOROLOGICAL_WATER_TEMPERATURE = "WATER_TEMPERATURE";
		private static final String JSON_TAG_METEOROLOGICAL_DEW_POINT_TEMPERATURE = "DEW_POINT_TEMPERATURE";
		private static final String JSON_TAG_METEOROLOGICAL_STATION_VISIBILITY = "STATION_VISIBILITY";
		private static final String JSON_TAG_METEOROLOGICAL_PRESSURE_TENDENCY = "PRESSURE_TENDENCY";
		private static final String JSON_TAG_METEOROLOGICAL_TIDE = "TIDE";

		private String mDataTimeStr;
        private Date mDataTime;
		private String mWindDirection;
		private String mWindSpeed;
		private String mWindGust;
		private String mSignificantWaveHeight;
		private String mDominantWavePeriod;
		private String mAverageWavePeriod;
		private String mDominantWaveDirection;
		private String mSeaLevelPressure;
		private String mAirTemperature;
		private String mWaterTemperature;
		private String mDewPointTemperature;
		private String mStationVisibility;
		private String mPressureTendency;
		private String mTide;

        public NDBCMeteorologicalData(JSONObject json) {

            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			try {

                mDataTimeStr = json.getString(JSON_TAG_METEOROLOGICAL_DATA_TIME);
                try {
                    mDataTime = dateFormat.parse(mDataTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

				mWindDirection = json
						.getString(JSON_TAG_METEOROLOGICAL_WIND_DIRECTION);
				mWindSpeed = json.getString(JSON_TAG_METEOROLOGICAL_WIND_SPEED);
				mWindGust = json.getString(JSON_TAG_METEOROLOGICAL_WIND_GUST);
				mSignificantWaveHeight = json
						.getString(JSON_TAG_METEOROLOGICAL_SIGNIFICANT_WAVE_HEIGHT);
				mDominantWavePeriod = json
						.getString(JSON_TAG_METEOROLOGICAL_DOMINANT_WAVE_PERIOD);
				mAverageWavePeriod = json
						.getString(JSON_TAG_METEOROLOGICAL_AVERAGE_WAVE_PERIOD);
				mDominantWaveDirection = json
						.getString(JSON_TAG_METEOROLOGICAL_DOMINANT_WAVE_DIRECTION);
				mSeaLevelPressure = json
						.getString(JSON_TAG_METEOROLOGICAL_SEA_LEVEL_PRESSURE);
				mAirTemperature = json
						.getString(JSON_TAG_METEOROLOGICAL_AIR_TEMPERATURE);
				mWaterTemperature = json
						.getString(JSON_TAG_METEOROLOGICAL_WATER_TEMPERATURE);
				mDewPointTemperature = json
						.getString(JSON_TAG_METEOROLOGICAL_DEW_POINT_TEMPERATURE);
				mStationVisibility = json
						.getString(JSON_TAG_METEOROLOGICAL_STATION_VISIBILITY);
				mPressureTendency = json
						.getString(JSON_TAG_METEOROLOGICAL_PRESSURE_TENDENCY);
				mTide = json.getString(JSON_TAG_METEOROLOGICAL_TIDE);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public Date getDataTime() {
			return mDataTime;
		}

		public Double getWindDirection() {
			if (mWindDirection.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindDirection);
		}

		public void setWindDirection(Double windDirection) {
			mWindDirection = String.valueOf(windDirection);
		}

		public Double getWindSpeed() {
			if (mWindSpeed.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindSpeed);
		}

		public void setWindSpeed(Double windSpeed) {
			mWindSpeed = String.valueOf(windSpeed);
		}

		public Double getWindGust() {
			if (mWindGust.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindGust);
		}

		public void setWindGust(Double windGust) {
			mWindGust = String.valueOf(windGust);
		}

		public Double getSignificantWaveHeight() {
			if (mSignificantWaveHeight.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mSignificantWaveHeight);
		}

		public void setSignificantWaveHeight(Double significantWaveHeight) {
			mSignificantWaveHeight = String.valueOf(significantWaveHeight);
		}

		public Double getDominantWavePeriod() {
			if (mDominantWavePeriod.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mDominantWavePeriod);
		}

		public void setDominantWavePeriod(Double dominantWavePeriod) {
			mDominantWavePeriod = String.valueOf(dominantWavePeriod);
		}

		public Double getAverageWavePeriod() {
			if (mAverageWavePeriod.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mAverageWavePeriod);
		}

		public void setAverageWavePeriod(Double averageWavePeriod) {
			mAverageWavePeriod = String.valueOf(averageWavePeriod);
		}

		public Double getDominantWaveDirection() {
			if (mDominantWaveDirection.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mDominantWaveDirection);
		}

		public void setDominantWaveDirection(Double dominantWaveDirection) {
			mDominantWaveDirection = String.valueOf(dominantWaveDirection);
		}

		public Double getSeaLevelPressure() {
			if (mSeaLevelPressure.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mSeaLevelPressure);
		}

		public void setSeaLevelPressure(Double seaLevelPressure) {
			mSeaLevelPressure = String.valueOf(seaLevelPressure);
		}

		public Double getAirTemperature() {
			if (mAirTemperature.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mAirTemperature);
		}

		public void setAirTemperature(Double airTemperature) {
			mAirTemperature = String.valueOf(airTemperature);
		}

		public Double getWaterTemperature() {
			if (mWaterTemperature.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWaterTemperature);
		}

		public void setWaterTemperature(Double waterTemperature) {
			mWaterTemperature = String.valueOf(waterTemperature);
		}

		public Double getDewPointTemperature() {
			if (mDewPointTemperature.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mDewPointTemperature);
		}

		public void setDewPointTemperature(Double dewPointTemperature) {
			mDewPointTemperature = String.valueOf(dewPointTemperature);
		}

		public Double getStationVisibility() {
			if (mStationVisibility.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mStationVisibility);
		}

		public void setStationVisibility(Double stationVisibility) {
			mStationVisibility = String.valueOf(stationVisibility);
		}

		public Double getPressureTendency() {
			if (mPressureTendency.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mPressureTendency);
		}

		public void setPressureTendency(Double pressureTendency) {
			mPressureTendency = String.valueOf(pressureTendency);
		}

		public Double getTide() {
			if (mTide.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mTide);
		}

		public void setTide(Double tide) {
			mTide = String.valueOf(tide);
		}
	}

	public class NDBCDriftingBuoyData {

		private static final String JSON_TAG_DRIFTING_BUOY_DATA_TIME = "DATA_TIME";
		private static final String JSON_TAG_DRIFTING_BUOY_LATITUDE = "LATITUDE";
		private static final String JSON_TAG_DRIFTING_BUOY_LONGITUDE = "LONGITUDE";
		private static final String JSON_TAG_DRIFTING_BUOY_WIND_DIRECTION = "WIND_DIRECTION";
		private static final String JSON_TAG_DRIFTING_BUOY_WIND_SPEED = "WIND_SPEED";
		private static final String JSON_TAG_DRIFTING_BUOY_WIND_GUST = "WIND_GUST";
		private static final String JSON_TAG_DRIFTING_BUOY_SEA_LEVEL_PRESSURE = "SEA_LEVEL_PRESSURE";
		private static final String JSON_TAG_DRIFTING_BUOY_PRESSURE_TENDENCY = "PRESSURE_TENDENCY";
		private static final String JSON_TAG_DRIFTING_BUOY_AIR_TEMPERATURE = "AIR_TEMPERATURE";
		private static final String JSON_TAG_DRIFTING_BUOY_WATER_TEMPERATURE = "WATER_TEMPERATURE";

		private String mDataTimeStr;
        private Date mDataTime;
		private String mLatitude;
		private String mLongitude;
		private String mWindDirection;
		private String mWindSpeed;
		private String mWindGust;
		private String mSeaLevelPressure;
		private String mPressureTendency;
		private String mAirTemperature;
		private String mWaterTemperature;

		public NDBCDriftingBuoyData(JSONObject json) {

            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			try {
                mDataTimeStr = json.getString(JSON_TAG_DRIFTING_BUOY_DATA_TIME);
                try {
                    mDataTime = dateFormat.parse(mDataTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

				mLatitude = json.getString(JSON_TAG_DRIFTING_BUOY_LATITUDE);
				mLongitude = json.getString(JSON_TAG_DRIFTING_BUOY_LONGITUDE);
				mWindDirection = json
						.getString(JSON_TAG_DRIFTING_BUOY_WIND_DIRECTION);
				mWindSpeed = json.getString(JSON_TAG_DRIFTING_BUOY_WIND_SPEED);
				mWindGust = json.getString(JSON_TAG_DRIFTING_BUOY_WIND_GUST);
				mSeaLevelPressure = json
						.getString(JSON_TAG_DRIFTING_BUOY_SEA_LEVEL_PRESSURE);
				mAirTemperature = json
						.getString(JSON_TAG_DRIFTING_BUOY_AIR_TEMPERATURE);
				mWaterTemperature = json
						.getString(JSON_TAG_DRIFTING_BUOY_WATER_TEMPERATURE);
				mPressureTendency = json
						.getString(JSON_TAG_DRIFTING_BUOY_PRESSURE_TENDENCY);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public Date getDataTime() {
			return mDataTime;
		}

		public Double getLatitude() {
			if (mLatitude.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mLatitude);
		}

		public void setLatitude(Double latitude) {
			mLatitude = String.valueOf(latitude);
		}

		public Double getLongitude() {
			if (mLongitude.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mLongitude);
		}

		public void setLongitude(Double longitude) {
			mLongitude = String.valueOf(longitude);
		}

		public Double getWindDirection() {
			if (mWindDirection.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindDirection);
		}

		public void setWindDirection(Double windDirection) {
			mWindDirection = String.valueOf(windDirection);
		}

		public Double getWindSpeed() {
			if (mWindSpeed.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindSpeed);
		}

		public void setWindSpeed(Double windSpeed) {
			mWindSpeed = String.valueOf(windSpeed);
		}

		public Double getWindGust() {
			if (mWindGust.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindGust);
		}

		public void setWindGust(Double windGust) {
			mWindGust = String.valueOf(windGust);
		}

		public Double getSeaLevelPressure() {
			if (mSeaLevelPressure.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mSeaLevelPressure);
		}

		public void setSeaLevelPressure(Double seaLevelPressure) {
			mSeaLevelPressure = String.valueOf(seaLevelPressure);
		}

		public Double getPressureTendency() {
			if (mPressureTendency.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mPressureTendency);
		}

		public void setPressureTendency(Double pressureTendency) {
			mPressureTendency = String.valueOf(pressureTendency);
		}

		public Double getAirTemperature() {
			if (mAirTemperature.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mAirTemperature);
		}

		public void setAirTemperature(Double airTemperature) {
			mAirTemperature = String.valueOf(airTemperature);
		}

		public Double getWaterTemperature() {
			if (mWaterTemperature.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWaterTemperature);
		}

		public void setWaterTemperature(Double waterTemperature) {
			mWaterTemperature = String.valueOf(waterTemperature);
		}

	}

	public class NDBCSpectralWaveData {

		private static final String JSON_TAG_SPECTRAL_WAVE_DATA_TIME = "DATA_TIME";
		private static final String JSON_TAG_SPECTRAL_WAVE_WAVE_HEIGHT = "WAVE_HEIGHT";
		private static final String JSON_TAG_SPECTRAL_WAVE_SWELL_HEIGHT = "SWELL_HEIGHT";
		private static final String JSON_TAG_SPECTRAL_WAVE_SWELL_PERIOD = "SWELL_PERIOD";
		private static final String JSON_TAG_SPECTRAL_WAVE_WIND_WAVE_HEIGHT = "WIND_WAVE_HEIGHT";
		private static final String JSON_TAG_SPECTRAL_WAVE_WIND_WAVE_PERIOD = "WIND_WAVE_PERIOD";
		private static final String JSON_TAG_SPECTRAL_WAVE_SWELL_DIRECTION = "SWELL_DIRECTION";
		private static final String JSON_TAG_SPECTRAL_WAVE_WIND_WAVE_DIRECTION = "WIND_WAVE_DIRECTION";
		private static final String JSON_TAG_SPECTRAL_WAVE_WAVE_STEEPNESS = "WAVE_STEEPNESS";
		private static final String JSON_TAG_SPECTRAL_WAVE_AVERAGE_WAVE_PERIOD = "AVERAGE_WAVE_PERIOD";
		private static final String JSON_TAG_SPECTRAL_WAVE_DOMINANT_WAVE_DIRECTION = "DOMINANT_WAVE_DIRECTION";

		private String mDataTimeStr;
        private Date mDataTime;
		private String mWaveHeight;
		private String mSwellHeight;
		private String mSwellPeriod;
		private String mWindWaveHeight;
		private String mWindWavePeriod;
		private String mSwellDirection;
		private String mWindWaveDirection;
		private String mWaveSteepness;
		private String mAverageWavePeriod;
		private String mDominantWaveDirection;

		public NDBCSpectralWaveData(JSONObject json) {

            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			try {
                mDataTimeStr = json.getString(JSON_TAG_SPECTRAL_WAVE_DATA_TIME);
                try {
                    mDataTime = dateFormat.parse(mDataTimeStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

				mWaveHeight = json
						.getString(JSON_TAG_SPECTRAL_WAVE_WAVE_HEIGHT);
				mSwellHeight = json
						.getString(JSON_TAG_SPECTRAL_WAVE_SWELL_HEIGHT);
				mSwellPeriod = json
						.getString(JSON_TAG_SPECTRAL_WAVE_SWELL_PERIOD);
				mWindWaveHeight = json
						.getString(JSON_TAG_SPECTRAL_WAVE_WIND_WAVE_HEIGHT);
				mWindWavePeriod = json
						.getString(JSON_TAG_SPECTRAL_WAVE_WIND_WAVE_PERIOD);
				mSwellDirection = json
						.getString(JSON_TAG_SPECTRAL_WAVE_SWELL_DIRECTION);
				mWindWaveDirection = json
						.getString(JSON_TAG_SPECTRAL_WAVE_WIND_WAVE_DIRECTION);
				mWaveSteepness = json
						.getString(JSON_TAG_SPECTRAL_WAVE_WAVE_STEEPNESS);
				mAverageWavePeriod = json
						.getString(JSON_TAG_SPECTRAL_WAVE_AVERAGE_WAVE_PERIOD);
				mDominantWaveDirection = json
						.getString(JSON_TAG_SPECTRAL_WAVE_DOMINANT_WAVE_DIRECTION);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public Date getDataTime() {
			return mDataTime;
		}

		public Double getWaveHeight() {
			if (mWaveHeight.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWaveHeight);
		}

		public void setWaveHeight(Double waveHeight) {
			mWaveHeight = String.valueOf(waveHeight);
		}

		public Double getSwellHeight() {
			if (mSwellHeight.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mSwellHeight);
		}

		public void setSwellHeight(Double swellHeight) {
			mSwellHeight = String.valueOf(swellHeight);
		}

		public Double getSwellPeriod() {
			if (mSwellPeriod.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mSwellPeriod);
		}

		public void setSwellPeriod(Double swellPeriod) {
			mSwellPeriod = String.valueOf(swellPeriod);
		}

		public Double getWindWaveHeight() {
			if (mWindWaveHeight.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindWaveHeight);
		}

		public void setWindWaveHeight(Double windWaveHeight) {
			mWindWaveHeight = String.valueOf(windWaveHeight);
		}

		public Double getWindWavePeriod() {
			if (mWindWavePeriod.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mWindWavePeriod);
		}

		public void setWindWavePeriod(Double windWavePeriod) {
			mWindWavePeriod = String.valueOf(windWavePeriod);
		}

		public String getSwellDirection() {
			if (mSwellDirection.equals(NAString)) {
				return null;
			}

			return mSwellDirection;
		}

		public void setSwellDirection(String swellDirection) {
			mSwellDirection = swellDirection;
		}

		public String getWindWaveDirection() {
			if (mWindWaveDirection.equals(NAString)) {
				return null;
			}

			return mWindWaveDirection;
		}

		public void setWindWaveDirection(String windWaveDirection) {
			mWindWaveDirection = windWaveDirection;
		}

		public String getWaveSteepness() {
			if (mWaveSteepness.equals(NAString)) {
				return null;
			}
			return mWaveSteepness;
		}

		public void setWaveSteepness(String waveSteepness) {
			mWaveSteepness = waveSteepness;
		}

		public Double getAverageWavePeriod() {
			if (mAverageWavePeriod.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mAverageWavePeriod);
		}

		public void setAverageWavePeriod(Double averageWavePeriod) {
			mAverageWavePeriod = String.valueOf(averageWavePeriod);
		}

		public Double getDominantWaveDirection() {
			if (mDominantWaveDirection.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mDominantWaveDirection);
		}

		public void setDominantWaveDirection(Double dominantWaveDirection) {
			mDominantWaveDirection = String.valueOf(dominantWaveDirection);
		}
	}

	public class NDBCOceanicData {

		private static final String JSON_TAG_OCEANIC_DATA_TIME = "DATA_TIME";
		private static final String JSON_TAG_OCEANIC_DEPTH_MEASUREMENT = "DEPTH_MEASUREMENT";
		private static final String JSON_TAG_OCEANIC_OCEAN_TEMPERATURE = "OCEAN_TEMPERATURE";
		private static final String JSON_TAG_OCEANIC_CONDUCTIVITY = "CONDUCTIVITY";
		private static final String JSON_TAG_OCEANIC_SALINITY = "SALINITY";
		private static final String JSON_TAG_OCEANIC_OXYGEN_CONCENTRATION_PERCENT = "OXYGEN_CONCENTRATION_PERCENT";
		private static final String JSON_TAG_OCEANIC_OXYGEN_CONCENTRATION_PARTS_MILLION = "OXYGEN_CONCENTRATION_PARTS_MILLION";
		private static final String JSON_TAG_OCEANIC_CHLOROPHYLL_CONCENTRATION = "CHLOROPHYLL_CONCENTRATION";
		private static final String JSON_TAG_OCEANIC_TURBIDITY = "TURBIDITY";
		private static final String JSON_TAG_OCEANIC_PH = "PH";
		private static final String JSON_TAG_OCEANIC_EH = "EH";

		private String mDataTimeStr;
        private Date mDataTime;
		private String mDepthMeasurement;
		private String mOceanTemperature;
		private String mConductivity;
		private String mSalinity;
		private String mOxygenConcentrationPercent;
		private String mOxygenConcentrationPartsMillion;
		private String mChlorophyllConcentration;
		private String mTurbidity;
		private String mPh;
		private String mEh;

		public NDBCOceanicData(JSONObject json) {

            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			try {
                if (json.has(JSON_TAG_OCEANIC_DATA_TIME)) {
                    mDataTimeStr = json.getString(JSON_TAG_OCEANIC_DATA_TIME);
                    try {
                        mDataTime = dateFormat.parse(mDataTimeStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (json.has(JSON_TAG_OCEANIC_DEPTH_MEASUREMENT)) {
                    mDepthMeasurement = json.getString(JSON_TAG_OCEANIC_DEPTH_MEASUREMENT);
                }

                if (json.has(JSON_TAG_OCEANIC_OCEAN_TEMPERATURE)) {
                    mOceanTemperature = json.getString(JSON_TAG_OCEANIC_OCEAN_TEMPERATURE);
                }

                if (json.has(JSON_TAG_OCEANIC_CONDUCTIVITY)) {
                    mConductivity = json.getString(JSON_TAG_OCEANIC_CONDUCTIVITY);
                }

                if (json.has(JSON_TAG_OCEANIC_SALINITY)) {
                    mSalinity = json.getString(JSON_TAG_OCEANIC_SALINITY);
                }

                if (json.has(JSON_TAG_OCEANIC_OXYGEN_CONCENTRATION_PERCENT)) {
                    mOxygenConcentrationPercent = json.getString(JSON_TAG_OCEANIC_OXYGEN_CONCENTRATION_PERCENT);
                }

                if (json.has(JSON_TAG_OCEANIC_OXYGEN_CONCENTRATION_PARTS_MILLION)) {
                    mOxygenConcentrationPartsMillion = json.getString(JSON_TAG_OCEANIC_OXYGEN_CONCENTRATION_PARTS_MILLION);
                }

                if (json.has(JSON_TAG_OCEANIC_CHLOROPHYLL_CONCENTRATION)) {
                    mChlorophyllConcentration = json.getString(JSON_TAG_OCEANIC_CHLOROPHYLL_CONCENTRATION);
                }

                if (json.has(JSON_TAG_OCEANIC_TURBIDITY)) {
                    mTurbidity = json.getString(JSON_TAG_OCEANIC_TURBIDITY);
                }

                if (json.has(JSON_TAG_OCEANIC_PH)) {
                    mPh = json.getString(JSON_TAG_OCEANIC_PH);
                }

                if (json.has(JSON_TAG_OCEANIC_EH)) {
                    mEh = json.getString(JSON_TAG_OCEANIC_EH);
                }
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public Date getDataTime() {
			return mDataTime;
		}

		public Double getDepthMeasurement() {
			if (mDepthMeasurement.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mDepthMeasurement);
		}

		public void setDepthMeasurement(Double depthMeasurement) {
			mDepthMeasurement = String.valueOf(depthMeasurement);
		}

		public Double getOceanTemperature() {
			if (mOceanTemperature.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mOceanTemperature);
		}

		public void setOceanTemperature(Double oceanTemperature) {
			mOceanTemperature = String.valueOf(oceanTemperature);
		}

		public Double getConductivity() {
			if (mConductivity.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mConductivity);
		}

		public void setConductivity(Double conductivity) {
			mConductivity = String.valueOf(conductivity);
		}

		public Double getSalinity() {
			if (mSalinity.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mSalinity);
		}

		public void setSalinity(Double salinity) {
			mSalinity = String.valueOf(salinity);
		}

		public Double getOxygenConcentrationPercent() {
			if (mOxygenConcentrationPercent.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mOxygenConcentrationPercent);
		}

		public void setOxygenConcentrationPercent(
				Double oxygenConcentrationPercent) {
			mOxygenConcentrationPercent = String
					.valueOf(oxygenConcentrationPercent);
		}

		public Double getOxygenConcentrationPartsMillion() {
			if (mOxygenConcentrationPartsMillion.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mOxygenConcentrationPartsMillion);
		}

		public void setOxygenConcentrationPartsMillion(
				Double oxygenConcentrationPartsMillion) {
			mOxygenConcentrationPartsMillion = String
					.valueOf(oxygenConcentrationPartsMillion);
		}

		public Double getChlorophyllConcentration() {
			if (mChlorophyllConcentration.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mChlorophyllConcentration);
		}

		public void setChlorophyllConcentration(Double chlorophyllConcentration) {
			mChlorophyllConcentration = String
					.valueOf(chlorophyllConcentration);
		}

		public Double getTurbidity() {
			if (mTurbidity.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mTurbidity);
		}

		public void setTurbidity(Double turbidity) {
			mTurbidity = String.valueOf(turbidity);
		}

		public Double getPh() {
			if (mPh.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mPh);
		}

		public void setPh(Double ph) {
			mPh = String.valueOf(ph);
		}

		public Double getEh() {
			if (mEh.equals(NAString)) {
				return null;
			}

			return Double.valueOf(mEh);
		}

		public void setEh(Double eh) {
			mEh = String.valueOf(eh);
		}
	}

	private static final String NAString = "N/A";

	private static final String JSON_TAG_STATION_ID = "STATION_ID";
	private static final String JSON_TAG_STATION_NAME = "NAME";
	private static final String JSON_TAG_STATION_LATITUDE = "LATITUDE";
	private static final String JSON_TAG_STATION_LONGITUDE = "LONGITUDE";
	private static final String JSON_TAG_STATION_LAST_UPDATE = "LAST_UPDATE";

	public static final String TAG_MIN_LAST_UPDATE_TIMESTAMP = "MIN_LAST_UPDATE_TIMESTAMP";
	
	private long mStationId;
	private String mStationName;
	private double mLatitude;
	private double mLongitude;
	private Date mLastOnlineUpdate;
	private Date mLastUserUpdate;

	private ArrayList<NDBCMeteorologicalData> mMeteorologicalData;
	private ArrayList<NDBCDriftingBuoyData> mDriftingBuoyData;
	private ArrayList<NDBCSpectralWaveData> mSpectralWaveData;
	private ArrayList<NDBCOceanicData> mOceanicData;

	public NDBCStation() {
		mStationId = -1;
		mStationName = "";
		mLatitude = 0;
		mLongitude = 0;
		mLastOnlineUpdate = new Date(0);
		mLastUserUpdate = new Date(0);

		mMeteorologicalData = new ArrayList<NDBCMeteorologicalData>();
		mDriftingBuoyData = new ArrayList<NDBCDriftingBuoyData>();
		mSpectralWaveData = new ArrayList<NDBCSpectralWaveData>();
		mOceanicData = new ArrayList<NDBCOceanicData>();
	}

	public NDBCStation(JSONObject json) {

        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			mStationId = json.getLong(JSON_TAG_STATION_ID);
			mStationName = json.getString(JSON_TAG_STATION_NAME);
			mLatitude = json.getDouble(JSON_TAG_STATION_LATITUDE);
			mLongitude = json.getDouble(JSON_TAG_STATION_LONGITUDE);

			mLastOnlineUpdate = new Date();

			try {
				mLastOnlineUpdate = dateFormat.parse(json.getString(JSON_TAG_STATION_LAST_UPDATE));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			mLastUserUpdate = new Date(0);

			mMeteorologicalData = new ArrayList<NDBCMeteorologicalData>();
			mDriftingBuoyData = new ArrayList<NDBCDriftingBuoyData>();
			mSpectralWaveData = new ArrayList<NDBCSpectralWaveData>();
			mOceanicData = new ArrayList<NDBCOceanicData>();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long getStationId() {
		return mStationId;
	}

	public void setStationId(long stationId) {
		mStationId = stationId;
	}

	public String getStationName() {
		return mStationName;
	}

	public void setStationName(String stationName) {
		mStationName = stationName;
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

	public Date getLastOnlineUpdate() {
		return mLastOnlineUpdate;
	}

	public void setLastOnlineUpdate(Date lastUpdate) {
		mLastOnlineUpdate = lastUpdate;
	}

	public Date getLastUserUpdate() {
		return mLastUserUpdate;
	}

	public void setLastUserUpdate(Date lastUpdate) {
		mLastUserUpdate = lastUpdate;
	}

	public int getMeteorologicalDataCount() {
		return mMeteorologicalData.size();
	}

	public void addMeteorologicalData(NDBCMeteorologicalData data) {
		mMeteorologicalData.add(data);
	}

	public void addMeteorologicalData(JSONObject jsonData) {
		mMeteorologicalData.add(new NDBCMeteorologicalData(jsonData));
	}

	public NDBCMeteorologicalData getMeteorologicalData(int index) {
		return mMeteorologicalData.get(index);
	}

	public NDBCMeteorologicalData getLatestMeteorologicalData() {
		NDBCMeteorologicalData latestData = null;
		Date latestTimestamp = new Date(0);
		for (int i = 0; i < getMeteorologicalDataCount(); i++) {
			if (getMeteorologicalData(i).getDataTime().after(latestTimestamp)) {
				latestData = getMeteorologicalData(i);
				latestTimestamp = getMeteorologicalData(i).getDataTime();
			}
		}
		return latestData;
	}

	public void clearMeteorologicalData() {
		mMeteorologicalData.clear();
	}

	public void copyMeteorologicalData(NDBCStation ndbcStation) {
		mMeteorologicalData.addAll(ndbcStation.mMeteorologicalData);
	}

	public int getDriftingBuoyDataCount() {
		return mDriftingBuoyData.size();
	}

	public void addDriftingBuoyData(NDBCDriftingBuoyData data) {
		mDriftingBuoyData.add(data);
	}

	public void addDriftingBuoyData(JSONObject jsonData) {
		mDriftingBuoyData.add(new NDBCDriftingBuoyData(jsonData));
	}

	public NDBCDriftingBuoyData getDriftingBuoyData(int index) {
		return mDriftingBuoyData.get(index);
	}

	public NDBCDriftingBuoyData getLatestDriftingBuoyData() {
		NDBCDriftingBuoyData latestData = null;
		Date latestTimestamp = new Date(0);
		for (int i = 0; i < getDriftingBuoyDataCount(); i++) {
			if (getDriftingBuoyData(i).getDataTime().after(latestTimestamp)) {
				latestData = getDriftingBuoyData(i);
				latestTimestamp = getDriftingBuoyData(i).getDataTime();
			}
		}
		return latestData;
	}

	public void clearDriftingBuoyData() {
		mDriftingBuoyData.clear();
	}

	public void copyDriftingBuoyData(NDBCStation ndbcStation) {
		mDriftingBuoyData.addAll(ndbcStation.mDriftingBuoyData);
	}

	public int getSpectralWaveDataCount() {
		return mSpectralWaveData.size();
	}

	public void addSpectralWaveData(NDBCSpectralWaveData data) {
		mSpectralWaveData.add(data);
	}

	public void addSpectralWaveData(JSONObject jsonData) {
		mSpectralWaveData.add(new NDBCSpectralWaveData(jsonData));
	}

	public NDBCSpectralWaveData getSpectralWaveData(int index) {
		return mSpectralWaveData.get(index);
	}

	public NDBCSpectralWaveData getLatestSpectralWaveData() {
		NDBCSpectralWaveData latestData = null;
		Date latestTimestamp = new Date(0);
		for (int i = 0; i < getSpectralWaveDataCount(); i++) {
			if (getSpectralWaveData(i).getDataTime().after(latestTimestamp)) {
				latestData = getSpectralWaveData(i);
				latestTimestamp = getSpectralWaveData(i).getDataTime();
			}
		}
		return latestData;
	}

	public void clearSpectralWaveData() {
		mSpectralWaveData.clear();
	}

	public void copySpectralWaveData(NDBCStation ndbcStation) {
		mSpectralWaveData.addAll(ndbcStation.mSpectralWaveData);
	}

	public int getOceanicDataCount() {
		return mOceanicData.size();
	}

	public void addOceanicData(NDBCOceanicData data) {
		mOceanicData.add(data);
	}

	public void addOceanicData(JSONObject jsonData) {
		mOceanicData.add(new NDBCOceanicData(jsonData));
	}

	public NDBCOceanicData getOceanicData(int index) {
		return mOceanicData.get(index);
	}

	public NDBCOceanicData getLatestOceanicData() {
		NDBCOceanicData latestData = null;
		Date latestTimestamp = new Date(0);
		for (int i = 0; i < getOceanicDataCount(); i++) {
			if (getOceanicData(i).getDataTime().after(latestTimestamp)) {
				latestData = getOceanicData(i);
				latestTimestamp = getOceanicData(i).getDataTime();
			}
		}
		return latestData;
	}

	public void clearOceanicData() {
		mOceanicData.clear();
	}

	public void copyOceanicData(NDBCStation ndbcStation) {
		mOceanicData.addAll(ndbcStation.mOceanicData);
	}
}
