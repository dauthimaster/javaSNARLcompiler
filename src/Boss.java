/*
  Boss

  James Current
  Date: 3/6/12
  Time: 7:24 PM
 */
public class Boss {
    private BasicType person;   //Person Type
    private BasicType adult;    //Adult Type
    private BasicType child;    //Child Type
    private BasicType man;      //Man Type
    private BasicType woman;    //Woman Type
    private BasicType boy;
    private BasicType girl;

    public Boss(){
        person = new BasicType("person",Type.wordSize,null);
        adult = new BasicType("adult",Type.wordSize,person);
        child = new BasicType("child",Type.wordSize,person);
        man = new BasicType("man",Type.wordSize,adult);
        woman = new BasicType("woman",Type.wordSize,adult);
        boy = new BasicType("boy",Type.wordSize,child);
        girl = new BasicType("girl",Type.wordSize,child);
    }

    public static void main(String[] args){
        Boss boss = new Boss();
        System.out.println("A girl is a subset of person,");
        System.out.println(boss.girl.isSubtype(boss.person) + ".");
        
        ArrayType fivePerson = new ArrayType(5,boss.person);
        ArrayType fiveMan = new ArrayType(5,boss.man);
        
        System.out.println(fivePerson);
        System.out.println(fiveMan);

        System.out.println("A [5]person is a subset of [5]man,");
        System.out.println(fivePerson.isSubtype(fiveMan) + ".");

        System.out.println("A [5]man is a subset of [5]person,");
        System.out.println(fiveMan.isSubtype(fivePerson) + ".");

        ProcedureType couple = new ProcedureType();
        couple.addParameter(boss.man);
        couple.addParameter(boss.woman);
        couple.addValue(boss.child);
        
        System.out.println(couple);
        
        ProcedureType join = new ProcedureType();
        join.addParameter(boss.person);
        join.addParameter(boss.person);
        join.addValue(boss.person);
        
        System.out.println(join);
        
        System.out.println("A couple is a subset of join");
        System.out.println(couple.isSubtype(join));

        System.out.println("A join is a subset of couple");
        System.out.println(join.isSubtype(couple));
    }
}
