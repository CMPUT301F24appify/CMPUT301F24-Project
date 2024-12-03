package com.example.appify.Model;

/**
 * Represents a facility in the application with essential details like name, location,
 * email, description, capacity, and the ID of the organizer managing the facility.
 */
public class Facility {
    private String id;
    private String name;
    private String location;
    private String email;
    private String description;
    private Integer capacity;
    private String organizerID;

    /**
     * Constructs a new Facility instance with the specified details.
     *
     * @param id          Unique identifier for the facility.
     * @param name        Name of the facility.
     * @param location    Physical location of the facility.
     * @param email       Contact email for the facility.
     * @param description A brief description of the facility.
     * @param capacity    Maximum capacity of people that the facility can hold.
     * @param organizerID Unique identifier of the organizer responsible for the facility.
     */
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
    /**
     * Returns the unique identifier of the facility.
     *
     * @return The facility's ID.
     */
    public String getId() { return id; }

    /**
     * Returns the name of the facility.
     *
     * @return The facility's name.
     */
    public String getName() { return name; }

    /**
     * Returns the physical location of the facility.
     *
     * @return The facility's location.
     */
    public String getLocation() { return location; }

    /**
     * Returns the contact email for the facility.
     *
     * @return The facility's email.
     */
    public String getEmail() { return email; }

    /**
     * Returns the description of the facility.
     *
     * @return The facility's description.
     */
    public String getDescription() { return description; }

    /**
     * Returns the maximum capacity of people that the facility can hold.
     *
     * @return The facility's capacity.
     */
    public Integer getCapacity() { return capacity; }

    /**
     * Returns the unique identifier of the organizer responsible for the facility.
     *
     * @return The organizer's ID.
     */
    public String getOrganizerID() { return organizerID; }


    // Setters
    /**
     * Sets the name of the facility.
     *
     * @param name The new name for the facility.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the physical location of the facility.
     *
     * @param location The new location for the facility.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Sets the contact email for the facility.
     *
     * @param email The new email address for the facility.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets a brief description of the facility.
     *
     * @param description The new description for the facility.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the maximum capacity of people that the facility can hold.
     *
     * @param capacity The new capacity for the facility.
     */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

}