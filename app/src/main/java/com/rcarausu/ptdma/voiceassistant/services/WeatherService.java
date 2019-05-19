package com.rcarausu.ptdma.voiceassistant.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.location.Address;
import android.location.Location;

import com.rcarausu.ptdma.voiceassistant.configuration.AppConfig;
import com.rcarausu.ptdma.voiceassistant.domain.CurrentWeatherForecast;
import com.rcarausu.ptdma.voiceassistant.domain.TomorrowWeatherForecast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.rcarausu.ptdma.voiceassistant.services.RecognitionService.DEFAULT_LOCATION;
import static com.rcarausu.ptdma.voiceassistant.services.RecognitionService.DEFAULT_LOCATION_PREFERENCES;

public class WeatherService {

    private static final String EXAMPLE_FORECAST_JSON = "example_forecast.json";
    private static final String CURRENTLY = "currently";
    private static final String SUMMARY = "summary";
    private static final String TEMPERATURE = "temperature";
    private static final String ICON = "icon";
    private static final String HUMIDITY = "humidity";
    private static final String TEMPERATURE_HIGH = "temperatureHigh";
    private static final String TEMPERATURE_LOW = "temperatureLow";
    private static final String PRECIP_PROBABILITY = "precipProbability";
    private static final String DAILY = "daily";
    private static final String DATA = "data";
    private static final String DARK_SKY_URL = "dark-sky-url";
    private static final String DARK_SKY_SECRET = "dark-sky-secret";
    private static final String DARK_SKY_FORECAST_PATH = "dark-sky-forecast-path";
    private static final String DEFAULT_LATITUDE = "default-latitude";
    private static final String DEFAULT_LONGITUDE = "default-longitude";

    private String city;
    private String forecast;
    private Activity activity;
    private Location lastKnownLocation;

    public WeatherService(Activity activity) {
        this.activity = activity;
    }

    public CurrentWeatherForecast currentForecast() {

        try {

            Response response = getForecast();

            if (response.isSuccessful() && response.body() != null) {
                forecast = response.body().string();
            } else {
                forecast =  readExampleForecast(this.activity.getAssets());
            }

            return buildCurrentForecast(forecast);

        } catch (Exception e) {
            if (e.getClass() == UnknownHostException.class) {
                return buildCurrentForecast(readExampleForecast(this.activity.getAssets()));
            }
            e.printStackTrace();
            return null;
        }
    }

    public TomorrowWeatherForecast tomorrowForecast() {

        try {
            Response response = getForecast();

            if (response.isSuccessful() && response.body() != null) {
                forecast = response.body().string();
            } else {
                forecast =  readExampleForecast(this.activity.getAssets());
            }

            return buildTomorrowForecast(forecast);

        } catch (Exception e) {
            if (e.getClass() == UnknownHostException.class) {
                return buildTomorrowForecast(readExampleForecast(this.activity.getAssets()));
            }
            e.printStackTrace();
            return null;
        }
    }

    private Response getForecast() throws IOException {
        Properties config = new AppConfig(this.activity).getProperties();
        String url = config.getProperty(DARK_SKY_URL);
        String secret = config.getProperty(DARK_SKY_SECRET);
        String path  = config.getProperty(DARK_SKY_FORECAST_PATH);

        Double latitude = Double.parseDouble(config.getProperty(DEFAULT_LATITUDE));
        Double longitude = Double.parseDouble(config.getProperty(DEFAULT_LONGITUDE));

        SharedPreferences preferences = activity.getSharedPreferences(DEFAULT_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
        if (preferences.contains(DEFAULT_LOCATION)) {
            Address address = GeolocationService.getAddressFromLocationName(
                    preferences.getString(DEFAULT_LOCATION, null),
                    activity.getApplicationContext())
            .get(0);
            city = address.getLocality();

            latitude = address.getLatitude();
            longitude = address.getLongitude();
        } else {
            LastKnownLocationService lastKnownLocationService = LastKnownLocationService.getInstance();

            lastKnownLocation = lastKnownLocationService.getLastKnownLocation();

            if (lastKnownLocation != null) {
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                List<Address> addresses = GeolocationService.getAddresFromLocation(lastKnownLocation, activity.getApplicationContext());
                if (addresses.size() > 0) {
                    city = addresses.get(0).getLocality();
                }
            }
        }

        path = String.format(path, secret, latitude, longitude);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url + path)
                .build();

        return client.newCall(request).execute();
    }

    private CurrentWeatherForecast buildCurrentForecast(String forecast) {
        try {
            JSONObject jsonObject = new JSONObject(forecast);
            String currentSummary = jsonObject.getJSONObject(CURRENTLY).getString(SUMMARY);
            Double currentTemperature = jsonObject.getJSONObject(CURRENTLY).getDouble(TEMPERATURE);
            String currentIcon = jsonObject.getJSONObject(CURRENTLY).getString(ICON);
            Double relativeHumidity = jsonObject.getJSONObject(CURRENTLY).getDouble(HUMIDITY);

            CurrentWeatherForecast currentWeatherForecast = new CurrentWeatherForecast(
                    city, currentSummary, currentTemperature, currentIcon, relativeHumidity);
            city = null;
            return currentWeatherForecast;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private TomorrowWeatherForecast buildTomorrowForecast(String forecast) {
        try {
            TomorrowWeatherForecast tomorrowForecast = new TomorrowWeatherForecast();

            JSONObject jsonObject = new JSONObject(forecast);
            JSONObject tomorrowData = (JSONObject) jsonObject.getJSONObject(DAILY)
                    .getJSONArray(DATA).get(0);

            String tomorrowSummary = tomorrowData.getString(SUMMARY);
            Double maxTemperature = tomorrowData.getDouble(TEMPERATURE_HIGH);
            Double minTemperature = tomorrowData.getDouble(TEMPERATURE_LOW);
            Double precipitationProbability = tomorrowData.getDouble(PRECIP_PROBABILITY);
            Double relativeHumidity = tomorrowData.getDouble(HUMIDITY);
            String icon = tomorrowData.getString(ICON);

            tomorrowForecast.setCity(city);
            city = null;
            tomorrowForecast.setSummary(tomorrowSummary);
            tomorrowForecast.setMaxTemperature(maxTemperature);
            tomorrowForecast.setMinTemperature(minTemperature);
            tomorrowForecast.setPrecipitationProbability(precipitationProbability);
            tomorrowForecast.setRelativeHumidity(relativeHumidity);
            tomorrowForecast.setIcon(icon);

            return tomorrowForecast;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readExampleForecast(AssetManager assets) {

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(assets.open(EXAMPLE_FORECAST_JSON))
            );

            StringBuilder stringBuilder = new StringBuilder();
            String result;
            while ((result = reader.readLine()) != null) {
                stringBuilder.append(result);
            }

            return stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
