package com.example.myweatheractivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Weather {

    String weatherInJson;
    private String description;
    private String currentTemperature;
    private String currentHumidity;
    private String todayLowTemp;
    private String todayHighTemp;

    
    Weather(String weatherInJson) {
        this.weatherInJson = weatherInJson;
        System.out.println(weatherInJson);
    }
    
    public Object parseWeather () {
        JSONObject weatherJson = null;
        if (weatherInJson != null) {
            try {
                weatherJson = new JSONObject(weatherInJson);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        try {
                JSONArray currentWeatherJson = weatherJson.getJSONArray("weather");
                JSONObject currentDescription = currentWeatherJson.getJSONObject(0);
                this.description = (String) currentDescription.get("main");
                JSONObject currentWeather = weatherJson.getJSONObject("main");
                this.currentHumidity = currentWeather.get("humidity").toString();
                this.currentTemperature = currentWeather.get("temp").toString();
                this.todayHighTemp = currentWeather.get("temp_max").toString();
                this.todayLowTemp = currentWeather.get("temp_min").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        return weatherJson; // Not used in current version but might be useful for testing
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public String getCurrentHumidity() {
        return currentHumidity;
    }

    public void setCurrentHumidity(String currentHumidity) {
        this.currentHumidity = currentHumidity;
    }

    public String getTodayLowTemp() {
        return todayLowTemp;
    }

    public void setTodayLowTemp(String todayLowTemp) {
        this.todayLowTemp = todayLowTemp;
    }

    public String getTodayHighTemp() {
        return todayHighTemp;
    }

    public void setTodayHighTemp(String todayHighTemp) {
        this.todayHighTemp = todayHighTemp;
    }
}
