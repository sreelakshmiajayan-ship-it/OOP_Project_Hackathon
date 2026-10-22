package service;

import dao.AppointmentDAO;
import dao.MedicineDAO;
import dao.PrescriptionDAO;
import model.Appointment;
import model.Medicine;
import model.Prescription;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptionService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public List<Prescription> getPrescriptionsForDoctor(int doctorId)
            throws SQLException {
        if (doctorId <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID.");
        }

        return prescriptionDAO.getPrescriptionsByDoctorId(doctorId);
    }

    public List<Prescription> getPrescriptionsForPatient(int patientId)
        throws SQLException {

    if (patientId <= 0) {
        throw new IllegalArgumentException("Invalid patient ID.");
    }

    return prescriptionDAO.getPrescriptionsByPatientId(patientId);
    }

    public List<Medicine> getAvailableMedicines() throws SQLException {
        return medicineDAO.getAllMedicines().stream()
                .filter(Medicine::isAvailable)
                .collect(Collectors.toList());
    }

    public List<Prescription> getAllPrescriptionsForAdmin()
        throws SQLException {

    return prescriptionDAO.getAllPrescriptions();
    
    }

    
    public boolean createPrescription(
            Prescription prescription, int authenticatedDoctorId)
            throws SQLException {
        if (prescription == null || prescription.getDoctor() == null
                || prescription.getPatient() == null
                || prescription.getMedicine() == null) {
            throw new IllegalArgumentException(
                    "Patient, doctor, and medicine are required.");
        }
        if (authenticatedDoctorId <= 0
                || prescription.getDoctor().getId() != authenticatedDoctorId) {
            throw new IllegalArgumentException(
                    "A doctor can only create prescriptions under their "
                            + "own account.");
        }
        if (prescription.getAppointment() == null
                || prescription.getAppointment().getAppointmentId() <= 0) {
            throw new IllegalArgumentException(
                    "Select an appointment for this prescription.");
        }
        if (prescription.getDosage() == null
                || prescription.getDosage().isBlank()
                || prescription.getFrequency() == null
                || prescription.getFrequency().isBlank()
                || prescription.getDurationDays() <= 0
                || prescription.getPrescriptionDate() == null) {
            throw new IllegalArgumentException(
                    "Enter dosage, frequency, a positive duration, "
                            + "and prescription date.");
        }

        Appointment appointment = appointmentDAO.getAppointmentById(
                prescription.getAppointment().getAppointmentId());
        if (appointment == null
                || appointment.getDoctor() == null
                || appointment.getPatient() == null
                || appointment.getDoctor().getId() != authenticatedDoctorId
                || appointment.getPatient().getId()
                        != prescription.getPatient().getId()) {
            throw new IllegalArgumentException(
                    "The selected appointment is not assigned to this "
                            + "doctor and patient.");
        }
        if (!"Scheduled".equalsIgnoreCase(appointment.getStatus())
                && !"Completed".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "A prescription can only be linked to a scheduled or "
                            + "completed appointment.");
        }

        Medicine medicine = medicineDAO.getMedicineById(
                prescription.getMedicine().getMedicineId());
        if (medicine == null || !medicine.isAvailable()) {
            throw new IllegalArgumentException(
                    "The selected medicine is unavailable.");
        }

        prescription.setDoctor(appointment.getDoctor());
        prescription.setPatient(appointment.getPatient());
        prescription.setAppointment(appointment);
        prescription.setMedicine(medicine);

        return prescriptionDAO.addPrescription(prescription);
    }
}
