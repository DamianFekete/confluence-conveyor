package org.randombits.confluence.conveyor.xwork;

import org.randombits.confluence.conveyor.ConveyorException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assists with parsing full "action*override!method" action names into smaller chunks.
 */
public class ActionRequest {

    private static final String OVERRIDE_TYPES = getOverrideTypePattern();

    private static final Pattern ACTION_NAME = Pattern.compile( "([^" + OVERRIDE_TYPES + "!]+)(?:([" + OVERRIDE_TYPES + "])([^!]+))?(?:!(.+))?" );

    private final String actionName;

    private final OverrideType overrideType;

    private final String overrideKey;

    private final String methodName;

    public ActionRequest( String actionName, OverrideType overrideType, String overrideKey, String methodName ) {
        this.actionName = actionName;
        this.overrideType = overrideType;
        this.overrideKey = overrideKey;
        this.methodName = methodName;
    }

    public String getActionName() {
        return actionName;
    }

    public OverrideType getOverrideType() {
        return overrideType;
    }

    public String getOverrideKey() {
        return overrideKey;
    }

    public String getMethodName() {
        return methodName;
    }

    public static ActionRequest parse( String fullActionName ) throws ConveyorException {
        Matcher matcher = ACTION_NAME.matcher( fullActionName );
        if ( matcher.matches() ) {
            return new ActionRequest( matcher.group( 1 ), OverrideType.forPrefix( matcher.group( 2 ) ), matcher.group( 3 ), matcher.group( 4 ) );
        } else {
            throw new ConveyorException( "Unsupported action name syntax: '" + fullActionName + "'" );
        }
    }

    private static String getOverrideTypePattern() {
        StringBuilder out = new StringBuilder();
        for( OverrideType ot : OverrideType.values() ) {
            out.append( "\\" ).append( ot );
        }
        return out.toString();
    }

    @Override
    public String toString() {
        return "{action request: action name=" + getActionName() + "; override type=" + getOverrideType()
                + "; override key = " + getOverrideKey() + "; method name=" + getMethodName() + "}";
    }
}
