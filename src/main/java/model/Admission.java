
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Admission {
    private String admissionId;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;
    private String roomNumber;
    private String wardName;
    private String status;

    // Existing constructor preserved
    public Admission(String admissionId, Patient patient, Doctor doctor, String roomNumber) {
        this.admissionId = admissionId;
        this.patient = patient;
        this.doctor = doctor;
        this.admissionDate = LocalDateTime.now();
        this.dischargeDate = null;
        this.roomNumber = roomNumber;
        this.wardName = null;
        this.status = "Active";
    }

    // New constructor for specifying ward and admission date
    public Admission(String admissionId, Patient patient, Doctor doctor,
                     String roomNumber, String wardName,
                     LocalDateTime admissionDate) {
        this.admissionId = admissionId;
        this.patient = patient;
        this.doctor = doctor;
        this.admissionDate = admissionDate;
        this.dischargeDate = null;
        this.roomNumber = roomNumber;
        this.wardName = wardName;
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

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Record the discharge date and update the status
    public void discharge() {
        if (!"Discharged".equalsIgnoreCase(status)) {
            this.dischargeDate = LocalDateTime.now();
            this.status = "Discharged";
        }
    }

    public String getAdmissionDetails() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String admissionDateText = admissionDate != null
                ? admissionDate.format(formatter) : "N/A";

        String dischargeDateText = dischargeDate != null
                ? dischargeDate.format(formatter) : "Not discharged";

        String patientName = patient != null
                ? patient.getFirstname() + " " + patient.getLastname()
                : "N/A";

        String doctorName = doctor != null
                ? doctor.getFirstname() + " " + doctor.getLastname()
                : "N/A";

        return "Admission ID: " + admissionId
                + ", Patient: " + patientName
                + ", Doctor: " + doctorName
                + ", Ward: " + (wardName != null ? wardName : "N/A")
                + ", Room: " + (roomNumber != null ? roomNumber : "N/A")
                + ", Admission Date: " + admissionDateText
                + ", Discharge Date: " + dischargeDateText
                + ", Status: " + status;
    }
}
