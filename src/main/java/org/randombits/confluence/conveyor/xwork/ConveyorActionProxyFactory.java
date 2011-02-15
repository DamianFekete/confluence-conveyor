package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.DefaultActionProxyFactory;
import com.opensymphony.xwork.XworkException;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.ActionDetails;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverrideManager;
import org.randombits.confluence.conveyor.PackageDetails;

import java.util.Map;

/**
 * Overrides the standard DefaultActionProxyFactory to correctly handle instances
 * of PluginAwareActionConfig.
 */
public class ConveyorActionProxyFactory extends DefaultActionProxyFactory {

    private final OverrideManager overrideManager;

    public ConveyorActionProxyFactory( OverrideManager overrideManager ) {
        this.overrideManager = overrideManager;
    }

    @Override
    public ActionProxy createActionProxy( String namespace, String actionName, Map extraContext )
            throws Exception {
        setupConfigIfActionIsCommand( namespace, actionName );
        return new ConveyorActionProxy( namespace, actionName, extraContext, true );
    }

    @Override
    public ActionProxy createActionProxy( String namespace, String actionName, Map extraContext, boolean executeResult )
            throws Exception {
        setupConfigIfActionIsCommand( namespace, actionName );
        return new ConveyorActionProxy( namespace, actionName, extraContext, executeResult );
    }

    private void setupConfigIfActionIsCommand( String namespace, String actionName ) throws ConveyorException {
        // Check the cache
        if ( ConfigurationManager.getConfiguration().getRuntimeConfiguration().getActionConfig( namespace, actionName ) != null )
            return;

        ActionRequest name = ActionRequest.parse( actionName );

        // Make sure we have something to do...
        if ( name.getOverrideKey() == null && name.getMethodName() == null )
            return;

        ActionConfig actionConfig = ConfigurationManager.getConfiguration().getRuntimeConfiguration().getActionConfig( namespace, name.getActionName() );

        ActionConfig newConfig;
        if ( actionConfig instanceof PluginAwareActionConfig ) {
            Plugin plugin = ( (PluginAwareActionConfig) actionConfig ).getPlugin();
            newConfig = new PluginAwareActionConfig( name.getMethodName(), actionConfig.getClassName(), actionConfig.getParams(), actionConfig.getResults(), actionConfig.getInterceptors(), actionConfig.getExternalRefs(), actionConfig.getPackageName(), plugin );
        } else if ( actionConfig != null ) {
            newConfig = new ActionConfig( name.getMethodName(), actionConfig.getClassName(), actionConfig.getParams(), actionConfig.getResults(), actionConfig.getInterceptors(), actionConfig.getExternalRefs(), actionConfig.getPackageName() );
        } else {
            throw new XworkException( "Unable to find action named '" + name.getActionName() + "' in the '" + namespace + "' namespace." );
        }

        PackageConfig packageConfig = ConfigurationManager.getConfiguration().getPackageConfig( newConfig.getPackageName() );
        PackageDetails packageDetails = overrideManager.getPackage( packageConfig, false );

        if ( packageDetails != null ) {
            // Need to let the OverrideManager know that there is an extra alias in play so it can clean up.
            ActionDetails details = packageDetails.getAction( name.getActionName() );
            if ( details != null )
                details.addAlias( actionName );
        }

        packageConfig.addActionConfig( actionName, newConfig );
        ConfigurationManager.getConfiguration().rebuildRuntimeConfiguration();
    }
}
