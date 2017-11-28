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
package com.esri.vehiclecommander.view;

import com.esri.core.geometry.AngularUnit;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.map.Layer;
import com.esri.map.MapOverlay;
import com.esri.militaryapps.controller.ChemLightController;
import com.esri.militaryapps.controller.LocationController;
import com.esri.militaryapps.controller.LocationListener;
import com.esri.militaryapps.controller.MessageController;
import com.esri.militaryapps.controller.PositionReportController;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.NavigationMode;
import com.esri.runtime.ArcGISRuntime;
import com.esri.vehiclecommander.controller.AdvancedSymbolController;
import com.esri.vehiclecommander.controller.AppConfigController;
import com.esri.vehiclecommander.controller.AppConfigListener;
import com.esri.vehiclecommander.controller.GPAdapter;
import com.esri.vehiclecommander.controller.IdentifyListener;
import com.esri.vehiclecommander.controller.MapController;
import com.esri.vehiclecommander.controller.RouteController;
import com.esri.vehiclecommander.controller.VehicleStatusController;
import com.esri.vehiclecommander.controller.ViewshedController;
import com.esri.vehiclecommander.model.IdentifiedItem;
import com.esri.vehiclecommander.model.MapConfig;
import com.esri.vehiclecommander.model.MapConfigReader;
import com.esri.vehiclecommander.util.Utilities;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * The JVehicleCommander application class. When instantiated, this class creates
 * a JFrame and opens it with a JMap and other controls.
 *
 * Main frame GUI code goes in this class. If it's not GUI code, consider putting
 * it in another class.
 */
public class VehicleCommanderJFrame extends javax.swing.JFrame
        implements IdentifyListener, AppConfigListener {
    
    @SuppressWarnings("serial")
	private class ChemLightButton extends ToolbarToggleButton {
    
        private final Color color;

        public ChemLightButton(Color color) {
            super();
            setUnselectButton(jToggleButton_deactivateAllTools);
            setMapController(mapController);
            this.color = color;
        }

        @Override
        protected void toggled(boolean selected) {
            if (selected) {
                new Thread() {

                    @Override
                    public void run() {
                        /**
                         * Sleep for a few millis so that any existing component's call to
                         * cancelTrackAsync can finish first.
                         * TODO this depends on a race condition; it's probably fine but we
                         * might want to clean this up in the future.
                         */
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        mapController.trackAsync(new MapOverlayAdapter() {

                            @Override
                            public void mouseClicked(MouseEvent event) {
                                Point pt = mapController.toMapPointObject(event.getX(), event.getY());
                                chemLightController.sendChemLight(pt.getX(), pt.getY(), mapController.getSpatialReference().getID(), color.getRGB());
                            }

                        }, MapController.EVENT_MOUSE_CLICKED);
                    }
                }.start();                    
            } else {
                mapController.cancelTrackAsync();
            }
        }
        
    }

    private static final long serialVersionUID = -4694556823082813076L;
    private static final String KEY_FRAME_EXTENDED_STATE = VehicleCommanderJFrame.class.getSimpleName() + "frameExtendedState";
    private static final String KEY_FRAME_WIDTH = VehicleCommanderJFrame.class.getSimpleName() + "frameWidth";
    private static final String KEY_FRAME_HEIGHT = VehicleCommanderJFrame.class.getSimpleName() + "frameHeight";
    private static final String KEY_FRAME_LOCATION_X = VehicleCommanderJFrame.class.getSimpleName() + "frameLocationX";
    private static final String KEY_FRAME_LOCATION_Y = VehicleCommanderJFrame.class.getSimpleName() + "frameLocationY";
    public static final String LICENSE_NOT_SET = "NOT SET (insert license string here)";
    public static final String BUILT_IN_LICENSE_STRING = LICENSE_NOT_SET; // TODO: (insert license string here)
    public static final String BUILT_IN_EXTS_STRING = LICENSE_NOT_SET;    // TODO: (insert extension license string(s) here)
    public static final String BUILT_IN_CLIENT_ID = LICENSE_NOT_SET;      // TODO: (insert client ID here)
    private final MainMenuJPanel mainMenu;
    private final BasemapsJPanel basemapsPanel;
    private final IdentifyResultsJPanel identifyPanel;
    private final ViewshedJPanel viewshedPanel;
    private final RouteJPanel routePanel;
    private final MapController mapController;
    private final ChemLightController chemLightController;
    private final AppConfigController appConfigController;
    private final MessageController messageController;
    private final VehicleStatusController vehicleStatusController;
    private final PositionReportController positionReportController;
    private final ViewshedController viewshedController;
    private final RouteController routeController;
    private final String mapConfigFilename;
    private MapConfig mapConfig;
    private String licenseString;
    private String[] extsStrings;
    private String clientId;
    private AdvancedSymbolController symbolController;
    private final MapOverlay stopFollowMeOverlay;
    private final Timer updateTimeDisplayTimer;

    /**
     * Creates a new VehicleCommanderJFrame, which in turn creates and opens the application
     * frame and its contents. This default constructor assumes that a map configuration
     * file called mapconfig.xml is found in the working directory.
     */
    public VehicleCommanderJFrame() {
        this("./mapconfig.xml", null, null, null);
    }

    /**
     * Creates a new JVehicleCommanderJFrame, which in turn creates and opens the application
     * frame and its contents. This constructor loads mapConfigFilename as a map
     * configuration file.
     * @param mapConfigFilename the name of the map configuration file to load.
     * @param licenseString the ArcGIS Runtime license string, or the name of a
     *                      file that contains an ArcGIS Runtime license string.
     * @param extsString the ArcGIS Runtime extension license strings, separated
     *                   by semicolons, or the name of a file that contains the
     *                   ArcGIS Runtime extension license strings separated by semicolons.
     * @param clientId the ArcGIS client ID for this app, or the name of a file that
     *                 contains the ArcGIS client ID for this app.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public VehicleCommanderJFrame(String mapConfigFilename, String licenseString, String extsString, String clientId) {
        if (null == mapConfigFilename || !new File(mapConfigFilename).exists()) {
            mapConfigFilename = "./mapconfig.xml";
        }
        this.mapConfigFilename = mapConfigFilename;

        setLicenseString((null != licenseString) ? licenseString : BUILT_IN_LICENSE_STRING);
        setExtensionLicensesString((null != extsString) ? extsString : BUILT_IN_EXTS_STRING);
        setClientId((null != clientId) ? clientId : BUILT_IN_CLIENT_ID);
        
        if (LICENSE_NOT_SET.equals(this.clientId) || LICENSE_NOT_SET.equals(this.licenseString) || LICENSE_NOT_SET.equals(this.extsStrings[0])) {
            System.out.println("Warning: LICENSE NOT SET - this must be run on a Development License machine");
        } else {
            ArcGISRuntime.setClientID(this.clientId);
            ArcGISRuntime.License.setLicense(this.licenseString, this.extsStrings);
        }
        
        ArcGISRuntime.initialize();

        appConfigController = new AppConfigController();
        appConfigController.addListener(this);
        
        initComponents();
        
        getLayeredPane().add(floatingPanel);
        
        addToolbarButton((ToolbarToggleButton) jToggleButton_viewshed);
        addToolbarButton((ToolbarToggleButton) jToggleButton_route);

        messageController = new MessageController(appConfigController.getPort(), appConfigController.getUsername());
        appConfigController.setMessageController(messageController);

        chemLightController = new ChemLightController(messageController, appConfigController.getUsername());

        mapController = new MapController(map, this, appConfigController, chemLightController);

        new Timer(1000 / 24, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ((RotatableImagePanel) rotatableImagePanel_northArrow).setRotation(Math.toRadians(mapController.getRotation()));
            }
        }).start();

        //Window location and size
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        int windowState = prefs.getInt(KEY_FRAME_EXTENDED_STATE, -1) & (Frame.ICONIFIED ^ 0xffffffff);
        if (-1 < windowState) {
            int width = prefs.getInt(KEY_FRAME_WIDTH, getWidth());
            int height = prefs.getInt(KEY_FRAME_HEIGHT, getHeight());
            int locationX = prefs.getInt(KEY_FRAME_LOCATION_X, 0);
            int locationY = prefs.getInt(KEY_FRAME_LOCATION_Y, 0);
            java.awt.Point location = new java.awt.Point(locationX, locationY);
            Rectangle bounds = new Rectangle(locationX, locationY, width, height);
            
            //Verify that the location is on a valid screen
            boolean onValidScreen = false;
            GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screenDevices = genv.getScreenDevices();
            for (GraphicsDevice dev : screenDevices) {
                if (dev.getDefaultConfiguration().getBounds().intersects(bounds)) {
                    onValidScreen = true;
                    break;
                }
            }
            if (!onValidScreen) {
                location = screenDevices[0].getDefaultConfiguration().getBounds().getLocation();
            }
            
            //Only set the size if not maximized
            if (0 == (windowState & Frame.MAXIMIZED_BOTH)) {
                setSize(width, height);
            }
            
            setLocation(location);
            setExtendedState(windowState);
        }

        identifyPanel = new IdentifyResultsJPanel(mapController);
        identifyPanel.setVisible(false);
        getLayeredPane().add(identifyPanel, JLayeredPane.MODAL_LAYER);

        viewshedController = new ViewshedController(mapController);
        viewshedController.addGPListener(new GPAdapter() {

            @Override
            public void gpEnabled() {
                jToggleButton_viewshed.setEnabled(true);
            }

            @Override
            public void gpDisbled() {
                jToggleButton_viewshed.setEnabled(false);
            }
        });
        
        routeController = new RouteController(mapController);
        routeController.addGPListener(new GPAdapter() {

            @Override
            public void gpEnabled() {
                jToggleButton_route.setEnabled(true);
            }

            @Override
            public void gpDisbled() {
                jToggleButton_route.setEnabled(false);
            }
        });
        
        basemapsPanel = new BasemapsJPanel();
        basemapsPanel.setVisible(false);
        getLayeredPane().add(basemapsPanel, JLayeredPane.MODAL_LAYER);
        basemapsPanel.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(ComponentEvent e) {
                jToggleButton_openBasemapPanel.setSelected(false);
            }     
        });
        
        resetMapConfig();
        
        //Set up extensions
        List<Map<String, String>> toolbarItems = mapConfig.getToolbarItems();
        for (Map<String, String> item : toolbarItems) {
            try {
                if (item.containsKey("class")) {
                    if (item.containsKey("jar")) {
                        String jar = item.get("jar");
                        try {
                            Utilities.loadJar(jar);
                        } catch (Exception ex) {
                            Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
                            //Swallow this exception. Who knows--we might be able
                            //to load this item without the JAR.
                        }
                    }
                    String className = item.get("class");
                    Class<?> targetClass = Class.forName(className);
                    if (ToolbarToggleButton.class.isAssignableFrom(targetClass)) {
                        Constructor<?> ctor = targetClass.getDeclaredConstructor();
                        ToolbarToggleButton button = (ToolbarToggleButton) ctor.newInstance();
                        button.setUnselectButton(jToggleButton_deactivateAllTools);
                        if (button instanceof ToolbarToggleButton) {
                            ToolbarToggleButton ttButton = (ToolbarToggleButton) button;
                            ttButton.setProperties(item);
                            ttButton.setMapController(mapController);
                            if (button instanceof ComponentShowingButton) {
                                ((ComponentShowingButton) button).setComponentParent(getLayeredPane());
                            }
                        }
                        addToolbarButton(button);
                    } else if (JButton.class.isAssignableFrom(targetClass)) {
                        Constructor<?> ctor = targetClass.getDeclaredConstructor();
                        JButton button = (JButton) ctor.newInstance();
                        addToolbarButton(button);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
        }
 
        LocationController locationController = null;
        try {
            locationController = mapController.getLocationController();
            locationController.setMode(appConfigController.getLocationMode(), false);
            double speedMultiplier = appConfigController.getSpeedMultiplier();
            if (0 < speedMultiplier) {
                locationController.setSpeedMultiplier(speedMultiplier);
            }
            locationController.addListener(identifyPanel);
            if (locationController instanceof com.esri.vehiclecommander.controller.LocationController) {
                appConfigController.setLocationController((com.esri.vehiclecommander.controller.LocationController) locationController);
            }
            locationController.addListener(new LocationListener() {

                public void onLocationChanged(Location location) {
                    if (null != location) {
                        updatePosition(GeometryEngine.project(location.getLongitude(), location.getLatitude(), mapController.getSpatialReference()), location.getHeading());
                    }
                }

                public void onStateChanged(LocationProvider.LocationProviderState state) {

                }
            });
            locationController.start();
        } catch (Throwable t) {
            Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, t);
            Utilities.showGPSErrorMessage(t.getMessage());
        }
        
        messageController.addListener(symbolController);
        messageController.startReceiving();
        
        positionReportController = new PositionReportController(
                mapController.getLocationController(),
                messageController,
                appConfigController.getUsername(),
                appConfigController.getVehicleType(),
                appConfigController.getUniqueId(),
                appConfigController.getSic());
        positionReportController.setPeriod(appConfigController.getPositionMessageInterval());
        positionReportController.setEnabled(true);
        
        vehicleStatusController = new VehicleStatusController(appConfigController, messageController);

        //Key listener for application-wide key events
        ApplicationKeyListener keyListener = new ApplicationKeyListener(this, mapController);
        addKeyListener(keyListener);
        map.addKeyListener(keyListener);

        mainMenu = new MainMenuJPanel(this, mapController, appConfigController,
                symbolController, routeController, positionReportController);
        routeController.addRouteListener(mainMenu);
        mainMenu.setVisible(false);
        getLayeredPane().add(mainMenu, JLayeredPane.MODAL_LAYER);
        mainMenu.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentHidden(ComponentEvent e) {
                jToggleButton_mainMenu.setSelected(false);
            }
        });
        
        viewshedPanel = new ViewshedJPanel(this, mapController, viewshedController);
        viewshedPanel.setVisible(false);
        ((ComponentShowingButton) jToggleButton_viewshed).setComponentParent(getLayeredPane());
        ((ComponentShowingButton) jToggleButton_viewshed).setComponent(viewshedPanel);

        routePanel = new RouteJPanel(this, mapController, routeController);
        routePanel.setVisible(false);
        ((ComponentShowingButton) jToggleButton_route).setComponentParent(getLayeredPane());
        ((ComponentShowingButton) jToggleButton_route).setComponent(routePanel);
        
        stopFollowMeOverlay = new MapOverlay() {

            @Override
            public void onMouseDragged(MouseEvent event) {
                cancelFollowMe();
                super.onMouseDragged(event);
            }
            
        };
        map.addMapOverlay(stopFollowMeOverlay);
        
        updateTimeDisplayTimer = new Timer(1000 / 24, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                DateFormat dateFormat = appConfigController.isShowLocalTimeZone() ?
                        Utilities.DATE_FORMAT_MILITARY_LOCAL :
                        Utilities.DATE_FORMAT_MILITARY_ZULU;
                jLabel_time.setText(dateFormat.format(new Date()));
            }
        });
        updateTimeDisplayTimer.start();
    }

    /**
     * Initializes or resets the map configuration, according to the contents of
     * the mapconfig.xml file that provided to the constructor, or ./mapconfig.xml
     * if none was provided to the constructor.
     */
    public final void resetMapConfig() {
        mapController.removeAllLayers();

        //Read XML config file
        try {
            mapConfig = MapConfigReader.readMapConfig(
                    new File(mapConfigFilename),
                    mapController,
                    appConfigController,
                    viewshedController);
            basemapsPanel.setBasemapLayers(mapConfig.getBasemapLayers());
        } catch (Throwable t) {
            //If anything goes wrong, we just have to create a blank MapConfig
            mapConfig = new MapConfig();
            JOptionPane.showMessageDialog(this, "There was a problem loading "
                    + mapConfigFilename + ". This could be a problem with the XML file or with one of the layers.\n\nError details:\n\n"
                    + t.getMessage());
        }
        
        try {
            symbolController = new AdvancedSymbolController(mapController,
                    ImageIO.read(getClass().getResourceAsStream("/com/esri/vehiclecommander/resources/spot_report.png")),
                    messageController, appConfigController);
            mapController.setAdvancedSymbolController(symbolController);
            new Thread() {

                @Override
                public void run() {
                    try {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                Image iconImage = VehicleCommanderJFrame.this.getIconImage();
                                final int width;
                                final int height;
                                if (null != iconImage) {
                                    width = iconImage.getWidth(null);
                                    height = iconImage.getHeight(null);
                                } else {
                                    width = height = 16;
                                }
                                setIconImage(symbolController.getSymbolImage("Ground Vehicle F", width, height));
                            }
                        });
                        //First search is sometimes slow, so fire off the first search right here
                        symbolController.findSymbols("ATM Hostile");
                    } catch (IOException ex) {
                        Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }.start();
        } catch (IOException ex) {
            Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cancelFollowMe() {
        mapController.setAutoPan(false);
        jToggleButton_followMe.setSelected(false);
    }

    /**
     * Sets the ArcGIS Runtime license string to use, or the filename of a text file
     * containing the license string.
     * @param licenseString the ArcGIS Runtime license string to use, or the filename
     *                      of a text file containing the license string.
     */
    private void setLicenseString(String licenseString) {
        if (null != licenseString) {
            this.licenseString = readFileIntoStringOrReturnString(licenseString);
        }
    }
    
    private void setExtensionLicensesString(String exts) {
        if (null != exts) {
            String extsStr = readFileIntoStringOrReturnString(exts);
            //Parse the extension strings into an array
            StringTokenizer tok = new StringTokenizer(extsStr, ";");
            extsStrings = new String[tok.countTokens()];
            for (int i = 0; i < extsStrings.length; i++) {
                extsStrings[i] = tok.nextToken();
            }
        }
    }
    
    private void setClientId(String clientId) {
        if (null != clientId) {
            this.clientId = readFileIntoStringOrReturnString(clientId);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup_tools = new javax.swing.ButtonGroup();
        jToggleButton_deactivateAllTools = new javax.swing.JToggleButton();
        jToggleButton_viewshed = new ComponentShowingButton();
        ((ComponentShowingButton) jToggleButton_viewshed).setUnselectButton(jToggleButton_deactivateAllTools);
        jToggleButton_route = new ComponentShowingButton();
        ((ComponentShowingButton) jToggleButton_route).setUnselectButton(jToggleButton_deactivateAllTools);
        buttonGroup_navigationModes = new javax.swing.ButtonGroup();
        floatingPanel = new javax.swing.JPanel();
        jPanel_header = new javax.swing.JPanel();
        jPanel_classification = new javax.swing.JPanel();
        jLabel_classification = new javax.swing.JLabel();
        jToggleButton_openBasemapPanel = new javax.swing.JToggleButton();
        jToggleButton_openAnalysisToolbar = new javax.swing.JToggleButton();
        jPanel_left = new javax.swing.JPanel();
        jPanel_toolButtons = new javax.swing.JPanel();
        jToggleButton_chemLightRed = new ChemLightButton(Color.RED);
        jToggleButton_chemLightYellow = new ChemLightButton(Color.YELLOW);
        jToggleButton_chemLightGreen = new ChemLightButton(Color.GREEN);
        jToggleButton_chemLightBlue = new ChemLightButton(Color.BLUE);
        jToggleButton_mainMenu = new javax.swing.JToggleButton();
        jToggleButton_grid = new javax.swing.JToggleButton();
        jToggleButton_911 = new javax.swing.JToggleButton();
        jPanel_navigation = new javax.swing.JPanel();
        URL resource = getClass().getResource("/com/esri/vehiclecommander/resources/North-Arrow.png");
        Image image = Toolkit.getDefaultToolkit().getImage(resource);
        rotatableImagePanel_northArrow = new RotatableImagePanel(image);
        jToggleButton_followMe = new javax.swing.JToggleButton();
        jButton_zoomIn = new javax.swing.JButton();
        jButton_panUp = new javax.swing.JButton();
        jButton_panLeft = new javax.swing.JButton();
        jButton_panRight = new javax.swing.JButton();
        jButton_panDown = new javax.swing.JButton();
        jButton_zoomOut = new javax.swing.JButton();
        jToggleButton_northUp = new javax.swing.JToggleButton();
        jToggleButton_trackUp = new javax.swing.JToggleButton();
        jToggleButton_waypointUp = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel_footer = new javax.swing.JPanel();
        jPanel_position = new javax.swing.JPanel();
        jLabel_locationLabel = new javax.swing.JLabel();
        jLabel_location = new javax.swing.JLabel();
        jLabel_timeLabel = new javax.swing.JLabel();
        jLabel_time = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0), new java.awt.Dimension(10, 0));
        jLabel_headingLabel = new javax.swing.JLabel();
        jLabel_heading = new javax.swing.JLabel();
        jPanel_mainToolbar = new javax.swing.JPanel();
        jPanel_subToolbar = new javax.swing.JPanel();
        jPanel_subToolbar.setVisible(false);
        jButton_clearMessages = new javax.swing.JButton();
        map = new com.esri.map.JMap();
        map.setShowingEsriLogo(false);

        buttonGroup_tools.add(jToggleButton_deactivateAllTools);
        jToggleButton_deactivateAllTools.setText("jToggleButton1");

        buttonGroup_tools.add(jToggleButton_viewshed);
        jToggleButton_viewshed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Viewshed-Normal.png"))); // NOI18N
        jToggleButton_viewshed.setBorderPainted(false);
        jToggleButton_viewshed.setContentAreaFilled(false);
        jToggleButton_viewshed.setFocusable(false);
        jToggleButton_viewshed.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_viewshed.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_viewshed.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_viewshed.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_viewshed.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Viewshed-Pressed.png"))); // NOI18N

        buttonGroup_tools.add(jToggleButton_route);
        jToggleButton_route.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/OpenRoutePanel-Normal.png"))); // NOI18N
        jToggleButton_route.setBorderPainted(false);
        jToggleButton_route.setContentAreaFilled(false);
        jToggleButton_route.setFocusable(false);
        jToggleButton_route.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_route.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_route.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_route.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_route.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/OpenRoutePanel-Pressed.png"))); // NOI18N

        floatingPanel.setOpaque(false);

        jPanel_header.setBackground(new java.awt.Color(0, 0, 0));
        jPanel_header.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        jPanel_classification.setBackground(new java.awt.Color(0, 204, 0));
        jPanel_classification.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        jLabel_classification.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_classification.setText("Unclassified");
        jPanel_classification.add(jLabel_classification);

        jPanel_header.add(jPanel_classification);

        jToggleButton_openBasemapPanel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Basemap-Normal.png"))); // NOI18N
        jToggleButton_openBasemapPanel.setBorderPainted(false);
        jToggleButton_openBasemapPanel.setContentAreaFilled(false);
        jToggleButton_openBasemapPanel.setFocusable(false);
        jToggleButton_openBasemapPanel.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_openBasemapPanel.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_openBasemapPanel.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_openBasemapPanel.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_openBasemapPanel.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Basemap-Pressed.png"))); // NOI18N
        jToggleButton_openBasemapPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_openBasemapPanelActionPerformed(evt);
            }
        });

        jToggleButton_openAnalysisToolbar.setBackground(new java.awt.Color(216, 216, 216));
        jToggleButton_openAnalysisToolbar.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jToggleButton_openAnalysisToolbar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Tools-Normal.png"))); // NOI18N
        jToggleButton_openAnalysisToolbar.setBorder(null);
        jToggleButton_openAnalysisToolbar.setBorderPainted(false);
        jToggleButton_openAnalysisToolbar.setContentAreaFilled(false);
        jToggleButton_openAnalysisToolbar.setFocusable(false);
        jToggleButton_openAnalysisToolbar.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_openAnalysisToolbar.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_openAnalysisToolbar.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_openAnalysisToolbar.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_openAnalysisToolbar.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Tools-Pressed.png"))); // NOI18N
        jToggleButton_openAnalysisToolbar.setRolloverEnabled(false);
        jToggleButton_openAnalysisToolbar.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Tools-Pressed.png"))); // NOI18N
        jToggleButton_openAnalysisToolbar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_openAnalysisToolbarActionPerformed(evt);
            }
        });

        jPanel_left.setOpaque(false);
        jPanel_left.setLayout(new java.awt.GridBagLayout());

        jPanel_toolButtons.setMaximumSize(new java.awt.Dimension(50, 218));
        jPanel_toolButtons.setMinimumSize(new java.awt.Dimension(50, 218));
        jPanel_toolButtons.setOpaque(false);
        jPanel_toolButtons.setPreferredSize(new java.awt.Dimension(50, 218));

        buttonGroup_tools.add(jToggleButton_chemLightRed);
        jToggleButton_chemLightRed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Red-Normal.png"))); // NOI18N
        jToggleButton_chemLightRed.setBorderPainted(false);
        jToggleButton_chemLightRed.setContentAreaFilled(false);
        jToggleButton_chemLightRed.setFocusable(false);
        jToggleButton_chemLightRed.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_chemLightRed.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightRed.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightRed.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightRed.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Red-Pressed.png"))); // NOI18N

        buttonGroup_tools.add(jToggleButton_chemLightYellow);
        jToggleButton_chemLightYellow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Yellow-Normal.png"))); // NOI18N
        jToggleButton_chemLightYellow.setBorderPainted(false);
        jToggleButton_chemLightYellow.setContentAreaFilled(false);
        jToggleButton_chemLightYellow.setFocusable(false);
        jToggleButton_chemLightYellow.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_chemLightYellow.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightYellow.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightYellow.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightYellow.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Yellow-Pressed.png"))); // NOI18N

        buttonGroup_tools.add(jToggleButton_chemLightGreen);
        jToggleButton_chemLightGreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Green-Normal.png"))); // NOI18N
        jToggleButton_chemLightGreen.setBorderPainted(false);
        jToggleButton_chemLightGreen.setContentAreaFilled(false);
        jToggleButton_chemLightGreen.setFocusable(false);
        jToggleButton_chemLightGreen.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_chemLightGreen.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightGreen.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightGreen.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightGreen.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Green-Pressed.png"))); // NOI18N

        buttonGroup_tools.add(jToggleButton_chemLightBlue);
        jToggleButton_chemLightBlue.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Blue-Normal.png"))); // NOI18N
        jToggleButton_chemLightBlue.setBorderPainted(false);
        jToggleButton_chemLightBlue.setContentAreaFilled(false);
        jToggleButton_chemLightBlue.setFocusable(false);
        jToggleButton_chemLightBlue.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_chemLightBlue.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightBlue.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightBlue.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_chemLightBlue.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/ChemLights-Blue-Pressed.png"))); // NOI18N

        javax.swing.GroupLayout jPanel_toolButtonsLayout = new javax.swing.GroupLayout(jPanel_toolButtons);
        jPanel_toolButtons.setLayout(jPanel_toolButtonsLayout);
        jPanel_toolButtonsLayout.setHorizontalGroup(
            jPanel_toolButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToggleButton_chemLightRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jToggleButton_chemLightYellow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jToggleButton_chemLightGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(jToggleButton_chemLightBlue, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel_toolButtonsLayout.setVerticalGroup(
            jPanel_toolButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_toolButtonsLayout.createSequentialGroup()
                .addComponent(jToggleButton_chemLightRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jToggleButton_chemLightYellow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jToggleButton_chemLightGreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jToggleButton_chemLightBlue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel_left.add(jPanel_toolButtons, new java.awt.GridBagConstraints());

        jToggleButton_mainMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Menu-Normal.png"))); // NOI18N
        jToggleButton_mainMenu.setBorderPainted(false);
        jToggleButton_mainMenu.setContentAreaFilled(false);
        jToggleButton_mainMenu.setFocusable(false);
        jToggleButton_mainMenu.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_mainMenu.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_mainMenu.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_mainMenu.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_mainMenu.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Menu-Pressed.png"))); // NOI18N
        jToggleButton_mainMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_mainMenuActionPerformed(evt);
            }
        });

        jToggleButton_grid.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Grid-Normal.png"))); // NOI18N
        jToggleButton_grid.setSelected(appConfigController.isShowMgrsGrid());
        jToggleButton_grid.setBorderPainted(false);
        jToggleButton_grid.setContentAreaFilled(false);
        jToggleButton_grid.setFocusable(false);
        jToggleButton_grid.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_grid.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_grid.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_grid.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_grid.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Grid-Pressed.png"))); // NOI18N
        jToggleButton_grid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_gridActionPerformed(evt);
            }
        });

        jToggleButton_911.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/911-Normal.png"))); // NOI18N
        jToggleButton_911.setBorderPainted(false);
        jToggleButton_911.setContentAreaFilled(false);
        jToggleButton_911.setFocusable(false);
        jToggleButton_911.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_911.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_911.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_911.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_911.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/911-Pressed.png"))); // NOI18N
        jToggleButton_911.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_911ActionPerformed(evt);
            }
        });

        jPanel_navigation.setOpaque(false);
        jPanel_navigation.setLayout(new java.awt.GridBagLayout());

        rotatableImagePanel_northArrow.setMaximumSize(new java.awt.Dimension(60, 60));
        rotatableImagePanel_northArrow.setMinimumSize(new java.awt.Dimension(60, 60));
        rotatableImagePanel_northArrow.setOpaque(false);
        rotatableImagePanel_northArrow.setPreferredSize(new java.awt.Dimension(60, 60));

        javax.swing.GroupLayout rotatableImagePanel_northArrowLayout = new javax.swing.GroupLayout(rotatableImagePanel_northArrow);
        rotatableImagePanel_northArrow.setLayout(rotatableImagePanel_northArrowLayout);
        rotatableImagePanel_northArrowLayout.setHorizontalGroup(
            rotatableImagePanel_northArrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );
        rotatableImagePanel_northArrowLayout.setVerticalGroup(
            rotatableImagePanel_northArrowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 60, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanel_navigation.add(rotatableImagePanel_northArrow, gridBagConstraints);

        jToggleButton_followMe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/FollowMe-Normal.png"))); // NOI18N
        jToggleButton_followMe.setBorderPainted(false);
        jToggleButton_followMe.setContentAreaFilled(false);
        jToggleButton_followMe.setFocusable(false);
        jToggleButton_followMe.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_followMe.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_followMe.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_followMe.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_followMe.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/FollowMe-Pressed.png"))); // NOI18N
        jToggleButton_followMe.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/FollowMe-Pressed.png"))); // NOI18N
        jToggleButton_followMe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_followMeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        jPanel_navigation.add(jToggleButton_followMe, gridBagConstraints);

        jButton_zoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-Plus-Normal.png"))); // NOI18N
        jButton_zoomIn.setBorderPainted(false);
        jButton_zoomIn.setContentAreaFilled(false);
        jButton_zoomIn.setFocusable(false);
        jButton_zoomIn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_zoomIn.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_zoomIn.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_zoomIn.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_zoomIn.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-Plus-Pressed.png"))); // NOI18N
        jButton_zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_zoomInActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        jPanel_navigation.add(jButton_zoomIn, gridBagConstraints);

        jButton_panUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-North-Normal.png"))); // NOI18N
        jButton_panUp.setBorderPainted(false);
        jButton_panUp.setContentAreaFilled(false);
        jButton_panUp.setFocusable(false);
        jButton_panUp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_panUp.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_panUp.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_panUp.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_panUp.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-North-Pressed.png"))); // NOI18N
        jButton_panUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_panUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        jPanel_navigation.add(jButton_panUp, gridBagConstraints);

        jButton_panLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-West-Normal.png"))); // NOI18N
        jButton_panLeft.setBorderPainted(false);
        jButton_panLeft.setContentAreaFilled(false);
        jButton_panLeft.setFocusable(false);
        jButton_panLeft.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_panLeft.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_panLeft.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_panLeft.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_panLeft.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-West-Pressed.png"))); // NOI18N
        jButton_panLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_panLeftActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        jPanel_navigation.add(jButton_panLeft, gridBagConstraints);

        jButton_panRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-East-Normal.png"))); // NOI18N
        jButton_panRight.setBorderPainted(false);
        jButton_panRight.setContentAreaFilled(false);
        jButton_panRight.setFocusable(false);
        jButton_panRight.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_panRight.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_panRight.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_panRight.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_panRight.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-East-Pressed.png"))); // NOI18N
        jButton_panRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_panRightActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        jPanel_navigation.add(jButton_panRight, gridBagConstraints);

        jButton_panDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-South-Normal.png"))); // NOI18N
        jButton_panDown.setBorderPainted(false);
        jButton_panDown.setContentAreaFilled(false);
        jButton_panDown.setFocusable(false);
        jButton_panDown.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_panDown.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_panDown.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_panDown.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_panDown.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-South-Pressed.png"))); // NOI18N
        jButton_panDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_panDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        jPanel_navigation.add(jButton_panDown, gridBagConstraints);

        jButton_zoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-Minus-Normal.png"))); // NOI18N
        jButton_zoomOut.setBorderPainted(false);
        jButton_zoomOut.setContentAreaFilled(false);
        jButton_zoomOut.setFocusable(false);
        jButton_zoomOut.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_zoomOut.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_zoomOut.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_zoomOut.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_zoomOut.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Nav-Controls-Minus-Pressed.png"))); // NOI18N
        jButton_zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_zoomOutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        jPanel_navigation.add(jButton_zoomOut, gridBagConstraints);

        buttonGroup_navigationModes.add(jToggleButton_northUp);
        jToggleButton_northUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapNorth-Normal.png"))); // NOI18N
        jToggleButton_northUp.setSelected(true);
        jToggleButton_northUp.setBorderPainted(false);
        jToggleButton_northUp.setContentAreaFilled(false);
        jToggleButton_northUp.setFocusable(false);
        jToggleButton_northUp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_northUp.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_northUp.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_northUp.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_northUp.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapNorth-Pressed.png"))); // NOI18N
        jToggleButton_northUp.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapNorth-Pressed.png"))); // NOI18N
        jToggleButton_northUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_northUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel_navigation.add(jToggleButton_northUp, gridBagConstraints);

        buttonGroup_navigationModes.add(jToggleButton_trackUp);
        jToggleButton_trackUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapToMovement-Normal.png"))); // NOI18N
        jToggleButton_trackUp.setBorderPainted(false);
        jToggleButton_trackUp.setContentAreaFilled(false);
        jToggleButton_trackUp.setFocusable(false);
        jToggleButton_trackUp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_trackUp.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_trackUp.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_trackUp.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_trackUp.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapToMovement-Pressed.png"))); // NOI18N
        jToggleButton_trackUp.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapToMovement-Pressed.png"))); // NOI18N
        jToggleButton_trackUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_trackUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        jPanel_navigation.add(jToggleButton_trackUp, gridBagConstraints);

        buttonGroup_navigationModes.add(jToggleButton_waypointUp);
        jToggleButton_waypointUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapToWaypoint-Normal.png"))); // NOI18N
        jToggleButton_waypointUp.setBorderPainted(false);
        jToggleButton_waypointUp.setContentAreaFilled(false);
        jToggleButton_waypointUp.setFocusable(false);
        jToggleButton_waypointUp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton_waypointUp.setMaximumSize(new java.awt.Dimension(50, 50));
        jToggleButton_waypointUp.setMinimumSize(new java.awt.Dimension(50, 50));
        jToggleButton_waypointUp.setPreferredSize(new java.awt.Dimension(50, 50));
        jToggleButton_waypointUp.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapToWaypoint-Pressed.png"))); // NOI18N
        jToggleButton_waypointUp.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/RotateMapToWaypoint-Pressed.png"))); // NOI18N
        jToggleButton_waypointUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_waypointUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        jPanel_navigation.add(jToggleButton_waypointUp, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setMaximumSize(new java.awt.Dimension(8, 32767));
        jSeparator1.setMinimumSize(new java.awt.Dimension(8, 0));
        jSeparator1.setPreferredSize(new java.awt.Dimension(8, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridheight = 5;
        jPanel_navigation.add(jSeparator1, gridBagConstraints);

        jPanel_footer.setOpaque(false);

        jPanel_position.setBackground(new java.awt.Color(216, 216, 216));
        jPanel_position.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel_position.setLayout(new java.awt.GridBagLayout());

        jLabel_locationLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel_locationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_locationLabel.setText("Location: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        jPanel_position.add(jLabel_locationLabel, gridBagConstraints);

        jLabel_location.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel_location.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        jPanel_position.add(jLabel_location, gridBagConstraints);

        jLabel_timeLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel_timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel_timeLabel.setText("Time: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel_position.add(jLabel_timeLabel, gridBagConstraints);

        jLabel_time.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel_time.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel_position.add(jLabel_time, gridBagConstraints);
        jPanel_position.add(filler1, new java.awt.GridBagConstraints());

        jLabel_headingLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel_headingLabel.setText("Heading: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 0);
        jPanel_position.add(jLabel_headingLabel, gridBagConstraints);

        jLabel_heading.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel_heading.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 2);
        jPanel_position.add(jLabel_heading, gridBagConstraints);

        jPanel_footer.add(jPanel_position);

        jPanel_mainToolbar.setFocusable(false);
        jPanel_mainToolbar.setOpaque(false);
        jPanel_mainToolbar.setLayout(new javax.swing.BoxLayout(jPanel_mainToolbar, javax.swing.BoxLayout.X_AXIS));
        jPanel_mainToolbar.setVisible(false);

        jPanel_subToolbar.setFocusable(false);
        jPanel_subToolbar.setOpaque(false);
        jPanel_subToolbar.setLayout(new javax.swing.BoxLayout(jPanel_subToolbar, javax.swing.BoxLayout.LINE_AXIS));

        jButton_clearMessages.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Discard-Normal.png"))); // NOI18N
        jButton_clearMessages.setBorderPainted(false);
        jButton_clearMessages.setContentAreaFilled(false);
        jButton_clearMessages.setFocusable(false);
        jButton_clearMessages.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_clearMessages.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_clearMessages.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_clearMessages.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_clearMessages.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Discard-Pressed.png"))); // NOI18N
        jButton_clearMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_clearMessagesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout floatingPanelLayout = new javax.swing.GroupLayout(floatingPanel);
        floatingPanel.setLayout(floatingPanelLayout);
        floatingPanelLayout.setHorizontalGroup(
            floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(floatingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(floatingPanelLayout.createSequentialGroup()
                        .addComponent(jToggleButton_mainMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jToggleButton_grid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(86, 86, 86)
                        .addComponent(jPanel_footer, javax.swing.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
                    .addGroup(floatingPanelLayout.createSequentialGroup()
                        .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel_left, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButton_openBasemapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(floatingPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jToggleButton_openAnalysisToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel_mainToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(floatingPanelLayout.createSequentialGroup()
                                .addGap(70, 70, 70)
                                .addComponent(jPanel_subToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(floatingPanelLayout.createSequentialGroup()
                        .addComponent(jButton_clearMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButton_911, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel_navigation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10))
            .addComponent(jPanel_header, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        floatingPanelLayout.setVerticalGroup(
            floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(floatingPanelLayout.createSequentialGroup()
                .addComponent(jPanel_header, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(floatingPanelLayout.createSequentialGroup()
                        .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton_openAnalysisToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButton_openBasemapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel_mainToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel_left, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(floatingPanelLayout.createSequentialGroup()
                                .addComponent(jPanel_subToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton_mainMenu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel_footer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jToggleButton_grid, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(floatingPanelLayout.createSequentialGroup()
                        .addGroup(floatingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jToggleButton_911, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton_clearMessages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel_navigation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(11, 11, 11))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Vehicle Commander");
        setUndecorated(!appConfigController.isDecorated());
        setPreferredSize(new java.awt.Dimension(1024, 708));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        map.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mapComponentResized(evt);
            }
        });

        javax.swing.GroupLayout mapLayout = new javax.swing.GroupLayout(map);
        map.setLayout(mapLayout);
        mapLayout.setHorizontalGroup(
            mapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 842, Short.MAX_VALUE)
        );
        mapLayout.setVerticalGroup(
            mapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 405, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton_zoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_zoomInActionPerformed
        mapController.zoomIn();
    }//GEN-LAST:event_jButton_zoomInActionPerformed

    private void jButton_zoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_zoomOutActionPerformed
        mapController.zoomOut();
    }//GEN-LAST:event_jButton_zoomOutActionPerformed

    private void jButton_panUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_panUpActionPerformed
        cancelFollowMe();
        mapController.pan(MapController.PanDirection.UP);
    }//GEN-LAST:event_jButton_panUpActionPerformed

    private void jButton_panDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_panDownActionPerformed
        cancelFollowMe();
        mapController.pan(MapController.PanDirection.DOWN);
    }//GEN-LAST:event_jButton_panDownActionPerformed

    private void jButton_panLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_panLeftActionPerformed
        cancelFollowMe();
        mapController.pan(MapController.PanDirection.LEFT);
    }//GEN-LAST:event_jButton_panLeftActionPerformed

    private void jButton_panRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_panRightActionPerformed
        cancelFollowMe();
        mapController.pan(MapController.PanDirection.RIGHT);
    }//GEN-LAST:event_jButton_panRightActionPerformed

    private void jToggleButton_mainMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_mainMenuActionPerformed
        if (jToggleButton_mainMenu.isSelected()) {
            int menuWidth = getWidth() / 5;
            if (300 > menuWidth) {
                menuWidth = 300;
            }
            mainMenu.setSize(menuWidth, jToggleButton_mainMenu.getLocation().y - 5 - jToggleButton_openBasemapPanel.getLocation().y);
            mainMenu.setLocation(jToggleButton_openBasemapPanel.getLocation());
            mainMenu.setVisible(true);
        } else {
            mainMenu.setVisible(false);
            mainMenu.resetMenu();
        }
    }//GEN-LAST:event_jToggleButton_mainMenuActionPerformed

    private void jToggleButton_911ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_911ActionPerformed
        positionReportController.setStatus911(jToggleButton_911.isSelected());
    }//GEN-LAST:event_jToggleButton_911ActionPerformed

    private void jToggleButton_followMeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_followMeActionPerformed
        mapController.setAutoPan(jToggleButton_followMe.isSelected());
    }//GEN-LAST:event_jToggleButton_followMeActionPerformed

    private void jToggleButton_gridActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_gridActionPerformed
        mapController.setGridVisible(jToggleButton_grid.isSelected());
        appConfigController.setShowMgrsGrid(jToggleButton_grid.isSelected());
    }//GEN-LAST:event_jToggleButton_gridActionPerformed

    private void jToggleButton_openAnalysisToolbarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_openAnalysisToolbarActionPerformed
        boolean visible = jToggleButton_openAnalysisToolbar.isSelected();
        jPanel_mainToolbar.setVisible(visible);
        if (!visible) {
            //See if one of the tools in this bar is active. If so, deactivate it.
            Component[] components = jPanel_mainToolbar.getComponents();
            for (Component c : components) {
                if (c instanceof JToggleButton) {
                    JToggleButton button = (JToggleButton) c;
                    if (button.isSelected()) {
                        jToggleButton_deactivateAllTools.setSelected(true);
                        break;
                    }
                }
            }
        }
    }//GEN-LAST:event_jToggleButton_openAnalysisToolbarActionPerformed

    private void jToggleButton_openBasemapPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_openBasemapPanelActionPerformed
        if (jToggleButton_openBasemapPanel.isSelected()) {
            java.awt.Point loc = (java.awt.Point) jToggleButton_openBasemapPanel.getLocation().clone();
            loc.y += jToggleButton_openBasemapPanel.getHeight();
            basemapsPanel.setLocation(loc);
            basemapsPanel.setVisible(true);
        } else {
            basemapsPanel.setVisible(false);
        }
    }//GEN-LAST:event_jToggleButton_openBasemapPanelActionPerformed

    private void jToggleButton_trackUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_trackUpActionPerformed
        mapController.getLocationController().setNavigationMode(NavigationMode.TRACK_UP);
    }//GEN-LAST:event_jToggleButton_trackUpActionPerformed

    private void jToggleButton_northUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_northUpActionPerformed
        mapController.getLocationController().setNavigationMode(NavigationMode.NORTH_UP);
        mapController.setRotation(0);
    }//GEN-LAST:event_jToggleButton_northUpActionPerformed

    private void jToggleButton_waypointUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_waypointUpActionPerformed
        mapController.getLocationController().setNavigationMode(NavigationMode.WAYPOINT_UP);
    }//GEN-LAST:event_jToggleButton_waypointUpActionPerformed

    private void mapComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mapComponentResized
        floatingPanel.setSize(map.getSize());
    }//GEN-LAST:event_mapComponentResized

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        map.dispose();
        
        //Store window location and size
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.putInt(KEY_FRAME_EXTENDED_STATE, getExtendedState());
        Dimension size = getSize();
        prefs.putInt(KEY_FRAME_WIDTH, size.width);
        prefs.putInt(KEY_FRAME_HEIGHT, size.height);
        java.awt.Point location = getLocation();
        prefs.putInt(KEY_FRAME_LOCATION_X, (int) Math.round(location.getX()));
        prefs.putInt(KEY_FRAME_LOCATION_Y, (int) Math.round(location.getY()));
    }//GEN-LAST:event_formWindowClosing

    private void jButton_clearMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_clearMessagesActionPerformed
        ArrayList<String> layerNames = new ArrayList<>();
        String[] messageLayerNames = symbolController.getMessageLayerNames();
        for (String layerName : messageLayerNames) {
            layerNames.add(layerName);
        }
        ClearMessagesDialog dialog = ClearMessagesDialog.getInstance(this, false, layerNames, symbolController);
        java.awt.Point buttonLocation = jButton_clearMessages.getLocationOnScreen();
        dialog.setLocation(buttonLocation.x - dialog.getWidth() + jButton_clearMessages.getWidth(), buttonLocation.y + jButton_clearMessages.getHeight());
        dialog.setVisible(true);
    }//GEN-LAST:event_jButton_clearMessagesActionPerformed

    /**
     * Updates the position panel with the specified location and heading.
     * @param mapLocation the location to display.
     * @param headingDegrees the compass heading to display, in degrees.
     */
    public void updatePosition(Point mapLocation, Double headingDegrees) {
        if (null != mapLocation) {
            try {
                String locationString;
                if (appConfigController.isShowMgrs()) {
                    locationString = mapController.pointToMgrs(mapLocation, mapController.getSpatialReference());
                } else {
                    SpatialReference mapSr = mapController.getSpatialReference();
                    if (!mapSr.isWGS84()) {
                        double[] coords = mapController.projectPoint(mapLocation.getX(), mapLocation.getY(), mapSr.getID(), Utilities.WGS84.getID());
                        mapLocation = new Point(coords[0], coords[1]);
                    }
                    locationString = String.format("%06f", mapLocation.getY()) + " " + String.format("%05f", mapLocation.getX());
                }
                jLabel_location.setText(locationString);
            } catch (RuntimeException re) {
                /**
                 * This probably means the map is not yet initialized, so we don't
                 * yet have a valid spatial reference.
                 */
                Logger.getLogger(getClass().getName()).log(Level.FINE, "Couldn't update position text (probably the map is not yet initialized)", re);
            }
        } else {
            jLabel_location.setText("N/A");
        }
        if (null != headingDegrees) {
            AngularUnit destUnit = Utilities.getAngularUnit(appConfigController.getHeadingUnits());
            AngularUnit degreesUnit = Utilities.getAngularUnit(AngularUnit.Code.DEGREE);
            double headingInDestUnit = headingDegrees * degreesUnit.getConversionFactor(destUnit);
            jLabel_heading.setText(Long.toString(Math.round(headingInDestUnit))
                    + destUnit.getAbbreviation());
        } else {
            jLabel_heading.setText("N/A");
        }
    }

    /**
     * Indicates whether the unit is engaged, or in other words, whether the 911
     * button is depressed.
     * @return true if the 911 button is depressed, and false otherwise.
     */
    public boolean isEngaged() {
        return jToggleButton_911.isSelected();
    }
    
    /**
     * If the parameter is a valid filename, reads the file and returns the contents
     * as a string; otherwise, returns the string.
     * @return the file contents, if the parameter is a file; otherwise, the parameter
     *         itself.
     */
    private static String readFileIntoStringOrReturnString(String filenameOrString) {
        File licenseFile = new File(filenameOrString);
        if (licenseFile.exists() && licenseFile.isFile()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(licenseFile));
                String firstLine = reader.readLine();
                if (null != firstLine) {
                    filenameOrString = firstLine.trim();
                }
                reader.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ioe) {
                Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ioe);
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(VehicleCommanderJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return filenameOrString;
    }

    /**
     * Runs the application.
     * @param args the command line arguments. Valid arguments:
     * <ul>
     *     <li>-mapconfig &lt;map configuration XML filename&gt;</li>
     *     <li>-license &lt;license string or file&gt;</li>
     *     <li>-exts <extensions license filename> OR <extension license string 1>;<ext license 2>;...;<ext license n></li>
     *     <li>-clientid &lt;client ID or file&gt;</li>
     * </ul>
     */
    public static void main(String args[]) {
        String mapConfig = null;
        String license = null;
        String exts = null;
        String clientId = null;
        for (int i = 0; i < args.length; i++) {
            if ("-mapconfig".equals(args[i]) && i < (args.length - 1)) {
                mapConfig = args[++i];
            } else if ("-license".equals(args[i]) && i < (args.length - 1)) {
                license = readFileIntoStringOrReturnString(args[++i]);
            } else if ("-exts".equals(args[i]) && i < (args.length - 1)) {
                exts = readFileIntoStringOrReturnString(args[++i]);                
            } else if ("-clientid".equalsIgnoreCase(args[i]) && i < (args.length - 1)) {
                clientId = readFileIntoStringOrReturnString(args[++i]);
            }
        }
        final String finalMapConfig = mapConfig;
        final String finalLicense = license;
        final String finalExts = exts;
        final String finalClientId = clientId;

        String jarName = "<JAR file name>";
        try {
            File jarFile = new File(VehicleCommanderJFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            jarName = jarFile.getName();
            if (!jarName.endsWith(".jar")) {
                jarName = "<JAR file name>";
            }
        } catch (URISyntaxException ex) {
        }
        System.out.println("Vehicle Commander " + Utilities.APP_VERSION);
        System.out.println("Usage: java -jar " + jarName + "\n"
                + "\t-mapconfig \"<map config XML filename>\" (optional)\n"
                + "\t-license \"<ArcGIS Runtime license string or filename>\" (optional)\n"
                + "\t-exts \"<extensions license filename>\" OR \"<extension license string 1>;<ext license 2>;...;<ext license n>\" (optional)");
        System.out.println("Starting Vehicle Commander with these parameters:");
        System.out.println("\tMap configuration XML file: " + (null == finalMapConfig ? "<default>" : finalMapConfig));
        System.out.println("\tArcGIS license string or file: " + (null == finalLicense ? "<default>" : finalLicense));
        System.out.println("\tArcGIS extension license string or file: " + (null == finalExts ? "<default>" : finalExts));

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new VehicleCommanderJFrame(finalMapConfig, finalLicense, finalExts, finalClientId).setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_navigationModes;
    private javax.swing.ButtonGroup buttonGroup_tools;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel floatingPanel;
    private javax.swing.JButton jButton_clearMessages;
    private javax.swing.JButton jButton_panDown;
    private javax.swing.JButton jButton_panLeft;
    private javax.swing.JButton jButton_panRight;
    private javax.swing.JButton jButton_panUp;
    private javax.swing.JButton jButton_zoomIn;
    private javax.swing.JButton jButton_zoomOut;
    private javax.swing.JLabel jLabel_classification;
    private javax.swing.JLabel jLabel_heading;
    private javax.swing.JLabel jLabel_headingLabel;
    private javax.swing.JLabel jLabel_location;
    private javax.swing.JLabel jLabel_locationLabel;
    private javax.swing.JLabel jLabel_time;
    private javax.swing.JLabel jLabel_timeLabel;
    private javax.swing.JPanel jPanel_classification;
    private javax.swing.JPanel jPanel_footer;
    private javax.swing.JPanel jPanel_header;
    private javax.swing.JPanel jPanel_left;
    private javax.swing.JPanel jPanel_mainToolbar;
    private javax.swing.JPanel jPanel_navigation;
    private javax.swing.JPanel jPanel_position;
    private javax.swing.JPanel jPanel_subToolbar;
    private javax.swing.JPanel jPanel_toolButtons;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton jToggleButton_911;
    private javax.swing.JToggleButton jToggleButton_chemLightBlue;
    private javax.swing.JToggleButton jToggleButton_chemLightGreen;
    private javax.swing.JToggleButton jToggleButton_chemLightRed;
    private javax.swing.JToggleButton jToggleButton_chemLightYellow;
    private javax.swing.JToggleButton jToggleButton_deactivateAllTools;
    private javax.swing.JToggleButton jToggleButton_followMe;
    private javax.swing.JToggleButton jToggleButton_grid;
    private javax.swing.JToggleButton jToggleButton_mainMenu;
    private javax.swing.JToggleButton jToggleButton_northUp;
    private javax.swing.JToggleButton jToggleButton_openAnalysisToolbar;
    private javax.swing.JToggleButton jToggleButton_openBasemapPanel;
    private javax.swing.JToggleButton jToggleButton_route;
    private javax.swing.JToggleButton jToggleButton_trackUp;
    private javax.swing.JToggleButton jToggleButton_viewshed;
    private javax.swing.JToggleButton jToggleButton_waypointUp;
    private com.esri.map.JMap map;
    private javax.swing.JPanel rotatableImagePanel_northArrow;
    // End of variables declaration//GEN-END:variables

    /**
     * Called when an identify operation completes.
     * @see IdentifyListener
     * @param identifyPoint the point used to run the identify operation.
     * @param results the identify results.
     * @param resultToLayer a map of results to the layer from which each result comes.
     */
    public void identifyComplete(Point identifyPoint, IdentifiedItem[] results, Map<IdentifiedItem, Layer> resultToLayer) {
        if (0 < results.length) {
            identifyPanel.setIdentifyPoint(identifyPoint);
            identifyPanel.setResults(results, resultToLayer);
            int panelWidth = getWidth() / 5;
            if (300 > panelWidth) {
                panelWidth = 300;
            }
            identifyPanel.setSize(panelWidth,
                    jPanel_navigation.getLocation().y + jPanel_navigation.getSize().height - jToggleButton_911.getLocation().y);
            identifyPanel.setLocation(
                    jPanel_navigation.getLocation().x + jPanel_navigation.getWidth() - identifyPanel.getWidth(),
                    jToggleButton_911.getLocation().y);
            identifyPanel.setVisible(true);
        }
    }

    public void decoratedChanged(boolean isDecorated) {
        if (isUndecorated() == isDecorated) {
            JOptionPane.showMessageDialog(this, "You must restart the application to "
                    + (isDecorated ? "add" : "remove") + " the title bar.");
        }
    }

    public void showMessageLabelsChanged(boolean showMessageLabels) {
        if (null != symbolController) {
            symbolController.setShowLabels(showMessageLabels);
        }
    }
    
    /**
     * Adds an action button to the main toolbar.
     * @param button the button to add.
     */
    public void addToolbarButton(JButton button) {
        jPanel_mainToolbar.add(button);
    }
    
    /**
     * Adds a toggle button to the main toolbar.
     * @param button the button to add.
     */
    public final void addToolbarButton(ToolbarToggleButton button) {
        if (button instanceof ComponentShowingButton) {
            buttonGroup_tools.add(button);
        }
        jPanel_mainToolbar.add(button);
    }
    
}
