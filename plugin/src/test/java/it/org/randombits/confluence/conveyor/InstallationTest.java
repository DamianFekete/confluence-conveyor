package it.org.randombits.confluence.conveyor;

import com.atlassian.confluence.it.User;
import com.atlassian.confluence.webdriver.AbstractWebDriverTest;
import it.org.randombits.confluence.conveyor.pageobject.AddOnOSGiPage;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Yong Chong
 * @since 1.0.6.20140915
 */
public class InstallationTest extends AbstractWebDriverTest {

    AddOnOSGiPage addOnOSGiPage;

    @Test public void testFilteringCorePluginIsInstalled() throws Exception {
        addOnOSGiPage = product.login(User.ADMIN, AddOnOSGiPage.class);
        addOnOSGiPage.filterOsgiAddons("Confluence Conveyor");

        assertTrue(addOnOSGiPage.getFilteringCoreText().contains("Confluence Conveyor"));
    }
}
