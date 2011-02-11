package org.randombits.confluence.conveyor.config;

import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides clean access to packages defined by a configuration.
 */
public class PackageDetails {
    private final PackageConfig packageConfig;

    private final boolean override;

    private final List<ActionDetails> actions;

    public PackageDetails( PackageConfig packageConfig, boolean override ) {
        this.packageConfig = packageConfig;
        this.override = override;
        actions = new ArrayList<ActionDetails>();
    }

    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    public boolean isOverride() {
        return override;
    }

    public void addAction( ActionDetails details ) {
        actions.add( details );
    }

    public void addAction( String name, ActionConfig actionConfig ) {
        addAction( new ActionDetails( this, name, actionConfig ) );
    }

    public List<ActionDetails> getActions() {
        return actions;
    }

    public void revert() {
        for ( ActionDetails action : actions ) {
            action.revert();
        }
    }
}
