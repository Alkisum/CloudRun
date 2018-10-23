package com.alkisum.android.cloudrun.location;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class defining coordinates (time, latitude, longitude and elevation).
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public class Coordinate implements Parcelable {

    /**
     * Time.
     */
    private final long time;

    /**
     * Latitude.
     */
    private final double latitude;

    /**
     * Longitude.
     */
    private final double longitude;

    /**
     * Elevation.
     */
    private final double elevation;

    /**
     * Coordinate constructor.
     *
     * @param time      Time
     * @param latitude  Latitude
     * @param longitude Longitude
     * @param elevation Elevation
     */
    public Coordinate(final long time, final double latitude,
                      final double longitude, final double elevation) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    /**
     * @return Time
     */
    public final long getTime() {
        return time;
    }

    /**
     * @return Latitude
     */
    public final double getLatitude() {
        return latitude;
    }

    /**
     * @return Longitude
     */
    public final double getLongitude() {
        return longitude;
    }

    /**
     * @return Elevation
     */
    public final double getElevation() {
        return elevation;
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
                latitude,
                longitude,
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
        time = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        elevation = in.readDouble();
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(time);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(elevation);
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
