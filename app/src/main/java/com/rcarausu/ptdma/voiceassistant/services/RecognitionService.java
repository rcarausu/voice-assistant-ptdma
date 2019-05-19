package com.rcarausu.ptdma.voiceassistant.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rcarausu.ptdma.voiceassistant.R;
import com.rcarausu.ptdma.voiceassistant.activities.HelpActivity;
import com.rcarausu.ptdma.voiceassistant.activities.TodoActivity;
import com.rcarausu.ptdma.voiceassistant.domain.CurrentWeatherForecast;
import com.rcarausu.ptdma.voiceassistant.domain.TomorrowWeatherForecast;
import com.rcarausu.ptdma.voiceassistant.tasks.WeatherTodayForecastTask;
import com.rcarausu.ptdma.voiceassistant.tasks.WeatherTomorrowForecastTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

public class RecognitionService {

    public static final String DEFAULT_LOCATION_PREFERENCES = "default_location";
    public static final String DEFAULT_LOCATION = "default_location";

    public static List<String> tokenizeString(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        List<String> tokens = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }

        return tokens;
    }

    public static void recognizeTokens(List<String> tokens, Context context, Activity activity) {
        if (tokens.contains("help")) {
            activity.startActivity(new Intent(context, HelpActivity.class));
        } else if (tokens.contains("set") && tokens.contains("default") && tokens.contains("location") && tokens.contains("to")) {
            int auxIndex = tokens.indexOf("to") + 1;
            String defaultLocation = "";
            while (auxIndex < tokens.size()) {
                defaultLocation += tokens.get(auxIndex) + " ";
                auxIndex++;
            }
            List<Address> addresses = GeolocationService.getAddressFromLocationName(defaultLocation, context);
            if (addresses.size() > 0) {
                SharedPreferences preferences = activity.getSharedPreferences(DEFAULT_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(DEFAULT_LOCATION, defaultLocation);
                editor.apply();

                Toast.makeText(activity, String.format("Default weather location set to %s", defaultLocation), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, R.string.default_location_change_error, Toast.LENGTH_SHORT).show();
            }

        } else if (tokens.contains("remove") && tokens.contains("default") && tokens.contains("location")) {
            SharedPreferences preferences = activity.getSharedPreferences(DEFAULT_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
            if (preferences.contains(DEFAULT_LOCATION)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove(DEFAULT_LOCATION);
                editor.apply();
                Toast.makeText(activity, "Default weather location removed correctly", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "The default weather location has not been set", Toast.LENGTH_SHORT).show();
            }

        } else if (tokens.contains("show") && tokens.contains("default") && tokens.contains("location")) {
            SharedPreferences preferences = activity.getSharedPreferences(DEFAULT_LOCATION_PREFERENCES, Context.MODE_PRIVATE);
            if (preferences.contains(DEFAULT_LOCATION)) {
                Toast.makeText(activity,
                        String.format("Default weather location is %s",
                                preferences.getString(DEFAULT_LOCATION, "")),
                        Toast.LENGTH_SHORT)
                    .show();
            } else {
                Toast.makeText(activity, "The default weather location has not been set", Toast.LENGTH_SHORT).show();
            }
        } else if ((tokens.contains("to") && tokens.contains("do")) || (tokens.contains("to-do"))) {

            activity.startActivity(new Intent(context, TodoActivity.class));

        } else if (tokens.contains("location")) {

            LastKnownLocationService locationService = LastKnownLocationService.getInstance();

            if (locationService.checkLocationPermissions(activity)) {
                Location location = locationService.getLastKnownLocation();

                if (location != null) {
                    List<Address> addresses = GeolocationService.getAddresFromLocation(location, context);

                    if (addresses.size() > 0) {
                        fillLocationCard(addresses.get(0), activity);
                        activity.findViewById(R.id.locationLayout).setVisibility(View.VISIBLE);
                        activity.findViewById(R.id.tomorrowForecastLayout).setVisibility(View.GONE);
                        activity.findViewById(R.id.currentForecastLayout).setVisibility(View.GONE);
                    } else {
                        Toast.makeText(activity, R.string.location_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, R.string.last_location_unknown_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                locationService.grantLocationPermissions(activity);
            }

        } else if (tokens.contains("calendar") || tokens.contains("event")) {
            if (CalendarService.getInstance().checkCalendarPermissions(activity)) {
                addEventToCalendar(activity);
            } else {
                CalendarService.getInstance().grantCalendarPermissions(activity);
            }

        } else if (tokens.contains("weather") && tokens.contains("tomorrow")) {
            if (!LastKnownLocationService.getInstance().checkLocationPermissions(activity)) {
                Toast.makeText(activity, R.string.weather_example_toast, Toast.LENGTH_LONG).show();
            }
            AsyncTask task = new WeatherTomorrowForecastTask().execute(activity);
            try {
                TomorrowWeatherForecast tomorrowForecast = (TomorrowWeatherForecast) task.get();

                if (tomorrowForecast != null) {
                    fillTomorrowWeatherCard(tomorrowForecast, activity);

                    activity.findViewById(R.id.tomorrowForecastLayout).setVisibility(View.VISIBLE);
                    activity.findViewById(R.id.currentForecastLayout).setVisibility(View.GONE);
                    activity.findViewById(R.id.locationLayout).setVisibility(View.GONE);

                    if (tomorrowForecast.getCity() != null) {
                        Toast.makeText(activity,
                                String.format("Weather in %s for tomorrow", tomorrowForecast.getCity()),
                                Toast.LENGTH_SHORT)
                            .show();
                    }

                } else {
                    Toast.makeText(activity, R.string.forecast_error, Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else if (tokens.contains("weather")) {
            if (!LastKnownLocationService.getInstance().checkLocationPermissions(activity)) {
                Toast.makeText(activity, R.string.weather_example_toast, Toast.LENGTH_LONG).show();
            }
            AsyncTask task = new WeatherTodayForecastTask().execute(activity);
            try {
                CurrentWeatherForecast currentForecast = (CurrentWeatherForecast) task.get();

                if (currentForecast != null) {
                    fillCurrentWeatherCard(currentForecast, activity);

                    activity.findViewById(R.id.currentForecastLayout).setVisibility(View.VISIBLE);
                    activity.findViewById(R.id.tomorrowForecastLayout).setVisibility(View.GONE);
                    activity.findViewById(R.id.locationLayout).setVisibility(View.GONE);
                    if (currentForecast.getCity() != null) {
                        Toast.makeText(activity,
                                String.format("Weather in %s", currentForecast.getCity()),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    Toast.makeText(
                            activity, R.string.forecast_error, Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(
                    activity, R.string.recognition_error, Toast.LENGTH_SHORT).show();
        }
    }

    private static void addEventToCalendar(Activity activity) {
        Calendar beginTime = Calendar.getInstance();

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());

        activity.startActivity(intent);
    }

    private static void fillLocationCard(Address address, Activity activity) {
        TextView latitude = activity.findViewById(R.id.latitude);
        latitude.setText(String.format("Latitude: %.4f", address.getLatitude()));

        TextView longitude = activity.findViewById(R.id.longitude);
        longitude.setText(String.format("Longitude: %.4f", address.getLongitude()));

        TextView street = activity.findViewById(R.id.address);
        street.setText(String.format("%s, %s, %s, %s",
                address.getThoroughfare(),
                address.getSubThoroughfare(),
                address.getPostalCode(),
                address.getLocality()));
    }

    private static void fillCurrentWeatherCard(CurrentWeatherForecast forecast, Activity activity) {
        ImageView forecastIconIV = activity.findViewById(R.id.currentForecastIcon);
        forecastIconIV.setImageResource(getWeatherIconId(forecast.getIcon()));

        TextView forecastTemperature = activity.findViewById(R.id.currentForecastTemperature);

        forecastTemperature.setText(String.format("Temperature: %.1f ºC",
                forecast.getTemperature()));

        TextView forecastHumidity = activity.findViewById(R.id.currentForecastHumidity);
        forecastHumidity.setText(String.format("Relative Humidity: %.2f",
                forecast.getRelativeHumidity()));

        TextView forecastSummary = activity.findViewById(R.id.currentForecastSummary);
        forecastSummary.setText(String.format("Summary: %s", forecast.getSummary()));
    }

    private static void fillTomorrowWeatherCard(TomorrowWeatherForecast forecast, Activity activity) {
        ImageView forecastIconIV = activity.findViewById(R.id.tomorrowForecastIcon);
        forecastIconIV.setImageResource(getWeatherIconId(forecast.getIcon()));

        TextView maxTempTV = activity.findViewById(R.id.tomorrowForecastMaxTemp);
        maxTempTV.setText(String.format("Max: %.2f ºC", forecast.getMaxTemperature()));

        TextView minTempTV = activity.findViewById(R.id.tomorrowForecastMinTemp);
        minTempTV.setText(String.format("Min: %.2f ºC", forecast.getMinTemperature()));

        TextView precipProbTV = activity.findViewById(R.id.tomorrowPrecipProb);
        precipProbTV.setText(String.format("Probability: %.2f",
                forecast.getPrecipitationProbability()));

        TextView forecastHumidity = activity.findViewById(R.id.tomorrowHumidity);
        forecastHumidity.setText(String.format("Relative Humidity: %.2f",
                forecast.getRelativeHumidity()));

        TextView forecastSummary = activity.findViewById(R.id.tomorrowForecastSummary);
        forecastSummary.setText(String.format("Summary: %s", forecast.getSummary()));
    }

    private static Integer getWeatherIconId(String icon) {
        switch (icon) {
            case "clear-day":
                return R.drawable.ic_clear_day;
            case "clear-night":
                return R.drawable.ic_clear_night;
            case "rain":
                return R.drawable.ic_rain;
            case "snow":
                return R.drawable.ic_snow;
            case "sleet":
                return R.drawable.ic_sleet;
            case "wind":
                return R.drawable.ic_wind;
            case "fog":
                return R.drawable.ic_fog;
            case "cloudy":
                return R.drawable.ic_clowdy;
            case "partly-cloudy-day":
                return R.drawable.ic_partly_cloudy_day;
            case "partly-cloudy-night":
                return R.drawable.ic_partly_clowdy_night;
            default:
                return R.drawable.ic_clear_day;
        }
    }
}
