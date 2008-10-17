package org.randombits.confluence.conveyor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.ConfigurationProvider;

public final class ConveyorAssistant {
    private static final Logger LOG = Logger.getLogger( ConveyorAssistant.class );

    private static final ConveyorAssistant INSTANCE = new ConveyorAssistant();

    public static ConveyorAssistant getInstance() {
        return INSTANCE;
    }

    private Set<ConveyorConfigurationProvider> providers;

    private boolean enabled;

    private ConveyorAssistant() {
        providers = new java.util.LinkedHashSet<ConveyorConfigurationProvider>();
        enabled = false;
    }

    /**
     * Adds the specified array of providers to the list for this plugin.
     * 
     * @param providers
     *            The list of providers to add.
     */
    public synchronized void addProviders( ConveyorConfigurationProvider[] providers ) {
        // Disable any existing providers
        boolean wasEnabled = enabled;
        disable();

        Collections.addAll( this.providers, providers );

        // Re-enable with the new settings.
        if ( wasEnabled )
            enable();
    }

    public synchronized void reload() {
        if ( providers.size() > 0 ) {
            Iterator<ConveyorConfigurationProvider> i = providers.iterator();
            while ( i.hasNext() ) {
                ConveyorConfigurationProvider provider = i.next();
                ConfigurationManager.addConfigurationProvider( provider );
            }

            ConfigurationManager.getConfiguration().reload();
        }
    }

    public synchronized void enable() {
        LOG.debug( "Enabling the Conveyor XWork Configuration Provider" );
        if ( !enabled ) {
            reload();
            enabled = true;
        }
    }

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
}
