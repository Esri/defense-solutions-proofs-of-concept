/*******************************************************************************
 * Copyright 2012-2015 Esri
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

import com.esri.core.geometry.AngularUnit;
import com.esri.militaryapps.controller.LocationController.LocationMode;
import com.esri.militaryapps.controller.MessageController;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A controller for application configuration settings. For example, this class
 * manages settings used for sending position reports to other users.
 */
public class AppConfigController {

    private static final String KEY_USERNAME = AppConfigController.class.getSimpleName() + "username";
    private static final String KEY_VEHICLE_TYPE = AppConfigController.class.getSimpleName() + "vehicleType";
    private static final String KEY_UNIQUE_ID = AppConfigController.class.getSimpleName() + "uniqueId";
    private static final String KEY_SIC = AppConfigController.class.getSimpleName() + "sic";
    private static final String KEY_PORT = AppConfigController.class.getSimpleName() + "port";
    private static final String KEY_POSITION_MESSAGE_INTERVAL = AppConfigController.class.getSimpleName() + "positionMessageInterval";
    private static final String KEY_VEHICLE_STATUS_MESSAGE_INTERVAL = AppConfigController.class.getSimpleName() + "vehicleStatusMessageInterval";
    private static final String KEY_GPS_TYPE = AppConfigController.class.getSimpleName() + "gpsType";
    private static final String KEY_GPX = AppConfigController.class.getSimpleName() + "gpx";
    private static final String KEY_SPEED_MULTIPLIER = AppConfigController.class.getSimpleName() + "speedMultiplier";
    private static final String KEY_MPK_CHOOSER_DIR = AppConfigController.class.getSimpleName() + "mpkFileChooserDirectory";
    private static final String KEY_GPX_CHOOSER_DIR = AppConfigController.class.getSimpleName() + "gpxFileChooserDirectory";
    private static final String KEY_SHOW_MESSAGE_LABELS = AppConfigController.class.getSimpleName() + "showMessageLabels";
    private static final String KEY_DECORATED = AppConfigController.class.getSimpleName() + "decorated";
    private static final String KEY_SHOW_MGRS_GRID = AppConfigController.class.getSimpleName() + "showMgrsGrid";
    private static final String KEY_SHOW_LOCAL_TIME_ZONE = AppConfigController.class.getSimpleName() + "showLocalTimeZone";
    private static final String KEY_MGRS_COORDINATE_NOTATION = AppConfigController.class.getSimpleName() + "useMgrs";
    private static final String KEY_HEADING_UNITS = AppConfigController.class.getSimpleName() + "headingUnits";
    private static final String KEY_GEOMESSAGE_VERSION = AppConfigController.class.getSimpleName() + "geomessageVersion";

    private boolean gpsTypeDirty = false;

    /**
     * @param messageController the messageController to set
     */
    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
        if (null != messageController && -1 != getPort()) {
            messageController.setPort(getPort());
        }
    }
    
    public MessageController getMessageController() {
        return messageController;
    }

    private class AppConfigHandler extends DefaultHandler {

        private String username = null;
        private String vehicleType = null;
        private String uniqueId = null;
        private String sic = null;
        private int port = -1;
        private int positionMessageInterval = -1;
        private int vehicleStatusMessageInterval = -1;
        private LocationMode gpsType = LocationMode.SIMULATOR;
        private String gpx = null;
        private double speedMultiplier = -1;
        private int headingUnits = AngularUnit.Code.DEGREE;
        private String geomessageVersion = "1.1";
        
        private boolean readingUser = false;
        private boolean readingCode = false;
        private boolean readingMessaging = false;
        private boolean readingPort = false;
        private boolean readingPositionMessageInterval = false;
        private boolean readingVehicleStatusMessageInterval = false;
        private boolean readingGps = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("user".equalsIgnoreCase(qName)) {
                readingUser = true;
                username = attributes.getValue("name");
                vehicleType = attributes.getValue("type");
                uniqueId = attributes.getValue("id");
            } else if (readingUser) {
                if ("code".equalsIgnoreCase(qName)) {
                    readingCode = true;
                }
            } else if ("messaging".equalsIgnoreCase(qName)) {
                readingMessaging = true;
            } else if (readingMessaging) {
                if ("port".equalsIgnoreCase(qName)) {
                    readingPort = true;
                } else if ("interval".equalsIgnoreCase(qName) || "positionmessageinterval".equalsIgnoreCase(qName)) {
                    //Vehicle Commander 1.0 used "interval" instead of "positionmessageinterval"; accept either one
                    readingPositionMessageInterval = true;
                } else if ("vehiclestatusmessageinterval".equalsIgnoreCase(qName)) {
                    readingVehicleStatusMessageInterval = true;
                }
            } else if ("gps".equalsIgnoreCase(qName)) {
                readingGps = true;
                gpsType = "onboard".equalsIgnoreCase(attributes.getValue("type"))
                        ? LocationMode.LOCATION_SERVICE : LocationMode.SIMULATOR;
                gpx = attributes.getValue("gpx");
                String speedMultiplierString = attributes.getValue("speedMultiplier");
                if (null != speedMultiplierString) {
                    try {
                        speedMultiplier = Double.parseDouble(speedMultiplierString);
                    } catch (Throwable t) {}
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);
            if (readingCode) {
                sic = value;
            } else {
                try {
                    int intValue = Integer.parseInt(value);
                    if (readingPort) {
                        port = intValue;
                    } else if (readingPositionMessageInterval) {
                        positionMessageInterval = intValue;
                    } else if (readingVehicleStatusMessageInterval) {
                        vehicleStatusMessageInterval = intValue;
                    }
                } catch (NumberFormatException nfe) {

                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("user".equalsIgnoreCase(qName)) {
                readingUser = false;
            } else if ("code".equalsIgnoreCase(qName)) {
                readingCode = false;
            } else if ("messaging".equalsIgnoreCase(qName)) {
                readingMessaging = false;
            } else if ("port".equalsIgnoreCase(qName)) {
                readingPort = false;
            } else if ("interval".equalsIgnoreCase(qName) || "positionmessageinterval".equalsIgnoreCase(qName)) {
                //Vehicle Commander 1.0 used "interval" instead of "positionmessageinterval"; accept either one
                readingPositionMessageInterval = false;
            } else if ("vehiclestatusmessageinterval".equalsIgnoreCase(qName)) {
                readingVehicleStatusMessageInterval = false;
            } else if ("gps".equalsIgnoreCase(qName)) {
                readingGps = false;
            }
        }
        
    }

    private final Preferences preferences;
    private final Set<AppConfigListener> listeners = new HashSet<AppConfigListener>();

    private LocationController locationController;
    private MessageController messageController;

    /**
     * Creates a new AppConfigController. This constructor first reads the user's
     * settings from the system. Then, if appconfig.xml is present in the working
     * directory, any settings not present in the user profile will be read from
     * appconfig.xml.
     */
    public AppConfigController() {
        preferences = Preferences.userNodeForPackage(getClass());
        
        try {
            resetFromAppConfigFile(false);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(AppConfigController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(AppConfigController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppConfigController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(AppConfigController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addListener(AppConfigListener listener) {
        listeners.add(listener);
    }

    /**
     * Returns the file that will be read when resetting application configuration
     * settings. This file may or may not actually exist.
     * @return the file that will be read when resetting application configuration
     *         settings.
     */
    private URI getAppConfigFileUri() throws URISyntaxException {
        File configFile = new File("./appconfig.xml");
        if (configFile.exists()) {
            return configFile.toURI();
        } else {
            return getClass().getResource("/com/esri/vehiclecommander/resources/appconfig.xml").toURI();
        }
    }

    /**
     * Resets application configuration settings by reading appconfig.xml found
     * in the working directory.
     * @param overwriteExistingSettings true if settings that are present should
     *                                  be overwritten; false if settings that
     *                                  are present should not be overwritten
     */
    public final void resetFromAppConfigFile(boolean overwriteExistingSettings) throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
        if (overwriteExistingSettings || null == getUsername() || null == getUniqueId()
                || null == getVehicleType()
                || null == getGpsType() || null == getGpx() || 0 >= getSpeedMultiplier()
                || null == getSic() || -1 == getPort() || -1 == getPositionMessageInterval()
                || -1 == getVehicleStatusMessageInterval()) {
            AppConfigHandler handler = new AppConfigHandler();
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(getAppConfigFileUri().toString(), handler);
            if (overwriteExistingSettings || null == getUsername()) {
                setUsername(handler.username);
            }
            if (overwriteExistingSettings || null == getVehicleType()) {
                setVehicleType(handler.vehicleType);
            }
            if (overwriteExistingSettings || null == getUniqueId()) {
                setUniqueId(handler.uniqueId);
            }
            if (overwriteExistingSettings || null == getSic()) {
                setSic(handler.sic);
            }
            if (overwriteExistingSettings || -1 == getPort()) {
                setPort(handler.port);
            }
            if (overwriteExistingSettings || -1 == getPositionMessageInterval()) {
                setPositionMessageInterval(handler.positionMessageInterval);
            }
            if (overwriteExistingSettings || -1 == getVehicleStatusMessageInterval()) {
                setVehicleStatusMessageInterval(handler.vehicleStatusMessageInterval);
            }
            if (overwriteExistingSettings || null == getGpsType()) {
                setLocationMode(handler.gpsType, false);
            }
            if (overwriteExistingSettings || null == getGpx()) {
                setGpx(handler.gpx, false);
            }
            if (overwriteExistingSettings || -1 == getSpeedMultiplier()) {
                setSpeedMultiplier(handler.speedMultiplier);
            }
            if (overwriteExistingSettings || null == getGeomessageVersion()) {
                setGeomessageVersion(handler.geomessageVersion);
            }
            resetLocationController();
        }
    }

    private void setPreference(String key, String value) {
        if (null != key) {
            if (null == value) {
                preferences.remove(key);
            } else {
                preferences.put(key, value);
            }
        }
    }

    private void setPreference(String key, int value) {
        preferences.putInt(key, value);
    }

    private void setPreference(String key, double value) {
        preferences.putDouble(key, value);
    }

    private void setPreference(String key, boolean value) {
        preferences.putBoolean(key, value);
    }

    /**
     * Saves the specified username to the application configuration settings.
     * @param username the username, or null to erase the setting.
     */
    public void setUsername(String username) {
        setPreference(KEY_USERNAME, username);
    }

    /**
     * Returns the stored username, or null if no username has been set.
     * @return the stored username, or null if no username has been set.
     */
    public final String getUsername() {
        return preferences.get(KEY_USERNAME, null);
    }

    /**
     * Saves the specified vehicle type to the application configuration settings.
     * @param vehicleType the vehicle type, or null to erase the setting.
     */
    public void setVehicleType(String vehicleType) {
        setPreference(KEY_VEHICLE_TYPE, vehicleType);
    }

    /**
     * Returns the stored vehicle type, or null if no vehicle type has been set.
     * @return the stored vehicle type, or null if no vehicle type has been set.
     */
    public final String getVehicleType() {
        return preferences.get(KEY_VEHICLE_TYPE, null);
    }

    /**
     * Saves the specified unique ID to the application configuration settings.
     * Normally this is a UUID/GUID.
     * @param uniqueId the unique ID. Unique ID cannot be null, so if uniqueId parameter
     *                 is null, the unique ID will be set to a new random GUID.
     */
    public void setUniqueId(String uniqueId) {
        if (null == uniqueId) {
            uniqueId = UUID.randomUUID().toString();
        }
        setPreference(KEY_UNIQUE_ID, uniqueId);

    }

    /**
     * Returns the stored unique ID. If no ID has been set, a new ID will be generated.
     * @return the stored unique ID. If no ID has been set, a new ID will be generated.
     */
    public final String getUniqueId() {
        String uniqueId = preferences.get(KEY_UNIQUE_ID, null);
        if (null == uniqueId) {
            uniqueId = UUID.randomUUID().toString();
            setUniqueId(uniqueId);
        }
        return uniqueId;
    }

    /**
     * Saves the specified symbol ID code to the application configuration settings.
     * @param sic the symbol ID code, or null to erase the setting.
     */
    public void setSic(String sic) {
        setPreference(KEY_SIC, sic);
    }

    /**
     * Returns the stored symbol ID code, or null if no symbol ID code has been set.
     * @return the stored symbol ID code, or null if no symbol ID code has been set.
     */
    public final String getSic() {
        return preferences.get(KEY_SIC, null);
    }

    /**
     * Saves the specified UDP port number for messaging to the application configuration settings.
     * @param port the messaging port number.
     * @throws IllegalArgumentException if port is less than 0 or more than 0xFFFF.
     */
    public void setPort(int port) throws IllegalArgumentException {
        if (port < 0 || port > 0xFFFF) {
            //Invalid port number; do nothing
            Logger.getLogger(AppConfigController.class.getName()).log(Level.WARNING, "Invalid port number: {0}", port);
        } else {
            setPreference(KEY_PORT, port);
            if (null != messageController) {
                messageController.setPort(port);
            }
        }
    }

    /**
     * Returns the stored UDP port number for messaging, or -1 if no port number has been set.
     * @return the stored UDP port number for messaging, or -1 if no port number has been set.
     */
    public final int getPort() {
        return preferences.getInt(KEY_PORT, -1);
    }

    /**
     * Saves the specified position message interval, in milliseconds, to the application configuration settings.
     * @param positionMessageInterval the position message interval, in milliseconds.
     */
    public void setPositionMessageInterval(int positionMessageInterval) {
        setPreference(KEY_POSITION_MESSAGE_INTERVAL, positionMessageInterval);
    }

    /**
     * Returns the stored position message interval, in milliseconds, or -1 if no messaging interval has been set.
     * @return the stored position message interval, in milliseconds, or -1 if no messaging interval has been set.
     */
    public final int getPositionMessageInterval() {
        return preferences.getInt(KEY_POSITION_MESSAGE_INTERVAL, -1);
    }

    /**
     * Saves the specified vehicle status message interval, in milliseconds, to the application configuration settings.
     * @param vehicleStatusMessageInterval the vehicle status message interval, in milliseconds.
     */
    public void setVehicleStatusMessageInterval(int vehicleStatusMessageInterval) {
        setPreference(KEY_VEHICLE_STATUS_MESSAGE_INTERVAL, vehicleStatusMessageInterval);
    }

    /**
     * Returns the stored vehicle status message interval, in milliseconds, or -1 if no messaging interval has been set.
     * @return the stored vehicle status message interval, in milliseconds, or -1 if no messaging interval has been set.
     */
    public final int getVehicleStatusMessageInterval() {
        return preferences.getInt(KEY_VEHICLE_STATUS_MESSAGE_INTERVAL, -1);
    }

    /**
     * Saves the GPS type to the application configuration settings.
     * @deprecated use setLocationMode instead.
     * @param locationMode the location mode.
     */
    public void setGpsType(LocationMode locationMode) throws ParserConfigurationException, SAXException, IOException {
        setLocationMode(locationMode);
    }

    /**
     * Saves the location mode to the application configuration settings.
     * @param locationMode the location mode.
     */
    public void setLocationMode(LocationMode locationMode) throws ParserConfigurationException, SAXException, IOException {
        setLocationMode(locationMode, true);
    }
    
    private void setLocationMode(LocationMode locationMode, boolean resetNow) throws ParserConfigurationException, SAXException, IOException {
        LocationMode oldMode = getLocationMode();
        setPreference(KEY_GPS_TYPE, locationMode.toString());

        if (null == oldMode || !oldMode.equals(locationMode)) {
            gpsTypeDirty = true;
            if (resetNow) {
                resetLocationController();
            }
        }
    }

    private void resetLocationController() throws ParserConfigurationException, SAXException, IOException {
        if (null != locationController) {
            locationController.pause();
            locationController.reset();
            if (getLocationMode().equals(LocationMode.SIMULATOR)) {
                locationController.setGpxFile(null == getGpx() ? null : new File(getGpx()));
            }
            locationController.setMode(getLocationMode(), false);
            gpsTypeDirty = false;
            locationController.start();
        }
    }

    /**
     * Returns the GPS type.
     * @return the GPS type.
     * @deprecated use getLocationMode() instead.
     */
    public LocationMode getGpsType() {
        return getLocationMode();
    }
    
    public LocationMode getLocationMode() {
        String name = preferences.get(KEY_GPS_TYPE, null);
        if (null == name) {
            return null;
        } else {
            try {
                return LocationMode.valueOf(name);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    /**
     * Saves the GPX file to be used for simulated GPS to the application configuration settings.
     * @param gpx the GPX file to be used for simulated GPS.
     */
    public void setGpx(String gpx) throws ParserConfigurationException, SAXException, IOException {
        setGpx(gpx, true);
    }

    private void setGpx(String gpx, boolean resetNow) throws ParserConfigurationException, SAXException, IOException {
        String oldGpx = getGpx();
        setPreference(KEY_GPX, gpx);
        if (resetNow) {
            if (gpsTypeDirty) {
                resetLocationController();
            } else if (null == oldGpx) {
                if (null != gpx) {
                    resetLocationController();
                }
            } else if (!oldGpx.equals(gpx)) {
                resetLocationController();
            }
        }
    }

    /**
     * Returns the GPX file to be used for simulated GPS.
     * @return the GPX file to be used for simulated GPS.
     */
    public String getGpx() {
        return preferences.get(KEY_GPX, null);
    }

    /**
     * Saves the simulated GPS speed multiplier to the application configuration settings.
     * @param multiplier the simulated GPS speed multiplier.
     */
    public void setSpeedMultiplier(double multiplier) {
        setPreference(KEY_SPEED_MULTIPLIER, multiplier);
        if (null != locationController) {
            locationController.setSpeedMultiplier(multiplier);
        }
    }

    /**
     * Returns the simulated GPS speed multiplier, or -1 if no speed multiplier has been set.
     * @return the simulated GPS speed multiplier, or -1 if no speed multiplier has been set.
     */
    public final double getSpeedMultiplier() {
        return preferences.getDouble(KEY_SPEED_MULTIPLIER, -1);
    }

    /**
     * Saves the current directory for the map package file chooser to the application
     * configuration settings.
     * @param the current directory for the map package file chooser.
     */
    public void setMPKFileChooserCurrentDirectory(String dir) {
        setPreference(KEY_MPK_CHOOSER_DIR, dir);
    }

    /**
     * Returns the stored current directory for the map package file chooser, or
     * null if no directory has been set.
     * @return the stored current directory for the map package file chooser, or
     *         null if no directory has been set.
     */
    public String getMPKFileChooserCurrentDirectory() {
        return preferences.get(KEY_MPK_CHOOSER_DIR, null);
    }

    /**
     * Saves the current directory for the GPX file chooser to the application
     * configuration settings.
     * @param the current directory for the GPX file chooser.
     */
    public void setGPXFileChooserCurrentDirectory(String dir) {
        setPreference(KEY_GPX_CHOOSER_DIR, dir);
    }

    /**
     * Returns the stored current directory for the GPX file chooser, or
     * null if no directory has been set.
     * @return the stored current directory for the GPX file chooser, or
     *         null if no directory has been set.
     */
    public String getGPXFileChooserCurrentDirectory() {
        return preferences.get(KEY_GPX_CHOOSER_DIR, null);
    }

    /**
     * Gets the application's GPSController.
     * @return the gpsController
     * @deprecated use getLocationController() instead.
     */
    public LocationController getGpsController() {
        return getLocationController();
    }
    
    public LocationController getLocationController() {
        return locationController;
    }

    /**
     * Gives this AppConfigController a reference to the application's GPSController.
     * @param gpsController the gpsController to set
     * @deprecated use setLocationController(LocationController) instead.
     */
    public void setGpsController(LocationController locationController) {
        setLocationController(locationController);
    }
    
    public void setLocationController(LocationController locationController) {
        this.locationController = locationController;
    }

    /**
     * Returns true if the application should show labels for new message features.
     * @return true if the application should show labels for new message features.
     */
    public boolean isShowMessageLabels() {
        return preferences.getBoolean(KEY_SHOW_MESSAGE_LABELS, true);
    }

    /**
     * Tells the application whether it should show labels for new message features.
     * @param showMessageLabels true if the application should show labels for new message features.
     */
    public void setShowMessageLabels(final boolean showMessageLabels) {
        final boolean oldValue = isShowMessageLabels();
        setPreference(KEY_SHOW_MESSAGE_LABELS, showMessageLabels);
        if (oldValue != showMessageLabels) {
            synchronized (listeners) {
                for (final AppConfigListener listener : listeners) {
                    new Thread() {

                        @Override
                        public void run() {
                            listener.showMessageLabelsChanged(showMessageLabels);
                        }
                        
                    }.start();                    
                }
            }
        }
    }

    /**
     * Returns true if the application should be decorated (title bar, resizable, etc.).
     * @return true if the application should be decorated (title bar, resizable, etc.).
     */
    public boolean isDecorated() {
        return preferences.getBoolean(KEY_DECORATED, true);
    }

    /**
     * Tells the application whether it should be decorated (title bar, resizable, etc.).
     * @param decorated true if the application should be decorated (title bar, resizable, etc.).
     */
    public void setDecorated(final boolean decorated) {
        boolean oldDecorated = isDecorated();
        setPreference(KEY_DECORATED, decorated);
        if (decorated != oldDecorated) {
            for (final AppConfigListener listener : listeners) {
                listener.decoratedChanged(decorated);
            }
        }
    }
    
    /**
     * Returns true if the application should show an MGRS grid on the map.
     * @return true if the application should show an MGRS grid on the map.
     */
    public boolean isShowMgrsGrid() {
        return preferences.getBoolean(KEY_SHOW_MGRS_GRID, false);
    }

    /**
     * Tells the application whether it should show an MGRS grid on the map.
     * @param showMessageLabels true if the application should show an MGRS grid on the map.
     */
    public void setShowMgrsGrid(final boolean showMgrsGrid) {
        setPreference(KEY_SHOW_MGRS_GRID, showMgrsGrid);
    }
    
    /**
     * Returns true if the application should display the time in the machine's
     * time zone.
     * @return true if the application should display the time in the machine's
     *         time zone.
     */
    public boolean isShowLocalTimeZone() {
        return preferences.getBoolean(KEY_SHOW_LOCAL_TIME_ZONE, false);
    }

    /**
     * Tells the application whether it should display the time in the machine's
     * time zone.
     * @param showLocalTimeZone true if the application should display the time
     *                          in the machine's time zone.
     */
    public void setShowLocalTimeZone(boolean showLocalTimeZone) {
        setPreference(KEY_SHOW_LOCAL_TIME_ZONE, showLocalTimeZone);
    }
    
    /**
     * Returns true if the application should display the current GPS location in
     * MGRS coordinates.
     * @return true if the application should display the current GPS location in
     * MGRS coordinates and false if the application should display the current
     * GPS location in longitude/latitude.
     */
    public boolean isShowMgrs() {
        return preferences.getBoolean(KEY_MGRS_COORDINATE_NOTATION, true);
    }
    
    /**
     * Tells the application whether to use MGRS or longitude/latitude coordinate
     * notation for coordinate display.
     * @param useMgrs true for MGRS and false for longitude/latitude.
     */
    public void setShowMgrs(boolean showMgrs) {
        setPreference(KEY_MGRS_COORDINATE_NOTATION, showMgrs);
    }
    
    /**
     * Tells the application the units in which to display the GPS heading.
     * @param an AngularUnit.Code constant representing the units in which to display
     *        the GPS heading.
     * @see AngularUnit.Code
     */
    public void setHeadingUnits(int headingUnits) {
        setPreference(KEY_HEADING_UNITS, headingUnits);
    }

    /**
     * Returns the units in which to display the GPS heading.
     * @return an AngularUnit.Code constant representing the units in which to display
     *         the GPS heading. The default is degrees.
     */
    public int getHeadingUnits() {
        return preferences.getInt(KEY_HEADING_UNITS, AngularUnit.Code.DEGREE);
    }
    
    /**
     * Tells the application the Geomessage version to use for outgoing messages.
     * @param geomessageVersion the Geomessage version.
     */
    public void setGeomessageVersion(String geomessageVersion) {
        setPreference(KEY_GEOMESSAGE_VERSION, geomessageVersion);
    }
    
    /**
     * Returns the Geomessage version that the application is using for outgoing messages.
     * @return the Geomessage version that the application is using for outgoing messages.
     */
    public String getGeomessageVersion() {
        return preferences.get(KEY_GEOMESSAGE_VERSION, "1.1");
    }

}
