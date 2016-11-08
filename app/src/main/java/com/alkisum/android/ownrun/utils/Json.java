package com.alkisum.android.ownrun.utils;

/**
 * Class containing constant for Json files.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */

public final class Json {

    /**
     * JSON file name prefix.
     */
    public static final String FILE_PREFIX = "ownRun_";

    /**
     * JSON file extension.
     */
    public static final String FILE_EXT = ".json";

    /**
     * Regex for the Json file.
     */
    public static final String FILE_REGEX = Json.FILE_PREFIX
            + "(\\d{4})-(\\d{2})-(\\d{2})_(\\d{6})" + Json.FILE_EXT;

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

    /**
     * Json constructor.
     */
    private Json() {

    }
}
