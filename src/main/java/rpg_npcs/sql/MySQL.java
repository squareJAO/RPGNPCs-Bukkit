package rpg_npcs.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface MySQL {
    Connection connect() throws SQLException;
}
