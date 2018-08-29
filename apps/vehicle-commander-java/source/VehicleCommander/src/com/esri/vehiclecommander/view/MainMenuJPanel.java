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

import com.esri.core.geometry.Point;
import com.esri.core.gps.Satellite;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.advanced.SymbolProperties;
import com.esri.map.Layer;
import com.esri.map.LayerInitializeCompleteEvent;
import com.esri.map.LayerInitializeCompleteListener;
import com.esri.militaryapps.controller.LocationController;
import com.esri.militaryapps.controller.LocationController.LocationMode;
import com.esri.militaryapps.controller.LocationListener;
import com.esri.militaryapps.controller.PositionReportController;
import com.esri.militaryapps.controller.SpotReportController;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.militaryapps.model.SpotReport;
import com.esri.militaryapps.model.SpotReport.Activity;
import com.esri.militaryapps.model.SpotReport.Size;
import com.esri.vehiclecommander.controller.AdvancedSymbolController;
import com.esri.vehiclecommander.controller.AppConfigController;
import com.esri.vehiclecommander.controller.AppConfigDialog;
import com.esri.vehiclecommander.controller.MapController;
import com.esri.vehiclecommander.controller.MapControllerListenerAdapter;
import com.esri.vehiclecommander.controller.MgrsLayerController;
import com.esri.vehiclecommander.controller.RouteController;
import com.esri.vehiclecommander.controller.RouteListener;
import com.esri.vehiclecommander.util.Utilities;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * The application's main menu.
 */
public class MainMenuJPanel extends RoundedJPanel implements LocationListener, RouteListener {

    private static final long serialVersionUID = 8752811864834332672L;
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 18);

    private final Frame app;
    private final MapController mapController;
    private final SpotReport spotReport;
    private final SpotReportController spotReportController;
    private final AppConfigController appConfigController;
    private final AdvancedSymbolController mil2525CSymbolController;
    private final MgrsLayerController mgrsLayerController;
    private final RouteController routeController;
    private final PositionReportController positionReportController;
    private final Map<JToggleButton, Integer> waypointButtonToGraphicId = new HashMap<JToggleButton, Integer>();
    private final Map<Integer, JToggleButton> graphicIdToWaypointButton = new HashMap<Integer, JToggleButton>();
    
    private AppConfigDialog appConfigDialog = null;
    private JFileChooser mpkFileChooser = null;
    private JFileChooser gpxFileChooser = null;
    private boolean initializedEquipmentBrowse = false;
    private String selectedCategory = null;
    private JToggleButton selectedWaypointButton = null;
    private boolean initializedEquipmentButtons = false;

    /**
     * Creates the MainMenuJPanel but does not add it to the application.
     * @param app The application that is opening this MainMenuJPanel
     * @param mapController the application's MapController.
     * @param locationController the application's LocationController.
     * @param appConfigController the application's AppConfigController.
     * @param mil2525CSymbolController the application's MIL-STD-2525C symbol controller.
     */
    public MainMenuJPanel(Frame app, final MapController mapController, AppConfigController appConfigController,
            AdvancedSymbolController mil2525CSymbolController, RouteController routeController,
            PositionReportController positionReportController) {
        this.mgrsLayerController = new MgrsLayerController(mapController, appConfigController);
        this.app = app;
        this.mapController = mapController;
        if (null != mapController.getLocationController()) {
            mapController.getLocationController().addListener(this);
        }
        this.mil2525CSymbolController = mil2525CSymbolController;
        this.spotReport = new SpotReport();
        this.spotReportController = new SpotReportController(mapController, appConfigController.getMessageController());
        this.appConfigController = appConfigController;
        this.routeController = routeController;
        this.positionReportController = positionReportController;
        initComponents();

        ActionListener equipmentButtonListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setSpotReportEquipment(SpotReport.Equipment.valueOf(e.getActionCommand()));
            }
        };
        ((EquipmentListJPanel) equipmentListJPanel_srEquipmentSearchResults).addButtonListener(equipmentButtonListener);
        ((EquipmentListJPanel) equipmentListJPanel_srEquipmentCategoryResults).addButtonListener(equipmentButtonListener);

        mapController.addListener(new MapControllerListenerAdapter() {

            @Override
            public void layersChanged(boolean isOverlay) {
                refreshTOC();
            }
        });
    }
    
    private synchronized void initEquipmentButtons(JButton[] buttons) {
        if (!initializedEquipmentButtons) {
            for (JButton button : buttons) {
                button.setIcon(new ImageIcon(MainMenuJPanel.this.mil2525CSymbolController.getSymbolImage(button.getText())));
            }
            initializedEquipmentButtons = true;
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup_gpsMode = new javax.swing.ButtonGroup();
        buttonGroup_waypoints = new javax.swing.ButtonGroup();
        jPanel_main = new javax.swing.JPanel();
        jButton_reports = new javax.swing.JButton();
        jButton_overlays = new javax.swing.JButton();
        jButton_waypoints = new javax.swing.JButton();
        jButton_buddies = new javax.swing.JButton();
        jButton_observations = new javax.swing.JButton();
        jButton_navigation = new javax.swing.JButton();
        jButton_options = new javax.swing.JButton();
        jButton_quitApplication = new javax.swing.JButton();
        jButton_close = new javax.swing.JButton();
        jLabel_mainMenu = new javax.swing.JLabel();
        jPanel_reports = new javax.swing.JPanel();
        jButton_spotReport = new javax.swing.JButton();
        jButton_reportsBack = new javax.swing.JButton();
        jLabel_reports = new javax.swing.JLabel();
        jPanel_spotReport = new javax.swing.JPanel();
        jButton_spotReportBack = new javax.swing.JButton();
        jLabel_spotReport = new javax.swing.JLabel();
        jButton_srSize = new javax.swing.JButton();
        jButton_srActivity = new javax.swing.JButton();
        jButton_srLocation = new javax.swing.JButton();
        jButton_srUnit = new javax.swing.JButton();
        jButton_srTime = new javax.swing.JButton();
        jButton_srEquipment = new javax.swing.JButton();
        jButton_srSend = new javax.swing.JButton();
        jPanel_srSize = new javax.swing.JPanel();
        jButton_srSizeBack = new javax.swing.JButton();
        jLabel_srSize = new javax.swing.JLabel();
        jButton_srSizeTeam = new javax.swing.JButton();
        jButton_srSizeSquad = new javax.swing.JButton();
        jButton_srSizeSection = new javax.swing.JButton();
        jButton_srSizePlatoon = new javax.swing.JButton();
        jButton_srSizeCompany = new javax.swing.JButton();
        jButton_srSizeBattalion = new javax.swing.JButton();
        jButton_srSizeRegiment = new javax.swing.JButton();
        jButton_srSizeBrigade = new javax.swing.JButton();
        jButton_srSizeDivision = new javax.swing.JButton();
        jButton_srSizeCorps = new javax.swing.JButton();
        jButton_srSizeArmy = new javax.swing.JButton();
        jButton_srSizeArmyGroup = new javax.swing.JButton();
        jButton_srSizeRegion = new javax.swing.JButton();
        jButton_srSizeCommand = new javax.swing.JButton();
        jPanel_srActivity = new javax.swing.JPanel();
        jButton_srActivityBack = new javax.swing.JButton();
        jLabel_srActivity = new javax.swing.JLabel();
        jButton_srActivityAttacking = new javax.swing.JButton();
        jButton_srActivityDefending = new javax.swing.JButton();
        jButton_srActivityMoving = new javax.swing.JButton();
        jButton_srActivityStationary = new javax.swing.JButton();
        jButton_srActivityCache = new javax.swing.JButton();
        jButton_srActivityCivilian = new javax.swing.JButton();
        jButton_srActivityPersonnelRecovery = new javax.swing.JButton();
        jPanel_srLocation = new javax.swing.JPanel();
        jButton_srLocationBack = new javax.swing.JButton();
        jLabel_srLocation = new javax.swing.JLabel();
        jToggleButton_srLocationFromMap = new javax.swing.JToggleButton();
        jButton_srLocationMGRS = new javax.swing.JButton();
        jButton_srLocationLatLon = new javax.swing.JButton();
        jButton_srLocationOffset = new javax.swing.JButton();
        jPanel_srUnit = new javax.swing.JPanel();
        jButton_srUnitBack = new javax.swing.JButton();
        jLabel_srUnit = new javax.swing.JLabel();
        jButton_srUnitConventional = new javax.swing.JButton();
        jButton_srUnitIrregular = new javax.swing.JButton();
        jButton_srUnitCoalition = new javax.swing.JButton();
        jButton_srUnitHostNation = new javax.swing.JButton();
        jButton_srUnitNGO = new javax.swing.JButton();
        jButton_srUnitCivilian = new javax.swing.JButton();
        jButton_srUnitFacility = new javax.swing.JButton();
        jPanel_srTime = new javax.swing.JPanel();
        jButton_srTimeBack = new javax.swing.JButton();
        jLabel_srTime = new javax.swing.JLabel();
        jButton_srTimeNow = new javax.swing.JButton();
        jButton_srTimeOther = new javax.swing.JButton();
        jPanel_srEquipment = new javax.swing.JPanel();
        jButton_srEquipmentBack = new javax.swing.JButton();
        jLabel_srEquipment = new javax.swing.JLabel();
        jButton_srEquipmentSearch = new javax.swing.JButton();
        jScrollPane_srEquipmentPresets = new javax.swing.JScrollPane();
        jPanel_srEquipmentPresets = new javax.swing.JPanel();
        jButton_srEquipmentMissileLauncherH = new javax.swing.JButton();
        jButton_srEquipmentGrenadeLauncherH = new javax.swing.JButton();
        jButton_srEquipmentHowitzerH = new javax.swing.JButton();
        jButton_srEquipmentArmoredPersonnelCarrierH = new javax.swing.JButton();
        jButton_srEquipmentGroundVehicleH = new javax.swing.JButton();
        jButton_srEquipmentArmoredTankH = new javax.swing.JButton();
        jButton_srEquipmentRifleH = new javax.swing.JButton();
        jButton_srEquipmentIEDH = new javax.swing.JButton();
        jButton_srEquipmentBrowse = new javax.swing.JButton();
        jPanel_srEquipmentSearch = new javax.swing.JPanel();
        jButton_srEquipmentSearchBack = new javax.swing.JButton();
        jLabel_srEquipmentSearch = new javax.swing.JLabel();
        jTextField_srEquipmentSearchField = new javax.swing.JTextField();
        jScrollPane_srEquipmentSearchResults = new javax.swing.JScrollPane();
        jScrollPane_srEquipmentSearchResults.getViewport().setOpaque(false);
        equipmentListJPanel_srEquipmentSearchResults = new EquipmentListJPanel(mil2525CSymbolController);
        jPanel_srEquipmentBrowseCategories = new javax.swing.JPanel();
        jButton_srEquipmentBrowseCategoriesBack = new javax.swing.JButton();
        jLabel_srEquipmentBrowseCategories = new javax.swing.JLabel();
        jScrollPane_srEquipmentCategories = new javax.swing.JScrollPane();
        jScrollPane_srEquipmentSearchResults.getViewport().setOpaque(false);
        jPanel_srEquipmentCategories = new javax.swing.JPanel();
        jPanel_srEquipmentCategory = new javax.swing.JPanel();
        jButton_srEquipmentCategoryBack = new javax.swing.JButton();
        jLabel_srEquipmentCategory = new javax.swing.JLabel();
        jScrollPane_srEquipmentCategoryResults = new javax.swing.JScrollPane();
        jScrollPane_srEquipmentSearchResults.getViewport().setOpaque(false);
        equipmentListJPanel_srEquipmentCategoryResults = new EquipmentListJPanel(mil2525CSymbolController);
        jPanel_overlays = new javax.swing.JPanel();
        jButton_mapPackage = new javax.swing.JButton();
        jButton_overlaysBack = new javax.swing.JButton();
        jLabel_overlays = new javax.swing.JLabel();
        jScrollPane_toc = new javax.swing.JScrollPane();
        jScrollPane_toc.getViewport().setOpaque(false);
        jPanel_toc = new javax.swing.JPanel();
        jPanel_waypoints = new javax.swing.JPanel();
        jButton_waypointsBack = new javax.swing.JButton();
        jLabel_waypoints = new javax.swing.JLabel();
        jScrollPane_waypointsList = new javax.swing.JScrollPane();
        jScrollPane_toc.getViewport().setOpaque(false);
        jPanel_waypointsList = new javax.swing.JPanel();
        jPanel_navigation = new javax.swing.JPanel();
        jButton_navigationBack = new javax.swing.JButton();
        jLabel_navigation = new javax.swing.JLabel();
        jLabel_navMgrs = new javax.swing.JLabel();
        jTextField_navMgrs = new javax.swing.JTextField();
        jButton_navGoToMgrs = new javax.swing.JButton();
        jLabel_mgrsMessage = new javax.swing.JLabel();
        jPanel_options = new javax.swing.JPanel();
        jButton_optionsBack = new javax.swing.JButton();
        jLabel_options = new javax.swing.JLabel();
        jButton_aboutMe = new javax.swing.JButton();
        jToggleButton_showMe = new javax.swing.JToggleButton();
        jToggleButton_sendMyLocation = new javax.swing.JToggleButton();
        jButton_resetMap = new javax.swing.JButton();
        jButton_gpsOptions = new javax.swing.JButton();
        jPanel_gpsOptions = new javax.swing.JPanel();
        jButton_gpsOptionsBack = new javax.swing.JButton();
        jLabel_gpsOptions = new javax.swing.JLabel();
        jButton_chooseGPXFile = new javax.swing.JButton();
        jRadioButton_onboardGPS = new javax.swing.JRadioButton();
        jRadioButton_simulatedGPS = new javax.swing.JRadioButton();
        jLabel_gpsStatus = new javax.swing.JLabel();

        setOpaque(false);
        setLayout(new java.awt.CardLayout());

        jPanel_main.setOpaque(false);

        jButton_reports.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_reports.setText("Reports");
        jButton_reports.setFocusable(false);
        jButton_reports.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_reports.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_reports.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_reports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_reportsActionPerformed(evt);
            }
        });

        jButton_overlays.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_overlays.setText("Overlays");
        jButton_overlays.setFocusable(false);
        jButton_overlays.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_overlays.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_overlays.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_overlays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_overlaysActionPerformed(evt);
            }
        });

        jButton_waypoints.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_waypoints.setText("Waypoints");
        jButton_waypoints.setFocusable(false);
        jButton_waypoints.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_waypoints.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_waypoints.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_waypoints.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_waypointsActionPerformed(evt);
            }
        });

        jButton_buddies.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_buddies.setText("Buddies");
        jButton_buddies.setFocusable(false);
        jButton_buddies.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_buddies.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_buddies.setPreferredSize(new java.awt.Dimension(150, 60));

        jButton_observations.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_observations.setText("Observations");
        jButton_observations.setFocusable(false);
        jButton_observations.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_observations.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_observations.setPreferredSize(new java.awt.Dimension(150, 60));

        jButton_navigation.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_navigation.setText("Navigation");
        jButton_navigation.setFocusable(false);
        jButton_navigation.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_navigation.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_navigation.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_navigation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_navigationActionPerformed(evt);
            }
        });

        jButton_options.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_options.setText("Options");
        jButton_options.setFocusable(false);
        jButton_options.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_options.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_options.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_options.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_optionsActionPerformed(evt);
            }
        });

        jButton_quitApplication.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_quitApplication.setText("Quit App");
        jButton_quitApplication.setFocusable(false);
        jButton_quitApplication.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_quitApplication.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_quitApplication.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_quitApplication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_quitApplicationActionPerformed(evt);
            }
        });

        jButton_close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/X-Normal.png"))); // NOI18N
        jButton_close.setBorderPainted(false);
        jButton_close.setContentAreaFilled(false);
        jButton_close.setFocusable(false);
        jButton_close.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_close.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/X-Pressed.png"))); // NOI18N
        jButton_close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_closeActionPerformed(evt);
            }
        });

        jLabel_mainMenu.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_mainMenu.setText("Main Menu");

        javax.swing.GroupLayout jPanel_mainLayout = new javax.swing.GroupLayout(jPanel_main);
        jPanel_main.setLayout(jPanel_mainLayout);
        jPanel_mainLayout.setHorizontalGroup(
            jPanel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_mainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_close, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_mainMenu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_reports, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_overlays, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_waypoints, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_buddies, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_observations, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_options, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_navigation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel_mainLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton_quitApplication, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel_mainLayout.setVerticalGroup(
            jPanel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_mainLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_close)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_mainMenu)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_reports, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_overlays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_waypoints, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_buddies, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_observations, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_navigation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_options, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(143, Short.MAX_VALUE))
            .addGroup(jPanel_mainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_mainLayout.createSequentialGroup()
                    .addContainerGap(633, Short.MAX_VALUE)
                    .addComponent(jButton_quitApplication, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        add(jPanel_main, "Main Card");

        jPanel_reports.setOpaque(false);

        jButton_spotReport.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_spotReport.setText("Spot Report");
        jButton_spotReport.setFocusable(false);
        jButton_spotReport.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_spotReport.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_spotReport.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_spotReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_spotReportActionPerformed(evt);
            }
        });

        jButton_reportsBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_reportsBack.setBorderPainted(false);
        jButton_reportsBack.setContentAreaFilled(false);
        jButton_reportsBack.setFocusable(false);
        jButton_reportsBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_reportsBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_reportsBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_reportsBackActionPerformed(evt);
            }
        });

        jLabel_reports.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_reports.setText("Reports");

        javax.swing.GroupLayout jPanel_reportsLayout = new javax.swing.GroupLayout(jPanel_reports);
        jPanel_reports.setLayout(jPanel_reportsLayout);
        jPanel_reportsLayout.setHorizontalGroup(
            jPanel_reportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_reportsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_reportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_reportsBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_reports, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_spotReport, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_reportsLayout.setVerticalGroup(
            jPanel_reportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_reportsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_reportsBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_reports)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_spotReport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(539, Short.MAX_VALUE))
        );

        add(jPanel_reports, "Reports Card");

        jPanel_spotReport.setOpaque(false);
        jPanel_spotReport.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel_spotReportComponentShown(evt);
            }
        });

        jButton_spotReportBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_spotReportBack.setBorderPainted(false);
        jButton_spotReportBack.setContentAreaFilled(false);
        jButton_spotReportBack.setFocusable(false);
        jButton_spotReportBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_spotReportBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_spotReportBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_spotReportBackActionPerformed(evt);
            }
        });

        jLabel_spotReport.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_spotReport.setText("Spot Report");

        jButton_srSize.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSize.setText("Size: N/A");
        jButton_srSize.setFocusable(false);
        jButton_srSize.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSize.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSize.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeActionPerformed(evt);
            }
        });

        jButton_srActivity.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivity.setText("Activity: N/A");
        jButton_srActivity.setFocusable(false);
        jButton_srActivity.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivity.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivity.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityActionPerformed(evt);
            }
        });

        jButton_srLocation.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srLocation.setText("Location: N/A");
        jButton_srLocation.setFocusable(false);
        jButton_srLocation.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srLocation.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srLocation.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srLocationActionPerformed(evt);
            }
        });

        jButton_srUnit.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnit.setText("Unit: N/A");
        jButton_srUnit.setFocusable(false);
        jButton_srUnit.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnit.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnit.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitActionPerformed(evt);
            }
        });

        jButton_srTime.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srTime.setText("Time: N/A");
        jButton_srTime.setFocusable(false);
        jButton_srTime.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srTime.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srTime.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srTimeActionPerformed(evt);
            }
        });

        jButton_srEquipment.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipment.setText("Equipment: N/A");
        jButton_srEquipment.setFocusable(false);
        jButton_srEquipment.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipment.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipment.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentActionPerformed(evt);
            }
        });

        jButton_srSend.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSend.setText("Send");
        jButton_srSend.setFocusable(false);
        jButton_srSend.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSend.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSend.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_spotReportLayout = new javax.swing.GroupLayout(jPanel_spotReport);
        jPanel_spotReport.setLayout(jPanel_spotReportLayout);
        jPanel_spotReportLayout.setHorizontalGroup(
            jPanel_spotReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_spotReportLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_spotReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_spotReportBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_spotReport, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSize, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivity, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnit, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srTime, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipment, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSend, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_spotReportLayout.setVerticalGroup(
            jPanel_spotReportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_spotReportLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_spotReportBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_spotReport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srEquipment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 138, Short.MAX_VALUE)
                .addComponent(jButton_srSend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel_spotReport, "Spot Report Card");

        jPanel_srSize.setOpaque(false);

        jButton_srSizeBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srSizeBack.setBorderPainted(false);
        jButton_srSizeBack.setContentAreaFilled(false);
        jButton_srSizeBack.setFocusable(false);
        jButton_srSizeBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srSizeBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srSizeBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeBackActionPerformed(evt);
            }
        });

        jLabel_srSize.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srSize.setText("Size");

        jButton_srSizeTeam.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeTeam.setText(Size.TEAM.toString());
        jButton_srSizeTeam.setFocusable(false);
        jButton_srSizeTeam.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeTeam.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeTeam.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeTeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeTeamActionPerformed(evt);
            }
        });

        jButton_srSizeSquad.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeSquad.setText(Size.SQUAD.toString());
        jButton_srSizeSquad.setFocusable(false);
        jButton_srSizeSquad.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeSquad.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeSquad.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeSquad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeSquadActionPerformed(evt);
            }
        });

        jButton_srSizeSection.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeSection.setText(Size.SECTION.toString());
        jButton_srSizeSection.setFocusable(false);
        jButton_srSizeSection.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeSection.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeSection.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeSection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeSectionActionPerformed(evt);
            }
        });

        jButton_srSizePlatoon.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizePlatoon.setText(Size.PLATOON.toString());
        jButton_srSizePlatoon.setFocusable(false);
        jButton_srSizePlatoon.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizePlatoon.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizePlatoon.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizePlatoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizePlatoonActionPerformed(evt);
            }
        });

        jButton_srSizeCompany.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeCompany.setText(Size.COMPANY.toString());
        jButton_srSizeCompany.setFocusable(false);
        jButton_srSizeCompany.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCompany.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCompany.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCompany.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeCompanyActionPerformed(evt);
            }
        });

        jButton_srSizeBattalion.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeBattalion.setText(Size.BATTALION.toString());
        jButton_srSizeBattalion.setFocusable(false);
        jButton_srSizeBattalion.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeBattalion.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeBattalion.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeBattalion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeBattalionActionPerformed(evt);
            }
        });

        jButton_srSizeRegiment.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeRegiment.setText(Size.REGIMENT.toString());
        jButton_srSizeRegiment.setFocusable(false);
        jButton_srSizeRegiment.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeRegiment.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeRegiment.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeRegiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeRegimentActionPerformed(evt);
            }
        });

        jButton_srSizeBrigade.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeBrigade.setText(Size.BRIGADE.toString());
        jButton_srSizeBrigade.setFocusable(false);
        jButton_srSizeBrigade.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeBrigade.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeBrigade.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeBrigade.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeBrigadeActionPerformed(evt);
            }
        });

        jButton_srSizeDivision.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeDivision.setText(Size.DIVISION.toString());
        jButton_srSizeDivision.setFocusable(false);
        jButton_srSizeDivision.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeDivision.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeDivision.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeDivision.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeDivisionActionPerformed(evt);
            }
        });

        jButton_srSizeCorps.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeCorps.setText(Size.CORPS.toString());
        jButton_srSizeCorps.setFocusable(false);
        jButton_srSizeCorps.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCorps.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCorps.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCorps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeCorpsActionPerformed(evt);
            }
        });

        jButton_srSizeArmy.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeArmy.setText(Size.ARMY.toString());
        jButton_srSizeArmy.setFocusable(false);
        jButton_srSizeArmy.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeArmy.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeArmy.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeArmy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeArmyActionPerformed(evt);
            }
        });

        jButton_srSizeArmyGroup.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeArmyGroup.setText(Size.ARMY_GROUP.toString());
        jButton_srSizeArmyGroup.setFocusable(false);
        jButton_srSizeArmyGroup.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeArmyGroup.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeArmyGroup.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeArmyGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeArmyGroupActionPerformed(evt);
            }
        });

        jButton_srSizeRegion.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeRegion.setText(Size.REGION.toString());
        jButton_srSizeRegion.setFocusable(false);
        jButton_srSizeRegion.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeRegion.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeRegion.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeRegionActionPerformed(evt);
            }
        });

        jButton_srSizeCommand.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srSizeCommand.setText(Size.COMMAND.toString());
        jButton_srSizeCommand.setFocusable(false);
        jButton_srSizeCommand.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCommand.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCommand.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srSizeCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srSizeCommandActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_srSizeLayout = new javax.swing.GroupLayout(jPanel_srSize);
        jPanel_srSize.setLayout(jPanel_srSizeLayout);
        jPanel_srSizeLayout.setHorizontalGroup(
            jPanel_srSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srSizeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_srSizeBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_srSize, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeTeam, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeSquad, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeSection, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizePlatoon, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeCompany, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeBattalion, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeRegiment, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeBrigade, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeDivision, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeCorps, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeArmy, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeArmyGroup, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeRegion, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srSizeCommand, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srSizeLayout.setVerticalGroup(
            jPanel_srSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srSizeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srSizeBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeTeam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeSquad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizePlatoon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeCompany, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeBattalion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeRegiment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeBrigade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeDivision, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeCorps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeArmy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeArmyGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeRegion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srSizeCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel_srSize, "Spot Report Size Card");

        jPanel_srActivity.setOpaque(false);

        jButton_srActivityBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srActivityBack.setBorderPainted(false);
        jButton_srActivityBack.setContentAreaFilled(false);
        jButton_srActivityBack.setFocusable(false);
        jButton_srActivityBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srActivityBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srActivityBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityBackActionPerformed(evt);
            }
        });

        jLabel_srActivity.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srActivity.setText("Activity");

        jButton_srActivityAttacking.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityAttacking.setText("Attacking");
        jButton_srActivityAttacking.setFocusable(false);
        jButton_srActivityAttacking.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityAttacking.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityAttacking.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityAttacking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityAttackingActionPerformed(evt);
            }
        });

        jButton_srActivityDefending.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityDefending.setText("Defending");
        jButton_srActivityDefending.setFocusable(false);
        jButton_srActivityDefending.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityDefending.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityDefending.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityDefending.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityDefendingActionPerformed(evt);
            }
        });

        jButton_srActivityMoving.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityMoving.setText("Moving");
        jButton_srActivityMoving.setFocusable(false);
        jButton_srActivityMoving.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityMoving.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityMoving.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityMoving.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityMovingActionPerformed(evt);
            }
        });

        jButton_srActivityStationary.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityStationary.setText("Stationary");
        jButton_srActivityStationary.setFocusable(false);
        jButton_srActivityStationary.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityStationary.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityStationary.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityStationary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityStationaryActionPerformed(evt);
            }
        });

        jButton_srActivityCache.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityCache.setText("Cache");
        jButton_srActivityCache.setFocusable(false);
        jButton_srActivityCache.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityCache.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityCache.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityCache.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityCacheActionPerformed(evt);
            }
        });

        jButton_srActivityCivilian.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityCivilian.setText("Civilian");
        jButton_srActivityCivilian.setFocusable(false);
        jButton_srActivityCivilian.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityCivilian.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityCivilian.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityCivilian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityCivilianActionPerformed(evt);
            }
        });

        jButton_srActivityPersonnelRecovery.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srActivityPersonnelRecovery.setText("Personnel Recovery");
        jButton_srActivityPersonnelRecovery.setFocusable(false);
        jButton_srActivityPersonnelRecovery.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityPersonnelRecovery.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srActivityPersonnelRecovery.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srActivityPersonnelRecovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srActivityPersonnelRecoveryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_srActivityLayout = new javax.swing.GroupLayout(jPanel_srActivity);
        jPanel_srActivity.setLayout(jPanel_srActivityLayout);
        jPanel_srActivityLayout.setHorizontalGroup(
            jPanel_srActivityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srActivityLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srActivityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_srActivityBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_srActivity, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityAttacking, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityDefending, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityMoving, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityStationary, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityCache, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityCivilian, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srActivityPersonnelRecovery, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srActivityLayout.setVerticalGroup(
            jPanel_srActivityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srActivityLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srActivityBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srActivity)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityAttacking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityDefending, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityMoving, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityStationary, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityCache, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityCivilian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srActivityPersonnelRecovery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(143, Short.MAX_VALUE))
        );

        add(jPanel_srActivity, "Spot Report Activity Card");

        jPanel_srLocation.setOpaque(false);

        jButton_srLocationBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srLocationBack.setBorderPainted(false);
        jButton_srLocationBack.setContentAreaFilled(false);
        jButton_srLocationBack.setFocusable(false);
        jButton_srLocationBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srLocationBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srLocationBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srLocationBackActionPerformed(evt);
            }
        });

        jLabel_srLocation.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srLocation.setText("Location");

        jToggleButton_srLocationFromMap.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jToggleButton_srLocationFromMap.setText("From Map");
        jToggleButton_srLocationFromMap.setFocusable(false);
        jToggleButton_srLocationFromMap.setMaximumSize(new java.awt.Dimension(150, 60));
        jToggleButton_srLocationFromMap.setMinimumSize(new java.awt.Dimension(150, 60));
        jToggleButton_srLocationFromMap.setPreferredSize(new java.awt.Dimension(150, 60));
        jToggleButton_srLocationFromMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_srLocationFromMapActionPerformed(evt);
            }
        });

        jButton_srLocationMGRS.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srLocationMGRS.setText("Manual MGRS");
        jButton_srLocationMGRS.setEnabled(false);
        jButton_srLocationMGRS.setFocusable(false);
        jButton_srLocationMGRS.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srLocationMGRS.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srLocationMGRS.setPreferredSize(new java.awt.Dimension(150, 60));

        jButton_srLocationLatLon.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srLocationLatLon.setText("Manual Latitude/Longitude");
        jButton_srLocationLatLon.setEnabled(false);
        jButton_srLocationLatLon.setFocusable(false);
        jButton_srLocationLatLon.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srLocationLatLon.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srLocationLatLon.setPreferredSize(new java.awt.Dimension(150, 60));

        jButton_srLocationOffset.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srLocationOffset.setText("Offset from Me");
        jButton_srLocationOffset.setEnabled(false);
        jButton_srLocationOffset.setFocusable(false);
        jButton_srLocationOffset.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srLocationOffset.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srLocationOffset.setPreferredSize(new java.awt.Dimension(150, 60));

        javax.swing.GroupLayout jPanel_srLocationLayout = new javax.swing.GroupLayout(jPanel_srLocation);
        jPanel_srLocation.setLayout(jPanel_srLocationLayout);
        jPanel_srLocationLayout.setHorizontalGroup(
            jPanel_srLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srLocationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel_srLocationLayout.createSequentialGroup()
                        .addGroup(jPanel_srLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton_srLocationBack, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel_srLocation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(jPanel_srLocationLayout.createSequentialGroup()
                        .addComponent(jToggleButton_srLocationFromMap, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                        .addGap(12, 12, 12))
                    .addGroup(jPanel_srLocationLayout.createSequentialGroup()
                        .addGroup(jPanel_srLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton_srLocationLatLon, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                            .addComponent(jButton_srLocationOffset, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                            .addComponent(jButton_srLocationMGRS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel_srLocationLayout.setVerticalGroup(
            jPanel_srLocationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srLocationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srLocationBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srLocation)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton_srLocationFromMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srLocationMGRS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srLocationLatLon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srLocationOffset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(341, Short.MAX_VALUE))
        );

        add(jPanel_srLocation, "Spot Report Location Card");

        jPanel_srUnit.setOpaque(false);

        jButton_srUnitBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srUnitBack.setBorderPainted(false);
        jButton_srUnitBack.setContentAreaFilled(false);
        jButton_srUnitBack.setFocusable(false);
        jButton_srUnitBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srUnitBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srUnitBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitBackActionPerformed(evt);
            }
        });

        jLabel_srUnit.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srUnit.setText("Activity");

        jButton_srUnitConventional.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitConventional.setText("Conventional");
        jButton_srUnitConventional.setFocusable(false);
        jButton_srUnitConventional.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitConventional.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitConventional.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitConventional.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitConventionalActionPerformed(evt);
            }
        });

        jButton_srUnitIrregular.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitIrregular.setText("Irregular");
        jButton_srUnitIrregular.setFocusable(false);
        jButton_srUnitIrregular.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitIrregular.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitIrregular.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitIrregular.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitIrregularActionPerformed(evt);
            }
        });

        jButton_srUnitCoalition.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitCoalition.setText("Coalition");
        jButton_srUnitCoalition.setFocusable(false);
        jButton_srUnitCoalition.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitCoalition.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitCoalition.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitCoalition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitCoalitionActionPerformed(evt);
            }
        });

        jButton_srUnitHostNation.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitHostNation.setText("Host Nation");
        jButton_srUnitHostNation.setFocusable(false);
        jButton_srUnitHostNation.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitHostNation.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitHostNation.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitHostNation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitHostNationActionPerformed(evt);
            }
        });

        jButton_srUnitNGO.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitNGO.setText("NGO");
        jButton_srUnitNGO.setFocusable(false);
        jButton_srUnitNGO.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitNGO.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitNGO.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitNGO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitNGOActionPerformed(evt);
            }
        });

        jButton_srUnitCivilian.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitCivilian.setText("Civilian");
        jButton_srUnitCivilian.setFocusable(false);
        jButton_srUnitCivilian.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitCivilian.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitCivilian.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitCivilian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitCivilianActionPerformed(evt);
            }
        });

        jButton_srUnitFacility.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srUnitFacility.setText("Facility");
        jButton_srUnitFacility.setFocusable(false);
        jButton_srUnitFacility.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitFacility.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srUnitFacility.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srUnitFacility.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srUnitFacilityActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_srUnitLayout = new javax.swing.GroupLayout(jPanel_srUnit);
        jPanel_srUnit.setLayout(jPanel_srUnitLayout);
        jPanel_srUnitLayout.setHorizontalGroup(
            jPanel_srUnitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srUnitLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srUnitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_srUnitBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_srUnit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitConventional, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitIrregular, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitCoalition, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitHostNation, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitNGO, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitCivilian, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srUnitFacility, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srUnitLayout.setVerticalGroup(
            jPanel_srUnitLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srUnitLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srUnitBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srUnit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitConventional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitIrregular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitCoalition, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitHostNation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitNGO, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitCivilian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srUnitFacility, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(143, Short.MAX_VALUE))
        );

        add(jPanel_srUnit, "Spot Report Unit Card");

        jPanel_srTime.setOpaque(false);

        jButton_srTimeBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srTimeBack.setBorderPainted(false);
        jButton_srTimeBack.setContentAreaFilled(false);
        jButton_srTimeBack.setFocusable(false);
        jButton_srTimeBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srTimeBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srTimeBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srTimeBackActionPerformed(evt);
            }
        });

        jLabel_srTime.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srTime.setText("Time");

        jButton_srTimeNow.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srTimeNow.setText("Now");
        jButton_srTimeNow.setFocusable(false);
        jButton_srTimeNow.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srTimeNow.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srTimeNow.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srTimeNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srTimeNowActionPerformed(evt);
            }
        });

        jButton_srTimeOther.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srTimeOther.setText("Other");
        jButton_srTimeOther.setEnabled(false);
        jButton_srTimeOther.setFocusable(false);
        jButton_srTimeOther.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srTimeOther.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srTimeOther.setPreferredSize(new java.awt.Dimension(150, 60));

        javax.swing.GroupLayout jPanel_srTimeLayout = new javax.swing.GroupLayout(jPanel_srTime);
        jPanel_srTime.setLayout(jPanel_srTimeLayout);
        jPanel_srTimeLayout.setHorizontalGroup(
            jPanel_srTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_srTimeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton_srTimeNow, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srTimeBack)
                    .addComponent(jLabel_srTime, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srTimeOther, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srTimeLayout.setVerticalGroup(
            jPanel_srTimeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srTimeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srTimeBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srTimeNow, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srTimeOther, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(473, Short.MAX_VALUE))
        );

        add(jPanel_srTime, "Spot Report Time Card");

        jPanel_srEquipment.setOpaque(false);

        jButton_srEquipmentBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srEquipmentBack.setBorderPainted(false);
        jButton_srEquipmentBack.setContentAreaFilled(false);
        jButton_srEquipmentBack.setFocusable(false);
        jButton_srEquipmentBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srEquipmentBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srEquipmentBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentBackActionPerformed(evt);
            }
        });

        jLabel_srEquipment.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srEquipment.setText("Equipment");

        jButton_srEquipmentSearch.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentSearch.setText("Search");
        jButton_srEquipmentSearch.setFocusable(false);
        jButton_srEquipmentSearch.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentSearch.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentSearch.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentSearch.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentSearchActionPerformed(evt);
            }
        });

        jPanel_srEquipmentPresets.setLayout(new javax.swing.BoxLayout(jPanel_srEquipmentPresets, javax.swing.BoxLayout.Y_AXIS));

        jButton_srEquipmentMissileLauncherH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentMissileLauncherH.setText("Missile Launcher H");
        jButton_srEquipmentMissileLauncherH.setFocusable(false);
        jButton_srEquipmentMissileLauncherH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentMissileLauncherH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentMissileLauncherH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentMissileLauncherH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentMissileLauncherH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentMissileLauncherHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentMissileLauncherH);

        jButton_srEquipmentGrenadeLauncherH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentGrenadeLauncherH.setText("Grenade Launcher H");
        jButton_srEquipmentGrenadeLauncherH.setFocusable(false);
        jButton_srEquipmentGrenadeLauncherH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentGrenadeLauncherH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentGrenadeLauncherH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentGrenadeLauncherH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentGrenadeLauncherH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentGrenadeLauncherHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentGrenadeLauncherH);

        jButton_srEquipmentHowitzerH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentHowitzerH.setText("Howitzer H");
        jButton_srEquipmentHowitzerH.setFocusable(false);
        jButton_srEquipmentHowitzerH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentHowitzerH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentHowitzerH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentHowitzerH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentHowitzerH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentHowitzerHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentHowitzerH);

        jButton_srEquipmentArmoredPersonnelCarrierH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentArmoredPersonnelCarrierH.setText("Armored Personnel Carrier H");
        jButton_srEquipmentArmoredPersonnelCarrierH.setFocusable(false);
        jButton_srEquipmentArmoredPersonnelCarrierH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentArmoredPersonnelCarrierH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentArmoredPersonnelCarrierH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentArmoredPersonnelCarrierH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentArmoredPersonnelCarrierH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentArmoredPersonnelCarrierHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentArmoredPersonnelCarrierH);

        jButton_srEquipmentGroundVehicleH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentGroundVehicleH.setText("Ground Vehicle H");
        jButton_srEquipmentGroundVehicleH.setFocusable(false);
        jButton_srEquipmentGroundVehicleH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentGroundVehicleH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentGroundVehicleH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentGroundVehicleH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentGroundVehicleH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentGroundVehicleHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentGroundVehicleH);

        jButton_srEquipmentArmoredTankH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentArmoredTankH.setText("Armored Tank H");
        jButton_srEquipmentArmoredTankH.setFocusable(false);
        jButton_srEquipmentArmoredTankH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentArmoredTankH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentArmoredTankH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentArmoredTankH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentArmoredTankH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentArmoredTankHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentArmoredTankH);

        jButton_srEquipmentRifleH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentRifleH.setText("Rifle H");
        jButton_srEquipmentRifleH.setFocusable(false);
        jButton_srEquipmentRifleH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentRifleH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentRifleH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentRifleH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentRifleH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentRifleHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentRifleH);

        jButton_srEquipmentIEDH.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentIEDH.setText("IED H");
        jButton_srEquipmentIEDH.setFocusable(false);
        jButton_srEquipmentIEDH.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentIEDH.setMaximumSize(new java.awt.Dimension(1500, 60));
        jButton_srEquipmentIEDH.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentIEDH.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentIEDH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentIEDHActionPerformed(evt);
            }
        });
        jPanel_srEquipmentPresets.add(jButton_srEquipmentIEDH);

        jScrollPane_srEquipmentPresets.setViewportView(jPanel_srEquipmentPresets);

        jButton_srEquipmentBrowse.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_srEquipmentBrowse.setText("Browse");
        jButton_srEquipmentBrowse.setFocusable(false);
        jButton_srEquipmentBrowse.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButton_srEquipmentBrowse.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentBrowse.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentBrowse.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_srEquipmentBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentBrowseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_srEquipmentLayout = new javax.swing.GroupLayout(jPanel_srEquipment);
        jPanel_srEquipment.setLayout(jPanel_srEquipmentLayout);
        jPanel_srEquipmentLayout.setHorizontalGroup(
            jPanel_srEquipmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_srEquipmentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srEquipmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane_srEquipmentPresets, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipmentBack)
                    .addComponent(jLabel_srEquipment, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipmentBrowse, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipmentSearch, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srEquipmentLayout.setVerticalGroup(
            jPanel_srEquipmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srEquipmentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srEquipmentBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srEquipment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane_srEquipmentPresets, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srEquipmentSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_srEquipmentBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel_srEquipment, "Spot Report Equipment Card");

        jPanel_srEquipmentSearch.setOpaque(false);
        jPanel_srEquipmentSearch.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel_srEquipmentSearchComponentShown(evt);
            }
        });

        jButton_srEquipmentSearchBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srEquipmentSearchBack.setBorderPainted(false);
        jButton_srEquipmentSearchBack.setContentAreaFilled(false);
        jButton_srEquipmentSearchBack.setFocusable(false);
        jButton_srEquipmentSearchBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srEquipmentSearchBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srEquipmentSearchBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentSearchBackActionPerformed(evt);
            }
        });

        jLabel_srEquipmentSearch.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srEquipmentSearch.setText("Equipment Search");

        jTextField_srEquipmentSearchField.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jTextField_srEquipmentSearchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField_srEquipmentSearchFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_srEquipmentSearchFieldFocusLost(evt);
            }
        });
        jTextField_srEquipmentSearchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField_srEquipmentSearchFieldKeyReleased(evt);
            }
        });

        jScrollPane_srEquipmentSearchResults.setFocusable(false);
        jScrollPane_srEquipmentSearchResults.setOpaque(false);

        equipmentListJPanel_srEquipmentSearchResults.setOpaque(false);
        equipmentListJPanel_srEquipmentSearchResults.setLayout(new javax.swing.BoxLayout(equipmentListJPanel_srEquipmentSearchResults, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane_srEquipmentSearchResults.setViewportView(equipmentListJPanel_srEquipmentSearchResults);

        javax.swing.GroupLayout jPanel_srEquipmentSearchLayout = new javax.swing.GroupLayout(jPanel_srEquipmentSearch);
        jPanel_srEquipmentSearch.setLayout(jPanel_srEquipmentSearchLayout);
        jPanel_srEquipmentSearchLayout.setHorizontalGroup(
            jPanel_srEquipmentSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_srEquipmentSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srEquipmentSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane_srEquipmentSearchResults, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipmentSearchBack)
                    .addComponent(jLabel_srEquipmentSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jTextField_srEquipmentSearchField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srEquipmentSearchLayout.setVerticalGroup(
            jPanel_srEquipmentSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srEquipmentSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srEquipmentSearchBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srEquipmentSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_srEquipmentSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane_srEquipmentSearchResults, javax.swing.GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel_srEquipmentSearch, "Spot Report Equipment Search Card");

        jPanel_srEquipmentBrowseCategories.setOpaque(false);
        jPanel_srEquipmentBrowseCategories.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel_srEquipmentBrowseCategoriesComponentShown(evt);
            }
        });

        jButton_srEquipmentBrowseCategoriesBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srEquipmentBrowseCategoriesBack.setBorderPainted(false);
        jButton_srEquipmentBrowseCategoriesBack.setContentAreaFilled(false);
        jButton_srEquipmentBrowseCategoriesBack.setFocusable(false);
        jButton_srEquipmentBrowseCategoriesBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srEquipmentBrowseCategoriesBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srEquipmentBrowseCategoriesBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentBrowseCategoriesBackActionPerformed(evt);
            }
        });

        jLabel_srEquipmentBrowseCategories.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srEquipmentBrowseCategories.setText("Categories");

        jScrollPane_srEquipmentCategories.setFocusable(false);
        jScrollPane_srEquipmentCategories.setOpaque(false);

        jPanel_srEquipmentCategories.setOpaque(false);
        jPanel_srEquipmentCategories.setLayout(new javax.swing.BoxLayout(jPanel_srEquipmentCategories, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane_srEquipmentCategories.setViewportView(jPanel_srEquipmentCategories);

        javax.swing.GroupLayout jPanel_srEquipmentBrowseCategoriesLayout = new javax.swing.GroupLayout(jPanel_srEquipmentBrowseCategories);
        jPanel_srEquipmentBrowseCategories.setLayout(jPanel_srEquipmentBrowseCategoriesLayout);
        jPanel_srEquipmentBrowseCategoriesLayout.setHorizontalGroup(
            jPanel_srEquipmentBrowseCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_srEquipmentBrowseCategoriesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srEquipmentBrowseCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane_srEquipmentCategories, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipmentBrowseCategoriesBack)
                    .addComponent(jLabel_srEquipmentBrowseCategories, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srEquipmentBrowseCategoriesLayout.setVerticalGroup(
            jPanel_srEquipmentBrowseCategoriesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srEquipmentBrowseCategoriesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srEquipmentBrowseCategoriesBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srEquipmentBrowseCategories)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane_srEquipmentCategories, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel_srEquipmentBrowseCategories, "Spot Report Equipment Browse Card");

        jPanel_srEquipmentCategory.setOpaque(false);
        jPanel_srEquipmentCategory.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jPanel_srEquipmentCategoryComponentShown(evt);
            }
        });

        jButton_srEquipmentCategoryBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_srEquipmentCategoryBack.setBorderPainted(false);
        jButton_srEquipmentCategoryBack.setContentAreaFilled(false);
        jButton_srEquipmentCategoryBack.setFocusable(false);
        jButton_srEquipmentCategoryBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_srEquipmentCategoryBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_srEquipmentCategoryBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_srEquipmentCategoryBackActionPerformed(evt);
            }
        });

        jLabel_srEquipmentCategory.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_srEquipmentCategory.setText("Category");

        jScrollPane_srEquipmentCategoryResults.setFocusable(false);
        jScrollPane_srEquipmentCategoryResults.setOpaque(false);

        equipmentListJPanel_srEquipmentCategoryResults.setOpaque(false);
        equipmentListJPanel_srEquipmentCategoryResults.setLayout(new javax.swing.BoxLayout(equipmentListJPanel_srEquipmentCategoryResults, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane_srEquipmentCategoryResults.setViewportView(equipmentListJPanel_srEquipmentCategoryResults);

        javax.swing.GroupLayout jPanel_srEquipmentCategoryLayout = new javax.swing.GroupLayout(jPanel_srEquipmentCategory);
        jPanel_srEquipmentCategory.setLayout(jPanel_srEquipmentCategoryLayout);
        jPanel_srEquipmentCategoryLayout.setHorizontalGroup(
            jPanel_srEquipmentCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_srEquipmentCategoryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_srEquipmentCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane_srEquipmentCategoryResults, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_srEquipmentCategoryBack)
                    .addComponent(jLabel_srEquipmentCategory, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_srEquipmentCategoryLayout.setVerticalGroup(
            jPanel_srEquipmentCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_srEquipmentCategoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_srEquipmentCategoryBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_srEquipmentCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane_srEquipmentCategoryResults, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel_srEquipmentCategory, "Spot Report Equipment Category Card");

        jPanel_overlays.setOpaque(false);

        jButton_mapPackage.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_mapPackage.setText("Open...");
        jButton_mapPackage.setFocusable(false);
        jButton_mapPackage.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_mapPackage.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_mapPackage.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_mapPackage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_mapPackageActionPerformed(evt);
            }
        });

        jButton_overlaysBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_overlaysBack.setBorderPainted(false);
        jButton_overlaysBack.setContentAreaFilled(false);
        jButton_overlaysBack.setFocusable(false);
        jButton_overlaysBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_overlaysBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_overlaysBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_overlaysBackActionPerformed(evt);
            }
        });

        jLabel_overlays.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_overlays.setText("Overlays");

        jScrollPane_toc.setOpaque(false);

        jPanel_toc.setOpaque(false);
        jPanel_toc.setLayout(new javax.swing.BoxLayout(jPanel_toc, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane_toc.setViewportView(jPanel_toc);

        javax.swing.GroupLayout jPanel_overlaysLayout = new javax.swing.GroupLayout(jPanel_overlays);
        jPanel_overlays.setLayout(jPanel_overlaysLayout);
        jPanel_overlaysLayout.setHorizontalGroup(
            jPanel_overlaysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_overlaysLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_overlaysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane_toc, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_overlaysBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_overlays, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_mapPackage, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_overlaysLayout.setVerticalGroup(
            jPanel_overlaysLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_overlaysLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_overlaysBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_overlays)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_mapPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane_toc, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel_overlays, "Overlays Card");

        jPanel_waypoints.setOpaque(false);

        jButton_waypointsBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_waypointsBack.setBorderPainted(false);
        jButton_waypointsBack.setContentAreaFilled(false);
        jButton_waypointsBack.setFocusable(false);
        jButton_waypointsBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_waypointsBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_waypointsBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_waypointsBackActionPerformed(evt);
            }
        });

        jLabel_waypoints.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_waypoints.setText("Waypoints");

        jScrollPane_waypointsList.setOpaque(false);

        jPanel_waypointsList.setOpaque(false);
        jPanel_waypointsList.setLayout(new javax.swing.BoxLayout(jPanel_waypointsList, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane_waypointsList.setViewportView(jPanel_waypointsList);

        javax.swing.GroupLayout jPanel_waypointsLayout = new javax.swing.GroupLayout(jPanel_waypoints);
        jPanel_waypoints.setLayout(jPanel_waypointsLayout);
        jPanel_waypointsLayout.setHorizontalGroup(
            jPanel_waypointsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_waypointsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_waypointsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane_waypointsList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_waypointsBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_waypoints, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_waypointsLayout.setVerticalGroup(
            jPanel_waypointsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_waypointsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_waypointsBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_waypoints)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane_waypointsList, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel_waypoints, "Waypoints Card");

        jPanel_navigation.setOpaque(false);

        jButton_navigationBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_navigationBack.setBorderPainted(false);
        jButton_navigationBack.setContentAreaFilled(false);
        jButton_navigationBack.setFocusable(false);
        jButton_navigationBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_navigationBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_navigationBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_navigationBackActionPerformed(evt);
            }
        });

        jLabel_navigation.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_navigation.setText("Navigation");

        jLabel_navMgrs.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_navMgrs.setText("Go to MGRS");

        jTextField_navMgrs.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jTextField_navMgrs.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField_navMgrsFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_navMgrsFocusLost(evt);
            }
        });
        jTextField_navMgrs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField_navMgrsKeyReleased(evt);
            }
        });

        jButton_navGoToMgrs.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_navGoToMgrs.setText("Go");
        jButton_navGoToMgrs.setFocusable(false);
        jButton_navGoToMgrs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_navGoToMgrsActionPerformed(evt);
            }
        });

        jLabel_mgrsMessage.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_mgrsMessage.setForeground(new java.awt.Color(204, 0, 0));
        jLabel_mgrsMessage.setText(" ");

        javax.swing.GroupLayout jPanel_navigationLayout = new javax.swing.GroupLayout(jPanel_navigation);
        jPanel_navigation.setLayout(jPanel_navigationLayout);
        jPanel_navigationLayout.setHorizontalGroup(
            jPanel_navigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_navigationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_navigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_mgrsMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel_navigation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel_navMgrs, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_navigationLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton_navigationBack))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel_navigationLayout.createSequentialGroup()
                        .addComponent(jTextField_navMgrs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_navGoToMgrs)))
                .addContainerGap())
        );
        jPanel_navigationLayout.setVerticalGroup(
            jPanel_navigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_navigationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_navigationBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_navigation)
                .addGap(18, 18, 18)
                .addComponent(jLabel_navMgrs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel_navigationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jTextField_navMgrs)
                    .addComponent(jButton_navGoToMgrs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_mgrsMessage)
                .addContainerGap(500, Short.MAX_VALUE))
        );

        add(jPanel_navigation, "Navigation Card");

        jPanel_options.setOpaque(false);

        jButton_optionsBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_optionsBack.setBorderPainted(false);
        jButton_optionsBack.setContentAreaFilled(false);
        jButton_optionsBack.setFocusable(false);
        jButton_optionsBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_optionsBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_optionsBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_optionsBackActionPerformed(evt);
            }
        });

        jLabel_options.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_options.setText("Options");

        jButton_aboutMe.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_aboutMe.setText("Settings");
        jButton_aboutMe.setFocusable(false);
        jButton_aboutMe.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_aboutMe.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_aboutMe.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_aboutMe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_aboutMeActionPerformed(evt);
            }
        });

        jToggleButton_showMe.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jToggleButton_showMe.setSelected(true);
        jToggleButton_showMe.setText("Show Me");
        jToggleButton_showMe.setFocusable(false);
        jToggleButton_showMe.setMaximumSize(new java.awt.Dimension(150, 60));
        jToggleButton_showMe.setMinimumSize(new java.awt.Dimension(150, 60));
        jToggleButton_showMe.setPreferredSize(new java.awt.Dimension(150, 60));
        jToggleButton_showMe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_showMeActionPerformed(evt);
            }
        });

        jToggleButton_sendMyLocation.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jToggleButton_sendMyLocation.setSelected(true);
        jToggleButton_sendMyLocation.setText("Send My Location");
        jToggleButton_sendMyLocation.setFocusable(false);
        jToggleButton_sendMyLocation.setMaximumSize(new java.awt.Dimension(150, 60));
        jToggleButton_sendMyLocation.setMinimumSize(new java.awt.Dimension(150, 60));
        jToggleButton_sendMyLocation.setPreferredSize(new java.awt.Dimension(150, 60));
        jToggleButton_sendMyLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton_sendMyLocationActionPerformed(evt);
            }
        });

        jButton_resetMap.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_resetMap.setText("Reset Map");
        jButton_resetMap.setFocusable(false);
        jButton_resetMap.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_resetMap.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_resetMap.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_resetMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_resetMapActionPerformed(evt);
            }
        });

        jButton_gpsOptions.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_gpsOptions.setText("GPS Options");
        jButton_gpsOptions.setFocusable(false);
        jButton_gpsOptions.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_gpsOptions.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_gpsOptions.setPreferredSize(new java.awt.Dimension(150, 60));
        jButton_gpsOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_gpsOptionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_optionsLayout = new javax.swing.GroupLayout(jPanel_options);
        jPanel_options.setLayout(jPanel_optionsLayout);
        jPanel_optionsLayout.setHorizontalGroup(
            jPanel_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_optionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_optionsBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_options, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_aboutMe, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jToggleButton_showMe, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jToggleButton_sendMyLocation, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_resetMap, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_gpsOptions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_optionsLayout.setVerticalGroup(
            jPanel_optionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_optionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_optionsBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_options)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_aboutMe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton_showMe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton_sendMyLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_resetMap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton_gpsOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(275, Short.MAX_VALUE))
        );

        add(jPanel_options, "Options Card");

        jPanel_gpsOptions.setOpaque(false);

        jButton_gpsOptionsBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Normal.png"))); // NOI18N
        jButton_gpsOptionsBack.setBorderPainted(false);
        jButton_gpsOptionsBack.setContentAreaFilled(false);
        jButton_gpsOptionsBack.setFocusable(false);
        jButton_gpsOptionsBack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_gpsOptionsBack.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Back-Pressed.png"))); // NOI18N
        jButton_gpsOptionsBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_gpsOptionsBackActionPerformed(evt);
            }
        });

        jLabel_gpsOptions.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_gpsOptions.setText("GPS Options");

        jButton_chooseGPXFile.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_chooseGPXFile.setText("Choose GPX File");
        jButton_chooseGPXFile.setFocusable(false);
        jButton_chooseGPXFile.setMaximumSize(new java.awt.Dimension(150, 60));
        jButton_chooseGPXFile.setMinimumSize(new java.awt.Dimension(150, 60));
        jButton_chooseGPXFile.setPreferredSize(new java.awt.Dimension(150, 60));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jRadioButton_simulatedGPS, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jButton_chooseGPXFile, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jButton_chooseGPXFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_chooseGPXFileActionPerformed(evt);
            }
        });

        buttonGroup_gpsMode.add(jRadioButton_onboardGPS);
        jRadioButton_onboardGPS.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jRadioButton_onboardGPS.setSelected(LocationMode.LOCATION_SERVICE.equals(appConfigController.getLocationMode()));
        jRadioButton_onboardGPS.setText("Onboard GPS");
        jRadioButton_onboardGPS.setFocusable(false);
        jRadioButton_onboardGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_onboardGPSActionPerformed(evt);
            }
        });

        buttonGroup_gpsMode.add(jRadioButton_simulatedGPS);
        jRadioButton_simulatedGPS.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jRadioButton_simulatedGPS.setSelected(LocationMode.SIMULATOR.equals(appConfigController.getLocationMode()));
        jRadioButton_simulatedGPS.setText("Simulated GPS");
        jRadioButton_simulatedGPS.setFocusable(false);
        jRadioButton_simulatedGPS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton_simulatedGPSActionPerformed(evt);
            }
        });

        jLabel_gpsStatus.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_gpsStatus.setText("Not Connected");

        javax.swing.GroupLayout jPanel_gpsOptionsLayout = new javax.swing.GroupLayout(jPanel_gpsOptions);
        jPanel_gpsOptions.setLayout(jPanel_gpsOptionsLayout);
        jPanel_gpsOptionsLayout.setHorizontalGroup(
            jPanel_gpsOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_gpsOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel_gpsOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_gpsOptionsBack, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel_gpsOptions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jRadioButton_onboardGPS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jButton_chooseGPXFile, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jRadioButton_simulatedGPS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                    .addComponent(jLabel_gpsStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel_gpsOptionsLayout.setVerticalGroup(
            jPanel_gpsOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_gpsOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_gpsOptionsBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_gpsOptions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_onboardGPS, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton_simulatedGPS, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_chooseGPXFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel_gpsStatus)
                .addContainerGap(375, Short.MAX_VALUE))
        );

        add(jPanel_gpsOptions, "GPS Options Card");

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_aboutMeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_aboutMeActionPerformed
        if (null == appConfigDialog) {
            appConfigDialog = new AppConfigDialog(app, true, appConfigController);
            appConfigDialog.setLocationRelativeTo(app);
        }
        appConfigDialog.setVisible(true);
}//GEN-LAST:event_jButton_aboutMeActionPerformed

    private void jButton_optionsBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_optionsBackActionPerformed
        resetMenu();
}//GEN-LAST:event_jButton_optionsBackActionPerformed

    private void jButton_overlaysBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_overlaysBackActionPerformed
        resetMenu();
}//GEN-LAST:event_jButton_overlaysBackActionPerformed

    private void jButton_mapPackageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_mapPackageActionPerformed
        if (null == mpkFileChooser) {
            mpkFileChooser = new JFileChooser(appConfigController.getMPKFileChooserCurrentDirectory());
            FileNameExtensionFilter filter = new FileNameExtensionFilter("ArcGIS Map Packages", "mpk");
            mpkFileChooser.setFileFilter(filter);
        }
        int result = mpkFileChooser.showOpenDialog(app);

        //Save current directory as a preference
        appConfigController.setMPKFileChooserCurrentDirectory(mpkFileChooser.getCurrentDirectory().getAbsolutePath());

        if (JFileChooser.APPROVE_OPTION == result) {
            mapController.openMapPackage(mpkFileChooser.getSelectedFile());
        }
}//GEN-LAST:event_jButton_mapPackageActionPerformed

    private void jButton_srEquipmentIEDHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentIEDHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.IED);
}//GEN-LAST:event_jButton_srEquipmentIEDHActionPerformed

    private void jButton_srEquipmentRifleHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentRifleHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.RIFLE);
}//GEN-LAST:event_jButton_srEquipmentRifleHActionPerformed

    private void jButton_srEquipmentArmoredTankHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentArmoredTankHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.ARMORED_TANK);
}//GEN-LAST:event_jButton_srEquipmentArmoredTankHActionPerformed

    private void jButton_srEquipmentGroundVehicleHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentGroundVehicleHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.GROUND_VEHICLE);
}//GEN-LAST:event_jButton_srEquipmentGroundVehicleHActionPerformed

    private void jButton_srEquipmentArmoredPersonnelCarrierHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentArmoredPersonnelCarrierHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.ARMORED_PERSONNEL_CARRIER);
}//GEN-LAST:event_jButton_srEquipmentArmoredPersonnelCarrierHActionPerformed

    private void jButton_srEquipmentHowitzerHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentHowitzerHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.HOWITZER);
}//GEN-LAST:event_jButton_srEquipmentHowitzerHActionPerformed

    private void jButton_srEquipmentGrenadeLauncherHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentGrenadeLauncherHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.GRENADE_LAUNCHER);
}//GEN-LAST:event_jButton_srEquipmentGrenadeLauncherHActionPerformed

    private void jButton_srEquipmentMissileLauncherHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentMissileLauncherHActionPerformed
        setSpotReportEquipment(SpotReport.Equipment.MISSILE_LAUNCHER);
}//GEN-LAST:event_jButton_srEquipmentMissileLauncherHActionPerformed

    private void jButton_srEquipmentBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentBackActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_srEquipmentBackActionPerformed

    private void jButton_srTimeNowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srTimeNowActionPerformed
        setSpotReportTime(Calendar.getInstance());
}//GEN-LAST:event_jButton_srTimeNowActionPerformed

    private void jButton_srTimeBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srTimeBackActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_srTimeBackActionPerformed

    private void jButton_srUnitFacilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitFacilityActionPerformed
        setSpotReportUnit(SpotReport.Unit.FACILITY);
}//GEN-LAST:event_jButton_srUnitFacilityActionPerformed

    private void jButton_srUnitCivilianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitCivilianActionPerformed
        setSpotReportUnit(SpotReport.Unit.CIVILIAN);
}//GEN-LAST:event_jButton_srUnitCivilianActionPerformed

    private void jButton_srUnitNGOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitNGOActionPerformed
        setSpotReportUnit(SpotReport.Unit.NGO);
}//GEN-LAST:event_jButton_srUnitNGOActionPerformed

    private void jButton_srUnitHostNationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitHostNationActionPerformed
        setSpotReportUnit(SpotReport.Unit.HOST_NATION);
}//GEN-LAST:event_jButton_srUnitHostNationActionPerformed

    private void jButton_srUnitCoalitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitCoalitionActionPerformed
        setSpotReportUnit(SpotReport.Unit.COALITION);
}//GEN-LAST:event_jButton_srUnitCoalitionActionPerformed

    private void jButton_srUnitIrregularActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitIrregularActionPerformed
        setSpotReportUnit(SpotReport.Unit.IRREGULAR);
}//GEN-LAST:event_jButton_srUnitIrregularActionPerformed

    private void jButton_srUnitConventionalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitConventionalActionPerformed
        setSpotReportUnit(SpotReport.Unit.CONVENTIONAL);
}//GEN-LAST:event_jButton_srUnitConventionalActionPerformed

    private void jButton_srUnitBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitBackActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_srUnitBackActionPerformed

    private void jToggleButton_srLocationFromMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_srLocationFromMapActionPerformed
        if (jToggleButton_srLocationFromMap.isSelected()) {
            mapController.trackAsync(new MapOverlayAdapter() {

                @Override
                public void mouseClicked(MouseEvent event) {
                    mapController.cancelTrackAsync();
                    jToggleButton_srLocationFromMap.setSelected(false);
                    Point pt = mapController.toMapPointObject(event.getX(), event.getY());
                    spotReport.setLocationX(pt.getX());
                    spotReport.setLocationY(pt.getY());
                    spotReport.setLocationWkid(mapController.getSpatialReference().getID());
                    jButton_srLocation.setText("Location: " + mapController.pointToMgrs(pt, mapController.getSpatialReference()));
                    showCard("Spot Report Unit Card");
                }

            }, MapController.EVENT_MOUSE_CLICKED);
        } else {
            mapController.cancelTrackAsync();
        }
}//GEN-LAST:event_jToggleButton_srLocationFromMapActionPerformed

    private void jButton_srLocationBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srLocationBackActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_srLocationBackActionPerformed

    private void jButton_srActivityPersonnelRecoveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityPersonnelRecoveryActionPerformed
        setSpotReportActivity(Activity.PERSONNEL_RECOVERY);
}//GEN-LAST:event_jButton_srActivityPersonnelRecoveryActionPerformed

    private void jButton_srActivityCivilianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityCivilianActionPerformed
        setSpotReportActivity(Activity.CIVILIAN);
}//GEN-LAST:event_jButton_srActivityCivilianActionPerformed

    private void jButton_srActivityCacheActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityCacheActionPerformed
        setSpotReportActivity(Activity.CACHE);
}//GEN-LAST:event_jButton_srActivityCacheActionPerformed

    private void jButton_srActivityStationaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityStationaryActionPerformed
        setSpotReportActivity(Activity.STATIONARY);
}//GEN-LAST:event_jButton_srActivityStationaryActionPerformed

    private void jButton_srActivityMovingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityMovingActionPerformed
        setSpotReportActivity(Activity.MOVING);
}//GEN-LAST:event_jButton_srActivityMovingActionPerformed

    private void jButton_srActivityDefendingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityDefendingActionPerformed
        setSpotReportActivity(Activity.DEFENDING);
}//GEN-LAST:event_jButton_srActivityDefendingActionPerformed

    private void jButton_srActivityAttackingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityAttackingActionPerformed
        setSpotReportActivity(Activity.ATTACKING);
}//GEN-LAST:event_jButton_srActivityAttackingActionPerformed

    private void jButton_srActivityBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityBackActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_srActivityBackActionPerformed

    private void jButton_srSizeDivisionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeDivisionActionPerformed
        setSpotReportSize(Size.DIVISION);
}//GEN-LAST:event_jButton_srSizeDivisionActionPerformed

    private void jButton_srSizeBrigadeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeBrigadeActionPerformed
        setSpotReportSize(Size.BRIGADE);
}//GEN-LAST:event_jButton_srSizeBrigadeActionPerformed

    private void jButton_srSizeRegimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeRegimentActionPerformed
        setSpotReportSize(Size.REGIMENT);
}//GEN-LAST:event_jButton_srSizeRegimentActionPerformed

    private void jButton_srSizeBattalionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeBattalionActionPerformed
        setSpotReportSize(Size.BATTALION);
}//GEN-LAST:event_jButton_srSizeBattalionActionPerformed

    private void jButton_srSizeCompanyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeCompanyActionPerformed
        setSpotReportSize(Size.COMPANY);
}//GEN-LAST:event_jButton_srSizeCompanyActionPerformed

    private void jButton_srSizePlatoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizePlatoonActionPerformed
        setSpotReportSize(Size.PLATOON);
}//GEN-LAST:event_jButton_srSizePlatoonActionPerformed

    private void jButton_srSizeSectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeSectionActionPerformed
        setSpotReportSize(Size.SECTION);
}//GEN-LAST:event_jButton_srSizeSectionActionPerformed

    private void jButton_srSizeSquadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeSquadActionPerformed
        setSpotReportSize(Size.SQUAD);
}//GEN-LAST:event_jButton_srSizeSquadActionPerformed

    private void jButton_srSizeTeamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeTeamActionPerformed
        setSpotReportSize(Size.TEAM);
}//GEN-LAST:event_jButton_srSizeTeamActionPerformed

    private void jButton_srSizeBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeBackActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_srSizeBackActionPerformed

    private void jButton_srSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSendActionPerformed
        try {
            spotReportController.sendSpotReport(spotReport, appConfigController.getUsername());
        } catch (IOException ex) {
            Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        showCard("Reports Card");
}//GEN-LAST:event_jButton_srSendActionPerformed

    private void jButton_srEquipmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentActionPerformed
        showCard("Spot Report Equipment Card");
}//GEN-LAST:event_jButton_srEquipmentActionPerformed

    private void jButton_srTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srTimeActionPerformed
        showCard("Spot Report Time Card");
}//GEN-LAST:event_jButton_srTimeActionPerformed

    private void jButton_srUnitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srUnitActionPerformed
        showCard("Spot Report Unit Card");
}//GEN-LAST:event_jButton_srUnitActionPerformed

    private void jButton_srLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srLocationActionPerformed
        showCard("Spot Report Location Card");
}//GEN-LAST:event_jButton_srLocationActionPerformed

    private void jButton_srActivityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srActivityActionPerformed
        showCard("Spot Report Activity Card");
}//GEN-LAST:event_jButton_srActivityActionPerformed

    private void jButton_srSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeActionPerformed
        showCard("Spot Report Size Card");
}//GEN-LAST:event_jButton_srSizeActionPerformed

    private void jButton_spotReportBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_spotReportBackActionPerformed
        showCard("Reports Card");
}//GEN-LAST:event_jButton_spotReportBackActionPerformed

    private void jButton_reportsBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_reportsBackActionPerformed
        resetMenu();
}//GEN-LAST:event_jButton_reportsBackActionPerformed

    private void jButton_spotReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_spotReportActionPerformed
        showCard("Spot Report Card");
}//GEN-LAST:event_jButton_spotReportActionPerformed

    private void jButton_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_closeActionPerformed
        setVisible(false);
        resetMenu();
}//GEN-LAST:event_jButton_closeActionPerformed

    private void jButton_quitApplicationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_quitApplicationActionPerformed
        Utilities.closeApplication(app);
}//GEN-LAST:event_jButton_quitApplicationActionPerformed

    private void jButton_optionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_optionsActionPerformed
        showCard("Options Card");
}//GEN-LAST:event_jButton_optionsActionPerformed

    private void jButton_overlaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_overlaysActionPerformed
        showCard("Overlays Card");
}//GEN-LAST:event_jButton_overlaysActionPerformed

    private void jButton_reportsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_reportsActionPerformed
        showCard("Reports Card");
}//GEN-LAST:event_jButton_reportsActionPerformed

    private void jButton_srEquipmentSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentSearchActionPerformed
        showCard("Spot Report Equipment Search Card");
    }//GEN-LAST:event_jButton_srEquipmentSearchActionPerformed

    private void jButton_srEquipmentSearchBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentSearchBackActionPerformed
        showCard("Spot Report Equipment Card");
    }//GEN-LAST:event_jButton_srEquipmentSearchBackActionPerformed

    private void jPanel_srEquipmentSearchComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel_srEquipmentSearchComponentShown
        jTextField_srEquipmentSearchField.requestFocusInWindow();
    }//GEN-LAST:event_jPanel_srEquipmentSearchComponentShown

    /**
     * Use keyReleased instead of keyTyped because keyTyped seems to fire before
     * the JTextField text has actually changed.
     * @param evt
     */
    private void jTextField_srEquipmentSearchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_srEquipmentSearchFieldKeyReleased
        if (KeyEvent.VK_ESCAPE == evt.getKeyCode()) {
            Utilities.closeApplication(app);
        } else {
            equipmentListJPanel_srEquipmentSearchResults.removeAll();
            if (2 <= jTextField_srEquipmentSearchField.getText().length()) {
                try {
                    List<SymbolProperties> symbols = mil2525CSymbolController.findSymbols(jTextField_srEquipmentSearchField.getText());
                    List<String> symbolNames = new ArrayList<String>(symbols.size());
                    for (SymbolProperties symbol : symbols) {
                        symbolNames.add(symbol.getName());
                    }
                    ((EquipmentListJPanel) equipmentListJPanel_srEquipmentSearchResults).setEquipmentNames(symbolNames);
                } catch (IOException ex) {
                    Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jTextField_srEquipmentSearchFieldKeyReleased

    private void jButton_srEquipmentBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentBrowseActionPerformed
        showCard("Spot Report Equipment Browse Card");
    }//GEN-LAST:event_jButton_srEquipmentBrowseActionPerformed

    private void jButton_srEquipmentBrowseCategoriesBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentBrowseCategoriesBackActionPerformed
        showCard("Spot Report Equipment Card");
    }//GEN-LAST:event_jButton_srEquipmentBrowseCategoriesBackActionPerformed

    private void jPanel_srEquipmentBrowseCategoriesComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel_srEquipmentBrowseCategoriesComponentShown
        if (!initializedEquipmentBrowse) {
            List<String> categories = mil2525CSymbolController.getCategories();
            for (final String category : categories) {
                final JButton button = new JButton(category);
                button.setFont(BUTTON_FONT);
                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setFocusable(false);
                button.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        selectedCategory = category;
                        equipmentListJPanel_srEquipmentCategoryResults.removeAll();
                        jLabel_srEquipmentCategory.setText(selectedCategory);
                        showCard("Spot Report Equipment Category Card");
                    }
                });
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                button.setMinimumSize(new Dimension(0, 60));
                jPanel_srEquipmentCategories.add(button);
            }

            initializedEquipmentBrowse = true;
        }
    }//GEN-LAST:event_jPanel_srEquipmentBrowseCategoriesComponentShown

    private void jButton_srEquipmentCategoryBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srEquipmentCategoryBackActionPerformed
        showCard("Spot Report Equipment Browse Card");
    }//GEN-LAST:event_jButton_srEquipmentCategoryBackActionPerformed

    private void jPanel_srEquipmentCategoryComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel_srEquipmentCategoryComponentShown
        try {
            List<SymbolProperties> symbols = mil2525CSymbolController.getSymbolsInCategory(selectedCategory);
            List<String> names = new ArrayList<String>(symbols.size());
            for (SymbolProperties symbol : symbols) {
                names.add(symbol.getName());
            }
            ((EquipmentListJPanel) equipmentListJPanel_srEquipmentCategoryResults).setEquipmentNames(names);
        } catch (IOException ex) {
            Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jPanel_srEquipmentCategoryComponentShown

    private void jButton_gpsOptionsBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_gpsOptionsBackActionPerformed
        showCard("Options Card");
    }//GEN-LAST:event_jButton_gpsOptionsBackActionPerformed

    private void jButton_chooseGPXFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_chooseGPXFileActionPerformed
        if (null == gpxFileChooser) {
            gpxFileChooser = new JFileChooser(appConfigController.getGPXFileChooserCurrentDirectory());
            FileNameExtensionFilter filter = new FileNameExtensionFilter("GPX Files", "gpx");
            gpxFileChooser.setFileFilter(filter);
        }
        int result = gpxFileChooser.showOpenDialog(app);

        //Save current directory as a preference
        appConfigController.setGPXFileChooserCurrentDirectory(gpxFileChooser.getCurrentDirectory().getAbsolutePath());

        if (JFileChooser.APPROVE_OPTION == result) {
            try {
                appConfigController.setGpx(gpxFileChooser.getSelectedFile().getAbsolutePath());
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton_chooseGPXFileActionPerformed

    private void jRadioButton_onboardGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_onboardGPSActionPerformed
        changeLocationMode(LocationMode.LOCATION_SERVICE);
    }//GEN-LAST:event_jRadioButton_onboardGPSActionPerformed

    private void jRadioButton_simulatedGPSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton_simulatedGPSActionPerformed
        changeLocationMode(LocationMode.SIMULATOR);
    }//GEN-LAST:event_jRadioButton_simulatedGPSActionPerformed

    private void jButton_navigationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_navigationActionPerformed
        showCard("Navigation Card");
    }//GEN-LAST:event_jButton_navigationActionPerformed

    private void jButton_gpsOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_gpsOptionsActionPerformed
        showCard("GPS Options Card");
    }//GEN-LAST:event_jButton_gpsOptionsActionPerformed

    private void jButton_resetMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_resetMapActionPerformed
        if (app instanceof VehicleCommanderJFrame) {
            ((VehicleCommanderJFrame) app).resetMapConfig();
        }
    }//GEN-LAST:event_jButton_resetMapActionPerformed

    private void jToggleButton_showMeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_showMeActionPerformed
        LocationController locationController = mapController.getLocationController();
        if (locationController instanceof com.esri.vehiclecommander.controller.LocationController) {
            ((com.esri.vehiclecommander.controller.LocationController) locationController).showGPSLayer(jToggleButton_showMe.isSelected());
        }
    }//GEN-LAST:event_jToggleButton_showMeActionPerformed

    private void jToggleButton_sendMyLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_sendMyLocationActionPerformed
        positionReportController.setEnabled(jToggleButton_sendMyLocation.isSelected());
    }//GEN-LAST:event_jToggleButton_sendMyLocationActionPerformed

    private void jButton_navigationBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_navigationBackActionPerformed
        resetMenu();
    }//GEN-LAST:event_jButton_navigationBackActionPerformed

    private void jTextField_navMgrsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_navMgrsKeyReleased
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ESCAPE: {
                Utilities.closeApplication(app);
                break;
            }
            case KeyEvent.VK_ENTER: {
                jButton_navGoToMgrsActionPerformed(null);
                break;
            }
            default: {
                //Clear the error message
                jLabel_mgrsMessage.setText(" ");
            }
        }
    }//GEN-LAST:event_jTextField_navMgrsKeyReleased

    private void jButton_navGoToMgrsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_navGoToMgrsActionPerformed
        Point pt = mapController.panTo(jTextField_navMgrs.getText());
        if (null != pt) {
            mgrsLayerController.showPoint(pt, mapController.getSpatialReference());
        } else {
            jLabel_mgrsMessage.setText("<html>Invalid MGRS string</html>");
        }
    }//GEN-LAST:event_jButton_navGoToMgrsActionPerformed

    private void jTextField_navMgrsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_navMgrsFocusGained
        mapController.setKeyboardEnabled(false);
    }//GEN-LAST:event_jTextField_navMgrsFocusGained

    private void jTextField_navMgrsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_navMgrsFocusLost
        mapController.setKeyboardEnabled(true);
    }//GEN-LAST:event_jTextField_navMgrsFocusLost

    private void jTextField_srEquipmentSearchFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_srEquipmentSearchFieldFocusGained
        mapController.setKeyboardEnabled(false);
    }//GEN-LAST:event_jTextField_srEquipmentSearchFieldFocusGained

    private void jTextField_srEquipmentSearchFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_srEquipmentSearchFieldFocusLost
        mapController.setKeyboardEnabled(true);
    }//GEN-LAST:event_jTextField_srEquipmentSearchFieldFocusLost

    private void jButton_waypointsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_waypointsActionPerformed
        showCard("Waypoints Card");
    }//GEN-LAST:event_jButton_waypointsActionPerformed

    private void jButton_waypointsBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_waypointsBackActionPerformed
        showCard("Main Card");
    }//GEN-LAST:event_jButton_waypointsBackActionPerformed

    private void jButton_srSizeCorpsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeCorpsActionPerformed
        setSpotReportSize(Size.CORPS);
    }//GEN-LAST:event_jButton_srSizeCorpsActionPerformed

    private void jButton_srSizeArmyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeArmyActionPerformed
        setSpotReportSize(Size.ARMY);
    }//GEN-LAST:event_jButton_srSizeArmyActionPerformed

    private void jButton_srSizeArmyGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeArmyGroupActionPerformed
        setSpotReportSize(Size.ARMY_GROUP);
    }//GEN-LAST:event_jButton_srSizeArmyGroupActionPerformed

    private void jButton_srSizeRegionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeRegionActionPerformed
        setSpotReportSize(Size.REGION);
    }//GEN-LAST:event_jButton_srSizeRegionActionPerformed

    private void jButton_srSizeCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_srSizeCommandActionPerformed
        setSpotReportSize(Size.COMMAND);
    }//GEN-LAST:event_jButton_srSizeCommandActionPerformed

    private void jPanel_spotReportComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel_spotReportComponentShown
        JButton[] buttons = new JButton[] {
            jButton_srEquipmentArmoredPersonnelCarrierH,
            jButton_srEquipmentArmoredTankH,
            jButton_srEquipmentGrenadeLauncherH,
            jButton_srEquipmentGroundVehicleH,
            jButton_srEquipmentHowitzerH,
            jButton_srEquipmentIEDH,
            jButton_srEquipmentMissileLauncherH,
            jButton_srEquipmentRifleH
        };
        initEquipmentButtons(buttons);
    }//GEN-LAST:event_jPanel_spotReportComponentShown

    private void changeLocationMode(final LocationMode newMode) {
        jLabel_gpsStatus.setText("Starting...");
        new Thread() {

            @Override
            public void run() {
                try {
                    appConfigController.setLocationMode(newMode);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MainMenuJPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }.start();
    }

    private void setSpotReportSize(Size size) {
        spotReport.setSize(size);
        jButton_srSize.setText("Size: " + spotReport.getSize());
        showCard("Spot Report Activity Card");
    }

    private void setSpotReportActivity(Activity activity) {
        spotReport.setActivity(activity);
        jButton_srActivity.setText("Activity: " + spotReport.getActivity());
        showCard("Spot Report Location Card");
    }

    private void setSpotReportUnit(SpotReport.Unit unit) {
        spotReport.setUnit(unit);
        jButton_srUnit.setText("Unit: " + spotReport.getUnit());
        showCard("Spot Report Time Card");
    }

    private void setSpotReportTime(Calendar time) {
        spotReport.setTime(time);
        jButton_srTime.setText("Time: " + spotReport.getTimeString());
        showCard("Spot Report Equipment Card");
    }

    private void setSpotReportEquipment(SpotReport.Equipment equipment) {
        spotReport.setEquipment(equipment);
        jButton_srEquipment.setText("Equipment: " + spotReport.getEquipment());
        showCard("Spot Report Card");
    }

    private synchronized void refreshTOC() {
        //Redo TOC checkboxes based on current layers
        jPanel_toc.removeAll();
        List<Layer> overlayLayers = mapController.getOverlayLayers();
        for (int i = overlayLayers.size() - 1; i >= 0; i--) {
            final Layer layer = overlayLayers.get(i);
            final JToggleButton toggleButton = new JToggleButton("Loading...");
            if (null != layer.getName()) {
                toggleButton.setText(layer.getName());
            } else {
                layer.addLayerInitializeCompleteListener(new LayerInitializeCompleteListener() {

                    public void layerInitializeComplete(LayerInitializeCompleteEvent e) {
                        toggleButton.setText(layer.getName());
                    }
                });
            }
            Font font = new Font("Arial", Font.PLAIN, 18);
            toggleButton.setFont(font);
            toggleButton.setFocusable(false);
            toggleButton.setSelected(layer.isVisible());
            toggleButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    layer.setVisible(toggleButton.isSelected());
                }
            });
            toggleButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            toggleButton.setMinimumSize(new Dimension(0, 60));
            jPanel_toc.add(toggleButton);
        }
    }

    private void showCard(String cardName) {
        //Special handling for certain cards
        if ("Overlays Card".equals(cardName)) {
            refreshTOC();
        }

        CardLayout layout = (CardLayout) (getLayout());
        layout.show(this, cardName);
    }

    /**
     * Causes this MainMenuJPanel to show the main menu buttons. This method doesn't
     * make this panel visible if it is not already visible.
     */
    public void resetMenu() {
        showCard("Main Card");
    }
    
    public void onLocationChanged(Location location) {
        
    }

    public void onStateChanged(final LocationProvider.LocationProviderState state) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                jLabel_gpsStatus.setText(state.toString());
            }

        });
    }

    /**
     * Called when a new NMEA sentence is received.
     * @param newSentence the new NMEA sentence.
     */
    public void onNMEASentenceReceived(String newSentence) {
        //Do nothing
    }

    /**
     * Called when the GPS satellites in view change.
     * @param sattellitesInView the satellites now in view.
     */
    public void onSatellitesInViewChanged(Map<Integer, Satellite> sattellitesInView) {
        //Do nothing
    }    
    
    /**
     * Called when a waypoint is added. This implementation adds a waypoint button.
     * @param graphic the waypoint graphic, whose ID may or may not be populated.
     * @param graphicUid the waypoint graphic's ID.
     * @see RouteListener#waypointAdded(com.esri.core.map.Graphic, int)
     */
    public void waypointAdded(Graphic graphic, int graphicUid) {
        final JToggleButton button = new JToggleButton((String) graphic.getAttributeValue("name"));
        waypointButtonToGraphicId.put(button, graphicUid);
        graphicIdToWaypointButton.put(graphicUid, button);
        Font font = new Font("Arial", Font.PLAIN, 18);
        button.setFont(font);
        button.setFocusable(false);
        button.setSelected(false);
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (button == selectedWaypointButton) {
                    //Unselect
                    buttonGroup_waypoints.remove(button);
                    button.setSelected(false);
                    buttonGroup_waypoints.add(button);
                    selectedWaypointButton = null;

                    routeController.setSelectedWaypoint(null);
                } else {
                    selectedWaypointButton = button;

                    routeController.setSelectedWaypoint(waypointButtonToGraphicId.get(button));
                }
            }
        });
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        button.setMinimumSize(new Dimension(0, 60));
        jPanel_waypointsList.add(button);
        buttonGroup_waypoints.add(button);
    }

    /**
     * Called when a waypoint is removed. This implementation removes the corresponding
     * waypoint button.
     * @param graphicUid the removed waypoint graphic's ID.
     * @see RouteListener#waypointRemoved(int)
     */
    public void waypointRemoved(int graphicUid) {
        JToggleButton button = graphicIdToWaypointButton.get(graphicUid);
        jPanel_waypointsList.remove(button);
        buttonGroup_waypoints.remove(button);
        jPanel_waypointsList.repaint();
        graphicIdToWaypointButton.remove(graphicUid);
        waypointButtonToGraphicId.remove(button);
    }

    /**
     * Called when a waypoint is selected. This implementation stores 
     * @param graphic 
     */
    public void waypointSelected(Graphic graphic) {
        LocationController locationController = mapController.getLocationController();
        if (locationController instanceof com.esri.vehiclecommander.controller.LocationController) {
            ((com.esri.vehiclecommander.controller.LocationController) locationController).setSelectedWaypoint(graphic);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_gpsMode;
    private javax.swing.ButtonGroup buttonGroup_waypoints;
    private javax.swing.JPanel equipmentListJPanel_srEquipmentCategoryResults;
    private javax.swing.JPanel equipmentListJPanel_srEquipmentSearchResults;
    private javax.swing.JButton jButton_aboutMe;
    private javax.swing.JButton jButton_buddies;
    private javax.swing.JButton jButton_chooseGPXFile;
    private javax.swing.JButton jButton_close;
    private javax.swing.JButton jButton_gpsOptions;
    private javax.swing.JButton jButton_gpsOptionsBack;
    private javax.swing.JButton jButton_mapPackage;
    private javax.swing.JButton jButton_navGoToMgrs;
    private javax.swing.JButton jButton_navigation;
    private javax.swing.JButton jButton_navigationBack;
    private javax.swing.JButton jButton_observations;
    private javax.swing.JButton jButton_options;
    private javax.swing.JButton jButton_optionsBack;
    private javax.swing.JButton jButton_overlays;
    private javax.swing.JButton jButton_overlaysBack;
    private javax.swing.JButton jButton_quitApplication;
    private javax.swing.JButton jButton_reports;
    private javax.swing.JButton jButton_reportsBack;
    private javax.swing.JButton jButton_resetMap;
    private javax.swing.JButton jButton_spotReport;
    private javax.swing.JButton jButton_spotReportBack;
    private javax.swing.JButton jButton_srActivity;
    private javax.swing.JButton jButton_srActivityAttacking;
    private javax.swing.JButton jButton_srActivityBack;
    private javax.swing.JButton jButton_srActivityCache;
    private javax.swing.JButton jButton_srActivityCivilian;
    private javax.swing.JButton jButton_srActivityDefending;
    private javax.swing.JButton jButton_srActivityMoving;
    private javax.swing.JButton jButton_srActivityPersonnelRecovery;
    private javax.swing.JButton jButton_srActivityStationary;
    private javax.swing.JButton jButton_srEquipment;
    private javax.swing.JButton jButton_srEquipmentArmoredPersonnelCarrierH;
    private javax.swing.JButton jButton_srEquipmentArmoredTankH;
    private javax.swing.JButton jButton_srEquipmentBack;
    private javax.swing.JButton jButton_srEquipmentBrowse;
    private javax.swing.JButton jButton_srEquipmentBrowseCategoriesBack;
    private javax.swing.JButton jButton_srEquipmentCategoryBack;
    private javax.swing.JButton jButton_srEquipmentGrenadeLauncherH;
    private javax.swing.JButton jButton_srEquipmentGroundVehicleH;
    private javax.swing.JButton jButton_srEquipmentHowitzerH;
    private javax.swing.JButton jButton_srEquipmentIEDH;
    private javax.swing.JButton jButton_srEquipmentMissileLauncherH;
    private javax.swing.JButton jButton_srEquipmentRifleH;
    private javax.swing.JButton jButton_srEquipmentSearch;
    private javax.swing.JButton jButton_srEquipmentSearchBack;
    private javax.swing.JButton jButton_srLocation;
    private javax.swing.JButton jButton_srLocationBack;
    private javax.swing.JButton jButton_srLocationLatLon;
    private javax.swing.JButton jButton_srLocationMGRS;
    private javax.swing.JButton jButton_srLocationOffset;
    private javax.swing.JButton jButton_srSend;
    private javax.swing.JButton jButton_srSize;
    private javax.swing.JButton jButton_srSizeArmy;
    private javax.swing.JButton jButton_srSizeArmyGroup;
    private javax.swing.JButton jButton_srSizeBack;
    private javax.swing.JButton jButton_srSizeBattalion;
    private javax.swing.JButton jButton_srSizeBrigade;
    private javax.swing.JButton jButton_srSizeCommand;
    private javax.swing.JButton jButton_srSizeCompany;
    private javax.swing.JButton jButton_srSizeCorps;
    private javax.swing.JButton jButton_srSizeDivision;
    private javax.swing.JButton jButton_srSizePlatoon;
    private javax.swing.JButton jButton_srSizeRegiment;
    private javax.swing.JButton jButton_srSizeRegion;
    private javax.swing.JButton jButton_srSizeSection;
    private javax.swing.JButton jButton_srSizeSquad;
    private javax.swing.JButton jButton_srSizeTeam;
    private javax.swing.JButton jButton_srTime;
    private javax.swing.JButton jButton_srTimeBack;
    private javax.swing.JButton jButton_srTimeNow;
    private javax.swing.JButton jButton_srTimeOther;
    private javax.swing.JButton jButton_srUnit;
    private javax.swing.JButton jButton_srUnitBack;
    private javax.swing.JButton jButton_srUnitCivilian;
    private javax.swing.JButton jButton_srUnitCoalition;
    private javax.swing.JButton jButton_srUnitConventional;
    private javax.swing.JButton jButton_srUnitFacility;
    private javax.swing.JButton jButton_srUnitHostNation;
    private javax.swing.JButton jButton_srUnitIrregular;
    private javax.swing.JButton jButton_srUnitNGO;
    private javax.swing.JButton jButton_waypoints;
    private javax.swing.JButton jButton_waypointsBack;
    private javax.swing.JLabel jLabel_gpsOptions;
    private javax.swing.JLabel jLabel_gpsStatus;
    private javax.swing.JLabel jLabel_mainMenu;
    private javax.swing.JLabel jLabel_mgrsMessage;
    private javax.swing.JLabel jLabel_navMgrs;
    private javax.swing.JLabel jLabel_navigation;
    private javax.swing.JLabel jLabel_options;
    private javax.swing.JLabel jLabel_overlays;
    private javax.swing.JLabel jLabel_reports;
    private javax.swing.JLabel jLabel_spotReport;
    private javax.swing.JLabel jLabel_srActivity;
    private javax.swing.JLabel jLabel_srEquipment;
    private javax.swing.JLabel jLabel_srEquipmentBrowseCategories;
    private javax.swing.JLabel jLabel_srEquipmentCategory;
    private javax.swing.JLabel jLabel_srEquipmentSearch;
    private javax.swing.JLabel jLabel_srLocation;
    private javax.swing.JLabel jLabel_srSize;
    private javax.swing.JLabel jLabel_srTime;
    private javax.swing.JLabel jLabel_srUnit;
    private javax.swing.JLabel jLabel_waypoints;
    private javax.swing.JPanel jPanel_gpsOptions;
    private javax.swing.JPanel jPanel_main;
    private javax.swing.JPanel jPanel_navigation;
    private javax.swing.JPanel jPanel_options;
    private javax.swing.JPanel jPanel_overlays;
    private javax.swing.JPanel jPanel_reports;
    private javax.swing.JPanel jPanel_spotReport;
    private javax.swing.JPanel jPanel_srActivity;
    private javax.swing.JPanel jPanel_srEquipment;
    private javax.swing.JPanel jPanel_srEquipmentBrowseCategories;
    private javax.swing.JPanel jPanel_srEquipmentCategories;
    private javax.swing.JPanel jPanel_srEquipmentCategory;
    private javax.swing.JPanel jPanel_srEquipmentPresets;
    private javax.swing.JPanel jPanel_srEquipmentSearch;
    private javax.swing.JPanel jPanel_srLocation;
    private javax.swing.JPanel jPanel_srSize;
    private javax.swing.JPanel jPanel_srTime;
    private javax.swing.JPanel jPanel_srUnit;
    private javax.swing.JPanel jPanel_toc;
    private javax.swing.JPanel jPanel_waypoints;
    private javax.swing.JPanel jPanel_waypointsList;
    private javax.swing.JRadioButton jRadioButton_onboardGPS;
    private javax.swing.JRadioButton jRadioButton_simulatedGPS;
    private javax.swing.JScrollPane jScrollPane_srEquipmentCategories;
    private javax.swing.JScrollPane jScrollPane_srEquipmentCategoryResults;
    private javax.swing.JScrollPane jScrollPane_srEquipmentPresets;
    private javax.swing.JScrollPane jScrollPane_srEquipmentSearchResults;
    private javax.swing.JScrollPane jScrollPane_toc;
    private javax.swing.JScrollPane jScrollPane_waypointsList;
    private javax.swing.JTextField jTextField_navMgrs;
    private javax.swing.JTextField jTextField_srEquipmentSearchField;
    private javax.swing.JToggleButton jToggleButton_sendMyLocation;
    private javax.swing.JToggleButton jToggleButton_showMe;
    private javax.swing.JToggleButton jToggleButton_srLocationFromMap;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
