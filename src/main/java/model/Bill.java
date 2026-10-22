package model;

import java.time.LocalDate;

public class Bill {

    private int billId;
    private Patient patient;
    private Appointment appointment;
    private double consultationFee;
    private double medicineCharges;
    private double roomCharges;
    private double otherCharges;
    private double totalAmount;
    private LocalDate billDate;
    private String paymentStatus;
    private String paymentMethod;


    public Bill() {
        this.billDate = LocalDate.now();
        this.paymentStatus = "Pending";
    }

    public Bill(int billId, Patient patient, Appointment appointment,
                double consultationFee, double medicineCharges,
                double roomCharges, double otherCharges,
                LocalDate billDate, String paymentStatus,
                String paymentMethod) {

        this.billId = billId;
        this.patient = patient;
        this.appointment = appointment;
        this.consultationFee = consultationFee;
        this.medicineCharges = medicineCharges;
        this.roomCharges = roomCharges;
        this.otherCharges = otherCharges;
        this.billDate = billDate;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;

        calculateTotal();
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        calculateTotal();
    }

    public double getMedicineCharges() {
        return medicineCharges;
    }

    public void setMedicineCharges(double medicineCharges) {
        this.medicineCharges = medicineCharges;
        calculateTotal();
    }

    public double getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(double roomCharges) {
        this.roomCharges = roomCharges;
        calculateTotal();
    }

    public double getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(double otherCharges) {
        this.otherCharges = otherCharges;
        calculateTotal();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void calculateTotal() {
        totalAmount = consultationFee + medicineCharges
                    + roomCharges + otherCharges;
    }

    public void markAsPaid(String paymentMethod) {
        this.paymentStatus = "Paid";
        this.paymentMethod = paymentMethod;
    }

    public void displayDetails() {
        System.out.println("Bill ID: " + billId);

        if (patient != null) {
            System.out.println("Patient: " + patient.getFirstname()
                    + " " + patient.getLastname());
            System.out.println("OP Number: " + patient.getOpNumber());
        }

        System.out.println("Bill Date: " + billDate);
        System.out.println("Consultation Fee: " + consultationFee);
        System.out.println("Medicine Charges: " + medicineCharges);
        System.out.println("Room Charges: " + roomCharges);
        System.out.println("Other Charges: " + otherCharges);
        System.out.println("Total Amount: " + totalAmount);
        System.out.println("Payment Status: " + paymentStatus);
        System.out.println("Payment Method: " + paymentMethod);
    }
}