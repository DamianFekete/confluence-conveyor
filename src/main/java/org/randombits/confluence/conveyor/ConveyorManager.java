package org.randombits.confluence.conveyor;

import org.randombits.confluence.conveyor.xwork.ConveyorConfigurationProvider;

import java.util.Collection;

/**
 * Provides access to the list of conveyor packages that are installed on the system.
 */
public interface ConveyorManager {
    /**
     * Adds the specified array of providers to the list for this plugin.
     *
     * @param providers The list of providers to add.
     */
    void addProviders( ConveyorConfigurationProvider... providers );

    /**
     * Adds the specified array of providers to the list for this plugin.
     *
     * @param providers The list of providers to add.
     */
    void addProviders( Collection<ConveyorConfigurationProvider> providers );

    /**
     * Removes any registered providers in the list, and reloads the XWork stack.
     *
     * @param providers The list of providers to remove.
     */
    void removeProviders( ConveyorConfigurationProvider... providers );

    /**
     * Removes the specified array of providers from the list.
     *
     * @param providers The list of providers to add.
     */
    void removeProviders( Collection<ConveyorConfigurationProvider> providers );

    /**
     * @return The list of registered providers.
     */
    Collection<ConveyorConfigurationProvider> getProviders();

    /**
     * Reloads the registered providers into the XWork stack.
     */
    void reload();

    /**
     *
     */
    void enable();

    void disable();
}
