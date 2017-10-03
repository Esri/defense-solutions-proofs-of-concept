## Copyright 2016 Esri
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
## http://www.apache.org/licenses/LICENSE-2.0
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.​

import arcpy, urllib, json, datetime, time, logging
import utils.common
import config.settings

######## user variables ###########
db = arcpy.GetParameterAsText(0)
imageFolder = arcpy.GetParameterAsText(1)
weatherURL = arcpy.GetParameterAsText(2)
CONST_HEADING_PAD = 50

inFields = ["SHAPE@XY","msn_no","task_country","task_unit","task_unit_loc","msn_res_ind","msn_package_id","msn_type_p","msn_type_s","ac_type","ac_num","ac_callsign","ac_prim_config","dep_loc","rec_loc","route","gtgt_id","gtgt_name","gtgt_desc","gtgt_priority","gtgt_desig","gtgt_type","gtgt_tot","AMSID"]
outFields = ["SHAPE@XY","name","description","pic_url","thumb_url","icon_color","AMSID"]
insertValues = []

inFC = "%s\AirC2_ATO_MISSION" % (db)
outFC = "%s\AirC2_ATO_MISSION_BRIEFING" % (db)

########### logging items #########

utils.common.setupLogging("DEBUG", config.settings.LOG_ENABLE_FILE, config.settings.LOG_PATH, 'update_mission_briefing', config.settings.LOG_USE_TIMESTAMP)

utils.common.OutputMessage(logging.DEBUG, '-' * CONST_HEADING_PAD)
utils.common.OutputMessage(logging.DEBUG, "{0} Creatation of Mission Briefing Feature Class started".format(time.ctime()))
utils.common.OutputMessage(logging.DEBUG, '-' * CONST_HEADING_PAD)

try:

  cursor = arcpy.da.SearchCursor(inFC,inFields)
  
  # Set icon colour to green only change if the response from a weather service is not Favorable
  icon_color = "G"

  for row in cursor:

    utils.common.OutputMessage(logging.DEBUG, "{0} Processing ".format(time.ctime()) + row[1])

    name = row[1] + "<br /><font size='2'>&#40;<i><a target='_blank' href='http://www.esri.com'>Browse ETF</a>&#41;</i></font>"

    #for the rest of the name field to be built up we need to query the weather service to obtain the status of target acquisition

    #We need to use the Time on Target value for the weather, calculate the value as time in milliseconds from the epoch:

    SecondsFromEpoch = int(time.mktime(datetime.datetime.strptime(row[22], '%Y/%m/%d %H:%M:%S').timetuple()))*1000

    #The weather image service has rasters that are in 3 hour time bands from 00:00 (eg 00:00, 03:00, 06:00, 09:00....)
    #So we need to work out previous 3 hour time interval by taking away the remainder of seconds left over when devided by 10800000 (3 hours in milliseconds)

    previous3HourInSecondsFromEpoch = SecondsFromEpoch - (SecondsFromEpoch % 10800000) 

    #We can now build up the parameters for the query

    #parameters for 'Air Assault Impact of Cloud Ceiling on target acquisition in fixed wing aircraft'

    parameters = urllib.urlencode({'geometry': {'x': row[0][0], 'y': row[0][1], "spatialReference": {"wkid": 4326}},'geometryType':'esriGeometryPoint','renderingRule':'{"rasterFunction" :"Air Assault Impact of Cloud Ceiling on target acquisition in fixed wing aircraft" }','time': previous3HourInSecondsFromEpoch,'returnGeometry':'false','returnCatalogItems':'false', 'f': 'json'})

    request = weatherURL + '/identify?' + parameters

    try:
      #make URL call to service
      response = json.loads(urllib.urlopen(request).read())

      #process response    
      
      if response['value'] == "114, 137, 68": #Pixel value colour for Favorable
          name =  name + "<br /><font size='2'>Impact of Cloud Ceiling on target acquisition is</font><font style='color:lime' size='2'> Favorable.</font>"
          utils.common.OutputMessage(logging.DEBUG, "{0} Impact of Cloud Ceiling on target acquisition in fixed wing aircraft is Favorable".format(time.ctime()))
      if response['value'] == "215, 173, 96": #Pixel value colour for Marginal
          name =  name + "<br /><font size='2'>Impact of Cloud Ceiling on target acquisition is</font><font style='color:orange' size='2'> Marginal.</font>"
          utils.common.OutputMessage(logging.DEBUG, "{0} Impact of Cloud Ceiling on target acquisition in fixed wing aircraft is Marginal".format(time.ctime()))
          icon_color = "R"
      if response['value'] == "191, 55, 42": #Pixel value colour for Unfavorable
          name =  name + "<br /><font size='2'>Impact of Cloud Ceiling on target acquisition is</font><font style='color:red' size='2'> Unfavorable.</font>"
          utils.common.OutputMessage(logging.DEBUG, "{0} Impact of Cloud Ceiling on target acquisition in fixed wing aircraft is Unfavorable".format(time.ctime()))
          icon_color = "R"
    except:
      utils.common.OutputMessage(logging.DEBUG, "{0} An error occured whilst trying to derive the Impact of Cloud Ceiling on target acquisition in fixed wing aircraft".format(time.ctime()))
        
    #parameters for 'Impact of temperature on Air Defence'

    parameters = urllib.urlencode({'geometry': {'x': row[0][0], 'y': row[0][1], "spatialReference": {"wkid": 4326}},'geometryType':'esriGeometryPoint','renderingRule':'{"rasterFunction" :"Air Defense impact of Temperature." }','time': previous3HourInSecondsFromEpoch,'returnGeometry':'false','returnCatalogItems':'false', 'f': 'json'})

    #add parameters to weather service URL

    request2 = weatherURL + '/identify?' + parameters

    try:
      #make URL call to service
      response2 = json.loads(urllib.urlopen(request2).read())

      #process response    
      if response2['value'] == "114, 137, 68": #Pixel value colour for Favorable
        name =  name + "<br /><font size='2'>Impact of temperature on Air Defence is</font><font style='color:lime' size='2'> Favorable.</font>"
        utils.common.OutputMessage(logging.DEBUG, "{0} Impact of temperature on Air Defence is Favorable".format(time.ctime()))
      if response2['value'] == "215, 173, 96": #Pixel value colour for Marginal
        name =  name + "<br /><font size='2'>Impact of temperature on Air Defence is</font><font style='color:orange' size='2'> Marginal.</font>"
        utils.common.OutputMessage(logging.DEBUG, "{0} Impact of temperature on Air Defence is Marginal".format(time.ctime()))
        icon_color = "R"
      if response2['value'] == "191, 55, 42": #Pixel value colour for Unfavorable
        name =  name + "<br /><font size='2'>Impact of temperature on Air Defence is</font><font style='color:red' size='2'> Unfavorable.</font>"
        utils.common.OutputMessage(logging.DEBUG, "{0} Impact of temperature on Air Defence is Unfavorable".format(time.ctime()))
        icon_color = "R"
    except:
      utils.common.OutputMessage(logging.DEBUG, "{0} An error occured whilst trying to derive the Impact of temperature on Air Defence".format(time.ctime()))
        
    description = "<table style=\"width:100%\"><tbody><tr><td style=\"outline: none;\"><font size=\"2\" style=\"outline: none;\"><b>Tasked Country:</b> " + row[2] + "<br><b>Tasked Unit:</b> " + row[3] + "<br> <b>Task Unit Location:</b> " + row[4] + "<br><b>Residual Mission Indicator:</b> " + row[5] + "<br><b>Package ID:</b> " + row[6] + "<br><b>Primary Mission Type:</b> " + row[7] + "<br><b>Secondary Mission Type:</b> " + row[8] + "<br><b>Aircraft Type:</b> " + row[9] + "<br><b>Number of Aircraft:</b>  " + str(row[10]) + "<br><b>Aircraft Callsign:</b> " + row[11] + "<br><b>Primary Configuration:</b> " + row[12] + "<br></font></td><td style=\"outline: none;\"><font size=\"2\"><b>Departure Location:</b> " + row[13] + "<br><b>Recovery Location:</b> " + row[14] + "<br><b>Route:</b> " + row[15] + "<br><b>Ground Target ID:</b> " + row[16] + "<br><b>Ground TargetName:</b> " + row[17] + "<br><b>Ground Target Description:</b> " + row[18] + "<br><b>Ground Target Priority:</b> " + row[19] + "<br><b>GroundTarget Designation</b>: " + row[20] + "<br><b>Ground Target Type:</b>  " + row[21] + "<br><b>Ground Target Time on Target</b>: " + row[22] + "<br></font></td></tr></tbody></table>"
    pic_url = imageFolder +"/" + row[16] + ".jpg"
    thumb_url = imageFolder +"/" + row[16] + ".jpg"
    insertValues.append((row[0],name,description,pic_url,thumb_url,icon_color,row[23]))

  addCursor = arcpy.da.InsertCursor(outFC,outFields)
  utils.common.OutputMessage(logging.DEBUG, "{0} Writing features to ATO_MISSION_Breifing feature class".format(time.ctime()))
  for addRow in insertValues:
    addCursor.insertRow(addRow)
  del addCursor

except Exception, e:
  utils.common.OutputMessage(logging.DEBUG, e)
  
utils.common.OutputMessage(logging.DEBUG, '-' * CONST_HEADING_PAD)
utils.common.OutputMessage(logging.DEBUG, "{0} Creatation of Mission Briefing Feature Class complete".format(time.ctime()))
utils.common.OutputMessage(logging.DEBUG, '-' * CONST_HEADING_PAD)

      
