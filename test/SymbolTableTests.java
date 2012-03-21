/*
SNARL/SymbolTableTests

James Current
3/20/12
 */

import junit.framework.TestCase;

public class SymbolTableTests extends TestCase{
    SymbolTable symbolTable;
    BasicType basic;
    
    public SymbolTableTests(String name){
        super(name);
    }

    protected void setUp(){
        symbolTable = new SymbolTable();
        basic = new BasicType("test",Type.wordSize,null);
    }
    
    protected void tearDown(){
        symbolTable = null;
        basic = null;
    }
    
    public void testIsEmpty(){        
        assertTrue(symbolTable.isEmpty());
        
        symbolTable.level++;
        
        assertFalse(symbolTable.isEmpty());
    }
    
    public void testPush(){
        for (int i = 1; i <= 5; i++){
            symbolTable.push();

            assertFalse(symbolTable.isEmpty());
            assertEquals(symbolTable.level,i);
        }
    }
    
    public void testPop(){        
        try{
            symbolTable.pop();
            fail("Not allowed to pop from an empty SymbolTable.");
        } catch (RuntimeException ignore) {
            assertTrue(true);
        }
        
        try{
            symbolTable.push();
            symbolTable.pop();
        } catch (RuntimeException t) {
            fail("Should be able to pop after a push " + t.getMessage());
        }
        
        symbolTable.push();
        symbolTable.setDescriptor("dude", new Descriptor(basic));
        symbolTable.setDescriptor("man", new Descriptor(basic));
        
        symbolTable.push();
        symbolTable.setDescriptor("manBearPig", new Descriptor(basic));
        symbolTable.setDescriptor("man", new Descriptor(basic));
        
        symbolTable.pop();
        
        assertTrue(symbolTable.table.containsKey("dude"));
        assertTrue(symbolTable.table.containsKey("man"));
        assertFalse(symbolTable.table.containsKey("manBearPig"));
        assertEquals(symbolTable.table.get("man").size(), 1);
        
        symbolTable.pop();

        assertEquals(symbolTable.level, 0);
    }
    
    public void testIsDeclared(){
        symbolTable.push();
        symbolTable.setDescriptor("dude", new Descriptor(basic));
        symbolTable.setDescriptor("man", new Descriptor(basic));

        symbolTable.push();
        symbolTable.setDescriptor("manBearPig", new Descriptor(basic));
        symbolTable.setDescriptor("man", new Descriptor(basic));
        
        assertTrue(symbolTable.isDeclared("man"));
        assertTrue(symbolTable.isDeclared("dude"));
        assertTrue(symbolTable.isDeclared("manBearPig"));
        assertFalse(symbolTable.isDeclared("fail"));
    }

    public void testGetDescriptor(){
        try{
            symbolTable.getDescriptor("fail");
            fail("Should not be able to call getDescriptor on an empty SymbolTable");
        } catch (RuntimeException ignore) {
            assertTrue(true);
        }
        
        symbolTable.push();
        symbolTable.setDescriptor("dude", new Descriptor(basic));
        
        try{
            symbolTable.getDescriptor("man");
            fail("Should fail because man does not appear in the SymbolTable");
        } catch (SnarlCompilerException ignore) {
            assertTrue(true);
        }

        assertEquals(symbolTable.table.get("dude").peek().descriptor, symbolTable.getDescriptor("dude"));

        symbolTable.setDescriptor("man", new Descriptor(basic));
        symbolTable.push();
        symbolTable.setDescriptor("manBearPig", new Descriptor(basic));
        symbolTable.setDescriptor("man", new Descriptor(basic));
        
        assertEquals(symbolTable.table.get("man").peek().descriptor, symbolTable.getDescriptor("man"));
        assertEquals(symbolTable.table.get("dude").peek().descriptor, symbolTable.getDescriptor("dude"));
        assertEquals(symbolTable.table.get("manBearPig").peek().descriptor, symbolTable.getDescriptor("manBearPig"));
    }
    
    public void testSetDescriptor(){
        try{
            symbolTable.setDescriptor("fail", new Descriptor(basic));
            fail("Should not be able to call setDescriptor on an empty SymbolTable");
        } catch (RuntimeException ignore) {
            assertTrue(true);
        }
        
        symbolTable.push();
        symbolTable.setDescriptor("dude", new Descriptor(basic));
        
        assertTrue(symbolTable.table.containsKey("dude"));

        try{
            symbolTable.setDescriptor("dude", new Descriptor(basic));
            fail("dude has already been declared in this scope");
        } catch (SnarlCompilerException ignore) {
            assertTrue(true);
        }

        symbolTable.setDescriptor("man", new Descriptor(basic));

        assertTrue(symbolTable.table.containsKey("man"));

        symbolTable.push();
        symbolTable.setDescriptor("manBearPig", new Descriptor(basic));
        symbolTable.setDescriptor("man", new Descriptor(basic));

        assertTrue(symbolTable.table.containsKey("manBearPig"));
        assertTrue(symbolTable.table.containsKey("man"));
        assertEquals(symbolTable.table.get("man").peek().level, symbolTable.level);
    }
}
