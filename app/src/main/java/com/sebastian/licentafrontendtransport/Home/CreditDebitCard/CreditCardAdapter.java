package com.sebastian.licentafrontendtransport.Home.CreditDebitCard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.Payment.Card;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class CreditCardAdapter extends RecyclerView.Adapter<CreditCardAdapter.CardViewHolder> {

    private final List<Card> cardList;

    public CreditCardAdapter(List<Card> cardList) {
        this.cardList = cardList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_credit_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        //imagine
        if (card.getBrand().equals("visa")) {
            List<Integer> visaLogos = Arrays.asList(
                    R.drawable.visa_template_card_blue,
                    R.drawable.visa_template_card_gold,
                    R.drawable.visa_template_card_green,
                    R.drawable.visa_template_card_pink
                    );
            Collections.shuffle(visaLogos);
            holder.ivCardBackground.setImageResource( visaLogos.get(0));
        } else {
            List<Integer> mastercardLogos = Arrays.asList(
                    R.drawable.mastercard_template_card_red,
                    R.drawable.mastercard_template_card_blue,
                    R.drawable.mastercard_template_card_black,
                    R.drawable.mastercard_template_card_pink
            );
            Collections.shuffle(mastercardLogos);
            holder.ivCardBackground.setImageResource( mastercardLogos.get(0));
        }


        // Afișează ultimele 4 cifre
        holder.tvCardNumber.setText(String.format("•••• %s", card.getLast4()));

        // Afișează numele titularului
        holder.tvNameCard.setText(card.getCardName());

        // Afișează data expirării în format MM/AA
        holder.tvExpiry.setText(
                String.format(Locale.getDefault(), "%02d/%02d",
                        card.getExpMonth(), card.getExpYear() % 100)
        );
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCardNumber;
        final TextView tvNameCard;
        final TextView tvExpiry;
        final ImageView ivCardBackground;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvNameCard = itemView.findViewById(R.id.tvNameCard);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            this.ivCardBackground = itemView.findViewById(R.id.ivCardBackground);
        }
    }
}