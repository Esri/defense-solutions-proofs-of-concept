# -*- coding: utf-8 -*-

###############################################################################
# Author: Shaun Nicholson, Esri UK, May 2015
#
# (C) Copyright ESRI (UK) Limited 2011. All rights reserved
# ESRI (UK) Ltd, Millennium House, 65 Walton Street, Aylesbury, HP21 7QG
# Tel: +44 (0) 1296 745500  Fax: +44 (0) 1296 745544
###############################################################################

import arceditor
import arcpy
from arcpy import env
import sys
import logging
import os
import os.path
import inspect
import datetime
import json

import config.settings
import utils.common
import airspacemanagement.reader
import airspacemanagement.writer

PARAMINDEX_SOURCE_FILE  = 0
PARAMINDEX_TARGET_WS    = 1
PARAMINDEX_LOG_LEVEL    = 2
PARAMINDEX_COUNT        = 3
PARAMINDEX_OUT_WS       = 3

CONST_HEADING_PAD       = 50

def main():
    # Make sure enough arguments are provided
    if len(sys.argv) < PARAMINDEX_COUNT:
        raise Exception('Too few parameters.')

    # Get the input parameters
    inSourceFile    = arcpy.GetParameterAsText(PARAMINDEX_SOURCE_FILE)
    inTargetWS      = arcpy.GetParameterAsText(PARAMINDEX_TARGET_WS)
    inLogLevel      = arcpy.GetParameterAsText(PARAMINDEX_LOG_LEVEL)

    # Setup logging / message output
    utils.common.setupLogging(utils.common.parseLogLevel(inLogLevel), config.settings.LOG_ENABLE_FILE, config.settings.LOG_PATH, 'process_ato', config.settings.LOG_USE_TIMESTAMP)
    
    # #########################################################################
    # Validate the input parameters.
    # #########################################################################
    # Echo the input parameters
    utils.common.OutputMessage(logging.DEBUG, 'inSourceWS        %s' % (inSourceFile))
    utils.common.OutputMessage(logging.DEBUG, 'inTargetDatasets  %s' % (inTargetWS))
    utils.common.OutputMessage(logging.DEBUG, 'inLogLevel        %s' % (inLogLevel))

    # #########################################################################
    utils.common.OutputMessage(logging.INFO, '*' * CONST_HEADING_PAD)
    utils.common.OutputMessage(logging.INFO, 'Read ATO file..')

    reader      = airspacemanagement.reader.ATOReader(inSourceFile, inTargetWS)
    jsonData    = reader.execute()

    airspacemanagement.writer.writeJSON(jsonData, ['output', 'currentATO.txt'])

    utils.common.OutputMessage(logging.INFO, '*' * CONST_HEADING_PAD)
    utils.common.OutputMessage(logging.INFO, 'Write ATO data...')
    writer = airspacemanagement.writer.ATOWriter()
    writer.execute(jsonData, inTargetWS)

    #arcpy.SetParameter(PARAMINDEX_OUT_WS, inTargetWS)

try:
    utils.common.OutputMessage(1, 'main() Started.')
    main()
    utils.common.OutputMessage(1, 'main() Completed.')
except Exception as ErrorDesc:
    arcpy.AddError(str(ErrorDesc))
    arcpy.AddError(arcpy.GetMessages(2))

