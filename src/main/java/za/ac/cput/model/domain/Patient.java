package za.ac.cput.model.domain;

import java.time.LocalDate;

public class Patient extends User {
    private LocalDate dateRegistered;
    private String emergencyContact;

    public Patient() {}

    public LocalDate getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(LocalDate dateRegistered) { this.dateRegistered = dateRegistered; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}