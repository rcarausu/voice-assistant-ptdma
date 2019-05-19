package com.rcarausu.ptdma.voiceassistant.configuration;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {

    private Context context;

    public AppConfig(Context context) {
        this.context = context;
    }

    public Properties getProperties() {
        try {
            InputStream inputStream = this.context.getAssets().open("app.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

