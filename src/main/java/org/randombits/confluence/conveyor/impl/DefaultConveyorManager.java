package org.randombits.confluence.conveyor.impl;

import com.opensymphony.xwork.ActionProxyFactory;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.ConfigurationProvider;
import org.randombits.confluence.conveyor.ConveyorManager;
import org.randombits.confluence.conveyor.xwork.ConveyorActionProxyFactory;
import org.randombits.confluence.conveyor.xwork.ConveyorConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

public class DefaultConveyorManager implements InitializingBean, DisposableBean, ConveyorManager {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultConveyorManager.class );

    private Set<ConveyorConfigurationProvider> providers;

    private boolean enabled;

    private ActionProxyFactory originalActionProxyFactory;

    private ConveyorActionProxyFactory conveyorActionProxyFactory;

    public DefaultConveyorManager( ConveyorActionProxyFactory factory ) {
        LOG.debug( "Constructed " + this );
        providers = new java.util.LinkedHashSet<ConveyorConfigurationProvider>();
        enabled = false;

        this.conveyorActionProxyFactory = factory;
    }

    public synchronized void addProviders( ConveyorConfigurationProvider... providers ) {
        addProviders( Arrays.asList( providers ) );
    }

    /**
     * Adds the specified array of providers to the list for this plugin.
     *
     * @param providers The list of providers to add.
     */
    public synchronized void addProviders( Collection<ConveyorConfigurationProvider> providers ) {
        // Disable any existing providers
        boolean wasEnabled = enabled;
        disable();

        this.providers.addAll( providers );

        // Re-enable with the new settings.
        if ( wasEnabled )
            enable();
    }

    public synchronized void removeProviders( ConveyorConfigurationProvider... providers ) {
        removeProviders( Arrays.asList( providers ) );
    }

    /**
     * Adds the specified array of providers to the list for this plugin.
     *
     * @param providers The list of providers to add.
     */
    public synchronized void removeProviders( Collection<ConveyorConfigurationProvider> providers ) {
        // Disable any existing providers
        boolean wasEnabled = enabled;
        disable();

        this.providers.removeAll( providers );

        // Re-enable with the new settings.
        if ( wasEnabled )
            enable();
    }

    public Collection<ConveyorConfigurationProvider> getProviders() {
        return new ArrayList<ConveyorConfigurationProvider>( providers );
    }

    public synchronized void reload() {
        if ( providers.size() > 0 ) {
            for ( ConveyorConfigurationProvider provider : providers ) {
                ConfigurationManager.addConfigurationProvider( provider );
            }

            ConfigurationManager.getConfiguration().reload();
        }
    }

    public synchronized void enable() {
        if ( !enabled ) {
            LOG.debug( "Enabling " + this );
            reload();
            enabled = true;
        }
    }

    public synchronized void disable() {
        if ( enabled ) {
            LOG.debug( "Disabling " + this );

            List<ConfigurationProvider> allProviders = ConfigurationManager.getConfigurationProviders();
            for ( ConveyorConfigurationProvider provider : providers ) {
                provider.destroy();

                synchronized ( allProviders ) {
                    allProviders.remove( provider );
                }
            }
            ConfigurationManager.getConfiguration().reload();
            enabled = false;
        }
    }

    public void afterPropertiesSet() throws Exception {
        LOG.debug( "Initialising " + this );
        prepareActionProxyFactory();
        enable();
    }

    private void prepareActionProxyFactory() {
        originalActionProxyFactory = ActionProxyFactory.getFactory();
        ActionProxyFactory.setFactory( conveyorActionProxyFactory );
    }

    public void destroy() throws Exception {
        LOG.debug( "Destroying " + this );
        disable();
        resetActionProxyFactory();
    }

    private void resetActionProxyFactory() {
        if ( originalActionProxyFactory != null ) {
            ActionProxyFactory.setFactory( originalActionProxyFactory );
        }
    }

    @Override
    public String toString() {
        return "DefaultConveyorManager (" + System.identityHashCode( this ) + ")";
    }
}
