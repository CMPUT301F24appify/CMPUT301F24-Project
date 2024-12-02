package com.example.appify;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.appify.Model.Event;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

class EventTest {

    private FirebaseFirestore mockFirestore;
    private CollectionReference mockEventsCollection;
    private CollectionReference mockFacilitiesCollection;
    private DocumentReference mockEventDocument;
    private DocumentReference mockFacilityDocument;

    private Event event;

    @BeforeEach
    void setUp() {
        // Mock Firestore and its collections/documents
        mockFirestore = mock(FirebaseFirestore.class);
        mockEventsCollection = mock(CollectionReference.class);
        mockFacilitiesCollection = mock(CollectionReference.class);
        mockEventDocument = mock(DocumentReference.class);
        mockFacilityDocument = mock(DocumentReference.class);

        // When Firestore's collection("events") is called, return mockEventsCollection
        when(mockFirestore.collection("events")).thenReturn(mockEventsCollection);
        when(mockFirestore.collection("facilities")).thenReturn(mockFacilitiesCollection);

        // When collection("events").document(eventId) is called, return mockEventDocument
        when(mockEventsCollection.document(any(String.class))).thenReturn(mockEventDocument);

        // When collection("facilities").document(facilityId) is called, return mockFacilityDocument
        when(mockFacilitiesCollection.document(any(String.class))).thenReturn(mockFacilityDocument);

        // Initialize an Event instance with mocked Firestore
        String eventId = UUID.randomUUID().toString();
        event = new Event(
                "Sample Event",
                "2024-12-31",
                "Sample Facility",
                "2024-12-01",
                "This is a sample event.",
                100,
                50,
                "https://example.com/poster.jpg",
                true,
                true,
                true,
                true,
                true,
                "You are waitlisted for the event.",
                "You are enrolled in the event.",
                "Your enrollment was cancelled.",
                "You are invited to the event.",
                "organizer123"
        );
        event.setEventId(eventId);
        event.setDb(mockFirestore);
    }

    // Constructor Tests

    @Test
    void testFullConstructor() {
        String eventId = event.getEventId();
        assertEquals("Sample Event", event.getName());
        assertEquals("2024-12-31", event.getDate());
        assertEquals("Sample Facility", event.getFacility());
        assertEquals("2024-12-01", event.getRegistrationEndDate());
        assertEquals("This is a sample event.", event.getDescription());
        assertEquals(100, event.getMaxWaitEntrants());
        assertEquals(50, event.getMaxSampleEntrants());
        assertEquals("https://example.com/poster.jpg", event.getPosterUri());
        assertTrue(event.isGeolocate());
        assertTrue(event.isNotifyWaitlisted());
        assertTrue(event.isNotifyEnrolled());
        assertTrue(event.isNotifyCancelled());
        assertTrue(event.isNotifyInvited());
        assertEquals("You are waitlisted for the event.", event.getWaitlistedMessage());
        assertEquals("You are enrolled in the event.", event.getEnrolledMessage());
        assertEquals("Your enrollment was cancelled.", event.getCancelledMessage());
        assertEquals("You are invited to the event.", event.getInvitedMessage());
        assertEquals("organizer123", event.getOrganizerID());
        assertFalse(event.getLotteryRanFlag());
        assertFalse(event.getLotteryButton());
    }

    @Test
    void testAdminConstructor() {
        Event adminEvent = new Event(
                "Admin Event",
                "2024-11-30",
                "Admin Facility",
                "2024-11-15",
                200,
                100,
                "organizer456"
        );

        assertEquals("Admin Event", adminEvent.getName());
        assertEquals("2024-11-30", adminEvent.getDate());
        assertEquals("Admin Facility", adminEvent.getFacility());
        assertEquals("2024-11-15", adminEvent.getRegistrationEndDate());
        assertEquals(200, adminEvent.getMaxWaitEntrants());
        assertEquals(100, adminEvent.getMaxSampleEntrants());
        assertEquals("organizer456", adminEvent.getOrganizerID());
        // Other fields should be default
        assertNull(adminEvent.getDescription());
        assertNull(adminEvent.getPosterUri());
        assertFalse(adminEvent.isGeolocate());
        assertNull(adminEvent.getEventId());
        assertFalse(adminEvent.isNotifyWaitlisted());
        assertFalse(adminEvent.isNotifyEnrolled());
        assertFalse(adminEvent.isNotifyCancelled());
        assertFalse(adminEvent.isNotifyInvited());
        assertNull(adminEvent.getWaitlistedMessage());
        assertNull(adminEvent.getEnrolledMessage());
        assertNull(adminEvent.getCancelledMessage());
        assertNull(adminEvent.getInvitedMessage());
        assertFalse(adminEvent.getLotteryRanFlag());
        assertFalse(adminEvent.getLotteryButton());
    }

    @Test
    void testNoArgConstructor() {
        Event noArgEvent = new Event();
        assertNull(noArgEvent.getName());
        assertNull(noArgEvent.getDate());
        assertNull(noArgEvent.getFacility());
        assertNull(noArgEvent.getRegistrationEndDate());
        assertNull(noArgEvent.getDescription());
        assertEquals(0, noArgEvent.getMaxWaitEntrants());
        assertEquals(0, noArgEvent.getMaxSampleEntrants());
        assertNull(noArgEvent.getPosterUri());
        assertFalse(noArgEvent.isGeolocate());
        assertNull(noArgEvent.getEventId());
        assertFalse(noArgEvent.isNotifyWaitlisted());
        assertFalse(noArgEvent.isNotifyEnrolled());
        assertFalse(noArgEvent.isNotifyCancelled());
        assertFalse(noArgEvent.isNotifyInvited());
        assertNull(noArgEvent.getWaitlistedMessage());
        assertNull(noArgEvent.getEnrolledMessage());
        assertNull(noArgEvent.getCancelledMessage());
        assertNull(noArgEvent.getInvitedMessage());
        assertFalse(noArgEvent.getLotteryRanFlag());
        assertFalse(noArgEvent.getLotteryButton());
    }

    // Getter and Setter Tests

    @Test
    void testGetAndSetName() {
        assertEquals("Sample Event", event.getName());
        event.setName("Updated Event");
        assertEquals("Updated Event", event.getName());
    }

    @Test
    void testGetAndSetDate() {
        assertEquals("2024-12-31", event.getDate());
        event.setDate("2025-01-01");
        assertEquals("2025-01-01", event.getDate());
    }

    @Test
    void testGetAndSetFacility() {
        assertEquals("Sample Facility", event.getFacility());
        event.setFacility("Updated Facility");
        assertEquals("Updated Facility", event.getFacility());
    }

    @Test
    void testGetAndSetRegistrationEndDate() {
        assertEquals("2024-12-01", event.getRegistrationEndDate());
        event.setRegistrationEndDate("2024-12-15");
        assertEquals("2024-12-15", event.getRegistrationEndDate());
    }

    @Test
    void testGetAndSetDescription() {
        assertEquals("This is a sample event.", event.getDescription());
        event.setDescription("Updated description.");
        assertEquals("Updated description.", event.getDescription());
    }

    @Test
    void testGetAndSetMaxWaitEntrants() {
        assertEquals(100, event.getMaxWaitEntrants());
        event.setMaxWaitEntrants(150);
        assertEquals(150, event.getMaxWaitEntrants());
    }

    @Test
    void testGetAndSetMaxSampleEntrants() {
        assertEquals(50, event.getMaxSampleEntrants());
        event.setMaxSampleEntrants(75);
        assertEquals(75, event.getMaxSampleEntrants());
    }

    @Test
    void testGetAndSetPosterUri() {
        assertEquals("https://example.com/poster.jpg", event.getPosterUri());
        event.setPosterUri("https://example.com/newposter.jpg");
        assertEquals("https://example.com/newposter.jpg", event.getPosterUri());
    }

    @Test
    void testGetAndSetIsGeolocate() {
        assertTrue(event.isGeolocate());
        event.setGeolocate(false);
        assertFalse(event.isGeolocate());
    }

    @Test
    void testGetAndSetOrganizerID() {
        assertEquals("organizer123", event.getOrganizerID());
        event.setOrganizerID("organizer456");
        assertEquals("organizer456", event.getOrganizerID());
    }

    @Test
    void testGetAndSetNotifyWaitlisted() {
        assertTrue(event.isNotifyWaitlisted());
        event.setNotifyWaitlisted(false);
        assertFalse(event.isNotifyWaitlisted());
    }

    @Test
    void testGetAndSetNotifyEnrolled() {
        assertTrue(event.isNotifyEnrolled());
        event.setNotifyEnrolled(false);
        assertFalse(event.isNotifyEnrolled());
    }

    @Test
    void testGetAndSetNotifyCancelled() {
        assertTrue(event.isNotifyCancelled());
        event.setNotifyCancelled(false);
        assertFalse(event.isNotifyCancelled());
    }

    @Test
    void testGetAndSetNotifyInvited() {
        assertTrue(event.isNotifyInvited());
        event.setNotifyInvited(false);
        assertFalse(event.isNotifyInvited());
    }

    @Test
    void testGetAndSetWaitlistedMessage() {
        assertEquals("You are waitlisted for the event.", event.getWaitlistedMessage());
        event.setWaitlistedMessage("New waitlist message.");
        assertEquals("New waitlist message.", event.getWaitlistedMessage());
    }

    @Test
    void testGetAndSetEnrolledMessage() {
        assertEquals("You are enrolled in the event.", event.getEnrolledMessage());
        event.setEnrolledMessage("New enrolled message.");
        assertEquals("New enrolled message.", event.getEnrolledMessage());
    }

    @Test
    void testGetAndSetCancelledMessage() {
        assertEquals("Your enrollment was cancelled.", event.getCancelledMessage());
        event.setCancelledMessage("New cancelled message.");
        assertEquals("New cancelled message.", event.getCancelledMessage());
    }

    @Test
    void testGetAndSetInvitedMessage() {
        assertEquals("You are invited to the event.", event.getInvitedMessage());
        event.setInvitedMessage("New invited message.");
        assertEquals("New invited message.", event.getInvitedMessage());
    }

    @Test
    void testGetAndSetLotteryRanFlag() {
        assertFalse(event.getLotteryRanFlag());
        event.setLotteryRanFlag(true);
        assertTrue(event.getLotteryRanFlag());
    }

    @Test
    void testGetAndSetLotteryButton() {
        assertFalse(event.getLotteryButton());
        event.setLotteryButton(true);
        assertTrue(event.getLotteryButton());
    }
}