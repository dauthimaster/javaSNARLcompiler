import junit.framework.*;

public class AllocatorTests extends TestCase{
    public AllocatorTests(String name){
        super(name);
    }
    
    public void testRequestEmptyList(){
        Allocator allocator = new Allocator();
        
        for(int i = 0; i < 8; i++){
            allocator.request();
        }
        
        try{
            allocator.request();
            fail("There should not be any more registers.");
        } catch (SnarlCompilerException e){
            assertTrue(true);
        }
    }
    
    public void testRequest(){
        Allocator allocator = new Allocator();

        Allocator.Register test = allocator.registers;
        Allocator.Register pop = allocator.request();

        assertEquals(test, pop);
        assertTrue(pop.isUsed());
    }
    
    public void testReleaseAlreadyReleased(){
        Allocator allocator = new Allocator();

        try {
            allocator.release(allocator.registers);
            fail("Top of Registers stack is already released");
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }
    
    public void testRelease(){
        Allocator allocator = new Allocator();

        Allocator.Register register = allocator.request();
        allocator.release(register);
        
        assertEquals(allocator.registers, register);
        assertFalse(register.isUsed());
    }
}
