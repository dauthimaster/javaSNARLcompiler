import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: dauthimaster
 * Date: 1/28/12
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Parser extends Common{
    protected Scanner scanner;
    protected long comparisonOperators = makeSet(
            equalToken,
            greaterToken,
            lessToken,
            lessEqualToken,
            greaterEqualToken,
            lessGreaterToken);
    protected long sumOperators = makeSet(
            plusToken,
            dashToken);
    protected long productOperators = makeSet(
            starToken,
            slashToken);
    protected long termOperators = makeSet(
            dashToken,
            boldNotToken);
    protected long declarationTokens = makeSet(
            boldIntToken,
            boldStringToken,
            openBracketToken);
    protected Source source;
    
    public Parser(Source source){
        this.source = source;
        scanner = new Scanner(source);
    }

    protected long makeSet(int... elements){
        long set = 0L;
        for(int element : elements){           
            set |= (1L << element);
        }
        return set;
    }
    
    //test if element is in set
    protected boolean isInSet(int element, long set){
        return (set & (1L << element)) != 0L;
    }

    public void nextProgram(){
        enter("program");
        nextProgramPart();
        while (scanner.getToken() == semicolonToken){
            scanner.nextToken();
            nextProgramPart();
        }
        exit("program");
    }

    protected void nextProgramPart(){
        enter("program part");
        if(isInSet(scanner.getToken(), declarationTokens)){
            nextDeclaration();
        } else if(scanner.getToken() == boldProcToken){
            nextProcedure();
        } else {
            nextExpected(boldProcToken,boldIntToken,boldStringToken,openBracketToken);
        }
        exit("program part");
    }
    
    protected void nextDeclaration(){
        enter("declaration");
        nextExpected(boldIntToken, boldStringToken, openBracketToken);

        if(scanner.getToken() != nameToken){
            nextExpected(intConstantToken);
            nextExpected(closeBracketToken);
            nextExpected(boldIntToken);
        }

        nextExpected(nameToken);

        exit("declaration");
    }

    protected void nextProcedure(){
        enter("procedure");
        scanner.nextToken();

        nextExpected(nameToken);
        nextExpected(openParenToken);

        if(scanner.getToken() != closeParenToken){
            nextParameters();
        }

        nextExpected(closeParenToken);
        nextExpected(boldIntToken, boldStringToken);
        nextExpected(colonToken);

        nextBody();

        exit("procedure");
    }

    protected void nextParameters(){
        enter("parameters");

        nextDeclaration();

        while(scanner.getToken() == commaToken){
            scanner.nextToken();
            nextDeclaration();
        }

        exit("parameters");
    }

    protected void nextBody(){
        enter("body");

        if(scanner.getToken() != boldBeginToken){
            
            nextDeclaration();

            while(scanner.getToken() == semicolonToken){
                scanner.nextToken();
                nextDeclaration();
            }
        }
        
        nextBeginStatement();

        exit("body");
    }
    
    protected void nextStatement(){
        enter("statement");
        
        switch(scanner.getToken()){
            case nameToken: {nextAssignmentOrCallStatement();break;}
            case boldBeginToken: {nextBeginStatement();break;}
            case boldCodeToken: {nextCodeStatement();break;}
            case boldIfToken: {nextIfStatement();break;}
            case boldValueToken : {nextValueStatement();break;}
            case boldWhileToken: {nextWhileStatement();break;}
            default: nextExpected(nameToken,boldBeginToken,boldCodeToken,boldIfToken,boldValueToken,boldWhileToken);
        }
        
        exit("statement");
    }
    
    protected void nextAssignmentOrCallStatement(){
        enter("assignment or call statement");
        
        nextExpected(nameToken);
        
        switch(scanner.getToken()){
            case openParenToken: {
                scanner.nextToken();
                nextArguments();
                nextExpected(closeParenToken);
                break;
            }
            case openBracketToken: {
                scanner.nextToken();
                nextExpression();
                nextExpected(closeBracketToken);
            }
            case colonEqualToken: {
                scanner.nextToken();
                nextExpression();
                break;
            }
        }
        
        exit("assignment or call statement");
    }
    
    protected void nextBeginStatement(){
        enter("begin statement");
        
        nextExpected(boldBeginToken);
        
        if(scanner.getToken() != boldEndToken){
            
            nextStatement();
            
            while(scanner.getToken() == semicolonToken){
                scanner.nextToken();
                nextStatement();
            }
        }
        
        nextExpected(boldEndToken);
        
        exit("begin statement");
    }
    
    protected void nextCodeStatement(){
        enter("code statement");
        
        nextExpected(boldCodeToken);
        nextExpected(stringConstantToken);
        
        exit("code statement");
    }
    
    protected void nextIfStatement(){
        enter("if statement");
        
        nextExpected(boldIfToken);
        nextExpression();
        
        nextExpected(boldThenToken);
        nextStatement();
        
        if(scanner.getToken() == boldElseToken){
            nextStatement();
        }
        
        exit("if statement");
    }
    
    protected void nextValueStatement(){
        enter("value statement");
        
        nextExpected(boldValueToken);
        nextExpression();
        
        exit("value statement");
    }
    
    protected void nextWhileStatement(){
        enter("while statement");
        
        nextExpected(boldWhileToken);
        nextExpression();
        
        nextExpected(boldDoToken);
        nextStatement();
        
        exit("while statement");
    }

    protected void nextExpression(){
        enter("expression");
        
        nextConjunction();
        
        while (scanner.getToken() == boldOrToken){
            scanner.nextToken();
            nextConjunction();
        }
        
        exit("expression");
    }
    
    protected void nextConjunction(){
        enter("conjunction");
        
        nextComparison();
        
        while (scanner.getToken() == boldAndToken){
            scanner.nextToken();
            nextComparison();
        }
        
        exit("conjunction");
    }
    
    protected void nextComparison(){
        enter("comparison");
        
        nextSum();
        
        if(isInSet(scanner.getToken(), comparisonOperators)){
            scanner.nextToken();
            nextSum();
        }
        
        exit("comparison");
    }
    
    protected void nextSum(){
        enter("sum");
        
        nextProduct();
        
        while(isInSet(scanner.getToken(),sumOperators)){
            scanner.nextToken();
            nextProduct();
        }
        
        exit("sum");
    }

    protected void nextProduct(){
        enter("product");

        nextTerm();

        while(isInSet(scanner.getToken(),productOperators)){
            scanner.nextToken();
            nextTerm();
        }

        exit("product");
    }

    protected void nextTerm(){
        enter("term");

        while(isInSet(scanner.getToken(),termOperators)){
            scanner.nextToken();
        }

        nextUnit();

        exit("term");
    }
    
    protected void nextUnit(){
        enter("unit");
        switch (scanner.getToken()){
            case nameToken: {
                scanner.nextToken();
                switch(scanner.getToken()){
                    case openParenToken: {
                        scanner.nextToken();
                        if(scanner.getToken() != closeParenToken){
                            nextArguments();
                        }
                        nextExpected(closeParenToken);
                        break;
                    }
                    case openBracketToken: {
                        scanner.nextToken();
                        nextExpression();
                        nextExpected(closeBracketToken);
                        break;
                    }
                    default: break;
                }
                break;
            }
            case openParenToken: {
                scanner.nextToken();
                nextExpression();
                nextExpected(closeParenToken);
                break;
            }
            case stringConstantToken:
            case intConstantToken: {scanner.nextToken();break;}
            default: {nextExpected(
                    nameToken,
                    openParenToken,
                    stringConstantToken,
                    intConstantToken);
            }
        }
    }

    protected void nextArguments(){
        enter("arguments");

        nextExpression();

        while(scanner.getToken() == commaToken){
            scanner.nextToken();
            nextExpression();
        }

        exit("arguments");
    }

    protected void nextExpected(int token) throws SnarlCompilerException{
        if(scanner.getToken() == token){
            scanner.nextToken();
        } else {
            throw new SnarlCompilerException("Expected " + tokenToString(token) + ".");
        }
    }

    protected void nextExpected(int... tokens) throws SnarlCompilerException{
        boolean expected = false;
        for (int token : tokens) {
            if (scanner.getToken() == token) {
                expected = true;
            }
        }
        if(expected){
            scanner.nextToken();
        } else {
            StringBuilder error = new StringBuilder();
            for(int i = 0; i < tokens.length - 1; i++){
                error.append(tokenToString(tokens[i]));
                error.append(" or ");
            }
            error.append(tokenToString(tokens[tokens.length - 1]));
            throw new SnarlCompilerException("Expected " + error.toString() + ".");
        }
    }
    
    public static void main(String[] args){
        Parser parser;
        Source source;
        try {
            source = new Source(new FileReader(args[0]));
        } catch (FileNotFoundException ignore) {
            throw new RuntimeException("Cannot open" + args[0] + ".");
        }
        parser = new Parser(source);
        
        try{
            parser.nextProgram();
        } catch (SnarlCompilerException e){
            parser.source.error(e.message);
        }
    }
}
