package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.web.Condition;
import com.opensymphony.xwork.config.entities.ResultConfig;

import java.util.Map;

/**
 * Provides a definition for a result override.
 */
public class OverridingResultConfig extends ResultConfig {

    private Condition condition;

    public OverridingResultConfig( String name, Class clazz, Map params, Condition condition ) {
        super( name, clazz, params );
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition( Condition condition ) {
        this.condition = condition;
    }
}
