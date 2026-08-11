package za.ac.cput.model.domain;

import java.time.LocalDateTime;

public class TicketStatus {
    private int statusId;
    private String statusType; // OPEN, IN_PROGRESS, RESOLVED, CLOSED, ESCALATED
    private LocalDateTime statusDate;
    private String notes;
    // Note: backend TicketStatus.ticket is @JsonIgnore-worthy to avoid cycles —
    // if it's not ignored server-side, watch for infinite recursion with PatientTicket.statusHistory.

    public TicketStatus() {}

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public String getStatusType() { return statusType; }
    public void setStatusType(String statusType) { this.statusType = statusType; }

    public LocalDateTime getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDateTime statusDate) { this.statusDate = statusDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}