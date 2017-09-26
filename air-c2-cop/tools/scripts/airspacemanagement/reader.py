# -*- coding: utf-8 -*-

###############################################################################
# Authors: Shaun Nicholson, Esri UK, May 2015
#          Anthony Giles, Helyx SIS, Feb 2016
#
# (C) Copyright ESRI (UK) Limited 2011. All rights reserved
# ESRI (UK) Ltd, Millennium House, 65 Walton Street, Aylesbury, HP21 7QG
# Tel: +44 (0) 1296 745500  Fax: +44 (0) 1296 745544
###############################################################################

import arcpy
from arcpy import env
import sys
import os
import os.path
import inspect
import time
import datetime
import logging
from datetime import datetime
import copy
import json

import config
import utils.common
import airspacemanagement.parser

###########################################################################
# Top level class to read an ACO file and process the various blocks.
#
###########################################################################
class ACOReader:

    _sourceFile = None
    _targetWS   = None

    def __init__(self, sourceFile, targetWS):
        self._sourceFile    = sourceFile
        self._targetWS      = targetWS
        pass

    def execute(self):
        utils.common.OutputMessage(logging.DEBUG, "{0} ACOReader.execute() - Start".format(time.ctime()))

        fileParser = airspacemanagement.parser.FileParser(self._sourceFile)
        records = fileParser.execute()

        procHeader = ProcessACOHeader()
        procHeader.processBlock(records)

        procGeometry = ProcessGeometry()
        procGeometry.processBlock(records, procHeader.getYear())

        json = {}
        json.update(procHeader.getJson())
        json.update(procGeometry.getJson())
        json.update({'metadata':{'filename':self._sourceFile}})

        utils.common.OutputMessage(logging.DEBUG, "{0} ACOReader.execute() - Finish".format(time.ctime()))

        return json

###########################################################################
# Top level class to read an ATO file and process the various blocks.
#
###########################################################################
class ATOReader:

    _sourceFile = None
    _targetWS   = None

    def __init__(self, sourceFile, targetWS):
        self._sourceFile    = sourceFile
        self._targetWS      = targetWS
        pass

    def execute(self):
        utils.common.OutputMessage(logging.DEBUG, "{0} ATOReader.execute() - Start".format(time.ctime()))

        fileParser = airspacemanagement.parser.FileParser(self._sourceFile)
        records = fileParser.execute()

        procHeader = ProcessATOHeader()
        procHeader.processBlock(records)

        procGeometry = ProcessATOBlocks()
        procGeometry.processBlock(records, procHeader.getYear())

        json = {}
        json.update(procHeader.getJson())
        json.update(procGeometry.getJson())
        json.update({'metadata':{'filename':self._sourceFile}})

        utils.common.OutputMessage(logging.DEBUG, "{0} ATOReader.execute() - Finish".format(time.ctime()))

        return json

###########################################################################
# Class to process the ACO header.
# Process the block types:
#   EXER       - example: EXER /<exercise_nickname>/<exercise_additional_identifier>//
#                         EXER/OFF 450 FTU TRAINING/-//
#
#              <exercise_nickname> mandatory, 1−56 X
#              <exercise_additional_identifier> optional 4−16 AB
#               
#   MSGID      - example:  MSGID/<msgid>/<originator>/<message_serial_number>/<month_name>/<qualifier>/<serial_number_of_qualifier>//
#                          MSGID/ACO/C2WS/705401/FEB/CHG/02//
#                          MSGID/ACO/CAOC2/−/−/CHG/1//
#                          MSGID/ACO/CAOC2//
#
#              <msgid> mandatory, 3−20X. "ACO" is printed if it is an ACO. A "−" is printed if it is not specified
#              <originator> mandatory, 1−30X.  A "−" is printed if it is not specified
#              <message_serial_number> optional, 1−7N. It will either not be printed if the following fields are not specified, or a "−" will be printed if one or more of the following fields are specified.
#              <month_name> optional, 3A. It will either not be printed if the following fields are not specified, or a "−" will be printed if one or more of the following fields are specified.
#              <qualifier> optional, 3A. If it is not specified it will either not be printed if also the following fields are not specified, or a "−" will be printed if one or more of the following fields are specified.
#              <serial_number_of_qualifier> optional, 1−3N. It will not be printed if it is not specified
# 
#   AMPN       - example:  AMPN/<classification>/<aco_header>//
#                          AMPN/NU/ACO HEADER/
#                         
#              <classification> mandatory, 1−20X. A "−" is printed if it is not specified
#              <aco_header> mandatory, 1−99X. A "−" is printed if no header is specified
#
#   ACOID      - example: ACOID/<name_of_area_of_validity>/<aco_serial_number>//
#                         ACOID/COBRA GOLD AO/23B//
#
#              <name_of_area_of_validity> mandatory, 2−8X. A "−" is printed if it is not specified. If the name is longer than 8chars it will be truncated.
#              <aco_serial_number> optional, 2−3AN. It will not be printed if it is not specified
#
#   GEODATUM  - example:  GEODATUM/ <geodetic_datum>//
#                         GEODATUM/W84//
#
#                <geodetic_datum> mandatory, 3−25X. "W84" is printed if it is not specified
#
#   PERIOD    - example:  PERIOD/<start_day_time>/<stop_day_time>/<day_time>/
#                         PERIOD/230600FEB2007/240559FEB2007//
#              <start_day_time> mandatory, 7−14AN. It is the start validity time of the ACO. A "−" is printed if it is not specified.
#              <stop_day_time> mandatory, 3−14AN. It is the stop validity time. 
#              <day_time> conditional, 7−14AN.  if a stop qualifier is specified and is one of the following: "AFTER", "ASOF", "ASAPAFT", "ASAPNLT", "BEFORE", "NET", "NLT".
#
#   OPER       - example: OPER/<operation_codeword>/<plan_originator_and_number>/<option_nickname>/<secondary_option_nickname>//
#                         OPER/BALKAN/SACEUR 106/PAPER WASTE/ORANGE//
#                         OPER/DENY FLIGHT/−/PAPER WASTE//
#                         OPER/DENY FLIGHT/
#
#              <operation_codeword> mandatory, 1−32AB
#              <plan_originator_and_number> optional, 5−36X
#              <option_nickname> optional, 1−23X
#              <secondary_option_nickname> optional, 1−23X
#              
###########################################################################

class ProcessACOHeader:

    _json = {}

    def __init__(self):
        self._json = {}
        pass

    def processBlock(self, records):
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessACOHeader.processBlock() - Start".format(time.ctime()))

        for record in records:
            if record.startswith('EXER'):
                self._json['EXER'] = airspacemanagement.parser.parseEXER(record)
            elif record.startswith('OPER'):
                self._json['OPER'] = airspacemanagement.parser.parseOPER(record)
            elif record.startswith('MSGID'):
                self._json['MSGID'] = airspacemanagement.parser.parseMSGID(record)
            elif record.startswith('AMPN'):
                self._json['AMPN'] = airspacemanagement.parser.parseAMPN(record)
            elif record.startswith('ACOID'):
                self._json['ACOID'] = airspacemanagement.parser.parseACOID(record)
            elif record.startswith('GEODATUM'):
                self._json['GEODATUM'] = airspacemanagement.parser.parseGEODATUM(record)
            elif record.startswith('PERIOD'):
                self._json['PERIOD'] = airspacemanagement.parser.parsePERIOD(record)
            #Need to add GENTEXT
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessACOHeader.processBlock() - Finish".format(time.ctime()))

        pass

    def getJson(self):
        return { 'header': self._json }

    def getYear(self):
        return self._getDate(self._json['PERIOD']['period1']).year

    def _getDate(self, timeString):
        return datetime.strptime(timeString, "%Y/%m/%d %H:%M:00")

###########################################################################
# Class to process an ACMID block.
# Process the block types:
#   ACMID      -  example: ACMID/<airspace_control_means>/<airspace_control_means_identifier>/<type_of_airspace_shape>/ <airspace_usage>*//
#                          ACMID/ACM:REFPT/DESIG:RP1/CORRIDOR/USE:AIRCOR//
#
#              <airspace_control_means> mandatory, 2−6AN. It is the type of airspace control means
#              <airspace_control_means_identifier> mandatory, 1−30X. It is the identifier of the ACM
#              <type_of_airspace_shape> mandatory, 4−8A
#              <airspace_usage>  mandatory, repeatable, 2−6AN
#
#
#   POLYGON    - example: POLYGON/<polygon_points>*//
#                         POLYGON/LATM:2037N05943E/LATS:204400N0594300E/LATM:2048N05982E/LATM:2037N05943E//
#
#              <polygon_points> mandatory, repeatable, 11−15AN. It is the coordinates of the each point. It is prefixed by "LATS:" or "LATM:" depending on the precision of the coordinates.
#
#   CIRCLE     - example: CIRCLE/<circle_center>/<radius>//
#                         CIRCLE/LATS:151000N0590900E/150KM//
#
#              <circle_center> mandatory, 11−15AN. It is the coordinates of the center point of the CIRCLE. It is prefixed by "LATM:" or "LATS:" depending on the precision of the coordinates.
#              <radius> mandatory, 2−7ANS. It is the radius of the CIRCLE. A "−" is printed if it is not specified
#
#   CORRIDOR   - example: CORRIDOR/<width>/<position_or_point>*//
#                CORRIDOR/5NM/LATS:151000N0590900E/LATM:1610N06010E//
#
#              <width> mandatory, 2−7ANS. It is the width of the CORRIDOR.
#              <position_or_point> mandatory, repeatable, 1−15AN. It is the coordinates of each point of the CORRIDOR. It is prefixed by "LATM:" or "LATS:" depending on the precision of the coordinates.
#
#
#   GEOLINE    - example: GEOLINE/<position_or_point>*//
#                         GEOLINE/LATM:2037N05943E/LATS:204400N0594300E/LATM:2048N05982E//
#
#              <position_or_point> mandatory, repeatable, 11−15AN. It is the coordinates of the each point taken. It is prefixed by "LATS:" or "LATM:" depending on the precision of the coordinates
#
#   APOINT     - example: APOINT/<airspace_point>//
#                         APOINT/LATS:151000N0590900E//
#
#              <airspace_point> mandatory, 11−15AN. It is the coordinates of the POINT. It is prefixed by "LATM:" or "LATS:" depending on the precision of the coordinates.
#
#   EFFLEVEL   - example: EFFLEVEL/<vertical_dimension>//
#                         EFFLEVEL/FLFL:FL100−FL230//
#
#              <vertical_dimension> mandatory, 8−15ANS. It is the minimum and maximum dimension of the current shape. It is prefixed by "BRRA:" if the minimum altitude is a base reference point and maximum altitude is relative. #              It is prefixed by "BRFL:" if the minimum altitude is a base reference point and maximum altitude is a flight level. It is prefixed by "RAFL:" if the minimum altitude is relative and the maximum altitude is a #              flight level. It is prefixed by "FLFL:" if both altitudes are flight levels. It is prefixed by "RARA:" if both altitudes are relative.
#
#   APERIOD    - example: APERIOD/<airspace_time_mode>/<day_time_month_of_start>/<stop_time>/<interval_frequency>/<interval_duration>//
#                         APERIOD/DISCRETE/141530ZFEB/141730ZFEB//
#
#              <airspace_time_mode> mandatory, 8A. The legal values are "DISCRETE" and "INTERVAL".
#              <day_time_month_of_start> mandatory, 10AN. It is the start validity time. The format is "DDHHMMZMMM". A "−" is printed if it is not specified
#              <stop_time> mandatory 3−10AN. It is the end validity time. The format is "DDHHMMZMMM". It can also be one of the following values: "AFTER", "ASOF", "ASAP", "ASAPAFT", "ASAPNLT", "BEFORE", "INDEF", "NET", "NLT", #              "ONCALL", "UFN", "UNK", "TBD". "UNK" is printed if it is not specified.
#              <interval_frequency> conditional, 5−18AB. If the time mode is INTERVAL. The legal values are "BIWEEKLY", "DAILY", "YEARLY", "MONTHLY BY DATE", "MONTHLY BY WEEKDAY", "WEEKDAYS", "MON WED FRI", "TUE THUR", #              "WEEKENDS". A "−" is printed if it is not specified.
#              <interval_duration> conditional, 3−14AN. If the time mode is INTERVAL, it is the interval duration. It can be specified in term of days, weeks, bi−weeks, months, years. It can also be specified exactly with the #              format DDHHMMZMMMYYYY. It can also have the value UFN (Until Further Notice). A "−" is printed if it is not specified
#
#   To do: 
#
#   AORBIT     - example: AORBIT/<first_point_of_orbit>/<second_point_of_orbit>/<width>/<orbit_alighment>//
#                         AORBIT/LATS:151000N0590900E/LATM:1610N06010E/235KM/C//
#              <first_point_of_orbit> mandatory, 11−15AN. It is the coordinates of the first point of the ORBIT. It is prefixed by "LATM:" or "LATS:" depending on the precision of the coordinates.
#              <second_point_of_orbit> mandatory, 11−15AN. It is the coordinates of the second point of the ORBIT. It is prefixed by "LATM:" or "LATS:" depending on the precision of the coordinates.
#              <width> mandatory, 2−7ANS. It is the width of the ORBIT.
#              <orbit_alignment> mandatory, 1A. It is the orbit alignment. The legal values are "L" (left), "C" (center), and "R" (right). "C" is printed if it is not specified
#
#   RADARC     - example: RARARC/<bearing_orgin>/<beginning_radial_bearing>/<ending_radial_bearing>/<inner_radius>/<outer_radius>//
#                         RADARC/151000N0590900E/170T/050T/150KM/350KM//
#
#              <bearing_orgin> mandatory, 11−15AN. It is the origin point of the RADARC. A "−" is printed if it is not specified.
#              <beginning_radial_bearing> mandatory, 4AN. It is the beginning angle measured clockwise from true north. It is follow by the letter T (True North). A "−" is printed if it is not specified.
#              <ending_radial_bearing> mandatory, 4AN.  It is the ending angle measured clockwise from true north. It is follow by the letter T (True North). A "−" is printed if it is not specified.
#              <inner_radius> mandatory, 2−7ANS. It is the inner radius of the RADARC. A "−" is printed if it is not specified.
#              <outer_radius> mandatory, 2−7ANS. It is the outer radius of the RADARC. A "−" is printed if it is not specified
#
#   TRACK      - Not supported in ICC no example to date 
#   
#   CNTRLPT    - example: CNTRLPT/<control_point_type>/<position_or_point_name>/<control_point_location>/<control_point_altitude>//
#                         CNTRLPT/CP/APPLE/LATM:2034N05035E/BRRA:MSL−210AMSL/
#
#              <control_point_type> mandatory, 2A. It is the type of control point taken from the ASMAN ACM Editor window. The legal values are "IP" (Initial Point), "CP" (Contact Point), "EP" (Entry/Exit Point), "WP" (Way #              Point), "RP" (Rendezvous Point), "IF" (Initial Approach Fix), "FF" (Final Approach Fix), "ER" (End Aerial Refuelling), "OT" (Other). "OT" is printed if it is not specified.
#              <position_or_point_name> mandatory, 1−20X. It is the name of the control point. A "−" is printed if it is not specified.
#              <control_point_location> mandatory, 11−15AN. It is the coordinates of the control point.. It is prefixed by "LATM:" or "LATS:" depending on the precision of the location.
#              <control_point_altitude> mandatory, 8−15ANS. It is the minimum and maximum altitude of the control point. It is prefixed by "BRRA:" if the minimum altitude is a base reference point and maximum altitude is #              relative. It is prefixed by "BRFL:" if the minimum altitude is a base reference point and maximum altitude is a flight level. It is prefixed by "RAFL:" if the minimum altitude is relative and the maximum altitude #              is a flight level. It is prefixed by "FLFL:" if both altitudes are flight levels. It is prefixed by "RARA:" if both altitudes are relative
#
#              *********Not supported in ICC*********
#
###########################################################################
class ProcessGeometry:

    _json = []

    def __init__(self):
        pass

    def processBlock(self, records, year):
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessGeometry.processBlock() - Start".format(time.ctime()))

        currentRecordIndex = -1
        while currentRecordIndex < len(records):
            currentRecordIndex += 1
            if currentRecordIndex >= len(records):
                break

            if records[currentRecordIndex].startswith('ACMID'):
                self._processBlock(currentRecordIndex, records, year)

        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessGeometry.processBlock() - Finish".format(time.ctime()))

        pass

    def getJson(self):
        return { 'geometry': self._json }

    def _processBlock(self, currentRecordIndex, records, year):
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessGeometry._processBlock()".format(time.ctime()))

        json = {}

        if records[currentRecordIndex].startswith('ACMID'):
            json['ACMID'] = airspacemanagement.parser.parseACMID(records[currentRecordIndex])
            json['ACMID']['SORTORDER'] = len(self._json) + 1
        
        #We need to process the EFFLevel first so this can be passed into the create geometry to use as its z-value
        processEFFLEVELBlock = True
        currentEffLevelRecordIndex = currentRecordIndex
        json['ACMID']['efflevel'] = {'label': "No EFFLevel in record", 'min_height': 0, 'max_height': 0, 'ext_height': 0}
        while processEFFLEVELBlock == True:            
            record = records[currentEffLevelRecordIndex + 1]            
            if record.startswith('EFFLEVEL'):
                json['ACMID']['efflevel'] = airspacemanagement.parser.parseEFFLEVEL(record)
                processEFFLEVELBlock = False
            elif record.startswith('NARR'):
                processEFFLEVELBlock = False
            else:
                currentEffLevelRecordIndex += 1    
        
        processBlock = True
        while processBlock == True:
            record = records[currentRecordIndex + 1]
            #Does the block begin with an airspace shape
            
            #CIRCLE
            if record.startswith('CIRCLE'):
                json['ACMID']['geometry'] = airspacemanagement.parser.parseCIRCLE(record,float(json['ACMID']['efflevel']['min_height']))
                currentRecordIndex += 1
            #CORRIDOR
            elif record.startswith('CORRIDOR'):
                json['ACMID']['geometry'] = airspacemanagement.parser.parseCORRIDOR(record,float(json['ACMID']['efflevel']['min_height']))
                currentRecordIndex += 1
            #LINE
            elif record.startswith('GEOLINE'):
                json['ACMID']['geometry'] = airspacemanagement.parser.parseGEOLINE(record,float(json['ACMID']['efflevel']['min_height']))
                currentRecordIndex += 1
            #POINT
            elif record.startswith('APOINT'):
                json['ACMID']['geometry'] = airspacemanagement.parser.parseAPOINT(record,float(json['ACMID']['efflevel']['min_height']))
                currentRecordIndex += 1
            #POLYGON
            elif record.startswith('POLYGON'):
                json['ACMID']['geometry'] = airspacemanagement.parser.parsePOLYGON(record,float(json['ACMID']['efflevel']['min_height']))
                currentRecordIndex += 1
            elif record.startswith('APERIOD'):
                if ('period' in json['ACMID']) == False:
                    json['ACMID']['period'] = []
                tempJson = airspacemanagement.parser.parseAPERIOD(record, year)
                tempJson['SORTORDER'] = len(json['ACMID']['period']) + 1
                json['ACMID']['period'].append(tempJson)
                currentRecordIndex += 1
            elif record.startswith('EFFLEVEL'):
                #EFFLEVEL Already captured move on to next row
                currentRecordIndex += 1
            elif record.startswith('NARR'):
                processBlock = False
            else:
                utils.common.OutputMessage(logging.DEBUG, "The record " + record + " does not start with an Airspace Control Means data segment that is recognised")
                currentRecordIndex += 1
        
        self._json.append(json)

        pass

###########################################################################
# Class to process the ATO header.
# Process the block types:
#   EXER
#   MSGID
#
###########################################################################
class ProcessATOHeader:

    _json = {}

    def __init__(self):
        self._json = {}
        pass

    def processBlock(self, records):
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessATOHeader.processBlock() - Start".format(time.ctime()))

        self._json['GENTEXT'] = []
        
        for record in records:
            if record.startswith('EXER'):
                self._json['EXER'] = airspacemanagement.parser.parseEXER(record)
            elif record.startswith('OPER'):
                self._json['OPER'] = airspacemanagement.parser.parseOPER(record)
            elif record.startswith('AMPN'):
                self._json['AMPN'] = airspacemanagement.parser.parseAMPN(record)
            elif record.startswith('MSGID'):
                self._json['MSGID'] = airspacemanagement.parser.parseMSGID(record)
            elif record.startswith('TIMEFRAM'):
                self._json['TIMEFRAM'] = airspacemanagement.parser._parseBlockTIMEFRAM(record)
            elif record.startswith('GENTEXT'):
                self._json['GENTEXT'].append(airspacemanagement.parser.parseGENTEXT(record))                 
        
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessATOHeader.processBlock() - Finish".format(time.ctime()))

        pass

    def getJson(self):
        return { 'header': self._json }

    def getYear(self):
        return self._getDate(self._json['TIMEFRAM']['start']).year

    def _getDate(self, timeString):
        return datetime.strptime(timeString, "%Y/%m/%d %H:%M:00")

###########################################################################
# Class to process an ATO goemetry block.
# Process the block types:
#   AMSNDAT
#
###########################################################################
class ProcessATOBlocks:

    _json = { 'Missions' : []}

    def __init__(self):
        pass

    def processBlock(self, records, year):
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessSingleBlock.processBlock() - Start".format(time.ctime()))

        taskGroup  = {}
        taskUnit   = []
        
        currentRecordIndex = -1
        while currentRecordIndex < len(records) -1:
            currentRecordIndex += 1
            
            if currentRecordIndex >= len(records):
                break

            if records[currentRecordIndex].startswith('TSKGRPG'):
                taskGroup = airspacemanagement.parser.parseTSKGRPG(records[currentRecordIndex])
                taskGroup['taskUnit'] = {}
                self._json['Missions'].append(taskGroup)
            elif records[currentRecordIndex].startswith('TASKUNIT'):
                taskUnit = airspacemanagement.parser.parseTASKUNIT(records[currentRecordIndex])
                taskUnit['tasks'] = []
                taskGroup['taskUnit'] = taskUnit
            elif records[currentRecordIndex].startswith('AMSNDAT'):
                taskJson = self._processBlock(currentRecordIndex, records, year)
                taskUnit['tasks'].append(taskJson)  
            
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessSingleBlock.processBlock() - Finish".format(time.ctime()))

        pass

    def getJson(self):
        return self._json

    def _processBlock(self, currentRecordIndex, records, year):
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessATOBlocks._processBlock() - Start".format(time.ctime()))
        
        json = {}

        if records[currentRecordIndex].startswith('AMSNDAT'):
            json['AMSNDAT'] = airspacemanagement.parser.parseAMSNDAT(records[currentRecordIndex])
            json['AMSNDAT']['GTGTLOC'] = []
            json['AMSNDAT']['route'] = []
            
        processBlock = True
        while processBlock == True and currentRecordIndex < len(records) -1:            
            record = records[currentRecordIndex + 1]
            if record.startswith('MSNACFT'):
                json['AMSNDAT']['aircraft'] = airspacemanagement.parser.parseMSNACFT(record)
                currentRecordIndex += 1
                pass
            elif record.startswith('ROUTE'):
                json['AMSNDAT']['route'] = airspacemanagement.parser.parseROUTE(record)
                currentRecordIndex += 1
                pass
            elif record.startswith('GTGTLOC'):
                json['AMSNDAT']['GTGTLOC'] = airspacemanagement.parser.parseGTGTLOC(record,year)
                currentRecordIndex += 1
                pass
            elif record.startswith('GENTEXT'):
                # GENTEXT is captured at the ATO Header level no need to capture again within the AMSNDAT Block
                currentRecordIndex += 1
                pass
            elif record.startswith('AMSNDAT'):
                processBlock = False            
            else:
                utils.common.OutputMessage(logging.DEBUG, "The record " + record + " does not start with a segment that is recognised")
                currentRecordIndex += 1
                
        utils.common.OutputMessage(logging.DEBUG, "{0} ProcessATOBlocks._processBlock() - Finish".format(time.ctime()))
        return json

