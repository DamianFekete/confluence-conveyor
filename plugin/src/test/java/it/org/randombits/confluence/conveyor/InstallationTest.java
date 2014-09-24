package it.org.randombits.confluence.conveyor;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;
import it.com.servicerocket.randombits.AddOnTest;
import it.com.servicerocket.randombits.pageobject.AddOnOSGIPage;
import org.junit.Test;

/**
 * @author Yong Chong
 * @since 1.0.6.20140915
 */
public class InstallationTest extends AbstractWebDriverTest {

    @Test public void testFilteringCorePluginIsInstalled() throws Exception {
        assert new AddOnTest().isInstalled(
                product.login(User.ADMIN, AddOnOSGIPage.class),
                "Confluence Conveyor"
        );
    }
}
