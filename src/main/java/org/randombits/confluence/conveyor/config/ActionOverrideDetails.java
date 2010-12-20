package org.randombits.confluence.conveyor.config;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;

import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: david
* Date: 17/12/10
* Time: 10:29 PM
* To change this template use File | Settings | File Templates.
*/
public class ActionOverrideDetails {

    private PackageConfig packageConfig;
    private String actionName;

    private ActionOverrideConfig actionConfig;

    ActionOverrideDetails( PackageConfig packageConfig, String actionName, ActionOverrideConfig actionConfig ) {
        this.packageConfig = packageConfig;
        this.actionName = actionName;
        this.actionConfig = actionConfig;
    }

    void reset() {
        Map<String, ActionConfig> actionConfigs = packageConfig.getActionConfigs();
        if ( actionConfigs.get( actionName ) == actionConfig ) {
            packageConfig.addActionConfig( actionName, actionConfig.getOverriddenAction() );
        }
    }

    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    public String getActionName() {
        return actionName;
    }

    public ActionOverrideConfig getActionConfig() {
        return actionConfig;
    }
}
