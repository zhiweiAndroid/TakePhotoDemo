package sina.com.takephotodemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.model.TakePhotoOptions;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TakePhoto.TakeResultListener,InvokeListener {

    private Button mGallery;
    private Button mPhoto;
    private TextView mTv;
    private ImageView mIv;
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;
    private CropOptions cropOptions;
    private CompressConfig compressConfig;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initListener();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        mGallery = (Button) findViewById(R.id.btn_gallery);
        mPhoto = (Button) findViewById(R.id.btn_photo);
        mTv = (TextView) findViewById(R.id.tv_uri);
        mIv = (ImageView) findViewById(R.id.iv);


    }

    private void initListener() {
       mGallery.setOnClickListener(this);
       mPhoto.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_gallery:
                imageUri = getImageCropUri();
                //从相册中选取图片并裁剪
                takePhoto.onPickFromGalleryWithCrop(imageUri, cropOptions);
                //从相册中选取不裁剪
              //  takePhoto.onPickFromGallery();
                break;
            case R.id.btn_photo:
                imageUri = getImageCropUri();
                //拍照并裁剪
                takePhoto.onPickFromCaptureWithCrop(imageUri, cropOptions);
                //仅仅拍照不裁剪
               // takePhoto.onPickFromCapture(imageUri);
                break;
        }

    }

    @Override
    public void takeSuccess(TResult result) {
        String iconPath = result.getImage().getOriginalPath();
        //Toast显示图片路径
        if (!TextUtils.isEmpty(iconPath)){
            mTv.setText(iconPath);
            Glide.with(this).load(iconPath).into(mIv);
        }
        //Google Glide库 用于加载图片资源
    }

    @Override
    public void takeFail(TResult result, String msg) {
        Toast.makeText(MainActivity.this, "Error:" + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void takeCancel() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type=PermissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissionManager.handlePermissionsResult(this,type,invokeParam,this);
    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type=PermissionManager.checkPermission(TContextWrap.of(this),invokeParam.getMethod());
        if(PermissionManager.TPermissionType.WAIT.equals(type)){
            this.invokeParam=invokeParam;
        }
        return type;
    }

    /**
     *  获取TakePhoto实例
     * @return
     */
    public TakePhoto getTakePhoto(){
        if (takePhoto==null){
            takePhoto= (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this,this));
        }
        return takePhoto;
    }

    private void initData() {
        //获取TakePhoto实例
        takePhoto = getTakePhoto();
        //设置裁剪参数
        TakePhotoOptions.Builder builder=new TakePhotoOptions.Builder();
        //是否需要takephoto自带相册
        builder.setWithOwnGallery(true);
        //是否需要保存原图
        builder.setCorrectImage(true);
        takePhoto.setTakePhotoOptions(builder.create());
        cropOptions = new CropOptions.Builder().setAspectX(100).setAspectY(100).setWithOwnCrop(false).create();
        //设置压缩参数
        compressConfig=new CompressConfig.Builder().setMaxSize(50*800).setMaxPixel(800).enableReserveRaw(true).create();
        takePhoto.onEnableCompress(compressConfig,true);  //设置为需要压缩
    }

    //获得照片的输出保存Uri
    private Uri getImageCropUri() {
        File file=new File(Environment.getExternalStorageDirectory(), "/temp/"+System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists())file.getParentFile().mkdirs();
        return Uri.fromFile(file);
    }
}
