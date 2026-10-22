package exception;

public class AppointmentConflictException extends Exception {
    public AppointmentConflictException() {
    }

    public AppointmentConflictException(String message) {
        super(message);
    }
}