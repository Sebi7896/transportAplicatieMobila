package com.sebastian.licentafrontendtransport.models.Payment;

public class SetupIntentResponse {
    private String clientSecret;

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
