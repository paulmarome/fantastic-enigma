package com.classes;

public class Detector
{
    private final String idNumber;
    private final String password;

    public Detector(String idNumber, String password) {
        this.idNumber = idNumber;
        this.password = password;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Detector{" + "idNumber=" + idNumber + ", password=" 
                + password + '}';
    }
}