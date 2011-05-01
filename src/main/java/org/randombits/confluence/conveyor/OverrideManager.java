package org.randombits.confluence.conveyor;

import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.xwork.OverriddenPackageConfig;
import org.randombits.confluence.conveyor.xwork.OverridingPackageConfig;

import java.util.Collection;

/**
 * Provides services for keeping track of packages and overrides.
 */
public interface OverrideManager {

    /**
     * Overrides an existing package with the same name and namespace as that provided,
     * adding the details in the provided {@link PackageConfig}. No changes should be
     * made to the {@link PackageConfig} after this point, as they will be ignored.
     * Callers should keep the returned {@link Receipt} to be able to reverse the changes.
     *
     * @param packageConfig The package configuration to override with.
     * @return the receipt, which allows rolling back of the override.
     * @throws ConveyorException if there is not a existing PackageConfig that matches the override,
     *                           or if there is some other issue.
     */
    Receipt overridePackage( OverridingPackageConfig packageConfig ) throws ConveyorException;

    /**
     * Creates a new package based on the provided {@link PackageConfig}
     *
     * @param packageConfig The package configuration to create.
     * @return The receipt to assist in removing the package later.
     * @throws ConveyorException if there is already an existing package with the provided details.
     */
    Receipt createPackage( PackageConfig packageConfig ) throws ConveyorException;

    /**
     * Returns a set of receipts in a batch. This is equivalent to, but more efficient than calling
     * {@link org.randombits.confluence.conveyor.Receipt#returnReceipt()} on each item individually
     * since it can batch the reload of the XWork runtime configuration.
     *
     * @param receipts The set of receipts to return.
     */
    void returnReceipts( Iterable<? extends Receipt> receipts );

    /**
     * @return all packages with overridden actions.
     */
    Collection<OverriddenPackageConfig> getOverriddenPackages();

    /**
     * Returns the provided package configuration. If it has not been created or overridden,
     * no PackageDetails will be found.
     *
     * @param packageName The package name.
     * @return The package details for the provided.
     */
    OverriddenPackageConfig getPackage( String packageName );

    /**
     * Returns the {@link ActionConfig} for the current Action context,
     * if one is present. Any action which was overridden, or is an overriding action
     * will have an associated ActionDetails instance.
     *
     * @return The action details, if available.
     */
    ActionConfig getCurrentActionConfig();

    /**
     * Returns the {@link Plugin} instance for the Conveyor plugin.
     *
     * @return The Plugin.
     */
    Plugin getConveyorPlugin();
}
