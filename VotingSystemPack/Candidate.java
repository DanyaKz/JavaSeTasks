package VotingSystemPack;

public class Candidate {
    private String name;
    private int voices = 0;

    public Candidate(String name) {
        this.name = name;
    }

    void addVoices() {
        voices++;
    }

    public String getName() {
        return name;
    }

    public int getVoices() {
        return voices;
    }
}
