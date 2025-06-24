package com.sebastian.licentafrontendtransport.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import android.widget.Toast;
import android.widget.ImageButton;
import android.content.Intent;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sebastian.licentafrontendtransport.R;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.UiSettings;

public class PointMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Marker selectedMarker;
    private MaterialButton selectBtn;
    private FusedLocationProviderClient fusedLocationClient;
    private FloatingActionButton myLocationFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_point_map);

        // Initialize location client and My Location FAB
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        myLocationFab = findViewById(R.id.btn_my_location);
        myLocationFab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(loc -> {
                        if (loc != null && googleMap != null) {
                            LatLng myLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 15f));
                        }
                    });
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
            }
        });

        // Bind header buttons
        ImageButton closeBtn = findViewById(R.id.close_button);
        closeBtn.setOnClickListener(v -> finish());

        selectBtn = findViewById(R.id.select_button);
        selectBtn.setEnabled(false);
        selectBtn.setAlpha(0.5f);
        selectBtn.setOnClickListener(v -> {
            if (googleMap != null && selectedMarker != null) {
                LatLng pos = selectedMarker.getPosition();
                String addressText = "";
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                //extragem data de la markerul selectat
                try {
                    List<Address> addresses = geocoder.getFromLocation(pos.latitude, pos.longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        addressText = addr.getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent result = new Intent();
                result.putExtra("latitude", pos.latitude);
                result.putExtra("longitude", pos.longitude);
                result.putExtra("address", addressText);
                setResult(RESULT_OK, result);
                finish();
            } else {
                Toast.makeText(this,
                    "Please tap on the map to select a location",
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize map fragment
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map_fragment);
        assert mapFrag != null;
        mapFrag.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        // Enable blue dot for current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            UiSettings ui = googleMap.getUiSettings();
            ui.setMyLocationButtonEnabled(false); // hide default button if using custom FAB
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
        }
        // Move camera to Bucharest
        LatLng bucharest = new LatLng(44.439663, 26.096306);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bucharest, 12f));
        // Let user tap map to place a marker
        googleMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) selectedMarker.remove();
            selectedMarker = googleMap.addMarker(
                new MarkerOptions().position(latLng)
            );
            selectBtn.setEnabled(true);
            selectBtn.setAlpha(1f);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            myLocationFab.performClick();
        }
    }
}