package addressbook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Manager Class
 * Handles all database operations for the Address Book System
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/addressbook_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private Connection connection;
    private static DatabaseManager instance;

    // Singleton
    private DatabaseManager() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Database connection failed", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Create tables
    public void initializeDatabase() {

        String createContactsTable =
                "CREATE TABLE IF NOT EXISTS contacts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "phone VARCHAR(20) NOT NULL," +
                "email VARCHAR(100) NOT NULL UNIQUE," +
                "address TEXT," +
                "notes TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";

        String createUsersTable =
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) NOT NULL UNIQUE," +
                "password VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) NOT NULL UNIQUE," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {

            stmt.execute(createContactsTable);
            stmt.execute(createUsersTable);

            String insertAdmin =
                    "INSERT IGNORE INTO users (username, password, email) " +
                    "VALUES ('admin','admin123','admin@addressbook.com')";

            stmt.execute(insertAdmin);

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    // Insert sample data
    public void insertSampleData() {

        String checkQuery = "SELECT COUNT(*) FROM contacts";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {

            if (rs.next() && rs.getInt(1) == 0) {

                String[] sampleData = {
                        "('John Smith','1234567890','john.smith@email.com','123 Main St','Software Engineer')",
                        "('Sarah Johnson','2345678901','sarah.johnson@email.com','456 Oak Ave','Marketing Manager')",
                        "('Michael Brown','3456789012','michael.brown@email.com','789 Pine Rd','Data Analyst')",
                        "('Emily Davis','4567890123','emily.davis@email.com','321 Elm St','UX Designer')"
                };

                String base =
                        "INSERT INTO contacts (name, phone, email, address, notes) VALUES ";

                for (String row : sampleData) {
                    stmt.execute(base + row);
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to insert sample data: " + e.getMessage());
        }
    }

    // Authentication
    public boolean authenticateUser(String username, String password) {

        String query = "SELECT id FROM users WHERE username=? AND password=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // Insert contact
    public boolean insertContact(Contact c) {

        String query =
                "INSERT INTO contacts (name, phone, email, address, notes) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getNotes());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    // Get all contacts
    public List<Contact> getAllContacts() {

        List<Contact> list = new ArrayList<>();
        String query = "SELECT * FROM contacts ORDER BY name";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                list.add(mapContact(rs));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }

    // Search contacts
    public List<Contact> searchContacts(String term) {

        List<Contact> list = new ArrayList<>();

        String query =
                "SELECT * FROM contacts WHERE " +
                "name LIKE ? OR phone LIKE ? OR email LIKE ? OR address LIKE ? " +
                "ORDER BY name";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            String p = "%" + term + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);
            ps.setString(4, p);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapContact(rs));
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return list;
    }

    // Update contact
    public boolean updateContact(Contact c) {

        String query =
                "UPDATE contacts SET name=?, phone=?, email=?, address=?, notes=? WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getNotes());
            ps.setInt(6, c.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    // Delete contact
    public boolean deleteContact(int id) {

        try (PreparedStatement ps =
                     connection.prepareStatement("DELETE FROM contacts WHERE id=?")) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    // Get contact by ID
    public Contact getContactById(int id) {

        try (PreparedStatement ps =
                     connection.prepareStatement("SELECT * FROM contacts WHERE id=?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapContact(rs);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    // Backup database
    public boolean backupDatabase(String backupPath) {
        try {
            String command =
                    "mysqldump -u" + DB_USER +
                    " -p" + DB_PASSWORD +
                    " addressbook_db > " + backupPath;

            Process process = Runtime.getRuntime().exec(command);
            return process.waitFor() == 0;

        } catch (Exception e) {
            System.err.println("Database backup failed: " + e.getMessage());
            return false;
        }
    }

    // Helper mapper
    private Contact mapContact(ResultSet rs) throws SQLException {

        Contact c = new Contact();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setNotes(rs.getString("notes"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) c.setCreatedAt(created.toLocalDateTime());
        if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());

        return c;
    }

    public Connection getConnection() {
        return connection;
    }
}
