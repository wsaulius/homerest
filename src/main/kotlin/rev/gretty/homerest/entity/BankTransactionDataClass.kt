package rev.gretty.homerest.entity

import java.math.BigDecimal
import java.util.*

data class BankTransactionDataClass(val id: UUID,
                                    val transactionAmount: BigDecimal,
                                    val transactionType: Byte,
                                    val transactionTime: Calendar,
                                    val currencyUnit: String,
                                    val transactionStatus: Int,
                                    val transactionFrom: String,
                                    val transactionTo: String ) {

    /*
    constructor() : this("", BigDecimal.ZERO,
            0, null,
            null, null,
            null, null) { }

     */
    /*

        private val id: UUID? = null
        private val transactionAmount: BigDecimal? = null
        private val transactionType: Byte? = null
        private val transactionTime: Calendar? = null
        private val currencyUnit: String? = null
        private val transactionStatus: Int? = null
        private val transactionFrom: String? = null
        private val transactionTo: String? = null

     */

}

