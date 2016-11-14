package com.alkisum.android.ownrun.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.alkisum.android.ownrun.BuildConfig;
import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.utils.Format;
import com.alkisum.android.ownrun.utils.Pref;

import java.util.Date;

import butterknife.ButterKnife;

/**
 * Activity listing information about the application.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.2
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);


        Toolbar toolbar = ButterKnife.findById(this, R.id.about_toolbar);
        toolbar.setTitle(getString(R.string.about_title));
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
                R.id.about_frame_content, new AboutFragment()).commit();
    }

    /**
     * AboutFragment extending PreferenceFragment.
     */
    public static class AboutFragment extends PreferenceFragment {

        @Override
        public final void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.about_preferences);

            // Build version
            Preference versionPreference = findPreference(Pref.BUILD_VERSION);
            versionPreference.setSummary(BuildConfig.VERSION_NAME);

            // Build date
            Preference datePreference = findPreference(Pref.BUILD_DATE);
            datePreference.setSummary(Format.DATE_BUILD.format(
                    new Date(BuildConfig.TIMESTAMP)));

            // Github
            Preference githubPreference = findPreference(Pref.GITHUB);
            githubPreference.setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(
                                final Preference preference) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(getString(
                                            R.string.about_github)));
                            startActivity(intent);
                            return false;
                        }
                    });
        }
    }
}
