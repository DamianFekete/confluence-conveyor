package org.randombits.confluence.conveyor.xwork;

import com.opensymphony.xwork.config.entities.ResultConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a way to track the {@link OverridingResultConfig} for a result.
 */
public class OverriddenResultConfig extends ResultConfig {

    private List<OverridingResultConfig> overridingResults;

    private ResultConfig originalResult;

    public OverriddenResultConfig( ResultConfig originalResult ) {
        super( originalResult.getName(), OverriddenResult.class );
        setOriginalResult( originalResult );
        setOverridingResults( new ArrayList<OverridingResultConfig>() );
    }

    public ResultConfig getOriginalResult() {
        return originalResult;
    }

    public void setOriginalResult( ResultConfig originalResult ) {
        this.originalResult = originalResult;
        getParams().put( OverriddenResult.ORIGINAL_RESULT_PARAM, originalResult );
    }

    public List<OverridingResultConfig> getOverridingResults() {
        return overridingResults;
    }

    public void setOverridingResults( List<OverridingResultConfig> overridingResults ) {
        this.overridingResults = overridingResults;
        getParams().put( OverriddenResult.OVERRIDING_RESULTS_PARAM, overridingResults );
    }
}
