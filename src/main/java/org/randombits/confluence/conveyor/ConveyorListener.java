package org.randombits.confluence.conveyor;

import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

public class ConveyorListener extends AbstractConveyorListener {

    public ConveyorListener() {
        ConveyorAssistant.getInstance().addProviders( createProviders() );
    }

    @Override protected ConveyorConfigurationProvider[] createProviders() {
        return new ConveyorConfigurationProvider[] { new ConveyorConfigurationProvider() };
    }

}
