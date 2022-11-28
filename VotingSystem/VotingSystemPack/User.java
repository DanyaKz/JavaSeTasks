package VotingSystemPack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class User {
    private String name;
    private String login;
    private String password;

    public User(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;
    }


    static public ResultSet getUser(String login, String password) throws SQLException {
        String isExist = "select * from users where login = ? and us_password = ?";
        PreparedStatement preparedStatement = Main.conn.prepareStatement(isExist);
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, password);
        return preparedStatement.executeQuery();
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
