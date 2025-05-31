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
