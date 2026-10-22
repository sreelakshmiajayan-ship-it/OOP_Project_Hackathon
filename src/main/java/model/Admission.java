package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Admission {
    private String admissionId;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String wardName;
    private String roomNumber;
    private String status;

    public Admission(String admissionId, Patient patient, Doctor doctor, String roomNumber) {
        this.admissionId = admissionId;
        this.patient = patient;
        this.doctor = doctor;
        this.admissionDate = LocalDateTime.now();
        this.roomNumber = roomNumber;
        this.status = "Active";
    }

    public String getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(String admissionId) {
        this.admissionId = admissionId;
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

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void discharge() {
        this.status = "Discharged";
    }

    public String getAdmissionDetails() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Admission ID: " + admissionId + 
             ", Patient: " + (patient != null ? patient.getFirstname() + " " + patient.getLastname() : "N/A") + 
             ", Doctor: " + (doctor != null ? doctor.getFirstname() + " " + doctor.getLastname() : "N/A") + 
               ", Room: " + roomNumber + 
               ", Date: " + admissionDate.format(formatter) + 
               ", Status: " + status;
    }
}