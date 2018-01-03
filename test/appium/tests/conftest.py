from tests import test_data
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


latest_nightly_apk = dict()
latest_nightly_apk['name'] = get_latest_apk()
latest_nightly_apk['url'] = storage + latest_nightly_apk['name']


def pytest_addoption(parser):
    parser.addoption("--build",
                     action="store",
                     default='build_' + latest_nightly_apk['name'],
                     help="Specify build name")
    parser.addoption('--apk',
                     action='store',
                     default=latest_nightly_apk['url'],
                     help='Url or local path to apk')
    parser.addoption('--env',
                     action='store',
                     default='sauce',
                     help='Specify environment: local/sauce')
    parser.addoption('--log',
                     action='store',
                     default=False,
                     help='Display each test step in terminal as plain text: True/False')


def is_master(config):
    return not hasattr(config, 'slaveinput')


def is_uploaded():
    stored_files = SauceClient(sauce_username, sauce_access_key).storage.get_stored_files()
    for i in range(len(stored_files['files'])):
        if stored_files['files'][i]['name'] == test_data.apk_name:
            return True


def pytest_configure(config):

    if config.getoption('log'):
        import logging
        logging.basicConfig(level=logging.INFO)

    test_data.apk_name = ([i for i in [i for i in config.getoption('apk').split('/')
                                       if '.apk' in i]])[0]

    if is_master(config) and config.getoption('env') == 'sauce':
        if not is_uploaded():
            response = requests.get(config.getoption('apk'), stream=True)
            response.raise_for_status()
            file = BytesIO(response.content)
            del response
            requests.post('http://saucelabs.com/rest/v1/storage/'
                          + sauce_username + '/' + test_data.apk_name + '?overwrite=true',
                          auth=(sauce_username, sauce_access_key),
                          data=file,
                          headers={'Content-Type': 'application/octet-stream'})


def pytest_runtest_setup(item):
    test_data.test_name = item.name + '_' + test_data.apk_name
