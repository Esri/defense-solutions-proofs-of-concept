/*******************************************************************************
 * Copyright 2013-2017 Esri
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
package com.esri.squadleader.controller;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageGroupLayer;
import com.esri.core.symbol.advanced.MessageHelper;
import com.esri.core.symbol.advanced.SymbolDictionary;
import com.esri.militaryapps.controller.ChemLightController;
import com.esri.militaryapps.controller.MessageController;
import com.esri.militaryapps.controller.SpotReportController;
import com.esri.militaryapps.model.Geomessage;
import com.esri.squadleader.util.Utilities;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;

/**
 * A controller for ArcGIS Runtime advanced symbology. Use this class when you want to use
 * MessageGroupLayer, MessageProcessor, SymbolDictionary, and MIL-STD-2525C symbols.
 */
public class AdvancedSymbolController extends com.esri.militaryapps.controller.AdvancedSymbolController {

    private static final String TAG = AdvancedSymbolController.class.getSimpleName();

    public static final String SPOT_REPORT_LAYER_NAME = "Spot Reports";

    private final MapController mapController;
    private final MessageGroupLayer groupLayer;
    private final GraphicsLayer spotReportLayer;
    private final Symbol spotReportSymbol;
    private final MessageController messageController;
    private final File symDictDir;

    /**
     * Creates a new AdvancedSymbolController. IMPORTANT: this method requires WRITE_EXTERNAL_STORAGE permission.
     *
     * @param mapController           the application's MapController.
     * @param assetManager            the application's AssetManager, from which the advanced symbology database
     *                                will be copied.
     * @param symbolDictionaryDirname the name of the asset directory that contains the advanced symbology
     *                                database.
     * @param spotReportIcon          the Drawable for putting spot reports on the map.
     * @param messageController       a MessageController, for sending updates when messages are to be removed,
     *                                e.g. in clearLayer or clearAllMessages.
     * @throws IOException if the advanced symbology database is absent or corrupt or cannot be written
     *                     to the device.
     */
    public AdvancedSymbolController(
            MapController mapController,
            AssetManager assetManager,
            String symbolDictionaryDirname,
            Drawable spotReportIcon,
            MessageController messageController) throws IOException {
        super(mapController);
        this.mapController = mapController;
        symDictDir = copySymbolDictionaryToDisk(assetManager, symbolDictionaryDirname);

        spotReportLayer = new GraphicsLayer();
        spotReportLayer.setName(SPOT_REPORT_LAYER_NAME);
        mapController.addLayer(spotReportLayer);

        groupLayer = new MessageGroupLayer(SymbolDictionary.DictionaryType.MIL2525C, symDictDir.getAbsolutePath());
        mapController.addLayer(groupLayer);

        spotReportSymbol = new PictureMarkerSymbol(spotReportIcon);

        this.messageController = messageController;
    }

    /**
     * Copies the MIL-STD-2525C symbol dictionary from assets to the device's downloads directory if
     * it is not already there.
     *
     * @param assetManager            the application's AssetManager, from which the advanced symbology database
     *                                will be copied.
     * @param symbolDictionaryDirname the name of the asset directory that contains the advanced symbology
     *                                database.
     * @return the symbol dictionary directory. This may be freshly copied or it might have already
     * been there.
     * @throws IOException if the symbol dictionary cannot be copied to disk.
     */
    public static File copySymbolDictionaryToDisk(AssetManager assetManager, String symbolDictionaryDirname) throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        File symDictDir = new File(downloadsDir, symbolDictionaryDirname);

        boolean copyNeeded = !symDictDir.exists();
        if (!copyNeeded) {
            /**
             * Check to see if we need to upgrade the symbol dictionary. One way is to
             * see if PositionReport.json's renderer is of type 2525C (10.2) or mil2525c (10.2.4).
             */
            StringBuilder sb = new StringBuilder();
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(new File(symDictDir, "messagetypes/PositionReport.json")));
                String line;
                while (null != (line = in.readLine())) {
                    sb.append(line);
                }
                JSONObject json = new JSONObject(sb.toString());
                if (!"mil2525c".equals(json.getJSONObject("renderer").getString("dictionaryType"))) {
                    copyNeeded = true;
                }
            } catch (Exception e) {
                copyNeeded = true;
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (Throwable t) {
                        //Swallow
                    }
                }
            }
        }
        if (copyNeeded) {
            symDictDir.delete();
            Utilities.copyAssetToDir(assetManager, symbolDictionaryDirname, downloadsDir.getAbsolutePath());
        }
        return symDictDir;
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
            Log.e(TAG, "Could not parse spot report", nfe);
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

    @Override
    protected boolean processMessage(Geomessage geomessage) {
        String action = (String) geomessage.getProperty(Geomessage.ACTION_FIELD_NAME);
        Message message;
        if (MessageHelper.MESSAGE_ACTION_VALUE_HIGHLIGHT.equalsIgnoreCase(action)) {
            message = MessageHelper.create2525CHighlightMessage(
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                    true);
        } else if (MessageHelper.MESSAGE_ACTION_VALUE_UNHIGHLIGHT.equalsIgnoreCase(action)) {
            message = MessageHelper.create2525CHighlightMessage(
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                    false);
        } else if (MessageHelper.MESSAGE_ACTION_VALUE_REMOVE.equalsIgnoreCase(action)) {
            message = MessageHelper.create2525CRemoveMessage(
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME));
        } else {
            message = MessageHelper.create2525CUpdateMessage(
                    geomessage.getId(),
                    (String) geomessage.getProperty(Geomessage.TYPE_FIELD_NAME),
                    true);
            message.setProperties(geomessage.getProperties());
            message.setID(geomessage.getId());
        }

        return _processMessage(message);
    }

    private boolean _processMessage(Message message) {
        /**
         * Workaround: ArcGIS Runtime 10.2.4 requires a chem light message to have
         * a "sic" field.
         * TODO remove workaround when BUG-000085707 is fixed in Runtime
         */
        if (ChemLightController.REPORT_TYPE.equals(message.getProperty(MessageHelper.MESSAGE_TYPE_PROPERTY_NAME))
                && null == message.getProperty(Geomessage.SIC_FIELD_NAME)) {
            String sic = "SFGPU----------";
            message.setProperty(Geomessage.SIC_FIELD_NAME, sic);
        }

        return groupLayer.getMessageProcessor().processMessage(message);
    }

    @Override
    protected boolean processHighlightMessage(String geomessageId, String messageType, boolean highlight) {
        Message message = MessageHelper.create2525CHighlightMessage(geomessageId, messageType, highlight);
        return _processMessage(message);
    }

    @Override
    public String[] getMessageTypesSupported() {
        return groupLayer.getMessageProcessor().getMessageTypesSupported();
    }

    @Override
    public String getActionPropertyName() {
        return MessageHelper.MESSAGE_ACTION_PROPERTY_NAME.toLowerCase(Locale.getDefault());
    }

    @Override
    protected void processRemoveGeomessage(String geomessageId, String messageType) {
        Message message = MessageHelper.create2525CRemoveMessage(geomessageId, messageType);
        _processMessage(message);
    }

    @Override
    protected void removeSpotReportGraphic(int graphicId) {
        spotReportLayer.removeGraphic(graphicId);
    }

    @Override
    protected void toggleLabels() {
        /**
         * TODO this is the right way to toggle the labels, but there is a bug in the Runtime SDK for
         * Android that causes it not to work (NIM102986). When that bug is fixed, uncomment this code
         * and test it.
         */
//        Layer[] layers = groupLayer.getLayers();
//        for (Layer layer : layers) {
//            GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
//            if (graphicsLayer.getRenderer() instanceof DictionaryRenderer) {
//                DictionaryRenderer dictionaryRenderer = (DictionaryRenderer) graphicsLayer.getRenderer();
//                dictionaryRenderer.setLabelsVisible(isShowLabels());
//                graphicsLayer.setRenderer(dictionaryRenderer);
//            }
//        }
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

        File messageTypesDir = new File(symDictDir, "MessageTypes");
        File[] files = messageTypesDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return null != filename && filename.toLowerCase(Locale.getDefault()).endsWith(".json");
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
                Log.e(TAG, "Could not read and parse " + file.getAbsolutePath(), t);
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close file", e);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void clearLayer(String layerName, boolean sendRemoveMessageForOwnMessages) {
        if (SPOT_REPORT_LAYER_NAME.equals(layerName)) {
            int[] graphicIds = spotReportLayer.getGraphicIDs();
            loopAndRemove(graphicIds, spotReportLayer, sendRemoveMessageForOwnMessages, true);
        }
        Layer[] layers = groupLayer.getLayers(layerName);
        for (Layer layer : layers) {
            if (layer instanceof GraphicsLayer) {
                GraphicsLayer graphicsLayer = (GraphicsLayer) layer;
                int[] graphicIds = graphicsLayer.getGraphicIDs();
                loopAndRemove(graphicIds, graphicsLayer, sendRemoveMessageForOwnMessages, false);
            }
        }
    }

    private void loopAndRemove(int[] graphicIds, GraphicsLayer graphicsLayer, boolean sendRemoveMessageForOwnMessages, boolean removeGraphics) {
        if (null != graphicIds) {
            for (int graphicId : graphicIds) {
                Graphic graphic = graphicsLayer.getGraphic(graphicId);
                removeGeomessage(graphic, sendRemoveMessageForOwnMessages);
            }
            if (removeGraphics) {
                graphicsLayer.removeGraphics(graphicIds);
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
                        Log.e(TAG, "Couldn't send REMOVE message", t);
                    }
                }
            }.start();
        } else {
            processRemoveGeomessage(geomessageId, geomessageType);
        }
    }

    @Override
    public void clearAllMessages(boolean sendRemoveMessageForOwnMessages) {
        super.clearAllMessages(sendRemoveMessageForOwnMessages);
        clearLayer(spotReportLayer.getName(), sendRemoveMessageForOwnMessages);
    }

    /**
     * Identifies at most one Graphic in the specified layer within the specified tolerance.
     *
     * @param layerName the layer name.
     * @param screenX   the X value in pixels.
     * @param screenY   the Y value in pixels.
     * @param tolerance the tolerance in pixels.
     * @return the Graphic in the specified layer within the specified tolerance that is closest
     * to the point specified by screenX and screenY, or null if no such Graphic exists.
     */
    public Graphic identifyOneGraphic(String layerName, float screenX, float screenY, int tolerance) {
        Layer[] layerList = groupLayer.getLayers(layerName);
        if (SPOT_REPORT_LAYER_NAME.equals(layerName)) {
            Layer[] newLayerList = new Layer[layerList.length + 1];
            System.arraycopy(layerList, 0, newLayerList, 1, layerList.length);
            newLayerList[0] = spotReportLayer;
            layerList = newLayerList;
        }
        for (Layer layer : layerList) {
            if (layer instanceof GraphicsLayer) {
                GraphicsLayer gl = (GraphicsLayer) layer;
                int[] graphicIds = gl.getGraphicIDs(screenX, screenY, tolerance, 1);
                if (0 < graphicIds.length) {
                    return gl.getGraphic(graphicIds[0]);
                }
            }
        }
        return null;
    }

}
