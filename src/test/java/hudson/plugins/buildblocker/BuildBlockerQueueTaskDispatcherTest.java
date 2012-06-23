/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Frederik Fromm
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
 * Unit tests
 */
public class BuildBlockerQueueTaskDispatcherTest extends HudsonTestCase {

    /**
     * One test for all for faster execution.
     * @throws Exception
     */
    @Test
    public void testCanRun() throws Exception {
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

        CauseOfBlockage causeOfBlockage = dispatcher.canRun(item);

        assertNull(causeOfBlockage);

        BuildBlockerProperty property = new BuildBlockerProperty();

        property.setBlockingJobs(".*ocki.*");

        project.addProperty(property);

        causeOfBlockage = dispatcher.canRun(item);
        assertNotNull(causeOfBlockage);

        assertEquals("Blocking job " + blockingJobName + " is running.", causeOfBlockage.getShortDescription());

        while(!(future1.isDone() && future2.isDone() && future3.isDone())) {
            // wait until jobs are done.
        }
    }

    /**
     * Returns the future object for a newly created project.
     * @param blockingJobName the name for the project
     * @param shell the shell command task to add
     * @param label the label to bind to master or slave
     * @return the future object for a newly created project
     * @throws IOException
     */
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
