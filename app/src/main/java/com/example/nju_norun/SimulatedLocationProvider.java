package com.example.nju_norun;


import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;


import java.util.Random;
import java.util.Arrays;
import java.util.List;

public class SimulatedLocationProvider extends Thread {
    public LocationManager mLocationManager;
    public String mProviderName;
    Random random = new Random();
    private boolean isReachedNextPosition = false;
    private boolean isInterrupted = false;

    //鼓楼校区操场的经纬度
    //TODO:支持仙林校区的经纬度及自定义
    private List<Double> latitudeList = Arrays.asList(32.056378433201594, 32.05631023589969, 32.0562193060847, 32.056201120110856, 32.056382979686596, 32.05736501514904, 32.057555965264875, 32.05757415096942, 32.057492315270544, 32.057337736528524);
    private List<Double> longitudeList = Arrays.asList(118.7785434721809, 118.77861320961529, 118.77877414215618, 118.77898871887737, 118.77938032139355, 118.77926230419689, 118.77903699863964, 118.77880632866436, 118.77856492985302, 118.7783986328941);

    public SimulatedLocationProvider(LocationManager locationManager, String providerName) {
        mLocationManager = locationManager;
        mProviderName = providerName;
    }

    public void run() {
        int listCount = 0;
        double currentLatitude = latitudeList.get(0);
        double currentLongitude = longitudeList.get(0);

        while (!isInterrupted) {
            // 模拟位置数据
            Location location1 = new Location(mProviderName);
            location1.setLatitude(currentLatitude);
            location1.setLongitude(currentLongitude);
            location1.setAccuracy(0.5f);
            float speed = (float) (random.nextDouble() * 2.35 + 2.08);
            location1.setSpeed(speed);
            location1.setTime(System.currentTimeMillis());
            location1.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            // 发送位置数据
            mLocationManager.setTestProviderLocation(mProviderName, location1);
            int timeSleep = 500;
            List<Double> position = getCurrentPosition(currentLatitude, currentLongitude, listCount, speed * (timeSleep + 50) / 1000);
            currentLatitude = position.get(0);
            currentLongitude = position.get(1);
            if (isReachedNextPosition) {
                listCount = nextCount(listCount);
                isReachedNextPosition = false;
            }
            // 等待一段时间
            try {
                Thread.sleep(timeSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("线程", "异常！");
                break;
            }

        }
    }

    private int nextCount(int count) {
        if (count != latitudeList.size() - 1)
            return count + 1;
        else return 0;
    }

    private List<Double> getCurrentPosition(double currentLatitude, double currentLongitude, int listCount, float distance) {
        //计算经过一段距离后的地点，同时改变isReachedNextPosition
        double n = calculateDistance(currentLatitude, currentLongitude, latitudeList.get(nextCount(listCount)), longitudeList.get(nextCount(listCount))) / distance;
        if (n <= 1) {
            isReachedNextPosition = true;
            return Arrays.asList(latitudeList.get(nextCount(listCount)), longitudeList.get(nextCount(listCount)));
        } else {
            double tempLat = currentLatitude + (latitudeList.get(nextCount(listCount)) - latitudeList.get(listCount)) / n;
            double tempLon = currentLongitude + (longitudeList.get(nextCount(listCount)) - longitudeList.get(listCount)) / n;
            return Arrays.asList(tempLat, tempLon);
        }
    }

    private double calculateDistance(double latA, double mLonA, double latB, double mLonB) {
        //用两点的经纬度计算距离
        final double pi = 3.14159265359;
        final int r = 6371004;
        double c = Math.sin(latA * pi / 180) * Math.sin(latB * pi / 180) + Math.cos(latA * pi / 180) * Math.cos(latB * pi / 180) * Math.cos((mLonA - mLonB) * pi / 180);
        return Math.abs(r * Math.acos(c) * pi);
    }

    public void interrupt() {
        //中断
        isInterrupted = true;
        super.interrupt();
    }
}