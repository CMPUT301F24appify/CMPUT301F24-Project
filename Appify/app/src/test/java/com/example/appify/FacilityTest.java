package com.example.appify;

import com.example.appify.Model.Facility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacilityTest {

    private Facility facility;

    @BeforeEach
    void setUp() {
        facility = new Facility(
                "facility123",
                "Community Hall",
                "123 Main Street, Cityville",
                "contact@communityhall.com",
                "A spacious hall for community events.",
                200,
                "organizer567"
        );
    }

    @Test
    void testGetId() {
        assertEquals("facility123", facility.getId());
    }

    @Test
    void testGetName() {
        assertEquals("Community Hall", facility.getName());
    }

    @Test
    void testGetLocation() {
        assertEquals("123 Main Street, Cityville", facility.getLocation());
    }

    @Test
    void testGetEmail() {
        assertEquals("contact@communityhall.com", facility.getEmail());
    }

    @Test
    void testGetDescription() {
        assertEquals("A spacious hall for community events.", facility.getDescription());
    }

    @Test
    void testGetCapacity() {
        assertEquals(200, facility.getCapacity());
    }

    @Test
    void testGetOrganizerID() {
        assertEquals("organizer567", facility.getOrganizerID());
    }
}
