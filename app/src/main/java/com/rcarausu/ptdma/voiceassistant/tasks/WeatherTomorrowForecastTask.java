package com.rcarausu.ptdma.voiceassistant.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.rcarausu.ptdma.voiceassistant.domain.TomorrowWeatherForecast;
import com.rcarausu.ptdma.voiceassistant.services.WeatherService;

public class WeatherTomorrowForecastTask extends AsyncTask<Activity, Integer, TomorrowWeatherForecast> {
    @Override
    protected TomorrowWeatherForecast doInBackground(Activity... activities) {
        if (activities.length != 1) {
            throw new IndexOutOfBoundsException("There should be only 1 context");
        }

        WeatherService weatherService = new WeatherService(activities[0]);
        return weatherService.tomorrowForecast();
    }
}
