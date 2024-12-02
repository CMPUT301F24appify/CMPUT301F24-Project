package com.example.appify;

import com.example.appify.Model.Entrant;
import com.example.appify.Model.Event;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntrantTest {

    private Entrant entrant;

    @BeforeEach
    void setUp() {
        entrant = new Entrant("123", "John Doe", "1234567890", "john@example.com",
                "https://example.com/profile.jpg", true, "sdjajk2", 56.1304, -106.3468);
    }

    // Existing Tests

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

    // New Tests for Additional Getters and Setters

    @Test
    void testIsAdmin() {
        assertFalse(entrant.isAdmin());
        entrant.setAdmin(true);
        assertTrue(entrant.isAdmin());
    }

    @Test
    void testIsGeneratedPicture() {
        assertFalse(entrant.isGeneratedPicture());
        entrant.setGeneratedPicture(true);
        assertTrue(entrant.isGeneratedPicture());
    }

    @Test
    void testGetLatitude() {
        assertEquals(56.1304, entrant.getLatitude(), 0.0001);
    }

    @Test
    void testSetLatitude() {
        entrant.setLatitude(40.7128);
        assertEquals(40.7128, entrant.getLatitude(), 0.0001);
    }

    @Test
    void testGetLongitude() {
        assertEquals(-106.3468, entrant.getLongitude(), 0.0001);
    }

    @Test
    void testSetLongitude() {
        entrant.setLongitude(-74.0060);
        assertEquals(-74.0060, entrant.getLongitude(), 0.0001);
    }

    @Test
    void testSecondConstructor() {
        Entrant entrant2 = new Entrant("456", "Jane Smith", "5555555555", "jane@example.com",
                "https://example.com/jane.jpg", false, "facility456");
        assertEquals("456", entrant2.getId());
        assertEquals("Jane Smith", entrant2.getName());
        assertEquals("5555555555", entrant2.getPhoneNumber());
        assertEquals("jane@example.com", entrant2.getEmail());
        assertEquals("https://example.com/jane.jpg", entrant2.getProfilePictureUrl());
        assertFalse(entrant2.isNotifications());
        assertEquals("facility456", entrant2.getFacilityID());
        // Latitude and Longitude should default to 0.0
        assertEquals(0.0, entrant2.getLatitude(), 0.0001);
        assertEquals(0.0, entrant2.getLongitude(), 0.0001);
    }
}
