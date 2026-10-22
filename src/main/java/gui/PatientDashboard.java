
package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import model.Patient;

import model.Admission;
import service.AdmissionService;
import java.sql.SQLException;
import java.util.List;
import javax.swing.table.DefaultTableModel;

import model.Bill;
import service.BillingService;

import model.Doctor;
import service.DoctorService;




public class PatientDashboard extends JFrame {

    private final Patient authenticatedPatient;

    public PatientDashboard() {
        this(null);
    }

    public PatientDashboard(Patient patient) {
        authenticatedPatient = patient;
        setTitle("Hospital Management System - Patient Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 55, 85));
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Hospital Management System");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Patient Dashboard");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(Color.WHITE);

        JPanel headingPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headingPanel.setOpaque(false);
        headingPanel.add(title);
        headingPanel.add(subtitle);

        header.add(headingPanel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFocusPainted(false);
        header.add(logoutButton, BorderLayout.EAST);

       

        add(header, BorderLayout.NORTH);

        // Main content
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(new Color(245, 247, 250));
        content.setBorder(new EmptyBorder(25, 25, 25, 25));

        String patientName = patient == null
                ? "Patient"
                : (patient.getFirstname() + " " + patient.getLastname()).trim();
        JLabel welcome = new JLabel("Welcome, " + patientName + "!");
        welcome.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel description = new JLabel(
                "Manage your appointments and view your hospital information."
        );
        description.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel welcomePanel = new JPanel(new GridLayout(2, 1, 0, 8));
        welcomePanel.setOpaque(false);
        welcomePanel.add(welcome);
        welcomePanel.add(description);

        content.add(welcomePanel, BorderLayout.NORTH);

        // Dashboard buttons
        JPanel menuPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        addDashboardButton(
                menuPanel,
                "View My Profile",
                "View personal and patient details"
        );

        addDashboardButton(
                menuPanel,
                "Book Appointment",
                "Request an appointment with a doctor"
        );

        addDashboardButton(
                menuPanel,
                "View My Appointments",
                "Check appointment dates and status"
        );

        addDashboardButton(
                menuPanel,
                "View Prescriptions",
                "View prescriptions provided by doctors"
        );

        addDashboardButton(
                menuPanel,
                "View My Bills",
                "Check hospital charges and payment status"
        );

        addDashboardButton(
                menuPanel,
                "View Doctors",
                "See available doctors and specializations"
        );

        addDashboardButton(
        menuPanel,
        "View My Admissions",
        "View your hospital admission details"
              );

        content.add(menuPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel(
                "Hospital Management System | Patient Portal",
                SwingConstants.CENTER
        );
        footer.setBorder(new EmptyBorder(10, 10, 15, 10));
        footer.setForeground(Color.GRAY);

        add(footer, BorderLayout.SOUTH);

        // Logout action
        logoutButton.addActionListener(e -> logout());

        setVisible(true);
    }

    private void addDashboardButton(
            JPanel panel,
            String buttonText,
            String description
    ) {
        JButton button = new JButton(
                "<html><center><b>" + buttonText
                        + "</b><br><br><small>"
                        + description
                        + "</small></center></html>"
        );

        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(25, 55, 85));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> handleDashboardAction(buttonText));

        panel.add(button);
    }

    
private void handleDashboardAction(String action) {
    switch (action) {
        case "View My Profile":
    showMyProfile();
    break;

     case "View Doctors":
    showDoctors();
    break;

        case "Book Appointment":
        case "View My Appointments":
            if (authenticatedPatient == null
                    || authenticatedPatient.getId() <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Patient login details are missing. Please log in again.",
                        "Login Required",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            new AppointmentForm(authenticatedPatient).setVisible(true);
            break;

       case "View Prescriptions":
    if (authenticatedPatient == null
            || authenticatedPatient.getId() <= 0) {
        JOptionPane.showMessageDialog(
                this,
                "Patient login details are missing. Please log in again.",
                "Login Required",
                JOptionPane.WARNING_MESSAGE);
        return;
    }

    new PatientPrescriptionForm(authenticatedPatient);
    break;
        case "View My Bills":
    if (authenticatedPatient == null
            || authenticatedPatient.getId() <= 0) {
        JOptionPane.showMessageDialog(
                this,
                "Patient login details are missing. Please log in again.",
                "Login Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    showMyBills();
    break;

        

        case "View My Admissions":
    if (authenticatedPatient == null
            || authenticatedPatient.getId() <= 0) {
        JOptionPane.showMessageDialog(
                this,
                "Patient login details are missing. Please log in again.",
                "Login Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    showMyAdmissions();
    break;

        default:
            JOptionPane.showMessageDialog(
                    this,
                    "Unknown action."
            );
    }
}

    
private void showMyAdmissions() {
    try {
        if (authenticatedPatient == null
                || authenticatedPatient.getId() <= 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Patient login details are missing. Please log in again.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        AdmissionService admissionService = new AdmissionService();

        List<Admission> admissions =
                admissionService.getAdmissionsByPatientId(
                        authenticatedPatient.getId()
                );

        String[] columns = {
                "Admission ID",
                "Doctor",
                "Ward",
                "Bed Number",
                "Room Number",
                "Admission Date",
                "Status"
        };

        DefaultTableModel tableModel =
                new DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(
                            int row, int column) {
                        return false;
                    }
                };

        for (Admission admission : admissions) {
            String doctorName = admission.getDoctor() == null
                    ? "N/A"
                    : admission.getDoctor().getFirstname()
                      + " "
                      + admission.getDoctor().getLastname();

            String wardName = admission.getWardName() == null
                    || admission.getWardName().isBlank()
                    ? "N/A"
                    : admission.getWardName();

            String bedNumber = "N/A";

            // Find the bed number using the room number.
            try {
                dao.RoomDAO roomDAO = new dao.RoomDAO();
                model.Room room =
                        roomDAO.getRoomByNumber(
                                admission.getRoomNumber()
                        );

                if (room != null) {
                    bedNumber = String.valueOf(room.getBedNo());
                }
            } catch (Exception ex) {
                bedNumber = "N/A";
            }

            tableModel.addRow(new Object[]{
                    admission.getAdmissionId(),
                    doctorName,
                    wardName,
                    bedNumber,
                    admission.getRoomNumber(),
                    admission.getAdmissionDate(),
                    admission.getStatus()
            });
        }

        if (admissions.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No admissions found for your account.",
                    "My Admissions",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(850, 300));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "My Admissions",
                JOptionPane.PLAIN_MESSAGE
        );

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Could not load your admissions: "
                        + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

     


    
private void showMyBills() {
    try {
        BillingService billingService = new BillingService();

        List<Bill> bills = billingService.getBillsByPatientId(
                authenticatedPatient.getId()
        );

        String[] columns = {
                "Bill ID",
                "Appointment ID",
                "Consultation Fee",
                "Medicine Charges",
                "Room Charges",
                "Other Charges",
                "Total Amount",
                "Bill Date",
                "Payment Status",
                "Payment Method"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Bill bill : bills) {
            model.addRow(new Object[]{
                    bill.getBillId(),
                    bill.getAppointment() == null
                            ? "N/A"
                            : bill.getAppointment().getAppointmentId(),
                    bill.getConsultationFee(),
                    bill.getMedicineCharges(),
                    bill.getRoomCharges(),
                    bill.getOtherCharges(),
                    bill.getTotalAmount(),
                    bill.getBillDate(),
                    bill.getPaymentStatus(),
                    bill.getPaymentMethod() == null
                            ? "N/A"
                            : bill.getPaymentMethod()
            });
        }

        if (bills.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No bills found for your account.",
                    "My Bills",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1000, 300));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "My Bills",
                JOptionPane.PLAIN_MESSAGE
        );

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Could not load bills: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}


    private void showMessage(String feature) {
        JOptionPane.showMessageDialog(
                this,
                feature + " feature will be connected to the database next.",
                "Patient Dashboard",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    private void showMyProfile() {
    if (authenticatedPatient == null
            || authenticatedPatient.getId() <= 0) {
        JOptionPane.showMessageDialog(
                this,
                "Patient login details are missing. Please log in again.",
                "Login Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    Patient p = authenticatedPatient;

    String details =
            "Patient ID: " + p.getId()
            + "\nOP Number: " + p.getOpNumber()
            + "\nName: " + p.getFirstname() + " " + p.getLastname()
            + "\nAge: " + p.getAge()
            + "\nPhone: " + p.getPhonenumber()
            + "\nEmail: " + p.getEmail()
            + "\nDisease: " + p.getDisease()
            + "\nBlood Group: " + p.getBloodGroup()
            + "\nAddress: " + p.getAddress();

    JTextArea textArea = new JTextArea(details, 12, 35);
    textArea.setEditable(false);
    textArea.setFont(new Font("Arial", Font.PLAIN, 14));
    textArea.setCaretPosition(0);

    JOptionPane.showMessageDialog(
            this,
            new JScrollPane(textArea),
            "My Profile",
            JOptionPane.INFORMATION_MESSAGE
    );
}

    
private void showDoctors() {
    try {
        DoctorService doctorService = new DoctorService();
        List<Doctor> doctors = doctorService.getAllDoctors();

        String[] columns = {
                "Doctor ID",
                "Name",
                "Specialization",
                "Qualification",
                "Phone",
                "Email",
                "Consultation Fee"
        };

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Doctor doctor : doctors) {
            tableModel.addRow(new Object[]{
                    doctor.getId(),
                    doctor.getFirstname() + " " + doctor.getLastname(),
                    doctor.getSpecialization(),
                    doctor.getQualification(),
                    doctor.getPhonenumber(),
                    doctor.getEmail(),
                    doctor.getConsultationFee()
            });
        }

        if (doctors.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No doctors have been registered yet.",
                    "Available Doctors",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Available Doctors",
                JOptionPane.PLAIN_MESSAGE
        );

    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Could not load doctors: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}


    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
