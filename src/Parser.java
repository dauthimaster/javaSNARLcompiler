/**
 * Created by IntelliJ IDEA.
 * User: dauthimaster
 * Date: 1/28/12
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Parser extends Common{
    private void nextDisjunction(){
        enter("disjunction");
        nextConjunction();
        while (scanner.getToken() == boldOrToken) {
            scanner.nextToken();
            nextConjunction();
        }
        exit("disjunction");
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
