package rev.gretty.homerest.persistence;

import java.text.SimpleDateFormat;

// TODO: Locale intro

/**
 * Utility class for Hibernate, should introduce Locale and TimeZone serialization
 * (if needed)
 *
 */
public class ByLocaleTimeStampSerializerUtils extends ByLocaleDateSerializerUtils {

    ByLocaleTimeStampSerializerUtils() {
        super.formatter =  new SimpleDateFormat( BY_LOCALE_DATETIME );
    }

}