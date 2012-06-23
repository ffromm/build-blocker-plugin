package hudson.plugins.buildblocker;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: jet
 * Date: 6/22/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildBlockerProperty extends JobProperty<Job<?, ?>> {
    /**
     * the logger
     */
    private static final Logger LOG = Logger.getLogger(BuildBlockerProperty.class.getName());

    /**
     * the enable checkbox in the job's config
     */
    public static final String USE_BUILD_BLOCKER = "useBuildBlocker";

    /**
     * blocking jobs form field name
     */
    public static final String BLOCKING_JOBS_KEY = "blockingJobs";

    /**
     * the job names that block the build if running
     */
    private String blockingJobs;

    public String getBlockingJobs() {
        return blockingJobs;
    }

    public void setBlockingJobs(String blockingJobs) {
        this.blockingJobs = blockingJobs;
    }

    @Extension
    public static final class BuildBlockerDescriptor extends JobPropertyDescriptor {
        public BuildBlockerDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        @Override
        public BuildBlockerProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            BuildBlockerProperty buildBlockerProperty = new BuildBlockerProperty();

            if(formData.containsKey(USE_BUILD_BLOCKER)) {
                try {
                    buildBlockerProperty.setBlockingJobs(formData.getJSONObject(USE_BUILD_BLOCKER).getString(BLOCKING_JOBS_KEY));

                } catch(JSONException e) {
                    LOG.log(Level.WARNING, "could not get blocking jobs from " + formData.getString(BLOCKING_JOBS_KEY));
                }
            }

            return buildBlockerProperty;
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }
    }

}
