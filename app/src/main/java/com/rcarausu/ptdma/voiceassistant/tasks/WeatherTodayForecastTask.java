package com.rcarausu.ptdma.voiceassistant.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.rcarausu.ptdma.voiceassistant.domain.CurrentWeatherForecast;
import com.rcarausu.ptdma.voiceassistant.services.WeatherService;

public class WeatherTodayForecastTask extends AsyncTask<Activity, Integer, CurrentWeatherForecast> {
    @Override
    protected CurrentWeatherForecast doInBackground(Activity... activities) {
        if (activities.length != 1) {
            throw new IndexOutOfBoundsException("There should be only 1 context");
        }

        WeatherService weatherService = new WeatherService(activities[0]);
        return weatherService.currentForecast();
    }
}
