package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.Plugin;

import java.util.Comparator;

/**
 * Creates a link between the alias and config for an {@link OverridingActionConfig}. It also provides
 * a location to store the admin-specified weight.
 */
public class OverridingActionLink {

    private final String alias;

    private final OverridingActionConfig actionConfig;

    private int weight;

    public static final Comparator<? super OverridingActionLink> WEIGHT_COMPARATOR = new Comparator<OverridingActionLink>() {
        public int compare( OverridingActionLink actionKey1, OverridingActionLink actionKey2 ) {
            // We sort with highest weight at the top.
            return actionKey2.getWeight() - actionKey1.getWeight();
        }
    };

    public OverridingActionLink( String alias, OverridingActionConfig actionConfig ) {
        this.alias = alias;
        this.actionConfig = actionConfig;

        this.weight = actionConfig.getWeight();
    }

    public String getKey() {
        return actionConfig.getKey();
    }

    public String getAlias() {
        return alias;
    }

    public OverridingActionConfig getActionConfig() {
        return actionConfig;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight( int weight ) {
        this.weight = weight;
    }

    public Plugin getPlugin() {
        return actionConfig.getPlugin();
    }
}
