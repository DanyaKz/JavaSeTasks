package VotingSystemPack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Elector extends User {

    public Elector(String name, String login, String password) {
        super(name, login, password);
    }


    public void vote(String option, int selectionId) throws SQLException {
        PreparedStatement prStmt = Main.conn.prepareStatement("call setVote(?,?,?)");
        prStmt.setString(1, option);
        prStmt.setInt(2, selectionId);
        prStmt.setString(3, getLogin());
        prStmt.execute();
        prStmt.close();
    }

    public boolean isVoted() throws SQLException {
        String query = "select isVoted from users where login = ?;";
        PreparedStatement prStmt = Main.conn.prepareStatement(query);
        prStmt.setString(1, getLogin());
        System.out.println(prStmt.toString());
        ResultSet rs = prStmt.executeQuery();
        rs.next();
        boolean isVoted = rs.getBoolean(1);
        rs.close();
        return isVoted;
    }

}
