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
    
    public void testArgumentsSingle(){
        Source source = new Source(new StringReader("(1 or 0)"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextArguments();
        } catch (SnarlCompilerException e){
            fail("1 or 0 is an argument " + e.message);
        }
    }

    public void testArgumentsMulti(){
        Source source = new Source(new StringReader("(1 or 0, 2, 13)"));
        Parser parser = new Parser(source);

        try{
            parser.nextArguments();
        } catch (SnarlCompilerException e){
            fail("1 or 0, 2, 13 are arguments " + e.message);
        }
    }

    public void testArgumentsFail(){
        Source source = new Source(new StringReader("(1 or 0 2)"));
        Parser parser = new Parser(source);

        try{
            parser.nextArguments();
            fail("1 or 0 2 is missing a comma");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }
    
    public void testTermNoOp(){
        Source source = new Source(new StringReader("2"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextTerm();
        } catch (SnarlCompilerException e){
            fail("2 is a term " + e.message);
        }
    }

    public void testTermOneOp(){
        Source source = new Source(new StringReader("-2"));
        Parser parser = new Parser(source);

        try{
            parser.nextTerm();
        } catch (SnarlCompilerException e){
            fail("-2 is a term " + e.message);
        }
    }

    public void testTermMultiOp(){
        Source source = new Source(new StringReader("-not-not-2"));
        Parser parser = new Parser(source);

        try{
            parser.nextTerm();
        } catch (SnarlCompilerException e){
            fail("-not-not-2 is a term " + e.message);
        }
    }
    
    public void testProductSingle(){
        Source source = new Source(new StringReader("5"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextProduct();
        } catch (SnarlCompilerException e){
            fail("5 is a product " + e.message);
        }
    }
    
    public void testProductNorm(){
        Source source = new Source(new StringReader("5 * 7"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextProduct();
        } catch (SnarlCompilerException e){
            fail("5 * 7 is a product " + e.message);
        }
    }

    public void testProductMulti(){
        Source source = new Source(new StringReader("5 * 7 / 5"));
        Parser parser = new Parser(source);

        try{
            parser.nextProduct();
        } catch (SnarlCompilerException e){
            fail("5 * 7 / 5 is a product " + e.message);
        }
    }
    
    public void testSumSingle(){
        Source source = new Source(new StringReader("3"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextSum();
        } catch (SnarlCompilerException e){
            fail("3 is a sum " + e.message);
        }
    }

    public void testSumNorm(){
        Source source = new Source(new StringReader("3 + 5"));
        Parser parser = new Parser(source);

        try{
            parser.nextSum();
        } catch (SnarlCompilerException e){
            fail("3 + 5 is a sum " + e.message);
        }
    }

    public void testSumMulti(){
        Source source = new Source(new StringReader("3 + 5 - 8"));
        Parser parser = new Parser(source);

        try{
            parser.nextSum();
        } catch (SnarlCompilerException e){
            fail("3 + 5 - 8 is a sum " + e.message);
        }
    }
    
    public void testComparisonSingle(){
        Source source = new Source(new StringReader("42"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextComparison();
        } catch (SnarlCompilerException e){
            fail("42 is a comparison " + e.message);
        }
    }

    public void testComparisonNorm(){
        Source source = new Source(new StringReader("42 > 11"));
        Parser parser = new Parser(source);

        try{
            parser.nextComparison();
        } catch (SnarlCompilerException e){
            fail("42 > 11 is a comparison " + e.message);
        }
    }

    public void testComparisonFail(){
        Source source = new Source(new StringReader("42 > 11 > -5"));
        Parser parser = new Parser(source);

        try{
            parser.nextComparison();
            fail("42 > 11 > -5 is not a comparison");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }
    
    public void testConjunctionSingle(){
        Source source = new Source(new StringReader("29"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextConjunction();
        } catch (SnarlCompilerException e){
            fail("29 is a conjunction " + e.message);
        }
    }

    public void testConjunctionNorm(){
        Source source = new Source(new StringReader("29 and 404"));
        Parser parser = new Parser(source);

        try{
            parser.nextConjunction();
        } catch (SnarlCompilerException e){
            fail("29 and 404 is a conjunction " + e.message);
        }
    }

    public void testConjunctionMulti(){
        Source source = new Source(new StringReader("29 and 404 and 500"));
        Parser parser = new Parser(source);

        try{
            parser.nextConjunction();
        } catch (SnarlCompilerException e){
            fail("29 and 404 and 500 is a conjunction " + e.message);
        }
    }

    public void testExpressionSingle(){
        Source source = new Source(new StringReader("202"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextExpression();
        } catch (SnarlCompilerException e){
            fail("202 is an expression " + e.message);
        }
    }
                   
    public void testExpressionNorm(){
        Source source = new Source(new StringReader("2 or 1"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextExpression();
        } catch (SnarlCompilerException e){
            fail("2 or 1 is an expression " + e.message);
        }
    }

    public void testExpressionMulti(){
        Source source = new Source(new StringReader("2 or 1 or 0"));
        Parser parser = new Parser(source);

        try{
            parser.nextExpression();
        } catch (SnarlCompilerException e){
            fail("2 or 1 or 0 is an expression " + e.message);
        }
    }
    
    public void testWhileStatementNorm(){
        Source source = new Source(new StringReader("while 1 do begin end"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextWhileStatement();
        } catch (SnarlCompilerException e){
            fail("while 1 do begin end is a while statement " + e.message);
        }
    }

    public void testWhileStatementNested(){
        Source source = new Source(new StringReader("while 1 do while 0 do begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextWhileStatement();
        } catch (SnarlCompilerException e){
            fail("while 1 do while 0 do begin end is a while statement " + e.message);
        }
    }

    public void testWhileStatementFailWhile(){
        Source source = new Source(new StringReader("wile 1 do begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextWhileStatement();
            fail("wile 1 do begin end is not a while statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testWhileStatementFailDo(){
        Source source = new Source(new StringReader("while 1 o begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextWhileStatement();
            fail("while 1 o begin end is not a while statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testValueStatement(){
        Source source = new Source(new StringReader("value 21"));
        Parser parser = new Parser(source);

        try{
            parser.nextValueStatement();         
        } catch (SnarlCompilerException e){
            fail("value 21 is a value statement " + e.message);
        }
    }

    public void testValueStatementFail(){
        Source source = new Source(new StringReader("val 21"));
        Parser parser = new Parser(source);

        try{
            parser.nextValueStatement();
            fail("val 21 is not a value statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }
    
    public void testIfStatementNoElse(){
        Source source = new Source(new StringReader("if 1 then begin end"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextIfStatement();
        } catch (SnarlCompilerException e){
            fail("if 1 then begin end is an if statement " + e.message);
        }
    }

    public void testIfStatementWithElse(){
        Source source = new Source(new StringReader("if 1 then begin end else begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextIfStatement();
        } catch (SnarlCompilerException e){
            fail("if 1 then begin end else begin end is an if statement " + e.message);
        }
    }

    public void testIfStatementWithDanglingElse(){
        Source source = new Source(new StringReader(
                "if 1 then if 0 then begin end else begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextIfStatement();
        } catch (SnarlCompilerException e){
            fail("if 1 then if 0 then begin end else begin end is an if statement " + e.message);
        }
    }

    public void testIfStatementFailIf(){
        Source source = new Source(new StringReader("f 1 then begin end else begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextIfStatement();
            fail("f 1 then begin end else begin end is not an if statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testIfStatementFailThen(){
        Source source = new Source(new StringReader("if 1 ten begin end else begin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextIfStatement();
            fail("if 1 ten begin end else begin end is not an if statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }
    
    public void testCodeStatement(){
        Source source = new Source(new StringReader("code \"code goes here\""));
        Parser parser = new Parser(source);
        
        try{
            parser.nextCodeStatement();
        } catch (SnarlCompilerException e){
            fail("code \"code goes here\" is a code statement " + e.message);
        }
    }

    public void testCodeStatementFail(){
        Source source = new Source(new StringReader("ode \"code goes here\""));
        Parser parser = new Parser(source);

        try{
            parser.nextCodeStatement();
            fail("ode \"code goes here\" is not a code statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }
    
    public void testBeginStatementNoStatement(){
        Source source = new Source(new StringReader("begin end"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextBeginStatement();
        } catch (SnarlCompilerException e){
            fail("begin end is a begin statement " + e.message);
        }
    }

    public void testBeginStatementWithStatement(){
        Source source = new Source(new StringReader("begin code \"code goes here\" end"));
        Parser parser = new Parser(source);

        try{
            parser.nextBeginStatement();
        } catch (SnarlCompilerException e){
            fail("begin code \"code goes here\" end is a begin statement " + e.message);
        }
    }

    public void testBeginStatementWithMultiStatement(){
        Source source = new Source(new StringReader("begin code \"blah\"; code \"blah\" end"));
        Parser parser = new Parser(source);

        try{
            parser.nextBeginStatement();
        } catch (SnarlCompilerException e){
            fail("begin code \"blah\"; code \"blah\" end is a begin statement " + e.message);
        }
    }

    public void testBeginStatementFailBegin(){
        Source source = new Source(new StringReader("bgin end"));
        Parser parser = new Parser(source);

        try{
            parser.nextBeginStatement();
            fail("bgin end is not a begin statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testBeginStatementFailMultiNoSemicolon(){
        Source source = new Source(new StringReader("begin code \"blah\" code \"blah\" end"));
        Parser parser = new Parser(source);

        try{
            parser.nextBeginStatement();
            fail("begin code \"blah\" code \"blah\" end is not a begin statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testBeginStatementFailEnd(){
        Source source = new Source(new StringReader("begin ed"));
        Parser parser = new Parser(source);

        try{
            parser.nextBeginStatement();
            fail("begin ed is not a begin statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }       
    
    public void testAssignmentOrCallStatementParenNoArgs(){
        Source source = new Source(new StringReader("meow()"));
        Parser parser = new Parser(source);
        
        try{
            parser.nextAssignmentOrCallStatement();
        } catch (SnarlCompilerException e){
            fail("meow() is an assignment or call statement " + e.message);
        }
    }

    public void testAssignmentOrCallStatementParenWithArgs(){
        Source source = new Source(new StringReader("meow(1)"));
        Parser parser = new Parser(source);

        try{
            parser.nextAssignmentOrCallStatement();
        } catch (SnarlCompilerException e){
            fail("meow(1) is an assignment or call statement " + e.message);
        }
    }

    public void testAssignmentOrCallStatementBracket(){
        Source source = new Source(new StringReader("meow[1] := 42"));
        Parser parser = new Parser(source);

        try{
            parser.nextAssignmentOrCallStatement();
        } catch (SnarlCompilerException e){
            fail("meow[1] := 42 is an assignment or call statement " + e.message);
        }
    }

    public void testAssignmentOrCallStatementColonEqual(){
        Source source = new Source(new StringReader("meow := 42"));
        Parser parser = new Parser(source);

        try{
            parser.nextAssignmentOrCallStatement();
        } catch (SnarlCompilerException e){
            fail("meow := 42 is an assignment or call statement " + e.message);
        }
    }

    public void testAssignmentOrCallStatementFailName(){
        Source source = new Source(new StringReader("()"));
        Parser parser = new Parser(source);

        try{
            parser.nextAssignmentOrCallStatement();
            fail("() is not an assignment or call statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testAssignmentOrCallStatementFailNamePlusInvalid(){
        Source source = new Source(new StringReader("meow)"));
        Parser parser = new Parser(source);

        try{
            parser.nextAssignmentOrCallStatement();
            fail("meow) is not an assignment or call statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testAssignmentOrCallStatementFailNoCloseBracket(){
        Source source = new Source(new StringReader("meow[1 := 404"));
        Parser parser = new Parser(source);

        try{
            parser.nextAssignmentOrCallStatement();
            fail("meow[1 := 404 is not an assignment or call statement.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }

    public void testStatementName(){
        Source source = new Source(new StringReader("meow := 1"));
        Parser parser = new Parser(source);

        assertTrue(true);
    }
}
