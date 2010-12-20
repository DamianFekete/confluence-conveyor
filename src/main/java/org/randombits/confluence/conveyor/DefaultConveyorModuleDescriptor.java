package org.randombits.confluence.conveyor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import org.dom4j.Element;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads
 */
public class DefaultConveyorModuleDescriptor extends AbstractModuleDescriptor<List<ConveyorConfigurationProvider>> implements ConveyorModuleDescriptor {

    private List<ConveyorConfigurationProvider> providers;

    private ConveyorManager conveyorManager;

    public DefaultConveyorModuleDescriptor( ConveyorManager conveyorManager ) {
        this.conveyorManager = conveyorManager;
    }

    @Override
    public void init( Plugin plugin, Element element ) throws PluginParseException {
        super.init( plugin, element );

        providers = new ArrayList<ConveyorConfigurationProvider>();

        String resource = element.attributeValue( "resource" );
        if ( resource != null )
            providers.add( new ConveyorConfigurationProvider( plugin, resource ) );

        // Loop through any <resource type="xml".../> entries.
        for ( ResourceDescriptor rsrc : getResourceDescriptors( "xml" ) ) {
            providers.add( new ConveyorConfigurationProvider( plugin, rsrc.getLocation() ) );
        }

        if ( providers.size() == 0 )
            throw new PluginParseException( "Please specify either the 'resource' parameter or one or more 'resource' elements of type 'xml'." );
    }

    @Override
    public void enabled() {
        super.enabled();

        conveyorManager.addProviders( providers );
    }

    @Override
    public void disabled() {
        conveyorManager.removeProviders( providers );

        super.disabled();
    }

    @Override
    public void destroy( Plugin plugin ) {
        super.destroy( plugin );

        conveyorManager.removeProviders( providers );

        providers.clear();
    }

    @Override
    public List<ConveyorConfigurationProvider> getModule() {
        return providers;
    }
}
