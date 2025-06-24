package com.sebastian.licentafrontendtransport.Home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sebastian.licentafrontendtransport.HCE.TokenManager;
import com.sebastian.licentafrontendtransport.Home.BottomPage.NewsItem;
import com.sebastian.licentafrontendtransport.Home.BottomPage.NewsRecyclerViewBottomSheetAdapter;
import com.sebastian.licentafrontendtransport.Home.BottomPage.SliderImageBottomSheetAdapter;
import com.sebastian.licentafrontendtransport.Home.CreditDebitCard.CreditCardAdapter;
import com.sebastian.licentafrontendtransport.Home.Subscriptions.SubscriptionAdapter;
import com.sebastian.licentafrontendtransport.Login.LoginActivity;
import com.sebastian.licentafrontendtransport.Login.LoginManager;
import com.sebastian.licentafrontendtransport.MainActivity;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCardMember;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbissCardAPI;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.sebastian.licentafrontendtransport.models.Payment.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;


public class HomeFragment extends Fragment {

    private LoginManager loginManager;
    private ViewPager2 imageViewPager;
    private RecyclerView newsRecyclerView;
    // For Not Logged in
    private ViewPager2 adViewPager;
    private MaterialButton btnSignIn;
    private Runnable sliderRunnable;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    //for NFC scan dialog
    private AlertDialog tapDialog;
    private final BroadcastReceiver apduReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // As soon as we get any APDU, dismiss the dialog
            if (tapDialog != null && tapDialog.isShowing()) {
                tapDialog.dismiss();
            }
        }
    };

    //For Logged In
    private ViewPager2 cardsList;
    private MaterialButtonToggleGroup togglePaymentType;
    private MaterialButton btnPay;
    private List<Card> onlineCards = new ArrayList<>();
    private List<UrbisPassCardMember> subscriptionList = new ArrayList<>();

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loginManager = new LoginManager(requireActivity());
        View rootView;
        if (loginManager.isLoggedIn()) {
            rootView = inflater.inflate(R.layout.fragment_home_logged, container, false);
            setupLoggedIn(rootView);
        } else {
            rootView = inflater.inflate(R.layout.fragment_home_not_logged, container, false);
            setupNotLoggedIn(rootView);
        }
        initBottomSheet(rootView);
        return rootView;
    }

    private void setupNotLoggedIn(View view) {
        adViewPager = view.findViewById(R.id.adViewPager);
        btnSignIn = view.findViewById(R.id.btn_sign_in);

        adShowerMethod();
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            ((MainActivity) requireActivity()).launchFromFragment(intent);
        });
    }

    private void adShowerMethod() {
        List<Integer> adImages = Arrays.asList(
                R.drawable.not_logged_in_add,
                R.drawable.not_logged_in_2,
                R.drawable.not_logged_in_3
        );
        List<String> captions = Arrays.asList(
                "Add your subway subscription",
                "You can pay with your card",
                "Explore the map"
        );

        SliderImageBottomSheetAdapter adapter = new SliderImageBottomSheetAdapter(adImages, captions, R.color.red_color);
        adViewPager.setAdapter(adapter);

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int itemCount = adapter.getItemCount();
                int nextItem = (adViewPager.getCurrentItem() + 1) % itemCount;
                adViewPager.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 5000);
            }
        };

        adViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 5000);
            }
        });
    }

    private void setupLoggedIn(View view) {
        // Add any setup for logged in user here if needed

        cardsList = view.findViewById(R.id.viewpager_payment_items);
        cardsList.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        togglePaymentType = view.findViewById(R.id.toggle_payment_type);
        btnPay = view.findViewById(R.id.btn_pay);

        MaterialButton btnSubscription = view.findViewById(R.id.btn_subscription);
        MaterialButton btnCard = view.findViewById(R.id.btn_card);

        togglePaymentType.check(R.id.btn_subscription);
        fetchSubscriptions();
        int red = ContextCompat.getColor(requireContext(), R.color.red_color);
        int white = ContextCompat.getColor(requireContext(), R.color.white);
        int black = ContextCompat.getColor(requireContext(), R.color.black);
        btnSubscription.setBackgroundTintList(ColorStateList.valueOf(red));
        btnSubscription.setTextColor(white);
        btnCard.setBackgroundTintList(ColorStateList.valueOf(white));
        btnCard.setTextColor(black);
        togglePaymentType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            // Update page content
            if (checkedId == R.id.btn_subscription) {
                fetchSubscriptions();
            } else if (checkedId == R.id.btn_card) {
                fetchOnlineCards();
            }
            if (togglePaymentType.getCheckedButtonId() == R.id.btn_subscription) {
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

        btnPay.setOnClickListener(v -> {
            if (togglePaymentType.getCheckedButtonId() == R.id.btn_subscription) {
                subscriptionDialog();
            } else if (togglePaymentType.getCheckedButtonId() == R.id.btn_card) {
                paymentCardDialog();
            }
        });
    }

    private void fetchSubscriptions() {
        Retrofit client = RetrofitClient.getInstance(requireActivity());
        UrbissCardAPI api = client.create(UrbissCardAPI.class);

        String token = "Bearer " + loginManager.getAccessToken();
        Call<List<UrbisPassCardMember>> call = api.getMemberships(token);
        call.enqueue(new AuthCallBack<List<UrbisPassCardMember>>(requireActivity(), loginManager,
                () -> api.getMemberships("Bearer " + loginManager.getAccessToken())) {
            @Override
            public void onSucces(List<UrbisPassCardMember> memberships) {
                // Clear old list and add new

                subscriptionList.clear();
                if (memberships != null) {
                    subscriptionList.addAll(memberships);
                }
                // Notify adapter
                cardsList.setAdapter(new SubscriptionAdapter(requireActivity(), subscriptionList));

                Toast.makeText(requireActivity(),
                        "Subscriptions loaded: " + subscriptionList.size(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Response<List<UrbisPassCardMember>> response) {
                super.onError(response);
                Toast.makeText(requireActivity(),
                        "Error loading subscriptions: " + response.message(),
                        Toast.LENGTH_SHORT).show();
                // Set adapter with empty list to clear view
                subscriptionList.clear();
                cardsList.setAdapter(new SubscriptionAdapter(requireActivity(), subscriptionList));
            }
        });

    }

    private void paymentCardDialog() {
        if (onlineCards == null || onlineCards.isEmpty()) {
            Toast.makeText(requireActivity(), "You don't have any cards", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = cardsList.getCurrentItem();
        tapDialog = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Tap Card")
                .setMessage("Please place your phone near the validator to read it!")
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    TokenManager.NFC_ENABLED = false;
                    TokenManager.NFC_PAYLOAD = "";
                    dialog.dismiss();
                })
                .create();
        tapDialog.setOnShowListener(d -> {
            TokenManager.NFC_ENABLED = true;
            TokenManager.NFC_PAYLOAD = onlineCards.get(pos).getStripePaymentMethodId();
        });
        tapDialog.setOnDismissListener(d -> {
            TokenManager.NFC_ENABLED = false;
            TokenManager.NFC_PAYLOAD = "";
        });
        tapDialog.show();
    }

    private void subscriptionDialog() {
        if (subscriptionList == null || subscriptionList.isEmpty()) {
            Toast.makeText(requireActivity(), "You don't have any subscriptions", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = cardsList.getCurrentItem();
        if (pos < 0 || pos >= subscriptionList.size()) {
            Toast.makeText(requireActivity(), "Invalid subscription selected", Toast.LENGTH_SHORT).show();
            return;
        }
        UrbisPassCardMember member = subscriptionList.get(pos);
        String message = member.getCnp();
        tapDialog = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Tap Card")
                .setMessage("Please place your phone near the back of your device to read it!")
                .setCancelable(false)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    TokenManager.NFC_ENABLED = false;
                    TokenManager.NFC_PAYLOAD = "";
                    dialog.dismiss();
                })
                .create();
        tapDialog.setOnShowListener(d -> {
            TokenManager.NFC_ENABLED = true;

            TokenManager.NFC_PAYLOAD = message;
        });
        tapDialog.setOnDismissListener(d -> {
            TokenManager.NFC_ENABLED = false;
            TokenManager.NFC_PAYLOAD = "";
        });
        tapDialog.show();
    }

    private void fetchOnlineCards() {
        Retrofit client = RetrofitClient.getInstance(requireActivity());
        PaymentApi paymentApi = client.create(PaymentApi.class);

        Call<List<Card>> call = paymentApi.getCards(
                "Bearer " + loginManager.getAccessToken()
        );

        call.enqueue(new AuthCallBack<List<com.sebastian.licentafrontendtransport.models.Payment.Card>>(requireActivity(), loginManager,
                () -> paymentApi.getCards("Bearer " + loginManager.getAccessToken())) {
            @Override
            public void onSucces(List<Card> cards) {
                // Aici ai lista de carduri
                onlineCards.clear();
                if (cards != null) {
                    onlineCards.addAll(cards);
                }
                cardsList.setAdapter(new CreditCardAdapter(onlineCards));
                Toast.makeText(
                        requireActivity(),
                        "Cardurile au fost încărcate cu succes",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onError(Response<List<Card>> response) {
                super.onError(response);
                Toast.makeText(
                        requireActivity(),
                        "Eroare la încărcarea cardurilor",
                        Toast.LENGTH_SHORT
                ).show();
                onlineCards.clear();
                cardsList.setAdapter(new CreditCardAdapter(onlineCards));
            }
        });

    }

    private void initBottomSheet(View view) {
        imageViewPager = view.findViewById(R.id.image_viewPager_bottom_sheet);
        List<Integer> images = Arrays.asList(
                R.drawable.house_of_parliament,
                R.drawable.arcul_de_triumf,
                R.drawable.centrul_vechi
        );
        List<String> captions = Arrays.asList(
                "Discover the Parliament",
                "Explore Arcul de Triumf",
                "Experience Centrul Vechi"
        );

        SliderImageBottomSheetAdapter adapter = new SliderImageBottomSheetAdapter(images, captions, R.color.deep_color);
        imageViewPager.setAdapter(adapter);

        List<NewsItem> newsItems = Arrays.asList(
                new NewsItem(R.drawable.smiley_alex_velea,
                        "Concert la stația de metrou Piața Unirii cu Smiley, Alex Velea și Connect-R.",
                        "05/06/2025"),
                new NewsItem(R.drawable.metrou_reparatii_news,
                        "Metrorex va derula, de săptămâna viitoare, lucrări de modernizare şi înlocuire a aparatelor de cale între staţiile Tineretului şi Piaţa Unirii 2",
                        "04/06/2025"),
                new NewsItem(R.drawable.studenti_news,
                        "Reducere la abonamentele lunare pentru studenți",
                        "01/06/2025")
        );
        NewsRecyclerViewBottomSheetAdapter newsAdapter = new NewsRecyclerViewBottomSheetAdapter(newsItems);
        newsRecyclerView = view.findViewById(R.id.urbispass_news);
        newsRecyclerView.setAdapter(newsAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(apduReceiver,  new IntentFilter("com.sebastian.HCE_APDU_RECEIVED"), Context.RECEIVER_NOT_EXPORTED);
    }
    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(apduReceiver);
    }
}