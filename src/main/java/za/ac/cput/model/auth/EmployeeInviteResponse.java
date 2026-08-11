package za.ac.cput.model.auth;

/*import za.ac.cput.domain.enums.StaffRole;
import za.ac.cput.domain.enums.UserType;*/

public class EmployeeInviteResponse {

    private String email;
    private String userType;
    private String staffRole; // null for DOCTOR invites

    // Required no-arg constructor for Jackson deserialization
    public EmployeeInviteResponse() {}

    public EmployeeInviteResponse(String email, String userType, String staffRole) {
        this.email = email;
        this.userType = userType;
        this.staffRole = staffRole;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
}