package org.randombits.confluence.conveyor.config;

import java.util.List;
import java.util.Map;

import com.opensymphony.xwork.config.entities.ActionConfig;

/**
 * Replaces an existing action configuration.
 * 
 * @author David Peterson
 */
public class ActionOverrideConfig extends ActionConfig {

    private ActionConfig replacedAction;

    public ActionOverrideConfig() {
        super();
    }

    public ActionOverrideConfig( ActionConfig replacedAction, boolean copySettings, String methodName,
            String className, Map parameters, Map results, List interceptors ) {
        this( replacedAction, copySettings, methodName, className, parameters, results, interceptors, null, null );
    }

    public ActionOverrideConfig( ActionConfig replacedAction, boolean copySettings, String methodName,
            String className, Map parameters, Map results, List interceptors, List externalRefs, String packageName ) {
        super( methodName, className, parameters, results, interceptors, externalRefs, packageName );
        setReplacedAction( replacedAction, copySettings );
    }

    public ActionOverrideConfig( ActionConfig replacedAction, boolean copySettings ) {
        this( replacedAction, copySettings, null, null, null, null, null, null, null );
    }

    public ActionConfig getReplacedAction() {
        return replacedAction;
    }

    private void setReplacedAction( ActionConfig replacedAction, boolean copySettings ) {
        this.replacedAction = replacedAction;
        if ( copySettings ) {
            if ( className == null ) {
                setClassName( replacedAction.getClassName() );
                if ( methodName == null )
                    setMethodName( replacedAction.getMethodName() );
            }
            if ( params == null )
                setParams( ConveyorConfigurationProvider.copyParams( replacedAction.getParams() ) );
            if ( results == null )
                setResults( ConveyorConfigurationProvider.copyResults( replacedAction.getResults() ) );
            if ( interceptors == null )
                addInterceptors( ConveyorConfigurationProvider.copyInterceptors( replacedAction.getInterceptors() ) );
            if ( externalRefs == null )
                addExternalRefs( ConveyorConfigurationProvider.copyExternalRefs( replacedAction.getExternalRefs() ) );
            if ( packageName == null )
                setPackageName( replacedAction.getPackageName() );
        }
    }

    public void setReplacedAction( ActionConfig replacedAction ) {
        setReplacedAction( replacedAction, false );
    }

}
