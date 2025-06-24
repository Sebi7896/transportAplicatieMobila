package com.sebastian.licentafrontendtransport.models.Auth;

public class TokenResponse {
    private int statusCode;
    private String accessToken;
    public int getStatusCode() {
        return statusCode;
    }

    public String getAccessToken() {
        return accessToken;
    }
}