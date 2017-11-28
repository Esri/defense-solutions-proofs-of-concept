/*******************************************************************************
 * Copyright 2014 Esri
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
package com.esri.vehiclecommander.model;

import com.esri.core.gps.FixStatus;
import com.esri.core.gps.GPSEventListener;
import com.esri.core.gps.GPSException;
import com.esri.core.gps.GPSStatus;
import com.esri.core.gps.GeoPosition;
import com.esri.core.gps.GpsGeoCoordinate;
import com.esri.core.gps.IGPSWatcher;
import com.esri.core.gps.PositionChangeType;
import com.esri.militaryapps.controller.LocationListener;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * An IGPSWatcher for the LocationSimulator.
 */
public class LocationSimulator extends com.esri.militaryapps.model.LocationSimulator implements IGPSWatcher {

    /**
     * Instantiates a LocationSimulator that uses the military-apps-library-java's
     * built-in GPX file.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public LocationSimulator() throws ParserConfigurationException, SAXException, IOException {
        super();
    }

    /**
     * Instantiates a LocationSimulator based on a GPX file.
     * @param gpxFile the GPX file.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException 
     */
    public LocationSimulator(File gpxFile) throws ParserConfigurationException, SAXException, IOException {
        super(gpxFile);
    }

    /**
     * Adds a GPSEventListener as specified in IGPSWatcher. This method creates and
     * adds a LocationListener to this object.
     * @param gpsEventListener the GPSEventListener.
     * @throws GPSException 
     */
    public void addListener(final GPSEventListener gpsEventListener) throws GPSException {
        addListener(new LocationListener() {

            public void onLocationChanged(Location location) {
                GpsGeoCoordinate coord = new GpsGeoCoordinate();
                coord.setPositionChangeType(PositionChangeType.POSITION.getCode());
                coord.setCourse(location.getHeading());
                coord.setLongitude(location.getLongitude());
                coord.setLatitude(location.getLatitude());
                coord.setFixStatus(FixStatus.SIMULATION_MODE);
                coord.setSpeed(location.getSpeed());
                gpsEventListener.onPositionChanged(new GeoPosition(coord, location.getTimestamp().getTime()));
            }

            public void onStateChanged(LocationProvider.LocationProviderState state) {
                switch (state) {
                    case STARTED:
                        gpsEventListener.onStatusChanged(GPSStatus.RUNNING);
                        break;
                    case PAUSED:
                    case STOPPED:
                        gpsEventListener.onStatusChanged(GPSStatus.STOPPED);
                        break;
                }
            }
        });
    }

    /**
     * Gets the simulator's GPS status.
     * @return the simulator's GPS status.
     */
    public GPSStatus getStatus() {
        switch (getState()) {
            case STARTED:
                return GPSStatus.RUNNING;
            case PAUSED:
            case STOPPED:
            default:
                return GPSStatus.STOPPED;
        }
    }

    /**
     * Currently, this method does nothing and is only implemented because IGPSWatcher
     * requires it.
     * @param timeout
     * @throws GPSException 
     */
    public void setTimeout(int timeout) throws GPSException {
        
    }

    /**
     * Currently, this method simply returns Integer.MAX_VALUE and is only implemented
     * because IGPSWatcher requires it.
     * @return Integer.MAX_VALUE
     */
    public int getTimeout() {
        return Integer.MAX_VALUE;
    }

    /**
     * Stops this simulator.
     */
    public void dispose() {
        stop();
    }
    
}
