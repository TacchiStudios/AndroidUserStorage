package com.tacchistudios.androiduserstorage;

import android.util.Log;

import org.json.JSONObject;

import java.util.Set;

public class User {
    private static final String TAG = User.class.getSimpleName();

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

    private String clientId, clientSecret;
    public void setClientIDAndSecret(String _clientId, String _clientSecret) {
        clientId = _clientId;
        clientSecret = _clientSecret;
    }

    private String loginPath;
    public void setLoginPath(String path) {
        loginPath = path;
    }

    private String baseUrl;
    public void setBaseUrl(String url) {
        baseUrl = url;
    }

    public String oAuthToken() {
        return storage.getToken();
    }

    public boolean isLoggedIn() {
        return oAuthToken() != null; // TODO
    }

    // TODO: Maybe don't need?
    void loginRequest(String email, String password) {
//        SLAuthStore authStore = new SLAuthStore();
        Log.d(TAG, "Logging in");

        try {
//            authStore.setUsername(this.emailEditText.getText().toString());
//            authStore.setPassword(this.passwordEditText.getText().toString());
//            TokenResponse response = client.getTokenResponse(authStore.getMap());
//            Log.d(TAG, "Got reponse: " + response.error);
//            if (response != null) {
//                if (response.error != null && response.error.length() > 0) {
//                    dialog(response.error);
//                    showProgress(false);
//                } else {
//
////                    clearOldDataBeforeLoginSuccess();
//
//                    storage.setTokenDetails(response.access_token, email, password);
////                    SpeedLearningApplication.setRestClient(client);
//
////                    loginSuccess();
//                }
//            } else {
//                networkConnectionAlert();
//            }
        } catch (Exception e) {
            e.printStackTrace();
//            networkConnectionAlert();
//            showProgress(false);
        }
    }

    // TODO: Maybe don't need?
    public void handleSuccessfulLogin(String token, String email, String password) {

    }

    public interface Storage {
        String TOKEN = "token";
        String EMAIL = "email";
        String PASSWORD = "password";

        void setTokenDetails(String token, String email, String password);

        String getToken();
        String getEmail();
        String getPassword();

        Set<TokenDetails> tokenDetailsForSeparatedAppsThatCanBeExchangedForTokenForCurrentApp();
    }

    public class OAuthRequest {
        public String client_id = clientId;
        public String client_secret = clientSecret;

        public String grant_type = "password";
        public String username;
        public String password;
    }

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
