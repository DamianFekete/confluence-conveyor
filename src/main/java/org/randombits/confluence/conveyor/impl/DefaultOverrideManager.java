package org.randombits.confluence.conveyor.impl;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.predicate.PluginPredicate;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.config.Configuration;
import com.opensymphony.xwork.config.ConfigurationManager;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.apache.commons.lang.StringUtils;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverrideManager;
import org.randombits.confluence.conveyor.Receipt;
import org.randombits.confluence.conveyor.xwork.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * The default implementation of {@link OverrideManager}.
 */
public class DefaultOverrideManager implements OverrideManager, InitializingBean, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger( DefaultOverrideManager.class );

    private Map<String, OverriddenPackageConfig> overriddenPackages;

    private final Plugin plugin;

    private int returningReceipts;

    private List<Receipt> receipts;

    public DefaultOverrideManager( PluginAccessor pluginAccessor ) {
        this.plugin = findPlugin( pluginAccessor );
        overriddenPackages = new HashMap<String, OverriddenPackageConfig>();
        receipts = new ArrayList<Receipt>();
    }

    private Plugin findPlugin( PluginAccessor pluginAccessor ) {
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

    public OverridePackageReceipt overridePackage( OverridingPackageConfig overridingPackage ) throws ConveyorException {

        String name = overridingPackage.getName();
        String namespace = overridingPackage.getNamespace();

        LOG.debug( "Overriding '" + name + "' package with '" + namespace + "' namespace." );

        OverriddenPackageConfig overriddenPackage = asOverriddenPackageConfig( name, namespace );

        // Next, add the override package.
        addOverridingPackage( overriddenPackage, overridingPackage );

        // Lastly, we make sure the overridden package is in the current configuration.
        // It's done here so to avoid having to rollback if there was an error earlier.
        ConfigurationManager.getConfiguration().addPackageConfig( name, overriddenPackage );
        overriddenPackages.put( name, overriddenPackage );
        rebuildRuntimeConfiguration();

        return keepReceipt( new OverridePackageReceipt( overriddenPackage, overridingPackage, this ) );
    }

    private void addOverridingPackage( OverriddenPackageConfig overriddenPackage, OverridingPackageConfig overridingPackage ) throws ConveyorException {
        String name = overriddenPackage.getName();
        String namespace = overriddenPackage.getNamespace();

        try {
            if ( LOG.isDebugEnabled() )
                LOG.debug( "Attempting to override the '" + name + "' package." );

            // First, we check the packages match.
            if ( !StringUtils.equals( name, overridingPackage.getName() ) )
                throw new ConveyorException( "Cannot override a package named '" + name + "' with an override package named '" + overridingPackage.getName() + "'." );
            if ( !StringUtils.equals( namespace, overridingPackage.getNamespace() ) )
                throw new ConveyorException( "The '" + name + "' package has a namespace of '" + namespace
                        + "' an cannot be overridden with a package with a different namespace of '" + overridingPackage.getNamespace() );
            // Check that my plugin is a dependency of the overriding package's plugin
            String myPluginKey = getPackagePluginKey( overriddenPackage.getOriginalPackage() );
            if ( myPluginKey != null && !overridingPackage.getPlugin().getRequiredPlugins().contains( myPluginKey ) )
                throw new ConveyorException( "The overriding plugin (" + overridingPackage.getPlugin().getKey()
                        + ") must depend on the original plugin (" + myPluginKey + ") to be allowed to override its packages." );

            // Next, we copy the details, if possible.
            copyPackageDetails( overridingPackage, overriddenPackage );

            overrideActions( overriddenPackage, overridingPackage.getOverridingActionsMap() );

            overriddenPackage.getOverridingPackageConfigs().add( overridingPackage );

            if ( LOG.isDebugEnabled() )
                LOG.debug( "Completed overriding package '" + name + "' with namespace of '" + namespace + "'." );

        } catch ( ConveyorException e ) {
            if ( LOG.isDebugEnabled() )
                LOG.debug( "There was a problem while overriding the '" + name + "' package, reverting: " + e.getMessage() );
            // Revert if there is a failure
            revertOverridingPackage( overriddenPackage, overridingPackage );
            throw e;
        }

    }

    private void overrideActions( OverriddenPackageConfig overriddenPackage, Map<String, List<OverridingActionConfig>> overridingActionsMap ) throws ConveyorException {
        for ( Map.Entry<String, List<OverridingActionConfig>> entry : overridingActionsMap.entrySet() ) {

            String actionName = entry.getKey();

            if ( LOG.isDebugEnabled() )
                LOG.debug( "Attempting to override the '" + actionName + "' action in the '" + overriddenPackage.getName() + "' package." );

            OverriddenActionConfig overriddenAction = asOverriddenActionConfig( overriddenPackage, actionName );

            // Now, initialise the overriding actions and hook them up.
            Collection<OverridingActionLink> overridingActionLinks = overriddenAction.getOverridingActions();

            for ( OverridingActionConfig overridingAction : entry.getValue() ) {
                if ( hasExistingKey( overridingAction, overridingActionLinks ) )
                    throw new ConveyorException( "An action-override with the key of '" + overridingAction.getKey()
                            + "' already exists for the '" + actionName + "' in the '" + overriddenPackage.getName()
                            + "' package. Please select a unique key." );
                String originalPluginKey = getActionPluginKey( overriddenAction.getOriginalActionConfig() );
                if ( overridingAction.getPlugin().getRequiredPlugins().contains( originalPluginKey ) )
                    throw new ConveyorException( "The '" + overridingAction.getKey() + "' in the '" + overridingAction.getPackageName()
                            + "' package cannot override the '" + actionName + "' action because the plugin must depend on the '"
                            + originalPluginKey + "' plugin." );

                String alias = findActionAlias( overriddenPackage, actionName );

                OverridingActionLink actionLink = new OverridingActionLink( alias, overridingAction );
                overridingActionLinks.add( actionLink );

                // TODO: Load the admin-specified weight, if present.

                // Add copy of the action as a transient
                TransientActionConfig copiedAction = null;
                // Apply the original config if inherited.
                if ( overridingAction.isInherited() ) {
                    copiedAction = new TransientActionConfig( overriddenAction.getOriginalActionConfig(), overridingAction );
                } else {
                    copiedAction = new TransientActionConfig( overridingAction );
                }

                // Add it to the main config via the alias.
                overriddenPackage.addActionConfig( alias, copiedAction );
            }

            overriddenAction.sortOverridingActions();

            if ( LOG.isDebugEnabled() )
                LOG.debug( "Completed overriding the '" + actionName + "' action in the '" + overriddenPackage.getName() + "' package." );
        }
    }

    private static String findActionAlias( PackageConfig packageConfig, String actionName ) {
        Map<String, ActionConfig> actionConfigs = packageConfig.getActionConfigs();

        int i = 1;
        while ( actionConfigs.containsKey( getAlias( actionName, i ) ) )
            i++;

        return getAlias( actionName, i );
    }


    private static String getAlias( String actionName, int i ) {
        return actionName + "_" + i;
    }

    private boolean hasExistingKey( OverridingActionConfig overridingAction, Collection<OverridingActionLink> overridingActionLinks ) {
        for ( OverridingActionLink link : overridingActionLinks ) {
            if ( StringUtils.equals( link.getActionConfig().getKey(), overridingAction.getKey() ) )
                return true;
        }
        return false;
    }

    private OverriddenActionConfig asOverriddenActionConfig( OverriddenPackageConfig overriddenPackage, String actionName ) throws ConveyorException {
        OverriddenActionConfig overriddenAction;
        ActionConfig originalAction = overriddenPackage.getActionConfig( actionName );

        // Find the overridden action, or fail if there is a problem.
        if ( originalAction instanceof OverriddenActionConfig ) {
            overriddenAction = (OverriddenActionConfig) originalAction;
        } else if ( originalAction instanceof TransientActionConfig ) {
            throw new ConveyorException( "The action named '" + actionName + "' in the '"
                    + overriddenPackage.getName()
                    + "' package cannot be overridden because it's a temporary action." );
        } else if ( originalAction != null ) {
            // convert it to an OverriddenActionConfig.
            String alias = findActionAlias( overriddenPackage, actionName );
            overriddenAction = new OverriddenActionConfig( getConveyorPlugin(), alias, originalAction );
            // Clone the original and add it with the alias
            TransientActionConfig cloneAction = new TransientActionConfig( originalAction, getConveyorPlugin() );
            overriddenPackage.addActionConfig( alias, cloneAction );
            // Replace the original
            overriddenPackage.addActionConfig( actionName, overriddenAction );

            // Add it to the list of overridden actions.
            overriddenPackage.addOverriddenAction( actionName, overriddenAction );
        } else {
            throw new ConveyorException( "No action named '" + actionName + "' is present in the '" + overriddenPackage.getName() + "' package to override." );
        }

        return overriddenAction;
    }


    private void copyPackageDetails( PackageConfig sourcePackage, PackageConfig targetPackage ) throws ConveyorException {
        try {
            String name = targetPackage.getName();
            copyMapValues( sourcePackage.getResultTypeConfigs(), targetPackage.getResultTypeConfigs(), "Result Type Config", name );
            copyMapValues( sourcePackage.getInterceptorConfigs(), targetPackage.getInterceptorConfigs(), "Interceptor Config", name );
            copyMapValues( sourcePackage.getGlobalResultConfigs(), targetPackage.getGlobalResultConfigs(), "Global Result Config", name );
            copyMapValues( sourcePackage.getActionConfigs(), targetPackage.getActionConfigs(), "Action Config", name );
        } catch ( ConveyorException e ) {
            revertPackage( sourcePackage, targetPackage );
            throw e;
        }
    }


    public void revertOverridingPackage( OverriddenPackageConfig overriddenPackage, OverridingPackageConfig overridingPackage ) {
        revertActions( overriddenPackage, overridingPackage.getOverridingActionsMap() );
        revertPackage( overriddenPackage, overridingPackage );
        overriddenPackage.getOverridingPackageConfigs().remove( overridingPackage );

        if ( overriddenPackage.getOverridingPackageConfigs().isEmpty() ) {
            // Revert to the original package
            ConfigurationManager.getConfiguration().addPackageConfig( overriddenPackage.getName(), overriddenPackage.getOriginalPackage() );
            overriddenPackages.remove( overriddenPackage.getName() );
        }
    }

    private void revertActions( OverriddenPackageConfig overriddenPackage, Map<String, List<OverridingActionConfig>> overridingActionsMap ) {
        for ( String actionName : overridingActionsMap.keySet() ) {
            ActionConfig targetAction = overriddenPackage.getActionConfig( actionName );
            if ( targetAction instanceof OverriddenActionConfig ) {
                OverriddenActionConfig overriddenAction = (OverriddenActionConfig) targetAction;

                // Remove any matching overriding actions
                List<OverridingActionConfig> overridingActions = overridingActionsMap.get( actionName );
                for ( OverridingActionConfig overridingAction : overridingActions ) {
                    // Loop through the existing links.
                    Iterator<OverridingActionLink> i = overriddenAction.getOverridingActions().iterator();
                    while ( i.hasNext() ) {
                        OverridingActionLink link = i.next();
                        if ( link.getKey().equals( overridingAction.getKey() ) ) {
                            // Remove the aliased link
                            removeActionConfig( overriddenPackage, link.getAlias() );
                            i.remove();
                        }
                    }
                }
                // Now check if we should to back to the original action
                if ( overriddenAction.getOverridingActions().isEmpty() ) {
                    removeActionConfig( overriddenPackage, overriddenAction.getOriginalActionAlias() );
                    removeActionConfig( overriddenPackage, actionName );
                    overriddenPackage.getOverriddenActionConfigs().remove( actionName );
                    overriddenPackage.addActionConfig( actionName, overriddenAction.getOriginalActionConfig() );
                }
            }
        }
    }

    private void removeActionConfig( PackageConfig packageConfig, String actionName ) {
        packageConfig.getActionConfigs().remove( actionName );
        if ( packageConfig instanceof OverriddenPackageConfig ) {
            removeActionAliases( (OverriddenPackageConfig) packageConfig, actionName );
        }
    }

    private void removeActionAliases( OverriddenPackageConfig overriddenPackage, String actionName ) {
        Set<String> aliasSet = overriddenPackage.getAliases().get( actionName );
        if ( aliasSet != null ) {
            for ( String alias : aliasSet ) {
                removeActionConfig( overriddenPackage, alias );
            }
        }
    }

    private void revertPackage( PackageConfig sourcePackage, PackageConfig targetPackage ) {
        removeMapValues( sourcePackage.getResultTypeConfigs(), targetPackage.getResultTypeConfigs() );
        removeMapValues( sourcePackage.getInterceptorConfigs(), targetPackage.getInterceptorConfigs() );
        removeMapValues( sourcePackage.getGlobalResultConfigs(), targetPackage.getGlobalResultConfigs() );

        Map<String, ActionConfig> actionConfigs = sourcePackage.getActionConfigs();
        for ( Map.Entry<String, ActionConfig> entry : actionConfigs.entrySet() ) {
            removeActionConfig( targetPackage, entry.getKey() );
        }
    }

    /**
     * Copies the contents of the <code>source</code> map into the <code>target</code> map.
     * If an entry with the same name already exists in the target map, a {@link ConveyorException}
     * is thrown and no change is made to <code>target</code>
     *
     * @param source The source map.
     * @param target The target map.
     * @param type   The human-readable type name for the error message.
     * @param <K>    The key type.
     * @param <V>    The value type.
     * @throws ConveyorException if there is a duplicate.
     */
    protected <K, V> void copyMapValues( Map<K, V> source, Map<K, V> target, String type, String packageName ) throws ConveyorException {
        for ( K key : source.keySet() ) {
            if ( target.containsKey( key ) )
                throw new ConveyorException( type + " named '" + key
                        + " already exists in the '" + packageName
                        + "' package. Please select more unique name." );
        }

        for ( Map.Entry<K, V> entry : source.entrySet() ) {
            target.put( entry.getKey(), entry.getValue() );
        }
    }

    /**
     * Removes items with the same key and value in the <code>source</code> from the <code>target</code>.
     *
     * @param source The source map.
     * @param target The target map.
     * @param <K>    The key type.
     * @param <V>    The value type.
     */
    protected <K, V> void removeMapValues( Map<K, V> source, Map<K, V> target ) {
        if ( source != null && target != null ) {
            for ( Map.Entry<K, V> entry : source.entrySet() ) {
                if ( target.get( entry.getKey() ) == entry.getValue() ) {
                    source.remove( entry.getKey() );
                }
            }
        }
    }


    private String getPackagePluginKey( PackageConfig packageConfig ) {
        if ( packageConfig instanceof PluginAwarePackageConfig ) {
            Plugin plugin = ( (PluginAwarePackageConfig) packageConfig ).getPlugin();
            if ( plugin != null )
                return plugin.getKey();
        }
        return null;
    }

    private String getActionPluginKey( ActionConfig actionConfig ) {
        if ( actionConfig instanceof PluginAwareActionConfig ) {
            Plugin plugin = ( (PluginAwareActionConfig) actionConfig ).getPlugin();
            if ( plugin != null )
                return plugin.getKey();
        }
        return null;
    }

    /**
     * Returns any existing {@link OverriddenPackageConfig} if already defined, or converts a package to an override.
     * it does not update the configuration with the OverriddenPackageConfig if a new one was created.
     *
     * @param name      The name of the package.
     * @param namespace The namespace it must have.
     * @return The package as an {@link OverriddenPackageConfig}.
     * @throws ConveyorException if there was a problem converting the existing package, or none exists.
     */
    private OverriddenPackageConfig asOverriddenPackageConfig( String name, String namespace ) throws ConveyorException {
        OverriddenPackageConfig overriddenPackage = null;
        PackageConfig currentPackage = ConfigurationManager.getConfiguration().getPackageConfig( name );

        if ( currentPackage == null ) {
            throw new ConveyorException( "Unable to locate the '" + name + "' package to override." );
        } else if ( currentPackage instanceof OverriddenPackageConfig ) {
            LOG.debug( "Existing OverriddenPackageConfig for '" + name + "' package found." );
            overriddenPackage = (OverriddenPackageConfig) currentPackage;
        } else {
            if ( !StringUtils.equals( currentPackage.getNamespace(), namespace ) ) {
                throw new ConveyorException( "Requested an override of the '" + name + "' with a namespace of '"
                        + namespace + "', but the current package has a namespace of '"
                        + currentPackage.getNamespace() + "'." );
            }

            LOG.debug( "Creating a new OverriddenPackageConfig for '" + name + "' package." );

            // Convert it.
            overriddenPackage = new OverriddenPackageConfig( currentPackage );
            copyPackageDetails( currentPackage, overriddenPackage );
        }
        return overriddenPackage;
    }

    public CreatePackageReceipt createPackage( PackageConfig packageConfig ) throws ConveyorException {
        String name = packageConfig.getName();
        Configuration configuration = ConfigurationManager.getConfiguration();

        if ( configuration.getPackageConfig( name ) != null )
            throw new ConveyorException( "Unable to add a package named '" + name + "' because one already exists with the same name." );

        configuration.addPackageConfig( name, packageConfig );

        return keepReceipt( new CreatePackageReceipt( name, this ) );
    }

    private <T extends Receipt> T keepReceipt( T receipt ) {
        receipts.add( receipt );
        return receipt;
    }

    public Collection<OverriddenPackageConfig> getOverriddenPackages() {
        return Collections.unmodifiableCollection( overriddenPackages.values() );
    }

    public OverriddenPackageConfig getPackage( String packageName ) {
        return overriddenPackages.get( packageName );
    }

    /**
     * Retrieves the 'real' {@link ActionConfig) for the current context. This is different
     * to what is available in via the {@link com.opensymphony.xwork.ActionContext#getContext()}
     * method because that come from the 'runtime' configuration which has been generated separately.
     *
     * @return The original {@link ActionConfig} for the current context.
     */
    public ActionConfig getCurrentActionConfig() {
        if ( ActionContext.getContext() != null && ActionContext.getContext().getActionInvocation() != null &&
                ActionContext.getContext().getActionInvocation().getProxy() != null ) {
            ActionConfig currentAction = ActionContext.getContext().getActionInvocation().getProxy().getConfig();
            ActionRequest request = null;
            try {
                request = ActionRequest.parse( ActionContext.getContext().getName() );

                PackageConfig packageConfig = ConfigurationManager.getConfiguration().getPackageConfig( currentAction.getPackageName() );
                if ( packageConfig != null )
                    return (ActionConfig) packageConfig.getActionConfigs().get( request.getActionName() );
            } catch ( ConveyorException e ) {

            }
        }

        return null;
    }

    /**
     * @return the {@link Plugin} instance for Conveyor.
     */
    public Plugin getConveyorPlugin() {
        return plugin;
    }

    /**
     * Returns a batch of {@link Receipt}s at once, which makes cleaning up the
     * XWork runtime configuration more efficient.
     *
     * @param receipts The collection of receipts to return.
     * @throws ConveyorException
     */
    public synchronized void returnReceipts( Iterable<? extends Receipt> receipts ) {
        try {
            returningReceipts++;
            for ( Receipt receipt : receipts ) {
                receipt.returnReceipt();
            }
        } finally {
            returningReceipts--;
            rebuildRuntimeConfiguration();
        }
    }

    public void rebuildRuntimeConfiguration() {
        if ( returningReceipts == 0 )
            ConfigurationManager.getConfiguration().rebuildRuntimeConfiguration();
    }

    public void destroy() throws Exception {
        for ( Receipt receipt : receipts ) {
            receipt.returnReceipt();
        }
        rebuildRuntimeConfiguration();
    }

    public void afterPropertiesSet() throws Exception {
        // Nothing to do.
    }
}
