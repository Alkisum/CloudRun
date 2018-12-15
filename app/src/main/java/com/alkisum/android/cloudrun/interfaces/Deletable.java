package com.alkisum.android.cloudrun.interfaces;

import java.util.List;

/**
 * Interface for entities able to be deleted from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public interface Deletable {

    /**
     * Delete the selected entities.
     *
     * @return List of deleted entities
     */
    List<? extends Deletable> deleteSelected();
}
