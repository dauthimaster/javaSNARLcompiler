import junit.framework.*;

import java.io.StringReader;

public class ParserTests extends TestCase{
    private Parser parser;
    private Source source;
    
    public ParserTests(String name){
        super(name);

    }
    public void testExpression(){
        source = new Source(new StringReader("2 or 1"));
        parser = new Parser(source);
        
        try{
            parser.nextExpression();
        } catch (SnarlCompilerException e){
            fail("'2 or 1' is an expression " + e.message);
        }
        
    }
}
