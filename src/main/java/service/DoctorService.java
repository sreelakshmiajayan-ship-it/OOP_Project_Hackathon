
package service;

import dao.DoctorDAO;
import model.Doctor;
import util.PasswordUtil;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;

public class DoctorService {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    public boolean registerDoctor(Doctor doctor, String password)
            throws SQLException, GeneralSecurityException {

        if (doctor == null) {
            throw new IllegalArgumentException(
                    "Doctor details cannot be empty.");
        }

        if (doctor.getFirstname() == null
                || doctor.getFirstname().isBlank()
                || doctor.getLastname() == null
                || doctor.getLastname().isBlank()
                || doctor.getEmail() == null
                || doctor.getEmail().isBlank()
                || doctor.getSpecialization() == null
                || doctor.getSpecialization().isBlank()) {

            throw new IllegalArgumentException(
                    "Enter the doctor's first name, last name, email "
                            + "and specialization.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        doctor.setFirstname(doctor.getFirstname().trim());
        doctor.setLastname(doctor.getLastname().trim());
        doctor.setEmail(doctor.getEmail().trim());
        doctor.setSpecialization(doctor.getSpecialization().trim());

        String hashedPassword = PasswordUtil.hashPassword(password);

        return doctorDAO.registerDoctorWithAccount(
                doctor, hashedPassword);
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        return doctorDAO.getAllDoctors();
    }
}
