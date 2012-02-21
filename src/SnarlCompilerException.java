public class SnarlCompilerException extends RuntimeException{
    public String message;
    public Throwable cause;

    public SnarlCompilerException(Throwable cause){
        this.cause = cause;
    }

    public SnarlCompilerException(String message){
        this.message = message;
    }
    
    public SnarlCompilerException(String message, Throwable cause){
        this.message = message;
        this.cause = cause;
    }
}
