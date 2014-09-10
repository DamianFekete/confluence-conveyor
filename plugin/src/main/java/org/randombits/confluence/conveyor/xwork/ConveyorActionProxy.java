package org.randombits.confluence.conveyor.xwork;

import com.opensymphony.xwork.DefaultActionProxy;

import java.util.Map;

/**
 * Overrides the standard {@link DefaultActionProxy} to get around it's 'protected' status.
 */
public class ConveyorActionProxy extends DefaultActionProxy {

    protected ConveyorActionProxy( String namespace, String actionName, Map extraContext, boolean executeResult ) throws Exception {
        super( namespace, actionName, extraContext, executeResult );
    }
}
