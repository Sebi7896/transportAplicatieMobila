package com.sebastian.licentafrontendtransport.Login;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Auth.AuthCallBack;
import com.sebastian.licentafrontendtransport.models.Payment.Card;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentApi;
import com.sebastian.licentafrontendtransport.utils.RetrofitClient;
import com.stripe.android.core.exception.APIConnectionException;
import com.stripe.android.core.exception.APIException;
import com.stripe.android.core.exception.AuthenticationException;
import com.stripe.android.core.exception.InvalidRequestException;

import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class CreditCardsAdapter extends RecyclerView.Adapter<CreditCardsAdapter.CardViewHolder> {

    private final Context context;
    private final List<Card> cards;

    public CreditCardsAdapter(Context context, List<Card> cards) {
        this.context = context;
        this.cards = cards;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_credit_card_cards, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cards.get(position);
        holder.cardNumberText.setText(String.format("**** **** **** %s", card.getLast4()));
        holder.cardBrandText.setText(card.getBrand());
        holder.cardExpiryText.setText(String.format(Locale.US,"Exp: %d/%d", card.getExpMonth(), card.getExpYear()));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void removeCard(int position) {
        Card removed = cards.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "Removed card ending in " + removed.getLast4(), Toast.LENGTH_SHORT).show();
    }


    public ItemTouchHelper getSwipeHelper() {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                new AlertDialog.Builder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this card?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteCardFromServer(position);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        notifyItemChanged(position);
                        dialog.dismiss();
                    })
                    .setCancelable(false)
                    .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState,
                                    boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                Paint paint = new Paint();
                paint.setColor(Color.RED);

                RectF background = new RectF(
                        itemView.getRight() + dX, itemView.getTop(),
                        itemView.getRight(), itemView.getBottom()
                );
                c.drawRect(background, paint);

                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(48f);
                textPaint.setTextAlign(Paint.Align.RIGHT);

                float textX = itemView.getRight() - 50f;
                float textY = itemView.getTop() + (itemView.getHeight() / 2f) + 16f;
                c.drawText("Delete", textX, textY, textPaint);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView cardNumberText, cardBrandText;
        TextView cardExpiryText;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNumberText = itemView.findViewById(R.id.card_number_text);
            cardBrandText = itemView.findViewById(R.id.card_brand_text);
            cardExpiryText = itemView.findViewById(R.id.card_expiry_text);
        }
    }

    private void deleteCardFromServer(int position) {
        Card card = cards.get(position);
        // Exemplu Retrofit
        LoginManager loginManager = new LoginManager(context);
        String accessToken = loginManager.getAccessToken();
        PaymentApi api = RetrofitClient.getInstance(context).create(PaymentApi.class);
        api.deleteCard("Bearer " + accessToken, card.getStripePaymentMethodId()).enqueue(new AuthCallBack<String>(
                context, loginManager, () -> api.deleteCard("Bearer " + loginManager.getAccessToken(), card.getStripePaymentMethodId())
        ) {
            @Override
            public void onSucces(String responseBody) throws APIConnectionException, APIException, AuthenticationException, InvalidRequestException {
                removeCard(position);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                notifyItemChanged(position);
                Toast.makeText(context, "Failed to delete card!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}


