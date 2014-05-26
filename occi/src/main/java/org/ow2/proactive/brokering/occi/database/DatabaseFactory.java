package org.ow2.proactive.brokering.occi.database;

public class DatabaseFactory {

    private static String DB_NAME = "occi-database";
    private static Database mockup;

    public static void setDatabaseName(String databaseName) {
        DB_NAME = databaseName;
    }

    public static Database build() {
        if (mockup == null)
            return new OrientDB(DB_NAME);
        else
            return mockup;
    }

    public static void mockupWith(Database mockupdb) {
        mockup = mockupdb;
    }
}
