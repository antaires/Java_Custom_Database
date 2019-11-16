/* A class to handle the cell values in a table, as well as dealing with foreign key 
data and type constraints. Item objects are used by the Record class to construct
table rows. The Item object holds type information, and performs constraint checking
on INT, BOOL, and STRING types. Items may also store foreign key information, 
such as a pointer to the Item it references (this information is added by the database
class), and strings of the Table name and column name the foreign key references. 
Item class does not depend on any other classes.
*/

class Item {

    private String value;
    private Item down; 
    private Type type; 

    private boolean isForeignKey = false;
    private Item foreignKey = null;
    private String tableName;
    private String colName; 

    //default constructor used for column names
    Item(String value){
        this.value = value; 
        down  = null;
        this.type = Type.STRING; 
    }

    Item(String value, Type type){
        this.value = value; 
        down  = null;
        if (isValid(value, type)){
            this.type = type;
        } else {
            this.type = Type.STRING;
        }
    }

    //constructor for items that are foreign keys
    Item(String value, Type type, String tableName, String colName){
        this.value = value; 
        down  = null;
        if (isValid(value, type)){
            this.type = type;
        } else {
            this.type = Type.STRING;
        }
        isForeignKey = true; 
        this.tableName = tableName;
        this.colName = colName;
    }

    //the first time the database accesses the foreign key, it will set the pointer
    public void setForeignKey(Item foreignKey){
        this.foreignKey = foreignKey;
    }

    public Item getForeignKey(){
        return foreignKey; 
    }

    public Type getType(){
        return type; 
    }

    public String getForeignKeyTable(){
        return tableName;
    }

    public String getForeignKeyCol(){
        return colName;
    }

    public void setForeignKeyTable(String primaryTableName){
        isForeignKey = true;
        this.tableName =  primaryTableName;
    }

    public void setForeignKeyCol(String primaryColName){
        isForeignKey = true;
        this.colName = primaryColName; 
    }

    public boolean setValue(String value){
        //check type constraint
        if (isValid(value, this.type)){
            this.value = value;
            return true;
        }
        return false; 
    }  

    public static boolean isValid(String value, Type type){
        boolean valid = false;
        switch(type){
            case INT: 
                if (isNum(value)){
                valid = true;
                }
                //negative nums
                else if ( value.charAt(0) == '-' ){
                    String rest = value.substring(1, value.length()-1);
                    if (isNum(rest)){
                        valid = true;
                    }
                }
                break;
            case BOOL: 
                if (value.matches("^[0-1]")){
                valid = true;
                }
                break;
            case STRING:
                //accepts all values
                valid = true;
                break;
            default:
                valid = false;
                break;
        }
        return valid; 
    }

    private static boolean isNum(String value){
        String temp; 
        for(int i = 1; i < value.length(); i++){
            temp = value.substring(i-1, i);
            if (!temp.matches("^[0-9]")){
                return false;
            }
        }
        return true;
    }
    
    public String getValue(){
        return value; 
    }

    public void setDown(Item down_){
        this.down = down_;
    }

    public Item getDown(){
        if (down == null){
            return null;
        }
        return down; 
    }

    // ---------- Testing -----------
    public static void main(String[] args) {
        Item program = new Item("test");
        program.run();
        program.test();
    }

    // Run the tests
    private void run() {
        boolean testing = false;
        assert(testing = true);
        if (! testing) throw new Error("Use java -ea Item");
    }

    private void test(){
        //tests here
        testNullItem();
        testDown();
        testValue();
        testType();
        System.out.println("Item: all tests passed");
    }

    private void testNullItem(){
        Item newItem = new Item(null);

        assert(newItem.getValue() == null);
        assert(newItem.getDown() == null);
    }

    private void testDown(){
        Item n1 = new Item("one");
        Item n2 = new Item("two");

        assert(n1.getDown() == null);
        n1.setDown(n2);
        assert(n1.getDown().equals(n2));
    }

    private void testValue(){
        Item n1 = new Item("one");

        assert(n1.getValue().equals("one"));
        n1.setValue("two");
        assert(n1.getValue().equals("two"));
    }

    private void testType(){
        Item s = new Item("string", Type.STRING);
        Item b = new Item("0", Type.BOOL);
        Item i = new Item("979", Type.INT);

        assert(s.getType().equals(Type.STRING));
        assert(b.getType().equals(Type.BOOL));
        assert(i.getType().equals(Type.INT));

        //new strings can have any new values
        assert(s.getValue().equals("string"));
        assert(s.setValue("newString"));
        assert(s.getValue().equals("newString"));
        assert(s.setValue("234"));
        assert(s.getValue().equals("234"));

        //check bool value constraints
        assert(b.getValue().equals("0"));
        assert(b.setValue("1"));
        assert(b.setValue("a") == false);
        assert(b.setValue("9") == false);
        assert(b.getValue().equals("1"));

        //check int value constraints
        assert(i.getValue().equals("979"));
        assert(i.setValue("7"));
        assert(i.setValue("a213") == false);
        assert(i.setValue("apple") == false);
        assert(i.setValue("99wrong") == false);
        assert(i.getValue().equals("7"));

        //check negative numbers 
        Item n = new Item("-512", Type.INT);
        assert(n.getType().equals(Type.INT));
        assert(n.getValue().equals("-512"));
    }
}