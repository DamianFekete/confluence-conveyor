package org.randombits.confluence.conveyor;

import com.atlassian.plugin.ModuleDescriptor;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import java.util.List;

/**
 * Defines a module that contains a {@link DefaultConveyorManager}, which helps override modules.
 */
public interface ConveyorModuleDescriptor extends ModuleDescriptor<List<ConveyorConfigurationProvider>> {
    /**
     * The module's {@link ConveyorConfigurationProvider}.
     *
     * @return The assistant for this module.
     */
    List<ConveyorConfigurationProvider> getModule();
}
