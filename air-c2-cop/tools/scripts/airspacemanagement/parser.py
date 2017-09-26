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
import re
import math

import config
import utils.common

class FileParser:

    _sourceFile = None
    _records = []

    def __init__(self, sourceFile):
        self._sourceFile = sourceFile
        pass

    def execute(self):
        utils.common.OutputMessage(logging.DEBUG, "{0} FileParser.execute() - Start".format(time.ctime()))

        with open(self._sourceFile, 'r') as file:
            recordBuffer = ''
            for line in file:
                isRecordEnd = line.endswith('//\n')
                if isRecordEnd: line = line.rstrip('\n')
                recordBuffer += line
                if isRecordEnd:
                    self._records.append(recordBuffer)
                    recordBuffer = ''
        file.closed

        self._outputRecords()

        utils.common.OutputMessage(logging.DEBUG, "{0} FileParser.execute() - Finish".format(time.ctime()))

        return self._records

    def _outputRecords(self):
        utils.common.OutputMessage(logging.DEBUG, "{0} FileParser._outputRecords() - Start".format(time.ctime()))

        utils.common.OutputMessage(logging.DEBUG, "{0} FileParser._outputRecords() - Record Count={1}".format(time.ctime(), len(self._records)))

        for record in self._records:
            utils.common.OutputMessage(logging.DEBUG, 'RECORD: ' + record)

        utils.common.OutputMessage(logging.DEBUG, "{0} FileParser._outputRecords() - Finish".format(time.ctime()))

        pass

class RecordIterator:

    _records            = []
    _currentLineIndex   = -1

    def __init__(self, records):
        self._records = records
        self._currentLineIndex = 0
        pass
    
    def isEOF(self):
        return self._currentLineIndex >= len(self._records)

    def currentLine(self):
        return self._records[self._currentLineIndex]

    def nextLine(self):
        self._currentLineIndex += 1
        return

###########################################################################
# Parse a date in string format to a real datetime.
# Assume input is in the format:
#    140845ZAPR
#    ddhhmmZmon
###########################################################################
def parseDate(dateString):
    s = dateString.upper()
    if(len(s) > 1):
    #Handle empty string or - for no value
        if s[-1].isdigit() == False:
            return datetime.strptime(s, '%d%H%MZ%b')
        else:
            return datetime.strptime(s, '%d%H%MZ%b%Y')
    else:
        return datetime.strptime('010001ZJAN2001', '%d%H%MZ%b%Y')
###########################################################################
# Parse a date in string format to a real datetime.
#
###########################################################################
def getDateString(timeValue):
    #return timeValue.strftime("%d-%m-%Y %H:%M")
    return timeValue.strftime("%Y/%m/%d %H:%M:00")

###########################################################################
# Convert a string to a decimal degrees value.
# Assume input is in the format:
#    LATS:500000N0113000W
#    LATS:ddmmssNdddmmssW
#    LATM:5720N00720W
#    LATM:ddmmNdddmmW
#    DMPIT:5503.0146N00233.2405W
#    DMPIT:ddmm.ssssNdddmm.ssssW
###########################################################################
def parseLatLong(llValue,height):

    v = llValue.upper()
    if len(v) > 1:
        isLATS = False
        isLATM = False
        isDMPIT = False
        isDMPIK = False
        if v.startswith('LATS:') == True:
            v = v.replace('LATS:', '')
            isLATS = True
        if v.startswith('LATM:') == True:
            v = v.replace('LATM:', '')
            isLATM = True
        if v.startswith('DMPIT:') == True:
            v = v.replace('DMPIT:', '')
            isDMPIT = True
        if v.startswith('DMPIK:') == True:
            v = v.replace('DMPIK:', '')
            isDMPIK = True
        if isLATS == True:
            latD = v[0:2]
            latM = v[2:4]
            latS = v[4:6]
            latC = v[6:7]
            lonD = v[7:10]
            lonM = v[10:12]
            lonS = v[12:14]
            lonC = v[14:15]
        if isLATM == True:
            latD = v[0:2]
            latM = v[2:4]
            latS = 0
            latC = v[4:5]
            lonD = v[5:8]
            lonM = v[8:10]
            lonS = 0
            lonC = v[10:11]
        if isDMPIT == True:
            latD = v[0:2]
            latM = v[2:4]
            latS = float(v[5:9]) * 0.06
            latC = v[9:10]
            lonD = v[10:13]
            lonM = v[13:15]
            lonS = float(v[16:20]) * 0.06
            lonC = v[20:21]
        if isDMPIK == True:
            latD = v[0:2]
            latM = v[2:4]
            latS = float(v[5:8]) * 0.06
            latC = v[8:9]
            lonD = v[9:12]
            lonM = v[12:14]
            lonS = float(v[15:18]) * 0.06
            lonC = v[18:19]
            
        dd = [convertDecimalDegrees(lonD, lonM, lonS, lonC), convertDecimalDegrees(latD, latM, latS, latC), height]
        
        return dd
    else:
        return [0,0]
###########################################################################
# Convert degress minutes seconds into decimal degrees.
#
###########################################################################
def convertDecimalDegrees(d, m, s, c):
    dd = float(d) + float(m)/60 + float(s)/3600
    sign = 1
    if c.upper() in 'S' or c.upper() in 'W': sign = -1
    return dd * sign

###########################################################################
# Parse a distance string.  Assumes in the form:
# 15KM
# 7.5KM
# 1KM
#
###########################################################################
def parseDistance(distanceValue):
    value = distanceValue.upper()
    
    if value.endswith('KM'):
        distanceUnit = 'Kilometers'
        value = float(value.replace('KM', ''))
    elif value.endswith('NM'):
        distanceUnit = 'NauticalMiles'
        value = float(value.replace('NM', ''))
    else:
        raise Exception('Cannot process distance value, unknown units. (%s)' % distanceValue)
    
    return (value, distanceUnit)

def parseEXER(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseEXER()".format(time.ctime()))
    items = record.split('/')
    return { 'id': items[1] }

def parseOPER(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseOPER()".format(time.ctime()))
    items = record.split('/')
    return { 'id': items[1] }

def parseMSGID(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseMSGID()".format(time.ctime()))
    items = record.split('/')
    return { 'id': items[1], 'source': items[2], 'type': items[5], 'version': items[6] }

def parseAMPN(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseAMPN()".format(time.ctime()))
    items = record.split('/')
    return { 'title': items[1], 'classification': items[2], 'text': items[3] }

def parseACOID(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseACOID()".format(time.ctime()))
    items = record.split('/')
    return { 'item1': items[1], 'item2': items[2] }

def parseGEODATUM(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseGEODATUM()".format(time.ctime()))
    items = record.split('/')
    return { 'value': items[1] }

def parsePERIOD(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parsePERIOD()".format(time.ctime()))
    items = record.split('/')
    return { 'period1': getDateString(parseDate(items[1])), 'period2': getDateString(parseDate(items[2])) }

###########################################################################
# Parse a EFFLEVEL block into JSON.
# Assume the block is in the form:
# EFFLEVEL/BRRA:GL-220AMSL//
# 
###########################################################################
def parseEFFLEVEL(record):
    items   = record.split('/')
    height  = parseHeight(items[1])
    return { 'label': items[1], 'min_height': height['min_height'], 'max_height': height['max_height'], 'min_height_ref': height['min_height_ref'], 'max_height_ref': height['max_height_ref'],'ext_height': height['max_height'] - height['min_height']}

def parseHeight(value):
    json = { 'min_height': 0, 'max_height': 0, 'min_height_ref': '', 'max_height_ref': ''}

    if ':' in value:
      v = value.split(':')
      v = v[1].split('-')
    else:
      v = value.split('-')
    
    if(len(v)<=1):       
        min = re.sub(r'\D', '', v[0])
        min_ref = re.sub(r'\d', '', v[0])
        max = re.sub(r'\D', '', v[0])
        max_ref = re.sub(r'\d', '', v[0])
    else:        
        min = re.sub(r'\D', '', v[0])
        min_ref = re.sub(r'\d', '', v[0])
        max = re.sub(r'\D', '', v[1])
        max_ref = re.sub(r'\d', '', v[1])
    
    if len(min) != 0: json['min_height'] = float(min) * 100
    if len(max) != 0: json['max_height'] = float(max) * 100
    if len(min_ref) != 0: json['min_height_ref'] = min_ref
    if len(max_ref) != 0: json['max_height_ref'] = max_ref

    return json

###########################################################################
# Parse a ACMID block, assumes block is in the form:
# ACMID/ACM:ROZ/NAME:ATLANTIC/POLYGON/USE:RECCE//
#
###########################################################################
def parseACMID(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseACMID()".format(time.ctime()))
    items = record.split('/')
    return { 'id': items[1].split(':')[1], 'name': items[2].replace('NAME:', ''), 'type': items[3], 'use': items[4].replace('USE:', '') }

###########################################################################
# Parse a POLYGON block into Esri geometry JSON.
# Assume the POLYGON block is in the form:
# POLYGON/LATS:564100N0033500W/LATS:581700N0054000W/LATS:582500N0050500W/LATS:565200N0031000W//
#
###########################################################################
def parsePOLYGON(record,height):
    utils.common.OutputMessage(logging.DEBUG, "{0} parsePOLYGON()".format(time.ctime()))
    
    json = {}
    json['hasZ'] = True
    json['spatialReference'] = {"wkid" : 4326}
    json['rings'] = [[]]

    items = record.split('/')
    
    for i in items:
        if i.startswith('LATS') == True or i.startswith('LATM') == True or i.startswith('DMPIT') == True:
            json['rings'][0].append(parseLatLong(i,height))
            pass
    # Duplicate the first point to close the polygon.
    if len(json['rings'][0]) > 0:
        json['rings'][0].append(copy.copy(json['rings'][0][0]))

    return json

###########################################################################
# Parse a CORRIDOR block into Esri geometry JSON.
# Assume the CIRCLE block is in the form:
# CORRIDOR/5NM/LATS:3611800N13055416E/LATS:3523304N12911460E/LATS:3527615N12718004E/LATS:3652752N12635564E//
# 
###########################################################################
def parseCORRIDOR(record,height):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseCORRIDOR()".format(time.ctime()))
       
    line = []
    
    items = record.split('/')
    (distance, units) = parseDistance(items[1])
        
    for i in items:
        if i.startswith('LATS') == True or i.startswith('LATM') == True or i.startswith('DMPIT') == True:
            line.append(parseLatLong(i,height))
            pass   
    
    newline = arcpy.Polyline(arcpy.Array([arcpy.Point(*coords) for coords in line]),arcpy.SpatialReference(4326), False, False)
    
    arcpy.Buffer_analysis(arcpy.Polyline(arcpy.Array([arcpy.Point(*coords) for coords in line]),arcpy.SpatialReference(4326), False, False), r'in_memory\tempBuffer', '%s %s' % (distance/2, units), 'FULL', 'FLAT', 'ALL')
    geometries = arcpy.CopyFeatures_management(r'in_memory\tempBuffer', arcpy.Geometry())
    
    
    objJson = json.loads(geometries[0].JSON)

    objJson['hasZ'] = True
    
    for i in range(0, len(objJson['rings'])):
        for j in range(0, len(objJson['rings'][i])):
            objJson['rings'][i][j].append(height)
    
    arcpy.Delete_management(r'in_memory\tempBuffer')
    
    del geometries
    return objJson
    
###########################################################################
# Parse an AORBIT block into Esri geometry JSON.
# Assume the AORBIT block is in the form:
# AORBIT/LATS:151000N0590900E/LATM:1610N06010E/235KM/C//
# 
###########################################################################
def parseAORBIT(record,height):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseAORBIT()".format(time.ctime()))
       
    line = []
    
    items = record.split('/')
    (distance, units) = parseDistance(items[3])
        
    for i in items:
        if i.startswith('LATS') == True or i.startswith('LATM') == True or i.startswith('DMPIT') == True:
            line.append(parseLatLong(i,height))
            pass   
    
    newline = arcpy.Polyline(arcpy.Array([arcpy.Point(*coords) for coords in line]),arcpy.SpatialReference(4326), False, False)
    
    if items[4] == "R":
        line_side = "RIGHT"
        buffer_distance = distance
    elif items[4] == "L":
        line_side = "LEFT"
        buffer_distance = distance
    else:
        line_side = "FULL"
        buffer_distance = distance/2      
    
    arcpy.Buffer_analysis(arcpy.Polyline(arcpy.Array([arcpy.Point(*coords) for coords in line]),arcpy.SpatialReference(4326), False, False), r'in_memory\tempBuffer', '%s %s' % (buffer_distance/2, units), line_side, 'FLAT', 'ALL')
    geometries = arcpy.CopyFeatures_management(r'in_memory\tempBuffer', arcpy.Geometry())
        
    objJson = json.loads(geometries[0].JSON)

    objJson['hasZ'] = True
    
    for i in range(0, len(objJson['rings'])):
        for j in range(0, len(objJson['rings'][i])):
            objJson['rings'][i][j].append(height)
    
    arcpy.Delete_management(r'in_memory\tempBuffer')
    
    del geometries
    return objJson
    
###########################################################################
# Parse a APOINT block into Esri geometry JSON.
# Assume the APOINT block is in the form:
# APOINT/LATS:151000N0590900E//
# 
###########################################################################
def parseAPOINT(record,height):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseAPOINT()".format(time.ctime()))
       
    json = {}
    json['hasZ'] = True
    json['spatialReference'] = {"wkid" : 4326}
    
    items = record.split('/')
    for i in items:
        if i.startswith('LATS') == True or i.startswith('LATM') == True or i.startswith('DMPIT') == True:
            pointCoords = parseLatLong(i,height)
            json['x'] = pointCoords[0]
            json['y'] = pointCoords[1]
            json['z'] = pointCoords[2]
            pass    
    return json    

###########################################################################
# Parse a CNTRLPT block into Esri geometry JSON.
# Assume the CNTRLPT block is in the form:
# CNTRLPT/CP/APPLE/4520.3500N-02126.1500E/BRRA:MSL-210AMSL/
# 
###########################################################################
def parseCNTRLPT(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseAPOINT()".format(time.ctime()))
       
    json = {}
    json['hasZ'] = True
    json['spatialReference'] = {"wkid" : 4326}
    
    items = record.split('/')
    for i in items:
        if i.startswith('LATS') == True or i.startswith('LATM') == True or i.startswith('DMPIT') == True:
            pointCoords = parseLatLong(i,height)
            json['x'] = pointCoords[0]
            json['y'] = pointCoords[1]
            json['z'] = pointCoords[2]
            pass    
    return json
    
###########################################################################
# Parse a GEOLINE block into Esri geometry JSON.
# Assume the GEOLINE block is in the form:
# GEOLINE/LATM:2037N05943E/LATS:204400N0594300E/LATM:2048N05982E//
# 
###########################################################################
def parseGEOLINE(record,height):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseGEOLINE()".format(time.ctime()))
    
    json = {}
    json['hasZ'] = True
    json['spatialReference'] = {"wkid" : 4326}
    json['paths'] = [[]]
    
    items = record.split('/')
            
    for i in items:
        if i.startswith('LATS') == True or i.startswith('LATM') == True or i.startswith('DMPIT') == True:
            json['paths'][0].append(parseLatLong(i,height))
            pass
    
    return json
    
##########################################################################
# Parse a APERIOD block into JSON.
# Assume the block is in the form:
# APERIOD/DISCRETE/141030ZAPR/141130ZAPR//
# APERIOD/INTERVAL/010000JAN/012359JAN/DAILY/7DAY//
# APERIOD/INTERVAL/131325ZNOV/132359ZNOV/WEEKLY/4WK//
# APERIOD/TYPE/START/STOP/FREQUENCY/DURATION//
#
###########################################################################
def parseAPERIOD(record, year):
    utils.common.OutputMessage(logging.DEBUG, "{0} ProcessGeometry.parseAPERIOD()".format(time.ctime()))
    json        = {}
    items       = record.split('/')
    type        = items[1]
    startDate   = parseDate(items[2] + str(year))
    stopDate    = parseDate(items[3] + str(year))
    start       = getDateString(startDate)
    stop        = getDateString(stopDate)
    frequency   = ''
    duration    = ''
    if type.upper() == 'INTERVAL':
        frequency   = items[4]
        duration    = items[5]
    json['APERIOD'] = { 'type': type, 'start': start, 'stop': stop, 'frequency': frequency, 'duration': duration  }
    return json

###########################################################################
# Parse a CIRCLE block into Esri geometry JSON.
# Assume the CIRCLE block is in the form:
# CIRCLE/LATS:580429N0042748W/15KM//
# Uses a buffer tool to buffer the lat long by the specified distance.
#
###########################################################################
def parseCIRCLE(record,height):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseCIRCLE()".format(time.ctime()))

    items = record.split('/')
    centreLatLong = parseLatLong(items[1],height)
    (distance, units) = parseDistance(items[2])
    circle = createBufferedPoint(centreLatLong[0], centreLatLong[1], distance, units, 4326)

    objJson = copy.copy(json.loads(circle))
    
    objJson['hasZ'] = True
    for i in range(0, len(objJson['rings'][0])):
        objJson['rings'][0][i].append(height)
        
    return objJson

###########################################################################
# Create a buffered point from a lat, long, distance and units.
#
###########################################################################
def createBufferedPoint(x, y, distance, units, sr):

    centerPoint = arcpy.Point()
    centerPoint.X = x
    centerPoint.Y = y
    centerPoint.Z = 0

    centerPointGeometry = arcpy.PointGeometry(centerPoint, arcpy.SpatialReference(sr), False, False)

    outGeomList = arcpy.Buffer_analysis(centerPointGeometry, r'in_memory\tempBuffer', '%s %s' % (distance, units), None, None, 'ALL')

    geoms = []
    rows = arcpy.SearchCursor(r'in_memory\tempBuffer')
    row = next(rows)
    while row != None:
        geom = row.shape
        geoms.append(geom)
        row = next(rows)

    del row
    del rows

    arcpy.Delete_management(r'in_memory\tempBuffer')
    
    return geoms[0].JSON

############################################################################
#
# Function to check if a string value is a valid float
#
###########################################################################    
def isfloat(value):
  try:
    float(value)
    return True
  except:
    return False

############################################################################
#
# Function to check if a string value is a holder for an optional field i.e. '-'
#
###########################################################################    
def isNotSpacer(value):
  if value != '-':
    return True
  else:
    return False
    

###########################################################################
# ATO FUNCTIONS
#
###########################################################################    

    
###########################################################################
# Parse a TSKGRPG block, assumes block is in the form:
# TSKGRPG/UNIT/UKF2303//
#
###########################################################################
def parseTSKGRPG(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseTSKGRPG()".format(time.ctime()))
    items = record.split('/')
    return { 'TaskGroupingCategory': items[1] }

###########################################################################
# Parse a TASKUNIT block, assumes block is in the form:
# TASKUNIT/NATIONALITY/ARMED SERVICE CODE/UNIT ID/UNIT LOCATION/UNIT ADDRESS/UNIT REFERENCE
# TASKUNIT/US/F/UIC:USN7610/-/-//
###########################################################################
def parseTASKUNIT(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseTASKUNIT()".format(time.ctime()))
    items = record.split('/')
    items.pop(0)
    
    TASKUNITkeys = ['Nationality', 'ArmedServiceCode', 'UnitID', 'UnitLocation', 'UnitAddress', 'UnitRef', 'Comments']    
    
    TASKUNITvalues = []
    
    for i in items:
        TASKUNITvalues.append(i)
    
    TASKUNIT = dict(zip(TASKUNITkeys,TASKUNITvalues))    
    
    #Remove leading characters from UnitID
    TASKUNIT['UnitID'] = TASKUNIT['UnitID'].split(":")[1]
    
    return TASKUNIT
    
############################################################################
# Parse a AMSNDAT (AIRCRAFT MISSION DATA) block, assumes block is in the form:
# AMSNDAT/15JW4002/-/-/-/ASW/-/-/DEPLOC:EGQS/ARRLOC:EGQS//
# AMSNDAT/MISSIONNO/AMCNO/PACKAGEID/COMMANDER/1STMISSIONTYPE/2NDMISSIONYPE/DEPARTURELOC/RECOVERYLOC//
#
###########################################################################
def parseAMSNDAT(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseAMSNDAT()".format(time.ctime()))
    items = record.split('/')    
    items.pop(0)
    
    AMSNDATkeys = ['residualMissionIndicator', 'missionNo', 'missionType', 'packageId', 'commander', 'COMAO', 'primaryMissionType', 'secondaryMissionType', 'alertStatus', 'missionRole', 'departureLocation', 'departureTime', 'recoveryLocation', 'recoveryTime']    
      
    AMSNDATvalues = []
    
    for i in items:
        AMSNDATvalues.append(i)
    
    AMSNDAT = dict(zip(AMSNDATkeys,AMSNDATvalues)) 

    if isNotSpacer(AMSNDAT['departureLocation']):
        AMSNDAT['departureLocation'] = AMSNDAT['departureLocation'].split(":")[1]
    
    if isNotSpacer(AMSNDAT['recoveryLocation']):
        AMSNDAT['recoveryLocation'] = AMSNDAT['recoveryLocation'].split(":")[1]
            
    return AMSNDAT

############################################################################
# Parse a MSNACFT (INDIVIDUAL AIRCRAFT MISSION DATA) block, assumes block is in the form:
# MSNACFT/1/OTHAC:ALOU/BLUEBIRD01/BA/-/140/30022//
# MSNACFT/1/OTHAC:ALOU/BLUEBIRD01/BA/-/140/30022//
#
###########################################################################
def parseMSNACFT(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseMSNACFT()".format(time.ctime()))
    items = record.split('/')
    items.pop(0)
    
    MSNACFTkeys = ['aircraftCount', 'aircraftType', 'callsign', 'primeConfig', 'secondConfig', 'networkEnableWeapons', 'link16Callsign', 'TACCANChannel']    
    
    MSNACFTvalues = []
    
    for i in items:
        MSNACFTvalues.append(i)
    
    MSNACFT = dict(zip(MSNACFTkeys,MSNACFTvalues))
    
    #Remove leading characters from aircraft type
    MSNACFT['aircraftType'] = MSNACFT['aircraftType'].split(":")[1]
    
    return MSNACFT

############################################################################
# Parse a ROUTE block, assumes block is in the form:
# ROUTE/-/ICAO:HHAS/-/-/151540N0394729E/-/191026Z/NAME:2/-/191049Z/NAME:XKM021/-/-/151540N0394729E/-/-/ICAO:HHAS/-// 
#
###########################################################################
def parseROUTE(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseROUTE()".format(time.ctime()))
    items = record.split('/')
    
    ROUTEkeys = ['route']
    ROUTEvalues = []
    
    ROUTEvalue = ""
        
    for i in items:
        i = i.upper()
        if i.startswith('ICAO:') == True or i.startswith('NAME:') == True:            
            ROUTEvalue = ROUTEvalue + i.split(":")[1] + "-"
            
    ROUTEvalues.append(ROUTEvalue[:-1])

    ROUTE = dict(zip(ROUTEkeys,ROUTEvalues)) 
    
    return ROUTE  

############################################################################
# Parse a GTGTLOC block (GROUND TARGET LOCATION):
# GTGTLOC/P/TOT:141310ZAPR/NET:141300ZAPR/NLT:141345Z/DRAGONIA SAM SI/ID:0044NS0001-NS101/-/CONTROL ROOM/DMPIT:5503.0146N00233.2405W/W84/-/-/2//
#
###########################################################################
def parseGTGTLOC(record,year):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseGTGTLOC()".format(time.ctime()))

    items = record.split('/')
    
    elevation = 0
    
    if isNotSpacer(items[14]):
        if isfloat(items[14]):
            elevation = float(items[14]) * 100

    json = {}
    json['designator']      = items[1]
    
    #Check if time on target is not a spacer and parse to a date - otherwise just write in spacer
    if isNotSpacer(items[2]):
        timeOnTarget = parseDate(items[2].split(":")[1] + str(year))
        json['timeOnTarget']  = getDateString(timeOnTarget)
    else:
        json['timeOnTarget'] = items[2]
    
    #Check if not earlier than is not a spacer and parse to a date - otherwise just write in spacer        
    if isNotSpacer(items[3]):
        notEarlierThan = parseDate(items[3].split(":")[1] + str(year))
        json['notEarlierThan']  = getDateString(notEarlierThan)
    else:
        json['notEarlierThan'] = items[3]
    
    #Check if not later than is not a spacer and parse to a date - otherwise just write in spacer    
    if isNotSpacer(items[4]):
        notLaterThan = parseDate(items[4].split(":")[1] + str(year))
        json['notLaterThan']  = getDateString(notLaterThan)
    else:
        json['notLaterThan'] = items[4]
        
    json['targetName']      = items[5]
    json['targetPosition']  = items[6]
    json['targetType']      = items[7].split(":")[1]
    json['dmpiId']          = items[8]
    json['dmpiDesc']        = items[9]
    json['geodeticDatum']   = items[11]
    json['elevation']       = elevation
    json['priority']        = items[16]
    json['objective']       = items[17]
    
    geometry                = parseLatLong(items[10],elevation)
    json['geometry'] = {}
    json['geometry']['spatialReference'] = {"wkid" : 4326}
    json['geometry']['x'] = geometry[0]
    json['geometry']['y'] = geometry[1]
    json['geometry']['z'] = elevation * 0.3048

    return json
    
###########################################################################
# Parse a parseAMSNLOC block into Esri geometry JSON.
# Assume the POLYGON block is in the form:
# AMSNLOC/141230ZAPR/141630ZAPR/ISLAY/150/1/LATM:5720N01000W/LATM:5720N00720W/LATM:5650N00720W/LATM:5650N00500W/LATM:5540N00500W/LATM:5540N01000W//
#
###########################################################################
def parseAMSNLOC(record, year):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseAMSNLOC() - Start".format(time.ctime()))
    
    items = record.split('/')
    
    items.pop(0)
    
    AMSNLOCkeys = ['MSNPriority', 'startTime', 'stopTime', 'locationName', 'locationAltitude', 'areaGeometry', 'location']
    AMSNLOCvalues = []
    
    for i in items:
        AMSNLOCvalues.append(i)
    
    AMSNLOC = dict(zip(AMSNLOCkeys,AMSNLOCvalues))
    
    if isNotSpacer(AMSNLOC['startTime']):
        startDate = parseDate(AMSNLOC['startTime'] + str(year))
        AMSNLOC['startTime'] = getDateString(startDate)
           
    if isNotSpacer(AMSNLOC['stopTime']):
        stopDate = parseDate(AMSNLOC['stopTime'] + str(year))
        AMSNLOC['stopTime'] = getDateString(stopDate)
    
    if isfloat(AMSNLOC['locationAltitude']):
        AMSNLOC['locationAltitude'] = (float(AMSNLOC['locationAltitude']) * 100)
    else:
        AMSNLOC['locationAltitude'] = 0
           
    return AMSNLOC

###########################################################################
# Parse a parseGENTEXT block.
# Assume the parseGENTEXT block is in the form:
# GENTEXT/TEXT INDICATOR/FREE TEXT
# GENTEXT/GENERAL SPINS INFORMATION/This field then holds free text.....
###########################################################################
def parseGENTEXT(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} parseGENTEXT() - Start".format(time.ctime()))
    
    items = record.split('/')
    
    items.pop(0)
    
    GENTEXTkeys = ['TextIndicator', 'Info']    
    
    GENTEXTvalues = []    
    
    for i in items:
        GENTEXTvalues.append(i)
    
    GENTEXT = dict(zip(GENTEXTkeys,GENTEXTvalues))
    
    GENTEXT['Info'] = GENTEXT['Info'].replace('\n','')
    GENTEXT['Info'] = GENTEXT['Info'].replace('-','')
                   
    return GENTEXT
    
###########################################################################
# Parse a TIMEFRAM block into JSON.
# Assume the block is in the form:
# TIMEFRAM/FROM:140600ZAPR2015/TO:150559ZAPR2015/ASOF:140920ZAPR2015//
#
###########################################################################
def _parseBlockTIMEFRAM(record):
    utils.common.OutputMessage(logging.DEBUG, "{0} ProcessGeometry.parseTIMEFRAM()".format(time.ctime()))

    items       = record.split('/')

    startDate   = parseDate(items[1].replace('FROM:', ''))
    stopDate    = parseDate(items[2].replace('TO:', ''))
    asOfDate    = parseDate(items[3].replace('ASOF:', ''))

    json        = {}
    start       = getDateString(startDate)
    stop        = getDateString(stopDate)
    asof        = getDateString(asOfDate)

    return { 'start': start, 'stop': stop, 'asof': asof }
