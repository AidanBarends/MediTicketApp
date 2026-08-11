package za.ac.cput.model.domain;

import java.time.LocalDateTime;
import java.util.List;

public class PatientTicket {
    private int ticketId;
    private String ticketDescription;
    private LocalDateTime ticketCreatedDate;
    private Patient patient;
    private Appointment appointment;
    private List<TicketStatus> statusHistory;
    private String currentStatus; // denormalized, same values as StatusType

    public PatientTicket() {}

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getTicketDescription() { return ticketDescription; }
    public void setTicketDescription(String ticketDescription) { this.ticketDescription = ticketDescription; }

    public LocalDateTime getTicketCreatedDate() { return ticketCreatedDate; }
    public void setTicketCreatedDate(LocalDateTime ticketCreatedDate) { this.ticketCreatedDate = ticketCreatedDate; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public List<TicketStatus> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<TicketStatus> statusHistory) { this.statusHistory = statusHistory; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
}