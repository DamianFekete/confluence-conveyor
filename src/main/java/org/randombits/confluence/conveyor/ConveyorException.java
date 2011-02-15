package org.randombits.confluence.conveyor;

public class ConveyorException extends Exception {

    public ConveyorException() {
    }

    public ConveyorException( String message ) {
        super( message );
    }

    public ConveyorException( Throwable exception ) {
        super( exception );
    }

    public ConveyorException( String message, Throwable exception ) {
        super( message, exception );
    }

}
