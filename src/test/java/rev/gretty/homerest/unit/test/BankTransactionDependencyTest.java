package rev.gretty.homerest.unit.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("BankTransactionDependencyTest test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankTransactionDependencyTest {

    private static final Logger log = LoggerFactory.getLogger(rev.gretty.homerest.unit.test.BankTransactionDependencyTest.class);
    final BankTransactionUnitTests transactionUnitTest = new BankTransactionUnitTests();

    @ParameterizedTest
    @DisplayName("Test Bank Transaction Setup checks")
    @ArgumentsSource(BankAccountsAsArgument.class)
    public void testBankAccountCorrectness(String ibanInput, boolean insertFlag) throws Exception {

        final Collection<JSONObject> allJSONs = transactionUnitTest.getTransactionsAsCollection();
        log.info(allJSONs.size() + " items collected.");

        //assertThrows(UnrecognizedPropertyException.class, () -> {
          transactionUnitTest.getTransactionsAsIBANS( ibanInput );
        //});

    }
}
