package gui;

import model.Appointment;
import model.Doctor;
import model.Medicine;
import model.Prescription;
import service.AppointmentService;
import service.PrescriptionService;
import util.InputValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;



public class DoctorDashboard extends JFrame {

    private static final Color NAVY = new Color(25, 55, 85);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color ACCENT = new Color(20, 110, 130);

    private final Doctor doctor;
    private final AppointmentService appointmentService =
            new AppointmentService();
    private final PrescriptionService prescriptionService =
            new PrescriptionService();
    private final DefaultTableModel appointmentModel =
            createTableModel(new String[]{
                    "Appointment ID", "Patient", "Age", "Phone", "Email",
                    "Disease", "Date", "Time", "Status"
            });
    private final DefaultTableModel prescriptionModel =
            createTableModel(new String[]{
                    "Prescription ID", "Appointment ID", "Patient",
                    "Medicine", "Dosage", "Frequency", "Duration (days)",
                    "Date", "Instructions"
            });
    private final JTable appointmentTable = new JTable(appointmentModel);
    private final JTable prescriptionTable = new JTable(prescriptionModel);
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Completed", "Cancelled"});
    private final JButton updateStatusButton =
            new JButton("Update Selected Appointment");
    private final JButton createPrescriptionButton =
            new JButton("Create Prescription");

    private List<Appointment> appointments = List.of();

    public DoctorDashboard(Doctor doctor) {
        if (doctor == null || doctor.getId() <= 0) {
            throw new IllegalArgumentException(
                    "An authenticated doctor profile is required.");
        }
        this.doctor = doctor;

        setTitle("Hospital Management System - Doctor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 550));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        updateStatusButton.addActionListener(event -> updateAppointmentStatus());
        createPrescriptionButton.addActionListener(
                event -> loadPrescriptionChoices());
        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel headings = new JPanel(new GridLayout(2, 1, 0, 5));
        headings.setOpaque(false);

        JLabel title = new JLabel("Hospital Management System");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Doctor Dashboard");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 230, 240));
        headings.add(title);
        headings.add(subtitle);
        header.add(headings, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(event -> logout());
        header.add(logoutButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(12, 16));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(22, 26, 20, 26));

        JPanel welcomePanel = new JPanel(new BorderLayout(10, 5));
        welcomePanel.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, Dr. "
                + doctor.getFirstname() + " " + doctor.getLastname());
        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        welcome.setForeground(NAVY);

        String specialization = doctor.getSpecialization() == null
                ? "Doctor"
                : doctor.getSpecialization();
        String qualification = doctor.getQualification() == null
                ? ""
                : " | " + doctor.getQualification();
        JLabel profile = new JLabel(
                "Doctor ID: " + doctor.getId() + " | " + specialization
                        + qualification);
        profile.setForeground(new Color(85, 95, 105));

        JPanel profileText = new JPanel(new GridLayout(2, 1, 0, 5));
        profileText.setOpaque(false);
        profileText.add(welcome);
        profileText.add(profile);
        welcomePanel.add(profileText, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshData());
        welcomePanel.add(refreshButton, BorderLayout.EAST);
        content.add(welcomePanel, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Appointments", createAppointmentsPanel());
        tabs.addTab("My Prescriptions", createPrescriptionsPanel());
        content.add(tabs, BorderLayout.CENTER);
        return content;
    }

    private JPanel createAppointmentsPanel() {
        configureTable(appointmentTable);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 8, 8, 8));
        panel.add(new JScrollPane(appointmentTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(new JLabel("Set status:"));
        actions.add(statusBox);
        actions.add(updateStatusButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPrescriptionsPanel() {
        configureTable(prescriptionTable);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 8, 8, 8));
        panel.add(new JScrollPane(prescriptionTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(createPrescriptionButton);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshData() {
        setBusy(true);
        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws SQLException {
                return new DashboardData(
                        appointmentService.getAppointmentsForDoctor(
                                doctor.getId()),
                        prescriptionService.getPrescriptionsForDoctor(
                                doctor.getId()));
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    DashboardData data = get();
                    appointments = data.appointments;
                    appointmentModel.setRowCount(0);
                    for (Appointment appointment : appointments) {
                        var patient = appointment.getPatient();
                        appointmentModel.addRow(new Object[]{
                                appointment.getAppointmentId(),
                                patient == null ? "N/A"
                                        : patient.getFirstname() + " "
                                                + patient.getLastname(),
                                patient == null ? "" : patient.getAge(),
                                patient == null ? "" : patient.getPhonenumber(),
                                patient == null ? "" : patient.getEmail(),
                                patient == null ? "" : patient.getDisease(),
                                appointment.getAppointmentDate(),
                                appointment.getAppointmentTime(),
                                appointment.getStatus()
                        });
                    }

                    prescriptionModel.setRowCount(0);
                    for (Prescription prescription : data.prescriptions) {
                        Appointment appointment =
                                prescription.getAppointment();
                        prescriptionModel.addRow(new Object[]{
                                prescription.getPrescriptionId(),
                                appointment == null
                                        ? ""
                                        : appointment.getAppointmentId(),
                                patientName(prescription.getPatient()),
                                prescription.getMedicine() == null
                                        ? "N/A"
                                        : prescription.getMedicine()
                                                .getMedicineName(),
                                prescription.getDosage(),
                                prescription.getFrequency(),
                                prescription.getDurationDays(),
                                prescription.getPrescriptionDate(),
                                prescription.getInstructions()
                        });
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Loading dashboard data was interrupted.", ex);
                } catch (ExecutionException ex) {
                    showError("Could not load doctor dashboard data.",
                            ex.getCause());
                }
            }
        }.execute();
    }

    private void updateAppointmentStatus() {
        int selectedViewRow = appointmentTable.getSelectedRow();
        if (selectedViewRow < 0) {
            showWarning("Select an appointment first.");
            return;
        }

        int selectedModelRow =
                appointmentTable.convertRowIndexToModel(selectedViewRow);
        Appointment appointment = appointments.get(selectedModelRow);
        String status = (String) statusBox.getSelectedItem();
        if (status == null) {
            showWarning("Select an appointment status.");
            return;
        }

        updateStatusButton.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return appointmentService.updateStatusForDoctor(
                        appointment.getAppointmentId(), doctor.getId(), status);
            }

            @Override
            protected void done() {
                updateStatusButton.setEnabled(true);
                try {
                    if (get()) {
                        refreshData();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Updating the appointment was interrupted.", ex);
                } catch (ExecutionException ex) {
                    showError("Could not update the appointment.",
                            ex.getCause());
                }
            }
        }.execute();
    }

    private void loadPrescriptionChoices() {
        createPrescriptionButton.setEnabled(false);
        new SwingWorker<PrescriptionChoices, Void>() {
            @Override
            protected PrescriptionChoices doInBackground() throws SQLException {
                List<Appointment> scheduled = appointmentService
                        .getAppointmentsForDoctor(doctor.getId()).stream()
                        .filter(appointment ->
                                "Scheduled".equalsIgnoreCase(
                                        appointment.getStatus())
                                        || "Completed".equalsIgnoreCase(
                                                appointment.getStatus()))
                        .collect(Collectors.toList());
                return new PrescriptionChoices(
                        scheduled,
                        prescriptionService.getAvailableMedicines());
            }

            @Override
            protected void done() {
                createPrescriptionButton.setEnabled(true);
                try {
                    PrescriptionChoices choices = get();
                    if (choices.appointments.isEmpty()) {
                        showWarning(
                                "There are no scheduled appointments to "
                                        + "prescribe for.");
                        return;
                    }
                    if (choices.medicines.isEmpty()) {
                        showWarning("No available, unexpired medicines found.");
                        return;
                    }
                    showPrescriptionForm(choices);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Loading prescription choices was interrupted.",
                            ex);
                } catch (ExecutionException ex) {
                    showError("Could not load prescription choices.",
                            ex.getCause());
                }
            }
        }.execute();
    }

    private void showPrescriptionForm(PrescriptionChoices choices) {
        JComboBox<Appointment> appointmentBox =
                new JComboBox<>(choices.appointments.toArray(
                        new Appointment[0]));
        appointmentBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Appointment) {
                    Appointment appointment = (Appointment) value;
                    label.setText("Appointment "
                            + appointment.getAppointmentId() + " - "
                            + patientName(appointment.getPatient()) + " - "
                            + appointment.getAppointmentDate() + " "
                            + appointment.getAppointmentTime());
                }
                return label;
            }
        });

        JComboBox<Medicine> medicineBox =
                new JComboBox<>(choices.medicines.toArray(new Medicine[0]));
        medicineBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Medicine) {
                    Medicine medicine = (Medicine) value;
                    label.setText(medicine.getMedicineName()
                            + " (stock: " + medicine.getStockQuantity() + ")");
                }
                return label;
            }
        });

        JTextField dosageField = new JTextField();
        JTextField frequencyField = new JTextField();
        JTextField durationField = new JTextField();
        JTextArea instructionsArea = new JTextArea(3, 20);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("Appointment:"));
        form.add(appointmentBox);
        form.add(new JLabel("Medicine:"));
        form.add(medicineBox);
        form.add(new JLabel("Dosage:"));
        form.add(dosageField);
        form.add(new JLabel("Frequency:"));
        form.add(frequencyField);
        form.add(new JLabel("Duration (days):"));
        form.add(durationField);
        form.add(new JLabel("Instructions:"));
        form.add(new JScrollPane(instructionsArea));

        int choice = JOptionPane.showConfirmDialog(
                this, form, "Create Prescription",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        String durationText = durationField.getText().trim();
        if (!InputValidator.isNotEmpty(dosage)
                || !InputValidator.isNotEmpty(frequency)
                || !InputValidator.isValidQuantity(durationText)) {
            showWarning(
                    "Enter dosage, frequency, and a positive duration.");
            return;
        }

        Appointment appointment =
                (Appointment) appointmentBox.getSelectedItem();
        Medicine medicine = (Medicine) medicineBox.getSelectedItem();
        if (appointment == null || medicine == null) {
            showWarning("Select an appointment and medicine.");
            return;
        }

        Prescription prescription = new Prescription(
                0,
                appointment.getPatient(),
                doctor,
                appointment,
                medicine,
                dosage,
                frequency,
                Integer.parseInt(durationText),
                instructionsArea.getText().trim(),
                LocalDate.now());
        savePrescription(prescription);
    }

    private void savePrescription(Prescription prescription) {
        createPrescriptionButton.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return prescriptionService.createPrescription(
                        prescription, doctor.getId());
            }

            @Override
            protected void done() {
                createPrescriptionButton.setEnabled(true);
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                DoctorDashboard.this,
                                "Prescription saved successfully.",
                                "Prescription Saved",
                                JOptionPane.INFORMATION_MESSAGE);
                        refreshData();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError("Saving the prescription was interrupted.", ex);
                } catch (ExecutionException ex) {
    Throwable cause = ex.getCause();

    if (cause instanceof IllegalArgumentException) {
        showWarning(cause.getMessage());
    } else {
        showError("Could not save the prescription.", cause);
    }
}
            }
        }.execute();
    }

    private DefaultTableModel createTableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void configureTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
    }

    private String patientName(model.Patient patient) {
        return patient == null
                ? "N/A"
                : (patient.getFirstname() + " " + patient.getLastname()).trim();
    }

    private void setBusy(boolean busy) {
        setCursor(busy
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
        updateStatusButton.setEnabled(!busy);
        createPrescriptionButton.setEnabled(!busy);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Doctor Dashboard",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, Throwable cause) {
        String details = cause == null || cause.getMessage() == null
                ? ""
                : "\n" + cause.getMessage();
        JOptionPane.showMessageDialog(
                this, message + details, "Operation Failed",
                JOptionPane.ERROR_MESSAGE);
        if (cause != null) {
            cause.printStackTrace();
        }
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame();
        }
    }

    private static final class DashboardData {
        private final List<Appointment> appointments;
        private final List<Prescription> prescriptions;

        private DashboardData(
                List<Appointment> appointments,
                List<Prescription> prescriptions) {
            this.appointments = appointments;
            this.prescriptions = prescriptions;
        }
    }

    private static final class PrescriptionChoices {
        private final List<Appointment> appointments;
        private final List<Medicine> medicines;

        private PrescriptionChoices(
                List<Appointment> appointments, List<Medicine> medicines) {
            this.appointments = appointments;
            this.medicines = medicines;
        }
    }
}
