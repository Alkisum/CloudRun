package com.alkisum.android.ownrun.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

/**
 * IntentService for location update from GoogleAPI.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class LocationUpdateService extends IntentService {

    /**
     * Name for Location information provided by the Google API.
     */
    private static final String GOOGLE_LOCATION =
            "com.google.android.location.LOCATION";

    /**
     * Name for the Service.
     */
    public static final String LOCATION_NOTIFICATION =
            "com.alkisum.android.ownrun.location.LocationUpdateService";

    /**
     * Name for the extra containing the location information broadcast.
     */
    public static final String LOCATION = "location";

    /**
     * LocationUpdateService constructor.
     */
    public LocationUpdateService() {
        super("LocationUpdateService");
    }


    @Override
    protected final void onHandleIntent(final Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }
        Location location = bundle.getParcelable(GOOGLE_LOCATION);
        if (location == null) {
            return;
        }
        Intent intentBroadcast = new Intent(LOCATION_NOTIFICATION);
        intentBroadcast.putExtra(LOCATION, location);
        sendBroadcast(intentBroadcast);
    }
}
