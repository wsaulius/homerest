package rev.gretty.homerest.exception;

/**
 * Customized exception class
 */

public class BankTransactionDataException extends IllegalArgumentException {

    public BankTransactionDataException(String string, Exception exception) {
        super( string, exception );
    }

    public BankTransactionDataException(String string) {
        super( string );
    }
}
