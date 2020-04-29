/*
        Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
        Class: CS 4308 W01
        University: Kennesaw State University
        Professor: Dr. Jose Garrido
        Date: April 28th, 2020


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
*/


//imported packages for input/output and exception handling
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//Defined a complete scanner class
public class Scanner {
    //Declaration of private variables
    private boolean eofReached;
    private boolean EOL;
    public int count = 0;
    //Integer to represent token type: kw=1, identifiers=2, numbers=3, other=4, strings=5
    private int tokenType = 1;
    private FileReader fr;
    private int row, col;
    private int errorCount;

    //An access variable used to retrieve the current symbol, public
    public String currentLexeme;

    public Scanner(File in) {
        new Constants();
        eofReached = false;
        EOL = false;
        errorCount = 0;
        row = 1;
        col = 1;
        try {
            fr = new FileReader(in);
        } catch (FileNotFoundException e) {
            System.out.println("File '" + in.getPath() + "' was not able to be found.");
        }
    }
    //numeric value produced is the token code for the next symbol
    public int nextToken() {
        /* 
           This value will not show up in the test execution of scanner
           It will be substitutes with a better readable word which is returned by the convertType() method.
        */
        count++;
        currentLexeme = getSymbol();
        if (eofReached)
            return 0;
        switch (tokenType) {
            //If first case,  potential keywords will be looked up and its corresponding constants will be returned
            case 1:
                int tokenCode = lookup(currentLexeme);
                if (tokenCode == -1)
                    return 2;
                return tokenCode;
            case 2:
                return Constants.IDENT;
            case 3:
                return Constants.NUMBER;
            //This case looks for operators and accepted special characters
            case 4:
                return lookup(currentLexeme);
            case 5:
                return Constants.STRING;
            /*
                The Default statement shouldn't ever be reached
                The getSymbol() function will handle errors.Becuase of this, the tokenType variable
                should always be 1 through 4.
            */
            default:
                return lookup(currentLexeme);   


        }

    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    private String getSymbol() {
        // The token type is assumed to initially be a kword
        tokenType = 1;
        String symbol = "";
        if (EOL) {
            // Statement needed to account for new line immediately following a symbol
            EOL = false;
            row++;
            col = 0;
            //***return "\\LF";
        }

        char c = getChar();
        //The starting state arbitrarily 30
        int state = 30;

        while (true) {
            OUTER:
            switch (state) {
                case 30:
                    if ((int) c == 32) {
                        // space characters are skipped
                        c = getChar();
                        break;
                    }
                    // 6000 is the end of file character which is arbitrary.
                    else if ((int) c == 6000) {
                        eofReached = true;
                        return "\\EOF";
                    } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '<' || c == '>' || c == ',' || c == '=' || c == '"') {
                        tokenType = 4;
                        state = 12;//state 12 is reffered when a special character is detected
                        break;
                    } else if (Character.isLetter(c)) { //could be either an identifier or keyword
                        state = 15;
                        break;
                    } else if (Character.isDigit(c)) {
                        state = 16;                     //number literal (integer)
                    } else if ((int) c == 10 || (int) c == 13) {
                        row++;                          //  position of reader is being kept track of upon newline,
                        col = 0;
                        //***return "\\LF";
                        c = getChar();
                        break;
                    } else if (eofReached)              //when end of file is reached if escape char 6000 isnt detected
                        return "\\EOF";                 // boolean will be true and eof will be returned
                    else
                        state = -5;                    //Error state will be reached if no other state is matched
                    break;
                case 12:
                    //defined for special character interpretation
                    switch (c) {
                        // handles string literals
                        case '"':
                            tokenType = 5;
                            symbol = symbol.concat(Character.toString(c));
                            c = getChar();
                            while (c != '"' && (int) c != 10 && (int) c != 13) {
                                symbol = symbol.concat(Character.toString(c));
                                c = getChar();
                            }
                            if ((int) c == 10 || (int) c == 13) {
                                //error if newline is detected before a closing " symbol
                                state = -5;
                                break OUTER;
                            }
                            symbol = symbol.concat(Character.toString(c));
                            return symbol;
                        case '>':
                        case '<':       //defined cased for comparison
                        case '=':
                            symbol = symbol.concat(Character.toString(c));
                            c = getChar();
                            if (c == '=') {      //will look for a reacurring '='
                                symbol = symbol.concat(Character.toString(c));
                                return symbol;
                            }
                            return symbol;
                        default:
                            symbol = symbol.concat(Character.toString(c));
                            return symbol;
                    }
                case 15:
                    //Case for building keywords/identifiers
                    symbol = symbol.concat(Character.toString(c));
                    c = getChar();
                    while (Character.isLetterOrDigit(c)) {
                        // if a digit is encountered, type is moved from keyword to identifier
                        if (Character.isDigit(c))
                            tokenType = 2;
                        symbol = symbol.concat(Character.toString(c));
                        c = getChar();
                    }
                    //space, end of line, or end of file cause symbol to return
                    if (c == ' ') {
                        return symbol;
                    } else if ((int) c == 10 || (int) c == 13) {
                        EOL = true;
                        return symbol;
                    } else if ((int) c == 6000) {
                        return symbol;
                    } else {
                        //error state will be envoked if a character other than alphanumeric/ whitespace encountered
                        state = -5;
                        break;
                    }
                    //this case builds integer literals
                case 16:
                    tokenType = 3;
                    symbol = symbol.concat(Character.toString(c));
                    c = getChar();
                    while (Character.isDigit(c)) {
                        symbol = symbol.concat(Character.toString(c));
                        c = getChar();
                    }
                    //space, end of line, or end of file cause symbol to return
                    if (c == ' ') {
                        return symbol;
                    } else if ((int) c == 10 || (int) c == 13) {
                        EOL = true;
                        return symbol;
                    } else if ((int) c == 6000) {
                        return symbol;
                    } else {
                        //error state envoked if character other than digit or whitespace/encountered
                        state = -5;
                        break;
                    }
                case -5:
                    //error handling case. 
                    errorCount++;
                    //If an unidentified symbol is found, skips to next whitespace
                    while (!(c == ' ') && !eofReached) {
                        if ((int) c == 10 || (int) c == 13) {
                            row++;
                            break;
                        }
                        c = getChar();
                    }
                    return getSymbol();
                default:
                    //Default shouldn't be invoked, but needed for mandatory method return and testing
                    return "\\DEF";
            }
        }       // End of While  
    }

    private char getChar() {
        try {
            int x = fr.read();  //reads the next character from the input file
            if (x == -1) {
                eofReached = true;
                return 6000;
                /* 
                    Since char is the return type of this function, and char in java is an unsigned type
                    the value -1 cannot be cast to a char, so the default value returned is 6000 for end of file
                */
            }
            col++;
            return (char) x;
        } catch (IOException e) {
        }
        return '~';
    }

    private int lookup(String s) {
        //Defined a function which linearly searches kwords arrays
        //returns the corresponding constant from the kwConst array .If no keyword is found, a -1 is returned.
        for (int i = 0; i < kwords.length; i++) {
            if (s.compareTo(kwords[i]) == 0)
                return kwConst[i];
        }
        return -1;
    }
    //Declare a list of Keywords
    private String[] kwords = {
            "begin",
            "decrement",
            "define",
            "display",
            "do",
            "else",
            "endfun",
            "endif",
            "endwhile",
            "function",
            "if",
            "increment",
            "input",
            "integer",
            "is",
            "of",
            "set",
            "then",
            "type",
            "variables",
            "while",
            "+",
            "-",
            "*",
            "/",
            "=",
            "<",
            ">",
            "\\LF",
            "main",
            "==",
            ",",
            "string"
    };
    //Declared a list of keyword constants
    private int[] kwConst = {

            Constants.BEGIN,
            Constants.DECREMENT,
            Constants.DEFINE,
            Constants.DISPLAY,
            Constants.DO,
            Constants.ELSE,
            Constants.ENDFUNCTION,
            Constants.ENDIF,
            Constants.ENDWHILE,
            Constants.FUNCTION,
            Constants.IF,
            Constants.INCREMENT,
            Constants.INPUT,
            Constants.INTEGER,
            Constants.IS,
            Constants.OF,
            Constants.SET,
            Constants.THEN,
            Constants.TYPE,
            Constants.VARIABLES,
            Constants.WHILE,
            Constants.ADDOP,
            Constants.SUBOP,
            Constants.STAROP,
            Constants.DIVOP,
            Constants.EQUAOP,
            Constants.LTHAN,
            Constants.GTHAN,
            Constants.LF,
            Constants.MAIN,
            Constants.EQUATOO,
            Constants.COMMA,
            Constants.TSTRING
    };

    public int getErrors() {
        return errorCount;
    }

    public boolean endOfFileReached() {
        return eofReached;
    }

    public String convertType(int i) {

        /* This is a function which returns a string representation corresponding to a token type.
        This is mainly for displaying the type when using the scanner on its own, as the parses
        will only need the  numeric value of the token
        */
        switch (i) {
            case -1:
                return "unidentified symbol";
            case 0:
                return "End of file";
            case 2:
                return "identifier";
            case 3:
                return "number";
            case 5:
                return "String literal";
            case 17:
                return "add operator";
            case 18:
                return "subtract operator";
            case 19:
                return "multiply operator";
            case 20:
                return "divide operator";
            case 21:
                return "assignment operator";
            case 22:
                return "less than operator";
            case 23:
                return "greater than operator";
            case 24:
                return "equal to operator";
            case 25:
                return "comma ";
            default:
                return "keyword";
        }
    }
}
