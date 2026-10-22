
package gui;

import model.Admission;
import model.Doctor;
import model.Patient;
import model.Room;
import service.AdmissionService;
import service.DoctorService;
import service.PatientService;
import dao.RoomDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
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
    private final JComboBox<Room> roomCombo = new JComboBox<>();

    public AdmissionForm(JFrame parent) {
        super(parent, "Admit Patient", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(520, 380);
        setLocationRelativeTo(parent);

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 14));
        form.setBorder(new EmptyBorder(25, 25, 15, 25));

        form.add(new JLabel("Admission ID:"));
        form.add(admissionIdField);

        form.add(new JLabel("Select Patient:"));
        form.add(patientCombo);

        form.add(new JLabel("Select Doctor:"));
        form.add(doctorCombo);

        form.add(new JLabel("Available Room:"));
        form.add(roomCombo);

        patientCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(
                        list, value, index, selected, focused);

                if (value instanceof Patient) {
                    Patient p = (Patient) value;
                    setText(p.getId() + " - "
                            + p.getFirstname() + " "
                            + p.getLastname());
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
                    Doctor d = (Doctor) value;
                    setText(d.getId() + " - "
                            + d.getFirstname() + " "
                            + d.getLastname() + " ("
                            + d.getSpecialization() + ")");
                }
                return this;
            }
        });

        roomCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {
                super.getListCellRendererComponent(
                        list, value, index, selected, focused);

                if (value instanceof Room) {
                    Room r = (Room) value;
                    setText(r.getRoomNumber() + " - "
                            + r.getRoomType() + " - Rs. "
                            + r.getDailyCharge() + "/day");
                }
                return this;
            }
        });

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        JButton admitButton = new JButton("Admit Patient");
        JButton cancelButton = new JButton("Cancel");

        admitButton.addActionListener(e -> admitPatient());
        cancelButton.addActionListener(e -> dispose());

        buttons.add(admitButton);
        buttons.add(cancelButton);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        loadFormData();
    }

    private void loadFormData() {
        try {
            patientCombo.removeAllItems();
            doctorCombo.removeAllItems();
            roomCombo.removeAllItems();

            List<Patient> patients =
                    patientService.getAllPatients();

            List<Doctor> doctors =
                    doctorService.getAllDoctors();

            List<Room> rooms = roomDAO.getAvailableRooms();

            for (Patient p : patients) {
                patientCombo.addItem(p);
            }

            for (Doctor d : doctors) {
                doctorCombo.addItem(d);
            }

            for (Room r : rooms) {
                roomCombo.addItem(r);
            }

            if (patients.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "Register a patient before admission.");
            }

            if (doctors.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "Register a doctor before admission.");
            }

            if (rooms.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "No available rooms found. Add or release a room.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load admission details:\n"
                            + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void admitPatient() {
        String admissionId = admissionIdField.getText().trim();

        Patient patient =
                (Patient) patientCombo.getSelectedItem();

        Doctor doctor =
                (Doctor) doctorCombo.getSelectedItem();

        Room room = (Room) roomCombo.getSelectedItem();

        if (admissionId.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Enter an Admission ID.");
            return;
        }

        if (patient == null || doctor == null || room == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a patient, doctor, and available room.");
            return;
        }

        Admission admission = new Admission(
                admissionId, patient, doctor, room.getRoomNumber());

        try {
            boolean success =
                    admissionService.admitPatient(admission);

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Patient admitted successfully!\n"
                                + "Admission ID: " + admissionId
                                + "\nRoom: " + room.getRoomNumber(),
                        "Admission Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose();
            }

        } catch (SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Admission Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
