
package gui;

import model.Patient;
import model.Prescription;
import service.PrescriptionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PatientPrescriptionForm extends JFrame {

    private final Patient patient;
    private final PrescriptionService prescriptionService =
            new PrescriptionService();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                            "Prescription ID",
                            "Doctor",
                            "Medicine",
                            "Dosage",
                            "Frequency",
                            "Duration (days)",
                            "Instructions",
                            "Prescription Date"
                    }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable prescriptionTable = new JTable(tableModel);
    private final JButton refreshButton = new JButton("Refresh");

    public PatientPrescriptionForm(Patient patient) {
        if (patient == null || patient.getId() <= 0) {
            throw new IllegalArgumentException(
                    "A valid logged-in patient is required.");
        }

        this.patient = patient;

        setTitle("My Prescriptions");
        setSize(1000, 500);
        setMinimumSize(new Dimension(750, 350));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel heading = new JLabel("My Prescriptions");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setForeground(new Color(25, 55, 85));

        JLabel subtitle = new JLabel(
                "Prescriptions for "
                        + patient.getFirstname() + " "
                        + patient.getLastname());

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setOpaque(false);
        header.add(heading);
        header.add(subtitle);

        mainPanel.add(header, BorderLayout.NORTH);

        prescriptionTable.setFillsViewportHeight(true);
        prescriptionTable.setAutoCreateRowSorter(true);
        prescriptionTable.setRowHeight(25);

        mainPanel.add(
                new JScrollPane(prescriptionTable),
                BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(refreshButton);
        buttons.add(closeButton);

        mainPanel.add(buttons, BorderLayout.SOUTH);
        setContentPane(mainPanel);

        refreshButton.addActionListener(event -> loadPrescriptions());
        closeButton.addActionListener(event -> dispose());

        loadPrescriptions();
        setVisible(true);
    }

    private void loadPrescriptions() {
        refreshButton.setEnabled(false);

        new SwingWorker<List<Prescription>, Void>() {

            @Override
            protected List<Prescription> doInBackground()
                    throws SQLException {
                return prescriptionService.getPrescriptionsForPatient(
                        patient.getId());
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);

                try {
                    List<Prescription> prescriptions = get();
                    tableModel.setRowCount(0);

                    for (Prescription prescription : prescriptions) {
                        String doctorName = "N/A";

                        if (prescription.getDoctor() != null) {
                            doctorName = (
                                    prescription.getDoctor().getFirstname()
                                    + " "
                                    + prescription.getDoctor().getLastname()
                            ).trim();
                        }

                        String medicineName =
                                prescription.getMedicine() == null
                                        ? "N/A"
                                        : prescription.getMedicine()
                                                .getMedicineName();

                        tableModel.addRow(new Object[]{
                                prescription.getPrescriptionId(),
                                doctorName,
                                medicineName,
                                prescription.getDosage(),
                                prescription.getFrequency(),
                                prescription.getDurationDays(),
                                prescription.getInstructions(),
                                prescription.getPrescriptionDate()
                        });
                    }

                    if (prescriptions.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                PatientPrescriptionForm.this,
                                "No prescriptions found for your account.",
                                "My Prescriptions",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();

                    JOptionPane.showMessageDialog(
                            PatientPrescriptionForm.this,
                            "Loading prescriptions was interrupted.");

                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();

                    JOptionPane.showMessageDialog(
                            PatientPrescriptionForm.this,
                            "Could not load prescriptions: "
                                    + (cause == null
                                    ? "Unknown error"
                                    : cause.getMessage()),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}

