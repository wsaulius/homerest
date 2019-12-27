package rev.gretty.homerest.handler;

import com.google.inject.Injector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.guicebound.GuiceBoundConfigModule;
import rev.gretty.homerest.persistence.CallingHibernateStandalone;
import rev.gretty.homerest.persistence.ICallingHibernateStandalone;
import rev.gretty.homerest.persistence.IHibernateDDLConfiguraton;

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
public class AuthorityHandler extends AbstractHandler
{
    private static Logger log = LoggerFactory.getLogger( AuthorityHandler.class );
    private String body;

    private Injector injector;

    @Inject
    private ICallingHibernateStandalone calling;

    public AuthorityHandler()
    {
        this( AuthorityHandler.class.toString() );
    }

    public AuthorityHandler(final String type)
    {
        this( type,new String() );
        log.info( "Creating REST handler for " + type );
    }

    public AuthorityHandler(String type, String body)
    {
        this.body = body;
        if ( null == calling ) {
            // throw new NullArgumentException( "Injected value failed to initialize: calling" );
             injector = createInjector(new GuiceBoundConfigModule());
             calling = injector.getInstance( CallingHibernateStandalone.class );
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
        out.println( calling.selectAll("Authority" ).toString() );

        if (body != null)
        {
            out.println(body);

        }

        baseRequest.setHandled(true);
    }
}