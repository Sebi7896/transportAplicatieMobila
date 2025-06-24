package com.sebastian.licentafrontendtransport.models.Payment;

import android.app.DownloadManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PaymentApi {
    @POST("/credit-card/create")
    Call<PaymentResponse> createPayment(@Body PaymentMethodDto paymentMethod, @Header("Authorization") String token);
    @POST("/credit-card/setup-intent")
    Call<SetupIntentResponse> createSetupIntent(@Header("Authorization") String token);
    @GET("/credit-card/cards") // exemplu de endpoint
    Call<List<Card>> getCards(@Header("Authorization") String token);

    @POST("urbispass-charge")
    Call<List<String>> chargeCard(@Body PaymentCharge paymentId);

    @DELETE("credit-card/delete")
    Call<String> deleteCard(@Header("Authorization") String token, @Query("id") String cardId);
}
