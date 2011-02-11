package org.randombits.confluence.conveyor;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;

/**
 * A factory for {@link ConveyorModuleDescriptor}s.
 */
public class ConveyorModuleDescriptorFactory extends SingleModuleDescriptorFactory<ConveyorModuleDescriptor> {

    private final ConveyorManager conveyorManager;

    public ConveyorModuleDescriptorFactory( HostContainer hostContainer, ConveyorManager conveyorManager ) {
        super(hostContainer, "conveyor", ConveyorModuleDescriptor.class);
        this.conveyorManager = conveyorManager;
    }

    @Override
    public ModuleDescriptor getModuleDescriptor(String type) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        return hasModuleDescriptor(type) ? new ConveyorModuleDescriptor( conveyorManager ) : null;
    }
}