package com.sebastian.licentafrontendtransport.models.Auth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.sebastian.licentafrontendtransport.Login.LoginManager;
import com.sebastian.licentafrontendtransport.models.User.UserApi;
import com.stripe.android.core.exception.APIConnectionException;
import com.stripe.android.core.exception.APIException;
import com.stripe.android.core.exception.AuthenticationException;
import com.stripe.android.core.exception.InvalidRequestException;

import java.util.function.Supplier;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class AuthCallBack<T> implements Callback<T> {
    private final Context context;
    private final LoginManager loginManager;
    private final Supplier<Call<T>> callFactory;

    public AuthCallBack(Context context, LoginManager loginManager, Supplier<Call<T>>  call) {
        this.context = context;
        this.loginManager = loginManager;
        this.callFactory = call;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, Response<T> response) {
        //acces Token
        Log.d("API_CALL", "Response code: " + response.code());
        if (response.isSuccessful()) {
            try {
                onSucces(response.body());
            } catch (APIConnectionException | APIException | AuthenticationException |
                     InvalidRequestException e) {
                throw new RuntimeException(e);
            }
        } else if (response.code() == 401) { //access token is expired redo using refresh token
            loginManager.handleExpiredAccessToken(context,
                    () -> callFactory.get().clone().enqueue(this), //call again with the accessed token refreshed nu merge daca nu e clone
                    () -> onError(response));
        } else {
            onError(response);
        }
    }
    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        //is not even executed
        Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();

    }
    public abstract void onSucces(T responseBody) throws APIConnectionException, APIException, AuthenticationException, InvalidRequestException;

    public void onError(Response<T> response) {

        if(response.code() == 403 || response.code() == 498) {
            Toast.makeText(context, "You don't have permission to perform this action!", Toast.LENGTH_SHORT).show();
        }
    }

}
