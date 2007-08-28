package org.randombits.confluence.conveyor;

import com.atlassian.confluence.event.events.plugin.PluginDisableEvent;
import com.atlassian.confluence.event.events.plugin.PluginEnableEvent;
import com.atlassian.confluence.event.events.plugin.PluginInstallEvent;
import com.atlassian.confluence.event.events.plugin.PluginUninstallEvent;
import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.plugin.StateAware;

public class ConveyorListener implements EventListener, StateAware {

    private static final Class[] HANDLED_CLASSES = new Class[]{PluginInstallEvent.class,
            PluginUninstallEvent.class, PluginEnableEvent.class, PluginDisableEvent.class};

    public Class[] getHandledEventClasses() {
        return HANDLED_CLASSES;
    }

    public void handleEvent( Event event ) {
        ConveyorAssistant.getInstance().reload();
    }

    public void disabled() {
        ConveyorAssistant.getInstance().disable();
    }

    public void enabled() {
        ConveyorAssistant.getInstance().enable();
    }

}
