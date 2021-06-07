package com.alrayan.blackboxtester;

public class App {
    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                BlackBoxTestRunner blackBoxTestRunner = new BlackBoxTestRunner(args[0]);
                blackBoxTestRunner.runTests();
                blackBoxTestRunner.runReport();
                blackBoxTestRunner.runLoadReport();
            } else {
                help();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
            help();
        }
    }

    private static void help() {
        System.out.println("");
        System.out.println("usage : java -jar black-box-tester-1.0.jar <test file>");
        System.out.println("");
        System.exit(0);
    }
}
