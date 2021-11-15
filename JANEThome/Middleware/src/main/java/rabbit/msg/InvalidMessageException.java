package rabbit.msg;

/**
 * Exception raised when an invalid Message extended class if incorrectly generated(missing valid destination/from)
 */
public class InvalidMessageException extends Exception{

    InvalidMessageException(){
        super();
    }

}
