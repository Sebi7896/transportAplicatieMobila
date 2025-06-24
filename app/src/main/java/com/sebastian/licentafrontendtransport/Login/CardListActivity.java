package com.sebastian.licentafrontendtransport.Login;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;

import com.sebastian.licentafrontendtransport.Home.CreditDebitCard.CreditCardAdapter;
import com.sebastian.licentafrontendtransport.Home.Subscriptions.SubscriptionAdapter;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.Card;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCardMember;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissCardAPI;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CardListActivity extends AppCompatActivity {

    private LoginManager loginManager;
    private SubscriptionAdapter subscriptionAdapter;
    private CreditCardsAdapter bankCardAdapter;
    private MembershipCardsAdapter membershipCardsAdapter;

    private List<UrbisPassCardMember> subscriptionList;
    private List<Card> bankCardList;

    MaterialButton btnSubscription;
    MaterialButton btnCard;
    RecyclerView cardsRecycler;

    private ItemTouchHelper currentSwipeHelper;

    private ActivityResultLauncher<Intent> rechargeLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_card_list);

        loginManager = new LoginManager(this);

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggle_card_type);
        btnSubscription = findViewById(R.id.btn_subscription);
        btnCard = findViewById(R.id.btn_card);
        cardsRecycler= findViewById(R.id.card_list_recycler);

        setupRechargeLauncher();
        //fetch sub first
        fetchBankCards();
        toggleGroup.check(R.id.btn_card);

        int red = ContextCompat.getColor(this, R.color.red_color);
        int white = ContextCompat.getColor(this, R.color.white);
        int black = ContextCompat.getColor(this, R.color.black);
        btnCard.setBackgroundTintList(ColorStateList.valueOf(red));
        btnCard.setTextColor(white);
        btnSubscription.setBackgroundTintList(ColorStateList.valueOf(white));
        btnSubscription.setTextColor(black);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_subscription) {
                fetchSubscriptions();
            } else if (checkedId == R.id.btn_card) {
                fetchBankCards();
            }
            if (toggleGroup.getCheckedButtonId() == R.id.btn_subscription) {
                btnSubscription.setBackgroundTintList(ColorStateList.valueOf(red));
                btnSubscription.setTextColor(white);
                btnCard.setBackgroundTintList(ColorStateList.valueOf(white));
                btnCard.setTextColor(black);
            } else {
                btnCard.setBackgroundTintList(ColorStateList.valueOf(red));
                btnCard.setTextColor(white);
                btnSubscription.setBackgroundTintList(ColorStateList.valueOf(white));
                btnSubscription.setTextColor(black);
            }
        });
    }

    private void fetchSubscriptions() {
        Retrofit client = RetrofitClient.getInstance(this);
        UrbissCardAPI api = client.create(UrbissCardAPI.class);

        String token = "Bearer " + loginManager.getAccessToken();
        Call<List<UrbisPassCardMember>> call = api.getMemberships(token);
        call.enqueue(new AuthCallBack<List<UrbisPassCardMember>>(this, loginManager,
                () -> api.getMemberships("Bearer " + loginManager.getAccessToken())) {
            @Override
            public void onSucces(List<UrbisPassCardMember> memberships) {
                subscriptionList = new ArrayList<>(memberships);
                membershipCardsAdapter = new MembershipCardsAdapter(CardListActivity.this, subscriptionList, rechargeLauncher);
                cardsRecycler.setLayoutManager(new LinearLayoutManager(CardListActivity.this));
                cardsRecycler.setAdapter(membershipCardsAdapter);
                if (currentSwipeHelper != null) {
                    currentSwipeHelper.attachToRecyclerView(null);
                }
                currentSwipeHelper = membershipCardsAdapter.getSwipeHelper();
                currentSwipeHelper.attachToRecyclerView(cardsRecycler);
            }

            @Override
            public void onError(Response<List<UrbisPassCardMember>> response) {
                Toast.makeText(CardListActivity.this, "Error loading subscriptions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBankCards() {
        Retrofit client = RetrofitClient.getInstance(this);
        PaymentApi api = client.create(PaymentApi.class);

        Call<List<Card>> call = api.getCards("Bearer " + loginManager.getAccessToken());
        call.enqueue(new AuthCallBack<List<Card>>(this, loginManager,
                () -> api.getCards("Bearer " + loginManager.getAccessToken())) {
            @Override
            public void onSucces(List<Card> cards) {
                bankCardList= new ArrayList<>(cards);
                bankCardAdapter = new CreditCardsAdapter(CardListActivity.this,bankCardList);
                cardsRecycler.setLayoutManager(new LinearLayoutManager(CardListActivity.this));
                cardsRecycler.setAdapter(bankCardAdapter);
                if (currentSwipeHelper != null) {
                    currentSwipeHelper.attachToRecyclerView(null);
                }
                currentSwipeHelper = bankCardAdapter.getSwipeHelper();
                currentSwipeHelper.attachToRecyclerView(cardsRecycler);
            }

            @Override
            public void onError(Response<List<Card>> response) {
                Toast.makeText(CardListActivity.this, "Error loading bank cards", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRechargeLauncher() {
        rechargeLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(CardListActivity.this, "Card successfully recharged!", Toast.LENGTH_SHORT).show();
                        fetchSubscriptions(); // Refresh subscriptions after recharge
                    }
                });
    }
}