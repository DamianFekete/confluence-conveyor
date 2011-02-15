package org.randombits.confluence.conveyor.impl;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.predicate.PluginPredicate;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.ActionDetails;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverrideManager;
import org.randombits.confluence.conveyor.PackageDetails;
import org.randombits.confluence.conveyor.xwork.ActionRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The default implementation of {@link OverrideManager}.
 */
public class DefaultOverrideManager implements OverrideManager {

    private Map<String, PackageDetails> packages;

    private final Plugin plugin;

    public DefaultOverrideManager( PluginAccessor pluginAccessor ) {
        this.plugin = findPlugin( pluginAccessor );
        packages = new HashMap<String, PackageDetails>();
    }

    public Plugin findPlugin( PluginAccessor pluginAccessor ) {
        for ( Plugin plugin : pluginAccessor.getPlugins( new PluginPredicate() {
            public boolean matches( Plugin plugin ) {
                try {
                    Class<?> loadedClass = plugin.loadClass( DefaultOverrideManager.class.getName(), null );
                    Class<?> myClass = DefaultOverrideManager.class;
                    return loadedClass == myClass;
                } catch ( ClassNotFoundException e ) {
                    return false;
                }
            }
        } ) ) {
            return plugin;
        }
        return null;
    }


    public Collection<PackageDetails> getPackages() {
        return Collections.unmodifiableCollection( packages.values() );
    }

    public PackageDetails getPackage( PackageConfig packageConfig ) {
        return getPackage( packageConfig, true );
    }

    public PackageDetails getPackage( PackageConfig packageConfig, boolean create ) {
        String key = packageConfig.getName();
        PackageDetails packageDetails = packages.get( key );
        if ( packageDetails == null ) {
            packageDetails = new DefaultPackageDetails( this, packageConfig );
            packages.put( key, packageDetails );
        }
        return packageDetails;
    }

    public ActionDetails getCurrentActionDetails() {
        if ( ActionContext.getContext() != null && ActionContext.getContext().getActionInvocation() != null &&
                ActionContext.getContext().getActionInvocation().getProxy() != null ) {
            ActionConfig config = ActionContext.getContext().getActionInvocation().getProxy().getConfig();
            try {
                ActionRequest request  = ActionRequest.parse( ActionContext.getContext().getName() );

                PackageDetails details = packages.get( config.getPackageName() );
                if ( details != null )
                    return details.getAction( request.getActionName() );
            } catch ( ConveyorException e ) {
                return null;
            }
        }

        return null;
    }

    /**
     * Removes the package details from the list.
     *
     * @param packageDetails The package details to remove.
     */
    void removePackage( PackageDetails packageDetails ) {
        packages.remove( packageDetails.getPackageConfig().getName() );
    }

    public Plugin getPlugin() {
        return plugin;
    }
}
