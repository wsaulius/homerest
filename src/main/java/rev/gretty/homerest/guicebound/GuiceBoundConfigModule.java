package rev.gretty.homerest.guicebound;

import com.google.inject.AbstractModule;

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.guicebound.bound.IBankAccount;
import rev.gretty.homerest.guicebound.bound.IBankTransaction;
import rev.gretty.homerest.persistence.CallingHibernateStandalone;
import rev.gretty.homerest.persistence.fortest.CallingHibernateStandalone4Test;
import rev.gretty.homerest.persistence.ICallingHibernateStandalone;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.ITransactionService;
import rev.gretty.homerest.service.fortest.AccountServiceImpl4Test;
import rev.gretty.homerest.service.impl.AccountServiceImpl;
import rev.gretty.homerest.service.fortest.TransactionServiceImpl4Test;
import rev.gretty.homerest.service.impl.TransactionServiceImpl;

import javax.inject.Singleton;

/**
 * The purpose of this Configuration module is dynamic configuration of by Injecting
 * different services for different profiles: TEST and DEVT / PROD
 *
 * Customized binding is on application.env System environment variable
 *
 * In DEVT DDL schema drop is done at the startup time.
 *
 */

@Singleton
public class GuiceBoundConfigModule extends AbstractModule {

    final static String ENV_PROPERTY = "application.env";
    private static Logger log = LoggerFactory.getLogger(GuiceBoundConfigModule.class);

    @Override
    protected void configure() {

        log.info("Initialize " + this.getClass());
        final String environment = System.getenv(ENV_PROPERTY);

        Provider provider = null;

        // Default environment
        if (null == environment) {

            // Reconfigure DDL
            bind(ICallingHibernateStandalone.class)
                    .to(CallingHibernateStandalone.class)
                    .in(Singleton.class);

            // Actual entities
            bind( IBankAccount.class ).to( BankAccount.class );
            bind( IBankTransaction.class ).to( BankTransaction.class );

            bind( IAccountService.class).to( AccountServiceImpl.class );
            bind( ITransactionService.class ).to( TransactionServiceImpl.class );

            provider = this.getProvider( CallingHibernateStandalone.class );

        } else

        if (environment.equalsIgnoreCase("test")) {

            log.info( "CONFIG for :: " + environment.toUpperCase() );

            // Services
            bind( IAccountService.class).to( AccountServiceImpl4Test.class );
            bind( ITransactionService.class ).to( TransactionServiceImpl4Test.class );

            // Test mock tables ( if used )
            bind( IBankAccount.class ).to( BankAccount.class );
            bind( IBankTransaction.class).to( BankTransaction.class );

            // Reconfigure DDL - DROP and CREATE
            bind(ICallingHibernateStandalone.class)
                    .to(CallingHibernateStandalone4Test.class)
                    .in(Singleton.class);

            provider = this.getProvider( CallingHibernateStandalone4Test.class );

        }
        
        log.info( "CONFIG provider :: " + environment + " :: " + provider );
    }
}

