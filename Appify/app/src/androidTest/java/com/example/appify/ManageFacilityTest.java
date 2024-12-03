package com.example.appify;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.appify.Model.Facility;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ManageFacilityTest {

    private FirebaseFirestore db;
    private String facilityID;
    private String organizerID = "test_organizer_id";  // Mock organizer ID for testing
    private Facility testFacility;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();

        // Initialize Facility with required parameters
        testFacility = new Facility("test_facility_id", "Test Facility", "1234 Test St", "test@example.com", "Description of the facility", 100, organizerID);
        facilityID = testFacility.getId();

        // Pre-populate Firebase with the test facility
        CollectionReference facilitiesRef = db.collection("facilities");
        facilitiesRef.document(facilityID).set(testFacility);
    }

    // US 02.01.03: Create Facility Profile
    @Test
    public void testCreateFacility() {
        // Attempt to retrieve the facility from Firestore
        db.collection("facilities").document(facilityID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Facility facilityFromDb = task.getResult().toObject(Facility.class);
                assertNotNull("Facility should be created", facilityFromDb);
                assertEquals("Facility name should match", "Test Facility", facilityFromDb.getName());
                assertEquals("Facility location should match", "1234 Test St", facilityFromDb.getLocation());
                assertEquals("Facility email should match", "test@example.com", facilityFromDb.getEmail());
                assertEquals("Facility description should match", "Description of the facility", facilityFromDb.getDescription());
                assertEquals("Facility capacity should match", 100L, (long) facilityFromDb.getCapacity());
            }
        });
    }

    // US 02.01.03: Update Facility Profile
    @Test
    public void testUpdateFacility() {
        // Update the facility's details
        testFacility.setName("Updated Facility");
        testFacility.setLocation("5678 Updated St");
        testFacility.setEmail("updated@example.com");
        testFacility.setDescription("Updated description of the facility");
        testFacility.setCapacity(150);

        db.collection("facilities").document(facilityID).set(testFacility).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Retrieve updated facility from Firestore
                db.collection("facilities").document(facilityID).get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && task2.getResult() != null) {
                        Facility updatedFacility = task2.getResult().toObject(Facility.class);
                        assertNotNull("Updated facility should be retrieved", updatedFacility);
                        assertEquals("Facility name should be updated", "Updated Facility", updatedFacility.getName());
                        assertEquals("Facility location should be updated", "5678 Updated St", updatedFacility.getLocation());
                        assertEquals("Facility email should be updated", "updated@example.com", updatedFacility.getEmail());
                        assertEquals("Facility description should be updated", "Updated description of the facility", updatedFacility.getDescription());
                        assertEquals("Facility capacity should be updated", 150L, (long) updatedFacility.getCapacity());
                    }
                });
            }
        });
    }

    // US 02.01.03: Delete Facility Profile
    @Test
    public void testDeleteFacility() {
        // Delete the facility
        db.collection("facilities").document(facilityID).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Try to retrieve the facility after deletion
                db.collection("facilities").document(facilityID).get().addOnCompleteListener(task2 -> {
                    assertTrue("Facility should be deleted", task2.getResult() == null || !task2.getResult().exists());
                });
            }
        });
    }
}