package VotingSystemPack;

import java.sql.PreparedStatement;
import java.sql.*;

public class Admin extends User {

    public Admin(String name, String login, String password) {
        super(name, login, password);
    }

    static void addUser(String name, String login, String password) throws SQLException {
        String sql = "INSERT INTO users (name, login, us_password) Values (?, ?, ?)";
        PreparedStatement preparedStatement = Main.conn.prepareStatement(sql);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, login);
        preparedStatement.setString(3, password);

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }


}
