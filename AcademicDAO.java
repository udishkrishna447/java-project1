// ============================================================
//  Kongu Engineering College (R2024) – Academic System
//  File: AcademicDAO.java
//  Description: Marks entry (incremental phases), grading engine,
//               and all report generators (Mark Sheet, Defaulter,
//               Class Performance Summary).
// ============================================================

import java.sql.*;
import java.util.List;

public class AcademicDAO {

    // ============================================================
    //  SECTION 1 – INCREMENTAL MARKS ENTRY
    // ============================================================

    /**
     * PHASE 1 – Mid-Semester: Enter CIA Test 1 and CIA Test 2 marks.
     *
     * @param enrollmentId enrollment primary key
     * @param cia1         marks out of 100 (0–100)
     * @param cia2         marks out of 100 (0–100)
     * @return true on success
     */
    public boolean enterPhase1CIA(int enrollmentId, double cia1, double cia2) {
        if (cia1 < 0 || cia1 > 100 || cia2 < 0 || cia2 > 100) {
            System.out.println("[ERROR] CIA marks must be between 0 and 100.");
            return false;
        }
        String sql = "UPDATE assessment_marks SET cia1 = ?, cia2 = ? WHERE enrollment_id = ?";
        return executeMarkUpdate(sql, cia1, cia2, enrollmentId);
    }

    /**
     * PHASE 2 – End of Semester CIA: Enter Assignment / Quiz / Lab marks.
     *
     * @param enrollmentId       enrollment primary key
     * @param assignmentQuizLab  marks out of 100 (0–100)
     * @return true on success
     */
    public boolean enterPhase2Assignment(int enrollmentId, double assignmentQuizLab) {
        if (assignmentQuizLab < 0 || assignmentQuizLab > 100) {
            System.out.println("[ERROR] Assignment/Quiz/Lab marks must be between 0 and 100.");
            return false;
        }
        String sql = "UPDATE assessment_marks SET assignment_quiz_lab = ? WHERE enrollment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, assignmentQuizLab);
            ps.setInt(2, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] enterPhase2Assignment: " + e.getMessage());
            return false;
        }
    }

    /**
     * PHASE 3 – Post-Semester: Enter ESE marks or mark student as Absent (AB).
     *
     * @param enrollmentId enrollment primary key
     * @param eseMarks     marks out of 100 (0–100) – ignored if absent = true
     * @param absent       true if student was absent in ESE (marks AB)
     * @return true on success
     */
    public boolean enterPhase3ESE(int enrollmentId, double eseMarks, boolean absent) {
        if (!absent && (eseMarks < 0 || eseMarks > 100)) {
            System.out.println("[ERROR] ESE marks must be between 0 and 100.");
            return false;
        }
        String sql = absent
            ? "UPDATE assessment_marks SET ese_marks = NULL, ese_absent = 1 WHERE enrollment_id = ?"
            : "UPDATE assessment_marks SET ese_marks = ?, ese_absent = 0 WHERE enrollment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (!absent) {
                ps.setDouble(1, eseMarks);
                ps.setInt(2, enrollmentId);
            } else {
                ps.setInt(1, enrollmentId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] enterPhase3ESE: " + e.getMessage());
            return false;
        }
    }

    /**
     * Displays all current marks for a given enrollment (all phases).
     */
    public void showCurrentMarks(int enrollmentId) {
        String sql =
            "SELECT am.*, e.roll_number, e.course_code, c.course_type " +
            "FROM assessment_marks am " +
            "JOIN enrollments e ON am.enrollment_id = e.enrollment_id " +
            "JOIN courses c     ON e.course_code    = c.course_code " +
            "WHERE am.enrollment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n+-- Current Marks (Enrollment ID: " + enrollmentId + ") --+");
                    System.out.printf("  Student  : %s%n", rs.getString("roll_number"));
                    System.out.printf("  Course   : %s  [%s]%n", rs.getString("course_code"), rs.getString("course_type"));
                    System.out.printf("  CIA 1    : %s%n", nullSafe(rs, "cia1"));
                    System.out.printf("  CIA 2    : %s%n", nullSafe(rs, "cia2"));
                    System.out.printf("  Asgn/Quiz: %s%n", nullSafe(rs, "assignment_quiz_lab"));
                    System.out.printf("  ESE      : %s%n", rs.getInt("ese_absent") == 1 ? "AB (Absent)" : nullSafe(rs, "ese_marks"));
                } else {
                    System.out.println("[WARN] No marks record for Enrollment ID: " + enrollmentId);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] showCurrentMarks: " + e.getMessage());
        }
    }

    // ============================================================
    //  SECTION 2 – GRADING ENGINE (KEC R2024)
    // ============================================================

    /**
     * Core engine: calculates CIA total, ESE scaled marks, aggregate,
     * applies attendance / absence rules, derives letter grade and GP.
     *
     * All weightage values are per KEC Regulation 2024.
     */
    private GradeResult computeGrade(
            double cia1, double cia2, double aql, double ese,
            boolean eseAbsent,
            double attendancePct, String courseType) {

        GradeResult res = new GradeResult();

        // ---- Step 1: Attendance Gate ----------------------------------------
        if (attendancePct < 65.0) {
            res.grade   = "SA";
            res.gradePoint = 0;
            res.status  = "BARRED";
            res.ciaTotal = 0; res.eseScaled = 0; res.aggregate = 0;
            return res;
        }
        res.conditionalEligible = (attendancePct >= 65.0 && attendancePct < 75.0);

        // ---- Step 2: Absent in ESE ------------------------------------------
        if (eseAbsent) {
            res.grade   = "AB";
            res.gradePoint = 0;
            res.status  = "ABSENT";
            // CIA components still computed for records
            res.ciaTotal  = computeCIATotal(cia1, cia2, aql, courseType);
            res.eseScaled = 0;
            res.aggregate = 0;
            return res;
        }

        // ---- Step 3: CIA Computation ----------------------------------------
        //   CIA raw = average of (CIA1 + CIA2) / 2  and  Assignment/Quiz/Lab
        //   Then scale to CIA weightage marks.
        double ciaRaw   = computeCIATotal(cia1, cia2, aql, courseType);  // out of 100
        res.ciaTotal    = ciaRaw;

        // ---- Step 4: ESE Scaling & Minimum Threshold ------------------------
        double ciaWeight, eseWeight, eseThreshold, totalMarks = 100.0;
        switch (courseType) {
            case "THEORY":
                ciaWeight    = 40.0;
                eseWeight    = 60.0;
                eseThreshold = 27.0;   // 45% of 60
                break;
            case "PRACTICAL":
                ciaWeight    = 60.0;
                eseWeight    = 40.0;
                eseThreshold = 20.0;   // 50% of 40
                break;
            case "EMBEDDED":
            default:
                ciaWeight    = 50.0;
                eseWeight    = 50.0;
                eseThreshold = 22.5;   // 45% of 50
                break;
        }

        double ciaScaled = (ciaRaw / 100.0) * ciaWeight;    // scaled CIA marks
        double eseScaled = (ese    / 100.0) * eseWeight;    // scaled ESE marks
        res.eseScaled    = eseScaled;
        res.aggregate    = ciaScaled + eseScaled;

        // ---- Step 5: ESE Minimum Threshold check ----------------------------
        boolean esePass = (eseScaled >= eseThreshold);

        // ---- Step 6: Derive Grade -------------------------------------------
        if (!esePass || res.aggregate < 50.0) {
            res.grade      = "RA";
            res.gradePoint = 0;
            res.status     = "FAIL";
        } else {
            // Map aggregate to letter grade (10-point system)
            double ag = res.aggregate;
            if      (ag >= 91) { res.grade = "O";  res.gradePoint = 10; }
            else if (ag >= 81) { res.grade = "A+"; res.gradePoint = 9;  }
            else if (ag >= 71) { res.grade = "A";  res.gradePoint = 8;  }
            else if (ag >= 61) { res.grade = "B+"; res.gradePoint = 7;  }
            else               { res.grade = "B";  res.gradePoint = 6;  }
            res.status = "PASS";
        }
        return res;
    }

    /**
     * Computes the CIA component (raw, out of 100) based on course type.
     *   THEORY    : average of top-2 CIA tests × 70% + Assignment/Quiz × 30%
     *   PRACTICAL : average of CIA1 & CIA2 × 50% + Lab Eval × 50%
     *   EMBEDDED  : average of CIA1 & CIA2 × 60% + Assignment × 40%
     *
     * If any component is NULL (not yet entered), returns 0.
     */
    private double computeCIATotal(double cia1, double cia2, double aql, String courseType) {
        switch (courseType) {
            case "THEORY":
                // Average of CIA1 & CIA2 (best 2 of 2 here) → 70%, AQL → 30%
                return ((cia1 + cia2) / 2.0) * 0.70 + aql * 0.30;
            case "PRACTICAL":
                return ((cia1 + cia2) / 2.0) * 0.50 + aql * 0.50;
            case "EMBEDDED":
            default:
                return ((cia1 + cia2) / 2.0) * 0.60 + aql * 0.40;
        }
    }

    // ============================================================
    //  SECTION 3 – REPORT 1: STUDENT SEMESTER MARK SHEET
    // ============================================================

    /**
     * Prints the full semester mark sheet for a student.
     * Computes grades for each course, SGPA, and percentage.
     *
     * @param rollNumber student roll number
     * @param semester   semester to report
     */
    public void printMarkSheet(String rollNumber, int semester) {
        String sql =
            "SELECT e.enrollment_id, e.course_code, c.course_title, c.credits, c.course_type, " +
            "       e.total_classes, e.attended_classes, " +
            "       am.cia1, am.cia2, am.assignment_quiz_lab, am.ese_marks, am.ese_absent " +
            "FROM enrollments e " +
            "JOIN courses c          ON e.course_code    = c.course_code " +
            "JOIN assessment_marks am ON e.enrollment_id = am.enrollment_id " +
            "JOIN students s          ON e.roll_number   = s.roll_number " +
            "WHERE e.roll_number = ? AND e.semester = ? " +
            "ORDER BY e.course_code";

        String studentNameSql = "SELECT student_name, department FROM students WHERE roll_number = ?";

        try (Connection con = DBConnection.getConnection()) {

            // Fetch student name & dept
            String studentName = rollNumber, department = "";
            try (PreparedStatement ps = con.prepareStatement(studentNameSql)) {
                ps.setString(1, rollNumber.toUpperCase().trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        studentName = rs.getString("student_name");
                        department  = rs.getString("department");
                    }
                }
            }

            System.out.println("\n");
            System.out.println("=".repeat(100));
            System.out.println("         KONGU ENGINEERING COLLEGE (AUTONOMOUS) – Affiliated to Anna University");
            System.out.println("                  STUDENT SEMESTER MARK SHEET  [Regulation 2024]");
            System.out.println("=".repeat(100));
            System.out.printf("  Roll Number : %-20s  Name       : %s%n", rollNumber.toUpperCase(), studentName);
            System.out.printf("  Department  : %-20s  Semester   : %d%n", department, semester);
            System.out.println("-".repeat(100));
            System.out.printf("%-10s %-28s %-4s %-10s %6s %6s %6s %8s %6s  %-4s %6s  %-6s%n",
                "Code", "Title", "Cr", "Type",
                "CIA1", "CIA2", "Asgn", "CIA Tot", "ESE Sc", "Gr", "GP", "Status");
            System.out.println("-".repeat(100));

            double totalCreditPoints = 0.0;
            int    totalCredits      = 0;
            int    passCount         = 0, failCount = 0;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, rollNumber.toUpperCase().trim());
                ps.setInt(2, semester);

                try (ResultSet rs = ps.executeQuery()) {
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        int    credits    = rs.getInt("credits");
                        String courseType = rs.getString("course_type");
                        int    held       = rs.getInt("total_classes");
                        int    att        = rs.getInt("attended_classes");
                        double attPct     = held > 0 ? (att * 100.0 / held) : 0.0;

                        // Safe null handling for marks
                        double cia1 = rs.getDouble("cia1"); if (rs.wasNull()) cia1 = 0;
                        double cia2 = rs.getDouble("cia2"); if (rs.wasNull()) cia2 = 0;
                        double aql  = rs.getDouble("assignment_quiz_lab"); if (rs.wasNull()) aql = 0;
                        double ese  = rs.getDouble("ese_marks");           if (rs.wasNull()) ese = 0;
                        boolean absent = rs.getInt("ese_absent") == 1;

                        GradeResult gr = computeGrade(cia1, cia2, aql, ese, absent, attPct, courseType);

                        // Scaled marks for display
                        double ciaWeight = courseType.equals("THEORY") ? 40 : courseType.equals("PRACTICAL") ? 60 : 50;
                        double ciaScaled = (gr.ciaTotal / 100.0) * ciaWeight;

                        System.out.printf("%-10s %-28s %-4d %-10s %6.1f %6.1f %6.1f %8.2f %6.2f  %-4s %6d  %-6s%n",
                            rs.getString("course_code"),
                            truncate(rs.getString("course_title"), 28),
                            credits, courseType,
                            cia1, cia2, aql,
                            ciaScaled, gr.eseScaled,
                            gr.grade, gr.gradePoint,
                            gr.status + (gr.conditionalEligible ? "*" : ""));

                        totalCreditPoints += (double) credits * gr.gradePoint;
                        totalCredits      += credits;
                        if ("PASS".equals(gr.status)) passCount++;
                        else                           failCount++;
                    }
                    if (!any) {
                        System.out.println("  No enrollment records found for this student / semester.");
                    }
                }
            }

            System.out.println("-".repeat(100));

            if (totalCredits > 0) {
                double sgpa = totalCreditPoints / totalCredits;
                double pct  = sgpa * 10.0;
                System.out.printf("%n  Total Credits Enrolled  : %d%n", totalCredits);
                System.out.printf("  Total Credit Points     : %.2f%n", totalCreditPoints);
                System.out.printf("  SGPA                    : %.2f%n", sgpa);
                System.out.printf("  Equivalent Percentage   : %.2f %%%n", pct);
                System.out.printf("  Courses Passed          : %d  |  Courses Failed : %d%n", passCount, failCount);
                System.out.println("  * = Conditional Eligibility (Medical Condonation required)");
            }
            System.out.println("=".repeat(100));

        } catch (SQLException e) {
            System.out.println("[ERROR] printMarkSheet: " + e.getMessage());
        }
    }

    // ============================================================
    //  SECTION 4 – REPORT 2: ATTENDANCE DEFAULTER REPORT
    // ============================================================

    /**
     * Lists all (student, course) combinations where attendance < 65%.
     * These students are flagged SA (Shortage of Attendance).
     */
    public void printAttendanceDefaulterReport() {
        String sql =
            "SELECT s.roll_number, s.student_name, s.department, " +
            "       e.course_code, c.course_title, e.semester, " +
            "       e.total_classes, e.attended_classes, " +
            "       ROUND(e.attended_classes * 100.0 / e.total_classes, 2) AS att_pct " +
            "FROM enrollments e " +
            "JOIN students s ON e.roll_number = s.roll_number " +
            "JOIN courses c  ON e.course_code = c.course_code " +
            "WHERE e.total_classes > 0 " +
            "  AND (e.attended_classes * 100.0 / e.total_classes) < 65 " +
            "ORDER BY att_pct ASC, s.department, s.roll_number";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n");
            System.out.println("=".repeat(95));
            System.out.println("         KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
            System.out.println("              ATTENDANCE DEFAULTER REPORT  [< 65% Attendance  →  Grade: SA]");
            System.out.println("=".repeat(95));
            System.out.printf("%-12s  %-25s  %-5s  %-10s  %-30s  %5s  %5s  %6s%n",
                "Roll No.", "Name", "Dept", "Code", "Course Title", "Held", "Att", "Att%");
            System.out.println("-".repeat(95));

            int count = 0;
            while (rs.next()) {
                System.out.printf("%-12s  %-25s  %-5s  %-10s  %-30s  %5d  %5d  %5.1f%%%n",
                    rs.getString("roll_number"),
                    truncate(rs.getString("student_name"), 25),
                    rs.getString("department"),
                    rs.getString("course_code"),
                    truncate(rs.getString("course_title"), 30),
                    rs.getInt("total_classes"),
                    rs.getInt("attended_classes"),
                    rs.getDouble("att_pct"));
                count++;
            }

            System.out.println("-".repeat(95));
            if (count == 0) {
                System.out.println("  *** No attendance defaulters found. ***");
            } else {
                System.out.println("  Total Defaulter Records: " + count + "  (All awarded Grade SA, Grade Point = 0)");
            }
            System.out.println("=".repeat(95));

        } catch (SQLException e) {
            System.out.println("[ERROR] printAttendanceDefaulterReport: " + e.getMessage());
        }
    }

    // ============================================================
    //  SECTION 5 – REPORT 3: CLASS PERFORMANCE & RESULT SUMMARY
    // ============================================================

    /**
     * Generates a department-wise / semester-wise performance summary:
     * pass count, fail count, class average SGPA, and overall attendance %.
     */
    public void printClassPerformanceSummary() {
        // Get distinct department-semester pairs
        String deptSemSQL =
            "SELECT DISTINCT s.department, e.semester " +
            "FROM students s JOIN enrollments e ON s.roll_number = e.roll_number " +
            "ORDER BY s.department, e.semester";

        String detailSQL =
            "SELECT e.roll_number, e.course_code, c.credits, c.course_type, " +
            "       e.total_classes, e.attended_classes, " +
            "       am.cia1, am.cia2, am.assignment_quiz_lab, am.ese_marks, am.ese_absent " +
            "FROM enrollments e " +
            "JOIN courses c           ON e.course_code    = c.course_code " +
            "JOIN assessment_marks am ON e.enrollment_id  = am.enrollment_id " +
            "JOIN students s          ON e.roll_number    = s.roll_number " +
            "WHERE s.department = ? AND e.semester = ?";

        System.out.println("\n");
        System.out.println("=".repeat(80));
        System.out.println("      KONGU ENGINEERING COLLEGE (AUTONOMOUS)");
        System.out.println("      CLASS PERFORMANCE & RESULT SUMMARY  [Regulation 2024]");
        System.out.println("=".repeat(80));

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet deptRs = st.executeQuery(deptSemSQL)) {

            while (deptRs.next()) {
                String dept = deptRs.getString("department");
                int    sem  = deptRs.getInt("semester");

                System.out.printf("%n  Department : %-10s   Semester : %d%n", dept, sem);
                System.out.println("  " + "-".repeat(76));
                System.out.printf("  %-12s  %-6s  %-6s  %-4s  %-6s  %-6s%n",
                    "Roll No.", "SGPA", "Pct%", "Pass", "Fail", "Status");
                System.out.println("  " + "-".repeat(76));

                int    totalStudents = 0, totalPass = 0, totalFail = 0;
                double sumSGPA       = 0.0;

                // Group by student within dept+sem
                String studentSQL =
                    "SELECT DISTINCT e.roll_number FROM enrollments e " +
                    "JOIN students s ON e.roll_number = s.roll_number " +
                    "WHERE s.department = ? AND e.semester = ? ORDER BY e.roll_number";

                try (PreparedStatement stStu = con.prepareStatement(studentSQL)) {
                    stStu.setString(1, dept);
                    stStu.setInt(2, sem);
                    try (ResultSet stuRs = stStu.executeQuery()) {
                        while (stuRs.next()) {
                            String roll = stuRs.getString("roll_number");

                            double totalCreditPoints = 0.0;
                            int    totalCredits      = 0;
                            int    passC = 0, failC = 0;

                            try (PreparedStatement stD = con.prepareStatement(detailSQL)) {
                                stD.setString(1, dept);
                                stD.setInt(2, sem);
                                try (ResultSet dr = stD.executeQuery()) {
                                    while (dr.next()) {
                                        if (!dr.getString("roll_number").equals(roll)) continue;
                                        int    credits    = dr.getInt("credits");
                                        String ct         = dr.getString("course_type");
                                        int    held       = dr.getInt("total_classes");
                                        int    att        = dr.getInt("attended_classes");
                                        double attPct     = held > 0 ? (att * 100.0 / held) : 0.0;

                                        double cia1 = dr.getDouble("cia1"); if (dr.wasNull()) cia1 = 0;
                                        double cia2 = dr.getDouble("cia2"); if (dr.wasNull()) cia2 = 0;
                                        double aql  = dr.getDouble("assignment_quiz_lab"); if (dr.wasNull()) aql = 0;
                                        double ese  = dr.getDouble("ese_marks");           if (dr.wasNull()) ese = 0;
                                        boolean absent = dr.getInt("ese_absent") == 1;

                                        GradeResult gr = computeGrade(cia1, cia2, aql, ese, absent, attPct, ct);
                                        totalCreditPoints += (double) credits * gr.gradePoint;
                                        totalCredits      += credits;
                                        if ("PASS".equals(gr.status)) passC++;
                                        else                           failC++;
                                    }
                                }
                            }

                            double sgpa = totalCredits > 0 ? totalCreditPoints / totalCredits : 0.0;
                            double pct  = sgpa * 10.0;
                            String overallStatus = failC == 0 ? "PASS" : "FAIL";

                            System.out.printf("  %-12s  %6.2f  %5.1f%%  %4d  %6d  %-6s%n",
                                roll, sgpa, pct, passC, failC, overallStatus);

                            totalStudents++;
                            sumSGPA += sgpa;
                            if (failC == 0) totalPass++;
                            else            totalFail++;
                        }
                    }
                }

                double avgSGPA = totalStudents > 0 ? sumSGPA / totalStudents : 0.0;
                int    passRate = totalStudents > 0 ? (totalPass * 100 / totalStudents) : 0;

                System.out.println("  " + "-".repeat(76));
                System.out.printf("  Total Students : %-5d  Passed : %-5d  Failed : %-5d  Pass Rate : %d%%%n",
                    totalStudents, totalPass, totalFail, passRate);
                System.out.printf("  Class Average SGPA : %.2f   Equivalent Percentage : %.2f %%%n",
                    avgSGPA, avgSGPA * 10.0);
                System.out.println("  " + "-".repeat(76));
            }

        } catch (SQLException e) {
            System.out.println("[ERROR] printClassPerformanceSummary: " + e.getMessage());
        }
        System.out.println("=".repeat(80));
    }

    // ============================================================
    //  INNER CLASS – Grade Result Container
    // ============================================================

    /** Holds the computed result of the grading engine for one course. */
    static class GradeResult {
        double  ciaTotal          = 0.0;
        double  eseScaled         = 0.0;
        double  aggregate         = 0.0;
        String  grade             = "RA";
        int     gradePoint        = 0;
        String  status            = "FAIL";   // PASS | FAIL | BARRED | ABSENT
        boolean conditionalEligible = false;
    }

    // ============================================================
    //  UTILITIES
    // ============================================================

    private boolean executeMarkUpdate(String sql, double v1, double v2, int enrollmentId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, v1);
            ps.setDouble(2, v2);
            ps.setInt(3, enrollmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ERROR] executeMarkUpdate: " + e.getMessage());
            return false;
        }
    }

    private String nullSafe(ResultSet rs, String col) throws SQLException {
        Object v = rs.getObject(col);
        return v == null ? "-- (not entered)" : v.toString();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}
