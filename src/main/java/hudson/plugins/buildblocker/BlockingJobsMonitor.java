package hudson.plugins.buildblocker;

import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.model.queue.WorkUnit;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jet
 * Date: 6/22/12
 * Time: 6:47 PM
 * To change this template use File | Settings | File Templates.
 */

public class BlockingJobsMonitor {
    private List<String> blockingJobs;

    public BlockingJobsMonitor(String blockingJobs) {
        if(StringUtils.isNotBlank(blockingJobs)) {
            this.blockingJobs = Arrays.asList(blockingJobs.split("\n"));
        }
    }

    public String getBlockingJob() {
        if(this.blockingJobs == null) {
            return null;
        }

        Computer[] computers = Jenkins.getInstance().getComputers();

        for (Computer computer : computers) {
            List<Executor> executors = computer.getExecutors();

            for (Executor executor : executors) {
                if(executor.isBusy()) {
                    Queue.Executable currentExecutable = executor.getCurrentExecutable();

                    String activeJobName = currentExecutable.getParent().getDisplayName();

                    for (String blockingJob : this.blockingJobs) {
                        if(activeJobName.matches(blockingJob)) {
                            return activeJobName;
                        }
                    }
                }
            }
        }

        return null;
    }
}
