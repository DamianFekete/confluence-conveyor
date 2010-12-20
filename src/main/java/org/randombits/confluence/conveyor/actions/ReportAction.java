package org.randombits.confluence.conveyor.actions;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import org.randombits.confluence.conveyor.ConveyorManager;
import org.randombits.confluence.conveyor.config.ConveyorConfigurationProvider;

import java.util.Collection;

/**
 * Reports on currently installed Conveyor Packages.
 */
public class ReportAction extends ConfluenceActionSupport {

    private ConveyorManager conveyorManager;

    @Override
    public String execute() throws Exception {
        return SUCCESS;
    }

    public Collection<ConveyorConfigurationProvider> getProviders() {
        return conveyorManager.getProviders();
    }

    public void setConveyorManager( ConveyorManager conveyorManager ) {
        this.conveyorManager = conveyorManager;
    }
}
