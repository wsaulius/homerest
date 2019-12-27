package rev.gretty.homerest.persistence.fortest

import com.fasterxml.jackson.databind.SerializationFeature
import com.google.inject.Inject
import com.google.inject.Injector
import groovy.util.logging.Slf4j
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule
import rev.gretty.homerest.persistence.HibernateSessionUtils
import rev.gretty.homerest.persistence.ICallingHibernateStandalone

import javax.inject.Singleton

import static com.google.inject.Guice.createInjector

@Slf4j
@Singleton
class CallingHibernateStandalone4Test implements ICallingHibernateStandalone {

    def final static java.lang.String ENTITIES_IN = "rev.gretty.homerest.entity";
    private static generatedDDL = false

    @Inject
    CallingHibernateStandalone4Test() {

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            Injector injector = createInjector(new GuiceBoundConfigModule());
            def config = injector.getInstance( CallingHibernateDDLConfig.class )
            config.generateDDL()
            generatedDDL = true
    }

    def selectAll(final String entity ) {

        SessionFactory sessionFactory = HibernateSessionUtils.getSessionFactory()
        final StringBuilder  longString = new StringBuilder()

        Transaction transaction = null
        try {

            // TODO: Could be dynamic class find, RMI
            final Class<?> classType = Class.forName( String.format( "%s.%s", ENTITIES_IN, entity ));
            Session session = sessionFactory.openSession()

            // start a transaction
            transaction = session.beginTransaction();

            def query = String.format( "FROM %s", entity )
            def queryResults = session.createQuery( query, classType );

            def queryList = queryResults.list()
            if ( objectMapper.canSerialize( queryList.class ) ) {
                return this.class.toString() + ":"  + objectMapper.writeValueAsString( queryList );

            } else {

                queryList.each { one ->
                    log.info( "HBN:" + one )
                    longString.append( one.toString() )
                }
            }

            // close transaction
            transaction.commit();

        } catch (Exception e) {

            e.printStackTrace();

            if (transaction != null) {
                transaction.rollback();
            }
        }

        return longString.toString()
    }

}