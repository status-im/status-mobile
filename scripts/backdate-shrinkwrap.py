#!/usr/bin/env python

import json
import subprocess
import copy
from dateutil import parser
from datetime import datetime, timedelta
from distutils.version import StrictVersion

# Set this date for rollback
# min_age = datetime.today() - timedelta(days=7)
min_age = parser.parse('2016-10-22').replace(tzinfo=None)

newest_date = (datetime.utcfromtimestamp(0), '')  # epoch
DEPS = 'dependencies'
VER = 'version'

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

def alter_deps(pkg_obj):
    tmp_pkg = copy.deepcopy(pkg_obj)

    for package, info in tmp_pkg.iteritems():
        date, version = rollback_version(package)
        current_version = info['version']

        if DEPS in tmp_pkg[package]:
            tmp_pkg[package][DEPS] = alter_deps(tmp_pkg[package][DEPS])

        del tmp_pkg[package]['from']
        del tmp_pkg[package]['resolved']
        # if info['version'] != version:  # if use this, many inconsistencies
        try:
            if StrictVersion(current_version) > StrictVersion(version):
                tmp_pkg[package]['version'] = version
                print(package, current_version, version)
        except:
            print('ERR!', package, current_version, version)

    return tmp_pkg


with open('../npm-shrinkwrap.json') as f:
    shrinkwrap = json.load(f)
    shrinkwrap[DEPS] = alter_deps(shrinkwrap[DEPS])
    print(json.dumps(shrinkwrap,indent=4))
# TODO write new file
