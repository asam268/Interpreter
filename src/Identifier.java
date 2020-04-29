/**
 * Identifier.java
 * 
 */

//Identifier class protected keyword determines subclasses
//access to key variables.

public abstract class Identifier {
        
    final protected String name; //String value
    final protected int type;   //integer cast 
    
    public Identifier(String name, int type){
        this.name = name; //set String declaration
        this.type = type; //set int type casting
    }
    public String getName(){
        return name; //method that returns a String output upon execution
    }
    public int getType(){
        return type; //method that returns an integer output upon execution
    }
    public abstract String getValue(); //Instantiate a String getValue call
    public abstract void setValue(String value); //setValue return String
}

