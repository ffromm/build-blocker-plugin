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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import hudson.tasks.Shell;
import org.jvnet.hudson.test.HudsonTestCase;

import java.util.concurrent.Future;

/**
 * Unit tests
 */
public class BlockingJobsMonitorTest extends HudsonTestCase {

    /**
     * One test for all for faster execution
     * @throws Exception
     */
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
