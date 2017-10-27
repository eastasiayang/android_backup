package com.yang.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yang.basic.LogUtils;
import com.yang.basic.RegexUtils;
import com.yang.basic.ToastUtils;
import com.yang.network.HttpRequest;
import com.yang.network.HttpRequestListener;
import com.yang.network.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ActivityRegister extends Activity {
    private final String TAG = ActivityRegister.class.getSimpleName();

    EditText user, password, verification;
    Button get_verification, register;
    EventHandler sms_handler;
    private Handler mHandler = new Handler();
    private Handler net_handler;
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
                View view = getWindow().peekDecorView();
                if (view != null) {
                    InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                String phone = user.getText().toString();
                if (!CheckInput(phone)) {
                    return;
                }
                if (!NetworkUtil.isNetworkAvailable(ActivityRegister.this)){
                    ToastUtils.showShort(ActivityRegister.this,
                            R.string.tip_network_error_please_check);
                    return;
                }
                //SMSSDK.getVerificationCode("86", user.getText().toString());
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
                String phone = user.getText().toString();
                String pwd = password.getText().toString();
                if (!CheckInput(phone, pwd)) {
                    return;
                }
                if (!NetworkUtil.isNetworkAvailable(ActivityRegister.this)){
                    ToastUtils.showShort(ActivityRegister.this,
                            R.string.tip_network_error_please_check);
                    return;
                }
                //SMSSDK.submitVerificationCode("86", phone,
                //        verification.getText().toString());
                net_handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            //提示读取结果
                            String result = (String) msg.obj;
                            LogUtils.d(TAG, "result = " + result);
                            Toast.makeText(ActivityRegister.this, result, Toast.LENGTH_LONG).show();
                            if (result.contains("成")) {
                                Toast.makeText(ActivityRegister.this, result, Toast.LENGTH_LONG).show();
                                ToastUtils.showShort(ActivityRegister.this,
                                        R.string.register_success);
                                //final Intent it = new Intent(ActivityRegister.this, MainActivity.class); //你要转向的Activity
                                //Timer timer = new Timer();
                                //TimerTask task = new TimerTask() {
                                //    @Override
                                //    public void run() {
                                //        startActivity(it); //执行
                                //    }
                                //};
                                //timer.schedule(task, 1000); //1秒后
                            } else {
                                ToastUtils.showShort(ActivityRegister.this,
                                        R.string.register_fail);
                            }
                        }else if(msg.what == 2){
                            ToastUtils.showShort(ActivityRegister.this, (String) msg.obj);
                        }
                    }
                };
                String params = "&name=test" + "&passwd=" + "asdf123412" + "&number=" + "13760310761";//传递的数据
                // 启动线程来执行任务
                HttpRequest.requestNetwork("http://cdz.ittun.cn/cdz/user_register.php",
                    params, new HttpRequestListener() {
                        @Override
                        public void onResponse(String result) {
                            Message m = new Message();
                            m.what = 1;
                            m.obj = result;
                            net_handler.sendMessage(m);
                        }

                        @Override
                        public void onErrorResponse(String error) {
                            LogUtils.d(TAG, error);
                            Message m = new Message();
                            m.what = 2;
                            m.obj = error;
                            net_handler.sendMessage(m);
                        }
                    });
            }
        });

        sms_handler = new EventHandler() {
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
        SMSSDK.registerEventHandler(sms_handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }

    private void setButtonStatusOff() {
        get_verification.setText(String.format(
                getResources().getString(R.string.count_down), recLen--));
        get_verification.setClickable(false);
        get_verification.setTextColor(ContextCompat.getColor(this, R.color.white));
        get_verification.setBackgroundColor(ContextCompat.getColor(this, R.color.light_grey));
    }

    private void setButtonStatusOn() {
        timer.cancel();
        get_verification.setText("重新发送");
        get_verification.setTextColor(ContextCompat.getColor(this, R.color.white));
        get_verification.setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue));
        recLen = 60;
        get_verification.setClickable(true);
    }

    boolean CheckInput(String phone) {
        if (TextUtils.isEmpty(phone)) {
            ToastUtils.showShort(ActivityRegister.this, R.string.tip_please_input_phone);
            return false;
        } else if (phone.length() < 11) {
            ToastUtils.showShort(ActivityRegister.this, R.string.tip_phone_regex_not_right);
            return false;
        } else if (!RegexUtils.checkMobile(phone)) {
            ToastUtils.showShort(ActivityRegister.this, R.string.tip_phone_regex_not_right);
            return false;
        }
        return true;
    }

    boolean CheckInput(String phone, String password) {
        return true;
        //if (!CheckInput(phone)) {
        //return false;
        //}
        //if (TextUtils.isEmpty(password)) {
        //ToastUtils.showShort(this, R.string.tip_password_can_not_be_empty);
        //return false;
        //} else if (password.length() < 6 || password.length() > 32) {
        //    ToastUtils.showShort(this, R.string.tip_please_input_6_32_password);
        //    return false;
        //}
        //return true;
    }
}
