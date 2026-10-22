
package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.security.GeneralSecurityException;

import model.Patient;
import model.Doctor;
import service.PatientService;
import service.AuthService;

public class LoginFrame extends JFrame {

    private JComboBox<String> roleBox;
    private JPanel loginFields;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Patient login fields
    private JTextField patientNameField;
    private JTextField patientEmailField;

    // Doctor login fields
    private JTextField doctorEmailField;

    // Admin login field
    private JTextField adminIdField;

    private JPasswordField passwordField;

    private final PatientService patientService = new PatientService();
    private final AuthService authService = new AuthService();

    public LoginFrame() {

        setTitle("Hospital Management System");
        setSize(500, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createSignupPanel(), "SIGNUP");

        add(mainPanel);

        cardLayout.show(mainPanel, "LOGIN");

        setVisible(true);
    }

    // ---------------- LOGIN PANEL ----------------

    private JPanel createLoginPanel() {

        JPanel panel = new JPanel(new BorderLayout(15, 15));

        panel.setBorder(
                BorderFactory.createEmptyBorder(25, 35, 25, 35)
        );

        panel.setBackground(new Color(240, 248, 255));

        JLabel title = new JLabel(
                "HOSPITAL MANAGEMENT SYSTEM",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 19));
        title.setForeground(new Color(20, 100, 120));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        content.add(new JLabel("Select User Role"));
        content.add(Box.createVerticalStrut(5));

        String[] roles = {"Patient", "Doctor", "Admin"};

        roleBox = new JComboBox<>(roles);
        roleBox.setMaximumSize(new Dimension(400, 35));

        content.add(roleBox);
        content.add(Box.createVerticalStrut(20));

        loginFields = new JPanel(new GridLayout(0, 1, 5, 8));
        loginFields.setOpaque(false);
        content.add(loginFields);

        roleBox.addActionListener(e -> updateLoginFields());
        updateLoginFields();

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(20, 110, 130));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> login());

        content.add(Box.createVerticalStrut(15));
        content.add(loginButton);

        JButton signupButton = new JButton("New Patient? Sign Up");
        signupButton.addActionListener(
                e -> cardLayout.show(mainPanel, "SIGNUP")
        );

        content.add(Box.createVerticalStrut(8));
        content.add(signupButton);

        panel.add(title, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    // ---------------- UPDATE LOGIN FIELDS ----------------

    private void updateLoginFields() {

        loginFields.removeAll();

        String role = (String) roleBox.getSelectedItem();

        if ("Patient".equals(role)) {

    patientNameField = new JTextField();
    patientEmailField = new JTextField();
    passwordField = new JPasswordField();

    loginFields.add(new JLabel("Patient Name"));
    loginFields.add(patientNameField);

    loginFields.add(new JLabel("Email"));
    loginFields.add(patientEmailField);

    loginFields.add(new JLabel("Password"));
    loginFields.add(passwordField);



        } else if ("Doctor".equals(role)) {

            doctorEmailField = new JTextField();
            passwordField = new JPasswordField();

            loginFields.add(new JLabel("Doctor Email"));
            loginFields.add(doctorEmailField);

            loginFields.add(new JLabel("Password"));
            loginFields.add(passwordField);

        } else {

            adminIdField = new JTextField();
            passwordField = new JPasswordField();

            loginFields.add(new JLabel("Admin ID"));
            loginFields.add(adminIdField);

            loginFields.add(new JLabel("Password"));
            loginFields.add(passwordField);
        }

        loginFields.revalidate();
        loginFields.repaint();
    }

    // ---------------- SIGNUP PANEL ----------------

    private JPanel createSignupPanel() {

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));

        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        );

        JLabel title = new JLabel(
                "NEW PATIENT SIGN UP",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 20));

        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        panel.add(title);

        panel.add(new JLabel("Full Name"));
        panel.add(nameField);

        panel.add(new JLabel("Age"));
        panel.add(ageField);

        panel.add(new JLabel("Phone Number"));
        panel.add(phoneField);

        panel.add(new JLabel("Email"));
        panel.add(emailField);

        panel.add(new JLabel("Create Password"));
        panel.add(newPasswordField);

        panel.add(new JLabel("Confirm Password"));
        panel.add(confirmPasswordField);

        JButton signupButton = new JButton("Create Account");

        signupButton.addActionListener(e -> {

            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            String password =
                    new String(newPasswordField.getPassword());

            String confirmPassword =
                    new String(confirmPasswordField.getPassword());

            if (name.isEmpty() || ageText.isEmpty()
                    || phone.isEmpty() || email.isEmpty()
                    || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this, "Please fill in all fields."
                );
                return;
            }

            if (!name.matches("[\\p{L} .'-]+")) {
                JOptionPane.showMessageDialog(
                        this, "Please enter a valid name."
                );
                return;
            }

            String[] nameParts = name.split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName =
                    nameParts.length > 1 ? nameParts[1] : "";

            int age;

            try {
                age = Integer.parseInt(ageText);

                if (age <= 0 || age > 120) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this, "Enter an age between 1 and 120."
                );
                return;
            }

            if (!phone.matches("[0-9]{7,15}")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Enter a phone number with 7-15 digits."
                );
                return;
            }

            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                JOptionPane.showMessageDialog(
                        this, "Enter a valid email address."
                );
                return;
            }

            if (password.length() < 8) {
                JOptionPane.showMessageDialog(
                        this,
                        "Password must be at least 8 characters."
                );
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(
                        this, "Passwords do not match."
                );
                return;
            }

            Patient patient = new Patient();

            patient.setFirstname(firstName);
            patient.setLastname(lastName);
            patient.setAge(age);
            patient.setPhonenumber(phone);
            patient.setEmail(email);

            try {

                boolean registered =
                        patientService.registerPatientWithAccount(
                                patient, password
                        );

                if (registered) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Registration successful!\n"
                                    + "Your OP Number: "
                                    + patient.getOpNumber()
                                    + "\nYou can now log in."
                    );

                    nameField.setText("");
                    ageField.setText("");
                    phoneField.setText("");
                    emailField.setText("");
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");

                    cardLayout.show(mainPanel, "LOGIN");

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Registration failed. Try again."
                    );
                }

            } catch (SQLException | GeneralSecurityException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Registration failed.\n"
                                + "Check the database connection and "
                                + "whether the email already exists.\n"
                                + "Details: " + ex.getMessage(),
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE
                );

                ex.printStackTrace();

            } catch (IllegalArgumentException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Invalid Information",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        JButton backButton = new JButton("Back to Login");

        backButton.addActionListener(
                e -> cardLayout.show(mainPanel, "LOGIN")
        );

        panel.add(signupButton);
        panel.add(backButton);

        return panel;
    }

    // ---------------- LOGIN AUTHENTICATION ----------------


private void login() {

    String role = (String) roleBox.getSelectedItem();
    String password = new String(passwordField.getPassword());

    // PATIENT LOGIN
    if ("Patient".equals(role)) {

        String patientName = patientNameField.getText().trim();
        String email = patientEmailField.getText().trim();

        if (patientName.isEmpty() || email.isEmpty()
                || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your name, email and password."
            );
            return;
        }

        try {
            boolean authenticated =
                    authService.authenticatePatient(email, password);

            if (authenticated) {
                Patient patient = patientService.findPatientByEmail(email);
                if (patient == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "This account has no matching patient profile.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                JOptionPane.showMessageDialog(this, "Login successful!");
                new PatientDashboard(patient).setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid email or password, or this account "
                                + "is not registered as a Patient.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (SQLException | GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Patient login error: " + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }

    
    // DOCTOR LOGIN
    } else if ("Doctor".equals(role)) {

        String email = doctorEmailField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter your doctor email and password."
            );
            return;
        }

        try {
            Doctor doctor =
                    authService.authenticateDoctor(email, password);
            if (doctor == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid doctor email or password, or no matching "
                                + "doctor profile was found.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(this, "Login successful!");
            new DoctorDashboard(doctor).setVisible(true);
            dispose();
        } catch (SQLException | GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Doctor login error: " + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }

    // ADMIN LOGIN
    } else if ("Admin".equals(role)) {

        String adminId = adminIdField.getText().trim();

        if (adminId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter Admin ID and Password."
            );
            return;
        }

        try {
            boolean authenticated =
                    authService.authenticateAdmin(adminId, password);

            if (authenticated) {
                JOptionPane.showMessageDialog(
                        this,
                        "Admin login successful!"
                );

                new AdminDashboard().setVisible(true);
                dispose();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid Admin ID or Password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (SQLException | GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Admin login error: " + ex.getMessage(),
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }
}
}