'''
@author: A Chapkowski, 
@copyright: ESRI 2010
@version: ArcGIS v 10, Python 2.6.5
'''
import sys
import traceback
import inspect

class ErrorHandling(object):
    '''
    ErrorHandling Provides User/Developers with detailed information about
    errors that occur in the code.  By using this class, you can better
    debug a program or solve on site issues.
    '''


    def __init__(self):
        '''
        Constructor - no parameters
        '''
        pass
    
    def trace(self):
        '''
        trace finds the line, the filename and error message and returns it 
        to the user
        '''
        tb = sys.exc_info()[2]
        tbinfo = traceback.format_tb(tb)[0]
        # script name + line number
        line = tbinfo.split(", ")[1]
        filename = inspect.getfile( inspect.currentframe() )
        # Get Python syntax error
        #
        synerror = traceback.format_exc().splitlines()[-1]
        return line, filename, synerror