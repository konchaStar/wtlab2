package by.bsuir.phoneshop.model.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class ConnectionPool {
    private static final int MAX_SIZE = 10;
    private final List<Connection> pool = new ArrayList<>();
    private final List<Connection> usedConnections = new ArrayList<>();

    private static class SingletonHandler {
        private static final ConnectionPool instance = new ConnectionPool();
    }

    public static ConnectionPool getInstance() {
        return SingletonHandler.instance;
    }

    private ConnectionPool() {
        ResourceBundle bundle = ResourceBundle.getBundle("database");
        final String url = bundle.getString("db.url");
        final String name = bundle.getString("db.name");
        final String user = bundle.getString("db.user");
        final String password = bundle.getString("db.password");
        try {
            create(String.format("%s%s", url, name), user, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void create(String url, String user,
            String password) throws SQLException, ClassNotFoundException {
        for (int i = 0; i < MAX_SIZE; i++) {
            pool.add(createConnection(url, user, password));
        }
    }

    public Connection getConnection() {
        Connection connection = pool
                .remove(pool.size() - 1);
        usedConnections.add(connection);
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        pool.add(connection);
        return usedConnections.remove(connection);
    }

    private static Connection createConnection(
            String url, String user, String password)
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    public int getSize() {
        return pool.size() + usedConnections.size();
    }
}
