package VotingSystemPack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Voting {
    private int votingId;

    private String title;
    private List<Candidate> candidates = new ArrayList<Candidate>();

    public Voting(String title) {
        this.title = title;
    }

    public void appendCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    public void addVoting() throws SQLException {
        addSelection();
        String query = "insert into candidates (name, selection_id) value (?,?) ;";
        PreparedStatement prStmt = Main.conn.prepareStatement(query);
        for (Candidate c : candidates) {
            prStmt.setString(1, c.getName());
            prStmt.setInt(2, votingId);
            prStmt.addBatch();
        }
        prStmt.executeBatch();
        prStmt.close();
    }

    private void addSelection() throws SQLException {
        PreparedStatement prStmt = Main.conn.prepareStatement(
                "insert into selections(name) value(?);");
        prStmt.setString(1, title);
        prStmt.execute();
        Statement stmt = Main.conn.createStatement();
        ResultSet rs = stmt.executeQuery("select id from selections order by id desc limit 1;");
        rs.next();
        votingId = rs.getInt("id");
        prStmt.close();
    }


    private ArrayList<Double> calculateVoices() {
        ArrayList<Double> percentage = new ArrayList<Double>();
        int sum = 0;

        for (Candidate c : candidates) {
            sum += c.getVoices();
        }
        for (Candidate c : candidates) {
            percentage.add((double) c.getVoices() / sum * 100);
        }
        return percentage;
    }

    private String getResult() {
        ArrayList<Double> percentage = calculateVoices();
        StringBuilder result = new StringBuilder("Selection's result:\n");
        for (int i = 0; i < percentage.size(); i++) {
            result.append(String.format("%d. %s - %f", i + 1,
                    candidates.get(i).getName(), percentage.get(i))).append("%;\n");
        }
        return result.toString().replaceAll("(.$)", ".");
    }


    public void finishVoting() throws SQLException {
        String query = "call endVoting();";
        Statement stmt = Main.conn.createStatement();
        stmt.execute(query);
        stmt.close();
        System.out.println(getResult());
    }

    public int getVotingId() {
        return votingId;
    }

    public String getTitle() {
        return title;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

}
