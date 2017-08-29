package com.alkisum.android.cloudrun.location;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class defining coordinates (latitude, longitude and elevation).
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.0
 */
public class Coordinate implements Parcelable {

    /**
     * Latitude.
     */
    private final double mLatitude;

    /**
     * Longitude.
     */
    private final double mLongitude;

    /**
     * Elevation.
     */
    private final double mElevation;

    /**
     * Coordinate constructor.
     *
     * @param latitude  Latitude
     * @param longitude Longitude
     * @param elevation Elevation
     */
    Coordinate(final double latitude, final double longitude,
               final double elevation) {
        mLatitude = latitude;
        mLongitude = longitude;
        mElevation = elevation;
    }

    /**
     * @return Latitude
     */
    public final double getLatitude() {
        return mLatitude;
    }

    /**
     * @return Longitude
     */
    public final double getLongitude() {
        return mLongitude;
    }

    /**
     * @return Elevation
     */
    public final double getElevation() {
        return mElevation;
    }

    /**
     * Calculate the distance between to locations.
     *
     * @param dest Destination
     * @return Distance between the current location and the given destination
     */
    final float distanceTo(final Coordinate dest) {
        float[] results = new float[2];
        android.location.Location.distanceBetween(
                mLatitude,
                mLongitude,
                dest.getLatitude(),
                dest.getLongitude(),
                results);
        return results[0];
    }

    /**
     * Coordinate constructor.
     *
     * @param in Parcel to read
     */
    private Coordinate(final Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mElevation = in.readDouble();
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeDouble(mElevation);
    }

    /**
     * Parcelable creator.
     */
    public static final Parcelable.Creator<Coordinate> CREATOR
            = new Parcelable.Creator<Coordinate>() {
        @Override
        public Coordinate createFromParcel(final Parcel in) {
            return new Coordinate(in);
        }

        @Override
        public Coordinate[] newArray(final int size) {
            return new Coordinate[size];
        }
    };
}
