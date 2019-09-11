package moe.xetanai.rubix.database;

/**
 * Represents a table in the database
 */
public class Table {

    private Database db;

    /**
     * Creates a table
     *
     * @param db Database to connect to
     */
    public Table (Database db) {
        this.db = db;
    }

    /**
     * Get the database
     *
     * @return database
     */
    public Database getDb () {
        return this.db;
    }
}
