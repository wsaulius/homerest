package rev.gretty.homerest.persistence

import com.google.inject.ImplementedBy
import org.hibernate.cfg.Configuration

/**
 * Configure generalized Hibernate Interface
 *  The {@code @ImplementedBy} interface spec sets it to {@code @CallingHibernateDDLConfig}
 */

@ImplementedBy(CallingHibernateDDLConfig.class)
interface IHibernateDDLConfiguraton {
    Configuration generateDDL()
}
