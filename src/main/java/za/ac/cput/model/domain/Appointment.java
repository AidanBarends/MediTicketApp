package za.ac.cput.model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private int appointmentId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String confirmationStatus; // PENDING, CONFIRMED, REJECTED, COMPLETED, CANCELLED, RESCHEDULED
    private Patient patient;
    private Doctor doctor;
    private ClinicStaff staff;
    private String reason;

    public Appointment() {}

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getConfirmationStatus() { return confirmationStatus; }
    public void setConfirmationStatus(String confirmationStatus) { this.confirmationStatus = confirmationStatus; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public ClinicStaff getStaff() { return staff; }
    public void setStaff(ClinicStaff staff) { this.staff = staff; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}