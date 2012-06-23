package hudson.plugins.buildblocker;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import hudson.tasks.Shell;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: jet
 * Date: 6/22/12
 * Time: 6:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlockingJobsMonitorTest extends HudsonTestCase {
    @Test
    public void testConstructor() throws Exception {
        // init slave
        LabelAtom label = new LabelAtom("label");
        DumbSlave slave = this.createSlave(label);
        SlaveComputer c = slave.getComputer();
        c.connect(false).get(); // wait until it's connected
        if(c.isOffline()) {
            fail("Slave failed to go online: "+c.getLog());
        }

        String blockingJobName = "blockingJob";

        FreeStyleProject blockingProject = this.createFreeStyleProject(blockingJobName);

        Shell shell = new Shell("echo \"sleeping...\"\nsleep 1\necho \"done\"");
        blockingProject.getBuildersList().add(shell);

        Future<FreeStyleBuild> future = blockingProject.scheduleBuild2(0);

        while(! slave.getComputer().getExecutors().get(0).isBusy()) {
            // wait until job is started
        }

        BlockingJobsMonitor blockingJobsMonitorUsingNull = new BlockingJobsMonitor(null);
        assertNull(blockingJobsMonitorUsingNull.getBlockingJob());

        BlockingJobsMonitor blockingJobsMonitorNotMatching = new BlockingJobsMonitor("xxx");
        assertNull(blockingJobsMonitorNotMatching.getBlockingJob());

        BlockingJobsMonitor blockingJobsMonitorUsingFullName = new BlockingJobsMonitor(blockingJobName);
        assertEquals(blockingJobName, blockingJobsMonitorUsingFullName.getBlockingJob());

        BlockingJobsMonitor blockingJobsMonitorUsingRegex = new BlockingJobsMonitor("block.*");
        assertEquals(blockingJobName, blockingJobsMonitorUsingRegex.getBlockingJob());

        BlockingJobsMonitor blockingJobsMonitorUsingMoreLines = new BlockingJobsMonitor("xxx\nblock.*\nyyy");
        assertEquals(blockingJobName, blockingJobsMonitorUsingMoreLines.getBlockingJob());

        while (! future.isDone()) {
            // wait for blocking job to finish
        }

        assertNull(blockingJobsMonitorUsingFullName.getBlockingJob());
    }
}
