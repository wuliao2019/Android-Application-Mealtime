package com.cqu.mealtime.ui.home;

import static com.cqu.mealtime.util.RequestUtil.doGet;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.WeightedLatLng;
import com.cqu.mealtime.Canteen;
import com.cqu.mealtime.InfoWinAdapter;
import com.cqu.mealtime.R;
import com.cqu.mealtime.databinding.FragmentHomeBinding;
import com.cqu.mealtime.util.UtilKt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    public static final int COMPLETED3 = -3;
    private String toastMsg;
    private FragmentHomeBinding binding;
    public static List<Canteen> canteens = new ArrayList<>();
    public static boolean infoOpened = false;
    InfoWinAdapter infoWinAdapter;
    CanteenAdapter canteenAdapter;
    MapView mapView;
    AMap aMap;
    static List<Marker> markers = new ArrayList<>();
    List<WeightedLatLng> latLngs = new ArrayList<>();
    ProgressBar progressBar;
    RecyclerView canteenList;
    Timer timer;
    TileOverlay tileOverlay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("&&&&&&&&&&&&onCreate");
        new Thread(this::queryCanteens).start();
        infoWinAdapter = new InfoWinAdapter(getContext(), this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("&&&&&&&&&&&&onCreateView");
        AMapLocationClient.updatePrivacyShow(getContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(getContext(), true);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        progressBar = binding.progressBar2;
        if (canteens.size() == 0)
            progressBar.setVisibility(View.VISIBLE);
        mapView = binding.map;
        TextView title = binding.titleText;
        AssetManager mgr = requireActivity().getAssets();
        Typeface tf = Typeface.createFromAsset(mgr, "fonts/SmileySans_Oblique.ttf");
        title.setTypeface(tf);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        aMap.setPointToCenter(540, 800);
        aMap.setInfoWindowAdapter(infoWinAdapter);
        LatLng latLng = new LatLng(29.593, 106.298);

        aMap.setOnMapClickListener(latLng1 -> infoWinAdapter.hideInfo());
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
        ImageView btRst = binding.buttonReset;
        ImageView btEdit = binding.buttonEdit;
        ImageView btPhoto = binding.buttonPhoto;
        btRst.setOnClickListener(v -> {
            infoWinAdapter.hideInfo();
            LatLng lt = aMap.getCameraPosition().target;
            long t = (long) (AMapUtils.calculateLineDistance(lt, latLng));
            t = t <= 1000 && t > 0 ? t : 500;
            aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 16, 0, 0)), t, new AMap.CancelableCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onCancel() {

                }
            });
        });
        btEdit.setOnClickListener(v -> {
        });
        btPhoto.setOnClickListener(v -> {
        });
        UtilKt.addClickScale(btRst, 0.8f, 100);
        UtilKt.addClickScale(btEdit, 0.8f, 100);
        UtilKt.addClickScale(btPhoto, 0.8f, 100);
        canteenList = binding.canteenList;
        canteenList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        canteenAdapter = new CanteenAdapter(getContext(), canteens);
        canteenAdapter.setOnItemClickListener(new CanteenAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position, View v) {
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(canteens.get(position).getLocation(), 18, 0, 0)), 500, new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        markers.get(position).showInfoWindow();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
            }

            @Override
            public void onLongClick(int position, View v) {

            }
        });
        canteenList.setAdapter(canteenAdapter);
        LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.list_anim);
        canteenList.setLayoutAnimation(layoutAnimationController);
        refresh();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    String response = doGet("http://140.210.194.87:8088/realtime", "");
                    if (response != null && !canteens.isEmpty()) {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++)
                            canteens.get(i).setFlow((int) (jsonArray.getDouble(i) / 3 * 40));
                        Message msg = new Message();
                        msg.what = COMPLETED3;
                        handler.sendMessage(msg);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
//        AMap.OnInfoWindowClickListener listener = marker -> Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main).navigate(R.id.action_navigation_home_to_navigation_dashboard);
//绑定信息窗点击事件
//        aMap.setOnInfoWindowClickListener(listener);
        timer = new Timer();
        timer.schedule(task, 1000, 1000); // 立即执行一次task，然后每隔2秒执行一次task
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mapView.onDestroy();
        timer.cancel();
        tileOverlay = null;
        System.out.println("&&&&&&&&&&&&onDestroyView");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("&&&&&&&&&&&&onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        infoWinAdapter.destroy();
        System.out.println("&&&&&&&&&&&&onDestroy");
    }

    private void queryCanteens() {
        String response = doGet("http://140.210.194.87:8088/canteens", "");
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        try {
            JSONArray jsonArray = new JSONArray(response);
            System.out.println("调用接口成功");
            JSONObject jsonObject;
            canteens.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                canteens.add(new Canteen(jsonObject.getString("canteenName"), jsonObject.getInt("canteenId"), jsonObject.getDouble("canteenLatitude"), jsonObject.getDouble("canteenLongitude"), jsonObject.getString("canteenHours"), jsonObject.getString("videoUrl")));
            }
            latLngs.clear();
            for (int i = 0; i < canteens.size(); i++) {
                latLngs.add(new WeightedLatLng(canteens.get(i).getLocation()));
            }
            toastMsg = "获取食堂信息成功";
            Message msg = new Message();
            msg.what = COMPLETED2;
            handler.sendMessage(msg);
            if (progressBar != null)
                progressBar.setVisibility(View.INVISIBLE);
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
                realtimeUpdate();
                if (infoOpened)
                    infoWinAdapter.updateFlow();
            }
        }
    };

    private void refresh() {
        canteenAdapter.notifyDataSetChanged();
        aMap.clear();
        markers.clear();
        for (int i = 0; i < canteens.size(); i++) {
            markers.add(aMap.addMarker(new MarkerOptions().position(canteens.get(i).getLocation()).title(canteens.get(i).getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.meal)).snippet(String.valueOf(i))));
        }
        canteenList.startLayoutAnimation();
    }

    private void realtimeUpdate() {
        for (int i = 0; i < canteens.size(); i++) {
            canteenAdapter.notifyItemChanged(i, R.id.canteen_num);
            canteenAdapter.notifyItemChanged(i, R.id.canteen_state);
        }
        for (int i = 0; i < canteens.size(); i++) {
            latLngs.set(i, new WeightedLatLng(canteens.get(i).getLocation(), 1 + (double) canteens.get(i).getFlow() / 200));
        }
        if (!latLngs.isEmpty() && tileOverlay == null) {
            HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
            builder.weightedData(latLngs).radius(50);
            HeatmapTileProvider heatmapTileProvider = builder.build();
            // 初始化 TileOverlayOptions
            TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();
            tileOverlayOptions.tileProvider(heatmapTileProvider); // 设置瓦片图层的提供者
            // 向地图上添加 TileOverlayOptions 类对象
            tileOverlay = aMap.addTileOverlay(tileOverlayOptions);
//            if (tileOverlay != null)
//                tileOverlay.remove();
//            tileOverlay = t;
        }
    }

    public void closeInfo() {
        for (Marker marker : markers) {
            marker.hideInfoWindow();
            infoWinAdapter.stopPlayer();
            infoOpened = false;
        }
    }

    static class CanteenAdapter extends RecyclerView.Adapter<CanteenAdapter.Vh> {
        private final Context context;
        public List<Canteen> canteensList;

        public CanteenAdapter(Context context, List<Canteen> canteensList) {
            this.context = context;
            this.canteensList = canteensList;
        }

        @NonNull
        @Override
        public CanteenAdapter.Vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Vh(LayoutInflater.from(context).inflate(R.layout.canteen_card, null));
        }

        @Override
        public void onBindViewHolder(CanteenAdapter.Vh holder, final int position) {
            holder.itemName.setText(canteensList.get(position).getName());
            holder.itemTime.setText("营业时间：\n" + canteensList.get(position).getTime());
            holder.itemNum.setText(String.valueOf(canteensList.get(position).getFlow()));
            holder.itemState.setText(canteensList.get(position).getState());
            holder.itemState.setTextColor(canteensList.get(position).getColor());
            holder.itemNum.setTextColor(canteensList.get(position).getColor());

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

        private OnItemClickListener mOnItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
        }

        @Override
        public int getItemCount() {
            return canteensList.size();
        }

        static class Vh extends RecyclerView.ViewHolder {
            private final TextView itemName;
            private final TextView itemState;
            private final TextView itemNum;
            private final TextView itemTime;

            public Vh(View itemView) {
                super(itemView);
                UtilKt.addClickScale(itemView, 0.9f, 150);
                itemName = itemView.findViewById(R.id.canteen_name);
                itemState = itemView.findViewById(R.id.canteen_state);
                itemNum = itemView.findViewById(R.id.canteen_num);
                itemTime = itemView.findViewById(R.id.canteen_time);
            }
        }
    }
}