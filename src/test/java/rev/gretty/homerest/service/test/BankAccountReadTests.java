package rev.gretty.homerest.service.test;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;
import rev.gretty.homerest.persistence.CallingHibernateDDLConfig;
import rev.gretty.homerest.service.IAccountService;

import static com.google.inject.Guice.createInjector;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("BankAccountReadTests test case")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Singleton
public class BankAccountReadTests {

    private static final Logger log = LoggerFactory.getLogger(BankAccountReadTests.class);

    @Inject
    IAccountService iAccountServiceTest;

    @Mock
    private Injector injector;

    @BeforeAll
    public void init() {
        MockitoAnnotations.initMocks(this);

        injector = createInjector(new GuiceBoundConfigModule());
        iAccountServiceTest = injector.getInstance(IAccountService.class);
        Provider<CallingHibernateDDLConfig> ddlProvider
                = injector.getProvider(CallingHibernateDDLConfig.class);

        // DO NOT DROP and CREATE Single TABLE, we could do it if needed.
        ddlProvider.get().generateDDL(null);
    }

    @Order(1)
    @Test
    @DisplayName("BankAccount service read by account :from test case")
    void testServiceReadAccount() throws Exception {

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
}