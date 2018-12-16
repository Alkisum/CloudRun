package com.alkisum.android.cloudrun.interfaces;

/**
 * Interface for entities able to be restored from the database.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public interface Restorable {

    /**
     * Restore the given entities.
     *
     * @param restorables Restorable entities to restore
     */
    void restore(Restorable[] restorables);
}
