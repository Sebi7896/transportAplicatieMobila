package com.sebastian.licentafrontendtransport.AdminPage;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sebastian.licentafrontendtransport.HCE.HCEService;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentCharge;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissCardAPI;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissPassID;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.sebastian.licentafrontendtransport.utils.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AdminActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private static final int NFC_PERM_REQUEST = 1001;
    private ImageButton closeButton;
    private TextView nfcStatusTextView;
    private NfcAdapter nfcAdapter;
    private TextInputEditText etAlertTitle;
    private TextInputEditText etAlertMessage;
    private MaterialButton btnSendAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        initAlertViews();
        btnSendAlert.setOnClickListener(v -> addAlert());
        closeButton.setOnClickListener(v -> {
            if (nfcAdapter != null) nfcAdapter.disableReaderMode(this);
            enableHceService();
            finish();
        });
        //hardware support
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            nfcStatusTextView.setText(R.string.nfc_nu_este_suportat);
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            nfcStatusTextView.setText(R.string.nfc_este_dezactivat);
            return;
        }
        nfcStatusTextView.setText(R.string.welcome_approach_the_card);
    }

    private void initAlertViews() {
        closeButton = findViewById(R.id.close_button);
        nfcStatusTextView = findViewById(R.id.tv_nfc_status);
        etAlertTitle    = findViewById(R.id.et_alert_title);
        etAlertMessage  = findViewById(R.id.et_alert_message);
        btnSendAlert    = findViewById(R.id.btn_send_alert);
    }

    private void addAlert() {
        String title = Objects.requireNonNull(etAlertTitle.getText()).toString().trim();
        String message = Objects.requireNonNull(etAlertMessage.getText()).toString().trim();
        if (title.isEmpty()) {
            etAlertTitle.setError("Titlu necesar");
            return;
        }
        if (message.isEmpty()) {
            etAlertMessage.setError("Mesaj necesar");
            return;
        }
        //send to firestore
        Map<String, Object> alert = new HashMap<>();
        alert.put("title", title);
        alert.put("message", message);
        alert.put("timestamp", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance()
            .collection("alerts")
            .add(alert)
            .addOnSuccessListener(docRef -> {
                Toast.makeText(this, "Alertă trimisă!", Toast.LENGTH_SHORT).show();
                etAlertTitle.setText("");
                etAlertMessage.setText("");
            })
            .addOnFailureListener(e ->
                Toast.makeText(this, "Eroare la trimitere", Toast.LENGTH_SHORT).show()
            );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.NFC)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.NFC},
                        NFC_PERM_REQUEST
                );
                return;
            }
        }
        disableHceService();
        enableReader();
    }
    private void showTemporaryMessage(final String message) {
        runOnUiThread(() -> {
            nfcStatusTextView.setText(message);
            nfcStatusTextView.postDelayed(() ->
                nfcStatusTextView.setText(getString(R.string.welcome_approach_the_card)),
                5000
            );
        });
    }
    private void enableReader() {
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(
                    this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A
                            | NfcAdapter.FLAG_READER_NFC_B
                            | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                    null
            );
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
        enableHceService();
    }
    @Override
    protected void onDestroy() {
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
        enableHceService();
        super.onDestroy();
    }
    @Override
    public void onTagDiscovered(Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            runOnUiThread(() ->
                    nfcStatusTextView.setText(R.string.nfc_not_supported)
            );
            return;
        }
        StringBuilder text = new StringBuilder()
                .append("Tag UID: ").append(Utils.byteArrayToHex(tag.getId())).append("\n");
        try {
            isoDep.connect();
            byte[] select = Utils.hexStringToByteArray("00A4040007A0000002471001"); //SELECT AID
            byte[] resp = isoDep.transceive(select);
            String hex = Utils.byteArrayToHex(resp);
            text.append("SELECT: ").append(hex).append("\n");
            if (hex.endsWith("9000")) {
                byte[] read = Utils.hexStringToByteArray("00B000000F");
                resp = isoDep.transceive(read);
                int len = resp.length;
                byte sw1 = resp[len - 2], sw2 = resp[len - 1];
                if (sw1 == (byte) 0x90 && sw2 == (byte) 0x00) {
                    byte[] payload = Arrays.copyOf(resp, len - 2);
                    text.append("Payload: ").append(new String(payload));
                    String payloadString = new String(payload);
                    if(payloadString.isEmpty()) {
                        showTemporaryMessage("Nu ai selectat un card!");
                        return;
                    }
                    if (isValidMembership(payloadString)) {
                        verifySubscription(payloadString, text);
                    } else {
                        chargeCard(payloadString, 1000, text);
                    }
                } else {
                    text.append("Error SW: ")
                            .append(Utils.byteArrayToHex(new byte[]{sw1, sw2}));
                }
            }
        } catch (IOException e) {
            Log.e("AdminNFC", "I/O error", e);
            text.append("I/O error: ").append(e.getMessage());
        } finally {
            try { isoDep.close(); } catch (IOException ignored) {}
        }
    }
    private void verifySubscription(String idPayload, StringBuilder text) {
        Retrofit retrofitClient = RetrofitClient.getInstance(this);
        UrbissCardAPI urbisCardAPI = retrofitClient.create(UrbissCardAPI.class);
        UrbissPassID urbissPassID = new UrbissPassID(idPayload);
        Call<List<String>> call = urbisCardAPI.verifyCard(urbissPassID);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                String display;
                if (response.isSuccessful() && response.body() != null) {
                    display = TextUtils.join("\n", response.body());
                } else {
                    display = "❌ Eroare de procesare: " + response;
                    display += " " + response.message();
                    display += " " + response.code();
                    display += " " + response.errorBody();
                }
                String finalDisplay = display;
                showTemporaryMessage(finalDisplay);
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                String display = "Eroare rețea: " + t.getMessage();
                showTemporaryMessage(display);
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NFC_PERM_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableReader();
            } else {
                nfcStatusTextView.setText(R.string.refused_nfc_permission);
            }
        }
    }
    public void chargeCard(String stripePaymentId, int amount,StringBuilder text) {
        Retrofit retrofitClient = RetrofitClient.getInstance(this);
        PaymentCharge paymentCharge = new PaymentCharge(stripePaymentId, amount);
        PaymentApi paymentApi = retrofitClient.create(PaymentApi.class);
        Call<List<String>> call = paymentApi.chargeCard(paymentCharge);
        call.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call, @NonNull Response<List<String>> response) {
                String display;
                if (response.isSuccessful() && response.body() != null) {
                    display = TextUtils.join("\n", response.body());
                } else {
                    display = "❌ Eroare de procesare: " + response.message();
                }
                showTemporaryMessage(display);
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                String display = "Eroare rețea: " + t.getMessage();
                showTemporaryMessage(display);
            }
        });


    }
    private boolean isValidMembership(String id) {
        if (id == null) return false;
        return id.length() == 13 && TextUtils.isDigitsOnly(id);
    }
    private void disableHceService() {
        ComponentName component = new ComponentName(this, HCEService.class);
        getPackageManager().setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }
    private void enableHceService() {
        ComponentName component = new ComponentName(this, HCEService.class);
        getPackageManager().setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }
}