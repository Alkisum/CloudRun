package com.alkisum.android.ownrun.event;

import com.alkisum.android.ownrun.location.Coordinate;

/**
 * Class defining coordinate event for EventBus.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class CoordinateEvent {

    /**
     * Coordinate values.
     */
    private final Coordinate mValues;

    /**
     * CoordinateEvent constructor.
     *
     * @param value Coordinate values
     */
    public CoordinateEvent(final Coordinate value) {
        mValues = value;
    }

    /**
     * @return Coordinate values
     */
    public final Coordinate getValues() {
        return mValues;
    }
}
