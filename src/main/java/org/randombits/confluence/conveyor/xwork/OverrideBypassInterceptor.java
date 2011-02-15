package org.randombits.confluence.conveyor.xwork;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.interceptor.Interceptor;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 16/02/11
 * Time: 3:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class OverrideBypassInterceptor implements Interceptor {

    public void destroy() {
    }

    public void init() {
    }

    public String intercept( ActionInvocation actionInvocation ) throws Exception {
        ActionContext actionContext = actionInvocation.getInvocationContext();
        ActionConfig actionConfig = actionInvocation.getProxy().getConfig();
        String methodName = actionConfig != null ? actionConfig.getMethodName() : null;
        String actionName = actionContext != null ? actionContext.getName() : null;

        try {
            Action action = actionInvocation.getAction();
            if ( action instanceof OverriddenAction ) {
                OverriddenAction overriddenAction = (OverriddenAction) action;
                if ( actionName != null ) {
                    ActionRequest request = ActionRequest.parse( actionName );

                    overriddenAction.setTargetOverride( request.getOverrideKey() );
                    overriddenAction.setTargetMethod( request.getMethodName() );

                    // Hide the method name for this call.
                    actionConfig.setMethodName( null );
                    actionContext.setName( request.getActionName() );
                }
            }
            return actionInvocation.invoke();
        } finally {
            if ( actionConfig != null ) {
                actionConfig.setMethodName( methodName );
            }
            if ( actionContext != null ) {
                actionContext.setName( actionName );
            }
        }
    }
}
