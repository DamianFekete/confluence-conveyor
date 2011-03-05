package org.randombits.confluence.conveyor.condition;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

/**
 * Abstract base class for conditions that work with plugins.
 */
public abstract class AbstractPluginCondition implements Condition {

    public static final String PLUGIN_KEY_PARAM = "pluginKey";

    protected final PluginAccessor pluginAccessor;

    protected String pluginKey;

    public AbstractPluginCondition( PluginAccessor pluginAccessor ) {
        this.pluginAccessor = pluginAccessor;
    }

    public void init( Map<String, String> params ) throws PluginParseException {
        pluginKey = params.get( PLUGIN_KEY_PARAM );
        if ( pluginKey == null )
            throw new PluginParseException( "Please provide a 'pluginKey' parameter" );
    }
}
