
package dao;

import model.Bill;
import model.Patient;
import model.Appointment;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    // Add Bill
    public boolean addBill(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills "
                + "(patient_id, appointment_id, consultation_fee, "
                + "medicine_charges, room_charges, other_charges, "
                + "total_amount, bill_date, payment_status, payment_method) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        bill.calculateTotal();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bill.getPatient().getId());

            if (bill.getAppointment() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, bill.getAppointment().getAppointmentId());
            }

            ps.setDouble(3, bill.getConsultationFee());
            ps.setDouble(4, bill.getMedicineCharges());
            ps.setDouble(5, bill.getRoomCharges());
            ps.setDouble(6, bill.getOtherCharges());
            ps.setDouble(7, bill.getTotalAmount());
            ps.setDate(8, Date.valueOf(bill.getBillDate()));
            ps.setString(9, bill.getPaymentStatus());
            ps.setString(10, bill.getPaymentMethod());

            return ps.executeUpdate() > 0;
        }
    }

    // Get Bill by ID
    public Bill getBillById(int billId) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, billId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBill(rs);
                }
            }
        }

        return null;
    }

    // Get All Bills
    public List<Bill> getAllBills() throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        }

        return bills;
    }

    // Get Bills by Patient ID
    public List<Bill> getBillsByPatientId(int patientId)
            throws SQLException {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE patient_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bills.add(mapBill(rs));
                }
            }
        }

        return bills;
    }

    // Update Bill
    public boolean updateBill(Bill bill) throws SQLException {
        String sql = "UPDATE bills SET patient_id = ?, appointment_id = ?, "
                + "consultation_fee = ?, medicine_charges = ?, "
                + "room_charges = ?, other_charges = ?, total_amount = ?, "
                + "bill_date = ?, payment_status = ?, payment_method = ? "
                + "WHERE bill_id = ?";

        bill.calculateTotal();

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bill.getPatient().getId());

            if (bill.getAppointment() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, bill.getAppointment().getAppointmentId());
            }

            ps.setDouble(3, bill.getConsultationFee());
            ps.setDouble(4, bill.getMedicineCharges());
            ps.setDouble(5, bill.getRoomCharges());
            ps.setDouble(6, bill.getOtherCharges());
            ps.setDouble(7, bill.getTotalAmount());
            ps.setDate(8, Date.valueOf(bill.getBillDate()));
            ps.setString(9, bill.getPaymentStatus());
            ps.setString(10, bill.getPaymentMethod());
            ps.setInt(11, bill.getBillId());

            return ps.executeUpdate() > 0;
        }
    }

    // Mark Bill as Paid
    public boolean markBillAsPaid(int billId, String paymentMethod)
            throws SQLException {
        String sql = "UPDATE bills SET payment_status = 'Paid', "
                + "payment_method = ? WHERE bill_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, paymentMethod);
            ps.setInt(2, billId);

            return ps.executeUpdate() > 0;
        }
    }

    // Delete Bill
    public boolean deleteBill(int billId) throws SQLException {
        String sql = "DELETE FROM bills WHERE bill_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        }
    }

    // Convert database row into Bill object
    private Bill mapBill(ResultSet rs) throws SQLException {
        PatientDAO patientDAO = new PatientDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();

        Patient patient =
                patientDAO.getPatientById(rs.getInt("patient_id"));

        int appointmentId = rs.getInt("appointment_id");
        Appointment appointment = null;

        if (!rs.wasNull()) {
            appointment =
                    appointmentDAO.getAppointmentById(appointmentId);
        }

        Bill bill = new Bill(
                rs.getInt("bill_id"),
                patient,
                appointment,
                rs.getDouble("consultation_fee"),
                rs.getDouble("medicine_charges"),
                rs.getDouble("room_charges"),
                rs.getDouble("other_charges"),
                rs.getDate("bill_date").toLocalDate(),
                rs.getString("payment_status"),
                rs.getString("payment_method")
        );

        return bill;
    }
}
