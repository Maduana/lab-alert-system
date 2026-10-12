package za.co.lab.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;
    private Integer pathologistId;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getPathologistId() { return pathologistId; }
    public void setPathologistId(Integer pathologistId) { this.pathologistId = pathologistId; }
}