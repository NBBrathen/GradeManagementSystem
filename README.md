# Grade Management System (GMS)

A Java-based console application for managing, analyzing, and reporting student grades. This system processes grade data from JSON files, performs calculations, updates grades, and can output various reports and evaluations based on command-line arguments.

## Description

The Grade Management System (GMS) is designed to:
* Load student grade data, course schema, and grading schemes from external JSON files (`grades.json`, `schema.json`, `scheme.json`).
* Process grade updates from an `updates.json` file.
* Calculate numeric and letter grades for students.
* Provide various functions accessible via command-line arguments, including:
    * Displaying all student grades.
    * Summarizing grades by department.
    * Searching for specific student grades based on various criteria.
    * Evaluating students who may be at risk of failing and generating notices, including predictions for missing scores (pMax, pMed, pLR).
* Output results to the console and generate a `notices.json` file for failing student evaluations.

The system utilizes a `config.properties` file to specify data paths.

## Features

* **Data Input:** Reads initial student data, schema, and grading scheme from `grades.json`, `schema.json`, and `scheme.json` respectively.
* **Grade Updates:** Applies grade modifications from `updates.json`.
* **Grade Calculation:**
    * Calculates overall numeric grades for students.
    * Determines letter grades based on a configurable scheme.
* **Grade Display (`grades` argument):** Prints a formatted table of all student grades, including individual coursework scores, overall numeric grade, and letter grade.
* **Grade Summary (`summary` argument):** Provides a departmental summary of average scores for each piece of coursework and overall grades.
* **Grade Search (`search` argument):**
    * Supports searching by various keys:
        * `snf <FullName>`: Search by student full name (uses linear and binary search for benchmarking).
        * `snl <LastName>`: Search by student last name.
        * `sng <FirstName>`: Search by student given name.
        * `sid <ID>`: Search by student ID.
        * `cq[1-5] <ScoreOrRange>`: Search by Quiz score (e.g., `cq1 8`, `cq2 7-9`).
        * `ca[1-5] <ScoreOrRange>`: Search by Assignment score (e.g., `ca1 25`, `ca2 20-25`).
        * `cmi <ScoreOrRange>`: Search by Midterm score.
        * `cfi <ScoreOrRange>`: Search by Final exam score.
        * `cgr <GradeOrRange>`: Search by overall numeric grade.
        * `clg <LetterGrade>`: Search by letter grade.
    * Supports range queries for scores/grades (e.g., `80-90`, `min-70`, `90-max`).
    * Displays benchmark results for linear vs. binary search for `snf` key.
* **Grade Evaluation (`evaluation` argument):**
    * Identifies students with grades below a certain threshold (e.g., 70).
    * Predicts potential final grades based on different scenarios:
        * `pMax`: Assumes student gets maximum possible scores for remaining/missing items.
        * `pMed`: Assumes student gets median scores for remaining/missing items.
        * `pLR`: Uses linear regression (and median for fallback) to predict missing Q5/A5 scores and median/midterm for other missing items.
    * Outputs a `notices.json` file with details for these students, including expected final exam scores needed to pass.
* **Data Output:** Updated grades (after processing `updates.json`) are written back to `grades.json`. Failing student notices are written to `notices.json`.

## Technologies Used

* **Language:** Java
* **Libraries:**
    * `javax.json` (Jakarta JSON Processing API) for handling JSON data.
    * `org.apache.commons.math3.stat.regression.SimpleRegression` for linear regression in grade prediction.
* **Data Format:** JSON for input and output files.
* **Configuration:** `config.properties` for file paths.

## Setup and Installation

1.  **Prerequisites:**
    * Java Development Kit (JDK) installed (e.g., JDK 8 or newer).
    * Git installed.
    * Ensure the `javax.json` library (e.g., `org.glassfish.javax.json.jar` or the appropriate Jakarta EE 8+ equivalent) and `org.apache.commons.math3` library are included in your project's classpath/dependencies. 

2.  **Clone the repository:**
    ```bash
    git clone https://github.com/NBBrathen/GradeManagementSystem.git
    cd GradeManagementSystem
    ```

3.  **Project Structure & Data Files:**
    * The application expects a `config.properties` file in the root directory of the project. This file should define:
        * `datapath`: Relative or absolute path to the directory containing `grades.json`, `schema.json`, `scheme.json`, and `updates.json`.
        * `outputpath`: Relative or absolute path to the directory where `notices.json` will be saved.
    * Place your JSON input files (`grades.json`, `schema.json`, `scheme.json`, `updates.json`) into the directory specified by `datapath`.
    * The `outputpath` directory will be used to store `notices.json`.

4.  **Run the application:**
    * **From IntelliJ IDEA:**
        * Create a new Run/Debug Configuration.
        * Set the main class to `ManagementSystem.GradeManagementSystem`.
        * In the "Program arguments" field, enter the desired command (e.g., `grades`, `summary`, `search snf "Student Name"`).
        * Ensure the working directory is set to the project root so `config.properties` can be found.
    * **From Command Line (after compiling or building JAR):**
        * If you built a JAR (e.g., `GMS.jar`) located in `out/artifacts/GMS_jar/`:
          ```bash
          java -jar out/artifacts/GMS_jar/GMS.jar <command> [options]
          ```
        * If running from compiled classes (e.g., in `out/production/GMS`) and libraries are in a `lib` folder:
          ```bash
          java -cp "out/production/GMS:lib/*" ManagementSystem.GradeManagementSystem <command> [options]
          ```
          *(Adjust classpath `-cp` based on your actual library locations and output directory for compiled classes.)*

## Usage

Run the application from the command line (or configure arguments in IntelliJ's Run Configuration) by providing one of the following main arguments. The application first loads all data, applies updates from `updates.json`, and writes back to `grades.json` before executing the specified command.

```bash
# Using JAR example:
java -jar GMS.jar <command> [options]

# Or using compiled classes:
java -cp <your_classpath> ManagementSystem.GradeManagementSystem <command> [options]
