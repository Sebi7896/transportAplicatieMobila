package com.sebastian.licentafrontendtransport.Map;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;

import android.content.Intent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sebastian.licentafrontendtransport.Map.model.Location;
import com.sebastian.licentafrontendtransport.Map.model.LocationOption;
import com.sebastian.licentafrontendtransport.Map.model.Route;
import com.sebastian.licentafrontendtransport.R;

import java.util.Comparator;
import java.util.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddRouteActivity extends AppCompatActivity {

    private ImageButton closeButton;
    private ImageButton filterButton;
    private FloatingActionButton swapLocations;


    //for launching the route
    private ActivityResultLauncher<Intent> originLauncher;
    private ActivityResultLauncher<Intent> destinationLauncher;
    MaterialButton btnSaveDestination;

    private TextInputEditText etOrigin;
    private TextInputLayout tilOrigin;

    private TextInputLayout tilDestination;
    private TextInputEditText etDestination;
    private FusedLocationProviderClient fusedLocationClient;

    //Locations
    private LocationOption origin;
    private LocationOption destination;

    //RecyclerView
    private RecyclerView routesRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_route);

        initViews();
        if (getIntent().hasExtra("name")) {
            etDestination.setText(getIntent().getStringExtra("name"));
            //sterge dupa testare
            destination = new LocationOption(
                    getIntent().getStringExtra("name"),
                    getIntent().getDoubleExtra("latitude", -1),
                    getIntent().getDoubleExtra("longitude", -1)
            );
        }

        setupListeners();
        originLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String address = data.getStringExtra("address");
                        double lat = data.getDoubleExtra("latitude", 0);
                        double lng = data.getDoubleExtra("longitude", 0);
                        origin = new LocationOption(address, lat, lng);
                        etOrigin.setText(address);
                        calculateRoutte();
                    }
                }
            }
        );
        destinationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String address = data.getStringExtra("address");
                        double lat = data.getDoubleExtra("latitude", 0);
                        double lng = data.getDoubleExtra("longitude", 0);
                        destination = new LocationOption(address, lat, lng);
                        etDestination.setText(address);
                        calculateRoutte();
                    }
                }
            }
        );
        fetchUserLocation();

    }

    private void initViews() {
        closeButton    = findViewById(R.id.close_button);
        filterButton   = findViewById(R.id.btn_filter);
        swapLocations  = findViewById(R.id.swap_locations);
        etOrigin       = findViewById(R.id.et_origin);
        tilOrigin      = findViewById(R.id.til_origin);
        etDestination  = findViewById(R.id.et_destination);
        tilDestination = findViewById(R.id.til_destination);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        routesRecyclerView = findViewById(R.id.routesRecyclerView);
        btnSaveDestination = findViewById(R.id.btn_save_destination);
    }

    private void setupListeners() {
        // Close the activity when X is tapped
        closeButton.setOnClickListener(v -> finish());
        btnSaveDestination.setOnClickListener(v -> {
            Toast.makeText(this, "Saving destination…", Toast.LENGTH_SHORT).show();
            if(destination == null) {
                Toast.makeText(this, "You must select a destination first", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = getIntent();
                intent.putExtra("name", destination.getName().split(",")[0]);
                intent.putExtra("latitude", destination.getLatitude());
                intent.putExtra("longitude", destination.getLongitude());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // Swap the two text-fields
        swapLocations.setOnClickListener(v -> {
            if(origin == null || destination == null)
                return;
            String o = Objects.requireNonNull(etOrigin.getText()).toString();
            etOrigin.setText(Objects.requireNonNull(etDestination.getText()).toString());
            etDestination.setText(o);
            LocationOption temp = origin;
            origin = destination;
            destination = temp;
            calculateRoutte();
        });


        etOrigin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPlaceActivity.class);
            originLauncher.launch(intent);
            Toast.makeText(this, "Launching map…", Toast.LENGTH_SHORT).show();
        });
        etDestination.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPlaceActivity.class);
            destinationLauncher.launch(intent);
        });


    }
    private void fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            new Thread(() -> {
                                Geocoder geocoder = new Geocoder(AddRouteActivity.this, java.util.Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    String addressLine = (addresses != null && !addresses.isEmpty())
                                        ? addresses.get(0).getAddressLine(0)
                                        : "Unknown location";
                                    runOnUiThread(() -> {
                                        origin = new LocationOption(addressLine, location.getLatitude(), location.getLongitude());
                                        etOrigin.setText(origin.getName());
                                        if (destination != null) {
                                            Toast.makeText(AddRouteActivity.this, "Calculating route for current!", Toast.LENGTH_SHORT).show();
                                            calculateRoutte();
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else {
                            Toast.makeText(this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(this, e ->
                            Toast.makeText(this, "Error fetching location: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
            return;
        }
        Toast.makeText(this, "You not granted location permission!", Toast.LENGTH_SHORT).show();
    }


private void calculateRoutte() {
    if (origin == null || destination == null) {
        Toast.makeText(this, "Select both origin and destination first", Toast.LENGTH_SHORT).show();
        return;
    }
    String originParam = origin.getLatitude() + "," + origin.getLongitude();
    String destinationParam = destination.getLatitude() + "," + destination.getLongitude();
    String apiKey = getString(R.string.google_maps_id);
    // First try subway-only
    String urlSubway = "https://maps.googleapis.com/maps/api/directions/json"
            + "?origin=" + originParam
            + "&destination=" + destinationParam
            + "&mode=transit"
            + "&transit_mode=subway"
            + "&alternatives=true"
            + "&departure_time=now"
            + "&transit_routing_preference=less_walking"
            + "&key=" + apiKey;
    new DirectionsFetchTask(this).fetchRoutes(urlSubway, subwayRoutes -> runOnUiThread(() -> {
        if (subwayRoutes.isEmpty()) {
            // No subway-only routes, fetch with all transit modes
            Toast.makeText(this, "No subway-only routes found, showing all transit options", Toast.LENGTH_SHORT).show();
            String urlAll = "https://maps.googleapis.com/maps/api/directions/json"
                    + "?origin=" + originParam
                    + "&destination=" + destinationParam
                    + "&mode=transit"
                    + "&transit_mode=subway|bus|tram"
                    + "&alternatives=true"
                    + "&departure_time=now"
                    + "&transit_routing_preference=less_walking"
                    + "&key=" + apiKey;
            new DirectionsFetchTask(this).fetchRoutes(urlAll, allRoutes -> runOnUiThread(() -> setupRoutesList(subwayRoutes)));
        } else {
            setupRoutesList(subwayRoutes);
        }
    }));
}
    private void setupRoutesList(List<Route> routes) {
        List<Route> sortedRoutes = routes.stream().sorted((Comparator.comparing(route -> route.legs.get(0).arrival_time.text))).collect(Collectors.toList());
        RoutesAdapter adapter = new RoutesAdapter(AddRouteActivity.this, sortedRoutes, route -> {
            Intent intent = new Intent(this, RouteDetailActivity.class);
            intent.putExtra("route", route);
            startActivity(intent);
        });
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routesRecyclerView.setAdapter(adapter);

    }


}