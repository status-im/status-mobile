#!/usr/bin/env python

import json
import subprocess
from dateutil import parser
from datetime import datetime, timedelta
from distutils.version import StrictVersion

# Set this date for rollback
min_age = datetime.today() - timedelta(days=7)

newest_date = (datetime.utcfromtimestamp(0), '')  # epoch


def version_info(package):
    ''' Gets all versions from npm package, returns json '''
    jsondata = subprocess.Popen(['npm', 'view', package, 'time'],
                                stdout=subprocess.PIPE).communicate()[0]
    jsondata = jsondata.replace("'", '"')
    for prop in ['modified', 'created']:
        jsondata = jsondata.replace(prop, '"%s"' % (prop))
    return json.loads(jsondata)


def rollback_version(package):
    best_candidate = newest_date
    data = version_info(package)
    for version, tmptime in data.iteritems():
        if version in ['created', 'modified']:
            continue
        date = parser.parse(tmptime).replace(tzinfo=None)
        if date < min_age and date > best_candidate[0]:
            best_candidate = (date, version)
    return best_candidate

with open('../npm-shrinkwrap.json') as f:
    shrinkwrap = json.load(f)
    dependencies = shrinkwrap['dependencies']
    for package, info in dependencies.iteritems():
        date, version = rollback_version(package)
        # if info['version'] != version:  # if use this, many inconsistencies
        try:
            if StrictVersion(info['version']) > StrictVersion(version):
                print(package, info['version'], version)
        except:
            print('ERR!', package, info['version'], version)
# TODO write new file
