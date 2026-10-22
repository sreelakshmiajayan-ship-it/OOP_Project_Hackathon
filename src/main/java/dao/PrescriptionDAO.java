
package dao;

import model.Prescription;
import model.Patient;
import model.Doctor;
import model.Appointment;
import model.Medicine;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public boolean addPrescription(Prescription prescription)
            throws SQLException {

        String sql = "INSERT INTO prescriptions "
                + "(patient_id, doctor_id, appointment_id, medicine_id, "
                + "dosage, frequency, duration_days, instructions, "
                + "prescription_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prescription.getPatient().getId());
            ps.setInt(2, prescription.getDoctor().getId());

            if (prescription.getAppointment() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3,
                        prescription.getAppointment().getAppointmentId());
            }

            ps.setInt(4, prescription.getMedicine().getMedicineId());
            ps.setString(5, prescription.getDosage());
            ps.setString(6, prescription.getFrequency());
            ps.setInt(7, prescription.getDurationDays());
            ps.setString(8, prescription.getInstructions());
            ps.setDate(9, Date.valueOf(prescription.getPrescriptionDate()));

            return ps.executeUpdate() > 0;
        }
    }

    public Prescription getPrescriptionById(int id)
            throws SQLException {

        String sql = "SELECT * FROM prescriptions "
                + "WHERE prescription_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPrescription(rs);
                }
            }
        }

        return null;
    }

    public List<Prescription> getAllPrescriptions()
            throws SQLException {

        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prescriptions.add(mapPrescription(rs));
            }
        }

        return prescriptions;
    }

    public List<Prescription> getPrescriptionsByDoctorId(int doctorId)
            throws SQLException {

        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE doctor_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(mapPrescription(rs));
                }
            }
        }

        return prescriptions;
    }

    public List<Prescription> getPrescriptionsByPatientId(int patientId)
        throws SQLException {

    List<Prescription> prescriptions = new ArrayList<>();

    String sql = "SELECT * FROM prescriptions WHERE patient_id = ?";

    try (Connection con = DBConnection.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, patientId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prescriptions.add(mapPrescription(rs));
            }
        }
    }

    return prescriptions;
}

    public boolean updatePrescription(Prescription prescription)
            throws SQLException {

        String sql = "UPDATE prescriptions SET patient_id = ?, "
                + "doctor_id = ?, appointment_id = ?, medicine_id = ?, "
                + "dosage = ?, frequency = ?, duration_days = ?, "
                + "instructions = ?, prescription_date = ? "
                + "WHERE prescription_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, prescription.getPatient().getId());
            ps.setInt(2, prescription.getDoctor().getId());

            if (prescription.getAppointment() == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3,
                        prescription.getAppointment().getAppointmentId());
            }

            ps.setInt(4, prescription.getMedicine().getMedicineId());
            ps.setString(5, prescription.getDosage());
            ps.setString(6, prescription.getFrequency());
            ps.setInt(7, prescription.getDurationDays());
            ps.setString(8, prescription.getInstructions());
            ps.setDate(9, Date.valueOf(prescription.getPrescriptionDate()));
            ps.setInt(10, prescription.getPrescriptionId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePrescription(int id) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE prescription_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Prescription mapPrescription(ResultSet rs)
            throws SQLException {

        PatientDAO patientDAO = new PatientDAO();
        DoctorDAO doctorDAO = new DoctorDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();

        Patient patient =
                patientDAO.getPatientById(rs.getInt("patient_id"));

        Doctor doctor =
                doctorDAO.getDoctorById(rs.getInt("doctor_id"));

        int appointmentId = rs.getInt("appointment_id");
        Appointment appointment = null;

        if (!rs.wasNull()) {
            appointment =
                    appointmentDAO.getAppointmentById(appointmentId);
        }

        Medicine medicine = getMedicineById(rs.getInt("medicine_id"));

        return new Prescription(
                rs.getInt("prescription_id"),
                patient,
                doctor,
                appointment,
                medicine,
                rs.getString("dosage"),
                rs.getString("frequency"),
                rs.getInt("duration_days"),
                rs.getString("instructions"),
                rs.getDate("prescription_date").toLocalDate()
        );
    }

    private Medicine getMedicineById(int id) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date expiry = rs.getDate("expiry_date");

                    return new Medicine(
                            rs.getInt("medicine_id"),
                            rs.getString("medicine_name"),
                            rs.getString("manufacturer"),
                            rs.getString("batch_number"),
                            rs.getDouble("price"),
                            rs.getInt("stock_quantity"),
                            expiry == null ? null : expiry.toLocalDate()
                    );
                }
            }
        }

        return null;
    }
}
