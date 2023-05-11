package com.cqu.mealtime.ui.dashboard;

import static com.cqu.mealtime.util.RequestUtil.doGet;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cqu.mealtime.R;
import com.cqu.mealtime.Stall;
import com.cqu.mealtime.UploadActivity;
import com.cqu.mealtime.databinding.FragmentDashboardBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    public static final int COMPLETED3 = -3;
    private String toastMsg;
    Button bt1;
    Button bt2;
    Button bt3;
    OptionsPickerView pvOptions3;
    OptionsPickerView pvOptions12;
    RecyclerView stallList;
    EditText editText;
    StallAdapter stallAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().getInt("CanteenIndex") >= 0) {
            DashboardData.limit_can = getArguments().getInt("CanteenIndex");
            DashboardData.changed = true;
        }
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        bt1 = binding.buttonCanteen;
        bt2 = binding.buttonLocation;
        bt3 = binding.buttonType;
        editText = binding.editTextText;
        CardView btSearch = binding.buttonSearch;
        btSearch.setOnClickListener(v -> new Thread(this::queryStalls).start());
        FloatingActionButton floatingActionButton = binding.floatingActionButton2;
        floatingActionButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), UploadActivity.class)));
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
        stallList = binding.stallList;
        stallList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.list_anim);
        stallList.setLayoutAnimation(layoutAnimationController);
        //条件选择器
        pvOptions3 = new OptionsPickerBuilder(getContext(), (options1, options2, options3, v) -> {
            bt3.setText(DashboardData.types.get(options1));
            DashboardData.limit_type = options1;
            new Thread(this::queryStalls).start();
        }).build();
        pvOptions12 = new OptionsPickerBuilder(getContext(), (options1, options2, options3, v) -> {
            bt1.setText(DashboardData.canteens.get(options1));
            bt2.setText(DashboardData.locations.get(options1).get(options2));
            DashboardData.limit_can = options1;
            DashboardData.limit_loc = DashboardData.locId.get(options1).get(options2);
            new Thread(this::queryStalls).start();
        }).build();
//        DashboardData.stalls.clear();
        stallAdapter = new StallAdapter(getContext(), DashboardData.stalls);
        stallAdapter.setOnItemClickListener(new StallAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, View v) {

            }

            @Override
            public void onLongClick(int position, View v) {

            }
        });
        stallList.setAdapter(stallAdapter);
        if (DashboardData.changed)
            new Thread(() -> {
                System.out.println("开始获取列表");
                queryList();
                queryStalls();
                DashboardData.changed = false;
            }).start();
        else
            initLimit();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void queryList() {
        try {
            String response = doGet(getResources().getString(R.string.server_url) + "types", "");
            JSONObject jsonObject;
            JSONArray jsonArray = new JSONArray(response);
            DashboardData.types.clear();
            DashboardData.types.add("全部类别");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.types.add(jsonObject.getString("typeName"));
            }
            response = doGet(getResources().getString(R.string.server_url) + "canteens", "");
            jsonArray = new JSONArray(response);
            DashboardData.canteens.clear();
            DashboardData.canteens.add("全部食堂");
            DashboardData.locations.clear();
            DashboardData.locations.add(new ArrayList<>());
            DashboardData.locations.get(0).add("全部楼层");
            DashboardData.locId.clear();
            DashboardData.locId.add(new ArrayList<>());
            DashboardData.locId.get(0).add(0);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.canteens.add(jsonObject.getString("canteenName"));
                DashboardData.locations.add(new ArrayList<>());
                DashboardData.locations.get(i + 1).add("全部楼层");
                DashboardData.locId.add(new ArrayList<>());
                DashboardData.locId.get(i + 1).add(0);
            }
            DashboardData.loc.add("全部楼层");
            response = doGet(getResources().getString(R.string.server_url) + "locations", "");
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.loc.add(jsonObject.getString("locationName"));
                DashboardData.locations.get(jsonObject.getInt("canId")).add(jsonObject.getString("locationName"));
                DashboardData.locId.get(jsonObject.getInt("canId")).add(jsonObject.getInt("locationId"));
            }
            System.out.println("列表获取完成");
            Message msg = new Message();
            msg.what = COMPLETED3;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg = "无法连接服务器";
        }
    }

    private void queryStalls() {
        Map<String, Object> params = new HashMap<>();//组合参数
        if (DashboardData.limit_type > 0)
            params.put("tyId", String.valueOf(DashboardData.limit_type));
        if (DashboardData.limit_can > 0)
            params.put("canId", String.valueOf(DashboardData.limit_can));
        if (DashboardData.limit_loc > 0)
            params.put("locId", String.valueOf(DashboardData.limit_loc));
        if (editText.getText() != null && !editText.getText().toString().equals(""))
            params.put("stallName", String.valueOf(editText.getText()));
        String response = doGet(getResources().getString(R.string.server_url) + "stalls", urlEncode(params));
        try {
            JSONArray jsonArray = new JSONArray(response);
            System.out.println("调用档口接口成功");
            JSONObject jsonObject;
            DashboardData.stalls.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.stalls.add(new Stall(jsonObject.getString("stallName"), jsonObject.getInt("tyId"), jsonObject.getInt("stallNum"), jsonObject.getInt("canId"), jsonObject.getInt("locId"),jsonObject.getInt("peopleCount")));
            }
            toastMsg = "获取档口信息成功";
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

    private void initLimit() {
        pvOptions3.setPicker(DashboardData.types);
        pvOptions12.setPicker(DashboardData.canteens, DashboardData.locations);
        bt1.setOnClickListener(v -> pvOptions12.show());
        bt2.setOnClickListener(v -> pvOptions12.show());
        bt3.setOnClickListener(v -> pvOptions3.show());
        bt1.setText(DashboardData.canteens.get(DashboardData.limit_can));
        bt2.setText(DashboardData.loc.get(DashboardData.limit_loc));
        bt3.setText(DashboardData.types.get(DashboardData.limit_type));
    }

    private void refresh() {
        if (editText.isFocused())
            editText.clearFocus();
        stallAdapter.notifyDataSetChanged();
        stallList.startLayoutAnimation();
    }
}