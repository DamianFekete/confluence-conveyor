package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.Condition;

import java.util.List;
import java.util.Map;

/**
 * Overrides an existing action configuration.
 * 
 * @author David Peterson
 */
public class OverridingActionConfig extends ConveyorActionConfig {

    private boolean inherited;

    private String key;

    private int weight;

    private Plugin plugin;

    private Condition condition;

    public OverridingActionConfig( String methodName, String className, Map parameters, Map results, List interceptors, List externalRefs, String packageName, Plugin plugin, boolean inherited, String key, int weight, Condition condition ) {
        super( methodName, className, parameters, results, interceptors, externalRefs, packageName, plugin );
        this.inherited = inherited;
        this.key = key;
        this.weight = weight;
        this.plugin = plugin;
        this.condition = condition;
    }

    public String getKey() {
        return key;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isInherited() {
        return inherited;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin( Plugin plugin ) {
        this.plugin = plugin;
    }

    public Condition getCondition() {
        return condition;
    }
}
