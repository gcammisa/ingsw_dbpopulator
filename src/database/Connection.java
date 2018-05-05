package database;


import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * this class is specifically designed to work with postgresql Database.
 *
 */
public class Connection {

    private static final String  URL_TEMPLATE = "jdbc:postgresql://%s:%d/%s";


    private java.sql.Connection connection;

    /**
     * Open a connection to given database
     * @param ip database IPv4 address
     * @param port database port number
     * @param db database name
     * @param properties username and password, in "user" -> USER; "password" -> PASSWORD format
     * @throws SQLException error with db
     */
    public Connection(String ip, int port, String db, Properties properties) throws SQLException {
        String url = String.format(URL_TEMPLATE, ip, port, db);

        connection = DriverManager.getConnection(url, properties);

    }


    /**
     * close connection to given database
     * @throws SQLException error with db
     */
    public void  close() throws SQLException {
        connection.close();
    }

    /**
     * return a new PreparedStatement. query parameters are NOT set.
     * @param query SQL query
     * @return unset PreparedStatement.
     * @throws SQLException sql illegal statement
     */
    public PreparedStatement getPreperedStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }


}
