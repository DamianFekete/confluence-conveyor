package org.randombits.confluence.conveyor.config;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;

import java.util.Map;

/**
 * Represents an action in a 'package-override'.
 */
public class ActionDetails {

    private final PackageDetails packageDetails;

    private final String actionName;

    private final ActionConfig actionConfig;

    public ActionDetails( PackageDetails packageDetails, String actionName, ActionConfig actionConfig ) {
        this.packageDetails = packageDetails;
        this.actionName = actionName;
        this.actionConfig = actionConfig;
    }

    public PackageDetails getPackageDetails() {
        return packageDetails;
    }

    public String getActionName() {
        return actionName;
    }

    public ActionConfig getActionConfig() {
        return actionConfig;
    }

    public boolean isOverride() {
        return actionConfig instanceof ActionOverrideConfig;
    }

    public void revert() {
        PackageConfig packageConfig = packageDetails.getPackageConfig();
        Map<String, ActionConfig> actionConfigs = packageConfig.getActionConfigs();
        if ( actionConfig instanceof ActionOverrideConfig ) {
            ActionOverrideConfig actionOverrideConfig = (ActionOverrideConfig) actionConfig;
            if ( actionConfigs.get( actionName ) == actionConfig ) {
                packageConfig.addActionConfig( actionName, actionOverrideConfig.getOverriddenAction() );
            }
        } else {
            // Only remove it if it's still a match.
            if ( actionConfigs.get( actionName ).equals( actionConfig ) )
                actionConfigs.remove( actionName );
        }
    }
}
