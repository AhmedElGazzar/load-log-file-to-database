/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ef;

import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ahmed.elgazzar
 */
public class DBConnection {

    public Connection conn;
    public static DBConnection db;
    static HashMap environmentMap;

    private DBConnection() {
        HashMap dbEntries = null;
        try {
            dbEntries = getEntries("datasource.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String driver = (String) dbEntries.get("db_driver");
        String url = (String) dbEntries.get("db_url");
        String dbName = (String) dbEntries.get("db_schema");
        String userName = (String) dbEntries.get("db_user_name");
        String password = (String) dbEntries.get("db_password");
        /*
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/";
        String dbName = "wallethub";
        String userName = "root";
        String password = "admin";*/
        try {
            Class.forName(driver).newInstance();
            this.conn = (Connection) DriverManager.getConnection(url + dbName, userName, password);
        } catch (Exception sqle) {
            sqle.printStackTrace();
        }
    }

    public static synchronized DBConnection getDBConnection() {
        if (db == null) {
            db = new DBConnection();
        }
        return db;
    }

    public HashMap getEntries(String fileName) throws Exception {
        HashMap entryMap = new HashMap();
        try {
            Node child;
            String key, value;
            DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document xmlDoc;
            InputStream input = getClass().getResourceAsStream(fileName);
            xmlDoc = db.parse(input);
            Element root = xmlDoc.getDocumentElement();
            root.normalize();

            NodeList list = root.getChildNodes();
            System.out.println("Loading " + fileName);
            for (int i = 0; i < list.getLength(); i++) {
                if ((child = list.item(i)).getNodeName().equals("parameter")) {
                    key = ((Element) child).getAttribute("key");
                    value = ((Element) child).getAttribute("value");
                    entryMap.put(key, value);
                    System.out.println(key + " = " + value);
                }
            }

            System.out.println("----------------------------------");
            return entryMap;
        } catch (Exception i) {
            throw i;
        }
    }

}
