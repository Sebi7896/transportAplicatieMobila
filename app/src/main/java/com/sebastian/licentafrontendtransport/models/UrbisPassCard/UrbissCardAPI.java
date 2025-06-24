package com.sebastian.licentafrontendtransport.models.UrbisPassCard;

import com.sebastian.licentafrontendtransport.Cards.UrbisPassCard.UrbisPassCardResponse;
import com.sebastian.licentafrontendtransport.models.Payment.Card;
import com.sebastian.licentafrontendtransport.models.Payment.PaymentCharge;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UrbissCardAPI {
    @POST("urbis-pass-card/buy")
    Call<UrbisPassCardResponse> sendCardToBackend(@Header("Authorization") String token, @Body UrbisPassCard urbisPassCard);
    @GET("urbis-pass-card/memberships")
    Call<List<UrbisPassCardMember>> getMemberships(@Header("Authorization") String token);

    @POST("verify-membership")
    Call<List<String>> verifyCard(@Body UrbissPassID urbissPassID);

    @DELETE("urbis-pass-card/delete")
    Call<MemberDelete> deleteMembership(@Header("Authorization") String token, @Query("urbisPassId") String urbisPassId);

    @POST("urbis-pass-card/update")
    Call<MemberDelete> updateMembership(@Header("Authorization") String token, @Body UrbisPassCard urbisPassId);

}


