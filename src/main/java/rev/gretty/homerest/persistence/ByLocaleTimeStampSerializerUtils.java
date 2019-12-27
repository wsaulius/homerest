package rev.gretty.homerest.persistence;

import java.text.SimpleDateFormat;

// TODO: Locale intro
public class ByLocaleTimeStampSerializerUtils extends ByLocaleDateSerializerUtils {

    ByLocaleTimeStampSerializerUtils() {
        super.formatter =  new SimpleDateFormat( BY_LOCALE_DATETIME );
    }

}