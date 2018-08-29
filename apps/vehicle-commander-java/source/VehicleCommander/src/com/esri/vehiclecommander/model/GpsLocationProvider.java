package com.esri.vehiclecommander.model;

import com.esri.core.gps.GPSException;
import com.esri.core.gps.IGPSWatcher;
import com.esri.core.gps.SerialPortGPSWatcher;
import com.esri.militaryapps.model.LocationProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A LocationProvider based on ArcGIS Runtime's SerialPortGPSWatcher.
 */
public class GpsLocationProvider extends LocationProvider {
    
    private final SerialPortGPSWatcher gpsWatcher;

    public GpsLocationProvider() {
        gpsWatcher = new SerialPortGPSWatcher();
    }

    @Override
    public void start() {
        try {
            gpsWatcher.start();
        } catch (GPSException ex) {
            Logger.getLogger(GpsLocationProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * For this GPS-based provider, pause() simply calls stop().
     */
    @Override
    public void pause() {
        this.stop();
    }

    @Override
    public void stop() {
        try {
            gpsWatcher.stop();
        } catch (GPSException ex) {
            Logger.getLogger(GpsLocationProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public LocationProviderState getState() {
        switch (gpsWatcher.getStatus()) {
            case RUNNING:
                return LocationProviderState.STARTED;
            default:
                return LocationProviderState.STOPPED;
        }
    }
    
    /**
     * Returns this provider's IGPSWatcher.
     * @return this provider's IGPSWatcher.
     */
    public IGPSWatcher getGpsWatcher() {
        return gpsWatcher;
    }
    
}
