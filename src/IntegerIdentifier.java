/*
 * Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
        Class: CS 4308 W01
        University: Kennesaw State University
        Professor: Dr. Jose Garrido
        Date: April 28th, 2020


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
 * 
 */

public class IntegerIdentifier extends Identifier{

    private int value;
    //public class that serves as a template for the Integer Identifiers in IDTable of the parser
    public IntegerIdentifier(String name, int type, int value) {
        super(name, type); //String and integer declaration
        this.value = value;//set this.value to value
    }
    @Override
    //Method for getting value which returns an integer amount from user
    public String getValue(){
        return Integer.toString(value);
    }
    @Override
    //Method for setting a newValue String output
    public void setValue(String newValue){
        value = Integer.parseInt(newValue);
    }
    @Override
    //Method for getting output from String declaration
    //First set of accessor/mutator that returns value as String
    public String toString(){
        //expected output from user input which returns string value
        return "["+name+","+type+","+value+"]";
    }
    //Method which returns an integer value
    //Second set of accessor/mutator that returns value as integer
    public int getIntValue(){
        return value;
    }
    //Method for setting integer value
    public void setIntValue(int newValue){
        value = newValue; //set value to newValue
    }
}
