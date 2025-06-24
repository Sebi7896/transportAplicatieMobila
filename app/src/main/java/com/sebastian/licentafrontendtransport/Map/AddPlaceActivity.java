package com.sebastian.licentafrontendtransport.Map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentCharge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;

public class AddPlaceActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;

    private ImageButton closeBtn;

    // ← Change this from TextInputEditText to AutoCompleteTextView:
    private AutoCompleteTextView originField;

    // Places autocomplete support:
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private ArrayAdapter<String> originAdapter;
    private final List<AutocompletePrediction> originSuggestions = new ArrayList<>();
    private View locateOnMapItem;
    private ActivityResultLauncher<Intent> mapLauncher;
    // View references
    private View currentLocItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_place);

        mapLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String address = data.getStringExtra("address");
                    double lat = data.getDoubleExtra("latitude", 0);
                    double lng = data.getDoubleExtra("longitude", 0);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("address", address);
                    resultIntent.putExtra("latitude", lat);
                    resultIntent.putExtra("longitude", lng);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        );

        initViews();
        setupCloseButton();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initPlaces();
        setupCurrentLocationButton();
        setupMapButton();
    }

    private void initPlaces() {
        // Initialize the SDK if needed
        if (!Places.isInitialized()) {
            Places.initialize(
                    getApplicationContext(),
                    getString(R.string.google_maps_id),
                    Locale.getDefault()
            );
        }
        placesClient  = Places.createClient(this);
        sessionToken  = AutocompleteSessionToken.newInstance();

        // 1) Prepare the dropdown adapter
        originAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );
        originField.setAdapter(originAdapter);
        originField.setThreshold(1);

        // 2) Define Bucharest bounds
        RectangularBounds bucharestBounds = RectangularBounds.newInstance(
                new LatLng(44.28, 25.92),
                new LatLng(44.58, 26.26)
        );

        // 3) Listen to text changes
        originField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void afterTextChanged(Editable s){}
            @Override
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) return;

                FindAutocompletePredictionsRequest req = FindAutocompletePredictionsRequest.builder()
                        .setLocationRestriction(bucharestBounds)
                        .setSessionToken(sessionToken)
                        .setQuery(query)
                        .build();

                placesClient.findAutocompletePredictions(req)
                        .addOnSuccessListener(response -> {
                            originSuggestions.clear();
                            originAdapter.clear();
                            for (AutocompletePrediction p : response.getAutocompletePredictions()) {
                                originSuggestions.add(p);
                                originAdapter.add(p.getPrimaryText(null).toString());
                            }
                            originAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(AddPlaceActivity.this,
                                        "Prediction error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
            }
        });
        originField.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction sel = originSuggestions.get(position);
            List<Place.Field> fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.DISPLAY_NAME,
                    Place.Field.FORMATTED_ADDRESS,
                    Place.Field.LOCATION
            );
            FetchPlaceRequest fetch = com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(sel.getPlaceId(), fields)
                    .build();

            placesClient.fetchPlace(fetch)
                    .addOnSuccessListener(res -> {
                        Place place = res.getPlace();
                        Intent result = new Intent();
                        result.putExtra("address",   place.getFormattedAddress());
                        result.putExtra("latitude",  Objects.requireNonNull(place.getLocation()).latitude);
                        result.putExtra("longitude", Objects.requireNonNull(place.getLocation()).longitude);
                        setResult(RESULT_OK, result);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(AddPlaceActivity.this,
                                    "Fetch place error: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }
    private void initViews() {
        closeBtn = findViewById(R.id.close_button);
        originField = findViewById(R.id.text_input_origin);
        currentLocItem = findViewById(R.id.current_location_item);
        locateOnMapItem = findViewById(R.id.locate_on_map_item);
    }
    private void setupCloseButton() {
        closeBtn.setOnClickListener(v -> finish());
    }
    private void setupCurrentLocationButton() {
        currentLocItem.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, loc -> {
                            if (loc != null) {
                                double lat = loc.getLatitude();
                                double lng = loc.getLongitude();
                                new Thread(() -> {
                                    Geocoder geocoder = new Geocoder(AddPlaceActivity.this, Locale.getDefault());
                                    String addressLine = "Unknown location";
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                                        if (addresses != null && !addresses.isEmpty()) {
                                            addressLine = addresses.get(0).getAddressLine(0);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String finalAddress = addressLine;
                                    runOnUiThread(() -> {
                                        Intent result = new Intent();
                                        result.putExtra("address",   finalAddress);
                                        result.putExtra("latitude",  lat);
                                        result.putExtra("longitude", lng);
                                        setResult(RESULT_OK, result);
                                        finish();
                                    });
                                }).start();
                            } else {
                                Toast.makeText(this,
                                        "Unable to get location. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(this, e ->
                                Toast.makeText(this,
                                        "Error fetching location: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
            } else {
                Toast.makeText(this,
                        "Location permission not granted!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupMapButton() {
        locateOnMapItem.setOnClickListener(v -> {
            Intent intent = new Intent(this, PointMapActivity.class);
            mapLauncher.launch(intent);
        });
    }
}