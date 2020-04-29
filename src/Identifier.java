/**
 * Identifier.java
 * Noah Huck
 * CS4308
 * Section 03
 * 22 March 2019
 */

//package pkg;

public abstract class Identifier {
    
    // class provides a framework for identifier subclasses of different types
    // protected keyword chosen to give subclasses access to these variables.
    
    final protected String name;
    final protected int type;
    
    public Identifier(String name, int type){
        this.name = name;
        this.type = type;
    }
    public String getName(){
        return name;
    }
    public int getType(){
        return type;
    }
    //abstract methods present so objects cast as Identifier can use these methods
    public abstract String getValue();
    public abstract void setValue(String value);
}

