package org.ow2.proactive.brokering.occi;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive.brokering.occi.database.Database;
import org.ow2.proactive.brokering.occi.database.DatabaseFactory;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class DatabaseTest {

    private static final int NRO_RESOURCES = 100;
    private static final String TEST_DB_NAME_PREFIX = "occi-database-test-";

    private DatabaseFactory databaseFactory;

    @Before
    public void before() throws Exception {
        Random r = new Random();
        databaseFactory = new DatabaseFactory();
        databaseFactory.setDatabaseName(TEST_DB_NAME_PREFIX + r.nextInt(Integer.MAX_VALUE));
    }

    @After
    public void after() throws Exception {
        Database db = databaseFactory.build();
        db.drop();
        db.close();
    }

    @Test
    public void createDatabaseSimple_Test() throws Exception {
        Database db = databaseFactory.build();

        Assert.assertTrue(db.getAllResources().isEmpty());

        Resource res = generateStandardResource();
        db.store(res);

        Assert.assertTrue(db.getAllResources().size() == 1);

        db.close();
    }

    @Test
    public void delete_resource() throws Exception {
        Database db = databaseFactory.build();
        Resource res = generateStandardResource();

        db.store(res);
        assertEquals(1, db.getAllResources().size());

        db.delete(res.getUuid());
        assertEquals(0, db.getAllResources().size());

        db.close();
    }

    @Test
    public void update_resource() throws Exception {
        Database db = databaseFactory.build();
        Resource res = generateStandardResource();
        db.store(res);

        res.getAttributes().put("foo", "poo");
        db.store(res);

        Resource updatedResource = db.load(res.getUuid());
        assertEquals("poo", updatedResource.getAttributes().get("foo"));

        db.close();
    }

    @Test
    public void createDatabaseSimpleMultithreaded_Test() throws Exception {
        final Database dbe = databaseFactory.build();
        Assert.assertTrue(dbe.getAllResources().isEmpty());

        final Resource res = generateStandardResource();

        Thread t = new Thread(
            new Runnable() {
                public void run() {
                    final Database dbi = databaseFactory.build();
                    dbi.store(res);
                    dbi.close();
                }
            }
        );

        t.start();
        t.join();

        Assert.assertTrue(dbe.getAllResources().size() == 1);

        dbe.close();
    }

    @Test
    public void createDatabaseSimpleMultithreadedExtreme_Test() throws Exception {
        final int NRO_STORES = 1000;
        final int THREAD_POOL_SIZE = 10;
        final int TIMEOUT = 20;

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        final Database dbe = databaseFactory.build();
        Assert.assertTrue(dbe.getAllResources().isEmpty());

        Runnable r = new Runnable() {
            public void run() {
                final Database dbi = databaseFactory.build();
                Resource res = generateStandardResource();
                dbi.store(res);
                dbi.close();
            }
        };

        for (int i=0; i<NRO_STORES; i++) {
            executor.submit(r);
        }

        executor.shutdown();
        boolean terminated = executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);

        if (!terminated) {
            throw new RuntimeException("The insertion took too long");
        }

        Assert.assertTrue(dbe.getAllResources().size() == NRO_STORES);

        dbe.close();
    }

    @Test
    public void persistentDatabaseSingleItem_Test() throws Exception {
        final Database db = databaseFactory.build();

        Assert.assertTrue(db.getAllResources().isEmpty());

        Resource res1 = generateStandardResource();
        db.store(res1);

        Assert.assertTrue(db.getAllResources().size() == 1);

        db.close();

        final Database db1 = databaseFactory.build();

        Assert.assertTrue(db1.getAllResources().size() == 1);

        Resource res2 = db1.getAllResources().get(0);
        Assert.assertTrue(res2.equals(res1));

        db.close();
    }

    @Test
    public void persistentDatabaseMultipleItems_Test() throws Exception {

        final Database db = databaseFactory.build();

        Map<String, Resource> checkMap = new HashMap<String, Resource>();

        Assert.assertTrue(db.getAllResources().isEmpty());

        for (int i = 0; i < NRO_RESOURCES; i++) {
            Resource res = generateStandardResource();
            db.store(res);
            checkMap.put(res.getUuid(), res);
        }

        Assert.assertTrue(db.getAllResources().size() == NRO_RESOURCES);

        db.close();

        final Database db1 = databaseFactory.build();

        Assert.assertTrue(db1.getAllResources().size() == NRO_RESOURCES);

        for (Resource rFromDB: db1.getAllResources()) {

            Assert.assertTrue(rFromDB.getUuid() != null);

            Resource rFromCheckMap = checkMap.get(rFromDB.getUuid());

            Assert.assertTrue(rFromCheckMap != null);

            Assert.assertTrue(rFromDB.equals(rFromCheckMap));

        }

        db1.close();
    }

    private Resource generateStandardResource() {
        Map<String, String> map = new HashMap<String, String>();
        String uuid = UUID.randomUUID().toString();
        String templateName = "template_name" + UUID.randomUUID().toString();
        map.put("occi.compute.template_name", templateName);
        map.put("occi.core.id", uuid);
        return ResourceBuilder.factory(uuid, "compute", map);
    }

}
