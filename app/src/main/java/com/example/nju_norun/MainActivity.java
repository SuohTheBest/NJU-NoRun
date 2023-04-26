package com.example.nju_norun;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.location.provider.ProviderProperties;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {

    private static final long MIN_TIME_BETWEEN_UPDATES = 10;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 2.5f;
    private static boolean isProvidedPermission = false;


    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_main);
        Switch switchSimulateGps = (Switch) findViewById(R.id.switch_start_simulate_gps);
        TextView textShowGPS = findViewById(R.id.text_show_gps);
        TextView textShowStatus = findViewById(R.id.text_2);
        if (!checkGPSEnabled()) {
            textShowStatus.setText("位置权限未打开，请打开位置权限！");
            textShowStatus.setTextColor(Color.RED);
        }
        startTrackPosition();
        switchSimulateGps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            private SimulatedLocationProvider mSimulatedLocationProvider;

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //输出成功信息，同时开启GPS模拟
                    mSimulatedLocationProvider = new SimulatedLocationProvider((LocationManager) getSystemService(Context.LOCATION_SERVICE), LocationManager.GPS_PROVIDER);
                    textShowStatus.setText("开始模拟！");
                    textShowStatus.setTextColor(Color.GREEN);//绿色
                    try {
                        mSimulatedLocationProvider.mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, true, true, false, false, true, true, true, ProviderProperties.POWER_USAGE_HIGH, ProviderProperties.ACCURACY_FINE);
                        mSimulatedLocationProvider.mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                        mSimulatedLocationProvider.mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
                        mSimulatedLocationProvider.start();
                    } catch (SecurityException securityException) {
                        textShowStatus.setText("未允许位置模拟，请前往开发者模式勾选允许位置调试！");
                        textShowStatus.setTextColor(Color.RED);//红色
                    }

                } else {
                    //停止模拟GPS定位
                    textShowStatus.setText("GPS模拟已停止！");
                    textShowStatus.setTextColor(Color.GREEN);//绿色
                    try {
                        mSimulatedLocationProvider.mLocationManager.removeTestProvider(mSimulatedLocationProvider.mProviderName);
                        mSimulatedLocationProvider.interrupt();
                    } catch (SecurityException securityException) {
                        textShowStatus.setText("未允许位置模拟，请前往开发者模式勾选允许位置调试！");
                        textShowStatus.setTextColor(Color.RED);//红色
                    } catch (NullPointerException nullPointerException) {
                        textShowStatus.setText("GPS模拟已停止！");
                        textShowStatus.setTextColor(Color.GREEN);//绿色
                        mSimulatedLocationProvider.interrupt();
                    }
                }
            }
        });
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 处理位置更新事件
            TextView textShowGPS = findViewById(R.id.text_show_gps);
            textShowGPS.setText("经度：" + changeToDFM(location.getLongitude()) + "  纬度" + changeToDFM(location.getLatitude()));
        }

        /*@Override
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
        }*/
    };

    private boolean checkGPSEnabled() {
        //检查GPS的启用状况以及权限，如果权限未启用，则请求权限
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
        if (!isGpsEnabled) {
            return false;
        } else
            return true;
    }

    @SuppressLint("MissingPermission")
    public void startTrackPosition() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        TextView textShowGPS = findViewById(R.id.text_show_gps);
        LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (provider == null) {
            Log.e("startTrackPosition", "startTrackPosition:provider=null");
        }
        //已经获得了权限,实时更新位置
        if (isProvidedPermission) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
        }
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

    /**
     * 将经纬度转换为度分秒格式
     *
     * @param du 116.418847
     * @return 116°25'7.85"
     */
    public static String changeToDFM(double du) {
        int du1 = (int) du;
        double tp = (du - du1) * 60;
        int fen = (int) tp;
        String miao = String.format("%.2f", Math.abs(((tp - fen) * 60)));
        return du1 + "°" + Math.abs(fen) + "'" + miao + "\"";
    }
}


