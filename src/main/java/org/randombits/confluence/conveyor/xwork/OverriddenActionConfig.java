package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.core.ConfluenceAutowireInterceptor;
import com.atlassian.confluence.pages.actions.PageAwareInterceptor;
import com.atlassian.confluence.spaces.actions.SpaceAwareInterceptor;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.ActionChainResult;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;
import com.opensymphony.xwork.interceptor.Interceptor;
import com.opensymphony.xwork.interceptor.ParametersInterceptor;

import java.util.*;

/**
 * A subclass of {@link ActionConfig} which is configured to load an {@link OverriddenAction} instance.
 */
public class OverriddenActionConfig extends ConveyorActionConfig {

    private ActionConfig originalActionConfig;

    private String originalActionAlias;

    private List<OverridingActionLink> overridingActions;

    public OverriddenActionConfig( Plugin plugin, String originalActionAlias, ActionConfig originalActionConfig ) {
        super( null, OverriddenAction.class.getName(), defaultParameters(), defaultResults(), defaultInterceptors(), plugin );
        overridingActions = new ArrayList<OverridingActionLink>();
        this.originalActionAlias = originalActionAlias;
        this.originalActionConfig = originalActionConfig;
    }

    public String getOriginalActionAlias() {
        return originalActionAlias;
    }

    public ActionConfig getOriginalActionConfig() {
        return originalActionConfig;
    }

    public Collection<OverridingActionLink> getOverridingActions() {
        return overridingActions;
    }

    /**
     * Sorts the list of {@link OverridingActionLink}s by weight.
     */
    public void sortOverridingActions() {
        sortOverridingActions( OverridingActionLink.WEIGHT_COMPARATOR );
    }

    public void sortOverridingActions( Comparator<? super OverridingActionLink> comparator ) {
        Collections.sort( overridingActions, comparator );
    }

    // Defaults

    private static List<Interceptor> defaultInterceptors() {
        List<Interceptor> interceptors = new ArrayList<Interceptor>();

        interceptors.add( new PageAwareInterceptor() );
        interceptors.add( new SpaceAwareInterceptor() );
        interceptors.add( new ConfluenceAutowireInterceptor() );
        interceptors.add( new ParametersInterceptor() );
        interceptors.add( new OverrideInterceptor() );
        return interceptors;
    }

    private static Map<String, ResultConfig> defaultResults() {
        Map<String, ResultConfig> results = new HashMap<String, ResultConfig>();

        // SUCCESS results in a chaining to another action, as defined by the 'targetAction' field of the OverriddenAction.
        ResultConfig success = new ResultConfig();
        success.setName( OverriddenAction.SUCCESS );
        success.setClassName( ActionChainResult.class.getName() );
        success.addParam( ActionChainResult.DEFAULT_PARAM, OverriddenAction.SUCCESS_DEFAULT_PARAM );

        results.put( OverriddenAction.SUCCESS, success );

        return results;
    }

    private static Map<String, String> defaultParameters() {
        return Collections.EMPTY_MAP;
    }


}
