package hudson.plugins.buildblocker;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 */
@Extension
public class BuildBlockerQueueTaskDispatcher extends QueueTaskDispatcher {
    /**
     * the logger
     */
    private static final Logger LOG = Logger.getLogger(BuildBlockerQueueTaskDispatcher.class.getName());

    @Override
    public CauseOfBlockage canTake(Node node, Queue.BuildableItem item) {
        LOG.log(Level.ALL, "canTake buildBlocker on item " + item);

        if(item.task instanceof AbstractProject) {
            AbstractProject project = (AbstractProject) item.task;

            BuildBlockerProperty property = (BuildBlockerProperty) project.getProperty(BuildBlockerProperty.class);

            if(property != null) {
                String blockingJobs = property.getBlockingJobs();

                String blockingJob = new BlockingJobsMonitor(blockingJobs).getBlockingJob();

                if(blockingJob != null) {
                    LOG.log(Level.WARNING, "build blocked because job " + blockingJob + " is running.");
                    return CauseOfBlockage.fromMessage(Messages._BlockingJobIsRunning(blockingJob));
                }
            }
        }

        return super.canTake(node, item);
    }
}
