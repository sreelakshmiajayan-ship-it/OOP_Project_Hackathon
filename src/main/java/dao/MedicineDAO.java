
package dao;

import model.Medicine;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    
    public boolean addMedicine(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines "
                + "(medicine_name, manufacturer, batch_number, price, "
                + "stock_quantity, expiry_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, medicine.getMedicineName());
            ps.setString(2, medicine.getManufacturer());
            ps.setString(3, medicine.getBatchNumber());
            ps.setDouble(4, medicine.getPrice());
            ps.setInt(5, medicine.getStockQuantity());

            if (medicine.getExpiryDate() == null) {
                ps.setNull(6, java.sql.Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(medicine.getExpiryDate()));
            }

            return ps.executeUpdate() > 0;
        }
    }

    // Get Medicine by ID
    public Medicine getMedicineById(int id) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMedicine(rs);
                }
            }
        }

        return null;
    }

    // Get All Medicines
    public List<Medicine> getAllMedicines() throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        String sql = "SELECT * FROM medicines";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                medicines.add(mapMedicine(rs));
            }
        }

        return medicines;
    }

    // Update Medicine
    public boolean updateMedicine(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicines SET medicine_name = ?, "
                + "manufacturer = ?, batch_number = ?, price = ?, "
                + "stock_quantity = ?, expiry_date = ? "
                + "WHERE medicine_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, medicine.getMedicineName());
            ps.setString(2, medicine.getManufacturer());
            ps.setString(3, medicine.getBatchNumber());
            ps.setDouble(4, medicine.getPrice());
            ps.setInt(5, medicine.getStockQuantity());

            if (medicine.getExpiryDate() == null) {
                ps.setNull(6, java.sql.Types.DATE);
            } else {
                ps.setDate(6, Date.valueOf(medicine.getExpiryDate()));
            }

            ps.setInt(7, medicine.getMedicineId());

            return ps.executeUpdate() > 0;
        }
    }

    // Delete Medicine
    public boolean deleteMedicine(int id) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet to Medicine object
    private Medicine mapMedicine(ResultSet rs) throws SQLException {
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
