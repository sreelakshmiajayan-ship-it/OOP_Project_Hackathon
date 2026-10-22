package model;

public class Appointment {

    private int appointmentId;
    private Patient patient;
    private Doctor doctor;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private Room room;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
    public Appointment() {
        this.status = "Scheduled";
    }

    public Appointment(int appointmentId, Patient patient, Doctor doctor,
                       String appointmentDate, String appointmentTime,
                       String status) {

        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void cancelAppointment() {
        this.status = "Cancelled";
    }

    public void displayDetails() {
        System.out.println("Appointment ID: " + appointmentId);

        if (patient != null) {
            System.out.println("Patient: " + patient.getFirstname()
                    + " " + patient.getLastname());
        }

        if (doctor != null) {
            System.out.println("Doctor: " + doctor.getFirstname()
                    + " " + doctor.getLastname());
        }

        System.out.println("Date: " + appointmentDate);
        System.out.println("Time: " + appointmentTime);
        System.out.println("Status: " + status);
    }
}