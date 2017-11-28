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
import com.esri.vehiclecommander.util.Utilities;
import java.util.Enumeration;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JOptionPane;

/**
 * A dialog that lets the user change application configuration settings. The underlying
 * settings are managed by an AppConfigController object.
 */
public class AppConfigDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 7608310305216869748L;
    private static final String MGRS = "mgrs";
    private static final String LONLAT = "lonlat";
    
    private final AppConfigController appConfigController;

    /**
     * Creates a new AppConfigDialog but does not set it to be visible.
     * @param parent the parent frame for the dialog.
     * @param modal true if the dialog is modal and false otherwise.
     * @param appConfigController the AppConfigController that manages the application
     *                            configuration settings.
     */
    public AppConfigDialog(java.awt.Frame parent, boolean modal, AppConfigController appConfigController) {
        super(parent, modal);
        this.appConfigController = appConfigController;
        initComponents();
        copySettingsToUI();
        saveSettings();
    }

    private void copySettingsToUI() {
        /**
         * If one of the JSpinners is currently focused, it will keep its UI value
         * even if we set its value here. Therefore, have this dialog request the focus.
         */
        this.requestFocus();

        //Set field values from appConfigController
        String username = appConfigController.getUsername();
        if (null != username) {
            jTextField_username.setText(username);
        }
        String vehicleType = appConfigController.getVehicleType();
        if (null != username) {
            jTextField_vehicleType.setText(vehicleType);
        }
        String uniqueId = appConfigController.getUniqueId();
        if (null != uniqueId) {
            jTextField_uniqueId.setText(uniqueId);
        }
        String sic = appConfigController.getSic();
        if (null != sic) {
            jTextField_sic.setText(sic);
        }
        int port = appConfigController.getPort();
        if (-1 < port) {
            jSpinner_messagingPort.setValue(port);
        }
        int positionMessageInterval = appConfigController.getPositionMessageInterval();
        if (-1 < positionMessageInterval) {
            jSpinner_positionMessageInterval.setValue(positionMessageInterval);
        }
        int vehicleStatusMessageInterval = appConfigController.getVehicleStatusMessageInterval();
        if (-1 < vehicleStatusMessageInterval) {
            jSpinner_vehicleStatusMessageInterval.setValue(vehicleStatusMessageInterval);
        }
        double speedMultiplier = appConfigController.getSpeedMultiplier();
        if (0 < speedMultiplier) {
            jSpinner_speedMultiplier.setValue(speedMultiplier);
        }
        jCheckBox_showMessageLabels.setSelected(appConfigController.isShowMessageLabels());
        jCheckBox_decorated.setSelected(appConfigController.isDecorated());
        jCheckBox_showLocalTimeZone.setSelected(appConfigController.isShowLocalTimeZone());
        if (appConfigController.isShowMgrs()) {
            jRadioButton_mgrs.setSelected(true);
        } else {
            jRadioButton_lonLat.setSelected(true);
        }
        Enumeration<AbstractButton> headingUnitsButtons = buttonGroup_headingUnits.getElements();
        while (headingUnitsButtons.hasMoreElements()) {
            AbstractButton button = headingUnitsButtons.nextElement();
            if (button.getActionCommand().equals(Integer.toString(appConfigController.getHeadingUnits()))) {
                button.setSelected(true);
                break;
            }
        }
        jComboBox_geomessageVersion.setSelectedItem(appConfigController.getGeomessageVersion());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup_headingUnits = new javax.swing.ButtonGroup();
        buttonGroup_coordinateNotation = new javax.swing.ButtonGroup();
        jButton_resetAll = new javax.swing.JButton();
        jLabel_username = new javax.swing.JLabel();
        jLabel_uniqueId = new javax.swing.JLabel();
        jTextField_username = new javax.swing.JTextField();
        jTextField_uniqueId = new javax.swing.JTextField();
        generateRandomUniqueId();
        jButton_generateUniqueId = new javax.swing.JButton();
        jLabel_sic = new javax.swing.JLabel();
        jLabel_messagingPort = new javax.swing.JLabel();
        jLabel_positionMessageInterval = new javax.swing.JLabel();
        jTextField_sic = new javax.swing.JTextField();
        jSpinner_messagingPort = new javax.swing.JSpinner();
        jSpinner_positionMessageInterval = new javax.swing.JSpinner();
        jButton_cancel = new javax.swing.JButton();
        jButton_ok = new javax.swing.JButton();
        jSpinner_speedMultiplier = new javax.swing.JSpinner();
        jLabel_speedMultiplier = new javax.swing.JLabel();
        jCheckBox_showMessageLabels = new javax.swing.JCheckBox();
        jCheckBox_decorated = new javax.swing.JCheckBox();
        jCheckBox_showLocalTimeZone = new javax.swing.JCheckBox();
        jLabel_vehicleStatusMessageInterval = new javax.swing.JLabel();
        jSpinner_vehicleStatusMessageInterval = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jRadioButton_degrees = new javax.swing.JRadioButton();
        jRadioButton_mils = new javax.swing.JRadioButton();
        jComboBox_geomessageVersion = new javax.swing.JComboBox();
        jLabel_geomessageVersion = new javax.swing.JLabel();
        jLabel_vehicleType = new javax.swing.JLabel();
        jTextField_vehicleType = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton_mgrs = new javax.swing.JRadioButton();
        jRadioButton_lonLat = new javax.swing.JRadioButton();
        jLabel_appVersion = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Settings");

        jButton_resetAll.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jButton_resetAll.setText("Reset");
        jButton_resetAll.setFocusable(false);
        jButton_resetAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_resetAllActionPerformed(evt);
            }
        });

        jLabel_username.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_username.setText("Username");

        jLabel_uniqueId.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_uniqueId.setText("Unique ID");

        jTextField_username.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jTextField_username.setText("Honey Badgers 42B");

        jTextField_uniqueId.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N

        jButton_generateUniqueId.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jButton_generateUniqueId.setText("Generate Unique ID");
        jButton_generateUniqueId.setFocusable(false);
        jButton_generateUniqueId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_generateUniqueIdActionPerformed(evt);
            }
        });

        jLabel_sic.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_sic.setText("Symbol ID Code");

        jLabel_messagingPort.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_messagingPort.setText("Messaging Port");

        jLabel_positionMessageInterval.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_positionMessageInterval.setText("Position Message Interval (ms)");

        jTextField_sic.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jTextField_sic.setText("SFGPEVATM-EDUSG");

        jSpinner_messagingPort.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jSpinner_messagingPort.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner_messagingPort, "#####"));
        jSpinner_messagingPort.setValue(45678);

        jSpinner_positionMessageInterval.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jSpinner_positionMessageInterval.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner_positionMessageInterval, "#########"));
        jSpinner_positionMessageInterval.setValue(3000);

        jButton_cancel.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jButton_cancel.setText("Cancel");
        jButton_cancel.setFocusable(false);
        jButton_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_cancelActionPerformed(evt);
            }
        });

        jButton_ok.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jButton_ok.setText("OK");
        jButton_ok.setFocusable(false);
        jButton_ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_okActionPerformed(evt);
            }
        });

        jSpinner_speedMultiplier.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jSpinner_speedMultiplier.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(1.0E-7d), null, Double.valueOf(0.25d)));
        jSpinner_speedMultiplier.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner_speedMultiplier, ""));

        jLabel_speedMultiplier.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_speedMultiplier.setText("GPS Speed Multiplier");

        jCheckBox_showMessageLabels.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jCheckBox_showMessageLabels.setSelected(true);
        jCheckBox_showMessageLabels.setText("Show Message Labels");
        jCheckBox_showMessageLabels.setFocusable(false);

        jCheckBox_decorated.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jCheckBox_decorated.setSelected(appConfigController.isDecorated());
        jCheckBox_decorated.setText("Decorated");
        jCheckBox_decorated.setFocusable(false);

        jCheckBox_showLocalTimeZone.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jCheckBox_showLocalTimeZone.setText("Show Local Time Zone");
        jCheckBox_showLocalTimeZone.setFocusable(false);

        jLabel_vehicleStatusMessageInterval.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_vehicleStatusMessageInterval.setText("Vehicle Status Message Interval (ms)");

        jSpinner_vehicleStatusMessageInterval.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jSpinner_vehicleStatusMessageInterval.setEditor(new javax.swing.JSpinner.NumberEditor(jSpinner_vehicleStatusMessageInterval, "#########"));
        jSpinner_vehicleStatusMessageInterval.setValue(30000);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel1.setText("Heading Units");

        buttonGroup_headingUnits.add(jRadioButton_degrees);
        jRadioButton_degrees.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jRadioButton_degrees.setText("Degrees");
        jRadioButton_degrees.setActionCommand(Integer.toString(AngularUnit.Code.DEGREE));
        jRadioButton_degrees.setFocusable(false);

        buttonGroup_headingUnits.add(jRadioButton_mils);
        jRadioButton_mils.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jRadioButton_mils.setText("Mils");
        jRadioButton_mils.setActionCommand(Integer.toString(AngularUnit.Code.MIL_6400));
        jRadioButton_mils.setFocusable(false);

        jComboBox_geomessageVersion.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jComboBox_geomessageVersion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1.0", "1.1" }));
        jComboBox_geomessageVersion.setSelectedIndex(1);

        jLabel_geomessageVersion.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_geomessageVersion.setText("Geomessage v.");

        jLabel_vehicleType.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_vehicleType.setText("Vehicle Type");

        jTextField_vehicleType.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jTextField_vehicleType.setText("HMMWV");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel2.setText("Coordinate Notation");

        buttonGroup_coordinateNotation.add(jRadioButton_mgrs);
        jRadioButton_mgrs.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jRadioButton_mgrs.setSelected(true);
        jRadioButton_mgrs.setText("MGRS");
        jRadioButton_mgrs.setActionCommand(MGRS);
        jRadioButton_mgrs.setFocusable(false);

        buttonGroup_coordinateNotation.add(jRadioButton_lonLat);
        jRadioButton_lonLat.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jRadioButton_lonLat.setText("Lon/Lat");
        jRadioButton_lonLat.setActionCommand(LONLAT);
        jRadioButton_lonLat.setFocusable(false);

        jLabel_appVersion.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_appVersion.setText("Vehicle Commander " + Utilities.APP_VERSION);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_resetAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel_appVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_positionMessageInterval)
                            .addComponent(jLabel_messagingPort)
                            .addComponent(jLabel_sic))
                        .addGap(60, 60, 60)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_sic, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSpinner_messagingPort, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSpinner_positionMessageInterval)
                            .addComponent(jButton_generateUniqueId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox_showMessageLabels)
                        .addGap(29, 29, 29)
                        .addComponent(jCheckBox_showLocalTimeZone)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox_decorated))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel_vehicleStatusMessageInterval)
                        .addGap(10, 10, 10)
                        .addComponent(jSpinner_vehicleStatusMessageInterval))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel_speedMultiplier)
                        .addGap(133, 133, 133)
                        .addComponent(jSpinner_speedMultiplier))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton_degrees)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton_mils)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel_geomessageVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox_geomessageVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_vehicleType)
                            .addComponent(jLabel_uniqueId))
                        .addGap(201, 201, 201)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_vehicleType)
                            .addComponent(jTextField_uniqueId)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel_username)
                        .addGap(223, 223, 223)
                        .addComponent(jTextField_username))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton_mgrs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton_lonLat)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton_resetAll)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_username)
                    .addComponent(jTextField_username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_vehicleType)
                    .addComponent(jTextField_vehicleType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_uniqueId)
                    .addComponent(jTextField_uniqueId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton_generateUniqueId)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_sic)
                    .addComponent(jTextField_sic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_messagingPort)
                    .addComponent(jSpinner_messagingPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_positionMessageInterval)
                    .addComponent(jSpinner_positionMessageInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_vehicleStatusMessageInterval)
                    .addComponent(jSpinner_vehicleStatusMessageInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner_speedMultiplier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_speedMultiplier))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox_showMessageLabels)
                    .addComponent(jCheckBox_decorated)
                    .addComponent(jCheckBox_showLocalTimeZone))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jRadioButton_mgrs)
                    .addComponent(jRadioButton_lonLat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jRadioButton_degrees)
                    .addComponent(jRadioButton_mils)
                    .addComponent(jComboBox_geomessageVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_geomessageVersion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_cancel)
                    .addComponent(jButton_ok)
                    .addComponent(jLabel_appVersion))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_okActionPerformed
        saveSettings();

        setVisible(false);
    }//GEN-LAST:event_jButton_okActionPerformed

    private void saveSettings() {
        /**
         * If one of the JSpinners is currently focused, it will keep its UI value
         * even if we set its value here. Therefore, have this dialog request the focus.
         */
        this.requestFocus();
        
        appConfigController.setUsername(jTextField_username.getText().trim());
        appConfigController.setVehicleType(jTextField_vehicleType.getText().trim());
        appConfigController.setUniqueId(jTextField_uniqueId.getText().trim());
        appConfigController.setSic(jTextField_sic.getText().trim());
        Object port = jSpinner_messagingPort.getValue();
        if (null != port && port instanceof Integer) {
            appConfigController.setPort((Integer) port);
        }
        Object positionMessageInterval = jSpinner_positionMessageInterval.getValue();
        if (null != positionMessageInterval && positionMessageInterval instanceof Integer) {
            appConfigController.setPositionMessageInterval((Integer) positionMessageInterval);
        }
        Object vehicleStatusMessageInterval = jSpinner_vehicleStatusMessageInterval.getValue();
        if (null != vehicleStatusMessageInterval && vehicleStatusMessageInterval instanceof Integer) {
            appConfigController.setVehicleStatusMessageInterval((Integer) vehicleStatusMessageInterval);
        }
        Object speedMultiplier = jSpinner_speedMultiplier.getValue();
        if (null != speedMultiplier && speedMultiplier instanceof Double) {
            appConfigController.setSpeedMultiplier((Double) speedMultiplier);
        }
        appConfigController.setShowMessageLabels(jCheckBox_showMessageLabels.isSelected());
        appConfigController.setDecorated(jCheckBox_decorated.isSelected());
        appConfigController.setShowLocalTimeZone(jCheckBox_showLocalTimeZone.isSelected());
        appConfigController.setShowMgrs(MGRS.equals(buttonGroup_coordinateNotation.getSelection().getActionCommand()));
        try {
            appConfigController.setHeadingUnits(Integer.parseInt(buttonGroup_headingUnits.getSelection().getActionCommand()));
        } catch (NumberFormatException nfe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Heading units should be an AngularUnit.Code constant", nfe);
        }
        appConfigController.setGeomessageVersion((String) jComboBox_geomessageVersion.getSelectedItem());
    }

    private void jButton_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_cancelActionPerformed
        setVisible(false);
        copySettingsToUI();
    }//GEN-LAST:event_jButton_cancelActionPerformed

    private void jButton_generateUniqueIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_generateUniqueIdActionPerformed
        generateRandomUniqueId();
    }//GEN-LAST:event_jButton_generateUniqueIdActionPerformed

    private void jButton_resetAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_resetAllActionPerformed
        if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset all settings?",
                "Reset All Settings",
                JOptionPane.YES_NO_OPTION)) {
            try {
                appConfigController.resetFromAppConfigFile(true);
                copySettingsToUI();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not reset settings: " + e.getMessage(),
                        "Could Not Reset Settings",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton_resetAllActionPerformed

    private void generateRandomUniqueId() {
        jTextField_uniqueId.setText(UUID.randomUUID().toString());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup_coordinateNotation;
    private javax.swing.ButtonGroup buttonGroup_headingUnits;
    private javax.swing.JButton jButton_cancel;
    private javax.swing.JButton jButton_generateUniqueId;
    private javax.swing.JButton jButton_ok;
    private javax.swing.JButton jButton_resetAll;
    private javax.swing.JCheckBox jCheckBox_decorated;
    private javax.swing.JCheckBox jCheckBox_showLocalTimeZone;
    private javax.swing.JCheckBox jCheckBox_showMessageLabels;
    private javax.swing.JComboBox jComboBox_geomessageVersion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel_appVersion;
    private javax.swing.JLabel jLabel_geomessageVersion;
    private javax.swing.JLabel jLabel_messagingPort;
    private javax.swing.JLabel jLabel_positionMessageInterval;
    private javax.swing.JLabel jLabel_sic;
    private javax.swing.JLabel jLabel_speedMultiplier;
    private javax.swing.JLabel jLabel_uniqueId;
    private javax.swing.JLabel jLabel_username;
    private javax.swing.JLabel jLabel_vehicleStatusMessageInterval;
    private javax.swing.JLabel jLabel_vehicleType;
    private javax.swing.JRadioButton jRadioButton_degrees;
    private javax.swing.JRadioButton jRadioButton_lonLat;
    private javax.swing.JRadioButton jRadioButton_mgrs;
    private javax.swing.JRadioButton jRadioButton_mils;
    private javax.swing.JSpinner jSpinner_messagingPort;
    private javax.swing.JSpinner jSpinner_positionMessageInterval;
    private javax.swing.JSpinner jSpinner_speedMultiplier;
    private javax.swing.JSpinner jSpinner_vehicleStatusMessageInterval;
    private javax.swing.JTextField jTextField_sic;
    private javax.swing.JTextField jTextField_uniqueId;
    private javax.swing.JTextField jTextField_username;
    private javax.swing.JTextField jTextField_vehicleType;
    // End of variables declaration//GEN-END:variables
}
