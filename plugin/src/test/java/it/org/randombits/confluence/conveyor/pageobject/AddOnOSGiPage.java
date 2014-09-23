package it.org.randombits.confluence.conveyor.pageobject;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.upm.pageobjects.OSGiPage;

/**
 * @author Yong Chong
 * @since 1.0.3.20140915
 */
public class AddOnOSGiPage extends OSGiPage {

    @ElementBy(cssSelector = "h4.upm-plugin-name") PageElement filteringCoreElement;

    public String getFilteringCoreText() {
        return filteringCoreElement.getText();
    }
}
