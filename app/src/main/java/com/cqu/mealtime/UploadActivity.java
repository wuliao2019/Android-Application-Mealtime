package com.cqu.mealtime;

import static com.cqu.mealtime.util.RequestUtil.doGet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.cqu.mealtime.util.UtilKt;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UploadActivity extends AppCompatActivity {
    public static final int COMPLETED = -1;
    public static final int COMPLETED2 = -2;
    public static final int COMPLETED3 = -3;
    private String toastMsg;
    OptionsPickerView pvOptions;
    CardView cardView;
    TextView textView;
    ImageView imageView1;
    ImageView imageView2;
    List<String> canteens = new ArrayList<>();
    List<List<String>> stall_names;
    List<List<Integer>> stall_ids;
    int limit_can = 0;
    int limit_stall = 0;
    // 拍照回传码
    public final static int CAMERA_REQUEST_CODE = 0;
    // 相册选择回传吗
    public final static int GALLERY_REQUEST_CODE = 1;
    // 照片所在的Uri地址
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        getWindow().setStatusBarColor(getColor(R.color.page_back));
        if ((this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        else
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        new Thread(this::queryList).start();
        cardView = findViewById(R.id.button_choose2);
        ImageView backBt = findViewById(R.id.bottom_back2);
        backBt.setOnClickListener(v -> finish());
        UtilKt.addClickScale(cardView, 0.9f, 150);
        textView = findViewById(R.id.choose_loc2);
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
        imageView1 = findViewById(R.id.src_photo);
        imageView2 = findViewById(R.id.result_photo);
        imageView1.setOnClickListener(v -> showChooseDialog());
    }

    private void queryList() {
        try {
            String response = doGet("http://140.210.194.87:8088/canteens", "");
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
            response = doGet("http://140.210.194.87:8088/stalls", "");
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
                Toast.makeText(UploadActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
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

    private void showChooseDialog() {
        String[] string = {"相机", "从相册中选取"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(UploadActivity.this);
        dialog.setTitle("选择图片");
        dialog.setItems(string, (dialog1, which) -> {
            switch (which) {
                case 0:
                    //第二个参数是需要申请的权限
                    if (ContextCompat.checkSelfPermission(UploadActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(UploadActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {   //权限还没有授予，需要在这里写申请权限的代码
                        // 第二个参数是一个字符串数组，里面是需要申请的权限 可以设置申请多个权限，最后一个参数标志这次申请的权限，该常量在onRequestPermissionsResult中使用到
                        ActivityCompat.requestPermissions(UploadActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
                    } else { //权限已经被授予，在这里直接写要执行的相应方法即可
                        takePhoto();
                    }
                    break;
                case 1:
                    if (ContextCompat.checkSelfPermission(UploadActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {   //权限还没有授予，需要在这里写申请权限的代码
                        // 第二个参数是一个字符串数组，里面是需要申请的权限 可以设置申请多个权限，最后一个参数标志这次申请的权限，该常量在onRequestPermissionsResult中使用到
                        ActivityCompat.requestPermissions(UploadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQUEST_CODE);
                    } else { //权限已经被授予，在这里直接写要执行的相应方法即可
                        choosePhoto();
                    }
                    break;
                default:
                    break;
            }
        });
        dialog.show();
    }

    private void takePhoto() {
        // 跳转到系统的拍照界面
        // 拍照的照片的存储位置
        String mTempPhotoPath = Environment.getExternalStorageDirectory() + File.separator + "photo.jpeg";
        File output = new File(mTempPhotoPath);
        try//判断图片是否存在，存在则删除在创建，不存在则直接创建
        {
            if (!Objects.requireNonNull(output.getParentFile()).exists())
                output.getParentFile().mkdirs();
            if (output.exists())
                output.delete();
            output.createNewFile();
            imageUri = FileProvider.getUriForFile(UploadActivity.this, UploadActivity.this.getApplicationContext().getPackageName() + ".my.provider", output);
            Intent intentToTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intentToTakePhoto.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intentToTakePhoto, CAMERA_REQUEST_CODE);
            //调用会返回结果的开启方式，返回成功的话，则把它显示出来
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView fragment4ImageView0 = findViewById(R.id.src_photo);
        if (resultCode == UploadActivity.RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE -> {
                    // 获得图片
                    try {
                        //该uri就是照片文件夹对应的uri
                        Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        fragment4ImageView0.setImageBitmap(bit);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                case GALLERY_REQUEST_CODE -> {
                    // 获取图片
                    try {
                        //该uri是上一个Activity返回的
                        imageUri = data.getData();
                        if (imageUri != null) {
                            Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            fragment4ImageView0.setImageBitmap(bit);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            TextView txt = findViewById(R.id.tips_text);
            txt.setVisibility(View.INVISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
