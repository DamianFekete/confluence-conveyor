package org.randombits.confluence.conveyor;

import com.opensymphony.xwork.config.entities.PackageConfig;

import java.util.Collection;

/**
 * Provides services for keeping track of packages and overrides.
 */
public interface OverrideManager {

    /**
     * Returns all packages managed by Conveyor.
     *
     * @return The collection of packages
     */
    Collection<PackageDetails> getPackages();

    /**
     * Returns the provided package configuration. If no previous instance exists, a
     * new one will be created. The instance returned will be the same for subsequent
     * calls with the same instance of PackageConfig.
     *
     * @param packageConfig The package configuration.
     * @return The package details for the provided.
     * @throws ConveyorException if there is a problem retrieving the package for the specified configuration.
     */
    PackageDetails getPackage( PackageConfig packageConfig ) throws ConveyorException;

    /**
     * Overrides the provided package configuration. If <code>create</code> is <code>true</code>
     * a new instance will be created if necessary, otherwise <code>null</code> will be returned
     * if no prior calls have been made. The instance returned will be the same for subsequent
     * calls with the same instance of PackageConfig.
     *
     * @param packageConfig The package configuration.
     * @return The package details for the provided.
     * @throws ConveyorException if there is a problem retrieving the package for the specified configuration.
     */
    PackageDetails getPackage( PackageConfig packageConfig, boolean create ) throws ConveyorException;

    /**
     * Returns the {@link ActionDetails} for the current Action context,
     * if one is present. Any action which was overridden, or is an overriding action
     * will have an associated ActionDetails instance.
     *
     * @return The action details, if available.
     */
    ActionDetails getCurrentActionDetails();
}
