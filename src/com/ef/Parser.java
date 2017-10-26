/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ef;

import java.util.HashMap;

/**
 *
 * @author ahmed.elgazzar
 */
public class Parser {

    private static HashMap hashMap = null;
    private static String accesslog = null;
    private static String startDate = null;
    private static String duration = null;
    private static int threshold;
    private static String ip = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        DataAccess dataAccess = new DataAccess();
        hashMap = new HashMap();
        for (int i = 0; i < args.length; i++) {
            String key = (args[i].split("=")[0]).substring(2);
            String value = args[i].split("=")[1];
            hashMap.put(key, value);
        }

        if (hashMap.containsKey("accesslog")) {
            accesslog = hashMap.get("accesslog").toString();
            boolean checkFile = dataAccess.check(accesslog);
            if (checkFile) {
                dataAccess.insertFile(accesslog);
            } else {
                System.out.print("Failed to load '" + accesslog + "' file \n Please enter a valid file path");
            }
        }

        if (hashMap.containsKey("startDate") && hashMap.containsKey("duration") && hashMap.containsKey("threshold")) {
            startDate = hashMap.get("startDate").toString();
            duration = hashMap.get("duration").toString();
            threshold = Integer.parseInt(hashMap.get("threshold").toString());

            boolean checkD = dataAccess.check(startDate, duration, threshold);
            if (checkD) {
                System.out.print("\n IPs that made more than '" + threshold + "' requests are : \n");
                dataAccess.getIP(startDate, duration, threshold);
            } else {
                System.out.print("Failed to get IPs that made more than '" + threshold + "' requests \n Please enter a valid arguments");
            }
        }

        if (hashMap.containsKey("ip")) {
            ip = hashMap.get("ip").toString();
            boolean checkIP = dataAccess.checkIP(ip);
            if (checkIP) {
                System.out.print("\n \n The requests made by a given IP '" + ip + "' are : \n");
                dataAccess.getRequests(ip);
            }else{
                System.out.println("The IP invalid : Please insert correct IP format");
            }
        }
    }

}
