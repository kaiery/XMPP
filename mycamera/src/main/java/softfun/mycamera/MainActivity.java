package softfun.mycamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.UUID;

import softfun.mycamera.application.ImageLoaderUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int permsRequestCode = 200;
    private Button mBtn0;
    private Button mBtn1;
    private ImageView mIv;
    private File mTempFile;

    private void assignViews() {
        mBtn0 = (Button) findViewById(R.id.btn0);
        mBtn1 = (Button) findViewById(R.id.btn1);
        mIv = (ImageView) findViewById(R.id.iv);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assignViews();

        mBtn0.setOnClickListener(this);
        mBtn1.setOnClickListener(this);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1){
            getPermission();
        }
    }

    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.btn0:
                String path = Environment.getExternalStorageDirectory().getPath();
                mTempFile = new File(path, UUID.randomUUID().toString()+".jpg");
                Uri uri = Uri.fromFile(mTempFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                startActivityForResult(intent,1);
                break;
            case R.id.btn1:
                Intent intent1 = new Intent(MainActivity.this,CustomCamera.class);
                startActivityForResult(intent1,0);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==1){
                String imageUri = "file://"+mTempFile;
                ImageLoader.getInstance().displayImage(imageUri,mIv, ImageLoaderUtils.getOptions_NoCacheInMem_NoCacheInDisk_Exif_EXACTLY());
            }else if(requestCode==0){
                String capture_file = data.getStringExtra(CustomCamera.CAPTURE_FILE);
                //String imageUri = "file://"+capture_file;
                try {
                    FileInputStream fis = new FileInputStream(capture_file);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(-90f);
                    matrix.postScale(-1, 1);
                    matrix.postTranslate(bitmap.getWidth(),0);
                    bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                    mIv.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //ImageLoader.getInstance().displayImage(imageUri,mIv, ImageLoaderUtils.getOptions_NoCacheInMem_NoCacheInDisk_Exif_EXACTLY());
            }

        }
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    }, permsRequestCode );
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 200:
                boolean cameraAccepted = grantResults[2]==PackageManager.PERMISSION_GRANTED;
                if(cameraAccepted){
                    //授权成功之后，调用系统相机进行拍照操作等
                }else{

                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
