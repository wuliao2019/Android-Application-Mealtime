package com.cqu.mealtime.ui.dashboard;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.cqu.mealtime.databinding.FragmentDashboardBinding;
import com.cqu.mealtime.util.UtilKt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

    private int limit_type = 0;
    private int limit_can = 0;
    private int limit_loc = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        assert getArguments() != null;
        limit_can = getArguments().getInt("CanteenIndex");
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        bt1 = binding.buttonCanteen;
        bt2 = binding.buttonLocation;
        bt3 = binding.buttonType;
        editText = binding.editTextText;
        CardView btSearch = binding.buttonSearch;
        btSearch.setOnClickListener(v -> new Thread(this::queryStalls).start());
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (!editText.getText().toString().equals("") && (i == EditorInfo.IME_ACTION_SEARCH || keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
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
            limit_type = options1;
            new Thread(this::queryStalls).start();
        }).build();
        pvOptions12 = new OptionsPickerBuilder(getContext(), (options1, options2, options3, v) -> {
            bt1.setText(DashboardData.canteens.get(options1));
            bt2.setText(DashboardData.locations.get(options1).get(options2));
            limit_can = options1;
            limit_loc = DashboardData.locId.get(options1).get(options2);
            new Thread(this::queryStalls).start();
        }).build();
        DashboardData.stalls.clear();
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
        new Thread(() -> {
            System.out.println("开始获取列表");
            queryList();
            queryStalls();
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
            String response = doGet("http://140.210.194.87:8088/types", "");
            JSONObject jsonObject;
            JSONArray jsonArray = new JSONArray(response);
            DashboardData.types.clear();
            DashboardData.types.add("全部类别");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.types.add(jsonObject.getString("typeName"));
            }
            response = doGet("http://140.210.194.87:8088/canteens", "");
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
            response = doGet("http://140.210.194.87:8088/locations", "");
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.locations.get(0).add(jsonObject.getString("locationName"));
                DashboardData.locations.get(jsonObject.getInt("canId")).add(jsonObject.getString("locationName"));
                DashboardData.locId.get(0).add(i + 1);
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
        if (limit_type > 0)
            params.put("tyId", String.valueOf(limit_type));
        if (limit_can > 0)
            params.put("canId", String.valueOf(limit_can));
        if (limit_loc > 0)
            params.put("locId", String.valueOf(limit_loc));
        if (editText.getText() != null && !editText.getText().toString().equals(""))
            params.put("stallName", String.valueOf(editText.getText()));
        String response = doGet("http://140.210.194.87:8088/stalls", urlEncode(params));
        try {
            JSONArray jsonArray = new JSONArray(response);
            System.out.println("调用档口接口成功");
            JSONObject jsonObject;
            DashboardData.stalls.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                DashboardData.stalls.add(new Stall(jsonObject.getString("stallName"), jsonObject.getInt("tyId"), jsonObject.getInt("stallNum"), jsonObject.getInt("canId"), jsonObject.getInt("locId")));
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
        bt1.setText(DashboardData.canteens.get(limit_can));
    }

    private void refresh() {
        if (editText.isFocused())
            editText.clearFocus();
        stallAdapter.notifyDataSetChanged();
        stallList.startLayoutAnimation();
    }

    static class StallAdapter extends RecyclerView.Adapter<StallAdapter.Vh> {
        private final Context context;
        public List<Stall> stallsList;

        public StallAdapter(Context context, List<Stall> stallsList) {
            this.context = context;
            this.stallsList = stallsList;
        }

        @NonNull
        @Override
        public StallAdapter.Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new StallAdapter.Vh(LayoutInflater.from(context).inflate(R.layout.stall_card, null));
        }

        @Override
        public void onBindViewHolder(StallAdapter.Vh holder, final int position) {
            holder.itemName.setText(stallsList.get(position).getName());
            holder.itemId.setText("#" + stallsList.get(position).getId());
            holder.itemType.setText(DashboardData.types.get(stallsList.get(position).getType()));
            holder.itemTypeBack.setCardBackgroundColor(DashboardData.backColors.get(stallsList.get(position).getType() - 1));
            holder.itemLocation.setText(DashboardData.canteens.get(stallsList.get(position).getLocation1()) + "·" + DashboardData.locations.get(0).get(stallsList.get(position).getLocation2()));
            if (stallsList.get(position).getName().length() > 7)
                holder.itemName.setTextSize(14);
            else
                holder.itemName.setTextSize(16);
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(v -> mOnItemClickListener.onClick(position, v));
                holder.itemView.setOnLongClickListener(v -> {
                    mOnItemClickListener.onLongClick(position, v);
                    return true;
                });
            }
        }

        public interface OnItemClickListener {
            void onClick(int position, View v);

            void onLongClick(int position, View v);
        }

        private StallAdapter.OnItemClickListener mOnItemClickListener;

        public void setOnItemClickListener(StallAdapter.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        @Override
        public int getItemCount() {
            return stallsList.size();
        }

        class Vh extends RecyclerView.ViewHolder {
            private final TextView itemName;
            private final TextView itemId;
            private final TextView itemLocation;
            private final TextView itemType;
            private final CardView itemTypeBack;
//            private final TextView itemTime;

            public Vh(View itemView) {
                super(itemView);
                UtilKt.addClickScale(itemView, 0.9f, 150);
                itemName = itemView.findViewById(R.id.card_name);
                itemId = itemView.findViewById(R.id.card_id);
                itemLocation = itemView.findViewById(R.id.card_location);
                itemType = itemView.findViewById(R.id.card_type);
                itemTypeBack = itemView.findViewById(R.id.card_type_back);
//                itemTime = itemView.findViewById(R.id.canteen_time);
            }
        }
    }
}