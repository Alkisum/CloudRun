package com.alkisum.android.cloudrun.events;

/**
 * Class defining insert session event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 3.0
 */
public class InsertSessionEvent {

    /**
     * Insert operation finished successfully.
     */
    public static final int OK = 0;

    /**
     * Insert operation finished with errors.
     */
    public static final int ERROR = 1;

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
     * InsertSessionEvent constructor.
     *
     * @param result Insert operation result
     */
    public InsertSessionEvent(final int result) {
        this.result = result;
    }

    /**
     * InsertSessionEvent constructor.
     *
     * @param result    Insert operation result
     * @param exception Exception thrown during insert operation
     */
    public InsertSessionEvent(final int result, final Exception exception) {
        this.result = result;
        this.exception = exception;
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
