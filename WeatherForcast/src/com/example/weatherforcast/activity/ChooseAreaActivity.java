package com.example.weatherforcast.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.weatherforcast.R;
import com.example.weatherforcast.db.WeatherForcastDB;
import com.example.weatherforcast.model.City;
import com.example.weatherforcast.model.County;
import com.example.weatherforcast.model.Province;
import com.example.weatherforcast.util.HttpCallbackListener;
import com.example.weatherforcast.util.HttpUtil;
import com.example.weatherforcast.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WeatherForcastDB weatherForcastDB;
	private List<String> dataList = new ArrayList<String>();

	/*
	 * 省级列表
	 */
	private List<Province> provinceList;

	/*
	 * 市列表
	 */
	private List<City> cityList;

	/*
	 * 县列表
	 */
	private List<County> countyList;

	/*
	 * 选中的省份
	 */
	private Province selectedProvince;

	/*
	 * 选中的城市
	 */
	private City selectedCity;

	/*
	 * 当前选中的级别
	 */
	private int currentLevel;

/*
 * 是否从WeatherActivity中跳转过来
 */
	private boolean isFromWeatherActivity;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		/*
		 * 已经选择了城市且不是从WeatherActivity跳转过去，才会直接跳转到WeatherActivity
		 */
		if(prefs.getBoolean("city_selected",false)&&!isFromWeatherActivity){
			Intent intent=new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		listView=(ListView) findViewById(R.id.list_view);
	     titleText=(TextView) findViewById(R.id.title_text);
	     
	    adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
	    listView.setAdapter(adapter);
	    weatherForcastDB=WeatherForcastDB.getInstance(this);
	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel==LEVEL_PROVINE){
					selectedProvince=provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(position);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY){
					String countyCode=countyList.get(position).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
	    //加载省级数据
	    queryProvinces();
	}

	/*
	 * 加载全国所有的省，优先从数据库中查，没有的话再去服务器上找
	 */
	private void queryProvinces() {
		provinceList=weatherForcastDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINE;
		}else{
			queryFromServer(null,"province");
		}
	}
	
	/*
	 * 查询省内所有的市，数据库中没有的话就去服务器中查找
	 */
	private void queryCities() {
	cityList=weatherForcastDB.loadCities(selectedProvince.getId());
	if(cityList.size()>0){
		dataList.clear();
		for (City city : cityList) {
			dataList.add(city.getCityName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText(selectedProvince.getProvinceName());
		currentLevel=LEVEL_CITY;
	}else{
		queryFromServer(selectedProvince.getProvinceCode(), "city");
	}
		
	}

/*
 * 查选市内的所有的县，没有就从服务器中查
 */
	private void queryCounties() {
	countyList=weatherForcastDB.loadCounties(selectedCity.getId());
	if(countyList.size()>0){
		dataList.clear();
		for (County county : countyList) {
			dataList.add(county.getCountyName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText(selectedCity.getCityName());
		currentLevel=LEVEL_COUNTY;
	}else{
		queryFromServer(selectedCity.getCityCode(), "county");
	}
	}
	
/*
 * 根据传入的代号和类型，从服务器上查询省市县数据
 */
	private void queryFromServer(final String code,final String type) {
	String address;
	if(!TextUtils.isEmpty(code)){
				address="http://m.weather.com.cn/data5/city"+code+".xml";
	/*	address="http://www.weather.com.cn/data/list3/city"+code+".xml";*/
		Log.d("ABCD"+type,address);

	}else{
		/*address="http://www.weather.com.cn/data/list3/city.xml";*/
		address="http://m.weather.com.cn/data5/city.xml";
	}
	showProgressDialog();
	HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
		
		@Override
		public void onFininsh(String response) {
			boolean result=false;
			if("province".equals(type)){
				result=Utility.handleProvincesResponse(weatherForcastDB, response);
			}else if("city".equals(type)){
				result=Utility.handleCitiesResponse(weatherForcastDB, response, selectedProvince.getId());
			}else if("county".equals(type)){
				result=Utility.handleCountiesResponse(weatherForcastDB, response, selectedCity.getId());
			}
			if(result){
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread( new Runnable() {
					public void run() {
						closeProgressDialog();
						if("province".equals(type)){
							queryProvinces();
						}else if("city".equals(type)){
							queryCities();
						}else if("county".equals(type)){
							queryCounties();
						}
					}
				});
			}
		}
		
		@Override
		public void onError(Exception e) {
		//通过runonUithread()方法回到主线程处理逻辑
			runOnUiThread(new Runnable() {
				public void run() {
			     closeProgressDialog();
			     Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
				}
			});
		}
	});
	}


	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载中.....");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	
	/*
	 * 捕获back返回键，根据当前的级别来判断返回市列表、省列表还是直接退出
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel==LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel==LEVEL_CITY){
			queryProvinces();
		}else{
			if(isFromWeatherActivity){
				Intent intent=new Intent(this, WeatherActivity.class); 
				startActivity(intent);
			}
			finish();
		}
	}
}
