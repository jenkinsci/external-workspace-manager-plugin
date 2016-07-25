package org.jenkinsci.plugins.ewm.restrictions;

import com.synopsys.arc.jenkinsci.plugins.jobrestrictions.util.UserSelector;
import hudson.Extension;
import org.jenkinsci.plugins.ewm.DiskPoolRestrictionDescriptor;
import org.jenkinsci.plugins.ewm.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static hudson.Util.fixNull;

/**
 * @author Alexandru Somai
 */
public class WhitelistUserRestriction extends AbstractUserRestriction {

    @Nonnull
    // TODO - should provide own UserSelector Describable?
    private final Set<UserSelector> usersList;

    @CheckForNull
    private transient Set<String> acceptedUserIds = null;

    @DataBoundConstructor
    public WhitelistUserRestriction(Set<UserSelector> usersList) {
        this.usersList = fixNull(usersList);
    }

    @Nonnull
    public Set<UserSelector> getUsersList() {
        return usersList;
    }

    @Nonnull
    private synchronized Set<String> getAcceptedUserIds() {
        if (acceptedUserIds == null) {
            Set<UserSelector> userSelectors = getUsersList();
            acceptedUserIds = new HashSet<>(userSelectors.size());
            for (UserSelector selector : userSelectors) {
                acceptedUserIds.add(selector.getSelectedUserId());
            }
        }
        return acceptedUserIds;
    }

    @Override
    protected boolean acceptUser(@Nonnull String userId) {
        return getAcceptedUserIds().contains(userId);
    }

    @Extension
    public static class DescriptorImpl extends DiskPoolRestrictionDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.restrictions_WhitelistUserRestriction_DisplayName();
        }
    }
}
