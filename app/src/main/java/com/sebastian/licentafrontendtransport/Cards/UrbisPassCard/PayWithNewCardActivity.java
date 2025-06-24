package com.sebastian.licentafrontendtransport.Cards.UrbisPassCard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sebastian.licentafrontendtransport.Cards.CreditCard.MonthYearPickerDialog;
import com.sebastian.licentafrontendtransport.Cards.CreditCard.PhoneCardActivity;
import com.sebastian.licentafrontendtransport.Login.LoginManager;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.Card;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentMethodDto;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentResponse;
import com.sebastian.licentafrontendtransport.models.Payment.SetupIntentResponse;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCard;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissCardAPI;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.SetupIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmSetupIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.SetupIntent;

import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Activity for adding a credit card, with preview and optional “Save this card” functionality.
 */
public class PayWithNewCardActivity extends AppCompatActivity {

    private TextInputLayout numberCardLayout;
    private TextInputLayout nameCardLayout;
    private TextInputLayout cvvLayout;
    private TextInputLayout expirationLayout;
    private TextInputEditText numberCard;
    private TextInputEditText nameCard;
    private TextInputEditText cvv;
    private TextInputEditText expirationDateCard;
    private TextView cardNumberPreview;
    private TextView cardNamePreview;
    private TextView cardExpirationPreview;
    private Button addCreditCardButton;
    private ImageView creditCardLogo;
    private ImageButton btnClose;
    private MaterialCheckBox saveCardCheckbox;

    private Stripe stripe;
    private LoginManager loginManager;
    private UrbisPassCard urbisPassCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pay_with_new_card);

        if(getIntent().hasExtra("membership")) {
            urbisPassCard = (UrbisPassCard) getIntent().getSerializableExtra("membership");
        }

        initComponents();
        addWatchers();

        loginManager = new LoginManager(this);

        // Expiration date click / focus opens MonthYearPickerDialog
        expirationDateCard.setOnFocusChangeListener((v, hasFocus) -> showMonthYearPickerDialog(hasFocus));
        expirationDateCard.setOnClickListener(v -> showMonthYearPickerDialog(true));

        initStripeConnection();

        addCreditCardButton.setOnClickListener(v -> {
            if (isValid()) {
                // Ask confirmation about temporary authorization
                new AlertDialog.Builder(PayWithNewCardActivity.this)
                        .setTitle("Card Verification")
                        .setMessage("To verify the validity of your card, Stripe will temporarily authorize a small charge. This amount will not be withdrawn and will be automatically released.")
                        .setPositiveButton("Continue", (dialog, which) -> createStripePaymentMethod())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        btnClose.setOnClickListener(v -> finish());
    }

    private void initComponents() {
        numberCardLayout = findViewById(R.id.card_number_text_input_layout);
        nameCardLayout = findViewById(R.id.card_name_text_input_layout);
        cvvLayout = findViewById(R.id.card_cvc_text_input_layout);
        expirationLayout = findViewById(R.id.card_expiration_text_input_layout);

        numberCard = findViewById(R.id.card_number_text_input_edit_text);
        nameCard = findViewById(R.id.card_name_text_input_edit_text);
        cvv = findViewById(R.id.card_cvc_text_input_edit_text);
        expirationDateCard = findViewById(R.id.card_expiration_text_input_edit_text);
        expirationDateCard.setKeyListener(null); // read-only, open picker

        cardNumberPreview = findViewById(R.id.card_number);
        cardNamePreview = findViewById(R.id.card_name);
        cardExpirationPreview = findViewById(R.id.card_expiration);

        addCreditCardButton = findViewById(R.id.add_credit_card);
        creditCardLogo = findViewById(R.id.credit_card_logo);

        btnClose = findViewById(R.id.btn_close);
        saveCardCheckbox = findViewById(R.id.save_card_checkbox);
    }

    private void initStripeConnection() {
        // Initialize Stripe SDK with your publishable key
        PaymentConfiguration.init(
                getApplicationContext(),
                getString(R.string.stripe_publish_key)
        );
        stripe = new Stripe(
                getApplicationContext(),
                PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey()
        );
    }

    private void showMonthYearPickerDialog(boolean hasFocus) {
        if (!hasFocus) {
            return;
        }
        MonthYearPickerDialog monthYearPickerDialog = new MonthYearPickerDialog();
        monthYearPickerDialog.setOnDateSetListener((month, year) -> {
            String selectedDate = String.format(Locale.US, "%02d/%d", month, year);
            expirationDateCard.setText(selectedDate);
            cardExpirationPreview.setText(String.format(Locale.US, "Expiration: %02d/%d", month, year));
        });
        FragmentManager fragmentManager = getSupportFragmentManager();
        monthYearPickerDialog.show(fragmentManager, "MonthYearPickerDialog");
    }

    private boolean isValid() {
        boolean valid = true;

        // Card number: exactly 16 digits
        String num = Objects.requireNonNull(numberCard.getText()).toString().trim();
        if (num.length() != 16 || !num.matches("\\d{16}")) {
            numberCardLayout.setError("Please enter a valid card number (16 digits)");
            valid = false;
        } else {
            numberCardLayout.setError(null);
        }
        // Name: at least e.g. 4 characters
        String nm = Objects.requireNonNull(nameCard.getText()).toString().trim();
        if (nm.length() <= 4) {
            nameCardLayout.setError("Please enter a valid name");
            valid = false;
        } else {
            nameCardLayout.setError(null);
        }
        // CVC: exactly 3 digits
        String c = Objects.requireNonNull(cvv.getText()).toString().trim();
        if (c.length() != 3 || !c.matches("\\d{3}")) {
            cvvLayout.setError("Please enter a valid CVV (3 digits)");
            valid = false;
        } else {
            cvvLayout.setError(null);
        }
        // Expiration date: non-empty and valid format MM/YYYY or MM/YY
        String exp = Objects.requireNonNull(expirationDateCard.getText()).toString().trim();
        if (exp.isEmpty() || !exp.matches("\\d{2}/\\d{2,4}")) {
            expirationLayout.setError("Please select expiration date");
            valid = false;
        } else {
            expirationLayout.setError(null);
        }
        return valid;
    }

    private void addWatchers() {
        numberCard.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Format preview: show as XXXX-XXXX-XXXX-XXXX
                String formatted = formatCardNumber(s.toString());
                cardNumberPreview.setText(formatted);
                // Update logo based on BIN
                updateCardLogo(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        nameCard.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardNamePreview.setText(String.format(Locale.US, "Name: %s", s.toString()));
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        // Expiration preview is handled in date picker callback
    }

    private String formatCardNumber(String input) {
        // Insert dashes every 4 digits, up to 16 digits
        String digits = input.replaceAll("\\D", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length() && i < 16; i++) {
            sb.append(digits.charAt(i));
            if ((i + 1) % 4 == 0 && i != 15 && i != digits.length() - 1) {
                sb.append('-');
            }
        }
        // Fill placeholder if desired, or leave blank for missing digits
        // Here we just show entered digits with dashes
        return sb.toString();
    }

    private void updateCardLogo(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            creditCardLogo.setImageResource(R.drawable.visa);
        } else if (cardNumber.matches("^5[1-5].*") ||
                cardNumber.matches("^222[1-9].*") ||
                cardNumber.matches("^22[3-9].*") ||
                cardNumber.matches("^2[3-6].*") ||
                cardNumber.matches("^27[0-1].*") ||
                cardNumber.matches("^2720.*")) {
            creditCardLogo.setImageResource(R.drawable.mastercard);
        } else {
            creditCardLogo.setImageDrawable(null);
        }
    }

    private void createStripePaymentMethod() {
        // Disable button and show loading
        addCreditCardButton.setEnabled(false);
        addCreditCardButton.setText(R.string.loading_credit_card);

        // Parse expiration
        String[] dateParts = Objects.requireNonNull(expirationDateCard.getText()).toString().split("/");
        int expMonth = Integer.parseInt(dateParts[0]);
        int expYear = Integer.parseInt(dateParts[1].length() == 2
                ? "20" + dateParts[1]
                : dateParts[1]);

        // Create payment method
        PaymentMethodCreateParams.Card cardParams = new PaymentMethodCreateParams.Card.Builder()
                .setNumber(Objects.requireNonNull(numberCard.getText()).toString().trim())
                .setExpiryMonth(expMonth)
                .setExpiryYear(expYear)
                .setCvc(Objects.requireNonNull(cvv.getText()).toString().trim())
                .build();
        PaymentMethodCreateParams params = PaymentMethodCreateParams.create(cardParams,
                new PaymentMethod.BillingDetails.Builder()
                        .setName(Objects.requireNonNull(nameCard.getText()).toString().trim())
                        .build());


        String accessToken = loginManager.getAccessToken();
        PaymentApi paymentApi = RetrofitClient.getInstance(getApplicationContext()).create(PaymentApi.class);

        paymentApi.createSetupIntent("Bearer " + accessToken).enqueue(
                new AuthCallBack<SetupIntentResponse>(
                        PayWithNewCardActivity.this,
                        loginManager,
                        () -> paymentApi.createSetupIntent("Bearer " + loginManager.getAccessToken())
                ) {
                    @Override
                    public void onSucces(SetupIntentResponse responseBody) {
                        String clientSecret = responseBody.getClientSecret();
                        ConfirmSetupIntentParams confirmParams = ConfirmSetupIntentParams.create(
                                params,
                                clientSecret
                        );
                        // Use the explicit intent for SetupIntent confirmation (3D Secure, etc.)
                        stripe.confirmSetupIntent(PayWithNewCardActivity.this, confirmParams);
                    }
                    @Override
                    public void onError(Response<SetupIntentResponse> response) {
                        super.onError(response);
                        Toast.makeText(PayWithNewCardActivity.this, "Setup failed: " + response.message(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        );



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        stripe.onSetupResult(requestCode, data, new ApiResultCallback<SetupIntentResult>() {
            @Override
            public void onSuccess(@NonNull SetupIntentResult result) {
                SetupIntent intent = result.getIntent();
                String paymentMethodId = intent.getPaymentMethodId();
                if (paymentMethodId != null) {
                    //saving the membership
                    urbisPassCard.getCardPayment().setStripePaymentMethodId(paymentMethodId);

                    Retrofit client = RetrofitClient.getInstance(PayWithNewCardActivity.this);
                    UrbissCardAPI urbisCardAPI = client.create(UrbissCardAPI.class);
                    Call<UrbisPassCardResponse> call = urbisCardAPI.sendCardToBackend(
                            "Bearer " + loginManager.getAccessToken(),
                            urbisPassCard
                    );
                    call.enqueue(new AuthCallBack<UrbisPassCardResponse>(PayWithNewCardActivity.this, loginManager,
                            () -> urbisCardAPI.sendCardToBackend("Bearer " + loginManager.getAccessToken(), urbisPassCard)) {

                        @Override
                        public void onSucces(UrbisPassCardResponse response) {
                            Toast.makeText(PayWithNewCardActivity.this,"UrbisPass card saved successfully!" , Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onError(Response<UrbisPassCardResponse> response) {
                            super.onError(response);
                            Toast.makeText(getApplicationContext(),"Card already saved debit/credit card problems!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                    //saving the card
                    // If “Save this card” is checked, include that info in DTO to backend

                    boolean saveCard = saveCardCheckbox.isChecked();
                    // Build DTO including paymentMethodId and saveCard flag
                    PaymentMethodDto dto = new PaymentMethodDto(paymentMethodId,saveCard);
                    // If your API expects a flag, wrap it in appropriate request object:
                    // e.g. CreateCardRequest { paymentMethodId, saveCard }
                    PaymentApi paymentApi = RetrofitClient.getInstance(getApplicationContext())
                            .create(PaymentApi.class);
                    // Example: if API has endpoint createPayment that also handles saving:
                    paymentApi.createPayment(dto, "Bearer " + loginManager.getAccessToken())
                            .enqueue(new AuthCallBack<PaymentResponse>(
                                    PayWithNewCardActivity.this,
                                    loginManager,
                                    () -> paymentApi.createPayment(dto, "Bearer " + loginManager.getAccessToken())
                            ) {
                                @Override
                                public void onSucces(PaymentResponse responseBody) {
                                    // Here you know the card setup succeeded on backend.
                                    if (saveCard) {
                                        Toast.makeText(PayWithNewCardActivity.this,
                                                "Card saved successfully",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(PayWithNewCardActivity.this,
                                                "Card verified successfully",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                    finish();
                                }
                                @Override
                                public void onError(Response<PaymentResponse> response) {
                                    super.onError(response);
                                    finish();
                                }
                            });
                } else {
                    finish();
                    Toast.makeText(PayWithNewCardActivity.this,
                            "Payment method ID is null", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(@NonNull Exception e) {
                Log.e("Stripe", "Failed to complete setup", e);
                Toast.makeText(PayWithNewCardActivity.this,
                        "Setup failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}