package gui;

import dao.MedicineDAO;
import dao.RoomDAO;
import model.Admission;
import model.Appointment;
import model.Bill;
import model.Doctor;
import model.Medicine;
import model.Patient;
import model.Room;
import service.AdmissionService;
import service.AdminService;
import service.AppointmentService;
import service.BillingService;
import service.DoctorService;
import service.PatientService;
import util.InputValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class AdminDashboard extends JFrame {

    private static final Color NAVY = new Color(25, 55, 85);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color ACCENT = new Color(20, 110, 130);

    private final AdminService adminService = new AdminService();
    private final DoctorService doctorService = new DoctorService();
    private final PatientService patientService = new PatientService();
    private final AppointmentService appointmentService =
            new AppointmentService();
    private final AdmissionService admissionService =
            new AdmissionService();
    private final BillingService billingService = new BillingService();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public AdminDashboard() {
        setTitle("Hospital Management System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setSize(1050, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel headings = new JPanel(new GridLayout(2, 1, 0, 5));
        headings.setOpaque(false);

        JLabel title = new JLabel("Hospital Management System");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Administrator Dashboard");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 230, 240));

        headings.add(title);
        headings.add(subtitle);
        header.add(headings, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(event -> logout());
        header.add(logoutButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(28, 32, 20, 32));

        JPanel welcomePanel = new JPanel(new GridLayout(2, 1, 0, 7));
        welcomePanel.setOpaque(false);

        JLabel welcome = new JLabel("Welcome, Administrator");
        welcome.setFont(new Font("Arial", Font.BOLD, 23));
        welcome.setForeground(NAVY);

        JLabel description = new JLabel(
                "Choose a section to view and manage hospital records.");
        description.setFont(new Font("Arial", Font.PLAIN, 14));
        description.setForeground(new Color(85, 95, 105));

        welcomePanel.add(welcome);
        welcomePanel.add(description);
        content.add(welcomePanel, BorderLayout.NORTH);

        JPanel navigation = new JPanel(new GridLayout(3, 3, 16, 16));
        navigation.setOpaque(false);
        navigation.add(createNavigationButton(
                "View Doctors", "Browse registered doctor records",
                this::showDoctors));
        navigation.add(createNavigationButton(
                "Register Doctor", "Add a doctor to the database",
                this::registerDoctor));
        navigation.add(createNavigationButton(
                "View Patients", "Browse registered patient records",
                this::showPatients));
        navigation.add(createNavigationButton(
                "Manage Appointments", "Review or cancel appointments",
                this::showAppointments));
        navigation.add(createNavigationButton(
                "Manage Medicines", "Review medicine inventory",
                this::showMedicines));
        navigation.add(createNavigationButton(
                "Manage Rooms", "Review rooms and availability",
                this::showRooms));
        navigation.add(createNavigationButton(
                "Manage Admissions", "Review or discharge admissions",
                this::showAdmissions));
        navigation.add(createNavigationButton(
                  "Admit Patient", "Admit a patient and allocate a room",
                this::openAdmissionForm));
        navigation.add(createNavigationButton(
                "Manage Bills", "Review bills and record payments",
                this::showBills));
        navigation.add(createNavigationButton(
                "Generate Bill", "Create a new patient bill",
                this::openBilling));

        content.add(navigation, BorderLayout.CENTER);
        return content;
    }

    private JButton createNavigationButton(
            String title, String description, Runnable action) {
        JButton button = new JButton("<html><div style='text-align:center'>"
                + "<b>" + title + "</b><br><br><small>"
                + description + "</small></div></html>");
        button.setFont(new Font("Arial", Font.PLAIN, 15));
        button.setForeground(NAVY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 232)),
                new EmptyBorder(14, 10, 14, 10)));
        button.addActionListener(event -> action.run());
        return button;
    }

    private JLabel createFooter() {
        JLabel footer = new JLabel(
                "Hospital Management System  |  Admin Panel",
                SwingConstants.CENTER);
        footer.setBorder(new EmptyBorder(8, 10, 14, 10));
        footer.setForeground(Color.GRAY);
        return footer;
    }

    private void showDoctors() {
        try {
            List<Object[]> rows = new ArrayList<>();
            for (Doctor doctor : doctorService.getAllDoctors()) {
                rows.add(new Object[]{
                        doctor.getId(),
                        fullName(doctor.getFirstname(), doctor.getLastname()),
                        doctor.getAge(),
                        doctor.getPhonenumber(),
                        doctor.getEmail(),
                        doctor.getSpecialization(),
                        doctor.getQualification(),
                        doctor.getConsultationFee()
                });
            }
            showTable("Registered Doctors",
                    new String[]{"ID", "Name", "Age", "Phone", "Email",
                            "Specialization", "Qualification", "Fee"},
                    rows, null, null);
        } catch (SQLException ex) {
            showDatabaseError("Could not load doctors.", ex);
        }
    }

    private void showPatients() {
        try {
            List<Object[]> rows = new ArrayList<>();
            for (Patient patient : patientService.getAllPatients()) {
                rows.add(new Object[]{
                        patient.getId(),
                        patient.getOpNumber(),
                        fullName(patient.getFirstname(), patient.getLastname()),
                        patient.getAge(),
                        patient.getPhonenumber(),
                        patient.getEmail(),
                        patient.getDisease(),
                        patient.getBloodGroup()
                });
            }
            showTable("Registered Patients",
                    new String[]{"ID", "OP Number", "Name", "Age", "Phone",
                            "Email", "Disease", "Blood Group"},
                    rows, null, null);
        } catch (SQLException ex) {
            showDatabaseError("Could not load patients.", ex);
        }
    }

    private void registerDoctor() {
        JTextField firstNameField = new JTextField(18);
        JTextField lastNameField = new JTextField(18);
        JTextField ageField = new JTextField(18);
        JTextField phoneField = new JTextField(18);
        JTextField emailField = new JTextField(18);
        JTextField specializationField = new JTextField(18);
        JTextField qualificationField = new JTextField(18);
        JTextField feeField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.add(new JLabel("First name:"));
        form.add(firstNameField);
        form.add(new JLabel("Last name:"));
        form.add(lastNameField);
        form.add(new JLabel("Age (1-120):"));
        form.add(ageField);
        form.add(new JLabel("Phone (10 digits):"));
        form.add(phoneField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Specialization:"));
        form.add(specializationField);
        form.add(new JLabel("Qualification:"));
        form.add(qualificationField);
        form.add(new JLabel("Consultation fee:"));
        form.add(feeField);
        form.add(new JLabel("Login password (minimum 8 characters):"));
        form.add(passwordField);

        int choice = JOptionPane.showConfirmDialog(
                this, form, "Register Doctor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String ageText = ageField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String specialization = specializationField.getText().trim();
        String qualification = qualificationField.getText().trim();
        String feeText = feeField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!isValidName(firstName) || !isValidName(lastName)) {
            showValidationError(
                    "Enter valid first and last names using letters, "
                            + "spaces, apostrophes, periods, or hyphens.");
            return;
        }
        if (!InputValidator.isValidAge(ageText)) {
            showValidationError("Enter an age between 1 and 120.");
            return;
        }
        if (!InputValidator.isValidPhone(phone)) {
            showValidationError("Enter a valid 10-digit phone number.");
            return;
        }
        if (!InputValidator.isValidEmail(email)) {
            showValidationError("Enter a valid email address.");
            return;
        }
        if (!InputValidator.isNotEmpty(specialization)
                || !InputValidator.isNotEmpty(qualification)) {
            showValidationError(
                    "Specialization and qualification are required.");
            return;
        }
        if (!InputValidator.isValidAmount(feeText)) {
            showValidationError(
                    "Enter a valid, non-negative consultation fee.");
            return;
        }
        if (password.length() < 8) {
            showValidationError(
                    "Password must contain at least 8 characters.");
            return;
        }

        Doctor doctor = new Doctor();
        doctor.setFirstname(firstName);
        doctor.setLastname(lastName);
        doctor.setAge(Integer.parseInt(ageText));
        doctor.setPhonenumber(phone);
        doctor.setEmail(email);
        doctor.setSpecialization(specialization);
        doctor.setQualification(qualification);
        doctor.setConsultationFee(Double.parseDouble(feeText));

        try {
            boolean registered = doctorService.registerDoctor(doctor, password);

            if (registered) {
                JOptionPane.showMessageDialog(
                        this,
                        "Doctor registered successfully. The doctor can "
                                + "now log in using the registered email "
                                + "and password.",
                        "Registration Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "The doctor could not be registered.",
                        "Registration Failed",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            showDatabaseError("Could not register the doctor.", ex);
        } catch (GeneralSecurityException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not secure the doctor's password: "
                            + ex.getMessage(),
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
        }
    }

    private void showAppointments() {
        try {
            List<Appointment> appointments =
                    appointmentService.getAllAppointments();
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Appointment appointment : appointments) {
                ids.add(appointment.getAppointmentId());
                rows.add(new Object[]{
                        appointment.getAppointmentId(),
                        patientName(appointment.getPatient()),
                        doctorName(appointment.getDoctor()),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        appointment.getStatus()
                });
            }
            showTable("Appointments",
                    new String[]{"ID", "Patient", "Doctor", "Date", "Time",
                            "Status"},
                    rows, "Cancel Selected", (dialog, selectedRow) -> {
                        int appointmentId = ids.get(selectedRow);
                        int confirm = JOptionPane.showConfirmDialog(
                                dialog,
                                "Cancel appointment " + appointmentId + "?",
                                "Confirm Cancellation",
                                JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            try {
                                appointmentService.cancelAppointment(
                                        appointmentId);
                                dialog.dispose();
                                showAppointments();
                            } catch (SQLException ex) {
                                showDatabaseError(
                                        "Could not cancel the appointment.", ex);
                            } catch (IllegalArgumentException ex) {
                                showOperationError(ex.getMessage());
                            }
                        }
                    });
        } catch (SQLException ex) {
            showDatabaseError("Could not load appointments.", ex);
        }
    }

    private void showMedicines() {
        try {
            List<Object[]> rows = new ArrayList<>();
            for (Medicine medicine : medicineDAO.getAllMedicines()) {
                rows.add(new Object[]{
                        medicine.getMedicineId(),
                        medicine.getMedicineName(),
                        medicine.getManufacturer(),
                        medicine.getBatchNumber(),
                        medicine.getPrice(),
                        medicine.getStockQuantity(),
                        medicine.getExpiryDate()
                });
            }
            showTable("Medicine Inventory",
                    new String[]{"ID", "Medicine", "Manufacturer", "Batch",
                            "Price", "Stock", "Expiry Date"},
                    rows, null, null,
                    "Inventory records are displayed from the database. "
                            + "Add, edit, and delete screens are not implemented.");
        } catch (SQLException ex) {
            showDatabaseError("Could not load medicine inventory.", ex);
        }
    }

    private void showRooms() {
        try {
            List<Room> rooms = roomDAO.getAllRooms();
            List<Object[]> rows = new ArrayList<>();
            List<Room> roomRecords = new ArrayList<>();
            for (Room room : rooms) {
                roomRecords.add(room);
                rows.add(new Object[]{
                        room.getRoomId(),
                        room.getRoomNumber(),
                        room.getWardName(),
                        room.getBedNo() > 0
                                ? room.getBedNo() : room.getRoomNumber(),
                        room.getRoomType(),
                        room.getDailyCharge(),
                        room.isAvailable() ? "Available" : "Occupied"
                });
            }
            showTable("Rooms",
                    new String[]{"ID", "Room Number", "Ward", "Bed",
                            "Type", "Daily Charge", "Availability"},
                    rows, "Change Availability", (dialog, selectedRow) -> {
                        Room room = roomRecords.get(selectedRow);
                        boolean updated;
                        try {
                            updated = room.isAvailable()
                                    ? roomDAO.bookRoom(room.getRoomId())
                                    : roomDAO.releaseRoom(room.getRoomId());
                            if (updated) {
                                dialog.dispose();
                                showRooms();
                            } else {
                                showOperationError(
                                        "Room availability was not changed. "
                                                + "Refresh and try again.");
                            }
                        } catch (SQLException ex) {
                            showDatabaseError(
                                    "Could not change room availability.", ex);
                        }
                    });
        } catch (SQLException ex) {
            showDatabaseError("Could not load rooms.", ex);
        }
    }

    
private void showAdmissions() {
    try {
        List<Admission> admissions =
                admissionService.getAllAdmissions();

        List<Object[]> rows = new ArrayList<>();

        DateTimeFormatter dateFormat =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Admission admission : admissions) {
            String admissionDate =
                    admission.getAdmissionDate() == null
                            ? "N/A"
                            : admission.getAdmissionDate()
                                    .format(dateFormat);

            String dischargeDate =
                    admission.getDischargeDate() == null
                            ? "Not discharged"
                            : admission.getDischargeDate()
                                    .format(dateFormat);

            rows.add(new Object[]{
                    admission.getAdmissionId(),
                    patientName(admission.getPatient()),
                    doctorName(admission.getDoctor()),
                    admission.getWardName(),
                    admission.getRoomNumber(),
                    admissionDate,
                    dischargeDate,
                    admission.getStatus()
            });
        }

        showTable(
                "Admissions",
                new String[]{
                        "Admission ID",
                        "Patient",
                        "Doctor",
                        "Ward",
                        "Bed / Room",
                        "Admitted",
                        "Discharge Date",
                        "Status"
                },
                rows,
                "Discharge Selected",
                (dialog, selectedRow) -> {
                    Admission admission = admissions.get(selectedRow);

                    if (!"Active".equalsIgnoreCase(
                            admission.getStatus())) {
                        showOperationError(
                                "This admission has already been discharged.");
                        return;
                    }

                    int confirm = JOptionPane.showConfirmDialog(
                            dialog,
                            "Discharge admission "
                                    + admission.getAdmissionId() + "?",
                            "Confirm Discharge",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            admissionService.dischargePatient(
                                    admission.getAdmissionId());

                            dialog.dispose();
                            showAdmissions();

                        } catch (SQLException ex) {
                            showDatabaseError(
                                    "Could not discharge the admission.", ex);
                        } catch (IllegalArgumentException ex) {
                            showOperationError(ex.getMessage());
                        }
                    }
                });

    } catch (SQLException ex) {
        showDatabaseError("Could not load admissions.", ex);
    }
}


    private void showBills() {
        try {
            List<Bill> bills = billingService.getAllBills();
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Bill bill : bills) {
                ids.add(bill.getBillId());
                rows.add(new Object[]{
                        bill.getBillId(),
                        patientName(bill.getPatient()),
                        bill.getBillDate(),
                        bill.getTotalAmount(),
                        bill.getPaymentStatus(),
                        bill.getPaymentMethod()
                });
            }
            showTable("Bills",
                    new String[]{"Bill ID", "Patient", "Bill Date", "Total",
                            "Payment Status", "Payment Method"},
                    rows, "Record Payment", (dialog, selectedRow) -> {
                        int billId = ids.get(selectedRow);
                        String[] paymentMethods = {"Cash", "Card", "UPI"};
                        String paymentMethod = (String) JOptionPane.showInputDialog(
                                dialog, "Select the payment method:",
                                "Record Payment", JOptionPane.PLAIN_MESSAGE,
                                null, paymentMethods, paymentMethods[0]);
                        if (paymentMethod == null) {
                            return;
                        }
                        try {
                            billingService.payBill(billId, paymentMethod);
                            dialog.dispose();
                            showBills();
                        } catch (SQLException ex) {
                            showDatabaseError("Could not record payment.", ex);
                        } catch (IllegalArgumentException ex) {
                            showOperationError(ex.getMessage());
                        }
                    });
        } catch (SQLException ex) {
            showDatabaseError("Could not load bills.", ex);
        }
    }

    private void showTable(
            String title,
            String[] columns,
            List<Object[]> rows,
            String actionLabel,
            BiConsumer<JDialog, Integer> action) {
        showTable(title, columns, rows, actionLabel, action, null);
    }

    private void showTable(
            String title,
            String[] columns,
            List<Object[]> rows,
            String actionLabel,
            BiConsumer<JDialog, Integer> action,
            String notice) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        if (notice != null) {
            JLabel noticeLabel = new JLabel(notice);
            noticeLabel.setBorder(new EmptyBorder(12, 14, 0, 14));
            noticeLabel.setForeground(new Color(85, 95, 105));
            dialog.add(noticeLabel, BorderLayout.NORTH);
        }

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (actionLabel != null) {
            JButton actionButton = new JButton(actionLabel);
            actionButton.setBackground(ACCENT);
            actionButton.setForeground(Color.WHITE);
            actionButton.setFocusPainted(false);
            actionButton.addActionListener(event -> {
                int selectedViewRow = table.getSelectedRow();
                if (selectedViewRow < 0) {
                    JOptionPane.showMessageDialog(
                            dialog, "Select a record first.",
                            "No Record Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                action.accept(dialog,
                        table.convertRowIndexToModel(selectedViewRow));
            });
            buttons.add(actionButton);
        }

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(event -> dialog.dispose());
        buttons.add(closeButton);
        buttons.setBorder(new EmptyBorder(0, 8, 8, 8));
        dialog.add(buttons, BorderLayout.SOUTH);

        if (rows.isEmpty()) {
            JLabel emptyMessage = new JLabel(
                    "No records found.", SwingConstants.CENTER);
            emptyMessage.setForeground(Color.GRAY);
            dialog.add(emptyMessage, BorderLayout.WEST);
        }

        dialog.setSize(950, 500);
        dialog.setMinimumSize(new Dimension(650, 350));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean isValidName(String name) {
        return InputValidator.isNotEmpty(name)
                && name.matches("[\\p{L}][\\p{L} .'-]*");
    }

    private String fullName(String firstName, String lastName) {
        return ((firstName == null) ? "" : firstName)
                + " " + ((lastName == null) ? "" : lastName);
    }

    private String patientName(Patient patient) {
        return patient == null
                ? "N/A"
                : fullName(patient.getFirstname(), patient.getLastname()).trim();
    }

    private String doctorName(Doctor doctor) {
        return doctor == null
                ? "N/A"
                : fullName(doctor.getFirstname(), doctor.getLastname()).trim();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Invalid Information",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(String message, SQLException exception) {
        JOptionPane.showMessageDialog(
                this, message + "\n" + exception.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        exception.printStackTrace();
    }

    private void showOperationError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Operation Failed",
                JOptionPane.WARNING_MESSAGE);
    }

    private void openBilling() {
        new BillingForm().setVisible(true);
    }

    private void openAdmissionForm() {
    new AdmissionForm(this).setVisible(true);
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
}
