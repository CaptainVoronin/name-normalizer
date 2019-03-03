package datareader;

import java.io.Closeable;
import java.sql.*;
import java.util.Properties;

public class SQLProvider implements Closeable {
    String queryText;
    Connection connection;
    ResultSet resultSet;
    String url = "jdbc:postgresql://%s/%s";
    Properties props;
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }


    public SQLProvider( String host, String db, String user, String pwd )
    {
        props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        url = String.format( url, host, db );
    }

    public void open() throws SQLException {
        connect();
    }

    void connect() throws SQLException {
        connection = DriverManager.getConnection(url, props);
    }

    @Override
    public void close() {
        try {
            if (connection != null) {
                try {
                    if (resultSet != null) {
                        if (!resultSet.isClosed()) {
                            resultSet.close();
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection()
    {
        return connection;
    }
}
