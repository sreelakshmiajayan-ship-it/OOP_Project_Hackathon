package model;

public class Room {

    private int roomId;
    private String roomNumber;
    private String roomType;
    private String wardName;
    private int bedNo;
    private double dailyCharge;
    private boolean available;

    public Room() {
        this.available = true;
    }

    public Room(int roomId, String roomNumber, String roomType,
                double dailyCharge, boolean available) {

        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.dailyCharge = dailyCharge;
        this.available = available;
    }

    public Room(int roomId, String roomNumber, String roomType,
                String wardName, int bedNo, double dailyCharge,
                boolean available) {
        this(roomId, roomNumber, roomType, dailyCharge, available);
        this.wardName = wardName;
        this.bedNo = bedNo;
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

    public int getBedNo() {
        return bedNo;
    }

    public void setBedNo(int bedNo) {
        this.bedNo = bedNo;
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

    public void bookRoom() {
        if (available) {
            available = false;
            System.out.println("Room booked successfully.");
        } else {
            System.out.println("Room is already occupied.");
        }
    }

    public void releaseRoom() {
        available = true;
        System.out.println("Room is now available.");
    }

    public void displayDetails() {
        System.out.println("Room ID: " + roomId);
        System.out.println("Room Number: " + roomNumber);
        System.out.println("Room Type: " + roomType);
        System.out.println("Daily Charge: ₹" + dailyCharge);
        System.out.println("Available: " + (available ? "Yes" : "No"));
    }
}