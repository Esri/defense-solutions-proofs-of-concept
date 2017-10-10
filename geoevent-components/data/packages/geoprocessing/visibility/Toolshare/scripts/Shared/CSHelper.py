import os
import arcpy
import Shared.ErrorHandling as ErrorHandling
import srConfig

class srConverter:
    def __init__(self):
        try:
            parent = os.path.dirname((os.path.dirname(__file__)))
            sw = os.path.join(parent, 'scratch')
            
            ws = 'C:' + os.sep + os.path.join('agsresources','routing','arp','Toolshare','data', 'nx_chinarail')
            if not arcpy.env.scratchWorkspace:
                arcpy.env.scratchWorkspace = sw
            self.scratch = str(sw)    
            self.scratchgdb = None
            if float(arcpy.GetInstallInfo()['Version']) > 10.0:
                self.scratchgdb = str(arcpy.env.scratchGDB)
            else:
                self.scratchgdb = os.path.join(str(sw), 'scratch.gdb')
            arcpy.AddMessage('scratchgdb: ' + self.scratchgdb)
            arcpy.env.overwriteOutput = True
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddMessage(m)
            
    def __convert__(self, ds, tmpname, sr):
        try:
            out = os.path.join(self.scratchgdb, tmpname)
            arcpy.AddMessage('reproject fullpath: ' + out)
            arcpy.Project_management(ds, out, sr)
            return out
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddMessage(m)
    def convert(self, datasets, wkid):
        try:
            sr = None
            if float(arcpy.GetInstallInfo()['Version']) > 10.0: 
                sr=self.__getNewSR__(wkid)
            else:
                sr=self.__getNewSR_10_0__(wkid)
            outds = []
            count = 0
            for d in datasets:
                tmpname = 'tmp' + str(count)
                out = self.__convert__(d, tmpname, sr)
                outds.append(out)
                count = count + 1
            return outds
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddMessage(m)
    def __getNewSR__(self, wkidOut):
        try:
            sr = arcpy.SpatialReference(wkidOut)
            return sr
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddMessage(m)
    def __getNewSR_10_0__(self, wkidOut):
        try:
            parentDir = None
            p_type = wkidInfo[str(wkidOut)]['type']
            if p_type == 'gcs':
                parentDir = os.path.join(arcpy.GetInstallInfo()["InstallDir"],"Coordinate Systems","Geographic Coordinate Systems")
            elif p_type == 'pcs':
                parentDir = os.path.join(arcpy.GetInstallInfo()["InstallDir"],"Coordinate Systems","Projected Coordinate Systems")
            else:
                raise Exception("wkid not registered!")
            prjFile = parentDir
            path = wkidInfo[str(wkidOut)]['path'].split('/')
            for p in path:
                prjFile = os.join(prjFile, p)
            sr = arcpy.SpatialReference(prjFile)
            return sr
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddMessage(m)
        
            
