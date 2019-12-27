package rev.gretty.homerest.handler;

import com.google.inject.Injector;
import groovy.json.JsonOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.impl.AccountServiceImpl;

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
public class AccountHandler extends AbstractHandler
{
    private static Logger log = LoggerFactory.getLogger( AccountHandler.class );
    private String body;

    private Injector injector;

    //@Inject
    //private ICallingHibernateStandalone calling;

    @Inject
    private IAccountService iAccountService = null;

    public AccountHandler()
    {
        this( AccountHandler.class.toString() );
    }

    public AccountHandler(final String type)
    {
        this( type,new String() );
        log.info( "Creating REST handler for " + type );
    }

    public AccountHandler(String type, String body)
    {
        this.body = body;
        if ( null == iAccountService ) {
            // throw new NullArgumentException( "Injected value failed to initialize: calling" );
             injector = createInjector(new GuiceBoundConfigModule());
             // calling = injector.getInstance( CallingHibernateStandalone.class );

             // FOR TEST // TODO:
             iAccountService =  injector.getInstance( AccountServiceImpl.class );
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException
    {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();

        try {

            out.println(
                    "<pre id=\"json\">" +
                        JsonOutput.prettyPrint( iAccountService.getAllAccounts().toString()) +
                     "</pre>" );

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (body != null)
        {
            out.println(body);
        }

        baseRequest.setHandled(true);
    }
}