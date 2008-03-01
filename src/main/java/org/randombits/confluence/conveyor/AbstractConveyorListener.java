package org.randombits.confluence.conveyor;

import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.plugin.StateAware;

public abstract class AbstractConveyorListener implements EventListener, StateAware {

    private static final Class[] HANDLED_CLASSES = new Class[]{};

    public AbstractConveyorListener() {
        ConveyorAssistant.getInstance().addProviders( createProviders() );
    }

    /**
     * This method is called to create the configuration providers for conveyor.
     * 
     * @return The conveyor configuration providers.
     */
    protected abstract ConveyorConfigurationProvider[] createProviders();

    public Class[] getHandledEventClasses() {
        return HANDLED_CLASSES;
    }

    public void handleEvent( Event event ) {
    }

    public void disabled() {
        ConveyorAssistant.getInstance().disable();
    }

    public void enabled() {
        ConveyorAssistant.getInstance().enable();
    }

}