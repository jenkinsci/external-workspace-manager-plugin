package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.model.Run;
import org.jenkinsci.plugins.ewm.definitions.DiskPool;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

import java.util.List;

/**
 * TODO - To be added when I'll implement the step
 *
 * @author Alexandru Somai
 */
public class ExwsAllocateExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

    @Inject(optional = true)
    private transient ExwsAllocateStep exwsAllocateStep;

    @StepContextParameter
    private transient Run<?, ?> run;

    @Override
    protected Void run() throws Exception {
        List<DiskPool> diskPools = exwsAllocateStep.getDescriptor().getDiskPools();
        // TODO use globally defined disk pools
        System.out.println(diskPools);

        // TODO implement method
        return null;
    }
}
