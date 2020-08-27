package com.classes;

import java.sql.Blob;

public class Movie 
{
    private final String title;
    private final String language;
    private final String date;
    private final Blob image;
    private final String duration;
    private final double price;
    private final String description;
  
    public Movie(String title, String language, String date, Blob image, String duration, double price, String description) {
        this.title = title;
        this.language = language;
        this.date = date;
        this.image = image;
        this.duration = duration;
        this.price = price;
        this.description = description;
    }
    
    public Movie(String title, double price) {
        this(title, "", "", null, "", price, "");
    }
    
    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getDate() {
        return date;
    }

    public Blob getImage() {
        return image;
    }

    public String getDuration() {
        return duration;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString()
    {
        return "Movie{" + "title=" + title + ", language=" + language + 
                ", date=" + date + ", image=" + image + ", duration=" + duration
                + ", price=" + price + ", description=" + description + '}';
    }
}
