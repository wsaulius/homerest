package rev.gretty.homerest.persistence

import com.google.inject.ImplementedBy
import org.hibernate.cfg.Configuration

@ImplementedBy(CallingHibernateDDLConfig.class)
interface IHibernateDDLConfiguraton {
    Configuration generateDDL()
}
