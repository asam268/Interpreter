/**
 * StringIdentifier.java
 * Noah Huck
 * CS4308
 * Section 03
 * 22 March 2019
 */

//package pkg;

public class StringIdentifier extends Identifier {

    private String value;
    //class serves as template for String Identifiers in IDTable of parser
    public StringIdentifier(String name, int type, String value) {
        super(name, type);
        this.value = value;
    }
    @Override
    public String getValue(){
        return value;
    }
    @Override
    public void setValue(String newValue){
        value = newValue;
    }
    @Override
    public String toString(){
        return "["+name+","+type+","+value+"]";
    }
    

}
