package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.ResultConfig;
import org.randombits.confluence.conveyor.ConveyorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a temporary {@link ActionConfig} which can clone configuration details from other configs.
 */
public class TransientActionConfig extends PluginAwareActionConfig {

    private static final Logger LOG = LoggerFactory.getLogger( TransientActionConfig.class );

    private Plugin plugin;

    private String originalActionName;

    public TransientActionConfig( String originalActionName, ActionConfig originalAction, Plugin defaultPlugin ) {
        super( originalAction.getMethodName(), originalAction.getClassName(),
                cloneMap( originalAction.getParams() ), cloneMap( originalAction.getResults() ),
                cloneList( originalAction.getInterceptors() ), cloneList( originalAction.getExternalRefs() ),
                originalAction.getPackageName(),
                findPlugin( originalAction, defaultPlugin ) );
        this.originalActionName = originalActionName;
        plugin = super.getPlugin();
    }

    public TransientActionConfig( String originalActionName, OverridingActionConfig overridingActionConfig ) {
        this( originalActionName, overridingActionConfig, overridingActionConfig.getPlugin() );
    }

    public TransientActionConfig( String originalActionName, ActionConfig originalAction, OverridingActionConfig overridingActionConfig ) throws ConveyorException {
        this( originalActionName, originalAction, overridingActionConfig.getPlugin() );
        applyConfig( overridingActionConfig );
    }

    private static Plugin findPlugin( ActionConfig originalAction, Plugin defaultPlugin ) {
        if ( originalAction instanceof PluginAwareActionConfig )
            return ( (PluginAwareActionConfig) originalAction ).getPlugin();
        return defaultPlugin;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    private void applyConfig( OverridingActionConfig otherConfig ) throws ConveyorException {
        if ( otherConfig.getClassName() != null ) {
            setClassName( otherConfig.getClassName() );
            this.plugin = otherConfig.getPlugin();
        }
        if ( otherConfig.getMethodName() != null )
            setMethodName( otherConfig.getMethodName() );
        if ( otherConfig.getPackageName() != null )
            setPackageName( otherConfig.getPackageName() );

        copyMap( otherConfig.getParams(), getParams(), true );
        copyList( otherConfig.getExternalRefs(), getExternalRefs() );
        copyList( otherConfig.getInterceptors(), getInterceptors() );

        // Now, we deal with results.

        // First, copy in any new results
        copyMap( otherConfig.getResults(), getResults(), false );

        // Then, perform any overrides
        overrideResults( otherConfig );
    }

    private void overrideResults( OverridingActionConfig otherConfig ) throws ConveyorException {
        for( OverridingResultConfig overridingResult : otherConfig.getOverridingResults() ) {
            OverriddenResultConfig overriddenResult = asOverriddenResultConfig( overridingResult.getName() );
            overriddenResult.getOverridingResults().add( overridingResult );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Overrode the '" + overriddenResult.getName() + "' result in the '" + originalActionName + "' action in the '" + packageName + "' package." );
            }
        }
    }

    private OverriddenResultConfig asOverriddenResultConfig( String name ) throws ConveyorException {
        ResultConfig currentResult = (ResultConfig) getResults().get( name );
        if ( currentResult instanceof OverriddenResultConfig ) {
            return (OverriddenResultConfig) currentResult;
        } else if ( currentResult != null ) {
            OverriddenResultConfig overriddenResult = new OverriddenResultConfig( currentResult );
            getResults().put( name, overriddenResult );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Converted the '" + name + "' result in the '" + originalActionName + "' action in the '" + packageName + "' package to overridden." );
            }
            return overriddenResult;
        } else {
            throw new ConveyorException( "No result named '" + name + "' found to override for the '" + originalActionName + "' action in the '" + packageName + "' package." );
        }
    }

    private static <T> List<T> cloneList( List<T> source ) {
        return new ArrayList<T>( source );
    }

    private static <K, V> Map<K, V> cloneMap( Map<K, V> source ) {
        return new HashMap<K, V>( source );
    }

    private static <T> void copyList( List<T> source, List<T> target ) {
        for ( T value : source ) {
            if ( !target.contains( value ) )
                target.add( value );
        }
    }

    private <K, V> void copyMap( Map<K, V> source, Map<K, V> target, boolean allowReplacement ) throws ConveyorException {
        for ( Map.Entry<K, V> e : source.entrySet() ) {
            if ( !allowReplacement && target.containsKey( e.getKey() ) )
                throw new ConveyorException( "An entry named '" + e.getKey() + "' already exists in the '" + originalActionName + "' action in the '" + packageName + "' package and cannot be overridden." );

            target.put( e.getKey(), e.getValue() );
        }
    }

    public String getOriginalActionName() {
        return originalActionName;
    }

    public void setOriginalActionName( String originalActionName ) {
        this.originalActionName = originalActionName;
    }
}
