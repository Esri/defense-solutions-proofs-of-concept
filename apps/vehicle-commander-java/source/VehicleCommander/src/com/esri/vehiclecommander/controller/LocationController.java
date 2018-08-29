/*******************************************************************************
 * Copyright 2012-2014 Esri
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
package com.esri.vehiclecommander.controller;

import com.esri.core.gps.GPSException;
import com.esri.core.gps.IGPSWatcher;
import com.esri.core.map.Graphic;
import com.esri.map.GPSLayer;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.LocationSimulator;
import com.esri.vehiclecommander.model.GpsLocationProvider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * A location controller for the Vehicle Commander application.
 */
public class LocationController extends com.esri.militaryapps.controller.LocationController {

    private final MapController mapController;
    private final Object selectedWaypointLock = new Object();

    private GPSLayer gpsLayer;
    private Graphic selectedWaypoint = null;

    /**
     * Creates a new GPSController.
     * @param gpsWatcher the IGPSWatcher to be controlled by this GPSController.
     */
    public LocationController(
            MapController mapController,
            LocationMode mode,
            boolean startImmediately)
            throws IOException, ParserConfigurationException, SAXException {
        super(mode, startImmediately);
        this.mapController = mapController;
        checkAndAddGPSLayer();
    }
    
    @Override
    protected LocationProvider createLocationServiceProvider() {
        return new GpsLocationProvider();
    }

    @Override
    public void reset() throws ParserConfigurationException, SAXException, IOException {
        super.reset();
        
        if (null != gpsLayer) {
            mapController.removeLayer(gpsLayer);
        }
        checkAndAddGPSLayer();
    }
    
    /**
     * Displays or hides the GPS layer.
     * @param show true if the GPS layer should be shown and false otherwise.
     */
    public void showGPSLayer(boolean show) {
        gpsLayer.setVisible(show);
    }

    @Override
    public void start() throws ParserConfigurationException, SAXException, IOException {
        super.start();
        checkAndAddGPSLayer();
        IGPSWatcher gpsWatcher = getGpsWatcher();
        if (null != gpsWatcher) {
            try {
                gpsWatcher.start();
            } catch (GPSException ex) {
                Logger.getLogger(LocationController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private IGPSWatcher getGpsWatcher() {
        IGPSWatcher gpsWatcher = null;
        if (getLocationProvider() instanceof GpsLocationProvider) {
            GpsLocationProvider gpsLocationProvider = (GpsLocationProvider) getLocationProvider();
            gpsWatcher = gpsLocationProvider.getGpsWatcher();
        } else if (getLocationProvider() instanceof IGPSWatcher) {
            gpsWatcher = (IGPSWatcher) getLocationProvider();
        }
        return gpsWatcher;
    }

    /**
     * Checks to see if the GPS layer is in the map. If not, this method adds the
     * GPS layer to the map. The intent of this method is for resetting the map.
     */
    private void checkAndAddGPSLayer() {
        IGPSWatcher gpsWatcher = getGpsWatcher();
        if (null != gpsWatcher
            && !mapController.hasLayer(gpsLayer)) {
            gpsLayer = new GPSLayer(gpsWatcher);
            gpsLayer.setShowTrail(false);
            gpsLayer.setShowTrackPoints(false);
            /**
             * TODO leverage new GPSLayer.setMode instead of doing GPS navigation
             * ourselves. For now, just set it to OFF.
             */
            gpsLayer.setMode(GPSLayer.Mode.OFF);
            mapController.addLayer(gpsLayer, false);
        }
    }

    /**
     * Sets the selected waypoint.
     * @param selectedWaypoint the selected waypoint graphic.
     */
    public void setSelectedWaypoint(Graphic selectedWaypoint) {
        synchronized (selectedWaypointLock) {
            this.selectedWaypoint = selectedWaypoint;
        }
    }
    
    public Graphic getSelectedWaypoint() {
        synchronized (selectedWaypointLock) {
            return selectedWaypoint;
        }
    }

    @Override
    protected LocationSimulator createLocationSimulator() throws ParserConfigurationException, SAXException, IOException {
        if (null == getGpxFile()) {
            return new com.esri.vehiclecommander.model.LocationSimulator();
        } else {
            return new com.esri.vehiclecommander.model.LocationSimulator(getGpxFile());
        }
    }

}
