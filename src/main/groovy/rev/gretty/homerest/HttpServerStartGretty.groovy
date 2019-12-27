package rev.gretty.homerest

import com.google.inject.Inject
import com.google.inject.name.Named
import org.codehaus.groovy.runtime.InvokerHelper
import org.eclipse.jetty.quickstart.QuickStartWebApp
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.webapp.WebAppContext
import rev.gretty.homerest.handler.AccountHandler
import rev.gretty.homerest.handler.TransactionHandler
import rev.gretty.homerest.servlet.HomeRestAccountsTestingServlet
import rev.gretty.homerest.servlet.HomeRestTransactTestingServlet
import rev.gretty.homerest.view.AccountServiceHandler

import javax.net.ssl.*

/**
 * Main entry point for the API server. Configured by a set of properties in application.properties file.
 *  The {@code @setDefaultPort} is injected by Guice setter injector for properties
 */

class HttpServerStartGretty extends Script {

    private Properties properties

    HttpServerStartGretty() {

        this.properties = new Properties()
        properties.load(
                this.getClass().getClassLoader().getResourceAsStream('application.properties').newReader())
    }

    @Inject
    String setDefaultPort(@Named("jetty.http.port") Integer port) {
        return Integer.toString(port)
    }

    def run() {

        Server jetty = new Server(
                Integer.parseInt(properties.getOrDefault("jetty.http.port", setDefaultPort(8080))))

        println "Starting embedded Jetty server on config $jetty.URI $jetty.connectors"

        def nullTrustManager = [
                checkClientTrusted: { chain, authType -> },
                checkServerTrusted: { chain, authType -> },
                getAcceptedIssuers: { null }
        ]

        def trustAll = [getAcceptedIssuers: {},
                        checkClientTrusted: { one, two -> },
                        checkServerTrusted: { one, two -> }]

        def nullHostnameVerifier = [
                verify: { hostname, session -> true }
        ]

        SSLContext sc = SSLContext.getInstance("SSL")
        sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

        QuickStartWebApp webapp = new QuickStartWebApp()
        webapp.setAutoPreconfigure(true)

        String rootPath = jetty.getClass().getClassLoader().getResource(".").toString();
        WebAppContext webapps = new WebAppContext(rootPath + "../../src/main/webapp", "");
        jetty.setHandler(webapps);

        HandlerCollection allHandlers = new HandlerCollection()
        final Handler[] initHandlers

        allHandlers.setHandlers(initHandlers)

        // allHandlers.prependHandler(new AccountHandler())
        // allHandlers.addHandler(new AccountServiceHandler())
        // allHandlers.addHandler( new UserHandler() )
        // allHandlers.addHandler(new TransactionHandler())
        // allHandlers.prependHandler( new AuthorityHandler() )

        ServletContextHandler servletPostContext = new ServletContextHandler(
                ServletContextHandler.NO_SESSIONS)

        servletPostContext.setContextPath("/homerest")
        servletPostContext.addServlet(new ServletHolder(new HomeRestAccountsTestingServlet()), "/accounts/*")

        ServletContextHandler contextTransactionServet = new ServletContextHandler(
                ServletContextHandler.NO_SESSIONS)

        contextTransactionServet.setContextPath("/homerest")
        servletPostContext.addServlet(new ServletHolder(new HomeRestTransactTestingServlet()), "/transactions/*")

        ContextHandler contextTransactions = new ContextHandler("/transactions")
        contextTransactions.setContextPath("/get/transactions")
        contextTransactions.setHandler(new TransactionHandler())

        ContextHandler contextAccounts = new ContextHandler("/accounts")
        contextAccounts.setContextPath("/get/accounts")
        contextAccounts.setHandler(new AccountHandler())

        /*
        ContextHandler userAccounts = new ContextHandler("/users")
        userAccounts.setContextPath("/get/users")
        userAccounts.setHandler(new UserHandler())

        */

        ContextHandler contextAccountServices = new ContextHandler("/services")
        contextAccountServices.setContextPath("/get/services")
        contextAccountServices.setHandler(new AccountServiceHandler())

        ContextHandlerCollection contexts = new ContextHandlerCollection(
                contextTransactions, contextAccounts, contextAccountServices, servletPostContext
        )

        jetty.setHandler(contexts)
        // jetty.setHandler(allHandlers)

        jetty.setDumpAfterStart(false)
        jetty.setDumpBeforeStop(false)
        jetty.setStopAtShutdown(true)

        jetty.start()
        jetty.join()
    }

    static void main(String[] args) {
        InvokerHelper.runScript(HttpServerStartGretty, args)
    }
}