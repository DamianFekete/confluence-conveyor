package org.randombits.confluence.conveyor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.predicate.PluginPredicate;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.plugin.StateAware;

public abstract class AbstractConveyorListener implements EventListener, StateAware {

    private static final Class<?>[] HANDLED_CLASSES = new Class<?>[]{};

    private PluginAccessor pluginAccessor;

    public AbstractConveyorListener() {
        ConveyorAssistant.getInstance().addProviders( createProviders() );
    }

    /**
     * This method is called to create the configuration providers for conveyor.
     *
     * @return The conveyor configuration providers.
     */
    protected abstract ConveyorConfigurationProvider[] createProviders();

    @SuppressWarnings("unchecked")
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

    public Plugin findPlugin() {
        for ( Plugin plugin : pluginAccessor.getPlugins( new PluginPredicate() {
            public boolean matches( Plugin plugin ) {
                try {
                    Class<?> loadedClass = plugin.loadClass( AbstractConveyorListener.class.getName(), AbstractConveyorListener.class );
                    Class<?> myClass = AbstractConveyorListener.class;
                    return loadedClass == myClass;
                } catch ( ClassNotFoundException e ) {
                    return false;
                }
            }
        } ) ) {
            return plugin;
        }
        return null;
    }

    public void setPluginAccessor( PluginAccessor pluginAccessor ) {
        this.pluginAccessor = pluginAccessor;
    }
}