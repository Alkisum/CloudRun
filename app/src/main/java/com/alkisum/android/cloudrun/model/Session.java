package com.alkisum.android.cloudrun.model;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Insertable;
import com.alkisum.android.cloudrun.interfaces.Jsonable;
import com.alkisum.android.cloudrun.interfaces.Restorable;
import com.alkisum.android.cloudrun.utils.Format;
import com.alkisum.android.cloudrun.utils.Sessions;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

@Entity
public class Session implements Jsonable, Insertable, Deletable, Restorable {

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private long start;

    private Long end;

    private Long duration;

    private Float distance;

    @Transient
    private boolean selected;

    @ToMany(referencedJoinProperty = "sessionId")
    private List<DataPoint> dataPoints;

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1243220280)
    public synchronized void resetDataPoints() {
        dataPoints = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1358677256)
    public List<DataPoint> getDataPoints() {
        if (dataPoints == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DataPointDao targetDao = daoSession.getDataPointDao();
            List<DataPoint> dataPointsNew = targetDao._querySession_DataPoints(id);
            synchronized (this) {
                if (dataPoints == null) {
                    dataPoints = dataPointsNew;
                }
            }
        }
        return dataPoints;
    }

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1616835709)
    private transient SessionDao myDao;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    public Float getDistance() {
        return this.distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Long getEnd() {
        return this.end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Long getDuration() {
        return this.duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    @Generated(hash = 1103544323)
    public Session(Long id, long start, Long end, Long duration, Float distance) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.distance = distance;
    }

    @Generated(hash = 1317889643)
    public Session() {
    }

    @Override
    public JSONObject buildJson() throws JSONException {
        return Sessions.buildJson(this);
    }

    @Override
    public String buildJsonFileName() {
        return Sessions.Json.FILE_PREFIX + Format.getDateTimeJson().format(
                new Date(this.getStart())) + JsonFile.FILE_EXT;
    }

    @Override
    public String getFileNameRegex() {
        return Sessions.Json.FILE_REGEX;
    }

    @Override
    public void insertFromJson(JSONObject jsonObject) throws JSONException {
        if (!jsonObject.has(Sessions.Json.SESSION)) {
            // JSON file does not contain any session object
            return;
        }
        int version = jsonObject.getInt(Sessions.Json.VERSION);
        switch (version) {
            case 1:
                Sessions.buildFromJsonVersion1(jsonObject);
                break;
            case 2:
                Sessions.buildFromJsonVersion2(jsonObject);
                break;
            default:
                break;
        }
    }

    @Override
    public Deletable[] deleteEntities(Deletable... deletables) {
        // get DAO
        DataPointDao dataPointDao = Db.getInstance().getDaoSession()
                .getDataPointDao();

        // delete selected sessions and datapoints
        for (Deletable deletable : deletables) {
            dataPointDao.deleteInTx(((Session) deletable).getDataPoints());
            ((Session) deletable).delete();
        }
        return deletables;
    }

    @Override
    public void restore(final Restorable[] restorables) {
        // get DAOs
        DaoSession daoSession = Db.getInstance().getDaoSession();
        SessionDao sessionDao = daoSession.getSessionDao();
        DataPointDao dataPointDao = daoSession.getDataPointDao();

        // restore sessions and datapoints
        for (Restorable restorable : restorables) {
            sessionDao.insert((Session) restorable);
            dataPointDao.insertInTx(((Session) restorable).getDataPoints());
        }
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1458438772)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSessionDao() : null;
    }
}
