package model;

public class Patient extends Person {

    private String opNumber;
    private String disease;
    private String bloodGroup;
    private String address;

    public Patient() {
        super();
    }

    public Patient(int id, String firstname, String lastname,
                   int age, String phonenumber, String email,
                   String opNumber, String disease,
                   String bloodGroup, String address) {

        super(id, firstname, lastname, age, phonenumber, email);

        this.opNumber = opNumber;
        this.disease = disease;
        this.bloodGroup = bloodGroup;
        this.address = address;
    }

    public String getOpNumber() {
        return opNumber;
    }

    public void setOpNumber(String opNumber) {
        this.opNumber = opNumber;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void displayRole() {
        System.out.println("Role: Patient");
    }

    public void displayDetails() {
        System.out.println("OP Number: " + opNumber);
        System.out.println("Patient ID: " + getId());
        System.out.println("Name: " + getFirstname() + " " + getLastname());
        System.out.println("Age: " + getAge());
        System.out.println("Phone: " + getPhonenumber());
        System.out.println("Email: " + getEmail());
        System.out.println("Disease: " + disease);
        System.out.println("Blood Group: " + bloodGroup);
        System.out.println("Address: " + address);
    }
}