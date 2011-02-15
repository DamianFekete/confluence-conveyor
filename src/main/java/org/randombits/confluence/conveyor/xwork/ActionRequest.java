package org.randombits.confluence.conveyor.xwork;

import org.randombits.confluence.conveyor.ConveyorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 16/02/11
 * Time: 8:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class ActionRequest {

    static final Pattern ACTION_NAME = Pattern.compile( "([^\\^@!]+)([\\^@][^!]+)?(?:!(.+))?" );

    public static ActionRequest parse( String fullActionName ) throws ConveyorException {
        Matcher matcher = ACTION_NAME.matcher( fullActionName );
        if ( matcher.matches() ) {
            return new ActionRequest( matcher.group( 1 ), matcher.group( 2 ), matcher.group( 3 ) );
        } else {
            throw new ConveyorException( "Unsupported action name syntax: '" + fullActionName + "'" );
        }
    }

    private final String actionName;

    private final String overrideKey;

    private final String methodName;

    public ActionRequest( String actionName, String overrideKey, String methodName ) {
        this.actionName = actionName;
        this.overrideKey = overrideKey;
        this.methodName = methodName;
    }

    public String getActionName() {
        return actionName;
    }

    public String getOverrideKey() {
        return overrideKey;
    }

    public String getMethodName() {
        return methodName;
    }
}
