
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class Global {
    private Assembler assembler;
    private Hashtable<String,Label> stringTable;
    private ArrayList<Integer> sizes;
    private ArrayList<Label> labels;

    //Constructor. Returns a new, empty Global.

    public Global(Assembler assembler){
        this.assembler = assembler;
        stringTable = new Hashtable<String, Label>();
        sizes = new ArrayList<Integer>();
        labels = new ArrayList<Label>();
    }

    //EnterString. Returns a label which is connected to the given string. If a label does not exist for the given
    //string, it creates a new one and adds it to the stringTable with a key of the given string.

    public Label enterString(String string){
        if(stringTable.containsKey(string)){
            return stringTable.get(string);
        }
        Label label = new Label("str");
        stringTable.put(string, label);
        return label;
    }

    //EnterVariable. Returns a label that is associated with the size of the given type.
    
    public Label enterVariable(Type type){
        int size = type.getSize();
        Label label = new Label("var");
        sizes.add(size);
        labels.add(label);
        return label;
    }

    //Emit. Calls assembler.emit to write MIPS code for the global strings and variables stored in this Global.
    
    public void emit(){
        assembler.emit(".data");
        for(Enumeration<String> strings = stringTable.keys(); strings.hasMoreElements();){
            String string = strings.nextElement();
            Label label = stringTable.get(string);
            
            assembler.emit(label, ".asciiz \"" + string + "\"");
        }

        for(int i = 0; i < sizes.size(); i++){
            int size = sizes.get(i);
            Label label = labels.get(i);
            
            assembler.emit(label, ".space " + size);
        }
    }
}
