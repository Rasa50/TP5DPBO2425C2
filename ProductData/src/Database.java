import java.sql.*;
import javax.swing.JOptionPane;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/db_product";
    private static final String USER = "root";
    private static final String PASS = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found. Add connector JAR to classpath.", "Driver error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Execute SELECT queries
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        setParams(ps, params);
        return ps.executeQuery();
    }

    // Execute INSERT/UPDATE/DELETE
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
