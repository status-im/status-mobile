from tests import tests_data
import time
import requests
import re
from datetime import datetime
from os import environ
from io import BytesIO
from sauceclient import SauceClient

storage = 'http://artifacts.status.im:8081/artifactory/nightlies-local/'

sauce_username = environ.get('SAUCE_USERNAME')
sauce_access_key = environ.get('SAUCE_ACCESS_KEY')


def get_latest_apk():
    raw_data = requests.request('GET', storage).text
    dates = re.findall("\d{2}-[a-zA-Z]{3}-\d{4} \d{2}:\d{2}", raw_data)
    dates.sort(key=lambda date: datetime.strptime(date, "%d-%b-%Y %H:%M"), reverse=True)
    return re.findall('>(.*k)</a>\s*%s' % dates[0], raw_data)[0]

latest_apk = get_latest_apk()


def pytest_addoption(parser):
    parser.addoption("--build",
                     action="store",
                     default='build_' + time.strftime('%Y_%m_%d_%H_%M'),
                     help="Specify build name")
    parser.addoption('--apk',
                     action='store',
                     default='sauce-storage:' + latest_apk,
                     help='Please provide url or local path to apk')
    parser.addoption('--env',
                     action='store',
                     default='sauce',
                     help='Please specify environment: local/sauce')


def pytest_runtest_setup(item):
    tests_data.name = item.name + '_' + latest_apk


def is_master(config):
    return not hasattr(config, 'slaveinput')


def is_uploaded():
    stored_files = SauceClient(sauce_username, sauce_access_key).storage.get_stored_files()
    for i in range(len(stored_files['files'])):
        if stored_files['files'][i]['name'] == latest_apk:
            return True


def pytest_configure(config):
    import logging
    logging.basicConfig(level=logging.INFO)
    if is_master(config) and config.getoption('env') == 'sauce':
        if not is_uploaded():
            response = requests.get(storage + latest_apk, stream=True)
            response.raise_for_status()
            file = BytesIO(response.content)
            del response
            requests.post('http://saucelabs.com/rest/v1/storage/'
                          + sauce_username + '/' + latest_apk + '?overwrite=true',
                          auth=(sauce_username, sauce_access_key),
                          data=file,
                          headers={'Content-Type': 'application/octet-stream'})
