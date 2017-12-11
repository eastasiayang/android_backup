package com.yang.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yang.basic.ToastUtils;
import com.yang.network.HttpRequest;

public class ActivityLogin extends Activity{
    private final String TAG = ActivityLogin.class.getSimpleName();

    EditText user, password;
    Button login;
    TextView register, forget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user = (EditText) findViewById(R.id.EditText_login_user);
        password = (EditText) findViewById(R.id.EditText_login_password);
        login = (Button) findViewById(R.id.Button_login_login);
        register = (TextView) findViewById(R.id.TextView_login_register);
        forget = (TextView) findViewById(R.id.TextView_login_forget_password);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String sUser = user.getText().toString();
                String sPassword = password.getText().toString();
                if(sUser.equals("") || sPassword.equals("")){
                    Toast.makeText(ActivityLogin.this, getResources().getString(R.string.please_input_user_password),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                sPassword = Info.MD5(sPassword);
                ToastUtils.showShort(ActivityLogin.this, R.string.developing);
                //HttpRequest.login(sUser, sPassword);

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActivityLogin.this, ActivityRegister.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        forget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ActivityLogin.this, ActivityForget.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
