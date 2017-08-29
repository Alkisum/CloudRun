package com.alkisum.android.cloudrun.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class DataPoint {

    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private long time;

    private Double latitude;

    private Double longitude;

    private Double elevation;

    @NotNull
    private long sessionId;

    @ToOne(joinProperty = "sessionId")
    private Session session;

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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 478840882)
    public void setSession(@NotNull Session session) {
        if (session == null) {
            throw new DaoException(
                    "To-one property 'sessionId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.session = session;
            sessionId = session.getId();
            session__resolvedKey = sessionId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 624529852)
    public Session getSession() {
        long __key = this.sessionId;
        if (session__resolvedKey == null || !session__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            SessionDao targetDao = daoSession.getSessionDao();
            Session sessionNew = targetDao.load(__key);
            synchronized (this) {
                session = sessionNew;
                session__resolvedKey = __key;
            }
        }
        return session;
    }

    @Generated(hash = 274049648)
    private transient Long session__resolvedKey;

    /** Used for active entity operations. */
    @Generated(hash = 116337198)
    private transient DataPointDao myDao;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Double getElevation() {
        return this.elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1846517398)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDataPointDao() : null;
    }

    @Generated(hash = 519946263)
    public DataPoint(Long id, long time, Double latitude, Double longitude,
            Double elevation, long sessionId) {
        this.id = id;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.sessionId = sessionId;
    }

    @Generated(hash = 22358778)
    public DataPoint() {
    }
}
