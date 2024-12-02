package com.example.appify;

import com.example.appify.Model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event(
                "Music Fest",
                "2024-12-31",
                "Main Hall",
                "2024-12-25",
                "A grand music festival",
                100,
                50,
                "https://example.com/poster.jpg",
                true,
                true,
                true,
                true,
                true,
                "Waitlisted message",
                "Enrolled message",
                "Cancelled message",
                "Invited message",
                "organizer123"
        );
    }

    @Test
    void testGetName() {
        assertEquals("Music Fest", event.getName());
    }

    @Test
    void testSetName() {
        event.setName("Art Fest");
        assertEquals("Art Fest", event.getName());
    }

    @Test
    void testGetDate() {
        assertEquals("2024-12-31", event.getDate());
    }

    @Test
    void testSetDate() {
        event.setDate("2025-01-01");
        assertEquals("2025-01-01", event.getDate());
    }

    @Test
    void testGetFacility() {
        assertEquals("Main Hall", event.getFacility());
    }

    @Test
    void testSetFacility() {
        event.setFacility("Conference Room");
        assertEquals("Conference Room", event.getFacility());
    }

    @Test
    void testGetRegistrationEndDate() {
        assertEquals("2024-12-25", event.getRegistrationEndDate());
    }

    @Test
    void testSetRegistrationEndDate() {
        event.setRegistrationEndDate("2024-12-20");
        assertEquals("2024-12-20", event.getRegistrationEndDate());
    }

    @Test
    void testGetDescription() {
        assertEquals("A grand music festival", event.getDescription());
    }

    @Test
    void testSetDescription() {
        event.setDescription("A fun art festival");
        assertEquals("A fun art festival", event.getDescription());
    }

    @Test
    void testGetMaxWaitEntrants() {
        assertEquals(100, event.getMaxWaitEntrants());
    }

    @Test
    void testSetMaxWaitEntrants() {
        event.setMaxWaitEntrants(200);
        assertEquals(200, event.getMaxWaitEntrants());
    }

    @Test
    void testGetMaxSampleEntrants() {
        assertEquals(50, event.getMaxSampleEntrants());
    }

    @Test
    void testSetMaxSampleEntrants() {
        event.setMaxSampleEntrants(60);
        assertEquals(60, event.getMaxSampleEntrants());
    }

    @Test
    void testGetPosterUri() {
        assertEquals("https://example.com/poster.jpg", event.getPosterUri());
    }

    @Test
    void testSetPosterUri() {
        event.setPosterUri("https://newexample.com/poster.jpg");
        assertEquals("https://newexample.com/poster.jpg", event.getPosterUri());
    }

    @Test
    void testIsGeolocate() {
        assertTrue(event.isGeolocate());
    }

    @Test
    void testSetGeolocate() {
        event.setGeolocate(false);
        assertFalse(event.isGeolocate());
    }

    @Test
    void testGetOrganizerID() {
        assertEquals("organizer123", event.getOrganizerID());
    }

    @Test
    void testGetEventId() {
        assertNotNull(event.getEventId()); // Ensure the eventId is generated
    }

    @Test
    void testGetNotifyWaitlisted() {
        assertTrue(event.isNotifyWaitlisted());
    }

    @Test
    void testSetNotifyWaitlisted() {
        event.setNotifyWaitlisted(false);
        assertFalse(event.isNotifyWaitlisted());
    }

    @Test
    void testGetNotifyEnrolled() {
        assertTrue(event.isNotifyEnrolled());
    }

    @Test
    void testSetNotifyEnrolled() {
        event.setNotifyEnrolled(false);
        assertFalse(event.isNotifyEnrolled());
    }

    @Test
    void testGetNotifyCancelled() {
        assertTrue(event.isNotifyCancelled());
    }

    @Test
    void testSetNotifyCancelled() {
        event.setNotifyCancelled(false);
        assertFalse(event.isNotifyCancelled());
    }

    @Test
    void testGetNotifyInvited() {
        assertTrue(event.isNotifyInvited());
    }

    @Test
    void testSetNotifyInvited() {
        event.setNotifyInvited(false);
        assertFalse(event.isNotifyInvited());
    }

    @Test
    void testGetWaitlistedMessage() {
        assertEquals("Waitlisted message", event.getWaitlistedMessage());
    }

    @Test
    void testSetWaitlistedMessage() {
        event.setWaitlistedMessage("New waitlisted message");
        assertEquals("New waitlisted message", event.getWaitlistedMessage());
    }

    @Test
    void testGetEnrolledMessage() {
        assertEquals("Enrolled message", event.getEnrolledMessage());
    }

    @Test
    void testSetEnrolledMessage() {
        event.setEnrolledMessage("New enrolled message");
        assertEquals("New enrolled message", event.getEnrolledMessage());
    }

    @Test
    void testGetCancelledMessage() {
        assertEquals("Cancelled message", event.getCancelledMessage());
    }

    @Test
    void testSetCancelledMessage() {
        event.setCancelledMessage("New cancelled message");
        assertEquals("New cancelled message", event.getCancelledMessage());
    }

    @Test
    void testGetInvitedMessage() {
        assertEquals("Invited message", event.getInvitedMessage());
    }

    @Test
    void testSetInvitedMessage() {
        event.setInvitedMessage("New invited message");
        assertEquals("New invited message", event.getInvitedMessage());
    }

    @Test
    void testLotteryRanFlagDefault() {
        assertFalse(event.getLotteryRanFlag()); // Ensure default is false
    }

    @Test
    void testLotteryButtonDefault() {
        assertFalse(event.getLotteryButton()); // Ensure default is false
    }
}
