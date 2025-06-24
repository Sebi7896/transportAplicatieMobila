package com.sebastian.licentafrontendtransport.Cards.CreditCard;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.sebastian.licentafrontendtransport.R;

public class PhoneFragment extends Fragment {
    private Button addCard;
    private ActivityResultLauncher<Intent> launcher;
    private Intent currentIntent;
    public PhoneFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone, container, false);
        initComponents(view);

        addCard.setOnClickListener(addCard());

        return view;

    }
    public void initComponents(View view) {
        addCard = view.findViewById(R.id.add_card_phone_fragment);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

        });
    }
    private View.OnClickListener addCard() {
        return v -> {
            currentIntent = new Intent(requireActivity(), PhoneCardActivity.class);
            launcher.launch(currentIntent);
        };
    }
}