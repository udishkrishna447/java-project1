// ============================================================
//  Kongu Engineering College (R2024) – Academic System
//  File: StudentDAO.java
//  Description: Data Access Object for Students, Courses,
//               Enrollments, and Attendance operations.
// ============================================================

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // ============================================================
    //  SECTION 1 – STUDENT MANAGEMENT
    // ============================================================

    /**
     * Adds a new student to the database.
     *
     * @param rollNumber Unique roll number (e.g., 24ITR001)
     * @param name       Student full name
     * @param department Department code (CSE, IT, ECE …)
     * @param semester   Current semester (1–8)
     * @return true if inserted successfully
     */
    public boolean addStudent(String rollNumber, String name, String department, int semester) {
        String sql = "INSERT INTO students (roll_number, student_name, department, semester) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, rollNumber.toUpperCase().trim());
            ps.setString(2, name.trim());
            ps.setString(3, department.toUpperCase().trim());
            ps.setInt(4, semester);
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[ERROR] Student with Roll Number '" + rollNumber + "' already exists.");
            return false;
        } catch (SQLException e) {
            System.out.println("[ERROR] addStudent: " + e.getMessage());
            return false;
        }
    }

    /**
     * Displays all students currently registered.
     */
    public void listAllStudents() {
        String sql = "SELECT roll_number, student_name, department, semester FROM students ORDER BY department, roll_number";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n" + "=".repeat(65));
            System.out.printf("%-12s  %-30s  %-10s  %s%n", "Roll No.", "Name", "Dept", "Sem");
            System.out.println("-".repeat(65));
            int count = 0;
            while (rs.next()) {
                System.out.printf("%-12s  %-30s  %-10s  %d%n",
                    rs.getString("roll_number"),
                    rs.getString("student_name"),
                    rs.getString("department"),
                    rs.getInt("semester"));
                count++;
            }
            System.out.println("-".repeat(65));
            System.out.println("  Total Students: " + count);
            System.out.println("=".repeat(65));

        } catch (SQLException e) {
            System.out.println("[ERROR] listAllStudents: " + e.getMessage());
        }
    }

    /**
     * Checks whether a student roll number exists.
     */
    public boolean studentExists(String rollNumber) {
        String sql = "SELECT 1 FROM students WHERE roll_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNumber.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // ============================================================
    //  SECTION 2 – COURSE MANAGEMENT
    // ============================================================

    /**
     * Adds a new course to the course catalog.
     *
     * @param courseCode  Unique code (e.g., 24ITR31)
     * @param title       Course title
     * @param credits     Credit hours (1–6)
     * @param courseType  THEORY | PRACTICAL | EMBEDDED
     * @return true if inserted successfully
     */
    public boolean addCourse(String courseCode, String title, int credits, String courseType) {
        String sql = "INSERT INTO courses (course_code, course_title, credits, course_type) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, courseCode.toUpperCase().trim());
            ps.setString(2, title.trim());
            ps.setInt(3, credits);
            ps.setString(4, courseType.toUpperCase().trim());
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[ERROR] Course '" + courseCode + "' already exists.");
            return false;
        } catch (SQLException e) {
            System.out.println("[ERROR] addCourse: " + e.getMessage());
            return false;
        }
    }

    /**
     * Displays all courses in the catalog.
     */
    public void listAllCourses() {
        String sql = "SELECT course_code, course_title, credits, course_type FROM courses ORDER BY course_type, course_code";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n" + "=".repeat(75));
            System.out.printf("%-12s  %-38s  %-8s  %s%n", "Code", "Title", "Credits", "Type");
            System.out.println("-".repeat(75));
            int count = 0;
            while (rs.next()) {
                System.out.printf("%-12s  %-38s  %-8d  %s%n",
                    rs.getString("course_code"),
                    rs.getString("course_title"),
                    rs.getInt("credits"),
                    rs.getString("course_type"));
                count++;
            }
            System.out.println("-".repeat(75));
            System.out.println("  Total Courses: " + count);
            System.out.println("=".repeat(75));

        } catch (SQLException e) {
            System.out.println("[ERROR] listAllCourses: " + e.getMessage());
        }
    }

    /**
     * Checks whether a course code exists.
     */
    public boolean courseExists(String courseCode) {
        String sql = "SELECT 1 FROM courses WHERE course_code = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, courseCode.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // ============================================================
    //  SECTION 3 – ENROLLMENT MANAGEMENT
    // ============================================================

    /**
     * Enrolls a student in a course for a given semester and
     * initialises an empty assessment_marks row for that enrollment.
     *
     * @param rollNumber Roll number of the student
     * @param courseCode Course code to enroll in
     * @param semester   Semester number
     * @return true on success
     */
    public boolean enrollStudent(String rollNumber, String courseCode, int semester) {
        String enrollSQL = "INSERT INTO enrollments (roll_number, course_code, semester) VALUES (?, ?, ?)";
        String marksSQL  = "INSERT INTO assessment_marks (enrollment_id) VALUES (?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Insert enrollment
            int enrollmentId;
            try (PreparedStatement ps = con.prepareStatement(enrollSQL, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, rollNumber.toUpperCase().trim());
                ps.setString(2, courseCode.toUpperCase().trim());
                ps.setInt(3, semester);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Enrollment ID not generated.");
                    enrollmentId = keys.getInt(1);
                }
            }

            // Insert empty marks record
            try (PreparedStatement ps = con.prepareStatement(marksSQL)) {
                ps.setInt(1, enrollmentId);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[ERROR] Student '" + rollNumber + "' is already enrolled in '" + courseCode + "' for Semester " + semester + ".");
            rollbackQuietly(con);
            return false;
        } catch (SQLException e) {
            System.out.println("[ERROR] enrollStudent: " + e.getMessage());
            rollbackQuietly(con);
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /**
     * Returns the enrollment_id for a given (roll, course, sem) triple.
     * Returns -1 if not found.
     */
    public int getEnrollmentId(String rollNumber, String courseCode, int semester) {
        String sql = "SELECT enrollment_id FROM enrollments WHERE roll_number=? AND course_code=? AND semester=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNumber.toUpperCase().trim());
            ps.setString(2, courseCode.toUpperCase().trim());
            ps.setInt(3, semester);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("enrollment_id") : -1;
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    // ============================================================
    //  SECTION 4 – ATTENDANCE MANAGEMENT
    // ============================================================

    /**
     * Updates (or adds to) the attendance counters for an enrollment.
     * Pass delta values – classes held and classes attended since last update.
     *
     * @param enrollmentId    Primary key from enrollments table
     * @param additionalHeld  Number of new classes held to add
     * @param additionalAtt   Number of new classes attended to add
     * @return true on success
     */
    public boolean updateAttendance(int enrollmentId, int additionalHeld, int additionalAtt) {
        String sql = "UPDATE enrollments SET total_classes = total_classes + ?, attended_classes = attended_classes + ? WHERE enrollment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, additionalHeld);
            ps.setInt(2, additionalAtt);
            ps.setInt(3, enrollmentId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] updateAttendance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sets (overwrites) absolute attendance values for an enrollment.
     */
    public boolean setAttendance(int enrollmentId, int totalHeld, int totalAttended) {
        String sql = "UPDATE enrollments SET total_classes = ?, attended_classes = ? WHERE enrollment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, totalHeld);
            ps.setInt(2, totalAttended);
            ps.setInt(3, enrollmentId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] setAttendance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Displays all enrollments for a student with live attendance %.
     */
    public void showStudentEnrollments(String rollNumber) {
        String sql =
            "SELECT e.enrollment_id, e.course_code, c.course_title, c.course_type, c.credits, " +
            "       e.semester, e.total_classes, e.attended_classes " +
            "FROM enrollments e " +
            "JOIN courses c ON e.course_code = c.course_code " +
            "WHERE e.roll_number = ? " +
            "ORDER BY e.semester, e.course_code";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNumber.toUpperCase().trim());
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n" + "=".repeat(90));
                System.out.printf("%-6s  %-10s  %-35s  %-10s  %-4s  %-6s  %-6s  %s%n",
                    "EncID", "Code", "Title", "Type", "Cred", "Held", "Att", "Att%");
                System.out.println("-".repeat(90));
                int count = 0;
                while (rs.next()) {
                    int held = rs.getInt("total_classes");
                    int att  = rs.getInt("attended_classes");
                    double pct = held > 0 ? (att * 100.0 / held) : 0.0;
                    System.out.printf("%-6d  %-10s  %-35s  %-10s  %-4d  %-6d  %-6d  %.1f%%%n",
                        rs.getInt("enrollment_id"),
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getString("course_type"),
                        rs.getInt("credits"),
                        held, att, pct);
                    count++;
                }
                System.out.println("-".repeat(90));
                System.out.println("  Total Enrollments: " + count);
                System.out.println("=".repeat(90));
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] showStudentEnrollments: " + e.getMessage());
        }
    }

    // ============================================================
    //  UTILITIES
    // ============================================================

    private void rollbackQuietly(Connection con) {
        if (con != null) {
            try { con.rollback(); } catch (SQLException ignored) {}
        }
    }

    /** Returns a list of all roll numbers – used by reports. */
    public List<String> getAllRollNumbers() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT roll_number FROM students ORDER BY roll_number";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("roll_number"));
        } catch (SQLException e) {
            System.out.println("[ERROR] getAllRollNumbers: " + e.getMessage());
        }
        return list;
    }

    /** Returns a list of distinct departments. */
    public List<String> getAllDepartments() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT department FROM students ORDER BY department";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("department"));
        } catch (SQLException e) {
            System.out.println("[ERROR] getAllDepartments: " + e.getMessage());
        }
        return list;
    }
}
