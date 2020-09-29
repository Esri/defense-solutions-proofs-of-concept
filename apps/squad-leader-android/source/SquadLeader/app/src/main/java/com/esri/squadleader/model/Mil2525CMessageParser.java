/*******************************************************************************
 * Copyright 2015 Esri
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

package com.esri.squadleader.model;

import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageHelper;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * A parser for MIL-STD-2525C messages in XML. The easiest thing to do is to call
 * parseMessages. But you can also use it as a handler with a SAXParser if desired.
 */
public class Mil2525CMessageParser extends com.esri.militaryapps.model.Mil2525CMessageParser {

    private final ArrayList<Message> messages = new ArrayList<Message>();
    private Message message = null;

    public Mil2525CMessageParser() throws ParserConfigurationException, SAXException {
        super();
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    protected void newMessage() {
        message = new Message();
        messages.add(message);
    }

    @Override
    protected void setMessageId(String id) {
        message.setID(id);
    }

    @Override
    protected void setMessageProperty(String key, Object value) {
        message.setProperty(key, value);
    }

    @Override
    protected String getMessageIdPropertyName() {
        return MessageHelper.MESSAGE_ID_PROPERTY_NAME;
    }

    @Override
    public void clearMessages() {
        messages.clear();
    }

}
