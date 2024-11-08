package com.example.appify;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appify.Activities.EntrantEnlistActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;


public class QRScanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_qrscan);

        // Start the QR code scanner
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                handleQRCode(result.getContents());
            } else {
                Toast.makeText(this, "No QR code found", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleQRCode(String qrContent) {
        // Check if the QR code content matches the activity identifier
        if ("OPEN_ENTRANT_ENLIST_ACTIVITY".equals(qrContent)) {
            // Launch the target activity
            Intent intent = new Intent(this, EntrantEnlistActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Unrecognized QR code", Toast.LENGTH_SHORT).show();
        }
    }
}
