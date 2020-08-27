package com.classes;

public class Booking 
{
    private final String name;
    private final String contactNumber;
    private final int id;
    private final int count;

    public Booking( int id, String name, String contactNumber, int count) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.id = id;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public String getContactNumber() {
        return contactNumber;
    } 
    
    public int getTicketId() {
        return id;
    }
    
    public int getCount() {
        return count;
    }
    
    @Override
    public String toString() {
        return "Bookings{" + "name=" + name + ", contactNumber=" + 
                contactNumber + ", id=" + id + '}';
    }
}