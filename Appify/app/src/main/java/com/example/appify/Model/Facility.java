package com.example.appify.Model;

public class Facility {
    private String id;
    private String name;
    private String location;
    private String email;
    private String description;
    private Integer capacity;
    private String organizerID;

    public Facility(String id, String name, String location, String email, String description, Integer capacity, String organizerID) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.email = email;
        this.description = description;
        this.capacity = capacity;
        this.organizerID = organizerID;
    }

    // Getters
    public String getId() { return id; }

    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getEmail() { return email; }
    public String getDescription() { return description; }
    public Integer getCapacity() { return capacity; }
    public String getOrganizerID() { return organizerID; }
}
