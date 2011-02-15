package org.randombits.confluence.conveyor.impl;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.ActionDetails;
import org.randombits.confluence.conveyor.ConveyorException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an action in a 'package-override'.
 */
public abstract class BaseActionDetails implements ActionDetails {

    private final DefaultPackageDetails packageDetails;

    private final ActionConfig actionConfig;

    private String actionName;

    private boolean reverted;

    private Set<String> aliases;

    public BaseActionDetails( DefaultPackageDetails packageDetails, String actionName, ActionConfig actionConfig ) {
        this.packageDetails = packageDetails;
        this.actionName = actionName;
        this.actionConfig = actionConfig;
    }

    public DefaultPackageDetails getPackageDetails() {
        return packageDetails;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName( String actionName ) {
        this.actionName = actionName;
    }

    public ActionConfig getActionConfig() {
        return actionConfig;
    }

    /**
     * Checks if the action has been reverted.
     *
     * @return
     */
    public boolean isReverted() {
        return reverted;
    }

    /**
     * Reverts the action to its former state.
     *
     * @return
     */
    public boolean revert() throws ConveyorException {
        if ( !reverted ) {
            PackageConfig packageConfig = packageDetails.getPackageConfig();
            Map<String, ActionConfig> actionConfigs = packageConfig.getActionConfigs();

            // Only remove it if it's still a match.
            if ( actionConfig.equals( actionConfigs.get( actionName ) ) )
                actionConfigs.remove( actionName );

            // Then remove any aliases
            if ( aliases != null ) {
                for ( String alias : aliases ) {
                    packageConfig.getActionConfigs().remove( alias );
                }
                aliases.clear();
            }

            // Then remove ourselves from the PackageDetails.
            packageDetails.removeAction( this );

            reverted = true;
            return true;
        }
        return false;
    }

    public Plugin getPlugin() {
        if ( actionConfig instanceof PluginAwareActionConfig ) {
            return ( (PluginAwareActionConfig) actionConfig ).getPlugin();
        }
        return null;
    }

    public void addAlias( String actionName ) {
        if ( aliases == null )
            aliases = new HashSet<String>();
        aliases.add( actionName );
    }
}
