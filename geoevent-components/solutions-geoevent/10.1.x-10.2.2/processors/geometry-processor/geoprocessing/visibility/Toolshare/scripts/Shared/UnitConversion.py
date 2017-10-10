import string

unitmap={'meter': {'meter':1, 'feet': 3.28084, 'kilometer': 0.001, 'mile':0.000621371},
        'foot': {'meter':0.3048, 'foot': 1, 'kilometer': 0.0003048, 'mile': 0.000189394},
        'kilometer': {'meter': 1000, 'foot': 3280.84, 'kilometer': 1, 'mile':0.621371},
        'mile': {'meter':1609.34, 'foot': 5280, 'kilometer':1.60934 , 'mile':1} }

def convertUnits(unitsin, unitsout):
    if unitsin == 'US_Foot':
        unitsin = 'Foot'
    if unitsout == 'US_Foot':
        unitsout = 'Foot'
    unitsin = string.lower(unitsin)
    unitsout = string.lower(unitsout)
    factor = unitmap[unitsin][unitsout]
    return factor
    
