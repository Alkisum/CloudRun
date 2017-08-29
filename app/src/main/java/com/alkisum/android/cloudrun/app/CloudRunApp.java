package com.alkisum.android.cloudrun.app;

import android.app.Application;

import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.utils.Pref;
import com.squareup.leakcanary.LeakCanary;

/**
 * Application class.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.0
 */
public class CloudRunApp extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        Db.getInstance().init(this);

        Pref.init(this);
    }
}
