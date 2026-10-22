
package service;

import dao.PatientDAO;
import model.Patient;
import java.sql.SQLException;
import java.util.List;
import java.security.GeneralSecurityException;

public class PatientService {

    private final PatientDAO patientDAO = new PatientDAO();

    /**
     * Registers a new patient.
     */
    public boolean registerPatient(Patient patient) throws SQLException {
        if (patient == null) {
            throw new IllegalArgumentException(
                "Patient details cannot be null."
            );
        }

        return patientDAO.addPatient(patient);
    }

    /**
     * Finds a patient by ID.
     */
    public Patient findPatientById(int patientId) throws SQLException {
        if (patientId <= 0) {
            throw new IllegalArgumentException(
                "Invalid patient ID."
            );
        }

        return patientDAO.getPatientById(patientId);
    }

    public Patient findPatientByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Patient email cannot be empty.");
        }

        return patientDAO.getPatientByEmail(email.trim());
    }

    /**
     * Retrieves all registered patients.
     */
    public List<Patient> getAllPatients() throws SQLException {
      
        return patientDAO.getAllPatients();
    }

    public boolean registerPatientWithAccount(
        Patient patient, String password)
        throws SQLException, GeneralSecurityException {

    if (patient == null) {
        throw new IllegalArgumentException(
                "Patient details cannot be null.");
    }

    if (password == null || password.length() < 8) {
        throw new IllegalArgumentException(
                "Password must contain at least 8 characters.");
    }

    if (patient.getFirstname() == null
            || patient.getFirstname().isBlank()
            || patient.getLastname() == null
            || patient.getLastname().isBlank()
            || patient.getEmail() == null
            || patient.getEmail().isBlank()
            || patient.getPhonenumber() == null
            || patient.getPhonenumber().isBlank()
            || patient.getAge() <= 0) {
        throw new IllegalArgumentException(
                "Please enter valid patient details.");
    }

    return patientDAO.registerPatient(patient, password);
}
}
