import junit.framework.*;

import java.io.StringReader;

public class ParserTests extends TestCase{
    private Parser parser;
    private Source source;
    
    public ParserTests(String name){
        super(name);
    }
    
    public void testMakeSet(){
        source = new Source(new StringReader("ignored"));
        parser = new Parser(source);
        
        long oneTwoThree = parser.makeSet(1,2,3);
        long fourFiveSix = parser.makeSet(4,5,6);
        
        assertEquals((1L << 1 | 1L << 2 | 1L << 3),oneTwoThree);
        assertEquals((1L << 4 | 1L << 5 | 1L << 6),fourFiveSix);
        
        try{
            long shouldFail = parser.makeSet(65);
            fail("a long is only 64 bit");
        } catch (Throwable ignore){
            assertTrue(true);
        }
    }
    
    public void testIsInSet(){
        source = new Source(new StringReader("ignored"));
        parser = new Parser(source);
        
        long oneTwoThree = parser.makeSet(1,2,3);
        long fourFiveSix = parser.makeSet(4,5,6);
        
        assertTrue(parser.isInSet(1,oneTwoThree));
        assertTrue(parser.isInSet(2,oneTwoThree));
        assertTrue(parser.isInSet(3,oneTwoThree));
        assertTrue(parser.isInSet(4,fourFiveSix));
        assertTrue(parser.isInSet(5,fourFiveSix));
        assertTrue(parser.isInSet(6,fourFiveSix));
    }

    public void testUnit(){
        source = new Source(new StringReader(""));

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
    
    //public void test
}
