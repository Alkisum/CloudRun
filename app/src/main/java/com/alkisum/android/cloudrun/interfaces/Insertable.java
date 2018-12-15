package com.alkisum.android.cloudrun.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface for entities able to be inserted in the database using a JSON
 * object.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public interface Insertable {

    /**
     * Insert the given JSON object into the database.
     *
     * @param jsonObject JSON object to insert
     * @throws JSONException An error occurred while parsing the JSON object
     */
    void insertFromJson(JSONObject jsonObject) throws JSONException;
}
