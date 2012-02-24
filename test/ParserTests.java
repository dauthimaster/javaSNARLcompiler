import junit.framework.*;

import java.io.StringReader;

public class ParserTests extends TestCase{
    
    public ParserTests(String name){
        super(name);
    }
    
    public void testMakeSet(){
        Source source = new Source(new StringReader("ignored"));
        Parser parser = new Parser(source);
        
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
        Source source = new Source(new StringReader("ignored"));
        Parser parser = new Parser(source);
        
        long oneTwoThree = parser.makeSet(1,2,3);
        long fourFiveSix = parser.makeSet(4,5,6);
        
        assertTrue(parser.isInSet(1,oneTwoThree));
        assertTrue(parser.isInSet(2,oneTwoThree));
        assertTrue(parser.isInSet(3,oneTwoThree));
        assertTrue(parser.isInSet(4,fourFiveSix));
        assertTrue(parser.isInSet(5,fourFiveSix));
        assertTrue(parser.isInSet(6,fourFiveSix));
    }

    public void testUnitOpenParen(){
        Source source = new Source(new StringReader("(0 or 1)"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("(0 or 1) is a unit " + e.message);
        }
    }

    public void testUnitInt(){
        Source source = new Source(new StringReader("1234"));
        Parser parser = new Parser(source);

        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("1234 is a unit " + e.message);
        }
    }

    public void testUnitString(){
        Source source = new Source(new StringReader("\"JUnit <3\""));
        Parser parser = new Parser(source);

        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("\"JUnit <3\" is a unit " + e.message);
        }
    }

    public void testUnitName(){
        Source source = new Source(new StringReader("manBearPig"));
        Parser parser = new Parser(source);

        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("manBearPig is a unit " + e.message);
        }
    }

    public void testUnitNameOpenParenNoArgs(){
        Source source = new Source(new StringReader("manBearPig()"));
        Parser parser = new Parser(source);

        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("manBearPig() is a unit " + e.message);
        }
    }

    public void testUnitNameOpenParenArgs(){
        Source source = new Source(new StringReader("manBearPig(1 or 0)"));
        Parser parser = new Parser(source);

        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("manBearPig(1 or 0) is a unit " + e.message);
        }
    }

    public void testUnitNameOpenBracket(){
        Source source = new Source(new StringReader("manBearPig[1 or 0]"));
        Parser parser = new Parser(source);

        try{
            parser.nextUnit();
        } catch (SnarlCompilerException e){
            fail("manBearPig[1 or 0] is a unit " + e.message);
        }
    }
    
    public void testUnitFailure(){
        Source source = new Source(new StringReader("manBearPig(]"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextUnit();
            fail("manBearPig(] is not a unit");
        } catch (SnarlCompilerException ignore){
            assertTrue(true);
        }
    }
    
    

    public void testExpression(){
        Source source = new Source(new StringReader("2 or 1"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextExpression();
        } catch (SnarlCompilerException e){
            fail("'2 or 1' is an expression " + e.message);
        }
        
    }
    
    //public void test
}
