
package service;

import dao.UserDAO;
import dao.DoctorDAO;
import model.Doctor;
import model.User;
import util.PasswordUtil;

import java.sql.SQLException;
import java.security.GeneralSecurityException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    // Patient login using email and hashed password
    public boolean authenticatePatient(String email, String password)
            throws SQLException, GeneralSecurityException {

        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {
            return false;
        }

        User user = userDAO.getUserByEmail(email.trim());

        if (user == null || user.getPassword() == null
                || !"Patient".equalsIgnoreCase(user.getRole())) {
            return false;
        }

        return PasswordUtil.verifyPassword(
                password, user.getPassword());
    }

    // Admin login using Admin ID and hashed password
    public boolean authenticateAdmin(String adminId, String password)
            throws SQLException, GeneralSecurityException {

        if (adminId == null || adminId.isBlank()
                || password == null || password.isEmpty()) {
            return false;
        }

        final int id;

        try {
            id = Integer.parseInt(adminId.trim());
        } catch (NumberFormatException ex) {
            return false;
        }

        User admin = userDAO.getAdminById(id);

        if (admin == null || admin.getPassword() == null
                || !"Admin".equalsIgnoreCase(admin.getRole())) {
            return false;
        }

        return PasswordUtil.verifyPassword(
                password, admin.getPassword());
    }

    public Doctor authenticateDoctor(String email, String password)
            throws SQLException, GeneralSecurityException {
        if (email == null || email.isBlank()
                || password == null || password.isEmpty()) {
            return null;
        }

        User user = userDAO.getUserByEmail(email.trim());
        if (user == null || user.getPassword() == null
                || !"Doctor".equalsIgnoreCase(user.getRole())
                || !PasswordUtil.verifyPassword(
                        password, user.getPassword())) {
            return null;
        }

        return doctorDAO.getDoctorByEmail(email.trim());
    }

    // General login for users identified by email
    public boolean authenticate(String username, String password, String role)
            throws SQLException, GeneralSecurityException {

        if (username == null || password == null || role == null
                || username.isBlank() || password.isEmpty()
                || role.isBlank()) {
            return false;
        }

        User user = userDAO.getUserByEmail(username.trim());

        if (user == null || user.getPassword() == null
                || user.getRole() == null
                || !user.getRole().equalsIgnoreCase(role.trim())) {
            return false;
        }

        return PasswordUtil.verifyPassword(
                password, user.getPassword());
    }
}
