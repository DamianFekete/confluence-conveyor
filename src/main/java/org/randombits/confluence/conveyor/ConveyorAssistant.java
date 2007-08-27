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

    private ConveyorConfigurationProvider conveyorConfigurationProvider;

    private ConveyorAssistant() {
    }
    
    public synchronized void enable() {
        if ( conveyorConfigurationProvider == null ) {
            LOG.debug( "Enabling the Conveyor XWork Configuration Provider" );
            conveyorConfigurationProvider = new ConveyorConfigurationProvider();
            ConfigurationManager.addConfigurationProvider( conveyorConfigurationProvider );
            ConfigurationManager.getConfiguration().reload();
        }
    }

    public synchronized void disable() {
        if ( conveyorConfigurationProvider != null ) {
            LOG.debug( "Disabling the Conveyor XWork Configuration Provider" );
            List providers = ConfigurationManager.getConfigurationProviders();
            synchronized ( providers ) {
                providers.remove( conveyorConfigurationProvider );
            }
            conveyorConfigurationProvider = null;
            ConfigurationManager.getConfiguration().reload();
        }
    }
}
