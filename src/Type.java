//
//  SNARL/TYPE. The Snarl type system.
//
//    James Moen
//    02 Nov 11
//
//  These classes can represent more general types than the Snarl language will
//  actually allow. For example, we can represent arrays of strings, procedures
//  that take other procedures as arguments, etc.
//

//  TYPE. The most general type.

abstract class Type
{
  public static final int addressSize = 4;  //  Bytes in a MIPS address.
  public static final int wordSize    = 4;  //  Bytes in a MIPS word.

//  IS SUBTYPE. Test if this type is a subtype of THAT type.

  public abstract boolean isSubtype(Type that);

//  GET SIZE. Return the size of this type in bytes.

  public abstract int getSize();

//  TO STRING. Return a string that notates this type.

  public abstract String toString();
}

//  BASIC TYPE. A predefined type.

class BasicType extends Type
{
  private String    name;  //  This type's name.
  private int       size;  //  This type's size in bytes. It's nonnegative.
  private BasicType supe;  //  This type's supertype.

//  Constructor. Make a new basic type with NAME, SIZE, and SUPE.

  public BasicType(String name, int size, BasicType supe)
  {
    if (size < 0)
    {
      throw new IllegalArgumentException("Type " + name + " has size " + size);
    }
    else
    {
      this.name = name;
      this.size = size;
      this.supe = supe;
    }
  }

//  IS SUBTYPE. Test if this type is a subtype of TYPE.

  public boolean isSubtype(Type type)
  {
    if (type instanceof BasicType)
    {
      BasicType temp = this;
      while (temp != null && temp != type)
      {
        temp = temp.supe;
      }
      return temp == type;
    }
    else
    {
      return false;
    }
  }

//  GET SIZE. Return the size of this type in bytes.

  public int getSize()
  {
    return size;
  }

//  TO STRING. Return a string that notates this type.

  public String toString()
  {
    return name;
  }
}

//  ARRAY TYPE. The type of an array.

class ArrayType extends Type
{
  private Type base;    //  This type's base type.
  private int  length;  //  This type's length. It's nonnegative.

//  Constructor. Make a new array type that has LENGTH and BASE.

  public ArrayType(int length, Type base)
  {
    if (length < 0)
    {
      throw new NegativeArraySizeException("Array type has length " + length);
    }
    else
    {
      this.base   = base;
      this.length = length;
    }
  }

//  IS SUBTYPE. Test if this type is a subtype of TYPE.

  public boolean isSubtype(Type type)
  {
    if (type instanceof ArrayType)
    {
      ArrayType that = (ArrayType) type;
      return this.length == that.length && this.base.isSubtype(that.base);
    }
    else
    {
      return false;
    }
  }

//  GET BASE. Return the base type of this type.

  public Type getBase()
  {
    return base;
  }

//  GET SIZE. Return the size of this type in bytes.

  public int getSize()
  {
    return length * base.getSize();
  }

//  TO STRING. Return a string that notates this type.

  public String toString()
  {
    return "[" + length + "] " + base;
  }
}

//  PROCEDURE TYPE. The type of a procedure.

class ProcedureType extends Type
{

//  PARAMETER. Parameter types are kept in a linked list of these.

  public class Parameter
  {
    private Type      type;  //  This parameter's type.
    private Parameter next;  //  The parameter after this one.

//  Constructors. They're private, so only PROCEDURE TYPE can make PARAMETERs.

    private Parameter()
    { }

//  Make a new PARAMETER that has TYPE.

    private Parameter(Type type)
    {
      this.type = type;
      this.next = null;
    }

//  GET TYPE. Return this parameter's type.

    public Type getType()
    {
      return type;
    }

//  GET NEXT. Return the parameter after this one, or NULL.

    public Parameter getNext()
    {
      return next;
    }
  }

  private int       arity;  //  Number of parameters.
  private Parameter first;  //  Head node of the parameter list.
  private Parameter last;   //  Last node of the parameter list.
  private Type      value;  //  Type of object returned.

//  Constructor. Make a new procedure type. Its parameter list is empty and its
//  value type is missing. We'll fill them in later.

  public ProcedureType()
  {
    arity = 0;
    first = new Parameter(null);
    last  = first;
    value = null;
  }

//  ADD PARAMETER. Add a new parameter TYPE to the end of the parameter list.

  public void addParameter(Type type)
  {
    arity += 1;
    last.next = new Parameter(type);
    last = last.next;
  }

//  ADD VALUE. Add the value type.

  public void addValue(Type value)
  {
    this.value = value;
  }

//  IS SUBTYPE. Test if this type is a subtype of TYPE.

  public boolean isSubtype(Type type)
  {
    if (type instanceof ProcedureType)
    {
      ProcedureType that = (ProcedureType) type;
      if (this.arity == that.arity && this.value.isSubtype(that.value))
      {
        Parameter thisParameter = this.first.next;
        Parameter thatParameter = that.first.next;
        while (thisParameter != null)
        {
          if (thatParameter.type.isSubtype(thisParameter.type))
          {
            thisParameter = thisParameter.next;
            thatParameter = thatParameter.next;
          }
          else
          {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

//  GET ARITY. Return the number of parameters in this type.

  public int getArity()
  {
    return arity;
  }

//  GET PARAMETERS. Return the parameter list of this type.

  public Parameter getParameters()
  {
    return first.next;
  }

//  GET SIZE. Return the size of this type in bytes.

  public int getSize()
  {
    return addressSize;
  }

//  GET VALUE. Return the value type of this type.

  public Type getValue()
  {
    return value;
  }

//  TO STRING. Return a string that notates this type.

  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    Parameter parameter = first.next;
    buffer.append("proc (");
    if (parameter != null)
    {
      buffer.append(parameter.type);
      parameter = parameter.next;
      while (parameter != null)
      {
        buffer.append(", ");
        buffer.append(parameter.type);
        parameter = parameter.next;
      }
    }
    buffer.append(") ");
    buffer.append(value);
    return buffer.toString();
  }
}
