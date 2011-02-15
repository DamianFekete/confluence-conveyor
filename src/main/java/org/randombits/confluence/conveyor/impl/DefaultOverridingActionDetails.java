package org.randombits.confluence.conveyor.impl;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.web.Condition;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;
import com.opensymphony.xwork.interceptor.Interceptor;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverridingActionDetails;
import org.randombits.confluence.conveyor.xwork.ConveyorConfigurationProvider;
import org.randombits.confluence.conveyor.xwork.OverridingActionConfig;

import java.util.List;
import java.util.Map;

/**
 * Implements {@link org.randombits.confluence.conveyor.ActionDetails} for Actions which are
 * overriding another Action.
 */
public class DefaultOverridingActionDetails extends BaseActionDetails implements OverridingActionDetails {

    private final DefaultOverriddenActionDetails overriddenAction;

    private int weight;

    public DefaultOverridingActionDetails( DefaultOverriddenActionDetails overriddenAction, String actionName, OverridingActionConfig actionConfig ) {
        super( overriddenAction.getPackageDetails(), actionName, actionConfig );
        this.overriddenAction = overriddenAction;

        this.weight = actionConfig.getWeight();

        if ( actionConfig.isInherited() ) {
            inheritOriginalAction();
        }
    }

    private void inheritOriginalAction() {
        OverridingActionConfig overriding = getActionConfig();
        ActionConfig original = overriddenAction.getOriginalAction().getActionConfig();

        if ( overriding.getClassName() == null ) {
            overriding.setClassName( original.getClassName() );
            if ( overriding.getMethodName() == null )
                overriding.setMethodName( original.getMethodName() );

            // If the classname is inherited, we need to also inherit the Plugin to it can load the class...
            if ( original instanceof PluginAwareActionConfig ) {
                overriding.setPlugin( ( (PluginAwareActionConfig) original ).getPlugin() );
            }
        }

        // Copy the new params over the old params.
        Map<String, String> oldParams = ConveyorConfigurationProvider.copyParams( original.getParams() );
        if ( oldParams != null ) {
            if ( overriding.getParams() != null )
                oldParams.putAll( overriding.getParams() );
            overriding.setParams( oldParams );
        }

        // Copy the new results over the old results.
        Map<String, ResultConfig> oldResults = ConveyorConfigurationProvider.copyResults( original.getResults() );
        if ( oldResults != null ) {
            if ( overriding.getResults() != null )
                oldResults.putAll( overriding.getResults() );
            overriding.setResults( oldResults );
        }

        // only copy if no new interceptors are specified.
        List<Interceptor> interceptors = overriding.getInterceptors();
        if ( interceptors == null || interceptors.size() == 0 && original.getExternalRefs() != null )
            overriding.addInterceptors( ConveyorConfigurationProvider.copyInterceptors( original
                    .getInterceptors() ) );
        // only copy if now new external refs are specified.
        List externalRefs = overriding.getExternalRefs();
        if ( externalRefs == null || externalRefs.size() == 0 && original.getExternalRefs() != null )
            overriding.addExternalRefs( ConveyorConfigurationProvider.copyExternalRefs( original
                    .getExternalRefs() ) );
        // copy the package name.
        if ( overriding.getPackageName() == null )
            overriding.setPackageName( original.getPackageName() );
    }


    public DefaultOverriddenActionDetails getOverriddenAction() {
        return overriddenAction;
    }

    public OverridingActionConfig getActionConfig() {
        return (OverridingActionConfig) super.getActionConfig();
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight( int weight ) {
        this.weight = weight;
    }

    public String getKey() {
        return getActionConfig().getKey();
    }

    public Condition getCondition() {
        return getActionConfig().getCondition();
    }

    @Override
    public boolean revert() throws ConveyorException {
        if ( super.revert() ) {
            // Remove ourselves from the overridden action.
            overriddenAction.removeOverridingAction( this );
            return true;
        }
        return false;
    }

    public boolean matchesBypass( String bypass ) {
        return bypass == null || bypass.equals( getKey() ) || bypass.equals( getActionConfig().getClassName() );
    }
}
