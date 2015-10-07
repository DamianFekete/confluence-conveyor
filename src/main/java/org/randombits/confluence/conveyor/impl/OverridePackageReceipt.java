package org.randombits.confluence.conveyor.impl;

import org.randombits.confluence.conveyor.Receipt;
import org.randombits.confluence.conveyor.xwork.OverriddenPackageConfig;
import org.randombits.confluence.conveyor.xwork.OverridingPackageConfig;

/**
 * Provides a mechanism for removing an applied {@link OverridingPackageConfig} from
 * the specified {@link OverriddenPackageConfig}.
 */
public class OverridePackageReceipt implements Receipt {

    private final OverriddenPackageConfig overriddenPackage;

    private final OverridingPackageConfig overridingPackage;

    private final DefaultOverrideManager overrideManager;

    public OverridePackageReceipt( OverriddenPackageConfig overriddenPackage, OverridingPackageConfig overridingPackage, DefaultOverrideManager overrideManager ) {
        this.overriddenPackage = overriddenPackage;
        this.overridingPackage = overridingPackage;
        this.overrideManager = overrideManager;
    }

    public void returnReceipt() {
        overrideManager.revertOverridingPackage( overriddenPackage, overridingPackage );
    }
}
