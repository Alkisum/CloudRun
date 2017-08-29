package com.alkisum.android.cloudrun.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.alkisum.android.cloudops.utils.CloudPref;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.location.LocationHandler;
import com.alkisum.android.cloudrun.ui.NumberPickerPreference;
import com.alkisum.android.cloudrun.utils.Pref;

/**
 * Activity showing the application settings.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.1
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setTitle(getString(R.string.settings_title));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        onBackPressed();
                    }
                });

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

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            // Distance count
            npDistanceCnt = (NumberPickerPreference) findPreference(
                    Pref.DISTANCE_CNT);
            npDistanceCnt.setSummary(npDistanceCnt.getValue()
                    + getString(R.string.distance_cnt_summary));

            // About
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
                    int value = sharedPreferences.getInt(Pref.DISTANCE_CNT,
                            LocationHandler.DISTANCE_CNT_DEFAULT);
                    npDistanceCnt.setSummary(value + getString(
                            R.string.distance_cnt_summary));
                    break;
                case CloudPref.SAVE_CLOUD_INFO:
                    if (!sharedPreferences.getBoolean(
                            CloudPref.SAVE_CLOUD_INFO, false)) {
                        discardCloudInfo(sharedPreferences);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Discard cloud connection information. Called when the user turn
         * off the save cloud info settings.
         *
         * @param sharedPref Shared Preferences
         */
        private void discardCloudInfo(final SharedPreferences sharedPref) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(CloudPref.ADDRESS, "");
            editor.putString(CloudPref.PATH, "");
            editor.putString(CloudPref.USERNAME, "");
            editor.apply();
        }
    }
}
