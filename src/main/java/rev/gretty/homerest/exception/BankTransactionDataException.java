package rev.gretty.homerest.exception;

public class BankTransactionDataException extends IllegalArgumentException {

    public BankTransactionDataException(String string, Exception exception) {
        super( string, exception );
    }

    public BankTransactionDataException(String string) {
        super( string );
    }
}
