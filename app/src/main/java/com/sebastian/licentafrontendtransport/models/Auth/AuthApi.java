package com.sebastian.licentafrontendtransport.models.Auth;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/refresh")
    Call<TokenResponse> refreshToken(@Body RefreshTokenBody refresh_token);
}
