package com.yang.login;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yang.basic.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ActivityRegister extends Activity {
    private final String TAG = ActivityLogin.class.getSimpleName();

    EditText user, password, verification;
    Button get_verification, register;
    EventHandler handler;
    private Handler mHandler = new Handler();
    private Timer timer = new Timer();
    private int recLen = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = (EditText) findViewById(R.id.EditText_register_user);
        password = (EditText) findViewById(R.id.EditText_register_password);
        verification = (EditText) findViewById(R.id.EditText_register_verification);
        get_verification = (Button) findViewById(R.id.Button_register_get_verification_code);
        register = (Button) findViewById(R.id.Button_register_register);
        get_verification.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LogUtils.d(TAG, user.getText().toString());
                SMSSDK.getVerificationCode("86", user.getText().toString());
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setButtonStatusOff();
                                if (recLen < 1) {
                                    setButtonStatusOn();
                                }
                            }
                        });
                    }
                };
                timer = new Timer();
                timer.schedule(task, 0, 1000);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SMSSDK.submitVerificationCode("86", user.getText().toString(),
                        verification.getText().toString());
            }
        });

        handler = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    //回调完成
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        //提交验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActivityRegister.this, "验证成功", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActivityRegister.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {


                    }
                } else {
                    ((Throwable) data).printStackTrace();
                    Throwable throwable = (Throwable) data;
                    try {
                        JSONObject obj = new JSONObject(throwable.getMessage());
                        final String des = obj.optString("detail");
                        if (!TextUtils.isEmpty(des)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ActivityRegister.this, "提交错误信息", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        SMSSDK.registerEventHandler(handler);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    private void setButtonStatusOff() {
        get_verification.setText(String.format(
                getResources().getString(R.string.count_down), recLen--));
        get_verification.setClickable(false);
        get_verification.setTextColor(Color.parseColor("#f3f4f8"));
        get_verification.setBackgroundColor(Color.parseColor("#b1b1b3"));
    }

    private void setButtonStatusOn() {
        timer.cancel();
        get_verification.setText("重新发送");
        get_verification.setTextColor(getResources().getColor(R.color.white));
        get_verification.setBackgroundColor(Color.parseColor("#f3f4f8"));
        recLen = 60;
        get_verification.setClickable(true);
    }
}
