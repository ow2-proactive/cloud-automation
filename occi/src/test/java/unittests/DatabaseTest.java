package unittests;

import org.junit.*;
import java.util.*;
import junit.framework.Assert;
import org.ow2.proactive.brokering.occi.Database;
import org.ow2.proactive.brokering.occi.Resource;

public class DatabaseTest {

    private static final int NRO_RESOURCES = 100;
    private static final String TEST_DB_NAME = "occi-database-test";

    @Before
    public void before() throws Exception {
        Database.setDatabaseName(TEST_DB_NAME);
        Database.dropDB();
    }

    @After
    public void after() throws Exception {
        Database.setDatabaseName(TEST_DB_NAME);
        Database.dropDB();
    }

    @Test
    public void createDatabaseSimple_Test() throws Exception {
        Database db = Database.getInstance();

        Assert.assertTrue(db.getAllResources().isEmpty());

        Resource res = generateStandardResource();
        db.store(res);

        Assert.assertTrue(db.getAllResources().size() == 1);

        db.close();
    }

    @Test
    public void persistentDatabaseSingleItem_Test() throws Exception {
        Database db = Database.getInstance();

        Assert.assertTrue(db.getAllResources().isEmpty());

        Resource res1 = generateStandardResource();
        db.store(res1);

        Assert.assertTrue(db.getAllResources().size() == 1);

        db.close();

        Database.resetInstance();
        Database db1 = Database.getInstance();

        Assert.assertTrue(db1.getAllResources().size() == 1);

        Resource res2 = db1.getAllResources().get(0);
        Assert.assertTrue(res2.equals(res1));

        db.close();
    }

    @Test
    public void persistentDatabaseMultipleItems_Test() throws Exception {

        Database db = Database.getInstance();
        Map<String, Resource> checkMap = new HashMap<String, Resource>();

        Assert.assertTrue(db.getAllResources().isEmpty());

        for (int i = 0; i < NRO_RESOURCES; i++) {
            Resource res = generateStandardResource();
            db.store(res);
            checkMap.put(res.getUuid(), res);
        }

        Assert.assertTrue(db.getAllResources().size() == NRO_RESOURCES);

        db.close();

        Database.resetInstance();
        Database db1 = Database.getInstance();

        Assert.assertTrue(db1.getAllResources().size() == NRO_RESOURCES);

        for (Resource rFromDB: db1.getAllResources()) {

            Assert.assertTrue(rFromDB.getUuid() != null);

            Resource rFromCheckMap = checkMap.get(rFromDB.getUuid());

            Assert.assertTrue(rFromCheckMap != null);

            Assert.assertTrue(rFromDB.equals(rFromCheckMap));

        }

        db.close();
    }

    private Resource generateStandardResource() {
        Map<String, String> map = new HashMap<String, String>();
        String uuid = UUID.randomUUID().toString();
        String templateName = "template_name" + UUID.randomUUID().toString();
        map.put("occi.compute.template_name", templateName);
        map.put("occi.core.id", uuid);
        return Resource.factory(uuid, "compute", map);
    }

}
