package com.tacchistudios.androiduserstorage;

import android.content.Context;
import android.content.Intent;

public class User {
    private static final String TAG = User.class.getSimpleName();

    public static final String BROADCAST_LOGIN_STATE_CHANGED = "com.tacchistudios.androiduserstorage.LOGIN_STATE_CHANGED";
    public static final String EXTRA_PREVIOUS_STATE = "previous_state";
    public static final String EXTRA_NEW_STATE = "new_state";
    public static final String EXTRA_SENDER_ID = "sender_id";

    private static User ourInstance = new User();
    public static User getInstance() {
        return ourInstance;
    }
    private User() {
    }

    private Storage storage;
    public void setStorage(Storage _storage) {
        storage = _storage;
    }
    public Storage getStorage() {
        return storage;
    }

    public String oAuthToken() {
        return storage.getToken();
    }

    public boolean isLoggedIn() {
        return oAuthToken() != null && storage.getEmail() != null;
    }

    // TODO: Maybe don't need?
    public void handleSuccessfulLogin(String token, String email, String password) {
        storage.setTokenDetails(token, email, password);
    }

    public void logout() {
        storage.setTokenDetails(null, null, null);
    }

    public enum LoginState {
        NO_TOKEN,
        NO_TOKEN_AND_EXCHANGEABLE_TOKEN_AVAILABLE,
        ANON_TOKEN_ONLY,
        ANON_TOKEN_AND_EXCHANGEABLE_TOKEN_AVAILABLE,
        USER_TOKEN
    }

    public LoginState getLoginState() {
        LoginState result = LoginState.NO_TOKEN;

        if (isLoggedIn()) {
            result = LoginState.USER_TOKEN;
        } else if (oAuthToken() != null) {
            if (storage.areExchangableTokensAvailable()) {
                result = LoginState.ANON_TOKEN_AND_EXCHANGEABLE_TOKEN_AVAILABLE;
            } else {
                result = LoginState.ANON_TOKEN_ONLY;
            }
        } else if (storage.areExchangableTokensAvailable()) {
            return LoginState.NO_TOKEN_AND_EXCHANGEABLE_TOKEN_AVAILABLE;
        }

        return result;
    }

    public abstract static class Storage {
        public static final String TOKEN = "token";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";

        protected Context context;
        public Context getContext() {
        return context;
    }
        public void setContext(Context context) {
            this.context = context;
        }

        public void setTokenDetails(String token, String email, String password){
            LoginState previousState = User.getInstance().getLoginState();
            storeTokenDetails(token, email, password);
            LoginState newState = User.getInstance().getLoginState();
            if (previousState != newState) {
                Intent intent = new Intent(BROADCAST_LOGIN_STATE_CHANGED);
                intent.putExtra(EXTRA_PREVIOUS_STATE, previousState);
                intent.putExtra(EXTRA_NEW_STATE, newState);
                intent.putExtra(EXTRA_SENDER_ID, context.getPackageName());
                context.sendBroadcast(intent);
            }
        }

        protected abstract void storeTokenDetails(String token, String email, String password);

        public abstract String getToken();
        public abstract String getEmail();
        public abstract String getPassword();

        public abstract boolean areExchangableTokensAvailable();

        public abstract TokenDetails anyAvailableExchangeableTokenDetails();
    }

//    public static class OAuthRequest {
//        public String client_id;
//        public String client_secret;
//
//        public String grant_type = "password";
//        public String username;
//        public String password;
//    }

    public static class TokenDetails {
        public String packageName;
        public String token;
        public String email;
        public String password;

        public TokenDetails(String _package, String _token, String _email, String _password) {
            packageName = _package;
            token = _token;
            email = _email;
            password = _password;
        }
    }
}
