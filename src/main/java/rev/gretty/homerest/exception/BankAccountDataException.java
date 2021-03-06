package rev.gretty.homerest.exception;

/**
 * Customized exception class
 */

public class BankAccountDataException extends IllegalArgumentException {

    public BankAccountDataException(String string, Exception exception) {
        super( string, exception );
    }

    public BankAccountDataException(String string) {
        super( string );
    }
}
