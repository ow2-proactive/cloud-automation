package org.ow2.proactive.brokering.occi.database;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrientDB implements Database {

    private static String DB_DEFAULT_USERNAME = "admin";
    private static String DB_DEFAULT_PASSWORD = "admin";

    private static Logger logger = Logger.getLogger(OrientDB.class);

    private OObjectDatabaseTx db;

    public OrientDB (String name) {

        logger.info("Setting up DB");

        db = new OObjectDatabaseTx(generateDatabaseUrl(name));

        this.createIfNeeded();
        this.openIfNeeded();
        this.registerEntities();

        logger.info("DB correctly started");

    }

    @Override
    public void store(Resource resource) {
        try {
            logger.info("Resource to store in DB : " + resource.getUuid());
            db.save(resource);
        } catch (Throwable e) {
            logger.error("Resource storage to DB failed", e);
        }
    }

    @Override
    public void delete(String uuid) {
        try {
            Resource res = load(uuid);
            if (res != null)
                db.delete(res);
        } catch (Throwable e) {
            logger.error("Resource deletion from DB failed", e);
        }
    }

    @Override
    public Resource load(String uuid) {
        OSQLSynchQuery<Resource> q =
            new OSQLSynchQuery<Resource>("select * from Resource where uuid = '" + uuid + "'");
        List<Resource> list = db.query(q);

        if (list.size() == 0) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            logger.warn("Repeated uuid: " + uuid);
            return list.get(0);
        }
    }

    @Override
    public List<Resource> getAllResources() {
        logger.info("Load all resources");
        List<Resource> list = new ArrayList<Resource>();
        for (Resource r : db.browseClass(Resource.class))
            list.add(r);
        return list;
    }

    @Override
    public void drop() {
        db.drop();
    }


    @Override
    public void close() {
        db.close();
    }

    private static String generateDatabaseUrl(String name) {
        String tmp = System.getProperty("java.io.tmpdir");
        String fs = File.separator;
        return "local:" + tmp + fs + name;
    }

    private void registerEntities() {
        db.getEntityManager().registerEntityClass(Resource.class);
    }

    private void createIfNeeded() {
        if (!db.exists())
            db.create();
    }

    private void openIfNeeded() {
        if (db.isClosed())
            db.open(DB_DEFAULT_USERNAME, DB_DEFAULT_PASSWORD);
    }

}

