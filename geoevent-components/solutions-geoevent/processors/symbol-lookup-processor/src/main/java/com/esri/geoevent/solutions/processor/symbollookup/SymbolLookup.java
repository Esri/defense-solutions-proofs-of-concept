/*
 /*
 | Copyright 2013 Esri
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
package com.esri.geoevent.solutions.processor.symbollookup;

// TODO: PUT BACK IN !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//package com.esri.geoevent.solutions.processor.symbollookup;
// TODO: PUT BACK IN !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolLookup {

	public static String NOT_FOUND = "NOT FOUND"; 
	private static Map<String, MilitarySymbol> sidc2SymbolProps = new HashMap<String, MilitarySymbol>();
	private static Map<String, MilitarySymbol> symbolName2SymbolProps = new HashMap<String, MilitarySymbol>();
	private static boolean initialized = false;
		
	private static List<MilitarySymbol> symbolList = new ArrayList<MilitarySymbol>(); 
	
	public SymbolLookup() 
	{
		initialize();
	}
	
	private void initialize()
	{
		// we only want to do this once
		if (initialized)
			return;
		
		initialized = true;
		
		System.out.println("Initializing SymbolLookup");
				
		try {
// TODO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// TODO: figure out where this file is deployed:	
			
			InputStream csvFile = getClass().getResourceAsStream("/SymbolInfo2525C.csv") ;
// TODO: !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			
			
	            
			BufferedReader br = new BufferedReader(new InputStreamReader(csvFile));	
			String line = "";
			String splitBy = ",";		 
		 		 	
			while ((line = br.readLine()) != null) {
				 
				String[] fields = line.split(splitBy);
	 
				if (fields.length < 5)
					continue;
				
				String name = fields[0];
				String sidc = fields[1];
				String geometryType = fields[4];

				MilitarySymbol newSymbol = new MilitarySymbol(name, sidc, geometryType);

				if (newSymbol.isValidSymbol()) {
					symbolList.add(newSymbol);
				}	 
			}
	 
			br.close();
			
			for (MilitarySymbol s : symbolList )
            {
                String name = s.getName();
                              
                String symbolId = s.getSymbolId();
                
                // @SuppressWarnings("unused")
				// String geoType = s.getValues().get("GeometryConversionType");

                // IMPORTANT: note toUpperCase() - all keys will be upper case
                if (!sidc2SymbolProps.containsKey(symbolId))
                	sidc2SymbolProps.put(symbolId.toUpperCase(), s);
                // TODO: decide if a warning should be issued for these repeats 
                // else
                //	System.out.println(symbolId + " - already in table, can't add");                 
                
                if (!symbolName2SymbolProps.containsKey(name))
                	symbolName2SymbolProps.put(name.toUpperCase(), s);
                // TODO: decide if a warning should be issued for these repeats 
                // else
                //	System.out.println(name + " - already in table, can't add");                                

                // To see symbols list/table:
                // System.out.println(name + ":" + symbolId + ":" + geoType);
            }
			
			System.out.println("Initialization complete: unique symbols found=" + sidc2SymbolProps.size());

		} catch (IOException e) {
			// Should not happen
			e.printStackTrace();
		}
		
	}
	
	public String symbolIdToName(String symbolId) 
	{
		String symbolName = NOT_FOUND; 
		
		if ((symbolId == null) || symbolId.isEmpty())
			return symbolName;
		
		String maskedSymbolId = getMaskedSymbolId(symbolId);

        if (!sidc2SymbolProps.containsKey(maskedSymbolId))
        {
        	// try again with "F"/friendly version (some only have this version)
        	maskedSymbolId = getMaskedSymbolId(symbolId, "F");
        	
        	if (!sidc2SymbolProps.containsKey(maskedSymbolId))
        	{
	        	System.out.println(maskedSymbolId + "-not found in table, can't continue");
	        	return symbolName;
        	}
        }
        
        MilitarySymbol sp = sidc2SymbolProps.get(maskedSymbolId);
        
        if (sp == null) // should not happen
        	return symbolName;
        
        symbolName = sp.getName();
				
		return symbolName;
	}
	
	public String symbolNameToId(String symbolName) 
	{	
		String symbolId = NOT_FOUND;
		
		if ((symbolName == null) || symbolName.isEmpty())
			return symbolId;

		String lookupSymbolName = symbolName.toUpperCase();
		
		if (lookupSymbolName.contains("~"))
		{
			lookupSymbolName = lookupSymbolName.split("~")[0];
		}		
		
		try 
		{
	        if (!symbolName2SymbolProps.containsKey(lookupSymbolName))
	        {
	        	lookupSymbolName = lookupSymbolName + " F";
	        	
	        	if (!symbolName2SymbolProps.containsKey(lookupSymbolName))
	        	{
	        		System.out.println(symbolName + "-not found in table, can't continue");
	        		return symbolId;
	        	}
	        }
	        
	        MilitarySymbol sp = symbolName2SymbolProps.get(lookupSymbolName);
	        
	        if (sp == null) // should not happen
	        	return symbolId;
	        
	        symbolId = sp.getSymbolId();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return symbolId;		
	}	
	
	private String getMaskedSymbolId(String symbolId, String affiliation)
	{
		if (symbolId.length() < 15)
			return symbolId;
		
		StringBuilder sb = new StringBuilder();
		
		Character codingScheme = symbolId.charAt(0);
		
		sb.append(codingScheme);
		sb.append(affiliation);
		sb.append(symbolId.charAt(2));
		sb.append('P');
				
		for (int i = 4; i<10; i++)
			sb.append(symbolId.charAt(i));
		
		for (int i = 10; i<14; i++)
			sb.append('-');
		
		if (codingScheme == 'G')			
			sb.append('X');
		else 
			sb.append('-');
					
		return sb.toString().toUpperCase();		
	}
	
	private String getMaskedSymbolId(String symbolId)
	{
		if ((symbolId == null) || (symbolId.length() < 15))
			return symbolId;
		
		String affiliation = getAffiliationChar(symbolId);		
					
		return getMaskedSymbolId(symbolId, affiliation);
	}
	
	private String getAffiliationChar(String symbolId)
	{		
        Character ch = symbolId.toUpperCase().charAt(1);
        Character affilCh = ch;
        
        switch (ch) 
        {
        	// Simple cases (no change of affil char)
        	case 'F':
        	case 'H':
        	case 'U':
        	case 'N':
                affilCh = ch;
                break;
           	// Friendlies
        	case 'M':
        	case 'A':
        	case 'D':
        	case 'J':
        	case 'K':
                affilCh = 'F';
                break;
            // Hostile
           	case 'S':
                affilCh = 'H';
                break;   
            // Neutral
           	case 'L':
                affilCh = 'N';
                break;     
            // Unknown
        	case 'P':
        	case 'G':
        	case 'W':
        	case '-':
        	default :
                affilCh = 'U';
                break;                        
        }

        return affilCh.toString();
	}
            			
}