/* Deals with generating/updating tables from files and writing out a table to a file 
Responsible for reading / writing tables and databases to file, writing the index, the 
foreign key index, and creating the folder structure on command. Deals gracefully with
missing files */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

class ReadWrite {

    public boolean mkdirs(String folderName){
        //if folder doesn't exist, create it
        //returns true IFF new dir. created
        File f = new File(folderName);
        if (f.mkdirs()){
            return true;
        }
        return false;
    }

    //adds name to index of databases if its not there already - creates index.txt if needed
    public boolean setIndex(String name){

        //array to hold all database names from file
        ArrayList<String> dbNames = new ArrayList<String>();

        //create index if it doesn't exist
        File file = new File("databases/index.txt");
        Scanner sc;
        FileWriter fw; 

        try {
            sc = new Scanner(file); 
            //read current database names to array
            String currName; 
            if (sc.hasNextLine()){
                while(sc.hasNextLine()){
                    currName = sc.nextLine();
                    dbNames.add(currName);
                }        
            }
            sc.close();

            //if database name already in index, exit
            if ( dbNames.contains(name) ){return true;}

        } catch(FileNotFoundException ex){}
        
        //add new database name to list of dbs
        dbNames.add(name);

        //create / overwrite index file
        try {
            fw = new FileWriter("databases/index.txt");
        } catch (IOException ex2){
            System.out.println("ERROR: index not found and could not be created");
            return false;
        }

        //write array of database names to file
        for (String dbName : dbNames){
            try {
                fw.write(dbName);
                fw.write("\n");
            } catch (IOException ex3) {
                System.out.println("ERROR: could not write to index");
            }
        }

        try {
            fw.close();
        } catch (IOException ex4){
            System.out.println("ERROR: filewriter could not be closed");
            return false;
        }

        return true;
    }

    public ArrayList<String> getIndex(){

        ArrayList<String> dbNames = new ArrayList<String>();
        //create index if it doesn't exist
        File file = new File("databases/index.txt");
        Scanner sc;
        FileWriter fw; 

        try {
            sc = new Scanner(file); 
        } catch(FileNotFoundException ex) {          
            //if no index, create one
            try {
                fw = new FileWriter("databases/index.txt");
            } catch (IOException ex2){
                System.out.println("ERROR: index not found and could not be created");
                return dbNames;
            }
            try {
                fw.close();
            } catch (IOException ex3){
                System.out.println("ERROR: filewriter could not be closed");
                return dbNames;
            }
            return dbNames;
        }

        //get database names - 1 per line
        String name; 
        if (sc.hasNextLine()){
            while(sc.hasNextLine()){
                name = sc.nextLine();
                dbNames.add(name);
            }        
        }
        sc.close();

        return dbNames;
    }

    //removes the name of a database from the database index
    public boolean deleteIndex(String name){
       //array to hold all database names from file
       ArrayList<String> dbNames = new ArrayList<String>();

       //create index if it doesn't exist
       File file = new File("databases/index.txt");
       Scanner sc;
       FileWriter fw; 

       try {
           sc = new Scanner(file); 
           //read current database names to array
           String currName; 
           if (sc.hasNextLine()){
               while(sc.hasNextLine()){
                   currName = sc.nextLine();
                   dbNames.add(currName);
               }        
           }
           sc.close();

           //if database name already in index, remove it
           if ( dbNames.contains(name) ){
               dbNames.remove(name);
            }

       } catch(FileNotFoundException ex){}

       //create / overwrite index file
       try {
           fw = new FileWriter("databases/index.txt");
       } catch (IOException ex2){
           System.out.println("ERROR: index not found and could not be created");
           return false;
       }

       //write array of database names to file
       for (String dbName : dbNames){
           try {
               fw.write(dbName);
               fw.write("\n");
           } catch (IOException ex3) {
               System.out.println("ERROR: could not write to index");
           }
       }

       try {
           fw.close();
       } catch (IOException ex4){
           System.out.println("ERROR: filewriter could not be closed");
           return false;
       }

       return true;
    }

    public boolean deleteDirs(String filePath) {
        File f = new File(filePath);
        if (f.delete()){
            return true;
        }
        return false;
    }

    public boolean removeFile(String filePath, String fileName){
        File file = new File(filePath+fileName);
        return file.delete();
    }

    //returns names of all files inside given directory
    public ArrayList<String> getDirFiles(String filePath){
        ArrayList<String> results = new ArrayList<String>();
        File[] files = new File(filePath).listFiles();

        for (File file : files){
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return results;
    }

    //used by database to save foreign key information about its tables
    public void setForeignKeyIndex(String filepath, ArrayList<String> FKIndex){
        FileWriter fw;

        //create file
        try {
            fw = new FileWriter(filepath + "/FKIndex.txt");
            if (FKIndex.size() > 0){
                //add each row of array to a new line
                for (int i = 0; i < FKIndex.size(); i++){
                    fw.write(FKIndex.get(i));
                    fw.write("\n");
                }
            }
            fw.close();
        } catch (IOException ex){
            System.out.println("ERROR: failed to create foreign key index file");
        }
        
    }

    //used by database to construct tables with foreign keys
    //filepath is "databases/databaseName"
    public ArrayList<String> getForeignKeyIndex(String filepath){
        //if no foreignkey index, creates empty file and returns empty list
        ArrayList<String> FKIndex = new ArrayList<String>();
        String line; 
        File file = new File(filepath + "/FKIndex.txt");
        Scanner sc; 
        int cnt = 0;

        try {
            sc = new Scanner(file);
            if (sc.hasNextLine()){
                while(sc.hasNextLine()){
                    line = sc.nextLine();
                    FKIndex.add(cnt++, line);
                }        
            }
            sc.close();
        } catch(FileNotFoundException ex) {}
        return FKIndex;
    }

    //generate new table from file
    public Table readTable(String pathName, String filename){
        File file = new File(pathName + filename);
        Table newTable = null;
        Scanner sc; 
        //foreign key aspects
        Boolean hasForeignKey = false;
        ArrayList<String> FKIndex; 
        String[] line; 
        String primaryTableName = "", primaryColName = "", colName = ""; 
        String name = "";

        try {
            sc = new Scanner(file);
        } catch(FileNotFoundException ex) {
            System.out.println("ERROR: file not found");
            return null;
        }

        //get table name - always first line
        if (sc.hasNextLine()){
            name = sc.nextLine();
        }

        FKIndex = getForeignKeyIndex(pathName);
        //loop over all rows in FKIndex, and check if table has a foreign key
        if(!FKIndex.isEmpty()){
            for(int i = 0; i < FKIndex.size(); i++){
                line = FKIndex.get(i).split("\\s"); 
                if (line[2].equals(name)){
                    hasForeignKey = true;
                    primaryTableName = line[0];
                    primaryColName = line[1];
                    colName = line[3];
                }
            }
        }

        //get col names - always second line
        if (sc.hasNextLine()){
            String colNamesString = sc.nextLine();
            //split colNames into individual words as Strings based on whitespace
            String[] colNames = colNamesString.split("\\s");

            //make table with column names
            if (hasForeignKey){
                newTable = new Table(name, primaryTableName, primaryColName, colName, true, colNames);
            } else {
                newTable = new Table(name, colNames);
            }

            //add rows from file (each line)
            while(sc.hasNextLine()){
                String newRowString = sc.nextLine();
                String[] newRow = newRowString.split("\\s");
                newTable.addRow(newRow);
            }
        }

        sc.close();
        return newTable;
    }

    //given a table, write it to a file
    public void writeTable(Table table, String pathName, String filename){

        Record tempRow; 
        String tempString;
        FileWriter fw;

        //create file
        try {
            fw = new FileWriter(pathName + filename);
        
            //add table name
            fw.write(table.getName());
            fw.write("\n");

            //add each row to a new line
            //get primary keys and loop over to add rows
            List<String> keys = table.getPrimaryKeys();
            for (int i = 0; i < keys.size(); i++){
                tempRow = table.getRow(keys.get(i));
                tempString = tempRow.getAllItemString();
                fw.write(tempString);
                fw.write("\n");
            }

            fw.close();
        } catch (IOException ex){
            System.out.println("ERROR: failed to create file " + filename);
        }
    }

    // ---------- Testing -----------
    public static void main(String[] args) {
        ReadWrite program = new ReadWrite();
        program.run();
        program.test();
    }

    // Run the tests
    private void run() {
        boolean testing = false;
        assert(testing = true);
        if (! testing) throw new Error("Use java -ea ReadWrite");
    }

    private void test(){
        testReadTable();
        testWriteTable();
        testIndex();
        testType();
        testForeignKeyIndex();
        System.out.println("ReadWrite: all tests passed");
    }

    private void testReadTable(){
        ReadWrite rw = new ReadWrite();
        Table test = rw.readTable("tests/","test.txt");

        assert(test.getName().equals("testTable"));
        assert(test.colSize() == 4);
        assert(test.rowSize() == 3);
        assert(test.contains("col1"));
        assert(test.contains("col2"));
        assert(test.contains("item1"));
        assert(test.contains("item8"));
        assert(test.contains("blah") == false);
    }

    private void testWriteTable(){
        ReadWrite rw = new ReadWrite();
        Table test = rw.readTable("tests/","test.txt");

        rw.writeTable(test, "tests/", "test2.txt");

        //show that test and test2 are the same (reading/writing does not alter information)
        Table test2 = rw.readTable("tests/", "test2.txt");
        assert(test.getName().equals(test2.getName()));

        //show all items are the same, compare keys and values
        //compare primary string lists
        List<String> keys1 = test.getPrimaryKeys();
        List<String> keys2 = test2.getPrimaryKeys();
        assert(keys1.equals(keys2));

        //compare rows based using primary key lists
        for (int i = 0; i < keys1.size(); i++){
            Record t1 = test.getRow(keys1.get(i));
            Record t2 = test2.getRow(keys1.get(i));
            //loop over record, and show each contains all elements of other
            for (int j = 0; j < t1.size(); j++){
                assert( t2.contains(t1.getItemAt(j).getValue()) );
                assert( t1.contains(t2.getItemAt(j).getValue()) );
            }
        }
    }

    private void testIndex(){
        ReadWrite rw = new ReadWrite();
        String testIndex = "testSetIndex";
        rw.setIndex(testIndex);

        ArrayList<String> dbNames = getIndex();
        assert(dbNames.contains(testIndex));

        rw.deleteIndex(testIndex);
        dbNames = getIndex();
        assert(dbNames.contains(testIndex) == false);
    }

    private void testType(){
        ReadWrite rw = new ReadWrite();
        Table test = rw.readTable("tests/","testType.txt");

        rw.writeTable(test, "tests/", "testType2.txt");

        //show that test and test2 are the same (reading/writing does not alter information)
        Table test2 = rw.readTable("tests/", "testType2.txt");
        assert(test.getName().equals(test2.getName()));

        //show all items are the same, compare keys and values
        //compare primary string lists
        List<String> keys1 = test.getPrimaryKeys();
        List<String> keys2 = test2.getPrimaryKeys();
        assert(keys1.equals(keys2));

        //compare rows based using primary key lists
        for (int i = 0; i < keys1.size(); i++){
            Record t1 = test.getRow(keys1.get(i));
            Record t2 = test2.getRow(keys1.get(i));
            //loop over record, and show each contains all elements of other
            for (int j = 0; j < t1.size(); j++){
                assert( t2.contains(t1.getItemAt(j).getValue()) );
                assert( t1.contains(t2.getItemAt(j).getValue()) );
            }
        }
    }

    private void testForeignKeyIndex(){
        ReadWrite rw = new ReadWrite();
        ArrayList<String> FKIndex;

        //test get index file
        FKIndex = rw.getForeignKeyIndex("tests/dummyForeignKeyDb");
        assert(FKIndex.get(0).equals("primaryTable1 primaryCol1 fkTable1 fkCol1"));
        assert(FKIndex.get(1).equals("primaryTable2 primaryCol2 fkTable2 fkCol2"));
        assert(FKIndex.get(2).equals("primaryTable3 primaryCol3 fkTable3 fkCol3"));

        //test set Foreign Key index in a dummy database folder
        setForeignKeyIndex("tests/dummyForeignKeyDb2", FKIndex);
        //show that the written file is as expected
        FKIndex = rw.getForeignKeyIndex("tests/dummyForeignKeyDb2");
        assert(FKIndex.get(0).equals("primaryTable1 primaryCol1 fkTable1 fkCol1"));
        assert(FKIndex.get(1).equals("primaryTable2 primaryCol2 fkTable2 fkCol2"));
        assert(FKIndex.get(2).equals("primaryTable3 primaryCol3 fkTable3 fkCol3"));
    }
}