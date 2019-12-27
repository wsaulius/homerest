package rev.gretty.homerest.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.Inject;
import org.hibernate.annotations.GenericGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.exception.BankTransactionDataException;
import rev.gretty.homerest.exception.CurrencyAmountException;
import rev.gretty.homerest.exception.CurrencyCodeException;
import rev.gretty.homerest.guicebound.bound.IBankTransaction;

import javax.money.*;
import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Entity for the Bank Transacton which is a POJO used for Hibernate and JSON
 *
 */

@Entity
@GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
@Table(name = "bank_transaction")
@NamedQueries(
        {
                @NamedQuery(name = "SQL_GET_ACCOUNTS_IN_TRANSACTION", query = "from BankTransaction"),
                @NamedQuery(name = "SQL_UPDATE_ACCOUNTS_IN_TRANSACTION", query = "from BankTransaction")
        })
@JsonIgnoreProperties
public class BankTransaction implements IBankTransaction {

    private final static MonetaryOperator minorRounding =
            Monetary.getRounding(RoundingQueryBuilder.of().set("scale", 2)
                    .set(RoundingMode.HALF_UP).build());

    private static final Logger log = LoggerFactory.getLogger(BankTransaction.class);

    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "transaction_id")
    private UUID id;

    @Column(name = "transaction_amount")
    private BigDecimal transactionAmount;

    @Min(value = -1)      // Negative : Debit
    @Max(value = 1)       // Positive : Credit
    @Column(name = "transaction_type")
    private Byte transactionType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "transaction_time")
    private Calendar transactionTime;

    @Column(name = "currency_unit", length = 3, nullable = false, unique = false)
    private String currencyUnit;

    @Column(name = "transaction_status")
    private Integer transactionStatus;

    @Column(name = "transaction_from")
    private String transactionFrom;

    @Column(name = "transaction_to")
    private String transactionTo;

    @Inject
    public BankTransaction() {

        this.setId(UUID.randomUUID());

        // Set to neutral - no debit, no credit, no amount
        this.setTransactionStatus(0);
        this.setTransactionType(null);
        this.setTransactionTime(null);
    }

    public BankTransaction(Integer transactionStatus) {
        this();
        this.transactionStatus = transactionStatus;
    }

    @JsonGetter("id")
    public UUID getId() {
        return id;
    }

    @JsonSetter("id")
    public void setId(UUID id) {
        this.id = id;
    }

    @JsonGetter("transactionAmount")
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    @JsonSetter("transactionAmount")
    public void setTransactionAmount(BigDecimal transactionAmount) {

        try {
            transactionAmount.longValue();
        } catch (NullPointerException np) {

            log.debug(np.getMessage());
            transactionAmount = BigDecimal.ZERO;
        }

        // If currency is not set - throw exception?
        MonetaryAmount currency = Monetary.getDefaultAmountFactory()
                .setCurrency("EUR")
                .setNumber(transactionAmount)
                .create();

        if (currency.isNegative()) {
            throw new CurrencyAmountException(
                    "Current implementation does not allow negative amounts. Rollbacked. ");
        }

        this.transactionAmount = transactionAmount;
    }

    public void setTransactionAmount(MonetaryAmount transactionAmount) {

        MonetaryAmount currency = transactionAmount.with(minorRounding);

        // If currency is not set - throw exception?
        if (currency.isNegative()) {
            throw new CurrencyAmountException(
                    "Current implementation does not allow negative amounts. Rollbacked. ");
        }

        BigDecimal bigDecimalValue = BigDecimal.ZERO;
        try {
            bigDecimalValue = currency.getNumber().numberValue(BigDecimal.class);

        } catch (ArithmeticException e) {
            e.printStackTrace();
            throw e;
        }

        this.transactionAmount = bigDecimalValue;
    }

    @JsonGetter("transactionType")
    public Byte getTransactionType() {
        return transactionType;
    }

    @JsonSetter("transactionType")
    public void setTransactionType(Byte transactionType) {
        if (null == transactionType) {
            this.setDebitTransaction();
        } else
            this.transactionType = transactionType;
    }

    public void setCreditTransaction() {
        this.transactionType = new Byte("1");
    }

    public void setDebitTransaction() {
        this.transactionType = new Byte("-1");
    }

    @JsonGetter
    @JsonIgnore
    public Boolean isCredit() {

        if (null == this.transactionType) {
            throw new BankTransactionDataException("Credit transaction type cannot be null value.");
        }

        return this.transactionType.byteValue() == 1;
    }

    @JsonGetter
    @JsonIgnore
    public Boolean isDebit() {

        if (null == this.transactionType) {
            throw new BankTransactionDataException("Debit transaction type cannot be null value.");
        }

        return this.transactionType.byteValue() == -1;
    }

    @JsonGetter("transactionTime")
    public Calendar getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Calendar transactionTime) {

        // Update current time
        if (null == transactionTime) {
            this.transactionTime = Calendar.getInstance();
            this.transactionTime.setTime(new Date());
        } else {
            this.transactionTime = transactionTime;
        }
    }

    @JsonGetter("currencyUnit")
    public String getCurrencyUnit() {
        return currencyUnit;
    }

    @JsonSetter("currencyUnit")
    public void setCurrencyUnit(String currencyUnit) {

        if (null == currencyUnit) {
            throw new CurrencyCodeException("Currency code is not specified.");
        }

        try {
            CurrencyUnit checked = Monetary.getCurrency(currencyUnit);

            this.currencyUnit = checked.getCurrencyCode();
        } catch (Exception e) {

            throw new CurrencyCodeException("Currency code validation error.", e);
        }
    }

    @JsonGetter("transactionStatus")
    public Integer getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(Integer transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @JsonGetter("transactionFrom")
    public String getTransactionFrom() {
        return transactionFrom;
    }

    public void setTransactionFrom(String transactionFrom) {
        this.transactionFrom = transactionFrom;
    }

    @JsonGetter("transactionTo")
    public String getTransactionTo() {
        return transactionTo;
    }

    public void setTransactionTo(String transactionTo) {
        this.transactionTo = transactionTo;
    }

    @Override
    public String toString() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        BankTransaction bankTransaction = null;
        try {
            bankTransaction = new BankTransaction();

            bankTransaction.setId(this.getId());
            try {
                bankTransaction.setCurrencyUnit(this.getCurrencyUnit());
            } catch (CurrencyCodeException e) {

                // Default to EUR
                bankTransaction.setCurrencyUnit(Currency.getInstance(Locale.GERMANY).getCurrencyCode());
            }

            try {
                bankTransaction.setTransactionAmount(this.getTransactionAmount());
            } catch (NullPointerException e) {

                e.printStackTrace();
                // Default to zero
                bankTransaction.setTransactionAmount(BigDecimal.ZERO);
            }

            bankTransaction.setTransactionFrom(this.getTransactionFrom());
            bankTransaction.setTransactionTo(this.getTransactionTo());
            bankTransaction.setTransactionType(this.getTransactionType());
            bankTransaction.setTransactionStatus(this.getTransactionStatus());
            bankTransaction.setTransactionTime(this.getTransactionTime());

            // Defaulted to DEBIT
            try {
                bankTransaction.setTransactionType(this.getTransactionType());
            } catch (BankTransactionDataException e) {
                bankTransaction.setDebitTransaction();
            }

        } catch (IllegalArgumentException e) {
            throw e;
        }

        String jsonString = new String();
        try {
            jsonString = objectMapper.writeValueAsString(bankTransaction);

        } catch (JsonProcessingException e) {

            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        return jsonString;
    }

}
