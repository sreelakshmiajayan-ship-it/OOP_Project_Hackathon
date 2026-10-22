package gui;

import dao.AppointmentDAO;
import model.Appointment;
import model.Doctor;
import model.Medicine;
import model.Prescription;
import service.PrescriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PrescriptionForm extends JFrame {

    private final Doctor authenticatedDoctor;

    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final AppointmentDAO appointmentDAO =
            new AppointmentDAO();

    private JComboBox<Appointment> cmbAppointment;
    private JComboBox<Medicine> cmbMedicine;
    private JTextField txtDosage;
    private JTextField txtFrequency;
    private JTextField txtDuration;
    private JTextArea txtInstructions;

    public PrescriptionForm() {
        this(null);
    }

    public PrescriptionForm(Doctor doctor) {
        authenticatedDoctor = doctor;

        setTitle("Create Prescription");
        setSize(650, 580);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel heading = new JLabel("Create Prescription");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 55, 85));

        mainPanel.add(heading, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 12, 15));
        formPanel.setOpaque(false);

        cmbAppointment = new JComboBox<>();
        cmbMedicine = new JComboBox<>();

        txtDosage = new JTextField();
        txtFrequency = new JTextField();
        txtDuration = new JTextField();

        txtInstructions = new JTextArea(3, 20);
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);

        formPanel.add(new JLabel("Appointment:"));
        formPanel.add(cmbAppointment);

        formPanel.add(new JLabel("Medicine:"));
        formPanel.add(cmbMedicine);

        formPanel.add(new JLabel("Dosage:"));
        formPanel.add(txtDosage);

        formPanel.add(new JLabel("Frequency:"));
        formPanel.add(txtFrequency);

        formPanel.add(new JLabel("Duration (days):"));
        formPanel.add(txtDuration);

        formPanel.add(new JLabel("Instructions:"));
        formPanel.add(new JScrollPane(txtInstructions));

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JButton btnSave = new JButton("Save Prescription");
        JButton btnClear = new JButton("Clear");
        JButton btnClose = new JButton("Close");

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnClose);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        cmbAppointment.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Appointment) {
                    Appointment appointment = (Appointment) value;
                    String patientName = "Unknown patient";

                    if (appointment.getPatient() != null) {
                        patientName = (
                                appointment.getPatient().getFirstname()
                                + " "
                                + appointment.getPatient().getLastname()
                        ).trim();
                    }

                    setText(
                            "Appointment #" + appointment.getAppointmentId()
                            + " - " + patientName
                            + " - " + appointment.getAppointmentDate()
                            + " " + appointment.getAppointmentTime()
                            + " (" + appointment.getStatus() + ")"
                    );
                }

                return this;
            }
        });

        cmbMedicine.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Medicine) {
                    Medicine medicine = (Medicine) value;
                    setText(
                            medicine.getMedicineName()
                            + " (ID: " + medicine.getMedicineId() + ")"
                    );
                }

                return this;
            }
        });

        btnSave.addActionListener(e -> savePrescription());

        btnClear.addActionListener(e -> clearForm());

        btnClose.addActionListener(e -> dispose());

        loadFormData();

        setVisible(true);
    }

    private void loadFormData() {
        if (authenticatedDoctor == null
                || authenticatedDoctor.getId() <= 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Doctor login details are missing. "
                    + "Please log in again.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );

            dispose();
            return;
        }

        try {
            List<Appointment> appointments =
                    appointmentDAO.getAppointmentsByDoctorId(
                            authenticatedDoctor.getId()
                    );

            for (Appointment appointment : appointments) {
                String status = appointment.getStatus();

                if ("Scheduled".equalsIgnoreCase(status)
                        || "Completed".equalsIgnoreCase(status)) {
                    cmbAppointment.addItem(appointment);
                }
            }

            List<Medicine> medicines =
                    prescriptionService.getAvailableMedicines();

            for (Medicine medicine : medicines) {
                cmbMedicine.addItem(medicine);
            }

            if (cmbAppointment.getItemCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No eligible appointments are available "
                        + "for prescribing."
                );
            }

            if (cmbMedicine.getItemCount() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "No available medicines were found."
                );
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load appointments or medicines:\n"
                    + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void savePrescription() {
        Appointment appointment =
                (Appointment) cmbAppointment.getSelectedItem();

        Medicine medicine =
                (Medicine) cmbMedicine.getSelectedItem();

        String dosage = txtDosage.getText().trim();
        String frequency = txtFrequency.getText().trim();
        String durationText = txtDuration.getText().trim();
        String instructions = txtInstructions.getText().trim();

        if (appointment == null || medicine == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select an appointment and a medicine.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (dosage.isEmpty() || frequency.isEmpty()
                || durationText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter dosage, frequency, and duration.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int durationDays;

        try {
            durationDays = Integer.parseInt(durationText);

            if (durationDays <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Duration must be a positive whole number.",
                    "Invalid Duration",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Prescription prescription = new Prescription();

        prescription.setPatient(appointment.getPatient());
        prescription.setDoctor(authenticatedDoctor);
        prescription.setAppointment(appointment);
        prescription.setMedicine(medicine);
        prescription.setDosage(dosage);
        prescription.setFrequency(frequency);
        prescription.setDurationDays(durationDays);
        prescription.setInstructions(instructions);
        prescription.setPrescriptionDate(LocalDate.now());

        try {
            boolean saved = prescriptionService.createPrescription(
                    prescription,
                    authenticatedDoctor.getId()
            );

            if (saved) {
                JOptionPane.showMessageDialog(
                        this,
                        "Prescription saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                clearForm();

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Prescription could not be saved.",
                        "Save Failed",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error while saving prescription:\n"
                    + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearForm() {
        cmbAppointment.setSelectedIndex(-1);
        cmbMedicine.setSelectedIndex(-1);
        txtDosage.setText("");
        txtFrequency.setText("");
        txtDuration.setText("");
        txtInstructions.setText("");
    }
}