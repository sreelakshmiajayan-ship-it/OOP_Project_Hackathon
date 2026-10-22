
package service;

import dao.DoctorDAO;
import model.Doctor;

import java.sql.SQLException;
import java.util.List;

public class AdminService {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    // Add Doctor
    public boolean addDoctor(Doctor doctor) throws SQLException {
        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor details cannot be null.");
        }

        return doctorDAO.addDoctor(doctor);
    }

    // Remove Doctor by ID
    public boolean removeDoctor(int doctorId) throws SQLException {
        Doctor doctor = doctorDAO.getDoctorById(doctorId);

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor with ID " + doctorId + " not found.");
        }

        return doctorDAO.deleteDoctor(doctorId);
    }

    // Get All Doctors
    public List<Doctor> getAllDoctors() throws SQLException {
        return doctorDAO.getAllDoctors();
    }
}
