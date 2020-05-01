/*
        Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
        Class: CS 4308 W01
        University: Kennesaw State University
        Professor: Dr. Jose Garrido
        Date: April 28th, 2020


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
*/


public class AssignmentStatement {

    // This class assigns values to identifiers
    
    public AssignmentStatement(Identifier id, String value){
        id.setValue(value);
    }
    /**
     * This class could have been implemented as a method in Parser
     * Decision made to create a unique class for clarity
     */
}