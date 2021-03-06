/*
        Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
        Class: CS 4308 W01
        University: Kennesaw State University
        Professor: Dr. Jose Garrido
        Date: April 28th, 2020


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
*/
import java.io.File;
import java.util.ArrayList;

public class Parser {
    
    private Scanner scanner;                        //scanner object
    private boolean EOF;                            //end of file
    private int nextTok;                            //most recent token from scanner
    private String nextLex;                         //most recent lexeme from scanner
    protected ArrayList<Identifier> idTable;        //table to store identifiers as they're created. Also serves as memory at execution time
    private final ArrayList<String> wsal;           //ArrayList where current statement is being built (working statement array list)
    private int activeIdentifier;                   //Stores the index in idTable of the most recent identifier that has been called using lookup()
    private String activeIdentifierName;            //for use in declaration of identifiers at execution time
    private AssignmentStatement assignmentStatement;//object used as arbitrary reference for new assignment statements
    private final File f;                           //File object
    
    private java.util.Scanner inputHandler;
    
    boolean running = false;
    private boolean displayParsedLines = true;
    private boolean whileSkip = false;
    
    //Constructor:
    public Parser(String in){
        new Constants();
        f = new File(in);
        inputHandler = new java.util.Scanner(System.in);
        this.nextLex = "";
        this.idTable = new ArrayList();
        this.EOF = false;
        this.scanner = new Scanner(f);
        this.wsal = new ArrayList();
        
    }

    /**
     * Calls scanner, stores next token and lexeme
     */
    public void scan(){
        nextTok = scanner.nextToken();
        nextLex = scanner.currentLexeme;
    }

    /**
     * @return End of File
     */
    public boolean endOfFileReached(){      //public access to private end of file boolean
        return EOF;
    }

    /**
     * Prints Error Messages
     * @param str   error method
     * @param loc   row
     * @param skip  skip to next line
     */
    private void error(String str, int loc, boolean skip){
        System.err.println(str+"; line "+loc);
        if (skip){
            int x = scanner.getRow();
            while (x == scanner.getRow()){
                scan();
            }
        }
    }

    /**
     * Prints complete ID table during testing
     */
    public void printIdTable(){
        idTable.forEach((i) -> {
            System.out.println(i.toString());
        });
    }

    /**
     * Searches identifier table for string
     * @param str   string to look up
     * @return      true if string exists in identifier table
     */
    private boolean lookup(String str){
        activeIdentifierName = nextLex;
        for(Identifier id : idTable){
            if (str.compareTo(id.getName()) == 0){
                //active identifier is set to index of most recently referenced identifier
                //used for assignments
                activeIdentifier = idTable.indexOf(id);
                return true;
            }
        }
        return false;
    }

    /**
     * Prints the statement that has been built
     */
    public void flush(){
        if (running)
            displayParsedLines = false;
        if (!displayParsedLines){
            wsal.clear();
            return;
        }
        String out = "";
        for (String s : wsal){
            out = out.concat(s+" ");
        }
        System.out.println(out);
        wsal.clear();
    }

    /**
     *
     * @param out
     */
    public void output(String out){
        String s = out.replace("\"", "");
        System.out.print(s);
    }

    //Parsing Functions:

    /**
     * function parses for an entire function
     * Derived BNF for this method:
     *      FUNCTION IDENTIFIER IS
     *      ( | CONSTANTS data_declarations) VARIABLES data_declarations (  | STRUCT data_declarations)
     *      BEGIN pactions ENDFUN IDENTIFIER
     *
     */
    public void parseProgram(){
        scan();
        if (nextTok == Constants.FUNCTION){
            wsal.add(nextLex);
            scan();
            if (nextTok == Constants.MAIN){       //BNF subset supports 'main' keyword as function name, not identifiers
                wsal.add(nextLex);
                scan();
                if (nextTok == Constants.IS){
                    wsal.add(nextLex);
                    flush();
                    scan();
                    if (nextTok == Constants.VARIABLES){      //nested ifs were used because syntax is rigid
                        wsal.add(nextLex);      
                        flush();
                        scan();
                        variables();                  //all variable declaration statements here
                    } 
                    if (nextTok == Constants.BEGIN){
                        wsal.add(nextLex);
                        flush();
                        scan();
                        begin();           //all function body statements here
                        endfun();           
                    } else {
                        error("Must begin statement declaration list with 'begin' keyword", scanner.getRow(), false);
                    }
                }
                else error("Invalid Function Declaration", scanner.getRow(), false);
            }
            else error("Function name must be 'main' keyword", scanner.getRow(), false);
        }
        else {
            error("Must contain main function", scanner.getRow(), false);
        }
        if (nextTok == Constants.EOF){          //After parsing for a function, method checks for end of file
            EOF = true;
        }
    }
    
    //Declaration Parsing

    /**
     * Parses for variable declaration statements
     * comp_declare -> DEFINE data_declaration
     */
    private void variables() {
        while(nextTok == Constants.DEFINE){
            wsal.add(nextLex);
            scan();
            dataDeclaration();
        }
    }

    /**
     * data_declaration -> IDENTIFIER OF TYPE data_type
     */
    private void dataDeclaration() {
        if (nextTok == Constants.IDENT) {
            wsal.add(nextLex);
            lookup(nextLex); 
            scan();
            if (nextTok == Constants.OF) {
                wsal.add(nextLex);
                scan();
                if (nextTok == Constants.TYPE){
                    wsal.add(nextLex);
                    scan();
                    type();
                }
                else {
                    error("Invalid variable declaration", scanner.getRow(), true);
                }
            }
            else {
                error("Invalid variable declaration", scanner.getRow(), true);
            }
        } else {
            error("Invalid Identifier name", scanner.getRow(), true);
        }
    }

    /**
     * In BNF, the non-terminal associated with this method is only found in data declarations.
     * accordingly, this is the only place identifiers are declared and added to the identifier table.
     * data_type -> INTEGER | TSTRING
     */
    private void type() {
        switch (nextTok){
            case Constants.INTEGER:
                //default value of unassigned integers are 0.
                idTable.add(new IntegerIdentifier(this.activeIdentifierName, Constants.INTEGER, 0));  
                wsal.add(nextLex);
                flush();
                scan();
                return;
            case Constants.TSTRING:
                idTable.add(new StringIdentifier(this.activeIdentifierName, Constants.TSTRING, ""));
                wsal.add(nextLex);
                flush();
                scan();
                return;
            default:
                error("Type not supported.", scanner.getRow(), true);
        }
    }

    /**
     * Begin parsing
     * BEGIN pactions
     */
    private void begin(){
        /* BNF: BEGIN pactions */
        while (nextTok != Constants.ENDFUNCTION){
            actions();
        } if (nextTok == Constants.ENDFUNCTION) {
            wsal.add(nextLex);
            scan();
        }
    }

    /**
     * action_def -> SET name_ref EQUOP expr
     *             | INPUT name_ref
     *             | DISPLAY pvar_value_list
     *             | INCREMENT name_ref
     *             | DECREMENT name_ref
     *             | IF pcondition THEN pactions opt_else ENDIF
     *             | WHILE pcondition DO pactions ENDWHIL
     *
     */
    private void actions() {

        //actions() method is only looking for the first keyword from each RHS to determine proper method to call.
            switch (nextTok){
                case Constants.SET:
                    wsal.add(nextLex);
                    scan();
                    assignment();
                    flush();
                    break;
                case Constants.INPUT:
                    wsal.add(nextLex);
                    scan();
                    input();
                    flush();
                    break;
                case Constants.DISPLAY:
                    wsal.add(nextLex);
                    scan();
                    display();
                    flush();
                    break;
                case Constants.INCREMENT:
                    wsal.add(nextLex);
                    scan();
                    increment();
                    break;
                case Constants.DECREMENT:
                    wsal.add(nextLex);
                    scan();
                    decrement();
                    break;
                case Constants.IF:
                    wsal.add(nextLex);
                    scan();
                    ifStatement();
                    break;
                case Constants.WHILE:
                    wsal.add(nextLex);
                    scan();
                    whileStatement();
                    break;
                default:
                    //  decision made here to skip to the end of a line if no action method is applicable
                    error("Invalid word to begin a statement", scanner.getRow(), true);
                    scan();
                    break;
            }
    }

    /**
     * BNF: SET name_ref EQUOP expr
     */
    private void assignment() {
        if (nextTok == Constants.IDENT){
            // this call is necessary to establish current identifier as active
            lookup(nextLex);        
            
            wsal.add(nextLex);
            scan();
            if (nextTok == Constants.EQUAOP){
                wsal.add(nextLex);
                scan();
                if(running){
                    Identifier id = idTable.get(activeIdentifier);
                    assignmentStatement = new AssignmentStatement(id, expr());
                } else
                    expr();
            }
            else {
                error("Invalid syntax for assignment operation", scanner.getRow(), true);
            }
        } else {
            error("Invalid syntax for assignment operation", scanner.getRow(), true);
        }
    }
    /**
     * BNF: INPUT name_ref
     * BNF sample contradicts sample file where a string is included.
     * Actual implementation here allows for INPUT string_literal COMMA name_ref
     */
    private void input() {

        switch (nextTok) {
            case Constants.STRING:
                wsal.add(nextLex);
                if(running)output(nextLex);
                scan();
                if (nextTok == Constants.COMMA){
                    wsal.add(nextLex);
                    scan();
                    if (nextTok == Constants.IDENT){
                        wsal.add(nextLex);
                        if (running) {
                            lookup(nextLex);        //index of current identifier in idTable stored to global variable
                            String s = inputHandler.nextLine();
                            Identifier id = idTable.get(activeIdentifier);      // nre reference to existing identifier in memory
                            assignmentStatement = new AssignmentStatement(id, s);
                        }
                        scan();
                    } else
                        error("Input statement invalid syntax", scanner.getRow(), true);
                } else
                    error("Input statement invalid syntax", scanner.getRow(), true);
                break;
            case Constants.IDENT:
                wsal.add(nextLex);
                scan();
                break;
            default:
                error("Input statement invalid syntax", scanner.getRow(), true);
                break;
        }
        if(running)output("\n");
    }
    /**
     * BNF: DISPLAY pvar_value_list
     * BNF: pvar_value_list -> expr | pvar_value_list COMMA expr
     */
    private void display() {

        if(running)output(expr());
        else expr();
        while (nextTok == Constants.COMMA){
            wsal.add(nextLex);
            scan();
            if(running)output(" "+expr());
            else expr();
        }
        if (running)output("\n");
    }
    /**
     * BNF: INCREMENT name_ref
     * BNF: name_ref -> IDENTIFIER
     */
    private void increment() {

        if (nextTok == Constants.IDENT){
            wsal.add(nextLex);
            if (running){
                lookup(nextLex);
                IntegerIdentifier id = (IntegerIdentifier) idTable.get(activeIdentifier);
                int val = id.getIntValue();
                val++;
                id.setIntValue(val);
            }
            flush();
            scan();
        }
        else {
            error("Only valid identifiers can be incremented", scanner.getRow(), true);
        }
    }

    /**
     * BNF: DECREMENT name_ref
     * BNF: name_ref -> IDENTIFIER
     */
    private void decrement() {

        if (nextTok == Constants.IDENT){
            wsal.add(nextLex);
            if (running){
                lookup(nextLex);
                IntegerIdentifier id = (IntegerIdentifier) idTable.get(activeIdentifier);
                int val = id.getIntValue();
                val--;
                id.setIntValue(val);
            }
            flush();
            scan();
        }
        else {
            error("Only valid identifiers can be decremented", scanner.getRow(), true);
        }
    }
    /**
     * BNF: IF pcondition THEN pactions opt_else ENDIF
     */
    private void ifStatement() {

        boolean execute = pcondition();
        if (nextTok == Constants.THEN){
            wsal.add(nextLex);
            flush();
            scan();
            if (!execute)
                running = false;
            while (nextTok != Constants.ENDIF && nextTok != Constants.ELSE){
                actions();
            }
            if (nextTok == Constants.ELSE){
                wsal.add(nextLex);
                flush();
                scan();
                
                optionalElse(execute);
            } if (nextTok == Constants.ENDIF) {   //This is after optional else so it can be written only once
                wsal.add(nextLex);      //when optional else returns, it arrives at this if statement
                flush();
                scan();
                if(!whileSkip)
                    running = true;
            }
        }
        
    }
    /**
     * BNF: WHILE pcondition DO pactions ENDWHILE
     */
    private void whileStatement() {

        int conditionIterations = scanner.count;
        boolean execute = pcondition();     //boolean set to value of conditional expression
        if (nextTok == Constants.DO){
            wsal.add(nextLex);
            flush();
            scan();
            if (!execute){              // if condition was false, parse through loop
                running = false;        // but don't execute any code
                whileSkip = true;       // ensure if statements don't execute any code
            }
            while (nextTok != Constants.ENDWHILE) {
                actions();
            }
            if (execute){
                /*
                 * New parser instance created to come back to beginning of while loop
                 * New Instance will not print anything or execute any code by default
                 * New instance is given values for identifiers from current instance
                 */
                Parser p = new Parser(f.getAbsolutePath());
                p.displayParsedLines = false;
                p.idTable = this.idTable;
                for (int i = 0; i<conditionIterations; i++)
                    p.scan();
                /*
                 * Upon arriving at original conditional expression, it is evaluated again
                 * If true, while statement is parsed and executed again
                 * identifiers from this instance are given values from new instance
                 * New parser instance is reset to conditional statement point, 
                 * where it will be evaluated when loop returns
                 */
                while(p.pcondition()){
                    p.running = true;
                    p.scan();
                    while(p.nextTok != Constants.ENDWHILE){
                        p.actions();
                    }
                    this.idTable = p.idTable;
                    p = new Parser(f.getAbsolutePath());
                    p.displayParsedLines = false;
                    p.idTable = this.idTable;
                    for (int i = 0; i<conditionIterations; i++)
                       p.scan();
                }
            }
            if (nextTok == Constants.ENDWHILE){
                wsal.add(nextLex);
                flush();
                running = true;         // if condition was originally false, proper execution resumes
                scan();
            }
        }
        else error("Invalid while loop declaration", scanner.getRow(), true);
    }


    /**
     * Support for Actions Parsing:
     * BNF: expr -> term
     * | term PLUS term
     * | term MINUS term
     */
    private String expr() {
        String s = term();
        while (nextTok == Constants.ADDOP || nextTok == Constants.SUBOP){
            int i = Integer.parseInt(s);
            wsal.add(nextLex);
            if (nextTok == Constants.ADDOP){
                scan();
                String s1 = term();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i + i1);
            }
            else {
                scan();
                String s1 = term();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i - i1);
            }
        }
        return s;
    }

    /** BNF: term -> punary
     * | punary STAR punary
     * | punary DIVOP punary
     */
    private String term() {

        String s = punary();
        while (nextTok == Constants.STAROP || nextTok == Constants.DIVOP){
            int i = Integer.parseInt(s);
            wsal.add(nextLex);
            if (nextTok == Constants.STAROP){
                scan();
                String s1 = punary();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i * i1);
            }
            else {
                scan();
                String s1 = punary();
                int i1 = Integer.parseInt(s1);
                return Integer.toString(i / i1);
            }
            
        }
        return s;
    }

    /**
     * BNF: punary -> element | MINUS element
     */
    private String punary() {

        if (nextTok == Constants.SUBOP){
            wsal.add(nextLex);
            scan();
            return element();
        } else {
            return element();
        }
    }

    /**
     * where values are given out at execution time
     * BNF: element -> IDENTIFIER | STRING	| NUMBER
     */
    private String element() {
        switch (nextTok) {
            case Constants.STRING:
                wsal.add(nextLex);
                String s = nextLex;
                scan();
                return s;
            case Constants.IDENT:
                wsal.add(nextLex);
                String s1 = nextLex;
                scan();
                return idTable.get(getIndex(s1)).getValue();
            case Constants.NUMBER:
                wsal.add(nextLex);
                String s2= nextLex;
                scan();
                return s2;
            default:
                return "";
        }

    }
    private int getIndex(String s){
        for(Identifier id : idTable){
            if (s.compareTo(id.getName()) == 0){
                return idTable.indexOf(id);
            }
        }
        return -1;
    }
    /**
     * BNF: pcondition -> expr eq_v expr
     */
    private boolean pcondition() {

        String exp1 = expr();
        int operation = comparison();
        String exp2 = expr();
        
        int val1 = Integer.parseInt(exp1);
        int val2 = Integer.parseInt(exp2);
        switch (operation){
            case Constants.EQUATOO:
                if (val1 == val2)
                    return true;
                return false;
            case Constants.GTHAN:
                if (val1 > val2)
                    return true;
                return false;
            case Constants.LTHAN:
                if (val1 < val2)
                    return true;
                return false;
            default:
                return false;
        }
    }
    /**
     * BNF: eq_v -> EQUALS | GREATER THAN | LESS THAN
     */
    private int comparison() {

        wsal.add(nextLex);
        int ret = nextTok;
        switch (nextTok){
            case Constants.EQUATOO:
                scan();
                break;
            case Constants.LTHAN:
                scan();
                break;
            case Constants.GTHAN:
                scan();
                break;
            default:
                error("Invalid comparison operator", scanner.getRow(), false);
                scan();
        }
        return ret;
    }

    /**
     * opt_else ->
     * | ELSE pactions
     */
    private void optionalElse(boolean dontExecute) {

        if (dontExecute)
            running = false;
        if(!dontExecute)
            running = true;
         while (nextTok != Constants.ENDIF){      //optionalElse method looking for end of if statement
             actions();
         }
    }

    /**
     * BNF: ENDFUN IDENTIFIER
     * */
    private void endfun() {

        if (nextTok == Constants.IDENT || nextTok == Constants.MAIN){
            wsal.add(nextLex);
            flush();
            scan();
        }
        else {
            error("Invalid function name", scanner.getRow(), true);
        }
    }

}
