package org.randombits.confluence.conveyor.impl;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.*;
import org.randombits.confluence.conveyor.xwork.OverridingActionConfig;

import java.util.*;

/**
 * Provides clean access to packages defined by a configuration.
 */
public class DefaultPackageDetails implements PackageDetails {

    private final DefaultOverrideManager overrideManager;

    private final PackageConfig packageConfig;

    private final Map<String, ActionDetails> actions;

    private final Map<String, OverriddenActionDetails> overriddenActions;

    private boolean reverted;

    public DefaultPackageDetails( DefaultOverrideManager overrideManager, PackageConfig packageConfig ) {
        this.overrideManager = overrideManager;
        this.packageConfig = packageConfig;

        actions = new HashMap<String, ActionDetails>();
        overriddenActions = new HashMap<String, OverriddenActionDetails>();
    }

    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    /**
     * Attempts to override the provided action name in this package. If there is no existing
     * action with the provided name in the package, a ConveyorException is thrown. The returned
     * overriding action can be used to revert the action later.
     *
     * @param name                   The name of the action to override.
     * @param overridingActionConfig The configuration to use for the overriding action.
     * @return The details for the overriding action.
     * @throws org.randombits.confluence.conveyor.ConveyorException
     *          if there are any issues while overriding.
     */
    public OverridingActionDetails overrideAction( String name, OverridingActionConfig overridingActionConfig ) throws ConveyorException {
        checkReverted();

        DefaultOverriddenActionDetails overriddenAction = getOverriddenAction( name );

        // Now, add the action to the overridden action.
        return overriddenAction.addOverridingAction( overridingActionConfig );
    }

    /**
     * Either overrides, or returns the existing overridden action, or throws a ConveyorException.
     * The result will never be null.
     *
     * @param name The name of the action.
     * @return
     * @throws ConveyorException
     */
    private DefaultOverriddenActionDetails getOverriddenAction( String name ) throws ConveyorException {
        ActionDetails existingAction = actions.get( name );
        DefaultOverriddenActionDetails overriddenAction;

        if ( existingAction instanceof DefaultOverriddenActionDetails ) {
            // It has already been converted
            overriddenAction = (DefaultOverriddenActionDetails) existingAction;
        } else if ( existingAction == null ) {
            // Try to find the existing action in the PackageConfig hierarchy.
            overriddenAction = overrideUnknownAction( name );
        } else {
            throw new ConveyorException( "Overriding is not supported for the '" + name + "': " + existingAction.getActionConfig().getClassName() );
        }
        return overriddenAction;
    }

    private DefaultOverriddenActionDetails overrideUnknownAction( String name ) throws ConveyorException {
        DefaultOverriddenActionDetails details = overrideUnknownAction( packageConfig, name );
        if ( details == null )
            throw new ConveyorException( "No existing action was found to override: " + name );
        return details;
    }

    private DefaultOverriddenActionDetails overrideUnknownAction( PackageConfig packageConfig, String name ) throws ConveyorException {
        ActionConfig originalAction = (ActionConfig) packageConfig.getActionConfigs().get( name );
        if ( originalAction != null ) {
            DefaultOverriddenActionDetails overriddenAction = new DefaultOverriddenActionDetails( this, name, originalAction, overrideManager.getPlugin() );
            actions.put( name, overriddenAction );
            overriddenActions.put( name, overriddenAction );
            return overriddenAction;
        } else {

            PackageConfig realConfig = findPackageConfig( packageConfig, name );
            if ( realConfig != null ) {
                PackageDetails realDetails = overrideManager.getPackage( realConfig );
                if ( realDetails instanceof DefaultPackageDetails ) {
                    DefaultPackageDetails details = (DefaultPackageDetails) realDetails;
                    return details.getOverriddenAction( name );
                }
            }
        }

        return null;
    }

    private PackageConfig findPackageConfig( PackageConfig packageConfig, String name ) {
        return findPackageConfig( packageConfig, name, new HashSet<PackageConfig>() );
    }

    private PackageConfig findPackageConfig( PackageConfig packageConfig, String name, Set<PackageConfig> searched ) {
        if ( !searched.contains( packageConfig ) )
            return null;
        else
            searched.add( packageConfig );

        ActionConfig oldAction = (ActionConfig) packageConfig.getActionConfigs().get( name );
        if ( oldAction != null )
            return packageConfig;

        List<PackageConfig> parents = packageConfig.getParents();
        if ( parents != null ) {
            for ( PackageConfig config : parents ) {
                packageConfig = findPackageConfig( config, name, searched );
                if ( packageConfig != null )
                    return packageConfig;
            }
        }
        return null;
    }

    /**
     * Adds a new top-level action to the package. If an action already exists for that name,
     * a ConveyorException is thrown.
     *
     * @param name         The name of the action.
     * @param actionConfig The new action configuration.
     * @throws org.randombits.confluence.conveyor.ConveyorException
     *          if an action with that name already exists.
     */
    public ActionDetails createAction( String name, ActionConfig actionConfig ) throws ConveyorException {
        checkReverted();

        ActionConfig existingAction = (ActionConfig) packageConfig.getAllActionConfigs().get( name );
        if ( existingAction != null ) {
            throw new ConveyorException( "An action already exists with the name '" + name + "': " + existingAction.getClassName() );
        }

        // Add the action to the package
        packageConfig.addActionConfig( name, actionConfig );

        // Create the action details.
        DefaultActionDetails actionDetails = new DefaultActionDetails( this, name, actionConfig );
        actions.put( name, actionDetails );

        return actionDetails;
    }

    private void checkReverted() throws ConveyorException {
        if ( isReverted() )
            throw new ConveyorException( "This package has been reverted and can no longer be modified." );
    }

    /**
     * Removes the specified action. from this package.
     * Does not perform any actual cleanup of the {@link PackageConfig}, etc, however
     * it will 'revert' itself once it runs out of actions.
     *
     * @param actionDetails The action to remove.
     */

    protected void removeAction( ActionDetails actionDetails ) throws ConveyorException {
        actions.remove( actionDetails.getActionName() );
        if ( actions.isEmpty() ) {
            revert();
        }
    }

    public Collection<ActionDetails> getActions() {
        return actions.values();
    }

    public Collection<OverriddenActionDetails> getOverriddenActions() {
        return Collections.unmodifiableCollection( overriddenActions.values() );
    }

    public ActionDetails getAction( String name ) {
        return actions.get( name );
    }

    /**
     * Checks if the package has already been reverted.
     *
     * @return <code>true</code> if the package has already been reverted.
     */
    public boolean isReverted() {
        return reverted;
    }

    protected boolean revert() throws ConveyorException {
        if ( !reverted ) {
            for ( ActionDetails action : actions.values() ) {
                if ( !action.isReverted() )
                    action.revert();
            }
            reverted = true;

            overrideManager.removePackage( this );
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "{package: " + packageConfig + "}";
    }
}
