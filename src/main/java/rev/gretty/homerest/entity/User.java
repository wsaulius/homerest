package rev.gretty.homerest.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import rev.gretty.homerest.persistence.ByLocaleDateSerializerUtils;

/**
 * Entity for the User profile which a POJO used for Hibernate and JSON
 * It should allow secure and authenticated access later on, using LDAP
 * or Database table credentials
 */

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="date_created")
    private Calendar dateCreated;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "authority_id") })

    private Set<Authority> authorities = new HashSet<>();

    public User() {
    }

    // getters and setters
    @JsonGetter("id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @JsonGetter("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonGetter("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonSerialize(using = ByLocaleDateSerializerUtils.class)
    @JsonGetter("dateCreated")
    public Calendar getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Calendar dateCreated) {
        this.dateCreated = dateCreated;
    }

    @JsonIgnore
    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String toString() {

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        User user = null; 
        try {
            user = objectMapper.readValue("{}", User.class);
            
            user.setId( this.getId() );
            user.setUsername( this.getUsername() );
            user.setPassword( this.getPassword() );
            
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String jsonString = new String();
        try {
            jsonString = objectMapper.writeValueAsString( user );

        } catch (JsonProcessingException e) {

            e.printStackTrace();
        }

        return jsonString;
    }
}

