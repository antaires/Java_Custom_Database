/* A class to wrap up the other classes into a working database The database 
class has an Array List of all of its tables. These tables have all the 
functionality of the tables. Added to this, the database performs foreign 
key constraint checking, and assures all tables added with foreign keys are 
legal (the other tables, values and columns exist). The database pulls in 
several other classes and uses them as components. Thus is has a display object
to show its tables, a Read Write object to access tables stored as files and 
to write out its tables and store the data (as well as type and foreign key 
information) between sessions. */
import java.util.*;

class Database {

    private Display display = new Display(20);
    private ReadWrite rw = new ReadWrite();
    private ArrayList<Table> tables = new ArrayList<Table>();
    //adds outer database folder to store all databases
    private String filePath = "databases/";
    private String name; 
    //foreign key fields
    private ArrayList<String> foreignKeyIndex; 
    //for help with checking foreign key constraints
    private ArrayList<String> tableInfo = new ArrayList<>(); 

    //the name of the database is used as the name of the folder containing all table txt files
    Database(String databaseName){
        this.name = databaseName;
        String[] line; 

        //add database name to index of db
        if (!rw.setIndex(name)){
            display.phrase("Db is not in index but did not write to index");
        }

        //access folder - if folder doesn't exist already, create it
        if (!rw.mkdirs(filePath + databaseName)){
            //if dir already exists, load all tables from files inside folder
            ArrayList<String> files = rw.getDirFiles(filePath + databaseName);

            //make new table for each table file in directory
            //ReadWrite checks FKIndex file, and automatically adds foreign key data to table Items
            for (String file : files){
                Table t = rw.readTable(filePath + databaseName + "/", file);
                if (t != null){
                    tables.add(t);
                }
            }

            //build list of table names and columns names as an array
            //to be used later to to check foreign key constraints
            buildTableInfo();

            //access foreignKeyIndex.txt file, and compile list of tables that contain foreign keys
            foreignKeyIndex = rw.getForeignKeyIndex(filePath + databaseName);
            //check all tables/columns in foreing key index exist, otherwise throw an error 
            if (foreignKeyIndex.size()!=0){
                for(int i = 0; i < foreignKeyIndex.size(); i++){
                    line = foreignKeyIndex.get(i).split("\\s");
                    
                    //check against each table - so every table name and column name must exist
                    Integer primaryTableCount = 0;
                    for(int j = 0; j < tableInfo.size(); j++){
                        if (tableInfo.get(j).contains(line[i])){
                            primaryTableCount++;
                        }
                    }

                    if (primaryTableCount != foreignKeyIndex.size()){
                        display.phrase("ERROR: table must exist to link to a foreign key");
                    }

                    //point each foreign key to the correct row
                    if (!linkForeignKeys(line[0], line[1], line[2], line[3])){
                        display.phrase("ERROR: foreign keys failed to link");
                    }
                }
            }
          
        } else {
            display.newDbCreated(name);
        }
    }

    //to make all table and column names easily checked for foreign key constraint
    private void buildTableInfo(){
        String tableString; 
        for (int i = 0; i < tables.size(); i++){
            //add name
            tableString = tables.get(i).getName();
            //add column names
            tableString = tableString + " " + tables.get(i).getColNamesString();
            //add to tableInfo array
            tableInfo.add(tableString);
        }
    }

    public String getName(){
        return name;
    }

    public int numTables(){
        return tables.size();
    }

    public Table getTable(String tableName){
        for (Table table : tables){
            if (table.getName().equals(tableName)){
                return table; 
            }
        }
        return null;
    }

    //generate a printout (using display) of all tables in db
    public void displayTables(){
        ArrayList<String> tableNames = new ArrayList<String>();
        display.drawName(name);
        for (Table table : tables){
            tableNames.add(table.getName());
        }
        display.drawTableNames(tableNames);
    }

    //update specific table
    public boolean addTableRow(String tableName, String...row){
        for (Table table : tables){
            if (table.getName().equals(tableName)){
                if (table.addRow(row)){
                    return true;
                }
            }
        }
        return false;
    }

    //add table
    public boolean addTable(String tableName, String...colNames){
        //keep table names unique
        for(Table t : tables){
            if (t.getName().equals(tableName)){
                return false;
            }
        }
        newTable(tableName, colNames);
        return true;
    }

    private void newTable(String tableName, String...colNames){
        Table newTable = new Table(tableName, colNames);
        tables.add(newTable);
    }

    //for adding tables with foreign keys
    public boolean addTable(String tableName, String primaryTable, String primaryCol, String foreignKeyCol, Boolean hasForeignKey, String...colNames){
        //keep table names unique
        for(Table t : tables){
            if (t.getName().equals(tableName)){
                return false;
            }
        }

        //foreign key constraint - if not table exists, abort
        if (foreignKeyCheck(primaryTable, primaryCol)){
            newTable(tableName, primaryTable, primaryCol, foreignKeyCol, true, colNames);

            //link foreign key to primary table
            if (!linkForeignKeys(primaryTable, primaryCol, tableName, foreignKeyCol)){
                display.phrase("ERROR: add table failed to link foreign keys");
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean foreignKeyCheck(String primaryTable, String primaryCol){
        Boolean isValid = false;
        buildTableInfo(); //make sure this is up-to-date
        //confirm table and column exist
        for(int j = 0; j < tableInfo.size(); j++){
            if (tableInfo.get(j).contains(primaryTable) && tableInfo.get(j).contains(primaryCol)){
                isValid = true;
            }
        }
        return isValid;
    }

    //for tables with foreign keys
    private void newTable(String tableName, String primaryTable, String primaryCol, String foreignKeyCol, Boolean hasForeignKey, String...colNames){
        Table newTable = new Table(tableName, primaryTable, primaryCol, foreignKeyCol, true, colNames);
        tables.add(newTable);
    }

    private boolean linkForeignKeys(String primaryTable, String primaryCol, String FKTable, String FKCol){
        Table fkTable = getTable(FKTable);
        Table pTable = getTable(primaryTable);
        List<Item> fkcolList = fkTable.getColumnNames();
        List<Item> pCol = pTable.getColumnNames();
        int linkCount = 0;

        //link row items by value (should match)
        for (int i = 0; i < fkcolList.size(); i++){
            for (int j = 0; j < pCol.size(); j++){

                //if values match: link 
                if (fkcolList.get(i).getValue().equals(pCol.get(j).getValue())){
                    fkcolList.get(i).setForeignKey(pCol.get(j));
                    linkCount++;
                }

                //if values are column names: link
                if (fkcolList.get(i).getValue().equals(FKCol) &&
                    pCol.get(j).getValue().equals(primaryCol) ){
                        fkcolList.get(i).setForeignKey(pCol.get(j));
                        linkCount++;
                }
            }
        }
        //confirm all links successful
        if (linkCount != fkcolList.size()){
            return false;
        }
        return true;
    }

    //removes table from table list and file associated with table (if it exists)
    public boolean removeTable(String tableName){
        for (int i = 0; i < tables.size(); i++){
            if (tables.get(i).getName().equals(tableName)){
                tables.remove(i);
                //if file exists, delete it
                ArrayList<String> files = rw.getDirFiles(filePath + name);
                for (String file : files){
                    if ((file).equals(tableName + ".txt")){
                        if (rw.removeFile(filePath + name + "/", file)){
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    //save all tables to files
    //convention - all file names are tableName.txt 
    public boolean writeDb(){
        if (tables.size() == 0){return true;}
        //write or overwrite all tables to file
        for (Table table : tables){
            rw.writeTable(table, filePath + name + "/", table.getName() + ".txt");
        }
        //confirm table has been written 
        ArrayList<String> files = rw.getDirFiles(filePath + name);

        //loop over table names, and show that files contains each table name
        for (Table table : tables){
            if (!files.contains(table.getName() + ".txt")){
                return false;
            }
        }
        writeForeignKeyIndex();
        return true;
    }

    private void writeForeignKeyIndex(){
        ArrayList<String> FKIndex = new ArrayList<>();
        String tempLine;
        String primaryTable, primaryCol, FKTable, FKCol; 
        //generate FKIndex:
        //loop over tables, and if it is a foreign key, 
        for (Table t : tables){
            if (t.hasForeignKey()){
                FKTable = t.getName();
                FKCol = t.getForeignKeyCol();
                primaryTable = t.getPrimaryTable();
                primaryCol = t.getPrimaryCol();
                FKIndex.add(primaryTable + " " + primaryCol + " " + FKTable + " " + FKCol);
            }
        }
        rw.setForeignKeyIndex(filePath + name, FKIndex);
    }

    //remove all tables and folders associated with database
    public boolean deleteDatabase(){

        if (tables.size() > 0){
            for (int i = tables.size()-1; i >= 0; i--){
                removeTable(tables.get(i).getName());
            }
        }

        //remove db from index
        rw.deleteIndex(name);

        //remove FKIndex file
        rw.removeFile(filePath+name, "/FKIndex.txt");

        //remove folder for db
        if (rw.deleteDirs(filePath + name)){
            display.dbDelted(name);
            return true;
        }
        return false;
    }

    // ---------- Testing -----------
    public static void main(String[] args) {
        Database program = new Database("main");
        program.run();
        program.deleteDatabase();
        program.test();
        //clean up folder system after tests
        program.deleteDatabase();
    }

    // Run the tests
    private void run() {
        boolean testing = false;
        assert(testing = true);
        if (! testing) throw new Error("Use java -ea Database");
    }

    private void test(){
        testConstruct();
        testAddTable();
        testAddRow();
        testRemoveTable();
        testDeleteDatabase();
        testWriteDb();
        testAddDB();
        testForeignKeys();
    }

    private void testConstruct(){
        Database db = new Database("test");
        ReadWrite rw = new ReadWrite();
        rw.mkdirs("databases/test");
        //returns false when folder already exists
        assert(rw.mkdirs("databases/test") == false);
        db.deleteDatabase();        
    }

    private void testAddTable(){
        Database db = new Database("testAdd");
        assert(db.numTables() == 0);
        assert( db.addTable("testAdd", "col1", "col2", "col3") );
        assert(db.numTables() == 1);
        assert( db.addTable("testAdd2", "col1", "col2", "col3", "col4") );
        assert(db.numTables() == 2);
        //cannot add table with same name as existing table
        assert(db.addTable("testAdd", "col1") == false);
        assert(db.numTables() == 2);
        db.deleteDatabase();
    }

    private void testAddRow(){
        Database db = new Database("testAddRow");
        assert( db.addTable("testAdd", "col1", "col2", "col3") );

        //table name must match existing table
        assert( db.addTableRow("notATable", "row1", "row", "row") == false);

        assert( db.addTableRow("testAdd", "row1", "row", "row") );
        Table testAdd = db.getTable("testAdd");
        assert(testAdd.contains("row"));  
        assert(testAdd.containsKey("row1"));  
        db.deleteDatabase();    
    }

    private void testRemoveTable(){
        Database db = new Database("testRemoveTable");
        assert( db.numTables() == 0 );
        assert( db.addTable("testRemoveTable", "col1", "col2", "col3") );
        assert( db.numTables() == 1 );
        assert( db.removeTable("testRemoveTable"));
        assert( db.numTables() == 0 );
        db.deleteDatabase();
    }

    private void testDeleteDatabase(){
        Database db = new Database("testDeleteDb");
        //create db with 1 table file
        assert( db.numTables() == 0 );
        assert( db.addTable("testTable", "col1", "col2", "col3") );
        assert( db.numTables() == 1 );

        assert( db.deleteDatabase() );
    }

    private void testWriteDb(){
        Database db = new Database("testWrite");
        db.addTable("testWrite1", "col1", "col2", "col3");
        db.addTable("testWrite2", "col1", "col2", "col3", "col4");
        db.addTable("testWrite3", "col1");
        assert(db.numTables() == 3);

        assert( db.writeDb() );
        db.deleteDatabase();
    }

    private void testAddDB(){
        Database db = new Database("SpaceTravel");
        db.addTable("Fleet", "ship-id", "shipType", "year", "carryingCapacity", "sector-id");
        db.addTable("Crew", "crew-id", "name", "title", "yearsExperience", "ship-id");
        db.addTable("Captains", "ship-id", "crew-id", "mission");
        db.addTable("StarBase", "base-id", "name", "capacity", "sector-id");
        db.addTable("Sectors", "sector-id", "name", "population", "government");
        Table fleet = db.getTable("Fleet");
        fleet.addRow("121", "ColonyShip", "2050", "50000");
        fleet.addRow("234", "Cargo", "2015", "150");
        fleet.addRow("513", "Cruiser", "2070", "25");
        Table crew = db.getTable("Crew");
        crew.addRow("455", "Sally-Ride", "Engineer", "25", "513");
        crew.addRow("001", "Laika", "doggo", "1", "121");
        crew.addRow("255", "Buzz", "Pilot", "10", "513");
        crew.addRow("243", "Ona", "Mechanic", "5", "234");
        crew.addRow("222", "Valentina-Tereshkova", "Pilot", "8", "234");
        Table captains = db.getTable("Captains");
        captains.addRow("513", "455");
        captains.addRow("234", "222");
        captains.addRow("121", "001");
        Table starbase = db.getTable("StarBase");
        starbase.addRow("0986", "Farout", "500000", "2314");
        starbase.addRow("5239", "Titan", "30000", "1302");
        Table sectors = db.getTable("Sectors");
        sectors.addRow("2314", "Omega", "760", "Klingon");
        sectors.addRow("1302", "Gas", "1200", "USA");

        db.writeDb();
 
        Database db2 = new Database("Dinosaurs");
        db2.addTable("Regions", "name");
        db2.addTable("Period", "Triassic", "Jurrasic", "Cretaceous");
        db2.addTable("Type", "name", "id", "size");

        db2.writeDb();
    }

    private void testForeignKeys(){
        //create a new database
        Database db = new Database("Robots");

        //add table1
        db.addTable("Robot", "s%id", "s%year", "s%function");
        db.addTableRow("Robot", "i%1", "i%2016", "s%spaceExploration");
        db.addTableRow("Robot", "i%2", "i%2019", "s%vacuum");
        assert(db.getTable("Robot").hasForeignKey() == false);

        //add table2, which has foreign keys to table1
        db.addTable("Seller", "Robot", "id", "product", true, "s%id", "s%product" );
        db.addTableRow("Seller", "i%34", "i%1");
        db.addTableRow("Seller", "i%55", "i%2");

        //assert(db.hasForeignKey());
        assert(db.getTable("Seller").hasForeignKey());

        db.writeDb();
    }
}
