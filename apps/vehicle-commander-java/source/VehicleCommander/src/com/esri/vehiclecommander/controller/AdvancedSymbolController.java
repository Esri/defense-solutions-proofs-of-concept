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

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.DictionaryRenderer;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageHelper;
import com.esri.core.symbol.advanced.SymbolDictionary;
import com.esri.core.symbol.advanced.SymbolDictionary.DictionaryType;
import com.esri.core.symbol.advanced.SymbolProperties;
import com.esri.map.GraphicsLayer;
import com.esri.map.Layer;
import com.esri.map.MessageGroupLayer;
import com.esri.militaryapps.controller.ChemLightController;
import com.esri.militaryapps.controller.MessageController;
import com.esri.militaryapps.controller.MessageControllerListener;
import com.esri.militaryapps.controller.SpotReportController;
import com.esri.militaryapps.model.Geomessage;
import com.esri.militaryapps.util.Utilities;
import com.esri.runtime.ArcGISRuntime;
import com.esri.vehiclecommander.model.IdentifyResultList;
import com.esri.vehiclecommander.model.Mil2525CMessageLayer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;


/**
 * A controller to handle the usage of MessageProcessor and SymbolDictionary by
 * the application.
 */
public class AdvancedSymbolController extends com.esri.militaryapps.controller.AdvancedSymbolController implements MessageControllerListener {
    
    public static final String SPOT_REPORT_LAYER_NAME = "Spot Reports";

    private final MapController mapController;
    private final MessageGroupLayer groupLayer;
    private final GraphicsLayer spotReportLayer;
    private final Symbol spotReportSymbol;
    private final MessageController messageController;
    private final File symDictDir;
    private final AppConfigController appConfigController;

    /**
     * Creates a new AdvancedSymbolController.
     * @param mapController the application's MapController.
     * @param spotReportIcon the spot report icon image.
     */
    public AdvancedSymbolController(
            MapController mapController,
            BufferedImage spotReportIcon,
            MessageController messageController,
            AppConfigController appConfigController) {
        super(mapController);
        this.mapController = mapController;
        this.appConfigController = appConfigController;
        
        spotReportLayer = new GraphicsLayer();
        spotReportLayer.setName(SPOT_REPORT_LAYER_NAME);
        mapController.addLayer(spotReportLayer, false);
        
        groupLayer = new MessageGroupLayer(SymbolDictionary.DictionaryType.Mil2525C);
        mapController.addLayer(groupLayer, false);
        
        spotReportSymbol = new PictureMarkerSymbol(spotReportIcon);
        
        setShowLabels(appConfigController.isShowMessageLabels());
        
        this.messageController = messageController;
        symDictDir = new File(ArcGISRuntime.getRuntimeBinariesDir(), "../../resources/symbols/mil2525c");
    }

    @Override
    protected void toggleLabels() {
        for (Layer layer : groupLayer.getLayers()) {
            GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
            if (graphicsLayer.getRenderer() instanceof DictionaryRenderer) {
                DictionaryRenderer dictionaryRenderer = (DictionaryRenderer) graphicsLayer.getRenderer();
                dictionaryRenderer.setLabelsVisible(isShowLabels());
                graphicsLayer.setRenderer(dictionaryRenderer);
            }
        }
    }
    
    @Override
    protected Integer displaySpotReport(double x, double y, final int wkid, Integer graphicId, Geomessage geomessage) {
        try {
            Geometry pt = new Point(x, y);
            if (null != mapController.getSpatialReference() && wkid != mapController.getSpatialReference().getID()) {
                pt = GeometryEngine.project(pt, SpatialReference.create(wkid), mapController.getSpatialReference());
            }
            if (null != graphicId) {
                spotReportLayer.updateGraphic(graphicId, pt);
                spotReportLayer.updateGraphic(graphicId, geomessage.getProperties());
            } else {
                Graphic graphic = new Graphic(pt, spotReportSymbol, geomessage.getProperties());
                graphicId = spotReportLayer.addGraphic(graphic);
                
            }
            return graphicId;
        } catch (NumberFormatException nfe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not parse spot report", nfe);
            return null;
        }
    }
    
    @Override
    protected String translateColorString(String geomessageColorString) {
        if ("1".equals(geomessageColorString)) {
            geomessageColorString = "red";
        } else if ("2".equals(geomessageColorString)) {
            geomessageColorString = "green";
        } else if ("3".equals(geomessageColorString)) {
            geomessageColorString = "blue";
        } else if ("4".equals(geomessageColorString)) {
            geomessageColorString = "yellow";
        }
        return geomessageColorString;
    }
    
    /**
     * Gets the symbol image of size 100x100 for the specified symbol name or SIC.
     * @param symbolNameOrId a symbol name or SIC.
     * @return the symbol image for the specified symbol name or SIC.
     */
    public BufferedImage getSymbolImage(String symbolNameOrId) {
        return getSymbolImage(symbolNameOrId, 100, 100);
    }
        
    /**
     * Gets the symbol image for the specified symbol name or SIC.
     * @param symbolNameOrId a symbol name or SIC.
     * @param width the width (in pixels) of the generated image.
     * @param height the height (in pixels) of the generated image.
     * @return the symbol image for the specified symbol name or SIC.
     */
    public BufferedImage getSymbolImage(String symbolNameOrId, int width, int height) {
        return groupLayer.getMessageProcessor().getSymbolDictionary().getSymbolImage(symbolNameOrId, width, height);
    }
    
    /**
     * Searches for symbols having a keyword matching (fully or partially) the provided
     * search string.
     * @param searchString The search string.
     * @return A list of symbols matching the provided search string.
     */
    public List<SymbolProperties> findSymbols(String searchString) throws IOException {
        ArrayList<String> keywords = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(searchString, Utilities.MIL_2525C_WHITESPACE_CHARS);
        while (tok.hasMoreTokens()) {
            keywords.add(tok.nextToken());
        }
        return groupLayer.getMessageProcessor().getSymbolDictionary().findSymbols(keywords);
    }
    
    /**
     * Returns a list of symbol categories.
     * @return a list of symbol categories.
     */
    public List<String> getCategories() {
        return groupLayer.getMessageProcessor().getSymbolDictionary().getFilters().get("Category");
    }
    
    /**
     * Returns a list of symbols in a category.
     * @param category the category name.
     * @return a list of symbols in a category.
     */
    public List<SymbolProperties> getSymbolsInCategory(String category) throws IOException {
        HashMap<String, List<String>> filters = new HashMap<String, List<String>>(1); 
        ArrayList<String> categories = new ArrayList<String>(1);
        categories.add(category);
        filters.put("Category", categories);
        return groupLayer.getMessageProcessor().getSymbolDictionary().findSymbols(filters);
    }
    
    @Override
    protected boolean processMessage(Geomessage geomessage) {
        //Filter out messages that we just sent
        if (null != geomessage.getId() && geomessage.getId().equals(appConfigController.getUniqueId())) {
            return false;
        }
        
        String action = (String) geomessage.getProperty(Geomessage.ACTION_FIELD_NAME);
        Message message;
        if ("select".equalsIgnoreCase(action)) {
            message = MessageHelper.createSelectMessage(DictionaryType.Mil2525C,
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                    true);
        } else if ("un-select".equalsIgnoreCase(action)) {
            message = MessageHelper.createSelectMessage(DictionaryType.Mil2525C,
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                    false);
        } else if ("remove".equalsIgnoreCase(action)) {
            message = MessageHelper.createRemoveMessage(DictionaryType.Mil2525C,
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME));
        } else {
            ArrayList<Point> points = new ArrayList<Point>();
            String pointsString = (String) geomessage.getProperty(Geomessage.CONTROL_POINTS_FIELD_NAME);
            if (null != pointsString) {
                StringTokenizer tok = new StringTokenizer(pointsString, ";");
                while (tok.hasMoreTokens()) {
                    StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), ",");
                    try {
                        points.add(new Point(Double.parseDouble(tok2.nextToken()), Double.parseDouble(tok2.nextToken())));
                    } catch (Throwable t) {
                        Logger.getLogger(getClass().getName()).warning("Couldn't parse point from '" + pointsString + "'");
                    }
                }
            }
            message = MessageHelper.createUpdateMessage(DictionaryType.Mil2525C,
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                    points);
            message.setProperties(geomessage.getProperties());
            message.setID(geomessage.getId());
        }
        
        try {
            return _processMessage(message);
        } catch (RuntimeException re) {
            //This is probably a message type that the MessageProcessor type doesn't support
            Logger.getLogger(getClass().getName()).log(Level.FINER, "Couldn't process message: " + re.getMessage() + "\n"
                    + "\tIt is possible that this MessageProcessor doesn't handle messages of type "
                    + message.getProperty(MessageHelper.MESSAGE_2525C_TYPE_PROPERTY_NAME) + ".");
            return false;
        }
    }
    
    private boolean _processMessage(Message message) {
        final int layerCount = groupLayer.getLayers().length;
        
        /**
         * Workaround: ArcGIS Runtime 10.2.4 requires a chem light message to have
         * a "sic" field.
         */
        if (ChemLightController.REPORT_TYPE.equals(message.getProperty(MessageHelper.MESSAGE_2525C_TYPE_PROPERTY_NAME))
                && null == message.getProperty(Geomessage.SIC_FIELD_NAME)) {
            String sic = appConfigController.getSic();
            if (null == sic) {
                sic = "SFGPU----------";
            }
            message.setProperty(Geomessage.SIC_FIELD_NAME, sic);
        }
        
        boolean success = groupLayer.getMessageProcessor().processMessage(message);
        if (layerCount < groupLayer.getLayers().length) {
            toggleLabels();
        }
        return success;
    }
    
    @Override
    protected boolean processHighlightMessage(String geomessageId, String messageType, boolean highlight) {
        Message message = MessageHelper.createSelectMessage(DictionaryType.Mil2525C, geomessageId, messageType, highlight);
        return _processMessage(message);
    }

    @Override
    public String[] getMessageTypesSupported() {
        return groupLayer.getMessageProcessor().getMessageTypesSupported();
    }

    @Override
    public String getActionPropertyName() {
        return MessageHelper.MESSAGE_ACTION_PROPERTY_NAME;
    }

    public IdentifyResultList identify(float screenX, float screenY, int tolerance) {
        IdentifyResultList results = new IdentifyResultList();
        Layer[] layers = groupLayer.getLayers();
        List<Layer> layerList = new ArrayList<Layer>(Arrays.asList(layers));
        layerList.add(spotReportLayer);
        for (Layer layer : layerList) {
            if (layer instanceof GraphicsLayer) {
                IdentifyResultList theseResults = Mil2525CMessageLayer.identify((GraphicsLayer) layer, screenX, screenY, tolerance);
                for (int i = 0; i < theseResults.size(); i++) {
                    results.add(theseResults.get(i), layer);
                }
            }
        }
        return results;
    }
    
    /**
     * Identifies at most one Graphic in the specified layer within the specified tolerance.
     * @param layerName the layer name.
     * @param screenX the X value in pixels.
     * @param screenY the Y value in pixels.
     * @param tolerance the tolerance in pixels.
     * @return the Graphic in the specified layer within the specified tolerance that is closest
     *         to the point specified by screenX and screenY, or null if no such Graphic exists.
     */
    public Graphic identifyOneGraphic(String layerName, float screenX, float screenY, int tolerance) {
        Layer layer = SPOT_REPORT_LAYER_NAME.equals(layerName) ? spotReportLayer : groupLayer.getLayer(layerName);
        if (null != layer && layer instanceof GraphicsLayer) {
            GraphicsLayer gl = (GraphicsLayer) layer;
            int[] graphicIds = gl.getGraphicIDs(screenX, screenY, tolerance, 1);
            if (0 < graphicIds.length) {
                return gl.getGraphic(graphicIds[0]);
            }
        }
        return null;
    }

    @Override
    protected void processRemoveGeomessage(String geomessageId, String messageType) {
        Message message = MessageHelper.createRemoveMessage(DictionaryType.Mil2525C, geomessageId, messageType);
        _processMessage(message);
    }
    
    @Override
    protected void removeSpotReportGraphic(int graphicId) {
        spotReportLayer.removeGraphic(graphicId);
    }

    public void geomessageReceived(Geomessage geomessage) {
        processGeomessage(geomessage);
    }

    public void datagramReceived(String contents) {
        
    }
    
    @Override
    public void clearLayer(String layerName, boolean sendRemoveMessageForOwnMessages) {
        if (SPOT_REPORT_LAYER_NAME.equals(layerName)) {
            int[] graphicIds = spotReportLayer.getGraphicIDs();
            loopAndRemove(graphicIds, spotReportLayer, sendRemoveMessageForOwnMessages, true);
        }
        Layer layer = groupLayer.getLayer(layerName);
        if (layer instanceof GraphicsLayer) {
            GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
            int[] graphicIds = graphicsLayer.getGraphicIDs();
            loopAndRemove(graphicIds, graphicsLayer, sendRemoveMessageForOwnMessages, false);
        }
    }
    
    @Override
    public String[] getMessageLayerNames() {
        Layer[] layers = groupLayer.getLayers();
        String[] names = new String[layers.length + 1];
        for (int i = 0; i < layers.length; i++) {
            names[i] = layers[i].getName();
        }
        names[layers.length] = SPOT_REPORT_LAYER_NAME;
        return names;
    }
    
    @Override
    public String getMessageLayerName(String messageType) {
        if (null == messageType) {
            return null;
        }
        
        if (SpotReportController.REPORT_TYPE.equals(messageType)) {
            return SPOT_REPORT_LAYER_NAME;
        }
        
        File messageTypesDir = new File(symDictDir, "messagetypes");
        File[] files = messageTypesDir.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String filename) {
                return null != filename && filename.toLowerCase().endsWith(".json");
            }
        });
        for (File file : files) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(file));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while (null != (line = in.readLine())) {
                    sb.append(line);
                }
                JSONObject obj = new JSONObject(sb.toString());
                if (messageType.equals(obj.getString("type"))) {
                    return obj.getString("layerName");
                }
            } catch (Throwable t) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not read and parse " + file.getAbsolutePath(), t);
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not close file", e);
                    }
                }
            }
        }
        
        return null;
    }
    
    private void loopAndRemove(int[] graphicIds, GraphicsLayer graphicsLayer, boolean sendRemoveMessageForOwnMessages, boolean removeGraphics) {
        if (null != graphicIds) {
            for (int graphicId : graphicIds) {
                Graphic graphic = graphicsLayer.getGraphic(graphicId);
                removeGeomessage(graphic, sendRemoveMessageForOwnMessages);
                if (removeGraphics) {
                    graphicsLayer.removeGraphic(graphicId);
                }
            }
        }
    }
    
    private void removeGeomessage(Graphic graphic, boolean sendRemoveMessageForOwnMessages) {
        final String geomessageId = (String) graphic.getAttributeValue(Geomessage.ID_FIELD_NAME);
        final String geomessageType = (String) graphic.getAttributeValue(Geomessage.TYPE_FIELD_NAME);
        String uniqueDesignation = (String) graphic.getAttributeValue("uniquedesignation");
        if (sendRemoveMessageForOwnMessages && null != uniqueDesignation && uniqueDesignation.equals(messageController.getSenderUsername())) {
            new Thread() {
                public void run() {
                    try {
                        sendRemoveMessage(messageController, geomessageId, geomessageType);
                    } catch (Throwable t) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Couldn't send REMOVE message", t);
                    }
                }
            }.start();
        } else {
            processRemoveGeomessage(geomessageId, geomessageType);
        }
    }

}
