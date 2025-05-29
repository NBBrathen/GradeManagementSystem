package ManagementSystem;

import javax.json.JsonObject;
import java.util.ArrayList;


enum LetterGrade {
    A("A"), A_MINUS("A-"),
    B_PLUS("B+"), B("B"), B_MINUS("B-"),
    C_PLUS("C+"), C("C"), C_MINUS("C-"),
    D_PLUS("D+"), D("D"), D_MINUS("D-"),
    E("E"), I("I");

    private final String grade;

    LetterGrade(String grade) {
        this.grade = grade;
    }

    public String getGrade() {
        return grade;
    }

    @Override
    public String toString() {
        return grade;
    }
}

public class StudentGrade {
    String name, department;
    private String id;


    private ArrayList<Quiz> quizzes;
    private ArrayList<Assignment> assignments;
    private Midterm midterm;
    private Finalexam finalexam;


    private float grade;
    private LetterGrade letterGrade;

    public void printGrade() {

        if (quizzes.size()!=5 || assignments.size() != 5) {
            System.out.println("INVALID NUM QUIZZES OR ASSIGNMENTS FOR STUDENT: " + name);
            return;
        }

        ArrayList<String> quizOutputs = new ArrayList<>();
        ArrayList<String> assignmentOutputs = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            quizOutputs.add(" ");
            assignmentOutputs.add(" ");
        }

        String midOutput;
        String finalOutput;
        for (int i = 0; i < 5; i++) {
            if (i < quizzes.size() && quizzes.get(i) != null) {
                int score = quizzes.get(i).getScore();
                if (score == 0){
                    quizOutputs.set(i, " ");
                } else{
                    quizOutputs.set(i, String.valueOf(score));
                }
            } else{
                quizOutputs.set(i, " ");
            }
        }
        for (int i = 0; i < 5; i++) {
            if (i < assignments.size() && assignments.get(i) != null) {
                int score = assignments.get(i).getScore();
                if (score == 0){
                    assignmentOutputs.set(i, " ");
                } else{
                    assignmentOutputs.set(i, String.valueOf(score));
                }
            } else{
                assignmentOutputs.set(i, " ");
            }
        }

        if (midterm != null) {
            int score = midterm.getScore();
            midOutput = (score == 0) ? " " : String.valueOf(score);
        } else {
            midOutput = " ";
        }

        if (finalexam != null) {
            int score = finalexam.getScore();
            finalOutput = (score == 0) ? " " : String.valueOf(score);
        } else {
            finalOutput = " ";
        }

        String gradeOutput;
        if (grade == 0) {
            gradeOutput = " ";
        } else {
            gradeOutput = String.format("%.2f", grade);
        }

        String letterOutput;
        if (letterGrade == null){
            letterOutput = " ";
        } else{
            letterOutput = String.valueOf(letterGrade);
        }

        System.out.printf("%-20.20s %-10s %-35.35s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-4s %-5s %-5s %-5s %-6s\n",
                name, id, department, quizOutputs.get(0), quizOutputs.get(1), quizOutputs.get(2), quizOutputs.get(3),
                quizOutputs.get(4), assignmentOutputs.get(0), assignmentOutputs.get(1), assignmentOutputs.get(2),
                assignmentOutputs.get(3), assignmentOutputs.get(4), midOutput, finalOutput, gradeOutput, letterOutput);
    }



    // Constructor
    public StudentGrade(String name, String id, String department, ArrayList<Quiz> quizzes,
                        ArrayList<Assignment> assignments, Midterm midterm, Finalexam finalExam){
        this.name = name;
        this.id = id;
        this.department = department;
        this.quizzes = quizzes;
        this.assignments = assignments;
        this.midterm = midterm;
        this.finalexam = finalExam;

    }

    public void calculateGrade() {
        float totalScore = 0.0f;
        float totalWeight = 0.0f;

        // Calculate Quiz average (if any taken)
        int quizCount = 0;
        float quizTotal = 0;
        for (Quiz quiz : quizzes) {
            if (quiz != null && quiz.getScore() > 0) {
                quizTotal += (float)quiz.getScore() / quiz.getMaxScore();
                quizCount++;
            }
        }
        if (quizCount > 0) {
            float quizAvg = quizTotal / quizCount;
            totalScore += quizAvg * quizzes.get(0).getWeight();
            totalWeight += quizzes.get(0).getWeight();
        }

        // Calculate Assignment average (if any taken)
        int assignmentCount = 0;
        float assignmentTotal = 0;
        for (Assignment assignment : assignments) {
            if (assignment != null && assignment.getScore() > 0) {
                assignmentTotal += (float)assignment.getScore() / assignment.getMaxScore();
                assignmentCount++;
            }
        }
        if (assignmentCount > 0) {
            float assignmentAvg = assignmentTotal / assignmentCount;
            totalScore += assignmentAvg * assignments.get(0).getWeight();
            totalWeight += assignments.get(0).getWeight();
        }

        // Add Midterm (if taken)
        if (midterm != null && midterm.getScore() > 0) {
            totalScore += ((float)midterm.getScore() / midterm.getMaxScore()) * midterm.getWeight();
            totalWeight += midterm.getWeight();
        }

        // Add Final (if taken)
        if (finalexam != null && finalexam.getScore() > 0) {
            totalScore += ((float)finalexam.getScore() / finalexam.getMaxScore()) * finalexam.getWeight();
            totalWeight += finalexam.getWeight();
        }

        // Calculate final grade
        if (totalWeight > 0) {
            this.grade = (totalScore / totalWeight) * 100;
        } else {
            this.grade = 0;
        }
    }

    public void calculateLetterGrade(JsonObject schemeObj) {
        if (grade >= schemeObj.getInt("A")){
            this.letterGrade = LetterGrade.A;
        } else if (grade >= schemeObj.getInt("A-")){
            this.letterGrade = LetterGrade.A_MINUS;
        } else if (grade >= schemeObj.getInt("B+")){
            this.letterGrade = LetterGrade.B_PLUS;
        } else if (grade >= schemeObj.getInt("B")){
            this.letterGrade = LetterGrade.B;
        } else if (grade >= schemeObj.getInt("B-")){
            this.letterGrade = LetterGrade.B_MINUS;
        } else if (grade >= schemeObj.getInt("C+")){
            this.letterGrade = LetterGrade.C_PLUS;
        } else if (grade >= schemeObj.getInt("C")){
            this.letterGrade = LetterGrade.C;
        } else if (grade >= schemeObj.getInt("C-")){
            this.letterGrade = LetterGrade.C_MINUS;
        } else if (grade >= schemeObj.getInt("D+")){
            this.letterGrade = LetterGrade.D_PLUS;
        } else if (grade >= schemeObj.getInt("D")){
            this.letterGrade = LetterGrade.D;
        } else if (grade >= schemeObj.getInt("D-")){
            this.letterGrade = LetterGrade.D_MINUS;
        } else{
            this.letterGrade = LetterGrade.E;
        }
    }

    // Getters
    public String getName() { return name; }
    public String getId() { return id; }
    public String getDepartment() {return department; }
    public ArrayList<Quiz> getQuizzes() { return quizzes; }
    public ArrayList<Assignment> getAssignments() { return assignments; }
    public Midterm getMidterm() { return midterm; }
    public Finalexam getFinalexam() { return finalexam; }
    public float getGrade() { return grade; }
    public LetterGrade getLetterGrade() { return letterGrade; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }
    public void setDepartment(String department) { this.department = department; }
    public void setQuiz(ArrayList<Quiz> quiz) { this.quizzes = quiz; }
    public void setAssignment(ArrayList<Assignment> assignment) { this.assignments = assignment; }
    public void setMidterm(Midterm midterm) { this.midterm = midterm; }
    public void setFinalexam(Finalexam finalexam) {this.finalexam = finalexam;}


    @Override
    public String toString() {
        return this.name + " /";
    }
}



