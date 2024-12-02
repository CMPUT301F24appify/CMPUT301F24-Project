package com.example.appify;

import com.example.appify.Model.Entrant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntrantTest {

    private Entrant entrant;

    @BeforeEach
    void setUp() {
        entrant = new Entrant(
                "123",
                "John Doe",
                "1234567890",
                "john@example.com",
                "https://example.com/profile.jpg",
                true,
                "mdsk49dklmf",
                56.1304,
                -106.3468
        );
        entrant.setAdmin(true);
        entrant.setGeneratedPicture(false);
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
    void testGetFacilityID() {
        assertEquals("mdsk49dklmf", entrant.getFacilityID());
    }

    @Test
    void testSetFacilityID() {
        entrant.setFacilityID("facility123");
        assertEquals("facility123", entrant.getFacilityID());
    }

    @Test
    void testGetLatitude() {
        assertEquals(56.1304, entrant.getLatitude());
    }

    @Test
    void testSetLatitude() {
        entrant.setLatitude(40.7128);
        assertEquals(40.7128, entrant.getLatitude());
    }

    @Test
    void testGetLongitude() {
        assertEquals(-106.3468, entrant.getLongitude());
    }

    @Test
    void testSetLongitude() {
        entrant.setLongitude(-74.0060);
        assertEquals(-74.0060, entrant.getLongitude());
    }

    @Test
    void testIsAdmin() {
        assertTrue(entrant.isAdmin());
    }

    @Test
    void testSetAdmin() {
        entrant.setAdmin(false);
        assertFalse(entrant.isAdmin());
    }

    @Test
    void testIsGeneratedPicture() {
        assertFalse(entrant.isGeneratedPicture());
    }

    @Test
    void testSetGeneratedPicture() {
        entrant.setGeneratedPicture(true);
        assertTrue(entrant.isGeneratedPicture());
    }
}
