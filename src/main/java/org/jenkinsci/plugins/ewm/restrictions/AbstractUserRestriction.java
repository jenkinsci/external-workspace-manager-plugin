package org.jenkinsci.plugins.ewm.restrictions;

import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.ewm.DiskPoolRestriction;

import javax.annotation.Nonnull;

/**
 * @author Alexandru Somai
 */
public abstract class AbstractUserRestriction extends DiskPoolRestriction {

    @Override
    public boolean isAllowed(@Nonnull Run<?, ?> run, @Nonnull TaskListener listener) {
        Cause.UserIdCause userIdCause = run.getCause(Cause.UserIdCause.class);
        if (userIdCause == null) {
            listener.getLogger().println("The build was not triggered by a user. Restricting Disk Pool.");
            return false;
        }

        String userId = userIdCause.getUserId();
        return acceptUser(userId);
    }

    protected abstract boolean acceptUser(@Nonnull String userId);
}
