package rev.gretty.homerest.exception;

/**
 * Customized exception class
 */

public class BankTransactionProcessingException extends IllegalArgumentException {

    public BankTransactionProcessingException(String string, Exception exception) {
        super( string, exception );
    }

    public BankTransactionProcessingException(String string) {
        super( string );
    }
}
