package rev.gretty.homerest.unit.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.iban4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.guicebound.bound.IBankAccount;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.fortest.AccountServiceImpl4Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

@DisplayName("BankTransactionBasicTests test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Singleton
public class BankTransactionBasicTests {

    private static final Logger log = LoggerFactory.getLogger(BankTransactionBasicTests.class);
    final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IAccountService iAccountService;

    @Mock
    private BankTransaction bankTransaction;

    static String IBAN_BANK_CODE = "35000";
    private Iban ibanBuilt;

    @BeforeAll
    void init() {

        MockitoAnnotations.initMocks(this);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @ParameterizedTest
    @ArgumentsSource(BankAccountsAsArgument.class)
    void testInsertGeneratedIBans( String ibanInput, boolean insertFlag ) {

        try {

            ibanBuilt = BankAccountUnitTests.buildDefaultIBAN( ibanInput );

            IbanUtil.validate(ibanBuilt.toFormattedString(), IbanFormat.Default);
            log.info( ibanBuilt.toFormattedString() + " from "  + ibanInput );

        } catch (IbanFormatException |
                InvalidCheckDigitException |
                UnsupportedCountryException e) {

            throw e;
        }
    }

    @Test
    @DisplayName("Test Bank Transaction UUID")
    public void testBankAccountUUID() {

        assertNotNull(bankTransaction);
        bankTransaction = spy(new BankTransaction());

        // These must be set at creation time
        assertNotNull(bankTransaction.getId());
        assertNotNull(bankTransaction.getTransactionStatus());
        assertNotNull(bankTransaction.getTransactionTime());

        assertTrue(bankTransaction.getId() instanceof UUID);
    }

    @Order(20)
    @DisplayName("Test Transaction Credit Calc")
    @ParameterizedTest
    @ArgumentsSource( BankTransactionsAsArgument.class )
    public void testBankAccountCredit( @ConvertWith( BankAccountTransactionConverter.class ) BankTransaction toPass )
            throws Exception {

        assertNotNull(toPass);
        bankTransaction = spy( toPass );

        // These must be set at creation time
        assertNotNull(bankTransaction.getId());
        assertNotNull(bankTransaction.getTransactionStatus());
        assertNotNull(bankTransaction.getTransactionTime());
        assertTrue(bankTransaction.getId() instanceof UUID);

        assertNotNull( bankTransaction.getTransactionType() );
        assertTrue( bankTransaction.isDebit() || bankTransaction.isCredit() );

        assertNotNull( bankTransaction.getTransactionAmount() );
        assertTrue( bankTransaction.getTransactionAmount() instanceof BigDecimal );

        IbanUtil.validate( bankTransaction.getTransactionFrom() );
        IbanUtil.validate( bankTransaction.getTransactionTo() );

        this.iAccountService = new AccountServiceImpl4Test();
        log.info( "Testing as " + iAccountService.getClass() );

        final List accountList = new LinkedList<>();

        final BankAccount
            bankAccountFrom = new BankAccount(),
            bankAccountTo = new BankAccount();

        assertNotNull( bankAccountFrom );
        assertNotNull( bankAccountTo );

        assertNotNull( bankAccountFrom.getId() );
        assertNotNull( bankAccountTo.getId() );

        bankAccountFrom.setAccountNumber( bankTransaction.getTransactionFrom() );
        bankAccountTo.setAccountNumber( bankTransaction.getTransactionTo() );

        bankAccountFrom.setCurrentBalance( new BigDecimal( "9111.56") );
        bankAccountTo.setCurrentBalance( BigDecimal.ZERO );

        bankAccountFrom.setCurrencyUnit( "EUR" );
        bankAccountTo.setCurrencyUnit( "EUR" );

        assertEquals( bankTransaction.getTransactionTo(), bankAccountTo.getAccountNumber() );
        assertEquals( bankTransaction.getTransactionFrom(), bankAccountFrom.getAccountNumber() );

        accountList.addAll(Arrays.asList(new BankAccount[]{bankAccountFrom, bankAccountTo}));
        iAccountService.transactInAccounts( bankTransaction, accountList );
    }

    @DisplayName("Test Transaction Debit Calc")
    @ParameterizedTest
    @ArgumentsSource( BankTransactionsAsArgument.class )
    public void testBankAccountDebit( ) {

        assertNotNull(bankTransaction);
        bankTransaction = spy(new BankTransaction());

        // These must be set at creation time
        assertNotNull(bankTransaction.getId());
        assertNotNull(bankTransaction.getTransactionStatus());
        assertNotNull(bankTransaction.getTransactionTime());

        assertTrue(bankTransaction.getId() instanceof UUID);

        /*
        Python 2.7.16 (default, Oct  7 2019, 17:36:04)
[GCC 8.3.0] on linux2
        Type "help", "copyright", "credits" or "license" for more information.
>>> 783.83 - 656.51
        127.32000000000005
                >>> 783.83 - 656.51 - 529.19
                -401.87
                >>> 656.51 - 529.19
        127.31999999999994
                >>> 529.19 - 401.87
        127.32000000000005
                >>> 401.87 - 274.55
        127.32
                >>> 274.55 - 147.23
        127.32000000000002
                >>> 147.23 - 19.91
        127.32
                >>>
*/

    }

    @Test
    @DisplayName("Test Transaction Credit Calc")
    public void testBankAccountCredit( ) {

        assertNotNull(bankTransaction);
        bankTransaction = spy(new BankTransaction());

        // These must be set at creation time
        assertNotNull(bankTransaction.getId());
        assertNotNull(bankTransaction.getTransactionStatus());
        assertNotNull(bankTransaction.getTransactionTime());

        assertTrue(bankTransaction.getId() instanceof UUID);
    }


}
