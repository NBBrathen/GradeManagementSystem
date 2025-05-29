package ManagementSystem;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import java.io.*;
import java.util.*;
import Utility.Benchmark;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class GradeManagementSystem {

    public static double[] predictMissingScores(StudentGrade student) {
        SimpleRegression quizRegression = new SimpleRegression();

        ArrayList<Quiz> quizzes = student.getQuizzes();
        for (int i = 0; i < 4; i++){
            if (quizzes.get(i).getScore() > 0){
                quizRegression.addData(i+1, quizzes.get(i).getScore());
            }
        }

        double predictedQ5 = quizRegression.predict(5);

        if (Double.isNaN(predictedQ5)) {
            int sum = 0, count = 0;
            for (int i = 0; i < 4; i++){
                if (quizzes.get(i).getScore() > 0){
                    sum += quizzes.get(i).getScore();
                    count++;
                }
            }
            predictedQ5 = (count > 0) ? sum / count : 0;
        }

        SimpleRegression assignmentRegression = new SimpleRegression();

        ArrayList<Assignment> assignments = student.getAssignments();
        for (int i = 0; i < 4; i++){
            if (assignments.get(i).getScore() > 0){
                assignmentRegression.addData(i+1, assignments.get(i).getScore());
            }
        }

        double predictedA5 = assignmentRegression.predict(5);

        if (Double.isNaN(predictedA5)){
            int sum = 0, count = 0;
            for (int i = 0; i < 4; i++){
                if (assignments.get(i).getScore() > 0){
                    sum += assignments.get(i).getScore();
                    count++;
                }
            }
            predictedA5 = (count > 0) ? sum / count : 0;
        }

        return new double[]{predictedQ5, predictedA5};
    }

    private static int parseGradeValue(String value) {
        if (value != null && !value.trim().isEmpty()) {
            return Integer.parseInt(value.trim());
        } else {
            return 0;
        }
    }

    private static void writeUpdatedGradesToFile(ArrayList<StudentGrade> grades, JsonObject schemaObj, Properties prop) {
        try {
            JsonObjectBuilder rootBuilder = Json.createObjectBuilder();
            JsonArrayBuilder studentsArrayBuilder = Json.createArrayBuilder();

            for (StudentGrade student : grades) {
                JsonObjectBuilder studentBuilder = Json.createObjectBuilder();
                studentBuilder.add("Name", student.getName());
                studentBuilder.add("ID", student.getId());
                studentBuilder.add("Department", student.getDepartment());

                JsonObjectBuilder courseworkBuilder = Json.createObjectBuilder();

                for (int i = 0; i < student.getQuizzes().size(); i++){
                    Quiz quiz = student.getQuizzes().get(i);
                    String score = (quiz.getScore() > 0) ? String.valueOf(quiz.getScore()) : "";
                    courseworkBuilder.add("Q" + (i+1), score);
                }

                for (int i = 0; i < student.getAssignments().size(); i++) {
                    Assignment assignment = student.getAssignments().get(i);
                    String score = (assignment.getScore() > 0) ? String.valueOf(assignment.getScore()) : "";
                    courseworkBuilder.add("A" + (i+1), score);
                }

                String midtermScore = (student.getMidterm().getScore() > 0) ?
                        String.valueOf(student.getMidterm().getScore()) : "";
                courseworkBuilder.add("Midterm", midtermScore);

                String finalScore = (student.getFinalexam().getScore() > 0) ?
                        String.valueOf(student.getFinalexam().getScore()) : "";
                courseworkBuilder.add("Final", finalScore);

                studentBuilder.add("Coursework", courseworkBuilder);
                studentsArrayBuilder.add(studentBuilder);
        }
            rootBuilder.add("Students", studentsArrayBuilder);
            JsonObject rootObject = rootBuilder.build();

            Map<String, Object> config = new HashMap<>();
            config.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory writerFactory = Json.createWriterFactory(config);

            String outputPath = prop.getProperty("datapath") + File.separator + "grades.json";
            try (FileWriter fileWriter = new FileWriter(outputPath);
                 JsonWriter jsonWriter = writerFactory.createWriter(fileWriter)) {
                     jsonWriter.writeObject(rootObject);
                System.out.println("Grades file updated successfully.");
            }
        } catch (IOException e) {
            System.err.println("Error writing to grades.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("GMS - Grade Management System On.");

        Properties prop = new Properties();
        FileInputStream fisConfig = new FileInputStream("config.properties");
        prop.load(fisConfig);

        String dataPath = prop.getProperty("datapath") + File.separator + "grades.json";
        String schemaPath = prop.getProperty("datapath") + File.separator + "schema.json";
        String schemePath = prop.getProperty("datapath") + File.separator + "scheme.json";
        JsonReader jReader = Json.createReader(new FileInputStream(dataPath));
        JsonReader jReaderSchema = Json.createReader(new FileInputStream(schemaPath));
        JsonReader jReaderScheme = Json.createReader(new FileInputStream(schemePath));

        System.out.println("Loading grades is complete.");

        // For grades.json
        JsonObject jObj = jReader.readObject();
        JsonArray jArr = jObj.getJsonArray("Students");
        int numStudents = jArr.size();

        // For schema.json
        JsonObject jObjSchema = jReaderSchema.readObject();
        JsonObject quizSchema = jObjSchema.getJsonObject("Quiz");
        JsonObject assignmentSchema = jObjSchema.getJsonObject("Assignment");
        JsonObject midtermSchema = jObjSchema.getJsonObject("Midterm");
        JsonObject finalSchema = jObjSchema.getJsonObject("Final");


        // For scheme.json
        JsonObject jObjScheme = jReaderScheme.readObject();

        ArrayList<StudentGrade> grades = new ArrayList<>();


        for (int i = 0; i < jArr.size(); i++) {
            JsonObject studentObj = jArr.getJsonObject(i);

            String name = studentObj.getString("Name");
            String id = studentObj.getString("ID");
            String department = studentObj.getString("Department");

            JsonObject courseworkObj = studentObj.getJsonObject("Coursework");

            int quizLength = quizSchema.getInt("Count");
            ArrayList<Quiz> quizzes = new ArrayList<>();
            for (int q = 0; q < quizLength; q++) {
                int quizScore = parseGradeValue(courseworkObj.getString("Q" + (q + 1)));
                quizzes.add(new Quiz(quizScore));
                quizzes.get(q).setMaxScore(quizSchema.getInt("MaxScore"));
                quizzes.get(q).setWeight(quizSchema.getInt("Weight"));
            }

            int assignmentLength = assignmentSchema.getInt("Count");
            ArrayList<Assignment> assignments = new ArrayList<>();
            for (int a = 0; a < assignmentLength; a++) {
                int assignmentScore = parseGradeValue(courseworkObj.getString("A" + (a + 1)));
                assignments.add(new Assignment(assignmentScore));
                assignments.get(a).setMaxScore(assignmentSchema.getInt("MaxScore"));
                assignments.get(a).setWeight(assignmentSchema.getInt("Weight"));
            }

            int midtermScore = parseGradeValue(courseworkObj.getString("Midterm"));
            Midterm midterm = new Midterm(midtermScore);
            midterm.setMaxScore(midtermSchema.getInt("MaxScore"));
            midterm.setWeight(midtermSchema.getInt("Weight"));

            int finalScore = parseGradeValue(courseworkObj.getString("Final"));
            Finalexam finalExam = new Finalexam(finalScore);
            finalExam.setMaxScore(finalSchema.getInt("MaxScore"));
            finalExam.setWeight(finalSchema.getInt("Weight"));

            grades.add(new StudentGrade(name, id, department, quizzes, assignments, midterm, finalExam));

            grades.get(i).calculateGrade();
            grades.get(i).calculateLetterGrade(jObjScheme);


        }

        String updatesPath = prop.getProperty("datapath") + File.separator + "updates.json";
        JsonReader updatesReader = Json.createReader(new FileInputStream(updatesPath));
        JsonObject updatesObj = updatesReader.readObject();
        JsonArray updatesArr = updatesObj.getJsonArray("Students");

        for (int i = 0; i < updatesArr.size(); i++){
            JsonObject updateStudentObj = updatesArr.getJsonObject(i);
            String updateName = updateStudentObj.getString("Name");
            String updateID = updateStudentObj.getString("ID");
            JsonObject updateCourseworkObj = updateStudentObj.getJsonObject("Coursework");

            for (StudentGrade student : grades){
                if (student.getId().equals(updateID) && student.getName().equals(updateName)) {
                    if (updateCourseworkObj.containsKey("Q4")){
                        int q4score = parseGradeValue(updateCourseworkObj.getString("Q4"));
                        student.getQuizzes().get(3).setScore(q4score);
                    }

                    if (updateCourseworkObj.containsKey("A3")) {
                        int a3Score = parseGradeValue(updateCourseworkObj.getString("A3"));
                        student.getAssignments().get(2).setScore(a3Score);
                    }

                    if (updateCourseworkObj.containsKey("A4")){
                        int a4Score = parseGradeValue(updateCourseworkObj.getString("A4"));
                        student.getAssignments().get(3).setScore(a4Score);
                    }

                    student.calculateGrade();
                    student.calculateLetterGrade(jObjScheme);
                    break;
                }
            }
        }

        writeUpdatedGradesToFile(grades, jObjSchema, prop);

        if (args.length == 0) {
            System.out.println("Arguments are needed to run this program");
            return;
        } else if (args[0].equals("grades")) {

            printGrades(grades);

        } else if (args[0].equals("summary")) {

            summarizeGrades(grades, jObjScheme);

        } else if (args[0].equals("search")) {

            // Example to read the key and the data from arguments
            String key = args[1], data = args[2];
            searchGrades(grades, key, data);
        } else if (args[0].equals("evaluation")) {

            evaluateGrades(grades, prop, jObjSchema);

        } else {
            System.out.println("Enter the proper arguments");
        }
    }

    private static void evaluateGrades(ArrayList<StudentGrade> grades, Properties prop, JsonObject schemaObj) {
        MyPriorityQueue failingGradesQueue = new MyPriorityQueue();

        System.out.printf("%-20s %-10s %-35s %-8s %-8s %-8s %-8s\n",
                "Name", "ID", "Department", "Grade", "p-Max", "p-Med", "p-LR");

        for (StudentGrade grade : grades) {
            if (grade.getGrade() < 70) {
                failingGradesQueue.enqueue(grade);
            }
        }

        int failingCount = 0;

        while (!failingGradesQueue.isEmpty()) {
            failingCount++;
            StudentGrade student = (StudentGrade) failingGradesQueue.dequeue();

            double pMax = pMax(student);
            double pMed = pMed(student);
            double pLR = pLR(student);

            System.out.printf("%-20s %-10s %-35s %-8.2f %-8.2f %-8.2f %-8.2f\n",
                    student.getName(),
                    student.getId(),
                    student.getDepartment(),
                    student.getGrade(),
                    pMax,
                    pMed,
                    pLR);
        }

        System.out.printf("%d out of %d students may not pass: %d%%\n",
                failingCount,
                grades.size(),
                (failingCount * 100) / grades.size());

        for (StudentGrade grade : grades) {
            if (grade.getGrade() < 70) {
                failingGradesQueue.enqueue(grade);
            }
        }

        writeNoticesToFilePriority(failingGradesQueue, prop, schemaObj);

    }

    private static double pMed(StudentGrade student) {

        ArrayList<Quiz> clonedQuizzes = new ArrayList<>();
        for (Quiz q : student.getQuizzes()) {
            Quiz newQuiz = new Quiz(q.getScore());
            newQuiz.setMaxScore(q.getMaxScore());
            newQuiz.setWeight(q.getWeight());
            clonedQuizzes.add(newQuiz);
        }

        ArrayList<Assignment> clonedAssignments = new ArrayList<>();
        for (Assignment a : student.getAssignments()) {
            Assignment newAssignment = new Assignment(a.getScore());
            newAssignment.setMaxScore(a.getMaxScore());
            newAssignment.setWeight(a.getWeight());
            clonedAssignments.add(newAssignment);
        }

        Midterm clonedMidterm = new Midterm(student.getMidterm().getScore());
        clonedMidterm.setMaxScore(student.getMidterm().getMaxScore());
        clonedMidterm.setWeight(student.getMidterm().getWeight());

        Finalexam clonedFinal = new Finalexam(student.getFinalexam().getScore());
        clonedFinal.setMaxScore(student.getFinalexam().getMaxScore());
        clonedFinal.setWeight(student.getFinalexam().getWeight());

        StudentGrade clone = new StudentGrade(
                student.getName(),
                student.getId(),
                student.getDepartment(),
                clonedQuizzes,
                clonedAssignments,
                clonedMidterm,
                clonedFinal
        );

        ArrayList<Integer> completedQuizScores = new ArrayList<>();
        ArrayList<Integer> completedAssignmentScores = new ArrayList<>();

        //QUIZ MEDIAN
        for (int q = 0; q < 5; q++) {
            if (clone.getQuizzes().get(q).getScore() != 0) {
                completedQuizScores.add(clone.getQuizzes().get(q).getScore());
            }
        }
        Collections.sort(completedQuizScores);
        int medianQuizScore;
        if (completedQuizScores.size() == 0) {
            medianQuizScore = 0;
        } else if (completedQuizScores.size() % 2 == 1) {
            medianQuizScore = completedQuizScores.get(completedQuizScores.size() / 2);
        } else {
            int middle1 = completedQuizScores.get(completedQuizScores.size() / 2 - 1);
            int middle2 = completedQuizScores.get(completedQuizScores.size() / 2);
            medianQuizScore = (middle1 + middle2) / 2;
        }


        for (int q = 0; q < 5; q++) {
            if (clone.getQuizzes().get(q).getScore() == 0) {
                clone.getQuizzes().get(q).setScore(medianQuizScore);
            }
        }

        //ASSIGNMENT MEDIAN
        for (int a = 0; a < 5; a++) {
            if (clone.getAssignments().get(a).getScore() != 0) {
                completedAssignmentScores.add(clone.getAssignments().get(a).getScore());
            }
        }
        Collections.sort(completedAssignmentScores);
        int medianAssignmentScore;
        if (completedAssignmentScores.size() == 0) {
            medianAssignmentScore = 0;
        } else if (completedAssignmentScores.size() % 2 == 1) {
            medianAssignmentScore = completedAssignmentScores.get(completedAssignmentScores.size() / 2);
        } else {
            int middle1 = completedAssignmentScores.get(completedAssignmentScores.size() / 2 - 1);
            int middle2 = completedAssignmentScores.get(completedAssignmentScores.size() / 2);
            medianAssignmentScore = (middle1 + middle2) / 2;
        }

        for (int a = 0; a < 5; a++) {
            if (clone.getAssignments().get(a).getScore() == 0) {
                clone.getAssignments().get(a).setScore(medianAssignmentScore);
            }
        }

        if (clone.getFinalexam().getScore() == 0) {
            clone.getFinalexam().setScore(clone.getMidterm().getScore());
        }

        clone.calculateGrade();
        return clone.getGrade();

    }

    private static double pMax(StudentGrade student) {

        ArrayList<Quiz> clonedQuizzes = new ArrayList<>();
        for (Quiz q : student.getQuizzes()) {
            Quiz newQuiz = new Quiz(q.getScore());
            newQuiz.setMaxScore(q.getMaxScore());
            newQuiz.setWeight(q.getWeight());
            clonedQuizzes.add(newQuiz);
        }

        ArrayList<Assignment> clonedAssignments = new ArrayList<>();
        for (Assignment a : student.getAssignments()) {
            Assignment newAssignment = new Assignment(a.getScore());
            newAssignment.setMaxScore(a.getMaxScore());
            newAssignment.setWeight(a.getWeight());
            clonedAssignments.add(newAssignment);
        }

        Midterm clonedMidterm = new Midterm(student.getMidterm().getScore());
        clonedMidterm.setMaxScore(student.getMidterm().getMaxScore());
        clonedMidterm.setWeight(student.getMidterm().getWeight());

        Finalexam clonedFinal = new Finalexam(student.getFinalexam().getScore());
        clonedFinal.setMaxScore(student.getFinalexam().getMaxScore());
        clonedFinal.setWeight(student.getFinalexam().getWeight());

        StudentGrade clone = new StudentGrade(
                student.getName(),
                student.getId(),
                student.getDepartment(),
                clonedQuizzes,
                clonedAssignments,
                clonedMidterm,
                clonedFinal
        );

        for (int q = 0; q < 5; q++) {
            if (clone.getQuizzes().get(q).getScore() == 0) {
                clone.getQuizzes().get(q).setScore(clone.getQuizzes().get(q).getMaxScore());
            }
        }

        for (int a = 0; a < 5; a++) {
            if (clone.getAssignments().get(a).getScore() == 0) {
                clone.getAssignments().get(a).setScore(clone.getAssignments().get(a).getMaxScore());
            }
        }

        if (clone.getMidterm().getScore() == 0) {
            clone.getMidterm().setScore(clone.getMidterm().getMaxScore());
        }

        if (clone.getFinalexam().getScore() == 0) {
            clone.getFinalexam().setScore(clone.getFinalexam().getMaxScore());
        }

        clone.calculateGrade();
        return clone.getGrade();

    }

    private static double pLR(StudentGrade student) {
        ArrayList<Quiz> clonedQuizzes = new ArrayList<>();
        for (Quiz q : student.getQuizzes()) {
            Quiz newQuiz = new Quiz(q.getScore());
            newQuiz.setMaxScore(q.getMaxScore());
            newQuiz.setWeight(q.getWeight());
            clonedQuizzes.add(newQuiz);
        }

        ArrayList<Assignment> clonedAssignments = new ArrayList<>();
        for (Assignment a : student.getAssignments()) {
            Assignment newAssignment = new Assignment(a.getScore());
            newAssignment.setMaxScore(a.getMaxScore());
            newAssignment.setWeight(a.getWeight());
            clonedAssignments.add(newAssignment);
        }

        Midterm clonedMidterm = new Midterm(student.getMidterm().getScore());
        clonedMidterm.setMaxScore(student.getMidterm().getMaxScore());
        clonedMidterm.setWeight(student.getMidterm().getWeight());

        Finalexam clonedFinal = new Finalexam(student.getFinalexam().getScore());
        clonedFinal.setMaxScore(student.getFinalexam().getMaxScore());
        clonedFinal.setWeight(student.getFinalexam().getWeight());

        StudentGrade clone = new StudentGrade(
                student.getName(),
                student.getId(),
                student.getDepartment(),
                clonedQuizzes,
                clonedAssignments,
                clonedMidterm,
                clonedFinal
        );

        double[] predictions = predictMissingScores(student);
        double predictedQ5 = predictions[0];
        double predictedA5 = predictions[1];

        for (int q = 0; q < 5; q++){
            if (clone.getQuizzes().get(q).getScore() == 0){
                if (q == 4){
                    clone.getQuizzes().get(q).setScore((int)Math.round(predictedQ5));
                } else{
                    ArrayList<Integer> completedQuizScores = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        if (student.getQuizzes().get(i).getScore() != 0){
                            completedQuizScores.add(student.getQuizzes().get(i).getScore());
                        }
                    }
                    int medianQuizScore = calculateMedian(completedQuizScores);
                    clone.getQuizzes().get(q).setScore(medianQuizScore);
                }
            }
        }

        for (int a = 0; a < 5; a++){
            if (clone.getAssignments().get(a).getScore() == 0){
                if (a == 4) {
                    clone.getAssignments().get(a).setScore((int)Math.round(predictedA5));
                } else {
                    ArrayList<Integer> completedAssignmentScores = new ArrayList<>();
                    for (int i = 0; i < 5; i++){
                        if (student.getAssignments().get(i).getScore() != 0) {
                            completedAssignmentScores.add(student.getAssignments().get(i).getScore());
                        }
                    }
                    int medianAssignmentScore = calculateMedian(completedAssignmentScores);
                    clone.getAssignments().get(a).setScore(medianAssignmentScore);
                }
            }
        }

        if (clone.getFinalexam().getScore() == 0) {
            clone.getFinalexam().setScore(clone.getMidterm().getScore());
        }

        clone.calculateGrade();
        return clone.getGrade();
    }

    private static int calculateMedian(ArrayList<Integer> scores){
        if (scores.size() == 0) {
            return 0;
        }

        Collections.sort(scores);

        if (scores.size() % 2 == 1) {
            return scores.get(scores.size() / 2);
        } else {
            int middle1 = scores.get(scores.size() / 2 - 1);
            int middle2 = scores.get(scores.size() / 2);
            return (middle1 + middle2) / 2;
        }
    }


    private static void writeNoticesToFilePriority(MyPriorityQueue failingGradeQueue, Properties prop, JsonObject schemaObj) {
        String outputPath = prop.getProperty("outputpath") + File.separator + "notices.json";

        File outputDir = new File(prop.getProperty("outputpath"));
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            JsonObjectBuilder rootBuilder = Json.createObjectBuilder();
            JsonArrayBuilder studentsArrayBuilder = Json.createArrayBuilder();

            while (!failingGradeQueue.isEmpty()) {
                StudentGrade student = (StudentGrade) failingGradeQueue.dequeue();

                double expectedFinalExam = calculateExpectedFinalExam(student, schemaObj);
                double pLRValue = pLR(student);

                JsonObjectBuilder studentBuilder = Json.createObjectBuilder()
                        .add("Name", student.getName())
                        .add("ID", student.getId())
                        .add("Grade", Math.round(student.getGrade() * 100) / 100.0)
                        .add("expected_grade_max", Math.round(pMax(student) * 100) / 100.0)
                        .add("expected_grade_median", Math.round(pMed(student) * 100) / 100.0)
                        .add("expected_grade_lr", Math.round(pLRValue * 100) / 100.0)
                        .add("expected_final_exam", Math.round(expectedFinalExam));
                studentsArrayBuilder.add(studentBuilder);
            }

            rootBuilder.add("students", studentsArrayBuilder);
            JsonObject rootObject = rootBuilder.build();

            Map<String, Object> config = new HashMap<>();
            config.put(JsonGenerator.PRETTY_PRINTING, true);
            JsonWriterFactory writerFactory = Json.createWriterFactory(config);

            try (FileWriter fileWriter = new FileWriter(outputPath);
                 JsonWriter jsonWriter = writerFactory.createWriter(fileWriter)) {
                jsonWriter.writeObject(rootObject);
            }
        } catch (IOException e) {
            System.err.println("Error writing to notices.json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void searchGrades(ArrayList<StudentGrade> grades, String key, String data) {

        if (key.equalsIgnoreCase("snf")) {
            System.out.println("[Search] Name Full: " + data);
            if (!data.equalsIgnoreCase("all")) {

                int indexLS = linearSearch(grades, data);
                int indexBS = binarySearch(grades, key, data);
                if (indexBS != -1) {
                    System.out.printf("%-20.20s %-10s %-35.35s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                            "Name", "ID", "Department", "Q1", "Q2", "Q3", "Q4", "Q5", "A1", "A2", "A3", "A4", "A5", "Mid", "Final",
                            "Grade", "Letter");
                    grades.get(indexBS).printGrade();
                } else {
                    System.out.println(data + " Not Found");
                }
                System.out.println("[Benchmark Results]");
                System.out.println("By Linear Search: " + Benchmark.getCounterLS() + " comparison(s)");
                System.out.println("By Binary Search: " + Benchmark.getCounterBS() + " comparison(s)");
            } else if (data.equalsIgnoreCase("all")) {
                System.out.printf("%-20.20s %-20s %-20s", "Name", "Linear Search", "Binary Search");
                System.out.println();

                int bsWin = 0;
                int lsWin = 0;
                double linearSearchTotal = 0;
                double binarySearchTotal = 0;

                for (StudentGrade grade : grades) {
                    Benchmark.resetCounterBS();
                    Benchmark.resetCounterLS();
                    int indexLS = linearSearch(grades, grade.name);
                    int indexBS = binarySearch(grades, key, grade.name);
                    linearSearchTotal += Benchmark.getCounterLS();
                    binarySearchTotal += Benchmark.getCounterBS();

                    if (Benchmark.getCounterLS() > Benchmark.getCounterBS()) {
                        bsWin++;
                    }
                    System.out.printf("%-20.20s %-20s %-20s", grade.name, Benchmark.getCounterLS(), Benchmark.getCounterBS());
                    System.out.println();
                }
                double linearSearchAverage = linearSearchTotal / grades.size();
                double binarySearchAverage = binarySearchTotal / grades.size();
                lsWin = grades.size() - bsWin;
                System.out.println("[Benchmark Result]");
                System.out.println("By Linear Search: " + String.format("%.2f", linearSearchAverage) + " comparison(s) | " + lsWin + " times");
                System.out.println("By Binary Search: " + String.format("%.2f", binarySearchAverage) + " comparison(s) | " + bsWin + " times");

                double bsWinRate = ((double) bsWin / grades.size()) * 100;
                System.out.println("Binary Search Winning Rate (vs Linear Search): " + String.format("%.2f", bsWinRate) + "%");


            }
        } else if (key.equalsIgnoreCase("snl")) {
            System.out.println("[Search] Name Last");
            findAndPrintMatches(grades, "nl", data);
        } else if (key.equalsIgnoreCase("sng")) {
            System.out.println("[Search] Name Given");
            findAndPrintMatches(grades, "ng", data);
        } else if (key.equalsIgnoreCase("sid")) {
            System.out.println("[Search] ID: " + data);
            findAndPrintMatches(grades, "id", data);
        } else if (key.toLowerCase().matches("cq[1-5]")) {
            String quizNum = key.substring(2, 3);
            System.out.println("[Search] Q" + quizNum + ": " + data);
            findAndPrintMatches(grades, "q" + quizNum, data);
        } else if (key.toLowerCase().matches("ca[1-5]")) {
            String assignmentNum = key.substring(2, 3);
            System.out.println("[Search] A" + assignmentNum + ": " + data);
            findAndPrintMatches(grades, "a" + assignmentNum, data);
        } else if (key.equalsIgnoreCase("cmi")) {
            System.out.println("[Search] Midterm: " + data);
            findAndPrintMatches(grades, "mi", data);
        } else if (key.equalsIgnoreCase("cfi")) {
            System.out.println("[Search] Finalexam: " + data);
            findAndPrintMatches(grades, "fi", data);
        } else if (key.equalsIgnoreCase("cgr")) {
            System.out.println("[Search] Grade: " + data);
            findAndPrintMatches(grades, "gr", data);
        } else if (key.equalsIgnoreCase("clg")) {
            System.out.println("[Search] Letter Grade: " + data);
            findAndPrintMatches(grades, "lg", data);
        } else {
            System.out.println(key + " is not a valid key. Please try again.");
        }


    }

    private static void mergeSort(ArrayList<StudentGrade> grades, String key, int left, int right) {
        if (left >= right - 1) {
            return;
        }

        int mid = (left + right) / 2;
        mergeSort(grades, key, left, mid);
        mergeSort(grades, key, mid, right);
        Merge(grades, key, left, mid, right);

    }

    private static void Merge(ArrayList<StudentGrade> grades, String key, int left, int mid, int right) {
        ArrayList<StudentGrade> temp = new ArrayList<>(right - left);

        int i = left;
        int j = mid;

        while (i < mid && j < right) {

            if (compareByKey(grades.get(i), grades.get(j), key) <= 0) {
                temp.add(grades.get(i));
                i++;
            } else {
                temp.add(grades.get(j));
                j++;
            }
        }

        while (i < mid) {
            temp.add(grades.get(i));
            i++;
        }

        while (j < right) {
            temp.add(grades.get(j));
            j++;
        }

        for (i = 0; i < temp.size(); i++) {
            grades.set(left + i, temp.get(i));
        }
    }

    private static int compareByKey(StudentGrade sg1, StudentGrade sg2, String key) {
        if (key.charAt(0) == 'q') {
            int quizNum = Integer.parseInt(key.substring(1, 2));

            switch (quizNum) {
                case 1:
                    return Integer.compare(sg1.getQuizzes().get(0).getScore(), sg2.getQuizzes().get(0).getScore());
                case 2:
                    return Integer.compare(sg1.getQuizzes().get(1).getScore(), sg2.getQuizzes().get(1).getScore());
                case 3:
                    return Integer.compare(sg1.getQuizzes().get(2).getScore(), sg2.getQuizzes().get(2).getScore());
                case 4:
                    return Integer.compare(sg1.getQuizzes().get(3).getScore(), sg2.getQuizzes().get(3).getScore());
                case 5:
                    return Integer.compare(sg1.getQuizzes().get(4).getScore(), sg2.getQuizzes().get(4).getScore());
                default:
                    return 0;
            }
        } else if (key.charAt(0) == 'a') {
            int assignmentNum = Integer.parseInt(key.substring(1, 2));
            switch (assignmentNum) {
                case 1:
                    return Integer.compare(sg1.getAssignments().get(0).getScore(), sg2.getAssignments().get(0).getScore());
                case 2:
                    return Integer.compare(sg1.getAssignments().get(1).getScore(), sg2.getAssignments().get(1).getScore());
                case 3:
                    return Integer.compare(sg1.getAssignments().get(2).getScore(), sg2.getAssignments().get(2).getScore());
                case 4:
                    return Integer.compare(sg1.getAssignments().get(3).getScore(), sg2.getAssignments().get(3).getScore());
                case 5:
                    return Integer.compare(sg1.getAssignments().get(4).getScore(), sg2.getAssignments().get(4).getScore());
                default:
                    return 0;
            }

        } else if (key.equalsIgnoreCase("mi")) {
            return Integer.compare(sg1.getMidterm().getScore(), sg2.getMidterm().getScore());
        } else if (key.equalsIgnoreCase("fi")) {
            return Integer.compare(sg1.getFinalexam().getScore(), sg2.getFinalexam().getScore());
        } else if (key.equalsIgnoreCase("gr")) {
            return Double.compare(sg1.getGrade(), sg2.getGrade());
        } else if (key.equalsIgnoreCase("lg")) {
            // This has to be negative since smaller letter is greater
            return -sg1.getLetterGrade().compareTo(sg2.getLetterGrade());
        } else if (key.equalsIgnoreCase("nl")) {
            String lastName1 = sg1.getName().split(" ")[1];
            String lastName2 = sg2.getName().split(" ")[1];
            return lastName1.compareTo(lastName2);
        } else if (key.equalsIgnoreCase("ng")) {
            String firstName1 = sg1.getName().split(" ")[0];
            String firstName2 = sg2.getName().split(" ")[0];
            return firstName1.compareTo(firstName2);
        }
        return 0;
    }

    private static int binarySearch(ArrayList<StudentGrade> grades, String key, String data) {
        int left = 0;
        int right = grades.size() - 1;
        if (key.equalsIgnoreCase("snf")) {
            String dataLastName = data.split(" ")[1].trim();
            String dataFirstName = data.split(" ")[0].trim();

            while (left <= right) {
                Benchmark.increaseCounterBS();
                int mid = left + (right - left) / 2;

                String studentFirstName = grades.get(mid).name.split(" ")[0].trim();
                String studentLastName = grades.get(mid).name.split(" ")[1].trim();

                int compareLastNames = dataLastName.compareTo(studentLastName);

                if (compareLastNames == 0) {
                    int compareFirstNames = dataFirstName.compareTo(studentFirstName);

                    if (compareFirstNames == 0) {
                        return mid;
                    } else if (compareFirstNames > 0) {
                        left = mid + 1;
                    } else {
                        right = mid - 1;
                    }
                } else if (compareLastNames > 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        } else {
            mergeSort(grades, key, 0, grades.size());

            while (left <= right) {
                int mid = left + (right - left) / 2;

                int comparison = compareToData(grades.get(mid), key, data);

                if (comparison == 0) {
                    return mid;
                } else if (comparison < 0) {
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
        }


        return -1;
    }

    private static int compareToData(StudentGrade grade, String key, String data) {

        if (key.charAt(0) == 'q') {
            int quizNum = Integer.parseInt(key.substring(1, 2));
            int quizValue = Integer.parseInt(data);
            return Integer.compare(grade.getQuizzes().get(quizNum - 1).getScore(), quizValue);
        } else if (key.charAt(0) == 'a') {
            int assignmentNum = Integer.parseInt(key.substring(1, 2));
            int assignmentValue = Integer.parseInt(data);
            return Integer.compare(grade.getAssignments().get(assignmentNum - 1).getScore(), assignmentValue);
        } else if (key.equalsIgnoreCase("mi")) {
            int midtermValue = Integer.parseInt(data);
            return Integer.compare(grade.getMidterm().getScore(), midtermValue);
        } else if (key.equalsIgnoreCase("fi")) {
            int finalValue = Integer.parseInt(data);
            return Integer.compare(grade.getFinalexam().getScore(), finalValue);
        } else if (key.equalsIgnoreCase("gr")) {
            float gradeValue = Float.parseFloat(data);
            return Float.compare(grade.getGrade(), gradeValue);
        } else if (key.equalsIgnoreCase("lg")) {
            LetterGrade valueGrade = null;
            for (LetterGrade lg : LetterGrade.values()) {
                if (lg.toString().equals(data)) {
                    valueGrade = lg;
                    break;
                }
            }
            if (valueGrade == null || grade.getLetterGrade() == null) {
                return 0;
            }
            return -grade.getLetterGrade().compareTo(valueGrade);
        } else if (key.equalsIgnoreCase("id")) {
            return grade.getId().compareTo(data);
        } else if (key.equalsIgnoreCase("nl")) {
            String lastName = grade.getName().split(" ")[1];
            return lastName.compareTo(data);
        } else if (key.equalsIgnoreCase("ng")) {
            String firstName = grade.getName().split(" ")[0];
            return firstName.compareTo(data);
        }
        return 0;
    }

    private static void findAndPrintMatches(ArrayList<StudentGrade> grades, String key, String data) {
        mergeSort(grades, key, 0, grades.size());

        if (isRangePattern(data)) {
            System.out.printf("%-20.20s %-10s %-35.35s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                    "Name", "ID", "Department", "Q1", "Q2", "Q3", "Q4", "Q5", "A1", "A2", "A3", "A4", "A5", "Mid", "Final",
                    "Grade", "Letter");

            boolean found = false;
            for (StudentGrade grade : grades) {
                if (compareToDataWithRange(grade, key, data) == 0) {
                    grade.printGrade();
                    found = true;
                }
            }
            if (!found) {
                System.out.println(data + " Not Found");
            }
            return;
        }

        int index = binarySearch(grades, key, data);

        if (index == -1) {
            System.out.println(data + " Not Found");
            return;
        }

        System.out.printf("%-20.20s %-10s %-35.35s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                "Name", "ID", "Department", "Q1", "Q2", "Q3", "Q4", "Q5", "A1", "A2", "A3", "A4", "A5", "Mid", "Final",
                "Grade", "Letter");

        int i = index;
        while (i >= 0 && compareToData(grades.get(i), key, data) == 0) {
            grades.get(i).printGrade();
            i--;
        }

        i = index + 1;
        while (i < grades.size() && compareToData(grades.get(i), key, data) == 0) {
            grades.get(i).printGrade();
            i++;
        }
    }

    private static int linearSearch(ArrayList<StudentGrade> grades, String data) {
        for (int i = 0; i < grades.size(); i++) {
            Benchmark.increaseCounterLS();
            String name = grades.get(i).name;
            if (name.compareTo(data) == 0) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isRangePattern(String data) {
        /* (?i) makes the regex case-insensitive
         * (min|\\d+) checks for "min" or a digit 0-9 one or more times
         * (max|\\d+) does the same just with "max."
         */

        return data.matches("(?i)(min|\\d+)-(max|\\d+)");
    }

    private static int compareToDataWithRange(StudentGrade grade, String key, String data) {

        if (isRangePattern(data)) {
            String[] parts = data.split("-");
            String lowerBound = parts[0].toLowerCase();
            String upperBound = parts[1].toLowerCase();
            int value;
            float floatValue;

            if (key.charAt(0) == 'q') {
                int quizNum = Integer.parseInt(key.substring(1, 2));
                value = grade.getQuizzes().get(quizNum - 1).getScore();

                if (lowerBound.equals("min")) {
                    int maxValue = upperBound.equals("max") ? grade.getQuizzes().get(0).getMaxScore() : Integer.parseInt(upperBound);
                    return (value <= maxValue) ? 0 : 1;
                } else if (upperBound.equals("max")) {
                    int minValue = Integer.parseInt(lowerBound);
                    return (value >= minValue) ? 0 : -1;
                } else {
                    int minValue = Integer.parseInt(lowerBound);
                    int maxValue = Integer.parseInt(upperBound);
                    if (value < minValue) return -1;
                    if (value > maxValue) return 1;
                    return 0;
                }
            } else if (key.charAt(0) == 'a') {
                int assignmentNum = Integer.parseInt(key.substring(1, 2));
                value = grade.getAssignments().get(assignmentNum - 1).getScore();

                if (lowerBound.equals("min")) {
                    int maxValue = upperBound.equals("max") ? grade.getAssignments().get(0).getMaxScore() : Integer.parseInt(upperBound);
                    return (value <= maxValue) ? 0 : 1;
                } else if (upperBound.equals("max")) {
                    int minValue = Integer.parseInt(lowerBound);
                    return (value >= minValue) ? 0 : -1;
                } else {
                    int minValue = Integer.parseInt(lowerBound);
                    int maxValue = Integer.parseInt(upperBound);
                    if (value < minValue) return -1;
                    if (value > maxValue) return 1;
                    return 0;
                }
            } else if (key.equalsIgnoreCase("mi")) {
                value = grade.getMidterm().getScore();
                if (lowerBound.equals("min")) {
                    int maxValue = upperBound.equals("max") ? grade.getMidterm().getMaxScore() : Integer.parseInt(upperBound);
                    return (value <= maxValue) ? 0 : 1;
                } else if (upperBound.equals("max")) {
                    int minValue = Integer.parseInt(lowerBound);
                    return (value >= minValue) ? 0 : -1;
                } else {
                    int minValue = Integer.parseInt(lowerBound);
                    int maxValue = Integer.parseInt(upperBound);
                    if (value < minValue) return -1;
                    if (value > maxValue) return 1;
                    return 0;
                }


            } else if (key.equalsIgnoreCase("fi")) {
                value = grade.getFinalexam().getScore();
                if (lowerBound.equals("min")) {
                    int maxValue = upperBound.equals("max") ? grade.getFinalexam().getMaxScore() : Integer.parseInt(upperBound);
                    return (value <= maxValue) ? 0 : 1;
                } else if (upperBound.equals("max")) {
                    int minValue = Integer.parseInt(lowerBound);
                    return (value >= minValue) ? 0 : -1;
                } else {
                    int minValue = Integer.parseInt(lowerBound);
                    int maxValue = Integer.parseInt(upperBound);
                    if (value < minValue) return -1;
                    if (value > maxValue) return 1;
                    return 0;
                }


            } else if (key.equalsIgnoreCase("gr")) {
                double gradeValue = grade.getGrade();
                if (lowerBound.equals("min")) {
                    double maxValue = upperBound.equals("max") ? 100.0 : Double.parseDouble(upperBound);
                    return (gradeValue <= maxValue) ? 0 : 1;
                } else if (upperBound.equals("max")) {
                    double minValue = Double.parseDouble(lowerBound);
                    return (gradeValue >= minValue) ? 0 : -1;
                } else {
                    double minValue = Double.parseDouble(lowerBound);
                    double maxValue = Double.parseDouble(upperBound);
                    if (gradeValue < minValue) return -1;
                    if (gradeValue > maxValue) return 1;
                    return 0;
                }
            }
        }
        return compareToData(grade, key, data);
    }

// print the grade table
    public static void printGrades(ArrayList<StudentGrade> grades) {
        System.out.printf("%-20.20s %-10s %-35.35s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                "Name", "ID", "Department", "Q1", "Q2", "Q3", "Q4", "Q5", "A1", "A2", "A3", "A4", "A5", "Mid", "Final",
                "Grade", "Letter");


        for (StudentGrade grade : grades) {

            grade.printGrade();
        }
    }


    public static void summarizeGrades(ArrayList<StudentGrade> grades, JsonObject schemeObj) {
        System.out.printf("%-35.35s %-6s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                "Department", "Number", "Q1", "Q2", "Q3", "Q4", "Q5", "A1", "A2", "A3", "A4", "A5", "Mid", "Final",
                "Grade", "Letter");

        // Making a list of all the departments

        ArrayList<String> departments = new ArrayList<>();
        ArrayList<Integer> departmentNum = new ArrayList<>();

        //Arraylists for the sums (I know, there's a lot of them)
        ArrayList<Double> q1Sum = new ArrayList<>();
        ArrayList<Double> q2Sum = new ArrayList<>();
        ArrayList<Double> q3Sum = new ArrayList<>();
        ArrayList<Double> q4Sum = new ArrayList<>();
        ArrayList<Double> q5Sum = new ArrayList<>();
        ArrayList<Double> a1Sum = new ArrayList<>();
        ArrayList<Double> a2Sum = new ArrayList<>();
        ArrayList<Double> a3Sum = new ArrayList<>();
        ArrayList<Double> a4Sum = new ArrayList<>();
        ArrayList<Double> a5Sum = new ArrayList<>();
        ArrayList<Double> midtermSum = new ArrayList<>();
        ArrayList<Double> finalSum = new ArrayList<>();
        ArrayList<Double> gradeSum = new ArrayList<>();


        for (StudentGrade grade : grades) {
            String department = grade.getDepartment();
            if (departments.contains(department)) {
                int index = departments.indexOf(department);
                departmentNum.set(index, departmentNum.get(index) + 1);

                ArrayList<Quiz> quizzes = grade.getQuizzes();
                if (quizzes.size() >= 1 && quizzes.get(0) != null) {
                    q1Sum.set(index, q1Sum.get(index) + quizzes.get(0).getScore());
                }
                if (quizzes.size() >= 2 && quizzes.get(1) != null) {
                    q2Sum.set(index, q2Sum.get(index) + quizzes.get(1).getScore());
                }
                if (quizzes.size() >= 3 && quizzes.get(2) != null) {
                    q3Sum.set(index, q3Sum.get(index) + quizzes.get(2).getScore());
                }
                if (quizzes.size() >= 4 && quizzes.get(3) != null) {
                    q4Sum.set(index, q4Sum.get(index) + quizzes.get(3).getScore());
                }
                if (quizzes.size() >= 5 && quizzes.get(4) != null) {
                    q5Sum.set(index, q5Sum.get(index) + quizzes.get(4).getScore());
                }

                ArrayList<Assignment> assignments = grade.getAssignments();
                if (assignments.size() >= 1 && assignments.get(0) != null) {
                    a1Sum.set(index, a1Sum.get(index) + assignments.get(0).getScore());
                }
                if (assignments.size() >= 2 && assignments.get(1) != null) {
                    a2Sum.set(index, a2Sum.get(index) + assignments.get(1).getScore());
                }
                if (assignments.size() >= 3 && assignments.get(2) != null) {
                    a3Sum.set(index, a3Sum.get(index) + assignments.get(2).getScore());
                }
                if (assignments.size() >= 4 && assignments.get(3) != null) {
                    a4Sum.set(index, a4Sum.get(index) + assignments.get(3).getScore());
                }
                if (assignments.size() >= 5 && assignments.get(4) != null) {
                    a5Sum.set(index, a5Sum.get(index) + assignments.get(4).getScore());
                }

                if (grade.getMidterm() != null) {
                    midtermSum.set(index, midtermSum.get(index) + grade.getMidterm().getScore());
                }
                if (grade.getFinalexam() != null) {
                    finalSum.set(index, finalSum.get(index) + grade.getFinalexam().getScore());
                }

                gradeSum.set(index, gradeSum.get(index) + grade.getGrade());

            } else {
                departments.add(department);
                departmentNum.add(1);

                ArrayList<Quiz> quizzes = grade.getQuizzes();
                q1Sum.add(quizzes.size() >= 1 && quizzes.get(0) != null ? (double) quizzes.get(0).getScore() : 0.0);
                q2Sum.add(quizzes.size() >= 2 && quizzes.get(1) != null ? (double) quizzes.get(1).getScore() : 0.0);
                q3Sum.add(quizzes.size() >= 3 && quizzes.get(2) != null ? (double) quizzes.get(2).getScore() : 0.0);
                q4Sum.add(quizzes.size() >= 4 && quizzes.get(3) != null ? (double) quizzes.get(3).getScore() : 0.0);
                q5Sum.add(quizzes.size() >= 5 && quizzes.get(4) != null ? (double) quizzes.get(4).getScore() : 0.0);

                ArrayList<Assignment> assignments = grade.getAssignments();
                a1Sum.add(assignments.size() >= 1 && assignments.get(0) != null ? (double) assignments.get(0).getScore() : 0.0);
                a2Sum.add(assignments.size() >= 2 && assignments.get(1) != null ? (double) assignments.get(1).getScore() : 0.0);
                a3Sum.add(assignments.size() >= 3 && assignments.get(2) != null ? (double) assignments.get(2).getScore() : 0.0);
                a4Sum.add(assignments.size() >= 4 && assignments.get(3) != null ? (double) assignments.get(3).getScore() : 0.0);
                a5Sum.add(assignments.size() >= 5 && assignments.get(4) != null ? (double) assignments.get(4).getScore() : 0.0);

                midtermSum.add(grade.getMidterm() != null ? (double) grade.getMidterm().getScore() : 0.0);
                finalSum.add(grade.getFinalexam() != null ? (double) grade.getFinalexam().getScore() : 0.0);
                gradeSum.add((double) grade.getGrade());
            }
        }

        for (int i = 0; i < departments.size(); i++) {
            int count = departmentNum.get(i);

            double q1Avg = q1Sum.get(i) / count;
            double q2Avg = q2Sum.get(i) / count;
            double q3Avg = q3Sum.get(i) / count;
            double q4Avg = q4Sum.get(i) / count;
            double q5Avg = q5Sum.get(i) / count;

            double a1Avg = a1Sum.get(i) / count;
            double a2Avg = a2Sum.get(i) / count;
            double a3Avg = a3Sum.get(i) / count;
            double a4Avg = a4Sum.get(i) / count;
            double a5Avg = a5Sum.get(i) / count;

            double midAvg = midtermSum.get(i) / count;
            double finalAvg = finalSum.get(i) / count;
            double gradeAvg = gradeSum.get(i) / count;
            String letterGrade = getLetterGrade(gradeAvg, schemeObj);
            String formattedGradeAllAvg = String.format("%.2f", gradeAvg);

            System.out.printf("%-35.35s %-6s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                    departments.get(i), count, formatValue(q1Avg), formatValue(q2Avg), formatValue(q3Avg),
                    formatValue(q4Avg), formatValue(q5Avg), formatValue(a1Avg), formatValue(a2Avg),
                    formatValue(a3Avg), formatValue(a4Avg), formatValue(a5Avg), formatValue(midAvg),
                    formatValue(finalAvg), formattedGradeAllAvg, letterGrade);
        }


        //Calculating all students number
        int totalStudents = 0;
        for (int i = 0; i < departments.size(); i++) {
            totalStudents += departmentNum.get(i);
        }
        double totalQ1Sum = 0, totalQ2Sum = 0, totalQ3Sum = 0, totalQ4Sum = 0, totalQ5Sum = 0;
        double totalA1Sum = 0, totalA2Sum = 0, totalA3Sum = 0, totalA4Sum = 0, totalA5Sum = 0;
        double totalMidSum = 0, totalFinalSum = 0, totalGradeSum = 0;


        for (int i = 0; i < departments.size(); i++) {

            totalQ1Sum += q1Sum.get(i);
            totalQ2Sum += q2Sum.get(i);
            totalQ3Sum += q3Sum.get(i);
            totalQ4Sum += q4Sum.get(i);
            totalQ5Sum += q5Sum.get(i);

            totalA1Sum += a1Sum.get(i);
            totalA2Sum += a2Sum.get(i);
            totalA3Sum += a3Sum.get(i);
            totalA4Sum += a4Sum.get(i);
            totalA5Sum += a5Sum.get(i);

            totalMidSum += midtermSum.get(i);
            totalFinalSum += finalSum.get(i);
            totalGradeSum += gradeSum.get(i);
        }

        double q1AllAvg = totalQ1Sum / totalStudents;
        double q2AllAvg = totalQ2Sum / totalStudents;
        double q3AllAvg = totalQ3Sum / totalStudents;
        double q4AllAvg = totalQ4Sum / totalStudents;
        double q5AllAvg = totalQ5Sum / totalStudents;

        double a1AllAvg = totalA1Sum / totalStudents;
        double a2AllAvg = totalA2Sum / totalStudents;
        double a3AllAvg = totalA3Sum / totalStudents;
        double a4AllAvg = totalA4Sum / totalStudents;
        double a5AllAvg = totalA5Sum / totalStudents;

        double midAllAvg = totalMidSum / totalStudents;
        double finAllAvg = totalFinalSum / totalStudents;
        double gradeAllAvg = totalGradeSum / totalStudents;

        String q1All = formatValue(q1AllAvg);
        String q2All = formatValue(q2AllAvg);
        String q3All = formatValue(q3AllAvg);
        String q4All = formatValue(q4AllAvg);
        String q5All = formatValue(q5AllAvg);

        String a1All = formatValue(a1AllAvg);
        String a2All = formatValue(a2AllAvg);
        String a3All = formatValue(a3AllAvg);
        String a4All = formatValue(a4AllAvg);
        String a5All = formatValue(a5AllAvg);

        String midAll = formatValue(midAllAvg);
        String finAll = formatValue(finAllAvg);

        String formattedGradeAllAvg = String.format("%.2f", gradeAllAvg);


        String letterGrade = getLetterGrade(gradeAllAvg, schemeObj);


        System.out.printf("%-35.35s %-6d " +
                        "%-4s %-4s %-4s %-4s %-4s " + "%-4s %-4s %-4s %-4s %-4s " + "%-5s %-5s "
                        + "%-5s %-6s\n",
                "All", totalStudents, q1All, q2All, q3All, q4All, q5All, a1All, a2All, a3All, a4All, a5All,
                midAll, finAll, formattedGradeAllAvg, letterGrade);
    }

    private static String formatValue(double value) {
        double rounded = Math.round(value * 10.0) / 10.0;
        if (rounded == 0.0) {
            return " ";
        } else {
            return (String.format("%.1f", rounded));
        }
    }

    private static String getLetterGrade(double gradeAvg, JsonObject schemeObj) {
        if (gradeAvg >= schemeObj.getInt("A")) {
            return "A";
        } else if (gradeAvg >= schemeObj.getInt("A-")) {
            return "A-";
        } else if (gradeAvg >= schemeObj.getInt("B+")) {
            return "B+";
        } else if (gradeAvg >= schemeObj.getInt("B")) {
            return "B";
        } else if (gradeAvg >= schemeObj.getInt("B-")) {
            return "B-";
        } else if (gradeAvg >= schemeObj.getInt("C+")) {
            return "C+";
        } else if (gradeAvg >= schemeObj.getInt("C")) {
            return "C";
        } else if (gradeAvg >= schemeObj.getInt("C-")) {
            return "C-";
        } else if (gradeAvg >= schemeObj.getInt("D+")) {
            return "D+";
        } else if (gradeAvg >= schemeObj.getInt("D")) {
            return "D";
        } else if (gradeAvg >= schemeObj.getInt("D-")) {
            return "D-";
        } else {
            return "E";
        }
    }

    private static double calculateExpectedFinalExam(StudentGrade student, JsonObject schemaObj){
        int quizMaxScore = schemaObj.getJsonObject("Quiz").getInt("MaxScore");
        int quizWeight = schemaObj.getJsonObject("Quiz").getInt("Weight");
        int assignmentMaxScore = schemaObj.getJsonObject("Assignment").getInt("MaxScore");
        int assignmentWeight = schemaObj.getJsonObject("Assignment").getInt("Weight");
        int midtermMaxScore = schemaObj.getJsonObject("Midterm").getInt("MaxScore");
        int midtermWeight = schemaObj.getJsonObject("Midterm").getInt("Weight");
        int finalMaxScore = schemaObj.getJsonObject("Final").getInt("MaxScore");
        int finalWeight = schemaObj.getJsonObject("Final").getInt("Weight");

        double quizSum = 0;
        int quizCount = 0;
        for (Quiz quiz : student.getQuizzes()) {
            if (quiz.getScore() > 0) {
                quizSum += quiz.getScore();
                quizCount++;
            }
        }
        double quizAvg = (quizCount > 0) ? quizSum / quizCount : 0;

        double assignmentSum = 0;
        int assignmentCount = 0;
        for (Assignment assignment : student.getAssignments()) {
            if (assignment.getScore() > 0) {
                assignmentSum += assignment.getScore();
                assignmentCount++;
            }
        }
        double assignmentAvg = (assignmentCount > 0) ? assignmentSum / assignmentCount : 0;

        int midtermScore = student.getMidterm().getScore();

        double targetGrade = 70.0;

        double quizComponent = (quizAvg / quizMaxScore) * quizWeight;
        double assignmentComponent = (assignmentAvg / assignmentMaxScore) * assignmentWeight;
        double midtermComponent = ((double) midtermScore / midtermMaxScore) * midtermWeight;

        double currentComponents = quizComponent + assignmentComponent + midtermComponent;

        double finalComponent = targetGrade - currentComponents;
        double finalExamScore = (finalComponent / finalWeight) * finalMaxScore;

        return Math.min(Math.max(finalExamScore, 0), finalMaxScore);
    }
}
