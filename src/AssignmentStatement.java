/**
 * AssignmentStatement.java
 * Noah Huck
 * CS4308
 * Section 03
 * 29 April 2019
 */

//package pkg;

public class AssignmentStatement {

    // class is used to assign value to identifiers
    
    public AssignmentStatement(Identifier id, String value){
        id.setValue(value);
    }
    /**
     * This class could have been implemented as a method in Parser
     * Decision made to create a unique class for clarity
     */
}