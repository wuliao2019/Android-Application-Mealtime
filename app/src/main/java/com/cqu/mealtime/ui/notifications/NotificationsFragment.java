package com.cqu.mealtime.ui.notifications;

import static com.cqu.mealtime.util.RequestUtil.doGet;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cqu.mealtime.Comment;
import com.cqu.mealtime.R;
import com.cqu.mealtime.Stall;
import com.cqu.mealtime.databinding.FragmentNotificationsBinding;
import com.cqu.mealtime.ui.dashboard.DashboardData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    public static final int COMPLETED3 = -3;
    private String toastMsg;
    RecyclerView recyclerView;
    CommentsAdapter commentsAdapter;
    EditText editText;
    List<Comment> comments = new ArrayList<>();
    List<String> canteens = new ArrayList<>();
    List<String> stalls = new ArrayList<>();
    int limit_can = 0;
    int limit_loc = 0;
    int limit_stall = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        editText=binding.editTextText;
        CardView btSearch = binding.buttonSearch;
        btSearch.setOnClickListener(v -> new Thread(this::queryComments).start());
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH || keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                btSearch.callOnClick();
                return false;
            }
            return true;
        });
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
        recyclerView = binding.commentList;
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.list_anim);
        recyclerView.setLayoutAnimation(layoutAnimationController);
        commentsAdapter = new CommentsAdapter(getContext(), comments, stalls, canteens);
        recyclerView.setAdapter(commentsAdapter);
        new Thread(() -> {
            Log.i("status", "开始获取列表");
            queryList();
            queryComments();
        }).start();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void queryList() {
        try {
            String response = doGet("http://140.210.194.87:8088/canteens", "");
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonObject;
            canteens.clear();
            canteens.add("全部食堂");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                canteens.add(jsonObject.getString("canteenName"));
            }
            stalls.clear();
            stalls.add("全部档口");
            response = doGet("http://140.210.194.87:8088/stalls", "");
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                stalls.add(jsonObject.getString("stallName"));
            }
            Log.i("status", "列表获取完成");
            Message msg = new Message();
            msg.what = COMPLETED3;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg = "无法连接服务器";
        }
    }

    private void queryComments() {
        Map<String, Object> params = new HashMap<>();//组合参数
        if (limit_can > 0)
            params.put("canId", String.valueOf(limit_can));
        if (limit_stall > 0)
            params.put("stallId", String.valueOf(limit_stall));
        if (editText.getText() != null && !editText.getText().toString().equals(""))
            params.put("discussionName", String.valueOf(editText.getText()));
        String response = doGet("http://140.210.194.87:8088/discussion", urlEncode(params));
        try {
            JSONArray jsonArray = new JSONArray(response);
            Log.i("status","调用讨论接口成功");
            JSONObject jsonObject;
            comments.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                comments.add(new Comment(jsonObject.getString("discussionName"), jsonObject.getString("discussionContent"), jsonObject.getString("discussionTime"), jsonObject.getString("userName"), jsonObject.getInt("canId"), jsonObject.getInt("stallId")));
            }
            toastMsg = "获取讨论区信息成功";
            Message msg = new Message();
            msg.what = COMPLETED2;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg = "无法连接服务器";
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
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
            } else if (msg.what == COMPLETED2) {
                refresh();
            } else if (msg.what == COMPLETED3) {
                initLimit();
            }
        }
    };
    private void refresh() {
        if (editText.isFocused())
            editText.clearFocus();
        commentsAdapter.notifyDataSetChanged();
        recyclerView.startLayoutAnimation();
    }

    private void initLimit() {
//        pvOptions3.setPicker(DashboardData.types);
//        pvOptions12.setPicker(DashboardData.canteens, DashboardData.locations);
//        bt1.setOnClickListener(v -> pvOptions12.show());
//        bt2.setOnClickListener(v -> pvOptions12.show());
//        bt3.setOnClickListener(v -> pvOptions3.show());
//        bt1.setText(DashboardData.canteens.get(limit_can));
    }
}