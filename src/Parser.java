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
        while (scanner.getToken() != endFileToken){
            nextExpected(semicolonToken);
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
            source.error("Invalid token: " + tokenToString(scanner.getToken()));
        }
        exit("program part");
    }
    
    protected void nextDeclaration(){
        enter("declaration");
        scanner.nextToken();

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
       
        if(scanner.getToken() == boldIntToken){
            scanner.nextToken();
        } else {
            nextExpected(boldStringToken);
        }

        nextExpected(colonToken);
        nextBody();

        exit("procedure");
    }

    protected void nextParameters(){
        enter("parameters");



        exit("parameters");
    }
    
    protected void nextExpression(){
        enter("expression");
        nextConjunction();
        while (scanner.getToken() == boldOrToken) {
            scanner.nextToken();
            nextConjunction();
        }
        exit("expression");
    }
    
    protected void nextUnit(){
        enter("unit");
        switch (scanner.getToken()){
            case nameToken: {nextCallOrVariable();break;}
            case openParenToken: {
                scanner.nextToken();
                nextExpression();
                nextExpected(closeParenToken);
                break;
            }
            case stringConstantToken:
            case intConstantToken: {scanner.nextToken();break;}
            default: {source.error("Unexpected symbol");break;}
        }
    }

    protected void nextExpected(int token){
        if(scanner.getToken() == token){
            scanner.nextToken();
        } else {
            source.error("Expected " + tokenToString(token) + ".");
        }
    }

    protected void nextExpected(int[] tokens){
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
            source.error("Expected " + error.toString() + ".");
        }
    }
}
