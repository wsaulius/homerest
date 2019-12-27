package rev.gretty.homerest.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.iban4j.IbanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.impl.AccountServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * Servlet for testing in Standalone API web server mode, used by explicit setting in @GuiceBoundConfigModule
 * Initialized with or DEVT/PROD instance by default, switched on or off
 *
 */

@WebServlet(name = "Accounts", value = "/accounts")
public class HomeRestAccountsTestingServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(HomeRestAccountsTestingServlet.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    IAccountService iAccountService = new AccountServiceImpl();
    static String IBAN_BANK_CODE = "35000";

    public HomeRestAccountsTestingServlet() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("GET reached: " + this.getClass());

        final String egTestFrom = "LT563500042914824919";
        final String egTestTo = "LT683500042934824939";

        // set response headers
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // create HTML form: keep it simple, for now
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")

                .append("<html>\r\n")
                .append("		<head>\r\n")
                .append("			<title>Form input</title>\r\n")
                .append("		</head>\r\n")
                .append("		<body>\r\n")
                .append("			<form action=\"accounts\" method=\"POST\">\r\n")

                .append("				Enter your amount: \r\n")
                .append("				<input type=\"text\" name=\"amount\" />\r\n")

                .append(" <select name=\"currencies\" id=\"currencies\"> ")
                .append(" <option value=\"EUR\">EUR</option> ")
                .append(" <option value=\"USD\">USD</option> ")
                .append(" <option value=\"WTF\">WTF</option> ")
                .append(" <option value=\"DKK\">DKK</option> ")
                .append(" </select> ")
/*
                .append(" <select name=\"transactType\" id=\"transactType\" align=\"right\"> ")
                .append(" <option value=\"CREDIT\">CREDIT</option> ")
                .append(" <option value=\"DEBIT\">DEBIT</option> ")
                .append(" </select> ")
*/
                .append("<p>For account number enter 11 digit code template or full number")

                .append("				Digit code: \r\n")
                .append("				<input type=\"text\" name=\"digitCode\" align=\"right\" />\r\n")

                .append("				Account Nr: \r\n")
                .append("				<input type=\"text\" name=\"accountNr\" align=\"right\" />\r\n")

                .append("			<div>\r\n")
                .append("				<input type=\"submit\" value=\"Enter\" align=\"right\"/>\r\n")
                .append("			</div>\r\n")

                .append("			</form>\r\n")
                .append("			<p/>\r\n")

                .append("			<div>\r\n")
                .append("			<p>\r\n")
                .append("			</p>\r\n")
                .append("			</div>\r\n")
                .append("		</body>\r\n")
                .append("</html>\r\n");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BigDecimal amount = new BigDecimal(request.getParameter("amount"));
        String transferFrom = request.getParameter("accountNr");
        final BankAccount bankAccount = new BankAccount();

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // create HTML response
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>\r\n")
                .append("<html>\r\n")
                .append("		<head>\r\n")
                .append("			<title>Account message</title>\r\n")
                .append("		</head>\r\n")
                .append("		<body>\r\n");

        final String digitCode = request.getParameter("digitCode");
        if (StringUtils.isNotBlank(digitCode)) {

            final Iban ibanReturned = new Iban.Builder()
                    .countryCode(CountryCode.LT)
                    .bankCode(IBAN_BANK_CODE)
                    .accountNumber(digitCode)
                    .build();

            IbanUtil.validate(ibanReturned.toString());
            bankAccount.setAccountNumber(ibanReturned.toString());

        } else {

            IbanUtil.validate(transferFrom);
            bankAccount.setAccountNumber(transferFrom);
        }


        bankAccount.setCurrentBalance(amount);
        bankAccount.setCurrencyUnit(request.getParameter("currencies"));

        bankAccount.setUpdateAt(null);

        if (amount != null && StringUtils.isNotBlank(amount.toString())) {
            writer.append("	Account of balance " + amount + " " +
                    bankAccount.getCurrencyUnit() + ".\r\n");
            writer.append(

                    String.format("You successfully created an account of " + "%s %s in %s.\r\n",
                            bankAccount.getCurrentBalance(),
                            bankAccount.getCurrencyUnit(),
                            bankAccount.getAccountNumber()
                    ));

            writer.append("<pre id=\"json\">" + bankAccount.toString() + "</pre>");

            try {

                if (iAccountService.putAccount(bankAccount)) {

                    writer.append("<p style=\"color:green;font-family:courier;\"><b><code>")
                            .append("All OK" + "\r\n")
                            .append("</code></b></p>");

                } else {

                    writer.append("<p style=\"color:red;font-family:courier;\"><b><code>")
                            .append("Failure" + "\r\n")
                            .append("</code></b></p>");

                }

            } catch (Exception e) {

                if (null != e && StringUtils.isNotBlank(e.getMessage())) {

                    final String stackTheException = Throwables.getStackTraceAsString(e);

                    writer.append(" Account create of " +
                            bankAccount.getCurrentBalance() + " "
                            + bankAccount.getCurrencyUnit()
                            + " failed with exception: ");

                    writer.append("<p style=\"color:red;font-family:courier;\"><b>")
                            .append(e.getMessage() + "\r\n")
                            .append("</b></p>");

                    writer.append("<p style=\"color:red;font-family:courier;\"><code>")
                            .append(stackTheException)
                            .append("</code></p>");
                }

//              writer.append( e.toString() );
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