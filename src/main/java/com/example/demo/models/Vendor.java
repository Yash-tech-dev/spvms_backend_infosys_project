package com.example.demo.models; // <--- This should match your folder structure
import jakarta.persistence.*;

@Entity
@Table(name = "vendor") // Maps to a table named 'vendor'
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "user_id") // to link with user_id column of users table
    private User user;

    private String name;

    private String contactPerson;

    private String email;

    private String phone;

    private String address;

    // --- Constructors, Getters, and Setters ---

    // Default constructor (required by JPA)
    public Vendor() {}

    // Getters and Setters for all fields (omitted for brevity)


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}