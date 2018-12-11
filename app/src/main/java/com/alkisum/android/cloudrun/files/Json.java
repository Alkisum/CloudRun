package com.alkisum.android.cloudrun.files;

import com.alkisum.android.cloudlib.file.json.JsonFile;
import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.net.Jsonable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for JSON.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */
public final class Json {

    /**
     * Json constructor.
     */
    private Json() {

    }

    /**
     * Build a list of JSON files from the given entities.
     *
     * @param entities Entities to build JSON files from
     * @return List of JSON files
     * @throws JSONException An error occurred while building the JSON object
     */
    public static List<JsonFile> buildJsonFiles(
            final List<? extends Jsonable> entities) throws JSONException {
        List<JsonFile> jsonFiles = new ArrayList<>();
        for (Jsonable entity : entities) {
            String fileName = entity.buildJsonFileName();
            JSONObject jsonObject = entity.buildJson();
            jsonFiles.add(new JsonFile(fileName, jsonObject));
        }
        return jsonFiles;
    }

    /**
     * Check if the file name is valid.
     *
     * @param jsonFile JSON file to check
     * @param regex    the regular expression to which the file name is to be
     *                 matched
     * @return true if the file name is valid, false otherwise
     */
    public static boolean isFileNameValid(final JsonFile jsonFile,
                                          final String regex) {
        return jsonFile.getName().matches(regex);
    }

    /**
     * Check if the entity is already in the database using the given JSON file.
     *
     * @param jsonFile    JSON file to check
     * @param entityClass Class to use when loading entities
     * @param <T>         Jsonable
     * @return true if the entity is already in the database, false otherwise
     */
    @SuppressWarnings("unchecked")
    public static <T extends Jsonable> boolean isAlreadyInDb(
            final JsonFile jsonFile, final Class<T> entityClass) {
        List<?> entities = Db.getInstance().getDaoSession().getDao(entityClass)
                .loadAll();
        if (entities.isEmpty()) {
            return false;
        }

        List<Jsonable> jsonables = (List<Jsonable>) entities;

        for (Jsonable jsonable : jsonables) {
            if (jsonFile.getName().equals(jsonable.buildJsonFileName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * List file names of the all entities stored in the database.
     *
     * @param entityClass Class to use when loading entities
     * @param <T>         Jsonable
     * @return List of file names of all sessions stored in the database
     */
    @SuppressWarnings("unchecked")
    public static <T extends Jsonable> List<String> getJsonFileNames(
            final Class<T> entityClass) {
        List<String> fileNames = new ArrayList<>();

        List<?> entities = Db.getInstance().getDaoSession().getDao(entityClass)
                .loadAll();
        if (entities.isEmpty()) {
            return fileNames;
        }

        List<Jsonable> jsonables = (List<Jsonable>) entities;
        for (Jsonable jsonable : jsonables) {
            fileNames.add(jsonable.buildJsonFileName());
        }
        return fileNames;
    }
}
