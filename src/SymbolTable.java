/*
  SNARL/SymbolTable

  James Current
  Date: 3/20/12
  Time: 7:50 PM
 */

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

                        //TODO: comment EVERYTHING!!!
public class SymbolTable {
    protected Hashtable<String, LinkedList<Scope>> table;
    protected int level;

    public SymbolTable(){
        table = new Hashtable<String, LinkedList<Scope>>();
        level = 0;
    }

    public boolean isEmpty(){
        return level == 0;
    }
    
    public void push(){
        level++;
    }
    
    public void pop(){
        if(isEmpty()){
            throw new RuntimeException("Unable to pop, table is empty.");
        }

        if(!table.isEmpty()){
            for(Enumeration<String> names = table.keys();names.hasMoreElements();){
                String name = names.nextElement();
                LinkedList<Scope> list = table.get(name);
                
                if(list.peek().level == level){
                    Scope ignore = list.pop();
                    
                    if(list.isEmpty()){
                        table.remove(name);
                    }
                }
            }
        }

        level--;
    }
    
    public boolean isDeclared(String name){
        return table.containsKey(name);
    }

    public Descriptor getDescriptor(String name){
        if(isEmpty()){
            throw new RuntimeException("Unable to get Descriptor " + name + ", table is empty.");
        }
        
        if(!isDeclared(name)){
            throw new SnarlCompilerException(name + "is not declared.");
        }
        
        return table.get(name).peek().descriptor;
    }
    
    public void setDescriptor(String name, Descriptor descriptor){
        if(isEmpty()){
            throw new RuntimeException("Unable to set Descriptor " + name + ", table is empty.");
        }
        
        if(isDeclared(name) && table.get(name).peek().level == level){
            throw new SnarlCompilerException(name + "is declared twice.");
        }
        
        if(!isDeclared(name)){
            LinkedList<Scope> list = new LinkedList<Scope>();
            list.push(new Scope(level,descriptor));
            table.put(name,list);
        } else {
            table.get(name).push(new Scope(level,descriptor));
        }
    }
}
