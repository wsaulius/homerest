package rev.gretty.homerest.exception;
import javax.money.MonetaryException;

/**
 * Customized exception class
 */

public class CurrencyAmountException extends MonetaryException {

    public CurrencyAmountException(String message) {
        super(message);
    }

    public CurrencyAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
