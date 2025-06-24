package com.sebastian.licentafrontendtransport.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sebastian.licentafrontendtransport.R;

import java.util.concurrent.TimeUnit;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getInstance(Context context) {
        if (retrofit == null) {
            CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    .add("urbispass.go.ro",
                            "sha256/V0cFBRV23XlDlvZJmg6YaZhM3oNF8EDJtGetpt35YGQ=")
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .certificatePinner(certificatePinner) // Optional pinning
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.base_link))
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}