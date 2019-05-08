package moe.xetanai.rubix.database;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an SQL column within a table
 * @param <T> The data type this column stores
 */
public class Column<T> {
	private String name;
	private T defaultValue;

	/**
	 * Creates a Column, with a name and default value to represent NULL by
	 * @param name The SQL Column name
	 * @param defaultValue The value to expose NULL in the database as
	 */
	public Column(@Nonnull String name, @Nonnull T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	/**
	 * Gets the SQL Column name
	 * @return SQL Column name
	 */
	@Nonnull
	public String getName() {
		return name;
	}

	/**
	 * Get the default value of the column
	 * @return the value NULL in the database is represented by
	 */
	@Nonnull
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Gets the column's value from a DB query ResultSet
	 * Returns the value casted as a string
	 *
	 * @param rs Database ResultSet
	 * @return Column value in the resultset's current row,
	 * the default value if null, and is null if not type castable into a String
	 */
	@Nonnull
	public String getString(@Nonnull ResultSet rs) {
		try {
			String val = rs.getString(this.name);
			if(val == null) {
				return (String) this.defaultValue;
			}

			return val;
		} catch (SQLException err) {
			return (String) this.defaultValue;
		}
	}

	/**
	 * Gets the column's value from a DB query ResultSet
	 * Returns the value casted as a Long
	 *
	 * @param rs Database ResultSet
	 * @return Column value in the resultset's current row,
	 * the default value if null, and is null if not type castable into a Long
	 */
	@Nonnull
	public Long getLong(@Nonnull ResultSet rs) {
		try {
			return rs.getLong(this.name);
		} catch (SQLException err) {
			return (Long) this.defaultValue;
		}
	}
}
