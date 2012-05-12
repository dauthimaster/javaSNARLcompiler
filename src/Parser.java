/*
SNARL/Parser

James Current
1/30/12
 */
public class Parser extends Common{
    protected Scanner scanner;                      //Parser pulls tokens from Scanner
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

    //Constructor. Returns a new Parser positioned at the first unignored token.
    
    public Parser(Source source){
        scanner = new Scanner(source);
        this.source = source;
        table = new SymbolTable();
        table.push();
        intType = new BasicType("int",Type.wordSize,null);
        stringType = new BasicType("string",Type.addressSize,null);
        allocator = new Allocator();
        assembler = new Assembler();
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
                table.setDescriptor(name,new Descriptor(procedure));
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
            nextDeclaration();
        } else if(scanner.getToken() == boldProcToken){
            nextProcedure();
        } else {
            nextExpected(boldProcToken,boldIntToken,boldStringToken,openBracketToken);
        }
        exit("program part");
    }
    
    //NextGlobalDeclaration. Parses the next global declaration.
    
    protected void nextGlobalDeclaration(){
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
                //TODO make string type like above
            }
            case openBracketToken: {
                //TODO make array type, similar to above except with GlobalArrayDescriptor
            }
            default: {
                nextExpected(
                        boldIntToken,
                        boldStringToken,
                        openBracketToken
                );
            }
        }
    }

    //NextDeclaration. Parses the next declaration.
    
    protected void nextDeclaration(){          
        enter("declaration");
        switch (scanner.getToken()){
            case openBracketToken: {
                ArrayType type;
                scanner.nextToken();
                type = new ArrayType(scanner.getInt(), intType);
                nextExpected(intConstantToken);
                nextExpected(closeBracketToken);
                nextExpected(boldIntToken);
                table.setDescriptor(scanner.getString(), new Descriptor(type));
                break;
            }
            case boldIntToken: {
                scanner.nextToken();
                table.setDescriptor(scanner.getString(),new Descriptor(intType));
                break;
            }
            case boldStringToken: {
                scanner.nextToken();
                table.setDescriptor(scanner.getString(),new Descriptor(stringType));
                break;}
            default: {nextExpected(boldIntToken, boldStringToken, openBracketToken);break;}
        }

        nextExpected(nameToken);

        exit("declaration");
    }

    //NextProcedure. Parses the next procedure.

    protected void nextProcedure(){
        enter("procedure");
        nextExpected(boldProcToken);
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

        nextBody();
        
        table.pop();

        exit("procedure");
    }

    //NextParameters. Parses the next parameters.

    protected void nextParameters(){
        enter("parameters");

        nextExpected(openParenToken);

        if (scanner.getToken() != closeParenToken) {
            nextDeclaration();

            while(scanner.getToken() == commaToken){
                scanner.nextToken();
                nextDeclaration();
            }
        }

        nextExpected(closeParenToken);

        exit("parameters");
    }

    //NextBody. Parses the next body.

    protected void nextBody(){
        enter("body");

        if(scanner.getToken() != boldBeginToken){
            
            nextDeclaration();

            while(scanner.getToken() == semicolonToken){
                scanner.nextToken();
                nextDeclaration();
            }
        }
        
        nextBeginStatement();

        exit("body");
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

    protected Descriptor nextExpression(){
        enter("expression");
        
        Descriptor left = nextConjunction();
        Descriptor right;

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
    
    protected Descriptor nextConjunction(){
        enter("conjunction");
        
        Descriptor left = nextComparison();
        Descriptor right;

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
    
    protected Descriptor nextComparison(){
        enter("comparison");
        
        Descriptor left = nextSum();
        Descriptor right;
        
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

    protected Descriptor nextSum(){           
        enter("sum");
        
        Descriptor left = nextProduct();
        Descriptor right;

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

    protected Descriptor nextProduct(){
        enter("product");

        Descriptor left = nextTerm();
        Descriptor right;

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

    protected Descriptor nextTerm(){
        enter("term");

        Boolean operator = false;
        
        while(isInSet(scanner.getToken(),termOperators)){
            scanner.nextToken();
            operator = true;
        }

        Descriptor descriptor = nextUnit();

        if(operator){
            typeCheck(descriptor, intType);
        }
        
        exit("term");
        
        return descriptor;
    }

    //NextUnit. Parses the next unit.
    
    protected RegisterDescriptor nextUnit(){       //TODO: convert to RegisterDescriptors
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
                        registerDescriptor = new RegisterDescriptor(((ProcedureType) type).getValue()); //TODO get register
                        break;
                    }
                    case openBracketToken: {
                        if(!(type instanceof ArrayType)){
                            throw new SnarlCompilerException(name + " is not an array.");
                        }
                        scanner.nextToken();
                        typeCheck(nextExpression(), intType);
                        nextExpected(closeBracketToken);
                        registerDescriptor = new RegisterDescriptor(((ArrayType) type).getBase());  //TODO get register
                        break;
                    }
                    default: {
                        registerDescriptor = table.getDescriptor(name);
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
