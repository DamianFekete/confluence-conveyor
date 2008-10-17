package org.randombits.confluence.conveyor.config;

import java.util.List;
import java.util.Map;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.ExternalReference;
import com.opensymphony.xwork.config.entities.InterceptorConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;

/**
 * Replaces an existing action configuration.
 * 
 * @author David Peterson
 */
public class ActionOverrideConfig extends ActionConfig {

    private ActionConfig overriddenAction;

    public ActionOverrideConfig() {
        super();
    }

    // public ActionOverrideConfig( ActionConfig overriddenAction, boolean
    // copySettings, String methodName,
    // String className, Map parameters, Map results, List interceptors ) {
    // this( overriddenAction, copySettings, methodName, className, parameters,
    // results, interceptors, null, null );
    // }

    public ActionOverrideConfig( ActionConfig overriddenAction, boolean copySettings, String methodName,
            String className, Map<String, String> parameters, Map<String, ResultConfig> results,
            List<InterceptorConfig> interceptors, List<ExternalReference> externalRefs, String packageName ) {
        super( methodName, className, parameters, results, interceptors, externalRefs, packageName );
        setOverriddenAction( overriddenAction, copySettings );
    }

    public ActionOverrideConfig( ActionConfig overriddenAction, boolean copySettings ) {
        this( overriddenAction, copySettings, null, null, null, null, null, null, null );
    }

    public ActionConfig getOverriddenAction() {
        return overriddenAction;
    }

    private void setOverriddenAction( ActionConfig overriddenAction, boolean copySettings ) {
        this.overriddenAction = overriddenAction;
        if ( copySettings ) {
            if ( className == null ) {
                setClassName( overriddenAction.getClassName() );
                if ( methodName == null )
                    setMethodName( overriddenAction.getMethodName() );
            }

            // Copy the new params over the old params.
            Map<String, String> oldParams = ConveyorConfigurationProvider.copyParams( overriddenAction.getParams() );
            if ( oldParams != null ) {
                if ( params != null )
                    oldParams.putAll( params );
                params = oldParams;
            }

            // Copy the new results over the old results.
            Map<String, ResultConfig> oldResults = ConveyorConfigurationProvider.copyResults( overriddenAction.getResults() );
            if ( oldResults != null ) {
                if ( results != null )
                    oldResults.putAll( results );
                results = oldResults;
            }

            // only copy if no new interceptors are specified.
            if ( interceptors == null || interceptors.size() == 0 && overriddenAction.getExternalRefs() != null )
                addInterceptors( ConveyorConfigurationProvider.copyInterceptors( overriddenAction
                        .getInterceptors() ) );
            // only copy if now new external refs are specified.
            if ( externalRefs == null || externalRefs.size() == 0 && overriddenAction.getExternalRefs() != null )
                addExternalRefs( ConveyorConfigurationProvider.copyExternalRefs( overriddenAction
                        .getExternalRefs() ) );
            // copy the package name.
            if ( packageName == null )
                setPackageName( overriddenAction.getPackageName() );
        }
    }

    public void setOverriddenAction( ActionConfig overriddenAction ) {
        setOverriddenAction( overriddenAction, false );
    }

}
