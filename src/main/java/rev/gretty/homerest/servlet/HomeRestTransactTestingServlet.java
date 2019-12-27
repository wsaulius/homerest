package rev.gretty.homerest.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.IbanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.service.ITransactionService;
import rev.gretty.homerest.service.impl.TransactionServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@WebServlet(name = "Transactions", value = "/transactions")
public class HomeRestTransactTestingServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(HomeRestTransactTestingServlet.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    ITransactionService itransactionService = new TransactionServiceImpl();

    public HomeRestTransactTestingServlet() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug( "GET reached: " + this.getClass() );

        final String egTestFrom = "LT563500042914824919";
        final String egTestTo = "LT683500042934824939";

        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        itransactionService.toString();

        // create HTML form: keep it simple, for now
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")

                .append("<html>\r\n")
                .append("		<head>\r\n")
                .append("			<title>Form input</title>\r\n")
                .append("		</head>\r\n")
                .append("		<body>\r\n")
                .append("			<form action=\"transactions\" method=\"POST\">\r\n")

                .append("				Enter your amount: \r\n")
                .append("				<input type=\"text\" name=\"amount\" />\r\n")

                .append(" <select name=\"currencies\" id=\"currencies\"> ")
                .append(" <option value=\"EUR\">EUR</option> ")
                .append(" <option value=\"USD\">USD</option> ")
                .append(" <option value=\"WTF\">WTF</option> ")
                .append(" <option value=\"DKK\">DKK</option> ")
                .append(" </select> ")

                .append("				Transact from: \r\n")
                .append("				<input type=\"text\" name=\"transactFrom\" />\r\n")

                .append("				Transfer to: \r\n")
                .append("				<input type=\"text\" name=\"transactTo\" />\r\n")

                .append("				<input type=\"submit\" value=\"Transfer\" />\r\n")
                .append("			</form>\r\n")
                .append("			<p/>\r\n")

                .append("			<div>\r\n")

                .append("			<p>\r\n")

                .append("FROM: e.g. " +  " from " + egTestFrom + "\r\n")
                .append("  TO: e.g. " +  " to " + egTestTo + "\r\n")

                .append("			</p>\r\n")
                .append("			</div>\r\n")

                .append("		</body>\r\n")
                .append("</html>\r\n");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BigDecimal amount = new BigDecimal( request.getParameter("amount")) ;
        String transferFrom = request.getParameter("transactFrom");
        String transferTo = request.getParameter("transactTo");

        IbanUtil.validate( transferFrom );
        IbanUtil.validate( transferTo );

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        BankTransaction newTransaction = new BankTransaction();

        newTransaction.setCreditTransaction();
        newTransaction.setTransactionAmount( amount );
        newTransaction.setTransactionFrom( transferFrom );
        newTransaction.setTransactionTo( transferTo );
        newTransaction.setCurrencyUnit( request.getParameter("currencies" ) );

        // create HTML response
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")
                .append("<html>\r\n")
                .append("		<head>\r\n")
                .append("			<title>Transfer message</title>\r\n")
                .append("		</head>\r\n")
                .append("		<body>\r\n");

        if (amount != null && StringUtils.isNotBlank ( amount.toString() ) ) {
            writer.append("	Transfer " + amount + " " +
                    newTransaction.getCurrencyUnit() + ".\r\n");
            writer.append(

    String.format("You successfully completed creating a transfer of " +
            "%s from %s to account %s.\r\n", amount, transferFrom, transferTo ) +
            "by initiating the following transaction \r\n\r\n" );

            writer.append( "<pre id=\"json\">" + newTransaction.toString() +"</pre>" );

            try {

                final Throwable ifException = itransactionService.processTransaction( newTransaction );

                if ( null != ifException && StringUtils.isNotBlank( ifException.getMessage() ) ) {

                    final String stackTheException = Throwables.getStackTraceAsString( ifException );

                    writer.append(" Your transfer of " +
                            newTransaction.getTransactionAmount() + " "
                            + newTransaction.getCurrencyUnit()
                            + " failed with exception: ");

                    writer.append( "<p style=\"color:red;font-family:courier;\"><b>")
                            .append(  ifException.getMessage() +"\r\n" )
                            .append( "</b></p>" );

                    writer.append( "<p style=\"color:red;font-family:courier;\"><code>")
                        .append( stackTheException )
                        .append( "</code></p>" );
                } else {

                    writer.append( "<p style=\"color:green;font-family:courier;\"><b><code>")
                            .append( "All OK" + "\r\n" )
                            .append( "</code></b></p>" );
                }

            } catch (Exception e) {

                writer.append( e.toString() );
            }

            writer
                .append("	<form action=\"/get/accounts\" method=\"POST\">\r\n")
                .append("		<input type=\"submit\" value=\"Check\" />\r\n")
                .append("	</form>\r\n");

        } else {
            writer.append("	You did not enter the correct data!\r\n");
        }
        writer.append("		</body>\r\n")
                .append("</html>\r\n");
    }


    /*
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestData = request.getReader().lines().collect(Collectors.joining());
        log.info( "POST is reached! : " + requestData );

        BankTransaction bankTransaction = objectMapper.readValue( requestData, BankTransaction.class );
        log.info( "Received via POST " + bankTransaction );
        */

/*
        //RequestDispatcher view = request.getRequestDispatcher("/homerest/post/transaction");
        //view.forward( request, response );
    }
*/

}