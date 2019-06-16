package rpg_npcs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private Connection con;
    
    private enum DatabaseType {
    	SQL,
    	SQLite
    }
    
    private final DatabaseType type;
    private final String path;
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    
    public MySQL(DatabaseType type, String path, String host, String port, String database, String username,
			String password) {
		this.type = type;
		this.path = path;
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}
    
    public static MySQL makeSQLite(String path) {
    	return new MySQL(DatabaseType.SQLite, path, null, null, null, null, null);
    }
    
    public static MySQL makeSQL(String host, String port, String database, String username, String password) {
    	return new MySQL(DatabaseType.SQL, null, host, port, database, username, password);
    }
    
    public Connection connect() throws SQLException {
    	switch (type) {
		case SQL:
			return connectSQL();
		case SQLite:
			return connectSQLite();
		default:
			return null;
		}
    }

	private Connection connectSQL() throws SQLException {
        if (!isConnected()) {
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            con.setAutoCommit(true);
        }
        
        return con;
    }

    private Connection connectSQLite() throws SQLException {
        if (!isConnected()) {
            con = DriverManager.getConnection("jdbc:sqlite:" + path);
            con.setAutoCommit(true);
        }
        
        return con;
    }

    public boolean isConnected() {
    	if (con == null) {
			return false;
		}
    	
        try {
			return !con.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			con = null;
			return false;
		}
    }

    public Connection getConnection() {
        return con;
    }
}
