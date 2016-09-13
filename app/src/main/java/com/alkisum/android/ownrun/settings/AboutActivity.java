package com.alkisum.android.ownrun.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.alkisum.android.ownrun.BuildConfig;
import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.utils.Format;

import java.util.Date;

import butterknife.ButterKnife;

/**
 * Activity listing information about the application.
 *
 * @author Alkisum
 * @version 1.2
 * @since 1.2
 */
public class AboutActivity extends AppCompatActivity {

    /**
     * Github address.
     */
    public static final String GITHUB_ADDRESS =
            "https://github.com/Alkisum/ownRun/";

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setGui();
    }

    /**
     * Set GUI.
     */
    private void setGui() {

        Toolbar toolbar = ButterKnife.findById(this, R.id.about_toolbar);
        if (toolbar != null) {
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
        }

        ListView listView = ButterKnife.findById(this, R.id.about_list);
        String[][] info = new String[][]{
                new String[]{"Build version", BuildConfig.VERSION_NAME},
                new String[]{"Build date", Format.DATE_BUILD.format(
                        new Date(BuildConfig.TIMESTAMP))},
                new String[]{"Github", GITHUB_ADDRESS}
        };
        listView.setAdapter(new AboutListAdapter(this, info));
    }
}
