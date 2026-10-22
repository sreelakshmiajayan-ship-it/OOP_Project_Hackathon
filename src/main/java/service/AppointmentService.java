
package service;

import exception.AppointmentConflictException;
import exception.RoomUnavailableException;
import dao.AppointmentDAO;
import model.Appointment;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AppointmentService {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    // Book Appointment
    public boolean bookAppointment(Appointment appointment)
            throws SQLException, AppointmentConflictException, RoomUnavailableException {

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment details cannot be null.");
        }

        if (appointment.getPatient() == null
                || appointment.getDoctor() == null) {
            throw new IllegalArgumentException(
                    "Patient and doctor are required.");
        }
        List<Appointment> existingAppointments = appointmentDAO.getAllAppointments();
        
        for (Appointment existing : existingAppointments) {
            if ("Scheduled".equalsIgnoreCase(existing.getStatus())) {
                boolean sameDateTime = existing.getAppointmentDate().equals(appointment.getAppointmentDate()) && 
                                       existing.getAppointmentTime().equals(appointment.getAppointmentTime());

                if (sameDateTime) {
                    boolean doctorBusy = existing.getDoctor().getId() == appointment.getDoctor().getId();
                    boolean patientBusy = existing.getPatient().getId() == appointment.getPatient().getId();

                    if (doctorBusy || patientBusy) {
                        throw new AppointmentConflictException(
                            "Appointment Conflict: The selected doctor or patient already has an appointment at this date and time."
                        );
                    }
                    if (appointment.getRoom() != null && existing.getRoom() != null) {
                        if (existing.getRoom().getRoomNumber().equalsIgnoreCase(appointment.getRoom().getRoomNumber())) {
                            throw new RoomUnavailableException(
                                "Room Unavailable: Room " + appointment.getRoom().getRoomNumber() + " is already booked for this time slot."
                            );
                        }
                    }
                }
            }
        }
       
        appointment.setStatus("Scheduled");

        return appointmentDAO.addAppointment(appointment);
    }

    // Get All Appointments
    public List<Appointment> getAllAppointments()
            throws SQLException {
        return appointmentDAO.getAllAppointments();
    }

    public List<Appointment> getAppointmentsForDoctor(int doctorId)
            throws SQLException {
        if (doctorId <= 0) {
            throw new IllegalArgumentException("Invalid doctor ID.");
        }

        return appointmentDAO.getAppointmentsByDoctorId(doctorId);
    }

    
public List<Appointment> getAppointmentsForPatient(int patientId)
        throws SQLException {

    if (patientId <= 0) {
        throw new IllegalArgumentException("Invalid patient ID.");
    }

    return appointmentDAO.getAppointmentsByPatientId(patientId);
}
    

public boolean rescheduleAppointment(
        int appointmentId,
        int patientId,
        String newDate,
        String newTime)
        throws SQLException, AppointmentConflictException {

    if (appointmentId <= 0 || patientId <= 0) {
        throw new IllegalArgumentException(
                "Appointment ID and patient ID must be positive.");
    }

    if (newDate == null || newTime == null
            || newDate.isBlank() || newTime.isBlank()) {
        throw new IllegalArgumentException(
                "New appointment date and time are required.");
    }

    // Check whether the new time conflicts with another appointment.
    boolean conflict = appointmentDAO.hasReschedulingConflict(
            appointmentId, patientId, newDate, newTime);

    if (conflict) {
        throw new AppointmentConflictException(
                "The doctor or patient already has an appointment "
                        + "at the selected date and time.");
    }

    // Update only if the appointment belongs to this patient.
    boolean updated = appointmentDAO.rescheduleAppointment(
            appointmentId, patientId, newDate, newTime);

    if (!updated) {
        throw new IllegalArgumentException(
                "Appointment not found, not owned by this patient, "
                        + "or no longer scheduled.");
    }

    return true;
}


    
public boolean cancelAppointmentForPatient(
        int appointmentId, int patientId) throws SQLException {

    if (appointmentId <= 0 || patientId <= 0) {
        throw new IllegalArgumentException(
                "Appointment ID and patient ID must be positive.");
    }

    boolean cancelled = appointmentDAO.cancelAppointmentByPatient(
            appointmentId, patientId);

    if (!cancelled) {
        throw new IllegalArgumentException(
                "Appointment not found, not owned by this patient, "
                        + "or no longer scheduled.");
    }

    return true;
}


    public boolean updateStatusForDoctor(
            int appointmentId, int doctorId, String status)
            throws SQLException {
        if (appointmentId <= 0 || doctorId <= 0) {
            throw new IllegalArgumentException(
                    "Appointment and doctor IDs must be positive.");
        }
        if (!"Completed".equals(status) && !"Cancelled".equals(status)) {
            throw new IllegalArgumentException(
                    "Doctors may mark scheduled appointments Completed "
                            + "or Cancelled.");
        }

        boolean updated = appointmentDAO.updateAppointmentStatus(
                appointmentId, doctorId, status);
        if (!updated) {
            throw new IllegalArgumentException(
                    "Appointment was not found, is not assigned to this "
                            + "doctor, or is no longer scheduled.");
        }

        return true;
    }

    // Get Appointment by ID
    public Appointment getAppointmentById(int appointmentId)
            throws SQLException {

        Appointment appointment =
                appointmentDAO.getAppointmentById(appointmentId);

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment with ID " + appointmentId + " not found.");
        }

        return appointment;
    }

    // Cancel Appointment
    public boolean cancelAppointment(int appointmentId)
            throws SQLException {

        boolean cancelled =
                appointmentDAO.cancelAppointment(appointmentId);

        if (!cancelled) {
            throw new IllegalArgumentException(
                    "Appointment not found or already cancelled.");
        }

        return true;
    }

    


}
