package org.randombits.confluence.conveyor.impl;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import org.randombits.confluence.conveyor.ConveyorManager;
import org.randombits.confluence.conveyor.ConveyorModuleDescriptor;
import org.randombits.confluence.conveyor.OverrideManager;

/**
 * A factory for
 * {@link org.randombits.confluence.conveyor.ConveyorModuleDescriptor}s.
 */
public class ConveyorModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConveyorModuleDescriptor> {

    private final OverrideManager overrideManager;
    private final ConveyorManager conveyorManager;
    private ModuleFactory moduleFactory;

    public ConveyorModuleDescriptorFactory(HostContainer hostContainer,
            ConveyorManager conveyorManager,
            OverrideManager overrideManager,
            ModuleFactory moduleFactory) {
        super(hostContainer, "conveyor", ConveyorModuleDescriptor.class);
        this.conveyorManager = conveyorManager;
        this.overrideManager = overrideManager;
        this.moduleFactory = moduleFactory;
    }

    @Override
    public ModuleDescriptor getModuleDescriptor(String type) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        return hasModuleDescriptor(type) ? new ConveyorModuleDescriptor(conveyorManager, overrideManager, moduleFactory) : null;
    }
}