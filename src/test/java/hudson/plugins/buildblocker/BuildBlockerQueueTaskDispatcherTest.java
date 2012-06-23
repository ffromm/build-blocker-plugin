package hudson.plugins.buildblocker;

import hudson.model.*;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.CauseOfBlockage;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import junit.framework.TestCase;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: jet
 * Date: 6/22/12
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildBlockerQueueTaskDispatcherTest extends HudsonTestCase {

    @Test
    public void testCanTake() throws Exception {
        // init slave
        LabelAtom slaveLabel = new LabelAtom("slave");
        LabelAtom masterLabel = new LabelAtom("master");

        DumbSlave slave = this.createSlave(slaveLabel);
        SlaveComputer c = slave.getComputer();
        c.connect(false).get(); // wait until it's connected
        if(c.isOffline()) {
            fail("Slave failed to go online: "+c.getLog());
        }

        BuildBlockerQueueTaskDispatcher dispatcher = new BuildBlockerQueueTaskDispatcher();

        String blockingJobName = "blockingJob";

        Shell shell = new Shell("sleep 1");

        Future<FreeStyleBuild> future1 = createBlockingProject("xxx", shell, masterLabel);
        Future<FreeStyleBuild> future2 = createBlockingProject(blockingJobName, shell, masterLabel);
        Future<FreeStyleBuild> future3 = createBlockingProject("yyy", shell, slaveLabel);
        // add project to slave
        FreeStyleProject project = this.createFreeStyleProject();
        project.setAssignedLabel(slaveLabel);

        Queue.BuildableItem item = new Queue.BuildableItem(new Queue.WaitingItem(Calendar.getInstance(), project, new ArrayList<Action>()));

        CauseOfBlockage causeOfBlockage = dispatcher.canTake(slave, item);

        assertNull(causeOfBlockage);

        BuildBlockerProperty property = new BuildBlockerProperty();

        property.setBlockingJobs(".*ocki.*");

        project.addProperty(property);

        causeOfBlockage = dispatcher.canTake(slave, item);
        assertNotNull(causeOfBlockage);

        assertEquals("Blocking job " + blockingJobName + " is running.", causeOfBlockage.getShortDescription());

        while(!(future1.isDone() && future2.isDone() && future3.isDone())) {
            // wait until jobs are done.
        }
    }

    private Future<FreeStyleBuild> createBlockingProject(String blockingJobName, Shell shell, Label label) throws IOException {
        FreeStyleProject blockingProject = this.createFreeStyleProject(blockingJobName);
        blockingProject.setAssignedLabel(label);

        blockingProject.getBuildersList().add(shell);
        Future<FreeStyleBuild> future = blockingProject.scheduleBuild2(0);

        while(! blockingProject.isBuilding()) {
            // wait until job is started
        }

        return future;
    }
}
