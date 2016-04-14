package com.ypf.frescoutil;

import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;
    Button btn5;
    Button btn6;
    Button btn7;
    SimpleDraweeView sv1;
    SimpleDraweeView sv2;
    SimpleDraweeView sv3;
    SimpleDraweeView sv4;
    SimpleDraweeView sv5;
    SimpleDraweeView sv6;
    SimpleDraweeView sv7;
    FrescoUtil fresco;
    private static final String urlNet = "http://fresco-cn.org/static/fresco-logo.png";
    private static final String urlNetgif = "https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=3150058589,46150701&fm=116&gp=0.jpg";
    private static final String urlLocal = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat("kuaicha").concat("/").concat("/image/").concat(File.separator).concat("1460536040684share.jpg");
    private static final String urlLocal_gif = "file://" + Environment.getExternalStorageDirectory().getAbsolutePath().concat("/").concat("kuaicha").concat("/").concat("/image/").concat(File.separator).concat("hand.gif");
    private static final String packageName = "res://com.ypf.frescoutil/";
    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fresco = FrescoUtil.getInstance(this, "");
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);
        btn5 = (Button) findViewById(R.id.btn5);
        btn6 = (Button) findViewById(R.id.btn6);
        btn7 = (Button) findViewById(R.id.btn7);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        sv1 = (SimpleDraweeView) findViewById(R.id.sv1);
        sv2 = (SimpleDraweeView) findViewById(R.id.sv2);
        sv3 = (SimpleDraweeView) findViewById(R.id.sv3);
        sv4 = (SimpleDraweeView) findViewById(R.id.sv4);
        sv5 = (SimpleDraweeView) findViewById(R.id.sv5);
        sv6 = (SimpleDraweeView) findViewById(R.id.sv6);
        sv7 = (SimpleDraweeView) findViewById(R.id.sv7);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                //网络
                fresco.displayPicFromNet(sv1, urlNet, ImageRequest.ImageType.SMALL, false);
                break;
            case R.id.btn2:
                //本地
                fresco.displayPicFromLocal(sv2, urlLocal, ImageRequest.ImageType.SMALL, false);
                break;
            case R.id.btn3:
                //网络监听
                ControllerListener<ImageInfo> listener = new BaseControllerListener<ImageInfo>() {

                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        Toast.makeText(MainActivity.this, "加载结束", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFinalImageSet");
                    }

                    @Override
                    public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                        Log.d(TAG, "onIntermediateImageSet");
                    }


                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure");
                    }

                };
                fresco.displayPicFromNetWithListener(sv3, urlNet, ImageRequest.ImageType.SMALL, listener, false);
                break;
            case R.id.btn4:
                //资源
                fresco.displayPicFromResource(sv4, packageName, R.mipmap.ic_launcher, false);
                break;
            case R.id.btn5:
                //网络动图
                fresco.displayPicFromNet(sv5, urlNetgif, ImageRequest.ImageType.SMALL, true);
                break;
            case R.id.btn6:
                //本地动图
                fresco.displayPicFromLocal(sv6, urlLocal_gif, ImageRequest.ImageType.SMALL, true);
                break;
            case R.id.btn7:
                //资源
                fresco.displayPicFromResource(sv7, packageName, R.mipmap.welcome, true);
                break;
        }
    }
}
