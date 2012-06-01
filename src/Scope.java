/*
  SNARL/Scope

  James Current
  Date: 3/20/12
  Time: 8:09 PM
 */

public class Scope {
    protected int level; //Level at which the scope exists
    protected NameDescriptor descriptor; //NameDescriptor of the name this scope is keyed to

    //Constructor. Return a new Scope positioned at the specified level and containing the specified descriptor

    public Scope(int level, NameDescriptor descriptor) {
        this.descriptor = descriptor;
        this.level = level;
    }
}
