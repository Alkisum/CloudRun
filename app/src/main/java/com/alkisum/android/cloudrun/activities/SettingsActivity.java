package com.alkisum.android.cloudrun.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.alkisum.android.cloudlib.utils.CloudPref;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.location.LocationHelper;
import com.alkisum.android.cloudrun.ui.NumberPickerPreference;
import com.alkisum.android.cloudrun.utils.Markers;
import com.alkisum.android.cloudrun.utils.Pref;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Activity showing the application settings.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.1
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction().replace(
                R.id.settings_frame_content, new SettingsFragment()).commit();
    }

    /**
     * SettingsFragment extending PreferenceFragment.
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        /**
         * NumberPicker for the distance count.
         */
        private NumberPickerPreference npDistanceCnt;

        /**
         * EditTextPreference for the distance to marker.
         */
        private EditTextPreference etpDistanceToMarker;

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            // distance count
            npDistanceCnt = (NumberPickerPreference) findPreference(
                    Pref.DISTANCE_CNT);
            npDistanceCnt.setSummary(npDistanceCnt.getValue()
                    + getString(R.string.distance_cnt_summary));

            // distance to marker
            etpDistanceToMarker = (EditTextPreference) findPreference(
                    Pref.DISTANCE_TO_MARKER);
            etpDistanceToMarker.setSummary(etpDistanceToMarker.getText()
                    + getString(R.string.distance_to_marker_summary));

            // about
            Preference aboutPreference = findPreference(Pref.ABOUT);
            aboutPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            startActivity(new Intent(getActivity(),
                                    AboutActivity.class));
                            return false;
                        }
                    });
        }

        @Override
        public final void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public final void onSharedPreferenceChanged(
                final SharedPreferences sharedPreferences, final String key) {
            switch (key) {
                case Pref.DISTANCE_CNT:
                    int distanceCnt = sharedPreferences.getInt(
                            Pref.DISTANCE_CNT,
                            LocationHelper.DISTANCE_CNT_DEFAULT);
                    npDistanceCnt.setSummary(distanceCnt + getString(
                            R.string.distance_cnt_summary));
                    break;
                case Pref.DISTANCE_TO_MARKER:
                    String distanceToMarker = sharedPreferences.getString(
                            Pref.DISTANCE_TO_MARKER,
                            Markers.DISTANCE_TO_MARKER_DEFAULT);
                    etpDistanceToMarker.setSummary(distanceToMarker
                            + getString(R.string.distance_to_marker_summary));
                    break;
                case CloudPref.SAVE_ADDRESS:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_ADDRESS,
                            CloudPref.DEFAULT_SAVE_ADDRESS)) {
                        discardCloudInfo(CloudPref.ADDRESS, sharedPreferences);
                    }
                    break;
                case CloudPref.SAVE_PATH:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_PATH,
                            CloudPref.DEFAULT_SAVE_PATH)) {
                        discardCloudInfo(CloudPref.PATH, sharedPreferences);
                    }
                    break;
                case CloudPref.SAVE_USERNAME:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_USERNAME,
                            CloudPref.DEFAULT_SAVE_USERNAME)) {
                        discardCloudInfo(CloudPref.USERNAME, sharedPreferences);
                    }
                    break;
                case CloudPref.SAVE_PASSWORD:
                    if (!sharedPreferences.getBoolean(CloudPref.SAVE_PASSWORD,
                            CloudPref.DEFAULT_SAVE_PASSWORD)) {
                        discardCloudInfo(CloudPref.PASSWORD, sharedPreferences);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Discard cloud connection information. Called when the user turn
         * off one of the save info settings.
         *
         * @param key        Key identifying the value to discard
         * @param sharedPref Shared Preferences
         */
        private void discardCloudInfo(final String key,
                                      final SharedPreferences sharedPref) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, "");
            editor.apply();
        }
    }
}
