package softfun.mycamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class CustomCamera extends AppCompatActivity implements View.OnClickListener ,SurfaceHolder.Callback{


    public static final String CAPTURE_FILE = "capture_file";
    private SurfaceHolder mHolder;
    private SurfaceView mSfv;
    private Button mBtnCapture;
    private Camera mCamera;
    protected int cameraOrientation;

    private void assignViews() {
        mSfv = (SurfaceView) findViewById(R.id.sfv);
        mBtnCapture = (Button) findViewById(R.id.btn_capture);
        mHolder = mSfv.getHolder();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);

        assignViews();
        mHolder.addCallback(this);
        mBtnCapture.setOnClickListener(this);

        listener = new MyOrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL);
        if(listener.canDetectOrientation()){
            listener.enable();
        }else{
            Toast.makeText(this,"无法启用感应器",Toast.LENGTH_SHORT).show();
        }
    }

    private void openFrontFacingCameraGingerbread() {
        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    mCamera = Camera.open(camIdx);
                    cameraOrientation = cameraInfo.orientation;
                    applyDeviceRotation();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void applyDeviceRotation(){
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        cameraOrientation += degrees;
        cameraOrientation = cameraOrientation%360;
    }

    /**
     * 获取相机对象
     * @return
     */
    private Camera getCamera(){
        try {
            //mCamera = Camera.open();
            openFrontFacingCameraGingerbread();
            mCamera.setDisplayOrientation((cameraOrientation + 180)%360);
        } catch (Exception e) {
            mCamera = null;
            e.printStackTrace();
        }
        return mCamera;
    }


    /**
     * 图像预览
     */
    private void setStartPreview(Camera camera,SurfaceHolder holder){
        //将相机与surfaceView绑定
        try {
            camera.setPreviewDisplay(holder);
//            Camera.Parameters parameters = mCamera.getParameters();
            //parameters.setPictureFormat(ImageFormat.JPEG);

//            if (this.getResources().getConfiguration().orientation !=    Configuration.ORIENTATION_LANDSCAPE) {
//                parameters.set("orientation", "portrait");
//                parameters.setRotation(90);
//                mCamera.setDisplayOrientation(90);
//                System.out.println("====================  parameters.setRotation(90)  =====================");
//            }else{
//                parameters.set("orientation", "landscape");
//                parameters.setRotation(0);
//                mCamera.setDisplayOrientation(0);
//                System.out.println("====================  parameters.setRotation(0)  =====================");
//            }
//            //parameters.setJpegQuality(100);
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//            mCamera.setParameters(parameters);

            //开始预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 释放相机
     */
    private void releaseCamera(){
        if(mCamera!=null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    @Override
    public void onClick(View v) {
        switch( v.getId() )
        {
            case R.id.btn_capture:
                capture();
                break;
            default:

                break;
        }
    }

    /**
     * 拍照
     */
    private void capture() {
        mCamera.autoFocus( new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
                    mCamera.takePicture(null,null,mPictureCallback);
                }
            }
        });
    }



    /**
     * 拍摄回调
     */
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String path = Environment.getExternalStorageDirectory().getPath();
            File tempFile = new File(path, UUID.randomUUID().toString()+".jpg");
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(data);
                fos.close();
                System.out.println("====================  filepath  ===================== "+tempFile.getAbsolutePath());

                Intent result = new Intent();
                result.putExtra(CAPTURE_FILE, tempFile.getAbsolutePath());
                setResult(RESULT_OK, result);
                finish();//关闭当前的界面  ★一定记得关闭界面,数据才会被返回
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera==null){
            mCamera = getCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        listener.disable();
    }


    private MyOrientationEventListener listener;
    private class MyOrientationEventListener extends OrientationEventListener{
        public MyOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            System.out.println("====================  onOrientationChanged  =====================:"+orientation);
        }
    }

    /*====================  使用surfaceView的时候必须要实现SurfaceHolder.Callback  =====================*/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera,mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera,mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
    /*====================  end  =====================*/


}
