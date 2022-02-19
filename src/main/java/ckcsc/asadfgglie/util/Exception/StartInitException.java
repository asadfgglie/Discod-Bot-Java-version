package ckcsc.asadfgglie.util.Exception;

public class StartInitException extends RuntimeException{
    public StartInitException(String msg){
        this(msg, null);
    }
    public StartInitException(String msg, Throwable cause){
        super(msg, cause);
    }
}
