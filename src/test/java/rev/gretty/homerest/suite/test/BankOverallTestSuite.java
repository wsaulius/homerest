package rev.gretty.homerest.suite.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.runner.RunWith;
import org.junit.platform.runner.JUnitPlatform;

@RunWith(JUnitPlatform.class)
@SelectPackages({ "rev.gretty.homerest.unit.test","rev.gretty.homerest.service.test" })
@ExcludePackages({"rev.gretty.homerest.integration.test",})
@ExcludeTags("development")
public class BankOverallTestSuite {

    @Test
    @DisplayName( "RUN BANK ACCOUNT :: SUITE ")
    public void allOK() {

    }

}
