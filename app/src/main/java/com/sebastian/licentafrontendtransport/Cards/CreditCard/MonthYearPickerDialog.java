package com.sebastian.licentafrontendtransport.Cards.CreditCard;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class MonthYearPickerDialog extends DialogFragment {

    private OnDateSetListener listener;

    public interface OnDateSetListener {
        void onDateSet(int month, int year);
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        // Create Number Pickers for Month & Year
        final NumberPicker monthPicker = new NumberPicker(context);
        final NumberPicker yearPicker = new NumberPicker(context);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(new String[]{
                "(01) January", "(02) February", "(03) March", "(04) April", "(05) May", "(06) June",
                "(07) July", "(08) August", "(09) September", "(10) October", "(11) November", "(12) December"
        });
        monthPicker.setValue(currentMonth);
        yearPicker.setMinValue(currentYear);
        yearPicker.setMaxValue(currentYear + 100);
        yearPicker.setValue(currentYear);
        yearPicker.setWrapSelectorWheel(false);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 30, 50, 30);
        layout.setWeightSum(2);

        // Add margin between the pickers
        LinearLayout.LayoutParams monthParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        monthParams.setMargins(0, 0, 20, 0);

        LinearLayout.LayoutParams yearParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        monthPicker.setLayoutParams(monthParams);
        yearPicker.setLayoutParams(yearParams);

        layout.addView(monthPicker);
        layout.addView(yearPicker);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Month & Year");
        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (listener != null) {
                listener.onDateSet(monthPicker.getValue(), yearPicker.getValue());
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}