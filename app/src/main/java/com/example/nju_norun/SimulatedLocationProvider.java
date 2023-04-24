package com.example.nju_norun;

import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

import java.util.Arrays;
import java.util.List;

public class SimulatedLocationProvider extends Thread {
    private LocationManager mLocationManager;
    private String mProviderName;
    private boolean mInterrupted = false;

    //鼓楼校区操场的经纬度
    //TODO:支持仙林校区的经纬度及自定义
    private List<Double> latitudeList = Arrays.asList(32.056378433201594, 32.05631023589969, 32.0562193060847, 32.056201120110856, 32.056382979686596, 32.05736501514904, 32.057555965264875, 32.05757415096942, 32.057492315270544, 32.057337736528524);
    private List<Double> longitudeList = Arrays.asList(118.7785434721809, 118.77861320961529, 118.77877414215618, 118.77898871887737, 118.77938032139355, 118.77926230419689, 118.77903699863964, 118.77880632866436, 118.77856492985302, 118.7783986328941);

    public SimulatedLocationProvider(LocationManager locationManager, String providerName) {
        mLocationManager = locationManager;
        mProviderName = providerName;
    }

    public void run(int time) {
        try {
            while (!mInterrupted) {
                // 模拟位置数据
                Location location = new Location(mProviderName);
                location.setLatitude(32.056378433201594);
                location.setLongitude(118.7785434721809);
                location.setAccuracy(5.0f);
                location.setTime(System.currentTimeMillis());
                location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                // 发送位置数据
                mLocationManager.setTestProviderLocation(mProviderName, location);

                // 等待一段时间
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        mInterrupted = true;
        super.interrupt();
    }
}