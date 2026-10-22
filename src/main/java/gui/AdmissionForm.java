package gui;

import dao.RoomDAO;
import model.Admission;
import model.Doctor;
import model.Patient;
import model.Room;
import service.AdmissionService;
import service.DoctorService;
import service.PatientService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdmissionForm extends JDialog {

    private final AdmissionService admissionService =
            new AdmissionService();
    private final PatientService patientService =
            new PatientService();
    private final DoctorService doctorService =
            new DoctorService();
    private final RoomDAO roomDAO = new RoomDAO();

    private final JTextField admissionIdField = new JTextField(18);
    private final JComboBox<Patient> patientCombo = new JComboBox<>();
    private final JComboBox<Doctor> doctorCombo = new JComboBox<>();
    private final JComboBox<String> wardCombo = new JComboBox<>();
    private final JComboBox<Room> bedCombo = new JComboBox<>();
    private final JSpinner admissionDate = new JSpinner(
            new SpinnerDateModel(new Date(), null, null, java.util.Calendar.MINUTE));
    private final DefaultTableModel activeAdmissionsModel =
            new DefaultTableModel(new String[]{
                    "Admission ID", "Patient", "Doctor", "Ward",
                    "Bed", "Admitted"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final DefaultTableModel bedsModel =
            new DefaultTableModel(new String[]{
                    "Ward", "Bed", "Room", "Type", "Availability"
            }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    private final List<Admission> activeAdmissions = new ArrayList<>();

    public AdmissionForm(JFrame parent) {
        super(parent, "Inpatient Admission and Bed Allocation", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 650);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(parent);

        JSpinner.DateEditor dateEditor =
                new JSpinner.DateEditor(admissionDate, "yyyy-MM-dd HH:mm");
        admissionDate.setEditor(dateEditor);

        patientCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value instanceof Patient) {
                    Patient patient = (Patient) value;
                    setText(patient.getId() + " - "
                            + patient.getFirstname() + " "
                            + patient.getLastname());
                }
                return this;
            }
        });
        doctorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value instanceof Doctor) {
                    Doctor doctor = (Doctor) value;
                    setText(doctor.getId() + " - "
                            + doctor.getFirstname() + " "
                            + doctor.getLastname() + " ("
                            + doctor.getSpecialization() + ")");
                }
                return this;
            }
        });
        bedCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(
                        list, value, index, selected, focused);
                if (value instanceof Room) {
                    Room room = (Room) value;
                    setText(bedLabel(room) + " - " + room.getRoomNumber()
                            + " (" + room.getRoomType() + ")");
                }
                return this;
            }
        });

        JPanel form = createAdmissionPanel();
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Active admissions", createAdmissionsPanel());
        tabs.addTab("Ward and bed availability", createBedsPanel());

        add(form, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        loadFormData();
    }

    private JPanel createAdmissionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 20, 10, 20));

        JPanel fields = new JPanel(new GridLayout(0, 2, 10, 8));
        fields.add(new JLabel("Admission ID:"));
        fields.add(admissionIdField);
        fields.add(new JLabel("Registered patient:"));
        fields.add(patientCombo);
        fields.add(new JLabel("Attending doctor:"));
        fields.add(doctorCombo);
        fields.add(new JLabel("Ward:"));
        fields.add(wardCombo);
        fields.add(new JLabel("Available bed:"));
        fields.add(bedCombo);
        fields.add(new JLabel("Admission date and time:"));
        fields.add(admissionDate);

        JButton admitButton = new JButton("Admit Patient");
        admitButton.addActionListener(event -> admitPatient());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshData());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(admitButton);

        wardCombo.addActionListener(event -> refreshWardBeds());
        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAdmissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(10, 15, 12, 15));
        JTable table = new JTable(activeAdmissionsModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        JButton dischargeButton = new JButton("Discharge Selected");
        dischargeButton.addActionListener(event ->
                dischargeSelected(table));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(dischargeButton);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBedsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 12, 15));
        JTable table = new JTable(bedsModel);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadFormData() {
        try {
            for (Patient patient : patientService.getAllPatients()) {
                patientCombo.addItem(patient);
            }
            for (Doctor doctor : doctorService.getAllDoctors()) {
                doctorCombo.addItem(doctor);
            }
            for (String ward : roomDAO.getAllWardNames()) {
                wardCombo.addItem(ward);
            }
            if (patientCombo.getItemCount() == 0) {
                showError("Register a patient before admission.");
            }
            if (doctorCombo.getItemCount() == 0) {
                showError("Register a doctor before admission.");
            }
            if (wardCombo.getItemCount() == 0) {
                showError("No wards found. Add rooms and apply the "
                        + "inpatient database migration.");
            }
            refreshWardBeds();
            loadActiveAdmissions();
        } catch (SQLException ex) {
            showDatabaseError("Could not load admission details.", ex);
        }
    }

    private void refreshData() {
        refreshWardBeds();
        loadActiveAdmissions();
    }

    private void refreshWardBeds() {
        bedCombo.removeAllItems();
        bedsModel.setRowCount(0);
        String ward = (String) wardCombo.getSelectedItem();
        if (ward == null) {
            return;
        }

        try {
            for (Room room : roomDAO.getAllRoomsByWard(ward)) {
                String availability =
                        room.isAvailable() ? "Available" : "Occupied";
                bedsModel.addRow(new Object[]{
                        room.getWardName(), bedLabel(room),
                        room.getRoomNumber(), room.getRoomType(), availability
                });
                if (room.isAvailable()) {
                    bedCombo.addItem(room);
                }
            }
        } catch (SQLException ex) {
            showDatabaseError("Could not refresh ward bed availability.", ex);
        }
    }

    private void loadActiveAdmissions() {
        activeAdmissionsModel.setRowCount(0);
        activeAdmissions.clear();
        try {
            activeAdmissions.addAll(admissionService.getActiveAdmissions());
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Admission admission : activeAdmissions) {
                activeAdmissionsModel.addRow(new Object[]{
                        admission.getAdmissionId(),
                        patientName(admission.getPatient()),
                        doctorName(admission.getDoctor()),
                        admission.getWardName(),
                        admission.getRoomNumber(),
                        admission.getAdmissionDate().format(formatter)
                });
            }
        } catch (SQLException ex) {
            showDatabaseError("Could not load active admissions.", ex);
        }
    }

    private void admitPatient() {
        String admissionId = admissionIdField.getText().trim();
        Patient patient = (Patient) patientCombo.getSelectedItem();
        Doctor doctor = (Doctor) doctorCombo.getSelectedItem();
        String ward = (String) wardCombo.getSelectedItem();
        Room bed = (Room) bedCombo.getSelectedItem();

        if (admissionId.isEmpty() || admissionId.length() > 30) {
            showError("Enter an Admission ID of 1 to 30 characters.");
            return;
        }
        if (patient == null || doctor == null || ward == null || bed == null) {
            showError("Select a registered patient, doctor, ward, and "
                    + "available bed.");
            return;
        }

        Date selectedDate = (Date) admissionDate.getValue();
        LocalDateTime admittedAt = LocalDateTime.ofInstant(
                selectedDate.toInstant(), ZoneId.systemDefault());
        Admission admission = new Admission(
                admissionId, patient, doctor, bed.getRoomNumber());
        admission.setAdmissionDate(admittedAt);
        admission.setWardName(ward);

        try {
            admissionService.admitPatient(admission, ward);
            JOptionPane.showMessageDialog(
                    this, "Patient admitted successfully to "
                            + ward + ", " + bedLabel(bed) + ".",
                    "Admission Successful", JOptionPane.INFORMATION_MESSAGE);
            admissionIdField.setText("");
            refreshData();
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Admission Failed",
                    JOptionPane.ERROR_MESSAGE);
            refreshData();
        }
    }

    private void dischargeSelected(JTable table) {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            showError("Select an active admission to discharge.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        Admission admission = activeAdmissions.get(row);
        int confirmation = JOptionPane.showConfirmDialog(
                this, "Discharge admission "
                        + admission.getAdmissionId() + "?",
                "Confirm Discharge", JOptionPane.YES_NO_OPTION);
        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            admissionService.dischargePatient(admission.getAdmissionId());
            JOptionPane.showMessageDialog(
                    this, "Patient discharged and bed released successfully.",
                    "Discharge Successful", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(), "Discharge Failed",
                    JOptionPane.ERROR_MESSAGE);
            refreshData();
        }
    }

    private static String bedLabel(Room room) {
        return room.getBedNo() > 0
                ? "Bed " + room.getBedNo() : room.getRoomNumber();
    }

    private static String patientName(Patient patient) {
        return patient == null ? "Unknown patient"
                : patient.getFirstname() + " " + patient.getLastname();
    }

    private static String doctorName(Doctor doctor) {
        return doctor == null ? "Unknown doctor"
                : doctor.getFirstname() + " " + doctor.getLastname();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Admission", JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(String message, SQLException ex) {
        JOptionPane.showMessageDialog(
                this, message + "\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
