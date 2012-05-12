/*
  SNARL/Descriptor

  James Current
  Date: 3/10/12
  Time: 2:50 PM
 */
abstract class Descriptor {
    private Type type;  //The type of the Descriptor

    //Constructor. Return a new Descriptor with type type.

    /*public Descriptor(Type type){
        this.type = type;
    }                             */

    //getType. Return this Descriptor's type.

    public abstract Type getType();
    /*{
        return type;
    }                       */
}

class RegisterDescriptor extends Descriptor{
    protected Type type;
    public RegisterDescriptor(Type type, Allocator.Register register){
        this.type = type;
    }
    
    public Type getType(){
        return type;
    }
}
