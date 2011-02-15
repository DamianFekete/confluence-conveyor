package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import org.randombits.confluence.conveyor.ActionDetails;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverriddenActionDetails;
import org.randombits.confluence.conveyor.OverrideManager;

/**
 * This is is an Action which is substituted for another Action.
 */
public class OverriddenAction extends ConfluenceActionSupport implements PageAware, SpaceAware {

    public static final String SUCCESS_DEFAULT_PARAM = "${fullTargetAction}";

    private OverrideManager overrideManager;

    private String targetOverride;

    private String targetAction;

    private AbstractPage page;

    private Space space;

    private String targetMethod;

    @Override
    public String execute() throws Exception {
        ActionDetails actionDetails = overrideManager.getCurrentActionDetails();

        if ( actionDetails instanceof OverriddenActionDetails ) {
            OverriddenActionDetails overriddenAction = (OverriddenActionDetails) actionDetails;

            targetAction = overriddenAction.getTargetAction( targetOverride, getWebInterfaceContext() ).getActionName();
            if ( targetAction != null )
                return SUCCESS;
        }
        // if we get this far, we have issues
        throw new ConveyorException( "Unable to locate an action to redirect to" + ( targetOverride != null ? " with a bypass of '" + targetOverride + "'." : "." )  );
    }

    public String getTargetOverride() {
        return targetOverride;
    }

    public void setTargetOverride( String targetOverride ) {
        this.targetOverride = targetOverride;
    }

    public String getTargetAction() {
        return targetAction;
    }

    public void setOverrideManager( OverrideManager overrideManager ) {
        this.overrideManager = overrideManager;
    }

    public AbstractPage getPage() {
        return this.page;
    }

    public void setPage( AbstractPage abstractPage ) {
        this.page = abstractPage;
    }

    public boolean isPageRequired() {
        return false;
    }

    public boolean isLatestVersionRequired() {
        return false;
    }

    public boolean isViewPermissionRequired() {
        return false;
    }

    public void setSpace( Space space ) {
        this.space = space;
    }

    public boolean isSpaceRequired() {
        return false;
    }

    public Space getSpace() {
        return space;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod( String targetMethod ) {
        this.targetMethod = targetMethod;
    }

    public String getFullTargetAction() {
        return targetAction + ( targetMethod != null ? "!" + targetMethod : "" );
    }
}
