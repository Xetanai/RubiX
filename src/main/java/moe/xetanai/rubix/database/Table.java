package moe.xetanai.rubix.database;

public class Table {
	private Database db;

	public Table(Database db) {
		this.db = db;
	}

	Database getDb() {
		return db;
	}
}
