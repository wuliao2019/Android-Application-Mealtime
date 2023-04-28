package com.cqu.mealtime;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.cqu.mealtime.ui.home.HomeFragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class InfoWinAdapter implements AMap.InfoWindowAdapter, View.OnClickListener {
    View infoWindow = null;
    private final Context mContext;

    ExoPlayer player;
    Uri uri = Uri.parse("rtmp://1.14.46.81/live/canteen1");
    RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
    MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));

    public InfoWinAdapter(Context context) {
        mContext = context;
        player = new ExoPlayer.Builder(mContext).setMediaSourceFactory(new DefaultMediaSourceFactory(mContext).setLiveTargetOffsetMs(1000).setLiveMaxOffsetMs(3000).setLiveMaxSpeed(1.1f)).build();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        if (infoWindow == null)
            infoWindow = LayoutInflater.from(mContext).inflate(R.layout.infowindow_card, null);
        render(marker, infoWindow);
        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null; //因为是自定义的布局，返回null
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.iv_left:
//                Toast.makeText(mContext,"我是左边按钮点击事件", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.iv_right:
//                Toast.makeText(mContext,"我是右边按钮点击事件",Toast.LENGTH_SHORT).show();
//                break;
    }

    public void render(Marker marker, View view) {
        System.out.println("&&&&&&&&&&&&renderCall");
        TextView titleTXT = view.findViewById(R.id.location_name);
        TextView stateTXT = view.findViewById(R.id.location_state);
        TextView flowTXT = view.findViewById(R.id.location_flow);
        TextView timeTXT = view.findViewById(R.id.location_time);
        //将显示控件绑定ExoPlayer
        StyledPlayerView styledPlayerView = view.findViewById(R.id.playerView);
        styledPlayerView.setPlayer(player);
        player.setMediaSource(videoSource);
        player.setPlayWhenReady(true);
        player.prepare();
        titleTXT.setText(marker.getTitle());
        String temps = marker.getSnippet();
        if (temps!=null){
            int temp=Integer.parseInt(temps);
            stateTXT.setText(HomeFragment.canteens.get(temp).getState());
            stateTXT.setTextColor(HomeFragment.canteens.get(temp).getColor());
            flowTXT.setText(String.valueOf(HomeFragment.canteens.get(temp).getFlow()));
            flowTXT.setTextColor(HomeFragment.canteens.get(temp).getColor());
            timeTXT.setText("营业时间：" + HomeFragment.canteens.get(temp).getTime());
        }
    }

    public void destroy() {
        player.release();
    }
}