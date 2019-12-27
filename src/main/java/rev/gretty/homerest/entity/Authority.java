package rev.gretty.homerest.entity;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import rev.gretty.homerest.persistence.AuthorityTypeConverterUtils;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "authority")
public class Authority implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Convert(converter = AuthorityTypeConverterUtils.class)
    private AuthorityType name;

    public Authority() {
    }

    public Authority(AuthorityType name) {
        this.name = name;
    }

    // Getters and setters
    @JsonGetter("id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonIgnore
    public AuthorityType getName() {
        return name;
    }

    public void setName(AuthorityType name) {
        this.name = name;
    }

    @Override
    public String toString() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Authority authority = null;
        try {
            authority = objectMapper.readValue("{}", Authority.class);

            authority.setId( this.getId() );
            authority.setName( this.getName() );

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String jsonString = new String();
        try {
            jsonString = objectMapper.writeValueAsString( authority );

        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        return jsonString;
    }
}