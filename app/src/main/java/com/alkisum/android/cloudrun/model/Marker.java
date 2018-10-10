package com.alkisum.android.cloudrun.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class Marker {

    @Id(autoincrement = true)
    private Long id;

    private String label;

    private Double latitude;

    private Double longitude;

    @NotNull
    private long routeId;

    @ToOne(joinProperty = "routeId")
    private Route route;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1808419515)
    private transient MarkerDao myDao;

    @Generated(hash = 2015615689)
    public Marker(Long id, String label, Double latitude, Double longitude,
            long routeId) {
        this.id = id;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
        this.routeId = routeId;
    }

    @Generated(hash = 800806583)
    public Marker() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getRouteId() {
        return this.routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    @Generated(hash = 1922987921)
    private transient Long route__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 939804297)
    public Route getRoute() {
        long __key = this.routeId;
        if (route__resolvedKey == null || !route__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            RouteDao targetDao = daoSession.getRouteDao();
            Route routeNew = targetDao.load(__key);
            synchronized (this) {
                route = routeNew;
                route__resolvedKey = __key;
            }
        }
        return route;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1500642670)
    public void setRoute(@NotNull Route route) {
        if (route == null) {
            throw new DaoException(
                    "To-one property 'routeId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.route = route;
            routeId = route.getId();
            route__resolvedKey = routeId;
        }
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 146372940)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMarkerDao() : null;
    }
}
