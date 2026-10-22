
package gui;

import dao.DoctorDAO;
import exception.AppointmentConflictException;
import exception.RoomUnavailableException;
import model.Appointment;
import model.Doctor;
import model.Patient;
import service.AppointmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentForm extends JFrame {

    private final Patient loggedInPatient;

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentService appointmentService =
            new AppointmentService();

    private JComboBox<Doctor> doctorComboBox;
    private JTextField dateField;
    private JTextField timeField;

    private JTable appointmentTable;
    private DefaultTableModel tableModel;

    private JButton bookButton;
    private JButton rescheduleButton;
    private JButton cancelButton;
    private JButton refreshButton;
    private JButton backButton;

    public AppointmentForm(Patient loggedInPatient) {

        if (loggedInPatient == null || loggedInPatient.getId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid logged-in patient is required.");
        }

        this.loggedInPatient = loggedInPatient;

        setTitle("Hospital Management System - Appointments");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 55, 85));
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel title = new JLabel("Appointment Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Patient: " + loggedInPatient.getFirstname()
                        + " " + loggedInPatient.getLastname());

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.WHITE);

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 5));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(subtitle);

        header.add(heading, BorderLayout.WEST);

        backButton = new JButton("Close");
        header.add(backButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(new Color(245, 247, 250));
        content.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Booking and rescheduling form
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 12, 12));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                "Book or Reschedule Appointment"));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Select Doctor:"));

        doctorComboBox = new JComboBox<>();
        formPanel.add(doctorComboBox);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));

        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Time (HH:mm):"));

        timeField = new JTextField();
        formPanel.add(timeField);

        // Appointment table
        String[] columns = {
                "Appointment ID",
                "Doctor",
                "Specialization",
                "Date",
                "Time",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        appointmentTable = new JTable(tableModel);
        appointmentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        appointmentTable.setRowHeight(25);
        appointmentTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "My Appointments"));

        // Buttons
        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setOpaque(false);

        bookButton = new JButton("Book Appointment");
        rescheduleButton = new JButton("Reschedule Selected");
        cancelButton = new JButton("Cancel Selected");
        refreshButton = new JButton("Refresh");

        bookButton.setBackground(new Color(25, 55, 85));
        bookButton.setForeground(Color.WHITE);

        buttonPanel.add(refreshButton);
        buttonPanel.add(bookButton);
        buttonPanel.add(rescheduleButton);
        buttonPanel.add(cancelButton);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);
        topPanel.add(formPanel, BorderLayout.CENTER);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        // Actions
        backButton.addActionListener(e -> dispose());

        bookButton.addActionListener(e -> bookAppointment());

        rescheduleButton.addActionListener(
                e -> rescheduleSelectedAppointment());

        cancelButton.addActionListener(
                e -> cancelSelectedAppointment());

        refreshButton.addActionListener(
                e -> loadAppointments());

        // Initialize data
        loadDoctors();
        loadAppointments();
    }

    // Load available doctors from MySQL
    private void loadDoctors() {
        try {
            doctorComboBox.removeAllItems();

            List<Doctor> doctors = doctorDAO.getAllDoctors();

            for (Doctor doctor : doctors) {
                doctorComboBox.addItem(doctor);
            }

            doctorComboBox.setRenderer(
                    new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus) {

                    super.getListCellRendererComponent(
                            list, value, index,
                            isSelected, cellHasFocus);

                    if (value instanceof Doctor) {
                        Doctor doctor = (Doctor) value;

                        setText("Dr. " + doctor.getFirstname()
                                + " " + doctor.getLastname()
                                + " - " + doctor.getSpecialization());
                    }

                    return this;
                }
            });

            if (doctors.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No doctors are currently available.");
            }

        } catch (SQLException ex) {
            showError("Could not load doctors: " + ex.getMessage());
        }
    }

    // Book a new appointment
    private void bookAppointment() {
        try {
            Doctor doctor =
                    (Doctor) doctorComboBox.getSelectedItem();

            if (doctor == null) {
                showError("Please select a doctor.");
                return;
            }

            LocalDate date = parseDate();
            LocalTime time = parseTime();

            if (date.isBefore(LocalDate.now())) {
                showError("Appointment date cannot be in the past.");
                return;
            }

            if (date.equals(LocalDate.now())
                    && !time.isAfter(LocalTime.now())) {
                showError("Please select a future appointment time.");
                return;
            }

            Appointment appointment = new Appointment(
                    0,
                    loggedInPatient,
                    doctor,
                    date.toString(),
                    time.toString(),
                    "Scheduled"
            );

            boolean success =
                    appointmentService.bookAppointment(appointment);

            if (success) {
                JOptionPane.showMessageDialog(
                        this, "Appointment booked successfully!");

                clearDateAndTime();
                loadAppointments();
            } else {
                showError("Appointment could not be booked.");
            }

        } catch (DateTimeParseException ex) {
            showError("Enter a valid date and time.");
        } catch (AppointmentConflictException
                | RoomUnavailableException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Could not book appointment: "
                    + ex.getMessage());
        }
    }

    // Reschedule the selected appointment
    private void rescheduleSelectedAppointment() {
        int row = getSelectedModelRow();

        if (row == -1) {
            showError("Please select an appointment first.");
            return;
        }

        int appointmentId =
                (int) tableModel.getValueAt(row, 0);

        String status =
                String.valueOf(tableModel.getValueAt(row, 5));

        if (!"Scheduled".equalsIgnoreCase(status)) {
            showError("Only scheduled appointments can be rescheduled.");
            return;
        }

        try {
            LocalDate date = parseDate();
            LocalTime time = parseTime();

            if (date.isBefore(LocalDate.now())
                    || (date.equals(LocalDate.now())
                    && !time.isAfter(LocalTime.now()))) {
                showError("Select a future date and time.");
                return;
            }

            boolean confirmed = JOptionPane.showConfirmDialog(
                    this,
                    "Reschedule appointment #" + appointmentId + "?",
                    "Confirm Rescheduling",
                    JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION;

            if (!confirmed) {
                return;
            }

            boolean success =
                    appointmentService.rescheduleAppointment(
                            appointmentId,
                            loggedInPatient.getId(),
                            date.toString(),
                            time.toString()
                    );

            if (success) {
                JOptionPane.showMessageDialog(
                        this, "Appointment rescheduled successfully!");

                clearDateAndTime();
                loadAppointments();
            }

        } catch (DateTimeParseException ex) {
            showError("Enter a valid date and time.");
        } catch (AppointmentConflictException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    // Cancel the selected appointment
    private void cancelSelectedAppointment() {
        int row = getSelectedModelRow();

        if (row == -1) {
            showError("Please select an appointment first.");
            return;
        }

        int appointmentId =
                (int) tableModel.getValueAt(row, 0);

        String status =
                String.valueOf(tableModel.getValueAt(row, 5));

        if (!"Scheduled".equalsIgnoreCase(status)) {
            showError("Only scheduled appointments can be cancelled.");
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel appointment #"
                        + appointmentId + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success =
                    appointmentService.cancelAppointmentForPatient(
                            appointmentId,
                            loggedInPatient.getId()
                    );

            if (success) {
                JOptionPane.showMessageDialog(
                        this, "Appointment cancelled successfully!");

                loadAppointments();
            }

        } catch (SQLException | IllegalArgumentException ex) {
            showError("Could not cancel appointment: "
                    + ex.getMessage());
        }
    }

    // Load only this patient's appointments
    private void loadAppointments() {
        try {
            tableModel.setRowCount(0);

            List<Appointment> appointments =
                    appointmentService.getAppointmentsForPatient(
                            loggedInPatient.getId());

            for (Appointment appointment : appointments) {
                Doctor doctor = appointment.getDoctor();

                String doctorName = doctor == null
                        ? "Unknown"
                        : "Dr. " + doctor.getFirstname()
                                + " " + doctor.getLastname();

                String specialization = doctor == null
                        ? "Unknown"
                        : doctor.getSpecialization();

                tableModel.addRow(new Object[]{
                    appointment.getAppointmentId(),
                    doctorName,
                    specialization,
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(),
                    appointment.getStatus()
                });
            }

        } catch (SQLException ex) {
            showError("Could not load appointments: "
                    + ex.getMessage());
        }
    }

    // Convert the selected view row into a model row
    private int getSelectedModelRow() {
        int selectedRow = appointmentTable.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        return appointmentTable.convertRowIndexToModel(selectedRow);
    }

    private LocalDate parseDate() {
        String value = dateField.getText().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter an appointment date.");
        }

        return LocalDate.parse(value);
    }

    private LocalTime parseTime() {
        String value = timeField.getText().trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please enter an appointment time.");
        }

        return LocalTime.parse(value);
    }

    private void clearDateAndTime() {
        dateField.setText("");
        timeField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Appointment Management",
                JOptionPane.WARNING_MESSAGE);
    }
}
