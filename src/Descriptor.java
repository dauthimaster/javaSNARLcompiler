//
//  SNARL/DESCRIPTOR. Objects that describe Snarl values.
//
//    James Moen
//    21 Feb 12
//
//  DESCRIPTORs describe values: arrays, integers, procedures, and strings. The
//  classes shown here implement the top of a hierarchy of DESCRIPTORs.
//
//    Descriptor               Describes a value.
//      RegisterDescriptor     Describes a value in a register.
//      NameDescriptor         Describes a value that has a name.
//        GlobalDescriptor     Describes a value that has a global name.
//        LocalDescriptor      Describes a value that has a local name.
//

//  DESCRIPTOR. Describes a value.

abstract class Descriptor
{
  protected Type type;  //  The value's type.

//  GET TYPE. Return the value's type.

  public Type getType()
  {
    return type;
  }

//  TO STRING. For debugging.

  public abstract String toString();
}

//  REGISTER DESCRIPTOR. Describes a value in a register.

class RegisterDescriptor extends Descriptor
{
  private Allocator.Register register;  //  The register that holds the value.

//  Constructor.

  public RegisterDescriptor(Type type, Allocator.Register register)
  {
    this.type = type;
    this.register = register;
  }

//  GET REGISTER. Return the register.

  public Allocator.Register getRegister()
  {
    return register;
  }

//  TO STRING. For debugging.

  public String toString()
  {
    return "[RegisterDescriptor " + type + " " + register + "]";
  }
}

//  NAME DESCRIPTOR. Describe a value that has a name.

abstract class NameDescriptor extends Descriptor
{

//  LVALUE. Used when the name appears alone on the left side of an assignment.
//  Return a REGISTER that holds the name's address, or call SOURCE.ERROR.

  protected abstract Allocator.Register lvalue();

//  RVALUE. Used then the name appears as part of an expression, perhaps on the
//  right side of an assignment. Return a REGISTER that holds the name's value,
//  or call SOURCE.ERROR.

  protected abstract Allocator.Register rvalue();
}

//  LOCAL DESCRIPTOR. Describe a value that has a local name.

abstract class LocalDescriptor extends NameDescriptor
{
  protected int offset;  //  Offset of the name in the current stack frame.

//  GET OFFSET. Return the local name's offset.

  protected int getOffset()
  {
    return offset;
  }
}

//  GLOBAL DESCRIPTOR. Describe a value that has a global name.

abstract class GlobalDescriptor extends NameDescriptor
{
  protected Label label;  //  Label corresponding to the global name.

//  GET LABEL. Return the global name's label.

  protected Label getLabel()
  {
    return label;
  }
}
