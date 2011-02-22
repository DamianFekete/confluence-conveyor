package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.actions.PageAware;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.SpaceAware;
import com.atlassian.plugin.web.Condition;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.config.entities.ActionConfig;
import org.randombits.confluence.conveyor.ConveyorException;
import org.randombits.confluence.conveyor.OverrideManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

/**
 * This is is an Action which is substituted for another Action.
 */
public class OverriddenAction extends ConfluenceActionSupport implements PageAware, SpaceAware {

    private static final Logger LOG = LoggerFactory.getLogger( OverriddenAction.class );

    private static final String ORIGINAL_ACTION = "original";

    public static final String SUCCESS_DEFAULT_PARAM = "${fullTargetAction}";

    public static final String ERROR_DEFAULT_PARAM = "/notpermitted.vm";

    private OverrideManager overrideManager;

    private String targetAction;

    private AbstractPage page;

    private Space space;

    private String targetMethod;

    private ActionRequest actionRequest;

    @Override
    public String execute() throws Exception {
        try {
            ActionConfig currentConfig = overrideManager.getCurrentActionConfig();

            if ( currentConfig instanceof OverriddenActionConfig ) {
                OverriddenActionConfig overriddenAction = (OverriddenActionConfig) currentConfig;

                if ( LOG.isDebugEnabled() )
                    LOG.debug( "Action Request: " + actionRequest );

                targetAction = getTargetAction( overriddenAction, actionRequest );
                if ( targetAction != null )
                    return SUCCESS;
            }
            // if we get this far, we have issues
            throw new ConveyorException( "Unable to locate an action to redirect to"
                    + ( actionRequest.getOverrideKey() != null ?
                    " with a bypass of '" + actionRequest.getOverrideKey() + "'." : "." ) );

        } catch ( ConveyorException e ) {
            LOG.debug( e.getMessage(), e );
            ServletActionContext.getResponse().sendError( HttpServletResponse.SC_NOT_FOUND );
        }
        LOG.debug( "Unable to locate an action to redirect to"
                + ( actionRequest.getOverrideKey() != null ?
                " with a bypass of '" + actionRequest.getOverrideKey() + "'." : "." ) );
        return ERROR;
    }

    public String getTargetAction( OverriddenActionConfig overriddenAction, ActionRequest actionRequest ) throws ConveyorException {
        String targetAction = null;

        Iterator<OverridingActionLink> i = overriddenAction.getOverridingActions().iterator();

        OverrideType overrideType = actionRequest.getOverrideType();
        String overrideKey = actionRequest.getOverrideKey();

        if ( overrideType == OverrideType.MATCH && ORIGINAL_ACTION.equals( overrideKey ) )
            return overriddenAction.getOriginalActionAlias();

        // First, we skip down to the specified override key, if applicable
        OverridingActionLink link;

        if ( overrideType != null ) {
            if ( i.hasNext() ) {
                boolean matchesOverride = false;
                do {
                    link = i.next();
                    if ( LOG.isDebugEnabled() )
                        LOG.debug( "Checking if '" + link.getKey() + "' matches requested key of '" + overrideKey + "'." );
                    matchesOverride = link.getKey().equals( overrideKey );
                } while ( i.hasNext() && matchesOverride );

                if ( !matchesOverride ) {
                    throw new ConveyorException( "Unable to find a matching override for this action: " + overrideKey );
                } else if ( overrideType == OverrideType.MATCH ) {
                    // We need to return the exact match.
                    if ( meetsCondition( link ) ) {
                        return link.getAlias();
                    } else {
                        throw new ConveyorException( "Unable to find a matching override for this action: " + overrideKey );
                    }
                }
            } else {
                throw new ConveyorException( "Unable to find a matching override for this action: " + overrideKey );
            }
        }

        // Check any conditions on the remaining overrides.
        if ( i.hasNext() ) {
            boolean conditionMet = false;
            do {
                link = i.next();
                conditionMet = meetsCondition( link );
            } while ( i.hasNext() && !conditionMet );

            if ( conditionMet )
                targetAction = link.getAlias();
        }

        if ( targetAction == null ) {
            targetAction = overriddenAction.getOriginalActionAlias();
        }

        return targetAction;
    }

    private boolean meetsCondition( OverridingActionLink override ) {
        Condition condition = override.getActionConfig().getCondition();
        return condition == null || condition.shouldDisplay( getWebInterfaceContext().toMap() );
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

    public void setActionRequest( ActionRequest request ) {
        this.actionRequest = request;
    }
}
