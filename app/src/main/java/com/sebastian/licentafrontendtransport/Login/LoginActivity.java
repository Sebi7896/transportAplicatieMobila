package com.sebastian.licentafrontendtransport.Login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sebastian.licentafrontendtransport.AdminPage.AdminActivity;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.User.User;
import com.sebastian.licentafrontendtransport.models.User.UserApi;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.sebastian.licentafrontendtransport.utils.UsefulMethods;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private TextView aboutTextView;
    private Button btnRegister;
    private LoginManager loginManager;
    private Button cardList;
    private Button addCard;
    private MaterialCardView alerts;
    private ImageButton closeButton;
    private Button updatesButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private User user;
    private MaterialButton btn_admin;

    private Intent intent;

    public static final int ALERTS_RESULT_CODE = 10001;
    public static final int ADD_CARD_RESULT_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        initViews();
        initLoginManager();

        // Handle callback from login redirect (deep link)
        callbackLogin(getIntent());
        //check if user is admin to redirect to his activity

        // Check login state
        loginManager.loginCheck(this::onLoggedIn, this::onNotLoggedIn);




        closeButton.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        updatesButton.setOnClickListener(v -> updatesFlow());
    }

    public void callbackLogin(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "callback".equals(data.getHost())) {
            String refreshToken = data.getQueryParameter("refreshToken");
            String accessToken = data.getQueryParameter("accessToken");

            if (refreshToken != null) loginManager.saveRefreshToken(refreshToken);
            if (accessToken != null) loginManager.saveToken(accessToken);

            setResult(RESULT_OK);
            finish();
        }
    }


    private void initViews() {
        welcomeTextView = findViewById(R.id.welcome_text_view);
        aboutTextView = findViewById(R.id.about_text_view);
        btnRegister = findViewById(R.id.btnRegister);
        addCard = findViewById(R.id.btnLinkCards);
        cardList = findViewById(R.id.btnViewCards);
        alerts = findViewById(R.id.alertsCard);
        closeButton = findViewById(R.id.close_button);
        updatesButton = findViewById(R.id.btnUpdate);
    }

    private void initLoginManager() {
        loginManager = new LoginManager(this);
    }

    private void onLoggedIn() {
        fetchUserProfile();
    }

    private void onNotLoggedIn() {
        welcomeTextView.setText(R.string.welcome_text);
        aboutTextView.setText(R.string.text_urbispass_first_card);
        btnRegister.setText(R.string.register_or_log_in);
        btnRegister.setOnClickListener(v -> loginManager.login());
        addCard.setOnClickListener(v -> UsefulMethods.notLogedIn(this,getString(R.string.add_card_not_logged_in),loginManager));
        cardList.setOnClickListener(v -> UsefulMethods.notLogedIn(this,getString(R.string.link_card_not_logged_in),loginManager));
        alerts.setOnClickListener(v -> {
            intent = getIntent();
            setResult(ALERTS_RESULT_CODE,intent);
            finish();
        });

    }




    private void fetchUserProfile() {

        UserApi userApi = RetrofitClient.getInstance(getApplicationContext()).create(UserApi.class);
        userApi.getProfile("Bearer " + loginManager.getAccessToken()).enqueue(
                new AuthCallBack<User>(LoginActivity.this, loginManager, () -> userApi.getProfile("Bearer " + loginManager.getAccessToken())) {
                    @Override
                    public void onSucces(User responseBody) {
                        user = responseBody;
                        isAdmin(responseBody);
                        showUserInfo(responseBody);
                        Toast.makeText(LoginActivity.this, "Profilul a fost incarcat cu succes", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Response<User> response) {
                        super.onError(response);

                    }
                }
        );
    }

    private void isAdmin(User responseBody) {
        if(responseBody.isAdmin()) {
            btn_admin = findViewById(R.id.btn_admin);
            btn_admin.setVisibility(View.VISIBLE);
            btn_admin.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(intent);//vreau sa intre in mod nu sa reintoarca si ceva
            });
            Toast.makeText(this, "Admin", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserInfo(User user) {
        handler.post(() -> {
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            welcomeTextView.setText(getString(R.string.welcome_back, firstName));
            aboutTextView.setText(R.string.this_is_your_personal_area_and_where_you_can_edit_your_preferences);
            btnRegister.setText(R.string.logout);
            btnRegister.setOnClickListener(v -> confirmLogout());
            alerts.setOnClickListener(v -> {
                intent = getIntent();
                setResult(ALERTS_RESULT_CODE,intent);
                finish();
            });
            addCard.setOnClickListener(v -> {
                intent = getIntent();
                setResult(ADD_CARD_RESULT_CODE,intent);
                finish();
            });
            cardList.setOnClickListener(v -> openCardList());
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_logout_title)
                .setMessage(R.string.confirm_logout_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void performLogout() {
        Toast.makeText(this, "Logout Successful!", Toast.LENGTH_SHORT).show();
        loginManager.logout();
        setResult(RESULT_OK);
        finish();
    }

    private void updatesFlow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_update_promo, null);

        ImageView promoImage = dialogView.findViewById(R.id.promo_image);
        FloatingActionButton closeButtonPromo = dialogView.findViewById(R.id.cancel_promo);
        Button accessSite = dialogView.findViewById(R.id.open_site_button);


        builder.setView(dialogView);

        // Permite dismiss când se apasă în afara dialogului
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        closeButtonPromo.setOnClickListener(v -> dialog.dismiss());
        accessSite.setOnClickListener(v -> {
            String url = getString(R.string.url_metrou);

            // Folosind Chrome Custom Tabs
            CustomTabsIntent.Builder builderTabs = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builderTabs.build();
            customTabsIntent.launchUrl(this, Uri.parse(url));
        });

        dialog.show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 5000);
    }
    private void openCardList() {
        //open the card man activity
        Intent intent = new Intent(this, CardListActivity.class);
        startActivity(intent); // no for result
    }

}

