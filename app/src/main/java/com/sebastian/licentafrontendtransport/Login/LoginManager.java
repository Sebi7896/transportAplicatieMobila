package com.sebastian.licentafrontendtransport.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthApi;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Auth.RefreshTokenBody;
import com.sebastian.licentafrontendtransport.models.Auth.TokenResponse;
import com.sebastian.licentafrontendtransport.models.User.UserApi;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.stripe.android.core.exception.APIConnectionException;
import com.stripe.android.core.exception.APIException;
import com.stripe.android.core.exception.AuthenticationException;
import com.stripe.android.core.exception.InvalidRequestException;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.security.GeneralSecurityException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginManager {
    private static final String PLAIN_PREFS_NAME = "login_plain_prefs";
    private static final String ENCRYPTED_PREFS_NAME = "login_secure_prefs";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private final Context context;
    private final SharedPreferences plainPrefs;
    private final SharedPreferences securePrefs;

    public LoginManager(Context context) {
        this.context = context;

        // 🟢 Plaintext SharedPreferences for access token
        this.plainPrefs = context.getSharedPreferences(PLAIN_PREFS_NAME, Context.MODE_PRIVATE);

        // 🔐 Encrypted SharedPreferences for refresh token
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setUserAuthenticationRequired(false)
                    .build();

            this.securePrefs = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize secure storage", e);
        }
    }

    // 🔐 Store encrypted refresh token
    public void saveRefreshToken(String token) {
        securePrefs.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    public String getRefreshToken() {
        return securePrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    // 🟢 Store access token in plain SharedPreferences
    public void saveToken(String accessToken) {
        plainPrefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply();
    }

    public String getAccessToken() {
        return plainPrefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public void logout() {
        plainPrefs.edit().clear().apply();
        securePrefs.edit().clear().apply();
    }

    public void login() {
        Uri loginUri = Uri.parse(context.getString(R.string.login_path));
        CustomTabsIntent intent = new CustomTabsIntent.Builder().setShowTitle(false).build();
        intent.launchUrl(context, loginUri);
    }

    public void loginCheck(Runnable onLogged, Runnable onNotLogged) {
        if (getAccessToken() != null) {
            onLogged.run();
        } else {
            onNotLogged.run();
        }
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }


    public void handleExpiredAccessToken(Context context, @NonNull Runnable onSuccess, @NonNull Runnable onFailure) {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            onFailure.run();
            return;
        }
        AuthApi authApi = RetrofitClient.getInstance(context).create(AuthApi.class);
        RefreshTokenBody refreshTokenBody = new RefreshTokenBody(refreshToken);
        Call<TokenResponse> call = authApi.refreshToken(refreshTokenBody);
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatusCode() != 401) {
                    saveToken(response.body().getAccessToken());
                    Toast.makeText(context,"Token refreshed!", Toast.LENGTH_SHORT).show(); //token refreshed
                    //rerun again the call
                    onSuccess.run();
                } else {
                    Toast.makeText(context,"Session has expired, you need to login again!", Toast.LENGTH_SHORT).show();
                    logout();
                    onFailure.run(); // Callback to trigger fallback/redirect
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                onFailure.run();
            }
        });
    }


}