package com.rcarausu.ptdma.voiceassistant.services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;

public class GeolocationService {


    public static List<Address> getAddresFromLocation(Location location, Context context) {

        Geocoder geocoder = new Geocoder(context);

        try {
           return geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Address> getAddressFromLocationName(String name, Context context) {
        Geocoder geocoder = new Geocoder(context);
        try {
            return geocoder.getFromLocationName(name, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
