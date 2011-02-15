package org.randombits.confluence.conveyor;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.xwork.OverridingActionConfig;

import java.util.Collection;

/**
 * Provides details about an XWork Action package.
 */
public interface PackageDetails {

    PackageConfig getPackageConfig();

    /**
     * Attempts to override the provided action name in this package. If there is no existing
     * action with the provided name in the package, a ConveyorException is thrown. The returned
     * overriding action can be used to revert the action later.
     *
     *
     * @param name                   The name of the action to override.
     * @param overridingActionConfig The configuration to use for the overriding action.
     * @return The details for the overriding action.
     * @throws ConveyorException if there are any issues while overriding.
     */
    OverridingActionDetails overrideAction( String name, OverridingActionConfig overridingActionConfig ) throws ConveyorException;

    /**
     * Adds a new top-level action to the package. If an action already exists for that name,
     * a ConveyorException is thrown.
     *
     *
     * @param name The name of the new action.
     * @param actionConfig The new action configuration.
     * @return the details for the new action.
     * @throws ConveyorException if an action with that name already exists.
     */
    ActionDetails createAction( String name, ActionConfig actionConfig ) throws ConveyorException;

    /**
     * Returns a collection of all the actions registered for the package via Conveyor.
     *
     * @return the collection of actions.
     */
    Collection<ActionDetails> getActions();

    /**
     * Returns a collection of all overridden actions in this package.
     *
     * @return The set of overridden actions.
     */
    Collection<OverriddenActionDetails> getOverriddenActions();

    /**
     * Retrieves the specific action. Note that this only returns actions
     * which have been configured via Conveyor. Other actions in a package
     * which have not been configured are likely to exist, but will
     * not return a value from this method.
     *
     * @param name The name of the action.
     * @return The action details, if they exist.
     */
    ActionDetails getAction( String name );

    /**
     * Checks if the package has already been reverted. Once it ahs been reverted,
     * it should be considered disabled.
     *
     * @return <code>true</code> if the package has already been reverted.
     */
    boolean isReverted();
}
