package com.sebastian.licentafrontendtransport.Cards;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.sebastian.licentafrontendtransport.Cards.UrbisPassCard.UrbisPassCardsFragment;
import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.Cards.CreditCard.PhoneFragment;

public class WalletFragment extends Fragment {

    private Toolbar toolbar;
    private MaterialButtonToggleGroup toggleButtonGroup;
    private MaterialButton buttonCards;
    private MaterialButton buttonPhone;
    private FragmentContainerView fragment;

    public WalletFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        initComponents(view);
        setToggleButtonListener();
        return view;
    }

    private void setToggleButtonListener() {
        toggleButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            int red = ContextCompat.getColor(requireContext(), R.color.red_color);
            int white = ContextCompat.getColor(requireContext(), R.color.white);
            int black = ContextCompat.getColor(requireContext(), R.color.black);

            if (checkedId == R.id.buttonCards) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new UrbisPassCardsFragment()).commit();

                buttonCards.setBackgroundTintList(ColorStateList.valueOf(red));
                buttonCards.setTextColor(white);
                buttonCards.setIconTint(ColorStateList.valueOf(white));
                buttonPhone.setBackgroundTintList(ColorStateList.valueOf(white));
                buttonPhone.setTextColor(black);
                buttonPhone.setIconTint(ColorStateList.valueOf(black));
            } else if (checkedId == R.id.buttonPhone) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new PhoneFragment()).commit();

                buttonPhone.setBackgroundTintList(ColorStateList.valueOf(red));
                buttonPhone.setTextColor(white);
                buttonPhone.setIconTint(ColorStateList.valueOf(white));
                buttonCards.setBackgroundTintList(ColorStateList.valueOf(white));
                buttonCards.setTextColor(black);
                buttonCards.setIconTint(ColorStateList.valueOf(black));
            }
        });
    }

    private void initComponents(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toggleButtonGroup = view.findViewById(R.id.toggleButtonGroup);
        buttonCards = view.findViewById(R.id.buttonCards);
        buttonPhone = view.findViewById(R.id.buttonPhone);

        // Default selection and UI setup
        toggleButtonGroup.check(R.id.buttonCards);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new UrbisPassCardsFragment()).commit();

        buttonCards.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_color)));
        buttonCards.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        buttonCards.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
        buttonPhone.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
        buttonPhone.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        buttonPhone.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
    }
}