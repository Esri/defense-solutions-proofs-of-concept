###############################################################################
# Author: Shaun Nicholson, Esri UK, May 2015
#
# (C) Copyright ESRI (UK) Limited 2011. All rights reserved
# ESRI (UK) Ltd, Millennium House, 65 Walton Street, Aylesbury, HP21 7QG
# Tel: +44 (0) 1296 745500  Fax: +44 (0) 1296 745544
###############################################################################

# Import required modules
import arcpy
import string
import os
import sys
import locale
import time
import datetime
import logging
import logging.config

import config.settings

_logger = logging.getLogger(__name__)

def OutputMessage(level, message):
    #arcpy.AddMessage('level=%s,isEnabledFor=%s,getEffectiveLevel=%s' % (level, _logger.isEnabledFor(level), _logger.getEffectiveLevel()))
    if _logger.isEnabledFor(level) == True:
        arcpy.AddMessage(message)
        print(message)
        pass
    _logger.debug(message)
    pass

#def ShowMessage(level, message, logger):
#    OutputMessage(level, message)
#    logger.debug(message)

def checkIsFGDB(workspace):
    desc = arcpy.Describe(workspace)
    if desc.workspaceType.lower() == 'localdatabase': return True
    return False

def parseLogLevel(levelString):
    if levelString.lower() == 'normal': return logging.INFO
    if levelString.lower() == 'info': return logging.INFO
    if levelString.lower() == 'debug': return logging.DEBUG
    return logging.INFO
    
def setupLogging(logLevel=logging.DEBUG, enableFileLogging=False, filePath='', fileName='pythonlog', timeStampFileName=True):
    #logging.basicConfig(level=logLevel)

    # This type of logging setup is neat, tidy and very flexible.
    # However, when running in an add-in it seems to throw an IOException even though text is logged to file as expected.
    if config.settings.LOG_CONFIG != '':
        logging.config.fileConfig(config.settings.LOG_CONFIG)
        return

    logger = logging.getLogger(__name__)
    logger.setLevel(logLevel)

    if enableFileLogging == False: return

    timeStamp = ''
    if timeStampFileName == True:
        currentTimeTuple = datetime.datetime.now().timetuple()
        timeStamp = '%s_%s_%s_%s_%s_%s_' % (currentTimeTuple.tm_mday, currentTimeTuple.tm_mon, currentTimeTuple.tm_year, currentTimeTuple.tm_hour, currentTimeTuple.tm_min, currentTimeTuple.tm_sec)

    logFolder = filePath
    if filePath.startswith('..'):
        logFolder = os.path.join(os.path.dirname(__file__), filePath)
    print(logFolder)

    logFile = '%s%s%s.txt' % (logFolder, timeStamp, fileName)
    handler = logging.FileHandler(logFile)
    formatter = logging.Formatter(config.settings.LOG_FORMATTER)
    handler.setFormatter(formatter)
    logger.addHandler(handler)

def setLogLevel(logLevel):
    logger = logging.getLogger(__name__)
    logger.setLevel(logLevel)

