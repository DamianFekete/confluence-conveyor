package org.randombits.confluence.conveyor.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import org.randombits.confluence.conveyor.OverrideManager;
import org.randombits.confluence.conveyor.xwork.OverriddenActionConfig;
import org.randombits.confluence.conveyor.xwork.OverriddenPackageConfig;

import java.util.*;

/**
 * Reports on currently installed Conveyor Packages.
 */
public class ReportAction extends ConfluenceActionSupport {

    private OverrideManager overrideManager;

    private Collection<OverriddenPackageConfig> packages;

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    public Collection<OverriddenPackageConfig> getPackages() {
        if ( packages == null ) {
            packages = sort( overrideManager.getOverriddenPackages(), new Comparator<OverriddenPackageConfig>() {
                public int compare( OverriddenPackageConfig overriddenPackageConfig, OverriddenPackageConfig overriddenPackageConfig1 ) {
                    return overriddenPackageConfig.getNamespace().compareTo( overriddenPackageConfig1.getNamespace() );
                }
            } );
        }
        return packages;
    }

    public Collection<String> getActionNames( OverriddenPackageConfig overriddenPackage ) {
        return sort( overriddenPackage.getOverriddenActionConfigs().keySet(), new Comparator<String>() {
            public int compare( String s1, String s2 ) {
                return s1.compareTo( s2 );
            }
        } );
    }

    public OverriddenActionConfig getActionConfig( OverriddenPackageConfig overriddenPackage, String name ) {
        return overriddenPackage.getOverriddenActionConfigs().get( name );
    }

    private <T> List<T> sort( Collection<T> values, Comparator<T> comparator ) {
        ArrayList<T> sorted = new ArrayList<T>( values );
        Collections.sort( sorted, comparator );
        return sorted;
    }

    public void setOverrideManager( OverrideManager overrideManager ) {
        this.overrideManager = overrideManager;
    }
}
