package com.tacchistudios.androiduserstorage;

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
