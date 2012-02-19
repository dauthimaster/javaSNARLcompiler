import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Hashtable;

/*
SNARL/Scanner

James Current
1/22/12
 */
public class Scanner extends Common{                                
    protected int token; //current token
    protected Source source; //read chars from here
    protected String tokenString; //current token as string
    protected int tokenInt; //current token as int
    protected Hashtable <String,Integer>reserved = new Hashtable<String,Integer>(15); // Hashtable of reserved names

    //CONSTRUCTOR. Return a new Scanner positioned at the beginning of the file and with a populated Hashtable

    public Scanner(Source source){
        this.source = source;
        for(int token = boldAndToken; token <= boldWhileToken; token++){
            reserved.put(tokenToString(token), token);
        }
        tokenInt = 0;
        tokenString = "";
        nextToken();
    }

    //getInt. Return the current token's int value (if applicable, otherwise an undefined int)

    public int getInt(){
        return tokenInt;
    }

    //getString. Return the current token's string (if applicable, otherwise an undefined string)
    
    public String getString(){
        return tokenString;
    }

    //getToken. Return the current token from the source file
    
    public int getToken(){
        return token;
    }
    
    //NEXT TOKEN. Read the next token in from the source file
    public void nextToken(){
        token = ignoredToken;
        while (token == ignoredToken){
            if(isLetter(source.getChar())){
                nextName();
            } else if(isDigit(source.getChar())){
                nextIntConstant();
            } else if(isBlank(source.getChar())){
                nextBlank();
            } else {
                switch (source.getChar()){
                    case '#': {nextComment();break;} 
                    case '\"': {nextStringConstant();break;}
                    case '+': {nextSingle(plusToken);break;}
                    case '-': {nextSingle(dashToken);break;}
                    case '*': {nextSingle(starToken);break;}
                    case '/': {nextSingle(slashToken);break;}
                    case '[': {nextSingle(openBracketToken);break;}
                    case ']': {nextSingle(closeBracketToken);break;}
                    case '(': {nextSingle(openParenToken);break;}
                    case ')': {nextSingle(closeParenToken);break;}
                    case ',': {nextSingle(commaToken);break;}
                    case '=': {nextSingle(equalToken);break;}
                    case ';': {nextSingle(semicolonToken);break;}
                    case ':': {nextColon();break;}
                    case '<': {nextLess();break;}
                    case '>': {nextGreater();break;}
                    case eofChar: {nextEofToken();break;}
                    default: source.error("Illegal token");
                }   
            }
        }
    }

    //isLetter. Checks if a character is a letter

    protected boolean isLetter(char ch){
        return 'A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z';
    }

    //isDigit. Checks if a character is a digit

    protected boolean isDigit(char ch){
        return '0' <= ch && ch <= '9';
    }

    //isBlank. Checks if a character is a blank

    protected boolean isBlank(char ch){
        return ch == ' ' || ch =='\n' || ch == '\t' || ch == '\r';
    }

    //isReserved. Checks if a string is a reserved name

    protected boolean isReserved(String name){
        return reserved.containsKey(name);
    }

    //getReserved. Retrieves the token by looking it up in the Hashtable by its string

    protected int getReserved(String name){
        return reserved.get(name);
    }

    //nextBlank. Skips characters considered to be blanks
    
    protected void nextBlank(){
        source.nextChar();
        while (isBlank(source.getChar())){
            source.nextChar();    
        }
        
    }

    //nextComment. Skips all characters until newline
    
    protected void nextComment(){
        source.nextChar();
        while (!source.atLineEnd()){
            source.nextChar();
        }
        source.nextChar();
    }

    //nextColon. Tests for : or :=
    
    protected void nextColon(){
        source.nextChar();
        if (source.getChar() == '='){
            token = colonEqualToken;
            source.nextChar();
        } else {
            token = colonToken;
        }
    }

    //nextSingle. Called for any stand alone tokens (like * + - ,)

    protected void nextSingle(int token){
        this.token = token;
        source.nextChar();
    }

    //nextLess. Tests for < , <= , or <>

    protected void nextLess(){
        source.nextChar();
        if(source.getChar() == '>'){
            token = lessGreaterToken;
            source.nextChar();
        } else if(source.getChar() == '='){
            token = lessEqualToken;
            source.nextChar();
        } else {
            token = lessToken;
        }
    }

    //nextGreater. Tests for either a > or a >=
    
    protected void nextGreater(){
        source.nextChar();
        if(source.getChar() == '='){
            token = greaterEqualToken;
            source.nextChar();
        } else {
            token = greaterToken;
        }
    }

    //nextStringConstant. Collects characters other than " and newline and builds a string.
    //Calls ERROR if string contains a newline (logic also covers if missing a ") ex:
    //WriteString("Fail);
    //WriteInteger(10);
    
    protected void nextStringConstant(){
        StringBuilder stringBuilder = new StringBuilder();
        source.nextChar();
        while (source.getChar() != '\"' && !source.atLineEnd()){
            stringBuilder.append(source.getChar());
            source.nextChar();
        }
        if(source.atLineEnd()){
            source.error("Invalid String");
        } else {
            token = stringConstantToken;
            tokenString = stringBuilder.toString();
            source.nextChar();
        }
    }

    //nextName. Collects letters and digits to build a name, checks if name is reserved

    protected void nextName(){
        StringBuilder nameString = new StringBuilder();
        while (isLetter(source.getChar()) || isDigit(source.getChar())){
            nameString.append(source.getChar());
            source.nextChar();
        }
        tokenString = nameString.toString();
        if(isReserved(tokenString)){
            token = getReserved(tokenString);
        } else {
            token = nameToken;
        }
    }

    //nextIntConstant. Collects digits to build an integer, calls ERROR if int is larger than 32-bit.

    protected void nextIntConstant(){
        token = intConstantToken;
        StringBuilder intString = new StringBuilder();
        while (isDigit(source.getChar())){
            intString.append(source.getChar());
            source.nextChar();
        }
        try{
            tokenInt = Integer.parseInt(intString.toString());
        } catch (NumberFormatException ignore){
            source.error("Integer larger than 32-bit");
        }
        tokenString = intString.toString();
    }

    //nextEofToken. Called at End Of File

    protected void nextEofToken(){
        token = endFileToken;
    }

    //MAIN. For testing. List path to test file
    
    public static void main(String[] args){
        Scanner scanner;
        Source source;
        try {
            source = new Source(new FileReader(args[0]));
        } catch (FileNotFoundException ignore) {
            throw new RuntimeException("Cannot open" + args[0] + ".");
        }
        scanner = new Scanner(source);
        while(scanner.getToken() != endFileToken){
            scanner.nextToken();
            System.out.print(tokenToString(scanner.getToken()) + " ");
            if(scanner.getToken() == intConstantToken){
                System.out.print(scanner.getInt() + " ");
                System.out.print(scanner.getString() + " ");
            } else if(scanner.getToken() == stringConstantToken){
                System.out.print("\"" + scanner.getString() + "\" ");
            } else if(scanner.getToken() == nameToken || scanner.reserved.containsValue(scanner.getToken())){
                System.out.print(scanner.getString() + " ");
            }
            System.out.println();
        }
        System.out.println();

        //Two other more focused outputs
        /*scanner.source.reset();
        while(scanner.getToken() != endFileToken){
            scanner.nextToken();
            if(scanner.reserved.containsValue(scanner.getToken())){
                System.out.print(tokenToString(scanner.getToken()) + " ");
                System.out.print(scanner.getString());
                System.out.println();
            }
        }
        System.out.println();
        scanner.source.reset();
        while(scanner.getToken() != endFileToken){
            scanner.nextToken();
            if(scanner.getToken() == stringConstantToken){
                System.out.print("\"" + scanner.getString() + "\"");
                System.out.println();
            }
        }*/
    }
}
                                  