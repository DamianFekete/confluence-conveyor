package org.randombits.confluence.conveyor;

import java.util.*;

import org.apache.log4j.Logger;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.ConfigurationProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class DefaultConveyorManager implements InitializingBean, DisposableBean, ConveyorManager {
    private static final Logger LOG = Logger.getLogger( DefaultConveyorManager.class );

    private Set<ConveyorConfigurationProvider> providers;

    private boolean enabled;

    public DefaultConveyorManager() {
        providers = new java.util.LinkedHashSet<ConveyorConfigurationProvider>();
        enabled = false;
    }

    @Override
    public synchronized void addProviders( ConveyorConfigurationProvider... providers ) {
        addProviders( Arrays.asList( providers ) );
    }

    /**
     * Adds the specified array of providers to the list for this plugin.
     *
     * @param providers The list of providers to add.
     */
    @Override
    public synchronized void addProviders( Collection<ConveyorConfigurationProvider> providers ) {
        // Disable any existing providers
        boolean wasEnabled = enabled;
        disable();

        this.providers.addAll( providers );

        // Re-enable with the new settings.
        if ( wasEnabled )
            enable();
    }

    @Override
    public synchronized void removeProviders( ConveyorConfigurationProvider... providers ) {
        removeProviders( Arrays.asList( providers ) );
    }

    /**
     * Adds the specified array of providers to the list for this plugin.
     *
     * @param providers The list of providers to add.
     */
    @Override
    public synchronized void removeProviders( Collection<ConveyorConfigurationProvider> providers ) {
        // Disable any existing providers
        boolean wasEnabled = enabled;
        disable();

        this.providers.removeAll( providers );

        // Re-enable with the new settings.
        if ( wasEnabled )
            enable();
    }

    @Override
    public Collection<ConveyorConfigurationProvider> getProviders() {
        return new ArrayList<ConveyorConfigurationProvider>( providers );
    }

    @Override
    public synchronized void reload() {
        if ( providers.size() > 0 ) {
            for ( ConveyorConfigurationProvider provider : providers ) {
                ConfigurationManager.addConfigurationProvider( provider );
            }

            ConfigurationManager.getConfiguration().reload();
        }
    }

    @Override
    public synchronized void enable() {
        LOG.debug( "Enabling the Conveyor XWork Configuration Provider" );
        if ( !enabled ) {
            reload();
            enabled = true;
        }
    }

    @Override
    public synchronized void disable() {
        if ( enabled ) {
            LOG.debug( "Disabling the Conveyor XWork Configuration Provider" );

            List<ConfigurationProvider> allProviders = ConfigurationManager.getConfigurationProviders();
            for ( ConveyorConfigurationProvider provider : providers ) {
                provider.destroy();

                synchronized ( allProviders ) {
                    allProviders.remove( provider );
                }
                provider = null;
            }
            ConfigurationManager.getConfiguration().reload();
            enabled = false;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        enable();
    }

    @Override
    public void destroy() throws Exception {
        disable();
    }
}
