package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.ExternalReferenceResolver;
import com.opensymphony.xwork.config.entities.PackageConfig;

import java.util.List;

/**
 * Creates a package configuration which records the plugin which defined it.
 */
public class PluginAwarePackageConfig extends PackageConfig {

    private final Plugin plugin;

    public PluginAwarePackageConfig( Plugin plugin ) {
        this.plugin =  plugin;
    }

    public PluginAwarePackageConfig( String name, Plugin plugin ) {
        super( name );
        this.plugin = plugin;
    }

    public PluginAwarePackageConfig( String name, String namespace, boolean isAbstract, ExternalReferenceResolver externalRefResolver, Plugin plugin ) {
        super( name, namespace, isAbstract, externalRefResolver );
        this.plugin = plugin;
    }

    public PluginAwarePackageConfig( String name, String namespace, boolean isAbstract, ExternalReferenceResolver externalRefResolver, List parents, Plugin plugin ) {
        super( name, namespace, isAbstract, externalRefResolver, parents );
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
