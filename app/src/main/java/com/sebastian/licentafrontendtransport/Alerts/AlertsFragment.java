package com.sebastian.licentafrontendtransport.Alerts;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sebastian.licentafrontendtransport.R;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

public class AlertsFragment extends Fragment {

    private RecyclerView rvAlerts;
    private FirebaseFirestore firestore;
    private ListenerRegistration alertsListener;
    private AlertAdapter adapter;
    public AlertsFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlerts = view.findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        //reading from firestore database
        firestore = FirebaseFirestore.getInstance();
        alertsListener = firestore.collection("alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING) //desc sort
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<AlertItem> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        AlertItem item = doc.toObject(AlertItem.class);
                        list.add(item);
                    }
                    adapter = new AlertAdapter(requireActivity(),list);
                    rvAlerts.setAdapter(adapter);
                });

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alertsListener != null) {
            alertsListener.remove();
            alertsListener = null;
        }
    }
}