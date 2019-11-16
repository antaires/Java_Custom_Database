/* a class to store individual records (ROWS) that contains fields (ITEMS) 
Record class uses a Linked list to store Items. The first item value automatically 
becomes the primary key. The record class only depends on the Item class. The Record 
class can link two records together (to allow items to be accessed as columns), 
return its primary key, return the type of its items, add items and check types, 
remove items, and check if it contains a given value.
*/
import java.util.*;

class Record {

    private LinkedList<Item> row  = new LinkedList<Item>();  
    private Item primaryKey = new Item("\\"); //points to first item

    //first item is automatically taken as primary key
    Record(String...itemString){
        if (itemString.length == 0){return;}
        Item newItem; 
        for (String i : itemString){
            newItem = makeNewItem(i);
            row.add(newItem);
        }
        primaryKey = row.get(0);
    }

    public static Item makeNewItem(String i){
        //to deal with dividing type indicator from value
        String[] value; 
        Type type; 
        Item newItem; 
        //split string to extract type information- limits split to 2 strings
        //allows value to still contain regex % later on
        value = i.split("%", 2);

        //if no type declared, default to type string (used for column names)
        if (value[0].equals(i)){
            newItem = new Item(i, Type.STRING);
        } else {
            type = getType(value[0]);
            newItem = new Item(value[1], type);
        }
        return newItem;
    }

    private static String getType(Type type){
        String t; 
        switch(type){
            case INT: t = "i";
                break;
            case BOOL: t = "b";
                break;
            default: t = "s";
                break;
        }
        return t;
    }

    private static Type getType(String value){
        Type t;
        switch(value){
            case "i": t = Type.INT;
                break;
            case "b": t = Type.BOOL;
                break;
            default:
                t = Type.STRING;
        }
        return t;
    }

    public static void linkItems(Item top, Item bottom){
        top.setDown(bottom);
    }

    public static boolean link(Record top, Record bottom){
        if (top == null || bottom == null || top.size() == 0 || top.size() != bottom.size()){
            return false;
        }
        for(int i = 0; i < top.size(); i++){
            //sets the down pointer of the top Item to the bottom item
            Record.linkItems(top.getItemAt(i), bottom.getItemAt(i));
        }
        return true;
    }

    public String getPrimaryKey(){
        return primaryKey.getValue(); 
    }

    public int size(){
        return row.size();
    }

    public Item getItemAt(int index){
        if (index < 0 || index > row.size()){
            return null;
        } 
        return row.get(index);
    }

    public String getValueAt(int index){
        if (index >= 0 && index < row.size()){
            return getItemAt(index).getValue(); 
        } 
        return null;
    }

    public Item getLast(){
        return row.getLast();
    }

    public boolean updateItemAt(int index, String itemString){
        if (index >= 0 && index < row.size()){
            Type itemType = row.get(index).getType();
            if (Item.isValid(itemString, itemType)){
                Item updatedItem = new Item(itemString, itemType);
                //replaces item at that index with new one
                row.set(index, updatedItem);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public void insertItemAt(int index, String itemString){
        if (index >= 0 && index < row.size()){
            Item newItem = new Item(itemString);
            row.add(index, newItem);
        }     
    }

    public void insertItemAt(int index, String itemString, Type type){
        if (index >= 0 && index < row.size()){
            Item newItem = new Item(itemString, type);
            row.add(index, newItem);
        }     
    }

    public int getIndex(String itemString){
        for (int i = 0; i < row.size(); i++){
            if (itemString.equals(row.get(i).getValue())){
                return i; 
            }
        }
        return -1; 
    }

    public void remove(int index){
        row.remove(index);
    }

    public boolean contains(String itemString){
        if (row.size() == 0 || itemString == null){
            return false;
        }
        for (int i = 0; i < row.size(); i++){
            if (row.get(i).getValue() != null){
                if (row.get(i).getValue().equals(itemString)){
                    return true;
                }
            }
            
        }
        return false;
    }

    //get individual Items as a cloned list
    public LinkedList<Item> getItems(){
        LinkedList<Item> clone = new LinkedList<>(row);
        return clone;
    }

    //for use by ReadWrite
    public String getAllItemString(){
        String tempString = "";
        String type; 
        //loop over items in row, and add item.getValue to String
        for(Item item : row)
            if (item == null){
            } else {
                type = getType(item.getType());
                tempString = tempString + type + "%" + item.getValue() + " ";
            }
        return tempString;
    }

    //add new item to the end of the row list
    public void addItem(String itemString){
        //type checks inside makeNewItem
        Item newItem = makeNewItem(itemString);
        addItem(newItem);
    }

    public void addItem(Item item){
        //if first item added, set as primary key
        if (primaryKey.getValue().equals("\\")){
            primaryKey = item;
        }
        row.add(item);
    }

    public int indexOf(String itemValue){
        for (int i = 0; i < row.size(); i++){
            if (row.get(i).getValue().equals(itemValue)){
                return i;
            }
        }
        return -1; 
    }

    //returns a string with all values of the row separated by spaces
    public String getString(){
        String rowString = "";
        //loop over all items in row, and concat the item value to string
        for(int i = 0; i < row.size(); i++){
            if (i < row.size()-1){
                rowString = rowString + row.get(i).getValue() + " "; 
            } else {
                //prevent extra space being added 
                rowString = rowString + row.get(i).getValue(); 
            }
        }
        return rowString; 
    }

    // ---------- Testing -----------
    public static void main(String[] args) {
        Record program = new Record();
        program.run();
        program.test();
    }

    // Run the tests
    private void run() {
        boolean testing = false;
        assert(testing = true);
        if (! testing) throw new Error("Use java -ea Record");
    }

    private void test(){

        testContains();
        testGetIndex();
        testLink();
        testGetAllItemString();
        testPrimaryKey();
        testGetIndexOf();
        testType();
        testGetString();
        System.out.println("Record: All tests passed");
    }

    private void testContains(){
        Record t1 = new Record("one", "two", "three");

        assert( t1.contains("one") );
        assert( t1.contains("two") );
        assert( t1.contains("three") );
        assert( t1.contains("five") == false );
        assert( t1.contains("six") == false );
    }

    private void testGetIndex(){
        Record t1 = new Record("one", "two", "three");

        assert(t1.getIndex("one") == 0);
        assert(t1.getIndex("two") == 1);
        assert(t1.getIndex("three") == 2);
        assert(t1.getIndex("four") == -1);
    }

    public void testLink(){
        Record t1 = new Record("one", "two", "three");
        Record t2 = new Record("one", "two", "three");

        Item n1 = new Item("top");
        Item n2 = new Item("middle");

        assert(n1.getDown() == null);
        assert(n2.getDown() == null);
        n1.setDown(n2);
        assert(n1.getDown().equals(n2));

        Item n3 = new Item("bottom");
        assert(n2.getDown() == null);
        Record.linkItems(n2, n3);
        assert(n2.getDown().equals(n3));

        Record.link(t1, t2);
    }

    private void testGetAllItemString(){
        Record t1 = new Record("one", "two", "three");
        String test = t1.getAllItemString();

        //confirm values AND type indicators added
        assert(test.equals("s%one s%two s%three "));
    }

    private void testPrimaryKey(){
        Record t1 = new Record("one", "two", "three");
        assert(t1.getPrimaryKey().equals("one"));
    }

    private void testGetIndexOf(){
        Record t1 = new Record("one", "two", "three");
        assert(t1.indexOf("one") == 0);
        assert(t1.indexOf("two") == 1);
        assert(t1.indexOf("three") == 2);
        assert(t1.indexOf("four") == -1);
    }

    private void testType(){
        Record t1 = new Record("s%one", "i%234", "b%0", "notype");

        //check correct types stored for each item
        assert(t1.getItemAt(0).getType().equals(Type.STRING));
        assert(t1.getItemAt(1).getType().equals(Type.INT));
        assert(t1.getItemAt(2).getType().equals(Type.BOOL));
        assert(t1.getItemAt(3).getType().equals(Type.STRING));

        //confirm value is stored without type indicator
        assert(t1.getItemAt(0).getValue().equals("one"));
        assert(t1.getItemAt(1).getValue().equals("234"));
        assert(t1.getItemAt(2).getValue().equals("0"));
        assert(t1.getItemAt(3).getValue().equals("notype"));
    }

    private void testGetString(){
        Record t1 = new Record("s%one", "i%234", "b%0", "notype");

        String rowString = t1.getString();
        assert(rowString.equals("one 234 0 notype"));
    }
}