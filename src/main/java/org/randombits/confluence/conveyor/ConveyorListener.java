package org.randombits.confluence.conveyor;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.plugin.StateAware;

public class ConveyorListener implements EventListener, StateAware {

    private static final Class[] HANDLED_CLASSES = new Class[0];

    public Class[] getHandledEventClasses() {
        return HANDLED_CLASSES;
    }

    public void handleEvent( Event event ) {
        // Do nothing.
    }

    public void disabled() {
        ConveyorAssistant.getInstance().disable();
    }

    public void enabled() {
        ConveyorAssistant.getInstance().enable();
    }

}
