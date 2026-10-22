
package dao;

import model.Patient;
import util.DBConnection;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.PasswordUtil;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class PatientDAO {

    public boolean addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients "
                + "(firstname, lastname, age, phonenumber, email, "
                + "op_number, disease, blood_group, address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patient.getFirstname());
            ps.setString(2, patient.getLastname());
            ps.setInt(3, patient.getAge());
            ps.setString(4, patient.getPhonenumber());
            ps.setString(5, patient.getEmail());
            ps.setString(6, patient.getOpNumber());
            ps.setString(7, patient.getDisease());
            ps.setString(8, patient.getBloodGroup());
            ps.setString(9, patient.getAddress());

            return ps.executeUpdate() > 0;
        }
    }

    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getInt("age"),
                            rs.getString("phonenumber"),
                            rs.getString("email"),
                            rs.getString("op_number"),
                            rs.getString("disease"),
                            rs.getString("blood_group"),
                            rs.getString("address")
                    );
                }
            }
        }

        return null;
    }

    public Patient getPatientByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM patients WHERE email = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Patient patient = new Patient(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getInt("age"),
                            rs.getString("phonenumber"),
                            rs.getString("email"),
                            rs.getString("op_number"),
                            rs.getString("disease"),
                            rs.getString("blood_group"),
                            rs.getString("address")
                    );
                    if (rs.next()) {
                        throw new SQLException(
                                "More than one patient record uses this email.");
                    }
                    return patient;
                }
            }
        }

        return null;
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getInt("age"),
                        rs.getString("phonenumber"),
                        rs.getString("email"),
                        rs.getString("op_number"),
                        rs.getString("disease"),
                        rs.getString("blood_group"),
                        rs.getString("address")
                );

                patients.add(patient);
            }
        }

        return patients;
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET firstname = ?, lastname = ?, "
                + "age = ?, phonenumber = ?, email = ?, op_number = ?, "
                + "disease = ?, blood_group = ?, address = ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, patient.getFirstname());
            ps.setString(2, patient.getLastname());
            ps.setInt(3, patient.getAge());
            ps.setString(4, patient.getPhonenumber());
            ps.setString(5, patient.getEmail());
            ps.setString(6, patient.getOpNumber());
            ps.setString(7, patient.getDisease());
            ps.setString(8, patient.getBloodGroup());
            ps.setString(9, patient.getAddress());
            ps.setInt(10, patient.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePatient(int id) throws SQLException {
        String sql = "DELETE FROM patients WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    public boolean registerPatient(Patient patient, String password)
        throws SQLException, GeneralSecurityException {

    String userSql = "INSERT INTO users "
            + "(firstname, lastname, email, phonenumber, password, role) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    String patientSql = "INSERT INTO patients "
            + "(firstname, lastname, age, phonenumber, email, "
            + "op_number, disease, blood_group, address) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = DBConnection.getInstance().getConnection()) {
        con.setAutoCommit(false);

        try {
            String hashedPassword = PasswordUtil.hashPassword(password);

            try (PreparedStatement userPs =
                         con.prepareStatement(userSql)) {

                userPs.setString(1, patient.getFirstname());
                userPs.setString(2, patient.getLastname());
                userPs.setString(3, patient.getEmail());
                userPs.setString(4, patient.getPhonenumber());
                userPs.setString(5, hashedPassword);
                userPs.setString(6, "Patient");

                userPs.executeUpdate();
            }

            String opNumber = "OP" + UUID.randomUUID()
                    .toString().replace("-", "")
                    .substring(0, 12).toUpperCase();

            try (PreparedStatement patientPs =
                         con.prepareStatement(patientSql)) {

                patientPs.setString(1, patient.getFirstname());
                patientPs.setString(2, patient.getLastname());
                patientPs.setInt(3, patient.getAge());
                patientPs.setString(4, patient.getPhonenumber());
                patientPs.setString(5, patient.getEmail());
                patientPs.setString(6, opNumber);
                patientPs.setString(7, patient.getDisease());
                patientPs.setString(8, patient.getBloodGroup());
                patientPs.setString(9, patient.getAddress());

                patientPs.executeUpdate();
            }

            con.commit();
            patient.setOpNumber(opNumber);
            return true;

        } catch (SQLException | GeneralSecurityException
                 | RuntimeException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException resetError) {
                // Connection is closed when the try block ends.
            }
        }
    }
}
}
