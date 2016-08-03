package org.jenkinsci.plugins.ewm;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * @author Alexandru Somai
 */
@Restricted(NoExternalUse.class)
public abstract class DiskInfoProvider extends AbstractDescribableImpl<DiskInfoProvider> implements ExtensionPoint {

    private final double readSpeed;
    private final double writeSpeed;

    protected DiskInfoProvider(double readSpeed, double writeSpeed) {
        this.readSpeed = readSpeed > 0 ? readSpeed : 0;
        this.writeSpeed = writeSpeed > 0 ? writeSpeed : 0;
    }

    /**
     * @return all registered {@link DiskInfoProvider}s.
     */
    public static ExtensionList<DiskInfoProvider> all() {
        return Jenkins.getActiveInstance().getExtensionList(DiskInfoProvider.class);
    }

    public double getReadSpeed() {
        return readSpeed;
    }

    public double getWriteSpeed() {
        return writeSpeed;
    }
}
