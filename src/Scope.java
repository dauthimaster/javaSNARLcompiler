/*
  SNARL/Scope

  James Current
  Date: 3/20/12
  Time: 8:09 PM
 */

public class Scope {
    protected int level; //Level at which the scope exists
    protected Descriptor descriptor; //Descriptor of the name this scope is keyed to

    //Constructor. Return a new Scope positioned at the specified level and containing the specified descriptor

    public Scope(int level, Descriptor descriptor) {
        this.descriptor = descriptor;
        this.level = level;
    }
}
