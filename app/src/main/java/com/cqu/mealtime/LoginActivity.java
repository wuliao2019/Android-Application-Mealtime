package com.cqu.mealtime;

import static com.cqu.mealtime.util.RequestUtil.doRequest;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.cqu.mealtime.util.UtilKt;
import com.github.ybq.android.spinkit.SpinKitView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    TextView nameBox, pwdBox1, pwdBox2;
    CardView pwdAgainBox, button1, button2;
    SpinKitView spinKitView;
    ConstraintLayout dashboard;
    private String toastMsg;
    public static final int COMPLETED = -1, COMPLETED2 = -2, COMPLETED3 = -3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setStatusBarColor(getColor(R.color.login_back));
        if ((this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        dashboard = findViewById(R.id.dashboard);
        nameBox = findViewById(R.id.user_name_box);
        pwdBox1 = findViewById(R.id.user_pwd_box1);
        pwdBox2 = findViewById(R.id.user_pwd_box2);
        pwdAgainBox = findViewById(R.id.cardView3);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        spinKitView = findViewById(R.id.spin_kit);
        UtilKt.addClickScale(button1, 0.9f, 150);
        UtilKt.addClickScale(button2, 0.9f, 150);
        SharedPreferences sp = getSharedPreferences("user_inf", Context.MODE_PRIVATE);
        if (sp != null && !sp.getString("name", "").equals("") && !sp.getString("pwd", "").equals("")) {
            nameBox.setText(sp.getString("name", ""));
            pwdBox1.setText(sp.getString("pwd", ""));
            new Thread(this::login).start();
        } else
            loginMode();
    }

    private void loginMode() {
        dashboard.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.INVISIBLE);
        pwdAgainBox.setVisibility(View.INVISIBLE);
        button1.setOnClickListener(v -> registerMode());
        button2.setOnClickListener(v -> {
            if (nameBox.getText().toString().length() == 0)
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            else if (pwdBox1.getText().toString().length() == 0)
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            else {
                spinKitView.setVisibility(View.VISIBLE);
                dashboard.setVisibility(View.INVISIBLE);
                new Thread(this::login).start();
            }
        });
    }

    private void registerMode() {
        pwdAgainBox.setVisibility(View.VISIBLE);
        button2.setOnClickListener(v -> loginMode());
        button1.setOnClickListener(v -> {
            if (nameBox.getText().toString().length() == 0)
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            else if (pwdBox1.getText().toString().length() < 6)
                Toast.makeText(this, "密码至少六位", Toast.LENGTH_SHORT).show();
            else if (!pwdBox1.getText().toString().equals(pwdBox2.getText().toString()))
                Toast.makeText(this, "两次输入密码不一致", Toast.LENGTH_SHORT).show();
            else {
                spinKitView.setVisibility(View.VISIBLE);
                new Thread(this::register).start();
            }
        });
    }

    private void login() {
        Map<String, Object> params = new HashMap<>();//组合参数
        params.put("userName", nameBox.getText());
        params.put("userPwd", pwdBox1.getText());
        try {
            String response = doRequest("POST", getResources().getString(R.string.server_url) + "users/Login", urlEncode(params));
            if (!response.equals("error")) {
                JSONArray jsonArray = new JSONArray(response);
                if (jsonArray.length() == 0)
                    toastMsg = "用户名或密码错误";
                else if (jsonArray.length() > 1)
                    toastMsg = "服务器错误";
                else {
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    SharedPreferences.Editor editor = getSharedPreferences("user_inf", MODE_PRIVATE).edit();
                    editor.putInt("id", jsonObject.getInt("usrId"));
                    editor.putString("name", jsonObject.getString("userName"));
                    editor.putString("pwd", jsonObject.getString("userPwd"));
                    editor.apply();
                    toastMsg = "登录成功";
                    Message msg = new Message();
                    msg.what = COMPLETED2;
                    handler.sendMessage(msg);
                }
            } else
                toastMsg = "请求出错";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Message msg = new Message();
            msg.what = COMPLETED;
            handler.sendMessage(msg);
            msg = new Message();
            msg.what = COMPLETED3;
            handler.sendMessage(msg);
        }
    }

    private void register() {
        Map<String, Object> params = new HashMap<>();//组合参数
        params.put("userName", nameBox.getText());
        params.put("userPwd", pwdBox1.getText());
        try {
            String response = doRequest("POST", getResources().getString(R.string.server_url) + "users", urlEncode(params));
            if (!response.equals("error")) {
                if (response.equals("-1\n"))
                    toastMsg = "用户名已存在";
                else if (response.equals("1\n")) {
                    toastMsg = "注册成功";
                    Message msg = new Message();
                    msg.what = COMPLETED3;
                    handler.sendMessage(msg);
                } else toastMsg = "服务器错误";
            } else
                toastMsg = "请求出错";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Message msg = new Message();
            msg.what = COMPLETED;
            handler.sendMessage(msg);
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                Toast.makeText(LoginActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                spinKitView.setVisibility(View.INVISIBLE);
            } else if (msg.what == COMPLETED2) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (msg.what == COMPLETED3) {
                loginMode();
            }
        }
    };
}
