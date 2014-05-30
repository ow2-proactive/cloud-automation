package org.ow2.proactive.brokering.occi.database

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.log4j.Logger
import org.ow2.proactive.brokering.occi.Resource
import org.ow2.proactive.brokering.occi.ResourceBuilder

class H2DB implements Database {

    private static Logger logger = Logger.getLogger(Database.class)

    private Sql sql

    public H2DB(String dbName) {
        try {
            Class.forName("org.h2.Driver")

            File dbFile = new File(System.getProperty("java.io.tmpdir"), dbName)
            sql = Sql.newInstance("jdbc:h2:" + dbFile, "sa", "")

            sql.execute("CREATE TABLE IF NOT EXISTS resources  (uuid VARCHAR PRIMARY KEY, category VARCHAR)")
            sql.execute("CREATE TABLE IF NOT EXISTS attributes  (uuid VARCHAR, key VARCHAR, value VARCHAR)")

        } catch (Throwable e) {
            logger.error("Failed to create database", e)
        }
    }

    public void delete(final String uuid) {
        try {
            sql.execute("DELETE FROM resources WHERE uuid=$uuid")
            sql.execute("DELETE FROM attributes WHERE uuid=$uuid")
        } catch (Throwable e) {
            e.printStackTrace()
        }
    }

    @Override
    void drop() {
        sql.execute("DROP TABLE attributes")
        sql.execute("DROP TABLE resources")
    }

    @Override
    void close() {

    }

    public void store(Resource resource) {
        try {
            logger.info("Resource to store in DB : " + resource.getUuid())
            final String uuid = resource.getUuid().toString()
            final String category = resource.getCategory()
            final Map<String, String> attributes = resource.getAttributes()

            delete(resource.getUuid())

            sql.execute("INSERT INTO resources VALUES ($uuid,$category)")
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                final String key = attribute.getKey()
                final String value = attribute.getValue()
                sql.execute("INSERT INTO attributes VALUES ($uuid,$key,$value)")
            }
            logger.debug("Resource stored in DB : " + uuid)

        } catch (Throwable e) {
            e.printStackTrace()
            logger.error("Resource storage to DB Failed: " + e.fillInStackTrace())

        }
    }

    public Resource load(final String uuid) {
        try {
            logger.info("Resource to load from DB : " + uuid)

            def row = sql.firstRow("SELECT * FROM resources WHERE uuid=$uuid")
            if (row) {
                def attributes = sql.rows("SELECT * FROM attributes WHERE uuid=$uuid").collectEntries {
                    [(it.key): it.value]
                }
                return ResourceBuilder.factory(row.uuid, row.category, attributes)
            }

        } catch (Throwable e) {
            e.printStackTrace()

        } finally {
        }
        return null
    }

    public List<Resource> getAllResources() {
        logger.info("Load all resources")
        try {
            List<GroovyRowResult> rows = sql.rows("SELECT uuid FROM resources")
            List<Resource> resourceList = new ArrayList<Resource>()
            for (GroovyRowResult row : rows) {
                Resource resource = internalLoad(row.uuid)
                resourceList.add(resource)
            }

            return resourceList

        } catch (Throwable e) {
            e.printStackTrace()
        }
        return null
    }

    private Resource internalLoad(String uuid) throws Exception {
        GroovyRowResult resource = sql.firstRow("SELECT * FROM resources WHERE uuid=$uuid")
        List<GroovyRowResult> attributes = sql.rows("SELECT * FROM attributes WHERE uuid=$uuid")

        Map<String, String> resourceAttributes = new HashMap<String, String>()
        for (GroovyRowResult attribute : attributes) {
            String key = attribute.key
            String value = attribute.value
            resourceAttributes.put(key, value)
        }
        return ResourceBuilder.factory(uuid, resource.category, resourceAttributes)
    }
}
