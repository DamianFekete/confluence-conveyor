package org.randombits.confluence.conveyor.xwork;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.ConveyorException;

import java.util.*;

/**
 * Represents a {@link PackageConfig} which has been overridden by at least
 * one {@link OverridingPackageConfig}. Overriding packages can define
 * new
 */
public class OverriddenPackageConfig extends PackageConfig {

    private final PackageConfig originalPackage;

    private final Map<String, Set<String>> aliases;

    private Set<OverridingPackageConfig> overridingPackageConfigs;

    private Map<String, OverriddenActionConfig> overriddenActionConfigs;

    public OverriddenPackageConfig( PackageConfig originalPackage ) throws ConveyorException {
        super( originalPackage.getName(), originalPackage.getNamespace(), originalPackage.isAbstract(), originalPackage.getExternalRefResolver(), originalPackage.getParents() );
        this.originalPackage = originalPackage;
        setDefaultResultType( originalPackage.getDefaultResultType() );
        setDefaultInterceptorRef( originalPackage.getDefaultInterceptorRef() );

        overridingPackageConfigs = new HashSet<OverridingPackageConfig>();
        overriddenActionConfigs = new HashMap<String, OverriddenActionConfig>();

        aliases = new HashMap<String, Set<String>>();
    }

    public PackageConfig getOriginalPackage() {
        return originalPackage;
    }

    /**
     * Returns the map of {@link OverriddenActionConfig}s to their names.
     *
     * @return
     */
    public Map<String, OverriddenActionConfig> getOverriddenActionConfigs() {
        return overriddenActionConfigs;
    }

    /**
     * Aliases are alternate versions of action names that include '@override', '*override'
     * and/or '!method' parts. These are cached with the full value, but plugged-in actions
     * need to also remove their aliases when removed.
     *
     * @param actionName
     * @param aliasName
     */
    public void addAlias( String actionName, String aliasName ) {
        Set<String> actionAliases = aliases.get( actionName );
        if ( actionAliases == null ) {
            actionAliases = new HashSet<String>();
            aliases.put( actionName, actionAliases );
        }
        actionAliases.add( aliasName );
    }

    public Map<String, Set<String>> getAliases() {
        return aliases;
    }

    /**
     * Convenience method for getting the named action from {@link #getActionConfigs()}.
     *
     * @param name The name of the action.
     * @return The action config, or <code>null</code> if none is set.
     */
    public ActionConfig getActionConfig( String name ) {
        return (ActionConfig) getActionConfigs().get( name );
    }

    public void addOverriddenAction( String actionName, OverriddenActionConfig overriddenAction ) {
        overriddenActionConfigs.put( actionName, overriddenAction );
    }

    public Set<OverridingPackageConfig> getOverridingPackageConfigs() {
        return overridingPackageConfigs;
    }

    public void setOverridingPackageConfigs( Set<OverridingPackageConfig> overridingPackageConfigs ) {
        this.overridingPackageConfigs = overridingPackageConfigs;
    }
}
