package org.randombits.confluence.conveyor.impl;

import com.opensymphony.xwork.config.entities.ActionConfig;
import org.randombits.confluence.conveyor.ConveyorException;

/**
 * The default implementation of {@link org.randombits.confluence.conveyor.ActionDetails}.
 */
public class DefaultOriginalActionDetails extends BaseActionDetails {

    private DefaultOverriddenActionDetails overriddenAction;

    public DefaultOriginalActionDetails( DefaultPackageDetails packageDetails, String actionName, ActionConfig actionConfig, DefaultOverriddenActionDetails overriddenAction ) {
        super( packageDetails, actionName, actionConfig );
        this.overriddenAction = overriddenAction;
    }

    /**
     * @return the action which is overriding this action, if this is the case.
     */
    public DefaultOverriddenActionDetails getOverriddenAction() {
        return overriddenAction;
    }

    /**
     * Sets the action that now overrides this action.
     *
     * @param overriddenAction The overridden action.
     */
    public void setOverriddenAction( DefaultOverriddenActionDetails overriddenAction ) {
        this.overriddenAction = overriddenAction;
    }

    @Override
    public boolean revert() throws ConveyorException {
        if ( overriddenAction != null )
            return overriddenAction.revert();
        else
            return super.revert();
    }
}
