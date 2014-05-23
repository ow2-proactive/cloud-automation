package org.ow2.proactive.brokering.occi.client;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import org.junit.Test;

public class ResourceInstanceTest {

    private static final String IN_HEADER_EXAMPLE = "X-OCCI-Location: http://10.200.96.143:8081/occi/api/occi/platform/7cb93650-42e6-4450-913c-299caf871299";
    private static final String LOCATION_EXAMPLE = "http://10.200.96.143:8081/occi/api/occi/platform/7cb93650-42e6-4450-913c-299caf871299";
    private static final String UUID_EXAMPLE = "7cb93650-42e6-4450-913c-299caf871299";
    private static final String CATEGORY_EXAMPLE = "platform";

    @Test
    public void constructorWithXOcciAttributePrefix_Test() throws Exception {
        ResourceInstance resource = new ResourceInstance(IN_HEADER_EXAMPLE);
        Assert.assertTrue(resource.getUuid().equals(UUID_EXAMPLE));
        Assert.assertTrue(resource.getLocation().equals(LOCATION_EXAMPLE));
        Assert.assertTrue(resource.getCategory().equals(CATEGORY_EXAMPLE));
    }

    @Test
    public void constructorWithLocation_Test() throws Exception {
        ResourceInstance resource = new ResourceInstance(LOCATION_EXAMPLE);
        Assert.assertTrue(resource.getUuid().equals(UUID_EXAMPLE));
        Assert.assertTrue(resource.getLocation().equals(LOCATION_EXAMPLE));
        Assert.assertTrue(resource.getCategory().equals(CATEGORY_EXAMPLE));
    }

    @Test
    public void constructorWithAttributes_Test() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("occi.core.id", UUID_EXAMPLE);
        ResourceInstance resource = new ResourceInstance(map);

        Assert.assertTrue(resource.getUuid().equals(UUID_EXAMPLE));
    }

}
