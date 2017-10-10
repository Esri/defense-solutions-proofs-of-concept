'''
Created on Aug 4, 2010

@author: andr5624
'''
import zipfile
import arcpy
import os
from os.path import isdir, join, normpath, split

class CompressionUtility(object):
    '''
        This is a class that references common utilities to zip a workspace and unzip a zip file
    '''
    def __init__(self, fileName, Action='r'):
        '''
            Creates an instance of utilities to zip and unzip a file and workspace
        '''
        if Action == 'r':
            self.zip = zipfile.ZipFile(fileName, 'r')
        elif Action == 'w':
            self.zip = zipfile.ZipFile(fileName, 'w', zipfile.ZIP_STORED)
        else:
            arcpy.AddError("Action can only be r for read or w for write")
            print ("Action can only be r for read or w for write")
    
        
    '''
        Zip Utility
    '''
    def zipws(self, path):
        isdir = os.path.isdir
        zip = self.zip
        # Check the contents of the workspace, if it the current
        # item is a directory, gets its contents and write them to
        # the zip file, otherwise write the current file item to the
        # zip file
        for each in os.listdir(path):
            fullname = path + "/" + each
            if not isdir(fullname):
                # If the workspace is a file geodatabase, avoid writing out lock
                # files as they are unnecessary
                if not each.endswith('.lock'):
                    arcpy.AddMessage("Adding " + each + " ...")
                    # Write out the file and give it a relative archive path
                    try: zip.write(fullname, str(each))
                    except IOError: None # Ignore any errors in writing file
            else:
                # Branch for sub-directories
                for eachfile in os.listdir(fullname):
                    if not isdir(eachfile):
                        if not each.endswith('.lock'):
                            arcpy.AddMessage("Adding " + eachfile + " ...")
                            # Write out the file and give it a relative archive path
                            try: zip.write(fullname + "/" + str(eachfile), \
                                           os.path.basename(fullname) + "/" + str(eachfile))
                            except IOError: None # Ignore any errors in writing file
        zip.close()
        return str(zip)
    
    def zipWorkspace(self, path, keep):
        zip = self.zip
        path = os.path.normpath(path)
        #  os.walk visits every subdirectory, returning a 3-tuple
        #  of directory name, subdirectories in it, and file names
        #  in it.
        #
        for (dirpath, dirnames, filenames) in os.walk(path):
            # Iterate over every file name
            #
            for file in filenames:
                # Ignore .lock files
                #
                if not file.endswith('.lock'):
                    arcpy.AddMessage("Adding %s..." % os.path.join(path, dirpath, file))
                    try:
                        if keep:
                            zip.write(os.path.join(dirpath, file),
                                      os.path.join(os.path.basename(path), 
                                      os.path.join(dirpath, file)[len(path) + len(os.sep):]))
                        else:
                            zip.write(os.path.join(dirpath, file),
                            os.path.join(dirpath[len(path):], file)) 
                    except Exception, e:
                        arcpy.AddWarning("    Error adding %s: %s" % (file, e))
        return None
    



    # Function to unzipping the contents of the zip file
    #
    def unZipFile(self, path):
        zip = self.zip
        # If the output location does not yet exist, create it
        #
        if not isdir(path):
            os.makedirs(path)    

        for each in zip.namelist():
            arcpy.AddMessage("Extracting " + os.path.basename(each) + " ...")
            
            # Check to see if the item was written to the zip file with an
            # archive name that includes a parent directory. If it does, create
            # the parent folder in the output workspace and then write the file,
            # otherwise, just write the file to the workspace.
            #
            if not each.endswith('/'): 
                root, name = split(each)
                directory = normpath(join(path, root))
                if not isdir(directory):
                    os.makedirs(directory)
                file(join(directory, name), 'wb').write(zip.read(each))


