/*
  SNARL/Descriptor

  James Current
  Date: 3/10/12
  Time: 2:50 PM
 */
public class Descriptor {
    private Type type;  //The type of the Descriptor

    //Constructor. Return a new Descriptor with type type.

    public Descriptor(Type type){
        this.type = type;
    }

    //getType. Return this Descriptor's type.

    public Type getType(){
        return type;
    }

    //toString. Return a String that notates this Descriptor
    
    public String toString(){
        return type.toString();
    }
}
