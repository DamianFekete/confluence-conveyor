package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.web.Condition;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Overrides an existing action configuration.
 *
 * @author David Peterson
 */
public class OverridingActionConfig extends ConveyorActionConfig {

    private static final String INHERITED = "@inherited";

    private final boolean inherited;

    private final String key;

    private final Condition condition;

    private final int weight;

    public OverridingActionConfig( String methodName, String className, Map parameters, Map results, List interceptors, List externalRefs, String packageName, Plugin plugin, boolean inherited, String key, int weight, Condition condition ) {
        super( methodName, className, parameters, results, interceptors, externalRefs, packageName, plugin );
        this.inherited = inherited;
        this.weight = weight;
        this.condition = condition;

        this.key = StringUtils.isBlank( key ) ? getPlugin().getKey() : key;
    }

    /**
     * Returns the key, which is either set in the constructor, or
     * <code>[plugin key]:[class name]</code>
     *
     * @return The key.
     * @see #getPlugin()
     * @see #getClassName()
     */
    public String getKey() {
        return key;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isInherited() {
        return inherited;
    }

    public Condition getCondition() {
        return condition;
    }
}
