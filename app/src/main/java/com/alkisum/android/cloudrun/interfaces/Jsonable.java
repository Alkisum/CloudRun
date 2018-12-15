package com.alkisum.android.cloudrun.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface for entities able to be transformed into JSON objects.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public interface Jsonable {

    /**
     * Build JSON object.
     *
     * @return JSON object
     * @throws JSONException Exception thrown while building JSON object
     */
    JSONObject buildJson() throws JSONException;

    /**
     * Build JSON file name.
     *
     * @return JSON file name
     */
    String buildJsonFileName();

    /**
     * @return Regular expression to check file name
     */
    String getFileNameRegex();
}
