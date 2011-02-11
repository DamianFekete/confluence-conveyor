package org.randombits.confluence.conveyor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.elements.ResourceDescriptor;
import org.dom4j.Element;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads
 */
public class ConveyorModuleDescriptor extends AbstractModuleDescriptor<Object> {

    private static final Logger LOG = LoggerFactory.getLogger( ConveyorModuleDescriptor.class );

    private List<ConveyorConfigurationProvider> providers;

    private ConveyorManager conveyorManager;

    public ConveyorModuleDescriptor( ConveyorManager conveyorManager ) {
        LOG.debug( "Constructed ConveyorModuleDescriptor with " + conveyorManager );
        this.conveyorManager = conveyorManager;
    }

    @Override
    public void init( Plugin plugin, Element element ) throws PluginParseException {
        LOG.debug( "Initialising 'conveyor' descriptor for " + plugin.getName() + " (" + plugin.getKey() + ")" );
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
        LOG.debug( "Enabled 'conveyor' module with " + conveyorManager );
        super.enabled();

        conveyorManager.addProviders( providers );
    }

    @Override
    public void disabled() {
        LOG.debug( "Disabled 'conveyor' module with " + conveyorManager );
        conveyorManager.removeProviders( providers );

        super.disabled();
    }

    @Override
    public void destroy( Plugin plugin ) {
        LOG.debug( "Destroyed 'conveyor' module with " + conveyorManager );
        super.destroy( plugin );

        conveyorManager.removeProviders( providers );

        providers.clear();
    }

    /**
     * No public module for this one.
     *
     * @return <code>null</code>
     */
    @Override
    public Object getModule() {
        return null;
    }
}
