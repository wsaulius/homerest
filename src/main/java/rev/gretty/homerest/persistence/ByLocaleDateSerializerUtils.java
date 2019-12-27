package rev.gretty.homerest.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

// TODO: Locale intro
public class ByLocaleDateSerializerUtils extends JsonSerializer<Calendar> {

    static public String BY_LOCALE_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    static public String BY_LOCALE_DATE = "yyyy-MM-dd";

    protected SimpleDateFormat formatter = new SimpleDateFormat( BY_LOCALE_DATE );

    @Override
    public void serialize(Calendar calendar, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {

        String dateAsString = formatter.format(calendar.getTime());
        jsonGenerator.writeString(dateAsString);

    }
}