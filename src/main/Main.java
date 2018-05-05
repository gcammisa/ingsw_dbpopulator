package main;

import database.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {



        String db_addr = askAndGet("Inserisci l'ip del DB: ");
        String db_name = askAndGet("Inserisci il nome del DB: ");
        String db_user = askAndGet("Inserisci il nome utente del DB: ")
        String db_passwd = askAndGet("Inserisci la password del DB: ");

        Connection mConnection;


        try {
            Class.forName("com.mysql.jdbc.Driver");
            mConnection = DriverManager.getConnection("jdbc:postgresql://" + db_addr + "/" + db_name + "?" + "user=" + db_user + "&password=" + db_passwd);
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to get mysql driver: " + e);
        } catch (SQLException e) {
            System.err.println("Unable to connect to server: " + e);
        }
        ScriptRunner runner = new ScriptRunner(mConnection, false, false);
        String file = "script.sql";
        runner.runScript(new BufferedReader(new FileReader(file)));

    }

    private String askAndGet(String question) {
        System.out.println(question + "\n");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

}
