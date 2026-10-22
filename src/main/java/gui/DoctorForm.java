package gui;

import javax.swing.*;
import java.awt.*;

public class DoctorForm extends JFrame {
    private JTextField txtName, txtSpecialization, txtQualification, txtPhone, txtEmail;
    private JButton btnSubmit, btnClear;

    public DoctorForm() {
        setTitle("Doctor Registration Form");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        add(new JLabel(" Doctor Name:"));
        txtName = new JTextField();
        add(txtName);

        add(new JLabel(" Specialization:"));
        txtSpecialization = new JTextField();
        add(txtSpecialization);

        add(new JLabel(" Qualification:"));
        txtQualification = new JTextField();
        add(txtQualification);

        add(new JLabel(" Phone:"));
        txtPhone = new JTextField();
        add(txtPhone);

        add(new JLabel(" Email:"));
        txtEmail = new JTextField();
        add(txtEmail);

        btnSubmit = new JButton("Submit");
        btnClear = new JButton("Clear");
        add(btnSubmit);
        add(btnClear);

        btnSubmit.addActionListener(e -> JOptionPane.showMessageDialog(null, "Doctor Data Saved (UI Ready for DB)"));
        btnClear.addActionListener(e -> {
            txtName.setText(""); txtSpecialization.setText(""); txtQualification.setText(""); txtPhone.setText(""); txtEmail.setText("");
        });
    }
}