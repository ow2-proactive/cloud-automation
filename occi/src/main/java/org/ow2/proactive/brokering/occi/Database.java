package org.ow2.proactive.brokering.occi;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

public class Database {
    private static Logger logger = Logger.getLogger(Database.class);
    private static Database instance;
    private SQLiteQueue dbQueue;

    private Database() {
        try {
            logger.info("Setup the Database");
            File dbFile = new File("/tmp/database.sqlite");
            dbQueue = new SQLiteQueue(dbFile);
            dbQueue.start();
            dbQueue.execute(new SQLiteJob<Object>() {
                @Override
                protected Object job(SQLiteConnection conn) throws Throwable {
                    SQLiteStatement st = null;
                    try {
                        st = conn.prepare("SELECT name FROM sqlite_master WHERE type='table' AND name='resources'");
                        if (!st.step()) {
                            conn.exec("CREATE TABLE resources (uuid TEXT PRIMARY KEY UNIQUE, url TEXT, category TEXT)");
                        }
                        st.dispose();
                        st = conn.prepare("SELECT name FROM sqlite_master WHERE type='table' AND name='attributes'");
                        if (!st.step()) {
                            conn.exec("CREATE TABLE attributes (uuid TEXT, key TEXT, value TEXT)");
                        }

                    } finally {
                        if (st != null) {
                            st.dispose();
                        }
                        return null;
                    }
                }
            }).complete();

        } catch (Throwable e) {
            e.printStackTrace();
            logger.error(e.fillInStackTrace());
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
            Resource.loadDatabase(instance.getAllResources());
        }
        return instance;
    }

    public String toString() {
        return dbQueue.toString();
    }

    public void delete(final UUID uuid) {
        try {
            dbQueue.execute(new SQLiteJob<Object>() {
                @Override
                protected Object job(SQLiteConnection connection) throws Throwable {
                    connection.setBusyTimeout(100);
                    connection.prepare("DELETE FROM resources WHERE uuid='" + uuid + "'").step();
                    return null;
                }
            }).complete();
            dbQueue.execute(new SQLiteJob<Object>() {
                @Override
                protected Object job(SQLiteConnection connection) throws Throwable {
                    connection.prepare("DELETE FROM attributes WHERE uuid='" + uuid + "'").step();
                    return null;
                }
            }).complete();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void store(Resource resource) {
        try {
            logger.info("Resource to store in DB : " + resource.getUuid());
            final String uuid = resource.getUuid().toString();
            final String host = resource.getHost();
            final String category = resource.getCategory();
            final Map<String, String> attributes = resource.getAttributes();

            delete(resource.getUuid());
            dbQueue.execute(new SQLiteJob<Object>() {
                @Override
                protected Object job(SQLiteConnection connection) throws Throwable {
                    connection.prepare("INSERT INTO resources VALUES ('" + uuid + "','" + host + "','" + category + "')").step();
                    return null;
                }
            }).complete();
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                final String key = attribute.getKey();
                final String value = attribute.getValue();
                dbQueue.execute(new SQLiteJob<Object>() {
                    @Override
                    protected Object job(SQLiteConnection connection) throws Throwable {
                        connection.prepare("INSERT INTO attributes VALUES ('" + uuid + "','" + key + "','" + value + "')").step();
                        return null;
                    }
                }).complete();
            }
            logger.debug("Resource stored in DB : " + uuid);

        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("Resource storage to DB Failed: " + e.fillInStackTrace());

        }
    }

    public Resource load(final UUID uuid) {
        SQLiteStatement st = null;
        try {
            logger.info("Resource to load from DB : " + uuid);
            SQLiteJob<Resource> job = dbQueue.execute(new SQLiteJob<Resource>() {
                @Override
                protected Resource job(SQLiteConnection conn) throws Throwable {
                    SQLiteStatement st = conn.prepare("SELECT * FROM resources WHERE uuid='" + uuid + "'");
                    if (!st.step()) {
                        return null;
                    }
                    String host = st.columnString(1);
                    String category = st.columnString(2);
                    st.dispose();
                    st = conn.prepare("SELECT * FROM attributes WHERE uuid='" + uuid + "'");
                    Map<String, String> attributes = new HashMap<String, String>();
                    while (st.step()) {
                        String key = st.columnString(1);
                        String value = st.columnString(2);
                        attributes.put(key, value);
                    }
                    st.dispose();
                    return Resource.factory(uuid, host, category, attributes);
                }
            });
            return job.complete();

        } catch (Throwable e) {
            e.printStackTrace();

        } finally {
            if (st != null) {
                st.dispose();
            }
        }
        return null;
    }

    public List<Resource> getAllResources() {
        logger.info("Load all resources");
        SQLiteJob<List<Resource>> job = dbQueue.execute(new SQLiteJob<List<Resource>>() {
            @Override
            protected List<Resource> job(SQLiteConnection conn) throws Throwable {
                SQLiteStatement st = null;
                try {
                    List<Resource> resourceList = new ArrayList<Resource>();
                    st = conn.prepare("SELECT uuid FROM resources");
                    while (st.step()) {
                        UUID uuid = UUID.fromString(st.columnString(0));
                        Resource resource = internalLoad(conn, uuid);
                        resourceList.add(resource);
                    }
                    st.dispose();
                    return resourceList;

                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (st != null) {
                        st.dispose();
                    }
                }
                return null;
            }
        });
        return job.complete();
    }

    private Resource internalLoad(SQLiteConnection conn, UUID uuid) throws Exception {
        SQLiteStatement st = conn.prepare("SELECT * FROM resources WHERE uuid='" + uuid + "'");
        st.step();
        String host = st.columnString(1);
        String category = st.columnString(2);
        st.dispose();
        st = conn.prepare("SELECT * FROM attributes WHERE uuid='" + uuid + "'");
        Map<String, String> attributes = new HashMap<String, String>();
        while (st.step()) {
            String key = st.columnString(1);
            String value = st.columnString(2);
            attributes.put(key, value);
        }
        st.dispose();
        return Resource.factory(uuid, host, category, attributes);
    }
}
