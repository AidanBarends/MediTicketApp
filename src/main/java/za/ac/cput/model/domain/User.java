package za.ac.cput.model.domain;

import java.time.LocalDate;

public class User {
    private int userId;
    private Name name;
    private String email;
    private String cellPhone;
    private String password; // rarely populated on responses; present for symmetry
    private LocalDate dob;
    private String accountStatus; // ACTIVE, INACTIVE, SUSPENDED
    private String userType;      // PATIENT, DOCTOR, CLINIC_STAFF

    public User() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Name getName() { return name; }
    public void setName(Name name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCellPhone() { return cellPhone; }
    public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
}