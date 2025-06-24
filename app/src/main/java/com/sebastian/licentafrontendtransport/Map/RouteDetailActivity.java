package com.sebastian.licentafrontendtransport.Map;


import android.os.Bundle;
import android.graphics.Color;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import com.google.maps.android.PolyUtil;
import com.sebastian.licentafrontendtransport.Map.model.Leg;
import com.sebastian.licentafrontendtransport.Map.model.Route;
import com.sebastian.licentafrontendtransport.Map.model.Step;
import com.sebastian.licentafrontendtransport.R;

import java.util.ArrayList;
import java.util.List;

public class RouteDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Route route;
    private RecyclerView recyclerSteps;
    private TextView tvRouteData;
    private List<Integer> paletteColors;

    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
    private MaterialButton fabMyLocation;
    private MaterialButton fabClose;
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_route_detail);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fabMyLocation = findViewById(R.id.fab_my_location);
        fabMyLocation.setOnClickListener(v -> centerOnUserLocation());
        fabClose = findViewById(R.id.fab_close);
        fabClose.setOnClickListener(v -> finish());

        // Load palette of colors for route legs
        paletteColors = getColors();

        // 1. Extract Route from Intent
        route = (Route) getIntent().getSerializableExtra("route");
        // or Parcelable, depending on your model

        // 2. Initialize TextView for route summary
        tvRouteData = findViewById(R.id.tv_route_details); // match your XML ID
        if (route != null && route.legs != null && !route.legs.isEmpty()) {
            Leg firstLeg = route.legs.get(0);
            // Example: show duration text and departure/arrival times
            String durationText = firstLeg.duration != null ? firstLeg.duration.text : "";
            String departureTime = firstLeg.departure_time != null ? firstLeg.departure_time.text : "";
            String arrivalTime = firstLeg.arrival_time != null ? firstLeg.arrival_time.text : "";
            tvRouteData.setText(String.format("%s : %s - %s", durationText, departureTime, arrivalTime));
        } else {
            tvRouteData.setText(""); // or hide if null
        }

        // 3. Initialize RecyclerView for steps
        recyclerSteps = findViewById(R.id.recycler_steps);
        recyclerSteps.setLayoutManager(new LinearLayoutManager(this));
        if (route != null && route.legs != null) {
            // Flatten all steps from all legs into one list
            List<Step> allSteps = new ArrayList<>();
            for (Step step : route.legs.get(0).steps) {
                if (step != null) {
                    allSteps.add(step);
                }
            }
            StepsAdapter stepsAdapter = new StepsAdapter(allSteps, paletteColors);
            recyclerSteps.setAdapter(stepsAdapter);
        }

        // 4. Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {}
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
        }

        if (route == null || route.legs == null || route.legs.isEmpty()) {
            return;
        }

        // 1. Determine start and end LatLng
        LatLng startLatLng = null;
        LatLng endLatLng = null;

        // If your Route has an overview polyline at top-level:
        String overviewPolyline = route.overview_polyline != null ? route.overview_polyline.points : null;

        // Otherwise, you can take start of first leg and end of last leg:
        try {
            Leg firstLeg = route.legs.get(0);
            if (firstLeg.start_location != null) {
                double lat = firstLeg.start_location.lat;
                double lng = firstLeg.start_location.lng;
                startLatLng = new LatLng(lat, lng);
            }
        } catch (Exception ignored) {}

        try {
            Leg lastLeg = route.legs.get(route.legs.size() - 1);
            if (lastLeg.end_location != null) {
                double lat = lastLeg.end_location.lat;
                double lng = lastLeg.end_location.lng;
                endLatLng = new LatLng(lat, lng);
            }
        } catch (Exception ignored) {}

        // 2. Add markers
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        if (startLatLng != null) {
            googleMap.addMarker(new MarkerOptions().position(startLatLng).title("Start"));
            boundsBuilder.include(startLatLng);
        }
        if (endLatLng != null) {
            googleMap.addMarker(new MarkerOptions().position(endLatLng).title("End"));
            boundsBuilder.include(endLatLng);
        }

        // Draw each step polyline with styling
        drawPerStep(googleMap, boundsBuilder);

        // 4. Adjust camera to fit entire route (with padding)
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = (int)(50 * getResources().getDisplayMetrics().density); // e.g., 50dp padding
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            // If boundsBuilder has only one point or invalid, fallback:
            if (startLatLng != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 13f));
            }
        }

        // 5. (Optional) Customize map UI settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        // etc.
    }

    private void drawPerStep(GoogleMap googleMap, LatLngBounds.Builder boundsBuilder) {
        if (route == null || route.legs.get(0) == null) return;
        for (int i = 0; i < route.legs.get(0).steps.size(); i++) {
            Step step = route.legs.get(0).steps.get(i);
            if (step.polyline != null) {
                List<LatLng> pts = PolyUtil.decode(step.polyline.points);
                if (!pts.isEmpty()) {
                    PolylineOptions po = new PolylineOptions().addAll(pts).width(12f);
                    int color = paletteColors.get(i % paletteColors.size());
                    po.color(color);
                    googleMap.addPolyline(po);
                    for (LatLng p: pts) boundsBuilder.include(p);
                }
            }
            // Marker at start of this leg
            if (route.legs.get(0).start_location != null) {
                LatLng legStart = new LatLng(route.legs.get(0).start_location.lat, route.legs.get(0).start_location.lng);
                googleMap.addMarker(new MarkerOptions()
                        .position(legStart)
                        .title("Leg " + (i + 1) + " Start"));
                boundsBuilder.include(legStart);
            }
            // Marker at end of this leg
            if (route.legs.get(0).end_location != null) {
                LatLng legEnd = new LatLng(route.legs.get(0).end_location.lat, route.legs.get(0).end_location.lng);
                googleMap.addMarker(new MarkerOptions()
                        .position(legEnd)
                        .title("Leg " + (i + 1) + " End"));
                boundsBuilder.include(legEnd);
            }
        }
    }

    private void centerOnUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
            return;
        }
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null && mMap != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(userLatLng)
                            .zoom(15f)
                            .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException ignored) {}
                }
            }
        }
    }

    /**
     * Load color palette from resources (string-array leg_color_hex) and parse into integer colors.
     * If parsing fails or array is empty, returns a default single-color list.
     */
    private List<Integer> getColors() {
        List<Integer> colors = new ArrayList<>();
        try {
            String[] hexes = getResources().getStringArray(R.array.leg_color_hex);
            for (String hex : hexes) {
                try {
                    colors.add(Color.parseColor(hex));
                } catch (IllegalArgumentException e) {
                    // skip invalid color
                }
            }
        } catch (Exception e) {
            // resource not found or other error
        }
        if (colors.isEmpty()) {
            colors.add(Color.BLACK);
        }
        return colors;
    }

}