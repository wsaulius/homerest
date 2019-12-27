package rev.gretty.homerest.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.iban4j.IbanUtil;

import org.jetbrains.annotations.NotNull;
import rev.gretty.homerest.exception.BankAccountDataException;
import rev.gretty.homerest.exception.CurrencyCodeException;
import rev.gretty.homerest.exception.CurrencyAmountException;
import rev.gretty.homerest.guicebound.bound.IBankAccount;
import rev.gretty.homerest.persistence.ByLocaleTimeStampSerializerUtils;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.persistence.*;

import java.math.BigDecimal;
import javax.money.CurrencyUnit;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
@GenericGenerator( name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
@Table(name = "bank_account")
@NamedQueries(
        {
            @NamedQuery(name = "SQL_GET_ACCOUNTS_BY_NUMBER", query = "from BankAccount"),
            @NamedQuery(name = "SQL_UPDATE_ACCOUNTS_BY_NUMBER", query = "from BankAccount"),

        })
public class BankAccount implements IBankAccount {

    @Id
    @GeneratedValue(generator = "uuid")
    @Column( name = "account_id")
    private UUID id = null;

    @JsonGetter("id")
    public UUID getId() {
        return id;
    }

    @Column(name="current_balance")
    private BigDecimal currentBalance;

    @Column(name="account_status")
    private Integer accountStatus;

    @Column(name="account_number", nullable = false, unique = true)
    private String accountNumber;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date_updated")
    private Calendar updateAt;

    @Column(name="currency_unit", length = 3, nullable = false, unique = false)
    private String currencyUnit;

    @Inject
    public BankAccount() {

    this.setId( UUID.randomUUID() );
    this.setUpdateAt( null );
    }

    public BankAccount(Integer accountStatus) {
        this();
        this.accountStatus = accountStatus;
    }

    @JsonSetter("id")
    public void setId(UUID id) {

        if ( id != null || StringUtils.isNotBlank( id.toString() )) {
            this.id = id;
        } else {
            throw new BankAccountDataException("Bank account ID is not set: " + id );
        }
    }

    @JsonGetter("currencyUnit")
    public String getCurrencyUnit() {
        return currencyUnit;
    }

    @JsonSetter("currencyUnit")
    public void setCurrencyUnit(String currencyUnit) {

        if ( null == currencyUnit ) {
            throw new CurrencyCodeException( "Currency code is not specified." );
        }

        try {
            CurrencyUnit checked = Monetary.getCurrency(currencyUnit);

            this.currencyUnit = checked.getCurrencyCode();
        } catch ( Exception e ) {

            throw new CurrencyCodeException( "Currency code validation error.", e );
        }
    }

    @JsonSerialize(using = ByLocaleTimeStampSerializerUtils.class)
    @JsonGetter("updateAt")
    public Calendar getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Calendar updateAt) {

        // Update is current // Always?
        if ( null == updateAt ) {
            this.updateAt = Calendar.getInstance();
            this.updateAt.setTime( new Date( ) );
        }
        else {
            this.updateAt = updateAt;
        }
    }

    @JsonGetter("balance")
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    @JsonSetter("balance")
    public void setCurrentBalance(BigDecimal currentBalance) {

        // If currency is not set - throw exception?
        MonetaryAmount currency = Monetary.getDefaultAmountFactory()
                .setCurrency( "EUR" )
                .setNumber( currentBalance )
                .create();

        if ( currency.isNegative() ) {
            throw new CurrencyAmountException(
                    "Current Overdraft implementation does not allow negative amounts. Rollbacked." );
        }

        this.setUpdateAt( null );
        this.currentBalance = currentBalance;
    }

    @JsonGetter("status")
    public Integer getAccountStatus() {
        return accountStatus;
    }

    @JsonSetter("status")
    public void setAccountStatus(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }

    @JsonGetter("accountNr")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonSetter("accountNr")
    public void setAccountNumber(String accountNumber) {

        // Check for correctness first
        IbanUtil.validate( accountNumber );
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        BankAccount account = null;
        try {
            account = new BankAccount();

            account.setAccountNumber( this.getAccountNumber() );
            account.setAccountStatus( this.getAccountStatus() );
            account.setCurrencyUnit( this.getCurrencyUnit() );
            account.setCurrentBalance( this.getCurrentBalance() );
            account.setId( this.getId() );
            account.setUpdateAt( null );

        } catch (IllegalArgumentException e) {
            throw e;
        }

        String jsonString = new String();
        try {
            jsonString = objectMapper.writeValueAsString( account );

        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        return jsonString;
    }

    @Override
    public int compareTo(@NotNull Object o) {

        // Compare by update dates
        return this.getUpdateAt().compareTo( ((BankAccount)o).getUpdateAt() );
    }
}
