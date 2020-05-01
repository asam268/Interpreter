<<<<<<< HEAD
/**
 * Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
=======
/*
        Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
>>>>>>> 624a1293640bd67a59b058baf6d1805fa837409c
        Class: CS 4308 W01
        University: Kennesaw State University
        Professor: Dr. Jose Garrido
        Date: April 28th, 2020
<<<<<<< HEAD


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
 */

//Interpreter class
=======


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
*/
>>>>>>> 624a1293640bd67a59b058baf6d1805fa837409c

public class Interpreter {

    public static void main(String[] args) {
        //file name is developed in as an argument
                 
        if (args.length > 1){
            switch (args[0]){ //switch argument concieved
                case "-p": //first case
                    System.out.printf("\nNow parsing file %s \n\n", args[1]);
                    Parser p = new Parser(args[1]);     //set parser to new parser
                    while (!p.endOfFileReached()){      //executes until end of file is reached
                        p.parseProgram();
                        // parseProgram parses for 1 complete function at a time
                        // loop will execute until all of the listed functions have been parsed
                    }
                    System.out.println("\nEnd of file reached."); //print result stating end of file reached
                    break;
                case "-s":  //second case
                    System.out.printf("\nNow Scanning file %s \n\n", args[1]);
                    Scanner s = new Scanner(new java.io.File(args[1]));
                    while (!s.endOfFileReached()){      //executes until end of file has been reached

                        //next token is converted to a string by the convertType function
                        System.out.printf("%s, row %d, col %d, symbol: %s\n",
                                s.convertType(s.nextToken()), s.getRow(), s.getCol(), s.currentLexeme);
                    } //
                    System.out.println("\nEnd of file reached."); //print result stating end of file reached
                    break;
                case "-e": //third case
                    System.out.printf("\nNow Executing file %s \n\n", args[1]);
                    Parser p2 = new Parser(args[1]);   //set parser p2 to new parser arugument one
                    p2.running = true; //set running parser to true 
                    while (!p2.endOfFileReached()){      //executes until end of file has been reached
                        p2.parseProgram();
                        // parser both parses line by line and then executes corresponding function
                        // loop will execute until all functions have been parsed
                    }
                    break;
            }
        } else {
        System.out.printf("\nNow Executing file: %s \n\n", args[0]);
            Parser p2 = new Parser(args[0]); //set parser p2 to new parser with argument 0
            p2.running = true;  //set running parser to true
            while (!p2.endOfFileReached()){      //executes until end of file has been reached
                p2.parseProgram();
                // parser both parses line by line and then executes the corresponding function
                // loop will execute until all of the functions have been parsed
            }
        }

//   for testing purposes:
//        System.out.println("\nIdentifier Table: \n\n");
//        p2.printIdTable();
    }
    
}
