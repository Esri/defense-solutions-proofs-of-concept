/*
 | Copyright 2014 Esri
 |
 | Licensed under the Apache License, Version 2.0 (the "License");
 | you may not use this file except in compliance with the License.
 | You may obtain a copy of the License at
 |
 |    http://www.apache.org/licenses/LICENSE-2.0
 |
 | Unless required by applicable law or agreed to in writing, software
 | distributed under the License is distributed on an "AS IS" BASIS,
 | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 | See the License for the specific language governing permissions and
 | limitations under the License.
 */
package com.esri.geoevent.solutions.adapter.regexText;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.adapter.text.TextInboundAdapter;
import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.core.component.ComponentException;


public class RegexTextInboundAdapter extends TextInboundAdapter
{
	// This class enables an attribute separator to be defined as a regular expression (which the base class does not handle).
	// It swaps all matches of this regular expression in the incoming stream with a single-character delimiter (which the base class will handle)
	// It then passes the altered stream to the base class to handle as normal
	static final private Log log = LogFactory.getLog(RegexTextInboundAdapter.class);
	private String attributeSeparator;
	protected static String ATTRIBUTE_SEPARATOR_TO_BE_REPLACED_PROPERTY_NAME = "AttributeSeparatorToBeReplaced";
	private String separatorToBeReplaced = "";
	private static Charset charset = Charset.forName("UTF-8");
	
	public RegexTextInboundAdapter(AdapterDefinition adapterDefinition)
			throws ComponentException
	{
		super(adapterDefinition);
	}

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();
		// grab references to two properties in the definition that will be used to adapt the incoming bytes
		if( hasProperty(ATTRIBUTE_SEPARATOR_TO_BE_REPLACED_PROPERTY_NAME) )
			separatorToBeReplaced = (String)getProperty(ATTRIBUTE_SEPARATOR_TO_BE_REPLACED_PROPERTY_NAME).getValue();
		if( hasProperty(ATTRIBUTE_SEPARATOR_PROPERTY_NAME) )
			attributeSeparator = (String)getProperty(ATTRIBUTE_SEPARATOR_PROPERTY_NAME).getValue();
	}
	
	@Override
	public void receive(ByteBuffer buf, String str)
	{
		ByteBuffer bb = buf;
		// Check first whether we need to do anything different from the out-of-the-box TextAdapter
		if(StringUtils.isNotBlank(separatorToBeReplaced))
		{
			// Decode the buffer
			CharSequence charseq = decode(buf);
			// Replace the attribute separator with a simple one
			log.debug("Replacing separator '" + separatorToBeReplaced + "' with '" + attributeSeparator + "'");
			Pattern p = Pattern.compile(separatorToBeReplaced);
			Matcher m = p.matcher(charseq);
			String newString = m.replaceAll(attributeSeparator);
			// Put up some debugging info
			if (newString.length() >= 300)
				log.debug(newString.substring(0, 300) + "...---..." + newString.substring(newString.length() - 300));
			else
				log.debug(newString);
			// Re-encode the string into a byte buffer
			bb = encode(newString);
		}
		// Let the base class now handle the input as normal
		super.receive(bb, str);
	}
	
	private CharSequence decode(ByteBuffer buf)
	{
		// Decode a byte buffer into a CharBuffer
		CharBuffer isodcb = null;
		CharsetDecoder isodecoder = charset.newDecoder();
		try {
			isodcb = isodecoder.decode(buf);
		} catch (CharacterCodingException e) {
			log.error(e);
		}
		return (CharSequence)isodcb;
	}
	
	private ByteBuffer encode(String str)
	{
		// Encode a string into a byte buffer
		ByteBuffer bb = null;
		CharsetEncoder isoencoder = charset.newEncoder();
		try {
			bb = isoencoder.encode(CharBuffer.wrap(str));
		} catch (CharacterCodingException e) {
			log.error(e);
		}
		return bb;
	}
}