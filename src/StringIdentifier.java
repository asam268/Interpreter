
/*
        Author: Chukwufunayan Ojiagbaje, James Bozhkov, Asa Marshall
        Class: CS 4308 W01
        University: Kennesaw State University
        Professor: Dr. Jose Garrido
        Date: April 28th, 2020


        Title: Semester Project Deliverable 3
        SCL Language Interpreter
*/

//Method class Identifier implementation
public class StringIdentifier extends Identifier {

    private String value;
    //class serves as template for String Identifiers in IDTable of parser
    public StringIdentifier(String name, int type, String value) {
        super(name, type);  //instantiate name and type.
        this.value = value; //set this.value to value.
    }
    @Override
    public String getValue(){
        return value; //output for getValue
    }
    @Override
    public void setValue(String newValue){
        value = newValue;  //set value to newValue
    }
    @Override
    public String toString(){
        return "["+name+","+type+","+value+"]"; //expected output
    }
    

}
