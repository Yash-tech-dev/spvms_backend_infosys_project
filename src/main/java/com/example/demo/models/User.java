package com.example.demo.models;

import jakarta.persistence.*;

/**
 * Entity representing a User within the system.
 * This class handles authentication details, role-based access control,
 * and profile information for Admins, Finance users, and Vendors.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique identifier used for login purposes. */
    @Column(unique = true, nullable = false)
    private String username;

    /** Hashed security password for the user. */
    @Column(nullable = false)
    private String password;

    private String email;

    /** Stores the user role (e.g., ROLE_ADMIN, ROLE_FINANCE, ROLE_VENDOR). */
    @Column(nullable = false)
    private String role;

    @Column(name= "company_name")
    private String companyName;

    @Column(name= "address")
    private String address;

    /** * Mapped to 'mobile_no' in the database.
     * Used to store the primary contact number.
     */
    @Column(name = "mobile_no") // DB column name se match karne ke liye
    private String phone;

    /** Indicates whether the user account is enabled or disabled. */
    @Column(name = "is_active")
    private boolean active = true;

    /**
     * Enumeration of available user roles for type safety.
     */
    public enum Role {
        ADMIN,
        FINANCE,
        VENDOR
    }

    // --- Getters and Setters ---

    /** @return the unique primary key of the user */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    /** @return the username */
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /** @return the password (usually encoded) */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /** @return the email address */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /** @return the role assigned to the user */
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /** * Alias getter for the 'phone' variable.
     * Working: Provides compatibility with existing logic that expects a 'mobileNo' field.
     */
    public String getMobileNo() {
        return phone;
    }

    /** * Alias setter for the 'phone' variable.
     * Working: Maps mobile number input directly to the internal 'phone' field.
     */
    public void setMobileNo(String mobileNo) {
        this.phone = mobileNo;
    }

    /** @return the registered company name */
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /** @return the physical address of the user or company */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /** @return the raw phone number value */
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** @return true if the account is currently active */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}