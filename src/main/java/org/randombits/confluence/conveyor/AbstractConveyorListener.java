package org.randombits.confluence.conveyor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.predicate.PluginPredicate;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.plugin.StateAware;

/**
 * This listener class provides backwards-compatibility for Plugins 1 plugins. Conveyor
 * will function, but doesn't not integrate with the new management features. It is preferred
 * that plugins use the &lt;conveyor&gt; module type instead if they support the Plugins 2 framework.
 */
@Deprecated
public abstract class AbstractConveyorListener implements EventListener, StateAware {

    private static final Class<?>[] HANDLED_CLASSES = new Class<?>[]{};

    private PluginAccessor pluginAccessor;

    private ConveyorManager conveyorManager;

    public AbstractConveyorListener() {
        conveyorManager = new DefaultConveyorManager();
        conveyorManager.addProviders( createProviders() );
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
        conveyorManager.disable();
    }

    public void enabled() {
        conveyorManager.enable();
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