package com.sebastian.licentafrontendtransport.Cards.CreditCard;




import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sebastian.licentafrontendtransport.Login.LoginManager;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentMethodDto;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentResponse;
import com.sebastian.licentafrontendtransport.models.Payment.SetupIntentResponse;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.SetupIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.model.ConfirmSetupIntentParams;
import com.stripe.android.model.SetupIntent;

import java.util.Locale;
import java.util.Objects;

import retrofit2.Response;

public class PhoneCardActivity extends AppCompatActivity {


    private TextInputLayout numberCardLayout;
    private TextInputLayout nameCardLayout;
    private TextInputLayout cvvLayout;
    private TextInputEditText numberCard;
    private TextInputEditText nameCard;
    private TextInputEditText cvv;
    private TextInputEditText expirationDateCard;
    private TextView cardNumberPreview;
    private TextView cardNamePreview;
    private TextView cardExpirationPreview;
    private Button addCreditCard;
    private ImageView creditCardLogo;
    private Stripe stripe;
    private LoginManager loginManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_card);



        initComponents();
        addWatchers();
        loginManager = new LoginManager(this);
        expirationDateCard.setOnFocusChangeListener((v, hasFocus)  -> showMonthYearPickerDialog(hasFocus));
        expirationDateCard.setOnClickListener((v)  ->  showMonthYearPickerDialog(true));

        initStripeConnection();

        addCreditCard.setOnClickListener((v) -> {
            if(isValid()) {
                new AlertDialog.Builder(PhoneCardActivity.this)
                        .setTitle("Card Verification")
                        .setMessage("To verify the validity of your card, Stripe will temporarily authorize a charge of 2 RON. This amount will not be withdrawn and will be automatically released.")
                        .setPositiveButton("Continue", (dialog, which) -> createStripePaymentMethod())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }
    private void initStripeConnection() {
        PaymentConfiguration.init(
                getApplicationContext(),
                getString(R.string.stripe_publish_key)
        );
        stripe = new Stripe(
                getApplicationContext(),
                PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey()
        );
    }

    public void initComponents() {
        numberCardLayout = findViewById(R.id.card_number_text_input_layout);
        nameCardLayout = findViewById(R.id.card_name_text_input_layout);
        cvvLayout = findViewById(R.id.card_cvc_text_input_layout);
        numberCard = findViewById(R.id.card_number_text_input_edit_text);
        nameCard = findViewById(R.id.card_name_text_input_edit_text);
        cvv = findViewById(R.id.card_cvc_text_input_edit_text);
        expirationDateCard = findViewById(R.id.card_expiration_text_input_edit_text);
        expirationDateCard.setKeyListener(null);

        cardNumberPreview = findViewById(R.id.card_number);
        cardNamePreview = findViewById(R.id.card_name);
        cardExpirationPreview = findViewById(R.id.card_expiration);
        addCreditCard = findViewById(R.id.add_credit_card);
        creditCardLogo = findViewById(R.id.credit_card_logo);
        // Close button
        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
    }

    private void showMonthYearPickerDialog(boolean hasFocus) {
        if(!hasFocus) {
            return;
        }
        MonthYearPickerDialog monthYearPickerDialog = new MonthYearPickerDialog();
        monthYearPickerDialog.setOnDateSetListener((month, year) -> {
            String selectedDate = String.format(Locale.US,"%02d/%d", month, year);
            expirationDateCard.setText(selectedDate);
            cardExpirationPreview.setText(String.format(Locale.US,"Expiration: %02d/%d", month, year));
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        monthYearPickerDialog.show(fragmentManager, "MonthYearPickerDialog");
    }

    public boolean isValid() {
        boolean value = true;
        if(numberCard.getText() != null && numberCard.getText().toString().length() != 16) {
            numberCardLayout.setError("Please enter a valid card number (16 digits)");
            value = false;
        }else {
            numberCardLayout.setError(null);
        }
        if(nameCard.getText() != null && nameCard.getText().toString().length() <=4) {
            nameCardLayout.setError("Please enter a valid name");
            value = false;
        }else {
            nameCardLayout.setError(null);
        }
        if(cvv.getText() != null && cvv.getText().toString().length() != 3) {
            cvvLayout.setError("Please enter a valid CVV (3 digits)");
            value = false;
        }else {
            cvvLayout.setError(null);
        }
        return value;
    }

    public void addWatchers() {
        numberCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getCardType(s.toString());
                String formatted = formatCardNumber(s.toString());
                cardNumberPreview.setText(formatted);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        nameCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardNamePreview.setText(String.format(Locale.US,"Name: " + s));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private String formatCardNumber(String input) {
        StringBuilder stringBuilder = new StringBuilder("XXXX-XXXX-XXXX-XXXX");

        int inputIndex = 0;
        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == 'X' && inputIndex < input.length()) {
                stringBuilder.setCharAt(i, input.charAt(inputIndex));
                inputIndex++;
            }
        }
        return stringBuilder.toString();
    }

    public void getCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            creditCardLogo.setImageResource(R.drawable.visa);
            return;
        } else if (cardNumber.matches("^5[1-5].*") || cardNumber.matches("^222[1-9].*") ||
                cardNumber.matches("^22[3-9].*") || cardNumber.matches("^2[3-6].*") ||
                cardNumber.matches("^27[0-1].*") || cardNumber.matches("^2720.*")) {
            creditCardLogo.setImageResource(R.drawable.mastercard);
            return;
        }

        creditCardLogo.setImageBitmap(null);
    }

    private void createStripePaymentMethod() {
        //disable button
        addCreditCard.setEnabled(false);
        addCreditCard.setText(R.string.loading_credit_card);

        String[] dateParts = Objects.requireNonNull(expirationDateCard.getText()).toString().split("/");
        int expMonth = Integer.parseInt(dateParts[0]);
        int expYear = Integer.parseInt(dateParts[1]);

        PaymentMethodCreateParams.Card cardParams = new PaymentMethodCreateParams.Card.Builder()
                .setNumber(Objects.requireNonNull(numberCard.getText()).toString())
                .setExpiryMonth(expMonth)
                .setExpiryYear(expYear)
                .setCvc(Objects.requireNonNull(cvv.getText()).toString())
                .build();

        PaymentMethodCreateParams params = PaymentMethodCreateParams.create(cardParams,
                new PaymentMethod.BillingDetails.Builder()
                        .setName(Objects.requireNonNull(nameCard.getText()).toString())
                        .build());

        String accessToken = loginManager.getAccessToken();
        PaymentApi paymentApi = RetrofitClient.getInstance(getApplicationContext()).create(PaymentApi.class);

        paymentApi.createSetupIntent("Bearer " + accessToken).enqueue(
                new AuthCallBack<SetupIntentResponse>(
                        PhoneCardActivity.this,
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
                        //3D Secure
                        stripe.confirmSetupIntent(PhoneCardActivity.this, confirmParams);
                    }
                    @Override
                    public void onError(Response<SetupIntentResponse> response) {
                        super.onError(response);
                        Toast.makeText(PhoneCardActivity.this, "Setup failed: " + response.message(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //after the pass of 3D Secure
        stripe.onSetupResult(requestCode, data, new ApiResultCallback<SetupIntentResult>() {
            @Override
            public void onSuccess(@NonNull SetupIntentResult result) {
                SetupIntent intent = result.getIntent();
                String paymentMethodId = intent.getPaymentMethodId();

                if (paymentMethodId != null) {
                    PaymentMethodDto dto = new PaymentMethodDto(paymentMethodId,true);
                    PaymentApi paymentApi = RetrofitClient.getInstance(getApplicationContext()).create(PaymentApi.class);
                    paymentApi.createPayment(dto, "Bearer " + loginManager.getAccessToken())
                            .enqueue(new AuthCallBack<PaymentResponse>(
                                    PhoneCardActivity.this,
                                    loginManager,
                                    () -> paymentApi.createPayment(dto, "Bearer " + loginManager.getAccessToken())
                            ) {
                                @Override
                                public void onSucces(PaymentResponse responseBody) {
                                    finish();
                                    Toast.makeText(PhoneCardActivity.this, "Card saved successfully", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onError(Response<PaymentResponse> response) {
                                    super.onError(response);
                                    Toast.makeText(PhoneCardActivity.this, "Setup failed: Card already saved" , Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                } else {
                    finish();
                    Toast.makeText(PhoneCardActivity.this, "Payment method ID is null", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull Exception e) {
                Log.e("Stripe", "Failed to complete setup", e);
                Toast.makeText(PhoneCardActivity.this, "Setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}