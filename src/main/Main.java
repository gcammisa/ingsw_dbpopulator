package main;

import com.sun.org.apache.xpath.internal.operations.Bool;
import data.Sex;
import database.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.*;

public class Main {

    /**
     * some useful,general purpose, regex
     */
    static final Pattern NAME_REGEX = Pattern.compile("^[A-Z][a-z]+$");
    //year must be a 4 digit number, otherwise we'll have dates like 06/05/0095
    //this regex is more restrictive then previous one.
    static final Pattern DATE_REGEX = Pattern.compile("^((3[01]/(0?[13578]|10|12))|(30/(0?[469]|11))|((0?[1-9]|[12]\\d)/(1[012]|0?[1-9])))/(19|20)\\d{2}$");

    static final Pattern CF_REGEX =  Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}[A-Z][A-Z0-9]{2}[A-Z][A-Z0-9]{3}[A-Z]$");
    static final Pattern PWD_REGEX =  Pattern.compile("^[a-zA-Z0-9]{8,40}$");
    private static final Pattern SEX_REGEX = Pattern.compile("^[mMfF]$");

    public static void main(String[] args) {

        //Asks database data to the user
        String db_addr = askAndGet("Inserisci l'ip del DB: ");
        String db_port = askAndGet("Inserisci la porta del DB: ");
        String db_name = askAndGet("Inserisci il nome del DB: ");
        String db_user = askAndGet("Inserisci il nome utente del DB: ");
        String db_passwd = askAndGet("Inserisci la password del DB: ");

        //Asks admin data to the user
        String name = askAndCheck("Inserisci il nome dell'admin: ", NAME_REGEX);
        String surname = askAndCheck("Inserisci il cognome dell'admin: ", NAME_REGEX);
        String birthDate = askAndCheck("Inserisci la data di nascita gg/mm/yyyy: ", DATE_REGEX).replace("\\", "-");
        Sex sex = stringToSex(askAndCheck("Inserisci il sesso (M o F): ", SEX_REGEX));
        String CF = askAndCheck("Inserisci il codice fiscale: ", CF_REGEX);
        String passwordHash = getPasswordHash(askAndCheck("Inserisci la password: ", PWD_REGEX), CF);



        Connection mConnection = null;

        try {
            mConnection = DriverManager.getConnection("jdbc:postgresql://" + db_addr + ":" + db_port + "/" + db_name + "?" + "user=" + db_user + "&password=" + db_passwd);
        } catch (SQLException e) {
            System.err.println("Unable to connect to server: " + e);
        }
        ScriptRunner runner = new ScriptRunner(mConnection, false, false);
        String file = "script.sql";

        try {
            String text = new String(Files.readAllBytes(Paths.get(file)), UTF_8);

            StringBuilder sb = new StringBuilder();
            sb.append(text);
            sb.append("\n");
            sb.append(queryBuilder(name,surname, birthDate, sex, CF, passwordHash));
            String completeQuery = sb.toString();

            runner.runScript(new BufferedReader(new StringReader(completeQuery)));

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

    private static String askAndCheck(String question, Pattern pattern) {
        boolean finito=false;
        String lettura = null;
        do
        {
            lettura = askAndGet(question);
            lettura = lettura.trim();
            if (lettura.length() > 0 && Pattern.matches(pattern.toString(), lettura))
                finito=true;
            else
                System.out.println("Input non valido, riprova: ");
        } while (!finito);

        return lettura;
    }

    private static Sex stringToSex(String input)
    {
        if (input.matches("m") || input.matches("M"))
        {
            return Sex.MALE;
        }
        if (input.matches("f") || input.matches("F"))
        {
            return Sex.FEMALE;
        }
        return Sex.MALE;
    }

    static String getPasswordHash(String password, String login){
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            String tmp = password + login;
            byte[] bytes = md.digest(tmp.getBytes());
            StringBuilder sb = new StringBuilder();
            for(byte b : bytes){
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return generatedPassword;
    }

    static String stringToQuoted(String input)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append(input);
        sb.append("'");

        return sb.toString();
    }

    static String queryBuilder(String nome, String cognome, String birthDate, Sex sex, String CF, String passwordHash )
    {
        StringBuilder sb = new StringBuilder();

        //Builds the query to add a person
        sb.append("INSERT INTO public.person(\"CF\", birthdate, name, surname, sex)\n");
        sb.append("VALUES (");
        sb.append(stringToQuoted(CF));
        sb.append(",");
        sb.append(stringToQuoted(birthDate));
        sb.append(",");
        sb.append(stringToQuoted(nome));
        sb.append(",");
        sb.append(stringToQuoted(cognome));
        sb.append(",");
        sb.append(stringToQuoted(sex.toString().substring(0, 1)));
        sb.append(");\n");

        //Builds the query to add an operator
        sb.append("INSERT INTO public.login(username, passwordhash)\n");
        sb.append("VALUES (");
        sb.append(stringToQuoted(CF));
        sb.append(",");
        sb.append(stringToQuoted(passwordHash));
        sb.append(");\n");


        //Builds the query to add a login
        sb.append("INSERT INTO public.operator(login)\n");
        sb.append("VALUES (");
        sb.append(stringToQuoted(CF));
        sb.append(");\n");

        //Returns the whole query to add at the end of the sql script
        return sb.toString();
    }
}
