package com.cqu.mealtime;

import static com.cqu.mealtime.util.RequestUtil.doGet;
import static com.cqu.mealtime.util.RequestUtil.doRequest;
import static com.cqu.mealtime.util.RequestUtil.getPic;
import static com.cqu.mealtime.util.RequestUtil.uriToFile;
import static com.cqu.mealtime.util.RequestUtil.urlEncode;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadActivity extends AppCompatActivity {
    public static final int[] COMPLETED = {-1, -2, -3, -4, -5, -6};
    private String toastMsg, people_count;
    OptionsPickerView pvOptions;
    CardView cardView;
    TextView textView, tips_txt, result_txt;
    ImageView imageView1, imageView2;
    ProgressBar progressBar;
    Button buttonUpload, buttonSubmit;
    List<String> canteens = new ArrayList<>();
    List<List<String>> stall_names;
    List<List<Integer>> stall_ids;
    int limit_can = 0, limit_stall = 0;
    // 照片所在的Uri地址
    private Uri imageUri;
    Bitmap bitmap;
    private ActivityResultLauncher<Intent> launcherAlbum, launcherCamera;

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
        if (ContextCompat.checkSelfPermission(UploadActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(UploadActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)   //权限还没有授予，需要在这里写申请权限的代码
            // 第二个参数是一个字符串数组，里面是需要申请的权限 可以设置申请多个权限，最后一个参数标志这次申请的权限，该常量在onRequestPermissionsResult中使用到
            ActivityCompat.requestPermissions(UploadActivity.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        launcherAlbum = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == UploadActivity.RESULT_OK) {
                Intent data = result.getData();
                try {
                    assert data != null;
                    imageUri = data.getData();
                    Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    imageView1.setImageBitmap(bit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TextView txt = findViewById(R.id.tips_text);
                txt.setVisibility(View.INVISIBLE);
                buttonUpload.setEnabled(true);
                buttonUpload.setAlpha(1f);
            }
        });
        launcherCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == UploadActivity.RESULT_OK) {
                try {
                    Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    imageView1.setImageBitmap(bit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                TextView txt = findViewById(R.id.tips_text);
                txt.setVisibility(View.INVISIBLE);
                buttonUpload.setEnabled(true);
                buttonUpload.setAlpha(1f);
            }
        });
        progressBar = findViewById(R.id.progressBar);
        cardView = findViewById(R.id.button_choose2);
        ImageView backBt = findViewById(R.id.bottom_back2);
        backBt.setOnClickListener(v -> finish());
        UtilKt.addClickScale(cardView, 0.9f, 100);
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
        tips_txt = findViewById(R.id.tips_text2);
        result_txt = findViewById(R.id.people_count);
        UtilKt.addClickScale(imageView1, 0.9f, 150);
        buttonUpload = findViewById(R.id.button_upload);
        buttonSubmit = findViewById(R.id.button_submit);
        buttonUpload.setOnClickListener(v -> {
            if (imageUri != null) {
                progressBar.setVisibility(View.VISIBLE);
                tips_txt.setVisibility(View.VISIBLE);
                tips_txt.setText("正在上传图片");
                new Thread(this::doPostAImage).start();
            } else {
                toastMsg = "未选择图片";
                Message msg = new Message();
                msg.what = COMPLETED[0];
                handler.sendMessage(msg);
            }
        });
        buttonSubmit.setOnClickListener(v -> {
            if (limit_can == 0 || limit_stall == 0) {
                toastMsg = "请选择具体的档口";
                Toast.makeText(UploadActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            } else
                new Thread(this::submit).start();
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
            msg.what = COMPLETED[2];
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void submit() {
        try {
            Map<String, Object> params = new HashMap<>();//组合参数
            params.put("stallId", stall_ids.get(limit_can).get(limit_stall));
            params.put("peopleCount", people_count);
            doRequest("PUT", getResources().getString(R.string.server_url) + "stalls", urlEncode(params));
            toastMsg = "已提交人流量信息";
            Message msg = new Message();
            msg.what = COMPLETED[5];
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED[0]) {
                Toast.makeText(UploadActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            } else if (msg.what == COMPLETED[1]) {
                finish();
            } else if (msg.what == COMPLETED[2]) {
                initLimit();
            } else if (msg.what == COMPLETED[3]) {
                progressBar.setVisibility(View.INVISIBLE);
                tips_txt.setVisibility(View.INVISIBLE);
                imageView2.setImageBitmap(bitmap);
                result_txt.setText("人数：" + people_count);
                buttonUpload.setEnabled(false);
                buttonUpload.setAlpha(.5f);
                buttonSubmit.setAlpha(1f);
                buttonSubmit.setEnabled(true);
            } else if (msg.what == COMPLETED[4])
                tips_txt.setText("正在接收结果");
            else if (msg.what == COMPLETED[5]) {
                Toast.makeText(UploadActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                finish();
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
            if (which == 0)
                takePhoto();
            else
                choosePhoto();
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
            launcherCamera.launch(intentToTakePhoto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void choosePhoto() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        launcherAlbum.launch(intentToPickPic);
    }

    private void doPostAImage() {//post提交param参数
        try {
            OkHttpClient client = new OkHttpClient();
            File file = uriToFile(imageUri, this);
            Log.i("photoSrc: ", file.getPath());
            if (!file.exists()) {
//                Toast.makeText(NetUtil_image.this, "文件不存在", Toast.LENGTH_SHORT).show();
                System.out.println("doPostAImage失败");
            } else {
//                RequestBody requestBody2 = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                RequestBody multipartBody = new MultipartBody.Builder()
                        //一定要设置这句
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "imageOut.png", RequestBody.create(file, MediaType.parse("image/*")))
                        .build();
                final Request request = new Request.Builder()
                        .url(getResources().getString(R.string.server_url) + "realtime/photo")
                        .post(multipartBody)
                        .build();
                Response response = client.newCall(request).execute();//执行
                String result = response.body().string();
                Log.i("UploadPhoto: Done!", result);
                JSONObject jsonObject = new JSONObject(result);
                people_count = jsonObject.getString("num");
                Message msg = new Message();
                msg.what = COMPLETED[4];
                handler.sendMessage(msg);
                bitmap = getPic(getResources().getString(R.string.server_url) + "realtime/getImg");
                msg = new Message();
                msg.what = COMPLETED[3];
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}