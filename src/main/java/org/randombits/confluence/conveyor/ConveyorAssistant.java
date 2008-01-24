package org.randombits.confluence.conveyor;

import java.util.List;

import org.apache.log4j.Logger;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import com.opensymphony.xwork.config.ConfigurationManager;

public final class ConveyorAssistant {
    private static final Logger LOG = Logger.getLogger( ConveyorAssistant.class );

    private static final ConveyorAssistant INSTANCE = new ConveyorAssistant();

    public static ConveyorAssistant getInstance() {
        return INSTANCE;
    }

    private ConveyorConfigurationProvider provider;

    private ConveyorAssistant() {
    }
    
    public synchronized void reload() {
        if ( provider != null ) {
            ConfigurationManager.addConfigurationProvider( provider );
            ConfigurationManager.getConfiguration().reload();
        }
    }

    public synchronized void enable() {
        LOG.debug( "Enabling the Conveyor XWork Configuration Provider" );
        if ( provider == null ) {
            provider = new ConveyorConfigurationProvider();
            reload();
        }
    }

    public synchronized void disable() {
        if ( provider != null ) {
            LOG.debug( "Disabling the Conveyor XWork Configuration Provider" );
            
            provider.destroy();
            
            List providers = ConfigurationManager.getConfigurationProviders();
            synchronized ( providers ) {
                providers.remove( provider );
            }
            provider = null;
            ConfigurationManager.getConfiguration().reload();
        }
    }
}
