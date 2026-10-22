
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

   
public boolean addAdmission(Admission admission) throws SQLException {
    String lockBedSql =
            "SELECT room_id FROM rooms "
            + "WHERE room_number = ? AND ward_name = ? FOR UPDATE";
    String activeAdmissionSql =
            "SELECT admission_id FROM admissions "
            + "WHERE room_number = ? AND status = 'Active' "
            + "LIMIT 1 FOR UPDATE";
    String reserveBedSql =
            "UPDATE rooms SET available = FALSE "
            + "WHERE room_number = ? AND ward_name = ? AND available = TRUE";

    String insertAdmissionSql =
            "INSERT INTO admissions "
            + "(admission_id, patient_id, doctor_id, admission_date, "
            + "room_number, status, discharge_date) "
            + "VALUES (?, ?, ?, ?, ?, ?, NULL)";

    try (Connection con = DBConnection.getInstance().getConnection()) {
        boolean originalAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            try (PreparedStatement ps = con.prepareStatement(lockBedSql)) {
                ps.setString(1, admission.getRoomNumber());
                ps.setString(2, admission.getWardName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ps =
                    con.prepareStatement(activeAdmissionSql)) {
                ps.setString(1, admission.getRoomNumber());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        con.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement ps =
                    con.prepareStatement(reserveBedSql)) {
                ps.setString(1, admission.getRoomNumber());
                ps.setString(2, admission.getWardName());

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            // Step 2: Insert the admission record.
            try (PreparedStatement ps =
                    con.prepareStatement(insertAdmissionSql)) {

                ps.setString(1, admission.getAdmissionId());
                ps.setInt(2, admission.getPatient().getId());
                ps.setInt(3, admission.getDoctor().getId());
                ps.setTimestamp(
                        4, Timestamp.valueOf(admission.getAdmissionDate()));
                ps.setString(5, admission.getRoomNumber());
                ps.setString(6, admission.getStatus());

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            // Both operations succeeded.
            con.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }
}

    

    // Get Admission by ID
    public Admission getAdmissionById(String admissionId)
            throws SQLException {
        String sql = "SELECT a.*, r.ward_name FROM admissions a "
                + "JOIN rooms r ON a.room_number = r.room_number "
                + "WHERE a.admission_id = ?";

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
        String sql = "SELECT a.*, r.ward_name FROM admissions a "
                + "JOIN rooms r ON a.room_number = r.room_number "
                + "ORDER BY a.admission_date DESC";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                admissions.add(mapAdmission(rs));
            }
        }

        return admissions;
    }

    public List<Admission> getActiveAdmissions() throws SQLException {
        List<Admission> admissions = new ArrayList<>();
        String sql = "SELECT a.*, r.ward_name FROM admissions a "
                + "JOIN rooms r ON a.room_number = r.room_number "
                + "WHERE a.status = 'Active' "
                + "ORDER BY a.admission_date DESC";

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

    String findAdmissionSql =
            "SELECT room_number FROM admissions "
            + "WHERE admission_id = ? AND status = 'Active' "
            + "FOR UPDATE";

    String dischargeSql =
            "UPDATE admissions SET status = 'Discharged', "
            + "discharge_date = CURRENT_TIMESTAMP "
            + "WHERE admission_id = ? AND status = 'Active'";

    String releaseBedSql =
            "UPDATE rooms SET available = TRUE "
            + "WHERE room_number = ? AND available = FALSE "
            + "AND NOT EXISTS (SELECT 1 FROM admissions "
            + "WHERE room_number = ? AND status = 'Active')";

    try (Connection con = DBConnection.getInstance().getConnection()) {

        boolean originalAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try {
            String roomNumber;

            // Step 1: Find the bed assigned to this active admission.
            try (PreparedStatement ps =
                    con.prepareStatement(findAdmissionSql)) {

                ps.setString(1, admissionId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }

                    roomNumber = rs.getString("room_number");
                }
            }

            // Step 2: Mark the patient as discharged within this transaction.
            try (PreparedStatement ps =
                    con.prepareStatement(dischargeSql)) {

                ps.setString(1, admissionId);

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            // Step 3: Make the bed available again.
            try (PreparedStatement ps =
                    con.prepareStatement(releaseBedSql)) {

                ps.setString(1, roomNumber);
                ps.setString(2, roomNumber);

                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            return true;

        } catch (SQLException | RuntimeException e) {
            try {
                con.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
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
        Timestamp dischargeTimestamp = rs.getTimestamp("discharge_date");
        if (dischargeTimestamp != null) {
            admission.setDischargeDate(
                    dischargeTimestamp.toLocalDateTime());
        }
        admission.setWardName(rs.getString("ward_name"));

        return admission;
    }

    // Get admissions for a specific doctor
public List<Admission> getAdmissionsByDoctorId(int doctorId)
        throws SQLException {

    List<Admission> admissions = new ArrayList<>();

    String sql = "SELECT a.*, r.ward_name FROM admissions a "
            + "JOIN rooms r ON a.room_number = r.room_number "
            + "WHERE a.doctor_id = ?";

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

    String sql = "SELECT a.*, r.ward_name FROM admissions a "
            + "JOIN rooms r ON a.room_number = r.room_number "
            + "WHERE a.patient_id = ?";

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
   
public List<String[]> getAdmissionWardAndBedDetails()
        throws SQLException {

    List<String[]> details = new ArrayList<>();

    String sql =
            "SELECT a.admission_id, a.room_number, "
            + "r.ward_name, r.bed_no, a.status, "
            + "a.admission_date, a.discharge_date "
            + "FROM admissions a "
            + "LEFT JOIN rooms r "
            + "ON a.room_number = r.room_number "
            + "ORDER BY a.admission_date DESC";

    try (Connection con = DBConnection.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            details.add(new String[] {
                rs.getString("admission_id"),
                rs.getString("room_number"),
                rs.getString("ward_name"),
                String.valueOf(rs.getInt("bed_no")),
                rs.getString("status"),
                String.valueOf(rs.getTimestamp("admission_date")),
                String.valueOf(rs.getTimestamp("discharge_date"))
            });
        }
    }

    return details;
}

}
