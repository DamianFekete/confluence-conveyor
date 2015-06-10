package org.randombits.confluence.conveyor.xwork;

import com.atlassian.plugin.web.Condition;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.Result;

/**
 * Represents an overriding action, keeping it's condition attached, even though it doesn't actually check
 * the condition itself.
 */
public class OverridingResult implements Result {

    private Condition condition;

    private Result realResult;

    public OverridingResult() {
    }

    public void execute( ActionInvocation invocation ) throws Exception {
        realResult.execute( invocation );
    }

    public Result getRealResult() {
        return realResult;
    }

    public void setRealResult( Result realResult ) {
        this.realResult = realResult;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition( Condition condition ) {
        this.condition = condition;
    }
}
