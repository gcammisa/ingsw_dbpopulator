package main;

import database.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        //Asks database data to the user
        String db_addr = askAndGet("Inserisci l'ip del DB: ");
        String db_port = askAndGet("Inserisci la porta del DB: ");
        String db_name = askAndGet("Inserisci il nome del DB: ");
        String db_user = askAndGet("Inserisci il nome utente del DB: ");
        String db_passwd = askAndGet("Inserisci la password del DB: ");

        Connection mConnection = null;

        try {
            mConnection = DriverManager.getConnection("jdbc:postgresql://" + db_addr + ":" + db_port + "/" + db_name + "?" + "user=" + db_user + "&password=" + db_passwd);
        } catch (SQLException e) {
            System.err.println("Unable to connect to server: " + e);
        }
        ScriptRunner runner = new ScriptRunner(mConnection, false, false);
        String file = "script.sql";
        try {
            runner.runScript(new BufferedReader(new FileReader(file)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //Helper function to ask the user and return his answer as string
    private static String askAndGet(String question) {
        System.out.println(question);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

}
