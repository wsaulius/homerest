package rev.gretty.homerest.view;

import javax.inject.Inject;
import javax.inject.Singleton;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.fortest.AccountServiceImpl4Test;

@Singleton
public class AccountServiceHandler extends AbstractHandler
{
    @Inject
    IAccountService accountService;

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        try {

            accountService = new AccountServiceImpl4Test();
            Optional<BankAccount> bankAccount =
                    accountService.getAccountByNumber( "LT473500069985948443" );

            response.getWriter().println( "<pre id=\"json\">" + bankAccount.get() + "</pre>" );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}