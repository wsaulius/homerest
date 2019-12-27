package rev.gretty.homerest.unit.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.iban4j.Iban;
import org.iban4j.IbanFormat;
import org.iban4j.IbanUtil;
import org.javamoney.moneta.Money;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankTransaction;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@DisplayName("BankTransactionUnitTests test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankTransactionUnitTests {

    private static final Logger log = LoggerFactory.getLogger(BankTransactionUnitTests.class);

    final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BankTransaction bankTransaction;

    static String IBAN_BANK_CODE = "35000";
    private Iban ibanBuilt;

    @AfterAll
    static void tearDownAll() {
    }

    @BeforeAll
    void init() {

        MockitoAnnotations.initMocks(this);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    }

    @DisplayName("Test Bank Transaction edit accounts")
    public Collection<JSONObject> getTransactionsAsIBANS( final String ibanInput )
            throws Exception {

        final Collection<JSONObject> allJSONs = this.getTransactionsAsCollection();
        final Collection<JSONObject> ibanJSONs = new ArrayList<>();

        log.info(allJSONs.size() + " items collected.");

        // Because of exceptions in lambdas
        Iterator<JSONObject> ii = allJSONs.iterator();

        try {

        for (int i = 0; ii.hasNext() && i < 10; i++) {

            ibanBuilt = BankAccountUnitTests.buildDefaultIBAN( ibanInput );
            IbanUtil.validate(ibanBuilt.toFormattedString(), IbanFormat.Default);

            // Check parsers too
            bankTransaction = objectMapper.readValue(
                    (String) ii.next().toString(), BankTransaction.class);

            final String fixedPart = ibanInput.substring(0, 6);
            final String permutatePart = ibanInput.substring( ibanInput.length() - 5);

            AtomicInteger permCount = new AtomicInteger();
            BankAccountsAsArgument.permutations(permutatePart).forEach(gen -> {

                Integer salt = new Random().nextInt((9 - 1) + 1) + 1;
                Integer permute = new Random().nextInt((salt - 1) + 1) + 1;

                final String genIbanInput =
                        ibanInput.replace(salt.toString(), permute.toString());
                Iban genIBAN = BankAccountUnitTests.buildDefaultIBAN(genIbanInput);

                IbanUtil.validate(genIBAN.toFormattedString(), IbanFormat.Default);

                if ( permCount.getAndIncrement() % 3 == 0) {
                    bankTransaction.setTransactionFrom(ibanBuilt.toString());

                    if (bankTransaction.isDebit() && salt > 5) {
                        bankTransaction.setCreditTransaction();
                    }

                } else {
                    bankTransaction.setTransactionTo(genIBAN.toString());
                }

                if ( permCount.getAndIncrement() % 5 == 0) {
                    bankTransaction.setTransactionTo(ibanBuilt.toString());

                    if (bankTransaction.isDebit() && salt > 8) {
                        bankTransaction.setCreditTransaction();
                    }

                } else {
                    bankTransaction.setTransactionFrom(genIBAN.toString());
                }

                // log.info("Transaction COLLECT: "  + bankTransaction.getId() );

                try {
                    ibanJSONs.add( new JSONObject( bankTransaction.toString() ));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

        } catch ( com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException e ) {
            log.warn( e.getMessage() );

        }

        return ibanJSONs;
    }

    @DisplayName("Test Bank Transaction collect all")
    public Collection<JSONObject> getTransactionsAsCollection() throws Exception {

        bankTransaction = spy(new BankTransaction());
        assertTrue(bankTransaction.getTransactionTime() instanceof Calendar);

        log.info(bankTransaction.toString());
        assertNotNull(bankTransaction.getTransactionType());

        assertFalse(bankTransaction.isCredit());
        assertTrue(bankTransaction.isDebit());
        assertTrue(bankTransaction.isDebit() instanceof Boolean);

        bankTransaction.setCreditTransaction();
        assertTrue(bankTransaction.isCredit());

        assertThrows(ArithmeticException.class, () -> {
            bankTransaction.setTransactionAmount(
                    BigDecimal.TEN.add(new BigDecimal(160))
                            .divide(new BigDecimal(7)));
        });

        log.info(bankTransaction.toString());

        final int from = 5, to = 20;
        final LinkedList<JSONObject> allJSONs = new LinkedList<>();

        for (int iteration = from; iteration <= to; iteration++) {

            final BigDecimal bigDecimal =
                    BigDecimal.TEN.add(new BigDecimal(iteration * 700)).divide(
                            new BigDecimal(7.5f),
                            MathContext.DECIMAL32);

            MonetaryAmount currencyAmount = Monetary.getDefaultAmountFactory()
                    .setCurrency("EUR")
                    .setNumber(bigDecimal)
                    .create();

            MonetaryAmount monetaryAmount = Money.of(bigDecimal, currencyAmount.getCurrency());
            bankTransaction.setTransactionAmount(monetaryAmount);

            JSONObject expected = new JSONObject();
            JSONObject actual = null;

            try {

                actual = new JSONObject(bankTransaction.toString());
                allJSONs.add(actual);

                log.info(actual.toString());

            } catch (JSONException e) {
                continue;
            }

            log.info(BigDecimal.valueOf(new Double(actual.get("transactionAmount")
                    .toString()))
                    .toString());
            // JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
        }

        assertEquals(allJSONs.size(), IntStream.rangeClosed(from, to).toArray().length);
        return allJSONs;
    }
}
