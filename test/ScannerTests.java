import junit.framework.TestCase;

import java.io.StringReader;

public class ScannerTests extends TestCase{
    public ScannerTests(String name){
        super(name);
    }
    
    public void testGetToken(){
        Source source = new Source(new StringReader("+"));
        Scanner scanner = new Scanner(source);
        
        assertEquals(scanner.getToken(), scanner.plusToken);
    }
    
    public void testIsLetter(){
        Source source = new Source(new StringReader(""));
        Scanner scanner = new Scanner(source);
        
        assertTrue(scanner.isLetter('A'));
        assertTrue(scanner.isLetter('Z'));
        assertTrue(scanner.isLetter('Q'));
        assertTrue(scanner.isLetter('a'));
        assertTrue(scanner.isLetter('z'));
        assertTrue(scanner.isLetter('q'));
        assertFalse(scanner.isLetter('8'));
    }
    
   /* public void testIsDigit(){
        Source source = new Source(new StringReader(""));
        Scanner scanner = new Scanner(source);

        for() {
            assertTrue(scanner.isDigit());
        }
    }*/
}
