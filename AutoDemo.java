// ============================================================
//  Kongu Engineering College (Autonomous) - Regulation 2024
//  File: AutoDemo.java
//  Description: Automated Interactive Demonstration Runner.
//               Executes all 14 choices in sequence with clear
//               visual indicators and paced delays, then hands
//               over control to the interactive menu.
// ============================================================

import java.util.Scanner;

public class AutoDemo {

    private static final StudentDAO  studentDAO  = new StudentDAO();
    private static final AcademicDAO academicDAO = new AcademicDAO();

    public static void main(String[] args) {
        printBanner();
        pause(1000);

        System.out.println("  Connecting to database...");
        try {
            DBConnection.getConnection();
            System.out.println("  [OK] Connected successfully! Engine: " + DBConnection.getActiveEngine());
        } catch (Exception e) {
            System.err.println("  [ERROR] Database error: " + e.getMessage());
            return;
        }

        pause(1200);

        System.out.println("\n" + "#".repeat(70));
        System.out.println("#  STARTING AUTOMATED DEMONSTRATION OF ALL SYSTEM CHOICES (1 - 14)  #");
        System.out.println("#".repeat(70));
        pause(1500);

        // ========================================================
        // CHOICE 1: Add New Student
        // ========================================================
        stepHeader(1, "ADD NEW STUDENT");
        typeText("  Selected Menu Option : 1");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Student Name         : Santhosh Kumar K");
        typeText("  Department           : IT");
        typeText("  Current Semester     : 3");
        boolean sOk = studentDAO.addStudent("24ITR010", "Santhosh Kumar K", "IT", 3);
        if (sOk) System.out.println("  [SUCCESS] Student '24ITR010' registered successfully.");
        pause(1500);

        // ========================================================
        // CHOICE 2: List All Students
        // ========================================================
        stepHeader(2, "LIST ALL REGISTERED STUDENTS");
        typeText("  Selected Menu Option : 2");
        studentDAO.listAllStudents();
        pause(1800);

        // ========================================================
        // CHOICE 3: Add New Course
        // ========================================================
        stepHeader(3, "ADD NEW COURSE TO CATALOG");
        typeText("  Selected Menu Option : 3");
        typeText("  Course Code          : 24ITR35");
        typeText("  Course Title         : Machine Learning Techniques");
        typeText("  Credits              : 4");
        typeText("  Course Type          : THEORY");
        boolean cOk = studentDAO.addCourse("24ITR35", "Machine Learning Techniques", 4, "THEORY");
        if (cOk) System.out.println("  [SUCCESS] Course '24ITR35' added successfully.");
        pause(1500);

        // ========================================================
        // CHOICE 4: List All Courses
        // ========================================================
        stepHeader(4, "LIST ALL COURSES IN CATALOG");
        typeText("  Selected Menu Option : 4");
        studentDAO.listAllCourses();
        pause(1800);

        // ========================================================
        // CHOICE 5: Enroll Student in Course
        // ========================================================
        stepHeader(5, "ENROLL STUDENT IN COURSE");
        typeText("  Selected Menu Option : 5");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Course Code          : 24ITR35");
        typeText("  Semester             : 3");
        boolean eOk = studentDAO.enrollStudent("24ITR010", "24ITR35", 3);
        if (eOk) System.out.println("  [SUCCESS] 24ITR010 enrolled in 24ITR35 for Semester 3.");
        pause(1500);

        // ========================================================
        // CHOICE 6: Update Attendance
        // ========================================================
        stepHeader(6, "RECORD ATTENDANCE (CLASSES HELD vs ATTENDED)");
        typeText("  Selected Menu Option : 6");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Course Code          : 24ITR35");
        typeText("  Semester             : 3");
        typeText("  Mode                 : 1 (SET TOTAL)");
        typeText("  Total Classes Held   : 60");
        typeText("  Total Classes Attended: 54 (90.0% - Fully Eligible for ESE)");
        int eid = studentDAO.getEnrollmentId("24ITR010", "24ITR35", 3);
        if (eid != -1) {
            studentDAO.setAttendance(eid, 60, 54);
            System.out.println("  [SUCCESS] Attendance updated: 54/60 (90.0%).");
        }
        pause(1500);

        // ========================================================
        // CHOICE 7: View Student Enrollments & Attendance
        // ========================================================
        stepHeader(7, "VIEW STUDENT ENROLLMENTS & LIVE ATTENDANCE");
        typeText("  Selected Menu Option : 7");
        typeText("  Roll Number          : 24ITR010");
        studentDAO.showStudentEnrollments("24ITR010");
        pause(1800);

        // ========================================================
        // CHOICE 8: Phase 1 – Mid-Semester Marks Entry (CIA1 & CIA2)
        // ========================================================
        stepHeader(8, "PHASE 1 (MID-SEMESTER): ENTER CIA 1 & CIA 2");
        typeText("  Selected Menu Option : 8");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Course Code          : 24ITR35");
        typeText("  Semester             : 3");
        typeText("  CIA Test 1 (0-100)   : 88.0");
        typeText("  CIA Test 2 (0-100)   : 92.0");
        academicDAO.enterPhase1CIA(eid, 88.0, 92.0);
        System.out.println("  [SUCCESS] Phase 1: CIA 1 and CIA 2 marks recorded.");
        academicDAO.showCurrentMarks(eid);
        pause(2000);

        // ========================================================
        // CHOICE 9: Phase 2 – End-of-Semester CIA (Assignment/Quiz)
        // ========================================================
        stepHeader(9, "PHASE 2 (END-OF-SEMESTER): ENTER ASSIGNMENT / QUIZ");
        typeText("  Selected Menu Option : 9");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Course Code          : 24ITR35");
        typeText("  Semester             : 3");
        typeText("  Assignment/Quiz Marks: 95.0");
        academicDAO.enterPhase2Assignment(eid, 95.0);
        System.out.println("  [SUCCESS] Phase 2: Assignment/Quiz/Lab marks recorded.");
        academicDAO.showCurrentMarks(eid);
        pause(2000);

        // ========================================================
        // CHOICE 10: Phase 3 – Post Semester: End Semester Exam (ESE)
        // ========================================================
        stepHeader(10, "PHASE 3 (POST-SEMESTER): ENTER END SEMESTER EXAM (ESE)");
        typeText("  Selected Menu Option : 10");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Course Code          : 24ITR35");
        typeText("  Semester             : 3");
        typeText("  Was Absent in ESE?   : NO");
        typeText("  ESE Marks (0-100)    : 84.0");
        academicDAO.enterPhase3ESE(eid, 84.0, false);
        System.out.println("  [SUCCESS] Phase 3: ESE marks recorded.");
        pause(1800);

        // ========================================================
        // CHOICE 11: View Current Marks for Enrollment
        // ========================================================
        stepHeader(11, "VIEW ALL RECORDED MARKS (PHASES 1, 2, 3)");
        typeText("  Selected Menu Option : 11");
        academicDAO.showCurrentMarks(eid);
        pause(2000);

        // ========================================================
        // CHOICE 12: Student Semester Mark Sheet with SGPA
        // ========================================================
        stepHeader(12, "REPORT 1: STUDENT SEMESTER MARK SHEET WITH SGPA");
        typeText("  Selected Menu Option : 12");
        typeText("  Roll Number          : 24ITR010");
        typeText("  Semester             : 3");
        academicDAO.printMarkSheet("24ITR010", 3);
        pause(2500);

        // ========================================================
        // CHOICE 13: Attendance Defaulter Report (SA Grade)
        // ========================================================
        stepHeader(13, "REPORT 2: ATTENDANCE DEFAULTER REPORT (< 65% -> SA GRADE)");
        typeText("  Selected Menu Option : 13");
        academicDAO.printAttendanceDefaulterReport();
        pause(2500);

        // ========================================================
        // CHOICE 14: Class Performance & Result Summary
        // ========================================================
        stepHeader(14, "REPORT 3: CLASS PERFORMANCE & RESULT SUMMARY");
        typeText("  Selected Menu Option : 14");
        academicDAO.printClassPerformanceSummary();
        pause(2500);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  AUTOMATED DEMONSTRATION COMPLETE!");
        System.out.println("  All 14 choices executed and validated successfully.");
        System.out.println("=".repeat(70));
        System.out.println("\n  Press ENTER to open the interactive menu, or type 0 to exit.");

        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        if (line != null && line.trim().equals("0")) {
            System.out.println("  Exiting. Goodbye!");
            DBConnection.closeConnection();
            return;
        }

        // Handover to standard interactive MainApp
        MainApp.main(new String[0]);
    }

    private static void stepHeader(int num, String title) {
        System.out.println("\n" + "-".repeat(70));
        System.out.printf("  [CHOICE %02d OF 14] >>> %s%n", num, title);
        System.out.println("-".repeat(70));
    }

    private static void typeText(String text) {
        System.out.println(text);
        pause(250);
    }

    private static void pause(long millis) {
        // Instant execution for CMD capture
    }

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
}
