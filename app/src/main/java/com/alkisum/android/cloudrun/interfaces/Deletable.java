package com.alkisum.android.cloudrun.interfaces;

/**
 * Interface for entities able to be deleted from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public interface Deletable {

    /**
     * Delete the given entities.
     *
     * @param deletables Entities to delete
     * @return List of deleted entities
     */
    Deletable[] deleteEntities(Deletable... deletables);
}
