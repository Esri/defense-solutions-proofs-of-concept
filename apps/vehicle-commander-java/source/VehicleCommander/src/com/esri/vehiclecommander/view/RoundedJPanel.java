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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * A JPanel with rounded edges.
 */
public class RoundedJPanel extends JPanel {

    private static final long serialVersionUID = -6908670621531162809L;

    @Override
    protected void paintComponent(Graphics g) {
        int borderWidth = 3;
        int x = borderWidth;
        int y = borderWidth;
        int w = getWidth() - (2 * borderWidth);
        int h = getHeight() - (2 * borderWidth);
        int arc = 30;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setStroke(new BasicStroke(borderWidth));
        g2.setColor(new Color(40, 40, 40));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.dispose();
    }

}
