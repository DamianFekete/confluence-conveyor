package org.randombits.confluence.conveyor;

/**
 * Provides an interface to Actions which override another Action.
 */
public interface OverridingActionDetails extends ActionDetails {

    /**
     * @return the action which has been overridden.
     */
    OverriddenActionDetails getOverriddenAction();

    /**
     * The weight of an action determines the order in which it is processed.
     *
     * @return The current weight value.
     */
    int getWeight();

    /**
     * Sets the weight of the action, to affect the order it is processed when
     * more than one action overrides another action.
     *
     * @param weight The weight.
     */
    void setWeight( int weight );

    /**
     * An overriding action can have a 'key' which makes it easier to bypass.
     * To bypass an action, simply add 'bypass=&lt;key&gt;' to the URL and Conveyor
     * will continue processing the list of action overrides after the specified action.
     *
     * @return The key.
     */
    String getKey();
}
