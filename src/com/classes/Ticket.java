package com.classes;

public class Ticket
{
    private final int ticketNumber;
    private final String name;
    private final String title;
    private final String time;
    private final String date;

    public Ticket(int ticketNumber, String name, String title, String time, String date) {
        this.ticketNumber = ticketNumber;
        this.name = name;
        this.title = title;
        this.time = time;
        this.date = date;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Ticket{" + "ticketNumber=" + ticketNumber + ", name=" + name +
                ", title=" + title + ", time=" + time + ", date=" + date + '}';
    }   
}
