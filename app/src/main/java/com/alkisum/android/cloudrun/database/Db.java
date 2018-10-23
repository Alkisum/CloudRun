package com.alkisum.android.cloudrun.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alkisum.android.cloudrun.model.DaoMaster;
import com.alkisum.android.cloudrun.model.DaoSession;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.model.SessionDao;

import org.greenrobot.greendao.database.Database;

import java.util.List;

/**
 * Singleton class handling database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public final class Db {

    /**
     * Log tag.
     */
    private static final String TAG = "Db";

    /**
     * Database name.
     */
    private static final String NAME = "CloudRun.db";

    /**
     * DaoSession instance.
     */
    private DaoSession daoSession;

    /**
     * Database instance.
     */
    private static Db instance = null;

    /**
     * Db constructor.
     */
    private Db() {

    }

    /**
     * @return Database instance
     */
    public static Db getInstance() {
        if (instance == null) {
            instance = new Db();
        }
        return instance;
    }

    /**
     * Initialize database.
     *
     * @param context Context
     */
    public void init(final Context context) {
        DaoMaster.OpenHelper helper = new DbOpenHelper(
                context.getApplicationContext(), NAME, null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    /**
     * @return DaoSession instance
     */
    public DaoSession getDaoSession() {
        return daoSession;
    }

    /**
     * Class extending SQLiteOpenHelper, used for upgrading database from one
     * version to another.
     */
    private class DbOpenHelper extends DaoMaster.OpenHelper {

        /**
         * DbOpenHelper constructor.
         *
         * @param context Context
         * @param name    Database name
         * @param factory Cursor factory
         */
        DbOpenHelper(final Context context, final String name,
                     final SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(final Database db, final int oldVersion,
                              final int newVersion) {
            Log.i(TAG, "Upgrade database from " + oldVersion
                    + " to " + newVersion);
            if (oldVersion < 2) {
                // Update duration
                Log.i(TAG, "Add column DURATION in table SESSION");
                db.execSQL("ALTER TABLE " + SessionDao.TABLENAME
                        + " ADD COLUMN 'DURATION' INTEGER;");
                Log.i(TAG, "Set duration for old sessions");
                SessionDao sessionDao = new DaoMaster(db).newSession()
                        .getSessionDao();
                List<Session> sessions = sessionDao.loadAll();
                for (Session session : sessions) {
                    session.setDuration(session.getEnd() - session.getStart());
                }
                sessionDao.updateInTx(sessions);
            }
        }
    }
}
