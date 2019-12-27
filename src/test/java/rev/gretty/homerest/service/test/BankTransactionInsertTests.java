package rev.gretty.homerest.service.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;
import rev.gretty.homerest.persistence.CallingHibernateDDLConfig;

import rev.gretty.homerest.service.ITransactionService;
import rev.gretty.homerest.unit.test.BankAccountsAsArgument;
import rev.gretty.homerest.unit.test.BankTransactionUnitTests;

import java.util.Iterator;

import static com.google.inject.Guice.createInjector;

@DisplayName("BankTransactionInsertTests test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Singleton
public class BankTransactionInsertTests {

    final BankTransactionUnitTests unitTests = new BankTransactionUnitTests();
    final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(BankAccountInsertTests.class);

    @Inject
    ITransactionService iTransactionServiceTest;

    @Mock
    private Injector injector;

    @Inject
    BankTransactionInsertTests( ) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @BeforeAll
    public void init() {
        MockitoAnnotations.initMocks(this);

        injector = createInjector(new GuiceBoundConfigModule());
        iTransactionServiceTest  = injector.getInstance( ITransactionService.class );
        Provider<CallingHibernateDDLConfig> ddlProvider
                = injector.getProvider( CallingHibernateDDLConfig.class );

        // DROP and CREATE Single TABLE
        // ddlProvider.get().generateDDL( injector.getProvider( BankTransaction.class ) );

        // DO NOT DROP and CREATE Single TABLE
        ddlProvider.get().generateDDL( null );
    }

    @Order(2)
    @ArgumentsSource(BankAccountsAsArgument.class)
    @ParameterizedTest
    void testInsertDirect(String ibanInput, boolean insertFlag) throws Exception {

        final Iterator<JSONObject>
            iiTransactions = unitTests.getTransactionsAsIBANS(ibanInput).stream().iterator();

        for ( int range = 0; iiTransactions.hasNext() && range < 20; range++ ) {

            final BankTransaction bankTransaction =
                    objectMapper.readValue( iiTransactions.next().toString(), BankTransaction.class);

            if ( insertFlag ) {
                iTransactionServiceTest.putTransaction( bankTransaction );
            }
        }
    }
}
