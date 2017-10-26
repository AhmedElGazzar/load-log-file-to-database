/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ef;

import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;

/**
 *
 * @author ahmed.elgazzar
 */
public class DataAccess {

    BufferedReader br = null;
    FileReader fr = null;
    Statement statement;
    PreparedStatement preparedStmt = null;
    ResultSet rs = null;
    DBConnection db = DBConnection.getDBConnection();
    public static final Logger LOGGER = Logger.getLogger(DataAccess.class.getName());
    Handler fileHandler;
    SimpleFormatter formatter;

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public void insertFile(String accesslog) {
        System.out.println("Inserting File in Database is running ...... please wait ....");
        logFile("Inserting File in Database is running ...... ");
        String query = "LOAD DATA LOCAL INFILE '" + accesslog + "'\n"
                + "     INTO TABLE wallethub_table\n"
                + "     FIELDS TERMINATED BY '|'\n"
                + "     LINES TERMINATED BY '\\n'\n"
                + "\n"
                + "     (date, ip,request, status,user_agent);";

        try {
            statement = db.conn.createStatement();
            rs = statement.executeQuery(query);
            System.out.println("\n The file inserted in Database : Done. \n");
            logFile(query);
            logFile("The File '" + accesslog + "' inserted in Database **** Done.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ArrayList<String> getIP(String startDate, String duration, int threshold) {
        ArrayList<String> list = new ArrayList<>();
        if (startDate.contains(".")) {
            startDate = startDate.replace(".", " ");
        }
        java.sql.Timestamp tsEnd = null;
        java.sql.Timestamp tsStart = Timestamp.valueOf(startDate);
        Calendar c = Calendar.getInstance();
        c.setTime(tsStart);

        if (duration.equalsIgnoreCase("hourly")) {
            c.add(Calendar.HOUR, 1);
            tsEnd = new Timestamp(c.getTime().getTime());

        } else if (duration.equalsIgnoreCase("daily")) {
            c.add(Calendar.DAY_OF_MONTH, 1);
            tsEnd = new Timestamp(c.getTime().getTime());
        }
        try {
            String query = "SELECT ip, count(ip) FROM wallethub_table WHERE date BETWEEN '" + tsStart + "' AND '" + tsEnd + "' GROUP BY ip HAVING count(ip) > '" + threshold + "'";
            logFile(query);
            statement = db.conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                String ip = rs.getString("ip");
                insertInAnotherTable(ip, threshold);
                list.add(ip);
                System.out.println(ip);
            }
            logFile("\n Retrieve IPs are Done. \n");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> getRequests(String ip) {
        ArrayList<String> list = new ArrayList<>();
        try {
            String query = "SELECT date,request FROM wallethub_table WHERE ip = '" + ip + "'";
            logFile(query);
            statement = db.conn.createStatement();
            rs = statement.executeQuery(query);
            while (rs.next()) {
                String requests = rs.getString("request");
                list.add(requests);
                System.out.println(requests);
            }
            String query1 = "SELECT count(request) FROM wallethub_table WHERE ip = '" + ip + "'";
            logFile(query1);
            statement = db.conn.createStatement();
            rs = statement.executeQuery(query1);
            rs.next();
            int rowCount = rs.getInt(1);
            System.out.println("\n Count of request made by '" + ip + "' is = " + rowCount);
            logFile("\n Retrieve requestes are Done.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public void insertInAnotherTable(String ip, int threshold) {
        try {
            String comment = "The IP: " + ip + " is blocked because made more than : " + threshold;
            String query = " INSERT INTO wallethub_table_comments (ip, comment)"
                    + " values (?, ?)";
            logFile(query);
            preparedStmt = db.conn.prepareStatement(query);
            preparedStmt.setString(1, ip);
            preparedStmt.setString(2, comment);

            preparedStmt.execute();
            logFile("IPs were inserted in Another Table : Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean check(String startDate, String duration, int threshold) {

        SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd.HH:mm:ss");
        try {
            format.parse(startDate);
        } catch (ParseException e) {
            System.out.println("The startDate invalid : Please insert date format: yyyy-MM-dd.HH:mm:ss");
            return false;
        }

        if (!duration.equals("daily") && !duration.equals("hourly")) {
            System.out.println("The duration invalid : Please insert hourly or daily");
            return false;
        }
        if (duration.equals("hourly")) {
            if (threshold > 200) {
                System.out.println("The threshold limit for hourly must equal or less than 200");
                return false;
            }
        }
        if (duration.equals("daily")) {
            if (threshold > 500) {
                System.out.println("The threshold limit for daily must equal or less than 500");
                return false;
            }
        }
        return true;
    }

    public boolean check(String accesslog) {
        File f = new File(accesslog);
        if (!f.exists()) {
            System.out.println("The file isn't exist");
            return false;
        }
        return true;
    }

    public boolean checkIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }

    public void logFile(String s) {
        String tempPath = System.getProperty("user.dir");

        tempPath = tempPath + "\\LogFile\\";
        File tempFile = new File(tempPath);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        String strFilePath = tempPath + "database.log";
        try {
            fileHandler = new FileHandler(strFilePath, true);
            LOGGER.setLevel(Level.ALL);
            fileHandler.setLevel(Level.ALL);
            LOGGER.addHandler(fileHandler);

            formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            LogRecord logRecord = new LogRecord(Level.ALL, s);
            LOGGER.log(logRecord);

        } catch (IOException | SecurityException e) {

        } finally {
             LOGGER.removeHandler(fileHandler);
            if (fileHandler != null) {
                fileHandler.close();
            }
        }
    }

}
