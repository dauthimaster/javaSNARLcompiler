import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: James Current
 * Date: 1/22/12
 * Time: 10:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Scanner extends Common{                                
    private int token; //current token
    private Source source; //read chars from here
    private String tokenString; //current token as string
    private int tokenInt; //current token as int
    private Hashtable reserved = new Hashtable(15); // Hashtable of reserved names
    
    public Scanner(String path){
        source = new Source(path);
        for(int token = boldAndToken; token <= boldWhileToken; token++){
            reserved.put(tokenToString(token), token);
        }
    }
    
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
                    default: source.error("Illegal token");
                }   
            }
        }
    }

    private boolean isLetter(char ch){
        return 'A' <= ch && ch <= 'Z' || 'a' <= ch && ch <= 'z';
    }

    private boolean isDigit(char ch){
        return '0' <= ch && ch <= '9';
    }

    private boolean isBlank(char ch){
        return ch == ' ' || ch =='\n' || ch == '\t';
    }

    private boolean isReserved(String name){
        return reserved.containsKey(name);
    }

    private int getReserved(String name){
        return (Integer)reserved.get(name);
    }
    
    private void nextBlank(){
        source.nextChar();
    }
    
    private void nextComment(){
        source.nextChar();
        while (!source.atLineEnd()){
            source.nextChar();
        }
        source.nextChar();
    }
    
    private void nextColon(){
        source.nextChar();
        if (source.getChar() == '='){
            token = colonEqualToken;
            source.nextChar();
        } else {
            token = colonToken;
        }
    }

    private void nextSingle(int token){
        this.token = token;
        source.nextChar();
    }

    private void nextLess(){
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
    
    private void nextGreater(){
        source.nextChar();
        if(source.getChar() == '='){
            token = greaterEqualToken;
            source.nextChar();
        } else {
            token = greaterToken;
        }
    }

    private void nextName(){
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

    private void nextIntConstant(){
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
}
                                  