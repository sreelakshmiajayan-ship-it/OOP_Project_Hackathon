package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import model.Bill;
import model.Patient;
import service.BillingService;
import service.PatientService;

public class BillingForm extends JFrame {
    private JTextField billIdField;
    private JTextField patientIdField;
    private JTextField consultationFeeField;
    private JTextField medicineChargesField;
    private JTextField roomChargesField;
    private JTextField otherChargesField;
    private JComboBox<String> paymentStatusCombo;
    private JComboBox<String> paymentMethodCombo;
    private JButton submitButton;
    private JButton backButton;

    // Services
    private BillingService billingService = new BillingService();
    private PatientService patientService = new PatientService();

    public BillingForm() {
        setTitle("Hospital Management System - Billing Form");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel (Matching AdminDashboard style)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 55, 85));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Hospital Management System");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Patient Billing & Payment Management");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.WHITE);

        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headingPanel.setOpaque(false);
        headingPanel.add(title);
        headingPanel.add(subtitle);
        header.add(headingPanel, BorderLayout.WEST);

        backButton = new JButton("Close");
        backButton.setFocusPainted(false);
        header.add(backButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main Content Panel
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(new Color(245, 247, 250));
        content.setBorder(new EmptyBorder(25, 40, 25, 40));

        JLabel formTitle = new JLabel("Enter Bill Details");
        formTitle.setFont(new Font("Arial", Font.BOLD, 18));
        formTitle.setForeground(new Color(25, 55, 85));
        content.add(formTitle, BorderLayout.NORTH);

        // Form Fields Grid (8 rows)
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 12, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        formPanel.add(new JLabel("Bill ID:"));
        billIdField = new JTextField();
        formPanel.add(billIdField);

        formPanel.add(new JLabel("Patient ID:"));
        patientIdField = new JTextField();
        formPanel.add(patientIdField);

        formPanel.add(new JLabel("Consultation Fee:"));
        consultationFeeField = new JTextField("0.0");
        formPanel.add(consultationFeeField);

        formPanel.add(new JLabel("Medicine Charges:"));
        medicineChargesField = new JTextField("0.0");
        formPanel.add(medicineChargesField);

        formPanel.add(new JLabel("Room Charges:"));
        roomChargesField = new JTextField("0.0");
        formPanel.add(roomChargesField);

        formPanel.add(new JLabel("Other Charges:"));
        otherChargesField = new JTextField("0.0");
        formPanel.add(otherChargesField);

        formPanel.add(new JLabel("Payment Status:"));
        paymentStatusCombo = new JComboBox<>(new String[]{"Pending", "Paid"});
        formPanel.add(paymentStatusCombo);

        formPanel.add(new JLabel("Payment Method:"));
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Credit Card", "UPI", "Insurance", "Debit Card"});
        formPanel.add(paymentMethodCombo);

        content.add(formPanel, BorderLayout.CENTER);

        // Submit Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        submitButton = new JButton("Generate Bill");
        submitButton.setFont(new Font("Arial", Font.BOLD, 14));
        submitButton.setBackground(new Color(25, 55, 85));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        buttonPanel.add(submitButton);

        content.add(buttonPanel, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        // Actions
        backButton.addActionListener(e -> dispose());

        submitButton.addActionListener(e -> {
            try {
                String billIdText = billIdField.getText().trim();
                String patientIdText = patientIdField.getText().trim();

                if (billIdText.isEmpty() || patientIdText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Bill ID and Patient ID are required fields.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int billId = Integer.parseInt(billIdText);
                int patientIdInt = Integer.parseInt(patientIdText);

                // Fetch Patient safely using PatientService
                Patient patient = patientService.findPatientById(patientIdInt);
                if (patient == null) {
                    JOptionPane.showMessageDialog(this, "Patient not found with ID: " + patientIdText, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double consultationFee = consultationFeeField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(consultationFeeField.getText().trim());
                double medicineCharges = medicineChargesField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(medicineChargesField.getText().trim());
                double roomCharges = roomChargesField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(roomChargesField.getText().trim());
                double otherCharges = otherChargesField.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(otherChargesField.getText().trim());

                String paymentStatus = (String) paymentStatusCombo.getSelectedItem();
                String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

                // Create Bill object using your detailed constructor (Appointment can be null if not required for standard billing)
                Bill bill = new Bill(
                    billId, 
                    patient, 
                    null, 
                    consultationFee, 
                    medicineCharges, 
                    roomCharges, 
                    otherCharges, 
                    LocalDate.now(), 
                    paymentStatus, 
                    paymentMethod
                );

                // Call BillingService to add bill (assuming your service has a createBill or addBill method)
                boolean success = billingService.generateBill(bill); // Change method name if your BillingService uses addBill instead

                if (success) {
                    JOptionPane.showMessageDialog(this, "Bill generated successfully! Total Amount: " + bill.getTotalAmount());
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save bill to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for IDs and charges.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
