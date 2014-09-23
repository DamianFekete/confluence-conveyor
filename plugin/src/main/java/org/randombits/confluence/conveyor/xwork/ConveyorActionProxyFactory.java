package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.ActionProxy;
import com.opensymphony.xwork.DefaultActionProxyFactory;
import com.opensymphony.xwork.XworkException;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.ConveyorException;

import java.util.Map;

/**
 * Overrides the standard DefaultActionProxyFactory to correctly handle instances
 * of PluginAwareActionConfig.
 */
public class ConveyorActionProxyFactory extends DefaultActionProxyFactory {

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
        // Record the alias so it can be removed again later if necessary.
        if ( packageConfig instanceof OverriddenPackageConfig ) {
            ( (OverriddenPackageConfig) packageConfig ).addAlias( name.getActionName(), actionName );
        } else if ( name.getOverrideType() != null ) {
            // don't allow '@' or '*' actions for non-overridden packages.
            return;
        }

        packageConfig.addActionConfig( actionName, newConfig );
        ConfigurationManager.getConfiguration().rebuildRuntimeConfiguration();
    }
}
