package com.example.weatherforcast.util;

import com.example.weatherforcast.db.WeatherForcastDB;
import com.example.weatherforcast.model.City;
import com.example.weatherforcast.model.County;
import com.example.weatherforcast.model.Province;

import android.text.TextUtils;

public class Utility {
/*
 * 解析和处理服务器返回的省级数据
 */
	public synchronized static boolean handleProvincesResponse(WeatherForcastDB weatherForcastDB,String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces=response.split(",");
		    if(allProvinces!=null&&allProvinces.length>0){
		    	for (String p : allProvinces) {
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
				   //将解析出来的数据存到Province表
					weatherForcastDB.saveProvince(province);
		    	}
		    	return true;
		    }
		}	
		return false;
	}

	/*
	 * 解析和处理服务器返回的市级数据
	 */
	public static boolean handleCitiesResponse(WeatherForcastDB weatherForcastDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
		    if(allCities!=null&&allCities.length>0){
		    	for (String p : allCities) {
					String[] array=p.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
				   //将解析出来的数据存到Province表
					weatherForcastDB.saveCity(city);
		    	}
		    	return true;
		    }
		}		
		return false;
	}
	

	/*
	 * 解析和处理服务器返回的县级数据
	 */
	public static boolean handleCountiesResponse(WeatherForcastDB weatherForcastDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
		    if(allCounties!=null&&allCounties.length>0){
		    	for (String p : allCounties) {
					String[] array=p.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
				   //将解析出来的数据存到Province表
					weatherForcastDB.saveCounty(county);
		    	}
		    	return true;
		    }
		}		
		return false;
	}
}
