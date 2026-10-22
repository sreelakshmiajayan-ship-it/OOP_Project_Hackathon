package model;

public class Doctor extends Person {

    private String specialization;
    private String qualification;
    private double consultationFee;

    public Doctor() {
        super();
    }

    public Doctor(int id, String firstname, String lastname,
                  int age, String phonenumber, String email,
                  String specialization, String qualification,
                  double consultationFee) {

        super(id, firstname, lastname, age, phonenumber, email);

        this.specialization = specialization;
        this.qualification = qualification;
        this.consultationFee = consultationFee;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public void displayRole() {
        System.out.println("Role: Doctor");
    }

    public void displayDetails() {
        System.out.println("Doctor ID: " + getId());
        System.out.println("Name: " + getFirstname() + " " + getLastname());
        System.out.println("Age: " + getAge());
        System.out.println("Phone: " + getPhonenumber());
        System.out.println("Email: " + getEmail());
        System.out.println("Specialization: " + specialization);
        System.out.println("Qualification: " + qualification);
        System.out.println("Consultation Fee: " + consultationFee);
    }
}