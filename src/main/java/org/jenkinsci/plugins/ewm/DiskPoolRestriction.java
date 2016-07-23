package org.jenkinsci.plugins.ewm;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public abstract class DiskPoolRestriction extends AbstractDescribableImpl<DiskPoolRestriction> implements ExtensionPoint {

    public abstract boolean isAllowed(@Nonnull Run<?, ?> run, @Nonnull TaskListener listener);

    @Override
    public String toString() {
        return getDescriptor().getDisplayName();
    }
}
