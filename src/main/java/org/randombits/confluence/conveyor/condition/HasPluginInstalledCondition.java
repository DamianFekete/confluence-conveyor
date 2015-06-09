package org.randombits.confluence.conveyor.condition;

import com.atlassian.plugin.PluginAccessor;

import java.util.Map;

/**
 * Checks if the provided plugin is currently installed. It <b>does not</b> check if the plugin is enabled.
 * It should be passed the plugin key as the 'pluginKey' parameter. Eg:
 *
 * <code>
 *   &lt;condition class="org.randombits.confluence.conveyor.condition.HasPluginInstalledCondition"&gt;
 *     &lt;param name="pluginKey"&gt;my.plugin.key&lt;/param&gt;
 *   &lt;/condition&gt;
 * </code>
 */
public class HasPluginInstalledCondition extends AbstractPluginCondition {

    public HasPluginInstalledCondition( PluginAccessor pluginAccessor ) {
        super( pluginAccessor );
    }

    public boolean shouldDisplay( Map<String, Object> stringObjectMap ) {
        return pluginAccessor.getPlugin( pluginKey ) != null;
    }
}
