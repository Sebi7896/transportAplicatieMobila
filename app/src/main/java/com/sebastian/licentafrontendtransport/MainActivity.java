package com.sebastian.licentafrontendtransport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sebastian.licentafrontendtransport.Alerts.AlertsFragment;
import com.sebastian.licentafrontendtransport.Home.HomeFragment;
import com.sebastian.licentafrontendtransport.Lines.LineFragment;
import com.sebastian.licentafrontendtransport.Login.LoginActivity;
import com.sebastian.licentafrontendtransport.Map.MapFragment;
import com.sebastian.licentafrontendtransport.Cards.WalletFragment;

public class MainActivity extends AppCompatActivity {

    //Toolbar
    private View toolbarLayout;
    private ImageButton imageButtonAccount;
    //for launching the login activity
    private ActivityResultLauncher<Intent> launcher;
    private ActivityResultLauncher<String> launcher2;
    //For mainFragments
    private FrameLayout frameLayout;
    //Bottom Navigation Bar

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        initViews();
        imageButtonAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            launcher.launch(intent);
        });

        //!!!load the home fragment
        loadFragment(new HomeFragment());
        bottomNavLoad();
        initFirebaseServices();
    }

    private void bottomNavLoad() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_real_time) {
                loadFragment(new MapFragment());
                return true;
            } else if (id == R.id.nav_lines) {
                loadFragment(new LineFragment());
                return true;
            } else loadFragment(new AlertsFragment());
                if (id == R.id.wallet) {
                loadFragment(new WalletFragment());
                return true;
            } else return id == R.id.nav_service_status;
        });
    }


    private void initViews() {
        toolbarLayout = findViewById(R.id.toolbar_layout);
        imageButtonAccount = toolbarLayout.findViewById(R.id.account_button);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            //callback from login activity
            if(result.getResultCode() == LoginActivity.ADD_CARD_RESULT_CODE) {
                loadFragment(new WalletFragment());
                bottomNavigationView.setSelectedItemId(R.id.wallet);
                return;
            }else if (result.getResultCode() == LoginActivity.ALERTS_RESULT_CODE) {
                loadFragment(new AlertsFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_service_status);
                return;
            }
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        });
        frameLayout = findViewById(R.id.main_frame_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

    }


    private void loadFragment(Fragment fragment) {
        if (isFinishing() || isDestroyed()) return;

        getSupportFragmentManager();
        if (!getSupportFragmentManager().isStateSaved()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_frame_layout, fragment)
                    .commit();
        }
    }


    private void initFirebaseServices() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
        } catch (Exception e) {
            Log.w("MainActivity", "FirebaseApp initialization failed", e);
        }
        // Subscribe to topic and retrieve token
        setupFirebaseMessaging();
    }

    private void setupFirebaseMessaging() {
        // Cere permisiunea POST_NOTIFICATIONS la Android 13+ (dacă nu ai deja)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                launcher2.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Fetching FCM registration token failed", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Get new FCM registration token
                    Toast.makeText(MainActivity.this, "FCM Token: " + task.getResult(), Toast.LENGTH_SHORT).show();
                    // Subscribe to the "all_users" topic to receive broadcast messages
                    // This is how you push to all devices that have the app
                   FirebaseMessaging.getInstance().subscribeToTopic("alerts");

                });
    }

    public void launchFromFragment(Intent intent) {
        launcher.launch(intent);
    }
}
