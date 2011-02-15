package org.randombits.confluence.conveyor;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;

import java.util.Collection;

/**
 * An action which has been overridden by other {@link OverridingActionDetails}.
 * This class provides access to the original {@link ActionDetails} that was overridden,
 * as well as the list of actions that are overriding it, sorted by weight.
 */
public interface OverriddenActionDetails extends ActionDetails {

    /**
     * Returns the details for the original action.
     *
     * @return
     */
    ActionDetails getOriginalAction();

    /**
     * Returns the list of {@link ActionDetails} which override this action.
     *
     * @return The list of overriding actions.
     */
    Collection<? extends OverridingActionDetails> getOverridingActions();

    /**
     * Returns the target action for the overridden action, taking into account
     * the bypass key. The bypass key can be either the class name of an action,
     * or the 'key' of an overriding action. If the bypass key is not
     * <code>null</code>, the list of overriding actions will be skipped until
     * a matching class name or key value is found. Each overriding action after
     * the matching action will be checked against any {@link com.atlassian.plugin.web.Condition}s,
     * and if it passes, it will be executed. If no matching overriding actions are found,
     * the original action is returned.
     *
     * @param bypass  The bypass key/class name, or <code>null/code> if nothing is being bypassed.
     * @param context
     * @return The matching action, or <code>null</code> if no overriding action was found matching
     *         the provided bypass.
     */
    ActionDetails getTargetAction( String bypass, WebInterfaceContext context ) throws ConveyorException;
}
