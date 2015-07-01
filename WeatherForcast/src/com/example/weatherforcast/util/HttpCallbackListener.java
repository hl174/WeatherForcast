package com.example.weatherforcast.util;

public interface HttpCallbackListener {
void onFininsh(String response);
void onError(Exception e);
}
