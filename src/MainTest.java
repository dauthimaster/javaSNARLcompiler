import java.io.FileNotFoundException;
import java.io.FileReader;

public class MainTest {
    public MainTest(){}
    public static void main(String[] args){
        String file = args[0];
        Compiler compiler = null;
        Source source = null;
        try {
            source = new Source(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        compiler = new Compiler(source);

        try {
            compiler.passOne();
        } catch (Exception e) {
            e.printStackTrace();
            source.error(e.getMessage());
        }
    }
}
