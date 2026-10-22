
package dao;

import model.Doctor;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    public boolean addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors "
                + "(firstname, lastname, age, phonenumber, email, "
                + "specialization, qualification, consultation_fee) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, doctor.getFirstname());
            ps.setString(2, doctor.getLastname());
            ps.setInt(3, doctor.getAge());
            ps.setString(4, doctor.getPhonenumber());
            ps.setString(5, doctor.getEmail());
            ps.setString(6, doctor.getSpecialization());
            ps.setString(7, doctor.getQualification());
            ps.setDouble(8, doctor.getConsultationFee());

            return ps.executeUpdate() > 0;
        }
    }

    public Doctor getDoctorById(int id) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getInt("age"),
                            rs.getString("phonenumber"),
                            rs.getString("email"),
                            rs.getString("specialization"),
                            rs.getString("qualification"),
                            rs.getDouble("consultation_fee")
                    );
                }
            }
        }

        return null;
    }

    public Doctor getDoctorByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE email = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Doctor doctor = mapDoctor(rs);
                    if (rs.next()) {
                        throw new SQLException(
                                "More than one doctor record uses this email.");
                    }
                    return doctor;
                }
            }
        }

        return null;
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                doctors.add(mapDoctor(rs));
            }
        }

        return doctors;
    }

    public boolean updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET firstname = ?, lastname = ?, "
                + "age = ?, phonenumber = ?, email = ?, specialization = ?, "
                + "qualification = ?, consultation_fee = ? WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, doctor.getFirstname());
            ps.setString(2, doctor.getLastname());
            ps.setInt(3, doctor.getAge());
            ps.setString(4, doctor.getPhonenumber());
            ps.setString(5, doctor.getEmail());
            ps.setString(6, doctor.getSpecialization());
            ps.setString(7, doctor.getQualification());
            ps.setDouble(8, doctor.getConsultationFee());
            ps.setInt(9, doctor.getId());

            return ps.executeUpdate() > 0;
        }
    }

    private Doctor mapDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getInt("age"),
                rs.getString("phonenumber"),
                rs.getString("email"),
                rs.getString("specialization"),
                rs.getString("qualification"),
                rs.getDouble("consultation_fee")
        );
    }

    public boolean deleteDoctor(int id) throws SQLException {
        String sql = "DELETE FROM doctors WHERE id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
    // Register a doctor and create the doctor's login account together
public boolean registerDoctorWithAccount(
        Doctor doctor, String hashedPassword) throws SQLException {

    String userSql = "INSERT INTO users "
            + "(firstname, lastname, email, phonenumber, password, role) "
            + "VALUES (?, ?, ?, ?, ?, 'Doctor')";

    String doctorSql = "INSERT INTO doctors "
            + "(firstname, lastname, age, phonenumber, email, "
            + "specialization, qualification, consultation_fee, user_id) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection con = DBConnection.getInstance().getConnection()) {

        boolean originalAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            int userId;

            // 1. Create the doctor's login account
            try (PreparedStatement userPs = con.prepareStatement(
                    userSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                userPs.setString(1, doctor.getFirstname());
                userPs.setString(2, doctor.getLastname());
                userPs.setString(3, doctor.getEmail());
                userPs.setString(4, doctor.getPhonenumber());
                userPs.setString(5, hashedPassword);

                userPs.executeUpdate();

                try (ResultSet keys = userPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException(
                                "Could not create the doctor's login account.");
                    }
                    userId = keys.getInt(1);
                }
            }

            // 2. Save the doctor's professional details
            try (PreparedStatement doctorPs = con.prepareStatement(
                    doctorSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

                doctorPs.setString(1, doctor.getFirstname());
                doctorPs.setString(2, doctor.getLastname());
                doctorPs.setInt(3, doctor.getAge());
                doctorPs.setString(4, doctor.getPhonenumber());
                doctorPs.setString(5, doctor.getEmail());
                doctorPs.setString(6, doctor.getSpecialization());
                doctorPs.setString(7, doctor.getQualification());
                doctorPs.setDouble(8, doctor.getConsultationFee());
                doctorPs.setInt(9, userId);

                doctorPs.executeUpdate();

                try (ResultSet keys = doctorPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        doctor.setId(keys.getInt(1));
                    }
                }
            }

            con.commit();
            return true;

        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                ex.addSuppressed(rollbackError);
            }
            throw ex;

        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }
}
}
