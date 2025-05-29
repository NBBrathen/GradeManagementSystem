package ManagementSystem;


abstract class Coursework {
    private int score;                  // grade for the coursework
    private int weight;                 // weight for the coursework
    private int maxScore;

    public Coursework(int score) {
        this.score = score;
    }

    public Coursework(int score, int weight) {
        this.score = score;
        this.weight = weight;
    }

    // Do not change the type and the argument in the following abstract methods
    abstract boolean setScore(int score);
    abstract boolean setWeight(int weight);
    abstract boolean setMaxScore(int max);
    abstract float getWeightedScore();

    // TODO: you may add more methods below

    public int getMaxScore(){
        return maxScore;
    }
    public int getScore(){
        return score;
    }
    public int getWeight(){
        return weight;
    }

    public void updateScore(int score) {
        this.score = score;
    }

    public void updateWeight(int weight) {
        this.weight = weight;
    }

    public void updateMaxScore(int max) {
        this.maxScore = max;
    }
}

