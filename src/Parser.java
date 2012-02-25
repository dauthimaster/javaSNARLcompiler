import java.io.FileNotFoundException;
import java.io.FileReader;

/*
SNARL/Parser

James Current
1/30/12
 */
public class Parser extends Common{
    protected Scanner scanner;                      //Parser pulls tokens from Scanner
    protected long comparisonOperators = makeSet(   //Set of comparison operators
            equalToken,
            greaterToken,
            lessToken,
            lessEqualToken,
            greaterEqualToken,
            lessGreaterToken);
    protected long sumOperators = makeSet(          //Set of sum operators
            plusToken,
            dashToken);
    protected long productOperators = makeSet(      //Set of product operators
            starToken,
            slashToken);
    protected long termOperators = makeSet(         //Set of term operators
            dashToken,
            boldNotToken);
    protected long declarationTokens = makeSet(     //Set of tokens that can start a declaration
            boldIntToken,
            boldStringToken,
            openBracketToken);
    protected Source source;                        //Source for calling error in main

    //Constructor. Returns a new Parser positioned at the first unignored token.
    
    public Parser(Source source){
        this.source = source;
        scanner = new Scanner(source);
    }
    
   /* protected void errorRecoveryExample(){
        if(!isInSet(scanner.getToken(), declarationTokens)){
            source.recoverableError("message");
            while (! isInSet(scanner.getToken(), declarationFollowSet)){
                source.nextChar();
            }
        }
    }
     */
    //MakeSet. Returns a set of the elements.

    protected long makeSet(int... elements){
        long set = 0L;
        for(int element : elements){           
            set |= (1L << element);
        }
        return set;
    }
    
    //IsInSet. Tests if element is in set.

    protected boolean isInSet(int element, long set){
        return (set & (1L << element)) != 0L;
    }

    //NextProgram. Parses the next program.

    public void nextProgram(){
        enter("program");
        nextProgramPart();
        while (scanner.getToken() == semicolonToken){
            scanner.nextToken();
            nextProgramPart();
        }
        nextExpected(endFileToken);
        exit("program");
    }

    //NextProgramPart. Parses the next program part.

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

    //NextDeclaration. Parses the next declaration.
    
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

    //NextProcedure. Parses the next procedure.

    protected void nextProcedure(){
        enter("procedure");
        nextExpected(boldProcToken);
        nextExpected(nameToken);

        nextParameters();

        nextExpected(boldIntToken, boldStringToken);
        nextExpected(colonToken);

        nextBody();

        exit("procedure");
    }

    //NextParameters. Parses the next parameters.

    protected void nextParameters(){
        enter("parameters");

        nextExpected(openParenToken);

        if (scanner.getToken() != closeParenToken) {
            nextDeclaration();

            while(scanner.getToken() == commaToken){
                scanner.nextToken();
                nextDeclaration();
            }
        }

        nextExpected(closeParenToken);

        exit("parameters");
    }

    //NextBody. Parses the next body.

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

    //NextStatement. Parses the next statement.
    
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

    //NextAssignmentOrCallStatement. Parses the next assignment or call statement.
    
    protected void nextAssignmentOrCallStatement(){
        enter("assignment or call statement");
        
        nextExpected(nameToken);
        
        switch(scanner.getToken()){
            case openParenToken: {nextArguments();break;}
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
            default: {
                nextExpected(
                        openParenToken,
                        openBracketToken,
                        colonEqualToken);
            }
        }
        
        exit("assignment or call statement");
    }

    //NextBeginStatement. Parses the next begin statement.
    
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

    //NextCodeStatement. Parses the next code statement.
    
    protected void nextCodeStatement(){
        enter("code statement");
        
        nextExpected(boldCodeToken);
        nextExpected(stringConstantToken);
        
        exit("code statement");
    }

    //NextIfStatement. Parses the next if statement.
    
    protected void nextIfStatement(){
        enter("if statement");
        
        nextExpected(boldIfToken);
        nextExpression();
        
        nextExpected(boldThenToken);
        nextStatement();
        
        if(scanner.getToken() == boldElseToken){
            scanner.nextToken();
            nextStatement();
        }
        
        exit("if statement");
    }

    //NextValueStatement. Parses the next value statement.
    
    protected void nextValueStatement(){
        enter("value statement");
        
        nextExpected(boldValueToken);
        nextExpression();
        
        exit("value statement");
    }

    //NextWhileStatement. Parses the next while statement.
    
    protected void nextWhileStatement(){
        enter("while statement");
        
        nextExpected(boldWhileToken);
        nextExpression();
        
        nextExpected(boldDoToken);
        nextStatement();
        
        exit("while statement");
    }

    //NextExpression. Parses the next expression.

    protected void nextExpression(){
        enter("expression");
        
        nextConjunction();
        
        while (scanner.getToken() == boldOrToken){
            scanner.nextToken();
            nextConjunction();
        }
        
        exit("expression");
    }

    //NextConjunction. Parses the next conjunction.
    
    protected void nextConjunction(){
        enter("conjunction");
        
        nextComparison();
        
        while (scanner.getToken() == boldAndToken){
            scanner.nextToken();
            nextComparison();
        }
        
        exit("conjunction");
    }

    //NextComparison. Parses the next comparison.
    
    protected void nextComparison(){
        enter("comparison");
        
        nextSum();
        
        if(isInSet(scanner.getToken(), comparisonOperators)){
            scanner.nextToken();
            nextSum();
        }

        if(isInSet(scanner.getToken(), comparisonOperators)){
            throw new SnarlCompilerException("Chaining comparison operators is not allowed");
        }
        
        exit("comparison");
    }

    //NextSum. Parses the next sum.

    protected void nextSum(){
        enter("sum");
        
        nextProduct();
        
        while(isInSet(scanner.getToken(),sumOperators)){
            scanner.nextToken();
            nextProduct();
        }
        
        exit("sum");
    }

    //NextProduct. Parses the next product.

    protected void nextProduct(){
        enter("product");

        nextTerm();

        while(isInSet(scanner.getToken(),productOperators)){
            scanner.nextToken();
            nextTerm();
        }

        exit("product");
    }

    //NextTerm. Parses the next term.

    protected void nextTerm(){
        enter("term");

        while(isInSet(scanner.getToken(),termOperators)){
            scanner.nextToken();
        }

        nextUnit();

        exit("term");
    }

    //NextUnit. Parses the next unit.
    
    protected void nextUnit(){
        enter("unit");
        switch (scanner.getToken()){
            case nameToken: {
                scanner.nextToken();
                switch(scanner.getToken()){
                    case openParenToken: {nextArguments();break;}
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

        exit("unit");
    }

    //NextArguments. Parses the next arguments.

    protected void nextArguments(){
        enter("arguments");
        
        nextExpected(openParenToken);

        if(scanner.getToken() != closeParenToken){
            nextExpression();

            while(scanner.getToken() == commaToken){
                scanner.nextToken();
                nextExpression();
            }
        }

        nextExpected(closeParenToken);

        exit("arguments");
    }

    //NextExpected(int token). Checks to see if the current token is the token passed, if not throws an exception.

    protected void nextExpected(int token) throws SnarlCompilerException{
        if(scanner.getToken() == token){
            scanner.nextToken();
        } else {
            throw new SnarlCompilerException("Expected " + tokenToString(token) + ".");
        }
    }

    //NextExpected(int... tokens). Checks to see if the current token is in tokens, if not throws an exception.

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

    //Main.
    
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
