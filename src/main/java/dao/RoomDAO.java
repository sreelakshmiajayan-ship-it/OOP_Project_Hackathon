
package dao;

import model.Room;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    // Add Room
    public boolean addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms "
                + "(room_number, room_type, daily_charge, available) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setDouble(3, room.getDailyCharge());
            ps.setBoolean(4, room.isAvailable());

            return ps.executeUpdate() > 0;
        }
    }

    // Get Room by ID
    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        }

        return null;
    }

    // Get All Rooms
    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        }

        return rooms;
    }

    // Get Available Rooms
    public List<Room> getAvailableRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE available = TRUE";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        }

        return rooms;
    }

    // Update Room Details
    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, "
                + "room_type = ?, daily_charge = ?, available = ? "
                + "WHERE room_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setDouble(3, room.getDailyCharge());
            ps.setBoolean(4, room.isAvailable());
            ps.setInt(5, room.getRoomId());

            return ps.executeUpdate() > 0;
        }
    }

    // Book Room
    public boolean bookRoom(int roomId) throws SQLException {
        String sql = "UPDATE rooms SET available = FALSE "
                + "WHERE room_id = ? AND available = TRUE";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // Release Room
    public boolean releaseRoom(int roomId) throws SQLException {
        String sql = "UPDATE rooms SET available = TRUE "
                + "WHERE room_id = ? AND available = FALSE";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // Delete Room
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    // Convert ResultSet into Room object
    private Room mapRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getString("room_type"),
                rs.getDouble("daily_charge"),
                rs.getBoolean("available")
        );
    }


    // Get Room by Room Number
public Room getRoomByNumber(String roomNumber) throws SQLException {
    String sql = "SELECT * FROM rooms WHERE room_number = ?";

    try (Connection con = DBConnection.getInstance().getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, roomNumber);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapRoom(rs);
            }
        }
    }

    return null;
}

    
    // Get rooms by ward name
    public List<Room> getRoomsByWard(String wardName)
            throws SQLException {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT * FROM rooms WHERE ward_name = ? "
                   + "ORDER BY bed_no";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, wardName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        }

        return rooms;
    }

    // Get available rooms in a particular ward
    public List<Room> getAvailableRoomsByWard(String wardName)
            throws SQLException {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT * FROM rooms "
                   + "WHERE ward_name = ? AND available = TRUE "
                   + "ORDER BY bed_no";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, wardName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        }

        return rooms;
    }

    // Get all ward names
    public List<String> getAllWardNames() throws SQLException {

        List<String> wards = new ArrayList<>();

        String sql = "SELECT DISTINCT ward_name FROM rooms "
                   + "WHERE ward_name IS NOT NULL "
                   + "ORDER BY ward_name";

        try (Connection con = DBConnection.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                wards.add(rs.getString("ward_name"));
            }
        }

        return wards;
    }

}
