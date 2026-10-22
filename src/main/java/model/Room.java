
package model;

public class Room {

    private int roomId;
    private String roomNumber;
    private String roomType;
    private String wardName;
    private double dailyCharge;
    private boolean available;

    // Default constructor
    public Room() {
        this.available = true;
    }

    // Existing constructor preserved
    public Room(int roomId, String roomNumber, String roomType,
                double dailyCharge, boolean available) {

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.dailyCharge = dailyCharge;
        this.available = available;
    }

    // New constructor with ward information
    public Room(int roomId, String roomNumber, String roomType,
                String wardName, double dailyCharge, boolean available) {

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.wardName = wardName;
        this.dailyCharge = dailyCharge;
        this.available = available;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public double getDailyCharge() {
        return dailyCharge;
    }

    public void setDailyCharge(double dailyCharge) {
        this.dailyCharge = dailyCharge;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Mark the room as occupied
    public void bookRoom() {
        if (available) {
            available = false;
            System.out.println("Room booked successfully.");
        } else {
            System.out.println("Room is already occupied.");
        }
    }

    // Release the room after discharge
    public void releaseRoom() {
        available = true;
        System.out.println("Room is now available.");
    }

    public void displayDetails() {
        System.out.println("Room ID: " + roomId);
        System.out.println("Room Number: " + roomNumber);
        System.out.println("Room Type: " + roomType);
        System.out.println("Ward: "
                + (wardName != null ? wardName : "Not specified"));
        System.out.println("Daily Charge: ₹" + dailyCharge);
        System.out.println("Available: " + (available ? "Yes" : "No"));
    }
}
