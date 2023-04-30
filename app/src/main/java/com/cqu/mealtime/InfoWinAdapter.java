package com.cqu.mealtime;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.cqu.mealtime.ui.home.HomeFragment;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class InfoWinAdapter implements AMap.InfoWindowAdapter, View.OnClickListener {
    View infoWindow = null;
    private final Context mContext;
    ExoPlayer player;
    TextView titleTXT;
    TextView stateTXT;
    TextView flowTXT;
    TextView timeTXT;
    int index = -1;
    RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
    ValueAnimator animator = ValueAnimator.ofInt(0, 0);
    ValueAnimator animator2 = ValueAnimator.ofInt(0, 0);

    public InfoWinAdapter(Context context) {
        mContext = context;
        animator.setDuration(300);
        animator.addUpdateListener(valueAnimator -> flowTXT.setText(animator.getAnimatedValue().toString()));
        animator2.setDuration(300);
        animator2.setEvaluator(new ArgbEvaluator());
        animator2.addUpdateListener(valueAnimator -> {
            stateTXT.setTextColor((int) animator2.getAnimatedValue());
            flowTXT.setTextColor((int) animator2.getAnimatedValue());
        });
        player = new ExoPlayer.Builder(mContext).setMediaSourceFactory(new DefaultMediaSourceFactory(mContext).setLiveTargetOffsetMs(1000).setLiveMaxOffsetMs(3000).setLiveMaxSpeed(1.5f)).build();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        if (infoWindow == null) {
            infoWindow = LayoutInflater.from(mContext).inflate(R.layout.infowindow_card, null);
            titleTXT = infoWindow.findViewById(R.id.location_name);
            stateTXT = infoWindow.findViewById(R.id.location_state);
            flowTXT = infoWindow.findViewById(R.id.location_flow);
            timeTXT = infoWindow.findViewById(R.id.location_time);
            //将显示控件绑定ExoPlayer
            StyledPlayerView styledPlayerView = infoWindow.findViewById(R.id.playerView);
            styledPlayerView.setPlayer(player);
            player.setPlayWhenReady(true);
        }
        String temps = marker.getSnippet();
        if (temps != null && Integer.parseInt(temps) != index) {
            player.stop();
            index = Integer.parseInt(temps);
            titleTXT.setText(marker.getTitle());
            stateTXT.setText(HomeFragment.canteens.get(index).getState());
            stateTXT.setTextColor(HomeFragment.canteens.get(index).getColor());
            flowTXT.setText(String.valueOf(HomeFragment.canteens.get(index).getFlow()));
            flowTXT.setTextColor(HomeFragment.canteens.get(index).getColor());
            timeTXT.setText("营业时间：" + HomeFragment.canteens.get(index).getTime());
            Uri uri = Uri.parse(HomeFragment.canteens.get(index).getV_url());
            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri));
            player.setMediaSource(videoSource);
        }
        if (temps != null)
            HomeFragment.infoOpened = true;
        player.prepare();
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


    public void destroy() {
        player.stop();
        player.release();
    }

    public void stopPlayer() {
        player.stop();
    }

    public void updateFlow() {
        if (stateTXT != null && flowTXT != null && index >= 0) {
            animator.setIntValues(Integer.parseInt(flowTXT.getText().toString()), HomeFragment.canteens.get(index).getFlow());
            animator.start();
            animator2.setIntValues(stateTXT.getCurrentTextColor(), HomeFragment.canteens.get(index).getColor());
            animator2.start();
            stateTXT.setText(HomeFragment.canteens.get(index).getState());
        }
    }
}