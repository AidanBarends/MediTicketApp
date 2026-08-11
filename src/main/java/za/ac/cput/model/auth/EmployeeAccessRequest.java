package za.ac.cput.model.auth;

/*import za.ac.cput.domain.enums.RequestStatus;
import za.ac.cput.domain.enums.StaffRole;
import za.ac.cput.domain.enums.UserType;*/

import java.time.LocalDateTime;

public class EmployeeAccessRequest {

    private int requestId;
    private String email;
    private String requestedUserType;
    private String requestedStaffRole;
    private String status;
    private LocalDateTime requestDate;
    private LocalDateTime processedDate;
    private String adminNotes;

    public EmployeeAccessRequest() {}

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRequestedUserType() {
        return requestedUserType;
    }

    public void setRequestedUserType(String requestedUserType) {
        this.requestedUserType = requestedUserType;
    }

    public String getRequestedStaffRole() {
        return requestedStaffRole;
    }

    public void setRequestedStaffRole(String requestedStaffRole) {
        this.requestedStaffRole = requestedStaffRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(LocalDateTime processedDate) {
        this.processedDate = processedDate;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}