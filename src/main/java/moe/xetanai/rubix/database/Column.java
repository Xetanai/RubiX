package moe.xetanai.rubix.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Column<T> {
	private String name;
	private T defaultValue;

	Column(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getString(ResultSet rs) {
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

	public Long getLong(ResultSet rs) {
		try {
			return rs.getLong(this.name);
		} catch (SQLException err) {
			return (Long) this.defaultValue;
		}
	}
}
