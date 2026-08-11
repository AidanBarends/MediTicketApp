package za.ac.cput.model.auth;

/*import za.ac.cput.domain.enums.StaffRole;
import za.ac.cput.domain.enums.UserType;*/

public class EmployeeAccessRequestSubmission {

    private String email;
    private String userType;
    private String staffRole; // must be null for DOCTOR, NURSE for CLINIC_STAFF (never ADMIN)

    // Required no-arg constructor for Jackson deserialization
    public EmployeeAccessRequestSubmission() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }
}