package rev.gretty.homerest.handler;

import com.google.inject.Injector;
import groovy.json.JsonOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;
import rev.gretty.homerest.service.ITransactionService;
import rev.gretty.homerest.service.impl.TransactionServiceImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.inject.Guice.createInjector;

/**
 * REST handler for Jetty web application
 *
 * Customized binding is on application.env System environment variable
 *
 */

@Singleton
public class TransactionHandler extends AbstractHandler {
    private static Logger log = LoggerFactory.getLogger(TransactionHandler.class);
    private String body;

    private Injector injector;

    // @Inject
    //private ICallingHibernateStandalone calling;

    @Inject
    private ITransactionService iTransactService = null;

    public TransactionHandler() {
        this(TransactionHandler.class.toString());
    }

    public TransactionHandler(final String type) {
        this(type, "");
        log.info("Creating REST handler for " + type);
    }

    public TransactionHandler(String type, String body) {
        this.body = body;
        if (null == iTransactService) {
            // throw new NullArgumentException( "Injected value failed to initialize: calling" );
            injector = createInjector(new GuiceBoundConfigModule());
            // calling = injector.getInstance( CallingHibernateStandalone.class );

            // FOR TEST // TODO:
            iTransactService = injector.getInstance(TransactionServiceImpl.class);
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        try {

            out.println(
               "<pre id=\"json\">" +
               JsonOutput.prettyPrint(iTransactService.getAllTransactions().toString()) +
               "</pre>" );
/*
            listOfBankTranscations.stream().forEach(oneBankTransactionToProcess -> {
                // log.info(" 4Test DB:: " + oneBankTransactionToProcess );

                try {

                    // PROCESS in DB
                    this.processTransaction( oneBankTransactionToProcess );

                } catch (Exception e) {

                    e.printStackTrace();
                    // do not throw out of this lambda expr

                }
            });
*/

        } catch (Exception e) {

            e.printStackTrace();
        }

        if (body != null) {
            out.println(body);
        }

        baseRequest.setHandled(true);
    }
}