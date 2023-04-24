package com.example.nju_norun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.provider.ProviderProperties;
import android.os.Bundle;
import android.os.Process;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {
    private static final long MIN_TIME_BETWEEN_UPDATES = 10;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 2.5f;
    private static boolean isProvidedPermission = false;
    private static String provider = null;


    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        Switch switchSimulateGps = (Switch) findViewById(R.id.switch_start_simulate_gps);
        switchSimulateGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            private LocationManager mLocationManager;
            private String mProviderName;
            private SimulatedLocationProvider mSimulatedLocationProvider;
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 启动模拟GPS定位

                    if (startMockPosition()) {
                        //TODO：输出成功信息，同时开启GPS模拟
                        mLocationManager.addTestProvider(mProviderName, false, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_HIGH, ProviderProperties.ACCURACY_FINE);
                        mLocationManager.setTestProviderEnabled(mProviderName, true);
                        mSimulatedLocationProvider = new SimulatedLocationProvider(mLocationManager, mProviderName);
                        mSimulatedLocationProvider.start();

                    } else {
                        //TODO：没有打开定位服务，输出打开定位服务的信息，同时关闭按钮
                        mLocationManager.removeTestProvider(mProviderName);
                        mSimulatedLocationProvider.interrupt();

                    }
                } else {
                    // TODO：停止模拟GPS定位
                    mLocationManager.removeTestProvider(mProviderName);
                    mSimulatedLocationProvider.interrupt();
                }
            }
        });
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // TODO：处理位置更新事件
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO：处理位置状态变化事件
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO:处理位置提供程序启用事件
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO:处理位置提供程序禁用事件
        }
    };

    public boolean startMockPosition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGpsEnabled && !isNetworkEnabled) return false;
        //选择位置提供者
        provider = LocationManager.GPS_PROVIDER;
        if (!isGpsEnabled && isNetworkEnabled) {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 权限：ACCESS_FINE_LOCATION && ACCESS_COARSE_LOCATION
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("为了实现GPS模拟，我们需要获取您的位置信息。");
                builder.setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 请求权限
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 666);
                    }
                });
                builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户已拒绝权限,退出应用
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
                builder.show();
            } else {
                // 请求权限
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 666);
            }
        } else {
            isProvidedPermission = true;
        }
        //已经获得了权限,实时更新位置
        if (isProvidedPermission)
            locationManager.requestLocationUpdates(provider, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 666) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 用户已授予权限,开始实时位置请求
                isProvidedPermission = true;
            } else {
                // 用户已拒绝权限,退出程序
                Process.killProcess(Process.myPid());
            }
        }
    }

}


