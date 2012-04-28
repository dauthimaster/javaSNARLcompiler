//
//  SNARL/LABEL. A label in a MIPS assembly language program.
//
//    James Moen
//    30 Jan 12
//

//  LABEL. A label in an assembly language program.

class Label
{
  private static       int     count   = 0;        //  Uniquifying counter.
  private static final int     length  = 5;        //  Max PREFIX length.
  private              String  name;               //  What to print.
  private              boolean placed;             //  Has label been placed?
  private static final String  prefix  = "label";  //  Default prefix.

//  Constructors. Make a new LABEL whose NAME consists of a PREFIX, followed by
//  some digits that make it unique. Only the first LENGTH characters of PREFIX
//  are used, and PREFIX must not end with a digit. If no PREFIX is given, then
//  use the default. The LABEL is initially not PLACED.

  public Label()
  {
    name = prefix + count;
    count += 1;
    placed = false;
  }

  public Label(String prefix)
  {
    if (prefix.length() > length)
    {
      prefix = prefix.substring(0, length - 1);
    }
    if (Character.isDigit(prefix.charAt(prefix.length() - 1)))
    {
      throw new IllegalArgumentException("'" + prefix + "' ends with digit.");
    }
    name = prefix + count;
    count += 1;
    placed = false;
  }

//  PLACE. If this LABEL has been PLACED, then throw an exception. Otherwise we
//  record that this LABEL is now PLACED.

  public void place()
  {
    if (placed)
    {
      throw new IllegalStateException("'" + name + "' was placed twice.");
    }
    else
    {
      placed = true;
    }
  }

//  TO STRING. Convert this LABEL to a STRING for printing.

  public String toString()
  {
    return name;
  }
}
