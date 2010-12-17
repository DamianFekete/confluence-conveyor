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

    private ConveyorAssistant conveyorAssistant;

    public DefaultConveyorModuleDescriptor( ConveyorAssistant conveyorAssistant ) {
        this.conveyorAssistant = conveyorAssistant;
    }

    @Override
    public void init( Plugin plugin, Element element ) throws PluginParseException {
        super.init( plugin, element );
        System.out.println( "conveyor-module: init" );

        providers = new ArrayList<ConveyorConfigurationProvider>();

        String resource = element.attributeValue( "resource" );
        if ( resource != null )
            providers.add( new ConveyorConfigurationProvider( plugin, resource ) );

        // Loop through any <resource type="xml".../> entries.
        for ( ResourceDescriptor rsrc : getResourceDescriptors( "xml" ) ) {
            providers.add( new ConveyorConfigurationProvider( plugin, rsrc.getLocation() ) );
        }
    }

    @Override
    public void enabled() {
        super.enabled();

        conveyorAssistant.addProviders( providers );
    }

    @Override
    public void disabled() {
        conveyorAssistant.removeProviders( providers );

        super.disabled();
    }

    @Override
    public void destroy( Plugin plugin ) {
        super.destroy( plugin );

        conveyorAssistant.removeProviders( providers );

        providers.clear();
    }

    @Override
    public List<ConveyorConfigurationProvider> getModule() {
        return providers;
    }
}
