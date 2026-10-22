package exception;

public class RoomUnavailableException extends Exception {

    public RoomUnavailableException() {
    }

    public RoomUnavailableException(String message) {
        super(message);
    }
}