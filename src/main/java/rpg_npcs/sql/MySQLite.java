package rpg_npcs.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.JournalMode;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

public class MySQLite implements MySQL {
	private final SQLiteConnectionPoolDataSource pool;
	
	public MySQLite(String path) {
		SQLiteConfig config = new SQLiteConfig();
		config.setJournalMode(JournalMode.WAL);
		
		pool = new SQLiteConnectionPoolDataSource(config);
		pool.setUrl("jdbc:sqlite:" + path);
	}

	@Override
	public Connection connect() throws SQLException {
		return pool.getConnection();
	}

}
