package com.alkisum.android.ownrun.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.alkisum.android.ownrun.model.DaoMaster;
import com.alkisum.android.ownrun.model.DaoSession;

/**
 * Singleton class handling database.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public final class Db {

    /**
     * Database name.
     */
    private static final String NAME = "ownRun.db";

    /**
     * DaoSession instance.
     */
    private DaoSession mDaoSession;

    /**
     * Database instance.
     */
    private static Db mInstance = null;

    /**
     * Db constructor.
     */
    private Db() {

    }

    /**
     * @return Database instance
     */
    public static Db getInstance() {
        if (mInstance == null) {
            mInstance = new Db();
        }
        return mInstance;
    }

    /**
     * Initialize database.
     *
     * @param context Context
     * @return Database instance
     */
    public Db init(final Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(
                context.getApplicationContext(), NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
        return this;
    }

    /**
     * @return DaoSession instance
     */
    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
