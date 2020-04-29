/**
 * Scanner.java
 * Noah Huck
 * CS4308
 * Section 03
 * 25 February 2019
 */

//package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class Scanner {

    private boolean eofReached;
    private boolean EOL;
    public int count = 0;

    private int tokenType = 1;  //1 for kw, 2 for identifier, 3 for number, 4 for other, 5 for string
    private FileReader fr;
    private int row, col;
    private int errorCount;

    public String currentLexeme;        //accessed to retrieve the current symbol

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

    public int nextToken() {     //numeric value produced is the token code for the next symbol
        /* 
            This value will not show up in test execution of scanner
            It is substituted with a more readable word returned by the convertType() method.
        */
        count++;
        currentLexeme = getSymbol();
        if (eofReached)
            return 0;
        switch (tokenType) {
            case 1:               //first case looks up potential keywords and returns a corresponding constant
                int tokenCode = lookup(currentLexeme);
                if (tokenCode == -1)
                    return 2;
                return tokenCode;
            case 2:
                return Constants.IDENT;
            case 3:
                return Constants.NUMBER;
            case 4:             //This case is looking for operators and accepted special characters
                return lookup(currentLexeme);
            case 5:
                return Constants.STRING;
            default:
                return lookup(currentLexeme);   
            /*
                Default statement should never be reached
                getSymbol() function will handle errors so the tokenType variable
                should always be 1 through 4.
            */

        }

    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    private String getSymbol() {
        tokenType = 1;          // type is assumed to be keyword initially
        String symbol = "";
        if (EOL) {
            // Statement needed to account for new line immediately following a symbol
            EOL = false;
            row++;
            col = 0;
            //***return "\\LF";
        }

        char c = getChar();

        int state = 30;     //initial state arbitrarily 30

        while (true) {
            OUTER:
            switch (state) {
                case 30:
                    if ((int) c == 32) {
                        c = getChar();          //skips over space characters
                        break;
                    } else if ((int) c == 6000) {        // 6000 is the arbitrary end of file character.
                        eofReached = true;
                        return "\\EOF";
                    } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '<' || c == '>' || c == ',' || c == '=' || c == '"') {
                        tokenType = 4;
                        state = 12;                     //when special characters are found, refer to state 12
                        break;
                    } else if (Character.isLetter(c)) {
                        state = 15;                     //could be keyword or identifier
                        break;
                    } else if (Character.isDigit(c)) {
                        state = 16;                     //number (integer) literal
                    } else if ((int) c == 10 || (int) c == 13) {
                        row++;                          //upon newline, keep track of position of reader
                        col = 0;
                        //***return "\\LF";
                        c = getChar();
                        break;
                    } else if (eofReached)              //if eof reached without reading escape character 6000
                        return "\\EOF";                 //the boolean will be true and eof will be returned
                    else
                        state = -5;                    //in the case that no other state is matched, error state reached
                    break;
                case 12:
                    //used for interpretting special characters
                    switch (c) {
                        case '"':
                            //case handles string literals
                            tokenType = 5;
                            symbol = symbol.concat(Character.toString(c));
                            c = getChar();
                            while (c != '"' && (int) c != 10 && (int) c != 13) {
                                symbol = symbol.concat(Character.toString(c));
                                c = getChar();
                            }
                            if ((int) c == 10 || (int) c == 13) {
                                //if newline found before closing ", error encountered
                                state = -5;
                                break OUTER;
                            }
                            symbol = symbol.concat(Character.toString(c));
                            return symbol;
                        case '>':
                        case '<':       //These cases used for comparison operators
                        case '=':
                            symbol = symbol.concat(Character.toString(c));
                            c = getChar();
                            if (c == '=') {      //looking for a second '=' after a comparison symbol
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
                        //error state envoked if character other than alphanumeric or whitespace encountered
                        state = -5;
                        break;
                    }
                case 16:
                    //case for building integer literals
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
                        //error state envoked if character other than digit or whitespace encountered
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
                    //Shouldn't be invoked, but needed for mandatory method return and testing
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
        //function provides linear search of kwords array
        //returns the corresponding constant from the kwConst array
        //or -1 if the keyword is not found.
        for (int i = 0; i < kwords.length; i++) {
            if (s.compareTo(kwords[i]) == 0)
                return kwConst[i];
        }
        return -1;
    }

    private String[] kwords = { //Keyword list
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
    private int[] kwConst = { //Keyword constants

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
        /* 
            this function returns a string representation that corresponds to the token type
            mainly for showing the type when using scanner by itself, as the parser
            will only need the numeric value of the token
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
