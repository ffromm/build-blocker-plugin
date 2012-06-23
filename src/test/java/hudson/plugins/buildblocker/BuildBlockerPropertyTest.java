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

import net.sf.json.JSONObject;
import org.easymock.EasyMock;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.StaplerRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests
 */
public class BuildBlockerPropertyTest extends HudsonTestCase {

    /**
     * Simple property test
     * @throws Exception
     */
    public void testUseBuildBlocker() throws Exception {
        BuildBlockerProperty property = new BuildBlockerProperty();

        property.setUseBuildBlocker(true);
        assertTrue(property.isUseBuildBlocker());
    }

    /**
     * Simple property test
     * @throws Exception
     */
    public void testBlockingJobs() throws Exception {
        BuildBlockerProperty property = new BuildBlockerProperty();

        property.setBlockingJobs("blockingJobs");
        assertEquals("blockingJobs", property.getBlockingJobs());
    }

    /**
     * Use different form data to test descriptor newInstance
     * @throws Exception
     */
    public void testDescriptorNewInstance() throws Exception {
        JSONObject formData = new JSONObject();
        StaplerRequest staplerRequest = EasyMock.createNiceMock(StaplerRequest.class);

        BuildBlockerProperty property = new BuildBlockerProperty();
        property = (BuildBlockerProperty) property.getDescriptor().newInstance(staplerRequest, formData);
        assertNull(property.getBlockingJobs());

        Map<String, Map<String, String>> formDataMap = new HashMap<String, Map<String, String>>();
        Map<String, String> subMap = new HashMap<String, String>();

        formDataMap.put("useBuildBlocker", subMap);
        formData.accumulateAll(formDataMap);

        property = (BuildBlockerProperty) property.getDescriptor().newInstance(staplerRequest, formData);
        assertFalse(property.isUseBuildBlocker());
        assertNull(property.getBlockingJobs());

        // json data in request: "{\"useBuildBlocker\":{\"blockingJobs\":\".*ocki.*\"}}"
        String key = "blockingJobs";
        String value = ".*ocki.*";

        subMap.put(key, value);
        formDataMap.put("useBuildBlocker", subMap);

        formData = new JSONObject();
        formData.accumulateAll(formDataMap);

        property = (BuildBlockerProperty) property.getDescriptor().newInstance(staplerRequest, formData);
        assertTrue(property.isUseBuildBlocker());
        assertNotNull(property.getBlockingJobs());
        assertEquals(value, property.getBlockingJobs());
    }
}
