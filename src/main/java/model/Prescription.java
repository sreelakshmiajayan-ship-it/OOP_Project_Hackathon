package model;

import java.time.LocalDate;

public class Prescription {

    private int prescriptionId;
    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private Medicine medicine;
    private String dosage;
    private String frequency;
    private int durationDays;
    private String instructions;
    private LocalDate prescriptionDate;

    public Prescription() {
        this.prescriptionDate = LocalDate.now();
    }

    public Prescription(int prescriptionId, Patient patient,
                        Doctor doctor, Appointment appointment,
                        Medicine medicine, String dosage,
                        String frequency, int durationDays,
                        String instructions, LocalDate prescriptionDate) {

        this.prescriptionId = prescriptionId;
        this.patient = patient;
        this.doctor = doctor;
        this.appointment = appointment;
        this.medicine = medicine;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.instructions = instructions;
        this.prescriptionDate = prescriptionDate;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
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

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public void displayDetails() {
        System.out.println("Prescription ID: " + prescriptionId);

        if (patient != null) {
            System.out.println("Patient: " + patient.getFirstname()
                    + " " + patient.getLastname());
        }

        if (doctor != null) {
            System.out.println("Doctor: " + doctor.getFirstname()
                    + " " + doctor.getLastname());
        }

        if (medicine != null) {
            System.out.println("Medicine: " + medicine.getMedicineName());
        }

        System.out.println("Dosage: " + dosage);
        System.out.println("Frequency: " + frequency);
        System.out.println("Duration: " + durationDays + " days");
        System.out.println("Instructions: " + instructions);
        System.out.println("Prescription Date: " + prescriptionDate);
    }
}