package rev.gretty.homerest.persistence

import com.fasterxml.jackson.databind.ObjectMapper;

interface ICallingHibernateStandalone {

    final ObjectMapper objectMapper = new ObjectMapper()
    def selectAll( final String tableName )

}
