# -*- coding: utf-8 -*-

###############################################################################
# Author: Shaun Nicholson, Esri UK, May 2015
#
# (C) Copyright ESRI (UK) Limited 2011. All rights reserved
# ESRI (UK) Ltd, Millennium House, 65 Walton Street, Aylesbury, HP21 7QG
# Tel: +44 (0) 1296 745500  Fax: +44 (0) 1296 745544
###############################################################################

# Debug / diagnostic output levels
# 0-NODEBUG, 1-SHOW INFO MESSAGES, 2-SHOW DEBUG MESSAGES, 3-SHOW DETAILED MESSAGES
CONST_DEBUG_MODE=3

#******************************************************************************
# Logger settings.
#******************************************************************************
LOG_LEVEL           = None
LOG_CONFIG          = r''
LOG_PATH            = r'../../../logs/'     # Default logging path (can be abs or rel)
LOG_FILE            = r'customGPTools_log'     # Default logging path (can be abs or rel)
LOG_ENABLE_FILE     = True              # Enable logging to file.
LOG_USE_TIMESTAMP   = True              # Add timestamp to each logfile (unique file for each script run).
LOG_FORMATTER       = r'%(asctime)s %(levelname)s %(message)s'   # 

# Set the desired log level by uncommenting the relevant row below.
#DEFAULT_LOG_LEVEL = logging.INFO
#logLevel = logging.CRITICAL
#logLevel = logging.ERROR
#logLevel = logging.WARNING
#logLevel = logging.INFO
#logLevel = logging.DEBUG
#logLevel = logging.NOTSET

# Flag to add timestamp to each logfile (results in unique files for each script run).
logTimestampFile = True
