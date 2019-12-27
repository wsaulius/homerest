package rev.gretty.homerest.unit.test;

import org.iban4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.persistence.ByLocaleDateSerializerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@DisplayName("BankAccountUnitTest test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BankAccountUnitTests {

    private static final Logger log = LoggerFactory.getLogger(BankAccountUnitTests.class);

    @Mock
    private BankAccount bankAccount;

    static String IBAN_BANK_CODE = "35000";
    private Iban ibanBuilt;

    @AfterAll
    static void tearDownAll() {
    }

    @BeforeAll
    void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Test Bank Account UUID")
    public void testBankAccountUUID() {

        assertNotNull(bankAccount);
        bankAccount = spy(new BankAccount());

        assertThrows(IbanFormatException.class, () -> {
            log.info(bankAccount.toString());
        });

        assertNotNull(bankAccount.getId());
        assertNotNull(bankAccount.getUpdateAt());

        assertTrue(bankAccount.getId() instanceof UUID);
    }

    @Test
    @DisplayName("Test Bank Account DateTime")
    public void testUpdateAtDateType() throws ParseException {

        assertNotNull(bankAccount);
        bankAccount = spy(new BankAccount());
        assertTrue(bankAccount.getUpdateAt() instanceof Calendar);

        final Date testDate = new Date(bankAccount.getUpdateAt().getTimeInMillis());
        assertTrue(testDate instanceof Date);

        final String asUpdateDateString =
                new SimpleDateFormat(ByLocaleDateSerializerUtils.BY_LOCALE_DATETIME)
                        .format(bankAccount.getUpdateAt().getTime());

        final String asDateString =
                new SimpleDateFormat(ByLocaleDateSerializerUtils.BY_LOCALE_DATETIME)
                        .format(testDate);

        log.info(asDateString);
        log.info(asUpdateDateString);

        assertEquals(asDateString, asUpdateDateString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00211454334", "00334553241"})
    void testIBANgenerator(String ibanString) throws Exception {

        try {

            ibanBuilt = this.buildDefaultIBAN( ibanString );
            log.info(ibanBuilt.toFormattedString());

            IbanUtil.validate(ibanBuilt.toString(), IbanFormat.None);
            IbanUtil.validate(ibanBuilt.toFormattedString(), IbanFormat.Default);

        } catch (IbanFormatException |
                InvalidCheckDigitException |
                UnsupportedCountryException e) {

            throw e;
        }
    }

    @ParameterizedTest
    @CsvSource( {"00211454334,LT93 3500 0002 1145 4334"} )
    void formattedIBANgenerator(String ibanString, String expected) throws Exception {

        try {

            ibanBuilt = this.buildDefaultIBAN( ibanString );
            assertEquals(ibanBuilt.toFormattedString(), expected );

            IbanUtil.validate(ibanBuilt.toString(), IbanFormat.None);
            IbanUtil.validate(ibanBuilt.toFormattedString(), IbanFormat.Default);

            log.info( ibanBuilt.toFormattedString() );

        } catch (IbanFormatException |
                InvalidCheckDigitException |
                UnsupportedCountryException e) {

            throw e;
        }
    }

    @AfterEach
    void tearDown() {
    }

    public static Iban buildDefaultIBAN( final String ibanString ) {

        final Iban ibanReturned = new Iban.Builder()
                .countryCode(CountryCode.LT)
                .bankCode( IBAN_BANK_CODE )
                .accountNumber(ibanString)
                .build();

        return ibanReturned;
    }
}
