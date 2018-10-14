package com.ibeifeng.senior.usertrack.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by ibf on 12/02.
 */
public class ProcessTest {
    public static void main(String[] args) {
        String path = "/home/beifeng/o2o17/test.sh";
        String command = "sh " + path + " " + args[0];
        try {
            Process process = Runtime.getRuntime().exec(command);
            int waitValue = process.waitFor();
            if (waitValue == 0) {
                System.out.println("Success!!" + waitValue);
            } else {
                System.out.println("Failure!!!" + waitValue);
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = null;
                System.out.println("Error msg -----------------------");
                while((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                System.out.println("Error msg -----------------------");
            }
        } catch (IOException e) {
            System.out.println("error:" + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("error:" + e.getMessage());
        }
    }
}
