package rev.gretty.homerest.service.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;
import rev.gretty.homerest.persistence.CallingHibernateDDLConfig;
import rev.gretty.homerest.service.ITransactionService;

import java.lang.ref.SoftReference;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.inject.Guice.createInjector;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("BankTransactionReadTests test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Singleton
public class BankTransactionReadTests {

    private static final Logger log = LoggerFactory.getLogger(BankAccountInsertTests.class);
    final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    ITransactionService iTransactionService;

    @Mock
    private Injector injector;

    @BeforeAll
    public void init() {
        MockitoAnnotations.initMocks(this);

        injector = createInjector(new GuiceBoundConfigModule());
        iTransactionService = injector.getInstance(ITransactionService.class);
        Provider<CallingHibernateDDLConfig> ddlProvider
                = injector.getProvider(CallingHibernateDDLConfig.class);

        // DROP and CREATE Single TABLE
        // ddlProvider.get().generateDDL( injector.getProvider( BankTransaction.class ) );

        // DO NOT DROP and CREATE Single TABLE
        ddlProvider.get().generateDDL(null);
    }

    @Order(2)
    @Test
    @DisplayName("Bank Transaction service read both accounts :from and :to test case")
    void testServiceReadAccountsInTransact() throws Exception {

        assertNotNull(iTransactionService);

        log.info( "GETALL :: " +  iTransactionService.getAllTransactions().size() );

        /*
        try {

            iTransactionService.getAllTransactions().stream().forEach( one -> {

                log.info( "ACCT :: ToProcess " + one.toString() );

                // One by one read: in *concurrent* mode
                // Auto-destroy it
                SoftReference<ReentrantLock> outerLock = new SoftReference<ReentrantLock>(new ReentrantLock());
                try {
                    outerLock.get().lockInterruptibly();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {

                    // PROCESS transaction.
                    iTransactionService.processTransaction( one );

                } catch (Exception e) {

                    e.printStackTrace();

                } finally {

                    outerLock.get().unlock();
                }

            });

        } catch (Exception e) {

            e.printStackTrace();
            throw e;

        }


         */
    }
}
