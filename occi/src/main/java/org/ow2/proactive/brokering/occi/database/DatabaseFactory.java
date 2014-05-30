package org.ow2.proactive.brokering.occi.database;

public class DatabaseFactory {

    private String dbName = "occi-database";

    public Database build() {
        return new OrientDB(dbName);
    }

    public void setDatabaseName(String databaseName) {
        dbName = databaseName;
    }

}
