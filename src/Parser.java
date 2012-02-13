/**
 * Created by IntelliJ IDEA.
 * User: dauthimaster
 * Date: 1/28/12
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Parser extends Common{
    private Scanner scanner;
    private long comparisonOperators = makeSet(
            equalToken,
            greaterToken,
            lessToken,
            lessEqualToken,
            greaterEqualToken,
            lessGreaterToken);
    private long sumOperators = makeSet(
            plusToken,
            dashToken);
    private long productOperators = makeSet(
            starToken,
            slashToken);
    private long declarationTokens = makeSet(
            intConstantToken,
            stringConstantToken,
            openBracketToken);
    private Source source;
    
    public Parser(Source source){
        this.source = source;
        scanner = new Scanner(source);
    }

    private long makeSet(int... elements){
        long set = 0L;
        for(int element : elements){           
            set |= (1L << element);
        }
        return set;
    }
    
    //test if element is in set
    private boolean isInSet(int element, long set){
        return (set & (1L << element)) != 0L;
    }

    public void nextProgram(){
        enter("program");
        nextProgramPart();
        while (scanner.getToken() != endFileToken){
            nextExpected(semicolonToken);
            nextProgramPart();
        }
    }

    private void nextProgramPart(){
        if(isInSet(scanner.getToken(), declarationTokens)){
            nextDeclaration();
            scanner.nextToken();
        } else if(scanner.getToken() == boldProcToken){
            nextProcedure();
            scanner.nextToken();
        }
    }
    
    private void nextExpression(){
        enter("expression");
        nextConjunction();
        while (scanner.getToken() == boldOrToken) {
            scanner.nextToken();
            nextConjunction();
        }
        exit("expression");
    }
    
    private void nextUnit(){
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

    private void nextExpected(int token){
        if(scanner.getToken() == token){
            scanner.nextToken();
        } else {
            source.error("Expected " + tokenToString(token) + ".");
        }
    }
}
