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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;

/**
 * A JPanel that displays an image that can be rotated.
 */
public class RotatableImagePanel extends JPanel {

    private static final long serialVersionUID = 6168631087188216464L;

    private final Image image;
    private double currentAngle;

    /**
     * Creates a new RotatableImagePanel that initially is not rotated.
     * @param image the image.
     */
    public RotatableImagePanel(Image image) {
        this(image, 0);
    }

    /**
     * Creates a new RotatableImagePanel that initially is rotated.
     * @param image the image.
     * @param initialAngle the rotation angle in radians.
     */
    public RotatableImagePanel(Image image, double initialAngle) {
        this.image = image;
        currentAngle = initialAngle;
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForID(0);
        } catch (Exception e) {
        }
    }

    /**
     * Adds a number of radians to the image's current rotation.
     * @param radians the number of radians to add to the image's rotation.
     */
    public void rotate(double radians) {
        currentAngle += radians;
        fixAngle();
        repaint();
    }

    /**
     * Sets the image's rotation in radians.
     * @param radians the image's rotation in radians.
     */
    public void setRotation(double radians) {
        currentAngle = radians;
        fixAngle();
        repaint();
    }

    private void fixAngle() {
        if (currentAngle > 2 * Math.PI) {
            currentAngle -= 2 * Math.PI;
        } else if (currentAngle < 0) {
            currentAngle += 2 * Math.PI;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform origXform = g2d.getTransform();
        AffineTransform newXform = (AffineTransform) (origXform.clone());
        //center of rotation is center of the panel
        int xRot = this.getWidth() / 2;
        int yRot = this.getHeight() / 2;
        newXform.rotate((2 * Math.PI) - currentAngle, xRot, yRot);
        g2d.setTransform(newXform);
        //draw image centered in panel
        int x = (getWidth() - image.getWidth(this)) / 2;
        int y = (getHeight() - image.getHeight(this)) / 2;
        g2d.drawImage(image, x, y, this);
        g2d.setTransform(origXform);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(image.getWidth(this), image.getHeight(this));
    }
}
