# Kongu Engineering College (Autonomous) - Regulation 2024
## Student Academic & Continuous Evaluation System using Java JDBC

A database-driven Java application utilizing JDBC to manage student records, perform incremental marks entry (Continuous Internal Assessments and End Semester Examinations), enforce attendance eligibility rules, and generate formal academic performance reports according to **Kongu Engineering College (Autonomous) - Regulation 2024**.

---

## 🚀 Features

1. **Student & Course Management**:
   - Register students with Roll Number, Name, Department (CSE, IT, ECE, MECH, etc.), and Semester.
   - Course Catalog supporting **THEORY**, **PRACTICAL**, and **EMBEDDED** courses with credits (1–6).
   - Continuous Course Enrollment & Attendance Tracking (classes held vs. classes attended).

2. **Incremental Marks Entry ("Then & Now")**:
   - **Phase 1 (Mid-Semester)**: CIA Test 1 (0–100) and CIA Test 2 (0–100).
   - **Phase 2 (End of Semester - CIA)**: Assignments / Quizzes / Lab Evaluations (0–100).
   - **Phase 3 (Post Semester - ESE)**: End Semester Examination (ESE) Marks (0–100) and Absence (AB).

3. **Evaluation Rules & Weightage Engine (KEC R2024)**:
   - **THEORY**: CIA = 40% | ESE = 60% | ESE Min Threshold = 45% (27/60) | Overall Pass = 50 Marks
   - **PRACTICAL**: CIA = 60% | ESE = 40% | ESE Min Threshold = 50% (20/40) | Overall Pass = 50 Marks
   - **EMBEDDED**: CIA = 50% | ESE = 50% | ESE Min Threshold = 45% (22.5/50) | Overall Pass = 50 Marks

4. **Attendance Eligibility Filter**:
   - $\ge 75\%$: Eligible for ESE.
   - $65\% \text{ to } 74.9\%$: Conditionally eligible (Medical Condonation required).
   - $< 65\%$: Awarded **SA** (Shortage of Attendance, Grade Point = 0) and barred from ESE.

5. **10-Point Grading System**:
   - **O** (91–100, GP: 10), **A+** (81–90, GP: 9), **A** (71–80, GP: 8), **B+** (61–70, GP: 7), **B** (50–60, GP: 6)
   - **RA** (Re-Appearance / Fail if < 50 aggregate or failed ESE minimum, GP: 0)
   - **SA** (Shortage of Attendance, GP: 0)
   - **AB** (Absent in ESE, GP: 0)

6. **Academic Reports**:
   - Student Semester Mark Sheet with calculated **SGPA** and **Equivalent Percentage** ($\text{SGPA} \times 10$).
   - Attendance Defaulter Report ($< 65\%$ attendance list).
   - Class Performance & Department Result Summary (Pass/Fail count and Class Average SGPA).

---

## 📁 Repository Structure

```
├── DBConnection.java          # JDBC Connection Manager (MySQL primary + local fallback)
├── StudentDAO.java            # Student, Course, Enrollment, and Attendance DAO
├── AcademicDAO.java           # 3-Phase Marks Engine, Grading System & Reports
├── MainApp.java               # Interactive Menu-Driven CLI
├── AutoDemo.java              # Automated Demonstration Runner (Choices 1-14)
├── kec_schema.sql             # MySQL Database Schema DDL & Seed Data
├── run_app.bat                # 1-Click Windows Batch Launcher
├── auto_demo.bat              # 1-Click Automated Demo Launcher
└── lib/                       # JDBC Drivers (MySQL Connector & SQLite)
```

---

## 🛠️ How to Compile & Run

### 1-Click Windows Launcher:
Double-click `run_app.bat` or `auto_demo.bat`.

### Or via Command Prompt:
```cmd
javac -cp ".;mysql-connector-j-8.0.33.jar;sqlite-jdbc-3.45.1.0.jar;slf4j-api-1.7.36.jar;slf4j-simple-1.7.36.jar" DBConnection.java StudentDAO.java AcademicDAO.java MainApp.java

java -cp ".;mysql-connector-j-8.0.33.jar;sqlite-jdbc-3.45.1.0.jar;slf4j-api-1.7.36.jar;slf4j-simple-1.7.36.jar" MainApp
```

---

## 📜 Database Setup (MySQL)

```bash
mysql -u root -p < kec_schema.sql
```
Update credentials in `DBConnection.java` if needed.

---

## 👨‍💻 Author
- **Udishkrishna V**
- Kongu Engineering College
