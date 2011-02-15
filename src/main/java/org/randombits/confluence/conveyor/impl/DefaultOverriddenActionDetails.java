package org.randombits.confluence.conveyor.impl;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.plugin.Plugin;
import com.opensymphony.xwork.config.entities.ActionConfig;
import com.opensymphony.xwork.config.entities.PackageConfig;
import org.randombits.confluence.conveyor.*;
import org.randombits.confluence.conveyor.xwork.OverriddenActionConfig;
import org.randombits.confluence.conveyor.xwork.OverridingActionConfig;

import java.util.*;

/**
 * Represents an overridden @{link Action}
 */
public class DefaultOverriddenActionDetails extends BaseActionDetails implements OverriddenActionDetails {

    private static final String ORIGINAL_ACTION = "@original";

    private enum OverrideType {
        BYPASS( "^" ), MATCH( "@" );

        private final String prefix;

        OverrideType( String prefix ) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public String stripPrefix( String override ) {
            return override.substring( override.indexOf( prefix ) + 1 );
        }

        public static OverrideType forOverride( String override ) {
            if ( override == null )
                return null;
            for ( OverrideType type : values() ) {
                if ( override.startsWith( type.prefix ) )
                    return type;
            }
            return null;
        }
    }

    private DefaultOriginalActionDetails originalAction;

    private List<DefaultOverridingActionDetails> overrides;

    public DefaultOverriddenActionDetails( DefaultPackageDetails packageDetails, String actionName, ActionConfig originalActionConfig, Plugin plugin ) {
        super( packageDetails, actionName, new OverriddenActionConfig( packageDetails.getPackageConfig(), plugin ) );

        DefaultOriginalActionDetails originalActionDetails = new DefaultOriginalActionDetails( packageDetails, actionName, originalActionConfig, this );

        // Rename it to an alias.
        String originalActionAlias = findActionAlias( packageDetails, getActionName() );
        originalActionDetails.setActionName( originalActionAlias );

        originalAction = originalActionDetails;

        // Add the original into the package with it's cloned alias.
        packageDetails.getPackageConfig().addActionConfig( originalActionAlias, originalActionDetails.getActionConfig() );

        // Now, replace the old action with the OverriddenActionConfig.
        packageDetails.getPackageConfig().addActionConfig( getActionName(), getActionConfig() );

        overrides = new ArrayList<DefaultOverridingActionDetails>();
    }

    @Override
    public OverriddenActionConfig getActionConfig() {
        return (OverriddenActionConfig) super.getActionConfig();
    }

    @Override
    public boolean revert() throws ConveyorException {
        if ( super.revert() ) {
            PackageConfig packageConfig = getPackageDetails().getPackageConfig();

            // Remove any overrides
            for ( ActionDetails override : overrides ) {
                override.revert();
            }
            overrides.clear();

            // Remove the clone and put the original action config back with the original action name.
            originalAction.setOverriddenAction( null );
            originalAction.revert();
            originalAction.setActionName( getActionName() );
            packageConfig.addActionConfig( getActionName(), originalAction.getActionConfig() );

            return true;
        }
        return false;
    }

    private static String findActionAlias( PackageDetails packageDetails, String actionName ) {
        PackageConfig packageConfig = packageDetails.getPackageConfig();
        Map<String, ActionConfig> actionConfigs = packageConfig.getActionConfigs();

        int i = 1;
        while ( actionConfigs.containsKey( getAlias( actionName, i ) ) )
            i++;

        return getAlias( actionName, i );
    }

    private static String getAlias( String actionName, int i ) {
        return actionName + "_" + i;
    }

    protected void removeOverridingAction( DefaultOverridingActionDetails overridingActionDetails ) throws ConveyorException {
        checkReverted();

        overrides.remove( overridingActionDetails );

        if ( overrides.size() == 0 )
            revert();
    }

    private void checkReverted() throws ConveyorException {
        if ( isReverted() )
            throw new ConveyorException( "This action has been reverted and can no longer be modified." );
    }

    protected OverridingActionDetails addOverridingAction( OverridingActionConfig overridingActionConfig ) throws ConveyorException {
        checkReverted();

        PackageConfig packageConfig = getPackageDetails().getPackageConfig();

        String alias = findActionAlias( getPackageDetails(), getActionName() );
        packageConfig.addActionConfig( alias, overridingActionConfig );

        DefaultOverridingActionDetails details = new DefaultOverridingActionDetails( this, alias, overridingActionConfig );
        overrides.add( details );

        sortOverrides();

        return details;
    }

    private void sortOverrides() {
        Collections.sort( overrides, OVERRIDING_ACTION_DETAILS_COMPARATOR );
    }

    public ActionDetails getOriginalAction() {
        return originalAction;
    }

    public Collection<? extends OverridingActionDetails> getOverridingActions() {
        return Collections.unmodifiableList( overrides );
    }

    public ActionDetails getTargetAction( String overrideKey, WebInterfaceContext context ) throws ConveyorException {
        checkReverted();

        if ( ORIGINAL_ACTION.equals( overrideKey) )
            return originalAction;

        ActionDetails targetAction = null;

        Iterator<DefaultOverridingActionDetails> i = overrides.iterator();

        OverrideType overrideType = OverrideType.forOverride( overrideKey );

        // First, we skip down to the specified override key, if applicable
        if ( overrideType != null ) {
            String key = overrideType.stripPrefix( overrideKey );
            if ( i.hasNext() ) {
                DefaultOverridingActionDetails override;
                boolean matchesOverride = false;
                do {
                    override = i.next();
                    matchesOverride = key.equals( override.getKey() );
                } while ( i.hasNext() && !matchesOverride );

                if ( OverrideType.MATCH == overrideType ) {
                    if ( matchesOverride && meetsCondition( context, override ) )
                        return override;
                    else
                        throw new ConveyorException( "The '" + key + "' override was requested for '" + getActionName() + "' but its conditions do not allow it to run in this context." );
                }
            } else if ( OverrideType.MATCH == overrideType ) {
                throw new ConveyorException( "Unable to find an override with the specified key: " + key );
            }
        }

        // Check any conditions on the remaining overrides.
        if ( i.hasNext() ) {
            DefaultOverridingActionDetails override;
            boolean conditionMet = false;
            do {
                override = i.next();
                conditionMet = meetsCondition( context, override );
            } while ( i.hasNext() && !conditionMet );

            if ( conditionMet )
                targetAction = override;
        }

        if ( targetAction == null ) {
            targetAction = originalAction;
        }

        return targetAction;
    }

    private boolean meetsCondition( WebInterfaceContext context, DefaultOverridingActionDetails override ) {
        return override.getCondition() == null || override.getCondition().shouldDisplay( context.toMap() );
    }

    // Sorts overriding actions based on their weight.
    private static final Comparator<OverridingActionDetails> OVERRIDING_ACTION_DETAILS_COMPARATOR = new Comparator<OverridingActionDetails>() {
        public int compare( OverridingActionDetails oad1, OverridingActionDetails oad2 ) {
            return oad1.getWeight() - oad2.getWeight();
        }
    };
}
