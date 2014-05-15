package org.ow2.proactive.brokering.occi;

import com.orientechnologies.orient.core.db.object.ODatabaseObjectTx;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class Database {

    private static String DB_NAME = "occi-database";
    private static String DB_DEFAULT_USERNAME = "admin";
    private static String DB_DEFAULT_PASSWORD = "admin";

    private static Logger logger = Logger.getLogger(Database.class);
    private static Database instance;

    private ODatabaseObjectTx db;

    public static void setDatabaseName(String databaseName) {
        DB_NAME = databaseName;
    }

    public static void resetInstance() {
        instance = null;
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
            Resource.loadDatabase(instance.getAllResources());
        }
        return instance;
    }

    public static void dropDB() {
        ODatabaseObjectTx dba = new ODatabaseObjectTx(generateDatabaseUrl(DB_NAME));
        if (dba.exists()) {
            dba = dba.open(DB_DEFAULT_USERNAME, DB_DEFAULT_PASSWORD);
            dba.drop();
        }
        resetInstance();
    }

    private Database() {
        logger.info("Setting up DB");

        db = new ODatabaseObjectTx(generateDatabaseUrl(DB_NAME));

        this.createIfNeeded();
        this.openIfNeeded();
        this.registerEntities();

        logger.info("DB correctly started");
    }

    public void store(Resource resource) {
        try {
            logger.info("Resource to store in DB : " + resource.getUuid());
            db.save(resource);
        } catch (Throwable e) {
            logger.error("Resource storage to DB failed", e);
        }
    }

    public void delete(String uuid) {
        try {
            Resource res = load(uuid);
            if (res != null)
                db.delete(res);
        } catch (Throwable e) {
            logger.error("Resource deletion from DB failed", e);
        }
    }

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

    public List<Resource> getAllResources() {
        logger.info("Load all resources");
        List<Resource> list = new ArrayList<Resource>();
        for (Resource r : db.browseClass(Resource.class))
            list.add(r);
        return list;
    }

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

