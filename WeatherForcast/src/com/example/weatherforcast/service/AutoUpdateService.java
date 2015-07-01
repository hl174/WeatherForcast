package com.example.weatherforcast.service;

import com.example.weatherforcast.util.HttpCallbackListener;
import com.example.weatherforcast.util.HttpUtil;
import com.example.weatherforcast.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}
		}).start();
		//创建定时任务
		AlarmManager manager=(AlarmManager) getSystemService(ALARM_SERVICE);
	    int hour=8*60*60*1000;
	    long tiggerAtTime=SystemClock.elapsedRealtime()+hour;
	    Intent i=new Intent(this, AutoUpdateService.class);
	    PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
	    manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, tiggerAtTime, pi);
	    return super.onStartCommand(intent, flags, startId);
	}
	
/*
 * 更新天气信息
 */
	private void updateWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode=prefs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFininsh(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			
			@Override
			public void onError(Exception e) {
				e.printStackTrace();
			}
		});
		
	}
}
