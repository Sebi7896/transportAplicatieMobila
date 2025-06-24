package com.sebastian.licentafrontendtransport.Map;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sebastian.licentafrontendtransport.R;

import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.maps.model.Marker;

// For address picker launcher
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.sebastian.licentafrontendtransport.Map.PointMapActivity;

import java.util.Objects;

// Added for location permission and camera centering
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.UiSettings;

public class AddMainLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageButton closeButton;
    private TextView titleText;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private TextInputEditText addressInput;
    private TextInputEditText aliasInput;
    private Button savePlaceButton;

    // For address picker launcher
    private ActivityResultLauncher<Intent> addressPickerLauncher;

    // Added for location permission and camera centering
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1003;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_main_location);
        EdgeToEdge.enable(this);

        initViews();
        // set up address picker launcher
        addressPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double lat = data.getDoubleExtra("latitude", 0);
                    double lng = data.getDoubleExtra("longitude", 0);
                    String addr = data.getStringExtra("address");
                    selectedLatLng = new LatLng(lat, lng);
                    addressInput.setText(addr);
                    // place a marker at the selected location
                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                            .position(selectedLatLng)
                            .title(addr));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 12f));
                    }
                }
            }
        );

        // launch map picker when tapping the address field
        addressInput.setOnClickListener(v -> {
            Intent pickerIntent = new Intent(this, AddPlaceActivity.class);
            addressPickerLauncher.launch(pickerIntent);
        });

        if(getIntent().hasExtra("alias")) {
            aliasInput.setText(getIntent().getStringExtra("alias"));
        }

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // close action
        closeButton.setOnClickListener(v -> finish());

        // initialize map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // save action
        savePlaceButton.setOnClickListener(v -> {
            Intent intent = getIntent();
            if (selectedLatLng == null) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            intent.putExtra("name", Objects.requireNonNull(aliasInput.getText()).toString());
            intent.putExtra("latitude", selectedLatLng.latitude);
            intent.putExtra("longitude", selectedLatLng.longitude);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void initViews() {
        closeButton     = findViewById(R.id.close_button);
        addressInput    = findViewById(R.id.addressInput);
        aliasInput      = findViewById(R.id.aliasInput);
        savePlaceButton = findViewById(R.id.savePlaceButton);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float defaultZoom = 12f;
        // Check fine location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            // Default to Bucharest while waiting for permission
            LatLng bucharest = new LatLng(44.4268, 26.1025);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bucharest, defaultZoom));
            return;
        }
        // Permissions granted: enable location and center map
        mMap.setMyLocationEnabled(true);
        UiSettings ui = mMap.getUiSettings();
        ui.setMyLocationButtonEnabled(false);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, defaultZoom));
            } else {
                LatLng bucharest = new LatLng(44.4268, 26.1025);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bucharest, defaultZoom));
            }
        });
        // Marker on tap
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            // Add marker and keep reference
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected location"));
            selectedLatLng = latLng;
            // First set the marker title as the text
            if (marker != null) {
                addressInput.setText(marker.getTitle());
            }
            // Then reverse-geocode to get a human-readable address
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressLine = addresses.get(0).getAddressLine(0);
                    addressInput.setText(addressLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && mMap != null) {
            // Retry centering on user location
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 12f));
                }
            });
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            Toast.makeText(this,
                "Location permission is required to show your position on the map",
                Toast.LENGTH_SHORT).show();
        }
    }
}