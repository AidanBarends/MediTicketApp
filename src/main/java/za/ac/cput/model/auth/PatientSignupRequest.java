package za.ac.cput.model.auth;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class PatientSignupRequest {

    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String cellPhone;
    private String password;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateRegistered;

    private String emergencyContact;

    public PatientSignupRequest() {}

    public PatientSignupRequest(String firstName, String middleName, String lastName,
                                String email, String cellPhone, String password,
                                LocalDate dob, LocalDate dateRegistered,
                                String emergencyContact) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.email = email;
        this.cellPhone = cellPhone;
        this.password = password;
        this.dob = dob;
        this.dateRegistered = dateRegistered;
        this.emergencyContact = emergencyContact;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCellPhone() { return cellPhone; }
    public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public LocalDate getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(LocalDate dateRegistered) { this.dateRegistered = dateRegistered; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}