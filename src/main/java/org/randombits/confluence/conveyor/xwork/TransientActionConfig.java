package org.randombits.confluence.conveyor.xwork;

import com.atlassian.confluence.plugin.descriptor.PluginAwareActionConfig;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.entities.ActionConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a temporary {@link ActionConfig} which can clone configuration details from other configs.
 */
public class TransientActionConfig extends PluginAwareActionConfig {

    private Plugin plugin;

    public TransientActionConfig( ActionConfig originalAction, Plugin defaultPlugin ) {
        super( originalAction.getMethodName(), originalAction.getClassName(),
                cloneMap( originalAction.getParams() ), cloneMap( originalAction.getResults() ),
                cloneList( originalAction.getInterceptors() ), cloneList( originalAction.getExternalRefs() ),
                originalAction.getPackageName(),
                findPlugin( originalAction, defaultPlugin ) );
        plugin = super.getPlugin();
    }

    public TransientActionConfig( OverridingActionConfig overridingActionConfig ) {
        this( overridingActionConfig, overridingActionConfig.getPlugin() );
    }

    public TransientActionConfig( ActionConfig originalAction, OverridingActionConfig overridingActionConfig ) {
        this( originalAction, overridingActionConfig.getPlugin() );
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

    private void applyConfig( OverridingActionConfig otherConfig ) {
        if ( otherConfig.getClassName() != null ) {
            setClassName( otherConfig.getClassName() );
            this.plugin = otherConfig.getPlugin();
        }
        if ( otherConfig.getMethodName() != null )
            setMethodName( otherConfig.getMethodName() );
        if ( otherConfig.getPackageName() != null )
            setPackageName( otherConfig.getPackageName() );

        copyMap( otherConfig.getParams(), getParams() );
        copyMap( otherConfig.getResults(), getResults() );
        copyList( otherConfig.getExternalRefs(), getExternalRefs() );
        copyList( otherConfig.getInterceptors(), getInterceptors() );
    }

    private static <T> List<T> cloneList( List<T> source ) {
        return new ArrayList<T>( source );
    }

    private static <K,V> Map<K,V> cloneMap( Map<K,V> source ) {
        return new HashMap<K,V>( source );
    }

    private static <T> void copyList( List<T> source, List<T> target ) {
        for ( T value : source ) {
            if ( !target.contains( value ) )
                target.add( value );
        }
    }

    private static <K, V> void copyMap( Map<K, V> source, Map<K, V> target ) {
        for ( Map.Entry<K, V> e : source.entrySet() ) {
            target.put( e.getKey(), e.getValue() );
        }
    }
}
