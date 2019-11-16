/*A table is a collection of records (ROWS), all having the same number of fields, in 
the same order, with the same names. Each column has the same type. It uses a Linked 
Hash Map to store rows, and can easily access the last row added. Primary 
keys serve as the keys in the hash map, and are guaranteed to be unique. Columns are
stored via a pointer to the column name Item, using a hash map (the pointers prevent 
duplication of data) and the data structure further assures that the column row names 
are unique. This column structure, combined with the downward pointers in the Item 
class, allow for a single column to be easily extracted from a table. The table also 
stores if any of its columns are foreign key columns, and if so, stores the data needed 
to easily check this information is accurate.*/
import java.util.*;

import javax.xml.crypto.KeySelector;

class Table {

    private String name; 
    private Record colNames = new Record();
    //used to quickly link columns vertically
    private Record lastAdded;
    private LinkedHashMap<String, Record> rows = new LinkedHashMap<String, Record>();
    //allows for vertical searching of columns
    private HashMap<String, Item> cols = new HashMap<String, Item>(); 
    private boolean hasForeignKey = false;
    private String foreignKeyCol, primaryTable, primaryCol;

    Table(String name, String...columnNames){
        this.name = name; 
        Item tempItem; 
        if (columnNames.length == 0){return;}

        for (String i : columnNames){
            //account for type indicator and value
            tempItem = Record.makeNewItem(i);
            //keep column names unique
            if (!colNames.contains(tempItem.getValue())){
                colNames.addItem(tempItem);
            }
        }
        rows.put(colNames.getPrimaryKey(), colNames);
        lastAdded = colNames;

        //set up col hash
        for (int i = 0; i < colNames.size(); i++){
            cols.put(colNames.getItemAt(i).getValue(), colNames.getItemAt(i));
        }
    }

    //special constructor for tables with foreign keys, used by Database class to add tables with them
    Table(String name, String primaryTable, String primaryCol, String foreignKeyCol, Boolean hasForeignKey, String...columnNames){
        this.name = name; 
        this.hasForeignKey = true;
        this.foreignKeyCol = foreignKeyCol;
        this.primaryTable = primaryTable;
        this.primaryCol = primaryCol;
        Item tempItem; 
        if (columnNames.length == 0){return;}

        for (String i : columnNames){
            //account for type indicator and value
            tempItem = Record.makeNewItem(i);
            //if column is foreign key col, add fKey data to item
            if (i.equals(foreignKeyCol)){
                tempItem.setForeignKeyTable(primaryTable);
                tempItem.setForeignKeyCol(primaryCol);
            }
            //keep column names unique
            if (!colNames.contains(tempItem.getValue())){
                colNames.addItem(tempItem);
            }
        }
        rows.put(colNames.getPrimaryKey(), colNames);
        lastAdded = colNames;

        //set up col hash
        for (int i = 0; i < colNames.size(); i++){
            cols.put(colNames.getItemAt(i).getValue(), colNames.getItemAt(i));
        }
    }

    public String getName(){
        return name;
    }

    public Item getItem(String primaryKey, String colName){
        //use colName to get index of col, then return that Item from Record
        int colIndex = colNames.indexOf(colName);
        if (colIndex < 0){
            return null; 
        }

        //use primary key to get correct Record from rows
        Record temp = rows.get(primaryKey);
        if (temp == null){
            return null;
        }
        
        return temp.getItemAt(colIndex);
    }

    public boolean updateItem(String primaryKey, String colName, String newItemString){
        Item item = getItem(primaryKey, colName);
        if (item == null){
            return false;
        }

        item.setValue(newItemString);
        return true;
    }

    public int colSize(){
        return colNames.size();
    }

    public int rowSize(){
        return rows.size(); 
    }

    public List<Item> getColumnNames(){
        return colNames.getItems();
    }

    public Record getColNames(){
        return colNames;
    }

    public String getColNamesString(){
        return colNames.getString();
    }

    public List<String> getPrimaryKeys(){
        List<String> keys = new ArrayList<>(rows.keySet());
        return keys;
    }

    public Collection<Record> getValues(){
        return rows.values();
    }

    //select a row (perhaps by row number) -- will change to primary key
    public Record getRow(String primaryKey){    
        return rows.get(primaryKey);
    }

    public boolean hasForeignKey(){
        return hasForeignKey;
    }

    //insert a row to end ONLY IF new row length matches current number of columns
    //point the items at the item added below it (to allow easy column-access)
    public boolean addRow(String...itemStrings){

        //check that primray key is unique
        if (rows.containsKey(itemStrings[0])){
            return false;
        }

        if (itemStrings.length != colNames.size()){
            return false;
        }

        Item newItem; 
        Record newRow = new Record();
        for (String i : itemStrings){

            //type check
            newItem = Record.makeNewItem(i);

            if (hasForeignKey){
                if (newItem.getValue().equals(foreignKeyCol)){
                    //add additional foreign key info to item
                    newItem.setForeignKeyTable(colNames.getItemAt(colNames.getIndex(foreignKeyCol)).getForeignKeyTable());
                    newItem.setForeignKeyCol(colNames.getItemAt(colNames.getIndex(foreignKeyCol)).getForeignKeyCol());
                }
            } 
            newRow.addItem(newItem);
        }
        if (addRow(newRow)){
            return true;
        }
        return false;
    }

    public String getForeignKeyCol(){
        return foreignKeyCol;
    }

    public String getPrimaryTable(){
        return primaryTable;
    }

    public String getPrimaryCol(){
        return primaryCol;
    }

    private boolean addRow(Record newRow){
        if (newRow.size() == colNames.size()){
            //link newRow to last record in rows
            if (rows.keySet().size() != 0){
                //skip linking if its the first row to be added
                Record.link(lastAdded, newRow);
                lastAdded = newRow;
            }
            if (rows.put(newRow.getPrimaryKey(), newRow) == null){
                return true;
            }
        } 
        return false;
    }

    public boolean deleteRow(String primaryKey){
        //cannot delete column row
        if (colNames.getPrimaryKey().equals(primaryKey)){
            return false;
        }
        if (rows.remove(primaryKey) != null){
            return true;
        }
    
        return false;
    }

    //update a row - may only be done as a full unit (correct number of items)
    //may update column names if enter index 0
    public boolean updateRow(String primaryKey, String...itemStrings){
        if ( !rows.containsKey(primaryKey) || itemStrings.length != colSize() ){
            return false;
        }

        int cnt = 0;
        //update all values in row
        for (String i : itemStrings) {
            //get specific row and upate each element in it
            rows.get(primaryKey).updateItemAt(cnt++, i);
        }
        return true;
    }

    public Record getCol(String colName){
        Record column = new Record();
        Item top = cols.get(colName);
        if (top == null){
            return null;
        }
        column.addItem(top);
        while(top.getDown() != null){
            column.addItem(top.getDown());
            top = top.getDown();
        }
        return column;
    }

    public boolean addCol(String colName){
        //must be unique name
        if (colNames.contains(colName)){
            return false;
        }

        //add new column
        Item newCol = new Item(colName);
        colNames.addItem(newCol);

        //add pointer to new column to cols hashMap
        cols.put(colName, newCol);

        //use ordered list of keys to link columns in same order as rest of table
        List<String> keys = new ArrayList<>(rows.keySet());

        //add an item (value null) all down the new column
        for(int i = 1; i < rows.size(); i++){
            Item nullItem = new Item(null);

            //add new item to row under new column
            rows.get(keys.get(i)).addItem(nullItem);

            //link items (the last items added to row and Item above it in new col)
            newCol.setDown(rows.get(keys.get(i)).getLast());
            rows.get(keys.get(i)).addItem(nullItem);
            newCol.setDown(rows.get(keys.get(i)).getLast());

            newCol = newCol.getDown();
        }

        return true;
    }

    //add multiple columns at once
    public void addCol(String...colNames){
        for (String i : colNames){
            addCol(i);
        }
    }

    //add column name to row of column names
    public boolean addColBefore(String beforeCol, String colName){
        
        //get index of beforeCol
        int index = colNames.getIndex(beforeCol);
        if (index == -1){
            return false;
        }

        //get ordered list of primary keys
        List<String> keys = new ArrayList<>(rows.keySet());

        colNames.insertItemAt(index, colName);

        //add Items to new column, with value of null
        for (int i = 1; i < rows.size(); i++){
            rows.get(keys.get(i)).insertItemAt(index, null);
        }
        //link up new Items going down column
        for (int i = 0; i < colSize() - 2; i++){
            Record.linkItems(rows.get(keys.get(i)).getItemAt(index), rows.get(keys.get(i)).getItemAt(index+1) );
        }

        //add column to cols map
        int newIndex = colNames.getIndex(colName);
        cols.put(colName, colNames.getItemAt(newIndex));
        return true;
    }

    //update all values in a column at once (not including colName)
    public boolean updateCol(String colName, String...newValues){
        int colIndex = colNames.getIndex(colName);
        if (colIndex < 0 || newValues.length != rowSize()-1){
            return false;
        }

        //start at first row of values and update values down the row
        Item currItem = cols.get(colName);
        for (String i : newValues){
            currItem.setValue(i);
            currItem = currItem.getDown();
        }
        return true;
    }

    public void clearTable(){
        //deletes all records, but preserves column names and table name
        List<String> keys = new ArrayList<>(rows.keySet());
        int cnt = rows.size()-1;

        while(rows.size() > 1){
            rows.remove(keys.get(cnt--));
        }
        
    }

    public void updateTable(String[]...itemStringList){
        //add a collection of lists - adding many rows of information at once
        for (String[] itemString : itemStringList){
            addRow(itemString);
        }
    }

    public void clearUpdateTable(String[]...itemStringList){
        //replaces all information with new collection 
        //each row in list is added as a new row in table
        clearTable();
        updateTable(itemStringList);
    }

    //remove a specific column from a table
    public boolean removeCol(String colName){
        int index = colNames.indexOf(colName);
        if(index == -1){
            return false;
        }

        //remove from cols
        cols.remove(colName);

        //remove all Items from rows down the column
        List<String> keys = new ArrayList<>(rows.keySet());
        for (int i = 0; i < rows.size(); i++){
            rows.get(keys.get(i)).remove(index);
        }
        return true;
    }

    public boolean contains(String itemString){
        //currently checks column names too
        //go through all items in hashmap
        Collection<Record> temp = rows.values();

        for (Record r : temp){
            if( r.contains(itemString) ){
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(String keyString){
        if (rows.containsKey(keyString)){
            return true;
        }
        return false;
    }

    // ---------- Testing -----------
    public static void main(String[] args) {
        Table program = new Table("test");
        program.run();
        program.test();
    }

    // Run the tests
    private void run() {
        boolean testing = false;
        assert(testing = true);
        if (! testing) throw new Error("Use java -ea Table");
    }

    private void test(){
        testConstruct();
        testAddRow();
        testDeleteRow();
        testUpdateRow();
        testGetColAt();
        testAddDeleteCol();
        testUpdateItem();
        testUpdateCol();
        testClearTable();
        testUpdateTable();
        testForeignKeys();
        System.out.println("Table: all tests passed.");
    }

    private void testConstruct(){
        Table t1 = new Table("t1", "one", "two");
        Table t2 = new Table("t2", "one", "one", "two", "three");

        //test name
        assert(t1.getName().equals("t1"));
        assert(t2.getName().equals("t2"));

        //to show that only unique column names are accepted
        assert(t1.colSize() == 2);
        assert(t2.colSize() == 3);
        
        assert(t1.containsKey("one"));
        assert(t2.containsKey("one"));
    }

    private void testAddRow(){
        Table t1 = new Table("table1", "one", "two");

        //only rows of same size as column names may be added to table
        assert(t1.rowSize()== 1);

        assert(t1.addRow("row1", "two"));
        assert(t1.containsKey("row1")); 
        assert(t1.colSize() == 2);
        assert(t1.rowSize() == 2);

        //must be correct size to be added
        assert(t1.addRow("row2") == false);
        assert(t1.addRow("row2", "two", "three") == false);
    }

    private void testDeleteRow(){
        Table t1 = new Table("t1", "one", "two");
        t1.addRow("three", "four");

        assert(t1.rowSize() == 2);
        assert(t1.deleteRow("three"));
        assert(t1.rowSize() == 1);
    }

    private void testUpdateRow(){
        Table t1 = new Table("t1", "one", "two");
        t1.addRow("three", "four");

        assert(t1.contains("three"));
        assert(t1.contains("one"));
        assert(t1.updateRow("three", "bat", "hat"));
        assert(t1.contains("three") == false);
        assert(t1.contains("bat"));
        assert(t1.updateRow("three", "one", "two", "three") == false);
    }

    private void testGetColAt(){
        Table t1 = new Table("table1", "zeroCol", "oneCol", "twoCol");
        t1.addRow("zero0", "one", "two"); 
        t1.addRow("zero1", "one", "two"); 
        t1.addRow("zero2", "one", "two"); 

        Record colZero = t1.getCol("zeroCol");

        assert(colZero.size() == 4);
        assert(colZero.contains("zero0"));
        assert(colZero.contains("one") == false);
        assert(colZero.contains("two") == false);

        Record colOne = t1.getCol("oneCol");
        assert(colOne.size() == 4);
        assert(colOne.contains("one"));
        assert(colOne.contains("zero") == false);
        assert(colOne.contains("two") == false);
    }

    private void testAddDeleteCol(){
        Table t1 = new Table("t1", "zeroCol", "oneCol", "twoCol");
        t1.addRow("zero0", "one", "two"); 
        t1.addRow("zero1", "one", "two"); 

        assert(t1.colSize() == 3);
        t1.addCol("newCol");
        assert(t1.colSize() == 4);
        //test correct linking
        Record newCol = t1.getCol("twoCol");
        assert(newCol.size() == 3);

        //test incorrect column name returns null
        assert(t1.getCol("notAColumnName") == null);

        //test adding columns
        t1.addCol("n2", "n3", "n4");
        assert(t1.colSize() == 7);
        List<Item> allColNames = t1.getColumnNames();
        assert(allColNames.get(0).getValue().equals("zeroCol"));
        assert(allColNames.get(2).getValue().equals("twoCol"));
        assert(allColNames.get(3).getValue().equals("newCol"));
        assert(allColNames.get(6).getValue().equals("n4"));

        //test linking is correct
        Record newCol2 = t1.getCol("n4");
        assert(newCol2.size() == 3);

        //test removing columns
        assert(t1.contains("one"));
        t1.removeCol("oneCol");
        assert(t1.colSize() == 6);
        assert(t1.contains("one") == false);
    }

    private void testUpdateItem(){
        Table t1 = new Table("t1", "zeroCol", "one", "two");
        t1.addRow("zero", "one", "two"); 

        assert(t1.getItem("zero", "zeroCol").getValue().equals("zero"));
        t1.updateItem("zero", "zeroCol", "newItem");
        assert(t1.getItem("zero", "zeroCol").getValue().equals("newItem"));
    }

    private void testUpdateCol(){
        Table t1 = new Table("t1", "zeroCol", "oneCol", "twoCol");
        t1.addRow("zero0", "one", "two");      
        t1.addRow("zero1", "one", "two");      
        t1.addRow("zero2", "one", "two");   
        
        assert(t1.updateCol("twoCol", "new", "new", "new"));
        assert(t1.contains("new"));
        assert(t1.getItem("zero0", "new").getValue().equals("new"));

        assert(t1.updateCol("twoCol", "wrong", "wrong", "wrong", "wrong") == false);
        assert(t1.updateCol("twoCol", "wrong", "wrong") == false);
        assert(t1.updateCol("notACol", "wrong", "wrong", "wrong") == false);
        assert(t1.updateCol("alsoNotACol", "wrong", "wrong", "wrong") == false);

        assert(t1.updateCol("zeroCol", "ha", "ha", "ha")); 
        assert(t1.contains("ha"));
        //rename column heading
        t1.updateItem("zero0","zeroCol","HA");
        assert(t1.contains("zero0") == false);
    }

    private void testClearTable(){
        Table t1 = new Table("t1", "col1", "col2", "col3");
        t1.addRow("zero", "one", "two");      
        t1.addRow("three", "four", "five");      
        t1.addRow("six", "seven", "eight"); 
        
        assert(t1.contains("zero"));
        assert(t1.contains("four"));
        assert(t1.contains("eight"));

        t1.clearTable();

        assert(t1.contains("zero") == false);
        assert(t1.contains("four") == false);
        assert(t1.contains("eight") == false);
        //column names kept
        assert(t1.contains("col1"));
    }

    private void testUpdateTable(){
        Table t1 = new Table("t1", "col1", "col2", "col3");
        String[] oldRow1 = {"zero", "one", "two"};
        String[] oldRow2 = {"three", "four", "five"};
        t1.addRow(oldRow1); 
        t1.addRow(oldRow2); 

        String[] row1 = {"n1", "n2", "n3"};
        String[] row2 = {"n4", "n5", "n6"};
        String[] row3 = {"n7", "n8", "n9"};
        String[][] newRows = {row1, row2, row3};

        t1.clearUpdateTable(newRows);
        //show table cleared
        assert(t1.contains("zero") == false);
        assert(t1.contains("one") == false);
        assert(t1.contains("five") == false);
        //show table updated
        assert(t1.contains("n1"));
        assert(t1.contains("n5"));
        assert(t1.contains("n9"));

        String[][] oldRows = {oldRow1, oldRow2};
        t1.updateTable(oldRows);
        //old data kept
        assert(t1.contains("n1"));
        assert(t1.contains("n5"));
        assert(t1.contains("n9"));
        //new data added
        assert(t1.contains("zero"));
        assert(t1.contains("one"));
        assert(t1.contains("five"));
    }

    private void testForeignKeys(){
        Table t1 = new Table("Seller", "Robot", "id", "product", true, "s%id", "s%product" );
        t1.addRow("i%1", "i%2");
        assert(t1.hasForeignKey());
        assert(t1.getPrimaryTable().equals("Robot"));
        assert(t1.getPrimaryCol().equals("id"));
        assert(t1.getForeignKeyCol().equals("product"));
    }
    
}