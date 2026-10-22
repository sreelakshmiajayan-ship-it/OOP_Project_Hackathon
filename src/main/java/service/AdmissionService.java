
package service;

import dao.AdmissionDAO;
import dao.RoomDAO;
import model.Admission;
import model.Room;

import java.sql.SQLException;
import java.util.List;

public class AdmissionService {

    private final AdmissionDAO admissionDAO = new AdmissionDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    // Admit Patient and Allocate Room
    public boolean admitPatient(Admission admission)
            throws SQLException {
        if (admission == null) {
            throw new IllegalArgumentException(
                    "Admission details cannot be null.");
        }
        Room room = roomDAO.getRoomByNumber(admission.getRoomNumber());
        if (room == null) {
            throw new IllegalArgumentException(
                    "Selected bed does not exist.");
        }
        return admitPatient(admission, room.getWardName());
    }

    public boolean admitPatient(Admission admission, String wardName)
            throws SQLException {
        if (admission == null) {
            throw new IllegalArgumentException(
                    "Admission details cannot be null.");
        }

        if (admission.getPatient() == null
                || admission.getDoctor() == null) {
            throw new IllegalArgumentException(
                    "Patient and doctor details are required.");
        }
        if (admission.getPatient().getId() <= 0
                || admission.getDoctor().getId() <= 0) {
            throw new IllegalArgumentException(
                    "Select a valid registered patient and doctor.");
        }

        if (admission.getAdmissionId() == null
                || admission.getAdmissionId().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Admission ID is required.");
        }

        if (admission.getAdmissionDate() == null) {
            throw new IllegalArgumentException(
                    "Admission date is required.");
        }

        if (wardName == null || wardName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a ward.");
        }

        if (admission.getRoomNumber() == null
                || admission.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a room.");
        }

        Room room = roomDAO.getRoomByNumber(admission.getRoomNumber());

        if (room == null) {
            throw new IllegalArgumentException("Selected bed does not exist.");
        }

        if (!wardName.equals(room.getWardName())) {
            throw new IllegalArgumentException(
                    "Selected bed does not belong to the selected ward.");
        }

        if (!room.isAvailable()) {
            throw new IllegalArgumentException(
                    "Selected bed is already occupied.");
        }

        admission.setWardName(wardName);
        admission.setStatus("Active");
        if (!admissionDAO.addAdmission(admission)) {
            throw new IllegalArgumentException(
                    "Bed is no longer available or does not belong to "
                            + "the selected ward. Refresh and select another bed.");
        }
        return true;
    }

    // Discharge Patient and Release Room
    public boolean dischargePatient(String admissionId)
            throws SQLException {

        if (admissionId == null
                || admissionId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Admission ID cannot be empty.");
        }

        if (!admissionDAO.dischargeAdmission(admissionId)) {
            throw new IllegalArgumentException(
                    "Admission not found, already discharged, or its bed "
                            + "could not be released.");
        }
        return true;
    }

    // Get All Admissions (Admin)
    public List<Admission> getAllAdmissions()
            throws SQLException {
        return admissionDAO.getAllAdmissions();
    }

    public List<Admission> getActiveAdmissions() throws SQLException {
        return admissionDAO.getActiveAdmissions();
    }

    // Get Admission by ID
    public Admission getAdmissionById(String admissionId)
            throws SQLException {

        if (admissionId == null
                || admissionId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Admission ID cannot be empty.");
        }

        return admissionDAO.getAdmissionById(admissionId);
    }

    // Get Admissions for a Doctor
    public List<Admission> getAdmissionsByDoctorId(int doctorId)
            throws SQLException {

        if (doctorId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid doctor ID.");
        }

        return admissionDAO.getAdmissionsByDoctorId(doctorId);
    }

    // Get Admissions for a Patient
    public List<Admission> getAdmissionsByPatientId(int patientId)
            throws SQLException {

        if (patientId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid patient ID.");
        }

        return admissionDAO.getAdmissionsByPatientId(patientId);
    }
}
