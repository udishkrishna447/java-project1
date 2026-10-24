// ============================================================
//  Kongu Engineering College (R2024) – Academic System
//  File: MainApp.java
//  Description: Menu-driven command-prompt interface for all
//               data entry and report generation operations.
//
//  HOW TO COMPILE & RUN:
//    javac -cp ".;mysql-connector-j-*.jar" DBConnection.java StudentDAO.java AcademicDAO.java MainApp.java
//    java  -cp ".;mysql-connector-j-*.jar" MainApp
//
//  (On Linux/Mac replace ';' with ':' in the classpath)
// ============================================================

import java.sql.Connection;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MainApp {

    private static final Scanner    scanner     = new Scanner(System.in);
    private static final StudentDAO studentDAO  = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();

    // ============================================================
    //  MAIN ENTRY POINT
    // ============================================================
    public static void main(String[] args) {
        printBanner();
        testDBConnection();     // Verify connection before proceeding

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter your choice: ");
            switch (choice) {
                // ---- Student & Course Management ----
                case 1  -> menuAddStudent();
                case 2  -> menuListStudents();
                case 3  -> menuAddCourse();
                case 4  -> menuListCourses();
                case 5  -> menuEnrollStudent();
                case 6  -> menuUpdateAttendance();
                case 7  -> menuShowEnrollments();

                // ---- Incremental Marks Entry --------
                case 8  -> menuEnterPhase1CIA();
                case 9  -> menuEnterPhase2Assignment();
                case 10 -> menuEnterPhase3ESE();
                case 11 -> menuViewCurrentMarks();

                // ---- Reports -------------------------
                case 12 -> menuMarkSheet();
                case 13 -> academicDAO.printAttendanceDefaulterReport();
                case 14 -> academicDAO.printClassPerformanceSummary();

                case 0  -> {
                    System.out.println("\n  Closing application. Goodbye!");
                    DBConnection.closeConnection();
                    running = false;
                }
                default -> System.out.println("  [WARN] Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    // ============================================================
    //  BANNER & MENU DISPLAY
    // ============================================================

    private static void printBanner() {
        System.out.println();
        System.out.println("*".repeat(70));
        System.out.println("*" + " ".repeat(68) + "*");
        System.out.println("*    KONGU ENGINEERING COLLEGE (AUTONOMOUS)                        *");
        System.out.println("*    Student Academic & Continuous Evaluation System               *");
        System.out.println("*    Regulation 2024  |  Java JDBC Application                    *");
        System.out.println("*" + " ".repeat(68) + "*");
        System.out.println("*".repeat(70));
    }

    private static void printMainMenu() {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("             MAIN MENU");
        System.out.println("=".repeat(55));
        System.out.println("  -- Student & Course Management --");
        System.out.println("   1.  Add New Student");
        System.out.println("   2.  List All Students");
        System.out.println("   3.  Add New Course");
        System.out.println("   4.  List All Courses");
        System.out.println("   5.  Enroll Student in Course");
        System.out.println("   6.  Update Attendance");
        System.out.println("   7.  View Student Enrollments & Attendance");
        System.out.println();
        System.out.println("  -- Incremental Marks Entry (Phase-wise) --");
        System.out.println("   8.  Phase 1 – Enter CIA Test 1 & CIA Test 2");
        System.out.println("   9.  Phase 2 – Enter Assignment / Quiz / Lab");
        System.out.println("  10.  Phase 3 – Enter ESE Marks / Mark Absent");
        System.out.println("  11.  View Current Marks for Enrollment");
        System.out.println();
        System.out.println("  -- Reports --");
        System.out.println("  12.  Student Semester Mark Sheet");
        System.out.println("  13.  Attendance Defaulter Report  (SA Grade)");
        System.out.println("  14.  Class Performance & Result Summary");
        System.out.println();
        System.out.println("   0.  Exit");
        System.out.println("=".repeat(55));
    }

    // ============================================================
    //  MENU HANDLERS – Student & Course Management
    // ============================================================

    private static void menuAddStudent() {
        System.out.println("\n--- Add New Student ---");
        System.out.print("  Roll Number (e.g., 24ITR001) : ");
        String roll = scanner.nextLine().trim().toUpperCase();
        System.out.print("  Student Name                 : ");
        String name = scanner.nextLine().trim();
        System.out.print("  Department (CSE/IT/ECE/…)    : ");
        String dept = scanner.nextLine().trim().toUpperCase();
        int sem = readInt("  Current Semester (1-8)       : ");

        if (roll.isEmpty() || name.isEmpty() || dept.isEmpty()) {
            System.out.println("  [ERROR] Fields cannot be blank.");
            return;
        }
        if (sem < 1 || sem > 8) {
            System.out.println("  [ERROR] Semester must be between 1 and 8.");
            return;
        }

        boolean ok = studentDAO.addStudent(roll, name, dept, sem);
        if (ok) System.out.println("  [SUCCESS] Student '" + roll + "' added successfully.");
    }

    private static void menuListStudents() {
        studentDAO.listAllStudents();
    }

    private static void menuAddCourse() {
        System.out.println("\n--- Add New Course ---");
        System.out.print("  Course Code  (e.g., 24ITR31)          : ");
        String code = scanner.nextLine().trim().toUpperCase();
        System.out.print("  Course Title                           : ");
        String title = scanner.nextLine().trim();
        int credits = readInt("  Credits (1-6)                          : ");
        System.out.println("  Course Type options: THEORY | PRACTICAL | EMBEDDED");
        System.out.print("  Course Type                            : ");
        String type = scanner.nextLine().trim().toUpperCase();

        if (code.isEmpty() || title.isEmpty()) {
            System.out.println("  [ERROR] Code and title cannot be blank.");
            return;
        }
        if (credits < 1 || credits > 6) {
            System.out.println("  [ERROR] Credits must be between 1 and 6.");
            return;
        }
        if (!type.equals("THEORY") && !type.equals("PRACTICAL") && !type.equals("EMBEDDED")) {
            System.out.println("  [ERROR] Course type must be THEORY, PRACTICAL, or EMBEDDED.");
            return;
        }

        boolean ok = studentDAO.addCourse(code, title, credits, type);
        if (ok) System.out.println("  [SUCCESS] Course '" + code + "' added successfully.");
    }

    private static void menuListCourses() {
        studentDAO.listAllCourses();
    }

    private static void menuEnrollStudent() {
        System.out.println("\n--- Enroll Student in Course ---");
        System.out.print("  Roll Number  : ");
        String roll = scanner.nextLine().trim().toUpperCase();
        System.out.print("  Course Code  : ");
        String code = scanner.nextLine().trim().toUpperCase();
        int sem = readInt("  Semester     : ");

        if (!studentDAO.studentExists(roll)) {
            System.out.println("  [ERROR] Student '" + roll + "' not found.");
            return;
        }
        if (!studentDAO.courseExists(code)) {
            System.out.println("  [ERROR] Course '" + code + "' not found.");
            return;
        }

        boolean ok = studentDAO.enrollStudent(roll, code, sem);
        if (ok) System.out.println("  [SUCCESS] " + roll + " enrolled in " + code + " for Semester " + sem + ".");
    }

    private static void menuUpdateAttendance() {
        System.out.println("\n--- Update Attendance ---");
        System.out.print("  Roll Number  : ");
        String roll = scanner.nextLine().trim().toUpperCase();
        System.out.print("  Course Code  : ");
        String code = scanner.nextLine().trim().toUpperCase();
        int sem = readInt("  Semester     : ");

        int enrollmentId = studentDAO.getEnrollmentId(roll, code, sem);
        if (enrollmentId == -1) {
            System.out.println("  [ERROR] Enrollment not found for the given combination.");
            return;
        }

        System.out.println("  Choose mode:");
        System.out.println("    1. SET total attendance (overwrite)");
        System.out.println("    2. ADD incremental attendance (delta)");
        int mode = readInt("  Mode: ");

        if (mode == 1) {
            int held = readInt("  Total classes HELD    : ");
            int att  = readInt("  Total classes ATTENDED: ");
            if (att > held) { System.out.println("  [ERROR] Attended cannot exceed held."); return; }
            boolean ok = studentDAO.setAttendance(enrollmentId, held, att);
            if (ok) System.out.println("  [SUCCESS] Attendance set: " + att + "/" + held);
        } else if (mode == 2) {
            int deltaHeld = readInt("  Additional classes HELD     : ");
            int deltaAtt  = readInt("  Additional classes ATTENDED : ");
            if (deltaAtt > deltaHeld) { System.out.println("  [ERROR] Attended cannot exceed held."); return; }
            boolean ok = studentDAO.updateAttendance(enrollmentId, deltaHeld, deltaAtt);
            if (ok) System.out.println("  [SUCCESS] Attendance incremented by " + deltaAtt + "/" + deltaHeld + ".");
        } else {
            System.out.println("  [ERROR] Invalid mode selection.");
        }
    }

    private static void menuShowEnrollments() {
        System.out.println("\n--- View Student Enrollments ---");
        System.out.print("  Roll Number: ");
        String roll = scanner.nextLine().trim().toUpperCase();
        if (!studentDAO.studentExists(roll)) {
            System.out.println("  [ERROR] Student '" + roll + "' not found.");
            return;
        }
        studentDAO.showStudentEnrollments(roll);
    }

    // ============================================================
    //  MENU HANDLERS – Incremental Marks Entry
    // ============================================================

    private static void menuEnterPhase1CIA() {
        System.out.println("\n--- Phase 1: Enter CIA Test 1 & CIA Test 2 ---");
        int eid = getEnrollmentIdFromUser();
        if (eid == -1) return;

        double cia1 = readDouble("  CIA Test 1 Marks (0-100): ");
        double cia2 = readDouble("  CIA Test 2 Marks (0-100): ");

        boolean ok = academicDAO.enterPhase1CIA(eid, cia1, cia2);
        if (ok) System.out.println("  [SUCCESS] CIA 1 & CIA 2 marks saved.");
        academicDAO.showCurrentMarks(eid);
    }

    private static void menuEnterPhase2Assignment() {
        System.out.println("\n--- Phase 2: Enter Assignment / Quiz / Lab Marks ---");
        int eid = getEnrollmentIdFromUser();
        if (eid == -1) return;

        double aql = readDouble("  Assignment/Quiz/Lab Marks (0-100): ");

        boolean ok = academicDAO.enterPhase2Assignment(eid, aql);
        if (ok) System.out.println("  [SUCCESS] Assignment/Quiz/Lab marks saved.");
        academicDAO.showCurrentMarks(eid);
    }

    private static void menuEnterPhase3ESE() {
        System.out.println("\n--- Phase 3: Enter End Semester Examination (ESE) ---");
        int eid = getEnrollmentIdFromUser();
        if (eid == -1) return;

        System.out.print("  Was student ABSENT in ESE? (yes/no): ");
        String ans = scanner.nextLine().trim().toLowerCase();
        boolean absent = ans.equals("yes") || ans.equals("y");

        double eseMarks = 0;
        if (!absent) {
            eseMarks = readDouble("  ESE Marks (0-100): ");
        }

        boolean ok = academicDAO.enterPhase3ESE(eid, eseMarks, absent);
        if (ok) System.out.println("  [SUCCESS] ESE entry saved.");
        academicDAO.showCurrentMarks(eid);
    }

    private static void menuViewCurrentMarks() {
        System.out.println("\n--- View Current Marks ---");
        int eid = getEnrollmentIdFromUser();
        if (eid == -1) return;
        academicDAO.showCurrentMarks(eid);
    }

    // ============================================================
    //  MENU HANDLERS – Reports
    // ============================================================

    private static void menuMarkSheet() {
        System.out.println("\n--- Student Semester Mark Sheet ---");
        System.out.print("  Roll Number : ");
        String roll = scanner.nextLine().trim().toUpperCase();
        if (!studentDAO.studentExists(roll)) {
            System.out.println("  [ERROR] Student '" + roll + "' not found.");
            return;
        }
        int sem = readInt("  Semester    : ");
        academicDAO.printMarkSheet(roll, sem);
    }

    // ============================================================
    //  INPUT HELPERS
    // ============================================================

    /**
     * Prompts for Roll Number + Course Code + Semester and returns
     * the enrollment_id, or -1 on failure.
     */
    private static int getEnrollmentIdFromUser() {
        System.out.print("  Roll Number : ");
        String roll = scanner.nextLine().trim().toUpperCase();
        System.out.print("  Course Code : ");
        String code = scanner.nextLine().trim().toUpperCase();
        int sem = readInt("  Semester    : ");

        int eid = studentDAO.getEnrollmentId(roll, code, sem);
        if (eid == -1) {
            System.out.println("  [ERROR] No enrollment found for " + roll + " / " + code + " / Sem " + sem + ".");
            System.out.println("         Please verify student is enrolled first (Option 5).");
        }
        return eid;
    }

    /** Reads an integer from stdin with a prompt, handling invalid input. */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Please enter a valid integer.");
            }
        }
    }

    /** Reads a double from stdin with a prompt, handling invalid input. */
    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("  [ERROR] Please enter a valid decimal number.");
            }
        }
    }

    // ============================================================
    //  DB CONNECTION TEST
    // ============================================================

    private static void testDBConnection() {
        System.out.println("\n  Connecting to database...");
        try {
            Connection con = DBConnection.getConnection();
            if (con != null && !con.isClosed()) {
                System.out.println("  [OK] Connected successfully! Engine: " + DBConnection.getActiveEngine());
            }
        } catch (SQLException e) {
            System.out.println("\n" + "!".repeat(60));
            System.out.println("  DATABASE CONNECTION FAILED!");
            System.out.println("  " + e.getMessage());
            System.out.println("!".repeat(60));
            System.out.println("\n  Steps to fix:");
            System.out.println("  1. Ensure MySQL server is running.");
            System.out.println("  2. Run kec_schema.sql in MySQL Workbench or CLI.");
            System.out.println("  3. Update DB credentials in DBConnection.java.");
            System.out.println("  4. Add mysql-connector-j-*.jar to your classpath.");
            System.out.println("\n  Exiting...");
            System.exit(1);
        }
    }
}
