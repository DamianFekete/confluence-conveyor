package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.ObjectFactory;
import com.opensymphony.xwork.Result;
import com.opensymphony.xwork.config.entities.ResultConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents an overridden result, which will execute the first override which either has no condition, or
 * whose condition matches.
 */
public class OverriddenResult implements Result {

    static final String OVERRIDING_RESULTS_PARAM = "overridingResults";

    static final String ORIGINAL_RESULT_PARAM = "originalResult";

    List<OverridingResultConfig> overridingResults;

    ResultConfig originalResult;

    public OverriddenResult() {
        overridingResults = new ArrayList<OverridingResultConfig>();
    }

    public List<OverridingResultConfig> getOverridingResults() {
        return overridingResults;
    }

    public void setOverridingResults( List<OverridingResultConfig> overridingResults ) {
        this.overridingResults = overridingResults;
    }

    public ResultConfig getOriginalResult() {
        return originalResult;
    }

    public void setOriginalResult( ResultConfig originalResult ) {
        this.originalResult = originalResult;
    }

    public void execute( ActionInvocation invocation ) throws Exception {

        Map<String, Object> context = findContext( invocation );

        Result result = null;

        for ( OverridingResultConfig resultConfig : overridingResults ) {
            if ( resultConfig.getCondition().shouldDisplay( context ) ) {
                // Create a Result instance
                result = ObjectFactory.getObjectFactory().buildResult( resultConfig );
                break;
            }
        }

        if ( result == null ) {
            result = ObjectFactory.getObjectFactory().buildResult( originalResult );
        }

        // Execute the result
        result.execute( invocation );
    }

    private Map<String, Object> findContext( ActionInvocation invocation ) {
        if ( invocation.getAction() instanceof ConfluenceActionSupport ) {
            return ( (ConfluenceActionSupport) invocation.getAction() ).getWebInterfaceContext().toMap();
        }
        return Collections.EMPTY_MAP;
    }
}
