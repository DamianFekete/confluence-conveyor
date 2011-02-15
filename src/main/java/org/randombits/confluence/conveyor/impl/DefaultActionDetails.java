package org.randombits.confluence.conveyor.impl;

import com.opensymphony.xwork.config.entities.ActionConfig;

/**
 * The default implementation of {@link org.randombits.confluence.conveyor.ActionDetails}.
 */
public class DefaultActionDetails extends BaseActionDetails {
    public DefaultActionDetails( DefaultPackageDetails packageDetails, String actionName, ActionConfig actionConfig ) {
        super( packageDetails, actionName, actionConfig );
    }
}
