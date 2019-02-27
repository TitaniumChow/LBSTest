package com.example.chow.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);

        mTextureView = findViewById(R.id.texture_view);
        initView();

        positionText = findViewById(R.id.position_text_view);

        List<String>permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if(!permissionList.isEmpty()){
            String[]permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }else{
            requestLocation();
        }
    }

    private void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(1000);   //setScanSpam参数单位为毫秒ms 5000-->5s 设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }


    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length > 0){
                    for(int result : grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else{Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener{

        @Override
        public void onReceiveLocation(BDLocation Location){
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("Latitude：").append(Location.getLatitude()).append("\n");
            currentPosition.append("Longitude：").append(Location.getLongitude()).append("\n");
            currentPosition.append("Country：").append(Location.getCountry()).append("\n");
            currentPosition.append("Province：").append(Location.getProvince()).append("\n");
            currentPosition.append("City：").append(Location.getCity()).append("\n");
            currentPosition.append("District：").append(Location.getDistrict()).append("\n");
            currentPosition.append("Street：").append(Location.getStreet()).append("\n");
            currentPosition.append("Speed：").append(Location.getSpeed()).append("\n");  //单位km/h
            currentPosition.append("Direction：").append(Location.getDirection()).append("\n");  //单位 度
            currentPosition.append("Located By：");
            if(Location.getLocType()==BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }else if(Location.getLocType()==BDLocation.TypeNetWorkLocation){
                currentPosition.append("Network");
            }
            positionText.setText(currentPosition);
        }

    }
    private Camera mCamera;

    private TextureView mTextureView;

    private void initView() {
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startCamera(surface);
                Log.d("TAG","StartCamera");
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void startCamera(SurfaceTexture surface) {
        Camera.Parameters camParameters = mCamera.getParameters();

        List<Camera.Size> sizes = camParameters.getSupportedPreviewSizes();
        // Usually the highest quality
        Camera.Size s = sizes.get(0);

        camParameters.setPreviewSize(s.width, s.height);
        camParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        if (camParameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
        {
            camParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(camParameters);

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(0, info);

        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = (info.orientation - ORIENTATION_MAP.get(orientation) + 360) % 360;

        mCamera.setDisplayOrientation(degrees);

        try
        {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }



    private static final SparseIntArray ORIENTATION_MAP = new SparseIntArray();
    static
    {
        ORIENTATION_MAP.put(Surface.ROTATION_0, 0);
        ORIENTATION_MAP.put(Surface.ROTATION_90, 90);
        ORIENTATION_MAP.put(Surface.ROTATION_180, 180);
        ORIENTATION_MAP.put(Surface.ROTATION_270, 270);
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG","ObtainCamera");
        obtainCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TAG","ReleaseCamera");
        releaseCamera();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mLocationClient.stop();
        releaseCamera();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("TAG", "onRestart");
        setContentView(R.layout.activity_main);
        mTextureView = findViewById(R.id.texture_view);
        initView();
        positionText = findViewById(R.id.position_text_view);
        mLocationClient.restart();
        Log.d("TAG", "LocRestart");
    }

    private void obtainCamera() {
        if (mCamera == null)
        {
            mCamera = Camera.open(0);
            //Camera.open(1) to open front camera and i just dont want to see my ugly face on the screen
        }
    }

    private void releaseCamera() {
        if (mCamera != null)
        {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();

            mCamera = null;
        }
    }

}

