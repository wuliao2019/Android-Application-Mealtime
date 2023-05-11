package com.cqu.mealtime.ui.notifications;

import static com.cqu.mealtime.util.RequestUtil.doGet;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cqu.mealtime.Comment;
import com.cqu.mealtime.CommentActivity;
import com.cqu.mealtime.R;
import com.cqu.mealtime.Stall;
import com.cqu.mealtime.databinding.FragmentNotificationsBinding;
import com.cqu.mealtime.ui.dashboard.DashboardData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    OptionsPickerView pvOptions;
    CardView btf;
    List<Comment> comments = new ArrayList<>();
    List<String> canteens = new ArrayList<>();
    List<String> stalls = new ArrayList<>();
    List<Integer> locOfStall = new ArrayList<>();
    List<String> loc = new ArrayList<>();
    List<List<String>> stall_names;
    List<List<Integer>> stall_ids;
    int limit_can = 0;
    int limit_stall = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        FloatingActionButton floatingActionButton = binding.floatingActionButton;
        floatingActionButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), CommentActivity.class)));
        editText = binding.editTextText;
        TextView textView = binding.selected;
        CardView btSearch = binding.buttonSearch;
        btf = binding.buttonFilter;
        btSearch.setOnClickListener(v -> new Thread(this::queryComments).start());
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener((textView2, i, keyEvent) -> {
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
        commentsAdapter.setOnItemClickListener((position, v) -> {
            Comment comment = comments.get(position);
            Dialog dialog = new Dialog(getContext());
            //去掉标题线
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.comment_detail, null, false);
            TextView txt = view.findViewById(R.id.comment_title2);
            txt.setText(comment.getTitle());
            txt = view.findViewById(R.id.comment_remark2);
            txt.setText(comment.getRemark());
            txt.setMovementMethod(ScrollingMovementMethod.getInstance());
            txt = view.findViewById(R.id.comment_time2);
            txt.setText(comment.getTime());
            txt = view.findViewById(R.id.comment_stall2);
            txt.setText(canteens.get(comment.getCan_id()) + " · " + loc.get(locOfStall.get(comment.getStall_id())) + " · " + stalls.get(comment.getStall_id()));
            txt = view.findViewById(R.id.user_name2);
            txt.setText(comment.getUsername());
            dialog.setContentView(view);
            //背景透明
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
        });
        new Thread(() -> {
            Log.i("status", "开始获取列表");
            queryList();
        }).start();
        pvOptions = new OptionsPickerBuilder(getContext(), (options1, options2, options3, v) -> {
            if (options1 == 0 && options2 == 0)
                textView.setVisibility(View.GONE);
            else {
                textView.setVisibility(View.VISIBLE);
                textView.setText("筛选：" + canteens.get(options1) + " · " + stall_names.get(options1).get(options2));
            }
            limit_can = options1;
            limit_stall = options2;
            new Thread(this::queryComments).start();
        }).build();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
            response = doGet(getResources().getString(R.string.server_url) + "locations", "");
            jsonArray = new JSONArray(response);
            loc.clear();
            loc.add("全部楼层");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                loc.add(jsonObject.getString("locationName"));
            }
            stalls.clear();
            stalls.add("全部档口");
            locOfStall.clear();
            locOfStall.add(0);
            response = doGet(getResources().getString(R.string.server_url) + "stalls", "");
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                stalls.add(jsonObject.getString("stallName"));
                locOfStall.add(jsonObject.getInt("locId"));
                int cid = jsonObject.getInt("canId");
                stall_names.get(cid).add(jsonObject.getString("stallName"));
                stall_ids.get(cid).add(jsonObject.getInt("stallId"));
            }
            Log.i("status", "列表获取完成");
            Message msg = new Message();
            msg.what = COMPLETED3;
            handler.sendMessage(msg);
            queryComments();
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg = "无法连接服务器";
        }
    }

    private void queryComments() {
        Map<String, Object> params = new HashMap<>();//组合参数
        if (limit_can > 0) params.put("canId", String.valueOf(limit_can));
        if (limit_stall > 0) params.put("stallId", String.valueOf(limit_stall));
        if (editText.getText() != null && !editText.getText().toString().equals(""))
            params.put("discussionName", String.valueOf(editText.getText()));
        String response = doGet(getResources().getString(R.string.server_url) + "discussion", urlEncode(params));
        try {
            JSONArray jsonArray = new JSONArray(response);
            Log.i("status", "调用讨论接口成功");
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
        if (editText.isFocused()) editText.clearFocus();
        commentsAdapter.notifyDataSetChanged();
        recyclerView.startLayoutAnimation();
    }

    private void initLimit() {
        pvOptions.setPicker(canteens, stall_names);
        btf.setOnClickListener(v -> pvOptions.show());
    }
}