package com.yang.login;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yang.basic.LogUtils;

import cn.smssdk.SMSSDK;

public class ActivityRegister extends Activity {
    private final String TAG = ActivityLogin.class.getSimpleName();

    EditText user, password;
    Button register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = (EditText) findViewById(R.id.EditText_register_user);
        password = (EditText) findViewById(R.id.EditText_register_password);
        register = (Button) findViewById(R.id.Button_register_register);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SMSSDK.initSDK(ActivityRegister.this, "1893f467af140", "ca54ebd1730a1cddf2a7b9097a0de4f9");
                LogUtils.d(TAG, "phone = " + user.getText().toString());
                //SMSSDK.getVerificationCode("86", user.getText().toString());
                SMSSDK.getVerificationCode("86", "13760310761");
            }
        });
    }
}
