package com.alkisum.android.cloudrun.events;

import com.alkisum.android.cloudrun.interfaces.Insertable;

/**
 * Class defining inserted event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class InsertedEvent {

    /**
     * Insert operation finished successfully.
     */
    public static final int OK = 0;

    /**
     * Insert operation finished with errors.
     */
    public static final int ERROR = 1;

    /**
     * Insertable entity inserted.
     */
    private final Insertable insertable;

    /**
     * Insert operation result.
     */
    private final int result;

    /**
     * Exception thrown during insert operation, null if no exception has been
     * thrown.
     */
    private Exception exception;

    /**
     * InsertedEvent constructor.
     *
     * @param insertable Insertable entity inserted
     * @param result     Insert operation result
     */
    public InsertedEvent(final Insertable insertable, final int result) {
        this.insertable = insertable;
        this.result = result;
    }

    /**
     * InsertedEvent constructor.
     *
     * @param insertable Insertable entity inserted
     * @param result     Insert operation result
     * @param exception  Exception thrown during insert operation
     */
    public InsertedEvent(final Insertable insertable,
                         final int result, final Exception exception) {
        this.insertable = insertable;
        this.result = result;
        this.exception = exception;
    }

    /**
     * @return Insertable entity inserted
     */
    public Insertable getInsertable() {
        return this.insertable;
    }

    /**
     * @return Insert operation result
     */
    public int getResult() {
        return result;
    }

    /**
     * @return Exception thrown during insert operation, null if no exception
     * has been thrown
     */
    public Exception getException() {
        return exception;
    }
}
