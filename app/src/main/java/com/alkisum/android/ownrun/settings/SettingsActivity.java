package com.alkisum.android.ownrun.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.utils.Pref;

import butterknife.ButterKnife;

/**
 * Activity showing the application settings.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.1
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.settings_toolbar);
        if (toolbar != null) {
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
        }

        getFragmentManager().beginTransaction().replace(
                R.id.settings_frame_content, new SettingsFragment()).commit();
    }

    /**
     * SettingsFragment extending PreferenceFragment.
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
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
                case Pref.SAVE_OWNCLOUD_INFO:
                    if (!sharedPreferences.getBoolean(
                            Pref.SAVE_OWNCLOUD_INFO, false)) {
                        discardOwnCloudInfo(sharedPreferences);
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * Discard ownCloud connection information. Called when the user turn
         * off the save ownCloud info settings.
         *
         * @param sharedPref Shared Preferences
         */
        private void discardOwnCloudInfo(final SharedPreferences sharedPref) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Pref.ADDRESS, "");
            editor.putString(Pref.PATH, "");
            editor.putString(Pref.USERNAME, "");
            editor.apply();
        }
    }
}
