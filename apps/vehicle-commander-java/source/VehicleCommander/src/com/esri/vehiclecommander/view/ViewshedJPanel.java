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
package com.esri.vehiclecommander.view;

import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.map.ArcGISDynamicMapServiceLayer;
import com.esri.vehiclecommander.controller.GPAdapter;
import com.esri.vehiclecommander.controller.MapController;
import com.esri.vehiclecommander.controller.ViewshedController;
import com.esri.vehiclecommander.util.Utilities;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A form for viewshed analysis input.
 */
public class ViewshedJPanel extends RoundedJPanel {
    
    private static final HashMap<Object, Integer> presetToRange = new HashMap<Object, Integer>();
    static {
        presetToRange.put("Rifle", 800);
        presetToRange.put("Mortar", 2000);
        presetToRange.put("Artillery", 15000);
    }
    
    private final Frame app;
    private final MapController mapController;
    private final String originalDirections;
    private final ViewshedController viewshedController;
    
    private Point viewshedCenter = null;
    private ArcGISDynamicMapServiceLayer resultLayer = null;

    /**
     * Creates the viewshed form but does not add it to the application.
     */
    public ViewshedJPanel(Frame app, MapController mapController, ViewshedController viewshedController) {
        initComponents();
        setSize(getPreferredSize());
        this.app = app;
        this.mapController = mapController;
        originalDirections = jLabel_directions.getText();
        this.viewshedController = viewshedController;
        
        if (null != viewshedController) {
            viewshedController.addGPListener(new GPAdapter() {

                @Override
                public void gpStarted() {
                    jProgressBar_loading.setVisible(true);
                    jButton_runViewshed.setEnabled(false);
                }

                @Override
                public void gpEnded(final ArcGISDynamicMapServiceLayer resultLayer) {
                    jProgressBar_loading.setVisible(false);
                    if (null != ViewshedJPanel.this.resultLayer) {
                        ViewshedJPanel.this.mapController.removeLayer(ViewshedJPanel.this.resultLayer);
                    }
                    ViewshedJPanel.this.resultLayer = resultLayer;
                    if (null != resultLayer) {
                        resultLayer.setName("Viewshed");
                        ViewshedJPanel.this.mapController.addLayer(resultLayer, true);
                    }
                    jButton_runViewshed.setEnabled(true);
                }
                
            });
        }
        
        if (null != mapController) {
            StringBuilder sb = new StringBuilder("Radius");
            SpatialReference sr = mapController.getSpatialReference();
            if (null != sr) {
                if (null != sr.getUnit()) {
                    sb.append(" (").append(sr.getUnit().getAbbreviation()).append(")");
                }
            }
            sb.append(":");
            jLabel_radius.setText(sb.toString());
        }
        
        jTextField_radius.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                textChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                textChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                //Do nothing
            }
            
            private void textChanged() {
                try {
                    if (null != viewshedCenter) {
                        double radius = Double.parseDouble(jTextField_radius.getText());
                        jButton_runViewshed.setEnabled(true);
                        ViewshedJPanel.this.viewshedController.showViewshedBoundsGraphic(viewshedCenter, radius);
                    }
                } catch (NumberFormatException nfe) {
                    jButton_runViewshed.setEnabled(false);
                    ViewshedJPanel.this.viewshedController.removeViewshedBoundsGraphic();
                }
            }
        });
    }
    
    public ViewshedController getViewshedController() {
        return viewshedController;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel_title = new javax.swing.JLabel();
        jLabel_directions = new javax.swing.JLabel();
        jPanel_form = new javax.swing.JPanel();
        jLabel_radius = new javax.swing.JLabel();
        jTextField_radius = new javax.swing.JTextField();
        jButton_runViewshed = new javax.swing.JButton();
        jLabel_presets = new javax.swing.JLabel();
        jComboBox_presets = new javax.swing.JComboBox();
        jButton_close = new javax.swing.JButton();
        jPanel_loading = new javax.swing.JPanel();
        jProgressBar_loading = new javax.swing.JProgressBar();
        jProgressBar_loading.setVisible(false);

        setOpaque(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        jLabel_title.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel_title.setText("Viewshed");

        jLabel_directions.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_directions.setText("<html>Tap the desired viewshed center on the map.</html>");

        jPanel_form.setOpaque(false);
        jPanel_form.setVisible(false);

        jLabel_radius.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_radius.setText("Radius:");

        jTextField_radius.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jTextField_radius.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        jTextField_radius.setText("500");
        jTextField_radius.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField_radiusFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField_radiusFocusLost(evt);
            }
        });
        jTextField_radius.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField_radiusKeyReleased(evt);
            }
        });

        jButton_runViewshed.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jButton_runViewshed.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Play-Normal.png"))); // NOI18N
        jButton_runViewshed.setBorderPainted(false);
        jButton_runViewshed.setContentAreaFilled(false);
        jButton_runViewshed.setFocusable(false);
        jButton_runViewshed.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton_runViewshed.setMaximumSize(new java.awt.Dimension(50, 50));
        jButton_runViewshed.setMinimumSize(new java.awt.Dimension(50, 50));
        jButton_runViewshed.setPreferredSize(new java.awt.Dimension(50, 50));
        jButton_runViewshed.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/com/esri/vehiclecommander/resources/Play-Pressed.png"))); // NOI18N
        jButton_runViewshed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_runViewshedActionPerformed(evt);
            }
        });

        jLabel_presets.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jLabel_presets.setText("Preset Viewshed Radii:");

        jComboBox_presets.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        jComboBox_presets.setModel(new javax.swing.DefaultComboBoxModel(new String[] { " ", "Rifle", "Mortar", "Artillery" }));
        jComboBox_presets.setFocusable(false);
        jComboBox_presets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox_presetsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_formLayout = new javax.swing.GroupLayout(jPanel_form);
        jPanel_form.setLayout(jPanel_formLayout);
        jPanel_formLayout.setHorizontalGroup(
            jPanel_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 360, Short.MAX_VALUE)
            .addGroup(jPanel_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel_formLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jComboBox_presets, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel_formLayout.createSequentialGroup()
                            .addComponent(jLabel_radius)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jTextField_radius, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addGap(18, 18, 18)
                            .addComponent(jButton_runViewshed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel_formLayout.createSequentialGroup()
                            .addComponent(jLabel_presets)
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        jPanel_formLayout.setVerticalGroup(
            jPanel_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 134, Short.MAX_VALUE)
            .addGroup(jPanel_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel_formLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel_formLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel_radius, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_runViewshed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jTextField_radius, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel_presets)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jComboBox_presets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

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

        jPanel_loading.setMaximumSize(new java.awt.Dimension(50, 50));
        jPanel_loading.setMinimumSize(new java.awt.Dimension(50, 50));
        jPanel_loading.setOpaque(false);
        jPanel_loading.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel_loading.setLayout(new javax.swing.BoxLayout(jPanel_loading, javax.swing.BoxLayout.LINE_AXIS));

        jProgressBar_loading.setIndeterminate(true);
        jPanel_loading.add(jProgressBar_loading);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel_title)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel_loading, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton_close, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_directions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel_form, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel_title)
                    .addComponent(jButton_close, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel_loading, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_directions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_form, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(186, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        mapController.trackAsync(new MapOverlayAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {
                jPanel_form.setVisible(true);
                viewshedCenter = mapController.toMapPointObject(event.getX(), event.getY());
                viewshedController.showViewshedCenterGraphic(viewshedCenter);
                jLabel_directions.setText("<html>Tap a second point, or enter the viewshed radius:</html>");
                mapController.trackAsync(new MapOverlayAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent event) {
                        //This is the SECOND point; fill in the radius
                        Point viewshedOuter = mapController.toMapPointObject(event.getX(), event.getY());
                        double distance = viewshedController.showViewshedBoundsGraphic(viewshedCenter, viewshedOuter);
                        jTextField_radius.setText(Long.toString(Math.round(distance)));
                    }

                }, MapController.EVENT_MOUSE_CLICKED);
            }

        }, MapController.EVENT_MOUSE_CLICKED);
    }//GEN-LAST:event_formComponentShown

    private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
        mapController.cancelTrackAsync();
        jLabel_directions.setText(originalDirections);
        jPanel_form.setVisible(false);
        jComboBox_presets.setSelectedIndex(0);
        viewshedController.removeViewshedBoundsGraphic();
        viewshedController.removeViewshedCenterGraphic();
        viewshedCenter = null;
    }//GEN-LAST:event_formComponentHidden

    private void jComboBox_presetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox_presetsActionPerformed
        Integer range = presetToRange.get(jComboBox_presets.getSelectedItem());
        if (null != range) {
            jTextField_radius.setText(Integer.toString(range));
        }
    }//GEN-LAST:event_jComboBox_presetsActionPerformed

    private void jButton_closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_closeActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButton_closeActionPerformed

    private void jTextField_radiusKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField_radiusKeyReleased
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ESCAPE: {
                if (null != app) {
                    Utilities.closeApplication(app);
                }
                break;
            }
                
            case KeyEvent.VK_ENTER: {
                jButton_runViewshed.doClick();
                break;
            }
        }
    }//GEN-LAST:event_jTextField_radiusKeyReleased

    private void jButton_runViewshedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_runViewshedActionPerformed
        try {
            viewshedController.calculateViewshed(viewshedCenter, Double.parseDouble(jTextField_radius.getText()));
        } catch (NumberFormatException nfe) {
            //Do nothing
        }
    }//GEN-LAST:event_jButton_runViewshedActionPerformed

    private void jTextField_radiusFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_radiusFocusGained
        mapController.setKeyboardEnabled(false);
    }//GEN-LAST:event_jTextField_radiusFocusGained

    private void jTextField_radiusFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField_radiusFocusLost
        mapController.setKeyboardEnabled(true);
    }//GEN-LAST:event_jTextField_radiusFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_close;
    private javax.swing.JButton jButton_runViewshed;
    private javax.swing.JComboBox jComboBox_presets;
    private javax.swing.JLabel jLabel_directions;
    private javax.swing.JLabel jLabel_presets;
    private javax.swing.JLabel jLabel_radius;
    private javax.swing.JLabel jLabel_title;
    private javax.swing.JPanel jPanel_form;
    private javax.swing.JPanel jPanel_loading;
    private javax.swing.JProgressBar jProgressBar_loading;
    private javax.swing.JTextField jTextField_radius;
    // End of variables declaration//GEN-END:variables
}
