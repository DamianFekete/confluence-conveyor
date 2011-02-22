package org.randombits.confluence.conveyor;

/**
 * A receipt is used to keep track of changes made via the {@link OverrideManager}
 * and allows the changes to be reversed.
 */
public interface Receipt {
    void returnReceipt();
}
