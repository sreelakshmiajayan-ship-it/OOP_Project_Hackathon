
package model;

public class Admin extends User {

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int id, String firstname, String lastname,
                 String email, String phonenumber, String passwordHash) {
        super();
        setID(id);
        setFirstname(firstname);
        setLastname(lastname);
        setEmail(email);
        setPhonenumber(phonenumber);
        setPassword(passwordHash);
        setRole("ADMIN");
    }

    public void displayDetails() {
        System.out.println("Admin ID: " + getID());
        System.out.println("First Name: " + getFirstname());
        System.out.println("Last Name: " + getLastname());
        System.out.println("Email: " + getEmail());
        System.out.println("Phone Number: " + getPhonenumber());
        System.out.println("Role: " + getRole());
    }

}