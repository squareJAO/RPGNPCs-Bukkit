package rpg_npcs.sql;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MyPostgreSQL implements MySQL {
	private final ComboPooledDataSource pool;
	
	public MyPostgreSQL(String url, String username, String password) throws PropertyVetoException {
		pool = new ComboPooledDataSource();
		pool.setDriverClass("org.postgresql.Driver");
		pool.setJdbcUrl("jdbc:postgresql://" + url);
		pool.setUser(username);
		pool.setPassword(password);
	}

	@Override
	public Connection connect() throws SQLException {
		return pool.getConnection();
	}
}
