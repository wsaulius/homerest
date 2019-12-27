package rev.gretty.homerest.exception;

public class CurrencyCodeException extends IllegalArgumentException {

    public CurrencyCodeException(String string, Exception exception) {
        super( string, exception );
    }

    public CurrencyCodeException(String string) {
        super( string );
    }
}
