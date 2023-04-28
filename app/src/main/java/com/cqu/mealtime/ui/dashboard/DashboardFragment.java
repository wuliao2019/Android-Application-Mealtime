package com.cqu.mealtime.ui.dashboard;

import static com.cqu.mealtime.util.RequestUtil.doGet;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cqu.mealtime.R;
import com.cqu.mealtime.Stall;
import com.cqu.mealtime.databinding.FragmentDashboardBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private final List<String> canteens = new ArrayList<>();
    private final List<List<String>> locations = new ArrayList<>();
    private final List<List<Integer>> locId = new ArrayList<>();
    private final List<String> types = new ArrayList<>();
    private final List<Stall> stalls = new ArrayList<>();
    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    private String toastMsg;
    StallAdapter stallAdapter;
    private int limit_type = 0;
    private int limit_can = 0;
    private int limit_loc = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("&&&&&&&&&&&& Dashboard onCreate");
        if (canteens.size() == 0)
            new Thread(() -> {
                System.out.println("开始获取列表");
                queryList();
                queryStalls();
            }).start();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button bt1 = binding.buttonCanteen;
        Button bt2 = binding.buttonLocation;
        Button bt3 = binding.buttonType;
        CardView btSearch = binding.buttonSearch;
        btSearch.setOnClickListener(v -> new Thread(this::queryStalls).start());
//        btSearch.setOnClickListener(v -> stallAdapter.notifyDataSetChanged());
        stallAdapter = new StallAdapter(getContext(), stalls);
        RecyclerView stallList = binding.stallList;
        stallList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.list_anim);
        stallList.setLayoutAnimation(layoutAnimationController);
        stallList.setAdapter(stallAdapter);
        //条件选择器
        OptionsPickerView pvOptions3 = new OptionsPickerBuilder(getContext(), (options1, options2, options3, v) -> {
            bt3.setText(types.get(options1));
            limit_type = options1;
            new Thread(this::queryStalls).start();
        }).build();
        pvOptions3.setPicker(types);
        bt3.setOnClickListener(v -> pvOptions3.show());
        OptionsPickerView pvOptions12 = new OptionsPickerBuilder(getContext(), (options1, options2, options3, v) -> {
            bt1.setText(canteens.get(options1));
            bt2.setText(locations.get(options1).get(options2));
            limit_can = options1;
            limit_loc = locId.get(options1).get(options2);
            new Thread(this::queryStalls).start();
        }).build();
        pvOptions12.setPicker(canteens, locations);
        bt1.setOnClickListener(v -> pvOptions12.show());
        bt2.setOnClickListener(v -> pvOptions12.show());
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
            types.clear();
            types.add("全部类别");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                types.add(jsonObject.getString("typeName"));
            }
            response = doGet("http://140.210.194.87:8088/canteens", "");
            jsonArray = new JSONArray(response);
            canteens.clear();
            canteens.add("全部食堂");
            locations.clear();
            locations.add(new ArrayList<>());
            locations.get(0).add("全部楼层");
            locId.clear();
            locId.add(new ArrayList<>());
            locId.get(0).add(0);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                canteens.add(jsonObject.getString("canteenName"));
                locations.add(new ArrayList<>());
                locations.get(i + 1).add("全部楼层");
                locId.add(new ArrayList<>());
                locId.get(i + 1).add(0);
            }
            response = doGet("http://140.210.194.87:8088/locations", "");
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                locations.get(0).add(jsonObject.getString("locationName"));
                locations.get(jsonObject.getInt("canId")).add(jsonObject.getString("locationName"));
                locId.get(0).add(i + 1);
                locId.get(jsonObject.getInt("canId")).add(jsonObject.getInt("locationId"));
            }
            System.out.println("列表获取完成");
        } catch (Exception e) {
            e.printStackTrace();
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
        String response = doGet("http://140.210.194.87:8088/stalls", urlEncode(params));
        try {
            JSONArray jsonArray = new JSONArray(response);
            System.out.println("调用档口接口成功");
            JSONObject jsonObject;
            stalls.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                stalls.add(new Stall(jsonObject.getString("stallName"), jsonObject.getInt("tyId"), jsonObject.getInt("stallNum"), jsonObject.getInt("canId"), jsonObject.getInt("locId")));
            }
            toastMsg = "获取档口信息成功";
            Message msg = new Message();
            msg.what = COMPLETED2;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
            toastMsg = "无法连接网络";
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
            }
        }
    };

    private void refresh() {
        stallAdapter.setList(stalls);
    }

    class StallAdapter extends RecyclerView.Adapter<StallAdapter.Vh> {
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
            holder.itemLocation.setText(canteens.get(stallsList.get(position).getLocation1()) + "·" + locations.get(0).get(stallsList.get(position).getLocation2()));
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(v -> mOnItemClickListener.onClick(holder.getAdapterPosition(), v));
                holder.itemView.setOnLongClickListener(v -> {
                    mOnItemClickListener.onLongClick(holder.getAdapterPosition(), v);
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

        public void setList(List<Stall> newList) {
            this.stallsList = newList;
            notifyDataSetChanged();
        }

        class Vh extends RecyclerView.ViewHolder {
            private final TextView itemName;
            private final TextView itemId;
            private final TextView itemLocation;
//            private final TextView itemTime;

            public Vh(View itemView) {
                super(itemView);
                itemName = itemView.findViewById(R.id.card_name);
                itemId = itemView.findViewById(R.id.card_id);
                itemLocation = itemView.findViewById(R.id.card_location);
//                itemTime = itemView.findViewById(R.id.canteen_time);
            }
        }
    }
}