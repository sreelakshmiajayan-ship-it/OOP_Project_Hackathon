
package dao;

import model.Admission;
import model.Patient;
import model.Doctor;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class AdmissionDAO {

    // Add Admission
    public boolean addAdmission(Admission admission) throws SQLException {
        String sql = "INSERT INTO admissions "
                + "(admission_id, patient_id, doctor_id, admission_date, "
                + "room_number, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, admission.getAdmissionId());
            ps.setInt(2, admission.getPatient().getId());
            ps.setInt(3, admission.getDoctor().getId());
            ps.setTimestamp(4,
                    Timestamp.valueOf(admission.getAdmissionDate()));
            ps.setString(5, admission.getRoomNumber());
            ps.setString(6, admission.getStatus());

            return ps.executeUpdate() > 0;
        }
    }
    

    // Get Admission by ID
    public Admission getAdmissionById(String admissionId)
            throws SQLException {
        String sql = "SELECT * FROM admissions WHERE admission_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, admissionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAdmission(rs);
                }
            }
        }

        return null;
    }

    // Get All Admissions
    public List<Admission> getAllAdmissions() throws SQLException {
        List<Admission> admissions = new ArrayList<>();
        String sql = "SELECT * FROM admissions";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                admissions.add(mapAdmission(rs));
            }
        }

        return admissions;
    }

    // Update Admission
    public boolean updateAdmission(Admission admission)
            throws SQLException {
        String sql = "UPDATE admissions SET patient_id = ?, "
                + "doctor_id = ?, admission_date = ?, room_number = ?, "
                + "status = ? WHERE admission_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, admission.getPatient().getId());
            ps.setInt(2, admission.getDoctor().getId());
            ps.setTimestamp(3,
                    Timestamp.valueOf(admission.getAdmissionDate()));
            ps.setString(4, admission.getRoomNumber());
            ps.setString(5, admission.getStatus());
            ps.setString(6, admission.getAdmissionId());

            return ps.executeUpdate() > 0;
        }
    }

    // Discharge Patient
    public boolean dischargeAdmission(String admissionId)
            throws SQLException {
        String sql = "UPDATE admissions SET status = 'Discharged' "
                + "WHERE admission_id = ? AND status = 'Active'";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, admissionId);
            return ps.executeUpdate() > 0;
        }
    }

    // Delete Admission
    public boolean deleteAdmission(String admissionId)
            throws SQLException {
        String sql = "DELETE FROM admissions WHERE admission_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, admissionId);
            return ps.executeUpdate() > 0;
        }
    }

    // Convert database row into Admission object
    private Admission mapAdmission(ResultSet rs) throws SQLException {
        PatientDAO patientDAO = new PatientDAO();
        DoctorDAO doctorDAO = new DoctorDAO();

        Patient patient =
                patientDAO.getPatientById(rs.getInt("patient_id"));

        Doctor doctor =
                doctorDAO.getDoctorById(rs.getInt("doctor_id"));

        Admission admission = new Admission(
                rs.getString("admission_id"),
                patient,
                doctor,
                rs.getString("room_number")
        );

        Timestamp timestamp = rs.getTimestamp("admission_date");
        if (timestamp != null) {
            admission.setAdmissionDate(timestamp.toLocalDateTime());
        }

        admission.setStatus(rs.getString("status"));

        return admission;
    }

    // Get admissions for a specific doctor
public List<Admission> getAdmissionsByDoctorId(int doctorId)
        throws SQLException {

    List<Admission> admissions = new ArrayList<>();

    String sql = "SELECT * FROM admissions WHERE doctor_id = ?";

    try (Connection con = DBConnection.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, doctorId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                admissions.add(mapAdmission(rs));
            }
        }
    }

    return admissions;
}

// Get admissions for a specific patient
public List<Admission> getAdmissionsByPatientId(int patientId)
        throws SQLException {

    List<Admission> admissions = new ArrayList<>();

    String sql = "SELECT * FROM admissions WHERE patient_id = ?";

    try (Connection con = DBConnection.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, patientId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                admissions.add(mapAdmission(rs));
            }
        }
    }

    return admissions;
}
}
