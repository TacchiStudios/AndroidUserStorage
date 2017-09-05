package com.tacchistudios.androiduserstorage;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConfirmSharedLoginActivity extends Activity {

    public static final int RESULT_CODE_TOKEN_EXCHANGE = 12312;

    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";
    public static final String EXTRA_CONTINUE_BUTTON_TITLE = "EXTRA_CONTINUE_BUTTON_TITLE";
    public static final String EXTRA_LOGOUT_BUTTON_TITLE = "EXTRA_LOGOUT_BUTTON_TITLE";
    public static final String EXTRA_EMAIL_ADDRESS = "EXTRA_EMAIL_ADDRESS";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        LinearLayout layout = new LinearLayout(this);
//
//        TextView textView = new TextView(this);
//        textView.setText(EXTRA_MESSAGE);
//        layout.addView(textView);
//
//        Button continueButton = new Button(this);
//        continueButton.setText(getIntent().getIntExtra(EXTRA_CONTINUE_BUTTON_TITLE, 0));
//        continueButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setResult(RESULT_CODE_TOKEN_EXCHANGE);
//                finish();
//            }
//        });
//        layout.addView(continueButton);
//
//        Button logoutButton = new Button(this);
//        logoutButton.setText(getIntent().getIntExtra(EXTRA_LOGOUT_BUTTON_TITLE, 0));
//        logoutButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // TODO: Log out all apps
//                finish();
//            }
//        });
//        layout.addView(logoutButton);

//        this.setContentView(layout);

        this.setContentView(R.layout.activity_confirm_shared_login);

        TextView textView = (TextView)findViewById(R.id.messageTextView);
        textView.setText(String.format(getString(getIntent().getIntExtra(EXTRA_MESSAGE, 0)), getIntent().getStringExtra(EXTRA_EMAIL_ADDRESS)));

        Button confirmButton = (Button)findViewById(R.id.confirmButton);
        confirmButton.setText(String.format(getString(getIntent().getIntExtra(EXTRA_CONTINUE_BUTTON_TITLE, 0)), getIntent().getStringExtra(EXTRA_EMAIL_ADDRESS)));
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CODE_TOKEN_EXCHANGE);
                finish();
            }
        });
        Button logoutButton = (Button)findViewById(R.id.logoutButton);
        logoutButton.setText(getIntent().getIntExtra(EXTRA_LOGOUT_BUTTON_TITLE, 0));
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Log out all apps
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // TODO: tell them they can't!
    }
}


