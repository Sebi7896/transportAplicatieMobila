package com.sebastian.licentafrontendtransport.Cards.UrbisPassCard;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import com.google.android.material.button.MaterialButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.sebastian.licentafrontendtransport.R;


public class UrbisPassCardsFragment extends Fragment {

    private MaterialButton addCardButton;
    private ActivityResultLauncher<Intent> launcher;

    public UrbisPassCardsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards, container, false);
        initComponents(view);
        addCardButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddUrbisPassActivity.class);
            launcher.launch(intent);
        });
        return view;
    }

    private void initComponents(View view) {
        addCardButton = view.findViewById(R.id.start_now_button);
        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // handle result if needed
            }
        );
    }
}