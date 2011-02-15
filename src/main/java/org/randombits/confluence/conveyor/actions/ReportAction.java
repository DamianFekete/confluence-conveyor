package org.randombits.confluence.conveyor.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import org.randombits.confluence.conveyor.ActionDetails;
import org.randombits.confluence.conveyor.OverrideManager;
import org.randombits.confluence.conveyor.PackageDetails;

import java.util.*;

/**
 * Reports on currently installed Conveyor Packages.
 */
public class ReportAction extends ConfluenceActionSupport {

    private OverrideManager overrideManager;

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    public Collection<PackageDetails> getPackages() {
        return sort( overrideManager.getPackages(), new Comparator<PackageDetails>() {
            public int compare( PackageDetails packageDetails, PackageDetails packageDetails1 ) {
                return packageDetails.getPackageConfig().getNamespace().compareTo( packageDetails1.getPackageConfig().getNamespace() );
            }
        } );
    }

    public Collection<ActionDetails> sortActions( Collection<ActionDetails> actions ) {
        return sort( actions, new Comparator<ActionDetails>() {
            public int compare( ActionDetails actionDetails, ActionDetails actionDetails1 ) {
                return actionDetails.getActionName().compareTo( actionDetails1.getActionName() );
            }
        } );
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
