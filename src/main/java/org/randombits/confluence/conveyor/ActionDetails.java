package org.randombits.confluence.conveyor;

import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.entities.ActionConfig;

/**
 * Represents an XWork {@link com.opensymphony.xwork.Action} which has been
 * configured via Conveyor. It may be a new action, an overridden action
 * or an overriding action.
 *
 * {@see OverriddenActionDetails}
 * {@see OverridingActionDetails}
 */
public interface ActionDetails {

    /**
     * @return the package that the action exists in.
     */
    PackageDetails getPackageDetails();

    /**
     * @return the name this action has been assigned to.
     */
    String getActionName();

    /**
     * @return The action configuration.
     */
    ActionConfig getActionConfig();

    /**
     * Returns the plugin that the action was defined in. This will be <code>null</code>
     * for actions which were not defined in a plugin (ie. were defined in core Confluence).
     *
     * @return The plugin, if applicable.
     */
    Plugin getPlugin();

    /**
     * Checks if the action has been reverted.
     *
     * @return true if the action has been reverted.
     */
    boolean isReverted();

    /**
     * Reverts the action to its former state.
     * @return
     */
    boolean revert() throws ConveyorException;

    /**
     * Adds an action name as an alias of this action. The aliased action will be removed
     * when this action is reverted.
     *
     * @param actionName The alias.
     */
    void addAlias( String actionName );
}
