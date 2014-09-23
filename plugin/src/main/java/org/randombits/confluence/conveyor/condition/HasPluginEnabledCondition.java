package org.randombits.confluence.conveyor.condition;

import com.atlassian.plugin.PluginAccessor;

import java.util.Map;

/**
 * Checks if the provided plugin is currently installed <b>and</b> enabled. It should be passed the plugin key
 * as the 'pluginKey' parameter. Eg:
 *
 * <code>
 *   &lt;condition class="org.randombits.confluence.conveyor.condition.HasPluginEnabledCondition"&gt;
 *     &lt;param name="pluginKey"&gt;my.plugin.key&lt;/param&gt;
 *   &lt;/condition&gt;
 * </code>
 *
 * @see HasPluginInstalledCondition if you just want to check that it is installed, not also enabled.
 */
public class HasPluginEnabledCondition extends AbstractPluginCondition {

    public HasPluginEnabledCondition( PluginAccessor pluginAccessor ) {
        super( pluginAccessor );
    }

    public boolean shouldDisplay( Map<String, Object> stringObjectMap ) {
        return pluginAccessor.isPluginEnabled( pluginKey );
    }
}
