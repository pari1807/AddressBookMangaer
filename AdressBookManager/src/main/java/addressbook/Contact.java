package addressbook;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contact Entity Class
 * Represents a single contact with all necessary fields
 */
public class Contact {
    private int id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Contact() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Contact(String name, String phone, String email, String address, String notes) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Contact(int id, String name, String phone, String email, String address, String notes) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { 
        this.phone = phone; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { 
        this.address = address; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Validation methods
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[+]?[0-9]{10,15}$");
    }
    
    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }
    
    public static boolean isValidAddress(String address) {
        return address != null && address.trim().length() >= 5;
    }
    
    // Utility methods
    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getFormattedUpdatedAt() {
        return updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    @Override
    public String toString() {
        return String.format("Contact{id=%d, name='%s', phone='%s', email='%s'}", 
                            id, name, phone, email);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Contact contact = (Contact) obj;
        return id == contact.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}