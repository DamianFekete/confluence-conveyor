package org.randombits.confluence.conveyor;

import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

/**
 * Provides support for legacy Plugin 1-framework plugins that use Conveyor. Once switching to Plugins 2,
 * plugins should prefer to use the &lt;conveyor&gt; module type, since it will integrate better with other
 * plugins.
 */
@Deprecated
public class ConveyorListener extends AbstractConveyorListener {

    public ConveyorListener() {
    }

    @Override protected ConveyorConfigurationProvider[] createProviders() {
        return new ConveyorConfigurationProvider[] { new ConveyorConfigurationProvider( findPlugin() ) };
    }

}
