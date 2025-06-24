package com.sebastian.licentafrontendtransport.Cards.UrbisPassCard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sebastian.licentafrontendtransport.models.Payment.Card;

import java.util.List;

public class CardsAdapter extends ArrayAdapter<Card> {

    public CardsAdapter(Context context, List<Card> cards) {
        super(context, android.R.layout.simple_spinner_item, cards);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    // View for selected item in spinner (collapsed)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        Card card = getItem(position);
        if (card != null) {
            if (card.getBrand().compareTo("Non existing card") == 0) {
                label.setText("Non existing card");
                return label;
            }

            label.setText(String.format("%s • %s", card.getBrand(), card.getLast4()));
        }
        return label;
    }

    // View for each item in the dropdown list
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        Card card = getItem(position);
        if (card != null) {
            if (card.getBrand().compareTo("Non existing card") == 0) {
                label.setText("Non existing card");
                return label;
            }


            label.setText(String.format("%s (•••• %s) Exp: %02d/%d",
                    card.getBrand(),
                    card.getLast4(),
                    card.getExpMonth(),
                    card.getExpYear()));
        }
        return label;
    }
}
