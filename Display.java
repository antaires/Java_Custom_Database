/* Handles all aspects of display. Currently feeds to the console, 
but could be updated and replaced with a graphical display. Everything 
that appears in the console passes through this class. */
import java.util.*;

class Display {

    int colPrintWidth = 15;
    int tableNameLine = 20; 
    String length = "               "; //15 chars
    String nameLength = "   ";

    Display(int colPrintWidth){
        this.colPrintWidth = colPrintWidth;
    }

    public void start(){
        String start = "\nWELCOME TO THE DATABASE SYSTEM\n";
        System.out.println(start);
    }

    public void showDatabases(ArrayList<String> files){
        String start = "\nHere are the Databases: ";
        System.out.println(start);
        for (String file : files){
            System.out.println("> " + file);
        }
        System.out.println();
    }

    public void drawTableNames(ArrayList<String> tableNames){
        for (String name : tableNames){
            System.out.format("%-15.14s", "| " + name);
            for (int i = 0; i <= tableNameLine - name.length() - (15 - name.length()); i++){
                System.out.print(" ");
            }
            System.out.println("|");
        }
        System.out.print(" ");
        line(tableNameLine, "_");
        System.out.println();
    }

    public void drawName(String name){
        System.out.print(" ");
        line(tableNameLine, "_");
        System.out.println();
        System.out.print(name);
        System.out.println();
        System.out.print(" ");
        line(tableNameLine, "-");
    }

    private void line(int size, String seg){
        for (int i = 0; i < size; i ++){
            System.out.print(seg);
        }
        System.out.println();
    }

    public void drawTable(Table table){

        drawTableTop(table, "_");

        //draw line under column names
        drawRow(table.getColNames());
        System.out.print("|");
        System.out.println();
        drawLine(table, "-");

        //get primarykeys
        List<String> keys = table.getPrimaryKeys();
        for(int i = 1; i < keys.size(); i++){
            drawRow(table.getRow(keys.get(i)));
            System.out.print("|");
            System.out.println();
        }
        drawLine(table, "_");
    }

    public void drawRow(Record row){
        for (int i = 0; i < row.size(); i ++){
            System.out.format("%-15.14s", "| " + row.getItemAt(i).getValue());
        }
    }

    public void drawRowType(Record row){
        for (int i = 0; i < row.size(); i ++){
            System.out.format("%-15.14s", "| " + row.getItemAt(i).getType());
        }
    }

    public void drawType(Table table){
        drawTableTop(table, "_");
        drawRow(table.getColNames());
        System.out.print("|");
        System.out.println();
        drawLine(table, "-");

        List<String> keys = table.getPrimaryKeys();
        if (keys.size()>= 1){
            drawRowType(table.getRow(keys.get(1)));
            System.out.print("|");
            System.out.println();
        } 
        drawLine(table, "_");
    }

    private void drawLine(Table table, String lineSeg){
        System.out.print("|");
        for (int i = 0; i < (table.colSize() * length.length()-1); i++){
            System.out.print(lineSeg);
        }
        System.out.println("|");
    }

    private void drawTableTop(Table table, String lineSeg){
        System.out.println();
        System.out.print(" ");
        for (int i = 0; i < (table.colSize() * length.length()-1); i++){
            System.out.print(lineSeg);
        }
        System.out.println("");
        System.out.print("|");
        for (int i = 0; i < (table.colSize() * length.length()-1); i++){
            System.out.print(" ");
        }
        System.out.println("|");
        System.out.print("|");
        System.out.print(nameLength);
        System.out.print(table.getName());
        for (int i = 0; i < ((table.colSize() * length.length()-1) - nameLength.length() - table.getName().length()); i++){
            System.out.print(" ");
        }
        System.out.println("|");
        drawLine(table, "_");
    }

    public void newDbCreated(String name){
        System.out.println("new database " + name + " created");
    }

    public void dbDelted(String name){
        System.out.println("database " + name + " deleted");
    }

    public void invalid(){
        System.out.println("Invalid selection - please try again");
    }

    public void phrase(String phrase){
        System.out.println(phrase);
    }

    // ---------- Testing -----------
    public static void main(String[] args) {
        Display program = new Display(15);
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
        testDrawTable();
    }

    private void testDrawTable(){
        Display display = new Display(15);
        Table t1 = new Table("table1", "col1", "col2", "col3", "col4", "col5");
        t1.addRow("zero", "one", "two", "extra", "asdfsdfdf");      
        t1.addRow("three", "four", "five", "extra", "AccountingManager");      
        t1.addRow("six", "seven", "eight", "sdfsdf", "sdfsdsf"); 

        display.drawTable(t1);
    }
}