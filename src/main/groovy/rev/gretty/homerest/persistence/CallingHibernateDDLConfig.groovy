package rev.gretty.homerest.persistence

import com.google.inject.Provider
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.hibernate.dialect.Dialect
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType
import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.persistence.MappedSuperclass
import javax.persistence.Entity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class CallingHibernateDDLConfig {

    private static Logger log = LoggerFactory.getLogger( CallingHibernateDDLConfig.class )
    protected Configuration hibernateConfiguration = new Configuration()

    Configuration generateDDL( final Provider<?> mustProvide ) {

            def hibernateMetadata = this.getMetadataDLL( mustProvide )
            if ( hibernateMetadata instanceof MetadataSources ) {

                //log.info( ":: GEN SCHEMA :: for " +  ( mustProvide = null ) ? null : mustProvide.get().getClass() )
                SchemaExport ddExport = new SchemaExport();

                hibernateMetadata.getAnnotatedClassNames().each { one ->
                    log.info( "ANNT: " + one )
                }

                ddExport.create(EnumSet.of(TargetType.DATABASE), hibernateMetadata.buildMetadata())
                log.info( ":: SCHEMA addressed :: "  + hibernateMetadata.annotatedClassNames )
            }

            if ( hibernateMetadata instanceof Configuration ) {

                // TODO
                log.info( ":: CONFIG SCHEMA ::" )
            }

            return hibernateConfiguration
        }


    protected Object getMetadataDLL( final Provider<?> provider ) {

        Connection dbConnection = null;
        try {
            dbConnection = DriverManager.getConnection(
                    hibernateConfiguration.getProperty( "hibernate.connection.url" ),
                    hibernateConfiguration.getProperty( "hibernate.connection.username" ),
                    hibernateConfiguration.getProperty( "hibernate.connection.password" ));

        } catch (SQLException e) {

            System.err.println(e.getMessage());
            return hibernateConfiguration

        }

        final Reflections reflections = new Reflections( CallingHibernateStandalone.ENTITIES_IN )
        MetadataSources hibernateMetadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySetting("hibernate.dialect",
                                hibernateConfiguration.getProperty("hibernate.dialect"))
                        .applySetting("javax.persistence.schema-generation-connection", dbConnection )
                        .build());

        reflections.getTypesAnnotatedWith(MappedSuperclass.class).each {

            log.info( "Mapped super class: " + it )
        }

        if ( null != provider ) {

            reflections.getTypesAnnotatedWith(Entity.class).find { it ->
                        it.getCanonicalName().contentEquals(
                        provider.get().getClass().getCanonicalName() )

            }.each {

                log.info("Mapped entity class: " + it)
                hibernateMetadata.addAnnotatedClass(it);
                hibernateConfiguration.addAnnotatedClass(it)
            }

        } else {

            // None
            // TODO: DROP ALL TABLES?

        }

        log.info(":: GEN SCHEMA ::")
        SchemaExport ddExport = new SchemaExport();
        ddExport.create(EnumSet.of(TargetType.DATABASE), hibernateMetadata.buildMetadata())

        log.info(":: SCHEMA addressed :: " + hibernateMetadata.annotatedClassNames)

        return hibernateConfiguration;
    }

    final Dialect getDialect() {
        Dialect.getDialect( hibernateConfiguration.getProperties() )
    }

}
