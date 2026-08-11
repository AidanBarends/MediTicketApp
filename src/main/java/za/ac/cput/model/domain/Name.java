package za.ac.cput.model.domain;

public class Name {
    private String firstName;
    private String middleName;
    private String lastName;

    public Name() {}

    public Name(String firstName, String middleName, String lastName) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(firstName == null ? "" : firstName);
        if (middleName != null && !middleName.isBlank()) sb.append(" ").append(middleName);
        if (lastName != null && !lastName.isBlank()) sb.append(" ").append(lastName);
        return sb.toString().trim();
    }

    @Override
    public String toString() { return getFullName(); }
}