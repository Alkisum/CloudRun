package com.alkisum.android.cloudrun.utils;

import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Restorable;

/**
 * Utility class for deletables operations.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public final class Deletables {

    /**
     * Deletables constructor.
     */
    private Deletables() {

    }

    /**
     * Convert a deletable arrays into a restorable array.
     *
     * @param deletables Array to convert
     * @return Converted array
     */
    public static Restorable[] toRestorables(final Deletable[] deletables) {
        if (deletables.length == 0) {
            return new Restorable[0];
        } else if (!Restorable.class.isAssignableFrom(
                deletables[0].getClass())) {
            throw new IllegalArgumentException(
                    deletables[0].getClass().getName()
                            + " does not implement Restorable");
        }
        Restorable[] restorables = new Restorable[deletables.length];
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(deletables, 0, restorables, 0, deletables.length);
        return restorables;
    }
}
