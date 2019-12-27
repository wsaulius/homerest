package rev.gretty.homerest.unit.test;

import org.apache.commons.io.IOExceptionWithCause;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Read environment variables test case")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnvironmentProfilingTests {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentProfilingTests.class);

    final static String RESOURCE_FILE = "application.properties";
    final static String ENV_PROPERTY = "application.env";

    @Order(1)
    @DisplayName("Attempt to inject environment variables")
    @Test
    public void testApplicationProperties() throws Exception {

        assertNull(System.getenv( ENV_PROPERTY ));
        injectEnvironmentVariable( ENV_PROPERTY, "TEST");

        assertThat( System.getenv(ENV_PROPERTY), is("TEST"));
    }

    @Order(2)
    @DisplayName("List environment variables")
    @Test
    public void testEnvironment() throws IOException {

        // Now load from application.properties file from class path
        final Properties properties = new Properties();

        InputStream stream = null;

        try {
            stream = this.getClass().getClassLoader().getResourceAsStream(RESOURCE_FILE);
            if (null != stream) {
                log.info("OPEN: " + stream.available());

                properties.load(stream);
                properties.list( System.out );

                System.setProperty( ENV_PROPERTY, properties.getProperty( ENV_PROPERTY ) );
                assertNotNull( System.getenv( ENV_PROPERTY ));

                // All good, env is modified!
                assertEquals( System.getenv( ENV_PROPERTY ), "TEST" );

            } else {
                log.error("STREAM closed.");
                throw new IOExceptionWithCause(RESOURCE_FILE + " file not found.",
                        new Throwable("Locating of such a resource in CLASSPATH failed."));
            }

        } catch (Exception e) {

            e.printStackTrace();
            log.debug(e.getMessage());
            throw e;

        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    // Helpers
    private static void injectEnvironmentVariable(String key, String value)  throws Exception {

        Class<?> processEnvironment = Class.forName("java.lang.ProcessEnvironment");

        Field unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment");
        Object unmodifiableMap = unmodifiableMapField.get(null);
        injectIntoUnmodifiableMap(key, value, unmodifiableMap);

        Field mapField = getAccessibleField(processEnvironment, "theEnvironment");
        Map<String, String> map = (Map<String, String>) mapField.get(null);
        map.put(key, value);
    }

    private static Field getAccessibleField(Class<?> clazz, String fieldName)
            throws NoSuchFieldException {

        Field field = clazz.getDeclaredField(fieldName);
        // log.debug( field.getName() + " of " + fieldName + " in " + clazz );

        // Modify access for injection
        field.setAccessible(true);
        return field;
    }

    private static void injectIntoUnmodifiableMap(String key, String value, Object map)
            throws ReflectiveOperationException {

        Class unmodifiableMap = Class.forName("java.util.Collections$UnmodifiableMap");
        Field field = getAccessibleField(unmodifiableMap, "m");

        // log.debug( field.getName() );
        Object obj = field.get(map);
        ((Map<String, String>) obj).put(key, value);
    }

}
