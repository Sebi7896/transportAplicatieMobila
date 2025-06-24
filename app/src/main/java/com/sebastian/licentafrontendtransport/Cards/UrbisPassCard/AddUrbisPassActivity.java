package com.sebastian.licentafrontendtransport.Cards.UrbisPassCard;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.sebastian.licentafrontendtransport.Login.LoginManager;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.Card;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCard;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissCardAPI;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.stripe.android.core.exception.APIConnectionException;
import com.stripe.android.core.exception.APIException;
import com.stripe.android.core.exception.AuthenticationException;
import com.stripe.android.core.exception.InvalidRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddUrbisPassActivity extends AppCompatActivity {

    private ImageButton backButton;
    private TextInputEditText fullNameInput;
    private TextInputEditText personalIdInput;
    private Spinner cardTypeSpinner;
    private Spinner durationSpinner;
    private MaterialCheckBox studentCheckbox;
    private MaterialButton buyButton;
    private List<Card> onlineCards;
    private LoginManager loginManager;
    private ActivityResultLauncher<Intent> payLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_urbis_pass);

        payLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
                result -> finish()
        );

        // bind views
        initViews();
        // back -> finish
        backButton.setOnClickListener(v -> finish());
        // duration spinner
        setDurations();
        setupBuyButtonListener();

    }

    private boolean isValid() {
        String fullName = Objects.requireNonNull(fullNameInput.getText()).toString().trim();
        String personalId = Objects.requireNonNull(personalIdInput.getText()).toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidCNP(personalId)) {
            personalIdInput.setError("Invalid CNP");
            Toast.makeText(this, "Invalid CNP", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            personalIdInput.setError(null);
        }
        return true;
    }

    private boolean isValidCNP(String personalId) {
        if (TextUtils.isEmpty(personalId)) {
            return false;
        }
        return personalId.length() == 13;
    }


    private void setDurations() {
        //array cu preturi

        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.duration_array,
                android.R.layout.simple_spinner_item
        );
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);
    }

    private void initCardTypes() {
        Card addNewOption = new Card();
        if (onlineCards != null) {
            addNewOption.setBrand("Non existing card");
            onlineCards.add(addNewOption);
            CardsAdapter adapter = new CardsAdapter(this, onlineCards);
            cardTypeSpinner.setAdapter(adapter);
            return;
        }
        onlineCards = new ArrayList<>();

        addNewOption.setBrand("Non existing card");
        onlineCards.add(addNewOption);
    }

    private void initViews() {

        backButton       = findViewById(R.id.btn_back);
        fullNameInput    = findViewById(R.id.full_name_input);
        personalIdInput  = findViewById(R.id.cnp_input);
        cardTypeSpinner  = findViewById(R.id.spinner_card_type);
        durationSpinner  = findViewById(R.id.spinner_duration);
        studentCheckbox  = findViewById(R.id.student_checkbox);
        buyButton        = findViewById(R.id.btn_buy_card);
        loginManager     = new LoginManager(this);
        fetchCards();
    }

    private void setupBuyButtonListener() {
        buyButton.setOnClickListener(v -> {

            if (!isValid()) {
                return;
            }
            String fullName   = Objects.requireNonNull(fullNameInput.getText()).toString().trim();
            String personalId = Objects.requireNonNull(personalIdInput.getText()).toString().trim();
            Card cardType   = (Card) cardTypeSpinner.getSelectedItem();
            String duration   = durationSpinner.getSelectedItem().toString();
            boolean isStudent = studentCheckbox.isChecked();

            if (isStudent) {
                new AlertDialog.Builder(this)
                    .setTitle("Student Verification")
                    .setMessage("As a student, your card will not be valid until verified by your faculty!")
                    .setPositiveButton("OK", (dialogInterface, i) -> processPurchase(fullName, personalId, cardType, duration, true)).setNegativeButton("Cancel", null)
                    .show();

                return;
            }

            processPurchase(fullName, personalId, cardType, duration, isStudent);

        });
    }

    public void sendCardToBackend(UrbisPassCard urbisPassCard) {

        Retrofit client = RetrofitClient.getInstance(this);
        UrbissCardAPI urbisCardAPI = client.create(UrbissCardAPI.class);
        Call<UrbisPassCardResponse> call = urbisCardAPI.sendCardToBackend(
                "Bearer " + loginManager.getAccessToken(),
                urbisPassCard
        );
        call.enqueue(new AuthCallBack<UrbisPassCardResponse>(this, loginManager,
                () -> urbisCardAPI.sendCardToBackend("Bearer " + loginManager.getAccessToken(), urbisPassCard)) {

            @Override
            public void onSucces(UrbisPassCardResponse responseBody) {
                Toast.makeText(getApplicationContext(), responseBody.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onError(Response<UrbisPassCardResponse> response) {
                super.onError(response);
                Toast.makeText(getApplicationContext(),"Urbispass already exists or debit/credit card problems!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchCards() {
        Retrofit client = RetrofitClient.getInstance(this);
        PaymentApi paymentApi = client.create(PaymentApi.class);

        Call<List<Card>> call = paymentApi.getCards(
                "Bearer " + loginManager.getAccessToken()
        );

        call.enqueue(new AuthCallBack<List<Card>>(this, loginManager,
                () -> paymentApi.getCards("Bearer " + loginManager.getAccessToken())) {
            @Override
            public void onSucces(List<Card> cards) {
                // Aici ai lista de carduri
                onlineCards = cards;
                initCardTypes();
            }

            @Override
            public void onError(Response<List<Card>> response) {
                super.onError(response);
                Toast.makeText(getApplicationContext(),"Error fetching card!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processPurchase(String fullName, String personalId, Card cardType, String duration, boolean isStudent) {
        String durationString = duration.substring(0,duration.indexOf("->")).trim();
        if ("Non existing card".equals(cardType.getBrand())) {
            Intent intent = new Intent(this, PayWithNewCardActivity.class);

            UrbisPassCard urbisPassCard = new UrbisPassCard(fullName, personalId, Integer.parseInt(durationString), isStudent, cardType, false);
            intent.putExtra("membership",urbisPassCard);
            payLauncher.launch(intent);
            return;
        }

        UrbisPassCard urbisPassCard = new UrbisPassCard(fullName, personalId, Integer.parseInt(durationString), isStudent,cardType,false);
        sendCardToBackend(urbisPassCard);
    }


}
