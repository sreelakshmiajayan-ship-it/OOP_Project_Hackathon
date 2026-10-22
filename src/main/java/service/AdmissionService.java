
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

        if (admission.getPatient() == null
                || admission.getDoctor() == null) {
            throw new IllegalArgumentException(
                    "Patient and doctor details are required.");
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

        if (admission.getRoomNumber() == null
                || admission.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a room.");
        }

        Room room = roomDAO.getRoomByNumber(
                admission.getRoomNumber());

        if (room == null) {
            throw new IllegalArgumentException(
                    "Selected room does not exist.");
        }

        if (!room.isAvailable()) {
            throw new IllegalArgumentException(
                    "Selected room is already occupied.");
        }

        // Reserve the room before saving the admission.
        // bookRoom() updates only rooms still marked available.
        if (!roomDAO.bookRoom(room.getRoomId())) {
            throw new IllegalArgumentException(
                    "Room is no longer available. Please select another room.");
        }

        try {
            admission.setStatus("Active");

            if (admissionDAO.addAdmission(admission)) {
                return true;
            }

            throw new SQLException(
                    "Admission could not be saved.");
        } catch (SQLException | RuntimeException e) {

            // Compensate if saving the admission fails.
            try {
                roomDAO.releaseRoom(room.getRoomId());
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }

            throw e;
        }
    }

    // Discharge Patient and Release Room
    public boolean dischargePatient(String admissionId)
            throws SQLException {

        if (admissionId == null
                || admissionId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Admission ID cannot be empty.");
        }

        Admission admission =
                admissionDAO.getAdmissionById(admissionId);

        if (admission == null
                || !"Active".equalsIgnoreCase(
                        admission.getStatus())) {
            throw new IllegalArgumentException(
                    "Admission not found or patient is already discharged.");
        }

        Room room = roomDAO.getRoomByNumber(
                admission.getRoomNumber());

        if (room == null) {
            throw new IllegalArgumentException(
                    "Allocated room could not be found.");
        }

        // Release the room first.
        if (!roomDAO.releaseRoom(room.getRoomId())) {
            throw new IllegalArgumentException(
                    "Could not release the allocated room.");
        }

        try {
            if (admissionDAO.dischargeAdmission(admissionId)) {
                return true;
            }

            throw new SQLException(
                    "Could not discharge the patient.");
        } catch (SQLException | RuntimeException e) {

            // Re-book the room if discharge fails.
            try {
                roomDAO.bookRoom(room.getRoomId());
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }

            throw e;
        }
    }

    // Get All Admissions (Admin)
    public List<Admission> getAllAdmissions()
            throws SQLException {
        return admissionDAO.getAllAdmissions();
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
