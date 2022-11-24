package lk.ijse.dep9.lmsbackend.exception;

public class ResponseStatusException extends RuntimeException{

    private int status;

    public ResponseStatusException(int status, String message, Throwable t){
        super(message,t);
    }

    public ResponseStatusException(int status, String message){
        super(message);
    }

    public ResponseStatusException(int status, Throwable t){
        super(t);
    }

    public int getStatus(){
        return status;
    }
}
