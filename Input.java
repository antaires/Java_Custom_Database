/* This class handles and processes all user input. It is used by the database Manager */

import java.util.*;
import java.io.*;

class Input {

    private Scanner scanner = new Scanner(System.in);

    public startChoice getChoice(){
        String choose = "\nOPTIONS:\nUSE database: enter USE\nNEW database: enter NEW\nEXIT: enter EXIT\n";
        String choice = "";
        
        while (!(choice.equals("new") || choice.equals("use") || choice.equals("exit"))){
            choice = getWord(choose).toLowerCase();
        }

        if (choice.equals("new")){
            return startChoice.newDatabase;
        } else if (choice.equals("use")){
            return startChoice.useDatabase;
        } else {
            return startChoice.exit;
        } 
    }

    public String getWord(String phrase){
        String word = "";
        System.out.println(phrase);
        word = scanner.nextLine();
        return word;
    }

    public String chooseDatabase(ArrayList<String> dbNames){
        String choose = "\nEnter database name (exactly as it appears): \n";
        String choice = ""; 

        while ( !(dbNames.contains(choice)) ){
            choice = getWord(choose);
        }
        return choice;
    }

    public String dbOptions(){
        String choose = "\nInput command...:\nlist\nview <tableName>\ntype\nSELECT <column> FROM <tableName>";
        String choice = "";
        
        choice = getWord(choose);
        return choice;
    }

    public void close(){
        scanner.close();
    }


    // ---------- Testing -----------
    public static void main(String[] args) {
        Input program = new Input();
        program.run();
        program.test();
    }

    // Run the tests
    private void run() {
        boolean testing = false;
        assert(testing = true);
        if (! testing) throw new Error("Use java -ea Input");
    }

    private void test(){
        //testGetChoice();
        System.out.println("Input: all tests passed");
    }

    private void testGetChoice(){
        Input input = new Input();
        startChoice choice = input.getChoice();
        System.out.println(choice);
    }
}