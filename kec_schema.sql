-- ============================================================
--  Kongu Engineering College (Autonomous) - Regulation 2024
--  Student Academic & Continuous Evaluation System
--  Database Schema Script
--  File: kec_schema.sql
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS kec_academic;
USE kec_academic;

-- ------------------------------------------------------------
-- Drop tables in reverse FK order (safe re-run)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS assessment_marks;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS students;

-- ============================================================
-- TABLE 1: students
-- ============================================================
CREATE TABLE students (
    roll_number   VARCHAR(15)  NOT NULL,
    student_name  VARCHAR(100) NOT NULL,
    department    VARCHAR(50)  NOT NULL,
    semester      INT          NOT NULL CHECK (semester BETWEEN 1 AND 8),
    PRIMARY KEY (roll_number)
);

-- ============================================================
-- TABLE 2: courses
-- ============================================================
CREATE TABLE courses (
    course_code   VARCHAR(15)  NOT NULL,
    course_title  VARCHAR(150) NOT NULL,
    credits       INT          NOT NULL CHECK (credits BETWEEN 1 AND 6),
    course_type   ENUM('THEORY','PRACTICAL','EMBEDDED') NOT NULL,
    PRIMARY KEY (course_code)
);

-- ============================================================
-- TABLE 3: enrollments
-- ============================================================
-- Tracks which student is enrolled in which course for which
-- semester, along with running attendance figures.
-- ============================================================
CREATE TABLE enrollments (
    enrollment_id     INT          NOT NULL AUTO_INCREMENT,
    roll_number       VARCHAR(15)  NOT NULL,
    course_code       VARCHAR(15)  NOT NULL,
    semester          INT          NOT NULL CHECK (semester BETWEEN 1 AND 8),
    total_classes     INT          NOT NULL DEFAULT 0 CHECK (total_classes >= 0),
    attended_classes  INT          NOT NULL DEFAULT 0 CHECK (attended_classes >= 0),
    PRIMARY KEY (enrollment_id),
    UNIQUE KEY uq_student_course_sem (roll_number, course_code, semester),
    FOREIGN KEY (roll_number)  REFERENCES students(roll_number) ON DELETE CASCADE,
    FOREIGN KEY (course_code)  REFERENCES courses(course_code)  ON DELETE CASCADE
);

-- ============================================================
-- TABLE 4: assessment_marks
-- ============================================================
-- One row per enrollment; updated incrementally across phases.
--   Phase 1 (Mid-Semester)  : cia1, cia2
--   Phase 2 (End of Sem CIA): assignment_quiz_lab
--   Phase 3 (Post-Semester) : ese_marks, ese_absent
-- ============================================================
CREATE TABLE assessment_marks (
    marks_id              INT     NOT NULL AUTO_INCREMENT,
    enrollment_id         INT     NOT NULL,

    -- Phase 1
    cia1                  DECIMAL(5,2)  DEFAULT NULL CHECK (cia1  BETWEEN 0 AND 100),
    cia2                  DECIMAL(5,2)  DEFAULT NULL CHECK (cia2  BETWEEN 0 AND 100),

    -- Phase 2
    assignment_quiz_lab   DECIMAL(5,2)  DEFAULT NULL CHECK (assignment_quiz_lab BETWEEN 0 AND 100),

    -- Phase 3
    ese_marks             DECIMAL(5,2)  DEFAULT NULL CHECK (ese_marks BETWEEN 0 AND 100),
    ese_absent            TINYINT(1)    NOT NULL DEFAULT 0,   -- 1 = AB (absent in ESE)

    PRIMARY KEY (marks_id),
    UNIQUE KEY uq_enrollment_marks (enrollment_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id) ON DELETE CASCADE
);

-- ============================================================
-- Sample Seed Data (optional – comment out if not needed)
-- ============================================================

-- Students
INSERT INTO students VALUES
  ('24ITR001','Arun Kumar R',       'IT',   3),
  ('24ITR002','Priya Dharshini S',  'IT',   3),
  ('24CSE001','Karthick M',         'CSE',  3),
  ('24CSE002','Nithya Lakshmi B',   'CSE',  3),
  ('24ECE001','Suriya P',           'ECE',  3);

-- Courses
INSERT INTO courses VALUES
  ('24ITR31', 'Database Management Systems',   4, 'THEORY'),
  ('24ITR32', 'Operating Systems',             3, 'THEORY'),
  ('24ITR33', 'DBMS Laboratory',               2, 'PRACTICAL'),
  ('24CSE31', 'Data Structures and Algorithms',4, 'THEORY'),
  ('24CSE32', 'DS Lab',                        2, 'PRACTICAL'),
  ('24ECE31', 'Digital Electronics',           4, 'EMBEDDED');

-- Enrollments (semester 3)
INSERT INTO enrollments (roll_number, course_code, semester, total_classes, attended_classes) VALUES
  ('24ITR001','24ITR31',3,90,78),
  ('24ITR001','24ITR32',3,70,52),
  ('24ITR001','24ITR33',3,60,56),
  ('24ITR002','24ITR31',3,90,80),
  ('24ITR002','24ITR32',3,70,60),
  ('24CSE001','24CSE31',3,80,75),
  ('24CSE001','24CSE32',3,60,58),
  ('24CSE002','24CSE31',3,80,50),
  ('24ECE001','24ECE31',3,85,82);

-- Assessment marks (incremental – some fully entered, some partial)
INSERT INTO assessment_marks
    (enrollment_id, cia1, cia2, assignment_quiz_lab, ese_marks, ese_absent)
VALUES
  (1,  78, 82, 88,  72, 0),
  (2,  55, 48, 60,  40, 0),
  (3,  80, 85, 90,  88, 0),
  (4,  90, 88, 92,  85, 0),
  (5,  70, 65, 75,  NULL,0),  -- ESE not yet entered
  (6,  85, 80, 88,  78, 0),
  (7,  88, 90, 92,  85, 0),
  (8,  45, 50, 55,  NULL,0),  -- SA candidate (low attendance)
  (9,  88, 90, 92,  80, 0);
