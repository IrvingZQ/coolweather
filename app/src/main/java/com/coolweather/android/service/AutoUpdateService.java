package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
        int EightHour=8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+EightHour;
        Intent intent1=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,intent1,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    public void updateBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }

    public void updateWeather(){
        SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String weatherString=pref.getString("weather",null);
        if (weatherString!=null){
            Weather weather=Utility.handleWeatherResponse(weatherString);
            final String weatherId=weather.basic.weatherId;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=fd9d63330dd14745b4b29ab6b11195d7";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherString=response.body().string();
                    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weather",weatherString);
                    editor.apply();
                }
            });
        }
    }
}