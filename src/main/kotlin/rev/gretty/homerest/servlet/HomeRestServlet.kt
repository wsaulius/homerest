package rev.gretty.homerest

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import org.apache.logging.log4j.kotlin.Logging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.stream.Collectors
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(name = "Homerest", value = ["/kotlin"])
class HomeRestServlet constructor() : Logging, HttpServlet() {

    private val gson: Gson? = null

    companion object {

        val mapper = ObjectMapper()

        // Or this
        val log: Logger = LoggerFactory.getLogger( HomeRestServlet::class.toString() )
    }

    init {
        println("Initialize " + this.toString());
        logger.debug("Initializing Kotlin servlet " + this.toString())
    }

    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        res.writer.write("GET is reached!")
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        try {

            val requestData = request.reader.lines().collect(Collectors.joining())
            println("POST is reached! : " + requestData )

            //val transaction = mapper.readValue( requestData.toString(),
            //  rev.gretty.homerest.entity.BankTransaction::class )

        } catch (ex: Exception) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        } finally {
            response.contentType = "text/html;charset=UTF-8"
            response.writer.println("")
            response.writer.close()
        }
    }
}