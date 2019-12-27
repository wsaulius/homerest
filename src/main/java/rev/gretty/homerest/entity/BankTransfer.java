package rev.gretty.homerest.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "bank_transfer")
@GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
public class BankTransfer implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "transfer_id")
    private UUID id;

    @Column(name = "transaction_id")
    private UUID transactionID;

    @Column(name = "transfer_status")
    private int transferStatus;

    @JsonGetter("id")
    public UUID getId() {
        return id;
    }

    @JsonSetter("id")
    public void setId(UUID id) {
        this.id = id;
    }

    @JsonGetter("transactionID")
    public UUID getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(UUID transactionID) {
        this.transactionID = transactionID;
    }

    @JsonGetter("transactionStatus")
    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }

    @Override
    public String toString() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        BankTransfer transfer = null;
        try {
            transfer = new BankTransfer();

            transfer.setId( transfer.getId() );
            transfer.setTransactionID( transfer.getTransactionID() );
            transfer.setTransferStatus( transfer.getTransferStatus() );

        } catch (IllegalArgumentException e) {
            throw e;
        }

        String jsonString = new String();
        try {
            jsonString = objectMapper.writeValueAsString( transfer );

        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        return jsonString;
    }
}
