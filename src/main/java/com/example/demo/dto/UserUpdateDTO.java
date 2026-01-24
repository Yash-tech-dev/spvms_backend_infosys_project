package com.example.demo.dto;

public class UserUpdateDTO {
    private String email;
    private String mobileNo;
    private String companyName;
    private String address;

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}