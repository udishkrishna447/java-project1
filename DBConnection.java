// ============================================================
//  Kongu Engineering College (R2024) – Academic System
//  File: DBConnection.java
//  Description: Dual-Engine JDBC Connection Manager.
//               Attempts MySQL first; if unavailable, seamlessly
//               initializes local SQLite database so the app
//               runs instantly without configuration hurdles.
// ============================================================

import java.io.File;
import java.sql.*;

public class DBConnection {

    // -------------------------------------------------------
    //  MySQL Settings
    // -------------------------------------------------------
    private static final String MYSQL_URL      = "jdbc:mysql://localhost:3306/kec_academic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    private static final String MYSQL_USER     = "root";
    private static final String MYSQL_PASSWORD = "root";
    private static final String MYSQL_DRIVER   = "com.mysql.cj.jdbc.Driver";

    // -------------------------------------------------------
    //  SQLite Fallback Settings (Zero-Configuration)
    // -------------------------------------------------------
    private static final String SQLITE_DB_FILE = "kec_academic.db";
    private static final String SQLITE_URL     = "jdbc:sqlite:" + SQLITE_DB_FILE;
    private static final String SQLITE_DRIVER  = "org.sqlite.JDBC";

    private static Connection connection = null;
    private static String activeEngine   = "Unknown";

    private DBConnection() {}

    /**
     * Returns the singleton JDBC Connection.
     * Tries MySQL first. Falls back to SQLite if MySQL fails.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            boolean mysqlSuccess = false;

            // Attempt 1: Try MySQL
            try {
                Class.forName(MYSQL_DRIVER);
                connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
                connection.setAutoCommit(true);
                activeEngine = "MySQL (localhost:3306/kec_academic)";
                mysqlSuccess = true;
            } catch (Throwable t) {
                // MySQL not available or credentials failed
            }

            // Attempt 2: SQLite Zero-Config Fallback
            if (!mysqlSuccess) {
                try {
                    Class.forName(SQLITE_DRIVER);
                    boolean isNew = !new File(SQLITE_DB_FILE).exists() || new File(SQLITE_DB_FILE).length() == 0;
                    connection = DriverManager.getConnection(SQLITE_URL);
                    connection.setAutoCommit(true);
                    activeEngine = "SQLite (" + SQLITE_DB_FILE + ")";

                    if (isNew) {
                        initSQLiteSchema(connection);
                    }
                } catch (ClassNotFoundException e) {
                    throw new SQLException(
                        "[ERROR] Neither MySQL nor SQLite JDBC Driver was found.\n" +
                        "        Ensure mysql-connector-j or sqlite-jdbc is in your classpath.\n" +
                        "        Details: " + e.getMessage());
                }
            }
        }
        return connection;
    }

    public static String getActiveEngine() {
        return activeEngine;
    }

    /**
     * Initializes schema & seed data for SQLite fallback.
     */
    private static void initSQLiteSchema(Connection con) {
        try (Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS students (" +
                       "roll_number TEXT PRIMARY KEY, " +
                       "student_name TEXT NOT NULL, " +
                       "department TEXT NOT NULL, " +
                       "semester INTEGER NOT NULL);");

            st.execute("CREATE TABLE IF NOT EXISTS courses (" +
                       "course_code TEXT PRIMARY KEY, " +
                       "course_title TEXT NOT NULL, " +
                       "credits INTEGER NOT NULL, " +
                       "course_type TEXT NOT NULL);");

            st.execute("CREATE TABLE IF NOT EXISTS enrollments (" +
                       "enrollment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                       "roll_number TEXT NOT NULL, " +
                       "course_code TEXT NOT NULL, " +
                       "semester INTEGER NOT NULL, " +
                       "total_classes INTEGER NOT NULL DEFAULT 0, " +
                       "attended_classes INTEGER NOT NULL DEFAULT 0, " +
                       "UNIQUE(roll_number, course_code, semester), " +
                       "FOREIGN KEY (roll_number) REFERENCES students(roll_number) ON DELETE CASCADE, " +
                       "FOREIGN KEY (course_code) REFERENCES courses(course_code) ON DELETE CASCADE);");

            st.execute("CREATE TABLE IF NOT EXISTS assessment_marks (" +
                       "marks_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                       "enrollment_id INTEGER NOT NULL UNIQUE, " +
                       "cia1 REAL DEFAULT NULL, " +
                       "cia2 REAL DEFAULT NULL, " +
                       "assignment_quiz_lab REAL DEFAULT NULL, " +
                       "ese_marks REAL DEFAULT NULL, " +
                       "ese_absent INTEGER NOT NULL DEFAULT 0, " +
                       "FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE);");

            // Seed sample data
            st.execute("INSERT OR IGNORE INTO students VALUES " +
                       "('24ITR001','Arun Kumar R','IT',3)," +
                       "('24ITR002','Priya Dharshini S','IT',3)," +
                       "('24CSE001','Karthick M','CSE',3)," +
                       "('24CSE002','Nithya Lakshmi B','CSE',3)," +
                       "('24ECE001','Suriya P','ECE',3);");

            st.execute("INSERT OR IGNORE INTO courses VALUES " +
                       "('24ITR31','Database Management Systems',4,'THEORY')," +
                       "('24ITR32','Operating Systems',3,'THEORY')," +
                       "('24ITR33','DBMS Laboratory',2,'PRACTICAL')," +
                       "('24CSE31','Data Structures and Algorithms',4,'THEORY')," +
                       "('24CSE32','DS Lab',2,'PRACTICAL')," +
                       "('24ECE31','Digital Electronics',4,'EMBEDDED');");

            st.execute("INSERT OR IGNORE INTO enrollments (enrollment_id, roll_number, course_code, semester, total_classes, attended_classes) VALUES " +
                       "(1,'24ITR001','24ITR31',3,90,78)," +
                       "(2,'24ITR001','24ITR32',3,70,52)," +
                       "(3,'24ITR001','24ITR33',3,60,56)," +
                       "(4,'24ITR002','24ITR31',3,90,80)," +
                       "(5,'24ITR002','24ITR32',3,70,60)," +
                       "(6,'24CSE001','24CSE31',3,80,75)," +
                       "(7,'24CSE001','24CSE32',3,60,58)," +
                       "(8,'24CSE002','24CSE31',3,80,50)," +
                       "(9,'24ECE001','24ECE31',3,85,82);");

            st.execute("INSERT OR IGNORE INTO assessment_marks (enrollment_id, cia1, cia2, assignment_quiz_lab, ese_marks, ese_absent) VALUES " +
                       "(1, 78, 82, 88, 72, 0)," +
                       "(2, 55, 48, 60, 40, 0)," +
                       "(3, 80, 85, 90, 88, 0)," +
                       "(4, 90, 88, 92, 85, 0)," +
                       "(5, 70, 65, 75, NULL, 0)," +
                       "(6, 85, 80, 88, 78, 0)," +
                       "(7, 88, 90, 92, 85, 0)," +
                       "(8, 45, 50, 55, NULL, 0)," +
                       "(9, 88, 90, 92, 80, 0);");

        } catch (SQLException e) {
            System.err.println("[WARN] initSQLiteSchema: " + e.getMessage());
        }
    }

    /** Closes the connection gracefully. */
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("[INFO] Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("[WARN] Could not close connection: " + e.getMessage());
            }
        }
    }
}
