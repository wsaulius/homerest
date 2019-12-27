package rev.gretty.homerest.persistence

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configure generalized Hibernate Interface
 *
 */
interface ICallingHibernateStandalone {

    final ObjectMapper objectMapper = new ObjectMapper()
    def selectAll( final String tableName )

}
