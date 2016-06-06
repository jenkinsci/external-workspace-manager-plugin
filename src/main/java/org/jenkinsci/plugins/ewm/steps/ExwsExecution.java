package org.jenkinsci.plugins.ewm.steps;

import com.google.inject.Inject;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.WorkspaceList;
import hudson.util.DescribableList;
import org.jenkinsci.plugins.ewm.definitions.Template;
import org.jenkinsci.plugins.ewm.nodes.ExternalWorkspaceProperty;
import org.jenkinsci.plugins.workflow.steps.*;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The execution of {@link ExwsStep}.
 *
 * @author Alexandru Somai
 */
public class ExwsExecution extends AbstractStepExecutionImpl {

    @Inject(optional = true)
    private ExwsStep exwsStep;

    @StepContextParameter
    private transient Run<?, ?> run;
    @StepContextParameter
    private transient Computer computer;
    private BodyExecution body;

    @Override
    public boolean start() throws Exception {
        List<Template> templates = exwsStep.getDescriptor().getTemplates();
        // TODO - use global config templates
        System.out.println(templates);

        Node node = computer.getNode();
        if (node == null) {
            throw new Exception("Computer does not correspond to a live node");
        }

        DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties = node.getNodeProperties();
        for (NodeProperty<?> nodeProperty : nodeProperties) {
            if (nodeProperty instanceof ExternalWorkspaceProperty) {
                ExternalWorkspaceProperty exwsNodeProperty = (ExternalWorkspaceProperty) nodeProperty;

                // TODO - use exws node property
                System.out.println(exwsNodeProperty);
            }
        }

        // TODO implement method


        WorkspaceList.Lease lease = computer.getWorkspaceList().allocate(new FilePath(node.getRootPath(), "workspace"));
        body = getContext().newBodyInvoker()
                .withContext("workspace")
                .withCallback(new Callback(getContext(), lease))
                .withDisplayName(null)
                .start();
        return false;
    }

    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {
        if (body != null)
            body.cancel(cause);
    }

    private static final class Callback extends BodyExecutionCallback {

        private final StepContext context;
        private final WorkspaceList.Lease lease;

        Callback(StepContext context, WorkspaceList.Lease lease) {
            this.context = context;
            this.lease = lease;
        }

        @Override
        public void onSuccess(StepContext context, Object result) {
            lease.release();
            this.context.onSuccess(result);
        }

        @Override
        public void onFailure(StepContext context, Throwable t) {
            lease.release();
            this.context.onFailure(t);
        }

    }

    private static final long serialVersionUID = 1L;
}
