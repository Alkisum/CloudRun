package com.alkisum.android.cloudrun.events;

/**
 * Class defining session action event for EventBus.
 *
 * @author Alkisum
 * @version 4.0
 * @since 4.0
 */
public class SessionActionEvent {

    /**
     * Action performed when a session is started.
     */
    public static final String START = "Start";

    /**
     * Action performed when a session is resumed.
     */
    public static final String RESUME = "Resume";

    /**
     * Action performed when a session is paused.
     */
    public static final String PAUSE = "Pause";

    /**
     * Action performed when a session is stopped.
     */
    public static final String STOP = "Stop";

    /**
     * Action performed on session.
     */
    private final String action;

    /**
     * SessionActionEvent constructor.
     *
     * @param action Action performed on session
     */
    public SessionActionEvent(final String action) {
        this.action = action;
    }

    /**
     * @return Action performed on session
     */
    public final String getAction() {
        return action;
    }
}
