package exception;

public class PatientNotFoundException extends Exception {
    public PatientNotFoundException() {
    }

    public PatientNotFoundException(String message) {
        super(message);
    }
}