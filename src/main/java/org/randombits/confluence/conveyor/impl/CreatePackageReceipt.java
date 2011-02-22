package org.randombits.confluence.conveyor.impl;

import com.opensymphony.xwork.config.ConfigurationManager;
import org.randombits.confluence.conveyor.Receipt;

/**
 * Keeps track of a single created package and allows it to be removed.
 */
public class CreatePackageReceipt implements Receipt {

    private final DefaultOverrideManager overrideManager;

    private final String packageName;

    public CreatePackageReceipt( String packageName, DefaultOverrideManager overrideManager ) {
        this.packageName = packageName;
        this.overrideManager = overrideManager;
    }

    public void returnReceipt() {
        ConfigurationManager.getConfiguration().removePackageConfig( packageName );
        overrideManager.rebuildRuntimeConfiguration();
    }
}
