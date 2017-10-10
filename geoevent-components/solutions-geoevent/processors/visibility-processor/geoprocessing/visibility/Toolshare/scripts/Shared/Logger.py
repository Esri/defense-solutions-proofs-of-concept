'''

    @Created_By: Andrew Chapkowski
    @Copyright: Esri, 2011
    @Date: 1/13/2011
    @Contact: achapkowski@esri.com
    
    Logger.py contains logging functions described by PEP-8 design
    guide.  This should be used instead of print functions.

    Target: Python 2.4 or greater
'''
import logging
import os

def setupLogger(savePath, fileName="Logger.log",logName="myapp"):
    """
        Creates a logger and log file

        Inputs:
                savePath - (string) - save directory of log file
                fileName - (string) - name of log file (optional)
                logName - (string) - name of log object (optional)
        Output:
                logName - (string) - name of log
    """
    logger = logging.getLogger(logName)
    fileHandler = logging.FileHandler(savePath + os.sep + fileName)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    fileHandler.setFormatter(formatter)
    logger.addHandler(fileHandler)
    logger.setLevel(logging.INFO)
    return logName

def addLogMessage(loggerName, message="",severity="INFO"):
    """
        Adds a message to the log file based on the severity level it will
        log the information differently.  It excepts the following severity
        INFO,ERROR, and EXCEPTION, if these are not used, then it will be
        logged as INFO level.

        Inputs:
                loggerName - (string) - Name of the logger
                message - (string) - message to write to log file
                                     (optional)
                severity - (string) - level of logging (optional)
        
    """
    loggerObject = logging.getLogger(loggerName)
    if loggerObject == None:
        return
    if severity == "INFO":
        loggerObject.info(message)
    elif severity == "ERROR":
        loggerObject.error(message)
    elif severity == "EXCEPTION":
        loggerObject.exception(message)
    else:
        loggerObject.info(message)
def releaseLogger(loggerName):
    """
        Closes the log file

        Inputs:
                loggerName - (string) - Name of the logger
    """
    loggerObject = logging.getLogger(loggerName)
    handlerList = list(loggerObject.handlers)
    for h in handlerList:
        loggerObject.removeHandler(h)
        h.flush()
        h.close()

