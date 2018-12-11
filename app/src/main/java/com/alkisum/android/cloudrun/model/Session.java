package com.alkisum.android.cloudrun.model;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudrun.net.Jsonable;
import com.alkisum.android.cloudrun.utils.Format;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import static com.alkisum.android.cloudrun.model.Session.Json.DATAPOINTS;
import static com.alkisum.android.cloudrun.model.Session.Json.DATAPOINT_ELEVATION;
import static com.alkisum.android.cloudrun.model.Session.Json.DATAPOINT_LATITUDE;
import static com.alkisum.android.cloudrun.model.Session.Json.DATAPOINT_LONGITUDE;
import static com.alkisum.android.cloudrun.model.Session.Json.DATAPOINT_TIME;
import static com.alkisum.android.cloudrun.model.Session.Json.FILE_PREFIX;
import static com.alkisum.android.cloudrun.model.Session.Json.JSON_VERSION;
import static com.alkisum.android.cloudrun.model.Session.Json.SESSION;
import static com.alkisum.android.cloudrun.model.Session.Json.SESSION_DISTANCE;
import static com.alkisum.android.cloudrun.model.Session.Json.SESSION_DURATION;
import static com.alkisum.android.cloudrun.model.Session.Json.SESSION_END;
import static com.alkisum.android.cloudrun.model.Session.Json.SESSION_START;
import static com.alkisum.android.cloudrun.model.Session.Json.VERSION;

@Entity
public class Session implements Jsonable {

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

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
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
                if(dataPoints == null) {
                    dataPoints = dataPointsNew;
                }
            }
        }
        return dataPoints;
    }

    /** Used for active entity operations. */
    @Generated(hash = 1616835709)
    private transient SessionDao myDao;

    /** Used to resolve relations */
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
        JSONObject jsonSession = new JSONObject();
        jsonSession.put(SESSION_START, this.getStart());
        jsonSession.put(SESSION_END, this.getEnd());
        jsonSession.put(SESSION_DURATION, this.getDuration());
        jsonSession.put(SESSION_DISTANCE, this.getDistance());

        JSONArray jsonDataPoints = new JSONArray();

        List<DataPoint> dataPoints = this.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            JSONObject jsonDataPoint = new JSONObject();
            jsonDataPoint.put(DATAPOINT_TIME, dataPoint.getTime());
            jsonDataPoint.put(DATAPOINT_LATITUDE, dataPoint.getLatitude());
            jsonDataPoint.put(DATAPOINT_LONGITUDE,
                    dataPoint.getLongitude());
            jsonDataPoint.put(DATAPOINT_ELEVATION,
                    dataPoint.getElevation());
            jsonDataPoints.put(jsonDataPoint);
        }

        JSONObject jsonBase = new JSONObject();
        jsonBase.put(VERSION, JSON_VERSION);
        jsonBase.put(SESSION, jsonSession);
        jsonBase.put(DATAPOINTS, jsonDataPoints);

        return jsonBase;
    }

    @Override
    public String buildJsonFileName() {
        return FILE_PREFIX + Format.getDateTimeJson().format(
                new Date(this.getStart())) + JsonFile.FILE_EXT;
    }

    @Override
    public String getFileNameRegex() {
        return Json.FILE_REGEX;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1458438772)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSessionDao() : null;
    }

    public class Json {
        /**
         * JSON file version.
         */
        static final int JSON_VERSION = 2;

        /**
         * JSON file name prefix.
         */
        static final String FILE_PREFIX = "CloudRun_";

        /**
         * Regex for the Json file.
         */
        public static final String FILE_REGEX = FILE_PREFIX
                + "(\\d{4})-(\\d{2})-(\\d{2})_(\\d{6})"
                + JsonFile.FILE_EXT + "$";

        /**
         * JSON name for JSON file version number.
         */
        public static final String VERSION = "version";

        /**
         * JSON name for session object.
         */
        public static final String SESSION = "session";

        /**
         * JSON name for session start.
         */
        public static final String SESSION_START = "start";

        /**
         * JSON name for session end.
         */
        public static final String SESSION_END = "end";

        /**
         * JSON name for session duration.
         */
        public static final String SESSION_DURATION = "duration";

        /**
         * JSON name for session distance.
         */
        public static final String SESSION_DISTANCE = "distance";

        /**
         * JSON name for dataPoint array.
         */
        public static final String DATAPOINTS = "dataPoints";

        /**
         * JSON name for dataPoint time.
         */
        public static final String DATAPOINT_TIME = "time";

        /**
         * JSON name for dataPoint latitude.
         */
        public static final String DATAPOINT_LATITUDE = "latitude";

        /**
         * JSON name for dataPoint longitude.
         */
        public static final String DATAPOINT_LONGITUDE = "longitude";

        /**
         * JSON name for dataPoint elevation.
         */
        public static final String DATAPOINT_ELEVATION = "elevation";
    }
}
