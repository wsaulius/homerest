package rev.gretty.homerest.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.AuthorityType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Utility class for Hibernate
 *
 */

@Converter
public class AuthorityTypeConverterUtils implements AttributeConverter<AuthorityType, String> {

    private static Logger log = LoggerFactory.getLogger( AuthorityTypeConverterUtils.class );

    @Override
    public String convertToDatabaseColumn(AuthorityType authorityType) {

        log.debug( this.getClass().toString() + " converts to DB: " + authorityType.toString() );

        if (authorityType == null) {
            return new String();
        }

        return authorityType.toString();
    }

    @Override
    public AuthorityType convertToEntityAttribute(String authorityType) {

        log.debug( this.getClass().toString() + " converts from DB: " + authorityType.toString() );

        if (authorityType == null) {
            return null;
        }

        return AuthorityType.valueOf( authorityType );
    }
}
