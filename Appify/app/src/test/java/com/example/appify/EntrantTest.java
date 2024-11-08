package com.example.appify;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class EntrantTest {

    private Entrant entrant;



    @BeforeEach
    void setUp() {
        entrant = new Entrant("123", "John Doe", "1234567890", "john@example.com", "https://example.com/profile.jpg", true);
    }

    @Test
    void testGetId() {
        assertEquals("123", entrant.getId());
    }

    @Test
    void testGetName() {
        assertEquals("John Doe", entrant.getName());
    }

    @Test
    void testGetPhoneNumber() {
        assertEquals("1234567890", entrant.getPhoneNumber());
    }

    @Test
    void testSetPhoneNumber() {
        entrant.setPhoneNumber("0987654321");
        assertEquals("0987654321", entrant.getPhoneNumber());
    }

    @Test
    void testGetEmail() {
        assertEquals("john@example.com", entrant.getEmail());
    }

    @Test
    void testGetProfilePictureUrl() {
        assertEquals("https://example.com/profile.jpg", entrant.getProfilePictureUrl());
    }

    @Test
    void testSetProfilePictureUrl() {
        entrant.setProfilePictureUrl("https://newexample.com/profile.jpg");
        assertEquals("https://newexample.com/profile.jpg", entrant.getProfilePictureUrl());
    }

    @Test
    void testIsNotifications() {
        assertTrue(entrant.isNotifications());
    }

    @Test
    void testSetNotifications() {
        entrant.setNotifications(false);
        assertFalse(entrant.isNotifications());
    }

    @Test
    void testSetFacilityID() {
        entrant.setFacilityID("facility123");
        assertEquals("facility123", entrant.getFacilityID());
    }

}