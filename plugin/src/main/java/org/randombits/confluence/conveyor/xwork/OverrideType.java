package org.randombits.confluence.conveyor.xwork;

/**
* Created by IntelliJ IDEA.
* User: david
* Date: 16/02/11
* Time: 9:32 AM
* To change this template use File | Settings | File Templates.
*/
public enum OverrideType {
    BYPASS( "*" ), MATCH( "@" );

    private final String prefix;

    OverrideType( String prefix ) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }

    public static OverrideType forPrefix( String override ) {
        if ( override == null )
            return null;
        for ( OverrideType type : values() ) {
            if ( override.startsWith( type.prefix ) )
                return type;
        }
        return null;
    }
}
