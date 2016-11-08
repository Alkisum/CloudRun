package com.alkisum.android.ownrun.app;

import android.app.Application;

import com.alkisum.android.ownrun.data.Db;
import com.squareup.leakcanary.LeakCanary;

/**
 * Application class.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
public class OwnRunApp extends Application {

    @Override
    public final void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        Db.getInstance().init(this);
    }
}
