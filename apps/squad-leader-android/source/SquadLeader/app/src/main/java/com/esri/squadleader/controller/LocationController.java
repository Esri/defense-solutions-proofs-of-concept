/*******************************************************************************
 * Copyright 2013-2017 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.squadleader.controller;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.esri.android.map.LocationDisplayManager;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.LocationSimulator;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

public class LocationController extends com.esri.militaryapps.controller.LocationController {

    private static final String TAG = LocationController.class.getSimpleName();
    private static final String PREF_LOCATION_MODE = "pref_locationMode";
    private static final String PREF_GPX_FILE = "pref_gpxFile";

    /**
     * The name of the preferences file used to store LocationController preferences. This file will
     * be stored in the application's private space for the current user.
     */
    public static final String PREFS_NAME = "LocationControllerPrefs";

    private SharedPreferences prefs = null;
    private LocationDisplayManager locationDisplayManager = null;
    private Activity targetActivity = null;

    /**
     * Instantiates a LocationController. Note that if you want to use the device's location
     * capabilities, you must call setTargetActivity after instantiating so that LocationController
     * can check for location permission.
     *
     * @param mode           the location mode.
     * @param builtInGpxPath the built-in GPX resource path for simulated GPX. You can pass null if
     *                       you will never use the built-in GPX, or you can call setBuiltInGpxPath
     *                       later.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public LocationController(String builtInGpxPath, LocationMode mode)
            throws ParserConfigurationException, SAXException, IOException {
        super(mode);
        setBuiltInGpxPath(builtInGpxPath);
    }

    /**
     * Sets a target Activity for purposes of checking for location permission.
     *
     * @param targetActivity the target Activity, which LocationController uses to check for the
     *                       location permission. If targetActivity is null, LocationController will
     *                       not use the device's location capabilities but can use simulated
     *                       locations.
     */
    public void setTargetActivity(Activity targetActivity) {
        this.targetActivity = targetActivity;
    }

    /**
     * @param prefs a preferences object that contains or will contain the user's location settings.
     */
    public void setSharedPreferences(SharedPreferences prefs) {
        this.prefs = prefs;
        if (null != prefs) {
            String locationPref = prefs.getString(PREF_LOCATION_MODE, null);
            if (null != locationPref) {
                String gpxFilePath = prefs.getString(PREF_GPX_FILE, null);
                setGpxFile(null == gpxFilePath ? null : new File(gpxFilePath));
            }
        }
    }

    /**
     * Returns the LocationMode stored in the specified preferences, or null if a preference
     * has not been stored.
     *
     * @param prefs the SharedPreferences where the location mode may be stored.
     * @return the LocationMode stored in the specified preferences, or null if a preference
     * has not been stored.
     */
    public static LocationMode getLocationModeFromPreferences(SharedPreferences prefs) {
        String locationModePrefString = prefs.getString(PREF_LOCATION_MODE, null);
        if (null == locationModePrefString) {
            return null;
        } else {
            return LocationMode.valueOf(locationModePrefString);
        }
    }

    public void setLocationService(LocationDisplayManager locationService) {
        this.locationDisplayManager = locationService;
    }

    /**
     * Sets the location mode. If storePreferences is true and setSharedPreferences has been called
     * with a non-null SharedPreferences object, then this method will also store the mode in that object.
     *
     * @param mode            the location mode to use.
     * @param storePreference true if the mode should be stored as a preference.
     */
    @Override
    public void setMode(LocationMode mode, boolean storePreference) throws IOException, SAXException, ParserConfigurationException {
        super.setMode(mode, storePreference);
        if (storePreference && null != prefs) {
            prefs.edit().putString(PREF_LOCATION_MODE, mode.name()).apply();
        }
    }

    @Override
    public void setGpxFile(File gpxFile, boolean storePreference) {
        super.setGpxFile(gpxFile, storePreference);
        if (storePreference && null != prefs) {
            SharedPreferences.Editor editor = prefs.edit();
            if (null == gpxFile) {
                editor.remove(PREF_GPX_FILE);
            } else {
                editor.putString(PREF_GPX_FILE, gpxFile.getAbsolutePath());
            }
            editor.apply();
        }
    }

    @Override
    protected LocationProvider createLocationServiceProvider() {
        return new LocationProvider() {

            private LocationListener locationListener = new LocationListener() {

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }

                @Override
                public void onLocationChanged(android.location.Location location) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(location.getTime());
                    Location theLocation = new Location(location.getLongitude(), location.getLatitude(), cal, location.getSpeed(), location.getBearing());
                    sendLocation(theLocation);
                }
            };

            private LocationProviderState state = LocationProviderState.STOPPED;

            @Override
            public void start() {
                setupLocationListener();
                if (null == targetActivity) {
                    Log.w(TAG, "targetActivity is null, which means LocationController won't use the device's location capabilities.");
                } else if (ContextCompat.checkSelfPermission(targetActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    switch (getState()) {
                        case PAUSED: {
                            if (null != locationDisplayManager) {
                                locationDisplayManager.resume();
                            }
                            break;
                        }
                        case STOPPED: {
                            if (null != locationDisplayManager) {
                                locationDisplayManager.start();
                            }
                            break;
                        }
                        case STARTED:
                        default: {
                        }
                    }
                    state = LocationProviderState.STARTED;
                }
            }

            @Override
            public void pause() {
                if (LocationProviderState.STARTED == state) {
                    if (null != locationDisplayManager) {
                        locationDisplayManager.pause();
                    }
                    state = LocationProviderState.PAUSED;
                }
            }

            @Override
            public void stop() {
                if (null != locationDisplayManager) {
                    locationDisplayManager.stop();
                }
                state = LocationProviderState.STOPPED;
            }

            @Override
            public LocationProviderState getState() {
                return state;
            }

            private void setupLocationListener() {
                if (null != locationDisplayManager) {
                    locationDisplayManager.setLocationListener(locationListener);
                }
            }

        };
    }

    @Override
    protected LocationSimulator createLocationSimulator(InputStream gpxInputStream)
            throws ParserConfigurationException, SAXException, IOException {
        return new LocationSimulator(gpxInputStream);
    }

}
