package rev.gretty.homerest.service.test;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.hibernate.HibernateException;
import org.hibernate.exception.ConstraintViolationException;
import org.iban4j.Iban;
import org.iban4j.IbanFormatException;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;
import org.javamoney.moneta.spi.MoneyUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.exception.CurrencyAmountException;
import rev.gretty.homerest.exception.CurrencyCodeException;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;

import rev.gretty.homerest.guicebound.bound.IBankAccount;
import rev.gretty.homerest.persistence.CallingHibernateDDLConfig;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.unit.test.BankAccountUnitTests;
import rev.gretty.homerest.unit.test.BankAccountsAsArgument;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.ConversionQuery;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.inject.Guice.createInjector;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("BankAccountInsertTests test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Singleton
public class BankAccountInsertTests {

    public static final String[] VALID_CURRENCIES = {"EUR", "USD", "GBP", "NOK", "DKK", "SEK"};
    private static final Logger log = LoggerFactory.getLogger(BankAccountInsertTests.class);

    @Inject
    IAccountService iAccountServiceTest;

    @Mock
    private Injector injector;

    @BeforeAll
    public void init() {
        MockitoAnnotations.initMocks(this);

        injector = createInjector(new GuiceBoundConfigModule());
        iAccountServiceTest  = injector.getInstance( IAccountService.class );
        Provider<CallingHibernateDDLConfig> ddlProvider
                = injector.getProvider( CallingHibernateDDLConfig.class );

        // DROP and CREATE DB
        // ddlProvider.get().generateDDL( injector.getProvider( IBankAccount.class ) );

        // DO NOT DROP and CREATE Single TABLE
        ddlProvider.get().generateDDL( null );
    }

    @Order(1)
    @ParameterizedTest
    @ArgumentsSource(BankAccountsAsArgument.class)
    void testGeneratedIBanCurrencies(String ibanInput, boolean insertFlag) {

        try {

            Iban ibanGenerator = new BankAccountUnitTests().buildDefaultIBAN(ibanInput);
            BankAccount bankAccount = new BankAccount();

            bankAccount.setAccountNumber(ibanGenerator.toString());

            assertThrows(CurrencyCodeException.class, () -> {
                bankAccount.setCurrencyUnit(null);
            });

            assertThrows(CurrencyCodeException.class, () -> {
                bankAccount.setCurrencyUnit("WTF");
            });

            assertThrows(CurrencyCodeException.class, () -> {
                bankAccount.setCurrencyUnit("USD ");
            });

            assertThrows(CurrencyCodeException.class, () -> {
                bankAccount.setCurrencyUnit("eur");
            });

            Arrays.stream(VALID_CURRENCIES).forEach(one -> {

                        bankAccount.setCurrencyUnit(one);
                        bankAccount.setCurrentBalance(BigDecimal.TEN);

                        log.info(bankAccount.toString());
                    }
            );

        } catch (IbanFormatException |
                InvalidCheckDigitException |
                UnsupportedCountryException e) {

            throw e;
        }

    }

    @Order(2)
    @ParameterizedTest
    @ArgumentsSource(BankAccountsAsArgument.class)
    void testInsertDirect(String ibanInput, boolean insertFlag) throws Exception {

        Iban ibanGenerator = new BankAccountUnitTests().buildDefaultIBAN(ibanInput);
        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountNumber(ibanGenerator.toString());

        Optional<String> anyCurrency = Arrays.stream(VALID_CURRENCIES).findFirst();
        bankAccount.setCurrencyUnit(anyCurrency.get());

        Long putMoneyEUR = ThreadLocalRandom.current().nextLong(0L, 1000 * Byte.MAX_VALUE + 1);

        CurrencyUnit euro = Monetary.getCurrency(bankAccount.getCurrencyUnit());
        MonetaryAmount monetaryAmount = Money.ofMinor(euro, putMoneyEUR, 2);

        MonetaryAmountFormat euroFormat = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(Locale.GERMANY)
                        .set(CurrencyStyle.SYMBOL)
                        .set("pattern", String.format("#,##0.00### %s", euro.getCurrencyCode()))
                        .build());

        log.info(euroFormat.format(monetaryAmount));

        BigDecimal asBigDecimal = MoneyUtils.getBigDecimal(monetaryAmount.getNumber());
        assertThrows(CurrencyAmountException.class, () -> {
            // Make account negative
            bankAccount.setCurrentBalance( BigDecimal.TEN.negate() );
        });

        // Currency Code may not be null : must set it first!
        bankAccount.setCurrentBalance(asBigDecimal);

        // Cannnot enter data without CURRENCY_UNIT
        // assertThrows(javax.persistence.PersistenceException.class, () -> {

        try {

            if (insertFlag) {
                bankAccount.setCurrencyUnit(euro.getCurrencyCode());

                try {
                    iAccountServiceTest.putAccount(bankAccount);
                } catch ( PersistenceException duplicates ) {

                    if ( duplicates.getCause() instanceof ConstraintViolationException) {
                        log.warn( duplicates.toString() );
                    }

                }
            }

        } catch (HibernateException e) {

            e.printStackTrace();
            // We want to see the error description too.
            throw e;
        }
        // });

        log.info(bankAccount.toString());
    }

    @Order(3)
    @Test
    @DisplayName("BankAccount service read all after inserts test case")
    void testServiceReadAll() throws Exception {

        assertNotNull(iAccountServiceTest);
        try {

            iAccountServiceTest.getAllAccounts().stream().forEach(one -> {
                log.info("FROM DB: " + one.toString());
            });

        } catch (HibernateException e) {

            e.printStackTrace();
            throw e;
        }
    }

    @Order(5)
    @Test
    @Tag("development")
    public void testTimeInLocale() {

        Calendar localDate = GregorianCalendar.getInstance();

        localDate.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
        localDate.add(Calendar.DAY_OF_YEAR, -21);

        ConversionQuery conversionQuery = ConversionQueryBuilder.of()
                .setTermCurrency("EUR").set(localDate).build();

        MonetaryAmount money = Money.of(BigDecimal.TEN, "EUR");
    }

}
