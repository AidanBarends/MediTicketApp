package za.ac.cput.model.auth;

/*import za.ac.cput.domain.enums.StaffRole;
import za.ac.cput.domain.enums.UserType;*/

public class EmployeeInviteRequest {

    private String email;
    private String userType;

    // Only meaningful when userType == CLINIC_STAFF. Must be null for DOCTOR invites.
    private String staffRole;

    // Required no-arg constructor for Jackson deserialization
    public EmployeeInviteRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
}