package com.sebastian.licentafrontendtransport.models.Auth;

public class RefreshTokenBody {
    private final String refresh_token;

    public RefreshTokenBody(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }
}
