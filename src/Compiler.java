/*
SNARL/Compiler

James Current
1/30/12
 */
public class Compiler extends Common{
    protected Scanner scanner;                      //Compiler pulls tokens from Scanner
    protected Source source;                        //Used to make 2nd pass of input stream
    protected long comparisonOperators = makeSet(   //Set of comparison operators
            equalToken,
            greaterToken,
            lessToken,
            lessEqualToken,
            greaterEqualToken,
            lessGreaterToken);
    protected long sumOperators = makeSet(          //Set of sum operators
            plusToken,
            dashToken);
    protected long productOperators = makeSet(      //Set of product operators
            starToken,
            slashToken);
    protected long termOperators = makeSet(         //Set of term operators
            dashToken,
            boldNotToken);
    protected long declarationTokens = makeSet(     //Set of tokens that can start a declaration
            boldIntToken,
            boldStringToken,
            openBracketToken);
    protected SymbolTable table;
    protected BasicType intType;
    protected BasicType stringType;
    protected Type procValType;
    protected Allocator allocator;
    protected Assembler assembler;
    protected Global global;

    //  GLOBAL ARRAY DESCRIPTOR. Describe a global array.

    private class GlobalArrayDescriptor extends GlobalDescriptor
    {

//  Constructor.

        private GlobalArrayDescriptor(Type type, Label label)
        {
            this.type = type;
            this.label = label;
        }

//  LVALUE. An array can't be alone on the left side of an assignment.

        protected Allocator.Register lvalue()
        {
            throw new SnarlCompilerException("Cannot assign an array.");
        }

//  RVALUE. Return a register that holds the address of the global array.

        protected Allocator.Register rvalue()
        {
            Allocator.Register register = allocator.request();
            assembler.emit("la", register, label);
            return register;
        }

//  TO STRING. For debugging.

        public String toString()
        {
            return "[GlobalArrayDescriptor " + type + " " + label + "]";
        }
    }

//  GLOBAL PROCEDURE DESCRIPTOR. Describe a procedure.

    private class GlobalProcedureDescriptor extends GlobalDescriptor
    {

//  Constructor.

        private GlobalProcedureDescriptor(Type type, Label label)
        {
            this.type = type;
            this.label = label;
        }

//  LVALUE. A procedure can't be alone on the left side of an assignment.

        protected Allocator.Register lvalue()
        {
            throw new SnarlCompilerException("Illegal Procedure Call.");
        }

//  RVALUE. A procedure can't be used as a variable name either.

        protected Allocator.Register rvalue()
        {
            throw new SnarlCompilerException("Illegal Procedure Call.");
        }

//  TO STRING. For debugging.

        public String toString()
        {
            return "[GlobalProcedureDescriptor " + type + " " + label + "]";
        }
    }

//  GLOBAL VARIABLE DESCRIPTOR. Describe a global variable that's not an array.

    private class GlobalVariableDescriptor extends GlobalDescriptor
    {

//  Constructor.

        private GlobalVariableDescriptor(Type type, Label label)
        {
            this.type = type;
            this.label = label;
        }

//  LVALUE. Return a register that holds the global variable's address.

        protected Allocator.Register lvalue()
        {
            Allocator.Register register = allocator.request();
            assembler.emit("la", register, label);
            return register;
        }

//  RVALUE. Return a register that holds the global variable's value.

        protected Allocator.Register rvalue()
        {
            Allocator.Register register = allocator.request();
            assembler.emit("la", register, label);
            if(type == intType){
                assembler.emit("lw", register, 0, register);
            }
            return register;
        }

//  TO STRING. For debugging.

        public String toString()
        {
            return "[GlobalVariableDescriptor " + type + " " + label + "]";
        }
    }

//  LOCAL ARRAY DESCRIPTOR. Describe a local array variable.

    private class LocalArrayDescriptor extends LocalDescriptor
    {

//  Constructor.

        private LocalArrayDescriptor(Type type, int offset)
        {
            this.type = type;
            this.offset = offset;
        }

//  LVALUE. An array can't be alone on the left side of an assignment.

        protected Allocator.Register lvalue()
        {
            throw new SnarlCompilerException("Cannot assign an array.");
        }

//  RVALUE. Return a register that holds the address of a local array variable.

        protected Allocator.Register rvalue()
        {
            Allocator.Register register = allocator.request();
            assembler.emit("addi", register, allocator.fp, offset);
            return register;
        }

//  TO STRING. For debugging.

        public String toString()
        {
            return "[LocalArrayDescriptor " + type + " " + offset + "]";
        }
    }

//  LOCAL VARIABLE DESCRIPTOR. Describe a local variable that's not an array.

    private class LocalVariableDescriptor extends LocalDescriptor
    {

//  Constructor.

        private LocalVariableDescriptor(Type type, int offset)
        {
            this.type = type;
            this.offset = offset;
        }

//  LVALUE. Return a register that holds the address of the local variable.

        protected Allocator.Register lvalue()
        {
            Allocator.Register register = allocator.request();
            assembler.emit("addi", register, allocator.fp, offset);
            return register;
        }

//  RVALUE. Return a register that holds the value of the local variable.

        protected Allocator.Register rvalue()
        {
            Allocator.Register register = allocator.request();
            assembler.emit("lw", register, offset, allocator.fp);
            return register;
        }

//  TO STRING. For debugging.

        public String toString()
        {
            return "[LocalVariableDescriptor " + type + " " + offset + "]";
        }
    }

    //Constructor. Returns a new Compiler positioned at the first unignored token.
    
    public Compiler(Source source){
        scanner = new Scanner(source);
        this.source = source;
        table = new SymbolTable();
        table.push();
        intType = new BasicType("int",Type.wordSize,null);
        stringType = new BasicType("string",Type.addressSize,null);
        allocator = new Allocator();
        assembler = new Assembler("out.asm");
        global = new Global(assembler);
    }
    
   /* protected void errorRecoveryExample(){
        if(!isInSet(scanner.getToken(), declarationTokens)){
            source.recoverableError("message");
            while (! isInSet(scanner.getToken(), declarationFollowSet)){
                source.nextChar();
            }
        }
    }
     */

    //PassOne. Runs pass one of the parser, parsing procedure names and signatures only.

    public void passOne(){
        enter("pass one");
        while(scanner.getToken() != endFileToken){
            if(scanner.getToken() == boldProcToken){
                scanner.nextToken();
                ProcedureType procedure = new ProcedureType();
                Label label = new Label("proc");
                String name = scanner.getString();
                nextExpected(nameToken);
                nextExpected(openParenToken);
                while(scanner.getToken() != closeParenToken){
                    switch (scanner.getToken()){
                        case openBracketToken: {
                            scanner.nextToken();
                            ArrayType array = new ArrayType(scanner.getInt(), intType);
                            nextExpected(intConstantToken);
                            nextExpected(closeBracketToken);
                            nextExpected(boldIntToken);
                            procedure.addParameter(array);
                            break;
                        }
                        case boldIntToken: {
                            scanner.nextToken();
                            procedure.addParameter(intType);
                            break;
                        }
                        case boldStringToken: {
                            scanner.nextToken();
                            procedure.addParameter(stringType);
                            break;
                        }
                        default: {
                            nextExpected(
                                    openBracketToken,
                                    boldIntToken,
                                    boldStringToken
                            );
                        }
                    }
                    nextExpected(nameToken);
                }
                nextExpected(closeParenToken);
                switch (scanner.getToken()){
                    case boldIntToken: {
                        procedure.addValue(intType);
                        break;
                    }
                    case boldStringToken: {
                        procedure.addValue(stringType);
                        break;
                    }
                    default: {
                        nextExpected(
                                boldIntToken,
                                boldStringToken
                        );
                    }
                }
                table.setDescriptor(name,new GlobalProcedureDescriptor(procedure, label));
            }
            scanner.nextToken();
        }
        source.reset();
        scanner.nextToken();
        exit("pass one");
        passTwo();
    }
    
    //PassTwo. Runs pass two of the parser, parsing the bulk of the input.
    
    protected void passTwo(){
        enter("pass two");
        nextProgram();
        exit("pass two");
    }

    //TypeCheck. Tests if descriptor is of type type.

    protected void typeCheck(Descriptor descriptor, Type type){
        if(!descriptor.getType().isSubtype(type)){
            throw new SnarlCompilerException("Expression of type " + type + " expected.");
        }
    }

    //MakeSet. Returns a set of the elements.

    protected long makeSet(int... elements){
        long set = 0L;
        for(int element : elements){           
            set |= (1L << element);
        }
        return set;
    }
    
    //IsInSet. Tests if element is in set.

    protected boolean isInSet(int element, long set){
        return (set & (1L << element)) != 0L;
    }

    //NextProgram. Parses the next program.

    protected void nextProgram(){
        enter("program");
        nextProgramPart();
        while (scanner.getToken() == semicolonToken){
            scanner.nextToken();
            nextProgramPart();
        }
        nextExpected(endFileToken);
        exit("program");
    }

    //NextProgramPart. Parses the next program part.

    protected void nextProgramPart(){
        enter("program part");
        if(isInSet(scanner.getToken(), declarationTokens)){
            nextGlobalDeclaration();
        } else if(scanner.getToken() == boldProcToken){
            nextProcedure();
        } else {
            nextExpected(boldProcToken,boldIntToken,boldStringToken,openBracketToken);
        }
        exit("program part");
    }
    
    //NextGlobalDeclaration. Parses the next global declaration.
    
    protected void nextGlobalDeclaration(){
        enter("global declaration");
        switch (scanner.getToken()){
            case boldIntToken: {
                scanner.nextToken();
                String name = scanner.getString();
                nextExpected(nameToken);
                Label label = global.enterVariable(intType);
                table.setDescriptor(name, new GlobalVariableDescriptor(intType, label));
                break;
            }
            case boldStringToken: {
                //TODO ask Moen...
            }
            case openBracketToken: {
                scanner.nextToken();
                ArrayType type = new ArrayType(scanner.getInt(), intType);
                nextExpected(intConstantToken);
                nextExpected(closeBracketToken);
                nextExpected(boldIntToken);
                String name = scanner.getString();
                nextExpected(nameToken);
                Label label = global.enterVariable(type);
                table.setDescriptor(name, new GlobalArrayDescriptor(type, label));
                break;
            }
            default: {
                nextExpected(
                        boldIntToken,
                        boldStringToken,
                        openBracketToken
                );
            }
        }
        exit("global declaration");
    }

    //NextParameterDeclaration. Parses the next parameter declaration.

    protected void nextParameterDeclaration(int arity){
        enter("parameter declaration");
        switch (scanner.getToken()){
            case boldIntToken: {
                scanner.nextToken();
                String name = scanner.getString();
                nextExpected(nameToken);
                Label label = global.enterVariable(intType);
                table.setDescriptor(name, new LocalVariableDescriptor(intType, arity * -1 * Type.wordSize));
                break;
            }
            case boldStringToken: {
                //TODO ask Moen...
            }
            case openBracketToken: {
                scanner.nextToken();
                ArrayType type = new ArrayType(scanner.getInt(), intType);
                nextExpected(intConstantToken);
                nextExpected(closeBracketToken);
                nextExpected(boldIntToken);
                String name = scanner.getString();
                nextExpected(nameToken);
                Label label = global.enterVariable(type);
                table.setDescriptor(name, new LocalArrayDescriptor(type, arity * -1 * Type.wordSize));
                break;
            }
            default: {
                nextExpected(
                        boldIntToken,
                        boldStringToken,
                        openBracketToken
                );
            }
        }
        exit("parameter declaration");
    }

    //NextLocalDeclaration. Parses the next local declaration.
    
    protected int nextLocalDeclaration(int offset){          
        enter("declaration");
        
        int size;
        
        switch (scanner.getToken()){
            case openBracketToken: {
                ArrayType type;
                scanner.nextToken();
                type = new ArrayType(scanner.getInt(), intType);
                nextExpected(intConstantToken);
                nextExpected(closeBracketToken);
                nextExpected(boldIntToken);
                String name = scanner.getString();
                nextExpected(nameToken);
                table.setDescriptor(name, new LocalArrayDescriptor(type, offset));
                size = table.getDescriptor(name).getType().getSize();
                break;
            }
            case boldIntToken: {
                scanner.nextToken();
                String name = scanner.getString();
                nextExpected(nameToken);
                table.setDescriptor(name,new LocalVariableDescriptor(intType, offset));
                size = Type.wordSize;
                break;
            }
            case boldStringToken: {
                scanner.nextToken();
                String name = scanner.getString();
                nextExpected(nameToken);
                table.setDescriptor(name, new LocalVariableDescriptor(stringType, offset));
                size = Type.addressSize;
                break;
            }
            default: {
                nextExpected(boldIntToken, boldStringToken, openBracketToken);
                size = 0;
            }
        }

        exit("declaration");
        
        return size;
    }

    //NextProcedure. Parses the next procedure.

    protected void nextProcedure(){
        enter("procedure");
        nextExpected(boldProcToken);
        
        String name = scanner.getString();
        
        nextExpected(nameToken);

        table.push();
        nextParameters();

        switch (scanner.getToken()){
            case boldIntToken: {
                scanner.nextToken();
                procValType = intType;
                break;
            }
            case boldStringToken: {
                scanner.nextToken();
                procValType = stringType;
                break;
            }
            default: {
                nextExpected(boldIntToken, boldStringToken);
            }
        }
        nextExpected(colonToken);
        
        GlobalProcedureDescriptor descriptor = (GlobalProcedureDescriptor) table.getDescriptor(name);

        int local = nextBody(((ProcedureType) descriptor.getType()).getArity() * Type.addressSize);
        Label label = descriptor.getLabel();
        
        assembler.emit(label, "addi", allocator.sp, allocator.sp, -(40 + local));
        assembler.emit(label, "sw", allocator.ra, 40, allocator.sp);
        assembler.emit(label, "sw", allocator.fp, 36, allocator.sp);
        //TODO: FINISH THIS!
        
        table.pop();

        exit("procedure");
    }

    //NextParameters. Parses the next parameters.

    protected void nextParameters(){
        enter("parameters");

        nextExpected(openParenToken);
        
        int arity = 0;

        if (scanner.getToken() != closeParenToken) {
            nextParameterDeclaration(arity);
            ++arity;

            while(scanner.getToken() == commaToken){
                scanner.nextToken();
                nextParameterDeclaration(arity);
                ++arity;
            }
        }

        nextExpected(closeParenToken);

        exit("parameters");
    }

    //NextBody. Parses the next body.

    protected int nextBody(int parameterSize){
        enter("body");
        
        int size = 0;

        if(scanner.getToken() != boldBeginToken){
            
            size += nextLocalDeclaration(size + parameterSize);

            while(scanner.getToken() == semicolonToken){
                scanner.nextToken();
                size += nextLocalDeclaration(size + parameterSize);
            }
        }
        
        nextBeginStatement();

        exit("body");
        
        return size;
    }

    //NextStatement. Parses the next statement.

    protected void nextStatement(){
        enter("statement");

        switch(scanner.getToken()){
            case nameToken: {nextAssignmentOrCallStatement();break;}
            case boldBeginToken: {nextBeginStatement();break;}
            case boldCodeToken: {nextCodeStatement();break;}
            case boldIfToken: {nextIfStatement();break;}
            case boldValueToken : {nextValueStatement();break;}
            case boldWhileToken: {nextWhileStatement();break;}
            default: nextExpected(
                            nameToken,
                            boldBeginToken,
                            boldCodeToken,
                            boldIfToken,
                            boldValueToken,
                            boldWhileToken);
        }

        exit("statement");
    }

    //NextAssignmentOrCallStatement. Parses the next assignment or call statement.
    
    protected void nextAssignmentOrCallStatement(){
        enter("assignment or call statement");
        
        String name = scanner.getString();
        Type type = table.getDescriptor(name).getType();
        nextExpected(nameToken);
        
        switch(scanner.getToken()){
            case openParenToken: {
                if(!(type instanceof ProcedureType)){
                    throw new SnarlCompilerException(name + " is not a procedure.");
                }
                nextArguments((ProcedureType) type);
                break;
            }
            case openBracketToken: {
                if(!(type instanceof ArrayType)){
                    throw new SnarlCompilerException(name + " is not an array.");
                }
                scanner.nextToken();
                typeCheck(nextExpression(), intType);
                nextExpected(closeBracketToken);
                nextExpected(colonEqualToken);
                typeCheck(nextExpression(), intType);
                break;
            }
            case colonEqualToken: {                                                
                if(!(type == intType || type == stringType)){
                    throw new SnarlCompilerException(name + " must of type int or type string.");
                }
                scanner.nextToken();
                Descriptor descriptor = nextExpression();
                typeCheck(descriptor, type);
                break;
            }
            default: {
                nextExpected(
                        openParenToken,
                        openBracketToken,
                        colonEqualToken);
            }
        }
        
        exit("assignment or call statement");
    }

    //NextBeginStatement. Parses the next begin statement.
    
    protected void nextBeginStatement(){
        enter("begin statement");
        
        nextExpected(boldBeginToken);
        
        if(scanner.getToken() != boldEndToken){
            
            nextStatement();
            
            while(scanner.getToken() == semicolonToken){
                scanner.nextToken();
                nextStatement();
            }
        }
        
        nextExpected(boldEndToken);
        
        exit("begin statement");
    }

    //NextCodeStatement. Parses the next code statement.
    
    protected void nextCodeStatement(){
        enter("code statement");
        
        nextExpected(boldCodeToken);
        nextExpected(stringConstantToken);
        
        exit("code statement");
    }

    //NextIfStatement. Parses the next if statement.
    
    protected void nextIfStatement(){
        enter("if statement");
        
        nextExpected(boldIfToken);
        typeCheck(nextExpression(), intType);
        
        nextExpected(boldThenToken);
        nextStatement();
        
        if(scanner.getToken() == boldElseToken){
            scanner.nextToken();
            nextStatement();
        }
        
        exit("if statement");
    }

    //NextValueStatement. Parses the next value statement.
    
    protected void nextValueStatement(){
        enter("value statement");
        
        nextExpected(boldValueToken);
        typeCheck(nextExpression(), procValType);
        
        exit("value statement");
    }

    //NextWhileStatement. Parses the next while statement.
    
    protected void nextWhileStatement(){
        enter("while statement");
        
        nextExpected(boldWhileToken);
        typeCheck(nextExpression(), intType);
        
        nextExpected(boldDoToken);
        nextStatement();
        
        exit("while statement");
    }

    //NextExpression. Parses the next expression.

    protected RegisterDescriptor nextExpression(){
        enter("expression");
        
        RegisterDescriptor left = nextConjunction();
        RegisterDescriptor right;

        if (scanner.getToken() == boldOrToken) {
            typeCheck(left, intType);
            while (scanner.getToken() == boldOrToken){
                scanner.nextToken();
                right = nextConjunction();
                typeCheck(right, intType);
            }
        }

        exit("expression");
        
        return left;
    }

    //NextConjunction. Parses the next conjunction.
    
    protected RegisterDescriptor nextConjunction(){
        enter("conjunction");

        RegisterDescriptor left = nextComparison();
        RegisterDescriptor right;

        if (scanner.getToken() == boldAndToken) {
            typeCheck(left, intType);
            while (scanner.getToken() == boldAndToken){
                scanner.nextToken();
                right = nextComparison();
                typeCheck(right, intType);
            }
        }

        exit("conjunction");
        
        return left;
    }

    //NextComparison. Parses the next comparison.
    
    protected RegisterDescriptor nextComparison(){
        enter("comparison");

        RegisterDescriptor left = nextSum();
        RegisterDescriptor right;
        
        if(isInSet(scanner.getToken(), comparisonOperators)){
            typeCheck(left, intType);
            scanner.nextToken();
            right = nextSum();
            typeCheck(right, intType);
        }

        if(isInSet(scanner.getToken(), comparisonOperators)){
            throw new SnarlCompilerException("Chaining comparison operators is not allowed");
        }
        
        exit("comparison");
        
        return left;
    }

    //NextSum. Parses the next sum.

    protected RegisterDescriptor nextSum(){           
        enter("sum");

        RegisterDescriptor left = nextProduct();
        RegisterDescriptor right;

        if (isInSet(scanner.getToken(),sumOperators)) {
            typeCheck(left, intType);
            while(isInSet(scanner.getToken(),sumOperators)){
                scanner.nextToken();
                right = nextProduct();
                typeCheck(right, intType);
            }
        }

        exit("sum");

        return left;
    }

    //NextProduct. Parses the next product.

    protected RegisterDescriptor nextProduct(){
        enter("product");

        RegisterDescriptor left = nextTerm();
        RegisterDescriptor right;

        if (isInSet(scanner.getToken(), productOperators)) {
            typeCheck(left, intType);
            while(isInSet(scanner.getToken(),productOperators)){
                scanner.nextToken();
                right = nextTerm();
                typeCheck(right, intType);
            }
        }

        exit("product");
        
        return left;
    }

    //NextTerm. Parses the next term.

    protected RegisterDescriptor nextTerm(){
        enter("term");

        Boolean operator = false;
        
        while(isInSet(scanner.getToken(),termOperators)){
            scanner.nextToken();
            operator = true;
        }

        RegisterDescriptor descriptor = nextUnit();

        if(operator){
            typeCheck(descriptor, intType);
        }
        
        exit("term");
        
        return descriptor;
    }

    //NextUnit. Parses the next unit.
    
    protected RegisterDescriptor nextUnit(){       
        enter("unit");
        RegisterDescriptor registerDescriptor;
        switch (scanner.getToken()){
            case nameToken: {
                String name = scanner.getString();
                Type type = table.getDescriptor(name).getType();
                scanner.nextToken();
                NameDescriptor descriptor = table.getDescriptor(name);
                Allocator.Register register = descriptor.rvalue();
                registerDescriptor = new RegisterDescriptor(type, register); //TODO clean this up, probly split it out to other places
                switch(scanner.getToken()){
                    case openParenToken: {
                        if(!(type instanceof ProcedureType)){
                            throw new SnarlCompilerException(name + " is not a procedure.");
                        }
                        nextArguments((ProcedureType) type);
                        registerDescriptor = new RegisterDescriptor(((ProcedureType) type).getValue(), null); //TODO get register
                        break;
                    }
                    case openBracketToken: {
                        if(!(type instanceof ArrayType)){
                            throw new SnarlCompilerException(name + " is not an array.");
                        }
                        scanner.nextToken();
                        typeCheck(nextExpression(), intType);
                        nextExpected(closeBracketToken);
                        registerDescriptor = new RegisterDescriptor(((ArrayType) type).getBase(), null);  //TODO get register
                        break;
                    }
                    default: {
                        registerDescriptor = new RegisterDescriptor(table.getDescriptor(name).getType(), null);
                        break;
                    }
                }
                break;
            }
            case openParenToken: {
                scanner.nextToken();
                registerDescriptor = nextExpression();
                nextExpected(closeParenToken);
                break;
            }
            case stringConstantToken: {
                Allocator.Register register = allocator.request();
                Label label = global.enterString(scanner.getString());
                assembler.emit("la", register, label);
                registerDescriptor = new RegisterDescriptor(stringType, register);
                scanner.nextToken();
                break;
            }
            case intConstantToken: {
                Allocator.Register register = allocator.request();
                assembler.emit("li", register, scanner.getInt());
                registerDescriptor = new RegisterDescriptor(intType, register);
                scanner.nextToken();
                break;
            }
            default: {
                nextExpected(
                    nameToken,
                    openParenToken,
                    stringConstantToken,
                    intConstantToken
                );
                registerDescriptor = null;
            }
        }

        exit("unit");
        
        return registerDescriptor;
    }

    //NextArguments. Parses the next arguments.

    protected void nextArguments(ProcedureType proc){
        enter("arguments");
        
        nextExpected(openParenToken);
        ProcedureType.Parameter params = proc.getParameters();
        //int arity = 0;

        if(scanner.getToken() != closeParenToken){
            typeCheck(nextExpression(), params.getType());
            params = params.getNext();
            if(params == null){
                throw new SnarlCompilerException("Proc has too few args"); //TODO: cleanup
            }
            //arity++;

            while(scanner.getToken() == commaToken){
                scanner.nextToken();
                typeCheck(nextExpression(), params.getType());
                params = params.getNext();
                if(params == null){
                    throw new SnarlCompilerException("Proc has too few args"); //TODO: cleanup
                }
                //arity++;
            }
        }
        
        if(params != null){
            throw new SnarlCompilerException("Expected " + proc.getArity() + " arguments.");
        }
        
       /* if(arity != proc.getArity()){
            throw new SnarlCompilerException("Expected " + proc.getArity() + " arguments.");
        }*/

        nextExpected(closeParenToken);

        exit("arguments");
    }
         /*
    protected ProcedureType.Parameter advanceToNextParameter(Descriptor descriptor, ProcedureType.Parameter parameters){
        if(parameters == null){
            throw new SnarlCompilerException("Too many args"); //TODO: clean this up
        } else if(!descriptor.getType().isSubtype(parameters.getType())){
            throw new SnarlCompilerException("Arg has unexpected type");
        }
        return parameters.getNext();
    }      */

    //NextExpected(int token). Checks to see if the current token is the token passed, if not throws an exception.

    protected void nextExpected(int token) throws SnarlCompilerException{
        if(scanner.getToken() == token){
            scanner.nextToken();
        } else {
            throw new SnarlCompilerException("Expected " + tokenToString(token) + ".");
        }
    }

    //NextExpected(int... tokens). Checks to see if the current token is in tokens, if not throws an exception.

    protected void nextExpected(int... tokens) throws SnarlCompilerException{
        boolean expected = false;
        for (int token : tokens) {
            if (scanner.getToken() == token) {
                expected = true;
            }
        }
        if(expected){
            scanner.nextToken();
        } else {
            StringBuilder error = new StringBuilder();
            for(int i = 0; i < tokens.length - 1; i++){
                error.append(tokenToString(tokens[i]));
                error.append(" or ");
            }
            error.append(tokenToString(tokens[tokens.length - 1]));
            throw new SnarlCompilerException("Expected " + error.toString() + ".");
        }
    }
    
    //Stub for rvalue method in GlobalVariableDescriptor
    
    protected Allocator.Register rvalue(){
        Allocator.Register register = allocator.request();
        assembler.emit("la", register, label);
        assembler.emit("lw", register, 0, register);
        return register;
    }
}
