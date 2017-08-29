package com.alkisum.android.cloudrun.event;

import com.alkisum.android.cloudrun.location.Coordinate;

/**
 * Class defining coordinate event for EventBus.
 *
 * @author Alkisum
 * @version 3.0
 * @since 1.0
 */
public class CoordinateEvent {

    /**
     * Coordinate values.
     */
    private final Coordinate values;

    /**
     * CoordinateEvent constructor.
     *
     * @param values Coordinate values
     */
    public CoordinateEvent(final Coordinate values) {
        this.values = values;
    }

    /**
     * @return Coordinate values
     */
    public final Coordinate getValues() {
        return values;
    }
}
