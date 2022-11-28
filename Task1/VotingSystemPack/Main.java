package VotingSystemPack;

import java.sql.*;
import java.util.*;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    public static Connection conn;
    static Elector curUser = null;
    static Voting curVoting = null;
    static int usersCount;

    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost/votingDataBase?serverTimezone=Europe/Moscow&useSSL=false";
            String username = "root";
            String password = "regina16";
            conn = DriverManager.getConnection(url, username, password);
            electorCount();
            login();
        } catch (Exception ex) {
            System.out.println("Connection failed...");
            System.out.println(ex);
        }
    }

    static void login() throws SQLException {
        System.out.println(usersCount);
        if (usersCount <= 0) {
            System.out.println("This voting has ended. Results: \n");
            curVoting.finishVoting();
            conn.close();
            System.exit(0);
        }
        System.out.print("Enter login: ");
        String login = scanner.next();
        System.out.print("Enter password: ");
        String userPassword = scanner.next();
        ResultSet user = User.getUser(login, userPassword);
        if (user.next()) {
            if (user.getBoolean(5)) {
                User admin = new Admin(user.getString(2), user.getString(3), user.getString(4));
                System.out.println("Welcome, " + admin.getName());
                user.close();
                adminMenu();
            } else {
                curUser = new Elector(user.getString(2), user.getString(3), user.getString(4));
                if (curUser.isVoted()) {
                    System.out.println("You've already voted.");
                    curUser = null;
                    login();
                }
                System.out.println("Welcome, " + curUser.getName());
                user.close();
                electorMenu();
            }
        } else {
            System.out.println("Incorrect login or password");
            login();
        }
    }

    static void adminMenu() throws SQLException {
        System.out.print("Choose action: \n\t1. Add user;" +
                "\n\t2. Create selection;" +
                "\n\t3. Get previous selection data;" +
                "\n\t4. Get all selections history;" +
                "\n\t5. Sign out;" +
                "\n\t6. End voting." +
                "\n--> ");
        int act = scanner.nextInt();
        scanner.nextLine();
        switch (act) {
            case 1:
                fillUserData();
                break;
            case 2:
                createSelection();
                break;
            case 3:
                getPrevSelData();
                break;
            case 4:
                getAllSelData();
                break;
            case 5:
                login();
                break;
            case 6:
                if (!Objects.isNull(curVoting)) curVoting.finishVoting();
                Main.conn.close();
                break;
            default:
                adminMenu();
        }

    }

    private static void printCurVoting(List<Candidate> candidates) {
        StringBuilder selection = new StringBuilder("Current selection: \n");
        selection.append(curVoting.getTitle()).append(":\n");
        for (int i = 0; i < candidates.size(); i++) {
            selection.append(String.format("\t%d. %s,\n", i + 1, candidates.get(i).getName()));
        }
        System.out.println(selection.toString().replaceAll("(.$)", "."));
    }

    static void electorMenu() throws SQLException {
        if (curVoting == null) {
            System.out.println("Here's no active voting");
            curUser = null;
            login();
        }
        List<Candidate> candidates = curVoting.getCandidates();

        printCurVoting(candidates);

        System.out.print("Enter the number of option -> ");
        int option = scanner.nextInt();

        if (option <= 0 || option > candidates.size()) {
            System.out.println("Here is no " + option + " option");
            electorMenu();
        }
        candidates.get(option - 1).addVoices();
        curUser.vote(candidates.get(option - 1).getName(), curVoting.getVotingId());
        usersCount--;
        login();
    }

    static void fillUserData() throws SQLException {
        System.out.print("Enter much users you would like to add: ");
        int usersNum = scanner.nextInt();
        scanner.nextLine();
        while (usersNum > 0) {
            System.out.print("Input name: ");
            String name = scanner.next();
            System.out.print("Input login : ");
            String login = scanner.next();
            System.out.print("Input password: ");
            String password = scanner.next();

            try {
                Admin.addUser(name, login, password);
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("User with login \"" + login + "\" already exists");
                continue;
            }
            usersNum--;
        }
        electorCount();
        adminMenu();
    }

    static void createSelection() throws SQLException {
        System.out.print("Enter voting title: ");
        String title = scanner.nextLine();
        curVoting = new Voting(title);
        System.out.print("Enter how many options you'd like to add: ");
        int candidCapacity = scanner.nextInt();
        scanner.nextLine();
        for (int i = 0; i < candidCapacity; i++) {
            System.out.print("Enter option name: ");
            String name = scanner.nextLine();
            curVoting.appendCandidate(new Candidate(name));
        }
        curVoting.addVoting();
        adminMenu();
    }

    static void getPrevSelData() throws SQLException {
        String query = "select c.*, s.id, s.name, s.start_time from candidates c\n" +
                "inner join \n" +
                "\t(select selection_id s, max(voices) mv from candidates\n" +
                "    group by selection_id) sub\n" +
                "\ton sub.s = c.selection_id and sub.mv = c.voices\n" +
                "join selections s on s.id = c.selection_id\n" +
                "group by s.id" +
                "limit 1;";
        PreparedStatement prStmt = conn.prepareStatement(query);
        ResultSet result = prStmt.executeQuery();
        result.next();
        String toReturn = "Title - " + result.getString(6) + "\n" +
                "\tResult - " + result.getString(2) + "\n" +
                "\tDate - " + result.getString(7).split(" ")[0] + "\n" +
                "\tTime - " + result.getString(7).split(" ")[1] + "\n";
        System.out.println(toReturn);
        System.out.println("==================================");
        prStmt.close();
        adminMenu();
    }

    static void getAllSelData() throws SQLException {
        String query = "select c.*, s.id, s.name, s.start_time from candidates c\n" +
                "inner join \n" +
                "\t(select selection_id s, max(voices) mv from candidates\n" +
                "    group by selection_id) sub\n" +
                "\ton sub.s = c.selection_id and sub.mv = c.voices\n" +
                "join selections s on s.id = c.selection_id\n" +
                "group by s.id;";
        PreparedStatement prStmt = conn.prepareStatement(query);
        ResultSet result = prStmt.executeQuery();

        int numeration = 0;
        while (result.next()) {
            String toReturn = "Title - " + result.getString(6) + "\n" +
                    "\tResult - " + result.getString(2) + "\n" +
                    "\tDate - " + result.getString(7).split(" ")[0] + "\n" +
                    "\tTime - " + result.getString(7).split(" ")[1] + "\n";
            System.out.println(++numeration + ". " + toReturn);
        }
        System.out.println("==================================");
        prStmt.close();
        adminMenu();
    }

    static void electorCount() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) c from users where isSuperUser = 0 and isVoted = 0;");
        rs.next();
        usersCount = rs.getInt(1);
        rs.close();
    }

}
