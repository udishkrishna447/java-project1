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
├── JAVA ASSIGNMENT- UDISHKRISHNA V (25ITR165).pdf  # Full Project Report & Documentation
├── DBConnection.java                               # JDBC Connection Manager
├── StudentDAO.java                                 # Student, Course, Enrollment, and Attendance DAO
├── AcademicDAO.java                                # 3-Phase Marks Engine, Grading System & Reports
├── MainApp.java                                    # Interactive Menu-Driven CLI
├── kec_schema.sql                                  # MySQL Database Schema DDL & Seed Data
├── .gitignore                                      # Excludes class binaries & library jars
└── README.md                                       # Project Documentation
```

---

## 🛠️ Prerequisites & Setup

### 1. Download MySQL Connector/J JAR
Download **`mysql-connector-j-8.x.x.jar`** from [MySQL Official Downloads](https://dev.mysql.com/downloads/connector/j/) or Maven Central, and place it in the project root directory.

### 2. Database Setup (MySQL)
Run the schema script to create the database and tables:
```bash
mysql -u root -p < kec_schema.sql
```
*(Verify your database credentials in `DBConnection.java`).*

### 3. Compile & Run via Command Prompt

```cmd
javac -cp ".;mysql-connector-j-8.0.33.jar" DBConnection.java StudentDAO.java AcademicDAO.java MainApp.java

java -cp ".;mysql-connector-j-8.0.33.jar" MainApp
```

---

## 👨‍💻 Author
- **Udishkrishna V**
- Roll No: **25ITR165**
- Department of Information Technology
- **Kongu Engineering College (Autonomous)**
