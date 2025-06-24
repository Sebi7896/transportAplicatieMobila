package com.sebastian.licentafrontendtransport.models.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserApi {
    @GET("user/profile")
    Call<User> getProfile(@Header("Authorization") String token);
    @GET("user/checkAdmin")
    Call<String> getAdminStatus(@Header("Authorization") String token);
}