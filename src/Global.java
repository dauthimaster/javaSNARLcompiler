
import java.util.ArrayList;
import java.util.Hashtable;

public class Global {
    Assembler assembler;
    protected Hashtable<String,Label> stringTable;
    ArrayList<Integer> sizes;
    ArrayList<Label> labels;    //TODO: make a class that stores both a size and a label
    
    public Global(Assembler assembler){
        this.assembler = assembler;

    }

    public Label enterString(String string){
        
    }
    
    public Label enterVariable(Type type){

    }
}
