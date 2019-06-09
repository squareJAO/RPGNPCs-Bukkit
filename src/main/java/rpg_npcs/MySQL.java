package rpg_npcs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    public static Connection con;

    public static void connect(String host, String port, String database, String username, String password) throws SQLException {
        if (!isConnected()) {
            con = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        }
    }

    public static void connectSQLite(String path) throws SQLException {
        if (!isConnected()) {
            con = DriverManager.getConnection("jdbc:sqlite:" + path);
        }
    }

    public static void disconnect() throws SQLException {
        if (isConnected()) {
            con.close();
        }
    }

    public static boolean isConnected() {
        return (con == null ? false : true);
    }

    public static Connection getConnection() {
        return con;
    }
}
