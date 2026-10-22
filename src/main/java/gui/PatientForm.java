package gui;

import javax.swing.*;
import java.awt.*;

public class PatientForm extends JFrame {
    private JTextField txtName, txtAge, txtGender, txtPhone, txtAddress;
    private JButton btnSubmit, btnClear;

    public PatientForm() {
        setTitle("Patient Registration Form");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        add(new JLabel(" Full Name:"));
        txtName = new JTextField();
        add(txtName);

        add(new JLabel(" Age:"));
        txtAge = new JTextField();
        add(txtAge);

        add(new JLabel(" Gender:"));
        txtGender = new JTextField();
        add(txtGender);

        add(new JLabel(" Phone:"));
        txtPhone = new JTextField();
        add(txtPhone);

        add(new JLabel(" Address:"));
        txtAddress = new JTextField();
        add(txtAddress);

        btnSubmit = new JButton("Submit");
        btnClear = new JButton("Clear");
        add(btnSubmit);
        add(btnClear);

        btnSubmit.addActionListener(e -> JOptionPane.showMessageDialog(null, "Patient Data Saved (UI Ready for DB)"));
        btnClear.addActionListener(e -> {
            txtName.setText(""); txtAge.setText(""); txtGender.setText(""); txtPhone.setText(""); txtAddress.setText("");
        });
    }
}