package com.cqu.mealtime;

import static com.cqu.mealtime.util.RequestUtil.doGet;
import static com.cqu.mealtime.util.RequestUtil.doRequest;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cqu.mealtime.util.UtilKt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    List<String> canteens = new ArrayList<>();
    List<List<String>> stall_names;
    List<List<Integer>> stall_ids;
    int limit_can = 0;
    int limit_stall = 0;
    CardView cardView;
    TextView textView;
    EditText title;
    EditText remark;
    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    public static final int COMPLETED3 = -3;
    private String toastMsg;
    OptionsPickerView pvOptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(this::queryList).start();
        setContentView(R.layout.activity_comment);
        getWindow().setStatusBarColor(getColor(R.color.page_back));
        if ((this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ImageView backBt = findViewById(R.id.bottom_back);
        backBt.setOnClickListener(v -> finish());
        cardView = findViewById(R.id.button_choose);
        UtilKt.addClickScale(cardView, 0.9f, 150);
        textView = findViewById(R.id.choose_loc);
        title = findViewById(R.id.editText_title);
        remark = findViewById(R.id.editText_content);
        pvOptions = new OptionsPickerBuilder(this, (options1, options2, options3, v) -> {
            if (options1 == 0 && options2 == 0)
                textView.setText("选择地点");
            else if (options2 == 0)
                textView.setText("地点：" + canteens.get(options1));
            else
                textView.setText("地点：" + canteens.get(options1) + " · " + stall_names.get(options1).get(options2));
            limit_can = options1;
            limit_stall = options2;
        }).build();
        Button button = findViewById(R.id.button_submit);
        button.setOnClickListener(v -> {
            if (title.getText().toString().length() == 0) {
                toastMsg = "标题不能为空！";
                Message msg = new Message();
                msg.what = COMPLETED;
                handler.sendMessage(msg);
            } else
                new Thread(this::queryInsert).start();
        });
    }

    private void queryList() {
        try {
            String response = doGet(getResources().getString(R.string.server_url) + "canteens", "");
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonObject;
            canteens.clear();
            canteens.add("全部食堂");
            stall_names = new ArrayList<>();
            stall_ids = new ArrayList<>();
            stall_names.add(List.of("全部档口"));
            stall_ids.add(List.of(0));
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                canteens.add(jsonObject.getString("canteenName"));
                stall_names.add(new ArrayList<>());
                stall_names.get(i + 1).add("全部档口");
                stall_ids.add(new ArrayList<>());
                stall_ids.get(i + 1).add(0);
            }
            response = doGet(getResources().getString(R.string.server_url) + "stalls", "");
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                int cid = jsonObject.getInt("canId");
                stall_names.get(cid).add(jsonObject.getString("stallName"));
                stall_ids.get(cid).add(jsonObject.getInt("stallId"));
            }
            Log.i("status", "列表获取完成");
            Message msg = new Message();
            msg.what = COMPLETED3;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                Toast.makeText(CommentActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            } else if (msg.what == COMPLETED2) {
                finish();
            } else if (msg.what == COMPLETED3) {
                initLimit();
            }
        }
    };

    private void initLimit() {
        pvOptions.setPicker(canteens, stall_names);
        cardView.setOnClickListener(v -> pvOptions.show());
    }

    private void queryInsert() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA);
        SharedPreferences sp = getSharedPreferences("user_inf", MODE_PRIVATE);
        int usrId = 1;
        if (sp != null)
            usrId = sp.getInt("id", 1);
        try {
            Map<String, Object> params = new HashMap<>();//组合参数
            params.put("discussionName", title.getText());
            params.put("discussionContent", remark.getText());
            params.put("canId", String.valueOf(limit_can));
            params.put("stallId", String.valueOf(stall_ids.get(limit_can).get(limit_stall)));
            params.put("usrId", usrId);
            params.put("discussionTime", df.format(Calendar.getInstance().getTime()));
            String response = doRequest("POST", getResources().getString(R.string.server_url) + "discussion", urlEncode(params));
            if (!response.equals("error")) {
                toastMsg = "发布成功";
                Message msg = new Message();
                msg.what = COMPLETED2;
                handler.sendMessage(msg);
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
}
