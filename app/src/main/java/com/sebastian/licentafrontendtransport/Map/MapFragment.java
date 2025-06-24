package com.sebastian.licentafrontendtransport.Map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.sebastian.licentafrontendtransport.R;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private ActivityResultLauncher<Intent> routeLauncher;

    private MaterialButton searchJourney;
    private RecyclerView shortcutRecyclerView;
    private SharedPreferences sharedPreferences;
    private List<ShortcutLocation> shortcuts;
    private ShortcutLocationAdapter shortcutAdapter;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Register permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                this::onLocationPermissionResult
        );
        routeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    requireActivity();
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String name = data.getStringExtra("name");
                        double lat = data.getDoubleExtra("latitude", -1);
                        double lng = data.getDoubleExtra("longitude", -1);
                        if(Objects.equals(name, "Add")) {
                            return;
                        }
                        LatLng latLng = new LatLng(lat, lng);
                        //avem o locatie
                        assert name != null;
                        if (shortcuts.stream().map(ShortcutLocation::getName).noneMatch(name::equals)) {
                            // Insert new shortcut just before the Add+ item
                            int insertIndex = shortcuts.size() - 1;
                            shortcuts.add(insertIndex, new ShortcutLocation(name, latLng));
                            shortcutAdapter.notifyItemInserted(insertIndex);
                        } else {
                            // Update existing
                            for (int i = 0; i < shortcuts.size(); i++) {
                                if (shortcuts.get(i).getName().equals(name)) {
                                    shortcuts.set(i, new ShortcutLocation(name, latLng));
                                    shortcutAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                        // In all cases, save or overwrite the single entry for this shortcut
                        sharedPreferences.edit()
                                .putString(name, lat + "," + lng)
                                .apply();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup map fragment
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFrag != null) {
            mapFrag.getMapAsync(this);
        }

        // Initialize search button
        searchJourney = view.findViewById(R.id.search_journey);
        searchJourney.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddRouteActivity.class);
            routeLauncher.launch(intent);
        });

        // Initialize shortcut RecyclerView
        shortcutRecyclerView = view.findViewById(R.id.shortcut_recycler_view);
        sharedPreferences = requireActivity().getSharedPreferences("location-shortcuts", Context.MODE_PRIVATE);
        shortcuts = initShortcutViews(sharedPreferences);
        LinearLayoutManager lm = new LinearLayoutManager(requireActivity());
        lm.setOrientation(LinearLayoutManager.HORIZONTAL);
        shortcutRecyclerView.setLayoutManager(lm);
        shortcutAdapter = new ShortcutLocationAdapter(requireActivity(), shortcuts, shortcut -> {
            if (shortcut.latLng == null) {
                Intent intent = new Intent(requireActivity(), AddMainLocationActivity.class);
                intent.putExtra("alias", shortcut.name);
                routeLauncher.launch(intent);
                return;
            }
            //Start the route
            Intent intent = new Intent(requireActivity(), AddRouteActivity.class);
            intent.putExtra("name", shortcut.name);
            intent.putExtra("latitude", shortcut.latLng.latitude);
            intent.putExtra("longitude", shortcut.latLng.longitude);
            routeLauncher.launch(intent);
        });
        shortcutRecyclerView.setAdapter(shortcutAdapter);

        shortcutRecyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            private final android.view.GestureDetector gestureDetector = new android.view.GestureDetector(requireContext(),
                new android.view.GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public void onLongPress(android.view.MotionEvent e) {
                        View child = shortcutRecyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (child != null) {
                            int position = shortcutRecyclerView.getChildAdapterPosition(child);
                            ShortcutLocation selected = shortcuts.get(position);
                            new AlertDialog.Builder(requireContext())
                                .setTitle(selected.getName())
                                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                                    if (which == 0) {
                                        // Edit shortcut: launch AddMainLocationActivity to edit alias/location
                                        Intent editIntent = new Intent(requireActivity(), AddMainLocationActivity.class);
                                        editIntent.putExtra("alias", selected.getName());
                                        routeLauncher.launch(editIntent);
                                    } else {
                                        // Prevent deletion of default shortcuts
                                        if ("Home".equals(selected.getName()) || "Work".equals(selected.getName()) || "Add".equals(selected.getName())) {
                                            Toast.makeText(requireContext(), "Cannot delete " + selected.getName(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Delete shortcut
                                            shortcuts.remove(position);
                                            shortcutAdapter.notifyItemRemoved(position);
                                            sharedPreferences.edit().remove(selected.getName()).apply();
                                        }
                                    }
                                })
                                .show();
                        }
                    }
                });

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                gestureDetector.onTouchEvent(e);
                return false;
            }
        });
    }

    private List<ShortcutLocation> initShortcutViews(SharedPreferences sharedPreferences) {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        if (allEntries.isEmpty()) {
            defaultShortcuts(sharedPreferences);
            allEntries = sharedPreferences.getAll();
        }

        List<ShortcutLocation> orderedShortcuts = new ArrayList<>();

        // Add  Work first
        if (allEntries.containsKey("Work")) {
            String value = (String) allEntries.get("Work");
            LatLng workLatLng = null;
            if (value != null && !value.isEmpty()) {
                String[] parts = value.split(",");
                workLatLng = new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            }
            orderedShortcuts.add(new ShortcutLocation("Work", workLatLng));
        }

        // Add Home next
        if (allEntries.containsKey("Home")) {
            String value = (String) allEntries.get("Home");
            LatLng homeLatLng = null;
            if (value != null && !value.isEmpty()) {
                String[] parts = value.split(",");
                homeLatLng = new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            }
            orderedShortcuts.add(new ShortcutLocation("Home", homeLatLng));
        }

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String name = entry.getKey();
            if ("Work".equals(name) || "Home".equals(name)) continue;
            String value = (String) entry.getValue();
            LatLng latLng = null;
            if (value != null && !value.isEmpty()) {
                String[] parts = value.split(",");
                latLng = new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            }
            // Skip the Add button placeholder if present
            if ("Add".equals(name)) continue;
            orderedShortcuts.add(new ShortcutLocation(name, latLng));
        }

        // Finally, add the "Add" button
        orderedShortcuts.add(new ShortcutLocation("Add", null));
        return orderedShortcuts;
    }

    private void defaultShortcuts(SharedPreferences sharedPreferences) {
        sharedPreferences.edit()
            .putString("Home", "")
            .putString("Work", "")
            .apply();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableAndMoveToCurrentLocation();
        } else {
            // No permission -> show Bucharest
            LatLng bucharest = new LatLng(44.4268, 26.1025);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bucharest, 12f));
            // Request permission
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void onLocationPermissionResult(boolean granted) {
        if (granted && googleMap != null) {
            enableAndMoveToCurrentLocation();
        }
    }

    private void enableAndMoveToCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            // Move camera to last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), (Location location) -> {
                if (location != null) {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                }
            });
        }
    }

}