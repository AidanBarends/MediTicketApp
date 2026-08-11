package za.ac.cput.session;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String accessToken;
    private String refreshToken;
    private int userId;
    private String userType;   // PATIENT, DOCTOR, CLINIC_STAFF
    private String staffRole;  // NURSE, ADMIN — only when userType == CLINIC_STAFF
    private String email;
    private String fullName;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void clear() {
        accessToken = null;
        refreshToken = null;
        userId = 0;
        userType = null;
        staffRole = null;
        email = null;
        fullName = null;
    }

    public boolean isLoggedIn() {
        return accessToken != null;
    }

    public boolean isAdmin() {
        return "CLINIC_STAFF".equals(userType) && "ADMIN".equals(staffRole);
    }

    public boolean isNurse() {
        return "CLINIC_STAFF".equals(userType) && "NURSE".equals(staffRole);
    }

    // Getters / setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getStaffRole() { return staffRole; }
    public void setStaffRole(String staffRole) { this.staffRole = staffRole; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}