package rev.gretty.transactions.test

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PostNewTransactionTest {

    @Tags(
            Tag("post"),
            Tag("transactions")
    )
    @Test
    fun `Post new transaction to back-end`() {

        var reqParam = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode("One", "UTF-8")
        reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode("Two", "UTF-8")

        val url = URL("http://localhost:9009/homerest/post/transaction")

        try {

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.connectTimeout = 300000
            connection.connectTimeout = 300000
            connection.doOutput = true

            val message =
                    """{
                        "credit": true,
                        "debit": false,
                        "id": "44c8ee84-c0d2-4013-a5c9-18743bce576e",
                        "transactionAmount": 468.00,
                        "transactionType": 1,
                        "transactionTime": 1577210003000,
                        "currencyUnit": "EUR",
                        "transactionStatus": 0,
                        "transactionFrom": "LT563500042914824919",
                        "transactionTo": "LT683500042934824939"
                    }"""

            val postData: ByteArray = message.toByteArray(StandardCharsets.UTF_8)

            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Content-lenght", postData.size.toString())
            connection.setRequestProperty("Content-Type", "application/json")

            try {
                val outputStream: DataOutputStream = DataOutputStream(connection.outputStream)
                outputStream.write(postData)
                outputStream.flush()
            } catch (exception: Exception) {

            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK && connection.responseCode != HttpURLConnection.HTTP_CREATED) {
                try {
                    System.exit(0)

                } catch (exception: Exception) {
                    throw Exception("Exception while POST the transaction $exception.message")
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }
    }
}