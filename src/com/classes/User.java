package com.classes;

public class User
{   
    private String username;
    private String password;
    private String id;
    private String contactNo;
    private String gender;

    public User(String username, String password) 
    {   
        this(username, "", "", password, "M");
        this.username = username;
        this.password = password;
    }
    
    public User(String username, String password, String id, String contactNo) 
    {
        this(username, id, contactNo, password, "Other");
        this.username = username;
        this.id = id;
        this.contactNo = contactNo;
        this.password = password;    
    }
    
    public User(String username, String id, String contactNo, String password, String gender)
    {
        this.username = username;
        this.id = id;
        this.contactNo = contactNo;
        this.password = password;
        this.gender = gender;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }
    
    public String getContactNo() {
        return contactNo;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return password;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "User{" + "username=" + username + ", password=" + password + 
                ", id=" + id + ", contactNo=" + contactNo + ", gender=" + gender
                + '}';
    }
}