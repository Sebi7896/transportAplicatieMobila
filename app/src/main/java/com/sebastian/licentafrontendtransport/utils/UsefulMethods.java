package com.sebastian.licentafrontendtransport.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.sebastian.licentafrontendtransport.Login.LoginManager;

public class UsefulMethods {
    public static void notLogedIn(Context context, String message , LoginManager loginManager) {
        new AlertDialog.Builder(context)
                .setTitle("Not Logged In")
                .setMessage(message)
                .setPositiveButton("Sign In", (dialog, which) -> {
                    // Replace LoginActivity.class with your actual login activity class
                    loginManager.login();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true)
                .show();
    }
}
