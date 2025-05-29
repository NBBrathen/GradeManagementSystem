package ManagementSystem;


class Midterm extends Coursework {

    public Midterm(int score) {
        super(score);
    }

    public Midterm(int score, int weight){
        super(score, weight);
    }

    @Override
    boolean setScore(int score){
        if (score < 0 || score > getMaxScore()) {
            return false;
        }
        updateScore(score);
        return true;
    }

    @Override
    boolean setWeight(int weight) {
        updateWeight(weight);
        return true;
    }

    @Override
    boolean setMaxScore(int max) {
        updateMaxScore(max);
        return true;
    }

    @Override
    float getWeightedScore() {
        if (getMaxScore() == 0){
            return 0;
        }
        return (float) ((getScore() / (float)getMaxScore()) * getWeight());
    }

}
