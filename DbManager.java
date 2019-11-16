/* Wrapper for whole Db structure - handles program loop and user interface 
It deals with the main program loop, and prompts / deals with user queries. 
On command, it will show the databases in the system, allow users to add new 
databases or select an existing database loaded from file.*/

import java.io.File;
import java.io.FileFilter;
import java.util.*;

class DbManager {

    List<Database> dbs = new ArrayList<>();
    Database currentDb; 
    Input input = new Input();
    Display display = new Display(30);
    ReadWrite rw = new ReadWrite();
    Table currentT; 
    public static void main(String[] args) {
        DbManager program = new DbManager();
        program.run(args);
    }

    // Deal with the command line arguments
    void run(String[] args) {
        boolean testing = false;
        assert(testing = true);
        if (args.length == 0 && testing) test();
        else start();
    }

    private void start(){

        display.start();
        //get all files in database folder (if it exists - its created by first database)
        ArrayList<String> databaseNames = rw.getIndex();
        display.showDatabases(databaseNames);

        //load all databases to dbs
        for (String dbName : databaseNames){
            System.out.println(dbName + " is added to dbs");
            Database newDb = new Database(dbName);
            dbs.add(newDb);
        }

        startChoice choice = input.getChoice();
        switch(choice){
            case useDatabase:
                useDatabase();
                break;
            case newDatabase:
                newDatabase();
                break;
            case exit:
                System.exit(0);
                input.close();
                break;
        }

        //loop over this and do Db actions with current Db
        dbAction(input.dbOptions());
    }

    //sets current database to user choice
    private void useDatabase(){
        ArrayList<String> databaseNames = rw.getIndex();
        display.showDatabases(databaseNames);
        String dbName = input.chooseDatabase(databaseNames);

        //set currentDB to dbName
        for (Database db : dbs){
            if (db.getName().equals(dbName)){
                currentDb = db; 
            } 
        }

        //have options: updateDatabse() - add tables, add rows, view tables...
        display.phrase("WORKING WITH DATABASE: " + currentDb.getName());
    }

    private void dbAction(String action){
        //all operations that may be done on databases
        //split action string up by blanks, to get first command, and names of columns
        String[] actionArr= action.split("\\s");

        //list tables
        if (actionArr[0].equals("list") || actionArr[0].equals("LIST")){
            currentDb.displayTables();
            dbAction(input.dbOptions());
        }
        else if (actionArr[0].equals("view") || actionArr[0].equals("VIEW")){
            //print out column names of given table
            if (actionArr.length>1){
                currentT = currentDb.getTable(actionArr[1]);
                if (currentT != null){
                    display.drawTable(currentT);
                }
            } else {
                display.phrase("You must input: view <tableName>");
            }
            dbAction(input.dbOptions());
        }
        //SELECT colName, colName... FROM tableName WHERE colName ><= x
        else if (actionArr[0].equals("select") || actionArr[0].equals("SELECT")){
            if (actionArr.length == 4){
                String colName = actionArr[1];
                String tableName = actionArr[3];
                Table t = currentDb.getTable(tableName);
                Record r = null;
                if (t != null){
                    r = t.getCol(colName);
                }
                System.out.println();
                display.drawRow(r);
                System.out.println();
            }
            dbAction(input.dbOptions());
        }
        else if (actionArr[0].equals("type")){
            String tableName = input.getWord("Type table name exactly as it appears: ");
            Table t = currentDb.getTable(tableName);
            display.drawType(t);
        } else {
            dbAction(input.dbOptions());
        }
    }

    private void newDatabase(){
        String name = input.getWord("Enter the name of the new database: ");
        Database db = new Database(name);
        dbs.add(db);
        dbAction(input.dbOptions());
    }

    // ---------- Testing -----------
    private void test(){
        System.out.println("DbManager: all tests passed");
    }

}
