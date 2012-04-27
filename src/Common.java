//
//  SNARL/COMMON. Declarations used by many classes.
//
//    James Moen
//    16 Sep 11
//

//  COMMON. Declarations used by many classes.

class Common
{ 
  public  static final char    eofChar = '\u0000';  //  End of source sentinel.
  private static       int     level   = 0;         //  Parser recursion level.
  public  static final boolean tracing = true;      //  Enable ENTER/EXIT.

//  Token numbers.

  public static final int minToken            =  0;
  public static final int boldAndToken        =  1;
  public static final int boldBeginToken      =  2;
  public static final int boldCodeToken       =  3;
  public static final int boldDoToken         =  4;
  public static final int boldElseToken       =  5;
  public static final int boldEndToken        =  6;
  public static final int boldIfToken         =  7;
  public static final int boldIntToken        =  8;
  public static final int boldNotToken        =  9;
  public static final int boldOrToken         = 10;
  public static final int boldProcToken       = 11;
  public static final int boldStringToken     = 12;
  public static final int boldThenToken       = 13;
  public static final int boldValueToken      = 14;
  public static final int boldWhileToken      = 15;
  public static final int closeBracketToken   = 16;
  public static final int closeParenToken     = 17;
  public static final int colonEqualToken     = 18;
  public static final int colonToken          = 19;
  public static final int commaToken          = 20;
  public static final int dashToken           = 21;
  public static final int endFileToken        = 22;
  public static final int equalToken          = 23;
  public static final int greaterEqualToken   = 24;
  public static final int greaterToken        = 25;
  public static final int ignoredToken        = 26;
  public static final int intConstantToken    = 27;
  public static final int lessEqualToken      = 28;
  public static final int lessGreaterToken    = 29;
  public static final int lessToken           = 30;
  public static final int nameToken           = 31;
  public static final int openBracketToken    = 32;
  public static final int openParenToken      = 33;
  public static final int plusToken           = 34;
  public static final int semicolonToken      = 35;
  public static final int slashToken          = 36;
  public static final int starToken           = 37;
  public static final int stringConstantToken = 38;
  public static final int maxToken            = 39;

//  Map token numbers to strings.

  private static final String[] tokenString =
  { "min token",        //  minToken
    "and",              //  boldAndToken
    "begin",            //  boldBeginToken
    "code",             //  boldCodeToken
    "do",               //  boldDoToken
    "else",             //  boldElseToken
    "end",              //  boldEndToken
    "if",               //  boldIfToken
    "int",              //  boldIntToken
    "not",              //  boldNotToken
    "or",               //  boldOrToken
    "proc",             //  boldProcToken
    "string",           //  boldStringToken
    "then",             //  boldThenToken
    "value",            //  boldValueToken
    "while",            //  boldWhileToken
    "close bracket",    //  closeBracketToken
    "close paren",      //  closeParenToken
    "colon equal",      //  colonEqualToken
    "colon",            //  colonToken
    "comma",            //  commaToken
    "dash",             //  dashToken
    "end of file",      //  endFileToken
    "equal",            //  equalToken
    "greater-or-equal", //  greaterEqualToken
    "greater",          //  greaterToken
    "ignored",          //  ignoredToken
    "int constant",     //  intConstantToken
    "less-or-equal",    //  lessEqualToken
    "not-equal",        //  lessGreaterToken
    "less",             //  lessToken
    "name",             //  nameToken
    "open bracket",     //  openBracketToken
    "open paren",       //  openParenToken
    "plus",             //  plusToken
    "semicolon",        //  semicolonToken
    "slash",            //  slashToken
    "star",             //  starToken
    "string constant",  //  stringConstantToken
    "max token" };      //  maxToken

//  TOKEN TO STRING. Return a string that describes TOKEN.

  public static String tokenToString(int token)
  {
    return tokenString[token];
  }

//  ENTER. If we're TRACING, then write a message saying METHOD has started.

  public static void enter(String method)
  {
    if (tracing)
    {
      writeBlanks(level);
      System.out.println("Enter " + method + ".");
      level += 1;
    }
  }

//  EXIT. Like EXIT, but here the message says METHOD has finished.

  public static void exit(String method)
  {
    if (tracing)
    {
      level -= 1;
      writeBlanks(level);
      System.out.println("Exit " + method + ".");
    }
  }

//  WRITE BLANKS. Write COUNT blanks to SYSTEM.OUT, without a newline.

  public static void writeBlanks(int count)
  {
    while (count > 0)
    {
      System.out.print(' ');
      count -= 1;
    }
  }
}
