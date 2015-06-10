package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.ExternalReferenceResolver;
import com.opensymphony.xwork.config.entities.PackageConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extension of {@link PackageConfig} that represents a &lt;package-override&gt;.
 */
public class OverridingPackageConfig extends PluginAwarePackageConfig {

    private Map<String, List<OverridingActionConfig>> overridingActionsMap;

    public OverridingPackageConfig( Plugin plugin ) {
        super( plugin );
        init();
    }

    public OverridingPackageConfig( String name, Plugin plugin ) {
        super( name, plugin );
        init();
    }

    public OverridingPackageConfig( String name, String namespace, ExternalReferenceResolver externalRefResolver, List parents, Plugin plugin ) {
        super( name, namespace, true, externalRefResolver, parents, plugin );
        init();
    }

    private void init() {
        overridingActionsMap = new HashMap<String, List<OverridingActionConfig>>();
    }

    public Map<String, List<OverridingActionConfig>> getOverridingActionsMap() {
        return overridingActionsMap;
    }

    public void setOverridingActionsMap( Map<String, List<OverridingActionConfig>> overridingActionsMap ) {
        this.overridingActionsMap = overridingActionsMap;
    }

    /**
     * Adds an override configuration for the specified action name.
     *
     * @param overriddenActionName The original action name being overridden.
     * @param overridingAction The overriding action.
     */
    public void addOverridingAction( String overriddenActionName, OverridingActionConfig overridingAction ) {
        List<OverridingActionConfig> otherActions = overridingActionsMap.get( overriddenActionName );
        if ( otherActions == null ) {
            otherActions = new ArrayList<OverridingActionConfig>();
            overridingActionsMap.put( overriddenActionName, otherActions );
        }
        otherActions.add( overridingAction );
    }

    /**
     * Returns the set of {@link OverridingActionConfig} instances for the specified action name.
     * @param overriddenActionName
     * @return
     */
    public List<OverridingActionConfig> getOverridingActions( String overriddenActionName ) {
        return overridingActionsMap.get( overriddenActionName );
    }

    @Override
    public boolean equals( Object o ) {
        return o == this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode( this );
    }
}
