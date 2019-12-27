package rev.gretty.homerest.exception;

public class BankAccountDataException extends IllegalArgumentException {

    public BankAccountDataException(String string, Exception exception) {
        super( string, exception );
    }

    public BankAccountDataException(String string) {
        super( string );
    }
}
