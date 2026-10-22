package model;

import java.time.LocalDate;

public class Medicine {

    private int medicineId;
    private String medicineName;
    private String manufacturer;
    private String batchNumber;
    private double price;
    private int stockQuantity;
    private LocalDate expiryDate;

    public Medicine() {
    }

    public Medicine(int medicineId, String medicineName,
                    String manufacturer, String batchNumber,
                    double price, int stockQuantity,
                    LocalDate expiryDate) {

        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.expiryDate = expiryDate;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return expiryDate != null
                && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isAvailable() {
        return stockQuantity > 0 && !isExpired();
    }

    public void displayDetails() {
        System.out.println("Medicine ID: " + medicineId);
        System.out.println("Medicine Name: " + medicineName);
        System.out.println("Manufacturer: " + manufacturer);
        System.out.println("Batch Number: " + batchNumber);
        System.out.println("Price: ₹" + price);
        System.out.println("Stock Quantity: " + stockQuantity);
        System.out.println("Expiry Date: " + expiryDate);
        System.out.println("Expired: " + isExpired());
        System.out.println("Available: " + isAvailable());
    }
}