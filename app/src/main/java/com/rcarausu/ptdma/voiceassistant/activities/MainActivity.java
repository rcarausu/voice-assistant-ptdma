package com.rcarausu.ptdma.voiceassistant.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rcarausu.ptdma.voiceassistant.R;
import com.rcarausu.ptdma.voiceassistant.services.CalendarService;
import com.rcarausu.ptdma.voiceassistant.services.LastKnownLocationService;
import com.rcarausu.ptdma.voiceassistant.services.RecognitionService;
import com.rcarausu.ptdma.voiceassistant.utils.RequestCodes;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String DATA_LAST_QUERY = "DataLastQuery";
    private static final String LAST_QUERY = "last_query";

    private LastKnownLocationService lastKnownLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(DATA_LAST_QUERY, MODE_PRIVATE);
        String lastQuery = preferences.getString(
                LAST_QUERY,
                getResources().getString(R.string.default_query_message));

        CardView cvLastQuery = findViewById(R.id.lastQuery);
        final Activity main = this;
        final Context mainContext = getBaseContext();
        cvLastQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView query = findViewById(R.id.textLastQueries);
                RecognitionService.recognizeTokens(
                        RecognitionService.tokenizeString(query.getText().toString()),
                        mainContext,
                        main);
            }
        });

        LinearLayout currentForecastLayout = findViewById(R.id.currentForecastLayout);
        currentForecastLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearView(v);
                return true;
            }
        });

        LinearLayout tomorrowForecastLayout = findViewById(R.id.tomorrowForecastLayout);
        tomorrowForecastLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearView(v);
                return true;
            }
        });

        LinearLayout locationLayout = findViewById(R.id.locationLayout);
        locationLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearView(v);
                return true;
            }
        });


        TextView textView = findViewById(R.id.textLastQueries);
        textView.setText(lastQuery);

        final ImageView microphoneImage = findViewById(R.id.iViewMicrophone);
        microphoneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpeechRecognizer();
            }
        });

        lastKnownLocationService = LastKnownLocationService.getInstance();

        if (lastKnownLocationService.checkLocationPermissions(this)) {
            lastKnownLocationService.setLocationHandlersAndCallback(this);
        } else {
            lastKnownLocationService.grantLocationPermissions(this);
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RequestCodes.REQUEST_LOCATION_PERMISSIONS_CODE) {
            boolean fineLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean coarseLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (fineLocation && coarseLocation) {
                lastKnownLocationService.setLocationHandlersAndCallback(this);
            }
        }

        for(String permission: permissions){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                //denied
                Log.e("denied", permission);
            }else{
                if(ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
                    //allowed
                    Log.e("allowed", permission);
                } else{
                    //set to never ask again
                    Log.e("set to never ask again", permission);
                    Toast.makeText(this, R.string.permissions_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastKnownLocationService.checkLocationPermissions(this)) {
            lastKnownLocationService.startLocationUpdates(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lastKnownLocationService.checkLocationPermissions(this)) {
            lastKnownLocationService.stopLocationUpdates(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.todo_item:
                startActivity(new Intent(getBaseContext(), TodoActivity.class));
                return true;
            case R.id.help_item:
                startActivity(new Intent(getBaseContext(), HelpActivity.class));
                return true;
            default:
                return false;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            String spokenText = results.get(0);

            SharedPreferences preferences = getSharedPreferences(DATA_LAST_QUERY, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LAST_QUERY, spokenText);
            editor.apply();

            TextView textView = findViewById(R.id.textLastQueries);
            textView.setText(spokenText);

            List<String> tokens = RecognitionService.tokenizeString(spokenText);

            RecognitionService.recognizeTokens(tokens, getBaseContext(), this);

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.ENGLISH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        startActivityForResult(intent, RequestCodes.SPEECH_REQUEST_CODE);
    }

    private void clearView(final View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Are you sure?");
        dialogBuilder.setMessage("This will remove the selected card");
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                view.setVisibility(View.GONE);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.create().show();
    }

}
