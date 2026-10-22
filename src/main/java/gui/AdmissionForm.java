
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

    private final JTextField admissionIdField =
            new JTextField(18);

    private final JComboBox<Patient> patientCombo =
            new JComboBox<>();

    private final JComboBox<Doctor> doctorCombo =
            new JComboBox<>();

    private final JComboBox<String> wardCombo =
            new JComboBox<>();

    private final JComboBox<Room> bedCombo =
            new JComboBox<>();

    private final JSpinner admissionDate =
            new JSpinner(new SpinnerDateModel(
                    new Date(), null, null,
                    java.util.Calendar.MINUTE));

    private final DefaultTableModel activeAdmissionsModel =
            new DefaultTableModel(
                    new String[]{
                            "Admission ID",
                            "Patient",
                            "Doctor",
                            "Ward",
                            "Bed",
                            "Admitted"
                    }, 0) {

                @Override
                public boolean isCellEditable(
                        int row, int column) {
                    return false;
                }
            };

    private final DefaultTableModel bedsModel =
            new DefaultTableModel(
                    new String[]{
                            "Ward",
                            "Bed",
                            "Room",
                            "Type",
                            "Availability"
                    }, 0) {

                @Override
                public boolean isCellEditable(
                        int row, int column) {
                    return false;
                }
            };

    private final List<Admission> activeAdmissions =
            new ArrayList<>();

    private boolean updatingWardSelection = false;

    public AdmissionForm(JFrame parent) {
        super(parent,
                "Inpatient Admission and Bed Allocation", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 650);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(parent);

        JSpinner.DateEditor dateEditor =
                new JSpinner.DateEditor(
                        admissionDate, "yyyy-MM-dd HH:mm");

        admissionDate.setEditor(dateEditor);

        configurePatientRenderer();
        configureDoctorRenderer();
        configureBedRenderer();

        JPanel form = createAdmissionPanel();

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(
                "Active admissions",
                createAdmissionsPanel());

        tabs.addTab(
                "Ward and bed availability",
                createBedsPanel());

        add(form, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        loadFormData();
    }

    // Display patient ID and name in the dropdown.
    private void configurePatientRenderer() {
        patientCombo.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focused) {

                super.getListCellRendererComponent(
                        list, value, index,
                        selected, focused);

                if (value instanceof Patient) {
                    Patient patient = (Patient) value;

                    setText(
                            patient.getId() + " - "
                            + patient.getFirstname() + " "
                            + patient.getLastname());
                }

                return this;
            }
        });
    }

    // Display doctor ID, name and specialization.
    private void configureDoctorRenderer() {
        doctorCombo.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focused) {

                super.getListCellRendererComponent(
                        list, value, index,
                        selected, focused);

                if (value instanceof Doctor) {
                    Doctor doctor = (Doctor) value;

                    setText(
                            doctor.getId() + " - "
                            + doctor.getFirstname() + " "
                            + doctor.getLastname()
                            + " (" + doctor.getSpecialization()
                            + ")");
                }

                return this;
            }
        });
    }

    // Display the bed number and room details.
    private void configureBedRenderer() {
        bedCombo.setRenderer(
                new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean selected,
                    boolean focused) {

                super.getListCellRendererComponent(
                        list, value, index,
                        selected, focused);

                if (value instanceof Room) {
                    Room room = (Room) value;

                    setText(
                            bedLabel(room) + " - "
                            + room.getRoomNumber()
                            + " (" + room.getRoomType() + ")");
                }

                return this;
            }
        });
    }

    // Create the patient admission form.
    private JPanel createAdmissionPanel() {
        JPanel panel = new JPanel(
                new BorderLayout(10, 10));

        panel.setBorder(
                new EmptyBorder(15, 20, 10, 20));

        JPanel fields = new JPanel(
                new GridLayout(0, 2, 10, 8));

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

        JButton admitButton =
                new JButton("Admit Patient");

        admitButton.addActionListener(
                event -> admitPatient());

        JButton refreshButton =
                new JButton("Refresh");

        refreshButton.addActionListener(
                event -> refreshData());

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttons.add(refreshButton);
        buttons.add(admitButton);

        wardCombo.addActionListener(event -> {
            if (!updatingWardSelection) {
                refreshWardBeds();
            }
        });

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    // Create the active admissions table.
    private JPanel createAdmissionsPanel() {
        JPanel panel = new JPanel(
                new BorderLayout(8, 8));

        panel.setBorder(
                new EmptyBorder(10, 15, 12, 15));

        JTable table = new JTable(
                activeAdmissionsModel);

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        table.setAutoCreateRowSorter(true);

        JButton dischargeButton =
                new JButton("Discharge Selected");

        dischargeButton.addActionListener(
                event -> dischargeSelected(table));

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttons.add(dischargeButton);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    // Create the ward and bed availability table.
    private JPanel createBedsPanel() {
        JPanel panel = new JPanel(
                new BorderLayout());

        panel.setBorder(
                new EmptyBorder(10, 15, 12, 15));

        JTable table = new JTable(bedsModel);

        table.setAutoCreateRowSorter(true);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER);

        return panel;
    }

    // Load patients, doctors and ward names from the database.
    private void loadFormData() {
        patientCombo.removeAllItems();
        doctorCombo.removeAllItems();
        wardCombo.removeAllItems();

        updatingWardSelection = true;

        try {
            List<Patient> patients =
                    patientService.getAllPatients();

            for (Patient patient : patients) {
                patientCombo.addItem(patient);
            }

            List<Doctor> doctors =
                    doctorService.getAllDoctors();

            for (Doctor doctor : doctors) {
                doctorCombo.addItem(doctor);
            }

            List<String> wards =
                    roomDAO.getAllWardNames();

            for (String ward : wards) {
                wardCombo.addItem(ward);
            }

        } catch (SQLException ex) {
            showDatabaseError(
                    "Could not load admission details.", ex);

        } finally {
            updatingWardSelection = false;
        }

        if (patientCombo.getItemCount() == 0) {
            showError(
                    "Register a patient before admission.");
        }

        if (doctorCombo.getItemCount() == 0) {
            showError(
                    "Register a doctor before admission.");
        }

        if (wardCombo.getItemCount() == 0) {
            showError(
                    "No wards found in the database.");
        }

        refreshWardBeds();
        loadActiveAdmissions();
    }

    // Refresh bed availability and active admissions.
    private void refreshData() {
        refreshWardBeds();
        loadActiveAdmissions();
    }

    // Show all beds in the table but only available
    // beds in the admission dropdown.
    private void refreshWardBeds() {
        bedCombo.removeAllItems();
        bedsModel.setRowCount(0);

        String ward =
                (String) wardCombo.getSelectedItem();

        if (ward == null) {
            return;
        }

        try {
            List<Room> allRooms =
                    roomDAO.getAllRoomsByWard(ward);

            for (Room room : allRooms) {
                bedsModel.addRow(new Object[]{
                        room.getWardName(),
                        bedLabel(room),
                        room.getRoomNumber(),
                        room.getRoomType(),
                        room.isAvailable()
                                ? "Available"
                                : "Occupied"
                });
            }

            List<Room> availableRooms =
                    roomDAO.getAvailableRoomsByWard(ward);

            for (Room room : availableRooms) {
                bedCombo.addItem(room);
            }

            if (availableRooms.isEmpty()) {
                bedCombo.setToolTipText(
                        "No available beds in this ward.");
            } else {
                bedCombo.setToolTipText(
                        "Select an available bed.");
            }

        } catch (SQLException ex) {
            showDatabaseError(
                    "Could not refresh ward bed availability.",
                    ex);
        }
    }

    // Load active admissions from the database.
    private void loadActiveAdmissions() {
        activeAdmissionsModel.setRowCount(0);
        activeAdmissions.clear();

        try {
            activeAdmissions.addAll(
                    admissionService.getActiveAdmissions());

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd HH:mm");

            for (Admission admission : activeAdmissions) {
                LocalDateTime admittedAt =
                        admission.getAdmissionDate();

                String admittedText =
                        admittedAt == null
                                ? "Unknown"
                                : admittedAt.format(formatter);

                activeAdmissionsModel.addRow(
                        new Object[]{
                                admission.getAdmissionId(),
                                patientName(
                                        admission.getPatient()),
                                doctorName(
                                        admission.getDoctor()),
                                admission.getWardName(),
                                bedDisplay(admission),
                                admittedText
                        });
            }

        } catch (SQLException ex) {
            showDatabaseError(
                    "Could not load active admissions.", ex);
        }
    }

    // Admit the selected patient to the selected bed.
    private void admitPatient() {
        String admissionId =
                admissionIdField.getText().trim();

        Patient patient =
                (Patient) patientCombo.getSelectedItem();

        Doctor doctor =
                (Doctor) doctorCombo.getSelectedItem();

        String ward =
                (String) wardCombo.getSelectedItem();

        Room bed =
                (Room) bedCombo.getSelectedItem();

        if (admissionId.isEmpty()
                || admissionId.length() > 30) {

            showError(
                    "Enter an Admission ID of 1 to 30 characters.");
            return;
        }

        if (patient == null) {
            showError("Select a registered patient.");
            return;
        }

        if (doctor == null) {
            showError("Select a registered doctor.");
            return;
        }

        if (ward == null) {
            showError("Select a ward.");
            return;
        }

        if (bed == null) {
            showError(
                    "No available bed is selected. "
                    + "Choose another ward or refresh.");
            return;
        }

        Date selectedDate =
                (Date) admissionDate.getValue();

        LocalDateTime admittedAt =
                LocalDateTime.ofInstant(
                        selectedDate.toInstant(),
                        ZoneId.systemDefault());

        Admission admission = new Admission(
                admissionId,
                patient,
                doctor,
                bed.getRoomNumber());

        admission.setAdmissionDate(admittedAt);
        admission.setWardName(ward);
        admission.setStatus("Active");

        try {
            boolean success =
                    admissionService.admitPatient(
                            admission, ward);

            if (!success) {
                showError(
                        "Admission failed. The bed may no "
                        + "longer be available. Refresh and retry.");
                refreshData();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Patient admitted successfully to "
                            + ward + ", " + bedLabel(bed)
                            + " (" + bed.getRoomNumber() + ").",
                    "Admission Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            admissionIdField.setText("");

            refreshData();

        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Admission Failed",
                    JOptionPane.ERROR_MESSAGE);

            refreshData();
        }
    }

    // Discharge the selected active admission.
    private void dischargeSelected(JTable table) {
        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            showError(
                    "Select an active admission to discharge.");
            return;
        }

        int row =
                table.convertRowIndexToModel(viewRow);

        if (row < 0 || row >= activeAdmissions.size()) {
            showError(
                    "Could not identify the selected admission. "
                    + "Refresh and try again.");
            return;
        }

        Admission admission =
                activeAdmissions.get(row);

        int confirmation =
                JOptionPane.showConfirmDialog(
                        this,
                        "Discharge admission "
                                + admission.getAdmissionId()
                                + "?",
                        "Confirm Discharge",
                        JOptionPane.YES_NO_OPTION);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean success =
                    admissionService.dischargePatient(
                            admission.getAdmissionId());

            if (!success) {
                showError(
                        "Discharge failed. Refresh and check "
                        + "the admission and bed status.");
                refreshData();
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Patient discharged successfully.",
                    "Discharge Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            refreshData();

        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Discharge Failed",
                    JOptionPane.ERROR_MESSAGE);

            refreshData();
        }
    }

    // Display a bed number, falling back to its room number.
    private static String bedLabel(Room room) {
        return room.getBedNo() > 0
                ? "Bed " + room.getBedNo()
                : room.getRoomNumber();
    }

    // Display the bed number and room number in the admissions table.
    private String bedDisplay(Admission admission) {
        try {
            Room room = roomDAO.getRoomByNumber(
                    admission.getRoomNumber());

            if (room != null && room.getBedNo() > 0) {
                return "Bed " + room.getBedNo()
                        + " (" + room.getRoomNumber() + ")";
            }

        } catch (SQLException ex) {
            System.err.println(
                    "Could not load bed details: "
                            + ex.getMessage());
        }

        return admission.getRoomNumber();
    }

    private static String patientName(Patient patient) {
        return patient == null
                ? "Unknown patient"
                : patient.getFirstname() + " "
                        + patient.getLastname();
    }

    private static String doctorName(Doctor doctor) {
        return doctor == null
                ? "Unknown doctor"
                : doctor.getFirstname() + " "
                        + doctor.getLastname();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Admission",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showDatabaseError(
            String message, SQLException ex) {

        JOptionPane.showMessageDialog(
                this,
                message + "\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
