package com.alkisum.android.cloudrun.model;

import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Restorable;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;

@Entity
public class Route implements Deletable, Restorable {

    @Id(autoincrement = true)
    private Long id;

    private String name;

    @Transient
    private boolean selected;

    @ToMany(referencedJoinProperty = "routeId")
    private List<Marker> markers;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1511175683)
    private transient RouteDao myDao;

    @Generated(hash = 1916522784)
    public Route(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Generated(hash = 467763370)
    public Route() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1325356420)
    public List<Marker> getMarkers() {
        if (markers == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MarkerDao targetDao = daoSession.getMarkerDao();
            List<Marker> markersNew = targetDao._queryRoute_Markers(id);
            synchronized (this) {
                if (markers == null) {
                    markers = markersNew;
                }
            }
        }
        return markers;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 2076886870)
    public synchronized void resetMarkers() {
        markers = null;
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

    @Override
    public Deletable[] deleteEntities(Deletable... deletables) {
        // get DAO
        MarkerDao markerDao = Db.getInstance().getDaoSession().getMarkerDao();

        // delete selected routes and markers
        for (Deletable deletable : deletables) {
            markerDao.deleteInTx(((Route)deletable).getMarkers());
            ((Route)deletable).delete();
        }
        return deletables;
    }

    @Override
    public void restore(Restorable[] restorables) {
        // get DAOs
        DaoSession daoSession = Db.getInstance().getDaoSession();
        RouteDao routeDao = daoSession.getRouteDao();
        MarkerDao markerDao = daoSession.getMarkerDao();

        // restore routes and markers
        for (Restorable restorable : restorables) {
            routeDao.insert((Route) restorable);
            markerDao.insertInTx(((Route) restorable).getMarkers());
        }
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1333897230)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getRouteDao() : null;
    }
}
